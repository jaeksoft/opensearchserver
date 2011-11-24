<?php
/*
*  This file is part of OpenSearchServer.
*
*  Copyright (C) 2008-2011 Emmanuel Keller / Jaeksoft
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
if (!extension_loaded('curl')) {
  trigger_error("OssApi won't work whitout curl extension", E_USER_ERROR); die();
}
if (!extension_loaded('SimpleXML')) {
  trigger_error("OssApi won't work whitout SimpleXML extension", E_USER_ERROR); die();
}

if (!class_exists('RuntimeException')) {
  class RuntimeException extends Exception {}
}
if (!class_exists('LogicException')) {
  class LogicException extends Exception {}
}
if (!class_exists('InvalidArgumentException')) {
  class InvalidArgumentException extends LogicException {}
}
if (!class_exists('OutOfRangeException')) {
  class OutOfRangeException extends LogicException {}
}

/**
 * @file
 * Class to access OpenSearchServer API
 * @author pmercier <pmercier@open-search-server.com>
 * @package OpenSearchServer
 */


class OssApi {

  const API_SELECT   = 'select';
  const API_UPDATE   = 'update';
  const API_DELETE   = 'delete';
  const API_OPTIMIZE = 'optimize';
  //const API_RELOAD   = 'reload';
  const API_INDEX    = 'index';
  const API_ENGINE   = 'engine';
  const API_PATTERN  = 'pattern';
  const API_SCHEMA   = 'schema';
  const API_SEARCH_TEMPLATE='searchtemplate';

  const API_SEARCH_TEMPLATE_CREATE='create';
  const API_SEARCH_TEMPLATE_SETRETURNFIELD='setreturnfield';
  const API_SEARCH_TEMPLATE_SETSNIPPETFIELD='setsnippetfield';

  const API_SCHEMA_INDEX_LIST    = 'indexList';
  const API_SCHEMA_CREATE_INDEX  = 'createIndex';
  const API_SCHEMA_GET_SCHEMA    = 'getSchema';
  const API_SCHEMA_SET_FIELD    = "setField";
  const API_SCHEMA_DELETE_FIELD  = "deleteField";

  const INDEX_TEMPLATE_EMPTY  = 'empty_index';


  /** @var int Default timeout (specified in seconds) for CURLOPT_TIMEOUT option. See curl documentation */
  const DEFAULT_QUERY_TIMEOUT = 0;

  /** @var int Timeout (specified in seconds) for CURLOPT_CONNECTTIMEOUT option. See curl documentation */
  const DEFAULT_CONNEXION_TIMEOUT = 5;

  /**
   * List of supported languages
   * @todo Provide an API to request the supported languages from the engine (if possible)
   * @var array
   */
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

  /* @var string */
  protected $login;

  /* @var string */
  protected $apiKey;

