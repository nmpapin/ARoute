<?php
	/*
	SELECT DISTINCT s.*
	FROM route AS r
	JOIN route_variation AS rv on rv.route_id = r.id
	JOIN route_stop AS rs on rs.route_var_id = rv.id
	JOIN stop AS s on rs.stop_id = s.id
	WHERE r.id = 1 AND
	s.name LIKE '%Coronet%';
	*/

	$trans_sb = array
	(
		"CWMM" => 1293,
		"MaCa" => 8455,
		"HuHM" => 2053,
		"LuNo" => 3207,
		"AlBr" => 7941
	);
	
	$trans_nb = array
	(
		"CWMM" => 1293,
		"MaCa" => 8452,
		"HuHM" => 2084,
		"LuNo" => 3208,
		"AlBr" => 7941
	);
	
	$trans_eb = array
	(
		"DeSt" => 3542,
		"CaPS" => 3389,
		"NoNH" => 3331,
		"NASt" => 3211
	);
	
	$trans_wb = array
	(
		"DeSt" => 3542,
		"CaPS" => 3389,
		"NoNH" => 3318,
		"NASt" => 3211
	);
	
	$trans_table = array
	(
		$trans_sb,
		$trans_nb,
		$trans_eb,
		$trans_wb
	);
	
	function getStationFromShort($abbr, $direction)
	{
		$table = $trans_table[$direction];
		$stop = $table[$abbr];
		
		return $stop;
	}
?>