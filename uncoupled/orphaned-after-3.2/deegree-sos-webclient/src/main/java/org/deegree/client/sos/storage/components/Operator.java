package org.deegree.client.sos.storage.components;

import java.util.List;

/**
 * Helper class for StorageGetCapabilties class containing the contents of any
 * "Operator" XML element (e.g. "TemporalOperator" or "ComparisonOperator") from
 * the Filter_Capabilities of a GetCapabilities response.
 * 
 * @author <a href="mailto:neumeister@lat-lon.de">Ulrich Neumeister</a>
 * 
 */
public class Operator {

	private String name;

	private List<String> operands;

	public Operator() {

	}

	public List<String> getOperands() {
		return operands;
	}

	public String getName() {
		return name;
	}

	public void setName(String that) {
		name = that;
	}

	public void setOperands(List<String> that) {
		operands = that;
	}

}
