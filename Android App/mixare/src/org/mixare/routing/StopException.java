package org.mixare.routing;

public class StopException extends Exception
{
	public StopException()
	{
		super("Invalid map given: did not contain stopid, long, AND lat");
	}
}