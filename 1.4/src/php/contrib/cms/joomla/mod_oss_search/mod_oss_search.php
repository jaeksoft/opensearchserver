<?php

// no direct access
defined('_JEXEC') or die('Restricted access');

// Include the syndicate functions only once
require_once( dirname(__FILE__).DS.'helper.php' );

$moduleclass_sfx = $params->get('moduleclass_sfx', '');
require(JModuleHelper::getLayoutPath('mod_oss_search'));