  /**
   * @param $enginePath The URL to access the OSS Engine
   * @param $index The index name
   * @return OssApi
   */
  public function __construct($enginePath, $index = NULL, $login = NULL, $apiKey = NULL) {

    $parsedPath = OssApi::parseEnginePath($enginePath, $index);
    $this->enginePath  = $parsedPath['enginePath'];
    $this->index    = $parsedPath['index'];

    $this->credential($login, $apiKey);

    if (!function_exists('OssApi_Dummy_Function')) {
      function OssApi_Dummy_Function() {
      }
    }
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
   * Return an OssSearch using the current engine path and index
   * @param string $index If provided, this index name is used in place of the one defined in the API instance
   * @return OssSearch
   * This method require the file OssSearch.class.php. It'll be included if the OssSearch class don't exist.
   * It's expected to be in the same directory as OssApi.class.php.
   */
  public function select($index = NULL) {
    $index = $index ? $index : $this->index;
    if (!class_exists('OssSearch')) {
      require(dirname(__FILE__) . '/OssSearch.class.php');
    }
    return new OssSearch($this->enginePath, $index, NULL, NULL, $this->login, $this->apiKey);
  }

  /**
   * Return an OssSearch using the current engine path and index
   * @param string $index If provided, this index name is used in place of the one defined in the API instance
   * @return OssSearch
   * @deprecated Use OssApi::select
   */
  public function search($index = NULL) {
    $index = $index ? $index : $this->index;
    return $this->select($index);
  }

  /**
   * Optimize the index
   * param string $index If provided, this index name is used in place of the one defined in the API instance
   * return boolean True on success
   * see OSS Wiki [Web API optimize] documentation before using this method
   * FIXME Provide a link to the OSS WiKi
   */
  public function optimize($index = NULL) {
    $index = $index ? $index : $this->index;
    $return = $this->queryServer($this->getQueryURL(OssApi::API_OPTIMIZE, $index));
    return ($return !== FALSE);
  }

  /**
   * Reload the index
   * param string $index If provided, this index name is used in place of the one defined in the API instance
   * return boolean True on success
   * see OSS Wiki [Web API reload] documentation before using this method
   * FIXME See why API have been removed
   */
  public function reload($index = NULL) {
    $index = $index ? $index : $this->index;
    $return = $this->queryServer($this->getQueryURL(OssApi::API_RELOAD, $index));
    return ($return !== FALSE);
  }

  /**
   * todo Next release
   * ignore
   * param string $file
   * return NULL
   * FIXME See with ekeller if this's api won't be deprecated soon
   */
  public function push($file) {
    // http://localhost:8080/oss/push?use=fileNiet&fileName=test.pdf&version=1
  }

  /**
   * Add one or many pattern to crawl
   * param string|string[] $patterns
   * param boolean $deleteAll The provided patterns will replace the patterns already in the search engine
   * param string $index If provided, this index name is used in place of the one defined in the API instance
   * return boolean True on success
   */
  public function pattern($patterns, $deleteAll = FALSE, $index = NULL) {
    $index = $index ? $index : $this->index;
    if (is_array($patterns)) {
      $patterns = implode("\n", $patterns);
    }
    $return = $this->queryServer($this->getQueryURL(OssApi::API_PATTERN, $index) . ($deleteAll?'&deleteAll=yes':'&deleteAll=no'), $patterns);
    return ($return !== FALSE);
  }

  /**
   * Return the url to use with curl
   * param string $apiCall The Web API to call. Refer to the OSS Wiki documentation of [Web API]
   * param string $index The index to query. If none given, index of the current object will be used
   * param string $command The optional command to send to the API call
   * param string[] $options Additional query parameters
   * return string
   * Use OssApi::API_* constants for $apiCall.
   * Optionals query parameters are provided as a named list:
   * array(
   *   "arg1" => "value1",
   *   "arg2" => "value2"
   * )
   */
  protected function getQueryURL($apiCall, $index = NULL, $cmd = NULL, $options = NULL) {

    $path = $this->enginePath . '/' . $apiCall;
    $chunks = array();

    if (!empty($index)) {
      $chunks[] = 'use=' . urlencode($index);
    }

    if (!empty($cmd)) {
      $chunks[] = 'cmd=' . urlencode($cmd);
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
    }

    $path .= (strpos($path, '?') !== FALSE ? '&' : '?') . implode("&", $chunks);

    return $path;
  }

  /**
   * Send an xml list of documents to be indexed by the search engine
   * @param mixed $xml Can be an xml string, a OssIndexDocument, a SimpleXMLElement,
   *                   a DOMDocument or any object that implement the __toString
   *                   magic method
   * @param string $index If provided, this index name is used in place of the one defined in the API instance
   * @return boolean True on success
   */
  public function update($xml, $index = NULL) {

    $index = $index ? $index : $this->index;

    // Cast $xml to a string
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
      if (class_exists('OssException')) {
        throw new UnexpectedValueException('String, SimpleXMLElement or DOMDocument was expected for $xml.');
      }
      trigger_error(__CLASS__ . '::' . __METHOD__ . '($xml): String, SimpleXMLElement or DOMDocument was expected for $xml.', E_USER_ERROR);
      return FALSE;
    }

    $return = $this->queryServer($this->getQueryURL(OssApi::API_UPDATE, $index), $xml);
    return ($return !== FALSE);

  }


  /**
   * Return informations about the OSS Engine
   * @todo Finish implementation once API is availabe
   * @return string[]
   */
  public function getEngineInformations() {
    $infos = array(
      'engine_url'    => $this->enginePath,
      'engine_version'  => 'unknown',
      'login'        => $this->login,
      'apiKey'      => $this->apiKey
    );
    return $infos;
    //return OssApi::queryServerXML($this->enginePath.'/'.OssApi::API_ENGINE);
  }

