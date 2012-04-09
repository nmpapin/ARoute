<?php
	require_once 'db_helper.php';
	require_once 'mixare_tools.php';
	require_once 'common.php';
	
	if(!isset($_GET['latitude']) or !isset($_GET['longitude']))
	{
		die("Expected get data of 'latitude' and 'longitude'.");
	}
	
	$database = new Database();
	
	$lat = mysql_real_escape_string($_GET['latitude']);
	$long = mysql_real_escape_string($_GET['longitude']);
	
	$limit = 25;
	if(isset($_GET['maxRows']))
	{
		$limit = $_GET['maxRows'];
	}
	$limit = mysql_real_escape_string($limit);
	
	$maxD = 20;
	if(isset($_GET['radius']))
	{
		$maxD = $_GET['radius'];
	}
	$maxD = mysql_real_escape_string($maxD);
	
	$stops = $database->getResults
	(
		"SELECT *, (3959 * 1.609344 * ACOS(COS(RADIANS($lat)) * COS( RADIANS(latitude)) * COS(RADIANS(longitude) - RADIANS($long)) + SIN(RADIANS($lat)) * SIN(RADIANS(latitude)))) AS distance 
		FROM stop
		HAVING distance < $maxD 
		ORDER BY distance ASC;"
	);

	$final_stops = array();
	for($i = 0; $i < count($stops); $i++)
	{	
		$routes = getStopRouteVariations($database, $stops[$i]['id']);
		
		if(count($routes) > 0)
		{
			$stops[$i]['routes'] = $routes;
			array_push($final_stops, $stops[$i]);
			$limit--;
			
			if($limit <= 0)
				break;
		}
	}
	
	$ret = makeMixareDataWithResults($final_stops, array
	(
		'stop_id' => 'id', 
		'lat' => 'latitude',
		'lng' => 'longitude',
		'title' => 'name',
		'distance' => 'distance',
		'routes' => 'routes'
	));
	
	echo json_encode($ret);
?>