function loadReference(textFieldId, cbId, formatID) {
	var cb = document.getElementsByName(cbId);
	if (cb != null) {
		for ( var int = 0; int < cb.length; int++) {
			if (cb[int].checked) {
				// DO something!
				var textF = document.getElementById("emptyForm:" + textFieldId);
				textF.value = "Reference from " + cb[int].value;
				if (cb[int].value == "WFS") {
					var selectF = document.getElementById("emptyForm:"
							+ formatID);
					var optionsArray = selectF.options;
					for ( var i = 0; i < optionsArray.length; i++) {
						var pos = optionsArray[i].value.indexOf(";");
						var schema = optionsArray[i].value.substring(0, pos);
					}
				}
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