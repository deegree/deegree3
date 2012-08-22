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
package org.deegree.security.owsrequestvalidator.wfs;

import org.deegree.ogcwebservices.InvalidParameterValueException;
import org.deegree.ogcwebservices.OGCWebServiceRequest;
import org.deegree.ogcwebservices.getcapabilities.GetCapabilities;
import org.deegree.ogcwebservices.wfs.operation.DescribeFeatureType;
import org.deegree.ogcwebservices.wfs.operation.GetFeature;
import org.deegree.ogcwebservices.wfs.operation.GetFeatureWithLock;
import org.deegree.ogcwebservices.wfs.operation.LockFeature;
import org.deegree.ogcwebservices.wfs.operation.transaction.Transaction;
import org.deegree.security.UnauthorizedException;
import org.deegree.security.drm.model.User;
import org.deegree.security.owsrequestvalidator.Messages;
import org.deegree.security.owsrequestvalidator.OWSValidator;
import org.deegree.security.owsrequestvalidator.Policy;

/**
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth </a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class WFSValidator extends OWSValidator {

    private static final String MS_INVALIDREQUEST = Messages.getString( "WFSValidator.WFS_INVALIDREQUEST" );

    private GetFeatureRequestValidator getFeatureValidator;

    private GetFeatureResponseValidator getFeatureRespValidator;

    private DescribeFeatureTypeRequestValidator describeFeatureTypeValidator;

    private TransactionValidator transactionValidator;

    /**
     * @param policy
     * @param proxyURL
     */
    public WFSValidator( Policy policy, String proxyURL ) {
        super( policy, proxyURL );
        getFeatureValidator = new GetFeatureRequestValidator( policy );
        getFeatureRespValidator = new GetFeatureResponseValidator( policy );
        describeFeatureTypeValidator = new DescribeFeatureTypeRequestValidator( policy );
        transactionValidator = new TransactionValidator( policy );
    }

    /**
     * validates the passed <tt>OGCWebServiceRequest</tt> if it is valid against the defined
     * conditions for WFS requests
     *
     * @param request
     * @param user
     * @throws InvalidParameterValueException
     * @throws UnauthorizedException
     */
    @Override
    public void validateRequest( OGCWebServiceRequest request, User user )
                            throws InvalidParameterValueException, UnauthorizedException {

        if ( request instanceof GetCapabilities ) {
            getCapabilitiesValidator.validateRequest( request, user );
        } else if ( request instanceof GetFeature ) {
            getFeatureValidator.validateRequest( request, user );
        } else if ( request instanceof GetFeatureWithLock ) {
            throw new UnauthorizedException( "GetFeatureWithLock on the WFS are not allowed!" );
        } else if ( request instanceof LockFeature ) {
            throw new UnauthorizedException( "Lock on the WFS are not allowed!" );
        } else if ( request instanceof DescribeFeatureType ) {
            describeFeatureTypeValidator.validateRequest( request, user );
        } else if ( request instanceof Transaction ) {
            transactionValidator.validateRequest( request, user );
        } else {
            throw new InvalidParameterValueException( MS_INVALIDREQUEST
                                                      + request.getClass().getName() );
        }
    }

    /**
     * @param request
     * @param response
     * @param mime
     * @param user
     * @return the new response
     * @throws InvalidParameterValueException
     * @throws UnauthorizedException
     */
    @Override
    public byte[] validateResponse( OGCWebServiceRequest request, byte[] response, String mime,
                                    User user )
                            throws InvalidParameterValueException, UnauthorizedException {

        if ( request instanceof GetCapabilities ) {
            response = getCapabilitiesValidatorR.validateResponse( "WFS", response, mime, user );
        } else if ( request instanceof GetFeature ) {
            response = getFeatureRespValidator.validateResponse( "WFS", response, mime, user );
        }
        // TODO responses to other requests
        return response;
    }
}
