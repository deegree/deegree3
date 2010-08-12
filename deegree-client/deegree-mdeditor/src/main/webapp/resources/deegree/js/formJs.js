

function fgListConfirmDelete(msg){
	return window.confirm (msg);
}

function confirmDelete(msg){
	return window.confirm (msg);
}

function mdunload(){
	var form = document.getElementById("mdForm:emptyForm");
	 alert(form);
	if( form != null){
		form.submit();
	}
	return true;
}

function mdload(){
	window.onbeforeunload = mdunload;
}