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

if (!extension_loaded('SimpleXML')) {
  trigger_error("OssApi won't work whitout SimpleXML extension", E_USER_ERROR); die();
}

/**
 * Class to access OpenSearchServer API
 * @author philcube <egosse@open-search-server.com>
 * @package OpenSearchServer
 */
class OssPaging {

  protected $oss_result;
  protected $resultTotal;
  protected $resultLow;
  protected $resultHigh;
  protected $resultPrev;
  protected $resultNext;
  protected $pageBaseURI;
  protected $rowsParameter;
  protected $pageParameter;
  protected $paramSeparator;
  const MAX_PAGE_TO_LINK = 10;

  /**
   * @param $result The data
   * @param $model The list of fields
   * @return OssApi
   */
  public function __construct(SimpleXMLElement $result, $rowsParam = 'rows', $pageParam = 'p', $paramSeparator = '&amp;') {
    $this->oss_result  = $result;
    $this->rowsParameter = $rowsParam;
    $this->pageParameter = $pageParam;
    $this->paramSeparator = $paramSeparator;
    self::compute();

    if (!function_exists('OssApi_Dummy_Function')) {
      function OssApi_Dummy_Function() {
      }
    }
  }

  /**
   * GETTER
   */
  public function getResultCurrentPage() {
    return $this->resultCurrentPage;
  }

  public function getResultTotal() {
    return $this->resultTotal;
  }

  public function getResultLow() {
    return $this->resultLow;
  }

  public function getResultHigh() {
    return $this->resultHigh;
  }

  public function getResultPrev() {
    return $this->resultPrev;
  }

  public function getResultNext() {
    return $this->resultNext;
  }

  public function getPageBaseURI() {
    return $this->pageBaseURI;
  }


  public function compute() {
    $this->resultFound   = ((int) $this->oss_result->result['numFound'] - (int) $this->oss_result->result['collapsedDocCount']);
    $this->resultTime    = (float) $this->oss_result->result['time'] / 1000;
    $this->resultRows    = (int) $this->oss_result->result['rows'];
    $this->resultStart   = (int) $this->oss_result->result['start'];

    $this->resultCurrentPage = ($this->resultRows > 0) ? floor($this->resultStart / $this->resultRows) : 0;
    $this->resultTotal = ($this->resultRows > 0) ? ceil($this->resultFound / $this->resultRows) : 0;

    if ($this->resultTotal > 1) {
      $low  = $this->resultCurrentPage - (OssPaging::MAX_PAGE_TO_LINK / 2);
      $high = $this->resultCurrentPage + (OssPaging::MAX_PAGE_TO_LINK / 2 - 1);
      if ($low < 0) {
        $high += $low * -1;
      }
      if ($high > $this->resultTotal) {
        $low -= $high - $this->resultTotal;
      }

      $this->resultLow  = max($low, 0);
      $this->resultHigh = min($this->resultTotal, $high);
      $this->resultPrev = max($this->resultCurrentPage - 1, 0);
      if($this->resultCurrentPage + 1 < $this->resultHigh) {
        $this->resultNext = min($this->resultCurrentPage + 1, $this->resultTotal);
      }
      $this->pageBaseURI = preg_replace('/&(?:' . $this->pageParameter . '|' . $this->rowsParameter . ')=[\d]+/', '', $_SERVER['REQUEST_URI'])
      . $this->paramSeparator . $this->rowsParameter . '=' . $this->resultRows . $this->paramSeparator . $this->pageParameter . '=';
    }
  }

}
?>