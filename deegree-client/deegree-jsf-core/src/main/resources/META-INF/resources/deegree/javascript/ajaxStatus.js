function processAjaxUpdate(msgId, modal) {
	function processEvent(data) {
		var msg = document.getElementById(msgId);
		if (msg != null) {
			if (data.status == "begin") {
				if (modal) {
					document.body.style.cursor = 'wait'
					showPleaseWait();
				}
				msg.style.display = '';
			} else if (data.status == "success") {
				if (modal) {
					document.body.style.cursor = 'auto'
					hidePleaseWait();
				}
				msg.style.display = 'none';
			}
		}
	}
	return processEvent;
};

function registerAjaxStatus(msgId, modal) {
	jsf.ajax.addOnEvent(processAjaxUpdate(msgId, modal));
}

function showPleaseWait() {
	document.getElementById("PLEASEWAIT").style.display = "inline";
	document.getElementById("PLEASEWAIT_BG").style.display = "inline";
}

function hidePleaseWait() {
	document.getElementById("PLEASEWAIT").style.display = "none";
	document.getElementById("PLEASEWAIT_BG").style.display = "none";
}
