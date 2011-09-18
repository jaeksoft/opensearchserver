<?php
/*
 *  This file is part of OpenSearchServer.
 *
 *  Copyright (C) 2008-2009 Emmanuel Keller / Jaeksoft
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

if (!class_exists('OSS_API')) { trigger_error("OSS_Search won't work whitout OSS_API", E_USER_ERROR); die(); }

/**
 * @author pmercier <pmercier-oss@nkubz.net>
 * @package OpenSearchServer
 * FIXME Complete this documentations
 */
class OSS_Search {

	const VERSION = '1.1';
	
	protected $enginePath;
	protected $index;
	protected $query;
	protected $template;
	protected $start;
	protected $rows;
	protected $lang;
	protected $filter;
	protected $delete;
	protected $field;
	protected $sort;
	protected $collapse;
	protected $facet;
	protected $sortBy;

	protected $lastQueryString;

	/**
	 * @param $enginePath The URL to access the OSS Engine
	 * @param $index The index name
	 * @return OSS_Search
	 */
	public function __construct($enginePath, $index = null) {
		
		$parsedPath = OSS_API::parseEnginePath($enginePath, $index);
		$this->enginePath	= $parsedPath['enginePath'];
		$this->index		= $parsedPath['index'];

		$this->field	= array();
		$this->filter	= array();
		$this->sort		= array();
		$this->facets	= array();
		$this->collapse	= array('field' => null, 'max' => null, 'mode' => null);

	}

	/**
	 * Specify the index name to query
	 * @param $index string
	 * @return OSS_Search
	 */
	public function index($index = null) {
		$this->index = $index;
		return $this;
	}

	/**
	 * Specify the query
	 * @param $query string
	 * @return OSS_Search
	 */
	public function query($query = null) {
		$this->query = $query;
		return $this;
	}

	/**
	 * @return OSS_Search
	 */
	public function template($template = null) {
		$this->template = $template;
		return $this;
	}

	/**
	 * @return OSS_Search
	 */
	public function start($start = null) {
		$this->start = $start;
		return $this;
	}

	/**
	 * @return OSS_Search
	 */
	public function rows($rows = null) {
		$this->rows = $rows;
		return $this;
	}

	/**
	 * @return OSS_Search
	 */
	public function filter($filter = null) {
		$this->filter[] = $filter;
		return $this;
	}

	/**
	 * @return OSS_Search
	 */
	public function lang($lang = null) {
		$this->lang = $lang;
		return $this;
	}

	/**
	 * @return OSS_Search
	 */
	public function delete($delete = true) {
		$this->delete = (bool)$delete;
		return $this;
	}

	/**
	 * @return OSS_Search
	 */
	public function field($fields) {
		$this->field = array_unique(array_merge($this->field, (array)$fields));
		return $this;
	}

	/**
	 * @return OSS_Search
	 */
	public function sort($fields) {
		foreach ((array)$fields as $field)
			$this->sort[] = $field;
		return $this;
	}

	/**
	 * @return OSS_Search
	 */
	public function collapseField($field) {
		$this->collapse['field'] = $field;
		return $this;
	}

	/**
	 * @return OSS_Search
	 */
	public function collapseMode($mode) {
		$this->collapse['mode'] = $mode;
		return $this;
	}

	/**
	 * @return OSS_Search
	 */
	public function collapseMax($max) {
		$this->collapse['max'] = $max;
		return $this;
	}

	/**
	 * @return OSS_Search
	 */
	public function facet($field, $min = null, $multi = false) {
		$this->facet[$field] = array('min' => $min, 'multi' => $multi);
		return $this;
	}

	/**
	 * @return SimpleXMLElement False if the query produced an error
	 */
	public function execute($connectTimeOut = null, $timeOut = null) {

		// Do the query
		$this->lastQueryString = $this->prepareQueryString();
		$result = OSS_API::queryServer($this->lastQueryString, null, $connectTimeOut, $timeOut);
		if ($result === false) return false;
		
		// Check if we have a valid XML string from the engine
		$lastErrorLevel = error_reporting(0);
		$xmlResult = simplexml_load_string(OSS_API::cleanUTF8($result));
		error_reporting($lastErrorLevel);
		if (!$xmlResult instanceof SimpleXMLElement) {
			if (class_exists('OSSException'))
				throw new RuntimeException("The search engine didn't return a valid XML");
			trigger_error("The search engine didn't return a valid XML", E_USER_WARNING);
			return false;
		}

		return $xmlResult;

	}

	/**
	 * Return the last query string
	 * @return string
	 */
	public function getLastQueryString() {
		return $this->lastQueryString;
	}

	protected function prepareQueryString() {

		$queryChunks = array();

		$queryChunks[] = 'q='.urlencode((empty($this->query) ? "*:*" : $this->query));

		if (!empty($this->index))	 $queryChunks[] = 'use='.$this->index;
		if (!empty($this->template)) $queryChunks[] = 'qt='.$this->template;
		if (!empty($this->lang)) 	 $queryChunks[] = 'lang='.$this->lang;

		if ($this->rows		!== null) $queryChunks[] = 'rows='.(int)$this->rows;
		if ($this->start	!== null) $queryChunks[] = 'start='.(int)$this->start;

		if ($this->delete) $queryChunks[] = 'delete';

		// Sorting
		foreach ((array)$this->sortBy as $sort) {
			if (empty($sort)) continue;
			$queryChunks[] = 'sort='.$sort;
		}

		// Filters
		foreach ((array)$this->filter as $filter) {
			if (empty($filter)) continue;
			$queryChunks[] = 'fq='.urlencode($filter);
		}

		// Fields
		foreach ((array)$this->field as $field) {
			if (empty($field)) continue;
			$queryChunks[] = 'rf='.$field;
		}

		// Facets
		foreach ((array)$this->facet as $field => $options) {
			$facet  = $options['multi'] ? 'facet.multi=' : 'facet=';
			$facet .= $field;
			if ($options['min'] !== null)
				$facet .= '('.$options['min'].')';
			$queryChunks[] = $facet;
		}

		// Collapsing
		if ($this->collapse['field'])
			$queryChunks[] = 'collapse.field='.$this->collapse['field'];
		if ($this->collapse['mode'] !== null)
			$queryChunks[] = 'collapse.mode='.$this->collapse['mode'];
		if ($this->collapse['max'] !== null)
			$queryChunks[] = 'collapse.max='.(int)$this->collapse['max'];
		
		return $this->enginePath.'/'.OSS_API::API_SELECT.'?'.implode('&', $queryChunks);

	}
}