package org.deegree.client.sos.requesthandler.kvp;

import java.util.List;

import org.deegree.commons.utils.Pair;

/**
 * Helper class for the KVPGetObservation class. Contains the offering-specific
 * KVPs for a GetObservation request. These will be displayed in a Form later.
 * 
 * @author <a href="mailto:neumeister@lat-lon.de">Ulrich Neumeister</a>
 * 
 */
public class KVPfromOffering {

	private Pair<String, String> eventTime;

	private List<String> featureOfInterest;

	private String id;

	private String name;

	private List<String> observedProperties;

	private List<Pair<String, String>> procedures;

	private List<String> responseFormat;

	private List<String> responseMode;

	private List<String> result;

	private List<String> resultModel;

	private String srsName;

	public KVPfromOffering() {

	}

	public String getName() {
		return name;
	}

	public String getId() {
		return id;
	}

	public Pair<String, String> getEventTime() {
		return eventTime;
	}

	public List<String> getFeatureOfInterest() {
		return featureOfInterest;
	}

	public List<Pair<String, String>> getProcedures() {
		return procedures;
	}

	public List<String> getObservedProperties() {
		return observedProperties;
	}

	public List<String> getResponseFormat() {
		return responseFormat;
	}

	public List<String> getResponseMode() {
		return responseMode;
	}

	public List<String> getResult() {
		return result;
	}

	public List<String> getResultModel() {
		return resultModel;
	}

	public String getSRSName() {
		return srsName;
	}

	public void setName(String that) {
		name = that;
	}

	public void setId(String that) {
		id = that;
	}

	public void setEventTime(Pair<String, String> that) {
		eventTime = that;
	}

	public void setFeatureOfInterest(List<String> that) {
		featureOfInterest = that;
	}

	public void setProcedures(List<Pair<String, String>> that) {
		procedures = that;
	}

	public void setObservedProperties(List<String> that) {
		observedProperties = that;
	}

	public void setResponseFormat(List<String> that) {
		responseFormat = that;
	}

	public void setResponseMode(List<String> that) {
		responseMode = that;
	}

	public void setResult(List<String> that) {
		result = that;
	}

	public void setResultModel(List<String> that) {
		resultModel = that;
	}

	public void setSRSName(String that) {
		srsName = that;
	}

}
