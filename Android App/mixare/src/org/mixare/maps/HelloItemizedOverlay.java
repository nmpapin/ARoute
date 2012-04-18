package org.mixare.maps;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import java.util.ArrayList;

import com.google.android.maps.*;


public class HelloItemizedOverlay extends ItemizedOverlay
{
	
	private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
	
	Context mContext; 

	public HelloItemizedOverlay(Drawable defaultMarker)
	{	
		/* binds the marker to be centered on the bottom */
		super(boundCenterBottom(defaultMarker));
	}
	
	public HelloItemizedOverlay(Drawable defaultMarker, Context context) {
		this(boundCenterBottom(defaultMarker));
		mContext = context;
		populate();
	}

	/**
	 * Add an item overlay
	 * @param overlay
	 */
	public void addOverlay(OverlayItem overlay)
	{
		Drawable d = overlay.getMarker(0);
		
		if(d != null)
		{
			overlay.setMarker(boundCenterBottom(d));
		}
		
	    mOverlays.add(overlay);
	    populate(); //Called to prepare each item to be drawn
	}
	
	@Override
	/**
	 * Called by populate, must be set to return from our arraylist
	 */
	protected OverlayItem createItem(int i)
	{
	  return mOverlays.get(i);
	}
	
	@Override
	/**
	 * Set size to return the size of our arraylist
	 */
	public int size()
	{
	  return mOverlays.size();
	}
	
	@Override
	protected boolean onTap(int index) {
	  OverlayItem item = mOverlays.get(index);
	  AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
	  dialog.setTitle(item.getTitle());
	  dialog.setMessage(item.getSnippet());
	  dialog.show();
	  return true;
	}
}
