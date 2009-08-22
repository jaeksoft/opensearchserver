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
 */

/**
 * Open Search Server API wrapper
 * @author pmercier <pmercier-oss@nkubz.net>
 * @package OpenSearchServer
 *
 * FIXME Unify the error handling: always return an error. Use Exception ? Use false return + getError method ? What's the best ? Exceptions are prefered.
 * Prefered method is to return false and leave the user retrieve the Exception using getLastError. Other ideas ?
 */
class OSSAPI {

	const API_SEARCH   = 'select';
	const API_UPDATE   = 'update';
	const API_OPTIMIZE = 'optimize';
	const API_RELOAD   = 'reload';

	/** @var int Default timeout (specified in seconds) for CURLOPT_TIMEOUT option. See curl documentation */
	const QUERY_TIMEOUT = 0;
	/** @var int Timeout (specified in seconds) for CURLOPT_CONNECTTIMEOUT option. See curl documentation */
	const CONNEXION_TIMEOUT = 5;

	/** @var string */
	protected $searchEngineQueryPath;
	/** @var string */
	protected $index;
	/** @var Array */
	protected $lastError;

	/**
	 * @param string $searchEngineQueryPath Base URL to the search engine
	 * @param string $index Optional. Specify an index to use from the search engine. See below for details.
	 * @return OSSAPI
	 * @todo Add a control to remove the query string and the trailling slash
	 *
	 * Depending on your OSS configuration you won't have to specify the index you'll use, that's why
	 * we leaved this parameter as Optional. If you are using the default OSS configuration you must
	 * specify it else you'll receive an error from the search engine.
	 *
	 * UnexpectedValueException is thrown if the URL is invalid
	 */
	public function __construct($searchEngineQueryPath, $index = null) {
		if (!filter_var($searchEngineQueryPath, FILTER_VALIDATE_URL, FILTER_FLAG_SCHEME_REQUIRED))
			throw new UnexpectedValueException("[".$searchEngineQueryPath."] is not a valid URL.");
		$this->searchEngineQueryPath = (substr($searchEngineQueryPath, -1) == '/') ? $searchEngineQueryPath : $searchEngineQueryPath.'/';
		$this->index = $index;
	}

	/**
	 * Push a OSSDocumentIndex to the search engine to be indexed.
	 * @param OSSDocumentIndex $index The document index to push
	 * @param int $timeout Optional. Number of seconds before the query fail
	 * @return true|false|OSSError If posting produced an error
	 */
	public function pushDocumentIndex(OSSDocumentIndex $index, $timeout = OSSAPI::QUERY_TIMEOUT) {
		return $this->queryServer($this->getQueryURL(OSSAPI::API_UPDATE), (string)$index, $timeout, true);
	}

	/**
	 * Launch an optimize of the index
	 * @return true|false|OSSError If posting produced an error
	 * See the OSS Wiki [Web API optimize] documentation before using this method
	 */
	public function optimize() {
		return $this->queryServer($this->getQueryURL(OSSAPI::API_OPTIMIZE));
	}

	/**
	 * Reload the index
	 * @return true|false|OSSError If posting produced an error
	 * See the OSS Wiki [Web API reload] documentation before using this method
	 */
	public function reload() {
		return $this->queryServer($this->getQueryURL(OSSAPI::API_RELOAD));
	}

	/**
	 * Post data to an URL
	 * @param string $url
	 * @param string $data Optional. If provided will use a POST method. Only accept data as POST encoded string or raw XML string.
	 * @param int $timeout Optional. Number of seconds before the query fail
	 * @return true|false|OSSError If posting produced an error
	 *
	 * Will fail if more than 16 HTTP redirection
	 */
	protected function queryServer($url, $data = null, $timeout = OSSAPI::QUERY_TIMEOUT, $keepAlive = false) {

		// Use CURL to post the data
		$rCurl = curl_init($url);
		curl_setopt($rCurl, CURLOPT_BINARYTRANSFER, true);
		curl_setopt($rCurl, CURLOPT_RETURNTRANSFER, true);
		curl_setopt($rCurl, CURLOPT_FOLLOWLOCATION, true);
		curl_setopt($rCurl, CURLOPT_MAXREDIRS, 16);
        curl_setopt($rCurl, CURLOPT_CONNECTTIMEOUT, OSSAPI::CONNEXION_TIMEOUT);
        curl_setopt($rCurl, CURLOPT_TIMEOUT, $timeout);

        // Send provided string as POST data. Must be encoded to meet POST specification
		if ($data !== null) {
			curl_setopt($rCurl, CURLOPT_POST, true);
			curl_setopt($rCurl, CURLOPT_POSTFIELDS, (string)$data);
			curl_setopt($rCurl, CURLOPT_HTTPHEADER, array("Content-type: text/xml; charset=utf-8"));
		}

		// TODO Check if problem arise when using Connection Closing
		if (!$keepAlive) {
			curl_setopt($rCurl, CURLOPT_HTTPHEADER,  array("Connection: close"));
		}

		$content = curl_exec($rCurl);

		if ($content === false) {
			$this->lastError = array('code' => '000', 'message' => 'CURL failed to execute on URL "'.$url.'"');
			return false;
		}

		$aResponse 	= curl_getinfo($rCurl);
		if ($aResponse['http_code'] != 200)	{
			$this->lastError = array('code' => (string)$aResponse['http_code'], 'message' => 'HTTP error');
			return false;
		}

		// FIXME Possible problem to identify Locked Index message. Must set a lock on an index to check this
		if ($ossError = OSSError::factoryFromHTTPResponseString($content)) {
			$this->lastError = array('code' => 'JAVA', 'message' => $ossError->getMessage());
			return false;
		}

		return true;
	}

