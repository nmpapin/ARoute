<?php
	
	function makeMixareData($array)
	{	
		$ret = array
		(
			"status" => "OK",
			"num_results" => count($array),
			"results" => $array
		);
		
		return $ret;
	}
	
	function makeMixareDataWithResults($array, $mapping)
	{
		static $properties = array
		(
			'lat' => 0,
			'lng' => 0,
			'elevation' => 0,
			'title' => 'No Title',
			'distance' => 0,
			'has_detail_page' => 0,
			'webpage' => ''
		);
	
		$ret = array();
		
		foreach($array AS $item)
		{
			$item_arr = array();
			
			foreach($properties AS $key => $value)
			{
				if(isset($mapping[$key]))
				{
					$item_arr[$key] = $item[$mapping[$key]];
				}
				else
				{
					$item_arr[$key] = $value;
				}
			}
			
			array_push($ret, $item_arr);
		}
		
		return makeMixareData($ret);
	}
?>