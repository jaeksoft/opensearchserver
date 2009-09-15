<?php

define('BASE_DIR', dirname(__FILE__));
require (BASE_DIR.'/../lib/OSS_API.class.php');
require (BASE_DIR.'/../lib/OSS_Search.class.php');

$ossEnginePath  = 'http://localhost:8080';
$ossEngineIndex = 'myCrawler';

if (isset($_REQUEST['query'])) {
	$search = new OSS_Search($ossEnginePath, $ossEngineIndex);
	$result = $search->query($_REQUEST['query'])->execute(1);
	if ($result instanceof SimpleXMLElement) {
		$resultCount   = $result->result['numFound'];
		$resultEntries = $result->xpath('result/doc');
	}
}
?>
<html>
	<head>
		<style type="text/css">
		body { font-size: 12pt; }
		label[for="feed"] { width: 10%; display: block; float: left; }
		#submit { width: 10%; font-size: 1em; }
		.errors { color: red; }
		.errors span { display: block; }
		li h1	{ font-size: 1em; }
		</style>
	</head>
	<body>
		<form action="<?php echo basename(__FILE__); ?>" method="GET">
			<fieldset>
				<label for="query">Search</label>
				<input id="query" name="query" value="<?php if (isset($_REQUEST['query'])) echo $_REQUEST['query']; ?>" />
				<input id="submit" type="submit" />
				<div class="errors">
				</div>
				<a class="lastQueryQtring" href="<?php echo $search->getLastQueryString(); ?>"><?php echo $search->getLastQueryString(); ?></a>
			</fieldset>
		</form>
		<?php if (isset($resultCount)): ?>
		<?php if ($resultCount): ?>
		<div id="result">
			Found
			<?php if ($resultCount == 1): ?>
			1 result
			<?php else: echo $resultCount; ?>
			results
			<?php endif; ?>
			<ul>
			<?php foreach ($resultEntries as $entry): ?>
				<li>
					<h1><a href="<?php echo implode('', $entry->xpath('*[@name="url"]')); ?>" target="_new"><?php echo implode('', $entry->xpath('*[@name="title"]')); ?></a></h1>
					<div><?php echo implode('', $entry->xpath('*[@name="content"]')); ?></div>
				</li>
			<?php endforeach; ?>
			</ul>
		</div>
		<?php else: ?>
		<div id="result">
			No result
		</div>
		<?php endif; ?>
		<?php endif; ?>
	</body>
</html>