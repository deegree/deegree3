function confirmDelete() {
	return confirm("Do you really want do delete?");
}

function maximize() {
	var mainDiv = document.getElementById('main');
	mainDiv.className = 'main_maximized';
}