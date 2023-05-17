package org.deegree.client.sos.requesthandler.kvp;

import java.util.ArrayList;
import java.util.List;

import org.deegree.client.sos.storage.StorageGetCapabilities;
import org.deegree.client.sos.storage.components.ObservationOffering;
import org.deegree.client.sos.storage.components.Operation;
import org.deegree.client.sos.storage.components.Parameter;
import org.deegree.commons.utils.Pair;

/**
 * RequestHandler-class which gets the KVPs for the DescribeSensor request from
 * a StorageGetCapabilities.
 * 
 * @author <a href="mailto:neumeister@lat-lon.de">Ulrich Neumeister</a>
 * 
 */
public class GetKVPDescribeSensor {

	private KVPDescribeSensor kvpDescribeSensor;

	/**
	 * Public constructor fills the KVPDescribeSensor object with contents from
	 * the given StorageGetCapabilities object.
	 * 
	 * @param storage
	 */
	public GetKVPDescribeSensor(StorageGetCapabilities storage) {
		kvpDescribeSensor = new KVPDescribeSensor();
		kvpDescribeSensor.setHost(storage.getHost());
		kvpDescribeSensor.setOutputFormat(parseOutputFormat(storage
				.getOperationsMetadata()));
		kvpDescribeSensor.setProcedure(parseProcedure(storage.getContents()));
		kvpDescribeSensor.setVersion(parseVersion(storage
				.getServiceIdentification()));
	}

	/**
	 * @param operations
	 * @return
	 */
	private List<String> parseOutputFormat(List<Operation> operations) {
		List<String> result = new ArrayList<String>();
		for (Operation operation : operations) {
			if (operation.getName().equals("DescribeSensor")) {
				List<Parameter> parameters = operation.getParameters();
				for (Parameter parameter : parameters) {
					if (parameter.getName().equals("outputFormat")) {
						result = parameter.getAllowedValues();
					}
				}
			}
		}
		return result;
	}

	/**
	 * @param offerings
	 * @return
	 */
	private List<Pair<String, String>> parseProcedure(
			List<ObservationOffering> offerings) {
		List<Pair<String, String>> result = new ArrayList<Pair<String, String>>();
		for (ObservationOffering offering : offerings) {
			List<String> procedures = offering.getProcedures();
			for (String procedure : procedures) {
				String[] parts = procedure.split(":");
				String name = "";
				if (parts.length > 0) {
					name = parts[parts.length - 1];
				}
				Pair<String, String> pair = new Pair<String, String>();
				if (procedure != null && !procedure.trim().equals("")) {
					pair.first = procedure;
					pair.second = name;
				}
				result.add(pair);
			}
		}
		return result;
	}

	/**
	 * @param serviceIdentification
	 * @return
	 */
	private String parseVersion(List<Pair<String, String>> serviceIdentification) {
		String result = "";
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
	public KVPDescribeSensor getKVPDescribeSensor() {
		return kvpDescribeSensor;
	}

	/**
	 * @param that
	 */
	public void setKVPDescribeSensor(KVPDescribeSensor that) {
		kvpDescribeSensor = that;
	}

}
