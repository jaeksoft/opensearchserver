<?php

if (!defined("_ECRIRE_INC_VERSION")) return;
include_spip('inc/base');
include_spip('inc/OSS_API.class');
include_spip('inc/OSS_Search.class');

function inc_prepare_recherche($recherche, $table='articles', $cond=false, $serveur='') {
	$hash = substr(md5($recherche . $table),0,16);

	$type = oss_get_type_from_table($table);

	// On récupère les données depuis OSS
	$oss = oss_get_api_instance();
	
	$search = $oss->search();
	$search->query($recherche)->template('search')->rows(15)->filter('spip_type:"'.$type.'"');
	$result = $search->execute();
	
	// supprimer les anciens resultats de cette recherche
	// et les resultats trop vieux avec une marge
	sql_delete('spip_resultats','(maj<DATE_SUB(NOW(), INTERVAL '.($delai_fraicheur+100)." SECOND)) OR (recherche='$hash')",$serveur);

	$documents = $result->result;
	
	// inserer les resultats dans la table de cache des resultats
	if ($documents->doc) {
		$tab_couples = array();
		foreach ($documents->doc as $document){
			$id = (array)$document->xpath('field[@name="spip_id"]');
			$id = (string)reset($id);
			$tab_couples[] = array(
				'recherche' => $hash,
				'id' => $id,
				'points' => intval((float)$document['score'] * 100000)
			);
		}
		sql_insertq_multi('spip_resultats',$tab_couples,array(),$serveur);
	}
	return array("resultats.points AS points", "recherche='".$hash."'");
}



?>
