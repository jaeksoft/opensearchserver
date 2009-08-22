<?php
/**
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
 **/

/**
 * This class provide an easy interface to the Open Search Server Query API.
 *
 * @author Pascal MERCIER <pmercier-oss@nkubz.net>
 * @package OpenSearchServer
 *
 * FIXME Change the way default parameters are handled
 * FIXME Match the error handling with OSSAPI for more consistence
 */
class SearchQuery {

	/** Default start position */
	const DEFAULT_START_ROW = 0;
	/** Default query template to use for the queries */
	const DEFAULT_QUERY_TEMPLATE = null;
	/** How much result to retrieve by default */
	const DEFAULT_ROW_COUNT = 10;
	/** Default keywords if none is provided */
	const DEFAULT_KEYWORDS = '*:*';
	/** Default sor field if none is provided */
	const DEFAULT_SORT_BY = null;

	const DEFAULT_QUERY_TIMEOUT = 10;

	/** @var string */
	private $searchEngineURL;

	private $keywords;

	private $startRow;

	private $rowCount;

	private $filters;

	private $queryTemplate;

	private $sortBy;

	private $lastQueryString;

	/**
	 * @param string $searchEngineURL The complete Open Search Server Query API URL (scheme must be http or https)
	 * @param string $queryTemplate An optional template name to be used by the query
	 * @return SearchEngine
	 *
	 * If an invalid http(s) URL is provided, a RuntimeException will be thrown.
	 *
	 * Exemple:
	 * 	$query = new SearchQuery('http://localhost:8080/select');
	 */
	public function __construct($searchEngineURL, $queryTemplate = SearchQuery::DEFAULT_QUERY_TEMPLATE) {
		if (!(filter_var($searchEngineURL, FILTER_VALIDATE_URL) && preg_match('/^https?:/', $searchEngineURL)))
			throw new RuntimeException("Invalid HTTP URL provided for the Open Search Server Query API");

		$this->searchEngineURL = $searchEngineURL;
		$this->queryTemplate = $queryTemplate;
		$this->keywords = SearchQuery::DEFAULT_KEYWORDS;

		$this->startRow	= SearchQuery::DEFAULT_START_ROW;
		$this->rowCount	= SearchQuery::DEFAULT_ROW_COUNT;
		$this->sortBy	= SearchQuery::DEFAULT_SORT_BY;
		$this->filters  = array();
		$this->queryTemplate = SearchQuery::DEFAULT_QUERY_TEMPLATE;
	}

	public function getSearch() {
		return $this->keywords;
	}

	public function setSearch($search) {
		$search = trim($search);
		if (empty($search)) $search = SearchQuery::DEFAULT_KEYWORDS;
		$this->keywords = $search;
	}

	// SEE Merge getStartRow and setStartRow ?
	/**
	 * Get the start row of the query
	 * @return integer
	 */
	public function getStartRow() {
		return $this->startRow;
	}

	/**
	 * Set the start row for the query
	 * @param integer $startRow If not specified it'll restore the default start row
	 */
	public function setStartRow($startRow = SearchQuery::DEFAULT_START_ROW) {
		if (!is_numeric($startRow))
			throw new UnexpectedValueException(__CLASS__."::".__METHOD__." expected an unsigned integer");
		if ((int)$startRow < 0)
			throw new OutOfBoundsException("The start row must be a positive integer");
		$this->startRow = (int)$startRow;
	}


	// SEE Merge getRowCount and setRowCount ?

	/**
	 * Get the number of row to retrieve
	 * @return integer
	 */
	public function getRowCount() {
		return $this->rowCount;
	}

	/**
	 * Set the number of row to retrieve
	 * @param integer $startRow >= 1 If not specified it'll restore the default row count
	 */
	public function setRowCount($rowCount = SearchQuery::DEFAULT_ROW_COUNT) {
		if (!is_numeric($rowCount))
			throw new UnexpectedValueException(__CLASS__."::".__METHOD__." expected an unsigned integer");
		if ((int)$rowCount < 0)
			throw new OutOfBoundsException("The row count must be a positive integer");
		$this->rowCount = (int)$rowCount;
	}


