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

package org.deegree.protocol.wpvs;

import java.util.HashMap;
import java.util.Map;

import org.deegree.commons.types.ows.Version;
import org.deegree.protocol.i18n.Messages;

/**
 * Important constants from the WPVS specifications.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: schneider $
 *
 * @version $Revision: $, $Date: $
 */
public class WPVSConstants {

    /** Namespace for elements from the WPVS specification */
    public static final String WPVS_NS = "http://www.opengis.net/wpvs";

    /** Common namespace prefix for elements from the WMS specification */
    public static final String WMS_PREFIX = "wpvs";

    /** WPVS protocol version 0.4.0 */
    public static final Version VERSION_040 = Version.parseVersion( "0.4.0" );

    /** WPVS protocol version 1.0.0 */
    public static final Version VERSION_100 = Version.parseVersion( "1.0.0" );

    /** WPVS request name 'GetCapabilities' */
    public static final String GET_CAPABILITIES_NAME = "GetCapabilities";

    /** WPVS request name 'GetDescription' */
    public static final String GET_DESCRIPTION_NAME = "GetDescription";

    /** WPVS request name 'GetLegendGraphic' */
    public static final String GET_LEGEND_GRAPHIC_NAME = "GetLegendGraphic";

    /** WPVS request name 'GetView' */
    public static final String GET_VIEW_NAME = "GetView";

    /** All request names of the WPVS specifications. */
    public static final String[] REQUEST_NAMES = { GET_CAPABILITIES_NAME, GET_DESCRIPTION_NAME,
                                                  GET_LEGEND_GRAPHIC_NAME, GET_VIEW_NAME };

    private static final Map<String, WPVSRequestType> requestNameToWPVSRequest = new HashMap<String, WPVSRequestType>();

    static {
        requestNameToWPVSRequest.put( GET_CAPABILITIES_NAME, WPVSRequestType.GET_CAPABILITIES );
        requestNameToWPVSRequest.put( GET_DESCRIPTION_NAME, WPVSRequestType.GET_DESCRIPTION );
        requestNameToWPVSRequest.put( GET_LEGEND_GRAPHIC_NAME, WPVSRequestType.GET_LEGEND_GRAPHIC );
        requestNameToWPVSRequest.put( GET_VIEW_NAME, WPVSRequestType.GET_VIEW );
    }

    /**
     * Enum type for discriminating between the different types of WebPerspectiveView (WPVS) requests.
     *
     * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
     * @author last edited by: $Author: schneider $
     *
     * @version $Revision: $, $Date: $
     */
    public enum WPVSRequestType {
        /** Retrieve the capabilities of the service. */
        GET_CAPABILITIES,
        /** Retrieve the descriptions of one or more datasets or styles. */
        GET_DESCRIPTION,
        /** Retrieve a graphic containing a map legend for an identified dataset and style. */
        GET_LEGEND_GRAPHIC,
        /** Retrieve a perspective view of a certain point-of-interest. */
        GET_VIEW,
    }

    /**
     * Retrieves the corresponding {@link WPVSRequestType} for a given WPVS request name.
     *
     * @param requestName
     *            name of the request (case-sensitive)
     * @return corresponding type from
     * @throws IllegalArgumentException
     *             if the given request name is not a known WPVS request
     */
    public static WPVSRequestType getRequestTypeByName( String requestName )
                            throws IllegalArgumentException {

        WPVSRequestType requestType = requestNameToWPVSRequest.get( requestName );
        if ( requestType == null ) {
            throw new IllegalArgumentException( Messages.get( "WPVS_UNKNOWN_OPERATION", requestName ) );
        }
        return requestType;
    }
}
