package org.mixare.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.mixare.BusStopMarker;
import org.mixare.MixContext;
import org.mixare.MixView;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.SimpleExpandableListAdapter;

public class StopTimesTask extends AsyncTask<List, Void, Void> 
{
	private SimpleExpandableListAdapter mAdapter;
	private BusStopMarker mStop;
	private MixContext mCtx;
	
	public StopTimesTask(SimpleExpandableListAdapter adapter, BusStopMarker stop)
	{
		mAdapter = adapter;
		mStop = stop;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected Void doInBackground(List... params) 
	{
		List<Map<String, ?>> groups = (List<Map<String, ?>>)params[0];
		List<List<Map<String, ?>>> children = (List<List<Map<String, ?>>>)params[1];
		
		Log.i(MixView.TAG, "Loading Times for Stop ID:" + mStop.getStopID());
		
		Map<Integer, Map<String, ?>> routeVarMap = new HashMap<Integer, Map<String, ?>>();
		Map<Integer, List<Map<String, ?>>> routeVarMapParents = new HashMap<Integer, List<Map<String, ?>>>();
		for(List<Map<String, ?>> l : children)
		{
			for(Map<String, ?> m : l)
			{
				routeVarMap.put((Integer)m.get("id"), m);
				routeVarMapParents.put((Integer)m.get("id"), l);
			}
		}
		
		String url = DataInterface.DATA_URL_BASE + "get_stop_times.php?stop_id=" + mStop.getStopID();
		URL updateURL;
		try 
		{	        
	        String times = DataInterface.getURLContents(url);
	        
			updateFailure(children);
	        
	        JSONArray json = new JSONArray(times);
			
			for(int i = 0; i < json.length(); i++)
			{
				JSONObject curr = json.getJSONObject(i);
				JSONArray vars = curr.getJSONArray("variations");
				for(int j = 0; j < vars.length(); j++)
				{
					JSONObject currVar = vars.getJSONObject(j);
					JSONArray timeJSON = currVar.getJSONArray("times");
					
					String timeStr = "";
					for(int k = 0; k < timeJSON.length(); k++)
					{
						timeStr += timeJSON.getString(k);
						if(k < timeJSON.length() - 1)
							timeStr += " & ";
					}
					
					Map<String, Object> map = (Map<String, Object>)routeVarMap.get(currVar.getInt("id"));
					map.put("times", timeStr);
					
					routeVarMap.remove(currVar.getInt("id"));
					routeVarMapParents.remove(currVar.getInt("id"));
				}
			}
			
			for(Map.Entry<Integer, ?> kv : routeVarMap.entrySet())
			{
				int key = kv.getKey();
				List<Map<String, ?>> parentList = routeVarMapParents.get(key);
				if(parentList.size() == 1)
					break;
				parentList.remove(kv.getValue());
			}
		}
		catch (JSONException e) 
		{
			e.printStackTrace();
		}
		
		return null;
	}
	
	protected void onPostExecute(Void result) 
	{
        mAdapter.notifyDataSetChanged();
        //mAdapter.notifyDataSetInvalidated();
    }
	
	public void updateFailure(List<List<Map<String, ?>>> childData)
	{
		for(List<Map<String, ?>> l : childData)
		{
			for(Map<String, ?> m : l)
			{
				((Map<String, Object>)m).put("times", "No Times Scheduled");
			}
		}
	}
}
