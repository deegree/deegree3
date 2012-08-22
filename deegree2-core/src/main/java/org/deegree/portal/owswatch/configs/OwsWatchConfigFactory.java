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

package org.deegree.portal.owswatch.configs;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.deegree.framework.util.StringTools;
import org.deegree.framework.xml.NamespaceContext;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.framework.xml.XMLTools;
import org.deegree.portal.owswatch.CommonNamepspaces;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * This class takes the path to the config file and produce an instance of OwsWatchConfig Class
 *
 * @author <a href="mailto:elmasry@lat-lon.de">Moataz Elmasry</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class OwsWatchConfigFactory {

    private static NamespaceContext cnxt = CommonNamepspaces.getNameSpaceContext();

    private static String prefix = null;

    private static String dotPrefix = null;

    private OwsWatchConfigFactory() {
    }

    /**
     * @param filePath
     * @param webinfPath
     * @return OwsWatchConfig
     * @throws SAXException
     * @throws IOException
     * @throws XMLParsingException
     */
    public static OwsWatchConfig createOwsWatchConfig( String filePath, String webinfPath )
                            throws SAXException, IOException, XMLParsingException {

        File file = new File( filePath );
        FileInputStream stream = new FileInputStream( file );
        Document doc = instantiateParser().parse( stream, XMLFragment.DEFAULT_URL );
        return createOwsWatchConfig( doc.getDocumentElement(), webinfPath );
    }

    /**
     * @param root
     * @param webinfPath
     * @return OwsWatchConfig
     * @throws XMLParsingException
     * @throws IOException
     */
    public static OwsWatchConfig createOwsWatchConfig( Element root, String webinfPath )
                            throws XMLParsingException, IOException {

        if ( cnxt == null ) {
            cnxt = CommonNamepspaces.getNameSpaceContext();
        }
        prefix = root.lookupPrefix( CommonNamepspaces.DEEGREEWCNS.toASCIIString() );
        if ( prefix == null ) {
            throw new XMLParsingException( "The Configurations xml file does not contain the namespace: "
                                           + CommonNamepspaces.DEEGREEWCNS );
        } else {
            dotPrefix = prefix + ":";
        }
        cnxt.addNamespace( prefix, CommonNamepspaces.DEEGREEWCNS );
        GeneralConfig general = createGeneralConfig( XMLTools.getRequiredElement(
                                                                                  root,
                                                                                  StringTools.concat( 100, "./",
                                                                                                      dotPrefix,
                                                                                                      "GeneralConfiguration" ),
                                                                                  cnxt ) );
        ServiceDescription serviceConfig = createServiceConfig(
                                                                XMLTools.getRequiredElement(
                                                                                             root,
                                                                                             StringTools.concat(
                                                                                                                 100,
                                                                                                                 "./",
                                                                                                                 dotPrefix,
                                                                                                                 "ServiceConfiguration" ),
                                                                                             cnxt ), webinfPath );
        return new OwsWatchConfig( general, serviceConfig );
    }

    /**
     * Parses the General Config section
     *
     * @param elem
     * @return GeneralConfig
     * @throws XMLParsingException
     */
    protected static GeneralConfig createGeneralConfig( Element elem )
                            throws XMLParsingException {

        Map<String, User> users = createUsers( XMLTools.getRequiredElement( elem, StringTools.concat( 100, "./",
                                                                                                      dotPrefix,
                                                                                                      "Users" ), cnxt ) );
        int globalRefreshRate = XMLTools.getNodeAsInt( elem, StringTools.concat( 100, "./", dotPrefix,
                                                                                 "GlobalRefreshRate" ), cnxt, 1 );
        String mailFrom = XMLTools.getRequiredNodeAsString( elem, StringTools.concat( 100, "./", dotPrefix, "Mail/",
                                                                                      dotPrefix, "mailFrom" ), cnxt );
        String mailServer = XMLTools.getRequiredNodeAsString( elem, StringTools.concat( 100, "./", dotPrefix, "Mail/",
                                                                                        dotPrefix, "mailServer" ), cnxt );
        Element locElement = XMLTools.getRequiredElement( elem, StringTools.concat( 100, "./", dotPrefix, "Location" ),
                                                          cnxt );
        String serviceAddress = XMLTools.getRequiredNodeAsString( locElement, StringTools.concat( 100, "./", dotPrefix,
                                                                                                  "ServiceAddress" ),
                                                                  cnxt );
        String protFolderPath = XMLTools.getRequiredNodeAsString( locElement, StringTools.concat( 100, "./", dotPrefix,
                                                                                                  "ProtocolLocation" ),
                                                                  cnxt );
        String serviceInstancesPath = XMLTools.getRequiredNodeAsString(
                                                                        locElement,
                                                                        StringTools.concat( 100, "./", dotPrefix,
                                                                                            "ServiceInstanceLocation" ),
                                                                        cnxt );
        return new GeneralConfig( globalRefreshRate, users, mailFrom, mailServer, protFolderPath, serviceInstancesPath,
                                  serviceAddress );
    }

    /**
     * Parses the ServiceDescription Section
     *
     * @param elem
     * @param webinfPath
     * @return ServiceDescription
     * @throws XMLParsingException
     * @throws IOException
     */
    protected static ServiceDescription createServiceConfig( Element elem, String webinfPath )
                            throws XMLParsingException, IOException {
        Map<String, Service> services = createServices( XMLTools.getElement( elem, StringTools.concat( 100, "./",
                                                                                                       dotPrefix,
                                                                                                       "Services" ),
                                                                             cnxt ), webinfPath );
        List<Integer> testIntervals = createTestIntervals( XMLTools.getElement( elem,
                                                                                StringTools.concat( 100, "./",
                                                                                                    dotPrefix,
                                                                                                    "TestInterval" ),
                                                                                cnxt ) );

        return new ServiceDescription( testIntervals, services );
    }

    /**
     * Parses the Users section
     *
     * @param elem
     * @return Map
     * @throws XMLParsingException
     */
    protected static Map<String, User> createUsers( Element elem )
                            throws XMLParsingException {
        Map<String, User> users = new HashMap<String, User>();
        List<Element> list = XMLTools.getElements( elem, StringTools.concat( 100, "./", dotPrefix, "User" ), cnxt );
        for ( Element child : list ) {
            String username = XMLTools.getRequiredNodeAsString( child, StringTools.concat( 100, "./", dotPrefix,
                                                                                           "UserName" ), cnxt );
            String password = XMLTools.getRequiredNodeAsString( child, StringTools.concat( 100, "./", dotPrefix,
                                                                                           "Password" ), cnxt );
            String firstName = XMLTools.getRequiredNodeAsString( child, StringTools.concat( 100, "./", dotPrefix,
                                                                                            "FirstName" ), cnxt );
            String lastName = XMLTools.getRequiredNodeAsString( child, StringTools.concat( 100, "./", dotPrefix,
                                                                                           "LastName" ), cnxt );
            String email = XMLTools.getRequiredNodeAsString( child,
                                                             StringTools.concat( 100, "./", dotPrefix, "Email" ), cnxt );
            String rolesValue = XMLTools.getRequiredNodeAsString( child, StringTools.concat( 100, "./", dotPrefix,
                                                                                             "Roles" ), cnxt );
            List<String> roles = Arrays.asList( rolesValue.split( "," ) );
            User user = new User( username, password, firstName, lastName, email, roles );
            users.put( username, user );
        }
        return users;
    }

    /**
     * Parses the TestIntervals section
     *
     * @param elem
     * @return List
     * @throws XMLParsingException
     */
    protected static List<Integer> createTestIntervals( Element elem )
                            throws XMLParsingException {

        List<Element> list = XMLTools.getElements( elem, StringTools.concat( 100, "./", dotPrefix, "Value" ), cnxt );
        List<Integer> testIntervals = new ArrayList<Integer>();
        for ( Element child : list ) {
            testIntervals.add( XMLTools.getRequiredNodeAsInt( child, ".", cnxt ) );
        }
        return testIntervals;
    }

    /**
     * Parses the Services section
     *
     * @param elem
     * @param webinfPath
     * @return Map
     * @throws XMLParsingException
     * @throws IOException
     */
    protected static Map<String, Service> createServices( Element elem, String webinfPath )
                            throws XMLParsingException, IOException {

        Map<String, Service> tmpServices = new HashMap<String, Service>();
        List<Element> list = XMLTools.getElements( elem, StringTools.concat( 100, "./", dotPrefix, "Service" ), cnxt );
        for ( Element child : list ) {
            String serviceName = XMLTools.getAttrValue( child, null, "type", null );

            String[] tokens = null;
            if ( ( tokens = serviceName.split( ":" ) ).length == 2 ) {
                serviceName = tokens[1];
            }
            ServiceVersion serviceVersion = createServiceVersion( child, webinfPath );
            if ( !tmpServices.containsKey( serviceName ) ) {
                tmpServices.put( serviceName, new Service( serviceName ) );
            }
            tmpServices.get( serviceName ).getServiceVersions().put( serviceVersion.getVersion(), serviceVersion );
        }
        return tmpServices;
    }

    /**
     * Parses the (multiple)Service section(s) in Services
     *
     * @param elem
     * @param webinfPath
     * @return Service
     * @throws XMLParsingException
     * @throws IOException
     */
    protected static ServiceVersion createServiceVersion( Element elem, String webinfPath )
                            throws XMLParsingException, IOException {

        String version = XMLTools.getAttrValue( elem, null, "version", null );
        ServiceVersion serviceVersion = new ServiceVersion( version );

        List<Element> list = XMLTools.getElements( elem, StringTools.concat( 100, "./", dotPrefix, "ServiceRequest" ),
                                                   cnxt );
        Map<String, ServiceRequest> requests = new HashMap<String, ServiceRequest>();
        for ( Element child : list ) {
            ServiceRequest request = createServiceRequest( child, webinfPath );
            requests.put( request.getName(), request );
        }
        serviceVersion.setRequests( requests );
        return serviceVersion;
    }

    /**
     * Parses the Request Section in Service
     *
     * @param elem
     * @param webinfPath
     * @return ServiceRequest
     * @throws XMLParsingException
     * @throws IOException
     */
    protected static ServiceRequest createServiceRequest( Element elem, String webinfPath )
                            throws XMLParsingException, IOException {
        String requestName = XMLTools.getAttrValue( elem, null, "name", null );
        String attr = XMLTools.getAttrValue( elem, null, "isGetable", null );
        boolean canGet = ( attr != null && attr.equals( "1" ) ) ? true : false;

        attr = XMLTools.getAttrValue( elem, null, "isPostable", null );
        boolean canPost = ( attr != null && attr.equals( "1" ) ) ? true : false;

        String getForm = XMLTools.getNodeAsString( elem, StringTools.concat( 100, "./", dotPrefix, "GetForm" ), cnxt,
                                                   null );
        String postForm = XMLTools.getNodeAsString( elem, StringTools.concat( 100, "./", dotPrefix, "PostForm" ), cnxt,
                                                    null );
        List<Element> list = XMLTools.getElements( elem, StringTools.concat( 100, "./", dotPrefix,
                                                                             "RequestParameters/", dotPrefix, "Key" ),
                                                   cnxt );
        List<String> htmlKeys = new ArrayList<String>();
        for ( Element key : list ) {
            htmlKeys.add( key.getTextContent() );
        }
        String getSnippetPath = null;
        if ( getForm != null && getForm.length() > 0 ) {
            StringBuilder builder = new StringBuilder( 150 );
            builder.append( webinfPath );
            if ( !webinfPath.endsWith( "/" ) ) {
                builder.append( "/" );
            }
            builder.append( getForm );
            getSnippetPath = builder.toString();
        }

        String postSnippetPath = null;
        if ( postForm != null && postForm.length() > 0 ) {
            StringBuilder builder = new StringBuilder( 150 );
            builder.append( webinfPath );
            if ( !webinfPath.endsWith( "/" ) ) {
                builder.append( "/" );
            }
            builder.append( postForm );
            postSnippetPath = builder.toString();
        }

        return new ServiceRequest( requestName, getSnippetPath, postSnippetPath, canPost, canGet, htmlKeys );
    }

    /**
     * Creates a new instance of DocumentBuilder
     *
     * @return DocumentBuilder
     * @throws IOException
     */
    private static DocumentBuilder instantiateParser()
                            throws IOException {

        DocumentBuilder parser = null;

        try {
            DocumentBuilderFactory fac = DocumentBuilderFactory.newInstance();
            fac.setNamespaceAware( true );
            fac.setValidating( false );
            fac.setIgnoringElementContentWhitespace( false );
            parser = fac.newDocumentBuilder();
            return parser;
        } catch ( ParserConfigurationException e ) {
            throw new IOException( "Unable to initialize DocumentBuilder: " + e.getMessage() );
        }
    }

    /**
     * @return Prefix of the Configurations xml file Will only work after the first call to createOwsWatchConfig,
     *         otherwise it will return null;
     */
    public static String getPrefix() {
        return prefix;
    }
}
