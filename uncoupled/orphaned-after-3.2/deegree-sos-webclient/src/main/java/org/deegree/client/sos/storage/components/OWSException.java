package org.deegree.client.sos.storage.components;

/**
 * Helper class for StorageDescribeSensor and StorageGetObservation classes
 * containing the contents of the XML element "ows:Exception". This will be sent
 * by the SOS if any parameter was wrong in the request or other errors occured.
 * 
 * @author <a href="mailto:neumeister@lat-lon.de">Ulrich Neumeister</a>
 * 
 */
public class OWSException {

	private String exceptionCode;

	private String locator;

	private String exceptionText;

	public String getExceptionCode() {
		return exceptionCode;
	}

	public String getLocator() {
		return locator;
	}

	public String getExceptionText() {
		return exceptionText;
	}

	public void setExceptionCode(String that) {
		exceptionCode = that;
	}

	public void setLocator(String that) {
		locator = that;
	}

	public void setExceptionText(String that) {
		exceptionText = that;
	}

}