	// SEE Merge get/set for sortBy ?
	/**
	 * Set the sort field
	 * @param string $sortBy The name of the field to sort by
	 *
	 * Can have many field separated by a space like this:
	 * 	$query->sortBy('-title -url score');
	 * Like this it'll be sorted by title, url and then score. Note (-) mean reverse order.
	 *
	 * FIXME Use array or variable number of parameters.
	 * FIXME Provide a way to stack sort fields (add, del, clear)
	 */
	public function sortBy($sortBy = SearchQuery::DEFAULT_SORT_BY) {
		// @TODO check here if we need some additional checkings
		$sortBy = trim($sortBy);
		if (empty($sortBy)) $sortBy = SearchQuery::DEFAULT_SORT_BY;
		$this->sortBy = $sortBy;
	}

	/**
	 * Get the sort field query
	 * @return integer
	 */
	public function sortedBy() {
		return $this->sortBy;
	}

	/**
	 * Execute the query and retrieve the results from the search engine
	 * @param integer $startRow
	 * @return SearchResult
	 * FIXME If the URL it too long the query can fail. Use POST instead when url is too long.
	 */
	public function execute($startRow = null, $rowCount = null, $timeout = SearchQuery::DEFAULT_QUERY_TIMEOUT) {

		if ($startRow !== null) $this->setStartRow($startRow);
		if ($rowCount !== null) $this->setRowCount($rowCount);

		$this->lastQueryString = $this->prepareQuery();

		$ch = curl_init($this->lastQueryString);
		curl_setopt($ch, CURLOPT_MAXREDIRS,	100);
		curl_setopt($ch, CURLOPT_TIMEOUT,	$timeout);
		curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
		curl_setopt($ch, CURLOPT_CONNECTTIMEOUT, $timeout);
		curl_setopt($ch, CURLOPT_FOLLOWLOCATION, true);
		$xmlContent = curl_exec ($ch);
		curl_close($ch);

		// Check the response
		if (strpos($xmlContent, "<?xml ") !== 0)
			throw new UnexpectedValueException("The Search engine didn't respond with xml");

		$xml = simplexml_load_string($xmlContent);

		return new SearchResult($this, $xml);

	}

	/**
	 * Construct the query to be send to the search engine
	 * @return string
	 */
	private function prepareQuery() {
		$queryChunks = array();
		$queryChunks[] = 'q='.urlencode($this->keywords);
		if (!empty($this->queryTemplate))
			$queryChunks[] = 'qt='.$this->queryTemplate;
		if ($this->rowCount) $queryChunks[] = 'rows='.$this->rowCount;
		if ($this->sortBy)   $queryChunks[] = 'sort='.$this->sortBy;
		if ($this->startRow) $queryChunks[] = 'start='.$this->startRow;

		if (count($this->filters)) {
			// Encodage des filtres
			$filters = array();
			foreach ($this->filters as $filter)
				$filters[] = '('.$filter.')';
			$queryChunks[] = 'fq=('.urlencode(implode(' AND ', $filters)).')';
		}

		return $this->searchEngineURL
				.(strpos($this->searchEngineURL,'?') === false ? '?' : '&')
				.implode('&', $queryChunks);
	}

	public function getLastQueryString() {
		return $this->lastQueryString;
	}


	/**
	 * Check if the search engine is up an can respond to a query
	 * @param string $searchEngineURL The complete Open Search Server Query API URL (scheme must be http or https)
	 * @return boolean
	 */
	public static function isSearchServerUp($searchEngineURL, $timeout = SearchQuery::DEFAULT_QUERY_TIMEOUT) {
		$query = new SearchQuery($searchEngineURL);
		try {
			$result = $query->execute(0, 0, $timeout);
		}
		catch (Exception $e) {
			return false;
		}
		return (!$result instanceof SearchResult);
	}

}



/**
 * SearchQuery will return his results using this class.
 *
 * @author Pascal MERCIER <pmercier-oss@nkubz.net>
 * @package OpenSearchServer
 */
class SearchResult extends ArrayIterator {

