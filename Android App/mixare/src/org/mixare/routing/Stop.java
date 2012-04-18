package org.mixare.routing;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.mixare.data.DataInterface;

import android.util.Log;

public class Stop
{
	protected int stopid;
	protected double lat, lng;
	protected String name;
	
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
	public Stop(int stopid)
	{
		queryAllStopData();
	}
	
	/**
	 * Retrieve all stop information from database
	 */
	public void queryAllStopData()
	{
		Map<String, Object> m = MartaRouting.dbi.getStopData(stopid);
		
		stopid = this.parseStopID(m);
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
			double lng = Double.parseDouble(m.get("latitude").toString());
			
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
		/**
		 * Returns all stops within
		 * 
		 * @param lat		origin latitude
		 * @param lng		origin longitude
		 * @param distance	distance to search
		 * @return
		 */
		public static ArrayList<Stop> getStopsNear(double lat, double lng, int distance)
		{
			DataInterface dbi = MartaRouting.dbi; 
			boolean createdNewDBI = false;
			
			if (dbi == null)
			{
				dbi = MartaRouting.createDBI(); //use this method so later will give DBDataInterface
				createdNewDBI = true;
			}
			
			List<Map<String, Object>> stopMap = dbi.getNearbyStops(lat, lng, distance);
			
			if (createdNewDBI)
				dbi.close();
			
			try
			{
				return Stop.parseStopList(stopMap);
			} catch (StopException e)
			{
				Log.i("TimeStop", "failed to query nearby stops");
				e.printStackTrace();
			}
			
			return null;
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
				double lng = Double.parseDouble(m.get("latitude").toString());
				
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
			Log.i("Stop", msg);
		}
		
		public static void logPrintMinor(String msg)
		{
			Log.i("StopMinor", msg);
		}
}
