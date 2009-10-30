<?php
include_spip('inc/oss');
include_spip('inc/OSS_API.class');
include_spip('inc/OSS_IndexDocument.class');
include_spip('inc/OSS_Search.class');

// Store date in GMT ?
// Provide user the ability to change the field affectations
// Leave him the ability to deceide what to index
// Give him the ability to decide what query template to use
// Leave him to decide to index the published, waiting, ... articles

//FIXME Must add charset encoding support
//FIXME Add support for parsing the body, header, ... (Like <img1|left> ...)
function oss_indexation($param) {

	$_REQUEST['debug'] = true;
	
	// On récupère je l'id du document a indéxer sur oss
	$load = $param['args'];
	$data = $param['data'];
	$type = oss_get_type_from_table($param['args']['table']);
	$id = intval($load['id_objet']);

	$object = oss_load_object($type, $id);
	
	if (!$object) return;
	
	// Not published ? Removing from the index
	if ($object['statut'] == 'publie') {
		oss_update($type, $object);
	}
	else {
		$engineId = oss_construct_uniqid($type, $object);
		oss_delete($engineId);
	}
	
}


// Réindexe TOUT les articles ou autres
// FIXME Push and delete documents by parts
function oss_reindexation($type = 'all') {
	
	
	$tables = (array)explode(',', oss_get_table_from_type($type));
	
	foreach ($tables as $table) {
		
		$type = oss_get_type_from_table($table);
		$index = new OSS_IndexDocument();
		
		$toDelete = array();
		
		$idName = 'id_'.$type;
		$dataList = sql_allfetsel($idName.', statut', $table);
		foreach ($dataList as $data) {
			
			$object = oss_load_object($type, $data[$idName]);
			
			// Desindexation
			if ($data['statut'] != 'publie') {
				$toDelete[] = oss_construct_uniqid($type, $object);
				continue;
			}
			else {
				oss_update($type, $object, $index);
			}
			
		}

		oss_delete($toDelete);
		
		$oss = oss_get_api_instance();
		$oss->update($index);
		
	}
	
}

function oss_update($type, $object, $index = null) {

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
		
		// Récupération des auteurs
		$authors = (array)sql_allfetsel("SA.id_auteur, SA.nom", "spip_auteurs_articles AS SAA INNER JOIN spip_auteurs AS SA ON SA.id_auteur = SAA.id_auteur", "SAA.id_article=".$idObjet);
		
		foreach ($authors as $key => $author)
			$authors[$key] = $author['id_auteur'].'|'.$author['nom'];
		$document->newField('spip_author', $authors);
		
	}
	elseif ($type == 'breve') {
		
	}
	elseif ($type == 'rubrique') {
		
		$document->newField('spip_header',   $object['descriptif']);
		
	}
	
	$document->newField('spip_suptitle', strip_tags($object['surtitre']));
	$document->newField('spip_title',    strip_tags($object['titre']));
	$document->newField('spip_subtitle', strip_tags($object['surtitre']));
	$document->newField('spip_header',   strip_tags($object['chapo']));
	$document->newField('spip_body',     strip_tags($object['texte']));
	$document->newField('spip_date',     preg_replace('/[^\d]+/', '', isset($object['date_heure']) ? $object['date_heure'] : $object['date']));
	$document->newField('spip_up',       preg_replace('/[^\d]+/', '', $object['maj']));
	$document->newField('spip_site',	 $object['nom_site']);
	$document->newField('spip_url',		 $object['url_site']);
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

?>