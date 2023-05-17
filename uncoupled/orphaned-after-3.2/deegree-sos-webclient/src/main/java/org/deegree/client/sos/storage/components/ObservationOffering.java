package org.deegree.client.sos.storage.components;

import java.util.List;

import org.apache.axiom.om.OMElement;

/**
 * Helper class for StorageGetCapabilities class containing the contents of the
 * XML element "ObservationOffering".
 * 
 * @author <a href="mailto:neumeister@lat-lon.de">Ulrich Neumeister</a>
 * 
 */
public class ObservationOffering {

	private BoundedBy boundedBy;

	private List<OMElement> featuresOfInterest;

	private List<OMElement> intendedApplications;

	private List<OMElement> metadata;

	private List<String> observedProperties;

	private String offeringId;

	private List<String> procedures;

	private List<OMElement> responseFormats;

	private List<OMElement> responseModes;

	private List<OMElement> resultModels;

	private Time time;

	public ObservationOffering() {

	}

	public String getId() {
		return offeringId;
	}

	public List<OMElement> getMetadata() {
		return metadata;
	}

	public BoundedBy getBoundedBy() {
		return boundedBy;
	}

	public List<OMElement> getIntendedApplications() {
		return intendedApplications;
	}

	public Time getTime() {
		return time;
	}

	public List<String> getProcedures() {
		return procedures;
	}

	public List<String> getObservedProperties() {
		return observedProperties;
	}

	public List<OMElement> getFeaturesOfInterest() {
		return featuresOfInterest;
	}

	public List<OMElement> getResponseFormats() {
		return responseFormats;
	}

	public List<OMElement> getResponseModes() {
		return responseModes;
	}

	public List<OMElement> getResultModels() {
		return resultModels;
	}

	public void setId(String that) {
		offeringId = that;
	}

	public void setMetadata(List<OMElement> that) {
		metadata = that;
	}

	public void setBoundedBy(BoundedBy that) {
		boundedBy = that;
	}

	public void setIntendedApplications(List<OMElement> that) {
		intendedApplications = that;
	}

	public void setTime(Time that) {
		time = that;
	}

	public void setProcedures(List<String> that) {
		procedures = that;
	}

	public void setFeaturesOfInterest(List<OMElement> that) {
		featuresOfInterest = that;
	}

	public void setResponseFormats(List<OMElement> that) {
		responseFormats = that;
	}

	public void setResponseModes(List<OMElement> that) {
		responseModes = that;
	}

	public void setResultModels(List<OMElement> that) {
		resultModels = that;
	}

	public void setObservedProperties(List<String> that) {
		observedProperties = that;
	}

}
