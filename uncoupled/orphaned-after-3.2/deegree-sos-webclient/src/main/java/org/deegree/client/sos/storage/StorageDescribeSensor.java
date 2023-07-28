package org.deegree.client.sos.storage;

import java.util.List;

import org.apache.axiom.om.OMElement;

/**
 * Storage class containing the contents of a DescribeSensor request.
 * 
 * @author <a href="mailto:neumeister@lat-lon.de">Ulrich Neumeister</a>
 * 
 */
public class StorageDescribeSensor {

	private List<OMElement> keywords;

	private List<OMElement> identification; //

	private List<OMElement> classification; //

	private List<OMElement> validTime;

	private List<OMElement> securityConstraint;

	private List<OMElement> legalConstraint;

	private List<OMElement> characteristics;

	private List<OMElement> capabilities;

	private List<OMElement> contact;

	private List<OMElement> documentation;

	private List<OMElement> history;

	private List<OMElement> member; //

	public StorageDescribeSensor() {

	}

	public List<OMElement> getKeywords() {
		return keywords;
	}

	public List<OMElement> getIdentification() {
		return identification;
	}

	public List<OMElement> getClassification() {
		return classification;
	}

	public List<OMElement> getValidTime() {
		return validTime;
	}

	public List<OMElement> getSecurityConstraint() {
		return securityConstraint;
	}

	public List<OMElement> getLegalConstraint() {
		return legalConstraint;
	}

	public List<OMElement> getCharacteristics() {
		return characteristics;
	}

	public List<OMElement> getCapabilities() {
		return capabilities;
	}

	public List<OMElement> getContact() {
		return contact;
	}

	public List<OMElement> getDocumentation() {
		return documentation;
	}

	public List<OMElement> getHistory() {
		return history;
	}

	public List<OMElement> getMember() {
		return member;
	}

	public void setKeywords(List<OMElement> that) {
		keywords = that;
	}

	public void setIdentification(List<OMElement> that) {
		identification = that;
	}

	public void setClassification(List<OMElement> that) {
		classification = that;
	}

	public void setValidTime(List<OMElement> that) {
		validTime = that;
	}

	public void setSecurityConstraint(List<OMElement> that) {
		securityConstraint = that;
	}

	public void setLegalConstraint(List<OMElement> that) {
		legalConstraint = that;
	}

	public void setCharacteristics(List<OMElement> that) {
		characteristics = that;
	}

	public void setCapabilities(List<OMElement> that) {
		capabilities = that;
	}

	public void setContact(List<OMElement> that) {
		contact = that;
	}

	public void setDocumentation(List<OMElement> that) {
		documentation = that;
	}

	public void setHistory(List<OMElement> that) {
		history = that;
	}

	public void setMember(List<OMElement> that) {
		member = that;
	}
}
