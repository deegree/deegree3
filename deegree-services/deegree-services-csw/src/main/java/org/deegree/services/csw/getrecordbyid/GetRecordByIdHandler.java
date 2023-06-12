package org.deegree.services.csw.getrecordbyid;

import java.io.IOException;

import javax.xml.stream.XMLStreamException;

import org.deegree.commons.ows.exception.OWSException;
import org.deegree.commons.utils.kvp.InvalidParameterValueException;
import org.deegree.metadata.persistence.MetadataStore;
import org.deegree.services.controller.utils.HttpResponseBuffer;
import org.deegree.services.csw.profile.ServiceProfile;

public interface GetRecordByIdHandler {

	/**
	 * Preprocessing for the export of a {@link GetRecordById} request
	 * @param getRecBI the parsed getRecordById request
	 * @param response for the servlet request to the client
	 * @param isSoap
	 * @throws IOException
	 * @throws XMLStreamException
	 * @throws InvalidParameterValueException
	 * @throws OWSException
	 */
	public abstract void doGetRecordById(GetRecordById getRecBI, HttpResponseBuffer response, MetadataStore<?> store,
			ServiceProfile profile)
			throws XMLStreamException, IOException, InvalidParameterValueException, OWSException;

}