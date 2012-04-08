<?php
	// Class used to handle database connections and work.
	class Database
	{
		// Our connection.
		var $connection;
	
		// Constructor
		function __construct()
		{
			$db_host = "localhost";
			$db_database = "nmpapin_aroute";
			$db_username = "nmpapin_user";
			$db_password = "user_password";
		
			// Initialize the connection.
			$this->connection = mysql_connect($db_host, $db_username, $db_password);
			if(!$this->connection)
			{
				die("An error occured while connecting to the database.<br /><br />" . mysql_error());
			}
			
			// Select the database.
			$db = mysql_select_db($db_database);
			if(!$db)
			{
				die("An error occured while connecting to the database.<br /><br />" . mysql_error());
			}
		}
		
		// Obtains the result of a database select statement as an associated array.
		function getResults($dbQuery)
		{
			$dbResults = mysql_query($dbQuery);
			if(!$dbResults)
			{
				die("Error: " . mysql_error() . "<br/>Query: " . $dbQuery);
			}
			
			// Test for results.
			$resultsArray = array();
			if(mysql_num_rows($dbResults) > 0)
			{
				while($row = mysql_fetch_assoc($dbResults))
				{
					$resultsArray[] = $row;
				}
			}
			else // If we do not have any results, return an empty array.
			{
				return array();
			}
			
			return $resultsArray;
		}
		
		// Obtains a single result from a database select statement.  This will always return the first result.
		// null is returned in the case that nothing is found.
		function getResult($dbQuery)
		{
			$dbResults = mysql_query($dbQuery);
	
			if(!$dbResults)
			{
				die("Error: " . mysql_error() . "<br/>Query: " . $dbQuery);
			}
		
			if(mysql_num_rows($dbResults) > 0)
			{
				return mysql_fetch_assoc($dbResults);
			}
			else
			{
				return null;
			}
		}
		
		// Runs an actionable query and returns the number of rows affected.
		function getRowsAffected($dbQuery)
		{
			$dbResults = mysql_query($dbQuery);
			if($dbResults)
			{
				return array('rowsAffected'=>mysql_affected_rows());
			}
			else
			{
				die("Error: " . mysql_error() . "<br/>Query: " . $dbQuery);
			}
		}
		
		// Runs an insert query and returns the first inserted auto_increment id.
		function getResultInserted($dbQuery)
		{
			$dbResults = mysql_query($dbQuery);
			if($dbResults)
			{
				return mysql_insert_id();
			}
			else
			{
				die("Error: " . mysql_error() . "<br/>Query: " . $dbQuery);
			}
		}
	}
?>
