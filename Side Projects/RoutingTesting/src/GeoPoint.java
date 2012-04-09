
public class GeoPoint
{
	int lat, longitude;
	
	public GeoPoint(int lat, int longitude)
	{
		this.lat = lat;
		this.longitude = longitude;
	}
	
	public String toString()
	{
		return "(Latitude: "+lat+", Longitude: "+longitude+")";
	}
}
