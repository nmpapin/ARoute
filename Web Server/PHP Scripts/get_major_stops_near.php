<?php
	require_once 'db_helper.php';
	require_once 'common.php';
	
	if(!isset($_GET['latitude']) or !isset($_GET['longitude']))
	{
		die("Expected get data of 'latitude' and 'longitude'.");
	}
	
	$database = new Database();
	
	$lat = mysql_real_escape_string($_GET['latitude']);
	$long = mysql_real_escape_string($_GET['longitude']);
	
	$maxD = 1000;
	if(isset($_GET['radius']))
	{
		$maxD = $_GET['radius'];
	}
	$maxD = mysql_real_escape_string($maxD);
	
	$stops = $database->getResults
	(
		"SELECT id, name, latitude, longitude, (3959 * 1000 * 1.609344 * ACOS(COS(RADIANS($lat)) * COS( RADIANS(latitude)) * COS(RADIANS(longitude) - RADIANS($long)) + SIN(RADIANS($lat)) * SIN(RADIANS(latitude)))) AS distance 
		FROM routing_stop
		HAVING distance < $maxD 
		ORDER BY distance ASC;"
	);
	
	echo json_encode($stops);
?>