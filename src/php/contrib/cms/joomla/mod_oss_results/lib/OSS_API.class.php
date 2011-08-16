<?php
/*
 *  This file is part of OpenSearchServer.
 *
 *  Copyright (C) 2008-2009 Emmanuel Keller / Jaeksoft
 *
 *  http://www.open-search-server.com
 *
 *  OpenSearchServer is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  OpenSearchServer is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with OpenSearchServer.  If not, see <http://www.gnu.org/licenses/>.
 */

if (!extension_loaded('curl')) { trigger_error("OSS_API won't work whitout curl extension", E_USER_ERROR); die(); }
if (!extension_loaded('SimpleXML')) { trigger_error("OSS_API won't work whitout SimpleXML extension", E_USER_ERROR); die(); }

if (!class_exists('RuntimeException')) { class RuntimeException extends Exception {} }
if (!class_exists('LogicException')) { class LogicException extends Exception {} }
if (!class_exists('InvalidArgumentException')) { class InvalidArgumentException extends LogicException {} }
if (!class_exists('OutOfRangeException')) { class OutOfRangeException extends LogicException {} }

/**
 * @author pmercier <pmercier-oss@nkubz.net>
 * @package OpenSearchServer
 */
class OSS_API {

	const VERSION = '1.1';
	
	const API_SELECT   = 'select';
	const API_UPDATE   = 'update';
	const API_DELETE   = 'delete';
	const API_OPTIMIZE = 'optimize';
	const API_RELOAD   = 'reload';

	/** @var int Default timeout (specified in seconds) for CURLOPT_TIMEOUT option. See curl documentation */
	const DEFAULT_QUERY_TIMEOUT = 0;

	/** @var int Timeout (specified in seconds) for CURLOPT_CONNECTTIMEOUT option. See curl documentation */
	const DEFAULT_CONNEXION_TIMEOUT = 5;

	protected static $supportedLanguages = array(
			""   => "Undefined",
			"zh" => "Chinese",
			"da" => "Danish",
			"nl" => "Dutch",
			"en" => "English",
			"fi" => "Finnish",
			"fr" => "French",
			"de" => "German",
			"hu" => "Hungarian",
			"it" => "Italian",
			"no" => "Norwegian",
			"pt" => "Portuguese",
			"ro" => "Romanian",
			"ru" => "Russian",
			"es" => "Spanish",
			"sv" => "Swedish",
			"tr" => "Turkish"
	);

	/* @var string */
	protected $enginePath;
	/* @var string */
	protected $index;

	/**
	 * @param $enginePath The URL to access the OSS Engine
	 * @param $index The index name
	 * @return OSS_API
	 */
	public function __construct($enginePath, $index = null) {
		$parsedPath = OSS_API::parseEnginePath($enginePath, $index);
		$this->enginePath	= $parsedPath['enginePath'];
		$this->index		= $parsedPath['index'];
	}

	/**
	 * @return OSS_Search
	 */
	public function select() {
		return $this->search();
	}

	/**
	 * @return OSS_Search
	 */
	public function search() {
		if (!class_exists('OSS_Search')) require (dirname(__FILE__).'/OSS_Search.class.php');
		return new OSS_Search($this->enginePath, $this->index);
	}

	/**
	 * Launch an optimize of the index
	 * @return boolean True on success
	 * See the OSS Wiki [Web API optimize] documentation before using this method
	 */
	public function optimize() {
		$return = $this->queryServer($this->getQueryURL(OSS_API::API_OPTIMIZE));
		return ($return !== false);
	}

	/**
	 * Reload the index
	 * @return boolean True on success
	 * See the OSS Wiki [Web API reload] documentation before using this method
	 */
	public function reload() {
		$return = $this->queryServer($this->getQueryURL(OSS_API::API_RELOAD));
		return ($return !== false);
	}

