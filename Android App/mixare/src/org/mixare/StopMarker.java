package org.mixare;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mixare.data.DataSource;

import android.util.Log;

public class StopMarker extends Marker {

	public static final int MAX_OBJECTS = 25;
	public static final String StopDetails = "StopDetailsDialog";
	
	private Route[] mRoutes;
	private int mStopID;
	
	public StopMarker(int id, String title, double latitude, double longitude, double altitude, Route[] routes, DataSource datasource) 
	{
		super(title, latitude, longitude, altitude, null, datasource);
		
		mRoutes = routes;
		mStopID = id;
	}

	@Override
	public int getMaxObjects() 
	{
		return MAX_OBJECTS;
	}

	public int getStopID()
	{
		return mStopID;
	}
	
	public Route[] getRoutes()
	{
		return mRoutes;
	}
	
	public List<Map<String, ?>> getRouteList()
	{
		List<Map<String, ?>> maps = new ArrayList<Map<String, ?>>();
		for(Route r : mRoutes)
		{
			maps.add(r.getDataMap());
		}
		
		return maps;
	}
	
	public List<List<Map<String, ?>>> getRouteSubdataList()
	{
		List<List<Map<String, ?>>> maps = new ArrayList<List<Map<String, ?>>>();
		for(Route r : mRoutes)
		{
			maps.add(r.getVariationListMap());
		}
		
		return maps;
	}
	
	@Override
	public boolean fClick(float x, float y, MixContext ctx, MixState state) {		
		boolean evtHandled = false;

		if (isClickValid(x, y)) 
		{
			evtHandled = state.handleEvent(ctx, StopDetails, this);
		}
		
		return evtHandled;
	}
	
	public static class Route
	{
		private String mName;
		private String mID;
		private RouteVariation[] mVariations;
		
		public Route(String id, String name, RouteVariation[] variations)
		{
			mName = name;
			mID = id;
			mVariations = variations;
		}
		
		public String getName()
		{
			return mName;
		}
		
		public String getID()
		{
			return mID;
		}
		
		public String toString()
		{
			return mID + "-" + mName;
		}
		
		public Map<String, ?> getDataMap()
		{
			Map<String, Object> data = new HashMap<String, Object>();
			data.put("id", mID);
			data.put("name", mName);
			
			return data;
		}
		
		public List<Map<String, ?>> getVariationListMap()
		{
			List<Map<String, ?>> maps = new ArrayList<Map<String, ?>>();
			for(RouteVariation rv : mVariations)
			{
				maps.add(rv.getDataMap());
			}
			
			return maps;
		}
	}
	
	public static class RouteVariation
	{
		private String mName;
		private int mID;
		private int mDirection;
		
		public RouteVariation(int id, String name, int direction)
		{
			mID = id;
			mName = name;
			mDirection = direction;
		}
		
		public String getDirectionString()
		{
			String s = "";
			switch(mDirection)
			{
			case 0:
				s = "Southbound";
				break;
			case 1:
				s = "Northbound";
				break;
			case 2:
				s = "Eastbound";
				break;
			case 3:
				s = "Westbound";
				break;
			}
			return s;
		}
		
		public Map<String, ?> getDataMap()
		{
			Map<String, Object> data = new HashMap<String, Object>();
			data.put("id", mID);
			data.put("name", mName);
			data.put("direction", getDirectionString());
			data.put("times", "Loading...");
			return data;
		}
	}
}