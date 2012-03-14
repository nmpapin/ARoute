<?php
	require_once 'db_helper.php';
	
	$database = new Database();
	
	set_time_limit(240);
	
	function do_post_request($url, $data, $optional_headers = null)
	{
		$data_count = count($data);
		$data_string = '';
		foreach($data as $key=>$value)
		{
			$data_string .= $key.'='.$value.'&';
		}
		$data_string = rtrim($data_string, '&');
		
		$ch = curl_init();
		
		curl_setopt($ch, CURLOPT_URL, $url);
		curl_setopt($ch, CURLOPT_POST, 1);
		curl_setopt($ch, CURLOPT_POSTFIELDS, $data_string);
		curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1);
		
		$res = curl_exec($ch);
		if(!$res)
		{
			trigger_error(curl_error($ch));
		}
		
		curl_close($ch);
		return $res;
	}
	
	echo "";
	
	// Format Array of {"id": #, "short_name": bus_route or line, "name": name}
	$bus_routes = json_decode(file_get_contents("http://spice.ridecell.com/get_routes/buses/"), true);
	$train_routes = json_decode(file_get_contents("http://spice.ridecell.com/get_routes/trains/"), true);

	foreach($bus_routes AS $value)
	{		
		if(!$database->getResult("SELECT id FROM Route WHERE id='".mysql_real_escape_string($value['id'])."';"))
		{
			$route_vals = "(". mysql_real_escape_string($value['id']) .",'".mysql_real_escape_string($value['short_name'])."','".mysql_real_escape_string($value['name'])."','Bus')";
			
			$database->getResultInserted
			(
				"INSERT INTO Route
				(id, marta_id, route_name, type)
				VALUES $route_vals;"
			);
		}
	}
	foreach($train_routes AS $value)
	{		
		if(!$database->getResult("SELECT id FROM Route WHERE id='".mysql_real_escape_string($value['id'])."';"))
		{
			$route_vals = "(". mysql_real_escape_string($value['id']) .",'".mysql_real_escape_string($value['short_name'])."','".mysql_real_escape_string($value['name'])."','Train')";
		
			$database->getResultInserted
			(
				"INSERT INTO Route
				(id, marta_id, route_name, type)
				VALUES $route_vals;"
			);
		}
	}
	
	$routes = $database->getResults("SELECT id FROM Route;");
	
	$head_val = '';
	
	foreach($routes AS $route)
	{
		$headsigns = json_decode(do_post_request("http://spice.ridecell.com/desktop/route_headsigns/", array("route_id" => $route['id'])), true);
		$headsigns = $headsigns[1];
		
		foreach($headsigns AS $head)
		{
			if(!$database->getResult
			(
				"SELECT * 
				FROM Route_Variation 
				WHERE route_id='".mysql_real_escape_string($head['route_id'])."' 
				AND direction='".mysql_real_escape_string($head['direction'])."' 
				AND variation_name='".mysql_real_escape_string($head['headsign'])."' 
				AND route_shape_id='".mysql_real_escape_string($head['shape_id'])."';"
			))
			{
				$head_val .= "(".mysql_real_escape_string($head['direction']).",'".mysql_real_escape_string($head['headsign'])."',".mysql_real_escape_string($head['route_id']).",".mysql_real_escape_string($head['shape_id'])."),";
			}
		}
	}
	$head_val = rtrim($head_val, ',');
	
	$database->getResultInserted
	(
		"INSERT INTO Route_Variation
		(direction, variation_name, route_id, route_shape_id)
		VALUES $head_val;"
	);
	
	set_time_limit(240);
	
	$headsigns = $database->getResults("SELECT * FROM Route_Variation");
	
	foreach($headsigns AS $head)
	{
		if(!$database->getResult("SELECT id FROM Route_Stop WHERE route_var_id='".mysql_real_escape_string($head['id'])."';"))
		{
			$stops_arg = array("route_id" => $head['route_id'], "headsign" => $head['variation_name'], "direction" => $head['direction'], "shape_id" => $head['route_shape_id']);
		
			$stops = json_decode(do_post_request("http://spice.ridecell.com/get_stops_with_locations/", $stops_arg), true);
			
			$stop_ids = $stops['stop_ids'];
			$stop_names = $stops['stop_names'];
			$stop_pos = $stops['stop_lat_lngs'];
			
			// Make the stops.
			for($i = 0; $i < count($stop_ids); $i++)
			{
				if(!$database->getResult("SELECT * FROM Stop WHERE id='".mysql_real_escape_string($stop_ids[$i])."';"))
				{
					$stop_val = "(".mysql_real_escape_string($stop_ids[$i]).",'".mysql_real_escape_string($stop_names[$i])."',".mysql_real_escape_string($stop_pos[$i][0]).",".mysql_real_escape_string($stop_pos[$i][1]).")";
					
					$database->getResultInserted
					(
						"INSERT INTO Stop
						(id, name, latitude, longitude)
						VALUES $stop_val;"
					);
				}
			}
			
			$ordered_stops = $stops['ordered_stops_details'];
			
			// Add the route variation stops.
			$route_stop_val = '';
			for($i = 0; $i < count($ordered_stops); $i++)
			{
				$route_stop_val .= "(".mysql_real_escape_string($head['id']).",".mysql_real_escape_string($ordered_stops[$i][1]).",".mysql_real_escape_string($i)."),";
			}
			$route_stop_val = rtrim($route_stop_val, ',');
			
			if(strlen($route_stop_val) > 0)
			{
				$database->getResultInserted
				(
					"INSERT INTO Route_Stop
					(route_var_id, stop_id, route_order)
					VALUES $route_stop_val;"
				);
			}
		}
		
		set_time_limit(240);
	}
	
	echo "Done";
	exit;
?>