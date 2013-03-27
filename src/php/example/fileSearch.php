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
 * Simple search engine in PHP
 * @author pmercier <pmercier-oss@nkubz.net>
 * @package OpenSearchServer
 * @todo Some documentation ?
 * @todo Add sort params
 */
header('Content-type: text/html; charset=UTF-8');

// The required classes
define('BASE_DIR', dirname(__FILE__));
require BASE_DIR.'/../lib/oss_misc.lib.php';
require BASE_DIR.'/../lib/oss_api.class.php';
require BASE_DIR.'/../lib/oss_results.class.php';
require BASE_DIR.'/../lib/oss_paging.class.php';

define('MAX_PAGE_TO_LINK', 10);

$ossEnginePath  = config_request_value('ossEnginePath', 'http://localhost:8080', 'engineURL');
$ossEngineConnectTimeOut = config_request_value('ossEngineConnectTimeOut', 5, 'engineConnectTimeOut');
$ossEngineIndex = config_request_value('ossEngineIndex_contrib_filesearch', 'fileCrawler', 'engineIndex');
$ossEngineLogin = config_request_value('ossEngineLogin_contrib_filesearch', '', 'engineLogin');
$ossEngineApiKey = config_request_value('ossEngineApiKey_contrib_filesearch', '', 'engineApiKey');

