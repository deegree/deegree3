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

package org.deegree.ogcwebservices.wass.wss.configuration;

import java.io.IOException;
import java.net.URL;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.xml.InvalidConfigurationException;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.framework.xml.XMLTools;
import org.deegree.i18n.Messages;
import org.deegree.io.IODocument;
import org.deegree.io.JDBCConnection;
import org.deegree.model.metadata.iso19115.OnlineResource;
import org.deegree.ogcbase.CommonNamespaces;
import org.deegree.ogcwebservices.getcapabilities.InvalidCapabilitiesException;
import org.deegree.ogcwebservices.wass.common.Operation_1_0;
import org.deegree.ogcwebservices.wass.wss.capabilities.WSSCapabilities;
import org.deegree.ogcwebservices.wass.wss.capabilities.WSSCapabilitiesDocument;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * This class is called from the WSServiceFactory to read a configuration xml file. This file
 * consains all the capabilities this Web Security Service is able to. The standard calling
 * procedure is new WSSConfigurationDocument().getConfiguration( url_to_file ). This method returns
 * the "bean" in form of a WSSConfiguration class, which can be queried for it's values.
 *
 * @see WSSConfiguration
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 *
 * @version 2.0, $Revision$, $Date$
 *
 * @since 2.0
 */

public class WSSConfigurationDocument extends WSSCapabilitiesDocument {
    /**
     *
     */
    private static final long serialVersionUID = 4612405238432274887L;

    /**
     * The logger enhances the quality and simplicity of Debugging within the deegree2 framework
     */
    private static final ILogger LOG = LoggerFactory.getLogger( WSSConfigurationDocument.class );

    private static final String DBPRE = CommonNamespaces.DGJDBC_PREFIX + ":";

    /**
     * Loads the configuration file located at the given urls location.
     *
     * @param serviceConfigurationUrl
     *            the url to the configuration file
     * @return a WSSConfiguration which is a "bean" representation of the configuration xml document
     * @throws InvalidConfigurationException
     *             if an error occrur either with opening or parsing the xml configuration file.
     * @throws InvalidCapabilitiesException
     */
    public WSSConfiguration parseConfiguration( URL serviceConfigurationUrl )
                            throws InvalidConfigurationException, InvalidCapabilitiesException {


        WSSConfiguration result = null;
        try {
            load( serviceConfigurationUrl );
            WSSCapabilities capabilities = (WSSCapabilities) parseCapabilities();
            boolean doService = false;
            for( Operation_1_0 operation : capabilities.getOperationsMetadata().getAllOperations() ){
                if( "DoService".equals(operation.getName()) ){
                    doService = true;
                    break;
                }
            }

            if( !doService )
                throw new InvalidCapabilitiesException(
                                                       Messages.getMessage(
                                                                       "WASS_ERROR_CAPABILITIES_MISSING_REQUIRED_OPERATION",
                                                       "DoService" ) );

            WSSDeegreeParams deegreeParams = parseDeegreeParams( );
            result = new WSSConfiguration( capabilities, deegreeParams );
        } catch ( IOException e ) {
            LOG.logError( e.getLocalizedMessage(), e );
            throw new InvalidConfigurationException(
                                                     Messages.getMessage(
                                                                      "WASS_ERROR_CONFIGURATION_NOT_READ",
                                                                      "WSS" ), e );
        } catch ( SAXException e ) {
            LOG.logError( e.getLocalizedMessage(), e );
            throw new InvalidConfigurationException(
                                                     Messages.getMessage(
                                                                      "WASS_ERROR_CONFIGURATION_NOT_PARSED",
                                                                      "WSS" ), e );
        } catch ( XMLParsingException e ) {
            LOG.logError( e.getLocalizedMessage(), e );
            throw new InvalidConfigurationException(
                                                     Messages.getMessage(
                                                                      "WASS_ERROR_CONFIGURATION_NOT_PARSED",
                                                                      "WSS" ), e );
        }


        return result;
    }

    /**
     * Creates a class representation of the <code>deegreeParams</code>- section which are wss
     * specific.
     *
     * @return the deegree parameter data
     * @throws XMLParsingException
     */
    private WSSDeegreeParams parseDeegreeParams()
                            throws XMLParsingException {

        WSSDeegreeParams deegreeParams = null;

        final String preWSS = CommonNamespaces.DEEGREEWSS_PREFIX + ":";
        Node root = this.getRootElement();

        Element element = (Element) XMLTools.getRequiredNode( root, preWSS + "deegreeParam",
                                                              nsContext );

        OnlineResource defaultOnlineResource = null;
        defaultOnlineResource = parseOnLineResource( (Element) XMLTools.getRequiredNode(
                                                                                         element,
                                                                                         preWSS
                                                                                                                 + "DefaultOnlineResource",
                                                                                         nsContext ) );

        // 'deegreecsw:CacheSize'-element (optional, default: 100)
        int cacheSize = 0;
        cacheSize = XMLTools.getNodeAsInt( element, preWSS + "CacheSize", nsContext, 100 );

        // 'deegreecsw:RequestTimeLimit'-element (optional, default: 15)
        int requestTimeLimit = XMLTools.getNodeAsInt( element, preWSS + "RequestTimeLimit",
                                                          nsContext, 15 );
        requestTimeLimit *= 1000;


        // 'deegreecsw:Encoding'-element (optional, default: UTF-8)
        String characterSet = null;
        characterSet = XMLTools.getNodeAsString( element, preWSS + "Encoding", nsContext, "UTF-8" );

        StringBuffer sb = new StringBuffer().append( "/" ).append( preWSS );
        sb.append( "OnlineResource" );
        StringBuffer sor = new StringBuffer( preWSS ).append( "SecuredServiceAddress" ).append( sb );
        OnlineResource securedOnlineResource = null;
        securedOnlineResource = parseOnLineResource( (Element) XMLTools.getRequiredNode(
                                                                                         element,
                                                                                         sor.toString(),
                                                                                         nsContext ) );

        StringBuffer aor = new StringBuffer( preWSS );
        aor.append( "AuthenticationServiceAddress" ).append( sb );
        OnlineResource authOnlineResource = null;
        authOnlineResource = parseOnLineResource( (Element) XMLTools.getRequiredNode(
                                                                                      element,
                                                                                      aor.toString(),
                                                                                      nsContext ) );

        int sessionLifetime = XMLTools.getNodeAsInt( element, preWSS + "SessionLifetime",
                                                      nsContext, 1200 );
        sessionLifetime *= 1000;

        // parse database connection
        Element database = (Element)XMLTools.getNode( element, DBPRE + "JDBCConnection", nsContext );
        JDBCConnection dbConnection = null;
        if( database != null ) {
            IODocument io = new IODocument( database );
            dbConnection = io.parseJDBCConnection();
        }

        deegreeParams = new WSSDeegreeParams( defaultOnlineResource, cacheSize, requestTimeLimit,
                                              characterSet, securedOnlineResource,
                                              authOnlineResource, sessionLifetime, dbConnection );


        return deegreeParams;
    }
}
