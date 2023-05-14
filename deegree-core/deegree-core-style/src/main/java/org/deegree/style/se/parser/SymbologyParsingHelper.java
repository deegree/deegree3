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
package org.deegree.style.se.parser;

import static org.deegree.commons.xml.stax.XMLStreamUtils.skipElement;
import static org.deegree.filter.xml.Filter110XMLDecoder.parseExpression;
import static org.slf4j.LoggerFactory.getLogger;

import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.deegree.filter.Expression;
import org.slf4j.Logger;

/**
 * Responsible for parsing common elements in SE files.
 *
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 */
class SymbologyParsingHelper {

	static final Logger LOG = getLogger(SymbologyParsingHelper.class);

	static void parseCommon(Common common, XMLStreamReader in) throws XMLStreamException {
		if (in.getLocalName().equals("Name")) {
			common.name = in.getElementText();
		}
		Location l = in.getLocation();
		if (in.getLocalName().startsWith("Geometry")) {
			common.loc = l.getSystemId();
			common.line = l.getLineNumber();
			common.col = l.getColumnNumber();
			in.nextTag();
			common.geometry = parseExpression(in);
			in.nextTag();
		}
		if (in.getLocalName().equals("Description")) {
			parseDescription(in, common, l);
		}
		// in case of SLD 1.0.0:
		if (in.getLocalName().equals("Title")) {
			common.title = in.getElementText();
			in.nextTag();
		}
		if (in.getLocalName().equals("Abstract")) {
			common.abstract_ = in.getElementText();
			in.nextTag();
		}
	}

	private static void parseDescription(XMLStreamReader in, Common common, Location loc) throws XMLStreamException {
		while (!(in.isEndElement() && in.getLocalName().equals("Description"))) {
			in.nextTag();
			if (in.getLocalName().equals("Title")) {
				common.title = in.getElementText();
			}
			else if (in.getLocalName().equals("Abstract")) {
				common.abstract_ = in.getElementText();
			}
			else if (in.isStartElement()) {
				LOG.error("Found unknown element '{}' at line {}, column {}, skipping.",
						new Object[] { in.getLocalName(), loc.getLineNumber(), loc.getColumnNumber() });
				skipElement(in);
			}
		}
	}

	/**
	 * <code>Common</code>
	 *
	 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
	 */
	public static class Common {

		public Common() {
			// without location
		}

		Common(Location loc) {
			this.loc = loc.getSystemId();
			line = loc.getLineNumber();
			col = loc.getColumnNumber();
		}

		/***/
		public String name;

		/***/
		public String title;

		/***/
		public String abstract_;

		Expression geometry;

		String loc;

		int line, col;

	}

}
