<?php if (isset($resultTotalPages) && $resultTotalPages >= 1): ?>
<style type="text/css">
	@import url(pagination.css);
</style>


<div id=pagination">
	<table width="100%">
		<tr>
			<td width="45%"></td>
			<?php if ($resultLowPage > 0):?>
				<td><a href="<?php echo $pageBaseURI, 0; ?>">First&lt;&lt;</a></td>
			<?php endif;?>
			
			<?php if ($resultLowPrev < $resultLowPage):?>
				<td><a href="<?php echo $pageBaseURI, $resultLowPrev; ?>">Prev&lt;&lt;</a></td>
			<?php endif;?>
			
			<?php for ($i = $resultLowPage; $i < $resultHighPage; $i++): ?>
				<td><a  href="<?php echo $pageBaseURI, $i; ?>"
				<?php if ($i == $resultCurrentPage): ?> class="currentPage"
				
				<?php endif; ?>><?php echo $i + 1; ?></a></td>
			<?php endfor;?>
				<?php if ($resultHighNext > $resultHighPage):?>
				<td><a href="<?php echo $pageBaseURI, $resultHighNext; ?>">&gt;&gt;Next</a></td>
				<?php endif;?>
				<?php if ($resultHighPage < $resultTotalPages):?>
				<td><a href="<?php echo $pageBaseURI, $resultTotalPages; ?>">&gt;&gt;Last</a></td>
			<?php endif;?>
			<td width="45%"></td>
		</tr>
	</table>
	<span>Found <?php if ($resultFound == 1): ?>1 result<?php else: echo $resultFound; ?>&nbsp;
results<?php endif; ?></span> <span>(<?php printf('%0.2fs', $resultTime); ?>)</span>
</div>
<?php else: ?>
	<?php if (isset($resultTime)):?>
	<div id="result">No result <span>(<?php printf('%0.2fs', $resultTime); ?>)</span></div>
	<?php endif; ?>
<?php endif; ?>