function toggle(button, index) {
	var node = button.parentNode.parentNode.nextSibling;
	if (node != null) {
		var tmpSrc = button.src;
		if (node.style.display == "inline") {
			button.src = button.src.replace("page_min.gif", "page_max.gif");
			node.style.display = "none";
		} else {
			button.src = button.src.replace(/page_max.gif/, "page_min.gif");
			node.style.display = "inline";
		}
		// TODO: notify server!
	}
}

function confirmDelete(msg) {
	return window.confirm(msg);
}