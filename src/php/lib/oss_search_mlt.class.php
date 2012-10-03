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
 */
class OssSearchMlt extends OssSearchAbstract {

  protected $start;
  protected $rows;
  protected $lang;
  protected $analyzer;
  protected $filter;
  protected $field;
  protected $docQuery;
  protected $likeText;
  protected $minWordLen;
  protected $maxWordLen;
  protected $minDocFreq;
  protected $minTermFreq;
  protected $maxNumTokensParsed;
  protected $stopWords;

  /**
   * @param $enginePath The URL to access the OSS Engine
   * @param $index The index name
   * @return OssSearchMlt
   */
  public function __construct($enginePath, $index = NULL, $rows = NULL, $start = NULL, $login = NULL, $apiKey = NULL) {
    parent::__construct($enginePath, $index, $login, $apiKey);

    $this->rows($rows);
    $this->start($start);

    $this->lang = NULL;
    $this->analyzer = NULL;
    $this->field  = array();
    $this->filter  = array();
    $this->docQuery = NULL;
    $this->likeText = NULL;
    $this->minWordLen = NULL;
    $this->maxWordLen = NULL;
    $this->minDocFreq = NULL;
    $this->minTermFreq = NULL;
    $this->maxNumTokensParsed = NULL;
    $this->stopWords = NULL;
  }

  /**
   * @return OssSearchMlt
   */
  public function start($start = NULL) {
    $this->start = $start;
    return $this;
  }

  /**
   * @return OssSearchMlt
   */
  public function rows($rows = NULL) {
    $this->rows = $rows;
    return $this;
  }

  /**
   * Specify the query to identify one document
   * @param string $query string
   * @return OssSearchMlt
   */
  public function docQuery($docQuery = NULL) {
    $this->docQuery = $docQuery;
    return $this;
  }

  /**
   *
   * @param string $likeText
   * @return OssSearchMlt
   */
  public function likeText($likeText = NULL) {
    $this->likeText = $likeText;
    return $this;
  }


  /**
   * @return OssSearchMlt
   */
  public function filter($filter = NULL) {
    $this->filter[] = $filter;
    return $this;
  }

  /**
   * @return OssSearchMlt
   */
  public function lang($lang = NULL) {
    $this->lang = $lang;
    return $this;
  }

  /**
   * @return OssSearchMlt
   */
  public function analyzer($analyzer = NULL) {
    $this->analyzer = $analyzer;
    return $this;
  }

  /**
   * @return OssSearchMlt
   */
  public function field($fields = NULL) {
    if ($field != NULL) {
      $this->field = array_unique(array_merge($this->field, (array)$fields));
    }
    return $this;
  }

  /**
   * @param int $minWordLen
   * @return OssSearchMlt
   */
  public function minWordLen($minWordLen = NULL) {
    $this->minWordLen = $minWordLen;
    return $this;
  }

  /**
   * @param int $maxWordLen
   * @return OssSearchMlt
   */
  public function maxWordLen($maxWordLen = NULL) {
    $this->maxWordLen = $maxWordLen;
    return $this;
  }

  /**
   * @param int $minDocFreq
   * @return OssSearchMlt
   */
  public function minDocFreq($minDocFreq = NULL) {
    $this->minDocFreq = $minDocFreq;
    return $this;
  }

  /**
   * @param int $minTermFreq
   * @return OssSearchMlt
   */
  public function minTermFreq($minTermFreq = NULL) {
    $this->minTermFreq = $minTermFreq;
    return $this;
  }


  /**
   * @param int $maxNumTokensParsed
   * @return OssSearchMlt
   */
  public function maxNumTokensParsed($maxNumTokensParsed = NULL) {
    $this->maxNumTokensParsed = $maxNumTokensParsed;
    return $this;
  }

  /**
   *
   * @param string $stopWords
   * @return OssSearchMlt
   */
  public function stopWords($stopWords = NULL) {
    $this->stopWords = $stopWords;
    return $this;
  }

  /**
   *
   * @param array $queryChunks
   */
  protected function addParams($queryChunks) {

    $queryChunks = parent::addParams($queryChunks);

    if (!empty($this->lang)) {
      $queryChunks[] = 'lang=' . $this->lang;
    }

    if ($this->rows   !== NULL) {
      $queryChunks[] = 'rows='  . (int) $this->rows;
    }

    if ($this->start !== NULL) {
      $queryChunks[] = 'start=' . (int) $this->start;
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


    if ($this->likeText != NULL) {
      $queryChunks[] = 'mlt.liketext='.urlencode($this->likeText);
    }
    if ($this->docQuery != NULL) {
      $queryChunks[] = 'mlt.docquery=' . urlencode($this->docQuery);
    }
    if ($this->minWordLen != NULL) {
      $queryChunks[] = 'mlt.minwordlen=' . (int)$this->minWordLen;
    }
    if ($this->maxWordLen != NULL) {
      $queryChunks[] = 'mlt.maxwordlen=' . (int)$this->maxWordLen;
    }
    if ($this->minDocFreq != NULL) {
      $queryChunks[] = 'mlt.mindocfreq=' . (int)$this->minDocFreq;
    }
    if ($this->minTermFreq != NULL) {
      $queryChunks[] = 'mlt.mintermfreq=' . (int)$this->minTermFreq;
    }
    if ($this->maxNumTokensParsed != NULL) {
      $queryChunks[] = 'mlt.maxnumtokensparsed=' . (int)$this->maxNumTokensParsed;
    }
    if ($this->moreLikeThis['stopwords']) {
      $queryChunks[] = 'mlt.stopwords=' . urlencode($this->moreLikeThis['stopwords']);
    }

    return $queryChunks;
  }

}
?>