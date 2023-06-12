package org.deegree.client.sos.storage.components;

import java.util.List;

import org.deegree.commons.utils.Pair;

/**
 * Helper class for StorageGetObservation class containing the contents of the
 * XML element "Observation".
 * 
 * @author <a href="mailto:neumeister@lat-lon.de">Ulrich Neumeister</a>
 * 
 */
public class Observation {

	private DataArray dataArray;

	private String featureOfInterest;

	private List<String> observedProperty;

	private String procedure;

	private Pair<String, String> samplingTime;

	public Observation() {

	}

	public Pair<String, String> getSamplingTime() {
		return samplingTime;
	}

	public String getProcedure() {
		return procedure;
	}

	public List<String> getObservedProperty() {
		return observedProperty;
	}

	public String getFeatureOfInterest() {
		return featureOfInterest;
	}

	public DataArray getDataArray() {
		return dataArray;
	}

	public void setSamplingTime(Pair<String, String> that) {
		samplingTime = that;
	}

	public void setProcedure(String that) {
		procedure = that;
	}

	public void setObservedProperty(List<String> that) {
		observedProperty = that;
	}

	public void setFeatureOfInterest(String that) {
		featureOfInterest = that;
	}

	public void setDataArray(DataArray that) {
		dataArray = that;
	}

}
