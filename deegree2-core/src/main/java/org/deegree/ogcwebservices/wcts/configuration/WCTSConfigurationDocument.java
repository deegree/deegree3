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

package org.deegree.ogcwebservices.wcts.configuration;

import java.io.IOException;
import java.net.URL;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.framework.xml.XMLTools;
import org.deegree.ogcbase.CommonNamespaces;
import org.deegree.ogcwebservices.getcapabilities.InvalidCapabilitiesException;
import org.deegree.ogcwebservices.wcts.capabilities.Content;
import org.deegree.ogcwebservices.wcts.capabilities.WCTSCapabilities;
import org.deegree.ogcwebservices.wcts.capabilities.WCTSCapabilitiesDocument;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * <code>WCTSConfigurationDocument</code> loads an xml-dom-document and creates a {@link WCTSConfiguration}
 * bean-representation.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * 
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
public class WCTSConfigurationDocument extends WCTSCapabilitiesDocument {

    private static ILogger LOG = LoggerFactory.getLogger( WCTSConfigurationDocument.class );

    /**
     * 
     */
    private static final long serialVersionUID = -4585404178146440716L;

    /**
     * @param configURL
     *            to load from.
     * @throws SAXException
     *             if an xml parsing error has been detected.
     * @throws IOException
     *             if the file could not be loaded.
     */
    public WCTSConfigurationDocument( URL configURL ) throws IOException, SAXException {
        super.load( configURL );
    }

    /**
     * @return a new instance of a {@link WCTSConfiguration}
     * @throws InvalidCapabilitiesException
     *             if the configuration contains erroneous xml.
     */
    public WCTSConfiguration parseConfiguration()
                            throws InvalidCapabilitiesException {

        WCTSDeegreeParams deegreeParams = parseDeegreeParams();
        WCTSConfiguration config = new WCTSConfiguration(
                                                          (WCTSCapabilities) parseCapabilities( deegreeParams.getConfiguredCRSProvider() ),
                                                          deegreeParams );
        if ( deegreeParams.createDefaultTransformationsDescription() ) {
            LOG.logDebug( "Creating descriptions for the default Transformations." );
            Content capCon = config.getContents();
            if ( capCon != null ) {
                capCon.describeDefaultTransformations( deegreeParams.getTransformationPrefix() );
            }
        }
        return config;
    }

    /**
     * @return the deegreeparam section of the configuration.
     * @throws XMLParsingException
     */
    private WCTSDeegreeParams parseDeegreeParams() {
        Element root = getRootElement();
        WCTSDeegreeParams result = null;
        Element deegreeParams;
        try {
            deegreeParams = XMLTools.getElement( root, CommonNamespaces.DEEGREEWCTS_PREFIX + ":deegreeParams",
                                                 nsContext );
            if ( deegreeParams != null ) {
                int updateSequence = XMLTools.getNodeAsInt( deegreeParams, CommonNamespaces.DEEGREEWCTS_PREFIX
                                                                           + ":updateSequence", nsContext, -1 );

                if ( updateSequence > -1 && updateSequence < 60 ) {
                    // one hour
                    updateSequence = 60;
                }
                updateSequence *= 60000;
                LOG.logInfo( "Content cache will be updated every: " + ( updateSequence / 3600000d ) + " hours" );

                String provider = XMLTools.getNodeAsString( deegreeParams, CommonNamespaces.DEEGREEWCTS_PREFIX
                                                                           + ":crsProvider", nsContext, null );
                LOG.logDebug( "Using configured crs provider:" + provider );
                String transformPrefix = XMLTools.getNodeAsString( deegreeParams, CommonNamespaces.DEEGREEWCTS_PREFIX
                                                                                  + ":transformationIDPrefix",
                                                                   nsContext, null );

                boolean useDeegreeTransformType = XMLTools.getNodeAsBoolean(
                                                                             deegreeParams,
                                                                             CommonNamespaces.DEEGREEWCTS_PREFIX
                                                                                                     + ":useDeegreeTransformType",
                                                                             nsContext, true );
                boolean createDefaultTransformations = XMLTools.getNodeAsBoolean(
                                                                                  deegreeParams,
                                                                                  CommonNamespaces.DEEGREEWCTS_PREFIX
                                                                                                          + ":createDefaultTransformationDescriptions",
                                                                                  nsContext, true );
                result = new WCTSDeegreeParams( updateSequence, provider, transformPrefix, useDeegreeTransformType,
                                                createDefaultTransformations );
            }
        } catch ( XMLParsingException e ) {
            LOG.logError( e );
        }
        if ( result == null ) {
            result = new WCTSDeegreeParams();
        }
        return result;
    }

}
