function collapse(button, id, openImg, closeImg) {
	var element = document.getElementById(id);
	if (element) {
		var current = element.style.display;
		if (current == "none") {
			element.style.display = "inline";
			button.src = closeImg;
		} else {
			element.style.display = "none";
			button.src = openImg;
		}
	}
}