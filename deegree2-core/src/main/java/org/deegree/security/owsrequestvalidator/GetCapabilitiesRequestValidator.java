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
package org.deegree.security.owsrequestvalidator;

import java.util.List;

import org.deegree.i18n.Messages;
import org.deegree.ogcwebservices.InvalidParameterValueException;
import org.deegree.ogcwebservices.OGCWebServiceRequest;
import org.deegree.ogcwebservices.csw.capabilities.CatalogueGetCapabilities;
import org.deegree.ogcwebservices.wcs.getcapabilities.WCSGetCapabilities;
import org.deegree.ogcwebservices.wfs.operation.WFSGetCapabilities;
import org.deegree.ogcwebservices.wms.operation.WMSGetCapabilities;
import org.deegree.security.drm.model.User;
import org.deegree.security.owsproxy.Condition;
import org.deegree.security.owsproxy.OperationParameter;
import org.deegree.security.owsproxy.Request;

/**
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth </a>
 * @author last edited by: $Author$
 *
 * @version 1.1, $Revision$, $Date$
 *
 * @since 1.1
 */
public class GetCapabilitiesRequestValidator extends RequestValidator {

    // known condition parameter
    private static final String UPDATESEQUENCE = "updatesequence";

    /**
     * @param policy
     */
    public GetCapabilitiesRequestValidator( Policy policy ) {
        super( policy );
    }

    /**
     * validates the incoming GetCapabilities request
     *
     * @param request
     *            request to validate
     * @param user
     *            name of the user who likes to perform the request (can be null)
     */
    @Override
    public void validateRequest( OGCWebServiceRequest request, User user )
                            throws InvalidParameterValueException {

        String[] version = null;
        String updateSeq = null;
        Request req = null;

        if ( request instanceof WFSGetCapabilities ) {
            version = ( (WFSGetCapabilities) request ).getAcceptVersions();
            if ( version == null ) {
                version = new String[] { ( (WFSGetCapabilities) request ).getVersion() };
            }
            req = policy.getRequest( "WFS", "GetCapabilities" );
        } else if ( request instanceof WMSGetCapabilities ) {
            version = new String[1];
            version[0] = ( (WMSGetCapabilities) request ).getVersion();
            updateSeq = ( (WMSGetCapabilities) request ).getUpdateSequence();
            req = policy.getRequest( "WMS", "GetCapabilities" );
        } else if ( request instanceof WCSGetCapabilities ) {
            version = new String[1];
            version[0] = ( (WCSGetCapabilities) request ).getVersion();
            req = policy.getRequest( "WCS", "GetCapabilities" );
        } else if ( request instanceof CatalogueGetCapabilities ) {
            version = new String[1];
            version[0] = ( (CatalogueGetCapabilities) request ).getVersion();
            req = policy.getRequest( "CSW", "GetCapabilities" );
        } else {
            version = new String[0];
            req = policy.getRequest( request.getServiceName(), "GetCapabilities" );
        }

        // request is valid because no restrictions are made
        if ( req.isAny() || req.getPreConditions().isAny() ) {
            return;
        }
        for ( int i = 0; i < version.length; i++ ) {
            validateVersion( req.getPreConditions(), version[i] );
        }
        validateUpdateSeq( req.getPreConditions(), updateSeq );

    }

    /**
     *
     * @param updateSeq
     * @throws InvalidParameterValueException
     */
    private void validateUpdateSeq( Condition condition, String updateSeq )
                            throws InvalidParameterValueException {
        OperationParameter op = condition.getOperationParameter( UPDATESEQUENCE );
        // updatesequence is valid because no restrictions are made
        if ( op.isAny() ) {
            return;
        }
        List<String> list = op.getValues();
        if ( !list.contains( updateSeq ) ) {
            if ( !op.isUserCoupled() ) {
                String s = Messages.getMessage( "OWSPROXY_INVALID_UPDATESEQ" );
                throw new InvalidParameterValueException( s );
            }
            userCoupled = true;
        }
    }

}
