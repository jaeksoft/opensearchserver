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


/**
 * @file
 * Class to access OpenSearchServer API
 */

if (!class_exists('OssApi')) {
  trigger_error("OssSearch won't work whitout OssApi", E_USER_ERROR); die();
}

require_once('oss_abstract.class.php');


class OssSearchTemplate extends OssAbstract {

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
    $params['cmd'] = OssApi::API_SEARCH_TEMPLATE_CREATE;
    $return = OssApi::queryServerXML($this->getQueryURL(OssApi::API_SEARCH_TEMPLATE, $params));
    return $return === FALSE ? FALSE : TRUE;
  }

  public function setReturnField($qtname, $returnField) {
    $params = array("qt.name" => $qtname);
    $params['returnfield']=$returnField;
    $params['cmd'] = OssApi::API_SEARCH_TEMPLATE_SETRETURNFIELD;
    $return = OssApi::queryServerXML($this->getQueryURL(OssApi::API_SEARCH_TEMPLATE, $params));
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
    $params['cmd'] = OssApi::API_SEARCH_TEMPLATE_SETSNIPPETFIELD;
    $return = OssApi::queryServerXML($this->getQueryURL(OssApi::API_SEARCH_TEMPLATE, $params));
    return $return === FALSE ? FALSE : TRUE;
  }

}
?>