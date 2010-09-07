//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/base/trunk/resources/eclipse/files_template.xml $
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

package org.deegree.protocol.wms;

import org.deegree.commons.tom.ows.Version;

/**
 * Important constants from the WMS specifications.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: schneider $
 * 
 * @version $Revision: $, $Date: $
 */
public class WMSConstants {

    /** Namespace for elements from the WMS specifications (>= 1.3.0) */
    public static final String WMS_NS = "http://www.opengis.net/wms";

    /** Common namespace prefix for elements from the WMS specification */
    public static final String WMS_PREFIX = "wms";

    /** WMS protocol version 1.0.0 */
    public static final Version VERSION_100 = Version.parseVersion( "1.0.0" );

    /** WMS protocol version 1.1.0 */
    public static final Version VERSION_110 = Version.parseVersion( "1.1.0" );

    /** WMS protocol version 1.1.1 */
    public static final Version VERSION_111 = Version.parseVersion( "1.1.1" );

    /** WMS protocol version 1.3.0 */
    public static final Version VERSION_130 = Version.parseVersion( "1.3.0" );

    /**
     * Enum type for discriminating between the different types of WebMapService (WMS) requests.
     * 
     * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
     * @author last edited by: $Author: schneider $
     * 
     * @version $Revision: $, $Date: $
     */
    public enum WMSRequestType {

        /** Retrieve the description for one more layers. */
        DescribeLayer,
        /** Retrieve the capabilities of the service. */
        GetCapabilities,
        /** Retrieve the capabilities of the service 1.0.0. */
        capabilities,
        /** Retrieve the feature information for a certain position. */
        GetFeatureInfo,
        /** Retrieve a map that consists of one or more layers. */
        GetMap,
        /** Retrieve a map that consists of one or more layers. 1.0.0 */
        map,
        /** Retrieve a XSD application schema for given layers. deegree specific request. */
        GetFeatureInfoSchema, /***/
        GetLegendGraphic, /** Invented request to output the WMS 1.1.1 DTD. */
        DTD

    }
}
