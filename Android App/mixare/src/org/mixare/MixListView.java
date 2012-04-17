/*
 * Copyright (C) 2010- Peer internet solutions
 * 
 * This file is part of mixare.
 * 
 * This program is free software: you can redistribute it and/or modify it 
 * under the terms of the GNU General Public License as published by 
 * the Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version. 
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS 
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License 
 * for more details. 
 * 
 * You should have received a copy of the GNU General Public License along with 
 * this program. If not, see <http://www.gnu.org/licenses/>
 */
package org.mixare;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONObject;
import org.mixare.data.DataHandler;
import org.mixare.data.DataInterface;
import org.mixare.data.DataSource;
import org.mixare.data.DataSourceList;
import org.mixare.data.StopTimesTask;
import org.mixare.routing.RouteActivity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnCreateContextMenuListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;

/**
 * This class holds vectors with informaction about sources, their description
 * and whether they have been selected.
 */
public class MixListView extends ListActivity {

	private static int list;

	private Vector<SpannableString> listViewMenu;
	private Vector<Marker> selectedItem;
	private Vector<String> selectedItemURL;
	private Vector<String> dataSourceMenu;
	private Vector<String> dataSourceDescription;
	private Vector<Boolean> dataSourceChecked;
	private Vector<Integer> dataSourceIcon;
	
	private Vector<String> suggestionItems;
	private Vector<Location> suggestionLocations;
	
	private MixContext mixContext;

	private DataView dataView;
	//private static String selectedDataSource = "Wikipedia";
	/*to check which data source is active*/
	//	private int clickedDataSourceItem = 0;
	private ListItemAdapter adapter;
	public static String customizedURL="http://mixare.org/geotest.php";
	private static Context ctx;
	private static String searchQuery = "";
	private static SpannableString underlinedTitle;
	public static List<Marker> searchResultMarkers;
	public static List<Marker> originalMarkerList;

	public Vector<String> getDataSourceMenu() {
		return dataSourceMenu;
	}
	
	public Vector<String> getDataSourceDescription() {
		return dataSourceDescription;
	}

	public Vector<Boolean> getDataSourceChecked() {
		return dataSourceChecked;
	}
	public Vector<Integer> getDataSourceIcon() {
		return dataSourceIcon;
	}
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		//		mixCtx = MixView.ctx;
		dataView = MixView.dataView;	
		ctx = this;
		mixContext = dataView.getContext();
		
