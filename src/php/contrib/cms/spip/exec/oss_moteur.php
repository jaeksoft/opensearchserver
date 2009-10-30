<?php

if (!defined("_ECRIRE_INC_VERSION")) return;

include_spip('inc/presentation');
include_spip('inc/texte');
include_spip('inc/oss');

function exec_oss_moteur_dist() {
	
	pipeline('exec_init', array('args' => array('exec' => 'oss'), 'data' => ''));
	
	$commencer_page = charger_fonction('commencer_page', 'inc');
	echo $commencer_page(_T('oss:titre_page_iextra'), "configuration", "configuration");
    
	echo "<br/><br/><br/>";
	echo gros_titre(_T('oss:titre_cfg_moteur'), '', false);
	echo barre_onglets("onglet_oss", "oss_moteur");
	
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
	
	echo recuperer_fond('squelettes/config_indexation', array(
		'titre' => _T('oss:titre_cfg_champs'),
		'redirect' => generer_url_ecrire("oss"),
		'icone_retour' => icone_inline(_T('icone_retour'), generer_url_ecrire('oss'), find_in_path("images/icon_oss.png"), "rien.gif", $GLOBALS['spip_lang_left']),
	));
	
	echo pipeline('affiche_milieu', array('args' => array('exec' => 'oss'), 'data' => ''));
	echo fin_gauche(), fin_page();
	
}

function autoriser_oss_configuration_bouton_dist($faire, $type, $id, $qui, $opt) {
	return ($qui['statut'] == '0minirezo');
}
