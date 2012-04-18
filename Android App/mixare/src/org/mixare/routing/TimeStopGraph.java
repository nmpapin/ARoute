package org.mixare.routing;

import java.util.ArrayList;
import java.util.Hashtable;

import android.text.format.Time;

public class TimeStopGraph
{
	public ArrayList<TimeStop> timeNodes;
	public ArrayList<Edge> timeEdges;
	
	/**
	 * Key = int tStopID
	 * Value = ArrayList<Edge> of outgoing edges
	 */
	public Hashtable<Integer, ArrayList<Edge>> outEdges = new Hashtable<Integer, ArrayList<Edge>>();
	public Hashtable<Integer, ArrayList<Edge>> inEdges = new Hashtable<Integer, ArrayList<Edge>>();
	
	public TimeStopGraph()
	{
		timeNodes = new ArrayList<TimeStop>();
		timeEdges = new ArrayList<Edge>();
	}
	
	public void reset()
	{
		for(TimeStop ts : timeNodes)
		{
			ts.enqueued = false;
			ts.dequeued = false;
			ts.visited = false;
			ts.isDestStop = false;
			ts.isStartStop = false;
		}
	}
	
	public ArrayList<TimeStop> getEndStops()
	{
		ArrayList<TimeStop> ends = new ArrayList<TimeStop>();
		for(TimeStop ts : timeNodes)
		{
			if (ts.isDestStop)
				ends.add(ts);
		}
		return ends;
	}
	
	public ArrayList<TimeStop> getStartStops()
	{
		ArrayList<TimeStop> starts = new ArrayList<TimeStop>();
		for(TimeStop ts : timeNodes)
		{
			if (ts.isStartStop)
				starts.add(ts);
		}
		return starts;
	}
	
	/**
	 * Add timestop node
	 * 
	 * @param ts
	 * @return true if not already in list, false if already existed
	 */
	public boolean addTimeStop(TimeStop ts)
	{
		if (!(timeNodes.contains(ts)))
		{
			timeNodes.add(ts);
			return true;
		}
		return false;
	}

	public ArrayList<Edge> previousEdges(TimeStop end)
	{
		return inEdges.get(end);
	}
	
	public TimeStop previousStop(Edge edge)
	{
		return edge.t1;
	}

	public void addNewEdge(TimeStop ts1, TimeStop ts2, Route r)
	{
		new Edge(ts1, ts2, r);
	}
	
	public class Edge
	{
		TimeStop t1;
		TimeStop t2;
		Route r;
		long weight;
		
		//Instead of making intelligent object creation
		//Just use .contains to check if already
		public Edge(TimeStop t1, TimeStop t2, Route r)
		{
			this.t1 = t1;
			this.t2 = t2;
			this.r = r;
			addEdge();
			
			//weight = t2.time
		}
		
		/**
		 * Only if t1 before t2 as define by TimeStop class
		 * 
		 * Adds timestops to timestop list
		 * Edge to edge list
		 * and maps neighboring timestops as edge list
		 *
		 * Should change logic flow for less conditional eval
		 *
		 * @returns true if the edge was newly added
		 */
		public boolean addEdge()
		{
			if (!(t1.isBefore(t2)))
				return false;
			
			if (!(timeNodes.contains(t1)))
					timeNodes.add(t1);
			if (!(timeNodes.contains(t2)))
					timeNodes.add(t2);
			if (!(timeEdges.contains(this)))
			{
				timeEdges.add(this);
				
				//Add t2 to t1's outgoing edges
				if (!outEdges.contains(t1.tStopID))
					outEdges.put(t1.tStopID, new ArrayList<Edge>());
				ArrayList<Edge> t1out = outEdges.get(t1.tStopID);
				if (!t1out.contains(this))
				{
					t1out.add(this);
				}
				
				//Add t1 to t2's incoming edges
				if (!inEdges.contains(t2.tStopID))
					inEdges.put(t2.tStopID, new ArrayList<Edge>());
				ArrayList<Edge> t2in = inEdges.get(t2.tStopID);
				if (!t2in.contains(this))
				{
					t2in.add(this);
				}
				return true;
			}
			return false;
		}
		
		public boolean equals(Object o)
		{
			if (o instanceof Edge)
			{
				Edge e = (Edge) o;
				if (t2 == e.t2 && e.t1 == t1)
					return true;
				//Works w/ == because I didn't allow more than one Timestop to be generated
				//with any given tstopid
				
			}
			return false;
		}
	}
	
	public class SmartEdge// extends Edge
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
