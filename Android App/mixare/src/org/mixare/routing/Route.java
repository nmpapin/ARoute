package org.mixare.routing;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Map;

import org.mixare.data.DataInterface;

public class Route
{
	protected int routeID;
	protected String martaID;
	
	public Route(int routeID, String martaID)
	{
		this.routeID = routeID;
		this.martaID = martaID;
	}
	
	public Route(int routeID)
	{
		this(routeID, "");
	}
	
	public ArrayList<TimeStop> getFollowingStops(int stopid, Time time)
	{
		return getFollowingStops(routeID, stopid, time);
	}
	
	public ArrayList<TimeStop> getFollowingStops(TimeStop ts)
	{
		return getFollowingStops(routeID, ts);
	}

	public static ArrayList<TimeStop> getFollowingStops(int routeID, TimeStop ts)
	{
		return getFollowingStops(routeID, ts.stopid, ts.time);
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
	
	public ArrayList<TimeStop> getFollowingStations(int stopid, Time time)
	{
		return getFollowingStations(routeID, stopid, time);
	}
	
	public ArrayList<TimeStop> getFollowingStations(TimeStop ts)
	{
		return getFollowingStations(routeID, ts);
	}
	
	public static ArrayList<TimeStop> getFollowingStations(int routeID, TimeStop ts)
	{
		return getFollowingStations(routeID, ts.stopid, ts.time);
	}

	public static ArrayList<TimeStop> getFollowingStations(int routeID, int stopid, Time time)
	{
		DataInterface dbi = MartaRouting.dbi;
		
		MartaRouting.lastQueryList = dbi.getFollowingStations(routeID, stopid, time);
		
		ArrayList<TimeStop> listTimeStops = new ArrayList<TimeStop>();
		
		for (Map<String, Object> m : MartaRouting.lastQueryList)
		{
			TimeStop t = TimeStop.createTimeStop(Stop.parseStopMap(m),
												(Time) m.get("time"));
			listTimeStops.add(t);
		}
		
		return listTimeStops;
		
	}
	
	
	public static ArrayList<Route> getRoutesLeaving(int stopid, Time time)
	{
		MartaRouting.lastQueryList = MartaRouting.dbi.getRoutesLeaving(stopid, time);
		ArrayList<Route> results = new ArrayList<Route>();
		
		for(Map<String, Object> m : MartaRouting.lastQueryList)
		{
			results.add(new Route(parseRouteID(m), parseMartaID(m)));
		}
		
		return results;
	}
	
	public static int parseRouteID(Map<String, Object> m)
	{
		return (Integer) m.get("route_id");
	}
	
	public static String parseMartaID(Map<String, Object> m)
	{
		return m.get("marta_id").toString();
	}
}
