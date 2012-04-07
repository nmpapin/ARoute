<?php
	require_once 'db_helper.php';
	require_once 'mixare_tools.php';
	require_once 'common.php';

	if(!isset($_GET['stop_id']))
	{
		die("Expected post data of 'stop_id'.");
	}
	
	$database = new Database();
	$stop = mysql_real_escape_string($_GET['stop_id']);
	
	echo json_encode(getStopTimes($database, $stop));
?>