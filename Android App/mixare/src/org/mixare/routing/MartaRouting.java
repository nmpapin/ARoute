package org.mixare.routing;

import java.sql.Time;

import android.content.Context;

import com.google.android.maps.*;
import org.mixare.data.*; //For all database stuff
import org.mixare.*;

/**
 * This class is responsible for all the MARTA public transit Routing
 * 
 * @author Sundeep Ghuman
 * 
 */
public class MartaRouting
{
	public static boolean USE_DBDATAINTERFACE = true;
	
	/**
	 * Change with context change so that always uses correct context
	 * default = MixView.dataView.getContext() aka mixcontext
	 */
	public static Context useContext = MixView.dataView.getContext();
	
	public double startLat;
	public double startLng;
	public double destLat;
	public double destLng;
	public static DataInterface dbi; //null when not used, must always check and open
	
	public MartaRouting(double startLat, double startLng, double destLat,
						double destLng)
	{
		this.startLng = startLng;
		this.startLat = startLat;
		this.destLng = destLng;
		this.destLat = destLat;
	}
	
	/**
	 * Overloaded constructor to take in GeoPoints and convert them to degree coordinates
	 * @param start
	 * @param dest
	 */
	public MartaRouting(GeoPoint start, GeoPoint dest)
	{
		this(start.getLatitudeE6()/1000000, start.getLatitudeE6()/1000000,
				dest.getLatitudeE6()/1000000, dest.getLongitudeE6()/1000000);
	}
	
	/*public TimeStop findStartStop(Time time)
	{
		
	}*/
	
	/**
	 * Handles all databaseinterface creation so can easily
	 * toggle between DBDataInterface and DataInterface
	 * 
	 * @return a DataInterface to use
	 */
	public static DataInterface createDBI()
	{
		//TODO: if (USE_DBDATAINTERFACE)
		
		return new DataInterface(useContext);
	}
}
