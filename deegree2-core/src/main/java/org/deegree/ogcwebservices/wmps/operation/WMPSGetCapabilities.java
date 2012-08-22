//$HeadURL$
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
package org.deegree.ogcwebservices.wmps.operation;

import java.util.Map;

import org.deegree.ogcwebservices.InconsistentRequestException;
import org.deegree.ogcwebservices.getcapabilities.GetCapabilities;

/**
 * This interface desribes the access to the parameters common to a OGC GetCapabilities request. It
 * inherits three accessor methods from the general OGC web service request interface.
 *
 * <p>
 * --------------------------------------------------------
 * </p>
 *
 * @author <a href="mailto:deshmukh@lat-lon.de">Anup Deshmukh</a>
 * @version 2.0
 */
public class WMPSGetCapabilities extends GetCapabilities {

    private static final long serialVersionUID = -609973973617914526L;

    /**
     * creates an WMPS GetCapabilities Request
     *
     * @param paramMap
     *            the parameters of the request
     * @return the GetCapabilities request
     * @throws InconsistentRequestException
     *             if the request is inconsistent
     */
    public static WMPSGetCapabilities create( Map<String,String> paramMap )
                            throws InconsistentRequestException {

        String version = paramMap.remove( "VERSION" );

        if ( version == null ) {
            version = paramMap.remove( "WMTVER" );
        }

        String service = paramMap.remove( "SERVICE" );
        String updateSequence = paramMap.remove( "UPDATESEQUENCE" );

        if ( service == null ) {
            throw new InconsistentRequestException( "Required parameter 'SERVICE' is missing." );
        }

        if ( !service.equals( "WMPS" ) ) {
            throw new InconsistentRequestException( "Parameter 'SERVICE' must be 'WMPS'." );
        }

        return new WMPSGetCapabilities( paramMap.remove( "ID" ), version, updateSequence,
                                        paramMap );
    }

    /**
     * Creates a new WMPSGetCapabilities object.
     *
     * @param version
     * @param id
     * @param updateSequence
     * @param vendorSpecificParameter
     */
    WMPSGetCapabilities( String version, String id, String updateSequence,
                         Map<String, String> vendorSpecificParameter ) {
        super( version, id, updateSequence, null, null, null, vendorSpecificParameter );
    }

    /**
     * returns the URI of a HTTP GET request.
     *
     * @return String
     */
    @Override
    public String getRequestParameter() {
        if ( getVersion().equals( "1.0.0" ) ) {
            return "service=WMPS&version=" + getVersion() + "&request=capabilities";
        }
        return "service=WMPS&version=" + getVersion() + "&request=GetCapabilities";
    }

    /**
     * returns 'WMPS' as service name
     *
     * @return String
     */
    public String getServiceName() {
        return "WMPS";
    }


}
