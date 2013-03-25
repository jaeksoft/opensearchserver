<?php

include_spip('inc/base');

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


function oss_header_prive($head) {
	$head .= '<link href="'._DIR_PLUGIN_OSS.'/squelettes/style.css" type="text/css" rel="stylesheet">';
	return $head."\n";
}

?>