<?php

include_spip('inc/config');
function action_oss_config_engine_dist() {
	
	$securiser_action = charger_fonction('securiser_action', 'inc');
	$arg = $securiser_action();
	$r = rawurldecode(_request('redirect'));
	$r = parametre_url($r, 'configuration', $arg,"&");
	
	$valids = array(
		'oss_engine_path',
		'oss_engine_index',
		'oss_user_auth',
		'oss_user_login',
		'oss_user_passwd'
	);
	
	foreach ($valids as $meta) {
		$value = _request($meta);
		
		// Ignore passwd with only *
		if ($meta == 'oss_user_passwd' && $value == str_repeat('*', strlen($value)))
			continue;
		if (($x =$value)!==NULL)
			ecrire_meta($meta, $value);
		elseif  (!isset($GLOBALS['meta'][$meta]))
			ecrire_meta($meta, $value);
	}
	
	redirige_par_entete($r);
	
}