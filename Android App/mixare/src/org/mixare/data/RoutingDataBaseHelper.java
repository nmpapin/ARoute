package org.mixare.data;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;
import android.util.Log;

import java.sql.Time;

public class RoutingDataBaseHelper extends SQLiteOpenHelper 
{
	//
	// CLASS AND INSTANCE DATA
	//
	//The Android's default system path of your application database.
    private static String DB_PATH = "/data/data/org.mixare/databases/";
 
    private static String DB_NAME = "routing_database";
 
    private SQLiteDatabase mDataBase; 
 
    private final Context mContext;
	
	//
    // CTOR
    //
	public RoutingDataBaseHelper(Context context) 
	{
		super(context, DB_NAME, null, 1);
		mContext = context;
	}

	//
	// Book Keeping
	//
	public void createDataBase() throws IOException
	{
		 
    	boolean dbExist = checkDataBase();
 
    	if(dbExist)
    	{
    		//do nothing - database already exist
    	}
    	else
    	{
    		//By calling this method and empty database will be created into the default system path
            //of your application so we are gonna be able to overwrite that database with our database.
        	this.getReadableDatabase();
 
        	try 
        	{
    			copyDataBase();
    		} 
        	catch (IOException e) 
    		{
        		throw new Error("Error copying database");
        	}
    	}
    }
	
	public void openDataBase() throws SQLException
	{
    	//Open the database
        String myPath = DB_PATH + DB_NAME;
    	mDataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
    }
	
	private void copyDataBase() throws IOException
	{
    	//Open your local db as the input stream
    	InputStream myInput = mContext.getAssets().open(DB_NAME);
 
    	// Path to the just created empty db
    	String outFileName = DB_PATH + DB_NAME;
 
    	//Open the empty db as the output stream
    	OutputStream myOutput = new FileOutputStream(outFileName);
 
    	//transfer bytes from the inputfile to the outputfile
    	byte[] buffer = new byte[1024];
    	int length;
    	while ((length = myInput.read(buffer))>0)
    	{
    		myOutput.write(buffer, 0, length);
    	}
 
    	//Close the streams
    	myOutput.flush();
    	myOutput.close();
    	myInput.close();
    }
	
	private boolean checkDataBase()
	{
    	SQLiteDatabase checkDB = null;
 
    	try
    	{
    		String myPath = DB_PATH + DB_NAME;
    		checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
    	}
    	catch(SQLiteException e){}
 
    	if(checkDB != null)
    	{
    		checkDB.close();
    	}
 
    	return checkDB != null ? true : false;
    }
	
	
	@Override
	public synchronized void close() 
	{
		if(mDataBase != null)
    		mDataBase.close();
    	
		super.close();
	}
 
