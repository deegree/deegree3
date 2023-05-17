/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -

 This library is free software; you can redistribute it and/or modify it under
 the terms of the GNU Lesser General Public License as published by the Free
 Software Foundation; either version 2.1 of the License, or (at your option)
 any later version.
 This library is distributed in the hope that it will be useful, but WITHOUT
 ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 details.
 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation, Inc.,
 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

 Contact information:

 lat/lon GmbH
 Aennchenstr. 19, 53177 Bonn
 Germany
 http://lat-lon.de/

 Department of Geography, University of Bonn
 Prof. Dr. Klaus Greve
 Postfach 1147, 53001 Bonn
 Germany
 http://www.geographie.uni-bonn.de/deegree/

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.services.csw.getrecordbyid;

import static de.odysseus.staxon.json.JsonXMLOutputFactory.PROP_AUTO_ARRAY;
import static de.odysseus.staxon.json.JsonXMLOutputFactory.PROP_NAMESPACE_DECLARATIONS;
import static de.odysseus.staxon.json.JsonXMLOutputFactory.PROP_PRETTY_PRINT;
import static org.deegree.commons.ows.exception.OWSException.NO_APPLICABLE_CODE;
import static org.deegree.protocol.csw.CSWConstants.CSW_202_NS;
import static org.deegree.protocol.csw.CSWConstants.CSW_PREFIX;

import java.io.IOException;
import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.commons.ows.exception.OWSException;
import org.deegree.commons.tom.ows.Version;
import org.deegree.commons.utils.kvp.InvalidParameterValueException;
import org.deegree.commons.xml.stax.SchemaLocationXMLStreamWriter;
import org.deegree.commons.xml.stax.TrimmingXMLStreamWriter;
import org.deegree.metadata.MetadataRecord;
import org.deegree.metadata.persistence.MetadataResultSet;
import org.deegree.metadata.persistence.MetadataStore;
import org.deegree.protocol.csw.MetadataStoreException;
import org.deegree.services.controller.utils.HttpResponseBuffer;
import org.deegree.services.csw.profile.ServiceProfile;
import org.deegree.services.i18n.Messages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.odysseus.staxon.json.JsonXMLOutputFactory;

/**
 * Defines the export functionality for a {@link GetRecordById} request
 *
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 */
public class DefaultGetRecordByIdHandler implements GetRecordByIdHandler {

	private static final Logger LOG = LoggerFactory.getLogger(DefaultGetRecordByIdHandler.class);

	private ServiceProfile profile;

	@Override
	public void doGetRecordById(GetRecordById getRecBI, HttpResponseBuffer response, MetadataStore<?> store,
			ServiceProfile profile)
			throws XMLStreamException, IOException, InvalidParameterValueException, OWSException {
		this.profile = profile;
		LOG.debug("doGetRecordById: " + getRecBI);
		Version version = getRecBI.getVersion();

		String outputFormat = getRecBI.getOutputFormat();
		response.setContentType(outputFormat);

		// to be sure of a valid response
		String schemaLocation = profile.getGetRecordByIdSchemaLocation(getRecBI.getVersion());

		XMLStreamWriter xmlWriter = getXmlorJsonStreamWriter(outputFormat, response, schemaLocation);
		try {
			export(xmlWriter, getRecBI, version, store);
		}
		catch (OWSException e) {
			LOG.debug(e.getMessage());
			throw new InvalidParameterValueException(e.getMessage());
		}
		catch (MetadataStoreException e) {
			throw new OWSException(e.getMessage(), NO_APPLICABLE_CODE);
		}
		xmlWriter.flush();
	}

	private XMLStreamWriter getXmlorJsonStreamWriter(String outputFormat, HttpResponseBuffer response,
			String schemaLocation) throws XMLStreamException, IOException {
		XMLStreamWriter xmlWriter = null;
		if ("application/json".equals(outputFormat)) {
			JsonXMLOutputFactory factory = new JsonXMLOutputFactory();
			factory.setProperty(PROP_PRETTY_PRINT, true);
			factory.setProperty(PROP_NAMESPACE_DECLARATIONS, false);
			factory.setProperty(PROP_AUTO_ARRAY, true);
			xmlWriter = factory.createXMLStreamWriter(response.getOutputStream());
			xmlWriter.writeStartDocument();
		}
		else {
			xmlWriter = getXMLResponseWriter(response, schemaLocation);
		}
		return xmlWriter;
	}

