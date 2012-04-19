package org.mixare.routing;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.mixare.data.DataInterface;

import android.util.Log;

public class Stop
{
	protected int stopid;
	protected double lat, lng;
	protected String name;
	
	static Hashtable<Integer, Boolean> stations = new Hashtable<Integer, Boolean>(600);
	
	public Stop(int stopid, double lat, double lng, String name)
	{
		this.stopid = stopid;
		this.lat = lat;
		this.lng = lng;
		this.name = name;
	}
	
	/**
	 * Constructs Stop object using "" as the name
	 * 
	 * @param stopid
	 * @param lat
	 * @param lng
	 */
	public Stop(int stopid, double lat, double lng)
	{
		this(stopid, lat, lng, "");
	}
	
	/**
	 * Make stop using only stop id and query the rest of the information
	 * 
	 * @param stopid
	 */
	public Stop(int stopid, DataInterface db)
	{
		this.stopid = stopid;
		queryAllStopData(db);
	}
	
	/**
	 * Retrieve all stop information from database
	 */
	public void queryAllStopData(DataInterface db)
	{
		Map<String, Object> m = db.getStopData(stopid);
		
		Log.i("STOPID", "" + stopid);
		Log.i("failure", "" + (m==null));
		
		lat = this.parseStopLat(m);
		lng = this.parseStopLng(m);
		name = this.parseStopName(m);
	}
	
	public boolean equals(Object o)
	{
		if (!(o instanceof Stop))
			return false;
		
		Stop s = (Stop) o;
		return (stopid == s.stopid);
	}
	
	public String toString()
	{
		return "Stop id: +"+stopid+" Lat: "+lat+" Lng: "+lng+" Name: "+name;
	}
	
	
		/**
		 * Returns all stops within
		 * 
		 * @param lat		origin latitude
		 * @param lng		origin longitude
		 * @param distance	distance to search
		 * @return
		 */
		public static ArrayList<Stop> getStopsNear(double lat, double lng, int distance, DataInterface db)
		{			
			List<Map<String, Object>> stopMap = db.getNearbyStops(lat, lng, distance);
			
			try
			{
				return Stop.parseStopList(stopMap);
			} 
			catch (StopException e)
			{
				Log.i("TimeStop", "failed to query nearby stops");
				e.printStackTrace();
			}
			
			return null;
		}
		
		//TODO: make use parseStopMap
		/**
		 * Parse a database query map result into a list of stops
		 * 
		 * @param stopMap
		 * @return an ArrayList of nearby Stops
		 * @throws StopException
		 */
		public static ArrayList<Stop> parseStopList(List<Map<String, Object>> stopMap) throws StopException
		{
			ArrayList<Stop> stops = new ArrayList<Stop>();
			
			for(Map m : stopMap)
			{
				stops.add(parseStopMap(m));
				/*try {
					int id = Integer.parseInt(m.get("stop_id").toString());
					double lat = Double.parseDouble(m.get("latitude").toString());
					double lng = Double.parseDouble(m.get("latitude").toString());
					
					try {
						String name = m.get("name").toString();
						stops.add(new Stop(id, lat, lng, name));
					}
					catch (NullPointerException npe) {
						Log.i("Routing", "Couldn't parse stop name");
						stops.add(new Stop(id,lat,lng));
					}
				}
				catch (NullPointerException npe) {
					throw new StopException();
				}
				catch (NumberFormatException nfe) {
					throw new StopException();
				}*/
			}
			
			return stops;
		}
		
		public static Stop parseStopMap(Map<String, Object> m) throws StopException
		{
			
			try {
				int id = Integer.parseInt(m.get("stop_id").toString());
				double lat = Double.parseDouble(m.get("latitude").toString());
				double lng = Double.parseDouble(m.get("longitude").toString());
				
				try {
					String name = m.get("name").toString();
					Stop s = new Stop(id, lat, lng, name);
					logPrint("Parsed stop: "+s.toString());
					return s;
				}
				catch (NullPointerException npe) {
					logPrintMinor("Couldn't parse stop name");
					Stop s = new Stop(id,lat,lng);
					logPrint("Parsed Stop: "+s.toString());
					return s;
				}
			}
			catch (NullPointerException npe) {
				throw new StopException();
			}
			catch (NumberFormatException nfe) {
				throw new StopException();
			}
		}
		
		public static int parseStopID(Map<String, Object> m) throws StopException
		{			
			try {
				int id = Integer.parseInt(m.get("stop_id").toString());
				
				return id;
			}
			catch (NullPointerException npe) {
				throw new StopException("Missing stop id");
			}
			catch (NumberFormatException nfe) {
				throw new StopException("stop id not number");
			}
		}
		
		public static double parseStopLat(Map<String, Object> m) throws StopException
		{
			
			
			for(Map.Entry<String, Object> e : m.entrySet())
			{
				Log.i("KEY_ENTRY_THINGY", "" + e.getKey() + ": " + e.getValue());
			}
			
			
			try {
				double lat = Double.parseDouble(m.get("latitude").toString());
				
				return lat;
			}
			catch (NullPointerException npe) {
				throw new StopException("Missing stop id");
			}
			catch (NumberFormatException nfe) {
				throw new StopException("stop id not number");
			}
		}
		
		public static double parseStopLng(Map<String, Object> m) throws StopException
		{
			try {
				double lng = Double.parseDouble(m.get("longitude").toString());
				
				return lng;
			}
			catch (NullPointerException npe) {
				throw new StopException("Missing stop id");
			}
			catch (NumberFormatException nfe) {
				throw new StopException("stop id not number");
			}
		}
		
		public static String parseStopName(Map<String, Object> m) throws StopException
		{
			try {
				String name = m.get("name").toString();
				
				return name;
			}
			catch (NullPointerException npe) {
				throw new StopException("Missing stop id");
			}
			catch (NumberFormatException nfe) {
				throw new StopException("stop id not number");
			}
		}
		
		public static void logPrint(String msg)
		{
			//Log.i("Stop", msg);
		}
		
		public static void logPrintMinor(String msg)
		{
			//Log.i("StopMinor", msg);
		}

		public ArrayList<Route> getRoutesLeaving(Time time, DataInterface db)
		{
			return Route.getRoutesLeaving(stopid, time, db);
		}
		
		public boolean isStation(DataInterface db)
		{
			return isStation(stopid, db);
		}
		
		public static boolean isStation(int stopid, DataInterface db)
		{
			if(stations.get(stopid) != null)
				return stations.get(stopid);
			else
			{
				boolean isSta = db.isStation(stopid);
				stations.put(stopid, isSta);
				return isSta;
			}
		}
		
		public double distanceToStop(double fromLat, double fromLng, DataInterface db)
		{
			return distanceToStop(fromLat, fromLng, stopid, db);
		}
		
		public static double distanceToStop(double fromLat, double fromLng, int stopid, DataInterface db)
		{
			return db.distanceToStop(fromLat, fromLng, stopid);
		}
}
