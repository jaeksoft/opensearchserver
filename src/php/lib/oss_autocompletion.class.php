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

class OssAutocompletion extends OssAbstract {

  public function __construct($enginePath, $index = NULL, $login = NULL, $apiKey = NULL) {
    $this->init($enginePath, $index, $login, $apiKey);
  }

  /**
   *
   * @param string $query the characters to pass to the autocompletion query
   * @param int $rows The number of row to return
   */
  public function autocompletionQuery($query, $rows = 10) {
    $params = array('query' => $query, 'rows' => $rows);
    $return = $this->queryServerTXT(OssApi::API_AUTOCOMPLETION, $params);
    if ($return === FALSE) {
      return FALSE;
    }
    return $return;
  }

  /*
   * @deprecated  Use autocompleteQuery
  */
  public function autocomplete($query, $rows = 10) {
    return $this->autocompletionQuery($query, $rows);
  }

  /**
   * Build the autocompletion index
   * @param int $bufferSize the size of the buffer
   */
  public function autocompletionBuild($bufferSize = 1000) {
    $params = array('cmd' => 'build', 'bufferSize' => $bufferSize);
    $return = $this->queryServerXML(OssApi::API_AUTOCOMPLETION, $params);
    if ($return === FALSE) {
      return FALSE;
    }
    return TRUE;
  }

  /**
   * Set the field used by the autocompletion index
   * @param string $field the field name
   */
  public function autocompletionSet($field) {
    $params = array('cmd' => 'set', 'field' => $field);
    $return = $this->queryServerXML(OssApi::API_AUTOCOMPLETION, $params);
    if ($return === FALSE) {
      return FALSE;
    }
    return TRUE;
  }
}
?>