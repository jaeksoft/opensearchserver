<?php
/*
 *  This file is part of OpenSearchServer.
*
*  Copyright (C)2012 Emmanuel Keller / Jaeksoft
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

/**
 * @file
 * Class to access OpenSearchServer API
 */

require_once(dirname(__FILE__).'/oss_abstract.class.php');

class OssSchema extends OssAbstract {

  const API_SCHEMA   = 'schema';
  const API_SCHEMA_INDEX_LIST    = 'indexList';
  const API_SCHEMA_CREATE_INDEX  = 'createIndex';
  const API_SCHEMA_DELETE_INDEX  = 'deleteIndex';
  const API_SCHEMA_GET_SCHEMA    = 'getSchema';
  const API_SCHEMA_SET_FIELD    = "setField";
  const API_SCHEMA_DELETE_FIELD  = "deleteField";

  public function __construct($enginePath, $index = NULL, $login = NULL, $apiKey = NULL) {
    $this->init($enginePath, $index, $login, $apiKey);
  }

  /**
   * Return the list of indexes usable by the current credential
   * @return string[]
   */
  public function indexList() {
    $params = array('cmd' => OssSchema::API_SCHEMA_INDEX_LIST);
    $return = $this->queryServerXML(OssSchema::API_SCHEMA, $params);
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
    $params = array('index.name' => $index);
    if ($template) {
      $params['index.template'] = $template;
    }
    $params['cmd'] = OssSchema::API_SCHEMA_CREATE_INDEX;
    $return = $this->queryServerXML(OssSchema::API_SCHEMA, $params);
    if ($return === FALSE) {
      return FALSE;
    }
    return TRUE;
  }

  /**
   * Delete an index
   * @param string $index The name of the index to delete
   */
  public function deleteIndex($index) {
    $params = array('cmd' => OssSchema::API_SCHEMA_DELETE_INDEX);
    $params['index.delete.name'] = $index;
    $params['index.name'] = $index;
    $return = $this->queryServerXML(OssSchema::API_SCHEMA, $params);
    if ($return === FALSE) {
      return FALSE;
    }
    return TRUE;
  }

  /**
   * Retreive the complete schema of the index
   * @return SimpleXMLElement|OSS_Schema
   * The schema is provided by the OSS engine as an xml. This xml is actualy the complete configuration of the schema.
   * If you want to manipulate the schema, pass it to OSS_Schema::factoryFromXML(...) for easier access.
   */
  public function getSchema() {
    $params = array('cmd' => OssSchema::API_SCHEMA_GET_SCHEMA);
    return $this->queryServerXML(OssSchema::API_SCHEMA, $params);
  }

  /**
   * Create or alter a field
   * @param string $name
   * @param string $analyzer
   * @param string $stored
   * @param string $indexed
   * @param string $termVector
   * @return boolean
   */
  public function setField($name, $analyzer = NULL, $stored = NULL, $indexed = NULL, $termVector = NULL, $default = NULL, $unique = NULL) {
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
    if ($default) {
      $params["field.default"] = $default;
    }
    if ($unique) {
      $params["field.unique"] = $unique;
    }

    $params['cmd'] = OssSchema::API_SCHEMA_SET_FIELD;
    $return = $this->queryServerXML(OssSchema::API_SCHEMA, $params);

    if ($return === FALSE) {
      return FALSE;
    }
    return TRUE;
  }

  /**
   * Delete a field
   * @param string $name The name of the field to delete
   */
  public function deleteField($name) {
    $params = array("cmd" => OssSchema::API_SCHEMA_DELETE_FIELD);
    $params['field.name'] = $name;
    $return = $this->queryServerXML(OssSchema::API_SCHEMA, $params);

    if ($return === FALSE) {
      return FALSE;
    }
    return TRUE;
  }

}