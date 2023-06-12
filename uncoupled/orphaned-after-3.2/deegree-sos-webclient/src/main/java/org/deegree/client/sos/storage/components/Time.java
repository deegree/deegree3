package org.deegree.client.sos.storage.components;

import java.util.List;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;

/**
 * Helper class for StorageGetObservation class containing the contents of the
 * XML element "Time".
 * 
 * @author <a href="mailto:neumeister@lat-lon.de">Ulrich Neumeister</a>
 * 
 */
public class Time {

	private boolean isNull;

	private List<OMAttribute> attributesOfTime;

	private List<OMAttribute> attributesOfChild;

	private List<OMElement> elements;

	public Time() {

	}

	public boolean getIsNull() {
		return isNull;
	}

	public List<OMAttribute> getAttributesOfTime() {
		return attributesOfTime;
	}

	public List<OMAttribute> getAttributesOfChild() {
		return attributesOfChild;
	}

	public List<OMElement> getElements() {
		return elements;
	}

	public void setIsNull(boolean that) {
		isNull = that;
	}

	public void setAttributesOfTime(List<OMAttribute> that) {
		attributesOfTime = that;
	}

	public void setAttributesOfChild(List<OMAttribute> that) {
		attributesOfChild = that;
	}

	public void setElements(List<OMElement> that) {
		elements = that;
	}

}
