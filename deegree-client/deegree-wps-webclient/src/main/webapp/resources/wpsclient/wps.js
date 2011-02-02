function loadReference(textFieldId, cbId) {
	var cb = document.getElementsByName(cbId);
	if (cb != null) {
		for ( var int = 0; int < cb.length; int++) {
			if (cb[int].checked) {
				// DO something!
				var textF = document.getElementById("emptyForm:" + textFieldId);
				textF.value = "Reference from " + cb[int].value;
			}
		}
	}
}

function handleXMLOutput(requestRef) {
	alert("nothing to do");
}

function handleBinaryOutput(requestRef) {
	alert("nothing to do");
}

function handleLiteralOutput(value, uom, dataType) {
	alert("nothing to do");
}

function handleBBoxOutput(lower1, lower2, upper1, upper2, crs) {
	alert("nothing to do");
}