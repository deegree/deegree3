package org.deegree.client.sos.storage;

import java.util.List;

import org.deegree.client.sos.storage.components.Filter_Capabilities;
import org.deegree.client.sos.storage.components.ObservationOffering;
import org.deegree.client.sos.storage.components.Operation;
import org.deegree.commons.utils.Pair;

/**
 * Storage class containing the contents of a GetCapabilities request.<br>
 * "OperationsMetadata" is stored in a list of Operation-objects,
 * "Filter_Capabilities" is stored in a Filter_Capabilities-object and
 * "Contents" is stored in a list of ObservatinOffering-objects.
 * 
 * @author <a href="mailto:neumeister@lat-lon.de">Ulrich Neumeister</a>
 * 
 */
public class StorageGetCapabilities {

	private String host;

	private List<Pair<String, String>> serviceIdentification;

	private List<Pair<String, String>> serviceProvider;

	private List<Operation> operationsMetadata;

	private Filter_Capabilities filter_Capabilities;

	private List<ObservationOffering> contents;

	public StorageGetCapabilities() {

	}

	public String getHost() {
		return host;
	}

	public List<Pair<String, String>> getServiceIdentification() {
		return serviceIdentification;
	}

	public List<Pair<String, String>> getServiceProvider() {
		return serviceProvider;
	}

	public List<Operation> getOperationsMetadata() {
		return operationsMetadata;
	}

	public Filter_Capabilities getFilter_Capabilities() {
		return filter_Capabilities;
	}

	public List<ObservationOffering> getContents() {
		return contents;
	}

	public void setHost(String that) {
		host = that;
	}

	public void setServiceIdentification(List<Pair<String, String>> that) {
		serviceIdentification = that;
	}

	public void setServiceProvider(List<Pair<String, String>> that) {
		serviceProvider = that;
	}

	public void setOperationsMetadata(List<Operation> that) {
		operationsMetadata = that;
	}

	public void setFilter_Capabilities(Filter_Capabilities that) {
		filter_Capabilities = that;
	}

	public void setContents(List<ObservationOffering> that) {
		contents = that;
	}

}
