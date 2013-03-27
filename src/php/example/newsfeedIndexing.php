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
 * Indexing a news feed (rss/atom)
 * @author pmercier <pmercier-oss@nkubz.net>
 * @package OpenSearchServer
 * @todo Some documentation ?
 */
header('Content-type: text/html; charset=UTF-8');

// The required classes
define('BASE_DIR', dirname(__FILE__));
require BASE_DIR.'/../lib/oss_misc.lib.php';
require BASE_DIR.'/../lib/oss_api.class.php';
require BASE_DIR.'/../lib/oss_indexdocument.class.php';

$ossEnginePath  = config_request_value('ossEnginePath', 'http://localhost:8080/', 'engineURL');
$ossEngineConnectTimeOut = config_request_value('ossEngineConnectTimeOut', 5, 'engineConnectTimeOut');
$ossEngineIndex = config_request_value('ossEngineIndex_contrib_feedIndexing', 'newsfeeds', 'engineIndex');

$sampleFeed = array(
	'BlogEEE [fr, RSS2.0]' => array('http://feeds.feedburner.com/blogeee/articles', 'fr'),
	'Embedded projects from around the web [en, Atom0.3]' => array('http://www.embedds.com/feed/atom/', 'en'),
	'Everything USB [en, RSS2.0]' => array('http://feeds.feedburner.com/everythingusb', 'en'),
	'Instructables [en, RSS2.0]' => array('http://www.instructables.com/tag/type:id/featured:true/rss.xml', 'en'),
	'MAKE Magazine [en, RSS2.0]' => array('http://blog.makezine.com/index.xml', 'en')
);

$errors = array();

if (isset($_REQUEST['feed'])) {

	$curlInfos = array();
	$xml = retrieve_xml($_REQUEST['feed'], $curlInfos);

	if (!$xml instanceof SimpleXMLElement) {
		$errors[] = 'feedNotFoundOrInvalid';
	}
	else {
		$newsFeedParser = NewsFeedParser::factory($xml);

		// Create the index document with the helper
		$index = new OssIndexDocument();
		// Simple isn't it ?
		foreach ($newsFeedParser as $newsEntry) {
			$document = $index->newDocument($_REQUEST['lang']);
			$document->newField('channel_home', $newsFeedParser->getChannelHome());
			$document->newField('channel_title', $newsFeedParser->getChannelTitle());
			$document->newField('channel_subtitle', $newsFeedParser->getChannelSubtitle());

			$document->newField('id',		 $newsEntry->getId());
			$document->newField('author',	 $newsEntry->getAuthor());
			$document->newField('link',		 $newsEntry->getLink());
			$document->newField('published', $newsEntry->getPublished());
			$document->newField('content',	 strip_tags($newsEntry->getContent()));
			$document->newField('summary',	 strip_tags($newsEntry->getSummary()));
			$document->newField('title',	 strip_tags($newsEntry->getTitle()));

		}

		// Send the IndexDocument to the search server
		$server = new OssAPi($ossEnginePath, $ossEngineIndex);
		if ($server->update($index) === false) {
			$errors[] = 'failedToUpdate';
		}
		else {
			$feedIsIndexed = true;
		}

	}

}

