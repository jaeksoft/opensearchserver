<?php
/*
 *  This file is part of OpenSearchServer.
*
*  Copyright (C) 2008-2012 Emmanuel Keller / Jaeksoft
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

abstract class OssAbstract {

  protected $enginePath;
  protected $index;
  protected $login;
  protected $apiKey;
  protected $lastQueryString;

  public function init($enginePath, $index = NULL, $login = NULL, $apiKey = NULL) {
    if (!function_exists('OssApi_Dummy_Function')) {
      function OssApi_Dummy_Function() {
      }
    }
    $this->lastQueryString = null;
    $this->enginePath = $enginePath;
    $this->index = $index;
    $this->credential($login, $apiKey);
  }

  /**
   * Assign credential information for the next queries
   * @param $login string
   * @param $apiKey string
   * If $login is empty, credential is removed
   */
  public function credential($login, $apiKey) {
    // Remove credentials
    if (empty($login)) {
      $this->login  = NULL;
      $this->apiKey  = NULL;
      return;
    }

    // Else parse and affect new credentials
    if (empty($login) || empty($apiKey)) {
      if (class_exists('OssException')) {
        throw new UnexpectedValueException('You must provide a login and an api key to use credential.');
      }
      trigger_error(__CLASS__ . '::' . __METHOD__ . ': You must provide a login and an api key to use credential.', E_USER_ERROR);
      return FALSE;
    }

    $this->login  = $login;
    $this->apiKey  = $apiKey;
  }

  /**
   * Return the url to use with curl
   * param string $apiCall The Web API to call. Refer to the OSS Wiki documentation of [Web API]
   * param string[] $options Additional query parameters
   * return string
   * Optionals query parameters are provided as a named list:
   * array(
   *   "arg1" => "value1",
   *   "arg2" => "value2"
   * )
   */
  protected function getQueryURL($apiCall, $options = NULL) {

    $path = $this->enginePath . '/' . $apiCall;
    $chunks = array();

    if (!empty($this->index)) {
      $chunks[] = 'use=' . urlencode($this->index);
    }

    // If credential provided, include them in the query url
    if (!empty($this->login)) {
      $chunks[] = "login=" . urlencode($this->login);
      $chunks[] = "key="  . urlencode($this->apiKey);
    }

    // Prepare additionnal parameters
    if (is_array($options)) {
      foreach ($options as $argName => $argValue) {
        $chunks[] = $argName . "=" . urlencode($argValue);
      }
    } else if ($options != null) {
      $chunks[] = $options;
    }

    $path .= (strpos($path, '?') !== FALSE ? '&' : '?') . implode('&', $chunks);

    return $path;
  }

  /**
   * @return string The parsed engine path
   */
  public function getEnginePath() {
    return $this->enginePath;
  }

  /**
   * @return string The parsed index (NULL if not specified)
   */
  public function getIndex() {
    return $this->index;
  }

  /**
   * Post data to an URL
   * @param string $url
   * @param string $data Optional. If provided will use a POST method. Only accept
   *                     data as POST encoded string or raw XML string.
   * @param int $timeout Optional. Number of seconds before the query fail
   * @return FALSE|string
   *
   * Will fail if more than 16 HTTP redirection
   */
  protected function queryServer($url, $data = NULL, $connexionTimeout = OssApi::DEFAULT_CONNEXION_TIMEOUT, $timeout = OssApi::DEFAULT_QUERY_TIMEOUT) {

    $this->lastQueryString = $url;
    // Use CURL to post the data

    $rcurl = curl_init($url);
    curl_setopt($rcurl, CURLOPT_HTTP_VERSION, '1.0');
    curl_setopt($rcurl, CURLOPT_BINARYTRANSFER, TRUE);
    curl_setopt($rcurl, CURLOPT_RETURNTRANSFER, TRUE);
    curl_setopt($rcurl, CURLOPT_FOLLOWLOCATION, FALSE);
    curl_setopt($rcurl, CURLOPT_MAXREDIRS, 16);
    curl_setopt($rcurl, CURLOPT_VERBOSE, FALSE);

    if (is_integer($connexionTimeout) && $connexionTimeout >= 0) {
      curl_setopt($rcurl, CURLOPT_CONNECTTIMEOUT, $connexionTimeout);
    }

    if (is_integer($timeout) && $timeout >= 0) {
      curl_setopt($rcurl, CURLOPT_TIMEOUT, $timeout);
    }

    // Send provided string as POST data. Must be encoded to meet POST specification
    if ($data !== NULL) {
      curl_setopt($rcurl, CURLOPT_POST, TRUE);
      curl_setopt($rcurl, CURLOPT_POSTFIELDS, (string)$data);
      curl_setopt($rcurl, CURLOPT_HTTPHEADER, array("Content-type: text/xml; charset=utf-8"));
    }

    set_error_handler('OssApi_Dummy_Function', E_ALL);
    $content = curl_exec($rcurl);
    restore_error_handler();

    if ($content === FALSE) {
      if (class_exists('OssException')) {
        throw new RuntimeException('CURL failed to execute on URL "' . $url . '"');
      }
      trigger_error('CURL failed to execute on URL "' . $url . '"', E_USER_WARNING);
      return FALSE;
    }

    $aResponse   = curl_getinfo($rcurl);

    // Must check return code
    if ($aResponse['http_code'] >= 400) {
      if (class_exists('OssException')) {
        throw new TomcatException($aResponse['http_code'], $content);
      }
      trigger_error('HTTP ERROR ' . $aResponse['http_code'] . ': "' . trim(strip_tags($content)) . '"', E_USER_WARNING);
      return FALSE;
    }

    // FIXME Possible problem to identify Locked Index message. Must set a lock on an index to check this
    if ($this->isOSSError($content)) {
      if (class_exists('OssException')) {
        throw new OssException($content);
      }
      trigger_error('OSS Returned an error: "' . trim(strip_tags($content)) . '"', E_USER_WARNING);
      return FALSE;
    }

    return $content;
  }

  public function getLastQueryString() {
    return $this->lastQueryString;
  }

  protected function queryServerTXT($path, $params = null, $data = null, $connexionTimeout = OssApi::DEFAULT_CONNEXION_TIMEOUT, $timeout = OssApi::DEFAULT_QUERY_TIMEOUT) {
    return $this->queryServer($this->getQueryURL($path, $params), $data, $connexionTimeout, $timeout);
  }

  /**
   * Post data to an URL and retrieve an XML
   * @param string $url
   * @param string $data Optional. If provided will use a POST method. Only accept
   *                     data as POST encoded string or raw XML string.
   * @param int $timeout Optional. Number of seconds before the query fail
   * @return SimpleXMLElement
   * Use queryServer to retrieve an XML and check its validity
   */
  protected function queryServerXML($path, $params, $data = NULL, $connexionTimeout = OssApi::DEFAULT_CONNEXION_TIMEOUT, $timeout = OssApi::DEFAULT_QUERY_TIMEOUT) {
    $result = $this->queryServerTXT($path, $params, $data, $connexionTimeout, $timeout);
    if ($result === FALSE) {
      return FALSE;
    }

    // Check if we have a valid XML string from the engine
    $lastErrorLevel = error_reporting(0);
    $xmlResult = simplexml_load_string(OssApi::cleanUTF8($result));
    error_reporting($lastErrorLevel);
    if (!$xmlResult instanceof SimpleXMLElement) {
      if (class_exists('OssException')) {
        throw new RuntimeException("The search engine didn't return a valid XML");
      }
      trigger_error("The search engine didn't return a valid XML", E_USER_WARNING);
      return FALSE;
    }
    return $xmlResult;
  }

  /**
   * Check if the answer is an error returned by OSS
   * @param $xml string, DOMDocument or SimpleXMLElement
   * @return boolean True if error success
   */
  protected  function isOSSError($xml) {
    // Cast $xml param to be a SimpleXMLElement
    // If we don't find the word 'Error' in the xml string, exit immediatly
    if ($xml instanceof SimpleXMLElement) {
      if (strpos((string)$xml, 'Error') === FALSE) {
        return FALSE;
      }
      $xmlDoc = $xml;
    }
    elseif ($xml instanceof DOMDocument) {
      $xmlDoc = simplexml_import_dom($xml);
      if (strpos((string)$xmlDoc, 'Error') === FALSE) {
        return FALSE;
      }
    }
    else {
      if (strpos((string)$xml, 'Error') === FALSE) {
        return FALSE;
      }
      $previous_error_level = error_reporting(0);
      $xmlDoc = simplexml_load_string($xml);
      error_reporting($previous_error_level);
    }

    if (!$xmlDoc instanceof SimpleXMLElement) {
      return FALSE;
    }

    // Make sure the Error we found was a Status Error
    foreach ($xmlDoc->entry as $entry) {
      if ($entry['key'] == 'Status' && $entry == 'Error') {
        return TRUE;
      }
    }

    return FALSE;
  }

}
?>