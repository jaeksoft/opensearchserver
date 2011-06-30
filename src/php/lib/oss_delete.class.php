<?php
/*
 *  This file is part of Jaeksoft OpenSearchServer.
 *
 *  Copyright (C)2011 Emmanuel Keller / Jaeksoft
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

if (!class_exists('OSS_API')) { trigger_error("OSS_Search won't work whitout OSS_API", E_USER_ERROR); die(); }

 
class oss_delete {

	protected $enginePath;
	protected $index;
	
	
	 
	 
	public function __construct($enginePath, $index = null, $login = null, $apiKey = null) {
		
		$ossAPI = new OSS_API($enginePath, $index);

		$this->enginePath	= $ossAPI->getEnginePath();
		$this->index		= $ossAPI->getIndex();

		$this->credential($login, $apiKey);

		 
	}
 
	public function delete($query)
		{
			$params = array("q" => $query);	
			$return = OSS_API::queryServerXML($this->getQueryURL(OSS_API::API_DELETE, $this->index	, OSS_API::API_SCHEMA_DELETE_FIELD, $params));
			if ($return === false) return false;
			return true;
		}
	public function credential($login, $apiKey) {
		// Remove credentials
		if (empty($login)) {
			$this->login	= null;
			$this->apiKey	= null;
			return;
		}

		// Else parse and affect new credentials
		if (empty($login) || empty($apiKey)) {
			if (class_exists('OSSException'))
			throw new UnexpectedValueException('You must provide a login and an api key to use credential.');
			trigger_error(__CLASS__.'::'.__METHOD__.': You must provide a login and an api key to use credential.', E_USER_ERROR);
			return false;
		}

		$this->login	= $login;
		$this->apiKey	= $apiKey;
	}
	protected function getQueryURL($apiCall, $index = null, $cmd = null, $options = null) {
		
		$path = $this->enginePath.'/'.$apiCall;
		$chunks = array();
		
		if (!empty($index)) $chunks[] = 'use='.urlencode($index);
		
		if (!empty($cmd)) $chunks[] = 'cmd='.urlencode($cmd);
		
		// If credential provided, include them in the query url
		if (!empty($this->login)) {
			$chunks[] = "login=". urlencode($this->login);
			$chunks[] = "key="	. urlencode($this->apiKey);
		}
		
		// Prepare additionnal parameters
		if (is_array($options)) {
			foreach ($options as $argName => $argValue) {
				$chunks[] = $argName . "=" . urlencode($argValue);
			}
		}
		
		$path .= (strpos($path, '?') !== false ? '&' : '?') . implode("&", $chunks);
		
		return $path;
	}

 
}