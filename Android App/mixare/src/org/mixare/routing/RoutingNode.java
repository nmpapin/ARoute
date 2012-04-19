package org.mixare.routing;

import java.util.ArrayList;

public class RoutingNode implements Comparable<RoutingNode>
{
	public TimeStop ts;
	public RoutingNode parent;
	public Route route;
	public double cost;
	public RoutingSearchProblem prob;
	
	public RoutingNode(TimeStop t, RoutingNode p, Route r, double c, RoutingSearchProblem rsp)
	{
		ts = t;
		parent = p;
		route = r;
		cost = c;
		prob = rsp;
	}
	
	public boolean equals(Object o)
	{
		if(!(o instanceof RoutingNode))
			return false;
		
		return ts.equals(((RoutingNode)o).ts);
	}
	
	public ArrayList<RoutePoint> getPath()
	{
		ArrayList<RoutePoint> ret = new ArrayList<RoutePoint>();
		ArrayList<RoutingNode> ls = new ArrayList<RoutingNode>();
		RoutingNode current = this;
		
		while(current != null)
		{
			ls.add(current);
			current = current.parent;
		}
		
		ret.add(new RoutePoint
		(
				ls.get(0).ts.lat, 
				ls.get(0).ts.lng, 
				"walking", 
				"Walk to Destination", 
				"Approximately " + RoutingSearchProblem.distance(prob.destLat, prob.destLng, ls.get(0).ts.lat, ls.get(0).ts.lng) + " kilometers.", 
				ls.get(0).ts.time, 
				null
		));
		
		for(int i = 1; i < ls.size(); i++)
		{			
			String type = "";
			try
			{
				Integer routeType = Integer.parseInt(ls.get(i - 1).route.martaID);
				type = "bus";
			}
			catch(Exception e)
			{
				type = "rail";
			}
			
			TimeStop d = ls.get(i - 1).ts;
			TimeStop p = ls.get(i).ts;
			RoutePoint prev = ret.get(ret.size() - 1);
			
			ret.add(new RoutePoint
			(
				p.lat, 
				p.lng, 
				type, 
				"Take " + type + " route " + ls.get(i - 1).route.martaID + " from stop " + p.name + " to " + d.name + ".", 
				"Approximately " + RoutingSearchProblem.distance(p.lat, p.lng, d.lat, d.lng) + " kilometers.", 
				p.time, 
				prev
			));
		}
		
		ArrayList<RoutePoint> res = new ArrayList<RoutePoint>();
		for(RoutePoint r : ret)
		{
			res.add(0, r);
		}
		
		return res;
	}

	@Override
	public int compareTo(RoutingNode another) 
	{
		double myVal = this.cost + prob.heuristic(ts);
		double theirVal = another.cost + prob.heuristic(another.ts);
		
		return Double.compare(myVal, theirVal);
	}
}
