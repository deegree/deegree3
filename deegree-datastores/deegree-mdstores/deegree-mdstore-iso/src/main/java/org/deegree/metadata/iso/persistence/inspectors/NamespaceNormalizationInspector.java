/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2012 by:
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
package org.deegree.metadata.iso.persistence.inspectors;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.sql.Connection;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.commons.xml.NamespaceBindings;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.stax.NamespaceNormalizingXMLStreamWriter;
import org.deegree.metadata.iso.ISORecord;
import org.deegree.metadata.persistence.MetadataInspectorException;
import org.deegree.metadata.persistence.inspectors.RecordInspector;
import org.deegree.metadata.persistence.iso19115.jaxb.NamespaceNormalizer;
import org.deegree.metadata.persistence.iso19115.jaxb.NamespaceNormalizer.NamespaceBinding;
import org.deegree.sqldialect.SQLDialect;
import org.slf4j.Logger;

/**
 * {@link RecordInspector} that performs normalization of namespace prefixes.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
public class NamespaceNormalizationInspector implements RecordInspector<ISORecord> {

	private static final Logger LOG = getLogger(NamespaceNormalizationInspector.class);

	private final NamespaceBindings nsBindings = new NamespaceBindings();

	/**
	 * Creates a new {@link NamespaceNormalizationInspector} instance.
	 * @param config inspector configuration, must not be <code>null</code>
	 */
	public NamespaceNormalizationInspector(NamespaceNormalizer config) {
		if (config.getNamespaceBinding() != null) {
			for (NamespaceBinding binding : config.getNamespaceBinding()) {
				String prefix = binding.getPrefix();
				String namespaceUri = binding.getNamespaceURI();
				LOG.debug("'" + prefix + "' -> '" + namespaceUri + "'");
				nsBindings.addNamespace(prefix, namespaceUri);
			}
		}
	}

	@Override
	public ISORecord inspect(ISORecord record, Connection conn, SQLDialect dialect) throws MetadataInspectorException {

		ISORecord result = record;

		try {
			// create temporary sink for normalized XML
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			XMLStreamWriter writer = XMLOutputFactory.newInstance().createXMLStreamWriter(bos);
			writer = new NamespaceNormalizingXMLStreamWriter(writer, nsBindings);

			// create normalized copy
			XMLStreamReader reader = record.getAsXMLStream();
			XMLAdapter.writeElement(writer, reader);
			reader.close();
			writer.close();

			InputStream is = new ByteArrayInputStream(bos.toByteArray());
			XMLStreamReader xmlStream = XMLInputFactory.newInstance().createXMLStreamReader(is);
			result = new ISORecord(xmlStream);
		}
		catch (Throwable t) {
			LOG.error("Namespace normalization failed. Proceeding with unnormalized record. Error: " + t.getMessage());
		}

		return result;
	}

}
