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

/**
 * Simple search engine in PHP
 * @author pmercier <pmercier-oss@nkubz.net>
 * @package OpenSearchServer
 * FIXME Some documentation ?
 */

header('Content-type: text/html; charset=UTF-8');

// The required classes
define('BASE_DIR', dirname(__FILE__));
require BASE_DIR.'/lib/oss_misc.lib.php';
require BASE_DIR.'/lib/oss_api.class.php';
require BASE_DIR.'/lib/oss_results.class.php';
require BASE_DIR.'/lib/oss_paging.class.php';

define('MAX_PAGE_TO_LINK', 10); //The Maximum number of pagination links
define('MAX_RESULT_PER_PAGE', 10);//The Maximum number of Search Result per page

$ossEnginePath  = config_request_value('ossEnginePath', 'http://localhost:8080/', 'engineURL');
$ossEngineConnectTimeOut = config_request_value('ossEngineConnectTimeOut', 5, 'engineConnectTimeOut');
$ossEngineIndex = config_request_value('ossEngineIndex_contrib_websearch', 'oss_web', 'engineIndex');
$ossEngineLogin = config_request_value('ossEngineLogin_contrib_filesearch', '', 'engineLogin');
$ossEngineApiKey = config_request_value('ossEngineApiKey_contrib_filesearch', '', 'engineApiKey');

if (isset($_REQUEST['query'])) {

	$rows  = isset($_REQUEST['rows']) ? $_REQUEST['rows'] : null;
	$start = isset($_REQUEST['p'])    ? $_REQUEST['p'] : null;

	// Restrict row count at 50 max
	$rows  = isset($rows) ? max(1, min($rows, 50)) : MAX_RESULT_PER_PAGE;
	$start = isset($start)    ? max(0, $start-1) * $rows : 0;

	$search = new OssSearch($ossEnginePath, $ossEngineIndex, $rows, $start);
	if (!empty($ossEngineLogin) && !empty($ossEngineApiKey)) {
		$search->credential($ossEngineLogin, $ossEngineApiKey);
	}

	if (!empty($_REQUEST['lang'])) {
		$search->filter('lang:'.$_REQUEST['lang']);
	}
	if (!empty($_REQUEST['host'])) {
		$search->filter('host:'.$_REQUEST['host']);
	}

	$result = $search->query($_REQUEST['query'])
	->facet('lang', 1)
	->facet('host', 1)
	->field(array('url', 'contentBaseType', 'metaDescription', 'metaKeywords', 'host', 'lang'))
	->execute($ossEngineConnectTimeOut);

	if ($result instanceof SimpleXMLElement) {
		$ossResults = new OSSResults($result);
		$ossPaging = new OSSPaging($result);
	}
}
?>
<html>
<head>
<title>Open Search Server - contrib/search</title>
<script type="text/javascript" src="common.js"></script>
<link type="text/css" rel="stylesheet" href="common.css" />
<style type="text/css">
#query_fieldset {
	border: 0;
	background: url(search.png) no-repeat;
	width: 495px;
	height: 60px;
	padding: 26px 0 10px 110px;
	margin: 0 auto;
}

#query {
	margin-left: 0;
	width: 318px;
	border: 0;
	background: transparent;
	font-size: 20px;
	height: 30px;
}

#submit {
	background: transparent;
	border: 0px;
	width: 160px;
	height: 29px;
	cursor: pointer;
}

#info {
	margin-bottom: 15px;
	text-align: center;
}

#langs {
	display: block;
	border: 1;
	width: 200px;
	height: 100%;
	padding: 0;
	margin: 0 auto;
	text-align: left;
	float: left;
}

ul {
	list-style: none;
	border-top: 1px solid #E8E8E8;
	margin: 0px;
	padding: 0px;
}

/** RESULTS *******************************************************/
.result ul {
	float: right;
	width: 750px;
}

.result li {
	margin-bottom: 15px;
}

.result li h2 {
	font-size: 12px;
	font-weight: normal;
	margin-bottom: 2px;
}

.result li cite,li code {
	color: green;
	font-family: arial, sans-serif;
}

.result li code {
	font-weight: bold;
}

