package org.mixare;

import org.mixare.data.DataSource;

import android.util.Log;

public class BusStopMarker extends Marker {

	public static final int MAX_OBJECTS = 25;
	public static final String ON_CLICK = "StopDetailsDialog";
	
	private Route[] mRoutes;
	
	public BusStopMarker(String title, double latitude, double longitude, double altitude, Route[] routes, DataSource datasource) 
	{
		super(title, latitude, longitude, altitude, null, datasource);
		
		mRoutes = routes;
	}

	@Override
	public int getMaxObjects() 
	{
		return MAX_OBJECTS;
	}

	@Override
	public boolean fClick(float x, float y, MixContext ctx, MixState state) {
		Log.i(MixView.TAG, "Marker clicked");
		
		boolean evtHandled = false;

		if (isClickValid(x, y)) 
		{
			evtHandled = state.handleEvent(ctx, ON_CLICK + ":" + title, mRoutes);
		}
		
		return evtHandled;
	}
	
	public static class Route
	{
		private String mName;
		private String mID;
		
		public Route(String id, String name)
		{
			mName = name;
			mID = id;
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
	}	
}