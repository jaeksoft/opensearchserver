<?php

define('BASE_DIR', dirname(__FILE__));
// The required classes
require BASE_DIR.'/../lib/oss_api.class.php';
require BASE_DIR.'/../lib/oss_misc.lib.php';
require BASE_DIR.'/../lib/oss_results.class.php';
require BASE_DIR.'/../lib/oss_paging.class.php';

// Somes useful constants
define('MAX_PAGE_TO_LINK', 10);
define('OSS_RESULT_PER_PAGE', 10);
define('OSS_PATH',  'http://localhost:8080/');
define('OSS_CONNECT_TIMEOUT',  10);
define('OSS_LOGIN',  'admin');
define('OSS_APIKEY',  '2d1d943635c709c7240dc7768cf5b995');
define('OSS_INDEX_NAME',  'webindex');
define('OSS_QUERY_NAME',  'search');
define('OSS_MAX_ROW_COUNT',  50);


// Get the keywords if any
$query = stripcslashes($_REQUEST['q']);
if (isset($query) && strlen($query) > 0) {


	$start = isset($_REQUEST['p']) ? $_REQUEST['p'] : null;
	$start = isset($start) ? max(0, $start - 1) * OSS_RESULT_PER_PAGE : 0;

	// It is a good idea to espace some control characteres
	$escapechars = array('\\', '^', '~', ':', '(', ')', '{', '}', '[', ']' , '&&', '||', '!', '*', '?');
	foreach ($escapechars as $escchar) $query = str_replace($escchar, ' ', $query);
	$query = trim($query);

	$search = new OSSSearch(OSS_PATH, OSS_INDEX_NAME, OSS_RESULT_PER_PAGE, $start);
	// The OSS authenfication if any
	$search->credential(OSS_LOGIN, OSS_APIKEY);


	// The query is submitted to the OpenSearchServer instance
	$result = $search->query($query)->template(OSS_QUERY_NAME)->execute(OSS_CONNECT_TIMEOUT);

	// Is there a result ?
	if (isset($result) && $result instanceof SimpleXMLElement) {

		$ossResults = new OssResults($result);
		$ossPaging = new OssPaging($result);
		$resultTime = (float)$result->result['time'] / 1000;

		$max = ($ossResults->getResultStart() + $ossResults->getResultRows() > $ossResults->getResultFound()) ? $ossResults->getResultFound() : $ossResults->getResultStart() + $ossResults->getResultRows();
		//Documents iteration
		for ($i = $ossResults->getResultStart(); $i < $max; $i++) {
			$title	 = stripslashes($ossResults->getField($i, 'title', true));
			$content = stripslashes($ossResults->getField($i, 'content', true));
			$url = stripslashes($ossResults->getField($i, 'url', false));
			$url = stripslashes($ossResults->getField($i, 'contentType', false));
			if  (isset($contentType) && $contentType == ‘application/pdf’) {
				$title = parse_url($url, PHP_URL_PATH);
			}				
		}
	}
}

?>