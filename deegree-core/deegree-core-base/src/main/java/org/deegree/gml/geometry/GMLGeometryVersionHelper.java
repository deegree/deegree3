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
package org.deegree.gml.geometry;

import static org.deegree.commons.xml.CommonNamespaces.GML3_2_NS;
import static org.deegree.commons.xml.CommonNamespaces.GMLNS;
import static org.deegree.gml.GMLVersion.GML_2;
import static org.deegree.gml.GMLVersion.GML_31;
import static org.deegree.gml.GMLVersion.GML_32;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.deegree.gml.GMLInputFactory;
import org.deegree.gml.GMLVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO add class documentation here
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
public class GMLGeometryVersionHelper {

	private static Logger LOG = LoggerFactory.getLogger(GMLGeometryVersionHelper.class);

	public static GMLGeometryReader getGeometryReader(QName elName, XMLStreamReader xmlStream)
			throws XMLStreamException {

		String ns = elName.getNamespaceURI();
		GMLVersion gmlVersion = null;
		if (GMLNS.equals(ns)) {
			if ("Box".equals(elName.getLocalPart())) {
				gmlVersion = GML_2;
			}
			else {
				gmlVersion = GML_31;
			}
		}
		else if (GML3_2_NS.equals(ns)) {
			gmlVersion = GML_32;
		}
		else {
			LOG.warn("Unable to determine GML version for element: " + elName + ". Falling back to GML 2.");
			gmlVersion = GML_2;
		}
		return GMLInputFactory.createGMLStreamReader(gmlVersion, xmlStream).getGeometryReader();
	}

}
