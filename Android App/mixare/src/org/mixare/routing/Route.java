package org.mixare.routing;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Map;

import org.mixare.data.DataInterface;

public class Route
{
	protected int routeID;
	
	public Route(int routeID)
	{
		this.routeID = routeID;
	}
	
	public static ArrayList<TimeStop> getFollowingStops(int routeID, int stopid, Time time)
	{
		DataInterface dbi = MartaRouting.dbi;
		
		MartaRouting.lastQueryList = dbi.getFollowingStops(routeID, stopid, time);
		
		ArrayList<TimeStop> listTimeStops = new ArrayList<TimeStop>();
		
		for (Map<String, Object> m : MartaRouting.lastQueryList)
		{
			TimeStop t = TimeStop.createTimeStop(Stop.parseStopMap(m),
												(Time) m.get("time"));
			listTimeStops.add(t);
		}
		
		return listTimeStops;
		
	}
	
	public static ArrayList<TimeStop> getFollowingStops(int routeID, TimeStop ts)
	{
		return getFollowingStops(routeID, ts.stopid, ts.time);
	}
}
