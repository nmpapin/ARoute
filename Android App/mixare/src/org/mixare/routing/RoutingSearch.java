package org.mixare.routing;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.PriorityQueue;

import android.util.Log;

public class RoutingSearch 
{
	RoutingSearchProblem prob;
	
	public RoutingSearch(RoutingSearchProblem p)
	{
		prob = p;
	}
	
	public ArrayList<RoutePoint> search()
	{
		prob.db.open();
		PriorityQueue<RoutingNode> pq = new PriorityQueue<RoutingNode>();
		HashSet<TimeStop> closedSet = new HashSet<TimeStop>();
		RoutingNode current;		
		
		for(TimeStop ts : prob.getStartStates())
		{
			pq.add(new RoutingNode(ts, null, null, 0, prob));
		}
		
		current = pq.peek();
		
		while(!prob.isGoal(current.ts) && !pq.isEmpty())
		{
			current = pq.remove();
			
			if(!closedSet.contains(current.ts))
			{
				ArrayList<RoutingNode> succ = prob.getSuccessors(current.ts);
				for(RoutingNode s : succ)
				{
					if(!closedSet.contains(s.ts))
					{
						s.cost = current.cost + s.cost;
						s.parent = current;
						pq.add(s);
					}
				}
				closedSet.add(current.ts);
			}
		}
		
		prob.db.close();
		
		Log.i("RoutingSearch", "Found Likely Route");
		Log.i("RoutingSearch", "Lat:" + current.ts.lat);
		Log.i("RoutingSearch", "Lng:" + current.ts.lng);
		Log.i("RoutingSearch", "" + prob.isGoal(current.ts));
		
		if(prob.isGoal(current.ts))
			return current.getPath();
		else
			return null;
	}
}
