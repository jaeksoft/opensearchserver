<?php
// no direct access
defined('_JEXEC') or die('Restricted access');

if (isset($resultFound)):
if ($resultFound): ?>
<div class="result">
<ul>
<?php
foreach ($resultEntries as $entry):

$uri	   = array_first($entry->xpath('*[@name="url"]'));
//$directory = array_first($entry->xpath('*[@name="directory"]'));
//$file	   = str_replace($directory, '', $uri);
//$date	   = array_first($entry->xpath('*[@name="fileSystemDate"]'));
//$dateTS    = strtotime(preg_replace('/^(\d{4})(\d{2})(\d{2})(\d{2})(\d{2})(\d{2})\d+$/', '$1-$2-$3 $4:$5:$6', $date));
//$crawlDate = array_first($entry->xpath('*[@name="crawlDate"]'));
//$extension = array_last(explode('.', $file));
$content   = implode('', $entry->xpath('snippet[@name="content"]'));
if (empty($content)) {
	$content   = implode('', $entry->xpath('field[@name="content"]'));
}
?>
	<li>
	<h2><a href="<?php echo $uri; ?>" target="_new"><code><?php echo "[".$uri."] "; ?></code></a></h2>
	<?php echo $content; ?></li>
	<?php endforeach;
	$content = null; ?>
</ul>
</div>
<?php endif; ?>
<?php endif; ?>
