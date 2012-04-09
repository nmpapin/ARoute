package org.mixare.routing;

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

import org.mixare.R;
import org.mixare.maps.*;

public class RouteActivity extends MapActivity {
    
	LocationManager locationManager;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.walkingmap);
        Intent startIntent = getIntent();
        
        /* Set zoom capability */
        MapView mapView = (MapView) findViewById(R.id.mapview);
        mapView.setBuiltInZoomControls(true);
        
        /* Create map overlays */
        List<Overlay> mapOverlays = mapView.getOverlays();
        
        //Library starting location
        double startLat = 33.775366;
        double startLng = -84.39517;
        
        //Kroger ending location
        double destLat = 33.803186;
        double destLng = -84.41328;
        
        //Bus locations
        double bus1Lat = 33.781197;
        double bus1Lng = -84.398003;
        double bus2Lat = 33.80031;
        double bus2Lng = -84.415716;
        
        
        //Leg1***********************************
        Routing router = new Routing();
        ArrayList<GeoPoint> points = (ArrayList<GeoPoint>) router.getRouteGeoPoints(startLat, startLng, bus1Lat, bus1Lng, "walking");
        
        RouteOverlay walkToBus = new RouteOverlay(mapView, Color.GREEN, points);
        mapOverlays.add(walkToBus);
        
        /* Draw station at endpoint as bus icon */
        Drawable busdrawable = this.getResources().getDrawable(R.drawable.bus);	// added bus.gif to
        																		// res/drawable directory
        HelloItemizedOverlay busoverlay = new HelloItemizedOverlay(busdrawable, this);
        
        /* Walking icons */
        Drawable walkingdrawable = this.getResources().getDrawable(R.drawable.walkingman);	// added bus.gif to
		// res/drawable directory
        HelloItemizedOverlay walkingoverlay = new HelloItemizedOverlay(walkingdrawable, this);
        
        /* Star icons */
        Drawable stardrawable = this.getResources().getDrawable(R.drawable.star);	// added bus.gif to
		// res/drawable directory
        HelloItemizedOverlay staroverlay = new HelloItemizedOverlay(walkingdrawable, this);
        
        /* Create the Geopoint of the location you want to add, and add it to the overlay */
        //start walking
        GeoPoint point = new GeoPoint((int) (startLat * 1e6), (int) (startLng*1e6)); //specified in microdegrees (lat, long)
        OverlayItem overlayitem1 = new OverlayItem(point, "Walk to 10th St NW@Atlantic Dr NW Station", "0.7 miles: approximately 13 minutes");
        walkingoverlay.addOverlay(overlayitem1); //add point to overlay list
        
        //enter bus route
        point = new GeoPoint((int) (bus1Lat * 1e6), (int) (bus1Lng*1e6)); //specified in microdegrees (lat, long)
        OverlayItem overlayitem2 = new OverlayItem(point, "Depart 10th St NW@Atlantic Dr NW Station", "6:28am: Howell Mill Rd/Cumberland - Direction: 12 Cumberland Via Northside");
        busoverlay.addOverlay(overlayitem2); //add point to overlay list
        
        //Leg2************************************
        String[] waypoints = {"33.781502,-84.41328", "33.782928,-84.413538", "33.783856,-84.412079", "33.790347,-84.411821", "33.79641,-84.415941"};
        ArrayList<GeoPoint> points2 = (ArrayList<GeoPoint>) router.getRouteWithWaypoints(bus1Lat, bus1Lng, bus2Lat, bus2Lng, "driving", waypoints);
        
        RouteOverlay BusToBus = new RouteOverlay(mapView, Color.GREEN, points2);
        mapOverlays.add(BusToBus);
        
        //exit bus route
        point = new GeoPoint((int) (bus2Lat * 1e6), (int) (bus2Lng*1e6)); //specified in microdegrees (lat, long)
        OverlayItem overlayitem3 = new OverlayItem(point, "6:41am:	Arrive Howell Mill Rd NW@Bellemeade Ave NW", "Walk to destination - 0.2 miles: approximately 4 minutes");
        walkingoverlay.addOverlay(overlayitem3); //add point to overlay list
        
        //Leg3***********************************
        ArrayList<GeoPoint> points3 = (ArrayList<GeoPoint>) router.getRouteGeoPoints(bus2Lat, bus2Lng, destLat, destLng, "walking");
        
        RouteOverlay walkToDest = new RouteOverlay(mapView, Color.GREEN, points3);
        mapOverlays.add(walkToDest);
        
        //exit bus route
        point = new GeoPoint((int) (destLat * 1e6), (int) (destLng*1e6)); //specified in microdegrees (lat, long)
        OverlayItem overlayitem4 = new OverlayItem(point, "6:45am:	Arrive 1715 Howell Mill Road Northwest, Atlanta, GA 30318", "Travel Time: 29 minutes");
        staroverlay.addOverlay(overlayitem4); //add point to overlay list
        
        //Add all itemized overlays
        mapOverlays.add(busoverlay); //add overlay to mapview
        mapOverlays.add(walkingoverlay); //add overlay to mapview
        mapOverlays.add(staroverlay);
        /* End Drawing Station */
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
