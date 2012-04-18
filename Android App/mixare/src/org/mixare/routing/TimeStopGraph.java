package org.mixare.routing;

import java.util.ArrayList;

import android.text.format.Time;

public class TimeStopGraph
{
	public ArrayList<TimeStop> stops;
	public ArrayList<Edge> edges;
	
	public TimeStopGraph() {}
	
	public class Edge
	{
		TimeStop t1;
		TimeStop t2;
		Route r;
		int weight;
		
		public Edge(TimeStop t1, TimeStop t2, Route r)
		{
			this.t1 = t1;
			this.t2 = t2;
			this.r = r;
		}
	}
	
	public class SmartEdge extends Edge
	{
		TimeStop origin;
		Stop dest;
		Route route;
		
		/**
		 * Time in minutes between stops
		 */
		int weight;
		
		
		
		public SmartEdge(TimeStop origin, Stop dest, Route route, Time arrivalTime)
		{
			this.origin = origin;
			this.dest = dest;
			this.route = route;
			weight = origin.stoptimeInMins - (arrivalTime.hour*60+arrivalTime.minute);
		}
	}
}
