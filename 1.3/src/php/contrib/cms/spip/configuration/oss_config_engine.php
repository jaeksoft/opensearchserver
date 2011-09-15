<?php
if (!defined("_ECRIRE_INC_VERSION")) return;

include_spip('inc/presentation');
include_spip('inc/config');

function configuration_oss_config_engine_dist() {
	global $spip_lang_left;

	$oss_engine_path  = $GLOBALS['meta']["oss_engine_path"];
	$oss_engine_index = $GLOBALS['meta']["oss_engine_index"];
	$oss_user_auth    = $GLOBALS['meta']["oss_user_auth"];
	$oss_user_login   = $GLOBALS['meta']["oss_user_login"];
	$oss_user_passwd  = $GLOBALS['meta']["oss_user_passwd"];
	
	$oss = oss_get_api_instance();
	$isRunning = $oss->isEngineRunning();
	$isIndexAvailable = ($isRunning) ? $oss->isIndexAvailable() : false;
	
	$authHidden        = ($oss_user_auth) ? "" : " style='display:none;' ";
	$faultOnEnginePath = (!$isRunning) ? " style='color:#A00000;font-style:italic'" : "";
	$faultOnIndexName  = (!$isIndexAvailable) ? " style='color:#A00000;font-style:italic'" : "";
	
	$res = "";
	if (!$isRunning || !$isIndexAvailable)
		$res .= '<img style="margin: 10px; width: 48px; height: 48px; float: right;" alt="Avertissement" src="../prive/images/warning.gif"/>';
	if ($isRunning === null)
		$res .= _T('oss:text_server_unavailable') . "<br/><br/>";
	elseif ($isRunning === false)
		$res .= _T('oss:text_engine_unavailable') . "<br/><br/>";
	elseif (!$isIndexAvailable)
		$res .= _T('oss:text_index_unavailable') . "<br/><br/>";
	
	$res .= "<table border='0' cellspacing='1' cellpadding='3' width=\"100%\" class='oss_config'>"

	. "<tr>"
	. "<td align='$spip_lang_left' class='verdana2'>"
	. "<label for='oss_engine_path' $faultOnEnginePath>" . _T('oss:engine_path') . "</label>"
	. "</td>"
	. "<td align='$spip_lang_left' class='verdana2'>"
	. "<input id='oss_engine_path' name='oss_engine_path' class='text' value='$oss_engine_path' />"
	. "</td><td class='status'>&nbsp;"
	. "</td></tr>\n"
	
	
	. "<tr>"
	. "<td align='$spip_lang_left' class='verdana2'>"
	. "<label for='oss_engine_path' $faultOnIndexName>" . _T('oss:engine_index') . "</label>"
	. "</td>"
	. "<td align='$spip_lang_left' class='verdana2'>"
	. "<input id='oss_engine_index' name='oss_engine_index' class='text' value='$oss_engine_index' />"
	. "</td><td class='status'>&nbsp;"
	. "</td></tr>\n"
	
	
	. "<tr>"
	. "<td align='$spip_lang_left' class='verdana2'>"
	. "<label for='oss_user_auth'>" . _T('oss:user_auth') . "</label>"
	. "</td>"
	. "<td align='$spip_lang_left' class='verdana2'>"
	. bouton_radio("oss_user_auth", "1", _T('item_oui'),  $oss_user_auth, "jQuery('tr.engine_user_auth').show()")
	. " &nbsp;"
	. bouton_radio("oss_user_auth", "0", _T('item_non'), !$oss_user_auth, "jQuery('tr.engine_user_auth').hide()")
	
	//. afficher_choix('oss_user_auth', $user_auth, array('1' => _T('item_oui'), '0' => _T('item_non')), " &nbsp; ")
	. "</td></tr>\n"
	
	
	. "<tr class=\"engine_user_auth\" $authHidden>"
	. "<td align='$spip_lang_left' class='verdana2'>"
	. "<label for='oss_user_login'>" . _T('oss:user_login') . "</label>"
	. "</td>"
	. "<td align='$spip_lang_left' class='verdana2'>"
	. "<input id='oss_user_login' name='oss_user_login' class='text' value='$oss_user_login' />"
	. "</td><td class='status'>&nbsp;"
	. "</td></tr>\n"
	
	
	. "<tr class=\"engine_user_auth\" $authHidden>"
	. "<td align='$spip_lang_left' class='verdana2'>"
	. "<label for='oss_user_passwd'>" . _T('oss:user_passwd') . "</label>"
	. "</td>"
	. "<td align='$spip_lang_left' class='verdana2'>"
	. "<input id='oss_user_passwd' name='oss_user_passwd' class='text' type='password' value='".str_repeat("*", strlen($oss_user_passwd))."' />"
	. "</td><td class='status'>&nbsp;"
	. "</td></tr>\n"
	
	. "</table>";

	$res = debut_cadre_relief("", true, "", _T(''))
	. ajax_action_post('oss_config_engine', 'path', 'path', '', $res)
	. fin_cadre_relief(true);

	return ajax_action_greffe('oss_config_engine-path', '', $res);

}