	/**
	 * Return the url to use with curl
	 * @param string $apiCall The Web API to call. Refer to the OSS Wiki documentation of [Web API]
	 * @return string
	 */
	protected function getQueryURL($apiCall) {
		$path = $this->enginePath.'/'.$apiCall;
		if (!empty($this->index)) $path .= '?use='.$this->index;
		return $path;
	}

	/**
	 * Send an xml list of documents to be indexed by the search engine
	 * @param mixed $xml Can be an xml string, a OSS_IndexDocument, a SimpleXMLElement,
	 *                   a DOMDocument or any object that implement the __toString
	 *                   magic method
	 * @return boolean True on success
	 */
	public function update($xml) {

		if (!is_string($xml)) {
			if ($xml instanceof DOMDocument) {
				$xml = $xml->saveXML();
			}
			elseif ($xml instanceof SimpleXMLElement) {
				$xml = $xml->asXML();
			}
			elseif (is_object($xml)) {
				if (method_exists($xml, '__toString') || $xml instanceof SimpleXMLElement) {
					$xml = $xml->__toString();
				}
			}
		}

		if (!is_string($xml)) {
			if (class_exists('OSSException'))
				throw new UnexpectedValueException('String, SimpleXMLElement or DOMDocument was expected for $xml.');
			trigger_error(__CLASS__.'::'.__METHOD__.'($xml): String, SimpleXMLElement or DOMDocument was expected for $xml.', E_USER_ERROR);
			return false;
		}

		$return = $this->queryServer($this->getQueryURL(OSS_API::API_UPDATE), $xml);
		return ($return !== false);

	}

	/**
	 * Post data to an URL
	 * @param string $url
	 * @param string $data Optional. If provided will use a POST method. Only accept
	 *                     data as POST encoded string or raw XML string.
	 * @param int $timeout Optional. Number of seconds before the query fail
	 * @return false|string
	 *
	 * Will fail if more than 16 HTTP redirection
	 * FIXME Explain Exceptions
	 */
	public static function queryServer($url, $data = null, $connexionTimeout = OSS_API::DEFAULT_CONNEXION_TIMEOUT, $timeout = OSS_API::DEFAULT_QUERY_TIMEOUT) {

		// Use CURL to post the data
		$rCurl = curl_init($url);
		curl_setopt($rCurl, CURLOPT_HTTP_VERSION, '1.0');
		curl_setopt($rCurl, CURLOPT_BINARYTRANSFER, true);
		curl_setopt($rCurl, CURLOPT_RETURNTRANSFER, true);
		curl_setopt($rCurl, CURLOPT_FOLLOWLOCATION, true);
		curl_setopt($rCurl, CURLOPT_MAXREDIRS, 16);
		curl_setopt($rCurl, CURLOPT_VERBOSE, true);

		if (is_integer($connexionTimeout) && $connexionTimeout >= 0)
			curl_setopt($rCurl, CURLOPT_CONNECTTIMEOUT, $connexionTimeout);

		if (is_integer($timeout) && $timeout >= 0)
			curl_setopt($rCurl, CURLOPT_TIMEOUT, $timeout);

		// Send provided string as POST data. Must be encoded to meet POST specification
		if ($data !== null) {
			curl_setopt($rCurl, CURLOPT_POST, true);
			curl_setopt($rCurl, CURLOPT_POSTFIELDS, (string)$data);
			curl_setopt($rCurl, CURLOPT_HTTPHEADER, array("Content-type: text/xml; charset=utf-8"));
		}

		$content = curl_exec($rCurl);
		
		if ($content === false) {
			if (class_exists('OSSException'))
				throw new RuntimeException('CURL failed to execute on URL "'.$url.'"');
			trigger_error('CURL failed to execute on URL "'.$url.'"', E_USER_WARNING);
			return false;
		}

		$aResponse 	= curl_getinfo($rCurl);

		// Must check return code
		if ($aResponse['http_code'] >= 400) {
			if (class_exists('OSSException'))
				throw new TomcatException($aResponse['http_code'], $content);
			trigger_error('HTTP ERROR '.$aResponse['http_code'].': "'.trim(strip_tags($content)).'"', E_USER_WARNING);
			return false;
		}

		// FIXME Possible problem to identify Locked Index message. Must set a lock on an index to check this
		if (OSS_API::isOSSError($content)) {
			if (class_exists('OSSException'))
				throw new OSSException($content);
			trigger_error('OSS Returned an error: "'.trim(strip_tags($content)).'"', E_USER_WARNING);
			return false;
		}
		
		return $content;
	}

