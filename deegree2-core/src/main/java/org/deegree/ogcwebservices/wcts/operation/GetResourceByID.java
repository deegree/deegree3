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

package org.deegree.ogcwebservices.wcts.operation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.deegree.i18n.Messages;
import org.deegree.ogcbase.ExceptionCode;
import org.deegree.ogcwebservices.OGCWebServiceException;
import org.deegree.ogcwebservices.wcts.WCTService;

/**
 * <code>GetResourceByID</code> encapsulates the xml-dom representation or kvp request parameters of a GetResourceById
 * request.
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 *
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 *
 */
public class GetResourceByID extends WCTSRequestBase {

    private static final long serialVersionUID = 9201043457053591617L;

    private final List<String> resourceIDs;

    private final String outputFormat;

    /**
     * *
     *
     * @param version
     *            of the request
     * @param id
     *            of the request
     * @param resourceIDs
     *            a list of resources a client is interested in.
     * @param outputFormat
     *            if <code>null</code> it will be set to 'text/xml'
     */
    public GetResourceByID( String version, String id, List<String> resourceIDs, String outputFormat ) {
        super( version, id, null );
        if ( resourceIDs == null ) {
            resourceIDs = new ArrayList<String>();
        }
        this.resourceIDs = resourceIDs;

        if ( outputFormat == null ) {
            //ows 1.1.0 spec proposal.
            outputFormat = "text/xml";
        }
        this.outputFormat = outputFormat;

    }

    /**
     * @return the resourceIDs, may be empty but never <code>null</code>
     */
    public final List<String> getResourceIDs() {
        return resourceIDs;
    }

    /**
     * @return the outputFormat, may be empty but never <code>null</code>
     */
    public final String getOutputFormat() {
        return outputFormat;
    }

    /**
     * Create a {@link GetResourceByID}-request by extracting the values from the map, and calling the constructor with
     * these values.
     *
     * @param requestID
     *            service internal id for this request.
     * @param map
     *            to extract requested values from.
     * @return the bean representation
     * @throws OGCWebServiceException
     *             if the map is <code>null</code> or has size==0, or the service,request parameters have none
     *             accepted values.
     */
    public static GetResourceByID create( String requestID, Map<String, String> map )
                                                                                     throws OGCWebServiceException {
        if ( map == null || map.size() == 0 ) {
            throw new OGCWebServiceException( Messages.getMessage( "WCTS_REQUESTMAP_NULL" ),
                                              ExceptionCode.MISSINGPARAMETERVALUE );
        }
        String service = map.get( "SERVICE" );
        if ( service == null || !"WCTS".equals( service ) ) {
            throw new OGCWebServiceException( Messages.getMessage( "WCTS_NO_VERSION_KVP", service ),
                                              ExceptionCode.MISSINGPARAMETERVALUE );
        }
        String request = map.get( "REQUEST" );
        if ( request == null || !"GetResourceByID".equalsIgnoreCase( request ) ) {
            throw new OGCWebServiceException( Messages.getMessage( "WCTS_NO_REQUEST_KVP", "GetResourceByID" ),
                                              ( request == null ? ExceptionCode.MISSINGPARAMETERVALUE
                                                               : ExceptionCode.OPERATIONNOTSUPPORTED ) );
        }

        String version = map.get( "VERSION" );
        if ( version == null || !WCTService.version.equalsIgnoreCase( version ) ) {
            throw new OGCWebServiceException( Messages.getMessage( "WCTS_NO_VERSION_KVP", version ),
                                              ExceptionCode.MISSINGPARAMETERVALUE );
        }

        String tmp = map.get( "RESOURCEID" );
        List<String> resourceIDs = new ArrayList<String>( 10 );
        if ( tmp != null && !"".equals( tmp.trim() ) ) {
            String[] splitter = tmp.split( "," );
            for ( String split : splitter ) {
                if ( split != null && !"".equals( split.trim() ) ) {
                    resourceIDs.add( split.trim() );
                }
            }
        } else {
            throw new OGCWebServiceException( Messages.getMessage( "WCTS_MISSING_MANDATORY_KEY_KVP", "ResourceID" ),
                                              ExceptionCode.MISSINGPARAMETERVALUE );
        }

        String outputFormat = map.get( "OUTPUTFORMAT" );

        return new GetResourceByID( version, requestID, resourceIDs, outputFormat );
    }

}
