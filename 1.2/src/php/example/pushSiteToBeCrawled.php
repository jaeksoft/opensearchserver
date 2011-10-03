<?php

header('Content-type: text/html; charset=UTF-8');

define('BASE_DIR', dirname(__FILE__));
require BASE_DIR.'/../lib/misc.lib.php';
require BASE_DIR.'/../lib/OSS_API.class.php';
require BASE_DIR.'/../lib/OSS_Search.class.php';

$oss = new OSS_API('http://localhost:8080/oss', 'emptyWebCrawler');

$oss->pattern(array(
	'http://www.google.com/*',
	'http://www.j3tel.fr',
	'http://apple-tv-hacks.com/*'
), true);