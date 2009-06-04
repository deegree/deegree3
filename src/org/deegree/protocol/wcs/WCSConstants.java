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

package org.deegree.protocol.wcs;

import java.util.HashMap;
import java.util.Map;

import org.deegree.commons.types.ows.Version;
import org.deegree.protocol.i18n.Messages;

/**
 * Important constants from the WCS specifications.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: schneider $
 * 
 * @version $Revision: $, $Date: $
 */
public class WCSConstants {

    /** Namespace for elements from the WCS 1.0.0 specification */
    public static final String WCS_100_NS = "http://www.opengis.net/wcs";    
    
    /** Namespace for elements from the WCS 1.1.0 specification */
    public static final String WCS_110_NS = "http://www.opengis.net/wcs/1.1";

    /** Common namespace prefix for elements from WCS specifications */
    public static final String WCS_PREFIX = "wcs";

    /** WCS protocol version 1.0.0 */
    public static final Version VERSION_100 = Version.parseVersion( "1.0.0" );
    
    /** WCS protocol version 1.1.0 */
    public static final Version VERSION_110 = Version.parseVersion( "1.1.0" );    

    /** WCS request name 'DescribeCoverage' */
    public static final String DESCRIBE_COVERAGE_NAME = "DescribeCoverage";

    /** WCS request name 'GetCapabilities' */
    public static final String GET_CAPABILITIES_NAME = "GetCapabilities";

    /** WCS request name 'GetCoverage' */
    public static final String GET_COVERAGE_NAME = "GetCoverage";

    /** All request names of the WCS specifications. */
    public static final String[] REQUEST_NAMES = { DESCRIBE_COVERAGE_NAME, GET_CAPABILITIES_NAME, GET_COVERAGE_NAME };

    private static final Map<String, WCSRequestType> requestNameToWCSRequest = new HashMap<String, WCSRequestType>();

    static {
        requestNameToWCSRequest.put( DESCRIBE_COVERAGE_NAME, WCSRequestType.DESCRIBE_COVERAGE );
        requestNameToWCSRequest.put( GET_CAPABILITIES_NAME, WCSRequestType.GET_CAPABILITIES );
        requestNameToWCSRequest.put( GET_COVERAGE_NAME, WCSRequestType.GET_COVERAGE );
    }    
    
    /**
     * Enum type for discriminating between the different types of WebCoverageService (WCS) requests.
     * 
     * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
     * @author last edited by: $Author: schneider $
     * 
     * @version $Revision: $, $Date: $
     */
    public enum WCSRequestType {

        /** Describe a coverage. */
        DESCRIBE_COVERAGE,
        /** Retrieve the capabilities of the service. */
        GET_CAPABILITIES,
        /** Retrieve a coverage for a certain region. */
        GET_COVERAGE
    }

    /**
     * Retrieves the corresponding {@link WCSRequestType} for a given WCS request name.
     * 
     * @param requestName
     *            name of the request (case-sensitive)
     * @return corresponding type from
     * @throws IllegalArgumentException
     *             if the given request name is not a known WCS request
     */
    public static WCSRequestType getRequestTypeByName( String requestName )
                            throws IllegalArgumentException {

        WCSRequestType requestType = requestNameToWCSRequest.get( requestName );
        if ( requestType == null ) {
            throw new IllegalArgumentException( Messages.get( "WCS_UNKNOWN_OPERATION", requestName ) );
        }
        return requestType;
    }
}
