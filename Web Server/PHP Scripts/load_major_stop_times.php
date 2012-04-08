<?php
	require_once 'db_helper.php';
	require_once 'common.php';
	
	function translateDirection($direction)
	{
		switch($direction)
		{
		case "Southbound":
			return 0;
		case "Northbound":
			return 1;
		case "Eastbound":
			return 2;
		case "Westbound":
			return 3;
		}
	}
	
	function loadStops($stops, $direction, $stop_data, $database)
	{
		$vals = "";
		foreach($stops AS $stop)
		{
			if(!isset($stop_data[$direction][$stop['name']]))
				continue;
		
			$data_entry = $stop_data[$direction][$stop['name']];
			
			$stop_row = array
			(
				"name" => mysql_real_escape_string($stop['name']),
				"short_name" => mysql_real_escape_string($stop['short_name']),
				"direction" => mysql_real_escape_string($direction),
				"latitude" => mysql_real_escape_string($data_entry['latitude']),
				"longitude" => mysql_real_escape_string($data_entry['longitude'])
			);
			
			$sn = $stop_row['short_name'];
			$n = $stop_row['name'];
			$lat = $stop_row['latitude'];
			$lng = $stop_row['longitude'];
			
			$res = $database->getResults
			(
				"SELECT *
				FROM routing_stop
				WHERE short_name='$sn'
				AND name='$n'
				AND latitude=$lat
				AND longitude=$lng;"
			);
			
			if(count($res) != 0)
			{
				continue;
			}
			else
			{
				$vals .= "('$n', '$sn', $lat, $lng),";
			}
		}
		
		$vals = rtrim($vals);
		$vals = rtrim($vals, ',');
		if(strlen($vals) == 0)
			return;
			
		$database->getResultInserted
		(
			"INSERT INTO routing_stop
			(name, short_name, latitude, longitude)
			VALUES $vals;"
		);
	}
	
	function loadBusTimes($route, $route_id, $database)
	{	
		set_time_limit(240);
	
		$bus_data_page = file_get_contents("http://itsmarta.com/$route-w.aspx");
		
		$html = str_get_html($bus_data_page, true);
		
		$blocks = $html->find("div[style=width: 650px; overflow-x: scroll; scrollbar-face-color: #e7e7e7; scrollbar-3dlight-color: #a0a0a0; scrollbar-darkshadow-color: #888888]");
		
		$major_stop_data = file_get_contents("http://webwatch.itsmarta.com/UpdateWebMap.aspx?u=$route&timestamp=2");
		$major_stop_data = explode("*", $major_stop_data);
		if(count($major_stop_data) <= 1)
			return;
		
		$major_stop_data = $major_stop_data[1];
		$major_stop_data = explode(";", $major_stop_data);
		
		$msd = array();
		foreach($major_stop_data AS $major_stop)
		{
			if(strlen($major_stop) == 0)
				continue;
			
			$ms = explode("|", $major_stop);
			
			$ms_desc = array
			(
				"latitude" => trim($ms[0]),
				"longitude" => trim($ms[1]),
				"name" => trim($ms[2]),
				"direction" => trim($ms[3])
			);
			
			if(!isset($msd[$ms_desc['direction']]))
				$msd[$ms_desc['direction']] = array();
				
			$msd[$ms_desc['direction']][$ms_desc['name']]=$ms_desc;
		}
		
		foreach($blocks AS $block)
		{
			set_time_limit(240);
		
			$direction = $block->find("b");
			$direction = $direction[0]->text();
			$direction = explode(" ", $direction);
			$direction = $direction[1];
			
			$header_names = $block->find("table");
			$header_names = $header_names[0]->children();
			
			$stops = array();
			$stop_name_trans = array();
			foreach($header_names AS $row)
			{			
				array_push($stops, array
				(
					"short_name" => trim($row->children(1)->text()),
					"name" => trim($row->children(0)->text())
				));
				
				$stop_name_trans[trim($row->children(1)->text())] = trim($row->children(0)->text());
			}
			
			loadStops($stops, $direction, $msd, $database);
			
			$header = $block->find("tr[bgcolor=#333333]");
			$header = $header[0];
			$times = $header->parent();
			
			$stop_ids = array();
			for($i = 1; $i < count($header->children()); $i++)
			{
				$short = trim($header->children($i)->text());
				$name = $stop_name_trans[$short];
				
				if(!isset($msd[$direction][$name]))
					continue;
				
				$data_entry = $msd[$direction][$name];
				
				$stop_lat = mysql_real_escape_string($data_entry['latitude']);
				$stop_lng = mysql_real_escape_string($data_entry['longitude']);
				
				$short = mysql_real_escape_string($short);
				$name = mysql_real_escape_string($name);
				
				$res = $database->getResult
				(
					"SELECT id
					FROM routing_stop
					WHERE name='$name'
					AND short_name='$short'
					AND latitude='$stop_lat'
					AND longitude='$stop_lng';"
				);
				
				$stop_ids[$i] = $res['id'];
			}
			
			$route_variations = array();
			$route_stop_ids = array();
			$route_var_maps = array();
			$full_vals = "";
			for($i = 1; $i < count($times->children()); $i++)
			{
				$row = $times->children($i);
				
				$variation_map = array();
				$var_str = "";
				for($j = 1; $j < count($row->children()); $j++)
				{
					if(!isset($stop_ids[$j]))
						continue;
					
					$string = trim($row->children($j)->text());
					
					$variation_map[$j] = (strcmp("--", $string) != 0);
					
					$var_str .= ($variation_map[$j] ? "1" : "0");
				}
				
				if(!isset($route_variations[$var_str]))
				{
					$vals = "('$direction', $route_id)";
				
					$res = $database->getResultInserted
					(
						"INSERT INTO routing_route_variation
						(direction, route_id)
						VALUES $vals;"
					);
					
					$route_var_maps[$res] = $variation_map;
					$route_variations[$var_str] = $res;
					$route_stop_ids[$res] = array();
					
					$order = 0;
					for($j = 1; $j < count($row->children()); $j++)
					{
						if(!isset($stop_ids[$j]))
							continue;
						
						if(!$variation_map[$j])
							continue;
						
						$rsi_vals = "($res, ".$stop_ids[$j].", $order)";
						
						$rsi = $database->getResultInserted
						(
							"INSERT INTO routing_route_stop
							(route_var_id, stop_id, route_order)
							VALUES $rsi_vals;"
						);
						
						$route_stop_ids[$res][$stop_ids[$j]] = $rsi;
						
						$order++;
					}
				}
				
				for($j = 1; $j < count($row->children()); $j++)
				{
					if(!isset($stop_ids[$j]))
						continue;
					
					$string = trim($row->children($j)->text());
					$string = trim($string) . "m";
					
					$variation = $route_variations[$var_str];
					
					if(!$route_var_maps[$variation][$j])
						continue;
					
					$time = strtotime($string);
					
					$rsid = $route_stop_ids[$variation][$stop_ids[$j]];
					$time_str = strftime("%H:%M:%S", $time);
					
					$full_vals .= "($rsid, '$time_str'),";
				}
			}
			
			$full_vals = rtrim($full_vals);
			$full_vals = rtrim($full_vals, ',');
			if(strlen($full_vals) == 0)
				continue;
			
			$database->getResultInserted
			(
				"INSERT INTO routing_route_time
				(route_stop_id, stop_time)
				VALUES $full_vals;"
			);
		}
	}
	
	set_time_limit(240);
	
	$database = new Database();
	
	$route_page = file_get_contents("http://itsmarta.com/bus-routes-by-route.aspx");
	$html = str_get_html($route_page, true);
	
	$routes = $html->find(".PageHeaderBlack");
	$routes = $routes[0];
	$routes = $routes->parent()->parent();
	$routes = $routes->find("li");
	foreach($routes AS $route)
	{	
		$link = $route->first_child();
	
		if(!$link->title)
			continue;
		
		$route_vals = "('".mysql_real_escape_string(trim($link->title)).
					  "','".mysql_real_escape_string(trim($link->text())).
					  "','Bus')";
		
		$route_id = $database->getResultInserted
		(
			"INSERT INTO routing_route
			(marta_id, name, type)
			VALUES $route_vals;"
		);
		
		loadBusTimes(trim($link->title), $route_id, $database);
	}
	
	echo "Done!";
?>