		switch(list){
		case 1:
			adapter = new ListItemAdapter(this);
			//adapter.colorSource(getDataSource());
			getListView().setTextFilterEnabled(true);

			setListAdapter(adapter);
			break;

		case 2:
			selectedItem = new Vector<Marker>();
			listViewMenu = new Vector<SpannableString>();
			DataHandler jLayer = dataView.getDataHandler();
			/*add all marker items to a title and a URL Vector*/
			for (int i = 0; i < jLayer.getMarkerCount(); i++) {
				Marker ma = jLayer.getMarker(i);
				if(ma.isActive()) 
				{
					listViewMenu.add(new SpannableString(ma.getTitle()));
					selectedItem.add(ma);
				}
			}

			if (dataView.isFrozen()) {

				TextView searchNotificationTxt = new TextView(this);
				searchNotificationTxt.setVisibility(View.VISIBLE);
				searchNotificationTxt.setText(getString(DataView.SEARCH_ACTIVE_1)+" "+ DataSourceList.getDataSourcesStringList() + getString(DataView.SEARCH_ACTIVE_2));
				searchNotificationTxt.setWidth(MixView.dWindow.getWidth());

				searchNotificationTxt.setPadding(10, 2, 0, 0);
				searchNotificationTxt.setBackgroundColor(Color.DKGRAY);
				searchNotificationTxt.setTextColor(Color.WHITE);

				getListView().addHeaderView(searchNotificationTxt);

			}

			setListAdapter(new ArrayAdapter<SpannableString>(this, android.R.layout.simple_list_item_1,listViewMenu));
			getListView().setTextFilterEnabled(true);
			break;
		case 3:
			String addr = this.getIntent().getExtras().getString("address");
			Map<String, Location> locs = DataInterface.getRouteSuggestions(addr);
			
			suggestionItems = new Vector<String>();
			suggestionLocations = new Vector<Location>();
			
			for(Map.Entry<String, Location> l : locs.entrySet())
			{
				suggestionItems.add(l.getKey());
				suggestionLocations.add(l.getValue());
			}
			
			setListAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, suggestionItems));
			getListView().setTextFilterEnabled(true);
			break;
		}
	}

	private void handleIntent(Intent intent) {
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			String query = intent.getStringExtra(SearchManager.QUERY);
			getRouteSuggestions(query);
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		setIntent(intent);
		handleIntent(intent);
	}

	private void getRouteSuggestions(String query) 
	{
		MixListView.setList(3);
		Intent intent1 = new Intent(MixListView.this, MixListView.class); 
		intent1.putExtra("address", query);
		startActivityForResult(intent1, 42);
	}


	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		switch(list){
		/*Data Sources*/  
		case 1:
			//clickOnDataSource(position);	
			CheckBox cb = (CheckBox) v.findViewById(R.id.list_checkbox);
			cb.toggle();
			break;

			/*List View*/
		case 2:
			clickOnListView(position);
			break;
		case 3:
			clickOnSuggestion(position);
			break;
		}

	}

	public void clickOnSuggestion(int position)
	{
		if(position < suggestionItems.size())
		{
			String addr = suggestionItems.get(position);
			Location loc = suggestionLocations.get(position);
			
			Intent routeIntent = new Intent(RouteActivity.class.getName());
			routeIntent.putExtra("address", addr);
			routeIntent.putExtra("location", loc);
			startActivity(routeIntent);
		}
	}
	
	public void clickOnListView(int position)
	{
		Marker selectedMarker = position < selectedItem.size() ? selectedItem.get(position) : null;
		if(selectedMarker == null || !(selectedMarker instanceof StopMarker))
		{
			Toast.makeText( this, getString(DataView.NO_WEBINFO_AVAILABLE), Toast.LENGTH_LONG ).show();
		}
		else
		{
			StopMarker sm = (StopMarker)selectedMarker;
			this.loadStopDetailsDialog(sm.getTitle(), sm);
		}
	}

	public void loadStopDetailsDialog(String title, StopMarker stop)
	{
		Dialog d = new Dialog(this)
		{
			public boolean onKeyDown(int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_BACK)
					this.dismiss();
				return true;
			}
		};
		
		d.requestWindowFeature(Window.FEATURE_NO_TITLE);
		d.getWindow().setGravity(Gravity.CENTER);
		d.setContentView(R.layout.stopdetailsdialog);
		
		TextView titleView = (TextView)d.findViewById(R.id.stopDetailDialogTitle);
		titleView.setText(title);
		
		ExpandableListView list = (ExpandableListView)d.findViewById(R.id.stopDetailDialogRouteList);
		
		final Button button = (Button)d.findViewById(R.id.stopWalkRouteButton);
		final double longitude = stop.getLongitude();
		final double latitude = stop.getLatitude();
		
		final Location start = mixContext.getCurrentLocation();
		
        button.setOnClickListener(new View.OnClickListener() 
        {
            public void onClick(View v) 
            {
                Intent mapIntent = new Intent(org.mixare.maps.HelloGoogleMapsActivity.class.getName());
                mapIntent.putExtra("startLocation", start);
                mapIntent.putExtra("destLat", latitude);
                mapIntent.putExtra("destLong", longitude);
                
                startActivity(mapIntent);
            }
        });
		
		List<Map<String, ?>> groupMaps = stop.getRouteList();
		List<List<Map<String, ?>>> childMaps = stop.getRouteSubdataList();
		
		SimpleExpandableListAdapter adapter = new SimpleExpandableListAdapter(
				this,
				groupMaps,
				R.layout.stopdetailsdialogitemroute,
				new String[]{
					"id",
					"name"
				},
				new int[]{
					R.id.routeNumber,
					R.id.routeName
				},
				childMaps,
				R.layout.stopdetailsdialogroutevariation,
				new String[]{
					"direction",
					"name",
					"times"
				},
				new int[]{
					R.id.routeVariationDirection,
					R.id.routeVariationName,
					R.id.routeVariationTimes
				}
		);
		
		list.setAdapter(adapter);
		
		d.show();
		
		new StopTimesTask(adapter, stop).execute(groupMaps, childMaps);
	}
	
	public static void createContextMenu(ImageView icon) {
		icon.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {				
			public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
				int index=0;
				switch(ListItemAdapter.itemPosition){
				case 0:
					menu.setHeaderTitle("Wiki Menu");
					menu.add(index, index, index, "We are working on it...");			
					break;
				case 1:
					menu.setHeaderTitle("Twitter Menu");
					menu.add(index, index, index, "We are working on it...");
					break;
				case 2:
					menu.setHeaderTitle("Buzz Menu");
					menu.add(index, index, index, "We are working on it...");
					break;
				case 3:
					menu.setHeaderTitle("OpenStreetMap Menu");
					menu.add(index, index, index, "We are working on it...");
					break;
				case 4:
					AlertDialog.Builder alert = new AlertDialog.Builder(ctx);
					alert.setTitle("insert your own URL:");

					final EditText input = new EditText(ctx); 
					input.setText(customizedURL);
					alert.setView(input);

					alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {       		
							Editable value = input.getText();
							customizedURL = ""+value;
						}
					});
					alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {       		
							dialog.dismiss();
						}
					});
					alert.show();
					break;
				}
			}
		});

	}

	public void clickOnDataSource(int position){
//		if(dataView.isFrozen())
//			dataView.setFrozen(false);
//		switch(position){
//		/*WIKIPEDIA*/
//		case 0:
//			mixContext.toogleDataSource(DATASOURCE.WIKIPEDIA);
//			break;
//
//			/*TWITTER*/
//		case 1:		
//			mixContext.toogleDataSource(DATASOURCE.TWITTER);
//			break;
//
//			/*BUZZ*/
//		case 2:
//			mixContext.toogleDataSource(DATASOURCE.BUZZ);
//			break;
//
//			/*OSM*/
//		case 3:
//			mixContext.toogleDataSource(DATASOURCE.OSM);
//			break;
//
//			/*Own URL*/
//		case 4:
//			mixContext.toogleDataSource(DATASOURCE.OWNURL);
//			break;
//		}
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		int base = Menu.FIRST;

		/*define menu items*/
		MenuItem item1 = menu.add(base, base, base, getString(DataView.MENU_ITEM_3)); 
		MenuItem item2 = menu.add(base, base+1, base+1, getString(DataView.MENU_CAM_MODE));
		/*assign icons to the menu items*/
		item1.setIcon(android.R.drawable.ic_menu_mapmode);
		item2.setIcon(android.R.drawable.ic_menu_camera);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		switch(item.getItemId()){
		/*Map View*/
		case 1:
			createMixMap();
			finish();
			break;
			/*back to Camera View*/
		case 2:
			finish();
			break;
		case 3:
			Intent addDataSource = new Intent(this, DataSource.class);
			startActivity(addDataSource);
			break;
		
		}
		return true;
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case 1: 
			break;
		case 2: 
			break;
		case 3:
			break;
		}
		return false;
	}

	public void createMixMap(){
		Intent intent2 = new Intent(MixListView.this, MixMap.class); 
		startActivityForResult(intent2, 20);
	}

	/*public void setDataSource(String source){
		selectedDataSource = source;
	}

	public static String getDataSource(){
		return selectedDataSource;
	}*/

	public static void setList(int l){
		list = l;
	}

	public static String getSearchQuery(){
		return searchQuery;
	}

	public static void setSearchQuery(String query){
		searchQuery = query;
	}
}

