<?php

if (!defined("_ECRIRE_INC_VERSION")) return;

function oss_get_index_fields() {
	
	return array(
		'id',
		'title',
		'subtitle',
		'suptitle',
		'content',
		'description',
		'header',
		'footer',
		'dateCreation',
		'datePublication',
		'url'
	);
	
}