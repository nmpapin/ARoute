<?php
	require_once 'db_helper.php';
	require_once 'common.php';
	
	if(!isset($_GET['route']) or !isset($_GET['stop']))
	{
		die("Expected get data of 'route' and 'stop'.");
	}
	
	$database = new Database();
	
	$route = mysql_real_escape_string($_GET['route']);
	$stop = mysql_real_escape_string($_GET['stop']);
	
	$date = new DateTime("now" , new DateTimeZone("America/New_York"));
	$time = $date->format('H:i:s');
	if(isset($_GET['time']))
	{
		$time = $_GET['time'];
	}
	$time = mysql_real_escape_string($time);
	
	$stops = getFollowingStopTimes($database, $stop, $route, $time);
	
	echo json_encode(array_values($stops));
?>