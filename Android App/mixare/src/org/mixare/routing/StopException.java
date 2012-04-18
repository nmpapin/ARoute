package org.mixare.routing;

import android.util.Log;

public class StopException extends RuntimeException
{
	public StopException(String msg)
	{
		super(msg);
		Log.i("Routing", "StopException: "+msg);
	}
	
	public StopException()
	{
		this("Invalid map given: did not contain stopid, long, AND lat");
	}
}