/**
 * The ListItemAdapter is can store properties of list items, like background or
 * text color
 */
class ListItemAdapter extends BaseAdapter {

	private MixListView mixListView;

	private LayoutInflater myInflater;
	static ViewHolder holder;
	private int[] bgcolors = new int[] {0,0,0,0,0};
	private int[] textcolors = new int[] {Color.WHITE,Color.WHITE,Color.WHITE,Color.WHITE,Color.WHITE};
	private int[] descriptioncolors = new int[] {Color.GRAY,Color.GRAY,Color.GRAY,Color.GRAY,Color.GRAY};

	public static int itemPosition =0;

	public ListItemAdapter(MixListView mixListView) {
		this.mixListView = mixListView;
		myInflater = LayoutInflater.from(mixListView);
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		itemPosition = position;
		if (convertView==null) {
			convertView = myInflater.inflate(R.layout.main, null);

			holder = new ViewHolder();
			holder.text = (TextView) convertView.findViewById(R.id.list_text);
			holder.description = (TextView) convertView.findViewById(R.id.description_text);
			holder.checkbox = (CheckBox) convertView.findViewById(R.id.list_checkbox);
			holder.datasource_icon = (ImageView) convertView.findViewById(R.id.datasource_icon);
			
			convertView.setTag(holder);
		}
		else{
			holder = (ViewHolder) convertView.getTag();
		}

		holder.datasource_icon.setImageResource(mixListView.getDataSourceIcon().get(position));
		holder.checkbox.setChecked(mixListView.getDataSourceChecked().get(position));

		holder.checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
					mixListView.clickOnDataSource(position);
			}
			
		});
		
		holder.text.setPadding(20, 8, 0, 0);
		holder.description.setPadding(20, 40, 0, 0);

		holder.text.setText(mixListView.getDataSourceMenu().get(position));
		holder.description.setText(mixListView.getDataSourceDescription().get(position));

		int colorPos = position % bgcolors.length;
		convertView.setBackgroundColor(bgcolors[colorPos]);
		holder.text.setTextColor(textcolors[colorPos]);
		holder.description.setTextColor(descriptioncolors[colorPos]);

		return convertView;
	}

	public void changeColor(int index, int bgcolor, int textcolor){
		if (index < bgcolors.length) {
			bgcolors[index]=bgcolor;
			textcolors[index]= textcolor;
		}
		else
			Log.d("Color Error", "too large index");
	}

	public void colorSource(String source){
		for (int i = 0; i < bgcolors.length; i++) {
			bgcolors[i]=0;
			textcolors[i]=Color.WHITE;
		}
		
		if (source.equals("Wikipedia"))
			changeColor(0, Color.WHITE, Color.DKGRAY);
		else if (source.equals("Twitter"))
			changeColor(1, Color.WHITE, Color.DKGRAY);
		else if (source.equals("Buzz"))
			changeColor(2, Color.WHITE, Color.DKGRAY);
		else if (source.equals("OpenStreetMap"))
			changeColor(3, Color.WHITE, Color.DKGRAY);
		else if (source.equals("OwnURL"))
			changeColor(4, Color.WHITE, Color.DKGRAY);
	}

	@Override
	public int getCount() {
		return mixListView.getDataSourceMenu().size();
	}

	@Override
	public Object getItem(int position) {
		return this;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	private class ViewHolder {
		TextView text;
		TextView description;
		CheckBox checkbox;
		ImageView datasource_icon;
	}
}
