/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2011 by:
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
package org.deegree.metadata;

import static org.deegree.metadata.DCRecord.DC_RECORD_NS;
import static org.deegree.metadata.ebrim.RegistryObject.RIM_NS;
import static org.deegree.metadata.iso.ISORecord.ISO_RECORD_NS;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axiom.om.OMElement;
import org.apache.commons.io.IOUtils;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.XMLParsingException;
import org.deegree.metadata.ebrim.AdhocQuery;
import org.deegree.metadata.ebrim.Association;
import org.deegree.metadata.ebrim.Classification;
import org.deegree.metadata.ebrim.ClassificationNode;
import org.deegree.metadata.ebrim.ExtrinsicObject;
import org.deegree.metadata.ebrim.RIMType;
import org.deegree.metadata.ebrim.RegistryObject;
import org.deegree.metadata.ebrim.RegistryPackage;
import org.deegree.metadata.iso.ISORecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main entry point for creating {@link MetadataRecord} instances from XML
 * representations.
 *
 * TODO Factory concept needs reconsideration, especially with regard to plugability for
 * different metadata formats (ISO, ebRIM, ...). Ideally, this factory shouldn't have any
 * compile-time dependencies to the concrete record types.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
public class MetadataRecordFactory {

	private static Logger LOG = LoggerFactory.getLogger(MetadataRecordFactory.class);

	/**
	 * Creates a {@link MetadataRecord} instance out a {@link XMLStreamReader}. The reader
	 * must point to the START_ELEMENT of the record. After reading the record the stream
	 * points to the END_ELEMENT of the record.
	 * @param xmlStream xmlStream must point to the START_ELEMENT of the record, must not
	 * be <code>null</code>
	 * @return a {@link MetadataRecord} instance, never <code>null</code>
	 */
	public static MetadataRecord create(XMLStreamReader xmlStream) {
		if (!xmlStream.isStartElement()) {
			throw new XMLParsingException(xmlStream, "XMLStreamReader does not point to a START_ELEMENT.");
		}

		String ns = xmlStream.getNamespaceURI();

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		XMLStreamWriter writer = null;
		XMLStreamReader recordAsXmlStream;
		InputStream in = null;
		try {
			XMLOutputFactory factory = XMLOutputFactory.newInstance();
			factory.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, Boolean.TRUE);
			writer = factory.createXMLStreamWriter(out);

			writer.writeStartDocument();
			XMLAdapter.writeElement(writer, xmlStream);
			writer.writeEndDocument();

			writer.close();
			in = new ByteArrayInputStream(out.toByteArray());
			recordAsXmlStream = XMLInputFactory.newInstance().createXMLStreamReader(in);
		}
		catch (XMLStreamException e) {
			throw new XMLParsingException(xmlStream, e.getMessage());
		}
		catch (FactoryConfigurationError e) {
			throw new XMLParsingException(xmlStream, e.getMessage());
		}
		finally {
			IOUtils.closeQuietly(in);
			IOUtils.closeQuietly(out);
		}

		if (ISO_RECORD_NS.equals(ns)) {
			return new ISORecord(recordAsXmlStream);
		}
		if (RIM_NS.equals(ns)) {
			throw new UnsupportedOperationException(
					"Creating ebRIM records from XMLStreamReader is not implemented yet.");
		}
		if (DC_RECORD_NS.equals(ns)) {
			throw new UnsupportedOperationException("Creating DC records from XMLStreamReader is not implemented yet.");
		}
		throw new IllegalArgumentException("Unknown / unsuppported metadata namespace '" + ns + "'.");

	}

	/**
	 * Creates a new {@link MetadataRecord} from the given element.
	 * @param rootEl root element, must not be <code>null</code>
	 * @return metadata record instance, never <code>null</code>
	 * @throws IllegalArgumentException if the metadata format is unknown / record invalid
	 */
	public static MetadataRecord create(OMElement rootEl) throws IllegalArgumentException {
		String ns = rootEl.getNamespace().getNamespaceURI();
		if (ISO_RECORD_NS.equals(ns)) {
			return new ISORecord(rootEl);
		}
		if (RIM_NS.equals(ns)) {
			RIMType type = null;
			try {
				type = RIMType.valueOf(rootEl.getLocalName());
			}
			catch (Throwable t) {
				throw new IllegalArgumentException(
						"Element '" + rootEl.getLocalName() + "' does not denote an ebRIM 3.0 registry object.");
			}
			switch (type) {
				case AdhocQuery:
					return new AdhocQuery(rootEl);
				case Association:
					return new Association(rootEl);
				case Classification:
					return new Classification(rootEl);
				case ClassificationNode:
					return new ClassificationNode(rootEl);
				case ExtrinsicObject:
					return new ExtrinsicObject(rootEl);
				case RegistryObject:
					return new RegistryObject(rootEl);
				case RegistryPackage:
					return new RegistryPackage(rootEl);
				default:
					LOG.warn("Treating registry object '" + type + "' as generic registry object.");
					return new RegistryObject(rootEl);
			}
		}
		if (DC_RECORD_NS.equals(ns)) {
			throw new UnsupportedOperationException("Creating DC records from XML is not implemented yet.");
		}
		throw new IllegalArgumentException("Unknown / unsuppported metadata namespace '" + ns + "'.");
	}

	/**
	 * Creates a new {@link MetadataRecord} from the given file.
	 * @param file record file, must not be <code>null</code>
	 * @return metadata record instance, never <code>null</code>
	 * @throws IllegalArgumentException if the metadata format is unknown / record invalid
	 */
	public static MetadataRecord create(File file) throws IllegalArgumentException {
		return create(new XMLAdapter(file).getRootElement());
	}

}