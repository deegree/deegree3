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

package org.deegree.ogcwebservices.wass.was.configuration;

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
import org.deegree.ogcwebservices.wass.was.capabilities.WASCapabilities;
import org.deegree.ogcwebservices.wass.was.capabilities.WASCapabilitiesDocument;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * Parser for the configuration documents of a WAS.
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 *
 * @version 2.0, $Revision$, $Date$
 *
 * @since 2.0
 */

public class WASConfigurationDocument extends WASCapabilitiesDocument {

    private static final long serialVersionUID = 4612405238432274887L;

    private static final ILogger LOG = LoggerFactory.getLogger( WASConfigurationDocument.class );

    private static final String DBPRE = CommonNamespaces.DGJDBC_PREFIX + ":";

    /**
     * @param serviceConfigurationUrl
     * @return the configuration data
     * @throws InvalidCapabilitiesException
     * @throws InvalidConfigurationException
     */
    public WASConfiguration parseConfiguration( URL serviceConfigurationUrl )
                            throws InvalidCapabilitiesException, InvalidConfigurationException {

        WASConfiguration result = null;

        try {
            load( serviceConfigurationUrl );
            WASCapabilities cap = (WASCapabilities) parseCapabilities();

            /*
             * The required operation GetSAMLResponse is currently not supported for the was.
             */
//            boolean saml = false;
//            for ( Operation_1_0 operation : cap.getOperationsMetadata().getAllOperations() ) {
//                if ( "GetSAMLResponse".equals( operation.getName() ) ) {
//                    saml = true;
//                    break;
//                }
//            }
//
//            if ( !saml )
//                throw new InvalidCapabilitiesException(
//                                                        Messages.getMessage(
//                                                                         "WASS_ERROR_CAPABILITIES_MISSING_REQUIRED_OPERATION",
//                                                                         "GetSAMLResponse" ) );

            WASDeegreeParams deegreeParams = parseDeegreeParams();

            result = new WASConfiguration( cap, deegreeParams );

        } catch ( IOException e ) {
            LOG.logError( e.getLocalizedMessage(), e );
            throw new InvalidConfigurationException(
                                                     Messages.getMessage(
                                                                      "WASS_ERROR_CONFIGURATION_NOT_READ",
                                                                      "WAS" ), e );
        } catch ( SAXException e ) {
            LOG.logError( e.getLocalizedMessage(), e );
            throw new InvalidConfigurationException(
                                                     Messages.getMessage(
                                                                      "WASS_ERROR_CONFIGURATION_NOT_PARSED",
                                                                      "WAS" ), e );
        } catch ( XMLParsingException e ) {
            LOG.logError( e.getLocalizedMessage(), e );
            throw new InvalidConfigurationException(
                                                     Messages.getMessage(
                                                                      "WASS_ERROR_CONFIGURATION_NOT_PARSED",
                                                                      "WAS" ), e );
        }

        return result;
    }

    /**
     * Creates a class representation of the <code>deegreeParams</code>- section.
     *
     * @return the representation
     * @throws XMLParsingException
     */
    private WASDeegreeParams parseDeegreeParams()
                            throws XMLParsingException {


        WASDeegreeParams deegreeParams = null;

        final String preWAS = CommonNamespaces.DEEGREEWAS_PREFIX + ":";
        Node root = this.getRootElement();
        Element element = (Element) XMLTools.getRequiredNode( root, preWAS + "deegreeParam",
                                                              nsContext );

        OnlineResource defaultOnlineResource = parseOnLineResource( (Element) XMLTools.getRequiredNode(
                                                                                                        element,
                                                                                                        preWAS
                                                                                                                                + "DefaultOnlineResource",
                                                                                                        nsContext ) );

        // 'CacheSize'-element (optional, default: 100)
        int cacheSize = XMLTools.getNodeAsInt( element, preWAS + "CacheSize", nsContext, 100 );

        // 'RequestTimeLimit'-element (optional, default: 15)
        int requestTimeLimit = XMLTools.getNodeAsInt( element, preWAS + "RequestTimeLimit",
                                                      nsContext, 15 ) * 1000;

        // 'Encoding'-element (optional, default: UTF-8)
        String characterSet = XMLTools.getStringValue( "Encoding", CommonNamespaces.DEEGREEWAS,
                                                       element, "UTF-8" );

        StringBuffer sb = new StringBuffer().append( "/" ).append( preWAS );
        sb.append( "OnlineResource" );

        // SecuredServiceAddress does not make sense for a WAS
//        StringBuffer sor = new StringBuffer( preWAS ).append( "SecuredServiceAddress" ).append( sb );
//        OnLineResource securedOnlineResource = parseOnLineResource( (Element) XMLTools.getRequiredNode(
//                                                                                                        element,
//                                                                                                        sor.toString(),
//                                                                                                        nsContext ) );

        StringBuffer aor = new StringBuffer( preWAS );
        aor.append( "AuthenticationServiceAddress" ).append( sb );
        OnlineResource authOnlineResource = parseOnLineResource( (Element) XMLTools.getRequiredNode(
                                                                                                     element,
                                                                                                     aor.toString(),
                                                                                                     nsContext ) );

        int sessionLifetime = XMLTools.getNodeAsInt( element, preWAS + "SessionLifetime",
                                                     nsContext, 1200 );
        sessionLifetime *= 1000;

        // parse database connection
        Element database = (Element)XMLTools.getNode( element, DBPRE + "JDBCConnection", nsContext );
        JDBCConnection dbConnection = null;
        if( database != null ) {
            IODocument io = new IODocument( database );
            dbConnection = io.parseJDBCConnection();
        }

        deegreeParams = new WASDeegreeParams( defaultOnlineResource, cacheSize, requestTimeLimit,
                                              characterSet, authOnlineResource, sessionLifetime, dbConnection );


        return deegreeParams;
    }

}
