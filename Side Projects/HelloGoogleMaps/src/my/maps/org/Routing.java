package my.maps.org;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

import com.google.android.maps.*;

//imports for JSON
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

//additional imports for getting http
import android.net.http.AndroidHttpClient;
import android.util.Log;

import org.apache.http.*;
import org.apache.http.client.methods.HttpGet;

/**
 * Queries Google Maps for routing directions between two locations
 * All these methods could probably be static, but was
 * uncertain how to implement threading at time of writing
 * 
 * @author Sundeep Ghuman
 *
 */
public class Routing
{
	public boolean requestFinished; //Boolean marker using during development to deal with concurrency
	
	public Routing()
	{
		requestFinished = false;
	}
	
	public ArrayList<GeoPoint> getRouteGeoPoints(double fromLat, double fromLong, double toLat,
			double toLong)
	{
		return getRouteGeoPoints(fromLat, fromLong, toLat, toLong, "walking");
	}
	
	/**
	 * Returns the GeoPoints in a route path
	 * 
	 * @param fromLat
	 * @param fromLong
	 * @param toLat
	 * @param toLong
	 * @param mode "driving", "walking", or "bicycling" - the transportation mode 
	 * @return An ArrayList of GeoPoints along the route
	 */
	public ArrayList<GeoPoint> getRouteGeoPoints(double fromLat, double fromLong,
			double toLat, double toLong, String mode)
	{
		ArrayList<GeoPoint> geopoints = null;

		//remove after implementing concurrency
		requestFinished = false;
		
		try
		// JSON processing
		{
			// initial query
			JSONObject results = getJSONResults(getRoutingURL(fromLat,
					fromLong, toLat, toLong, mode));

			// routesArray contains ALL routes - we have chosen to use only one
			JSONArray routesArray = results.getJSONArray("routes");
			// Grab the first route
			JSONObject route = routesArray.getJSONObject(0);
			// Take all legs from the route
			JSONArray legs = route.getJSONArray("legs");
			// Grab steps (2nd element in legs array)
			
			JSONObject leg = legs.getJSONObject(0);
			//JSONObject stepObject = legs.getJSONObject("steps");
			JSONArray steps = leg.getJSONArray("steps");
			//System.out.println(steps);
			
			geopoints = extractGeoPoints(steps);

		} catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return geopoints;
	}

	/**
	 * Pull out the end location GeoPoints from each step in the route
	 * @param steps
	 * @return an ArrayList of all the GeoPoints in the route
	 * @throws JSONException
	 */
	public ArrayList<GeoPoint> extractGeoPoints(JSONArray steps)
			throws JSONException
	{
		ArrayList<GeoPoint> extracted = new ArrayList<GeoPoint>();
		
		for (int i = 0; i<steps.length(); i++)
		{
			extracted.add(getEndGeoPoint(steps.getJSONObject(i)));
		}
		
		return extracted;
	}

	/**
	 * Extracts an individual end location GeoPoint from a single step in the route
	 * 
	 * @param step
	 * @return
	 * @throws JSONException
	 */
	public GeoPoint getEndGeoPoint(JSONObject step) throws JSONException
	{
		JSONObject endPoint = step.getJSONObject("end_location");
		double lat = endPoint.getDouble("lat");
		double longitude = endPoint.getDouble("lng");

		return new GeoPoint((int) (lat * 1e6), (int) (longitude * 1e6));
	}
	
	/**
	 * Creates the url to query google maps for the routing output
	 * 
	 * @param fromLat
	 * @param fromLong
	 * @param toLat
	 * @param toLong
	 * @param mode
	 * @return the url string for the Google Maps API routing request
	 */
	public static String getRoutingURL(double fromLat, double fromLong, double toLat,
						double toLong, String mode)
	{
		StringBuilder url = new StringBuilder();
		
		url.append("http://maps.googleapis.com/maps/api/directions/json?"); //return json output
		
		/* Set origin location */
		url.append("origin=");
		url.append(fromLat);
		url.append(",");
		url.append(fromLong);
		
		/* Set destination */
		url.append("&destination="+toLat+","+toLong);
		
		/* Set traveling mode if valid input */
		if (mode.equals("driving") || mode.equals("walking") || mode.equals("bicycling"))
			url.append("&mode="+mode);
		// otherwise leaves blanks and defaults to driving directions
		
		/* Set sensor mode true since returning to mobile device */
		url.append("&sensor=true");
		
		return url.toString();
	}
	
	public JSONObject getJSONResults(String url) throws JSONException
	{
		//return new JSONObject(getHTTPResponse(url)); //old version
		
		try 
		{
			URL updateURL = new URL(url);
			URLConnection conn = updateURL.openConnection();
	        InputStream is = conn.getInputStream();
	        
			return new JSONObject(getStreamData(is));
		} 
		catch (MalformedURLException e) 
		{
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		} 
		catch (JSONException e) 
		{
			e.printStackTrace();
		}
		
		return null;
	}
	
	//New methods using url connection
	public String getStreamData(InputStream is)
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
	
	//Decided to skip using http client and replace with simply url connection
	/*
	public String getHTTPResponse(String url)
	{
		final AndroidHttpClient client = AndroidHttpClient.newInstance("Android");
	    final HttpGet getRequest = new HttpGet(url);

	    try {
	        HttpResponse response = client.execute(getRequest);
	        final int statusCode = response.getStatusLine().getStatusCode();
	        if (statusCode != HttpStatus.SC_OK) { 
	            Log.w("Routing Request", "Error " + statusCode + " while retrieving directions from " + url); 
	            return null;
	        }
	        
	        final HttpEntity entity = response.getEntity();
	        if (entity != null) {
	            InputStream inputStream = null;
	            try {
	                inputStream = entity.getContent();
	                
	                return getHttpInputString(inputStream); //convert inputStream to string and return
	            }
	            finally {
	                if (inputStream != null)
	                {
	                    inputStream.close();  
	                }
	                entity.consumeContent();
	            }
	        }
	    } catch (Exception e) {
	        // Could provide a more explicit error message for IOException or IllegalStateException
	        getRequest.abort();
	        Log.w("Routing request", "Error while retrieving directions from " + url);
	    } finally {
	        if (client != null) {
	            client.close();
	        }
	    }
	    return null;
	}
	
	public String getHttpInputString(InputStream is) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is), 8 * 1024);
		StringBuilder sb = new StringBuilder();

		try {
			String line;
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		requestFinished = true; //delete later
		return sb.toString();
	} */
	
	// Doesn't work - delete later
	/*
	public ArrayList<GeoPoint> testGetGeopointsFromJSONFile(File f)
	{
		ArrayList<GeoPoint> geopoints = null;
		
		try
		// JSON processing
		{
			// initial query
			JSONObject results = new JSONObject(getHttpInputString((InputStream) new FileInputStream(f)));

			// routesArray contains ALL routes - we have chosen to use only one
			JSONArray routesArray = results.getJSONArray("routes");
			// Grab the first route
			JSONObject route = routesArray.getJSONObject(0);
			// Take all legs from the route
			JSONArray legs = route.getJSONArray("legs");
			// Grab steps (2nd element in legs array)
			
			JSONObject leg = legs.getJSONObject(0);
			//JSONObject stepObject = legs.getJSONObject("steps");
			JSONArray steps = leg.getJSONArray("steps");
			//System.out.println(steps);
			
			geopoints = extractGeoPoints(steps);

		} catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return geopoints;
	}
	*/
	
}
