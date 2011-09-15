<?php

if (!defined("_ECRIRE_INC_VERSION")) return;

include_spip('inc/presentation');
include_spip('inc/texte');
include_spip('inc/base');

function exec_oss_config_engine_dist() {

	$engine = charger_fonction('oss_config_engine', 'configuration');

	if (_AJAX) {
		echo $engine();
		return;	
	}
	
	pipeline('exec_init', array('args' => array('exec' => 'oss'), 'data' => ''));
	
	$commencer_page = charger_fonction('commencer_page', 'inc');
	echo $commencer_page(_T('oss:page_title_info'), "configuration", "configuration");
    
	echo "<br/>";
	echo gros_titre('Open Search Server', '', false);
	echo "<br/>";
	echo barre_onglets("onglet_oss", $_GET['exec']);
	echo "<br/>";

	
	echo debut_gauche('COLONE GAUCHE', true);
	echo pipeline('affiche_gauche', array('args' => array('exec' => 'oss'), 'data' => ''));
	
	
	echo creer_colonne_droite('COLONEDROITE', true);
	echo pipeline('affiche_droite', array('args' => array('exec' => 'oss'), 'data' => ''));
	echo debut_droite('DEBUT COL DDROITE', true);

	echo debut_cadre_trait_couleur('', true, null, _T('oss:engine'));
	
	echo $engine();
	
	echo fin_cadre_trait_couleur(true);
	
	echo pipeline('affiche_milieu', array('args' => array('exec' => 'oss'), 'data' => ''));
	echo fin_gauche(), fin_page();
	
}
