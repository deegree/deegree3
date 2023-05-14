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
package org.deegree.services.csw.exporthandling;

import static org.deegree.commons.ows.exception.OWSException.NO_APPLICABLE_CODE;
import static org.deegree.commons.xml.CommonNamespaces.XSINS;
import static org.deegree.commons.xml.CommonNamespaces.XSI_PREFIX;
import static org.deegree.protocol.csw.CSWConstants.CSW_202_DISCOVERY_SCHEMA;
import static org.deegree.protocol.csw.CSWConstants.CSW_202_NS;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.commons.ows.exception.OWSException;
import org.deegree.commons.tom.ows.Version;
import org.deegree.commons.utils.kvp.InvalidParameterValueException;
import org.deegree.protocol.csw.MetadataStoreException;
import org.deegree.services.controller.utils.HttpResponseBuffer;
import org.deegree.services.csw.CSWController;
import org.deegree.services.csw.describerecord.DescribeRecord;
import org.deegree.services.csw.profile.ServiceProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Defines the export functionality for a {@link DescribeRecord} request.
 * <p>
 * NOTE:<br>
 * Due to the architecture of this CSW implementation there should be a typeName available
 * which recordStore is requested. But in the describeRecord operation there exists the
 * possibility to get all the recordStores without any typeName available. So at the
 * moment there is a HACK for this UseCase.
 *
 * @see CSWController
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 */
public class DescribeRecordHandler {

	private static final Logger LOG = LoggerFactory.getLogger(DescribeRecordHandler.class);

	private DescribeRecordHelper dcHelper = new DescribeRecordHelper();

	private ServiceProfile profile;

	/**
	 * Preprocessing for the export of a {@link DescribeRecord} request to determine which
	 * recordstore is requested.
	 * @param descRec the parsed describeRecord request
	 * @param response for the servlet request to the client
	 * @throws XMLStreamException
	 * @throws IOException
	 * @throws OWSException
	 */
	public void doDescribeRecord(DescribeRecord descRec, HttpResponseBuffer response, ServiceProfile profile)
			throws XMLStreamException, IOException, OWSException {

		this.profile = profile;

		QName[] typeNames = descRec.getTypeNames();

		Version version = descRec.getVersion();
		response.setContentType(descRec.getOutputFormat());

		XMLStreamWriter xmlWriter = dcHelper.getXMLResponseWriter(response, null);

		try {
			export(xmlWriter, typeNames, version);
		}
		catch (MetadataStoreException e) {
			LOG.debug(e.getMessage());
			throw new OWSException(e.getMessage(), NO_APPLICABLE_CODE);
		}
		xmlWriter.flush();

	}

	/**
	 *
	 * Exports the correct recognized request and determines to which version export it
	 * should delegate it.
	 * @param writer to write the XML to
	 * @param record the recordStore that is requested
	 * @throws XMLStreamException
	 * @throws MetadataStoreException
	 */
	private void export(XMLStreamWriter writer, QName[] typeNames, Version version)
			throws XMLStreamException, MetadataStoreException {
		List<String> supportedVersions = profile.getSupportedVersions();
		if (supportedVersions.contains(version.toString())) {
			export202(writer, typeNames);
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

	/**
	 * Exporthandling for the CSW version 2.0.2. <br>
	 * It is a container for zero or more SchemaComponent elements.
	 * @param writer
	 * @param record
	 * @throws XMLStreamException
	 * @throws MetadataStoreException
	 */
	private void export202(XMLStreamWriter writer, QName[] typeNames)
			throws XMLStreamException, MetadataStoreException {
		writer.setDefaultNamespace(CSW_202_NS);
		writer.writeStartElement(CSW_202_NS, "DescribeRecordResponse");
		writer.writeDefaultNamespace(CSW_202_NS);
		writer.writeNamespace(XSI_PREFIX, XSINS);
		writer.writeAttribute(XSINS, "schemaLocation", CSW_202_NS + " " + CSW_202_DISCOVERY_SCHEMA);

		if (typeNames == null || typeNames.length == 0) {
			typeNames = profile.getDefaultTypeNames();
		}
		for (QName typeName : typeNames) {
			URL schema = profile.getSchema(typeName);
			List<URL> schemaReferences = profile.getSchemaReferences(typeName);
			if (schema != null) {
				writeSchema(writer, typeName, schema);
			}
			else if (schemaReferences != null && !schemaReferences.isEmpty()) {
				for (URL ref : schemaReferences) {
					writeSchemaReference(writer, typeName, ref);
				}
			}
			else {
				String errorMessage = "The typeName " + typeName + " is not supported. ";
				LOG.debug(errorMessage);
				throw new InvalidParameterValueException(errorMessage);
			}
		}
		writer.writeEndElement();// DescribeRecordResponse
		writer.writeEndDocument();
	}

	private void writeSchema(XMLStreamWriter writer, QName typeName, URL url) throws MetadataStoreException {
		try {
			URLConnection urlConn = url.openConnection();
			BufferedInputStream bais = new BufferedInputStream(urlConn.getInputStream());
			InputStreamReader isr = new InputStreamReader(bais, "UTF-8");
			dcHelper.exportSchemaComponent(writer, typeName, isr);
		}
		catch (Exception e) {
			LOG.info("Could not get connection to " + url.toExternalForm() + ". Try to export schema as reference.");
			List<URL> schemaReferenceSnippet = profile.getSchemaReferences(typeName);
			for (URL ref : schemaReferenceSnippet) {
				writeSchemaReference(writer, typeName, ref);
			}
		}
	}

	private void writeSchemaReference(XMLStreamWriter writer, QName typeName, URL url) throws MetadataStoreException {
		if (url == null) {
			LOG.info("Could not find schema reference snippet for type name " + typeName);
			return;
		}
		try {
			InputStreamReader isr = new InputStreamReader(url.openStream(), "UTF-8");
			dcHelper.exportSchemaComponent(writer, typeName, isr);
		}
		catch (UnsupportedEncodingException e) {
			String msg = "Could not export " + typeName;
			LOG.debug(msg, e);
			throw new MetadataStoreException(msg);
		}
		catch (IOException e) {
			String msg = "Could not export " + typeName;
			LOG.debug(msg, e);
			throw new MetadataStoreException(msg);
		}
		catch (XMLStreamException e) {
			String msg = "Could not export " + typeName;
			LOG.debug(msg, e);
			throw new MetadataStoreException(msg);
		}
	}

}
