<?php

if (!defined("_ECRIRE_INC_VERSION")) return;
include_spip('inc/oss');
include_spip('inc/OSS_API.class');
include_spip('inc/OSS_Search.class');

function inc_prepare_recherche($recherche, $table='articles', $cond=false, $serveur='') {
	/*
	array
	  0 => string 'resultats.points AS points' (length=26)
	  1 => string 'recherche='fb3e0f407037adf1'' (length=28)
	*/	
	
	$hash = substr(md5($recherche . $table),0,16);

	$type = oss_get_type_from_table($table);
	var_dump($type);

	// On récupère les données depuis OSS
	
	$search = new OSS_Search('http://localhost:8080/oss', 'spip_index');
	$search->query($recherche)->template('search')->rows(15)->filter('spip_type:"'.$type.'"');
	$result = $search->execute();
	
	var_dump($result);
	
	die();
	
	// supprimer les anciens resultats de cette recherche
	// et les resultats trop vieux avec une marge
	sql_delete('spip_resultats','(maj<DATE_SUB(NOW(), INTERVAL '.($delai_fraicheur+100)." SECOND)) OR (recherche='$hash')",$serveur);

	// inserer les resultats dans la table de cache des resultats
	if (count($points)){
		$tab_couples = array();
		foreach ($points as $id => $p){
			$tab_couples[] = array(
				'recherche' => $hash,
				'id' => $id,
				'points' => $p['score']
			);
		}
		sql_insertq_multi('spip_resultats',$tab_couples,array(),$serveur);
	}
	
	$result = array("resultats.points AS points", "recherche='fb3e0f407037adf1'");
	return $result;
}



?>