if (isset($_REQUEST['query'])) {

	$search = new OssSearch($ossEnginePath, $ossEngineIndex);
	if (!empty($ossEngineLogin) && !empty($ossEngineApiKey)) {
		$search->credential($ossEngineLogin, $ossEngineApiKey);
	}

	if (!empty($_REQUEST['lang'])) {
		$search->lang($_REQUEST['lang'])
			   ->filter('lang:'.$_REQUEST['lang']);
	}

	// Restrict row count between 5 and 50
	$rows  = isset($_REQUEST['rows']) ? max(5, min($_REQUEST['rows'], 50)) : 10;
	$start = isset($_REQUEST['p'])    ? max(0, $_REQUEST['p']) * $rows : 0;

	$result = $search->query($_REQUEST['query'])
					 ->template('fileSearch')
					 ->facet('lang', 0)
					 ->field(array('uri', 'crawlDate', 'directory', 'fileSystemDate', 'metaDescription', 'metaKeywords', 'lang', 'title', 'when'))
					 ->start($start)
					 ->rows(10)
					 ->execute($ossEngineConnectTimeOut);

	if ($result instanceof SimpleXMLElement) {

		$resultFound   = (int)$result->result['numFound'];
		$resultTime    = (float)$result->result['time'] / 1000;
		$resultRows    = (int)$result->result['rows'];
		$resultStart   = (int)$result->result['start'];

		$resultEntries = $result->xpath('result/doc');

		// Navigation between pages
		$resultCurrentPage = floor($resultStart / $resultRows);
		$resultTotalPages  = floor($resultFound / $resultRows);
		if ($resultTotalPages > 1) {
			$low  = $resultCurrentPage - (MAX_PAGE_TO_LINK / 2);
			$high = $resultCurrentPage + (MAX_PAGE_TO_LINK / 2 - 1);
			if ($low < 0) {
				$high += $low * -1;
			}
			if ($high > $resultTotalPages) {
				$low -= $high - $resultTotalPages;
			}
			$resultLowPage  = max($low, 0);
			$resultHighPage = min($resultTotalPages, $high);
			$resultLowPrev  = max($resultCurrentPage - MAX_PAGE_TO_LINK, 0);
			$resultHighNext = min($resultCurrentPage + MAX_PAGE_TO_LINK, $resultTotalPages);
			$pageBaseURI = preg_replace('/&(?:p|rows)=[\d]+/', '', $_SERVER['REQUEST_URI']).'&rows='.$resultRows.'&p=';
		}

		$langFacets = $result->xpath('faceting/field[@name="lang"]/facet');
		if ($langFacets === false) $langFacets = array();
	}
}
?>
<html>
	<head>
		<title>Open Search Server - contrib/search</title>
		<script type="text/javascript" src="common.js"></script>
		<link type="text/css" rel="stylesheet" href="common.css" />
		<link type="text/css" rel="stylesheet" href="mime.css" />
		<style type="text/css">
			#query_fieldset { border: 0; background: url(search.png) no-repeat; width: 495px; height: 60px; padding: 26px 0 10px 110px; margin:0 auto; }
			#query { margin-left: 0; width: 318px; border: 0; background: transparent; font-size: 20px; height: 30px; }
			#submit { background: transparent; border: 0px; width: 160px; height: 29px; cursor: pointer; }
			#info { margin-bottom: 15px; text-align: center; }
			#langs { border: 0; width: 510px; padding: 0; margin:0 auto; text-align: center; }

			ul {
				list-style: none;
				border-top: 1px solid #E8E8E8;
				margin: 0px;
				padding: 0px;
			}

			/** RESULTS *******************************************************/
			.result li {
				margin-bottom: 15px;
			}
			.result li h2 {
				font-size: 12px;
				font-weight: normal;
				margin-bottom: 2px;
			}
			.result li cite, li code {
				color: green;
				font-family: arial,sans-serif;
			}
			.result li code {
				font-weight: bold;
			}
			.result li .content em,
			.result li .content strong,
			.result li .content b {
				color: #A03030;
			}
			.fileext {
				float:left;
				margin-right: 5px;
			}
			.time {
				color: #A0A0A0;
				font-style: italic;
			}
			/** PAGINATION ****************************************************/
			ul.pagination {
				text-align: right;
			}
			ul.pagination li {
				display: inline;
				padding-right: 5px;
			}
			ul.pagination .currentPage {
				font-weight: bold;
			}
		</style>
	</head>
	<body>
		<div id="bodywrap">
			<div class="lateralBorders" style="margin-bottom: 15px; border-color: #F8F8F8;">
			<div class="lateralBorders" style="border-color: #EDEDED;">
			<div class="lateralBorders" style="border-color: #E0E0E0;">
			<div class="lateralBorders" style="padding: 15px; border-color: #B8B8B8;">
				<div id="options">
					<form action="<?php echo basename(__FILE__); ?>" method="POST">
						<fieldset id="option_fieldset">
							<label>Engine URL</label><input name="engineURL" value="<?php echo $ossEnginePath; ?>" style="border-top-width:1px"/>
							<label>Index</label><input name="engineIndex" value="<?php echo $ossEngineIndex; ?>" />
							<label>Login</label><input name="engineLogin" value="<?php echo $ossEngineLogin; ?>" style="border-top-width:1px"/>
							<label>API Key</label><input name="engineApiKey" value="<?php echo $ossEngineApiKey; ?>" />
							<label>ConnectTimeOut (s)</label><input name="engineConnectTimeOut" value="<?php echo $ossEngineConnectTimeOut; ?>" />
							<label>&nbsp;</label><input type="submit" value="save">
						</fieldset>
					</form>
					<div id="options_title" onclick="javascript:toggleClass(this.parentNode, 'show'); return false; ">Options</div>
				</div>
				<form action="<?php echo basename(__FILE__); ?>" method="GET">


					<fieldset id="query_fieldset">
						<input id="query" name="query" value="<?php if (isset($_REQUEST['query'])) echo htmlspecialchars($_REQUEST['query']); ?>" />
						<input id="submit" type="submit" value=""/>
					</fieldset>
					<?php
						if (isset($langFacets)):
						if (count($langFacets)):
					?>
					<div id="langs">
						Languages:
						<input type="radio" name="lang" value="" id="lang_all" <?php if(!isset($_REQUEST['lang']) || $_REQUEST['lang'] == '') echo 'checked="checked"'; ?>>
						<label for="lang_all">All</label>
						<?php
							foreach ($langFacets as $langFacet):
								$lang = $langFacet['name'];
						?>
						<input type="radio" name="lang" value="<?php echo $lang; ?>" id="<?php echo $lang; ?>" <?php if(isset($_REQUEST['lang'])) if($_REQUEST['lang'] == $lang) echo 'checked="checked"'; ?>>
						<label for="<?php echo $lang; ?>"><?php echo ucfirst($lang); ?></label>
						<?php endforeach; ?>
					</div>
					<?php endif; endif; ?>
					<?php if (isset($search)): ?>
					<p>You queried the file search engine with the following call:<br/>
					<a style="padding-left: 15px;" class="lastQueryQtring" href="<?php echo $search->getLastQueryString(); ?>"><?php echo htmlentities(urldecode($search->getLastQueryString())); ?></a>
					</p>
					<?php endif; ?>
				</form>
				<?php
				if (isset($resultFound)):
				if ($resultFound): ?>
				<div class="result">
					<span>Found <?php if ($resultFound == 1): ?>1 result<?php else: echo $resultFound; ?> results<?php endif; ?></span>
					<span>(<?php printf('%0.2fs', $resultTime); ?>)</span>
					<ul><?php
						foreach ($resultEntries as $entry):

							$uri	   = array_first($entry->xpath('*[@name="uri"]'));
							$directory = array_first($entry->xpath('*[@name="directory"]'));
							$file	   = str_replace($directory, '', $uri);
							$date	   = array_first($entry->xpath('*[@name="fileSystemDate"]'));
							$dateTS    = strtotime(preg_replace('/^(\d{4})(\d{2})(\d{2})(\d{2})(\d{2})(\d{2})\d+$/', '$1-$2-$3 $4:$5:$6', $date));
							$crawlDate = array_first($entry->xpath('*[@name="crawlDate"]'));
							$extension = array_last(explode('.', $file));
							$content   = implode('', $entry->xpath('snippet[@name="content"]'));
							if (empty($content)) {
								$content   = implode('', $entry->xpath('field[@name="content"]'));
							}

							//$subType = preg_replace('/^[^\/]+\//', '', $type);

							?>
								<li>
									<div class="fileext fileext-<?php echo $extension; ?>">&nbsp;</div>
									<h2><code><?php echo "[".$extension."] "; ?></code> <a href="<?php echo $uri; ?>" target="_new"><?php echo $file; ?></a> - <span class="time"><?php echo strftime("%a %e %b %y %H:%M:%S", $dateTS); ?></span></h2>
									<div class="content"><?php echo $content; ?></div>
									<cite><?php echo $directory; ?></cite>
								</li>
					<?php endforeach; ?>
					</ul>
				</div>
				<?php if ($resultTotalPages > 1): ?>
				<div>
					<ul class="pagination">
						<?php if ($resultLowPage > 2):?>
						<li><a href="<?php echo $pageBaseURI, 0; ?>">First&lt;&lt;</a></li>
						<?php endif;?>
						<?php if ($resultLowPrev < $resultLowPage):?>
						<li><a href="<?php echo $pageBaseURI, $resultLowPrev; ?>">Prev&lt;&lt;</a></li>
						<?php endif;?>
						<?php for ($i = $resultLowPage; $i <= $resultHighPage; $i++): ?>
						<li><a href="<?php echo $pageBaseURI, $i; ?>" <?php if ($i == $resultCurrentPage): ?>class="currentPage"<?php endif; ?>><?php echo $i + 1; ?></a></li>
						<?php endfor;?>
						<?php if ($resultHighNext > $resultHighPage):?>
						<li><a href="<?php echo $pageBaseURI, $resultHighNext; ?>">&gt;&gt;Next</a></li>
						<?php endif;?>
						<?php if ($resultHighPage < $resultTotalPages):?>
						<li><a href="<?php echo $pageBaseURI, $resultTotalPages; ?>">&gt;&gt;Last</a></li>
						<?php endif;?>
					</ul>
				</div>
				<?php endif; ?>
				<?php else: ?>
				<div id="result">
					No result
					<span>(<?php printf('%0.2fs', $resultTime); ?>)</span>
				</div>
				<?php endif; ?>
				<?php endif; ?>
			</div>
			</div>
			</div>
			</div>
			<div id="info">
				Powered by <a href="http://www.open-search-server.com/">Open Search Server</a>. Copyright Jaeksoft.<br/>
				The file icons from <a href="http://www.everaldo.com/crystal/">Everaldo</a> are released under LGPL.
			</div>
		</div>
	</body>
</html>

