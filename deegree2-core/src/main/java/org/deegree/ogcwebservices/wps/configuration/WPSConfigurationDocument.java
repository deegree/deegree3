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
package org.deegree.ogcwebservices.wps.configuration;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.StringTools;
import org.deegree.framework.xml.InvalidConfigurationException;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.framework.xml.XMLTools;
import org.deegree.model.metadata.iso19115.OnlineResource;
import org.deegree.ogcwebservices.wps.capabilities.WPSCapabilitiesDocument;
import org.deegree.ogcwebservices.wps.execute.RequestQueueManager;
import org.w3c.dom.Element;

/**
 * WPSConfigurationDocument.java
 *
 * @author <a href="mailto:christian@kiehle.org">Christian Kiehle</a>
 * @author <a href="mailto:christian.heier@gmx.de">Christian Heier</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class WPSConfigurationDocument extends WPSCapabilitiesDocument {

    protected static final ILogger LOG = LoggerFactory.getLogger( WPSConfigurationDocument.class );

    /**
     * Creates a class representation of the document.
     *
     * @return class representation of the configuration document
     */
    public WPSConfiguration getConfiguration()
                            throws InvalidConfigurationException {
        WPSConfiguration WPSConfiguration = null;

        try {
            // last Parameter <code>ProcessOfferings</code> set to null, because
            // <code>WPSConfiguration</code>
            // constructor is responsible for instantiating ProcessOfferings
            WPSConfiguration = new WPSConfiguration( parseVersion(), parseUpdateSequence(), getServiceIdentification(),
                                                     getServiceProvider(), getOperationsMetadata(), null,
                                                     getDeegreeParams() );
        } catch ( XMLParsingException e ) {
            LOG.logError( e.getMessage() );
            throw new InvalidConfigurationException( e.getMessage() + "\n" + StringTools.stackTraceToString( e ) );
        }
        return WPSConfiguration;
    }

    /**
     * Creates the java representation of the <code>deegreeParams</code>- section.
     *
     * @return java representation of <code>deegreeParams</code> section
     * @throws InvalidConfigurationException
     */
    public WPSDeegreeParams getDeegreeParams()
                            throws InvalidConfigurationException {
        LOG.logInfo( nsContext.toString() );
        WPSDeegreeParams deegreeParams = null;

        try {
            Element element = (Element) XMLTools.getRequiredNode( getRootElement(), "./deegreewps:deegreeParams",
                                                                  nsContext );
            OnlineResource defaultOnlineResource = parseOnLineResource( (Element) XMLTools.getRequiredNode(
                                                                                                            element,
                                                                                                            "./deegreewps:DefaultOnlineResource",
                                                                                                            nsContext ) );
            int cacheSize = XMLTools.getNodeAsInt( element, "./deegreewps:CacheSize/text()", nsContext, 100 );
            int requestTimeLimit = XMLTools.getNodeAsInt( element, "./deegreewps:RequestTimeLimit/text()", nsContext, 2 );

            String[] processDirectories = XMLTools.getNodesAsStrings(
                                                                      element,
                                                                      "./deegreewps:ProcessDirectoryList/deegreewps:ProcessDirectory/text()",
                                                                      nsContext );
            int lengthOfProcessDirectoryList = processDirectories.length;
            if ( 0 == lengthOfProcessDirectoryList ) {
                LOG.logInfo( "No process directory specified. Using configuration document directory." );
                processDirectories = new String[] { "." };
            }

            for ( int i = 0; i < lengthOfProcessDirectoryList; i++ ) {
                try {
                    processDirectories[i] = resolve( processDirectories[i] ).toURI().getPath();
                } catch ( Exception e ) {
                    LOG.logError( "Process directory '" + processDirectories[i]
                                  + "' cannot be resolved as a directory: " + e.getMessage(), e );
                    throw new InvalidConfigurationException(
                                                             "Process directory '"
                                                                                     + processDirectories[i]
                                                                                     + "' cannot be resolved as a directory: "
                                                                                     + e.getMessage(), e );

                }
            }
            RequestQueueManager requestQueueManagerClass = getRequestQueueManagerClass( element );

            deegreeParams = new WPSDeegreeParams( defaultOnlineResource, cacheSize, requestTimeLimit,
                                                  processDirectories, requestQueueManagerClass );

        } catch ( XMLParsingException e ) {
            LOG.logError( e.getMessage() );
            throw new InvalidConfigurationException( "Error parsing the deegreeParams "
                                                     + "section of the WPS configuration: \n" + e.getMessage()
                                                     + StringTools.stackTraceToString( e ) );
        }
        return deegreeParams;
    }

    private RequestQueueManager getRequestQueueManagerClass( Element deegreeParamsNode )
                            throws XMLParsingException {

        // Get resonsible class for requestqueuemanager from deegreeParams
        // section
        RequestQueueManager requestQueueManager = null;

        String requestQueueManagerClass = XMLTools.getRequiredNodeAsString(
                                                                            deegreeParamsNode,
                                                                            "./deegreewps:RequestQueueManager/deegreewps:ResponsibleClass/text()",
                                                                            nsContext );

        Object tmp = null;
        try {
            tmp = Class.forName( requestQueueManagerClass ).newInstance();
        } catch ( ClassNotFoundException clnfe ) {

            String msg = "Responsible class for queue management: '" + requestQueueManagerClass + "' not found.";
            LOG.logError( msg, clnfe );
            throw new XMLParsingException( msg, clnfe );
        } catch ( InstantiationException ia ) {

            String msg = "Responsible class for queue management: '" + requestQueueManagerClass
                         + "' can not be instantiated.";
            LOG.logError( msg, ia );
            throw new XMLParsingException( msg, ia );
        } catch ( IllegalAccessException iae ) {

            String msg = "Responsible class for queue management: '" + requestQueueManagerClass
                         + "' can not be accessed.";

            LOG.logError( msg, iae );
            throw new XMLParsingException( msg, iae );
        }

        if ( tmp instanceof RequestQueueManager ) {
            requestQueueManager = (RequestQueueManager) tmp;
        } else {
            String msg = "Responsible class for queue management: '"
                         + requestQueueManagerClass
                         + "' does not implement required Interface 'org.deegree.ogcwebservices.wps.execute.RequestQueueManager'.";
            ;
            LOG.logError( msg );
            throw new XMLParsingException( msg );
        }
        LOG.logInfo( "requestQueueManager: " + requestQueueManagerClass );
        return requestQueueManager;
    }

}
