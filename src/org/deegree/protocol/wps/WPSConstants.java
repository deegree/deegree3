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

package org.deegree.protocol.wps;

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
public class WPSConstants {

    /** Namespace for elements from the WPS 1.0.0 specification */
    public static final String WPS_100_NS = "http://www.opengis.net/wps/1.0.0";

    /** Common namespace prefix for elements from the WPS specification */
    public static final String WPS_PREFIX = "wps";    
    
    /** WPS protocol version 1.0.0 */
    public static final Version VERSION_100 = Version.parseVersion( "1.0.0" );

    private static final String DESCRIBE_PROCESS_NAME = "DescribeProcess";

    private static final String GET_CAPABILITIES_NAME = "GetCapabilities";

    private static final String EXECUTE_NAME = "Execute";

    private static final String DEEGREEWPS_GET_OUTPUT_NAME = "GetOutput";

    private static final String DEEGREEWPS_GET_RESPONSE_DOCUMENT_NAME = "GetResponseDocument";

    private static final String DEEGREEWPS_GET_WPS_WSDL_NAME = "GetWPSWSDL";

    /** All request names of the WPS specifications. */
    public static final String[] REQUEST_NAMES = { DESCRIBE_PROCESS_NAME, GET_CAPABILITIES_NAME, EXECUTE_NAME };

    /** All request names of the WPS specifications + deegree WPS specific ones. */
    public static final String[] DEEGREE_WPS_REQUEST_NAMES = { DESCRIBE_PROCESS_NAME, GET_CAPABILITIES_NAME,
                                                              EXECUTE_NAME, DEEGREEWPS_GET_OUTPUT_NAME,
                                                              DEEGREEWPS_GET_RESPONSE_DOCUMENT_NAME,
                                                              DEEGREEWPS_GET_WPS_WSDL_NAME };

    private static final Map<String, WPSRequestType> requestNameToWPSRequest = new HashMap<String, WPSRequestType>();

    static {
        requestNameToWPSRequest.put( DESCRIBE_PROCESS_NAME, WPSRequestType.DESCRIBE_PROCESS );
        requestNameToWPSRequest.put( GET_CAPABILITIES_NAME, WPSRequestType.GET_CAPABILITIES );
        requestNameToWPSRequest.put( EXECUTE_NAME, WPSRequestType.EXECUTE );
        requestNameToWPSRequest.put( DEEGREEWPS_GET_OUTPUT_NAME, WPSRequestType.DEEGREEWPS_GET_OUTPUT );
        requestNameToWPSRequest.put( DEEGREEWPS_GET_RESPONSE_DOCUMENT_NAME, WPSRequestType.DEEGREEWPS_GET_RESPONSE_DOCUMENT );
        requestNameToWPSRequest.put( DEEGREEWPS_GET_WPS_WSDL_NAME, WPSRequestType.DEEGREEWPS_GET_WPS_WSDL );
    }

    /**
     * Enum type for discriminating between the different types of WebProcessingService (WPS) requests.
     * 
     * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
     * @author last edited by: $Author: schneider $
     * 
     * @version $Revision: $, $Date: $
     */
    public enum WPSRequestType {

        /** Retrieve the process description for one more processes. */
        DESCRIBE_PROCESS,
        /** Retrieve the capabilities of the service. */
        GET_CAPABILITIES,
        /** Execute a process. */
        EXECUTE,
        /** deegree WPS specific request for retrieving stored complex outputs. */
        DEEGREEWPS_GET_OUTPUT,
        /** deegree WPS specific request for retrieving response documents. */
        DEEGREEWPS_GET_RESPONSE_DOCUMENT,
        /** deegree WPS specific request for retrieving WSDL documents for the full service or single processes. */
        DEEGREEWPS_GET_WPS_WSDL,
    }

    /**
     * Retrieves the corresponding {@link WPSRequestType} for a given WPS request name.
     * 
     * @param requestName
     *            name of the request (case-sensitive)
     * @return corresponding type from
     * @throws IllegalArgumentException
     *             if the given request name is not a known WPS request
     */
    public static WPSRequestType getRequestTypeByName( String requestName )
                            throws IllegalArgumentException {

        WPSRequestType requestType = requestNameToWPSRequest.get( requestName );
        if ( requestType == null ) {
            throw new IllegalArgumentException( Messages.get( "WPS_UNKNOWN_OPERATION", requestName ) );
        }
        return requestType;
    }
}
