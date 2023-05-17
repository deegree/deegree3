/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
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

 Occam Labs UG (haftungsbeschränkt)
 Godesberger Allee 139, 53175 Bonn
 Germany
 http://www.occamlabs.de/

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.protocol.wmts;

import org.deegree.commons.tom.ows.Version;

/**
 * <code>WMTSConstants</code>
 *
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 */

public class WMTSConstants {

	/** Namespace for elements from WMTS specification 1.0.0 */
	public static final String WMTS_100_NS = "http://www.opengis.net/wmts/1.0";

	/** Common namespace prefix for elements from WMTS specification */
	public static final String WMTS_PREFIX = "wmts";

	/** WMTS protocol version 1.0.0 */
	public static final Version VERSION_100 = Version.parseVersion("1.0.0");

	/**
	 * <code>WMTSRequestType</code>
	 *
	 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
	 */
	public static enum WMTSRequestType {

		GetCapabilities, GetTile, GetFeatureInfo

	}

}
