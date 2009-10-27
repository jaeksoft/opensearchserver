<?php

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