<?php
	require_once 'db_helper.php';
	require_once 'common.php';

	if(!isset($_GET['stop']))
	{
		die("Expected get data of 'stop'.");
	}
	
	$database = new Database();
	
	$stop = mysql_real_escape_string($_GET['stop']);
	
	$date = new DateTime("now" , new DateTimeZone("America/New_York"));
	$time = $date->format('H:i:s');
	if(isset($_GET['time']))
	{
		$time = $_GET['time'];
	}
	$time = mysql_real_escape_string($time);

	$routes = $database->getResults
	(
		"SELECT DISTINCT rv.id, r.marta_id, r.name, rv.direction, rs.id AS route_stop_id
		FROM routing_route AS r
		JOIN routing_route_variation AS rv ON r.id = rv.route_id
		JOIN routing_route_stop AS rs ON rv.id = rs.route_var_id
		JOIN routing_stop AS s ON rs.stop_id = s.id
		WHERE s.id = $stop;"
	);
	
	foreach($routes AS $key=>$route)
	{	
		$new_time = $database->getResult
		(
			"SELECT stop_time
			FROM routing_route_time
			WHERE route_stop_id = ".$route['route_stop_id']." 
			AND stop_time >= '$time'
			ORDER BY stop_time ASC
			LIMIT 1;"
		);
		
		if($new_time == null)
		{
			$new_time = $database->getResult
			(
				"SELECT stop_time
				FROM routing_route_time
				WHERE route_stop_id = ".$route['route_stop_id']." 
				AND stop_time >= '00:00:00'
				ORDER BY stop_time ASC
				LIMIT 1;"
			);
		
			if($new_time == null)
			{
				unset($routes[$key]);
				continue;
			}
		}
		
		$routes[$key]['next_time'] = $new_time['stop_time'];
		unset($routes[$key]['route_stop_id']);
	}
	
	echo json_encode(array_values($routes));
?>