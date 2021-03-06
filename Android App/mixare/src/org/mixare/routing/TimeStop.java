package org.mixare.routing;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.mixare.data.DataInterface;

import android.util.Log;

import com.google.android.maps.GeoPoint;

/**
 * 
 * 
 * TODO: Need to modify time to take into account date
 * 		so that next day stops will work correctly
 * @author sghuman
 *
 */
public class TimeStop extends Stop
{
	public int tStopID; //unique designator of "stopid"+"hours"+"mins"
	
	public int stoptimeInMins; //Time of the stop in minutes
	public Time time;
	public boolean visited = false;
	public boolean enqueued = false;
	public boolean dequeued = false;
	
	/* Inherited fields from Stop
		//int stopid;
		//double lat, lng;
		//String name;
	*/
	
	public boolean isStartStop = false;
	public boolean isDestStop = false;
	
	public static int nearbyDistance = 500; //in meters
	
	static Hashtable<Integer, TimeStop> stopGraph = new Hashtable<Integer, TimeStop>(600);
	
	//to help keep track of efficiency
	public static int numTimesDatabaseOpened = 0;
	
	/**
	 * Only way to create a TimeStop to make sure no duplicates
	 * 
	 * @param stopid
	 * @param stoptime
	 * @param latitude
	 * @param longitude
	 * @return
	 */
	public static TimeStop createTimeStop(int stopid, Time stoptime,
											double latitude, double longitude)
	{
		int tstopid = generateTimeStopID(stopid, stoptime);
		if (stopGraph.containsKey(tstopid))
			return stopGraph.get(tstopid);
		else
			return new TimeStop(tstopid, stopid, stoptime, latitude, longitude);
	}

	public Time minsFromTime(int min)
	{
		long ms = this.time.getTime()+min*60*1000;
		return new Time(ms);
	}
	
	/**
	 * Only way to create a TimeStop to make sure no duplicates
	 * 
	 * @param stopid
	 * @param stoptime
	 * @param latitude
	 * @param longitude
	 * @param name
	 * @return
	 */
	public static TimeStop createTimeStop(int stopid, Time stoptime,
											double latitude, double longitude, String name)
	{
		int tstopid = generateTimeStopID(stopid, stoptime);
		if (stopGraph.containsKey(tstopid))
			return stopGraph.get(tstopid);
		else
		{
			return new TimeStop(tstopid, stopid, stoptime, latitude, longitude, name);
		}
	}
	
	public static TimeStop createTimeStop(Stop s, Time stoptime)
	{
		return createTimeStop(s.stopid, stoptime, s.lat, s.lng, s.name);
	}
	
	/**
	 * Must access through generateTimeStop
	 * 
	 * @param tStopID
	 * @param latitude
	 * @param longitude
	 */
	private TimeStop(int tStopID, int stopid, Time stoptime,
					double latitude, double longitude, String stopname)
	{
		super(stopid, latitude, longitude, stopname);
		this.tStopID = tStopID;
		this.stopid = stopid;
		this.stoptimeInMins = stoptime.getHours()*60+stoptime.getMinutes();
		this.time = stoptime;
		
		lat = latitude;
		lng = longitude;
		
		if (!stopGraph.containsKey(tStopID))
			stopGraph.put(tStopID, this);
	}
	
	/**
	 * Must access through generateTimeStop
	 * 
	 * @param tStopID
	 * @param latitude
	 * @param longitude
	 */
	private TimeStop(int tStopID, int stopid, Time stoptime,
					double latitude, double longitude)
	{
		this(tStopID, stopid, stoptime, latitude, longitude, "");
	}
	
	public static void clearGraph()
	{
		stopGraph.clear();
	}
	
	/**
	 * Generate a timestop id in the format of:
	 * 		"stopid"+"hour"+"minute" - concatentation, not addition
	 * 
	 * @param stopid
	 * @param stoptime
	 * @return
	 */
	public static int generateTimeStopID(int stopid, Time stoptime)
	{
		int tstopid = stopid*10000;
		tstopid += stoptime.getHours()*100;
		tstopid += stoptime.getMinutes();
		return tstopid;
	}
	
	public GeoPoint getGeoPoint()
	{
		return new GeoPoint((int) (lat*1e6), (int) (lng*1e6));
	}
	
	public static TimeStop getStop(int tStopID)
	{
		return stopGraph.get(tStopID);
	}
	
	/**
	 * Get stops within distance
	 * 
	 * @param distance
	 * @return
	 */
	public ArrayList<Stop> getStopsNearby(int distance, DataInterface db)
	{
		return super.getStopsNear(lat, lng, distance, db);
	}
	
	/**
	 * 
	 */
	public ArrayList<Route> getRoutesLeaving(DataInterface db)
	{
		return super.getRoutesLeaving(time, db);
	}
	
	public boolean equals(Object o)
	{
		if (! (o instanceof TimeStop))
			return false;
		TimeStop ts = (TimeStop) o;
		
		if (tStopID == ts.tStopID)
			return true;
		else
			return false;
	}
	
	public boolean isSameStopIgnoreTime(Stop s)
	{
		return stopid == s.stopid;
	}
	
	/**
	 * Compares time in minutes
	 * 
	 * @return time difference in minutes
	 * 				negative if second stop after (<)
	 */
	public int compareTime(TimeStop t)
	{
		return stoptimeInMins - t.stoptimeInMins;
	}
	
	
	//TODO: fix times to take in day as well so next day stops work
	public boolean isBefore(TimeStop t)
	{
		//TODO: return time.before(t.time);
		return compareTime(t) < 0;
	}
	
	/**
	 * 
	 */
	//public int compareTime(int tStopID)
	//{
	//	return ..
	//}
}