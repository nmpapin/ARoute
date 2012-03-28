<?php
	require_once 'db_helper.php';
	require_once 'mixare_tools.php';
	
	if(!isset($_GET['latitude']) or !isset($_GET['longitude']))
	{
		die("Expected post data of 'latitude' and 'longitude'.");
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
		ORDER BY distance ASC 
		LIMIT 0 , $limit;"
	);

	for($i = 0; $i < count($stops); $i++)
	{
		$routes = $database->getResults
		(
			"SELECT DISTINCT r.marta_id AS id, r.route_name AS name
			FROM route AS r INNER JOIN route_variation AS rv ON r.id = rv.route_id
			INNER JOIN route_stop AS rs ON rv.id = rs.route_var_id
			WHERE rs.stop_id = '" . $stops[$i]['id'] . "';"
		);
		
		$stops[$i]['routes'] = $routes;
	}
	
	$ret = makeMixareDataWithResults($stops, array
	(
		'lat' => 'latitude',
		'lng' => 'longitude',
		'title' => 'name',
		'distance' => 'distance',
		'routes' => 'routes'
	));
	
	echo json_encode($ret);
?>