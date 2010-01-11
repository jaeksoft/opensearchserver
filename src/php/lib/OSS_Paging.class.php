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
class OSS_Paging {

	protected $oss_results;
	protected $resultTotalPages;
	protected $resultLowPage;
	protected $resultHighPage;
	protected $resultLowPrev;
	protected $resultHighNext;
	protected $pageBaseURI;


	/**
	 * @param $result The data
	 * @param $model The list of fields
	 * @return OSS_API
	 */
	public function __construct(OSS_Results $oss_results) {
		$this->oss_results	= $oss_results;
		self::compute();

		if (!function_exists('OSS_API_Dummy_Function')) { function OSS_API_Dummy_Function() {} }
	}

	/**
	 * GETTER
	 */
	public function getResultCurrentPage() {
		return $this->resultCurrentPage;
	}
	public function getResultTotalPages() {
		return $this->resultTotalPages;
	}
	public function getResultLowPage() {
		return $this->resultLowPage;
	}
	public function getResultHighPage() {
		return $this->resultHighPage;
	}
	public function getResultLowPrev() {
		return $this->resultLowPrev;
	}
	public function getResultHighNext() {
		return $this->resultHighNext;
	}
	public function getPageBaseURI() {
		return $this->pageBaseURI;
	}


	public function compute() {
		$this->resultFound   = (int)$this->oss_results->getResult()->result['numFound'];
		$this->resultTime    = (float)$this->oss_results->getResult()->result['time'] / 1000;
		$this->resultRows    = (int)$this->oss_results->getResult()->result['rows'];
		$this->resultStart   = (int)$this->oss_results->getResult()->result['start'];

		$this->resultCurrentPage = floor($this->resultStart / $this->resultRows);
		$this->resultTotalPages  = ceil($this->resultFound / $this->resultRows);

		if ($this->resultTotalPages > 1) {
			$low  = $this->resultCurrentPage - (MAX_PAGE_TO_LINK / 2);
			$high = $this->resultCurrentPage + (MAX_PAGE_TO_LINK / 2 - 1);
			if ($low < 0) {
				$high += $low * -1;
			}
			if ($high > $this->resultTotalPages) {
				$low -= $high - $this->resultTotalPages;
			}

			$this->resultLowPage  = max($low, 0);
			$this->resultHighPage = min($this->resultTotalPages, $high);
			$this->resultLowPrev  = max($this->resultCurrentPage - MAX_PAGE_TO_LINK, 0);
			$this->resultHighNext = min($this->resultCurrentPage + MAX_PAGE_TO_LINK, $this->resultTotalPages);
			$this->pageBaseURI = preg_replace('/&(?:p|rows)=[\d]+/', '', $_SERVER['REQUEST_URI']).'&rows='.$this->resultRows.'&p=';
		}
	}






}