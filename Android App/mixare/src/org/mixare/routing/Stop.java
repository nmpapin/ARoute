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
	
	public Stop(int stopid, double lat, double lng)
	{
		this.stopid = stopid;
		this.lat = lat;
		this.lng = lng;
	}
	
	public boolean equals(Object o)
	{
		if (!(o instanceof Stop))
			return false;
		
		Stop s = (Stop) o;
		return (stopid == s.stopid);
	}
	
	/**
	 * Parse a database query map result into a list of stops
	 * 
	 * @param stopMap
	 * @return an ArrayList of nearby Stops
	 * @throws StopException
	 */
	public static ArrayList<Stop> parseStops(List<Map<String, Object>> stopMap) throws StopException
	{
		ArrayList<Stop> stops = new ArrayList<Stop>();
		
		for(Map m : stopMap)
		{
			try {
				int id = Integer.parseInt(m.get("stop_id").toString());
				double lat = Double.parseDouble(m.get("latitude").toString());
				double lng = Double.parseDouble(m.get("latitude").toString());
				
				stops.add(new Stop(id,lat,lng));
			}
			catch (NullPointerException npe) {
				throw new StopException();
			}
			catch (NumberFormatException nfe) {
				throw new StopException();
			}
		}
		
		return stops;
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
				return Stop.parseStops(stopMap);
			} catch (StopException e)
			{
				Log.i("TimeStop", "failed to query nearby stops");
				e.printStackTrace();
			}
			
			return null;
		}
}
