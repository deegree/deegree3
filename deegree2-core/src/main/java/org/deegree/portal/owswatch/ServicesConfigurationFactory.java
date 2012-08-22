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

package org.deegree.portal.owswatch;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.deegree.framework.util.StringTools;
import org.deegree.framework.xml.NamespaceContext;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.framework.xml.XMLTools;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This class is used to parse the services as services.xml. This xml file includes all services to be tested
 *
 * @author <a href="mailto:elmasry@lat-lon.de">Moataz Elmasry</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class ServicesConfigurationFactory implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -4433307342023097237L;

    private String prefix = null;

    private static NamespaceContext cnxt = CommonNamepspaces.getNameSpaceContext();

    /**
     * Parses a list of ServiceConfiguration from the given xml file
     *
     * @param url
     * @return List of ServiceMonitor. These are the parsed services
     * @throws XMLParsingException
     * @throws IOException
     */
    public List<ServiceConfiguration> parseServices( String url )
                            throws XMLParsingException, IOException {

        List<ServiceConfiguration> services = new ArrayList<ServiceConfiguration>();
        try {
            File file = new File( url );
            FileInputStream stream = new FileInputStream( file );
            Document doc = instantiateParser().parse( stream, XMLFragment.DEFAULT_URL );

            int idSequence = Integer.parseInt( XMLTools.getAttrValue( doc.getDocumentElement(), null,
                                                                      "service_id_sequence", "0" ) );
            ServiceConfiguration.setServiceCounter( idSequence );
            prefix = doc.lookupPrefix( CommonNamepspaces.DEEGREEWSNS.toASCIIString() );
            String dotPrefix = null;
            if ( prefix == null ) {
                throw new XMLParsingException( "The Services xml must contain the namespace: "
                                               + CommonNamepspaces.DEEGREEWSNS.toASCIIString() );
            }
            dotPrefix = prefix + ":";
            cnxt.addNamespace( prefix, CommonNamepspaces.DEEGREEWSNS );
            List<Element> list = XMLTools.getElements( doc.getDocumentElement(),
                                                       StringTools.concat( 150, "./", dotPrefix,
                                                                           Constants.SERVICE_MONITOR ), cnxt );
            for ( Element elem : list ) {
                services.add( parseService( elem, prefix ) );
            }
        } catch ( IOException e ) {
            throw new IOException( "The given path was not found: " + url );
        } catch ( Exception e ) {
            throw new XMLParsingException( e.getLocalizedMessage() );
        }
        return services;
    }

    /**
     * Creates a new instance of DocumentBuilder
     *
     * @return DocumentBuilder
     * @throws IOException
     */
    private DocumentBuilder instantiateParser()
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
     * Takes in a service Node and parses it.
     *
     * @param serviceNode
     *            xml node as an example refer to services.xml
     * @param prefix
     * @return ServiceConfiguration
     * @throws XMLParsingException
     * @throws ConfigurationsException
     */
    public ServiceConfiguration parseService( Node serviceNode, String prefix )
                            throws XMLParsingException, ConfigurationsException {

        String dotPrefix = null;
        if ( prefix == null ) {
            throw new XMLParsingException( Messages.getMessage( "ERROR_NULL_OBJ", "Prefix" ) );
        }
        dotPrefix = prefix + ":";
        int id = Integer.parseInt( XMLTools.getRequiredAttrValue( "id", null, serviceNode ) );
        String onlineResource = XMLTools.getRequiredNodeAsString( serviceNode,
                                                                  StringTools.concat( 100, "./", dotPrefix,
                                                                                      Constants.ONLINE_RESOURCE ), cnxt );
        String serviceName = XMLTools.getRequiredNodeAsString( serviceNode,
                                                               StringTools.concat( 100, "./", dotPrefix,
                                                                                   Constants.SERVICE_NAME ), cnxt );
        int interval = XMLTools.getRequiredNodeAsInt( serviceNode, StringTools.concat( 100, "./", dotPrefix,
                                                                                       Constants.INTERVAL ), cnxt );
        int timeout = XMLTools.getRequiredNodeAsInt( serviceNode, StringTools.concat( 100, "./", dotPrefix,
                                                                                      Constants.TIMEOUT_KEY ), cnxt );
        boolean active = XMLTools.getRequiredNodeAsBoolean( serviceNode, StringTools.concat( 100, "./", dotPrefix,
                                                                                             Constants.ACTIVE ), cnxt );

        Element httpElem = XMLTools.getElement( serviceNode, StringTools.concat( 100, "./", dotPrefix,
                                                                                 Constants.HTTP_METHOD ), cnxt );
        String httpMethod = XMLTools.getAttrValue( httpElem, null, "type", null );

        Properties props = null;
        if ( "GET".equals( httpMethod ) ) {
            props = parseHttpRequestGet( httpElem );
        } else if ( "POST".equals( httpMethod ) ) {
            props = parseHttpRequestPOST( httpElem, prefix );
        } else {
            throw new ConfigurationsException( "The Http method has to be either GET or POST" );
        }
        return new ServiceConfiguration( id, serviceName, httpMethod, onlineResource, active, interval, timeout, props );
    }

    /**
     * Parses a GET Request
     *
     * @param httpElem
     * @return Properties contains key value pairs of the request
     * @throws XMLParsingException
     */
    private Properties parseHttpRequestGet( Node httpElem )
                            throws XMLParsingException {
        Properties props = new Properties();

        NodeList list = httpElem.getChildNodes();
        for ( int i = 0; i < list.getLength(); i++ ) {
            Node node = list.item( i );
            if ( node.getNodeType() != Node.TEXT_NODE ) {
                props.setProperty( node.getLocalName(), node.getTextContent() );

            }
        }
        return props;
    }

    /**
     * Parses a POST xml request
     *
     * @param httpElem
     *            that contains the element XMLREQUEST to parse
     * @return Properties contains, service type,request type, version and the xml request
     * @throws XMLParsingException
     */
    private Properties parseHttpRequestPOST( Node httpElem, String prefix )
                            throws XMLParsingException {
        Properties props = new Properties();
        String dotPrefix = prefix + ":";
        String serviceType = XMLTools.getRequiredNodeAsString( httpElem, StringTools.concat( 100, "./", dotPrefix,
                                                                                             Constants.SERVICE_TYPE ),
                                                               cnxt );
        props.put( Constants.SERVICE_TYPE, serviceType );
        String requestType = XMLTools.getRequiredNodeAsString( httpElem, StringTools.concat( 100, "./", dotPrefix,
                                                                                             Constants.REQUEST_TYPE ),
                                                               cnxt );
        props.put( Constants.REQUEST_TYPE, requestType );
        props.put( Constants.VERSION, XMLTools.getRequiredNodeAsString( httpElem,
                                                                        StringTools.concat( 100, "./", dotPrefix,
                                                                                            Constants.VERSION ), cnxt ) );
        String xmlContent = XMLTools.getElement( httpElem,
                                                 StringTools.concat( 100, "./", dotPrefix, Constants.XML_REQUEST ),
                                                 cnxt ).getTextContent();

        props.put( Constants.XML_REQUEST, xmlContent != null ? xmlContent : "" );
        return props;
    }

    /**
     * @return deegree NameSpacecontext plus the cnxt of the ServiceConfiguration
     */
    public static NamespaceContext getCnxt() {
        return cnxt;
    }

    /**
     * @return Prefix of the Serviceconfiguration xml file
     */
    public String getPrefix() {
        return prefix;
    }
}
