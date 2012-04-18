package org.mixare.routing;

import java.util.ArrayList;

public class RoutingNode 
{
	public TimeStop ts;
	public RoutingNode parent;
	
	public RoutingNode(TimeStop t, RoutingNode p)
	{
		ts = t;
		parent = p;
	}
	
	public boolean equals(Object o)
	{
		if(!(o instanceof RoutingNode))
			return false;
		
		return ts.equals(((RoutingNode)o).ts);
	}
	
	public ArrayList<TimeStop> getPath()
	{
		if(parent == null)
		{
			ArrayList<TimeStop> ret = new ArrayList<TimeStop>();
			ret.add(ts);
			return ret;
		}
		else
		{
			ArrayList<TimeStop> ret = parent.getPath();
			ret.add(ts);
			return ret;
		}
	}
}
