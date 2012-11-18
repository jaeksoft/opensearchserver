function setVisibility(display) {
	var visibility = function(id) {
		var element = document.getElementById(id);
		if (!element) return;
		element.style.display = display ? '' : 'none';
	}
	var idList = arguments;
	idList.shift();
	for (var i = 0; i < idList.lenght; i++)
		visibility(idList[i]);
}