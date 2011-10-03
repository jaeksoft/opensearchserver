<script type="text/javascript">
<!--
function checkConfig() {
	if (editconnection.serverlocation) {
		if (editconnection.serverlocation.value == "") {
			alert("Please supply a valid OpenSearchServer server location");
			editconnection.serverlocation.focus();
			return false;
		}
	}
	if (editconnection.indexname) {
		if (editconnection.indexname.value == "") {
			alert("Please supply a valid index name");
			editconnection.indexname.focus();
			return false;
		}
	}
	return true;
}

function checkConfigForSave() {
	return checkConfig();
}
-->
</script>