package my.maps.org;

import com.google.android.maps.*;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import java.io.File;
import java.util.*;

public class HelloGoogleMapsActivity extends MapActivity {
    
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        /* Set zoom capability */
        MapView mapView = (MapView) findViewById(R.id.mapview);
        mapView.setBuiltInZoomControls(true);
        
        /* Create map overlays */
        List<Overlay> mapOverlays = mapView.getOverlays();
        Drawable drawable = this.getResources().getDrawable(R.drawable.bus);	// added bus.gif to
        																		// res/drawable directory
        HelloItemizedOverlay itemizedoverlay = new HelloItemizedOverlay(drawable, this);
        //holds all the overlays
        
        /* Create the Geopoint of the location you want to add, and add it to the overlay */
        GeoPoint point = new GeoPoint(33789248, -84387864); //specified in microdegrees (lat, long)
        OverlayItem overlayitem = new OverlayItem(point, "Arts Center Tower", "This is where I live!");
        
        itemizedoverlay.addOverlay(overlayitem); //add point to overlay list
        mapOverlays.add(itemizedoverlay); //add overlay to mapview
        
        //Testing Routing Function
        Routing router = new Routing();
        ArrayList<GeoPoint> points = (ArrayList<GeoPoint>) router.getRouteGeoPoints(33.790169, -84.3881, 33.775522, -84.39635, "walking");
        
        RouteOverlay route = null;
        if (points != null)
        	route = new RouteOverlay(mapView, Color.GREEN, points);
        mapOverlays.add(route);
        //End Routing Function test
    }
    
    @Override
    protected boolean isRouteDisplayed() //change if route displayed
    {
        return true;
    }
}