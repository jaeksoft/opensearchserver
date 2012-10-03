<?php
/*
 *  This file is part of OpenSearchServer.
*
*  Copyright (C) 2012 Emmanuel Keller / Jaeksoft
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
require_once(dirname(__FILE__).'/oss_search_abstract.class.php');


/**
 * @package OpenSearchServer
 * FIXME Complete this documentations
 * FIXME Clean this class and use facilities provided by OssApi
 */
class OssSearchDocument extends OssSearchAbstract {

  protected $field;
  protected $uniqueKeys;
  protected $docIds;

  /**
   * @param $enginePath The URL to access the OSS Engine
   * @param $index The index name
   * @return OssSearchDocument
   */
  public function __construct($enginePath, $index = NULL, $login = NULL, $apiKey = NULL) {
    parent::__construct($enginePath, $index, $login, $apiKey);

    $this->field  = array();
    $this->uniqueKey = array();
    $this->docIds = array();
  }

  /**
   * @return OssSearchDocument
   */
  public function field($fields) {
    $this->field = array_unique(array_merge($this->field, (array)$fields));
    return $this;
  }

  /**
   * @return OssSearchDocument
   */
  public function uniqueKey($uniqueKey = NULL) {
    $this->uniqueKeys[] = $uniqueKey;
    return $this;
  }

  /**
   * @return OssSearchDocument
   */
  public function docId($docId = NULL) {
    $this->docIds[] = $docId;
    return $this;
  }

  protected function addParams($queryChunks) {

    $queryChunks = parent::addParams($queryChunks);

    // Fields
    foreach ((array)$this->field as $field) {
      if (empty($field)) continue;
      $queryChunks[] = 'rf=' . $field;
    }

    // DocID
    foreach ((array) $this->docIds as $docId) {
      if (empty($docId)) {
        continue;
      }
      $queryChunks[] = 'id=' . urlencode($docId);
    }

    // UniqueKey
    foreach ((array) $this->uniqueKeys as $uniqueKey) {
      if (empty($uniqueKey)) {
        continue;
      }
      $queryChunks[] = 'uk=' . urlencode($uniqueKey);
    }
    return $queryChunks;
  }
}
?>