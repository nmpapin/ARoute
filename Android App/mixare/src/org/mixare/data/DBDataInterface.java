package org.mixare.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
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
}
