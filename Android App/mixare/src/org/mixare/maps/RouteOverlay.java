package org.mixare.maps;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;

import com.google.android.maps.*;
import java.util.*;

/**
 * An overlay for a complete route
 * 
 * @author Sundeep Ghuman
 *
 */
public class RouteOverlay extends Overlay
{
	private ArrayList<GeoPoint> points = new ArrayList<GeoPoint>();
	ArrayList<RouteSegment> segments = new ArrayList<RouteSegment>();
	private int color;
	
	private MapView mapView;
	
	/**
	 * 
	 * @param mapView the mapview the route will be drawing on
	 * @param color The color that the route should be drawn in
	 */
	public RouteOverlay(MapView mapView, int color)
	{
		this.color = color;
		this.mapView = mapView;
	}
	
	/**
	 * Used when you have all the GeoPoints ready
	 * 
	 * @param mapView the mapview the route will be drawing on
	 * @param color The color that the route should be drawn in
	 * @param points If you already know GeoPoints
	 */
	public RouteOverlay(MapView mapView, int color, ArrayList<GeoPoint> points)
	{
		this(mapView, color);
		this.points = points;
		repopulateSegments();
	}
	
	/**
	 * Receive a new GeoPoint to add to the route
	 * and invalidate the mapview so it knows to redraw itself
	 * @param point
	 */
	public void addGeoPoint(GeoPoint point)
	{
		points.add(point);
		int numPoints = points.size();
		if (numPoints > 1)
		{
			segments.add(new RouteSegment(points.get(numPoints-2), points.get(numPoints-1), color));
			mapView.invalidate(); //indicates mapview needs to be refreshed
		}
		
	}
	
	/**
	 * If geopoints changed instead of incremented,
	 * recalculate all segments
	 */
	public void repopulateSegments()
	{
		if (segments != null)
			segments.clear();
		int numPoints = points.size();
		if (numPoints > 1)
		{
			for (int i = 0; i < numPoints-1; i++)
			{
				segments.add(new RouteSegment(points.get(i), points.get(i+1), color));
			}
		}
	}
	
	@Override
    /**
     * Draws all the segments on the map
     */
    public void draw(Canvas canvas, MapView mapView, boolean shadow)
	{
		for (RouteSegment seg : segments)
		{
			seg.draw(canvas, mapView, shadow); //correct call?
		}
	}
	
	/**
	 * Represents an individual segment in the route
	 * Might not need to extends Overlay since only draws,
	 * but could be useful down the road
	 * 
	 * @author sghuman
	 *
	 */
	private class RouteSegment extends Overlay
	{
		private GeoPoint gp1;
	    private GeoPoint gp2;
	    private int color;
	 
	    public RouteSegment(GeoPoint gp1, GeoPoint gp2, int color)
	    {
	        this.gp1 = gp1;
	        this.gp2 = gp2;
	        this.color = color;
	    }
	    
	    @Override
	    /**
	     * Draws the segment on the map
	     * Connects only with straight lines between the points
	     */
	    public void draw(Canvas canvas, MapView mapView, boolean shadow) {
	        Projection projection = mapView.getProjection();
	        Paint paint = new Paint();
	        Point point = new Point();
	        projection.toPixels(gp1, point);
	        paint.setColor(color);
	        Point point2 = new Point();
	        projection.toPixels(gp2, point2);
	        paint.setStrokeWidth(5);
	        paint.setAlpha(120);
	        canvas.drawLine(point.x, point.y, point2.x, point2.y, paint);
	        super.draw(canvas, mapView, shadow);
	    }
	}
}