	/**
	 * Exports the correct recognized request and determines to which version export it
	 * should delegate the request
	 */
	private void export(XMLStreamWriter xmlWriter, GetRecordById getRecBI, Version version, MetadataStore<?> store)
			throws XMLStreamException, OWSException, MetadataStoreException {
		List<String> supportedVersions = profile.getSupportedVersions();
		if (supportedVersions.contains(version.toString())) {
			export202(xmlWriter, getRecBI, store);
		}
		else {
			StringBuilder sb = new StringBuilder();
			sb.append("Version '").append(version);
			sb.append("' is not supported.");
			sb.append(" Supported versions are ");
			boolean isFirst = true;
			for (String v : supportedVersions) {
				if (isFirst) {
					isFirst = false;
				}
				else {
					sb.append(", ");
				}
				sb.append(v);
			}
			throw new IllegalArgumentException(sb.toString());
		}
	}

	private void export202(XMLStreamWriter writer, GetRecordById getRecBI, MetadataStore<?> store)
			throws XMLStreamException, OWSException, MetadataStoreException {

		writer.writeStartElement(CSW_PREFIX, "GetRecordByIdResponse", CSW_202_NS);
		writer.writeNamespace(CSW_PREFIX, CSW_202_NS);

		MetadataResultSet<?> resultSet = null;
		int countIdList = 0;
		List<String> requestedIdList = getRecBI.getRequestedIds();
		int requestedIds = requestedIdList.size();
		MetadataRecord recordResponse = null;
		try {
			resultSet = getRecordById(getRecBI, store, requestedIdList);

			while (resultSet.next()) {
				countIdList++;
				recordResponse = resultSet.getRecord();
				if (profile.returnAsDC(getRecBI.getOutputSchema())) {
					recordResponse.toDublinCore().serialize(writer, getRecBI.getElementSetName());
				}
				else {
					recordResponse.serialize(writer, getRecBI.getElementSetName());
				}
				removeId(recordResponse, requestedIdList);
			}
			if (profile.isStrict() && countIdList != requestedIds) {
				String msg = Messages.getMessage("CSW_NO_IDENTIFIER_FOUND", requestedIdList);
				LOG.debug(msg);
				throw new MetadataStoreException(msg);
			}
		}
		finally {
			if (resultSet != null) {
				resultSet.close();
			}
		}
		writer.writeEndDocument();

	}

	protected MetadataResultSet<?> getRecordById(GetRecordById getRecBI, MetadataStore<?> store,
			List<String> requestedIdList) throws OWSException {
		if (store != null) {
			try {
				return store.getRecordById(requestedIdList, getRecBI.getTypeNames());
			}
			catch (MetadataStoreException e) {
				throw new OWSException(e.getMessage(), OWSException.NO_APPLICABLE_CODE);
			}
		}
		return null;
	}

	/**
	 * Returns an <code>XMLStreamWriter</code> for writing an XML response document.
	 * @param writer writer to write the XML to, must not be null
	 * @param schemaLocation allows to specify a value for the 'xsi:schemaLocation'
	 * attribute in the root element, must not be null
	 * @return {@link XMLStreamWriter}
	 * @throws XMLStreamException
	 * @throws IOException
	 */
	private XMLStreamWriter getXMLResponseWriter(HttpResponseBuffer writer, String schemaLocation)
			throws XMLStreamException, IOException {

		if (schemaLocation == null) {
			return writer.getXMLWriter();
		}
		XMLStreamWriter fWriter = new TrimmingXMLStreamWriter(writer.getXMLWriter());
		return new SchemaLocationXMLStreamWriter(fWriter, schemaLocation);
		// return new TrimmingXMLStreamWriter( writer.getXMLWriter() );
	}

	/**
	 * Removes the identifier of the requested ID list to have a proper subset of the
	 * {@link MetadataRecord}s that are not found in backend.
	 * @param recordResponse the {@link MetadataRecord} that is found in backend, not
	 * <Code>null</Code>.
	 * @param requestedIdList the list of requested identifier, not <Code>null</Code>.
	 * @throws MetadataStoreException
	 */
	private void removeId(MetadataRecord recordResponse, List<String> requestedIdList) throws MetadataStoreException {
		try {
			String identifier = recordResponse.getIdentifier();
			requestedIdList.remove(identifier);
		}
		catch (NullPointerException e) {
			// should not occur!
			String msg = "There is no Identifier available...whyever!";
			LOG.error(msg);
			throw new MetadataStoreException(msg);
		}
	}

}
