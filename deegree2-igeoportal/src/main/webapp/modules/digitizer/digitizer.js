function Digitizer() {
	
	// variable declaration
	this.targetDocument;
	this.targetElement;
	
	// method declaration
	this.paint = paint;
	this.repaint = repaint;
	
	/**
	 * 
	 * @param targetDocument
	 * @param targetElement
	 */
	function paint(targetDocument, targetElement) {
		this.targetDocument = targetDocument;
		this.targetElement = targetElement;
	}

	/**
	 * 
	 */
	function repaint() {
	}
	
}