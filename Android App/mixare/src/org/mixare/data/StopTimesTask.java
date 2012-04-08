package org.mixare.data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.mixare.BusStopMarker;
import org.mixare.MixContext;

import android.os.AsyncTask;
import android.widget.SimpleExpandableListAdapter;

public class StopTimesTask extends AsyncTask<List, Void, Void> 
{
	private SimpleExpandableListAdapter mAdapter;
	private BusStopMarker mStop;
	private MixContext mCtx;
	
	public StopTimesTask(SimpleExpandableListAdapter adapter, BusStopMarker stop, MixContext ctx)
	{
		mAdapter = adapter;
		mStop = stop;
		mCtx = ctx;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected Void doInBackground(List... params) 
	{
		updateFailure();
		
		List<Map<String, ?>> groups = (List<Map<String, ?>>)params[0];
		List<List<Map<String, ?>>> children = (List<List<Map<String, ?>>>)params[1];
		
		Map<String, Integer> routeIdMap = new HashMap<String, Integer>();
		for(int i = 0; i < groups.size(); i++)
		{
			Map<String, ?> route = groups.get(i);
			routeIdMap.put((String)route.get("id"), i);
		}
		
		String url = "http://nmpapin.heliohost.org/cs4261/" + "get_stop_times.php?stop_id=" + mStop.getID();
		try 
		{
			String times = mCtx.getHttpInputString(mCtx.getHttpGETInputStream(url));
			JSONArray json = new JSONArray(times);
			
			for(int i = 0; i < json.length(); i++)
			{
				
			}
			
		} catch (Exception e){}
		
		
		mAdapter.notifyDataSetChanged();
		return null;
	}
	
	public void updateFailure()
	{
		
	}
}
