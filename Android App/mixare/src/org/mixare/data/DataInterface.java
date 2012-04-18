package org.mixare.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.location.Location;
import android.text.format.Time;
import android.util.Log;

public class DataInterface 
{
	/**
	 * The top level area where the data will be accessed from.
	 */
	public static final String DATA_URL_BASE = "http://nmpapin.heliohost.org/cs4261/";
	
	/**
	 * Returns the distance to the stop denoted by the given id from the given coordinates.
	 */
	public static double distanceToStop(double lat, double lng, int stop)
	{
		String url = DATA_URL_BASE + "distance_to_stop.php?latitude=" + lat + "&longitude=" + lng + "&stop=" + stop;
		
		try 
		{
			JSONObject jo = new JSONObject(getURLContents(url));
			return jo.getDouble("distance");
		} 
		catch (JSONException e) 
		{
			return -1;
		}
	}
	
	/**
	 * Gets all following stops that are denoted as "stations" and their next times.
	 */
	public static JSONArray getFollowingStations(int route, int stop, Time time)
	{
		String url = DATA_URL_BASE + "get_following_stations.php?route=" + route + "&stop=" + stop;
		if(time != null)
		{
			url += "&time=" + time.format("%T");
		}
		
		try 
		{
			return new JSONArray(getURLContents(url));
		} 
		catch (JSONException e) 
		{
			return null;
		}
	}
	
	/**
	 * Gets all following stops and their next times.
	 */
	public static JSONArray getFollowingStops(int route, int stop, Time time)
	{
		String url = DATA_URL_BASE + "get_following_stops.php?route=" + route + "&stop=" + stop;
		if(time != null)
		{
			url += "&time=" + time.format("%T");
		}
		
		try 
		{
			return new JSONArray(getURLContents(url));
		} 
		catch (JSONException e) 
		{
			return null;
		}
	}
	
	/**
	 * Gets a listing of the nearby major stops within a certain distance (default 1km) from the specified coordinates.
	 * 
	 * @param maxDistance the maximum distance in meters.
	 */
	public static JSONArray getNearbyMajorStops(double lat, double lng, double maxDistance)
	{
		String url = DATA_URL_BASE + "get_major_stops_near.php?latitude=" + lat + "&longitude=" + lng;
		if(maxDistance > 0)
		{
			url += "&radius=" + maxDistance;
		}
		
		try
		{
			return new JSONArray(getURLContents(url));
		}
		catch (JSONException e)
		{
			return null;
		}
	}
	
	/**
	 * Returns a list of the routes leaving the given stop after a given time.
	 */
	public static JSONArray getRoutesLeaving(int stop, Time time)
	{
		String url = DATA_URL_BASE + "get_routes_leaving.php?stop=" + stop;
		if(time != null)
		{
			url += "&time=" + time.format("%T");
		}
		
		try 
		{
			return new JSONArray(getURLContents(url));
		} 
		catch (JSONException e) 
		{
			return null;
		}
	}
	
	/**
	 * Returns the coordinates of the given stop in a JSONObject as "latitude" and "longitude".
	 */
	public static JSONObject getStopCoordinates(int stop)
	{
		String url = DATA_URL_BASE + "get_stop_coords.php?stop=" + stop;
		
		try 
		{
			return new JSONObject(getURLContents(url));
		} 
		catch (JSONException e) 
		{
			return null;
		}
	}
	
	/**
	 * Returns whether or not the given stop is a "station".
	 */
	public static boolean isStation(int stop)
	{
		String url = DATA_URL_BASE + "is_station.php?stop=" + stop;
		
		try 
		{
			JSONObject jo = new JSONObject(getURLContents(url));
			return jo.getBoolean("is_station");
		} 
		catch (JSONException e) 
		{
			return false;
		}
	}
	
	/**
	 * Returns the contents of the given url as a string.
	 */
	public static String getURLContents(String url)
	{
		String data = null;
		
		try 
		{
			URL updateURL = new URL(url);
			URLConnection conn = updateURL.openConnection();
	        InputStream is = conn.getInputStream();
	        
	        data = getStreamData(is);
		} 
		catch (MalformedURLException e) 
		{
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		} 
		
		return data;
	}
	
	/**
	 * Returns a map of suggestions for the route for navigation mode.
	 */
	public static Map<String, Location> getRouteSuggestions(String query)
	{
		query = URLEncoder.encode(query);
		
		String url = "http://maps.googleapis.com/maps/api/geocode/json?address=" + query + "&sensor=true";
		
		Map<String, Location> ret = new HashMap<String, Location>();
		
		try
		{
			Log.i("GeocodeSuggestions", "Loading suggestions: " + url);
			JSONObject res = new JSONObject(getURLContents(url));
			JSONArray arr = res.getJSONArray("results");
			
			for(int i = 0; i < arr.length(); i++)
			{
				JSONObject jo = arr.getJSONObject(i);
				String name = jo.getString("formatted_address");
				JSONObject locs = jo.getJSONObject("geometry").getJSONObject("location");
				Location loc = new Location("geocode");
				loc.setLatitude(locs.getDouble("lat"));
				loc.setLongitude(locs.getDouble("lng"));
				
				ret.put(name, loc);
			}
			
			return ret;
		}
		catch (JSONException e) 
		{
			e.printStackTrace();
			//Log.i("GeocodeSuggestions", "Error occured parsing json.", e);
			return ret;
		}
	}
	
	private static String getStreamData(InputStream is)
	{
		BufferedReader reader = new BufferedReader(new InputStreamReader(is), 8 * 1024);
		StringBuilder sb = new StringBuilder();

		try 
		{
			String line;
			while ((line = reader.readLine()) != null) 
			{
				sb.append(line + "\n");
			}
		} 
		catch (IOException e)
		{
			e.printStackTrace();
		} 
		finally 
		{
			try 
			{
				is.close();
			}
			catch (IOException e) 
			{
				e.printStackTrace();
			}
		}
		return sb.toString();
	}
}