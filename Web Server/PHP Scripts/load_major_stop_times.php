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
	
	function loadBusTimes($route, $database)
	{	
		$bus_data_page = file_get_contents("http://itsmarta.com$route");
		
		$html = str_get_html($bus_data_page, true);
		
		$blocks = $html->find("div[style=width: 650px; overflow-x: scroll; scrollbar-face-color: #e7e7e7; scrollbar-3dlight-color: #a0a0a0; scrollbar-darkshadow-color: #888888]");
		
		foreach($blocks AS $block)
		{
			$direction = $block->find("b");
			$direction = $direction[0]->text();
			$direction = explode(" ", $direction);
			$direction = translateDirection($direction[1]);
		
			$header = $block->find("tr[bgcolor=#333333]");
			$header = $header[0];
			$times = $header->parent();
			
			$stop_ids = array();
			for($i = 1; $i < count($header->children()); $i++)
			{
				$short = $header->children($i)->text();
				$station_id = getStationFromShort($short, $direction);
				$stop_ids[$i] = $station_id;
			}
		}
		
		//echo $headers;
		//echo $times;
	}
	
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
		
		$database->getResultInserted
		(
			"INSERT INTO routing_route
			(marta_id, name, type)
			VALUES $route_vals;"
		);
		
		$link = $link->href;
		
		loadBusTimes($link, $database);
	}
?>