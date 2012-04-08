<?php
	require_once 'db_helper.php';
	require_once 'simple_html_dom.php';

	function do_request($url, $data, $post = true)
	{
		$data_count = count($data);
		$data_string = '';
		foreach($data as $key=>$value)
		{
			$data_string .= urlencode($key).'='.urlencode($value).'&';
		}
		$data_string = rtrim($data_string, '&');
		
		$ch = curl_init();
		
		if(!$post)
		{
			$url .= '?' . $data_string;
		}
		else
		{
			curl_setopt($ch, CURLOPT_POST, 1);
			curl_setopt($ch, CURLOPT_POSTFIELDS, $data_string);
		}
		
		curl_setopt($ch, CURLOPT_URL, $url);
		curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1);
		
		$res = curl_exec($ch);
		if(!$res)
		{
			trigger_error(curl_error($ch));
		}
		
		curl_close($ch);
		return $res;
	}
	
	function getMartaRoutes($database)
	{
		$routes = $database->getResults
		(
			
		);
		
		return $routes;
	}
	
	function getStopRoutes($database, $stop)
	{
		$routes = $database->getResults
		(
			"SELECT DISTINCT r.marta_id AS id, r.route_name AS name
			FROM route AS r INNER JOIN route_variation AS rv ON r.id = rv.route_id
			INNER JOIN route_stop AS rs ON rv.id = rs.route_var_id
			WHERE rs.stop_id = '" . $stop . "';"
		);
		
		return $routes;
	}
	
	function getStopRouteVariations($database, $stop)
	{
		$route_vars = $database->getResults
		(
			"SELECT DISTINCT r.marta_id AS route_id, r.route_name AS route_name, 
							 rv.id AS variation_id, rv.variation_name AS variation_name,
							 rv.direction AS variation_direction
			FROM route AS r INNER JOIN route_variation AS rv ON r.id = rv.route_id
			INNER JOIN route_stop AS rs ON rv.id = rs.route_var_id
			WHERE rs.stop_id = '" . $stop . "';"
		);
		
		$route_variations = array();
		foreach($route_vars AS $var)
		{
			if(!isset($route_variations[$var['route_id']]))
			{
				$route_variations[$var['route_id']] = array
				(
					'id' => $var['route_id'],
					'name' => $var['route_name'], 
					'variations' => array()
				);
			}
			
			array_push
			(
				$route_variations[$var['route_id']]['variations'], 
				array
				(
					'id' => $var['variation_id'], 
					'name' => $var['variation_name'], 
					'direction' => $var['variation_direction']
				)
			);
		}
		
		$ret = array();
		foreach($route_variations AS $var)
		{
			array_push($ret, $var);
		}
		
		return $ret;
	}
	
	function getStopTimes($database, $stop)
	{
		$route_variations = getStopRouteVariations($database, $stop);
		
		foreach($route_variations AS $route_num=>$route)
		{
			foreach($route['variations'] AS $var_num=>$var)
			{
				$post_args = array
				(
					'route_id' => $route['id'],
					'direction' => $var['direction'],
					'headsign' => $var['name'],
					'stop_id' => $stop
				);
				
				$data = do_request("http://spice.ridecell.com/ada/get_stop_times", $post_args, false);
				$html = str_get_html($data, true);
				$row = $html->find('tr');
				
				$last_bus = $row[0];
				$last_bus = $last_bus->first_child()->text();
				if(substr($last_bus, -2) === "-1")
				{
					unset($route['variations'][$var_num]);
					continue;
				}
				
				$num_bus = $row[1];
				$num_bus = $num_bus->first_child()->text();
				$num_bus = substr($num_bus, strlen("Prediction for next "), -1 * strlen(" buses"));
				
				if($num_bus > 0)
				{
					$var['times'] = array();
				}
				else
				{
					unset($route['variations'][$var_num]);
					continue;
				}
				
				$first_time_row = 3;
				for($i = 0; $i < $num_bus; $i++)
				{
					$time_txt = $row[$first_time_row + $i]->children(1)->text();
					$time = strtotime($time_txt);
					
					if($time)
						array_push($var['times'], $time_txt);
				}
				
				if(count($var['times']) < 1)
				{
					unset($route['variations'][$var_num]);
				}
				else
				{
					$route['variations'][$var_num] = $var;
				}
			}
			
			$route['variations'] = array_values($route['variations']);
			if(count($route['variations']) < 1)
			{
				unset($route_variations[$route_num]);
			}
			else
			{
				$route_variations[$route_num] = $route;
			}
		}
		
		$route_variations = array_values($route_variations);
		
		return $route_variations;
	}
?>