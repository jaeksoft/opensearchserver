<?php
// no direct access
defined('_JEXEC') or die('Restricted access');
?>

<div class="search" ><?php echo $params->get('welcomeMessage') ?></div>

<form method="GET">
<fieldset id="query_fieldset"><input id="query" name="query"
	value="<?php if (isset($_REQUEST['query'])) echo htmlspecialchars($_REQUEST['query']); ?>" />
<input id="submit" type="submit" value="<?php echo $params->get('buttonName') ?>" /></fieldset>
<?php
if (isset($langFacets)):
if (count($langFacets)):
?>
<div id="langs">Languages: <input type="radio" name="lang" value=""
	id="lang_all"
	<?php if(!isset($_REQUEST['lang']) || $_REQUEST['lang'] == '') echo 'checked="checked"'; ?>>
<label for="lang_all">All</label> <?php
foreach ($langFacets as $langFacet):
$lang = $langFacet['name'];
?> <input type="radio" name="lang" value="<?php echo $lang; ?>"
	id="<?php echo $lang; ?>"
	<?php if(isset($_REQUEST['lang'])) if($_REQUEST['lang'] == $lang) echo 'checked="checked"'; ?>>
<label for="<?php echo $lang; ?>"><?php echo ucfirst($lang); ?></label>
	<?php endforeach; ?></div>
	<?php endif; endif; ?>
</form>
