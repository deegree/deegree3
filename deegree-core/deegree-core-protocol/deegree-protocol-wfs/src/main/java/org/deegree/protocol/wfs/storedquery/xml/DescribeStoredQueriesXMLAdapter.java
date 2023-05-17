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
package org.deegree.protocol.wfs.storedquery.xml;

import static org.deegree.protocol.wfs.WFSConstants.VERSION_200;

import org.deegree.commons.tom.ows.Version;
import org.deegree.commons.utils.kvp.InvalidParameterValueException;
import org.deegree.commons.xml.XPath;
import org.deegree.protocol.i18n.Messages;
import org.deegree.protocol.wfs.AbstractWFSRequestXMLAdapter;
import org.deegree.protocol.wfs.storedquery.DescribeStoredQueries;

/**
 * Adapter between XML <code>DescribeStoredQueries</code> requests and
 * {@link DescribeStoredQueries} objects.
 * <p>
 * Supported WFS versions:
 * <ul>
 * <li>2.0.0</li>
 * </ul>
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
public class DescribeStoredQueriesXMLAdapter extends AbstractWFSRequestXMLAdapter {

	/**
	 * Parses a WFS <code>DescribeStoredQueries</code> document into a
	 * {@link DescribeStoredQueries} request.
	 * @return parsed {@link DescribeStoredQueries} request, never <code>null</code>
	 * @throws InvalidParameterValueException if a parameter contains a syntax error
	 */
	public DescribeStoredQueries parse() throws InvalidParameterValueException {

		// <xsd:attribute name="version" type="xsd:string" use="required" fixed="2.0.0"/>
		Version version = Version.parseVersion(getRequiredNodeAsString(rootElement, new XPath("@version", nsContext)));
		if (!(VERSION_200.equals(version))) {
			String msg = Messages.get("UNSUPPORTED_VERSION", version, Version.getVersionsString(VERSION_200));
			throw new InvalidParameterValueException(msg);
		}

		// <xsd:attribute name="handle" type="xsd:string"/>
		String handle = getNodeAsString(rootElement, new XPath("@handle", nsContext), null);

		// <xsd:element name="StoredQueryId" type="xsd:anyURI" minOccurs="0"
		// maxOccurs="unbounded"/>
		String[] storedQueryIds = getNodesAsStrings(rootElement, new XPath("wfs200:StoredQueryId", nsContext));

		return new DescribeStoredQueries(version, handle, storedQueryIds);
	}

}
