<?php

include_spip('inc/config');
include_spip('inc/base');

function action_oss_config_indexation_dist() {
	
	$securiser_action = charger_fonction('securiser_action', 'inc');
	$arg = $securiser_action();
	$r = rawurldecode(_request('redirect'));
	$r = parametre_url($r, 'configuration', $arg, "&");
	
	$indexable = oss_get_indexable_fields();
	$valids = array('oss_indexation_global_enabled');
	foreach ($indexable as $type => $fields) {
		$suffix = 'oss_indexation_'.$type.'_';
		$valids[] = $suffix.'enabled';
		foreach ($fields as $field) {
			$valids[] = $suffix.$field;
		}
	}
	
	foreach ($valids as $meta) {
		$value = _request($meta);
		if ($value !== NULL || !isset($GLOBALS['meta'][$meta]))
			ecrire_meta($meta, $value);
	}
	
	redirige_par_entete($r);
	
}