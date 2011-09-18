<?php

// no direct access
defined('_JEXEC') or die('Restricted access');

// Include the syndicate functions only once
require_once( dirname(__FILE__).DS.'helper.php' );

$moduleclass_sfx = $params->get('moduleclass_sfx', '');

define('BASE_DIR', dirname(__FILE__));
require BASE_DIR.'/lib/misc.lib.php';
require BASE_DIR.'/lib/OSS_API.class.php';
require BASE_DIR.'/lib/OSS_Search.class.php';

define('MAX_PAGE_TO_LINK', $params->get('maxResultPerPage',10));


$ossEnginePath  = configRequestValue('ossEnginePath', $params->get('urlSearchEngine'), 'engineURL');
$ossEngineConnectTimeOut = configRequestValue('ossEngineConnectTimeOut', $params->get('ossEngineConnectTimeOut',5), 'engineConnectTimeOut');
$ossEngineIndex = configRequestValue('ossEngineIndex_contrib_filesearch', $params->get('ossEngineIndexName'), 'engineIndex');

if (isset($_REQUEST['query'])) {

	$search = new OSS_Search($ossEnginePath, $ossEngineIndex);

	if (!empty($_REQUEST['lang'])) {
		$search->lang($_REQUEST['lang'])
		->filter('lang:'.$_REQUEST['lang']);
	}

	// Restrict row count between 5 and 50
	$rows  = isset($_REQUEST['rows']) ? max(1, min($_REQUEST['rows'], 50)) : 10;
	$start = isset($_REQUEST['p'])    ? max(0, $_REQUEST['p']) * $rows : 0;

	//echo $start;
	
	$result = $search->query($_REQUEST['query'])
	->template('search')
	->facet('lang', 0)
	// ->field(array('uri', 'crawlDate', 'directory', 'fileSystemDate', 'metaDescription', 'metaKeywords', 'lang', 'title', 'when'))
	->start($start)
	->rows($params->get('maxResultPerPage',10))
	->execute($ossEngineConnectTimeOut);

	
	if ($result instanceof SimpleXMLElement) {

		$resultFound   = (int)$result->result['numFound'];
		$resultTime    = (float)$result->result['time'] / 1000;
		$resultRows    = (int)$result->result['rows'];
		$resultStart   = (int)$result->result['start'];

		$resultEntries = $result->xpath('result/doc');
		
		// Navigation between pages
		$resultCurrentPage = floor($resultStart / $resultRows);
		$resultTotalPages  = ceil($resultFound / $resultRows);
		
		//echo $resultRows;
		
		if ($resultTotalPages > 1) {
			$low  = $resultCurrentPage - (MAX_PAGE_TO_LINK / 2);
			$high = $resultCurrentPage + (MAX_PAGE_TO_LINK / 2 - 1);
			if ($low < 0) {
				$high += $low * -1;
			}
			if ($high > $resultTotalPages) {
				$low -= $high - $resultTotalPages;
			}
			$resultLowPage  = max($low, 0);
			$resultHighPage = min($resultTotalPages, $high);
			$resultLowPrev  = max($resultCurrentPage - MAX_PAGE_TO_LINK, 0);
			$resultHighNext = min($resultCurrentPage + MAX_PAGE_TO_LINK, $resultTotalPages);
			$pageBaseURI = preg_replace('/&(?:p|rows)=[\d]+/', '', $_SERVER['REQUEST_URI']).'&rows='.$resultRows.'&p=';
		}

		/*$langFacets = $result->xpath('faceting/field[@name="lang"]/facet');
		if ($langFacets === false) $langFacets = array();*/
	}
}
require(JModuleHelper::getLayoutPath('mod_oss_results'));
