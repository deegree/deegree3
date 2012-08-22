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

package org.deegree.ogcwebservices.wass.common;

import org.deegree.framework.xml.XMLParsingException;
import org.deegree.framework.xml.XMLTools;
import org.deegree.i18n.Messages;
import org.deegree.ogcbase.CommonNamespaces;
import org.deegree.ogcbase.OGCDocument;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Parser class that can parse all elements within the namespace.
 *
 * Namespace: http://www.gdi-nrw.de/session
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 *
 * @version 2.0, $Revision$, $Date$
 *
 * @since 2.0
 */

public class SessionOperationsDocument extends OGCDocument {

    private static final long serialVersionUID = 7190634032990406558L;

    private static final String PSESSION = CommonNamespaces.WSSSESSION_PREFIX + ":";

    /**
     * Parses a GetSession element.
     *
     * @param id
     *            the request id
     *
     * @param request
     *            the element
     * @return an object with the parsed data
     * @throws XMLParsingException
     */
    public GetSession parseGetSession( String id, Element request )
                            throws XMLParsingException {


        String serviceName = parseService( request );
        String version = parseVersion( request );

        String pre = CommonNamespaces.GDINRW_AUTH_PREFIX + ":";
        Node data = XMLTools.getRequiredNode( request, pre + "AuthenticationData", nsContext );
        AuthenticationData authenticationData = new AuthenticationDocument().parseAuthenticationData( data );
        GetSession gs = new GetSession( id, serviceName, version, authenticationData );

        return gs;
    }

    /**
     * Parses a CloseSession element.
     *
     * @param id
     *            the request id
     *
     * @param request
     *            the element
     * @return an object with the data
     * @throws XMLParsingException
     */
    public CloseSession parseCloseSession( String id, Element request )
                            throws XMLParsingException {

        String serviceName = parseService( request );
        String version = parseVersion( request );
        String sessionID = XMLTools.getRequiredNodeAsString( request, PSESSION + "SessionID",
                                                             nsContext );
        CloseSession cs = new CloseSession( id, serviceName, version, sessionID );

        return cs;
    }

    /**
     * Parses the service name.
     *
     * @param basicRequest
     *            the request element
     * @return a String containing the service name
     * @throws XMLParsingException
     *             if the service name was not WAS or WSS
     */
    private String parseService( Element basicRequest )
                            throws XMLParsingException {

        String serviceName = XMLTools.getRequiredNodeAsString( basicRequest, "@service", nsContext );
        if ( !( serviceName.equals( "WAS" ) || serviceName.equals( "WSS" ) ) ) {
            throw new XMLParsingException(
                                           Messages.getMessage( "WASS_ERROR_NO_SERVICE_ATTRIBUTE" ) );
        }

        return serviceName;
    }

    /**
     * Parses the version attribute of a request element.
     *
     * @param basicRequest
     *            the element
     * @return a string containing the version number (currently "1.0")
     * @throws XMLParsingException
     */
    private String parseVersion( Element basicRequest )
                            throws XMLParsingException {

        String version = XMLTools.getRequiredNodeAsString( basicRequest, "@version", nsContext );
        if ( !version.equals( "1.0" ) ) {
            throw new XMLParsingException(
                                           Messages.getMessage( "WASS_ERROR_NO_VERSION_ATTRIBUTE" ) );
        }

        return version;
    }

}
