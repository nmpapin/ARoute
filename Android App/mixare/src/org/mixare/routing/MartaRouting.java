package org.mixare.routing;

import java.sql.Date;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Calendar;

import android.content.Context;
import android.util.Log;

import com.google.android.maps.*;
import org.mixare.data.*; //For all database stuff
import org.mixare.routing.TimeStopGraph.Edge;
import org.mixare.*;

import java.util.Stack;

/**
 * This class is responsible for all the MARTA public transit Routing
 * 
 * @author Sundeep Ghuman
 * 
 */
public class MartaRouting
{
	public static boolean USE_DBDATAINTERFACE = true;
	public int NEARBY_DISTANCE = 2000; //TODO: Should be a constant
	
	protected static List<Map<String, Object>> lastQueryList;
	
	public static TimeStopGraph currentGraph;
	
	/**
	 * Change with context change so that always uses correct context
	 * default = MixView.dataView.getContext() aka mixcontext
	 */
	public static Context useContext = MixView.dataView.getContext();
	
	public double startLat;
	public double startLng;
	public double destLat;
	public double destLng;
	public static DataInterface dbi; //null when not used, must always check and open
	
	public int startCounter = 0;
	public int destCounter = 0;
	public final int maxStopTries = 10;
	
	private ArrayList<Stop> possibleStartStops;
	private ArrayList<Stop> possibleDestStops;
	
	private int recursionDepth = 20;
	
	public Stack<TimeStop> tsStack;
	
	public TimeStopGraph tsg;
	
	public int solutions;
	public int maxSolutions = 1;
	
	/**
	 * 
	 * 
	 * @param startLat
	 * @param startLng
	 * @param destLat
	 * @param destLng
	 */
	public MartaRouting(double startLat, double startLng, double destLat,
						double destLng)
	{
		this.startLat = startLat;
		this.startLng = startLng;
		this.destLat = destLat;
		this.destLng = destLng;
		
		TimeStop.clearGraph(); //Clear timestop graph
		
		dbi = createDBI();
		possibleStartStops = Stop.getStopsNear(startLat, startLng, NEARBY_DISTANCE);
		possibleDestStops = Stop.getStopsNear(destLat, destLng, NEARBY_DISTANCE);
		calculateRoute();
		dbi.close();
		dbi = null;
	}
	
	public static MartaRouting MartaRoutingTestShort()
	{
		//Library starting location
        double startLat = 33.775366;
        double startLng = -84.39517;
        
        //Kroger ending location
        double destLat = 33.803186;
        double destLng = -84.41328;
        
        return new MartaRouting(startLat, startLng, destLat, destLng);
	}
	
	/**
	 * Overloaded constructor to take in GeoPoints and convert them to degree coordinates
	 * @param start
	 * @param dest
	 */
	public MartaRouting(GeoPoint start, GeoPoint dest)
	{
		this(start.getLatitudeE6()/1000000, start.getLatitudeE6()/1000000,
				dest.getLatitudeE6()/1000000, dest.getLongitudeE6()/1000000);
	}
	
	//TODO: implement to show only those stops with routes leaving them after certain time
	/*public Stop findStartStop(Time time)
	{
	}*/
	
	/**
	 * Cycles through the possible start stops
	 * 
	 * @return start stop or null if counter exceeds max tries/starts found
	 */
	public Stop getNextStartStop()
	{
		if (startCounter < maxStopTries && startCounter < possibleStartStops.size())
			return possibleStartStops.get(startCounter++); //increments after
		else
			return null;
	}
	
	/**
	 * Cycles through the possible destination stops
	 * 
	 * @return start stop or null if counter exceeds max tries/starts found
	 */
	public Stop getNextDestStop()
	{
		if (destCounter < maxStopTries && destCounter < possibleDestStops.size())
			return possibleStartStops.get(startCounter++); //increments after
		else
			return null;
	}
	
	/**
	 * Handles all databaseinterface creation so can easily
	 * toggle between DBDataInterface and DataInterface
	 * 
	 * @return a DataInterface to use
	 */
	public static DataInterface createDBI()
	{	
		//TODO: if (USE_DBDATAINTERFACE)
		
		return new DBDataInterface(useContext);
	}
	
