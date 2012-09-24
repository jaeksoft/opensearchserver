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

require_once(dirname(__FILE__).'/oss_abstract.class.php');
require_once(dirname(__FILE__).'/oss_schema.class.php');
require_once(dirname(__FILE__) . '/oss_search.class.php');


class OssApi extends OssAbstract {

  const API_UPDATE   = 'update';
  const API_DELETE   = 'delete';
  const API_OPTIMIZE = 'optimize';
  const API_RELOAD   = 'reload';
  const API_INDEX    = 'index';
  const API_ENGINE   = 'engine';
  const API_PATTERN  = 'pattern';
  const INDEX_TEMPLATE_EMPTY  = 'empty_index';

  const API_AUTOCOMPLETION = 'autocompletion';

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
    ''   => 'Undefined',
    'ar' => 'Arabic',
    'zh' => 'Chinese',
    'da' => 'Danish',
    'nl' => 'Dutch',
    'en' => 'English',
    'fi' => 'Finnish',
    'fr' => 'French',
    'de' => 'German',
    'hu' => 'Hungarian',
    'it' => 'Italian',
    'no' => 'Norwegian',
    'pt' => 'Portuguese',
    'ro' => 'Romanian',
    'ru' => 'Russian',
    'es' => 'Spanish',
    'sv' => 'Swedish',
    'tr' => 'Turkish'
  );

  /**
   * @param $enginePath The URL to access the OSS Engine
   * @param $index The index name
   * @return OssApi
   */
  public function __construct($enginePath, $index = NULL, $login = NULL, $apiKey = NULL) {
    $this->init($enginePath, $index, $login, $apiKey);
  }

  /**
   * Return an OssSearch using the current engine path and index
   * @param string $index If provided, this index name is used in place of the one defined in the API instance
   * @return OssSearch
   * This method require the file OssSearch.class.php. It'll be included if the OssSearch class don't exist.
   * It's expected to be in the same directory as OssApi.class.php.
   */
  public function select() {
    return new OssSearch($this->enginePath, NULL, NULL, $this->login, $this->apiKey);
  }

  /**
   * Return an OssSearch using the current engine path and index
   * @return OssSearch
   * @deprecated Use OssApi::select
   */
  public function search() {
    return $this->select();
  }

  /**
   * Optimize the index
   * return boolean True on success
   * see OSS Wiki [Web API optimize] documentation before using this method
   * FIXME Provide a link to the OSS WiKi
   */
  public function optimize() {
    $return = $this->queryServerTXT(OssApi::API_OPTIMIZE);
    return ($return !== FALSE);
  }

  /**
   * Reload the index
   * param string $index If provided, this index name is used in place of the one defined in the API instance
   * return boolean True on success
   * see OSS Wiki [Web API reload] documentation before using this method
   * FIXME See why API have been removed
   */
  public function reload() {
    $return = $this->queryServerTXT(OssApi::API_RELOAD);
    return ($return !== FALSE);
  }

  /**
   * Add one or many pattern to crawl
   * param string|string[] $patterns
   * param boolean $deleteAll The provided patterns will replace the patterns already in the search engine
   * return boolean True on success
   */
  public function pattern($patterns, $deleteAll = FALSE) {
    if (is_array($patterns)) {
      $patterns = implode("\n", $patterns);
    }
    $return = $this->queryServer($this->getQueryURL(OssApi::API_PATTERN) . ($deleteAll?'&deleteAll=yes':'&deleteAll=no'), $patterns);
    return ($return !== FALSE);
  }

  /**
   * Send an xml list of documents to be indexed by the search engine
   * @param mixed $xml Can be an xml string, a OssIndexDocument, a SimpleXMLElement,
   *                   a DOMDocument or any object that implement the __toString
   *                   magic method
   * @return boolean True on success
   */
  public function update($xml) {

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

    $return = $this->queryServer($this->getQueryURL(OssApi::API_UPDATE), $xml);
    return ($return !== FALSE);

  }

  /**
   * Return the list of indexes usable by the current credential
   * @return string[]
   */
  public function indexList() {
    $ossSchema = new OssSchema($this->enginePath, $this->index, $this->login, $this->apiKey);
    return $ossSchema->indexList();
  }

  /**
   * Create a new index using a template
   * @param string $index The name of the new index
   * @param string $template Optional. The name of the template to use
   * @return boolean
   */
  public function createIndex($index, $template = FALSE) {
    $ossSchema = new OssSchema($this->enginePath, $this->index, $this->login, $this->apiKey);
    return $ossSchema->createIndex($index, $template);
  }

  /**
   * Delete an index
   * @param string $index The name of the index to delete
   */
  public function deleteIndex($index) {
    $ossSchema = new OssSchema($this->enginePath, $this->index, $this->login, $this->apiKey);
    return $ossSchema->deleteIndex($index);
  }

  /**
   * Retreive the complete schema of the index
   * @return SimpleXMLElement|OSS_Schema
   * The schema is provided by the OSS engine as an xml. This xml is actualy the complete configuration of the schema.
   * If you want to manipulate the schema, pass it to OSS_Schema::factoryFromXML(...) for easier access.
   */
  public function getSchema() {
    $ossSchema = new OssSchema($this->enginePath, $this->index, $this->login, $this->apiKey);
    return $ossSchema->getSchema();
  }

  /**
   * Create or alter a field
   * @param string $name
   * @param string $analyzer
   * @param string $stored
   * @param string $indexed
   * @param string $termVector
   * @param string $default
   * @param string $unique
   * @return boolean
   */
  public function setField($name, $analyzer = NULL, $stored = NULL, $indexed = NULL, $termVector = NULL, $default = NULL, $unique = NULL) {
    $ossSchema = new OssSchema($this->enginePath, $this->index, $this->login, $this->apiKey);
    return $ossSchema->setField($name, $analyzer, $stored, $indexed, $termVector, $default, $unique);
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
   * Return the language constant from a 2 characters language code
   * @param string $twoCharsLang Two characters language
   */
  public static function getLanguage($twoCharsLang) {
    $lang = OssApi::$supportedLanguages[mb_strtolower($twoCharsLang)];
    if ($lang == NULL) {
      return OssApi::$supportedLanguages[''];
    }
    return $lang;
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