package org.mixare.routing;

import java.sql.Time;
import java.util.Hashtable;

import org.mixare.data.DataInterface;

import com.google.android.maps.GeoPoint;

public class TimeStop
{
	int tStopID; //unique designator of "stopid"+"hours"+"mins"
	int stopid;
	int stoptimeInMins; //Time of the stop in minutes
	Time time;
	double lat, lng;
	
	static Hashtable<Integer, TimeStop> allStops = new Hashtable<Integer, TimeStop>(600);
	
	//to help keep track of efficiency
	public static int numTimesDatabaseOpened = 0;
	
	public static TimeStop generateTimeStop(int stopid, Time stoptime,
											double latitude, double longitude)
	{
		int tstopid = generateTimeStopID(stopid, stoptime);
		if (allStops.containsKey(tstopid))
			return allStops.get(tstopid);
		else
			return new TimeStop(tstopid, stopid, stoptime, latitude, longitude);
	}
	
	/**
	 * Must access through generateTimeStop
	 * 
	 * @param tStopID
	 * @param latitude
	 * @param longitude
	 */
	private TimeStop(int tStopID, int stopid, Time stoptime, double latitude, double longitude)
	{
		this.tStopID = tStopID;
		this.stopid = stopid;
		this.stoptimeInMins = stoptime.getHours()*60+stoptime.getMinutes();
		
		lat = latitude;
		lng = longitude;
		
		if (!allStops.containsKey(tStopID))
			allStops.put(tStopID, this);
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
	
	public static TimeStop getStop(int id)
	{
		return allStops.get(id);
	}
	
	
	//TODO: possible point of slow down if overuse this item
	public static TimeStop getStopsNearby(double lat, double lng)
	{
		DataInterface dbi = MartaRouting.dbi; 
		
		if (dbi == null)
			dbi = MartaRouting.createDBI(); //use this method so later will give DBDataInterface
		
		dbi.close();
		
		return null;
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
}
