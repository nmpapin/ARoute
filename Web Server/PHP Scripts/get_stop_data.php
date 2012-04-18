<?php
	require_once 'db_helper.php';
	require_once 'common.php';
	
	if(!isset($_GET['stop']))
	{
		die("Expected get data of 'stop'.");
	}
	
	$database = new Database();

	$stop = mysql_real_escape_string($_GET['stop']);
	
	$stop_data = $database->getResult
	(
		"SELECT * 
		FROM routing_stop 
		WHERE id = $stop;"
	);
	
	echo json_encode($stop_data);
?>