<?php
include_spip('inc/article_select'); // necessaire si appel de l'espace public
include_spip('inc/OSS_API.class'); // necessaire si appel de l'espace public
include_spip('inc/OSS_IndexDocument.class'); // necessaire si appel de l'espace public

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
	$idObjet = intval($load['id_objet']);
	
	// Récupération du document
	$object = inc_article_select_dist($idObjet);
	
	// Récupération des auteurs
	$authors = (array)sql_allfetsel("SA.id_auteur, SA.nom", "spip_auteurs_articles AS SAA INNER JOIN spip_auteurs AS SA ON SA.id_auteur = SAA.id_auteur", "SAA.id_article=".$idObjet);
	
	$index = new OSS_IndexDocument();
	
	$document = $index->newDocument($object['lang']);
	
	$document->newField('spip_id', 	 	 $object['id_article']);
	$document->newField('spip_type', 	 $param['surtitre']);
	$document->newField('spip_suptitle', $object['surtitre']);
	$document->newField('spip_title',    $object['titre']);
	$document->newField('spip_subtitle', $object['surtitre']);
	$document->newField('spip_header',   $object['chapo']);
	$document->newField('spip_body',     $object['texte']);
	$document->newField('spip_date',     preg_replace('/[^\d]+/', '', $object['date']));
	$document->newField('spip_up',       preg_replace('/[^\d]+/', '', $object['maj']));
	$document->newField('spip_site',	 $object['nom_site']);
	$document->newField('spip_url',		 $object['url_site']);
	
	foreach ($authors as $key => $author)
		$authors[$key] = $author['id_auteur'].'|'.$author['nom'];
	$document->newField('spip_author', $authors);
	
	$oss = new OSS_API('http://localhost:8080/oss', 'spip_index');
	$oss->update($index);
	
}
?>