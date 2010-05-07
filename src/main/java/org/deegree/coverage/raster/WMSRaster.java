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

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.coverage.raster;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.deegree.coverage.raster.data.RasterData;
import org.deegree.coverage.raster.data.info.BandType;
import org.deegree.coverage.raster.data.info.RasterDataInfo;
import org.deegree.coverage.raster.geom.RasterGeoReference;
import org.deegree.coverage.raster.geom.RasterRect;
import org.deegree.coverage.raster.geom.RasterGeoReference.OriginLocation;
import org.deegree.cs.CRS;
import org.deegree.geometry.Envelope;
import org.deegree.protocol.wms.client.WMSClient111;
import org.slf4j.Logger;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class WMSRaster extends SimpleRaster {

    private static final Logger LOG = getLogger( WMSRaster.class );

    private final WMSClient111 client;

    private final List<String> layers;

    private final String requestedFormat;

    private final CRS requestedCRS;

    private final int requestTimeout;

    private final Object LOCK = new Object();

    private RasterDataInfo rasterDataInfo;

    /**
     * @param envelope
     * @param rasterReference
     * @param capabilitiesURL
     * @param requestedLayers
     * @param requestCRS
     * @param requestFormat
     * @param transparent
     * @param res
     * @param maxWidth
     * @param maxHeight
     * @param requestTimeout
     */
    protected WMSRaster( Envelope envelope, RasterGeoReference rasterReference, URL capabilitiesURL,
                         String[] requestedLayers, CRS requestCRS, String requestFormat, boolean transparent,
                         double res, int maxWidth, int maxHeight, int requestTimeout ) {
        this( null, envelope, rasterReference, capabilitiesURL, requestedLayers, requestCRS, requestFormat,
              transparent, res, maxWidth, maxHeight, requestTimeout );
    }

    /**
     * @param raster
     * @param envelope
     * @param rasterReference
     * @param capabilitiesURL
     * @param requestedLayers
     * @param requestCRS
     * @param requestFormat
     * @param transparent
     * @param res
     * @param maxWidth
     * @param maxHeight
     * @param requestTimeout
     */
    public WMSRaster( RasterData raster, Envelope envelope, RasterGeoReference rasterReference, URL capabilitiesURL,
                      String[] requestedLayers, CRS requestCRS, String requestFormat, boolean transparent, double res,
                      int maxWidth, int maxHeight, int requestTimeout ) {

        super( raster, envelope, rasterReference );
        this.client = new WMSClient111( capabilitiesURL );
        this.client.setMaxMapDimensions( maxWidth, maxHeight );
        this.layers = Arrays.asList( requestedLayers );
        this.requestedFormat = requestFormat;
        this.requestedCRS = requestCRS;
        this.requestTimeout = requestTimeout;
    }

    @Override
    public SimpleRaster getAsSimpleRaster() {
        return this;
    }

    @Override
    public RasterDataInfo getRasterDataInfo() {
        synchronized ( LOCK ) {
            if ( this.rasterDataInfo == null ) {

            }
        }
        return this.rasterDataInfo;

    }

    @Override
    public SimpleRaster getSubRaster( Envelope subsetEnv ) {
        return getSubRaster( subsetEnv, null );
    }

    @Override
    public SimpleRaster getSubRaster( Envelope subsetEnv, BandType[] bands ) {
        return getSubRaster( subsetEnv, bands, null );
    }

    @Override
    public SimpleRaster getSubRaster( Envelope subsetEnv, BandType[] bands, OriginLocation targetOrigin ) {
        // rb: testing for envelope equality can lead to a memory leak, because the memory can not be freed.
        RasterRect rasterRect = getRasterReference().createRelocatedReference( targetOrigin ).convertEnvelopeToRasterCRS(
                                                                                                                          subsetEnv );
        SimpleRaster raster = null;
        try {
            raster = client.getMapAsSimpleRaster( layers, rasterRect.width, rasterRect.height, subsetEnv, requestedCRS,
                                                  requestedFormat, true, true, requestTimeout, false,
                                                  new ArrayList<String>() ).first;
            LOG.debug( "Success" );
        } catch ( IOException e ) {
            LOG.debug( "Failed: " + e.getMessage(), e );
            // this must never happen, cause the above request uses errorsInImage=true
            throw new RuntimeException( e.getMessage() );
        }
        return raster;
    }

    @Override
    public void setSubRaster( Envelope env, AbstractRaster source ) {
        throw new UnsupportedOperationException( "WMS set raster is currently not supported" );
    }

    @Override
    public void setSubRaster( double x, double y, AbstractRaster source ) {
        throw new UnsupportedOperationException( "WMS set raster is currently not supported" );
    }

    @Override
    public void setSubRaster( double x, double y, int dstBand, AbstractRaster source ) {
        throw new UnsupportedOperationException( "WMS set raster is currently not supported" );
    }

    @Override
    public void setSubRaster( Envelope env, int dstBand, AbstractRaster source ) {
        throw new UnsupportedOperationException( "WMS set raster is currently not supported" );
    }

}
