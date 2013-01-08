//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -

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

 Occam Labs UG (haftungsbeschränkt)
 Godesberger Allee 139, 53175 Bonn
 Germany
 http://www.occamlabs.de/

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/

package org.deegree.services.wmts.controller;

import static org.deegree.commons.ows.exception.OWSException.NO_APPLICABLE_CODE;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.Map;

import javax.servlet.ServletException;

import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.commons.config.ResourceInitException;
import org.deegree.commons.ows.exception.OWSException;
import org.deegree.commons.tom.ows.Version;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.protocol.wmts.WMTSConstants.WMTSRequestType;
import org.deegree.services.controller.utils.HttpResponseBuffer;
import org.deegree.services.jaxb.metadata.DeegreeServicesMetadataType;
import org.slf4j.Logger;

/**
 * <code>RequestDispatcher</code>
 * 
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @author last edited by: $Author: mschneider $
 * 
 * @version $Revision: 31882 $, $Date: 2011-09-15 02:05:04 +0200 (Thu, 15 Sep 2011) $
 */

class WmtsRequestDispatcher {

    private static final Logger LOG = getLogger( WmtsRequestDispatcher.class );

    private CapabilitiesHandler capabilitiesHandler;

    private TileHandler tileHandler;

    private FeatureInfoHandler featureInfoHandler;

    WmtsRequestDispatcher( XMLAdapter controllerConf, DeegreeServicesMetadataType mainMetadataConf,
                           DeegreeWorkspace workspace, WmtsBuilder builder, String wmtsId )
                            throws ResourceInitException {
        featureInfoHandler = new FeatureInfoHandler( builder.getFeatureInfoFormatsConf(), controllerConf, workspace,
                                                     builder.getThemes() );
        capabilitiesHandler = new CapabilitiesHandler( mainMetadataConf, workspace, builder.getMetadataUrlTemplate(),
                                                       wmtsId, builder.getThemes(), featureInfoHandler.getManager() );
        tileHandler = new TileHandler( builder.getThemes() );
    }

    void handleRequest( WMTSRequestType req, HttpResponseBuffer response, Map<String, String> map, Version version )
                            throws OWSException, ServletException {
        switch ( req ) {
        case GetCapabilities:
            try {
                capabilitiesHandler.handleGetCapabilities( map, response.getXMLWriter() );
            } catch ( Throwable e ) {
                LOG.trace( "Stack trace:", e );
                throw new OWSException( e.getMessage(), NO_APPLICABLE_CODE );
            }
            break;
        case GetFeatureInfo:
            try {
                featureInfoHandler.getFeatureInfo( map, response );
            } catch ( OWSException e ) {
                throw e;
            } catch ( Throwable e ) {
                LOG.trace( "Stack trace:", e );
                throw new OWSException( e.getMessage(), NO_APPLICABLE_CODE );
            }
            break;
        case GetTile:
            tileHandler.getTile( map, response );
            break;
        }
    }

}