	/** @var SearchQuery */
	private $searchQuery;

	/** @var integer */
	private $found;

	/** @var integer */
	private $queryTime;

	/** @var integer */
	private $collasedDocCount;

	/** @var float */
	private $maxScore;


	public function __construct(SearchQuery $search, SimpleXMLElement $results) {
		$this->searchQuery = clone $search;

		$headNode   = $results->xpath('header');
		$headNode	= reset($headNode);

		$resultNode = $results->xpath('result[@name="response"]');
		$resultNode = reset($resultNode);

		$this->queryTime 		= (int)$resultNode['time'];
		$this->found 			= (int)$resultNode['numFound'];
		$this->maxScore			= (float)$resultNode['maxScore'];
		$this->collasedDocCount	= (int)$resultNode['collapsedDocCount'];

		$docs = $resultNode->xpath('doc');

		parent::__construct($docs);
	}

	/**
	 * How much result where found by the search engine
	 * @return integer
	 */
	public function found() {
		return $this->found;
	}

	/**
	 * Return in millisecond the query time
	 * @return integer
	 */
	public function getQueryTime() {
		return $this->queryTime;
	}

	/**
	 * Return the greatest score for the query
	 * @return float
	 */
	public function getMaxScore() {
		return $this->maxScore;
	}

	/**
	 * How much document was collapsed
	 * @return integer
	 */
	public function getCollasedDocCount() {
		return $this->collasedDocCount;
	}

	/**
	 * Return the start row of result
	 * @return integer
	 */
	public function getStartPosition() {
		return $this->searchQuery->getStartRow();
	}

	// Prevent modifications of the results
	/** This method don't do anything */
	public function offsetSet($offset, $value) { }
	/** This method don't do anything */
	public function offsetUnset($offset) { }
	/** This method don't do anything */
	public function append($value) { }


	public function next() {
		$row = parent::next();
		if (!$row) return $row;
		if ($row instanceof SimpleXMLElement) {
			$row = SearchResultItem::factory($row);
			parent::offsetSet(parent::key(), $row);
		}
		return $row;
	}

	public function offsetGet($index) {
		$row = parent::offsetGet($index);
		if (!$row) return $row;
		if ($row instanceof SimpleXMLElement) {
			$row = SearchResultItem::factory($row);
			parent::offsetSet($index, $row);
		}
		return $row;
	}

	public function current() {
		$row = parent::current();
		if (!$row) return $row;
		if ($row instanceof SimpleXMLElement) {
			$row = SearchResultItem::factory($row);
			parent::offsetSet(parent::key(), $row);
		}
		return $row;
	}


}



/**
 * One row in the result.
 *
 * @author Pascal MERCIER <pmercier-oss@nkubz.net>
 * @package OpenSearchServer
 *
 * This class instances are created on demand. They are not created in SearchResult::__constructor,
 * but in the access method of the herited ArrayIterator by SearchResult.
 */
class SearchResultItem {

	/** @var SimpleXMLElement */
	private $resultNode;

	/**
	 * Using a factory here give you the posibility to use different class depending the result
	 * provided by the search engine
	 * @param SimpleXMLElement $resultNode
	 * @return SearchResultItem
	 */
	public static function factory(SimpleXMLElement $resultNode) {

		$result = new SearchResultItem($resultNode);
		return $result;

	}

	/**
	 * @return SearchResultItem
	 */
	public function __construct(SimpleXMLElement $resultNode) {
		// @TODO Add some control here ?
		$this->resultNode = $resultNode;
	}

	public function getScore() {
		return (float)$this->resultNode['score'];
	}

	public function getPosition() {
		return (int)$this->resultNode['position'];
	}

	public function getURL() {
		$nodes = $this->resultNode->xpath('field[@name="url"]');
		return reset($nodes);
	}

	public function getTitle() {
		$nodes = $this->resultNode->xpath('snippet[@name="title"]');
		return reset($nodes);
	}

	public function getContent() {
		$nodes = $this->resultNode->xpath('snippet[@name="content"]');
		return implode("\n", $nodes);
	}
}
?>