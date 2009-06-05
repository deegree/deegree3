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

    /** WFS request name 'GetCapabilities' (1.0.0, 1.1.0 and 2.0.0) */
    public static final String GET_CAPABILITIES_NAME = "GetCapabilities";

    /** WFS request name 'DescribeFeatureType' (1.0.0, 1.1.0 and 2.0.0) */
    public static final String DESCRIBE_FEATURE_TYPE_NAME = "DescribeFeatureType";

    /** WFS request name 'GetFeature' (1.0.0, 1.1.0 and 2.0.0) */
    public static final String GET_FEATURE_NAME = "GetFeature";

    /** WFS request name 'Transaction' (1.0.0, 1.1.0, 2.0.0) */
    public static final String TRANSACTION_NAME = "Transaction";

    /** WFS request name 'LockFeature' (1.0.0, 1.1.0, 2.0.0) */
    public static final String LOCK_FEATURE_NAME = "LockFeature";

    /** WFS request name 'GetGmlObject' (1.1.0) */
    public static final String GET_GML_OBJECT_NAME = "GetGmlObject";

    /** WFS request name 'GetFeatureWithLock' (1.1.0 and 2.0.0) */
    public static final String GET_FEATURE_WITH_LOCK_NAME = "GetFeatureWithLock";

    /** WFS request name 'GetPropertyValue' (2.0.0) */
    public static final String GET_PROPERTY_VALUE_NAME = "GetPropertyValue";

    /** WFS request name 'CreateStoredQuery' (2.0.0) */
    public static final String CREATE_STORED_QUERY_NAME = "CreateStoredQuery";

    /** WFS request name 'DropStoredQuery' (2.0.0) */
    public static final String DROP_STORED_QUERY_NAME = "DropStoredQuery";

    /** WFS request name 'ListStoredQueries' (2.0.0) */
    public static final String LIST_STORED_QUERIES_NAME = "ListStoredQueries";

    /** WFS request name 'DescribeStoredQueries' (2.0.0) */
    public static final String DESCRIBE_STORED_QUERIES_NAME = "DescribeStoredQueries";

    /** All request names from all WFS specifications. */
    public static final String[] REQUEST_NAMES = { GET_CAPABILITIES_NAME, DESCRIBE_FEATURE_TYPE_NAME, GET_FEATURE_NAME,
                                                  TRANSACTION_NAME, LOCK_FEATURE_NAME, GET_GML_OBJECT_NAME,
                                                  GET_FEATURE_WITH_LOCK_NAME, GET_PROPERTY_VALUE_NAME,
                                                  CREATE_STORED_QUERY_NAME, DROP_STORED_QUERY_NAME,
                                                  LIST_STORED_QUERIES_NAME, DESCRIBE_STORED_QUERIES_NAME };

    private static final Map<String, WFSRequestType> requestNameToWFSRequest = new HashMap<String, WFSRequestType>();

    static {
        requestNameToWFSRequest.put( GET_CAPABILITIES_NAME, WFSRequestType.GET_CAPABILITIES );
        requestNameToWFSRequest.put( DESCRIBE_FEATURE_TYPE_NAME, WFSRequestType.DESCRIBE_FEATURE_TYPE );
        requestNameToWFSRequest.put( GET_FEATURE_NAME, WFSRequestType.GET_FEATURE );
        requestNameToWFSRequest.put( TRANSACTION_NAME, WFSRequestType.TRANSACTION );        
        requestNameToWFSRequest.put( GET_FEATURE_WITH_LOCK_NAME, WFSRequestType.GET_FEATURE_WITH_LOCK );
        requestNameToWFSRequest.put( GET_GML_OBJECT_NAME, WFSRequestType.GET_GML_OBJECT );
        requestNameToWFSRequest.put( LOCK_FEATURE_NAME, WFSRequestType.LOCK_FEATURE );
        requestNameToWFSRequest.put( GET_PROPERTY_VALUE_NAME, WFSRequestType.GET_PROPERTY_VALUE);
        requestNameToWFSRequest.put( CREATE_STORED_QUERY_NAME, WFSRequestType.CREATE_STORED_QUERY);
        requestNameToWFSRequest.put( DROP_STORED_QUERY_NAME, WFSRequestType.DROP_STORED_QUERY);
        requestNameToWFSRequest.put( LIST_STORED_QUERIES_NAME, WFSRequestType.LIST_STORED_QUERIES);
        requestNameToWFSRequest.put( DESCRIBE_STORED_QUERIES_NAME, WFSRequestType.DESCRIBE_STORED_QUERIES);
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

        /** Retrieve the capabilities of the service. */
        GET_CAPABILITIES,
        /** Retrieve the data model (schema) for one or more feature types. */
        DESCRIBE_FEATURE_TYPE,
        /** Query one or more feature types with optional filter expressions. */
        GET_FEATURE,
        /** Insert, update or delete features. */
        TRANSACTION,
        /** Query and lock features. */
        GET_FEATURE_WITH_LOCK,
        /** Retrieve features and elements by ID. */
        GET_GML_OBJECT,
        /** Lock features that match a filter expression. */
        LOCK_FEATURE,
        /** Retrieve the values of selected feature properties based on query constraints. */
        GET_PROPERTY_VALUE,
        /** Define persistent parametrized query expressions. */
        CREATE_STORED_QUERY,
        /** Drop a stored query from the service. */
        DROP_STORED_QUERY,
        /** Retrieve a list of stored queries offered by a service. */
        LIST_STORED_QUERIES,
        /** Retrieve a description of a stored query expresssion. */
        DESCRIBE_STORED_QUERIES
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