	@Override
	public void onCreate(SQLiteDatabase db) {}
 
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}
	
	//
	// DB Interface
	//
	public double distanceToStop(double lat, double lng, int stop)
	{
		Cursor cur = mDataBase.rawQuery
		(
				"SELECT latitude, longitude " +
				"FROM stop " +
				"WHERE _id = " + stop +
				";", 
				new String[0]
		);
		
		cur.moveToFirst();
		double d = distance(lat, lng, cur);
		cur.close();
		return d;
	}
	
	private double distance(double lat, double lng, Cursor cur)
	{
		double cLat = cur.getDouble(cur.getColumnIndex("latitude"));
		double cLng = cur.getDouble(cur.getColumnIndex("longitude"));
		final int R = 6371000; // Radious of the earth in meters
		
		Double latDistance = Math.toRadians(cLat-lat);
		Double lonDistance = Math.toRadians(cLng-lng);
		Double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) + 
				   Math.cos(Math.toRadians(lat)) * Math.cos(Math.toRadians(cLat)) * 
				   Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
		Double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
		Double distance = R * c;
		
		return distance;
	}
	
	public List<Map<String, Object>> getRoutesLeaving(int stop, String time)
	{
		List<Map<String, Object>> ret = new ArrayList<Map<String, Object>>();
		
		Cursor route = mDataBase.rawQuery
		(
			"SELECT DISTINCT rv._id, r.marta_id, r.name, rv.direction, rs._id AS route_stop_id " +
			"FROM route AS r " +
			"JOIN route_variation AS rv ON r._id = rv.route_id " +
			"JOIN route_stop AS rs ON rv._id = rs.route_var_id " +
			"JOIN stop AS s ON rs.stop_id = s._id " + 
			"WHERE s._id = " + stop + ";", 
			new String[0]
		);
		
		route.moveToFirst();
		while(route.isAfterLast() == false) 
		{
			Map<String, Object> m = new HashMap<String, Object>();
			m.put("route_id", route.getInt(route.getColumnIndex("_id")));
			m.put("marta_id", route.getString(route.getColumnIndex("marta_id")));
			m.put("name", route.getString(route.getColumnIndex("name")));
			m.put("direction", route.getString(route.getColumnIndex("direction")));
			
            Cursor t = mDataBase.rawQuery
            (
        		"SELECT stop_time " +
    			"FROM route_time " +
    			"WHERE route_stop_id = " + route.getInt(route.getColumnIndex("route_stop_id")) + 
    			" AND stop_time >= '" + time + "' " +
    			"ORDER BY stop_time ASC " +
    			"LIMIT 1;", 
            	new String[0]
            );			
			
            if(t.getCount() < 1)
            {
            	t.close();
            	t = mDataBase.rawQuery
                (
	        		"SELECT stop_time " +
	    			"FROM route_time " +
	    			"WHERE route_stop_id = " + route.getInt(route.getColumnIndex("route_stop_id")) + 
	    			" AND stop_time >= '00:00:00' " +
	    			"ORDER BY stop_time ASC " +
	    			"LIMIT 1;", 
	            	new String[0]
                );
            	
            	if(t.getCount() > 0)
            	{
            		t.moveToFirst();
            		m.put("next_time", Time.valueOf(t.getString(t.getColumnIndex("stop_time"))));
            		ret.add(m);
            	}
            }
            else
            {
            	t.moveToFirst();
        		m.put("next_time", Time.valueOf(t.getString(t.getColumnIndex("stop_time"))));
        		ret.add(m);
            }
            t.close();
            
            route.moveToNext();
        }
		route.close();
		
		return ret;
	}
	
	public List<Map<String, Object>> getFollowingStops(int route, int stop, String time)
	{
		List<Map<String, Object>> ret = new ArrayList<Map<String, Object>>();
		
		Cursor order = mDataBase.rawQuery
		(
			"SELECT route_order " +
			"FROM route_stop AS rs " +
			"JOIN stop AS s ON rs.stop_id = s._id " +
			"WHERE s._id = " + stop +
			" AND rs.route_var_id = " + route + 
			" LIMIT 1;",
			new String[0]
		);
			
		if(order.getCount() < 1)
		{
			return ret;
		}
		
		order.moveToFirst();
		int orderNum = order.getInt(order.getColumnIndex("route_order"));
		order.close();
		
		Cursor stops = mDataBase.rawQuery
		(
			"SELECT rs._id, rs.stop_id, s.name, s.latitude, s.longitude " +
			"FROM route_stop AS rs " +
			"JOIN stop AS s ON rs.stop_id = s._id " +
			"WHERE rs.route_order >= " + orderNum +
			" AND rs.route_var_id = " + route + 
			" ORDER BY rs.route_order ASC;",
			new String[0]
		);
		
		if(stops.getCount() < 2)
		{
			return ret;
		}
		
		stops.moveToFirst();
		int startStopId = stops.getInt(stops.getColumnIndex("_id"));	
		
		Map<String, Object> startData = new HashMap<String, Object>();
		startData.put("stop_id", stops.getInt(stops.getColumnIndex("stop_id")));
		startData.put("name", stops.getString(stops.getColumnIndex("name")));
		startData.put("latitude", stops.getDouble(stops.getColumnIndex("latitude")));
		startData.put("longitude", stops.getDouble(stops.getColumnIndex("longitude")));
		
		stops.moveToNext();
		
		Cursor startT = mDataBase.rawQuery
		(
			"SELECT stop_time " +
			"FROM route_time " +
			"WHERE route_stop_id = " + startStopId + 
			" AND stop_time >= '" + time + "' " +
			"ORDER BY stop_time ASC " +
			"LIMIT 1;",
			new String[0]
		);
		
		if(startT.getCount() < 1)
        {
			startT.close();
			startT = mDataBase.rawQuery
            (
        		"SELECT stop_time " +
    			"FROM route_time " +
    			"WHERE route_stop_id = " + startStopId + 
    			" AND stop_time >= '00:00:00' " +
    			"ORDER BY stop_time ASC " +
    			"LIMIT 1;", 
            	new String[0]
            );
        	
        	if(startT.getCount() < 1)
        	{
        		return ret;
        	}
        }
		
		String startTimeStr;
		startT.moveToFirst();
        startTimeStr = startT.getString(startT.getColumnIndex("stop_time"));
        
        startData.put("time", Time.valueOf(startTimeStr));
        ret.add(startData);
        
        startT.close();
		
        while(stops.isAfterLast() == false) 
		{
        	Map<String, Object> m = new HashMap<String, Object>();
			m.put("stop_id", stops.getInt(stops.getColumnIndex("stop_id")));
			m.put("name", stops.getString(stops.getColumnIndex("name")));
			m.put("latitude", stops.getDouble(stops.getColumnIndex("latitude")));
			m.put("longitude", stops.getDouble(stops.getColumnIndex("longitude")));
        	
        	int cid = stops.getInt(stops.getColumnIndex("_id"));
        	startT = mDataBase.rawQuery
            (
        		"SELECT stop_time " +
    			"FROM route_time " +
    			"WHERE route_stop_id = " + cid + 
    			" AND stop_time >= '" + startTimeStr + "' " +
    			"ORDER BY stop_time ASC " +
    			"LIMIT 1;", 
            	new String[0]
            );
        	
        	if(startT.getCount() < 1)
            {
    			startT.close();
    			startT = mDataBase.rawQuery
                (
            		"SELECT stop_time " +
        			"FROM route_time " +
        			"WHERE route_stop_id = " + cid + 
        			" AND stop_time >= '00:00:00' " +
        			"ORDER BY stop_time ASC " +
        			"LIMIT 1;", 
                	new String[0]
                );
            	
            	if(startT.getCount() < 1)
            	{
            		return ret;
            	}
            }
        	
        	startT.moveToFirst();
            startTimeStr = startT.getString(startT.getColumnIndex("stop_time"));
            startT.close();
            
            m.put("time", Time.valueOf(startTimeStr));
            ret.add(m);
            
            stops.moveToNext();
		}
        stops.close();
        
		return ret;
	}
	
	public boolean isStation(int stop)
	{
		Cursor number = mDataBase.rawQuery
		(
			"SELECT count(DISTINCT r._id) AS number " +
			"FROM stop AS s " +
			"JOIN route_stop AS rs ON s._id = rs.stop_id " +
			"JOIN route_variation AS rv ON rs.route_var_id = rv._id " +
			"JOIN route AS r ON rv.route_id = r._id " +
			"WHERE s._id = " + stop +
			" AND r.type = 'Train';",
			new String[0]
		);
		
		number.moveToFirst();
		int num = number.getInt(number.getColumnIndex("number"));
		number.close();
		
		return (num > 0);
	}
	
	public Location getStopCoords(int stop)
	{
		Cursor coords = mDataBase.rawQuery
		(
			"SELECT latitude, longitude " +
			"FROM stop " + 
			"WHERE id = " + stop + ";",
			new String[0]
		);
		
		coords.moveToFirst();
		double lat = coords.getDouble(coords.getColumnIndex("latitude"));
		double lng = coords.getDouble(coords.getColumnIndex("longitude"));
		coords.close();
		
		Location loc = new Location("ARouteDataInterface");
		loc.setLatitude(lat);
		loc.setLongitude(lng);
		return loc;
	}
	
	public List<Map<String, Object>> getNearbyMajorStops(double lat, double lng, double maxDistance)
	{
		List<Map<String, Object>> ret = new ArrayList<Map<String, Object>>();
		
		Cursor stops = mDataBase.rawQuery
		(
			"SELECT * " +
			"FROM stop;",
			new String[0]
		);
		
		stops.moveToFirst();
		while(stops.isAfterLast() == false) 
		{
			Map<String, Object> m = new HashMap<String, Object>();
			m.put("stop_id", stops.getInt(stops.getColumnIndex("_id")));
			m.put("name", stops.getString(stops.getColumnIndex("name")));
			m.put("latitude", stops.getDouble(stops.getColumnIndex("latitude")));
			m.put("longitude", stops.getDouble(stops.getColumnIndex("longitude")));
			
			double dist = distance(lat, lng, stops);
			
			if(dist <= maxDistance)
			{
				m.put("distance", dist);
				ret.add(m);
			}
			
			stops.moveToNext();
		}
		stops.close();
		
		Collections.sort(ret, new Comparator<Map<String, Object>>()
		{
			@Override
			public int compare(Map<String, Object> lhs, Map<String, Object> rhs) 
			{
				double d1 = (Double)lhs.get("distance");
				double d2 = (Double)rhs.get("distance");
				
				return Double.compare(d1, d2);
			}
		});
		
		return ret;
	}
	
	public Map<String, Object> getStopData(int stop)
	{
		Map<String, Object> m = new HashMap<String, Object>();
		Cursor stops = mDataBase.rawQuery
		(
			"SELECT * " +
			"FROM stop " +
			"WHERE _id = " + stop + ";",
			new String[0]
		);
		
		stops.moveToFirst();
		m.put("stop_id", stops.getInt(stops.getColumnIndex("_id")));
		m.put("name", stops.getString(stops.getColumnIndex("name")));
		m.put("latitude", stops.getDouble(stops.getColumnIndex("latitude")));
		m.put("longitude", stops.getDouble(stops.getColumnIndex("longitude")));
		stops.close();
		
		return m;
	}
}
