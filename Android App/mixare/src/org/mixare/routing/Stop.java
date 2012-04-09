package org.mixare.routing;

import java.util.Hashtable;

import com.google.android.maps.GeoPoint;

public class Stop
{
	int id;
	double lat, lng;
	static Hashtable<Integer, Stop> allStops = new Hashtable<Integer, Stop>(600);
	
	public Stop(int id, double latitude, double longitude)
	{
		this.id = id;
		lat = latitude;
		lng = longitude;
		
		if (!allStops.containsKey(id))
			allStops.put(id, this);
	}
	
	public GeoPoint getGeoPoint()
	{
		return new GeoPoint((int) (lat*1e6), (int) (lng*1e6));
	}
	
	public static Stop getStop(int id)
	{
		return allStops.get(id);
	}
	
	public static Stop getStopsNearby(double lat, double lng)
	{
		return null;
	}
}
