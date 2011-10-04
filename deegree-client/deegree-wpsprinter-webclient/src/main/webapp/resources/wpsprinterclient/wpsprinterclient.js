function updateMapDimension() {
	var width;
	var height;
	var inputs = document.getElementsByTagName("span");
	if (inputs != null) {
		for ( var int = 0; int < inputs.length; int++) {
			var input = inputs[int];
			if (endsWith(input.id, "map_width")) {
				width = input.firstChild.nodeValue;
			} else if (endsWith(input.id, "map_height")) {
				height = input.firstChild.nodeValue;
			}
		}
	}
	if (width != null && height != null) {
		alert(width + " - " + height);
	}
}

function setMapAndTableData() {
	// _mapInput
	// _dataInput
	var inputs = document.getElementsByTagName("input");
	if (inputs != null) {
		for ( var int = 0; int < inputs.length; int++) {
			if (endsWith(inputs[int].id, "_mapInput")) {
				alert("set value to the the map input");
			} else if (endsWith(inputs[int].id, "_dataInput")) {
				alert("set value to the the data input");
			}
		}
	}
}

function endsWith(string, s) {
	return string.length >= s.length
			&& string.substr(string.length - s.length) == s;
}
