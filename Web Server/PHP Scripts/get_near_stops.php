<?php
	require_once 'db_helper.php';
	require_once 'mixare_tools.php';
	
	if(!isset($_POST['lat']) or !isset($_POST['long']))
	{
		die("Expected post data of 'lat' and 'long'.");
	}
	
	$database = new Database();
	
	$lat = mysql_real_escape_string($_POST['lat']);
	$long = mysql_real_escape_string($_POST['long']);
	
	$limit = 25;
	if(isset($_POST['limit']))
	{
		$limit = $_POST['limit'];
	}
	$limit = mysql_real_escape_string($limit);
	
	$maxD = 10;
	if(isset($_POST['maxDistance']))
	{
		$maxD = $_POST['maxDistance'];
	}
	$maxD = mysql_real_escape_string($maxD);
	
	$stops = $database->getResults
	(
		"SELECT *, (3959 * ACOS(COS(RADIANS($lat)) * COS( RADIANS(latitude)) * COS(RADIANS(longitude) - RADIANS($long)) + SIN(RADIANS($lat)) * SIN(RADIANS(latitude)))) AS distance 
		FROM stop 
		HAVING distance < $maxD 
		ORDER BY distance ASC 
		LIMIT 0 , $limit;"
	);

	$ret = makeMixareDataWithResults($stops, array
	(
		'lat' => 'latitude',
		'lng' => 'longitude',
		'title' => 'name',
		'distance' => 'distance'
	));
	
	echo json_encode($ret);
?>