	/**
	 * Check if the answer is an error returned by OSS
	 * @param $xml string, DOMDocument or SimpleXMLElement
	 * @return boolean True if error success
	 */
	public static function isOSSError($xml) {

		// Cast $xml param to be a SimpleXMLElement
		// If we don't find the word 'Error' in the xml string, exit immediatly
		if ($xml instanceof SimpleXMLElement) {
			if (strpos((string)$xml, 'Error') === false)
				return false;
			$xmlDoc = $xml;
		}
		elseif ($xml instanceof DOMDocument) {
			$xmlDoc = simplexml_import_dom($xml);
			if (strpos((string)$xmlDoc, 'Error') === false)
				return false;
		}
		else {
			if (strpos((string)$xml, 'Error') === false)
				return false;
			$previousErrorlevel = error_reporting(0);
			$xmlDoc = simplexml_load_string($xml);
			error_reporting($previousErrorlevel);
		}

		if (!$xmlDoc instanceof SimpleXMLElement) return false;

		// Make sure the Error we found was a Status Error
		foreach ($xmlDoc->entry as $entry) {
			if ($entry['key'] == 'Status' && $entry == 'Error') {
				return true;
			}
		}

		return false;
	}

	/**
	 * Parse the enginePath parameter to extract the index
	 * @param 
	 */
	public static function parseEnginePath($enginePath, $index = null) {
		
		$urlParams = array();
		// Extract the use param in the query part if any
		if (strpos($enginePath, '?') !== false) {
			$parsedURL = parse_url($enginePath);
			parse_str($parsedURL['query'], $urlParams);
			if (isset($urlParams['use'])) {
				$index = $urlParams['use'];
				$enginePath = str_replace('&&', '&', str_replace("use=".$urlParams['use'], '', $enginePath));
				if (substr($enginePath, -1) == '?')
					$enginePath = substr($enginePath, 0, -1);
			}
		}
		
		return array('enginePath' => $enginePath, 'index' => $index);
		
	}
	
	/**
	 * Return a list of supported language. Array is indexed by ISO 639-1 format (en, de, fr, ...)
	 * @return Array<String>
	 */
	public static function supportedLanguages() {
		return OSS_API::$supportedLanguages;
	}

	/**
	 * Escape special chars for lucene
	 * @param $string
	 * @return string
	 */
	public static function escape($string) {
		static $escaping = array(
			array("+",   "-",   "&&",   "||",  "!",  "(",  ")",  "{",  "}",  "[",  "]",  "^", "\"",  "~",  "*",  "?",  ":", '\\'),
			array('\+', '\-', '\&\&', '\|\|', '\!', '\(', '\)', '\{', '\}', '\[', '\]', '\^', '\"', '\~', '\*', '\?', '\:', '\\\\')
		);
		return str_replace($escaping[0], $escaping[1], $string);
	}
	
	/**
	 * Clean an UTF-8 string to prevent simpleXMLElement to fail on some characters (remove them)
	 * @param string $string
	 * @return string
	 */
	public static function cleanUTF8($string, $replacement = '') {

		static $remove = array(	"\x00", "\x01", "\x02", "\x03", "\x04", "\x05", "\x06", "\x07",
								"\x08",                 "\x0B", "\x0C",         "0x0E", "\x0F",
								"\x10", "\x11", "\x12", "\x13", "\x14", "\x15", "\x16", "\x17",
								"\x18", "\x19", "\x1A", "\x1B", "\x1C", "\x1D", "\x1E", "\x1F"
		);
		return str_replace($remove, $replacement, $string);
		
	}
	
}