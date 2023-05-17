/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2012 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -
 and
 - Occam Labs UG (haftungsbeschränkt) -

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

 Occam Labs UG (haftungsbeschränkt)
 Godesberger Allee 139, 53175 Bonn
 Germany

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.featureinfo.parsing;

import static org.deegree.commons.tom.primitive.BaseType.STRING;
import static org.deegree.commons.xml.stax.XMLStreamUtils.nextElement;
import static org.deegree.commons.xml.stax.XMLStreamUtils.skipElement;
import static org.deegree.gml.GMLInputFactory.createGMLStreamReader;
import static org.deegree.gml.GMLVersion.GML_2;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.deegree.commons.tom.gml.property.Property;
import org.deegree.commons.tom.gml.property.PropertyType;
import org.deegree.commons.xml.XMLParsingException;
import org.deegree.commons.xml.stax.XMLStreamUtils;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.feature.FeatureCollection;
import org.deegree.feature.GenericFeature;
import org.deegree.feature.GenericFeatureCollection;
import org.deegree.feature.property.SimpleProperty;
import org.deegree.feature.types.DynamicAppSchema;
import org.deegree.feature.types.GenericFeatureType;
import org.deegree.feature.types.property.SimplePropertyType;
import org.deegree.gml.GMLStreamReader;
import org.slf4j.Logger;

/**
 * Responsible for parsing 'feature collections', even if they are broken (eg. ESRI or UMN
 * mapserver feature info responses). Used by the WMS and WMTS clients. Currently, ESRI,
 * UMN mapserver, mywms and normal GML2 feature collections are supported.
 *
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 */
public class DefaultFeatureInfoParser implements FeatureInfoParser {

	private static final Logger LOG = getLogger(DefaultFeatureInfoParser.class);

	private static final XMLInputFactory XML_FACTORY = XMLInputFactory.newInstance();

	@Override
	public FeatureCollection parseAsFeatureCollection(InputStream inputStream, String csvLayerNames)
			throws XMLStreamException {
		XMLStreamReader xmlReader = XML_FACTORY.createXMLStreamReader(inputStream);
		XMLStreamUtils.skipStartDocument(xmlReader);
		try {
			// yes, some versions use a namespace, some do not
			if ((xmlReader.getNamespaceURI() == null || xmlReader.getNamespaceURI().isEmpty()
					|| xmlReader.getNamespaceURI().equals("http://www.esri.com/wms"))
					&& xmlReader.getLocalName().equals("FeatureInfoResponse")) {
				return readESRICollection(xmlReader, csvLayerNames);
			}
			if ((xmlReader.getNamespaceURI() == null || xmlReader.getNamespaceURI().isEmpty())
					&& xmlReader.getLocalName().equals("featureInfo")) {
				return readMyWMSCollection(xmlReader);
			}
			if ((xmlReader.getNamespaceURI() == null || xmlReader.getNamespaceURI().isEmpty())
					&& xmlReader.getLocalName().equals("msGMLOutput")) {
				return readUMNCollection(xmlReader);
			}
			return readGml2FeatureCollection(xmlReader);
		}
		catch (Exception e) {
			String msg = "Unable to parse WMS GetFeatureInfo response as feature collection: " + e.getMessage();
			throw new XMLStreamException(msg, e);
		}
	}

	private FeatureCollection readGml2FeatureCollection(XMLStreamReader xmlReader)
			throws XMLStreamException, XMLParsingException, UnknownCRSException {
		GMLStreamReader reader = createGMLStreamReader(GML_2, xmlReader);
		reader.setApplicationSchema(new DynamicAppSchema());
		return reader.readFeatureCollection();
	}

