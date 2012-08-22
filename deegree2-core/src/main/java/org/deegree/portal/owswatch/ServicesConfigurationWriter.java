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

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;

import org.deegree.framework.xml.XMLFragment;
import org.deegree.framework.xml.XMLTools;
import org.w3c.dom.Element;

/**
 * This class is used to write services xml. This xml contains the needed informationfor the ServiceMonitor The main
 * operations here are add/remove. The file will be rewriten after any of these operations
 *
 * @author <a href="mailto:elmasry@lat-lon.de">Moataz Elmasry</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class ServicesConfigurationWriter {

    private String servicesUrl = null;

    private String prefix = null;

    /**
     * @param servicesUrl
     * @param prefix
     */
    public ServicesConfigurationWriter( String servicesUrl, String prefix ) {
        this.servicesUrl = servicesUrl;
        this.prefix = prefix;
    }

    /**
     * Build an xml document from the current stored services
     *
     * @param services
     * @return root element with all services
     */
    private Element buildXmlDocument( Map<Integer, ServiceConfiguration> services ) {

        String dotPrefix = prefix + ":";
        Element root = XMLTools.create().createElementNS( CommonNamepspaces.DEEGREEWSNS.toASCIIString(),
                                                          dotPrefix + "Config" );
        root.setAttribute( "service_id_sequence", String.valueOf( ServiceConfiguration.getServiceCounter() ) );

        for ( ServiceConfiguration service : services.values() ) {
            buildServiceElement( service, root );
        }
        return root;
    }

    /**
     * Builds an Element from the given service
     *
     * @param service
     *            from where the Element will be built
     * @param root
     *            where the service to be added
     * @return the newly build service element
     */
    private Element buildServiceElement( ServiceConfiguration service, Element root ) {

        String dotPrefix = prefix + ":";
        Element serviceElem = XMLTools.appendElement( root, CommonNamepspaces.DEEGREEWSNS, dotPrefix
                                                                                           + Constants.SERVICE_MONITOR );
        serviceElem.setAttribute( "id", String.valueOf( service.getServiceid() ) );
        // Appending the basic elements
        XMLTools.appendElement( serviceElem, CommonNamepspaces.DEEGREEWSNS, dotPrefix + Constants.ACTIVE,
                                String.valueOf( service.isActive() ) );
        XMLTools.appendElement( serviceElem, CommonNamepspaces.DEEGREEWSNS, dotPrefix + Constants.TIMEOUT_KEY,
                                String.valueOf( service.getTimeout() ) );
        XMLTools.appendElement( serviceElem, CommonNamepspaces.DEEGREEWSNS, dotPrefix + Constants.INTERVAL,
                                String.valueOf( service.getRefreshRate() ) );
        XMLTools.appendElement( serviceElem, CommonNamepspaces.DEEGREEWSNS, dotPrefix + Constants.ONLINE_RESOURCE,
                                String.valueOf( service.getOnlineResource() ) );
        XMLTools.appendElement( serviceElem, CommonNamepspaces.DEEGREEWSNS, dotPrefix + Constants.SERVICE_NAME,
                                String.valueOf( service.getServiceName() ) );
        Element httpMethodElem = XMLTools.appendElement( serviceElem, CommonNamepspaces.DEEGREEWSNS,
                                                         dotPrefix + Constants.HTTP_METHOD );
        httpMethodElem.setAttribute( "type", service.getHttpMethod() );

        // Appending to the HttpMethod Element the request keys/values
        Properties props = service.getProperties();
        Enumeration keys = props.keys();
        while ( keys.hasMoreElements() ) {
            String key = (String) keys.nextElement();
            String value = String.valueOf( props.get( key ) );
            XMLTools.appendElement( httpMethodElem, CommonNamepspaces.DEEGREEWSNS, dotPrefix + key, value );
        }
        return serviceElem;
    }

    /**
     * Writes the given root to the xml file
     *
     * @param root
     *            to be written
     * @throws IOException
     *             In case the file path is invalid or the file is locked
     */
    private void writeToXmlDocument( Element root )
                            throws IOException {

        BufferedWriter writer = new BufferedWriter( new FileWriter( servicesUrl ) );

        try {
            XMLFragment frag = new XMLFragment( root );
            String text = frag.getAsPrettyString();
            writer.write( text );
            writer.close();
        } catch ( Exception e ) {
            throw new IOException( "An error occured while writing the xml file: " + e.getMessage() );
        }

    }

    /**
     * Writes the root element to the given document
     *
     * @param serviceconfigs
     *
     * @throws IOException
     */
    public void writeDocument( Map<Integer, ServiceConfiguration> serviceconfigs )
                            throws IOException {

        Element root = buildXmlDocument( serviceconfigs );
        writeToXmlDocument( root );
    }
}
