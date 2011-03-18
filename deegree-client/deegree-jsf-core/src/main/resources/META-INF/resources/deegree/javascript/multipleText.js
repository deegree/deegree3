function remove(source) {
	var divToDelete = source.parentNode.parentNode;
	divToDelete.parentNode.removeChild(divToDelete);
	return false;
}

function add(source, templateId) {
	var template = document.getElementById(templateId);
	var newNode = template.cloneNode(true);
	newNode.style.display = "inline";
	newNode.id = "";
	newNode.name = "";

	var newDate = new Date;
	var newID = newDate.getTime();

	var name = newNode.firstChild.name;
	// TODO: /template$/
	var newName = name.replace("template", "child_" + newID)
	newNode.firstChild.id = newName;
	newNode.firstChild.name = newName;
	template.parentNode.insertBefore(newNode, template);
	return false;
}