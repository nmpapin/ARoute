package org.mixare.routing;

import java.sql.Date;
import java.sql.Time;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Calendar;
import java.util.Queue;

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
	public static final boolean DEBUG_MODE = true;
	
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
	private Stop closestDestinationStop;
	
	private int recursionDepth = 20;
	
	public Queue<TimeStop> tsQueue;
	
	public TimeStopGraph tsg;
	
	public int solutions;
	public int maxSolutions = 1;
	
	public boolean foundWorkingRoute = false;
	
	/**
	 * Whether calculate route was able to find the solution
	 * @return
	 */
	public boolean foundSolution()
	{
		return foundWorkingRoute;
	}
	
	/**
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
		
		TimeStop.clearGraph(); //Clear timestop graph so will make new timestomp
								//May actually be completely unnecessary
		
		dbi = createDBI();
		possibleStartStops = Stop.getStopsNear(startLat, startLng, NEARBY_DISTANCE);
		possibleDestStops = Stop.getStopsNear(destLat, destLng, NEARBY_DISTANCE);
		calculateRoute();
		checkTimeStopGraph();

		dbi.close();
		dbi = null;
	}
	
	public void close()
	{
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
				
				logPrintImportant("dist " + distance(d.lat,d.lng,startLat,startLng));
				logPrintImportant("time " + d.time);
				if(d.isStartStop)
				{
					toRemove.add(i);
				}
				else
				{
					logPrintImportant("Got Previous");
					Edge e = tsg.previousEdges(d).get(0);
					TimeStop p = tsg.previousStop(e);
					RoutePoint rp = routes.get(rb).get(routes.get(rb).size() - 1);
					
					String type = "";
					Integer routeType = Integer.getInteger(e.r.martaID);
					if(routeType == null)
					{
						type = "rail";
					}
					else
					{
						type = "bus";
					}
					
					
					routes.get(rb).add(new RoutePoint
					(
						p.lat, 
						p.lng, 
						type, 
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
	
	/**
	 * Return the closest Destination stop found
	 * 
	 * @return the closest timestop routed to
	 */
	public TimeStop getClosestDestStop()
	{
		ArrayList<TimeStop> stops = tsg.getEndStops();
		TimeStop closest = null;
		double closestDistance = Double.MAX_VALUE;
		
		for(TimeStop t : stops)
		{
			double dist = t.distanceToStop(startLat, startLng);
			if (dist < closestDistance)
			{
				closest = t;
				closestDistance = dist;
			}
		}
		return closest;
	}
	
	/**
	 * Traces a TimeStop back through the TimeStopGraph
	 * 
	 * @param end
	 * @return
	 */
	public TimeStop traceBackToStart(TimeStop end)
	{
		ArrayList<Edge> in = tsg.inEdges.get(end.tStopID);
		if(in == null)
		{
			logPrintImportant("No in edges existed for TS: "+end);
		}
		while (in != null && in.size() != 0)
		{
			end = in.get(0).t1; //get the first in edge
			in = tsg.inEdges.get(end.tStopID);
		}
			
		return end;
	}
	
	public ArrayList<TimeStop> filteredEndStops()
	{
		ArrayList<TimeStop> stops11 = tsg.getEndStops();
		ArrayList<TimeStop> stops12 = new ArrayList<TimeStop>();
		ArrayList<TimeStop> stops13 = new ArrayList<TimeStop>();
		
		for(TimeStop ts1 : stops11)
		{
			if(isPossibleDestStop(ts1))
			{
				stops12.add(ts1);
			}
		}
		
		ArrayList<TimeStop> finalStops = new ArrayList<TimeStop>();
		TimeStop x;
		for(TimeStop ts2 : stops12)
		{
			x = traceBackToStart(ts2);
			Log.i("Filtered Stops",ts2+" traced to "+x);
			
			if (!(ts2.equals(x)))
			{
				stops13.add(ts2);
				Log.i("Filtered Stops", "Determined "+ts2+" is viable endStop");
			}
		}
		
		return stops13;
	}
	
	public boolean checkTimeStopGraph()
	{
		boolean pass = true;
		
		//check destination stops are actually destination stops
		boolean allDests = true;
		if (tsg.getEndStops().size() == 0)
		{
			logPrintImportant("No Destinations Exist");
			return false;
		}
		
		ArrayList<TimeStop> stops = filteredEndStops();
		
		for(TimeStop dest : stops)
		{
			if (isPossibleDestStop(dest))
				verboseLogPrint("TimeStop "+dest+" verified as destination");
			else
			{
				allDests = false;
				pass = false;
				logPrintImportant("TimeStop "+dest+" failed as destination");
			}
		}
		
		if (allDests)
			logPrint("All Destinations Verified");
		
		boolean allStarts = true;
		
		stops = tsg.getStartStops();
		if (stops.size() == 0)
		{
			logPrintImportant("No Starts Exist");
			return false;
		}
		
		for(TimeStop start : stops)
		{
			if (isPossibleDestStop(start))
				verboseLogPrint("TimeStop "+start.tStopID+" verified as destination");
			else
			{
				allDests = false;
				pass = false;
				logPrintImportant("TimeStop "+start.tStopID+" failed as destination");
			}
		}
		if (allStarts)
			logPrint("All Destinations Verified");
		
		boolean traceback = false;
		TimeStop closest = this.getClosestDestStop();
		TimeStop start = traceBackToStart(closest);
		
		if(closest == start)
		{
			logPrintImportant("Traceback stayed the same");
		}
		if(isPossibleDestStop(start))
		{
			traceback = true;
			logPrint("Traceback worked");
		}
		else
		{
			pass = false;
			logPrintImportant("Could not trace back to a start");
		}
		
		logPrintImportant("checking graph passed = "+pass);
		return pass;
	}

	/**
	 * Calculate route between indicated 
	 * 
	 * @return true if successful;
	 */
	public boolean calculateRoute() {
		
		if (tsg != null)
			tsg.reset();
		
		tsg = new TimeStopGraph();
		tsQueue = new LinkedList<TimeStop>();
		
		solutions = 0;
		Stop s;
		
		Time now = new Time(Calendar.getInstance().getTimeInMillis());
		//logPrint(now.toString());
		
		//Enqueue the start stops
		while (solutions < maxSolutions && null != (s = getNextStartStop()))
		{
			//Default width = 5 and depth = 2
			ModBredthFirstSearch(s, 5, now);
		}

		logPrint("BFS roots created");
		
		logPrint("Starting iterations");
		int result = iterateBFSMOD(200, 1); //search up to 200 nodes, queuing 1stop/move
		
		if (result > 0) //successful
		{
			logPrint("Found result after "+result+" iterations");
			foundWorkingRoute = true;
		}
		else
		{
			logPrint("Did not find destination");
			foundWorkingRoute = false;
		}
		return foundWorkingRoute;
	}
	
	/**
	 * Builds to tsg
	 * 
	 * @param origin
	 * @param width numRoutes to search out for each starting node
	 * @param time
	 * @return
	 */
	public TimeStopGraph ModBredthFirstSearch(Stop origin, int width, Time time)
	{
		ArrayList<Route> routes = origin.getRoutesLeaving(time); //get routes servicing
		logPrint("Received "+routes.size()+" routes leaving "+origin+" @ "+timeToString(time));
		
		int count = 0;
		for(Route r : routes)
		{
			if (count >= width)
				break;
			
			ArrayList<TimeStop> stops = r.getFollowingStops(origin.stopid, time);
			logPrint("Received "+stops.size()+" stops after stop "+origin.stopid
								+" on route "+r.routeID+" after "+timeToString(time));
		
			TimeStop ts = ( stops.size()> 0 ? stops.get(0) : null); 
			
			if (ts != null)
			{
				ts.isStartStop = true;
				enqueue(ts);
		
				count++;
			}
			else
				logPrint("Route method getFollowingRoutes "+r+"returned 0 stops following "+origin
								+"at Time: "+time.getHours()+":"+time.getMinutes());
		}
		
		return tsg;
	}
	
	/**
	 * Ensures don't push the same TimeStop
	 * Will set as destination if applicable
	 * 
	 * @param ts TimeStop to add to queue
	 * @return true if ts was a new entry (not queued before)
	 */
	public boolean enqueue(TimeStop ts)
	{
		if (ts.enqueued)
		{
			verboseLogPrint("Did not enqueue again: "+ts);
			return false;
		}
		
		logPrint("enqueued TimeStop: "+ts);
		
		if (isPossibleDestStop(ts))
		{
			ts.enqueued = true;
			ts.isDestStop = true; //double check to be safe
			Log.i("Set Destination Stop", ts.toString());
			
			solutions++;
			
			logPrintImportant("Found Destination Stop: "+ts);
			logPrintImportant("Distance to final destination"+ ts.distanceToStop(destLat, destLng));
			
			return true; //Do not add to queue
		}
		
		ts.enqueued = true;
		tsQueue.add(ts);
		return true;
	}
	
	/**
	 * Pops a TimeStop and sets popped = true;
	 * @return
	 */
	public TimeStop dequeue()
	{
		TimeStop ts = tsQueue.poll();
		ts.dequeued = true;
		return ts;
	}
	
	/**
	 * Modified BFS loop were it searches the next x nodes along the route
	 * (because more likely continuing on same route will take you there faster)
	 * 
	 * For each stop it searches *all* the routes leaving that stop
	 * 
	 * @param maxIterations how many nodes to search before giving up
	 * @param depth how many stops down each route to traverse
	 * 
	 * @return the number of iterations until solution, -iterations if quit early
	 */
	public int iterateBFSMOD(int maxIterations, int depth)
	{
		int iterations = 0;
		while (solutions < maxSolutions && !(tsQueue.isEmpty()))
		{
			TimeStop ts = dequeue();
			//Check if solution - already took care of
			for(Route r : ts.getRoutesLeaving())
			{
				int count = 0;
				ArrayList<TimeStop> stops = r.getFollowingStops(ts); 
				for (TimeStop t2 : stops)
				{
					//TODO: check that breaks inner loop only
						
					if(t2.equals(ts))
						continue;
					
					if(t2 == null || count > depth-1)
						break;
					if (t2 == null || ts == t2)
						continue;
					
					if (enqueue(t2))
					{
						iterations++; //increase iteration for each new enqueue
						tsg.addNewEdge(ts,t2, r);
					}
					
					if (solutions >= maxSolutions)
					{
						logPrint("Found "+solutions+" solutions");
						return iterations;
					}
					if (iterations > maxIterations)
						return -iterations; //quit early
				}
			}
		}
		return iterations;
	}
	
	/**
	 * Top-priority information printed to log with
	 * tag: "MartaRouting important"
	 * 
	 * @param str
	 */
	public static void logPrintImportant(String str)
	{
		Log.i("MartaRouting important", str);
		logPrint(str);
	}
	
	/**
	 * Logs messages with MartaRouting tag so I can filter later
	 * @param str
	 */
	public static void logPrint(String str)
	{
		Log.i("MartaRouting", str);
		verboseLogPrint(str);
	}
	
	public static void verboseLogPrint(String str)
	{
		Log.i("MartaRouting verbose", str);
	}
	
	public boolean isPossibleDestStop(TimeStop ts)
	{
		for (Stop s : possibleDestStops)
		{
			if (ts.isSameStopIgnoreTime(s))
			{
				return true;
			}
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
	
	public String timeToString(Time t)
	{
		return t.getHours()+":"+t.getMinutes();
	}
}
