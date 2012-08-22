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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.deegree.framework.xml.XMLFragment;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.framework.xml.XMLTools;
import org.deegree.ogcbase.CommonNamespaces;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Parser class that can parse all elements within the namespace.
 *
 * Namespace: http://www.gdi-nrw.de/authentication
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 *
 * @version 2.0, $Revision$, $Date$
 *
 * @since 2.0
 */
public class AuthenticationDocument extends XMLFragment {

    private static final long serialVersionUID = -6467874541905139362L;

    private final static String PRE = CommonNamespaces.GDINRW_AUTH_PREFIX + ":";

    /**
     * Parses a SupportedAuthenticationMethodList element.
     *
     * @param listRoot
     *            the list element
     * @return an ArrayList with the parsed methods
     * @throws MalformedURLException
     * @throws XMLParsingException
     */
    public ArrayList<SupportedAuthenticationMethod> parseSupportedAuthenticationMethodList(
                                                                                           Element listRoot )
                            throws MalformedURLException, XMLParsingException {

        List<Node> methods = XMLTools.getRequiredNodes( listRoot, PRE + "SupportedAuthenticationMethod",
                                                  nsContext );
        ArrayList<SupportedAuthenticationMethod> values = new ArrayList<SupportedAuthenticationMethod>();
        for ( Object element : methods ) {
            values.add( parseSupportedAuthenticationMethod( (Element) element ) );
        }

        return values;
    }

    /**
     * Parses a SupportedAuthenticationMethod element.
     *
     * @param elem
     *            the element
     * @return the parsed data
     * @throws XMLParsingException
     * @throws MalformedURLException
     */
    public SupportedAuthenticationMethod parseSupportedAuthenticationMethod( Node elem )
                            throws XMLParsingException, MalformedURLException {

        Node method = XMLTools.getNode( elem, PRE + "AuthenticationMethod", nsContext );
        URN methodURN = parseAuthenticationMethod( method );
        Node metadata = XMLTools.getNode( elem, PRE + "WASAuthenticationMethodMD", nsContext );
        if ( metadata != null ) {
            WASAuthenticationMethodMD wasamd = parseWASAuthenticationMethodMD( metadata );
            return new SupportedAuthenticationMethod( methodURN, wasamd );
        }
        metadata = XMLTools.getNode( elem, PRE + "UnknownMethodMetadata", nsContext );
        String ummd = null;
        if ( metadata != null )
            ummd = parseUnknownMethodMetadata( metadata );

        SupportedAuthenticationMethod result = new SupportedAuthenticationMethod( methodURN, ummd );

        return result;
    }

    /**
     * Parses an AuthenticationMethod.
     *
     * @param elem
     *            the AuthenticationMethod element
     * @return an URN with the method
     * @throws XMLParsingException
     */
    public URN parseAuthenticationMethod( Node elem )
                            throws XMLParsingException {
        return new URN( XMLTools.getNodeAsString( elem, "@id", nsContext, null ) );
    }

    /**
     * Parses an UnknownMethodMetadata element.
     *
     * @param elem
     *            the element
     * @return a String with the data
     * @throws XMLParsingException
     */
    public String parseUnknownMethodMetadata( Node elem )
                            throws XMLParsingException {
        return XMLTools.getNodeAsString( elem, ".", nsContext, null );
    }

    /**
     * Parses a WASAuthenticationMethodMD element.
     *
     * @param elem
     *            the element
     * @return an object with the parsed data
     * @throws XMLParsingException
     * @throws MalformedURLException
     */
    public WASAuthenticationMethodMD parseWASAuthenticationMethodMD( Node elem )
                            throws XMLParsingException, MalformedURLException {

        String mdName = XMLTools.getRequiredNodeAsString( elem, PRE + "Name", nsContext );
        URL mdURL = XMLTools.getRequiredNodeAsURI( elem, PRE + "URL", nsContext ).toURL();
        ArrayList<URN> mdAuthenticationMethods = new ArrayList<URN>();
        String[] urns = XMLTools.getNodesAsStrings( elem, PRE + "AuthenticationMethod", nsContext );
        for ( int i = 0; i < urns.length; ++i )
            mdAuthenticationMethods.add( new URN( urns[i] ) );

        WASAuthenticationMethodMD result = new WASAuthenticationMethodMD( mdName, mdURL,
                                                                          mdAuthenticationMethods );

        return result;
    }

    /**
     * Parses an AuthenticationData element
     *
     * @param elem
     *            the element
     * @return an object with the parsed data
     * @throws XMLParsingException
     */
    public AuthenticationData parseAuthenticationData( Node elem )
                            throws XMLParsingException {

        Node method = XMLTools.getRequiredNode( elem, PRE + "AuthenticationMethod", nsContext );
        URN authenticationMethod = parseAuthenticationMethod( method );
        Node cred = XMLTools.getRequiredNode( elem, PRE + "Credentials", nsContext );
        String credentials = parseCredentials( cred );

        AuthenticationData result = new AuthenticationData( authenticationMethod, credentials );

        return result;
    }

    /**
     * Parses a Credentials element.
     *
     * @param elem
     *            the element
     * @return a String containing the data
     * @throws XMLParsingException
     */
    public String parseCredentials( Node elem )
                            throws XMLParsingException {
        return XMLTools.getRequiredNodeAsString( elem, ".", nsContext );
    }

}
