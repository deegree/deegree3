/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
 Department of Geography, University of Bonn
 and
 lat/lon GmbH

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
package org.deegree.filter.function;

import static org.deegree.commons.xml.CommonNamespaces.GML3_2_NS;
import static org.deegree.commons.xml.CommonNamespaces.GMLNS;
import static org.deegree.commons.xml.CommonNamespaces.XSNS;
import static org.deegree.gml.GMLVersion.GML_2;
import static org.deegree.gml.GMLVersion.GML_30;
import static org.deegree.gml.GMLVersion.GML_31;
import static org.deegree.gml.GMLVersion.GML_32;

import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import org.deegree.filter.expression.Function;
import org.deegree.gml.GMLVersion;

/**
 * Provides GML/XSD type information for an input/output parameter of a {@link Function}.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
public class ParameterType {

	public static final ParameterType GEOMETRY = new ParameterType(new QName(GMLNS, "_Geometry", "gml"),
			new QName(GMLNS, "_Geometry", "gml"), new QName(GMLNS, "_Geometry", "gml"),
			new QName(GML3_2_NS, "AbstractGeometry", "gml32"));

	public static final ParameterType POINT = new ParameterType(new QName(GMLNS, "Point", "gml"),
			new QName(GMLNS, "Point", "gml"), new QName(GMLNS, "Point", "gml"), new QName(GML3_2_NS, "Point", "gml32"));

	public static final ParameterType STRING = new ParameterType(new QName(XSNS, "string", "xsd"));

	public static final ParameterType DOUBLE = new ParameterType(new QName(XSNS, "double", "xsd"));

	public static final ParameterType INTEGER = new ParameterType(new QName(XSNS, "integer", "xsd"));

	public static final ParameterType BOOLEAN = new ParameterType(new QName(XSNS, "boolean", "xsd"));

	public static final ParameterType ANYTYPE = new ParameterType(new QName(XSNS, "anyType", "xsd"));

	private final Map<GMLVersion, QName> versionToName = new HashMap<GMLVersion, QName>();

	public ParameterType(QName name) {
		versionToName.put(GML_2, name);
		versionToName.put(GML_30, name);
		versionToName.put(GML_31, name);
		versionToName.put(GML_32, name);
	}

	public ParameterType(QName gml2Name, QName gml30Name, QName gml31Name, QName gml32Name) {
		versionToName.put(GML_2, gml2Name);
		versionToName.put(GML_30, gml30Name);
		versionToName.put(GML_31, gml31Name);
		versionToName.put(GML_32, gml32Name);
	}

	/**
	 * Returns the qualified name of the schema type.
	 * @param version GML version, must not be <code>null</code>
	 * @return qualified name, never <code>null</code>
	 */
	public QName getType(GMLVersion version) {
		return versionToName.get(version);
	}

}
