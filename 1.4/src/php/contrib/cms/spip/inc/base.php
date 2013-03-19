<?php

if (!defined('_DIR_PLUGIN_OSS')) {
	define('_DIR_PLUGIN_OSS', preg_replace(',/[^/]+$,', '', str_replace(str_replace('/ecrire', '', dirname($_SERVER['SCRIPT_FILENAME'])), '', dirname(__FILE__))));
}

include_spip('inc/OSS_API.class');
include_spip('inc/OSS_IndexDocument.class');
include_spip('inc/OSS_Search.class');


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

//TODO Complete once API is available
function oss_get_index_fields() {
	
	return array(
		'id' => array(),
		'spip_id' => array(),
		'spip_type' => array(),
		'spip_subtitle' => array(),
		'spip_title' => array(),
		'spip_suptitle' => array(),
		'spip_header' => array(),
		'spip_body' => array(),
		'spip_site' => array(),
		'spip_url' => array(),
		'spip_author' => array(),
		'spip_category' => array(),
		'spip_update' => array(),
		'date_indexation' => array(),
		'spip_lang' => array(),
		'doc_title' => array()
	);
	
}

function oss_get_indexable_fields() {
	return array(
		'article' => array(
			'id_article',
			'surtitre',
			'titre',
			'soustitre',
			'nom_rubrique', 
			'descriptif',
			'chapo',
			'texte',
			'date',
			'statut',
			'maj',
			'date_redac',
			'date_modif',
			'lang',
			'langue_choisie',
			'nom_auteur'
		),
		'breve' => array(
			'id_breve',
			'date_heure',
			'titre',
			'texte',
			'nom_rubrique', 
			'lang',
			'langue_choisie',
			'maj'
		),
		'rubrique' => array(
			'id_rubrique',
			'id_parent',
			'titre',
			'descriptif',
			'texte',
			'maj',
			'date',
			'lang',
			'langue_choisie'
		)
	);
}


function oss_update($type, $object, $index = null) {
	
	// Type is indexable ?
	if (!$GLOBALS['meta']['oss_indexation_global_enabled'] || !$GLOBALS['meta']['oss_indexation_'.$type.'_enabled'])
		return;
	
	$doUpdate = !(bool)$index;
	
	if (!$index) $index = new OSS_IndexDocument();
	
	$document = $index->newDocument($object['lang']);
	
	$engineId = oss_construct_uniqid($type, $object);
	
	// Chargement des object
	$document->newField('id', 	 	 $engineId);
	$document->newField('spip_id', 	 $object['id_'.$type]);
	$document->newField('spip_type', $type);
	
	// Récupération des données spécifiques
	if ($type == 'articles') {
		
		if ($GLOBALS['meta']['oss_indexation_article_nom_auteur']) {
			// Récupération des auteurs
			$authors = (array)sql_allfetsel("SA.id_auteur, SA.nom", "spip_auteurs_articles AS SAA INNER JOIN spip_auteurs AS SA ON SA.id_auteur = SAA.id_auteur", "SAA.id_article=".$idObjet);
			
			foreach ($authors as $key => $author)
				$authors[$key] = $author['id_auteur'].'|'.$author['nom'];
			$document->newField('spip_author', $authors);
		}
		
	}
	elseif ($type == 'breve') {
		
	}
	elseif ($type == 'rubrique') {
		
		if ($GLOBALS['meta']['oss_indexation_rubrique_descriptif']) {
			$document->newField('spip_header',   $object['descriptif']);
		}
		
	}
	
	
	if ($GLOBALS['meta']['oss_indexation_'.$type.'_surtitre'])
		$document->newField('spip_suptitle', strip_tags($object['surtitre']));
	if ($GLOBALS['meta']['oss_indexation_'.$type.'_titre'])
		$document->newField('spip_title',    strip_tags($object['titre']));
	if ($GLOBALS['meta']['oss_indexation_'.$type.'_soustitre'])
		$document->newField('spip_subtitle', strip_tags($object['soustitre']));
	if ($GLOBALS['meta']['oss_indexation_'.$type.'_chapo'])
		$document->newField('spip_header',   strip_tags($object['chapo']));
	if ($GLOBALS['meta']['oss_indexation_'.$type.'_texte'])
		$document->newField('spip_body',     strip_tags($object['texte']));
	if ($GLOBALS['meta']['oss_indexation_'.$type.'_date_heure'] || $GLOBALS['meta']['oss_indexation_'.$type.'_date'])
		$document->newField('spip_date',     preg_replace('/[^\d]+/', '', isset($object['date_heure']) ? $object['date_heure'] : $object['date']));
	if ($GLOBALS['meta']['oss_indexation_'.$type.'_maj'])
		$document->newField('spip_up',       preg_replace('/[^\d]+/', '', $object['maj']));
	if ($GLOBALS['meta']['oss_indexation_'.$type.'_nom_site'])
		$document->newField('spip_site',	 $object['nom_site']);
	if ($GLOBALS['meta']['oss_indexation_'.$type.'_url_site'])
		$document->newField('spip_url',		 $object['url_site']);
	if ($GLOBALS['meta']['oss_indexation_'.$type.'_lang'])
		$document->newField('spip_lang',     $object['lang']);
	
	$document->newField('date_indexation', date('YmdHis'));
	
	
	if ($doUpdate) {
		$oss = oss_get_api_instance();
		$oss->update($index);
	}
	
}

function oss_delete($engineIds) {
	
	$oss = oss_get_api_instance();
	
	$engineIds = (array)$engineIds;
	if (!count($engineIds)) return;
	
	$query = $oss->search();
	return $query->filter('id:('.implode(" OR ", $engineIds).")")
		  		 ->delete(true)
				 ->execute();
	
}