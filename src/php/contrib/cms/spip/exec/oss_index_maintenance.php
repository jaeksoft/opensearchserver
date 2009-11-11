<?php

if (!defined("_ECRIRE_INC_VERSION")) return;

include_spip('inc/presentation');
include_spip('inc/texte');
include_spip('inc/base');

function exec_oss_index_maintenance_dist() {
	
	pipeline('exec_init', array('args' => array('exec' => 'oss'), 'data' => ''));
	
	$commencer_page = charger_fonction('commencer_page', 'inc');
	echo $commencer_page(_T('oss:titre_page_iextra'), "configuration", "configuration");
    
	echo "<br/><br/><br/>";
	echo gros_titre(_T('oss:titre_cfg_maintenance'), '', false);
	echo barre_onglets("onglet_oss", $_GET['exec']);
	
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

	echo debut_cadre_sous_rub('', true, false, _T('oss:reindexer_base'));
	$res = generer_form_ecrire('oss_maintenance_reindexation', "", '', _T('bouton_maintenance_reindexer'));
	echo 
		'<img src="' .  chemin_image('warning.gif') . '" alt="',
	  	_T('info_avertissement'),
		"\" style='width: 48px; height: 48px; float: right;margin: 10px;' />",
	  _T('texte_maintenance_reindexer'),
		"<br class='nettoyeur' />",
		"\n<div style='text-align: center'>",
		"\n<div class='serif'>",
		"\n<b>"._T('avis_maintenance_reindexer')."&nbsp;!</b>",
		$res,
		"\n</div>",
		"</div>";
	echo fin_cadre_sous_rub(true);

	echo debut_cadre_sous_rub('', true, false, _T('oss:vider_l_index'));
	$res = generer_form_ecrire('oss_maintenance_vider', "", '', _T('bouton_maintenance_vider'));
	echo 
		'<img src="' .  chemin_image('warning.gif') . '" alt="',
	  	_T('info_avertissement'),
		"\" style='width: 48px; height: 48px; float: right;margin: 10px;' />",
	  _T('texte_maintenance_vider'),
		"<br class='nettoyeur' />",
		"\n<div style='text-align: center'>",
		debut_boite_alerte(),
		"\n<div class='serif'>",
		"\n<b>"._T('avis_maintenance_vider')."&nbsp;!</b>",
		$res,
		"\n</div>",
		fin_boite_alerte(),
		"</div>";
	echo fin_cadre_sous_rub(true);
	echo debut_cadre_sous_rub('', true, false, _T('oss:optimiser_l_index'));
	$res = generer_form_ecrire('oss_maintenance_optimiser', "", '', _T('bouton_maintenance_optimiser'));
	echo 
		'<img src="' .  chemin_image('warning.gif') . '" alt="',
	  	_T('info_avertissement'),
		"\" style='width: 48px; height: 48px; float: right;margin: 10px;' />",
	  _T('texte_maintenance_optimiser'),
		"<br class='nettoyeur' />",
		"\n<div style='text-align: center'>",
		"\n<div class='serif'>",
		"\n<b>"._T('avis_maintenance_optimiser')."&nbsp;!</b>",
		$res,
		"\n</div>",
		"</div>";
	echo fin_cadre_sous_rub(true);

	echo debut_cadre_sous_rub('', true, false, _T('oss:recharger_l_index'));
	$res = generer_form_ecrire('oss_maintenance_recharger', "", '', _T('bouton_maintenance_recharger'));
	echo 
		'<img src="' .  chemin_image('warning.gif') . '" alt="',
	  	_T('info_avertissement'),
		"\" style='width: 48px; height: 48px; float: right;margin: 10px;' />",
	  _T('texte_maintenance_recharger'),
		"<br class='nettoyeur' />",
		"\n<div style='text-align: center'>",
		"\n<div class='serif'>",
		"\n<b>"._T('avis_maintenance_recharger')."&nbsp;!</b>",
		$res,
		"\n</div>",
		"</div>";
	echo fin_cadre_sous_rub(true);
	
	echo pipeline('affiche_milieu', array('args' => array('exec' => 'oss'), 'data' => ''));
	echo fin_gauche(), fin_page();
	
}