.result li .content em,.result li .content strong,.result li .content b
	{
	color: #A03030;
}

/** PAGINATION ****************************************************/
ul.pagination {
	text-align: center;
}

ul.pagination li {
	display: inline;
	padding-right: 5px;
}

ul.pagination .currentPage {
	font-weight: bold;
}
</style>
<script language="javascript">
function fsubmit()
{
	window.document.forms['search'].submit();
}
</script>
</head>
<body>
	<div id="bodywrap">
		<div class="lateralBorders"
			style="margin-bottom: 15px; border-color: #F8F8F8;">
			<div class="lateralBorders" style="border-color: #EDEDED;">
				<div class="lateralBorders" style="border-color: #E0E0E0;">
					<div class="lateralBorders"
						style="padding: 15px; border-color: #B8B8B8;">

						<div id="options">
							<form action="<?php echo basename(__FILE__); ?>" method="POST">
								<fieldset id="option_fieldset">
									<label>Engine URL</label><input name="engineURL"
										value="<?php echo $ossEnginePath; ?>"
										style="border-top-width: 1px" /> <label>Index</label><input
										name="engineIndex" value="<?php echo $ossEngineIndex; ?>" /> <label>Login</label><input
										name="engineLogin" value="<?php echo $ossEngineLogin; ?>"
										style="border-top-width: 1px" /> <label>API Key</label><input
										name="engineApiKey" value="<?php echo $ossEngineApiKey; ?>" />
									<label>ConnectTimeOut(s)</label><input
										name="engineConnectTimeOut"
										value="<?php echo $ossEngineConnectTimeOut; ?>" /> <label>&nbsp;</label><input
										type="submit" value="save">
								</fieldset>
							</form>
							<div id="options_title"
								onclick="javascript:toggleClass(this.parentNode, 'show'); return false;">Options</div>
						</div>

						<form action="<?php echo basename(__FILE__); ?>" name="search"
							method="GET">
							<fieldset id="query_fieldset">
								<input id="query" name="query"
									value="<?php if (isset($_REQUEST['query'])) echo htmlspecialchars($_REQUEST['query']); ?>" />
								<input id="submit" type="submit" value="" />
							</fieldset>

							<?php if (isset($search)): ?>
							<p>
								You queried the search engine with the following call:<br /> <a
									style="padding-left: 15px;" class="lastQueryQtring"
									href="<?php echo $search->getLastQueryString(); ?>"
									target="_blank"><?php echo htmlentities(urldecode($search->getLastQueryString())); ?>
								</a>
							</p>
							<?php endif; ?>

							<?php if (isset($ossResults)): ?>
							<?php if ($ossResults->getResultFound()): ?>

							<div class="result">
								<span>Found <?php if ($ossResults->getResultFound() == 1): ?>1
									result <?php else: echo $ossResults->getResultFound(); ?>
									results <?php endif; ?>
								</span> <span>(<?php printf('%0.2fs', $ossResults->getResultTime()); ?>)
								</span>
							</div>

							<?php if (isset($ossResults) && count($ossResults->getFacets())): ?>
							<div id="langs">
								<?php foreach ($ossResults->getFacets() as $facet): ?>
								<br />
								<?php echo $facet; ?>
								: <br /> <input type="radio" name="<?php echo $facet; ?>"
									value="" id="<?php echo $facet; ?>_all"
									<?php if(!isset($_REQUEST[''.$facet]) || $_REQUEST[''.$facet] == '') echo 'checked="checked"'; ?>>
								<label for="<?php echo $facet; ?>_all">All</label> <br />
								<?php foreach ($ossResults->getFacet($facet) as $values): $value = $values['name']; ?>
								<input type="radio" name="<?php echo $facet; ?>"
									value="<?php echo $value; ?>" id="<?php echo $value; ?>"
									<?php if(isset($_REQUEST[''.$facet]) && $_REQUEST[''.$facet] == $value) echo 'checked="checked"'; ?>>
								<label for="<?php echo $value; ?>"><?php echo ucfirst($value); ?>
								</label>&nbsp;(
								<?php echo $values; ?>
								) <br />
								<?php endforeach; ?>
								<?php endforeach; ?>
							</div>
							<?php endif; ?>
						</form>

						<div class="result">
							<ul>
								<?php
								$max = ($ossResults->getResultStart() + $ossResults->getResultRows()> $ossResults->getResultFound())?$ossResults->getResultFound():$ossResults->getResultStart() + $ossResults->getResultRows();
								for ($i = $ossResults->getResultStart(); $i < $max; $i++):

								$indice = $i +1;
								$title	 = $ossResults->getField($i, 'title', true);
								$url	 = $ossResults->getField($i, 'url');
								$host	 = $ossResults->getField($i, 'host');
								$type	 = $ossResults->getField($i, 'contentBaseType');
								$content = $ossResults->getField($i, 'content', true);

								$subType = preg_replace('/^[^\/]+\//', '', $type);
								?>
								<?php if ($type == 'text/html' && !empty($content)): ?>
								<li>
									<h2>
										<?php echo $indice; ?>
										- <a href="<?php echo $url; ?>" target="_new"><?php echo $title; ?>
										</a>
									</h2>
									<div>
										<?php echo $content; ?>
									</div> <cite><?php echo $host; ?> </cite>
								</li>
								<?php elseif ($type == 'text/html'): ?>
								<li>
									<h2>
										<?php echo $indice; ?>
										- <a href="<?php echo $url; ?>" target="_new"><?php echo $url; ?>
										</a>
									</h2> <cite><?php echo $host; ?> </cite>
								</li>
								<?php else: ?>
								<li>
									<h2>
										<code>
											<?php echo $indice; ?>
											-
											<?php echo "[".$subType."] "; ?>
										</code>
										<a href="<?php echo $url; ?>" target="_new"><?php echo $url; ?>
										</a>
									</h2>
									<div>
										<?php echo $content; ?>
									</div> <cite><?php echo $host; ?> </cite>
								</li>
								<?php endif; ?>
								<?php endfor; ?>
							</ul>
							<br style="clear: both;" />
						</div>
						<?php if (isset($ossPaging) && $ossPaging->getResultTotal() >= 1): ?>
						<div>
							<ul class="pagination">
								<?php if ($ossPaging->getResultLow() > 0):?>
								<li><a href="<?php echo $ossPaging->getPageBaseURI(), 0; ?>">First&lt;&lt;</a>
								</li>
								<?php endif;?>

								<?php if ($ossPaging->getResultPrev() < $ossPaging->getResultLow()):?>
								<li><a
									href="<?php echo $ossPaging->getPageBaseURI(), $ossPaging->getResultPrev(); ?>">Prev&lt;&lt;</a>
								</li>
								<?php endif;?>

								<?php for ($i = $ossPaging->getResultLow(); $i < $ossPaging->getResultHigh(); $i++): ?>
								<li><a href="<?php echo $ossPaging->getPageBaseURI(), $i+1; ?>"
								<?php if ($i == $ossPaging->getResultCurrentPage()): ?>
									class="currentPage" <?php endif; ?>><?php echo $i + 1; ?> </a>
								</li>
								<?php endfor;?>

								<?php if ($ossPaging->getResultNext() > $ossPaging->getResultHigh()):?>
								<li><a
									href="<?php echo $ossPaging->getPageBaseURI(), $ossPaging->getResultNext(); ?>">&gt;&gt;Next</a>
								</li>
								<?php endif;?>

								<?php if ($ossPaging->getResultHigh()+1 < $ossPaging->getResultTotal()):?>
								<li><a
									href="<?php echo $ossPaging->getPageBaseURI(), $ossPaging->getResultTotal(); ?>">&gt;&gt;Last</a>
								</li>
								<?php endif;?>
							</ul>
						</div>
						<?php endif; ?>
						<?php else: ?>
						<div id="result">
							No result <span>(<?php printf('%0.2fs', $ossPaging->getResultTime()); ?>)
							</span>
						</div>
						<?php endif; ?>
						<?php endif; ?>
					</div>
				</div>
			</div>
		</div>
		<div id="info">
			Powered by <a href="http://www.open-search-server.com/">OpenSearchServer</a>.
			Copyright Jaeksoft.
		</div>
	</div>
</body>
</html>

