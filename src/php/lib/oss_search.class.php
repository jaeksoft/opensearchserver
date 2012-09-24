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


/**
 * @author pmercier <pmercier@open-search-server.com>
 * @package OpenSearchServer
 * FIXME Complete this documentations
 * FIXME Clean this class and use facilities provided by OssApi
 */
class OssSearch extends OssAbstract {

  const API_SELECT   = 'select';

  protected $query;
  protected $template;
  protected $start;
  protected $rows;
  protected $lang;
  protected $filter;
  protected $delete;
  protected $field;
  protected $sort;
  protected $operator;
  protected $collapse;
  protected $facet;
  protected $sortBy;
  protected $moreLikeThis;
  protected $log;
  protected $customLogs;
  protected $uniqueKeys;
  protected $docIds;

  /**
   * @param $enginePath The URL to access the OSS Engine
   * @param $index The index name
   * @return OssSearch
   */
  public function __construct($enginePath, $index = NULL, $rows = NULL, $start = NULL, $login = NULL, $apiKey = NULL) {

    $this->init($enginePath, $index, $login, $apiKey);

    $this->rows($rows);
    $this->start($start);

    $this->field  = array();
    $this->filter  = array();
    $this->sort    = array();
    $this->facets  = array();
    $this->operator = NULL;
    $this->collapse  = array('field' => NULL, 'max' => NULL, 'mode' => NULL);
    $this->moreLikeThis = array('active' => NULL, 'docquery' => NULL, 'minwordlen' => NULL,
      'maxwordlen' => NULL, 'mindocfreq' => NULL, 'mintermfreq' => NULL, 'stopwords' => NULL);
    $this->log = FALSE;
    $this->customLogs = array();
    $this->uniqueKey = array();
    $this->docIds = array();
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
  public function template($template = NULL) {
    $this->template = $template;
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
  public function delete($delete = TRUE) {
    $this->delete = (bool)$delete;
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
  public function collapseMax($max) {
    $this->collapse['max'] = $max;
    return $this;
  }

  /**
   * @return OssSearch
   */
  public function uniqueKey($uniqueKey = NULL) {
    $this->uniqueKeys[] = $uniqueKey;
    return $this;
  }

  /**
   * @return OssSearch
   */
  public function docId($docId = NULL) {
    $this->docIds[] = $docId;
    return $this;
  }

  /**
   * @return OssSearch
   */
  public function facet($field, $min = NULL, $multi = FALSE) {
    $this->facet[$field] = array('min' => $min, 'multi' => $multi);
    return $this;
  }

  public function moreLikeThisActive($active) {
    $this->moreLikeThis['active'] = $active;
  }

  public function moreLikeThisDocQuery($docQuery) {
    $this->moreLikeThis['docquery'] = $docQuery;
  }

  public function moreLikeThisMinWordLen($minwordlen) {
    $this->moreLikeThis['minwordlen'] = $minwordlen;
  }

  public function moreLikeThisMaxWordLen($maxwordlen) {
    $this->moreLikeThis['maxwordlen'] = $maxwordlen;
  }

  public function moreLikeThisMinDocFreq($mindocfreq) {
    $this->moreLikeThis['mindocfreq'] = $mindocfreq;
  }

  public function moreLikeThisMinTermFreq($mintermfreq) {
    $this->moreLikeThis['mintermfreq'] = $mintermfreq;
  }

  public function moreLikeThisMinStopWords($stopwords) {
    $this->moreLikeThis['stopwords'] = $stopwords;
  }

  public function setLog($log = FALSE) {
    $this->log = $log;
  }

  public function setCustomLog($pos, $log) {
    $this->customLogs[(int)$pos] = $log;
  }


  /**
   * @return SimpleXMLElement False if the query produced an error
   * FIXME Must think about OssApi inteegration inside OssSearch
   */
  public function execute($connectTimeOut = NULL, $timeOut = NULL) {
    $result = $this->queryServerXML(OssSearch::API_SELECT, $this->getParamsString(), $connectTimeOut, $timeOut);
    if ($result === FALSE) {
      return FALSE;
    }
    return $result;
  }

  protected function getParamsString() {

    $queryChunks = array();

    $queryChunks[] = 'q=' . urlencode((empty($this->query) ? "*:*" : $this->query));

    if (!empty($this->template)) {
      $queryChunks[] = 'qt='   . $this->template;
    }

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

    if ($this->delete) {
      $queryChunks[] = 'delete';
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
      $facet  = $options['multi'] ? 'facet.multi=' : 'facet=';
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
    if ($this->collapse['mode'] !== NULL) {
      $queryChunks[] = 'collapse.mode=' . $this->collapse['mode'];
    }
    if ($this->collapse['max'] !== NULL) {
      $queryChunks[] = 'collapse.max=' . (int)$this->collapse['max'];
    }

    // MoreLikeThis
    if ($this->moreLikeThis['active']) {
      $queryChunks[] = 'mlt=yes';
    }
    if ($this->moreLikeThis['docquery']) {
      $queryChunks[] = 'mlt.docquery=' . urlencode($this->moreLikeThis['docquery']);
    }
    if ($this->moreLikeThis['minwordlen']) {
      $queryChunks[] = 'mlt.minwordlen=' . (int)$this->moreLikeThis['minwordlen'];
    }
    if ($this->moreLikeThis['maxwordlen']) {
      $queryChunks[] = 'mlt.maxwordlen=' . (int)$this->moreLikeThis['maxwordlen'];
    }
    if ($this->moreLikeThis['mindocfreq']) {
      $queryChunks[] = 'mlt.mindocfreq=' . (int)$this->moreLikeThis['mindocfreq'];
    }
    if ($this->moreLikeThis['mintermfreq']) {
      $queryChunks[] = 'mlt.mintermfreq=' . (int)$this->moreLikeThis['mintermfreq'];
    }
    if ($this->moreLikeThis['stopwords']) {
      $queryChunks[] = 'mlt.stopwords=' . urlencode($this->moreLikeThis['stopwords']);
    }

    // Logs and customLogs
    if ($this->log) {
      $queryChunks[] = 'log=' . $this->log;
    }
    foreach ($this->customLogs as $pos => $customLog) {
      $queryChunks[] = 'log' . $pos . '=' . urlencode($customLog);
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
    return implode('&', $queryChunks);
  }
}
?>