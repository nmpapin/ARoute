package org.mixare.routing;

import java.sql.Time;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.util.Log;

import com.google.android.maps.*;
import org.mixare.data.*; //For all database stuff
import org.mixare.*;

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
		
		return new DataInterface(useContext);
	}
	
	/**
	 * Will return the route after it has been calculated
	 * @return
	 */
	public ArrayList<RoutePoint> getRoute()
	{
		return null;
	}
	
	/**
	 * Logs messages with MartaRouting tag so I can filter later
	 * @param str
	 */
	public static void logPrint(String str)
	{
		Log.i("MartaRouting", str);
	}
	
}
