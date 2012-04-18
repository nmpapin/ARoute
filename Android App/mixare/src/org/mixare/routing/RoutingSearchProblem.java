package org.mixare.routing;

import java.util.ArrayList;

import org.mixare.data.DBDataInterface;

import android.content.Context;
import android.location.Location;

public class RoutingSearchProblem 
{
	DBDataInterface db;
	ArrayList<Stop> goals;
	
	public RoutingSearchProblem(Context ctx, Location goal)
	{
		db = new DBDataInterface(ctx);
		
		goals = Stop.getStopsNear(goal.getLatitude(), goal.getLongitude(), 5000);		
	}
	
	public ArrayList<RoutingNode> getSuccessors(TimeStop ts)
	{
		ArrayList<RoutingNode> ret = new ArrayList<RoutingNode>();
		ArrayList<Route> routes = ts.getRoutesLeaving();
		
		for(Route r : routes)
		{
			TimeStop t = r.getFollowingStops(ts).get(1);
			ret.add(new RoutingNode(t, null));
		}
		
		return ret;
	}
	
	public boolean isGoal(TimeStop ts)
	{
		return goals.contains(new Stop(ts.stopid));
	}
}
