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

import java.util.HashMap;
import java.util.Map;

import org.deegree.commons.types.ows.Version;
import org.deegree.protocol.i18n.Messages;

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

    /** WMS request name 'capabilities' (old variant of 'GetCapabilities') */
    public static final String CAPABILITIES_NAME = "capabilities";

    /** WMS request name 'map' (old variant of 'GetMap') */
    public static final String MAP_NAME = "map";

    /** WMS request name 'feature_info' (old variant of 'GetFeatureInfo') */
    public static final String FEATURE_INFO_NAME = "feature_info";

    /** WMS request name 'DescribeLayer' */
    public static final String DESCRIBE_LAYER_NAME = "DescribeLayer";

    /** WMS request name 'GetCapabilities' */
    public static final String GET_CAPABILITIES_NAME = "GetCapabilities";

    /** WMS request name 'GetFeatureInfo' */
    public static final String GET_FEATURE_INFO_NAME = "GetFeatureInfo";

    /** WMS request name 'GetMap' */
    public static final String GET_MAP_NAME = "GetMap";

    /** All request names of the WMS specifications. */
    public static final String[] REQUEST_NAMES = { CAPABILITIES_NAME, MAP_NAME, FEATURE_INFO_NAME, DESCRIBE_LAYER_NAME,
                                                  GET_CAPABILITIES_NAME, GET_FEATURE_INFO_NAME, GET_MAP_NAME };

    private static final Map<String, WMSRequestType> requestNameToWMSRequest = new HashMap<String, WMSRequestType>();

    private static final Map<WMSRequestType, String> requestTypeToWMSRequestName100 = new HashMap<WMSRequestType, String>();

    private static final Map<WMSRequestType, String> requestTypeToWMSRequestName110 = new HashMap<WMSRequestType, String>();

    static {
        requestNameToWMSRequest.put( CAPABILITIES_NAME, WMSRequestType.GET_CAPABILITIES );
        requestNameToWMSRequest.put( GET_CAPABILITIES_NAME, WMSRequestType.GET_CAPABILITIES );
        requestNameToWMSRequest.put( MAP_NAME, WMSRequestType.GET_MAP );
        requestNameToWMSRequest.put( GET_MAP_NAME, WMSRequestType.GET_MAP );
        requestNameToWMSRequest.put( FEATURE_INFO_NAME, WMSRequestType.GET_FEATURE_INFO );
        requestNameToWMSRequest.put( GET_FEATURE_INFO_NAME, WMSRequestType.GET_FEATURE_INFO );
        requestNameToWMSRequest.put( DESCRIBE_LAYER_NAME, WMSRequestType.DESCRIBE_LAYER );

        requestTypeToWMSRequestName100.put( WMSRequestType.GET_CAPABILITIES, CAPABILITIES_NAME );
        requestTypeToWMSRequestName100.put( WMSRequestType.GET_FEATURE_INFO, FEATURE_INFO_NAME );
        requestTypeToWMSRequestName100.put( WMSRequestType.GET_MAP, MAP_NAME );

        requestTypeToWMSRequestName110.put( WMSRequestType.DESCRIBE_LAYER, DESCRIBE_LAYER_NAME);
        requestTypeToWMSRequestName110.put( WMSRequestType.GET_CAPABILITIES, GET_CAPABILITIES_NAME );
        requestTypeToWMSRequestName110.put( WMSRequestType.GET_FEATURE_INFO, GET_FEATURE_INFO_NAME );
        requestTypeToWMSRequestName110.put( WMSRequestType.GET_MAP, GET_MAP_NAME );
    }

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
        DESCRIBE_LAYER,
        /** Retrieve the capabilities of the service. */
        GET_CAPABILITIES,
        /** Retrieve the feature information for a certain position. */
        GET_FEATURE_INFO,
        /** Retrieve a map that consists of one or more layers. */
        GET_MAP,
    }

    /**
     * Retrieves the corresponding {@link WMSRequestType} for a given WMS request name.
     *
     * @param requestName
     *            name of the request (case-sensitive)
     * @return corresponding type from
     * @throws IllegalArgumentException
     *             if the given request name is not a known WMS request
     */
    public static WMSRequestType getRequestTypeByName( String requestName )
                            throws IllegalArgumentException {

        WMSRequestType requestType = requestNameToWMSRequest.get( requestName );
        if ( requestType == null ) {
            throw new IllegalArgumentException( Messages.get( "WMS_UNKNOWN_OPERATION", requestName ) );
        }
        return requestType;
    }

    /**
     * Retrieves the corresponding WMS request name for a {@link WMSRequestType}.
     *
     * @param request
     *            request type
     * @param is100
     *            set to true if names if 1.0.0 protocol should be returned, false for any other version
     * @return corresponding name
     */
    public static String getRequestNameByType( WMSRequestType request, boolean is100 ) {

        String requestName = null;
        if (is100) {
            requestName = requestTypeToWMSRequestName100.get(request);
            if (requestName == null) {
                throw new IllegalArgumentException( Messages.get( "WMS_NO_100_OPERATION" ) );
            }
        } else {
            requestName = requestTypeToWMSRequestName110.get(request);
        }
        return requestName;
    }
}
