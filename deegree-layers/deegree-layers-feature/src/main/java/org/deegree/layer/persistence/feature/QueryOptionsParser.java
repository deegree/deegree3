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
package org.deegree.layer.persistence.feature;

import static java.util.Collections.singleton;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static org.deegree.commons.xml.CommonNamespaces.OGCNS;
import static org.deegree.commons.xml.stax.XMLStreamUtils.moveReaderToFirstMatch;
import static org.deegree.commons.xml.stax.XMLStreamUtils.nextElement;
import static org.deegree.commons.xml.stax.XMLStreamUtils.requireStartElement;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.stream.StreamSource;

import org.deegree.commons.xml.NamespaceBindings;
import org.deegree.commons.xml.XPathUtils;
import org.deegree.filter.OperatorFilter;
import org.deegree.filter.expression.ValueReference;
import org.deegree.filter.sort.SortProperty;
import org.deegree.filter.xml.Filter110XMLDecoder;

/**
 * Parses sortby and filter sections of feature layer configurations.
 *
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 */
class QueryOptionsParser {

	static OperatorFilter parseFilter(int layerIndex, InputStream in) throws XMLStreamException {

		XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(new StreamSource(in));

		int idx = -1;
		while (idx != layerIndex) {
			moveReaderToFirstMatch(reader, new QName("http://www.deegree.org/layers/feature", "FeatureLayer"));
			reader.next();
			++idx;
		}

		while (!(reader.isEndElement() && reader.getLocalName().equals("FeatureLayer"))) {
			reader.next();
			if (reader.isStartElement() && reader.getLocalName().equals("Filter")
					&& reader.getNamespaceURI().equals(OGCNS)) {
				OperatorFilter filter = null;
				filter = (OperatorFilter) Filter110XMLDecoder.parse(reader);
				reader.close();
				return filter;
			}
		}

		reader.close();
		return null;
	}

	static List<SortProperty> parseSortBy(int layerIndex, InputStream in) throws XMLStreamException {

		XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(new StreamSource(in));

		int idx = -1;
		while (idx != layerIndex) {
			moveReaderToFirstMatch(reader, new QName("http://www.deegree.org/layers/feature", "FeatureLayer"));
			reader.next();
			++idx;
		}

		while (!(reader.isEndElement() && reader.getLocalName().equals("FeatureLayer"))) {
			reader.next();
			if (reader.isStartElement() && reader.getLocalName().equals("SortBy")
					&& reader.getNamespaceURI().equals(OGCNS)) {
				nextElement(reader);
				List<SortProperty> sortCrits = new ArrayList<SortProperty>();
				while (reader.isStartElement()) {
					SortProperty prop = parseSortProperty(reader);
					sortCrits.add(prop);
					nextElement(reader);
				}
				reader.close();
				return sortCrits;
			}
		}

		reader.close();
		return null;
	}

	private static SortProperty parseSortProperty(XMLStreamReader reader) throws XMLStreamException {

		requireStartElement(reader, singleton(new QName(OGCNS, "SortProperty")));
		nextElement(reader);

		requireStartElement(reader, singleton(new QName(OGCNS, "PropertyName")));

		String xpath = reader.getElementText().trim();
		Set<String> prefixes = XPathUtils.extractPrefixes(xpath);
		NamespaceBindings nsContext = new NamespaceBindings(reader.getNamespaceContext(), prefixes);
		ValueReference propName = new ValueReference(xpath, nsContext);
		nextElement(reader);

		boolean sortAscending = true;
		if (reader.isStartElement()) {
			requireStartElement(reader, singleton(new QName(OGCNS, "SortOrder")));
			String s = reader.getElementText().trim();
			sortAscending = "ASC".equals(s);
			nextElement(reader);
		}

		reader.require(END_ELEMENT, OGCNS, "SortProperty");
		return new SortProperty(propName, sortAscending);
	}

}