?>
<html>
	<head>
		<title>Open Search Server - contrib/newsFeedIndexation</title>
		<link type="text/css" rel="stylesheet" href="common.css" />
		<script type="text/javascript" src="common.js"></script>
		<style type="text/css">
		form fieldset {
			border: 1px solid #B8B8B8;
		}
		form label {
			font-size: 12px;
		}
		form label {
			display:   block;
			float:		left;
			width:	   100px;
			padding:	 4px;
			margin: 	   0;
		}
		form #feed {
			width: 	   430px;
		}
		form #lang {
			width: 	   100px;
		}
		form #submit {
			width: 	   100px;
		}

		h3 {
			margin-bottom: 8px;
		}
		#sampleFeeds {
			display:	block;
			overflow:	auto;
			height:		100px;
			margin:		 	0;
			padding:	    0;
		}
		#sampleFeeds li {
			padding-left: 16px;
		}

		#resultFeed {
			padding: 0 10px 0 10px;
		}
		#resultFeed label {
			display: block;
			float: left;
			width: 150px;
			font-weight: bold;
			margin-bottom: 5px;
		}
		#resultFeed span {
			display: block;
			float: left;
			width: 600px;
			margin-bottom: 5px;
		}

		#indexingXML {
			width: 770px;
			padding: 0;
			overflow: auto;
			height: 400px;
			border: 1px solid #B8B8B8;
			font-family: Courier New;
		}

		#indexingXML .node {
			margin-left: 10px;
		}

		.successIndexedFeed {
			color: green;
			margin: 10px;
		}

		.delimiter { color: blue; }
		.cdata { color: purple; }
		.nodeName { color: #800000; font-style: italic; font-weight: bold;}

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
					<div id="options_title" onclick="javascript:toggleClass(this.parentNode, 'show'); return false;">Options</div>
				</div>
				<form action="<?php echo basename(__FILE__); ?>" method="GET">
					<fieldset class="partlyRounded">
						<legend>RSS feed</legend>
						<label for="feed">URL</label>
						<input id="feed" name="feed" value="<?php if(isset($_REQUEST['feed'])) echo $_REQUEST['feed']; ?>" />
						<select id="lang" name="lang">
							<?php foreach (OssApi::supportedLanguages() as $langISO => $langLabel): ?>
							<option value="<?php echo $langISO; ?>"<?php if (isset($_REQUEST['lang']) && (string)$_REQUEST['lang'] == $langISO): ?> selected="true"<?php endif; ?>><?php echo $langLabel; ?></option>
							<?php endforeach; ?>
						</select>
						<input id="submit" type="submit" />
						<div class="errors">
							<?php if (in_array('feedNotFoundOrInvalid', $errors)): ?>
							<span>The feed was not found or is an invalid XML</span>
							<?php endif; ?>
							<?php if (in_array('failedToUpdate', $errors)): ?>
							<span>The feed couldn't be sent to the search engine</span>
							<?php endif; ?>
						</div>
						<?php if (count((array)$sampleFeed)): ?>
						<h3>Sample Feeds</h3>
						<ul id="sampleFeeds">
							<?php foreach ($sampleFeed as $feedName => $feedInfo):?>
							<li><a href="./<?php echo basename(__FILE__), '?feed=', urlencode($feedInfo[0]), '&lang=', $feedInfo[1]; ?>" alt="Feed for <?php echo $feedName; ?>"><?php echo $feedName; ?></a></li>
							<?php endforeach; ?>
						</ul>
						<?php endif; ?>
					</fieldset>
				</form>
				<?php if (isset($newsFeedParser)): ?>
				<?php if (isset($feedIsIndexed)): ?><span class='successIndexedFeed clearFix'>The feed have been indexed by the Search Engine</span><?php endif; ?>
				<div id="resultFeed" class='clearfix'>
					<label>Feed format</label><span><?php echo $newsFeedParser->getFeedFormat(); ?>&nbsp;</span><br/>
					<label>Channel Home</label><span><?php echo $newsFeedParser->getChannelHome(); ?>&nbsp;</span><br/>
					<label>Channel Title</label><span><?php echo $newsFeedParser->getChannelTitle(); ?>&nbsp;</span><br/>
					<label>Channel SubTitle</label><span><?php echo $newsFeedParser->getChannelSubtitle(); ?>&nbsp;</span>
					<label>Number entries</label><span><?php echo $newsFeedParser->count(); ?>&nbsp;</span>
				</div>
				<?php endif; ?>
				<?php if (isset($index)): ?>
				<div id="indexingXML" class="partlyRounded"><?php echo beautifulXML($index->__toString()); ?></div>
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
<?php echo $index->__toString(); ?>

