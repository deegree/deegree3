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

package org.deegree.ogcwebservices.wpvs;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.Map;

import org.deegree.datatypes.values.Values;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.IDGenerator;
import org.deegree.framework.util.StringTools;
import org.deegree.ogcwebservices.OGCWebService;
import org.deegree.ogcwebservices.OGCWebServiceException;
import org.deegree.ogcwebservices.wms.operation.GetMap;
import org.deegree.ogcwebservices.wms.operation.GetMapResult;
import org.deegree.ogcwebservices.wpvs.configuration.AbstractDataSource;
import org.deegree.ogcwebservices.wpvs.configuration.LocalWMSDataSource;
import org.deegree.ogcwebservices.wpvs.utils.ImageUtils;
import org.deegree.ogcwebservices.wpvs.utils.ResolutionStripe;

/**
 * Invoker for a Web Map Service.
 *
 * @author <a href="mailto:taddei@lat-lon.de">Ugo Taddei</a>
 * @author last edited by: $Author$
 *
 * $Revision$, $Date$
 */
public class WMSInvoker extends GetViewServiceInvoker {

    private static final ILogger LOG = LoggerFactory.getLogger( WMSInvoker.class );

    private int id;

    /**
     * Creates a new instance of this class.
     *
     * @param owner
     *            the handler that owns this invoker
     * @param id
     *            which can be used to sort all the request/responses in the resolutionStripe.
     */
    public WMSInvoker( ResolutionStripe owner, int id ) {
        super( owner );
        this.id = id;
    }

    @Override
    public void invokeService( AbstractDataSource dataSource ) {
        // int count = 0;
        if ( !( dataSource != null && ( AbstractDataSource.LOCAL_WMS == dataSource.getServiceType() || AbstractDataSource.REMOTE_WMS == dataSource.getServiceType() ) ) ) {
            LOG.logError( "The given AbstractDataSource is no LocalWMSDataSource instance. It is needed for a WMSInvoker" );
            throw new RuntimeException( "DataSource should be a WMS-instance for a WMSInvoker" );
        }
        OGCWebService service = null;
        try {
            service = dataSource.getOGCWebService();
        } catch ( OGCWebServiceException ogcwe ) {
            LOG.logError( ogcwe.getMessage() );
            throw new RuntimeException( ogcwe );
        }

        Object response = null;
        try {
            GetMap getMapRequest = createGetMapRequest( (LocalWMSDataSource) dataSource );
            if ( getMapRequest == null ) {
                return;
            }
            LOG.logDebug( dataSource.getName() + ": WMS request: " + getMapRequest );
            /**
             * Invoke the wms service.
             */
            response = service.doService( getMapRequest );
        } catch ( OGCWebServiceException ogcwse ) {
            if ( !Thread.currentThread().isInterrupted() ) {
                StringBuilder msg = new StringBuilder("Error when performing WMS (" );
                    msg.append( dataSource.getName() );
                    msg.append( "GetMap: ");
                    msg.append( ogcwse.getLocalizedMessage() );
                LOG.logError( msg.toString(), ogcwse );
                resolutionStripe.setTextureRetrievalException( dataSource.getName().getFormattedString() + id,
                                                               ogcwse );
            }

            return;
        }
        if ( response != null ) {
            if ( response instanceof GetMapResult ) {
                BufferedImage responseImage = null;
                // the exception may be inside the response
                if ( ( (GetMapResult) response ).getException() != null ) {
                    resolutionStripe.setTextureRetrievalException( dataSource.getName().getFormattedString() + id,
                                                                   ( (GetMapResult) response ).getException() );
                } else {
                    responseImage = (BufferedImage) ( (GetMapResult) response ).getMap();
                    // LOG.logDebug( StringTools.concat( 100, "WMS RESULT: ", response ) );
                    if ( responseImage != null ) {
                        Color[] colors = ( (LocalWMSDataSource) dataSource ).getTransparentColors();
                        if ( colors != null && colors.length > 0 ) {
                            ImageUtils imgUtil = new ImageUtils( colors );
                            Image img = imgUtil.filterImage( responseImage );
                            Graphics2D g2d = (Graphics2D) responseImage.getGraphics();
                            g2d.drawImage( img, 0, 0, null );
                            g2d.dispose();
                        }
                        resolutionStripe.addTexture( ( dataSource.getName().getFormattedString() + id ), responseImage );
                    }
                }
            }
        }
    }

    /**
     * Creates a new GetMap request for the given datasource.
     *
     * @param ds
     *            the WMs datasource
     * @return a new GetMap request
     */
    private GetMap createGetMapRequest( LocalWMSDataSource ds ) {

        GetMap getMapRequest = ds.getPartialGetMapRequest();

        Values elevation = null;
        Values time = null;
        Map<String, Values> sampleDim = null;

        // int tileSize = owner.getMaxTileSize();
        int tileWidth = resolutionStripe.getRequestWidthForBBox();
        int tileHeight = resolutionStripe.getRequestHeightForBBox();
        if ( tileWidth == -1 || tileHeight == -1 ) {
            LOG.logError( StringTools.concat( 500,
                                              "Not creating WMS-GetMap because the height or the width of the image are -1: " ) );
            return null;
        }
        // GeometryUtils.getImageSizeForSurface( (RankedSurface)box, owner.getMaxTileSize());

        IDGenerator idg = IDGenerator.getInstance();
        getMapRequest = GetMap.create( getMapRequest.getVersion(), String.valueOf( idg.generateUniqueID() ),
                                       getMapRequest.getLayers(), elevation, sampleDim, getMapRequest.getFormat(),
                                       tileWidth, tileHeight, resolutionStripe.getCRSName().getFormattedString(),
                                       resolutionStripe.getSurface().getEnvelope(), getMapRequest.getTransparency(),
                                       getMapRequest.getBGColor(), getMapRequest.getExceptions(), time, null, null,
                                       null );

        return getMapRequest;
    }
}
