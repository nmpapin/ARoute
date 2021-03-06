package org.mixare.routing;

import java.sql.Time;

import org.mixare.MixView;
import org.mixare.R;
import org.mixare.maps.HelloItemizedOverlay;

import android.graphics.drawable.Drawable;
import android.util.Log;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.OverlayItem;

public class RoutePoint
{
	public GeoPoint location;
	
	/**
	 * Either:
	 * 		start
	 * 		walking
	 * 		bus
	 * 		rail
	 * 		destination
	 * 
	 * Based on value determine what overlay to add the RoutePoint to
	 */
	public String type;
	
	/**
	 * Title for map overlay
	 */
	public String title;
	
	/**
	 * Information to be display under overlay
	 */
	public String snippet;
	
	/**
	 * 
	 */
	public Time arrivalTime;
	
	/**
	 * 
	 */
	public RoutePoint nextRoutePoint;
	
	
	
	public RoutePoint(GeoPoint location, String type, String title,
						String snippet, Time arrivalTime, RoutePoint nextRoutePoint)
	{
		super();
		this.location = location;
		this.type = type;
		this.title = title;
		this.snippet = snippet;
		this.arrivalTime = arrivalTime;
		this.nextRoutePoint = nextRoutePoint;
	}
	
	public RoutePoint(double lat, double lng, String type, String title,
			String snippet, Time arrivalTime, RoutePoint nextRoutePoint)
	{
		this(new GeoPoint((int) (lat * 1e6), (int) (lng*1e6)), type, title, snippet, arrivalTime, nextRoutePoint);
	}

	//FIX ME
	//Use all data and getMarker to create the overlay item for you
	public OverlayItem createOverlayItem()
	{
		OverlayItem rpOverlay = new OverlayItem(location, title, snippet);
    	rpOverlay.setMarker(getMarker());
    	
    	return rpOverlay;
	}
	
	//Based on "type"
	public Drawable getMarker()
	{
		Drawable walkingdrawable = RouteActivity.mCtx.getResources().getDrawable(R.drawable.walkingman);
        Drawable busdrawable = RouteActivity.mCtx.getResources().getDrawable(R.drawable.bus);
		
        Drawable ret = type.equals("walking") ? walkingdrawable : busdrawable;
        ret.setBounds(0, 0, ret.getIntrinsicWidth(), ret.getIntrinsicHeight());
        
        Log.i("Markers", ret == null ? "null" : ret.toString());
        
		return ret;
	}
}
