package org.deegree.client.sos.storage.components;

import java.util.List;

import org.deegree.commons.utils.Pair;

/**
 * Helper class for StorageGetCapabilties class containing the contents of the
 * XML element "Operation" from the "OperationsMetadata" element.
 * 
 * @author <a href="mailto:neumeister@lat-lon.de">Ulrich Neumeister</a>
 * 
 */
public class Operation {

	private String name;

	private List<Pair<String, String>> http;

	private List<Parameter> parameters;

	public Operation() {

	}

	public String getName() {
		return name;
	}

	public List<Pair<String, String>> getHttp() {
		return http;
	}

	public List<Parameter> getParameters() {
		return parameters;
	}

	public void setName(String that) {
		name = that;
	}

	public void setHttp(List<Pair<String, String>> that) {
		http = that;
	}

	public void setParameters(List<Parameter> that) {
		parameters = that;
	}

}
