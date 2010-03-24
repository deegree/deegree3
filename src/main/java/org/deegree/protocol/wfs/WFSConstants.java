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

package org.deegree.protocol.wfs;

import org.deegree.commons.tom.ows.Version;

/**
 * Important constants from the WFS specifications.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: schneider $
 * 
 * @version $Revision: $, $Date: $
 */
public class WFSConstants {

    /** Namespace for elements from the WFS specification 1.0.0 and 1.1.0 */
    public static final String WFS_NS = "http://www.opengis.net/wfs";

    /** Namespace for elements from the WFS specification 2.0.0 */
    public static final String WFS_200_NS = "http://www.opengis.net/wfs/2.0";

    /** Common namespace prefix for elements from the WFS specification */
    public static final String WFS_PREFIX = "wfs";

    /** WFS protocol version 1.0.0 */
    public static final Version VERSION_100 = Version.parseVersion( "1.0.0" );

    /** WFS protocol version 1.1.0 */
    public static final Version VERSION_110 = Version.parseVersion( "1.1.0" );

    /** WFS protocol version 2.0.0 */
    public static final Version VERSION_200 = Version.parseVersion( "2.0.0" );
    
    /** URL of the WFS 1.0.0 basic schema*/
    public static final String WFS_100_BASIC_SCHEMA_URL = "http://schemas.opengis.net/wfs/1.0.0/WFS-basic.xsd";

    /** URL of the WFS 1.0.0 capabilities schema */
    public static final String WFS_100_CAPABILITIES_SCHEMA_URL = "http://schemas.opengis.net/wfs/1.0.0/WFS-capabilities.xsd";

    /** URL of the WFS 1.0.0 transaction schema */
    public static final String WFS_100_TRANSACTION_URL = "http://schemas.opengis.net/wfs/1.0.0/WFS-transaction.xsd";

    /** URL of the WFS 1.1.0 schema */
    public static final String WFS_110_SCHEMA_URL = "http://schemas.opengis.net/wfs/1.1.0/wfs.xsd";

    /** URL of the WFS 2.0.0 schema */
    public static final String WFS_200_SCHEMA_URL = "http://schemas.opengis.net/wfs/2.0.0/wfs.xsd";    

    /**
     * Enum type for discriminating between the different types of WebFeatureService (WFS) requests.
     * 
     * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
     * @author last edited by: $Author: schneider $
     * 
     * @version $Revision: $, $Date: $
     */
    public enum WFSRequestType {

        /** Retrieve the capabilities of the service.(1.0.0, 1.1.0, 2.0.0) */
        GetCapabilities,
        /** Retrieve the data model (schema) for one or more feature types.(1.0.0, 1.1.0, 2.0.0) */
        DescribeFeatureType,
        /** Query one or more feature types with optional filter expressions.(1.0.0, 1.1.0, 2.0.0) */
        GetFeature,
        /** Insert, update or delete features.(1.0.0, 1.1.0, 2.0.0) */
        Transaction,
        /** Query and lock features.(1.1.0 and 2.0.0) */
        GetFeatureWithLock,
        /** Retrieve features and elements by ID.(1.1.0) */
        GetGmlObject,
        /** Lock features that match a filter expression. */
        LockFeature,
        /** Retrieve the values of selected feature properties based on query constraints.(2.0.0) */
        GetPropertyValue,
        /** Define persistent parametrized query expressions. (2.0.0) */
        CreateStoredQuery,
        /** Drop a stored query from the service.(2.0.0) */
        DropStoredQuery,
        /** Retrieve a list of stored queries offered by a service.(2.0.0) */
        ListStoredQueries,
        /** Retrieve a description of a stored query expresssion. (2.0.0) */
        DescribeStoredQueries
    }
}
