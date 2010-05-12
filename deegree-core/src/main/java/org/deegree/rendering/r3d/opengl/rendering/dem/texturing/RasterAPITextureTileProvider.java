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

import org.deegree.coverage.raster.AbstractRaster;
import org.deegree.coverage.raster.SimpleRaster;
import org.deegree.coverage.raster.cache.ByteBufferPool;
import org.deegree.coverage.raster.data.DataView;
import org.deegree.coverage.raster.data.info.BandType;
import org.deegree.coverage.raster.data.info.DataType;
import org.deegree.coverage.raster.data.nio.PixelInterleavedRasterData;
import org.deegree.coverage.raster.geom.RasterGeoReference;
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

        Envelope subsetEnv = fac.createEnvelope( minX, minY, maxX, maxY, null );

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
        ByteBuffer pixelBuffer = null;
        boolean ttHasAlpha = this.hasAlpha;
        if ( rasterData.isWithinDataArea() ) {
            pixelBuffer = rasterData.getByteBuffer();
        } else {
            if ( rasterData.getDataType() != DataType.BYTE ) {
                LOG.warn( "Raster type is not byte, textures can currently only be bytes." );
            } else {
                if ( !rasterData.isOutside() ) {
                    ttHasAlpha = true;
                    DataView rect = rasterData.getView();
                    LOG.debug( "Raster api texture rect: " + rect );
                    int bands = rasterData.getBands() + ( hasAlpha ? 0 : 1 );
                    pixelBuffer = ByteBufferPool.allocate( rect.width * rect.height * rasterData.getDataInfo().dataSize
                                                           * bands, false, false );

                    LOG.debug( "Intersects the raster, get the pixels one by one." );
                    final byte[] sample = new byte[rasterData.getBands()];
                    final byte[] noData = rasterData.getNullPixel( null );
                    final byte[] nNoData = new byte[bands];
                    if ( noData.length == 3 ) {
                        nNoData[0] = noData[0];
                        nNoData[1] = noData[0];
                        nNoData[2] = noData[0];
                        nNoData[3] = 0;
                    }
                    for ( int y = 0; y < rect.height; ++y ) {
                        for ( int x = 0; x < rect.width; ++x ) {
                            if ( rasterData.calculatePos( x, y ) == -1 ) {
                                pixelBuffer.put( nNoData );
                            } else {
                                rasterData.getPixel( x, y, sample );
                                pixelBuffer.put( sample );
                                // set the alpha visible
                                pixelBuffer.put( (byte) -1 );
                            }
                        }

                    }
                } else {
                    LOG.debug( "Requested texture is outside the raster data." );
                }

            }
        }

        if ( pixelBuffer == null ) {
            // System.out.println( "No pixelbuffer" );
            return null;
        }
        // System.out.println( "returning texture tile." );

        // DataView rect = rasterData.getView();
        // if ( rect.width % 2 != 0 ) {
        // rect.width++;
        // }
        // if ( rect.height % 2 != 0 ) {
        // rect.height++;
        // }

        // ByteBufferRasterData data = RasterDataFactory.createRasterData( rect.width, rect.height,
        // raster.getRasterDataInfo().bandInfo,
        // rasterData.getDataType(), InterleaveType.PIXEL,
        // false );
        // data.setByteBuffer( pixelBuffer, new DataView( new RasterRect( 0, 0, rect.width, rect.height ),
        // raster.getRasterDataInfo() ) );
        //
        // System.out.println( "hier3" );
        // Envelope env = raster.getRasterReference().getEnvelope( rect, null );
        // SimpleRaster sr = new SimpleRaster( data, env, raster.getRasterReference() );
        // try {
        // System.out.println( "hiere" );
        // RasterFactory.saveRasterToFile( sr, File.createTempFile( "result", ".png" ) );
        // } catch ( IOException e ) {
        // if ( LOG.isDebugEnabled() ) {
        // LOG.debug( "(Stack) Exception occurred: " + e.getLocalizedMessage(), e );
        // } else {
        // LOG.error( "Exception occurred: " + e.getLocalizedMessage() );
        // }
        // }
        // System.out.println( "columns: " + subset.getColumns() );
        // System.out.println( "rows: " + subset.getRows() );
        return new TextureTile( minX, minY, maxX, maxY, subset.getColumns(), subset.getRows(), pixelBuffer, ttHasAlpha,
                                true );

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
     * @return
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
