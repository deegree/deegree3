package org.deegree.client.sos.storage;

import java.util.List;

import org.deegree.client.sos.storage.components.Observation;

/**
 * Storage class containing the contents of a GetObservation request.<br>
 * O&M-data is stored in a list of Observation-objects.
 * 
 * @author <a href="mailto:neumeister@lat-lon.de">Ulrich Neumeister</a>
 * 
 */
public class StorageGetObservation {

	private List<Observation> observationCollection;

	public StorageGetObservation() {

	}

	public List<Observation> getObservationCollection() {
		return observationCollection;
	}

	public void setObservationCollection(List<Observation> that) {
		observationCollection = that;
	}

}
