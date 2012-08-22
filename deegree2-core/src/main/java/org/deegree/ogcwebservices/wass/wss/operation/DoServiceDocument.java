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

package org.deegree.ogcwebservices.wass.wss.operation;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.deegree.framework.xml.XMLFragment;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.framework.xml.XMLTools;
import org.deegree.i18n.Messages;
import org.deegree.ogcbase.CommonNamespaces;
import org.deegree.ogcwebservices.wass.common.AuthenticationData;
import org.deegree.ogcwebservices.wass.common.AuthenticationDocument;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * A parser for a xml DoService Request.
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 *
 * @version 2.0, $Revision$, $Date$
 *
 * @since 2.0
 */

public class DoServiceDocument extends XMLFragment {

    private static final long serialVersionUID = -8223141905965433189L;

    private static final String PRE = CommonNamespaces.GDINRWWSS_PREFIX + ":";

    /**
     * @param id
     *            the request id
     * @param rootElement
     * @return the encapsulated data
     * @throws XMLParsingException
     */
    public DoService parseDoService( String id, Element rootElement )
                            throws XMLParsingException {


        String service = XMLTools.getRequiredNodeAsString( rootElement, "@service", nsContext );
        if ( ! "WSS".equals( service  ) )
            throw new XMLParsingException( Messages.getMessage( "WASS_ERROR_NOT_WSS" ) );

        String version = XMLTools.getRequiredNodeAsString( rootElement, "@version", nsContext );
        if ( !version.equals( "1.0" ) )
            throw new XMLParsingException(
                                           Messages.getMessage( "WASS_ERROR_NO_VERSION_ATTRIBUTE" ) );

        String currentPre = CommonNamespaces.GDINRW_AUTH_PREFIX + ":";
        Element authData = (Element) XMLTools.getRequiredNode( rootElement, currentPre
                                                                            + "AuthenticationData",
                                                               nsContext );

        AuthenticationData authenticationData = new AuthenticationDocument().parseAuthenticationData( authData );

        Element serviceRequest = (Element) XMLTools.getRequiredNode( rootElement,
                                                                     PRE + "ServiceRequest",
                                                                     nsContext );

        String DCP = XMLTools.getRequiredNodeAsString( serviceRequest, "@DCP", nsContext );
        if ( !( DCP.equals( "HTTP_GET" ) || DCP.equals( "HTTP_POST" ) ) )
            throw new XMLParsingException(
                                           Messages.getMessage(
                                                            "WASS_ERROR_NOT_POST_OR_GET",
                                                            "WSS" ) );

        ArrayList<RequestParameter> requestParameters = parseRequestParameters( serviceRequest );

        String payload = XMLTools.getRequiredNodeAsString( serviceRequest, PRE + "Payload",
                                                           nsContext );

        URI facadeURL = XMLTools.getRequiredNodeAsURI( rootElement, PRE + "FacadeURL", nsContext );

        DoService theService = new DoService( id, service, version, authenticationData, DCP,
                                              requestParameters, payload, facadeURL );

        return theService;
    }

    private ArrayList<RequestParameter> parseRequestParameters( Element serviceRequest )
                            throws XMLParsingException {

        List<Node> requestParameter = XMLTools.getNodes( serviceRequest, PRE + "RequestParameter",
                                                   nsContext );
        if ( requestParameter == null ) {

            return null;
        }
        ArrayList<RequestParameter> serviceRequests = new ArrayList<RequestParameter>();
        for ( Object element : requestParameter ) {
            String id = XMLTools.getRequiredNodeAsString( (Element) element, "@id", nsContext );
            String nodeValue = ( (Element) element ).getNodeValue();
            RequestParameter param = new RequestParameter( nodeValue, id );
            serviceRequests.add( param );
        }

        return serviceRequests;
    }
}
