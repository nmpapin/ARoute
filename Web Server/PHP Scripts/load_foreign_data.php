<?php
	require_once 'db_helper.php';
	
	$database = new Database();
	
	function do_post_request($url, $data, $optional_headers = null)
	{
		$post_url = '';
		foreach($data AS $key=>$value) 
			$post_url .= $key.'='.$value.'&'; 
		$post_url = rtrim($post_url, '&'); 

		$params = array('http' => array(
			'method' => 'POST',
			'content' => $post_url
		));
		
		if ($optional_headers !== null) 
		{
			$params['http']['header'] = $optional_headers;
		}
		
		$ctx = stream_context_create($params);
		
		$fp = @fopen($url, 'rb', false, $ctx);
		if (!$fp) 
		{
			throw new Exception("Problem with $url, $php_errormsg");
		}
		
		$response = @stream_get_contents($fp);
		if ($response === false) 
		{
			throw new Exception("Problem reading data from $url, $php_errormsg");
		}
		
		fclose($fp);
		
		return $response;
	}
	
	// Format Array of {"id": #, "short_name": bus_route or line, "name": name}
	$bus_routes = json_decode(file_get_contents("http://spice.ridecell.com/get_routes/buses/"), true);
	$train_routes = json_decode(file_get_contents("http://spice.ridecell.com/get_routes/trains/"), true);
	
	$route_id_map = array();
	foreach($bus_routes AS $value)
	{
		$route_vals = "('".mysql_real_escape_string($value['short_name'])."','".mysql_real_escape_string($value['name'])."','Bus')";
		
		$route_id_map[$value['id']] = $database->getResultInserted
		(
			"INSERT INTO Route
			(marta_id, route_name, type)
			VALUES $route_vals;"
		);
	}
	foreach($train_routes AS $value)
	{
		$route_vals = "('".mysql_real_escape_string($value['short_name'])."','".mysql_real_escape_string($value['name'])."','Train')";
		
		$route_id_map[$value['id']] = $database->getResultInserted
		(
			"INSERT INTO Route
			(marta_id, route_name, type)
			VALUES $route_vals;"
		);
	}
	
	$routes = array_merge($bus_routes, $train_routes);
	
	$headsign_route_map = array();
	foreach($routes AS $value)
	{
		sleep(1);
	
		$headsigns = json_decode(do_post_request("http://spice.ridecell.com/desktop/route_headsigns/", array("route_id" => $value['id'])), true);
		$headsigns = $headsigns[1];
		$headsign_route_map[$value['id']] = $headsigns;
		
		foreach($headsigns AS $val2)
		{
			$head_val = "(".mysql_real_escape_string($val2['direction']).",'".mysql_real_escape_string($val2['headsign'])."',".mysql_real_escape_string($route_id_map[$val2['route_id']]).")";
			
			$database->getResultInserted
			(
				"INSERT INTO Route_Variation
				(direction, variation_name, route_id)
				VALUES $head_val;"
			);
		}
	}
	
	//$head_val = trim($head_val, ',');
	
	// Format: [success?, [{"direction": #(0 = south; 1 = north; 2 = east; 3 = west), "name": short_name, "color": color to print, "headsign": reads on bus, "route_id": #, "shape": array of 
	// coords for map, shape_id: "#"}, ...], bounds, error message
	//$headsigns = do_post_request("http://spice.ridecell.com/desktop/route_headsigns/", array("route_id" => $bus_routes[0]['id']));
	
	//echo $headsigns;
?>