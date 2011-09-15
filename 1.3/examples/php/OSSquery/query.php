<?php
/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2008-2009 Emmanuel Keller / Jaeksoft
 * 
 * http://www.open-search-server.com
 * 
 * This file is part of OpenSearchServer.
 *
 * OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with OpenSearchServer. 
 *  If not, see <http://www.gnu.org/licenses/>.
 *  
 **/

$spacer = "<br><br>";
$start = 0;
$rows = 10;

// Get the searched expression
if (isset( $_REQUEST['keyword'])) {
	$searchWord =  $_REQUEST['keyword'];
}
else {
	$searchWord = '*:*';
}

// Get Start and Rows parameters
if (isset($_REQUEST['start'])) {
	$start = $_REQUEST['start'];
}
if (isset($_REQUEST['rows'])) {
	$rows = $_REQUEST['rows'];
}

// HTML
$writer= "<h1>Open Search Server (OSS) Query Test</h1>";
$writer .= '---------------------------------------------------------------------------------------<br>';

// Parameters
$server = "http://dedwen:8080/GIIECIndexDocumentList/";
$queryUsed = "websearch";


// Html Form
$writer .= '<form method="post" id="myform">';
$writer .= "<b>Search this word : </b>";
$writer .= '<input value="'.$searchWord.'" name ="keyword"/> ';
$writer .= '<INPUT type="submit" value="Search !"> ';
$writer .= '<a href="" onclick="window.location.reload();">Reset</a><br> ';
$writer .= '<b>Start :</b><input value="'.$start.'" name ="start" size="2"/>   ';
$writer .= '<b>Rows :</b><input value="'.$rows.'" name ="rows" size="2"/> <br>';
$writer.= " <b>with this query</b> '".$queryUsed."'.".$spacer;


//Compute url to send
$url =	$server."select?qt=".$queryUsed."&start=".$start."&rows=".$rows."&q=". urlencode($searchWord);
$writer .= "<a href='". $url ."' target='_blank'>". $url ."</a><br/><br/>";

// Call to Server via Curl
$ch = curl_init();
curl_setopt($ch, CURLOPT_URL, $url);
curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
curl_setopt($ch, CURLOPT_CONNECTTIMEOUT, 20);
curl_setopt($ch,CURLOPT_FOLLOWLOCATION, true);
curl_setopt($ch,CURLOPT_MAXREDIRS,100);
curl_setopt($ch, CURLOPT_TIMEOUT, 120);

// Put results in $content variable
$xml_contents = curl_exec ($ch);
// close Curl
curl_close ($ch);

$writer .= '---------------------------------------------------------------------------------------<br>';
if(strstr($xml_contents, 'xml version="1.0" encoding="UTF-8"')) {

	// Parse xml
	$simple_xml = simplexml_load_string($xml_contents);

	//	Utile ::pre($simple_xml);
	if ($simple_xml) {
		// Show the query sent
		$q = $simple_xml->xpath("header/query");
		foreach ($q as $ex) {
			$writer .= "<b>Query sent : </b>".$ex.$spacer;
		}

		// Iteration on each result's document.
		$doc  = $simple_xml->xpath("result/doc");
		$i = $start;
		foreach ($doc as $current) {
			$i++;
			$writer .= "<b>Result n°".$i.": </b><br>";

			// Get field ID
			$ids = $current->xpath("field[@name='id']");
			$writer .= "<b>Id</b> : ".$ids[0]."<br>";

			// Get first snippet Content
			$contents = $current->xpath("snippet[@name='content']");
			$writer .= "<b>Content</b> : ".$contents[0].$spacer;
		}
	}

	$writer .= '</form>';

	$writer .= '<br>---------------------------------------------------------------------------------------<br>';
}

echo $writer;
?>