package org.mixare.routing;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Calendar;

import org.mixare.data.DBDataInterface;

import android.content.Context;
import android.location.Location;

public class RoutingSearchProblem 
{
	public DBDataInterface db;
	public ArrayList<Stop> goals;
	public ArrayList<TimeStop> starts;
	
	public double destLat;
	public double destLng;
	
	public RoutingSearchProblem(Context ctx, Location goal, Location start)
	{
		db = new DBDataInterface(ctx);
		
		destLat = goal.getLatitude();
		destLng = goal.getLongitude();
		
		goals = Stop.getStopsNear(goal.getLatitude(), goal.getLongitude(), 1000, db);
		starts = new ArrayList<TimeStop>();
		
		Time now = new Time(Calendar.getInstance().getTimeInMillis());
		ArrayList<Stop> stops = Stop.getStopsNear(start.getLatitude(), start.getLongitude(), 1000, db);
		for(Stop s : stops)
		{
			starts.add(TimeStop.createTimeStop(s, now));
		}
	}
	
	public ArrayList<TimeStop> getStartStates()
	{
		return starts;
	}
	
	public ArrayList<RoutingNode> getSuccessors(TimeStop ts)
	{
		ArrayList<RoutingNode> ret = new ArrayList<RoutingNode>();
		ArrayList<Route> routes = ts.getRoutesLeaving(db);
		
		for(Route r : routes)
		{
			ArrayList<TimeStop> tList = r.getFollowingStops(ts, db);
			if(tList.size() > 1)
			{
				for(int i = 1; i < tList.size(); i++)
				{
					TimeStop t = tList.get(i);
					ret.add(new RoutingNode(t, null, r, distance(ts.lat, ts.lng, t.lat, t.lng), this));
				}
			}
		}
		
		return ret;
	}
	
	public boolean isGoal(TimeStop ts)
	{
		for(Stop s : goals)
		{
			if(s.stopid == ts.stopid)
				return true;
		}
		
		return false;
	}
	
	public double heuristic(TimeStop ts)
	{
		return distance(ts.lat, ts.lng, destLat, destLng);
	}
	
	public static double distance(double lat, double lng, double lat2, double lng2)
	{
		double cLat = lat2;
		double cLng = lng2;
		final int R = 6371; // Radious of the earth in meters
		
		Double latDistance = Math.toRadians(cLat-lat);
		Double lonDistance = Math.toRadians(cLng-lng);
		Double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) + 
				   Math.cos(Math.toRadians(lat)) * Math.cos(Math.toRadians(cLat)) * 
				   Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
		Double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
		Double distance = R * c;
		
		return distance;
	}
}