	/**
	 * Will return the route after it has been calculated
	 * @return
	 */
	public ArrayList<RoutePoint> getRoute()
	{
		int count = 0;
		ArrayList<TimeStop> dests = new ArrayList<TimeStop>();
		ArrayList<Integer> routesBuilding = new ArrayList<Integer>();
		ArrayList<ArrayList<RoutePoint>> routes = new ArrayList<ArrayList<RoutePoint>>();
		for(TimeStop t : tsg.timeNodes)
		{
			if(t.isDestStop)
			{
				dests.add(t);
				routes.add(new ArrayList<RoutePoint>());
				routes.get(count).add(new RoutePoint
				(
					t.lat, 
					t.lng, 
					"walking", 
					"Walk to Destination", 
					"Approximately " + distance(destLat, destLng, t.lat, t.lng) + " kilometers.", 
					t.time, 
					null
				));
				
				
				routesBuilding.add(count++);
			}
		}
		
		while(routesBuilding.size() > 0)
		{
			ArrayList<Integer> toRemove = new ArrayList<Integer>();
			
			for(int i = 0; i < routesBuilding.size(); i++)
			{				
				int rb = routesBuilding.get(i);
				TimeStop d = dests.get(rb);
				
				if(d.isStartStop)
				{
					toRemove.add(i);
				}
				else
				{
					Edge e = tsg.previousEdges(d).get(0);
					TimeStop p = tsg.previousStop(e);
					RoutePoint rp = routes.get(rb).get(routes.get(rb).size() - 1);
					
					routes.get(rb).add(new RoutePoint
					(
						p.lat, 
						p.lng, 
						"bus", 
						"Take Route " + e.r.martaID + " from stop " + p.name + " to " + d.name + ".", 
						"Approximately " + distance(p.lat, p.lng, d.lat, d.lng) + " kilometers.", 
						p.time, 
						rp
					));
					
					dests.set(rb, p);
				}
			}
			
			for(Integer i : toRemove)
			{
				routesBuilding.remove(i);
			}
		}		
		
		return routes.get(0);
	}
	
	
	
	public void calculateRoute() {
		tsg = new TimeStopGraph();
		tsStack = new Stack<TimeStop>();
		
		solutions = 0;
		Stop s;
		
		Time now = new Time(Calendar.getInstance().getTimeInMillis());
		//logPrint(now.toString());
		
		
		while (solutions < maxSolutions && null != (s = getNextStartStop()))
		{
			BredthFirstSearch(s, 5, new Time( Calendar.getInstance().getTimeInMillis()));
		}
		
	}
	
	/**
	 * Builds to tsg
	 * 
	 * @param origin
	 * @param width numRoutes to search
	 * @param time
	 * @return
	 */
	public TimeStopGraph BredthFirstSearch(Stop origin, int width, Time time)
	{
		ArrayList<Route> routes = origin.getRoutesLeaving(time); //get routes servicing
		
		int count = 0;
		for(Route r : routes)
		{
			if (count >= width)
				break;
			
			tsStack.push(r.getFollowingStops(origin.stopid, time).get(0));

			count++;
		}
		
		return tsg;
	}
	
	public void iterateBFS(TimeStop origin, int numRoute)
	{
		
	}
	
	/**
	 * Logs messages with MartaRouting tag so I can filter later
	 * @param str
	 */
	public static void logPrint(String str)
	{
		Log.i("MartaRouting", str);
	}
	
	public boolean isPossibleDestStop(TimeStop ts)
	{
		for (Stop s : possibleDestStops)
		{
			if (ts.isSameStopIgnoreTime(s))
				return true;
		}
		
		return false;
	}
	
	private double distance(double lat, double lng, double lat2, double lng2)
	{
		double cLat = lat2;
		double cLng = lng2;
		final int R = 6371; // Radious of the earth in meters
		
		Double latDistance = Math.toRadians(cLat-lat);
		Double lonDistance = Math.toRadians(cLng-lng);
		Double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) + 
				   Math.cos(Math.toRadians(lat)) * Math.cos(Math.toRadians(cLat)) * 
				   Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
		Double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
		Double distance = R * c;
		
		return distance;
	}
}
