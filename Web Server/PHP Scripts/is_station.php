<?php
	require_once 'db_helper.php';
	require_once 'common.php';
	
	if(!isset($_GET['stop']))
	{
		die("Expected get data of 'stop'.");
	}
	
	$database = new Database();

	echo json_encode(array('is_station' => isStation($database, $_GET['stop'])));
?>