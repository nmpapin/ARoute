package org.mixare.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.mixare.MixView;

import android.content.Context;
import android.database.SQLException;
import android.location.Location;

import java.sql.Time;

public class DBDataInterface extends DataInterface 
{	
	//
	// CLASS AND INSTANCE DATA
	//
	/**
	 * The database used for routing calls.
	 */
	public RoutingDataBaseHelper mDB;
	
	public Context mCtx;
	
	//
	// CTOR
	//
	public DBDataInterface(Context ctx) 
	{
		super(ctx);
		
		mDB = new RoutingDataBaseHelper(ctx);
		
		try 
		{
        	mDB.createDataBase();
        	mDB.openDataBase();
		} 
		catch (IOException ioe) 
		{
			mDB = null;
		}
		catch (SQLException sql) 
		{
			mDB = null;
		}
	}
	
	//
	// INTERFACE METHODS
	//
	@Override
	public void close()
	{
		super.close();
		
		if(mDB != null)
		{
			mDB.close();
			mDB = null;
		}
	}
	
	@Override
	public void open()
	{
		super.open();
		
		if(mDB == null)
		{
			mDB = new RoutingDataBaseHelper(mCtx);
			
			try 
			{
	        	mDB.openDataBase();
			} 
			catch (SQLException sql) 
			{
				mDB = null;
			}
		}
	}
	
	@Override
	public double distanceToStop(double lat, double lng, int stop)
	{
		if(mDB != null)
		{
			return mDB.distanceToStop(lat, lng, stop);
		}
		else
		{
			return super.distanceToStop(lat, lng, stop);
		}
	}
	
	@Override
	public List<Map<String, Object>> getRoutesLeaving(int stop, Time time)
	{
		if(mDB != null)
		{
			return mDB.getRoutesLeaving(stop, time.toString());
		}
		else
		{
			return super.getRoutesLeaving(stop, time);
		}
	}
	
	@Override
	public List<Map<String, Object>> getFollowingStops(int route, int stop, Time time)
	{
		if(mDB != null)
		{
			return mDB.getFollowingStops(route, stop, time.toString());
		}
		else
		{
			return super.getFollowingStops(route, stop, time);
		}
	}
	
	@Override
	public List<Map<String, Object>> getFollowingStations(int route, int stop, Time time)
	{
		if(mDB != null)
		{
			List<Map<String, Object>> ret = new ArrayList<Map<String, Object>>();
			List<Map<String, Object>> maps = mDB.getFollowingStops(route, stop, time.toString());
			for(Map<String, Object> m : maps)
			{
				int sid = (Integer)m.get("stop_id");
				if(mDB.isStation(sid))
				{
					ret.add(m);
				}
			}
			
			return ret;
		}
		else
		{
			return super.getFollowingStops(route, stop, time);
		}
	}
	
	@Override
	public boolean isStation(int stop)
	{
		if(mDB != null)
		{
			return mDB.isStation(stop);
		}
		else
		{
			return super.isStation(stop);
		}
	}
	
	@Override
	public Location getStopCoordinates(int stop)
	{
		if(mDB != null)
		{
			return mDB.getStopCoords(stop);
		}
		else
		{
			return super.getStopCoordinates(stop);
		}
	}
	
	@Override
	public List<Map<String, Object>> getNearbyMajorStops(double lat, double lng, double maxDistance)
	{
		if(mDB != null)
		{
			return mDB.getNearbyMajorStops(lat, lng, maxDistance);
		}
		else
		{
			return super.getNearbyMajorStops(lat, lng, maxDistance);
		}
	}
	
	@Override
	public Map<String, Object> getStopData(int stop)
	{
		if(mDB != null)
		{
			return mDB.getStopData(stop);
		}
		else
		{
			return super.getStopData(stop);
		}
	}
}
