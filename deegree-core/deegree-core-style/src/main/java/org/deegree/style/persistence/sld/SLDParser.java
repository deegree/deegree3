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
package org.deegree.style.persistence.sld;

import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import static org.deegree.commons.xml.stax.XMLStreamUtils.skipElement;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.deegree.style.se.parser.SymbologyParser;
import org.deegree.style.se.unevaluated.Style;

/**
 * <code>SLDParser</code>
 *
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 */
public class SLDParser {

	/**
	 * @param in
	 * @param layerName
	 * @param styleNames
	 * @return the filters defined for the NamedLayer, and the matching styles
	 * @throws XMLStreamException
	 */
	public static Map<String, LinkedList<Style>> getStyles(XMLStreamReader in) throws XMLStreamException {

		Map<String, LinkedList<Style>> map = new HashMap<String, LinkedList<Style>>();

		while (!in.isStartElement() || in.getLocalName() == null
				|| !(in.getLocalName().equals("NamedLayer") || in.getLocalName().equals("UserLayer"))) {
			in.nextTag();
		}

		while (in.hasNext() && (in.getLocalName().equals("NamedLayer") && !in.isEndElement())
				|| in.getLocalName().equals("UserLayer")) {

			LinkedList<Style> styles = new LinkedList<Style>();

			in.nextTag();

			in.require(START_ELEMENT, null, "Name");
			String name = in.getElementText();

			in.nextTag();

			// skip description
			if (in.getLocalName().equals("Description")) {
				skipElement(in);
			}

			if (in.getLocalName().equals("LayerFeatureConstraints")) {
				skipElement(in);
			}

			if (in.getLocalName().equals("NamedStyle")) {
				// does not make sense to reference a named style when configuring it...
				skipElement(in);
			}

			String styleName = null;

			while (in.hasNext() && in.getLocalName().equals("UserStyle")) {

				while (in.hasNext() && !(in.isEndElement() && in.getLocalName().equals("UserStyle"))) {

					in.nextTag();

					if (in.getLocalName().equals("Name")) {
						styleName = in.getElementText();
					}

					// TODO skipped
					if (in.getLocalName().equals("Description")) {
						skipElement(in);
					}

					// TODO skipped
					if (in.getLocalName().equals("Title")) {
						in.getElementText();
					}

					// TODO skipped
					if (in.getLocalName().equals("Abstract")) {
						in.getElementText();
					}

					if (in.getLocalName().equals("IsDefault")) {
						String def = in.getElementText();
						if (styleName == null && def.equalsIgnoreCase("true")) {
							styleName = "default";
						}
					}

					while (in.getLocalName().equals("FeatureTypeStyle") || in.getLocalName().equals("CoverageStyle")
							|| in.getLocalName().equals("OnlineResource")) {
						Style style = SymbologyParser.INSTANCE.parseFeatureTypeOrCoverageStyle(in);
						style.setName(styleName);
						styles.add(style);
						in.nextTag();
					}

				}
				in.nextTag();

			}
			in.nextTag();
			map.put(name, styles);
		}

		return map;
	}

}
