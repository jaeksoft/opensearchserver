<?php
/*
 *  This file is part of Jaeksoft OpenSearchServer.
 *
 *  Copyright (C) 2008-2009 Emmanuel Keller / Jaeksoft
 *
 *  http://www.open-search-server.com
 *
 *  Jaeksoft OpenSearchServer is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Jaeksoft OpenSearchServer is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Jaeksoft OpenSearchServer.  If not, see <http://www.gnu.org/licenses/>.
 */

if (!extension_loaded('SimpleXML')) { trigger_error("OSS_API won't work whitout SimpleXML extension", E_USER_ERROR); die(); }

/**
 * Class to access OpenSearchServer API
 * @author philcube <egosse@open-search-server.com>
 * @package OpenSearchServer
 */
class OSS_Results {

	/* @var SimpleXMLElement */
	protected $result;
	protected $resultFound;
	protected $resultTime;
	protected $resultRows;
	protected $resultStart;


	/**
	 * @param $result The data
	 * @param $model The list of fields
	 * @return OSS_API
	 */
	public function __construct(SimpleXMLElement $result, $model = null) {
		$this->result	= $result;
		$this->resultFound   = (int)$this->result->result['numFound'];
		$this->resultTime    = (float)$this->result->result['time'] / 1000;
		$this->resultRows    = (int)$this->result->result['rows'];
		$this->resultStart   = (int)$this->result->result['start'];

		if (!function_exists('OSS_API_Dummy_Function')) { function OSS_API_Dummy_Function() {} }
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
	public function getField($position, $fieldName, $modeSnippet = false) {
		$field = null;
		$doc = $this->result->xpath('result/doc[@pos="'.$position.'"]');

		if (isset($doc[0]) && is_array($doc)) {
			$value = null;
			if ($modeSnippet) {
				$value = $doc[0]->xpath('snippet[@name="'.$fieldName.'"]');
			}
			if (!isset($value) || count($value) == 0) {
				$value =  $doc[0]->xpath('field[@name="'.$fieldName.'"]');
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
	public function getFields($position, $modeSnippet = false) {
		$doc = $this->result->xpath('result/doc[@pos="'.$position.'"]');

		$fields = $doc->xpath('field');
		foreach($fields as $field) {
			$name = (string) $field[0]['name'];
			$current[(string)$name] = (string) $field;
		}

		if ($modeSnippet) {
			$snippets = $doc->xpath('snippet');
			foreach($snippets as $field) {
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
		$currentFacet = isset($fieldName)? $this->result->xpath('faceting/field[@name="'.$fieldName.'"]/facet'):null;
		if(!isset($currentFacet) || ( isset($currentFacet) && $currentFacet === false)) {
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
		foreach($allFacets as $each) {
			$facets[] = $each[0]['name'];
		}
		return $facets;
	}

}