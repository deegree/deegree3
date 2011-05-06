function processAjaxUpdate(msgId, modal, forId) {
	function processEvent(data) {
		dumpln("pe: " + msgId + ", " + modal + ", " + forId + ", " + data.source.id);
		if (forId == null || data.source.id == forId) {
			var msg = document.getElementById(msgId);
			if (msg != null) {
				if (data.status == "begin") {
					if (modal) {
						document.body.style.cursor = 'wait'
					}
					msg.style.display = 'inline';
				} else if (data.status == "success") {
					if (modal) {
						document.body.style.cursor = 'auto'
					}
					msg.style.display = 'none';
				}
			}
		}
	}
	return processEvent;
};

function registerAjaxStatus(msgId, modal, forId) {
	dumpln("register: " + msgId + ", " + modal + ", " + forId);
	jsf.ajax.addOnEvent(processAjaxUpdate(msgId, modal, forId));
}