  /**
   * Return informations about the index
   * @todo Finish implementation once API is availabe
   * @param string $index If provided, this index name is used in place of the one defined in the API instance
   * @return string[]
   */
  public function getIndexInformations($index = NULL) {
    $index = $index ? $index : $this->index;

    $infos  = array(
      'name'  => $index,
      'size'  => NULL
    );

    set_error_handler('OssApi_Dummy_Function', E_ALL);
    try {
      $result = $this->queryServerXML($this->getQueryURL(OssApi::API_SELECT, $index) . '&q=*:*&rows=0');
    }
    catch (Exception $e) {
      $result = FALSE;
    }
    restore_error_handler();
    if ($result instanceof SimpleXMLElement) {
      $infos['count'] = $result->result['numFound'];
    }

    return $infos;
  }

  /**
   * Check if the engine is running. Don't check the existance of the index.
   * @return boolean Return NULL if can't connect to tomcat. Return FALSE if engine fail to answer. Return TRUE if the engine is running.
   * @todo Recode this method once API is provided
   */
  public function isEngineRunning() {

    // Check if the select api is answering
    $rcurl = curl_init($this->getQueryURL(OssApi::API_SELECT, $index) . '&q=!*:*&rows=0');
    curl_setopt($rcurl, CURLOPT_HTTP_VERSION, '1.0');
    curl_setopt($rcurl, CURLOPT_RETURNTRANSFER, TRUE);
    curl_setopt($rcurl, CURLOPT_CONNECTTIMEOUT, 5);
    set_error_handler('OssApi_Dummy_Function', E_ALL);
    curl_exec($rcurl);
    restore_error_handler();
    $infos = curl_getinfo($rcurl);
    if ($infos['http_code'] >= 200 && $infos['http_code'] < 300) {
      return TRUE;
    }
    if ($infos['http_code'] == 0) {
      return NULL;
    }
    return FALSE;

  }

  /**
   * Check if the index is available
   * @param string $index If provided, this index name is used in place of the one defined in the API instance
   * @return boolean True if exist.
   * FIXME Recode to use the new API Schema
   * @todo Recode this method once API is provided
   */
  public function isIndexAvailable($index = NULL) {
    $index = $index ? $index : $this->index;
    // Check if the select api is answering
    set_error_handler('OssApi_Dummy_Function', E_ALL);
    try {
      $result = $this->queryServerXML($this->getQueryURL(OssApi::API_SELECT, $index) . '&q=!*:*&rows=0');
    }
    catch (Exception $e) {
      $result = FALSE;
    }
    restore_error_handler();
    return (bool)$result;
  }

  /**
   * Return the list of indexes usable by the current credential
   * @return string[]
   */
  public function indexList() {
    $return = $this->queryServerXML($this->getQueryURL(OssApi::API_SCHEMA, NULL, OssApi::API_SCHEMA_INDEX_LIST));
    $indexes = array();
    foreach ($return->index as $index) {
      $indexes[] = (string)$index['name'];
    }
    return $indexes;
  }

  /**
   * Create a new index using a template
   * @param string $index The name of the new index
   * @param string $template Optional. The name of the template to use
   * @return boolean
   */
  public function createIndex($index, $template = FALSE) {

    $params = array("index.name" => $index);
    if ($template) {
      $params["index.template"] = $template;
    }
    $return = $this->queryServerXML($this->getQueryURL(OssApi::API_SCHEMA, NULL, OssApi::API_SCHEMA_CREATE_INDEX, $params));
    if ($return === FALSE) {
      return FALSE;
    }
    return TRUE;
  }

  /**
   * Retreive the complete schema of the index
   * @param string $index If provided, this index name is used in place of the one defined in the API instance
   * @return SimpleXMLElement|OSS_Schema
   * The schema is provided by the OSS engine as an xml. This xml is actualy the complete configuration of the schema.
   * If you want to manipulate the schema, pass it to OSS_Schema::factoryFromXML(...) for easier access.
   */
  public function getSchema($index = NULL) {
    $index = $index ? $index : $this->index;
    return $this->queryServerXML($this->getQueryURL(OssApi::API_SCHEMA, $index, OssApi::API_SCHEMA_GET_SCHEMA));
  }

