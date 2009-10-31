<?php

include_spip('inc/oss');
include_spip('inc/OSS_API.class');
include_spip('inc/OSS_IndexDocument.class');
include_spip('inc/OSS_Search.class');

if (!defined('_DIR_PLUGIN_OSS')) {
	define('_DIR_PLUGIN_OSS', preg_replace(',/[^/]+$,', '', str_replace(str_replace('/ecrire', '', dirname($_SERVER['SCRIPT_FILENAME'])), '', dirname(__FILE__))));
}

function oss_dummy_function() {}

function oss_load_object($type, $id) {
	// Chargement des object
	switch ($type) {
		case 'article':
			return (array)sql_fetsel("*", "spip_articles", "id_article=".$id);
		break;
		case 'breve':
			return (array)sql_fetsel("*", "spip_breves", "id_breve=".$id);
		break;
		case 'rubrique':
			return (array)sql_fetsel("*", "spip_rubriques", "id_rubrique=".$id);
		break;
	}
}

function oss_get_type_from_table($table) {
	switch ($table) {
		case 'articles':
		case 'spip_articles': return 'article';
		case 'breves':
		case 'spip_breves':return 'breve';
		case 'rubriques':
		case 'spip_rubriques': return 'rubrique';
	}
}


function oss_get_table_from_type($table) {
	switch ($table) {
		case 'article':  return 'spip_articles';
		case 'breve':    return 'spip_breves';
		case 'rubrique': return 'spip_rubriques';
		case 'all': 	 return 'spip_articles,spip_breves,spip_rubriques';
	}
}

function oss_construct_uniqid($type, $object) {
	return $type.'_'.$object['id_'.$type];
}

/**
 * @return OSS_API
 */
function oss_get_api_instance() {
	static $oss_api = null;
	if ($oss_api === null) {
		lire_metas();
		$oss_api = new OSS_API($GLOBALS['meta']['oss_engine_path'], $GLOBALS['meta']['oss_engine_index']);
	}
	return $oss_api;
}

/**
 * Return the number of documents known in the index by types
 * @return array<int>
 */
function oss_get_count() {
	$oss = oss_get_api_instance();
	$search = $oss->search();
	set_error_handler('oss_dummy_function', E_ALL);
	try { $results = $search->facet('spip_type', 0)->rows(0)->execute(); }
	catch (Exception $e) { $results = false; } 
	restore_error_handler();
	if (!$results) return array();
	
	// Return the overall count
	$count = array('all' => (int)$results->result['numFound']);
	
	// Get the facets
	$types = $results->faceting->xpath('field[@name="spip_type"]');
	$types = reset($types);
	foreach ($types->facet as $facet)
		$count[(string)$facet['name']] = (int)$facet;
	ksort($count);
	return $count;
}

function oss_header_prive($head) {
	$head .= '<link id="cssoss" href="'._DIR_PLUGIN_OSS.'/squelettes/style.css" type="text/css" rel="stylesheet">';
	return $head."\n";
}
