function toggleClass(obj, class) {
    if (!obj || !class) return;
    var styles = obj.className.split(' '),
    	hasStyle = false;
    for (var i = 0, iMax = styles.length; i < iMax; i++) {
	if (styles[i] == class) { styles[i] = ''; hasStyle= true; }
    }
    var style = styles.join(' ');
    if (!hasStyle) style += ' ' + class;
    obj.className = style.replace(/ +/, ' ').replace(/$ +| +^/, '');
}