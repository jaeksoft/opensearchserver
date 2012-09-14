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

abstract class OssAbstract {

  protected $enginePath;
  protected $index;
  protected $login;
  protected $apiKey;

  public function init($enginePath, $index = NULL, $login = NULL, $apiKey = NULL) {

    if (!function_exists('OssApi_Dummy_Function')) {
      function OssApi_Dummy_Function() {
      }
    }
    	
    $parsedPath = OssApi::parseEnginePath($enginePath, $index);
    $this->enginePath  = $parsedPath['enginePath'];
    $this->index    = $parsedPath['index'];

    $this->credential($login, $apiKey);
  }

  /**
   * Assign credential information for the next queries
   * @param $login string
   * @param $apiKey string
   * If $login is empty, credential is removed
   */
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

  /**
   * Return the url to use with curl
   * param string $apiCall The Web API to call. Refer to the OSS Wiki documentation of [Web API]
   * param string[] $options Additional query parameters
   * return string
   * Use OssApi::API_* constants for $apiCall.
   * Optionals query parameters are provided as a named list:
   * array(
   *   "arg1" => "value1",
   *   "arg2" => "value2"
   * )
   */
  protected function getQueryURL($apiCall, $options = NULL) {

    $path = $this->enginePath . '/' . $apiCall;
    $chunks = array();

    if (!empty($this->index)) {
      $chunks[] = 'use=' . urlencode($this->index);
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