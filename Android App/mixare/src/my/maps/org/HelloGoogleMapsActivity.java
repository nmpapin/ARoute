package my.maps.org;

import com.google.android.maps.*;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.*;
import android.os.Bundle;
import android.util.Log;

import java.io.File;
import java.util.*;

/**
 * Provides walking direction routing from a given location to a given destination
 * and displays on a Google Map
 * 
 * Intent should take in current location, destination latitude, destination longitude:
 * 
 * putExtra(String name, Parcelable value)
 * 	name: "startLocation" value: last known location (Location object)
 * 	ie locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, true));
 *  may choose to update first, provider is a string (see mixContent for example)
 * 
 * putExtra(String key, double value)
 *  key: destLat value: destination latitude
 *  
 * putExtra(String key, double value)
 *  key: destLong value: destination longitude
 * 
 * Todo revision notes: add current location updating
 *  
 * @author Sundeep Ghuman
 * @version 1.0
 * 
 */
public class HelloGoogleMapsActivity extends MapActivity {
    
	LocationManager locationManager;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Intent startIntent = getIntent();
        
        /* Set zoom capability */
        MapView mapView = (MapView) findViewById(R.id.mapview);
        mapView.setBuiltInZoomControls(true);
        
        /* Create map overlays */
        List<Overlay> mapOverlays = mapView.getOverlays();
        
        //Get destination coordinates from intent - defaults Georgia Tech CoC
        double destLat = startIntent.getDoubleExtra("destLat", 33.777239); 
        double destLong = startIntent.getDoubleExtra("destLong", -84.397649); 
        
        //Start location manager stuff - get from intent instead
//        
//        //Set up location manager - most likely will need to update location before creating map
//        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
//        Criteria criteria = new Criteria();
//        criteria.setAccuracy(Criteria.ACCURACY_FINE);
//        criteria.setAltitudeRequired(false);
//        
//        
//        //Set location provider
//        String provider = locationManager.getBestProvider(criteria, true)
//        try {
//			locationManager.requestLocationUpdates(provider, 0 , 0, lbounce); //lbound = instance of locationlistener in MixContent
//		} catch (Exception e) {
//			Log.d(TAG, "Could not initialize the bounce provider");
//		}
//        
//        Location lastKnownLocation =
//        		locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, true));
        
        // End Location Manager stuff - get from Intent instead
        
		//Replace here:
		Location curLoc = (Location) startIntent.getParcelableExtra("startLocation");
		
        //Should update location first, but calculate and display route
        //route from given location to destination
        Routing router = new Routing();
        ArrayList<GeoPoint> points = (ArrayList<GeoPoint>) router.getRouteGeoPoints(curLoc.getLatitude(), curLoc.getLongitude(), destLat, destLong, "walking");
        
        RouteOverlay route = null;
        if (points != null)
        	route = new RouteOverlay(mapView, Color.GREEN, points);
        mapOverlays.add(route);
        
        /* Draw station at endpoint as bus icon */
        Drawable drawable = this.getResources().getDrawable(R.drawable.bus);	// added bus.gif to
        																		// res/drawable directory
        HelloItemizedOverlay itemizedoverlay = new HelloItemizedOverlay(drawable, this);
        //holds all the overlays
        
        /* Create the Geopoint of the location you want to add, and add it to the overlay */
        GeoPoint point = new GeoPoint((int) (destLat * 1e6), (int) (destLong*1e6)); //specified in microdegrees (lat, long)
        OverlayItem overlayitem = new OverlayItem(point, "Arts Center Tower", "This is where I live!");
        
        itemizedoverlay.addOverlay(overlayitem); //add point to overlay list
        mapOverlays.add(itemizedoverlay); //add overlay to mapview
        
        /* End Drawing Station */
        
//        //***Testing Routing Function
//        Routing router = new Routing();
//        ArrayList<GeoPoint> points = (ArrayList<GeoPoint>) router.getRouteGeoPoints(33.790169, -84.3881, 33.775522, -84.39635, "walking");
//        
//        RouteOverlay route = null;
//        if (points != null)
//        	route = new RouteOverlay(mapView, Color.GREEN, points);
//        mapOverlays.add(route);
//        //***End Routing Function test
        
    }
    
    @Override
    protected boolean isRouteDisplayed() //change if route displayed
    {
        return true;
    }
    
    //*********** Implement location updates here
    //requires implementing locationlistener, adding several methods 
//    
//    //Stop location updates if map not visible
//    @Override
//    protected void onPause() {
//        //remove the listener
//        locationManager.removeUpdates(this);
//        super.onPause();
//    }
// 
//@Override
//    protected void onResume() {
//        //add the listener again
//        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 300000, 5000, this);
//        super.onResume();
//    }
//	//end stop/resume location updates

	//****** End location update code
}