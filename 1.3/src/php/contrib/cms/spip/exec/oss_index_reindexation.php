<?php

if (!defined("_ECRIRE_INC_VERSION")) return;

include_spip('inc/presentation');
include_spip('inc/texte');
include_spip('inc/base');

function exec_oss_index_reindexation_dist() {
	
	pipeline('exec_init', array('args' => array('exec' => 'oss'), 'data' => ''));
	
	$commencer_page = charger_fonction('commencer_page', 'inc');
	echo $commencer_page(_T('oss:titre_page_iextra'), "configuration", "configuration");
    
	echo gros_titre(_T('oss:titre_cfg_moteur'), '', false);
	//echo barre_onglets("configuration", "interface_extra");
	
	echo debut_gauche('COLONE GAUCHE', true);
	echo pipeline('affiche_gauche', array('args' => array('exec' => 'oss'), 'data' => ''));
	
	// Cadre gauche
	echo debut_cadre_trait_couleur('', true, false, 'Title');
	echo recuperer_fond('squelettes/cadre_gauche_info', array());
	echo fin_cadre_trait_couleur(true);
	
	// Corp principal
	echo creer_colonne_droite('COLONEDROITE', true);
	echo pipeline('affiche_droite', array('args' => array('exec' => 'oss'), 'data' => ''));
	
	echo debut_droite('DEBUT COL DDROITE', true);
	
	oss_reindexation('all');
	
	echo pipeline('affiche_milieu', array('args' => array('exec' => 'oss'), 'data' => ''));
	echo fin_gauche(), fin_page();
	
}
