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


/**
 * @file
 * Class to access OpenSearchServer API
 */

require_once(dirname(__FILE__).'/oss_abstract.class.php');
require_once(dirname(__FILE__).'/oss_search_abstract.class.php');


/**
 * @package OpenSearchServer
*/
class OssSearch extends OssSearchAbstract {

  protected $query;
  protected $start;
  protected $rows;
  protected $lang;
  protected $filter;
  protected $field;
  protected $sort;
  protected $operator;
  protected $collapse;
  protected $facet;

  /**
   * @param $enginePath The URL to access the OSS Engine
   * @param $index The index name
   * @return OssSearch
   */
  public function __construct($enginePath, $index = NULL, $rows = NULL, $start = NULL, $login = NULL, $apiKey = NULL) {
    parent::__construct($enginePath, $index, $login, $apiKey);

    $this->rows($rows);
    $this->start($start);

    $this->field  = array();
    $this->filter  = array();
    $this->sort    = array();
    $this->facet  = array();
    $this->query = NULL;
    $this->lang = NULL;
    $this->operator = NULL;
    $this->collapse  = array('field' => NULL, 'max' => NULL, 'mode' => NULL, 'type' => NULL);
  }

  /**
   * Specify the query
   * @param $query string
   * @return OssSearch
   */
  public function query($query = NULL) {
    $this->query = $query;
    return $this;
  }

  /**
   * @return OssSearch
   */
  public function start($start = NULL) {
    $this->start = $start;
    return $this;
  }

  /**
   * @return OssSearch
   */
  public function rows($rows = NULL) {
    $this->rows = $rows;
    return $this;
  }

  /**
   * Set the default operation OR or AND
   * @param unknown_type $start
   * @return OssSearch
   */
  public function operator($operator = NULL) {
    $this->operator = $operator;
    return $this;
  }

  /**
   * @return OssSearch
   */
  public function filter($filter = NULL) {
    $this->filter[] = $filter;
    return $this;
  }

  /**
   * @return OssSearch
   */
  public function lang($lang = NULL) {
    $this->lang = $lang;
    return $this;
  }

  /**
   * @return OssSearch
   */
  public function field($fields) {
    $this->field = array_unique(array_merge($this->field, (array)$fields));
    return $this;
  }

  /**
   * @return OssSearch
   */
  public function sort($fields) {
    foreach ((array)$fields as $field)
      $this->sort[] = $field;
    return $this;
  }

  /**
   * @return OssSearch
   */
  public function collapseField($field) {
    $this->collapse['field'] = $field;
    return $this;
  }

  /**
   * @return OssSearch
   */
  public function collapseMode($mode) {
    $this->collapse['mode'] = $mode;
    return $this;
  }

  /**
   * @return OssSearch
   */
  public function collapseType($type) {
    $this->collapse['type'] = $type;
    return $this;
  }


  /**
   * @return OssSearch
   */
  public function collapseMax($max) {
    $this->collapse['max'] = $max;
    return $this;
  }

  /**
   * @return OssSearch
   */
  public function facet($field, $min = NULL, $multi = FALSE, $multi_collapse = FALSE) {
    $this->facet[$field] = array('min' => $min, 'multi' => $multi, 'multi_collapse' => $multi_collapse);
    return $this;
  }


  protected function addParams($queryChunks) {

    $queryChunks = parent::addParams($queryChunks);
     
    $queryChunks[] = 'q=' . urlencode((empty($this->query) ? "*:*" : $this->query));

    if (!empty($this->lang)) {
      $queryChunks[] = 'lang=' . $this->lang;
    }

    if ($this->rows   !== NULL) {
      $queryChunks[] = 'rows='  . (int) $this->rows;
    }

    if ($this->start !== NULL) {
      $queryChunks[] = 'start=' . (int) $this->start;
    }

    if ($this->operator !== NULL) {
      $queryChunks[] = 'operator=' . $this->operator;
    }

    // Sorting
    foreach ((array) $this->sort as $sort) {
      if (empty($sort)) {
        continue;
      }
      $queryChunks[] = 'sort=' . urlencode($sort);
    }

    // Filters
    foreach ((array) $this->filter as $filter) {
      if (empty($filter)) {
        continue;
      }
      $queryChunks[] = 'fq=' . urlencode($filter);
    }

    // Fields
    foreach ((array)$this->field as $field) {
      if (empty($field)) continue;
      $queryChunks[] = 'rf=' . $field;
    }

    // Facets
    foreach ((array)$this->facet as $field => $options) {
      if ($options['multi']) {
        $facet = 'facet.multi=';
      } else if ($options['multi_collapse']) {
        $facet = 'facet.multi.collapse=';
      } else {
        $facet = 'facet=';
      }
      $facet .= $field;
      if ($options['min'] !== NULL) {
        $facet .= '(' . $options['min'] . ')';
      }
      $queryChunks[] = $facet;
    }

    // Collapsing
    if ($this->collapse['field']) {
      $queryChunks[] = 'collapse.field=' . $this->collapse['field'];
    }
    if ($this->collapse['type']) {
      $queryChunks[] = 'collapse.type=' . $this->collapse['type'];
    }
    if ($this->collapse['mode'] !== NULL) {
      $queryChunks[] = 'collapse.mode=' . $this->collapse['mode'];
    }
    if ($this->collapse['max'] !== NULL) {
      $queryChunks[] = 'collapse.max=' . (int)$this->collapse['max'];
    }

    return $queryChunks;
  }
}
?>