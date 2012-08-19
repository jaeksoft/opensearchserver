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

class OssSearchTemplate {

  protected $enginePath;
  protected $index;
  protected $query;
  protected $template;

  public function __construct($enginePath, $index = NULL, $login = NULL, $apiKey = NULL) {
    $ossAPI = new OssApi($enginePath, $index);
    $this->enginePath  = $ossAPI->getEnginePath();
    $this->index    = $ossAPI->getIndex();
    $this->credential($login, $apiKey);
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
    $return = OssApi::queryServerXML($this->getQueryURL(OssApi::API_SEARCH_TEMPLATE, $this->index  , OssApi::API_SEARCH_TEMPLATE_CREATE, $params));
    return $return === FALSE ? FALSE : TRUE;
  }

  public function setReturnField($qtname, $returnField) {
    $params = array("qt.name" => $qtname);
    $params['returnfield']=$returnField;
    $return = OssApi::queryServerXML($this->getQueryURL(OssApi::API_SEARCH_TEMPLATE, $this->index  , OssApi::API_SEARCH_TEMPLATE_SETRETURNFIELD, $params));
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
    $return = OssApi::queryServerXML($this->getQueryURL(OssApi::API_SEARCH_TEMPLATE, $this->index  , OssApi::API_SEARCH_TEMPLATE_SETSNIPPETFIELD, $params));
    return $return === FALSE ? FALSE : TRUE;
  }

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
}
?>