<?php

if (!defined("_ECRIRE_INC_VERSION")) return;

include_spip('inc/presentation');
include_spip('inc/texte');
include_spip('inc/oss');

function exec_oss_info_dist() {
	
	pipeline('exec_init', array('args' => array('exec' => 'oss'), 'data' => ''));
	
	$commencer_page = charger_fonction('commencer_page', 'inc');
	echo $commencer_page(_T('oss:titre_page_iextra'), "configuration", "configuration");
    
	echo "<br/><br/><br/>";
	echo gros_titre(_T('oss:titre_cfg_moteur'), '', false);
	echo barre_onglets("onglet_oss", "oss_info");
	
	echo debut_gauche('COLONE GAUCHE', true);
	echo pipeline('affiche_gauche', array('args' => array('exec' => 'oss'), 'data' => ''));
	
	// Cadre gauche
	echo debut_cadre_sous_rub('', true, false, _T('oss:documents_indexes'));
	//echo recuperer_fond('squelettes/cadre_gauche_info', array());
	$counts = oss_get_count();
	if (count($counts)) {
		echo '<table width="100%" cellspacing="1" cellpadding="3" border="0"><tbody>';
		foreach ($counts as $type => $count) {
			if ($type == 'all') continue;
			echo "<tr><td>", $type, "</td><td>", $count, "</td></tr>";
		}
		echo "</tbody><tfoot><tr><td>", _T('oss:nombre_de_documents_indexes'), "</td><td>", $counts['all'], "</td></tr></tfoot></table>";
	}
	echo fin_cadre_sous_rub(true);
	
	// Corp principal
	echo creer_colonne_droite('COLONEDROITE', true);
	echo pipeline('affiche_droite', array('args' => array('exec' => 'oss'), 'data' => ''));
	
	echo debut_droite('DEBUT COL DDROITE', true);

	
	echo debut_cadre_sous_rub('', true, false, _T('oss:moteur'));
	$infos = array(
		'oss_engine_url' => $GLOBALS['meta']['oss_engine_path'],
		'version_moteur' => '1.1.1beta',
		'utilisateur' => 'pmercier',
		'password' => '*******',
	);
	echo '<table width="100%" cellspacing="1" cellpadding="3" border="0"><tbody>';
	foreach ($infos as $k => $v) {
		echo "<tr><td>", _T('oss:'.$k), "</td><td>", $v, "</td></tr>";
	}
	echo "</tbody></table>";
	echo fin_cadre_sous_rub(true);
	
	echo debut_cadre_sous_rub('', true, false, _T('oss:index'));
	$infos = array(
		'oss_engine_index' => $GLOBALS['meta']['oss_engine_index'],
		'taille_indexes' => 'pas lourd',
		'nombre_de_documents_indexes' => $counts['all'],
		'dernier_document_indexe' => 'blabla',
		'date_derniere_indexation' => 'bling bling',
		'status_writer' => 'fermé',
	);
	echo '<table width="100%" cellspacing="1" cellpadding="3" border="0"><tbody>';
	foreach ($infos as $k => $v) {
		echo "<tr><td>", _T('oss:'.$k), "</td><td>", $v, "</td></tr>";
	}
	echo "</tbody></table>";
	
	echo fin_cadre_sous_rub(true);
	
	echo pipeline('affiche_milieu', array('args' => array('exec' => 'oss'), 'data' => ''));
	echo fin_gauche(), fin_page();
	
}

function autoriser_oss_configuration_bouton_dist($faire, $type, $id, $qui, $opt) {
	return ($qui['statut'] == '0minirezo');
}
