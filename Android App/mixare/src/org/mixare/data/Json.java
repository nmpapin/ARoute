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
package org.mixare.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.mixare.BusStopMarker;
import org.mixare.BusStopMarker.Route;
import org.mixare.BusStopMarker.RouteVariation;
import org.mixare.Marker;
import org.mixare.MixContext;
import org.mixare.MixView;
import org.mixare.NavigationMarker;
import org.mixare.POIMarker;
import org.mixare.SocialMarker;

import android.util.Log;
/**
 * This class can compose a list of markers. The markers are
 * made by other methods in the class, which take information
 * from multiple sources.
 */
public class Json extends DataHandler {

	public static final int MAX_JSON_OBJECTS = 1000;

	public List<Marker> load(JSONObject root, DataSource datasource) {
		JSONObject jo = null;
		JSONArray dataArray = null;
		List<Marker> markers = new ArrayList<Marker>();

		try {
			// Twitter & own schema
			if (root.has("results"))
				dataArray = root.getJSONArray("results");
			// Wikipedia
			else if (root.has("geonames"))
				dataArray = root.getJSONArray("geonames");

			if (dataArray != null) {

				Log.i(MixView.TAG, "processing " + datasource.getType()
						+ " JSON Data Array");
				int top = Math.min(MAX_JSON_OBJECTS, dataArray.length());

				for (int i = 0; i < top; i++) {

					jo = dataArray.getJSONObject(i);
					Marker ma = null;
					switch (datasource.getType()) {
					case TWITTER:
						ma = processTwitterJSONObject(jo, datasource);
						break;
					case WIKIPEDIA:
						ma = processWikipediaJSONObject(jo, datasource);
						break;
					case AROUTE:
						ma = processARouteJSONObject(jo, datasource);
						break;
					case MIXARE:
					default:
						ma = processMixareJSONObject(jo, datasource);
						break;
					}
					if (ma != null)
						markers.add(ma);
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return markers;
	}

	public Marker processTwitterJSONObject(JSONObject jo, DataSource datasource)
			throws NumberFormatException, JSONException {
		Marker ma = null;
		if (jo.has("geo")) {
			Double lat = null, lon = null;

			if (!jo.isNull("geo")) {
				JSONObject geo = jo.getJSONObject("geo");
				JSONArray coordinates = geo.getJSONArray("coordinates");
				lat = Double.parseDouble(coordinates.getString(0));
				lon = Double.parseDouble(coordinates.getString(1));
			} else if (jo.has("location")) {

				// Regex pattern to match location information
				// from the location setting, like:
				// iPhone: 12.34,56.78
				// ÃœT: 12.34,56.78
				// 12.34,56.78

				Pattern pattern = Pattern
						.compile("\\D*([0-9.]+),\\s?([0-9.]+)");
				Matcher matcher = pattern.matcher(jo.getString("location"));

				if (matcher.find()) {
					lat = Double.parseDouble(matcher.group(1));
					lon = Double.parseDouble(matcher.group(2));
				}
			}
			if (lat != null) {
				Log.v(MixView.TAG, "processing Twitter JSON object");
				String user=jo.getString("from_user");
				String url="http://twitter.com/"+user;
				
				ma = new SocialMarker(
						user+": "+jo.getString("text"), 
						lat, 
						lon, 
						0, url, 
						datasource);
			}
		}
		return ma;
	}

	public Marker processMixareJSONObject(JSONObject jo, DataSource datasource) throws JSONException {

		Marker ma = null;
		if (jo.has("title") && jo.has("lat") && jo.has("lng")
				&& jo.has("elevation")) {

			Log.v(MixView.TAG, "processing Mixare JSON object");
			String link=null;
	
			if(jo.has("has_detail_page") && jo.getInt("has_detail_page")!=0 && jo.has("webpage"))
				link=jo.getString("webpage");
			
        	if(datasource.getDisplay() == DataSource.DISPLAY.CIRCLE_MARKER) {
			ma = new POIMarker(
					unescapeHTML(jo.getString("title"), 0), 
					jo.getDouble("lat"), 
					jo.getDouble("lng"), 
					jo.getDouble("elevation"), 
					link, 
					datasource);
        	} else {
            	ma = new NavigationMarker(
            			unescapeHTML(jo.getString("title"), 0), 
        				jo.getDouble("lat"), 
        				jo.getDouble("lng"), 
        				0, 
        				link, 
        				datasource);
        	}
		}
		return ma;
	}

	public Marker processARouteJSONObject(JSONObject jo, DataSource datasource) throws JSONException
	{
		Marker ma = null;
		
		Log.v(MixView.TAG, "Attempting to process ARoute JSON object.");

		if (jo.has("title") && jo.has("lat") && jo.has("lng") && jo.has("elevation") && jo.has("routes")) 
		{
			Log.v(MixView.TAG, "processing ARoute JSON object.");
			
			JSONArray routes = jo.getJSONArray("routes");
			ArrayList<Route> stopRoutes = new ArrayList<Route>(); 
			
			for(int i = 0; i < routes.length(); i++)
			{
				JSONObject route = routes.getJSONObject(i);
				JSONArray vars = route.getJSONArray("variations");
				ArrayList<RouteVariation> routeVariations = new ArrayList<RouteVariation>();
				for(int j = 0; j < vars.length(); j++)
				{
					JSONObject variation = vars.getJSONObject(j);
					routeVariations.add(new RouteVariation(variation.getInt("id"), variation.getString("name"), variation.getInt("direction")));
				}
				
				stopRoutes.add(new Route(route.getString("id"), route.getString("name"), routeVariations.toArray(new RouteVariation[0])));
			}
			
			ma = new BusStopMarker(
					jo.getInt("stop_id"),
					unescapeHTML(jo.getString("title"), 0), 
					jo.getDouble("lat"), 
					jo.getDouble("lng"), 
					jo.getDouble("elevation"), 
					stopRoutes.toArray(new Route[0]), 
					datasource);
		}
		
		return ma;
	}
	
	public Marker processWikipediaJSONObject(JSONObject jo, DataSource datasource)
			throws JSONException {

		Log.v(MixView.TAG, "processing Wikipedia JSON object");
		
		Marker ma = null;
		if (jo.has("title") && jo.has("lat") && jo.has("lng")
				&& jo.has("elevation") && jo.has("wikipediaUrl")) {

			Log.v(MixView.TAG, "processing Wikipedia JSON object");
	
			ma = new POIMarker(
					unescapeHTML(jo.getString("title"), 0), 
					jo.getDouble("lat"), 
					jo.getDouble("lng"), 
					jo.getDouble("elevation"), 
					"http://"+jo.getString("wikipediaUrl"), 
					datasource);
		}
		return ma;
	}

	private static HashMap<String, String> htmlEntities;
	static {
		htmlEntities = new HashMap<String, String>();
		htmlEntities.put("&lt;", "<");
		htmlEntities.put("&gt;", ">");
		htmlEntities.put("&amp;", "&");
		htmlEntities.put("&quot;", "\"");
		htmlEntities.put("&agrave;", "Ã ");
		htmlEntities.put("&Agrave;", "Ã€");
		htmlEntities.put("&acirc;", "Ã¢");
		htmlEntities.put("&auml;", "Ã¤");
		htmlEntities.put("&Auml;", "Ã„");
		htmlEntities.put("&Acirc;", "Ã‚");
		htmlEntities.put("&aring;", "Ã¥");
		htmlEntities.put("&Aring;", "Ã…");
		htmlEntities.put("&aelig;", "Ã¦");
		htmlEntities.put("&AElig;", "Ã†");
		htmlEntities.put("&ccedil;", "Ã§");
		htmlEntities.put("&Ccedil;", "Ã‡");
		htmlEntities.put("&eacute;", "Ã©");
		htmlEntities.put("&Eacute;", "Ã‰");
		htmlEntities.put("&egrave;", "Ã¨");
		htmlEntities.put("&Egrave;", "Ãˆ");
		htmlEntities.put("&ecirc;", "Ãª");
		htmlEntities.put("&Ecirc;", "ÃŠ");
		htmlEntities.put("&euml;", "Ã«");
		htmlEntities.put("&Euml;", "Ã‹");
		htmlEntities.put("&iuml;", "Ã¯");
		htmlEntities.put("&Iuml;", "Ã�");
		htmlEntities.put("&ocirc;", "Ã´");
		htmlEntities.put("&Ocirc;", "Ã”");
		htmlEntities.put("&ouml;", "Ã¶");
		htmlEntities.put("&Ouml;", "Ã–");
		htmlEntities.put("&oslash;", "Ã¸");
		htmlEntities.put("&Oslash;", "Ã˜");
		htmlEntities.put("&szlig;", "ÃŸ");
		htmlEntities.put("&ugrave;", "Ã¹");
		htmlEntities.put("&Ugrave;", "Ã™");
		htmlEntities.put("&ucirc;", "Ã»");
		htmlEntities.put("&Ucirc;", "Ã›");
		htmlEntities.put("&uuml;", "Ã¼");
		htmlEntities.put("&Uuml;", "Ãœ");
		htmlEntities.put("&nbsp;", " ");
		htmlEntities.put("&copy;", "\u00a9");
		htmlEntities.put("&reg;", "\u00ae");
		htmlEntities.put("&euro;", "\u20a0");
	}

	public String unescapeHTML(String source, int start) {
		int i, j;

		i = source.indexOf("&", start);
		if (i > -1) {
			j = source.indexOf(";", i);
			if (j > i) {
				String entityToLookFor = source.substring(i, j + 1);
				String value = (String) htmlEntities.get(entityToLookFor);
				if (value != null) {
					source = new StringBuffer().append(source.substring(0, i))
							.append(value).append(source.substring(j + 1))
							.toString();
					return unescapeHTML(source, i + 1); // recursive call
				}
			}
		}
		return source;
	}
}
