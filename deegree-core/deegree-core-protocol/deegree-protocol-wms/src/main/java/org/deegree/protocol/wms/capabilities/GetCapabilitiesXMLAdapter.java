/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
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
package org.deegree.protocol.wms.capabilities;

import static org.deegree.protocol.wms.WMSConstants.VERSION_130;

import org.deegree.commons.tom.ows.Version;
import org.deegree.commons.xml.CommonNamespaces;
import org.deegree.protocol.ows.getcapabilities.GetCapabilities;
import org.deegree.protocol.ows.getcapabilities.GetCapabilitiesXMLParser;

/**
 * Adapter between XML encoded <code>GetCapabilities</code> requests (WMS) and
 * {@link GetCapabilities} objects.
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 */
public class GetCapabilitiesXMLAdapter extends GetCapabilitiesXMLParser {

	/**
	 * Parses a WMS <code>GetCapabilities</code> document into a {@link GetCapabilities}
	 * request.
	 * <p>
	 * Supported versions:
	 * <ul>
	 * <li>1.3.0 (OWS 2.0.0)</li>
	 * </ul>
	 * @param version specifies the request version, may be <code>null</code> (version
	 * attribute is evaluated then)
	 * @return parsed {@link GetCapabilities} request, never <code>null</code>
	 * @throws IllegalArgumentException if version is not supported
	 */
	public GetCapabilities parse(Version version) {
		Version wmsVersion = detectVersion(version);
		if (VERSION_130.equals(wmsVersion))
			return parse200();
		throw new IllegalArgumentException("Cannot parse Caapbilities request: Unsupported Version");

	}

	private Version detectVersion(Version version) {
		if (version != null)
			return version;
		else if (CommonNamespaces.OWS_20_NS.equals(getRootElement().getNamespace().getNamespaceURI()))
			return VERSION_130;
		return null;
	}

}