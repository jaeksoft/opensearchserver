<?php if (isset($resultTotalPages) && $resultTotalPages > 1): ?>
<style type="text/css">
	@import url(pagination.css);
</style>


<div id=pagination">
	<ul>
	<?php if ($resultLowPage > 0):?>
		<li><a href="<?php echo $pageBaseURI, 0; ?>">First&lt;&lt;</a></li>
	<?php endif;?>
	
	<?php if ($resultLowPrev < $resultLowPage):?>
		<li><a href="<?php echo $pageBaseURI, $resultLowPrev; ?>">Prev&lt;&lt;</a></li>
	<?php endif;?>
	
	<?php for ($i = $resultLowPage; $i <= $resultHighPage; $i++): ?>
		<li><a  href="<?php echo $pageBaseURI, $i; ?>"
		<?php if ($i == $resultCurrentPage): ?> class="currentPage"
		
		<?php endif; ?>><?php echo $i + 1; ?></a></li>
	<?php endfor;?>
		<?php if ($resultHighNext > $resultHighPage):?>
		<li><a href="<?php echo $pageBaseURI, $resultHighNext; ?>">&gt;&gt;Next</a></li>
		<?php endif;?>
		<?php if ($resultHighPage < $resultTotalPages):?>
		<li><a href="<?php echo $pageBaseURI, $resultTotalPages; ?>">&gt;&gt;Last</a></li>
		<?php endif;?>
	</ul>
</div>

<div id="result">No result <span>(<?php printf('%0.2fs', $resultTime); ?>)</span>
</div>
	<?php endif; ?>