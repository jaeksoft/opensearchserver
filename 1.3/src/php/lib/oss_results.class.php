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

if (!extension_loaded('SimpleXML')) {
  trigger_error("OssApi won't work whitout SimpleXML extension", E_USER_ERROR); die();
}

/**
 * @file
 * Class to access OpenSearchServer API
 * @author philcube <egosse@open-search-server.com>
 * @package OpenSearchServer
 */
class OssResults {

  /* @var SimpleXMLElement */
  protected $result;
  protected $resultFound;
  protected $resultTime;
  protected $resultRows;
  protected $resultStart;
  protected $resultCollapsedCount;

  /**
   * @param $result The data
   * @param $model The list of fields
   * @return OssApi
   */
  public function __construct(SimpleXMLElement $result, $model = NULL) {
    $this->result  = $result;
    $this->resultFound = (int)$this->result->result['numFound'];
    $this->resultTime = (float)$this->result->result['time'] / 1000;
    $this->resultRows = (int)$this->result->result['rows'];
    $this->resultStart = (int)$this->result->result['start'];
    $this->resultCollapsedCount = (int)$this->result->result['collapsedDocCount'];
    if (!function_exists('OssApi_Dummy_Function')) {
      function OssApi_Dummy_Function() {
      }
    }
  }
  public function getResultCollapsedCount() {
    return $this->resultCollapsedCount;
  }

  public function getResult() {
    return $this->result;
  }

  public function getResultFound() {
    return $this->resultFound;
  }

  public function getResultTime() {
    return $this->resultTime;
  }

  public function getResultRows() {
    return $this->resultRows;
  }

  public function getResultStart() {
    return $this->resultStart;
  }

  /**
   *  GETTER
   */
  public function getField($position, $fieldName, $modeSnippet = FALSE, $highlightedOnly = FALSE) {
    $field = NULL;
    $doc = $this->result->xpath('result/doc[@pos="' . $position . '"]');

    if (isset($doc[0]) && is_array($doc)) {
      $value = NULL;
      if ($modeSnippet) {
        if ($highlightedOnly) {
          $value = $doc[0]->xpath('snippet[@name="' . $fieldName . '" and @highlighted="yes"]');
        } else {
          $value = $doc[0]->xpath('snippet[@name="' . $fieldName . '"]');
        }
      }
      if (!isset($value) || count($value) == 0) {
        $value =  $doc[0]->xpath('field[@name="' . $fieldName . '"]');
      }
      if (isset($value[0])) {
        $field = $value[0];
      }
    }

    return $field;
  }


  /**
   *
   */
  public function getFields($position, $modeSnippet = FALSE) {
    $doc = $this->result->xpath('result/doc[@pos="' . $position . '"]');

    $fields = $doc->xpath('field');
    foreach ($fields as $field) {
      $name = (string) $field[0]['name'];
      $current[(string)$name] = (string) $field;
    }

    if ($modeSnippet) {
      $snippets = $doc->xpath('snippet');
      foreach ($snippets as $field) {
        $name = (string) $field[0]['name'];
        $current[(string)$name] = (string) $field;
      }
    }

    return $current;
  }

  /**
   *
   * @param unknown_type $fieldName
   * @return Ambigous <multitype:, NULL>
   */
  public function getFacet($fieldName) {
    $currentFacet = isset($fieldName)? $this->result->xpath('faceting/field[@name="' . $fieldName . '"]/facet'):NULL;
    if (!isset($currentFacet) || ( isset($currentFacet) && $currentFacet === FALSE)) {
      $currentFacet = array();
    }
    return $currentFacet;
  }

  /**
   *
   * @return unknown_type
   */
  public function getFacets() {
    $facets = array();
    $allFacets = $this->result->xpath('faceting/field');
    foreach ($allFacets as $each) {
      $facets[] = $each[0]['name'];
    }
    return $facets;
  }

  /**
   *
   * @return Return the spellsuggest array.
   */
  public function getSpellSuggestions($fieldName) {
    $currentSpellCheck = isset($fieldName)? $this->result->xpath('spellcheck/field[@name="' . $fieldName . '"]/word/suggest'):NULL;
    if (!isset($currentSpellCheck) || ( isset($currentSpellCheck) && $currentSpellCheck === FALSE)) {
      $currentSpellCheck = array();
    }
    return $currentSpellCheck;
  }
  /**
   *
   * @return Return the spellsuggest terms.
   */
  public function getSpellSuggest($fieldName) {
    $spellCheckWord = isset($fieldName)? $this->result->xpath('spellcheck/field[@name="' . $fieldName . '"]/word'):NULL;
    $queryExact = '';
    foreach ($spellCheckWord as $each) {
      $queryExact .= $each[0]->suggest.' ';
    }
    return $queryExact;
  }

}
?>