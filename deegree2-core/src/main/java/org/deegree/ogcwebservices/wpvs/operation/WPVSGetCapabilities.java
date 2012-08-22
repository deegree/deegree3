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

package org.deegree.ogcwebservices.wpvs.operation;

import java.util.Map;

import org.deegree.framework.util.StringTools;
import org.deegree.ogcwebservices.InvalidParameterValueException;
import org.deegree.ogcwebservices.MissingParameterValueException;
import org.deegree.ogcwebservices.getcapabilities.GetCapabilities;

/**
 * ...
 *
 * @author <a href="mailto:taddei@lat-lon.de">Ugo Taddei</a>
 * @author last edited by: $Author$
 *
 * @version 2.0, $Revision$, $Date$
 *
 * @since 2.0
 */
public class WPVSGetCapabilities extends GetCapabilities {

    /**
     *
     */
    private static final long serialVersionUID = 4226522219910647235L;

    private static final String WPVS_SERVICE_NAME = "WPVS";

    /**
     * Creates a <code>WPVSGetCapabilities</code> object.
     *
     * @param id
     * @param version
     * @param updateSeq
     * @param acceptedVersions
     * @param sections
     * @param acceptedFormats
     * @param vendoreSpec
     * TODO check if all pars are needed for WPV Service
     */
    public WPVSGetCapabilities( String id, String version, String updateSeq,
                                String[] acceptedVersions, String[] sections,
                                String[] acceptedFormats, Map<String,String> vendoreSpec) {
        super( id, version, updateSeq, acceptedVersions, sections, acceptedFormats, vendoreSpec);
    }

    /**
     * Creates a <code>WPVSGetCapabilites</code> request from a key-value-pairs in
     * <code>paramMap</code>.
     * @param paramMap Map containing te request parameters
     * @return an new instance of a WPVSGetCapabilities request.
     * @throws MissingParameterValueException if there is a parameter missing
     * @throws InvalidParameterValueException if one of the parameters has an invalid value
     */
    public static WPVSGetCapabilities create( Map<String,String> paramMap )
    	throws 	MissingParameterValueException,
        		InvalidParameterValueException {

        String id = paramMap.remove( "ID" );
        String service = paramMap.remove( "SERVICE" );

        if ( !service.equals( WPVS_SERVICE_NAME ) ) {
            throw new MissingParameterValueException( "WPVSGetCapabilities",
                "'service' parameter is missing" );
        }
        if ( !service.equals( WPVS_SERVICE_NAME ) ) {
            throw new InvalidParameterValueException( "WPVSGetCapabilities",
                "service attribute must equal 'WPVS'" );
        }

        String updateSeq = paramMap.remove( "UPDATESEQUENCE" );
        String version = paramMap.remove( "VERSION" );
        String tmp = paramMap.remove( "SECTION" );

        String[] sections = null;
        if ( tmp != null ) {
            sections = StringTools.toArray( tmp, ",", true );
        }

        return new WPVSGetCapabilities( id, service, updateSeq, new String[] { version },
            							sections, null, paramMap);
    }

    /**
     * returns 'WPVS' as service name.
     */
    public String getServiceName() {
        return WPVS_SERVICE_NAME;
    }
}
