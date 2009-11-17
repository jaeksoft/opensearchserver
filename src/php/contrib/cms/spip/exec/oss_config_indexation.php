<?php

if (!defined("_ECRIRE_INC_VERSION")) return;

include_spip('inc/presentation');
include_spip('inc/texte');
include_spip('inc/base');

function exec_oss_config_indexation_dist() {
	
	$indexation = charger_fonction('oss_config_indexation', 'configuration');

	if (_AJAX) {
		echo $indexation(_request('configuration'));
		return;	
	}
	
	pipeline('exec_init', array('args' => array('exec' => 'oss'), 'data' => ''));
	
	$commencer_page = charger_fonction('commencer_page', 'inc');
	echo $commencer_page(_T('oss:titre_page_iextra'), "configuration", "configuration");
    
	echo "<br/><br/><br/>";
	echo gros_titre(_T('oss:titre_cfg_indexation'), '', false);
	echo barre_onglets("onglet_oss", $_GET['exec']);
	
	echo debut_gauche('COLONE GAUCHE', true);
	echo pipeline('affiche_gauche', array('args' => array('exec' => 'oss'), 'data' => ''));
	
	// Cadre gauche
	echo debut_cadre_trait_couleur('../prive/images/', true);
	echo _T('oss:info_configuration_indexation');
	echo fin_cadre_trait_couleur(true);
	
	// Corp principal
	echo creer_colonne_droite('COLONEDROITE', true);
	echo pipeline('affiche_droite', array('args' => array('exec' => 'oss'), 'data' => ''));
	
	echo debut_droite('DEBUT COL DDROITE', true);

	echo debut_cadre_trait_couleur('../prive/images/referers-24.gif', true, null, _T('oss:indexation_global'));
	echo $indexation('global');
	echo fin_cadre_trait_couleur(true);
	
	echo debut_cadre_trait_couleur('../prive/images/article-24.gif', true, null, _T('oss:indexation_article'), null, 'indexation_global_part');
	echo $indexation('article');
	echo fin_cadre_trait_couleur(true);
	
	echo debut_cadre_trait_couleur('../prive/images/breve-24.gif', true, null, _T('oss:indexation_breve'), null, 'indexation_global_part');
	echo $indexation('breve');
	echo fin_cadre_trait_couleur(true);
	
	echo debut_cadre_trait_couleur('../prive/images/rubrique-24.gif', true, null, _T('oss:indexation_rubrique'), null, 'indexation_global_part');
	echo $indexation('rubrique');
	echo fin_cadre_trait_couleur(true);
	
	echo pipeline('affiche_milieu', array('args' => array('exec' => 'oss'), 'data' => ''));
	echo fin_gauche(), fin_page();
	
}
