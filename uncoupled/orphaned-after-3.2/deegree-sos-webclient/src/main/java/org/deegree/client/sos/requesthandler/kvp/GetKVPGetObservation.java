package org.deegree.client.sos.requesthandler.kvp;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.deegree.client.sos.storage.StorageGetCapabilities;
import org.deegree.client.sos.storage.components.ObservationOffering;
import org.deegree.client.sos.storage.components.Time;
import org.deegree.commons.utils.Pair;

/**
 * RequestHandler-class which gets the KVPs for the GetObservation request from
 * a StorageGetCapabilities.
 * 
 * @author <a href="mailto:neumeister@lat-lon.de">Ulrich Neumeister</a>
 * 
 */
public class GetKVPGetObservation {

	private KVPGetObservation kvpGetObservation;

	/**
	 * Public constructor fills the KVPGetObservation object with contents from
	 * the given StorageGetCapabilities object.
	 * 
	 * @param storage
	 */
	public GetKVPGetObservation(StorageGetCapabilities storage) {
		kvpGetObservation = new KVPGetObservation();
		kvpGetObservation.setHost( storage.getHost() );
		kvpGetObservation.setKVPfromOffering(parseOffering(storage
				.getContents()));
		kvpGetObservation.setVersion(parseVersion(storage
				.getServiceIdentification()));
	}

	/**
	 * @param contents
	 * @return A List of KVPfromOffering objects containing the
	 *         offering-specific KVPs from the GetCapabilities response.
	 */
	private List<KVPfromOffering> parseOffering(
			List<ObservationOffering> contents) {
		List<KVPfromOffering> result = new ArrayList<KVPfromOffering>();
		KVPfromOffering kvpfromOffering;
		for (ObservationOffering offering : contents) {
			kvpfromOffering = new KVPfromOffering();
			kvpfromOffering.setId(offering.getId());
			String name = "none";
			List<OMElement> elements = offering.getMetadata();
			for (OMElement element : elements) {
				if (element.getLocalName().equals("name")) {
					name = element.getText();
				}
			}
			kvpfromOffering.setName(name);
			Pair<String, String> eventTime = new Pair<String, String>();
			Time time = offering.getTime();
			if (!time.getIsNull()) {
				elements = time.getElements();
				for (OMElement element : elements) {
					String elementName = element.getLocalName();
					if (elementName.equals("beginPosition")
							|| elementName.equals("begin")) {
						eventTime.first = element.getText();
					} else if (elementName.equals("endPosition")
							|| elementName.equals("end")) {
						eventTime.second = element.getText();
					}
				}
			} else {
				eventTime.first = "null";
				eventTime.second = "null";
			}
			kvpfromOffering.setEventTime(eventTime);
			List<String> featureOfInterest = new ArrayList<String>();
			elements = offering.getFeaturesOfInterest();
			for (OMElement element : elements) {
				Iterator<OMAttribute> attributes;
				for (attributes = element.getAllAttributes(); attributes
						.hasNext();) {
					OMAttribute attribute = attributes.next();
					featureOfInterest.add(attribute.getAttributeValue());
				}
			}
			kvpfromOffering.setFeatureOfInterest(featureOfInterest);
			kvpfromOffering.setObservedProperties(offering
					.getObservedProperties());
			List<Pair<String, String>> procedures = new ArrayList<Pair<String, String>>();
			List<String> procfromOffering = offering.getProcedures();
			for (String procedure : procfromOffering) {
				String[] parts = procedure.split(":");
				String procedureName = "";
				if (parts.length > 0) {
					procedureName = parts[parts.length - 1];
				}
				Pair<String, String> pair = new Pair<String, String>();
				if (procedure != null && !procedure.trim().equals("")) {
					pair.first = procedure;
					pair.second = procedureName;
				}
				procedures.add(pair);
			}
			kvpfromOffering.setProcedures(procedures);
			List<String> responseFormat = new ArrayList<String>();
			List<String> responseMode = new ArrayList<String>();
			List<String> results = new ArrayList<String>();
			List<String> resultModel = new ArrayList<String>();
			elements = offering.getResponseFormats();
			for (OMElement element : elements) {
				responseFormat.add(element.getText());
			}
			elements = offering.getResponseModes();
			for (OMElement element : elements) {
				responseMode.add(element.getText());
			}
			elements = offering.getResultModels();
			for (OMElement element : elements) {
				resultModel.add(element.getText());
			}
			kvpfromOffering.setResponseFormat(responseFormat);
			kvpfromOffering.setResponseMode(responseMode);
			kvpfromOffering.setResult(results);
			kvpfromOffering.setResultModel(resultModel);
			String srsName = offering.getBoundedBy().getType();
			if (!srsName.equals("Null")) {
				List<OMAttribute> attributes = offering.getBoundedBy()
						.getAttributes();
				for (OMAttribute attribute : attributes) {
					if (attribute.getLocalName().equals("srsName")) {
						srsName = attribute.getAttributeValue();
					}
				}
			}
			kvpfromOffering.setSRSName(srsName);
			result.add(kvpfromOffering);
		}
		return result;
	}

	/**
	 * @param serviceIdentification
	 * @return
	 */
	private String parseVersion(List<Pair<String, String>> serviceIdentification) {
		String result = "none";
		for (Pair<String, String> pair : serviceIdentification) {
			if (pair.first.equals("ServiceTypeVersion")) {
				result = pair.second;
			}
		}
		return result;
	}

	/**
	 * @return
	 */
	public KVPGetObservation getKVPGetObservation() {
		return kvpGetObservation;
	}

	/**
	 * @param that
	 */
	public void setKVPGetObservation(KVPGetObservation that) {
		kvpGetObservation = that;
	}

}
