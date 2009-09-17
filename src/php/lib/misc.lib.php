<?php

/**
 * Store and retrieve a value from the browser (In order REQUEST, COOKIE, DEFAULT)
 * @return unknown_type
 */
function configRequestValue($key, $default, $requestField = null) {
	if (!empty($_REQUEST[$requestField])) {
		$value = $_REQUEST[$requestField];
		setcookie($key, $value, time() + 3600 * 365);
	}
	if (!$value) $value = $_COOKIE[$key];
	if (!$value) $value  = $default;
	return $value;
}