package org.deegree.client.sos.storage.components;

import java.util.List;

import org.apache.axiom.om.OMElement;

/**
 * Helper class for the StorageGetCapabilities class containing the contents of
 * the XML element "Filter_Capabilities".
 * 
 * @author <a href="mailto:neumeister@lat-lon.de">Ulrich Neumeister</a>
 * 
 */
public class Filter_Capabilities {

	private OMElement arithmeticOperators;

	private List<String> comparisonOperators;

	private List<String> geometryOperands;

	private OMElement id_Capabilities;

	private OMElement logicalOperators;

	private List<Operator> spatialOperators;

	private List<String> temporalOperands;

	private List<Operator> temporalOperators;

	public Filter_Capabilities() {

	}

	public List<String> getGeometryOperands() {
		return geometryOperands;
	}

	public List<Operator> getSpatialOperators() {
		return spatialOperators;
	}

	public List<String> getTemporalOperands() {
		return temporalOperands;
	}

	public List<Operator> getTemporalOperators() {
		return temporalOperators;
	}

	public List<String> getComparisonOperators() {
		return comparisonOperators;
	}

	public OMElement getArithmeticOperators() {
		return arithmeticOperators;
	}

	public OMElement getId_Capabilities() {
		return id_Capabilities;
	}

	public OMElement getLogicalOperators() {
		return logicalOperators;
	}

	public void setGeometryOperands(List<String> that) {
		geometryOperands = that;
	}

	public void setSpatialOperators(List<Operator> that) {
		spatialOperators = that;
	}

	public void setTemporalOperands(List<String> that) {
		temporalOperands = that;
	}

	public void setTemporalOperators(List<Operator> that) {
		temporalOperators = that;
	}

	public void setComparisonOperators(List<String> that) {
		comparisonOperators = that;
	}

	public void setArithmeticOperators(OMElement that) {
		arithmeticOperators = that;
	}

	public void setId_Capabilities(OMElement that) {
		id_Capabilities = that;
	}

	public void setLogicalOperators(OMElement that) {
		logicalOperators = that;
	}

}
