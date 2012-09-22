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

class OssSearchTemplate extends OssAbstract {

  const API_SEARCH_TEMPLATE='searchtemplate';
  const API_SEARCH_TEMPLATE_CREATE='create';
  const API_SEARCH_TEMPLATE_SETRETURNFIELD='setreturnfield';
  const API_SEARCH_TEMPLATE_SETSNIPPETFIELD='setsnippetfield';

  protected $query;
  protected $template;

  public function __construct($enginePath, $index = NULL, $login = NULL, $apiKey = NULL) {
    $this->init($enginePath, $index, $login, $apiKey);
  }

  public function createSearchTemplate($qtname, $qtquery = NULL, $qtoperator = NULL, $qtrows = NULL, $qtslop = NULL, $qtlang = NULL) {
    $params = array("qt.name" => $qtname);
    if ($qtquery) {
      $params['qt.query'] = $qtquery;
    }
    if ($qtoperator) {
      $params['qt.operator'] = $qtoperator;
    }
    if ($qtrows) {
      $params['qt.rows'] = $qtrows;
    }
    if ($qtslop) {
      $params['qt.slop'] = $qtslop;
    }
    if ($qtlang) {
      $params['qt.lang'] = $qtlang;
    }
    $params['cmd'] = OssSearchTemplate::API_SEARCH_TEMPLATE_CREATE;
    $return = $this->queryServerXML(OssSearchTemplate::API_SEARCH_TEMPLATE, $params);
    return $return === FALSE ? FALSE : TRUE;
  }
  /*
   * Function to create spell check Query Template
  * $qtsuggestions - No of suggestions to be returned
  * $qtfield - The field that is used for spell checking.
  * $qtscore - The minimum score of Spellcheck match.
  * $qtalgorithm -The alorithm used for spellcheck.OpenSearchServer has below alogrithms for spellcheck.
  *               1)LevensteinDistance
  *               2)NGramDistance
  *               3)JaroWinklerDistance
  */
  public function createSpellCheckTemplate($qtname, $qtquery = NULL, $qtsuggestions = NULL, $qtfield = NULL, $qtscore = NULL, $qtlang = NULL, $qtalgorithm = NULL) {
    $params = array("qt.name" => $qtname);
    $params['qt.type'] = 'SpellCheckRequest';
    if ($qtquery) {
      $params['qt.query'] = $qtquery;
    }
    if ($qtsuggestions) {
      $params['qt.suggestions'] = $qtsuggestions;
    }
    if ($qtfield) {
      $params['qt.field'] = $qtfield;
    }
    if ($qtscore) {
      $params['qt.score'] = $qtscore;
    }

    if ($qtlang) {
      $params['qt.lang'] = $qtlang;
    }
    if ($qtalgorithm) {
      $params['qt.algorithm'] = $qtalgorithm;
    }
    $params['cmd'] = OssSearchTemplate::API_SEARCH_TEMPLATE_CREATE;
    $return = $this->queryServerXML(OssSearchTemplate::API_SEARCH_TEMPLATE, $params);
    return $return === FALSE ? FALSE : TRUE;
  }

  public function setReturnField($qtname, $returnField) {
    $params = array("qt.name" => $qtname);
    $params['returnfield']=$returnField;
    $params['cmd'] = OssSearchTemplate::API_SEARCH_TEMPLATE_SETRETURNFIELD;
    $return = $this->queryServerXML(OssSearchTemplate::API_SEARCH_TEMPLATE, $params);
    return $return === FALSE ? FALSE : TRUE;
  }

  public function setSnippetField($qtname, $snippetField, $maxSnippetSize=NULL, $tag=NULL, $maxSnippetNo=NULL, $fragmenter=NULL) {
    $params = array("qt.name" => $qtname);
    if ($maxSnippetSize) {
      $params['qt.maxSnippetSize'] = $maxSnippetSize;
    }
    if ($tag) {
      $params['qt.tag']=$tag;
    }
    if ($maxSnippetNo) {
      $params['qt.maxSnippetNo'] = $maxSnippetNo;
    }
    if ($fragmenter) {
      $params['qt.fragmenter'] = $fragmenter;
    }
    $params['snippetfield'] = $snippetField;
    $params['cmd'] = OssSearchTemplate::API_SEARCH_TEMPLATE_SETSNIPPETFIELD;
    $return = $this->queryServerXML(OssSearchTemplate::API_SEARCH_TEMPLATE, $params);
    return $return === FALSE ? FALSE : TRUE;
  }

}
?>