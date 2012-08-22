function DataTable() {
	
	// variable declaration
	this.targetDocument;
	this.targetElement;
	
	// method declaration
	this.paint = paint;
	this.repaint = repaint;
	this.addData = addData;
	this.resetTabs = resetTabs;
	
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
	
	/**
	 * 
	 * @param data data object with signature:
	 * {
	 *  String name;
	 *  String[] columns;
	 *  String[][] data;
	 * }
	 */
	function addData(title, columns, data) {
		controller.getFrame( 'IDDataTable' ).addData(title, columns, data);
	}
	
	/**
	 * removes all tables from the according tab panel
	 * 
	 */
	function resetTabs() {
		controller.getFrame( 'IDDataTable' ).reset();
	}
	
}