	/**
	 * Return the url to use with curl
	 * @param string $apiCall The Web API to call. Refer to the OSS Wiki documentation of [Web API]
	 * @return string
	 */
	protected function getQueryURL($apiCall) {
		$path = $this->searchEngineQueryPath.$apiCall;
		if (!empty($this->use)) $path .= '?use='.$this->use;
		return $path;
	}

	/**
	 * Return the last error data
	 * @return Array {code: integer, message: string}
	 */
	public function getLastError() {
		if (is_array($this->lastError)) {
			$lastError = $this->lastError;
			unset($this->lastError);
			return $lastError;
		}
	}
}

/**
 * Open Search Server Document Index
 * @author pmercier <pmercier-oss@nkubz.net>
 * @package OpenSearchServer
 *
 * Use the PHP DOM extension
 */
class OSSDocumentIndex {

	/** @var DOMDocument */
	private $document;

	/**
	 * @return OSSDocumentIndex
	 */
	public function __construct() {
		$this->document = new DOMDocument;
		$this->document->loadXML('<?xml version="1.0" encoding="UTF-8"?><index></index>');
	}

	/**
	 * Create a new Document in this index
	 * @param string $lang Must be specifed using ISO 639-1 format (en, de, fr, ...)
	 * @return OSSDocumentIndexEntry
	 */
	public function newDocument($lang) {
		return new OSSDocumentIndexEntry($this->document, $lang);
	}

	/**
	 * Return the DOMDocument as XML string
	 * @return String
	 */
	public function __toString() {
		return $this->document->saveXML();
	}

}

/**
 * Open Search Server Document Item
 * @author pmercier <pmercier-oss@nkubz.net>
 * @package OpenSearchServer
 *
 * TODO Add a way to use data from a OSSSearchResultEntry
 * TODO Check if OSS provide extended field options
 */
class OSSDocumentIndexEntry {

	/** @var DOMElement */
	private $docNode;

	/** @var DOMDocument */
	private $document;

	/**
	 * We recommand you to call OSSDocumentIndex::newDocument
	 * @param DOMDocument $document
	 * @param string $lang Must be specifed using ISO 639-1 format (en, de, fr, ...)
	 * @return OSSDocumentIndexEntry
	 */
	public function __construct(DOMDocument $document, $lang) {
		$this->document = $document;
		$this->docNode = $document->documentElement->appendChild($document->createElement('document'));
		$this->docNode->setAttribute('lang', $lang);
	}

	/**
	 * Add one or many values to the document item
	 * @param string $fieldName The field name from the schema
	 * @param mixed $values One value or an array of values. Each value is casted to string so you can
	 *                      use object implementing a __toString magic method (like SimpleXMLElement)
	 * @return DOMElement
	 */
	public function addValues($fieldName, $values, $useCDATA = false) {
		$fieldNode = $this->document->createElement('field');
		$fieldNode->setAttribute('name', $fieldName);
		foreach ((array)$values as $value) {
			if ($useCDATA) {
				$valueNode = $this->document->createElement('value');
				$valueNode->appendChild($this->document->createCDATASection($value));
			}
			else {
				$valueNode = $this->document->createElement('value', (string)$value);
			}
			$fieldNode->appendChild($valueNode);
		}
		$this->docNode->appendChild($fieldNode);

		return $fieldNode;
	}

}

/**
 * Open Search Server Error
 * @author pmercier <pmercier-oss@nkubz.net>
 * @package OpenSearchServer
 *
 * Returned when using API method if the search engine encountered an error
 */
class OSSError extends RuntimeException {

	private $status;
	protected $message;

	public function __construct() {

	}

	/**
	 * Return the error status from the search engine
	 * @return string
	 */
	public function getStatus() {
		return $this->status;
	}

	/**
	 * Return the error contained in the HTTP Response from the search engine
	 * @return OSSError or null if no error
	 */
	public static function factoryFromHTTPResponseString($string) {

		// Errors are returned using an xml
		if (strpos($string, '<?xml version="1.0"') !== 0) return null;
		$doc = new DOMDocument();
		$doc->loadXML($string);
		$keyList = $doc->documentElement->getElementsByTagName('entry');
		if ($keyList->length) {

			$data = array();
			for ($i = 0; $i < $keyList->length; $i++) {
				$node = $keyList->item($i);
				if ($node->firstChild instanceof DOMText) {
					$data[$node->getAttribute("key")] = $node->firstChild->wholeText;
				}
			}

			if (!isset($data['Status'])) return null;

			$ossError = new OSSError();
			$ossError->status  = $data['Status'];
			$ossError->message = $data['Exception'];

			if ($data['Status'] != 'OK')
				return $ossError;
			return null;
		}

		return null;
	}

}
?>