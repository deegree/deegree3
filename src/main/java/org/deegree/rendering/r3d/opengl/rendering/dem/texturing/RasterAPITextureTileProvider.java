//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/base/trunk/resources/eclipse/files_template.xml $
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

package org.deegree.rendering.r3d.opengl.rendering.dem.texturing;

import java.nio.ByteBuffer;

import org.deegree.commons.utils.Pair;
import org.deegree.coverage.raster.AbstractRaster;
import org.deegree.coverage.raster.SimpleRaster;
import org.deegree.coverage.raster.data.info.BandType;
import org.deegree.coverage.raster.data.info.DataType;
import org.deegree.coverage.raster.data.nio.PixelInterleavedRasterData;
import org.deegree.coverage.raster.geom.RasterGeoReference;
import org.deegree.coverage.raster.geom.RasterRect;
import org.deegree.coverage.raster.geom.RasterGeoReference.OriginLocation;
import org.deegree.cs.CRS;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.GeometryFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The <code></code> class TODO add class documentation here.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: schneider $
 * 
 * @version $Revision: $, $Date: $
 */
public class RasterAPITextureTileProvider implements TextureTileProvider {

    private static Logger LOG = LoggerFactory.getLogger( RasterAPITextureTileProvider.class );

    private final GeometryFactory fac;

    private final AbstractRaster raster;

    private final double res;

    private boolean hasAlpha;

    /**
     * Read a texture from a file and load it using the raster api.
     * 
     * @param raster
     *            the raster api raster.
     * 
     */
    public RasterAPITextureTileProvider( AbstractRaster raster ) {
        fac = new GeometryFactory();
        this.raster = raster;
        BandType[] bandInfo = this.raster.getRasterDataInfo().bandInfo;
        hasAlpha = bandInfo.length == 4;
        for ( int i = 0; !hasAlpha && i < bandInfo.length; ++i ) {
            if ( bandInfo[i] == BandType.ALPHA ) {
                this.hasAlpha = true;
            }
        }

        // GriddedBlobTileContainer.create( file, options )
        this.res = raster.getRasterReference().getResolutionX();
    }

    private TextureTile getTextureTile( double minX, double minY, double maxX, double maxY ) {
        if ( raster.getRasterDataInfo().getDataType() != DataType.BYTE ) {
            LOG.warn( "Raster type is not byte, textures can currently only be bytes." );
            return null;
        }

        Envelope subsetEnv = fac.createEnvelope( minX, minY, maxX, maxY, null );

        double nMinX = minX;
        double nMaxX = maxX;
        double nMinY = minY;
        double nMaxY = maxY;

        // TODO when does this happen?
        // RasterRect r = raster.getRasterReference().convertEnvelopeToRasterCRS( subsetEnv );
        // if ( r.width % 2 != 0 || r.height % 2 != 0 ) {
        // StringBuilder wH = new StringBuilder();
        // if ( r.width % 2 != 0 ) {
        // wH.append( "width" );
        // }
        // if ( r.height % 2 != 0 ) {
        // if ( wH.length() > 0 ) {
        // wH.append( " and " );
        // }
        // wH.append( "height" );
        // }
        // LOG.warn(
        // "The requested texture for the world coordinates will result in a texture with odd (not even) texture ( "
        // + wH.toString() + " ), this may not be." );
        // return null;
        // }
        AbstractRaster subset = raster.getSubRaster( subsetEnv, null, OriginLocation.OUTER );

        // extract raw byte buffer (RGB, pixel interleaved)
        long begin2 = System.currentTimeMillis();
        SimpleRaster simpleRaster = subset.getAsSimpleRaster();
        if ( LOG.isDebugEnabled() ) {
            LOG.debug( "#getTextureTile(): as simple raster: " + ( System.currentTimeMillis() - begin2 ) + ", env: "
                       + simpleRaster.getEnvelope() );
        }
        PixelInterleavedRasterData rasterData = (PixelInterleavedRasterData) simpleRaster.getRasterData();
        if ( rasterData.isOutside() ) {
            return null;
        }
        boolean ttHasAlpha = this.hasAlpha;
        Pair<ByteBuffer, RasterRect> data = rasterData.getDataRect();
        int texWidth = data.second.width;
        int texHeight = data.second.height;
        ByteBuffer pixelBuffer = data.first;
        if ( !rasterData.isWithinDataArea() ) {
            // pixelBuffer = data.first;
            if ( !rasterData.isOutside() ) {
                // Pair<ByteBuffer, RasterRect> data = rasterData.getDataRect();
                Envelope envelope = raster.getRasterReference().getEnvelope( data.second, subset.getCoordinateSystem() );
                double dataMinX = envelope.getMin().get0();
                double dataMinY = envelope.getMin().get1();
                double dataMaxX = envelope.getMax().get0();
                double dataMaxY = envelope.getMax().get1();
                return new TextureTile( nMinX, nMinY, nMaxX, nMaxY, dataMinX, dataMinY, dataMaxX, dataMaxY, texWidth,
                                        texHeight, pixelBuffer, ttHasAlpha, true );
            }
        }
        return new TextureTile( nMinX, nMinY, nMaxX, nMaxY, texWidth, texHeight, pixelBuffer, ttHasAlpha, true );
        // RasterRect rect = data.second;
        // System.out.println( rect );
        // ByteBufferRasterData bbData = RasterDataFactory.createRasterData( rect.width, rect.height,
        // raster.getRasterDataInfo().bandInfo,
        // rasterData.getDataType(),
        // InterleaveType.PIXEL, false );
        // bbData.setByteBuffer( pixelBuffer, new DataView( new RasterRect( 0, 0, rect.width, rect.height ),
        // raster.getRasterDataInfo() ) );
        //
        // Envelope env = raster.getRasterReference().getEnvelope( rect, null );
        // SimpleRaster sr = new SimpleRaster( bbData, env, raster.getRasterReference() );
        // try {
        // RasterFactory.saveRasterToFile( sr, File.createTempFile( "result", ".png" ) );
        // } catch ( IOException e ) {
        // if ( LOG.isDebugEnabled() ) {
        // LOG.debug( "(Stack) Exception occurred: " + e.getLocalizedMessage(), e );
        // } else {
        // LOG.error( "Exception occurred: " + e.getLocalizedMessage() );
        // }
        // }

    }

    @Override
    public double getNativeResolution() {
        return res;
    }

    @Override
    public boolean hasTextureForResolution( double unitsPerPixel ) {
        return unitsPerPixel >= res;
    }

    @Override
    public TextureTile getTextureTile( TextureTileRequest request ) {
        return getTextureTile( request.getMinX(), request.getMinY(), request.getMaxX(), request.getMaxY() );
    }

    @Override
    public Envelope getEnvelope() {
        return raster.getEnvelope();
    }

    /**
     * @return the raster reference of the texture
     * 
     */
    public RasterGeoReference getRasterReference() {
        return raster.getRasterReference();
    }

    @Override
    public CRS getCRS() {
        return raster.getCoordinateSystem();
    }

}
