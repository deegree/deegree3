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

package org.deegree.protocol.sos;

import java.util.HashMap;
import java.util.Map;

import org.deegree.commons.types.ows.Version;
import org.deegree.protocol.i18n.Messages;

/**
 * Important constants from the SOS specifications.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: schneider $
 *
 * @version $Revision: $, $Date: $
 */
public class SOSConstants {

    /** Namespace for elements from the SOS 1.0.0 specification */
    public static final String SOS_100_NS = "http://www.opengis.net/sos/1.0";

    /** Common namespace prefix for elements from the SOS specification */
    public static final String SOS_PREFIX = "sos";

    /** SOS protocol version 1.0.0 */
    public static final Version VERSION_100 = Version.parseVersion( "1.0.0" );

    /** SOS request name 'DescribeSensor' */
    public static final String DESCRIBE_SENSOR_NAME = "DescribeSensor";

    /** SOS request name 'GetCapabilities' */
    public static final String GET_CAPABILITIES_NAME = "GetCapabilities";

    /** SOS request name 'GetObservation' */
    public static final String GET_OBSERVATION_NAME = "GetObservation";

    /** All request names of the SOS specifications. */
    public static final String[] REQUEST_NAMES = { DESCRIBE_SENSOR_NAME, GET_CAPABILITIES_NAME, GET_OBSERVATION_NAME };

    private static final Map<String, SOSRequestType> requestNameToSOSRequest = new HashMap<String, SOSRequestType>();

    /**
     * Enum type for discriminating between the different types of SensorObservationService (SOS) requests.
     *
     * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
     * @author last edited by: $Author: schneider $
     *
     * @version $Revision: $, $Date: $
     */
    public enum SOSRequestType {

        /** Retrieve information on one or more sensors. */
        DESCRIBE_SENSOR,
        /** Retrieve the capabilities of the service. */
        GET_CAPABILITIES,
        /** Retrieve an observation. */
        GET_OBSERVATION
    }

    /**
     * Retrieves the corresponding {@link SOSRequestType} for a given SOS request name.
     *
     * @param requestName
     *            name of the request (case-sensitive)
     * @return corresponding type from
     * @throws IllegalArgumentException
     *             if the given request name is not a known SOS request
     */
    public static SOSRequestType getRequestTypeByName( String requestName )
                            throws IllegalArgumentException {

        SOSRequestType requestType = requestNameToSOSRequest.get( requestName );
        if ( requestType == null ) {
            throw new IllegalArgumentException( Messages.get( "SOS_UNKNOWN_OPERATION", requestName ) );
        }
        return requestType;
    }
}
