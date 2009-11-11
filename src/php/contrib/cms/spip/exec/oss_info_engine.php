<?php

if (!defined("_ECRIRE_INC_VERSION")) return;

include_spip('inc/presentation');
include_spip('inc/texte');
include_spip('inc/base');

function exec_oss_info_engine_dist() {
	
	$oss = oss_get_api_instance();
	$isRunning = $oss->isEngineRunning();
	$isIndexAvailable = ($isRunning) ? $oss->isIndexAvailable() : false;
	
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
	
	
	// *** Indexed documents ************************************************
	if ($isIndexAvailable) {
	echo debut_cadre_sous_rub('', true, false, _T('oss:indexed_documents'));
		$counts = oss_get_count();
		if (count($counts)) {
			echo '<table width="100%" cellspacing="1" cellpadding="3" border="0"><tbody>';
			foreach ($counts as $type => $count) {
				if ($type == 'all') continue;
				echo "<tr><td>", $type, "</td><td>", $count, "</td></tr>";
			}
			echo "</tbody><tfoot><tr><td>", _T('oss:indexed_document_count'), "</td><td>", $counts['all'], "</td></tr></tfoot></table>";
		}
		echo fin_cadre_sous_rub(true);
	}
	
	echo creer_colonne_droite('COLONEDROITE', true);
	echo pipeline('affiche_droite', array('args' => array('exec' => 'oss'), 'data' => ''));
	echo debut_droite('DEBUT COL DDROITE', true);

	
	// *** Engine ***********************************************************
	if ($isRunning)
		echo debut_cadre_sous_rub('', true, false, _T('oss:engine'), null, 'available');
	else
		echo debut_cadre_sous_rub('', true, false, _T('oss:engine_unavailabe'), null, 'unavailable');
	
	if (!$isRunning)
		echo '<img style="margin: 10px; width: 48px; height: 48px; float: right;" alt="Avertissement" src="../prive/images/warning.gif"/>';
	if ($isRunning === null)
		echo _T('oss:text_server_unavailable'), "<br/><br/>";
	elseif ($isRunning === false)
		echo _T('oss:text_engine_unavailable'), "<br/><br/>";
	
	$infos = $oss->getEngineInformations();
	echo '<table width="100%" cellspacing="1" cellpadding="3" border="0"><tbody>';
	foreach ($infos as $k => $v) {
		echo "<tr><td>", _T('oss:'.$k), "</td><td>", $v, "</td></tr>";
	}
	echo "</tbody></table>";
	echo fin_cadre_sous_rub(true);
	
	
	// *** Index ************************************************************
	if ($isRunning) {
		if ($isIndexAvailable) {
			echo debut_cadre_sous_rub('', true, false, _T('oss:index'), null, 'available');
			$infos = $oss->getIndexInformations();
			echo '<table width="100%" cellspacing="1" cellpadding="3" border="0"><tbody>';
			foreach ($infos as $k => $v) {
				echo "<tr><td>", _T('oss:'.$k), "</td><td>", $v, "</td></tr>";
			}
			echo "</tbody></table>";
		}
		else {
			echo debut_cadre_sous_rub('', true, false, _T('oss:index_unavailable'), null, 'unavailable');
			echo '<img style="margin: 10px; width: 48px; height: 48px; float: right;" alt="Avertissement" src="../prive/images/warning.gif"/>';
			echo _T('oss:text_index_unavailable');
		}
		echo fin_cadre_sous_rub(true);
	}
	
	echo pipeline('affiche_milieu', array('args' => array('exec' => 'oss'), 'data' => ''));
	echo fin_gauche(), fin_page();
	
}