	private FeatureCollection readESRICollection(XMLStreamReader reader, String idPrefix) throws XMLStreamException {
		GenericFeatureCollection col = new GenericFeatureCollection();

		int count = 0;
		nextElement(reader);
		while (reader.isStartElement() && reader.getLocalName().equals("FIELDS")) {
			List<PropertyType> props = new ArrayList<PropertyType>(reader.getAttributeCount());
			List<Property> propValues = new ArrayList<Property>(reader.getAttributeCount());
			for (int i = 0; i < reader.getAttributeCount(); ++i) {
				String name = reader.getAttributeLocalName(i);
				name = name.substring(name.lastIndexOf(".") + 1);
				String value = reader.getAttributeValue(i);
				SimplePropertyType tp = new SimplePropertyType(new QName(name), 0, 1, STRING, null, null);
				propValues.add(new SimpleProperty(tp, value));
				props.add(tp);
			}
			GenericFeatureType ft = new GenericFeatureType(new QName("feature"), props, false);
			col.add(new GenericFeature(ft, idPrefix + "_esri_" + ++count, propValues, null));
			skipElement(reader);
			nextElement(reader);
		}
		LOG.debug("Found {} features.", col.size());
		return col;
	}

	private FeatureCollection readMyWMSCollection(XMLStreamReader reader) throws XMLStreamException {
		GenericFeatureCollection col = new GenericFeatureCollection();

		nextElement(reader);
		while (reader.isStartElement() && reader.getLocalName().equals("query_layer")) {

			String ftName = reader.getAttributeValue(null, "name");
			int count = 0;

			nextElement(reader);
			while (reader.isStartElement() && reader.getLocalName().equals("object")) {

				List<PropertyType> props = new ArrayList<PropertyType>();
				List<Property> propValues = new ArrayList<Property>();

				nextElement(reader);
				while (!(reader.isEndElement() && reader.getLocalName().equals("object"))) {
					String name = reader.getLocalName();
					String value = reader.getElementText();
					SimplePropertyType tp = new SimplePropertyType(new QName(name), 0, 1, STRING, null, null);
					propValues.add(new SimpleProperty(tp, value));
					props.add(tp);
					nextElement(reader);
				}

				GenericFeatureType ft = new GenericFeatureType(new QName(ftName), props, false);
				col.add(new GenericFeature(ft, "ftName_" + ++count, propValues, null));
				nextElement(reader);
			}
			nextElement(reader);
		}
		return col;
	}

	private FeatureCollection readUMNCollection(XMLStreamReader reader) throws XMLStreamException {
		GenericFeatureCollection col = new GenericFeatureCollection();
		nextElement(reader);

		String ftName = reader.getLocalName();
		String singleFeatureTagName = ftName.split("_")[0] + "_feature";

		while (reader.isStartElement() && reader.getLocalName().equals(ftName)) {

			int count = 0;
			nextElement(reader);

			// gml:name seems to be an optional element
			if (reader.getLocalName().equals("name")) {
				skipElement(reader);
				reader.nextTag();
			}

			while (reader.isStartElement() && reader.getLocalName().equals(singleFeatureTagName)) {
				List<PropertyType> props = new ArrayList<PropertyType>();
				List<Property> propValues = new ArrayList<Property>();

				nextElement(reader);
				while (!(reader.isEndElement() && reader.getLocalName().equals(singleFeatureTagName))) {

					// Skip boundedBy
					if (reader.isStartElement() && reader.getLocalName().equals("boundedBy")) {
						XMLStreamUtils.skipElement(reader);
						nextElement(reader);
					}

					// skip geometry
					if (reader.isStartElement() && reader.getLocalName().equals("geometry")) {
						XMLStreamUtils.skipElement(reader);
						nextElement(reader);
					}

					String name = reader.getLocalName();
					String value = reader.getElementText();
					SimplePropertyType tp = new SimplePropertyType(new QName(name), 0, 1, STRING, null, null);
					propValues.add(new SimpleProperty(tp, value));
					props.add(tp);
					nextElement(reader);
				}
				GenericFeatureType ft = new GenericFeatureType(new QName(ftName), props, false);
				col.add(new GenericFeature(ft, "ftName_" + ++count, propValues, null));
				nextElement(reader);
			}
			nextElement(reader);
		}
		return col;
	}

}
