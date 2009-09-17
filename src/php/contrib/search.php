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

/**
 * Simple search engine in PHP
 * @author pmercier <pmercier-oss@nkubz.net>
 * @package OpenSearchServer
 * @todo Some documentation ?
 * FIXME Ask the engine to return specific fields by programmation
 */
header('Content-type: text/html; charset=UTF-8');

define('BASE_DIR', dirname(__FILE__));
require BASE_DIR.'/../lib/misc.lib.php';
require BASE_DIR.'/../lib/OSS_API.class.php';
require BASE_DIR.'/../lib/OSS_Search.class.php';

$ossEnginePath  = configRequestValue('ossEnginePath', 'http://localhost:8080', 'engineURL');
$ossEngineConnectTimeOut = configRequestValue('ossEngineConnectTimeOut', 5, 'engineConnectTimeOut');
$ossEngineIndex = configRequestValue('ossEngineIndex_contrib_search', 'myCrawler', 'engineIndex');

if (isset($_REQUEST['query'])) {

	$search = new OSS_Search($ossEnginePath, $ossEngineIndex);

	if (!empty($_REQUEST['lang'])) {
		$search->lang($_REQUEST['lang'])
			   ->filter('lang:'.$_REQUEST['lang']);
	}

	$result = $search->query($_REQUEST['query'])
					 ->facet('lang', 0)
					 ->execute($ossEngineConnectTimeOut);

	if ($result instanceof SimpleXMLElement) {
		$resultCount   = $result->result['numFound'];
		$resultTime    = $result->result['numFound'] / 1000;
		$resultEntries = $result->xpath('result/doc');

		$langFacets = $result->xpath('faceting/field[@name="lang"]/facet');
		if ($langFacets === false) $langFacets = array();
	}
}
?>
<html>
	<head>
		<title>Open Search Server - contrib/search</title>
		<style type="text/css">
			html, body { height:100%; margin:0; }
			body { font-size: 12px; font-family: arial,sans-serif; }
			a { color:#2200CC; font-size: 12px; }

			#bodywrap {
				width:810px;
				height:100%;
				margin:0 auto;
				margin-bottom: 15px;
			}
			.lateralBorders { border: 1px solid #FF0000; border-width: 0 1px 1px 1px;}
			#submit { width: 10%;  }
			.errors { color: red; }
			.errors span { display: block; }


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
			li { margin-bottom: 15px; }
			li h2 {
				font-size: 12px;
				font-weight: normal;
				margin-bottom: 2px;
			}
			li cite, li code {
				color: green;
				font-family: arial,sans-serif;
			}
			li code { font-weight: bold; }

			#options {
				border: 1px solid #95D2E7;
				margin: -16px 0 15px;
				-moz-border-radius: 0 0 5px 5px;
				-webkit-border-radius:5px;
				-webkit-border-top-left-radius:0;
				-webkit-border-top-right-radius:0;
				border-radius: 0 0 5px 5px;
				border-top-left-radius:0;
				border-top-right-radius:0;
			}
			#options form  { display: none; margin: 0; padding: 0; border: 0; }
			#options:hover form { display: inherit; }
			#options_title {
				color: white;
				background: #95D2E7;
				padding: 3px;
			}
			#option_fieldset {
				border: 0px;
				margin: 0px;
			}
			#option_fieldset input {
				display: block;
				width: 320px;
				border: 1px solid #E8E8E8;
				border-top-width: 0;
			}
			#option_fieldset label {
				display: block;
				width: 180px;
				float: left;
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
							<label>ConnectTimeOut (s)</label><input name="engineConnectTimeOut" value="<?php echo $ossEngineConnectTimeOut; ?>" />
							<label>&nbsp;</label><input type="submit" value="save">
						</fieldset>
					</form>
					<div id="options_title">Options</div>
				</div>
				<form action="<?php echo basename(__FILE__); ?>" method="GET">


					<fieldset id="query_fieldset">
						<input id="query" name="query" value="<?php if (isset($_REQUEST['query'])) echo $_REQUEST['query']; ?>" />
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
						<input type="radio" name="lang" value="<?php echo $lang; ?>" id="<?php echo $lang; ?>" <?php if($_REQUEST['lang'] == $lang) echo 'checked="checked"'; ?>>
						<label for="<?php echo $lang; ?>"><?php echo ucfirst($lang); ?></label>
						<?php endforeach; ?>
					</div>
					<?php endif; endif; ?>
					<?php if (isset($search)): ?>
					<p>You queried the search engine with the following call:<br/>
					<a style="padding-left: 15px;" class="lastQueryQtring" href="<?php echo $search->getLastQueryString(); ?>"><?php echo htmlentities(urldecode($search->getLastQueryString())); ?></a>
					</p>
					<?php endif; ?>
				</form>
				<?php
				if (isset($resultCount)):
				if ($resultCount): ?>
				<div id="result">
					<span>Found <?php if ($resultCount == 1): ?>1 result<?php else: echo $resultCount; ?> results<?php endif; ?></span>
					<span>(<?php printf('%0.2fs', $resultTime); ?>)</span>
					<ul><?php
						foreach ($resultEntries as $entry):

							$url	 = implode('', $entry->xpath('*[@name="url"]'));
							$host	 = implode('', $entry->xpath('*[@name="host"]'));
							$type	 = implode('', $entry->xpath('field[@name="contentBaseType"]'));
							$content = implode('', $entry->xpath('*[@name="content"]'));

							$subType = preg_replace('/^[^\/]+\//', '', $type);

							if ($type == 'text/html' && !empty($content)): ?>
								<li>
									<h2><a href="<?php echo $url; ?>" target="_new"><?php echo implode('', $entry->xpath('*[@name="title"]')); ?></a></h2>
									<div><?php echo $content; ?></div>
									<cite><?php echo $host; ?></cite>
								</li>
								<?php
							elseif ($type == 'text/html'):
								?>
								<li>
									<h2><a href="<?php echo $url; ?>" target="_new"><?php echo $url; ?></a></h2>
									<cite><?php echo $host; ?></cite>
								</li>
								<?php
							else: ?>
								<li>
									<h2><code><?php echo "[".$subType."] "; ?></code> <a href="<?php echo $url; ?>" target="_new"><?php echo $url; ?></a></h2>
									<div><?php echo $content; ?></div>
									<cite><?php echo $host; ?></cite>
								</li>
								<?php
							endif;
							?>
					<?php endforeach; ?>
					</ul>
				</div>
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
				Powered by <a href="http://www.open-search-server.com/">Open Search Server</a>. Copyright Jaeksoft.
			</div>
		</div>
	</body>
</html>