//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/base/trunk/resources/eclipse/files_template.xml $
/*----------------    FILE HEADER  ------------------------------------------
 This file is part of deegree.
 Copyright (C) 2001-2009 by:
 Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 Lesser General Public License for more details.
 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 Contact:

 Andreas Poth
 lat/lon GmbH
 Aennchenstr. 19
 53177 Bonn
 Germany
 E-Mail: poth@lat-lon.de

 Prof. Dr. Klaus Greve
 Department of Geography
 University of Bonn
 Meckenheimer Allee 166
 53115 Bonn
 Germany
 E-Mail: greve@giub.uni-bonn.de
 ---------------------------------------------------------------------------*/

package org.deegree.protocol.wfs;

import java.util.HashMap;
import java.util.Map;

import org.deegree.commons.types.ows.Version;
import org.deegree.protocol.i18n.Messages;

/**
 * Important constants from the WFS specifications.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: schneider $
 * 
 * @version $Revision: $, $Date: $
 */
public class WFSConstants {

    /** Namespace for elements from the WFS specification */
    public static final String WFS_NS = "http://www.opengis.net/wfs";

    /** Common namespace prefix for elements from the WFS specification */
    public static final String WFS_PREFIX = "wfs";

    /** WFS protocol version 1.0.0 */
    public static final Version VERSION_100 = Version.parseVersion( "1.0.0" );

    /** WFS protocol version 1.1.0 */
    public static final Version VERSION_110 = Version.parseVersion( "1.1.0" );

    /** WFS protocol version 2.0.0 */
    public static final Version VERSION_200 = Version.parseVersion( "2.0.0" );

    /** WFS request name 'DescribeFeatureType' */
    public static final String DESCRIBE_FEATURE_TYPE_NAME = "DescribeFeatureType";

    /** WFS request name 'GetCapabilities' */
    public static final String GET_CAPABILITIES_NAME = "GetCapabilities";

    /** WFS request name 'GetFeature' */
    public static final String GET_FEATURE_NAME = "GetFeature";

    /** WFS request name 'GetFeatureWithLock' */
    public static final String GET_FEATURE_WITH_LOCK_NAME = "GetFeatureWithLock";

    /** WFS request name 'GetGmlObject' */
    public static final String GET_GML_OBJECT_NAME = "GetGmlObject";

    /** WFS request name 'LockFeature' */
    public static final String LOCK_FEATURE_NAME = "LockFeature";

    /** WFS request name 'Transaction' */
    public static final String TRANSACTION_NAME = "Transaction";

    /** All request names of the WFS specifications. */
    public static final String[] REQUEST_NAMES = { DESCRIBE_FEATURE_TYPE_NAME, GET_CAPABILITIES_NAME, GET_FEATURE_NAME,

    GET_FEATURE_WITH_LOCK_NAME, GET_GML_OBJECT_NAME, LOCK_FEATURE_NAME, TRANSACTION_NAME };

    private static final Map<String, WFSRequestType> requestNameToWFSRequest = new HashMap<String, WFSRequestType>();

    static {
        requestNameToWFSRequest.put( DESCRIBE_FEATURE_TYPE_NAME, WFSRequestType.DESCRIBE_FEATURE_TYPE );
        requestNameToWFSRequest.put( GET_CAPABILITIES_NAME, WFSRequestType.GET_CAPABILITIES );
        requestNameToWFSRequest.put( GET_FEATURE_NAME, WFSRequestType.GET_FEATURE );
        requestNameToWFSRequest.put( GET_FEATURE_WITH_LOCK_NAME, WFSRequestType.GET_FEATURE_WITH_LOCK );
        requestNameToWFSRequest.put( GET_GML_OBJECT_NAME, WFSRequestType.GET_GML_OBJECT );
        requestNameToWFSRequest.put( LOCK_FEATURE_NAME, WFSRequestType.LOCK_FEATURE );
        requestNameToWFSRequest.put( TRANSACTION_NAME, WFSRequestType.TRANSACTION );
    }

    /**
     * Enum type for discriminating between the different types of WebFeatureService (WFS) requests.
     * 
     * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
     * @author last edited by: $Author: schneider $
     * 
     * @version $Revision: $, $Date: $
     */
    public enum WFSRequestType {

        /** Retrieve the data model (schema) for one or more feature types. */
        DESCRIBE_FEATURE_TYPE,
        /** Retrieve the capabilities of the service. */
        GET_CAPABILITIES,
        /** Query one or more feature types with optional filter expressions. */
        GET_FEATURE,
        /** Query and lock features. */
        GET_FEATURE_WITH_LOCK,
        /** Retrieve features and elements by ID (without surrounding FeatureCollection element). */
        GET_GML_OBJECT,
        /** Lock features that match a filter expression. */
        LOCK_FEATURE,
        /** Insert, update or delete features. */
        TRANSACTION
    }

    /**
     * Retrieves the corresponding {@link WFSRequestType} for a given WFS request name.
     * 
     * @param requestName
     *            name of the request (case-sensitive)
     * @return corresponding type from
     * @throws IllegalArgumentException
     *             if the given request name is not a known WFS request
     */
    public static WFSRequestType getRequestTypeByName( String requestName )
                            throws IllegalArgumentException {

        WFSRequestType requestType = requestNameToWFSRequest.get( requestName );
        if ( requestType == null ) {
            throw new IllegalArgumentException( Messages.get( "WFS_UNKNOWN_OPERATION", requestName ) );
        }
        return requestType;
    }
}
