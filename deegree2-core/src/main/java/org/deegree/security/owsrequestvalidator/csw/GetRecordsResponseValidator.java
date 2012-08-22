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
package org.deegree.security.owsrequestvalidator.csw;

import org.deegree.framework.util.MimeTypeMapper;
import org.deegree.ogcwebservices.InvalidParameterValueException;
import org.deegree.security.drm.model.User;
import org.deegree.security.owsproxy.Request;
import org.deegree.security.owsrequestvalidator.Policy;
import org.deegree.security.owsrequestvalidator.ResponseValidator;

/**
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth </a>
 * @author last edited by: $Author$
 *
 * @version 1.1, $Revision$, $Date$
 */
class GetRecordsResponseValidator extends ResponseValidator {

    /**
     * @param policy
     */
    public GetRecordsResponseValidator( Policy policy ) {
        super( policy );
    }

    /**
     * validates the passed object as a response to a OWS request. The validity of the response may
     * is assigned to specific user rights. If the passed user is <>null this will be evaluated.
     * <br>
     * the reponse may contain three valid kinds of objects:
     * <ul>
     * <li>a xml encoded exception
     * <li>a GML document
     * <li>a XML document
     * <li>any other kind of document that is valid against the formats defined for GetRecord in
     * the capabilities
     * </ul>
     * Each of these types can be identified by the mime-type of the response that is also passed to
     * the method. <br>
     * If something basic went wrong it is possible that not further specified kind of object is
     * passed as response. In this case the method will throw an
     * <tt>InvalidParameterValueException</tt> to avoid sending bad responses to the client.
     *
     * @param service
     *            service which produced the response (WMS, WFS ...)
     * @param response
     * @param mime
     *            mime-type of the response
     * @param user
     * @return the response array
     * @throws InvalidParameterValueException
     */
    @Override
    public byte[] validateResponse( String service, byte[] response, String mime, User user )
                            throws InvalidParameterValueException {

        Request req = policy.getRequest( service, "GetRecord" );
        // request is valid because no restrictions are made
        if ( req.isAny() || req.getPostConditions().isAny() ) {
            return response;
        }

        // Condition condition = req.getPostConditions();

        if ( MimeTypeMapper.isKnownOGCType( mime ) ) {
            // if the mime-type isn't an image type but a known
            // OGC mime-type it must be an XML document.
            // probably it is an exception but it also could be
            // a GML document
            response = validateXML( response, mime, user );
        } else if ( mime.equals( "text/xml" ) ) {
            // if the mime-type isn't an image type but 'text/xml'
            // it could be an exception
            response = validateXML( response, mime, user );
        } else {
            throw new InvalidParameterValueException( UNKNOWNMIMETYPE + mime );
        }

        return response;
    }

    /**
     * validates the passed byte array to be valid against the policy
     *
     * @param xml
     * @param mime
     * @param user
     */
    private byte[] validateXML( byte[] xml, String mime, User user ) {
        // TODO
        // define useful post-validation for xml-documents
        // at the moment everything is valid
        return xml;
    }

}
