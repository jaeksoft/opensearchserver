<?php
if (!defined("_ECRIRE_INC_VERSION")) return;

include_spip('inc/presentation');
include_spip('inc/config');
include_spip('inc/base');

function configuration_oss_config_indexation_dist($type) {
	
	global $spip_lang_left;

	if ($type == 'global') {
		$field		= 'oss_indexation_global_enabled';
		$fieldValue = $GLOBALS['meta'][$field];
		if (!$fieldValue) {
			$res = "<div id=\"oss_indexation_global_will_be_disabled\">"
				 . '<img style="margin: 10px; width: 48px; height: 48px; float: right;" alt="Avertissement" src="../prive/images/warning.gif"/>'
				 . _T('oss:oss_indexation_global_is_disabled') . "<br/><br/>"
			     . "</div>";
		}
		else {
			$res = "<div id=\"oss_indexation_global_will_be_disabled\" style=\"display:none\">"
				 . '<img style="margin: 10px; width: 48px; height: 48px; float: right;" alt="Avertissement" src="../prive/images/warning.gif"/>'
				 . _T('oss:oss_indexation_global_will_be_disabled') . "<br/><br/>"
			     . "</div>";
		}
		$res.= "<table border='0' cellspacing='1' cellpadding='3' width=\"100%\" class='oss_config'>"
			 . "<tr>"
			 . "<td align='$spip_lang_left' class='verdana2'>"
			 . "<label for='oss_indexation_global_enabled'>" . _T('oss:oss_indexation_global_enabled') . "</label>"
			 . "</td>"
			 . "<td align='$spip_lang_left' class='verdana2'>"
			 . bouton_radio('oss_indexation_global_enabled', "1", _T('item_oui'),  $fieldValue, "jQuery('#oss_indexation_global_will_be_disabled').hide()")
			 . " &nbsp;"
			 . bouton_radio('oss_indexation_global_enabled', "0", _T('item_non'), !$fieldValue, "jQuery('#oss_indexation_global_will_be_disabled').show()")
			 . "</td></tr></table>";
		
	}
	else {
		$indexable = oss_get_indexable_fields();
		$valid = array();
		$suffix = 'oss_indexation_'.$type.'_';
		foreach ($indexable[$type] as $field) {
			$valid[] = $suffix.$field;
		}
		
		$enField      = $suffix.'enabled';
		$enFieldValue = $GLOBALS['meta'][$enField];
		$fieldHidden  = $enFieldValue ? '' : 'style="display:none"';
		$res = "<table border='0' cellspacing='1' cellpadding='3' width=\"100%\" class='oss_config'>"
			 . "<tr>"
			 . "<td align='$spip_lang_left' class='verdana2' style=\"border-bottom:1px solid black\">"
			 . "<label for='$enField'>" . _T('oss:'.$enField) . "</label>"
			 . "</td>"
			 . "<td align='$spip_lang_left' class='verdana2' style=\"border-bottom:1px solid black\">"
			 . bouton_radio($enField, "1", _T('item_oui'),  $enFieldValue, "jQuery('tr.$enField').show()")
			 . " &nbsp;"
			 . bouton_radio($enField, "0", _T('item_non'), !$enFieldValue, "jQuery('tr.$enField').hide()")
			 . "</td></tr>";
		
		foreach ($indexable[$type] as $field) {
			if (substr($field, 0, 3) == 'id_') continue;
			$field      = $suffix.$field;
			$fieldValue = $GLOBALS['meta'][$field];
			$res .= "<tr class=\"$enField\" $fieldHidden>"
				 .  "<td align='$spip_lang_left' class='verdana2'>"
				 .  "<label for='$field'>" . _T('oss:'.$field) . "</label>"
				 .  "</td>"
				 .  "<td align='$spip_lang_left' class='verdana2'>"
				 .  bouton_radio($field, "1", _T('item_oui'),  $fieldValue)
				 .  " &nbsp;"
				 .  bouton_radio($field, "0", _T('item_non'), !$fieldValue)
				 .  "</td></tr>\n";
		}
		$res .= '</table>';
	}
	
	$res = debut_cadre_relief("", true, "", _T(''))
	. ajax_action_post('oss_config_indexation', $type, $type, '', $res)
	. fin_cadre_relief(true);

	return ajax_action_greffe('oss_config_indexation-'.$type, '', $res);

}
