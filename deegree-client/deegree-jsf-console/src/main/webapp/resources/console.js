function show() {
	document.getElementById("PLEASEWAIT").style.display = 'inline';
	document.getElementById("PLEASEWAIT_BG").style.display = 'inline';
}

function blockInput(event) {
	if (event.status == "begin") {
		document.getElementById("PLEASEWAIT").style.display = 'inline';
		document.getElementById("PLEASEWAIT_BG").style.display = 'inline';
	} else if (event.status == "complete") {
		document.getElementById("PLEASEWAIT").style.display = 'none';
		document.getElementById("PLEASEWAIT_BG").style.display = 'none';
	}
}

function confirmDelete() {
	return confirm("Do you really want do delete?");
}

function maximize() {
	var mainDiv = document.getElementById('main');
	mainDiv.className = 'main_maximized';
}