  /**
   * Create or alter a field
   * @param string $name
   * @param string $analyzer
   * @param string $stored
   * @param string $indexed
   * @param string $termVector
   * @param string $index If provided, this index name is used in place of the one defined in the API instance
   * @return boolean
   */
  public function setField($name, $analyzer = NULL, $stored = NULL, $indexed = NULL, $termVector = NULL, $index = NULL, $default = NULL, $unique = NULL) {
    $index = $index ? $index : $this->index;
    $params = array("field.name" => $name);
    if ($analyzer) {
      $params["field.analyzer"]   = $analyzer;
    }
    if ($stored)  {
      $params["field.stored"]     = $stored;
    }
    if ($indexed) {
      $params["field.indexed"]    = $indexed;
    }
    if ($termVector) {
      $params["field.termVector"] = $termVector;
    }
    if ($termVector) {
      $params["field.default"] = $default;
    }
    if ($termVector) {
      $params["field.unique"] = $unique;
    }

    $return = $this->queryServerXML($this->getQueryURL(OssApi::API_SCHEMA, $index, OssApi::API_SCHEMA_SET_FIELD, $params));

    if ($return === FALSE) {
      return FALSE;
    }
    return TRUE;
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
  public static function queryServer($url, $data = NULL, $connexionTimeout = OssApi::DEFAULT_CONNEXION_TIMEOUT, $timeout = OssApi::DEFAULT_QUERY_TIMEOUT) {

    // Use CURL to post the data

    $rcurl = curl_init($url);
    curl_setopt($rcurl, CURLOPT_HTTP_VERSION, '1.0');
    curl_setopt($rcurl, CURLOPT_BINARYTRANSFER, TRUE);
    curl_setopt($rcurl, CURLOPT_RETURNTRANSFER, TRUE);
    curl_setopt($rcurl, CURLOPT_FOLLOWLOCATION, TRUE);
    curl_setopt($rcurl, CURLOPT_MAXREDIRS, 16);
    curl_setopt($rcurl, CURLOPT_VERBOSE, TRUE);

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
    if (OssApi::isOSSError($content)) {
      if (class_exists('OssException')) {
        throw new OssException($content);
      }
      trigger_error('OSS Returned an error: "' . trim(strip_tags($content)) . '"', E_USER_WARNING);
      return FALSE;
    }

    return $content;
  }

  /**
   * Post data to an URL and retrieve an XML
   * @param string $url
   * @param string $data Optional. If provided will use a POST method. Only accept
   *                     data as POST encoded string or raw XML string.
   * @param int $timeout Optional. Number of seconds before the query fail
   * @return SimpleXMLElement
   * Use OssApi::queryServerto retrieve an XML and check its validity
   */
  public static function queryServerXML($url, $data = NULL, $connexionTimeout = OssApi::DEFAULT_CONNEXION_TIMEOUT, $timeout = OssApi::DEFAULT_QUERY_TIMEOUT) {
    $result = OssApi::queryServer($url, $data, $connexionTimeout, $timeout);
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
  public static function isOSSError($xml) {

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

  /**
   * Parse the enginePath parameter to extract the index name.
   * @param $enginePath The URL to access the OSS Engine
   * @param $index The index name
   */
  public static function parseEnginePath($enginePath, $index = NULL) {

    $urlParams = array();
    // Extract the use param in the query part if any
    if (strpos($enginePath, '?') !== FALSE) {
      $parsedURL = parse_url($enginePath);
      parse_str($parsedURL['query'], $urlParams);
      if (isset($urlParams['use'])) {
        $index = $urlParams['use'];
        $enginePath = str_replace('&&', '&', str_replace("use=" . $urlParams['use'], '', $enginePath));
        if (substr($enginePath, -1) == '?') {
          $enginePath = substr($enginePath, 0, -1);
        }
      }
    }

    return array('enginePath' => $enginePath, 'index' => $index);

  }

  /**
   * Return a list of supported language. Array is indexed by ISO 639-1 format (en, de, fr, ...)
   * return Array<String>
   * see OssApi::$supportedLanguages
   */
  public static function supportedLanguages() {
    return OssApi::$supportedLanguages;
  }

  /**
   * Escape Lucene's special chars
   * param string $string
   * return string
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

    static $remove = array(
      "\x00", "\x01", "\x02", "\x03", "\x04", "\x05", "\x06", "\x07",
      "\x08",                 "\x0B", "\x0C",         "0x0E", "\x0F",
      "\x10", "\x11", "\x12", "\x13", "\x14", "\x15", "\x16", "\x17",
      "\x18", "\x19", "\x1A", "\x1B", "\x1C", "\x1D", "\x1E", "\x1F"
    );
    return str_replace($remove, $replacement, $string);

  }

}
?>