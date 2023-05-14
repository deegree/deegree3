package org.deegree.client.sos.storage.components;

import java.util.List;

/**
 * Helper class for Operation class (which is a helper class for
 * StorageGetCapabilities) containing the contents of the XML element
 * "Parameter".
 * 
 * @author <a href="mailto:neumeister@lat-lon.de">Ulrich Neumeister</a>
 * 
 */
public class Parameter {

	private List<String> allowedValues;

	private String name;

	public Parameter() {

	}

	public List<String> getAllowedValues() {
		return allowedValues;
	}

	public String getName() {
		return name;
	}

	public void setAllowedValues(List<String> that) {
		allowedValues = that;
	}

	public void setName(String that) {
		name = that;
	}

}
