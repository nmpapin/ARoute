package org.mixare.data;

import java.io.IOException;

import android.content.Context;

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
	public void close()
	{
		super.close();
		
		if(mDB != null)
		{
			mDB.close();
			mDB = null;
		}
	}
}
