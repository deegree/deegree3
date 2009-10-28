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

package org.deegree.rendering.r2d;

import static org.slf4j.LoggerFactory.getLogger;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import org.deegree.coverage.raster.AbstractRaster;
import org.deegree.coverage.raster.SimpleRaster;
import org.deegree.coverage.raster.data.RasterData;
import org.deegree.coverage.raster.data.RasterDataFactory;
import org.deegree.coverage.raster.data.info.BandType;
import org.deegree.coverage.raster.data.info.DataType;
import org.deegree.coverage.raster.utils.RasterFactory;
import org.deegree.geometry.Envelope;
import org.deegree.rendering.r2d.styling.RasterStyling;
import org.deegree.rendering.r2d.utils.Raster2RawData;
import org.slf4j.Logger;

/**
 * <code>Java2DRasterRenderer</code>
 * 
 * @author <a href="mailto:a.aiordachioaie@jacobs-university.de">Andrei Aiordachioaie</a>
 * @author last edited by: $Author: aaiordachioaie $
 * 
 * @version $Revision: 19497 $, $Date: 2009-09-11 $
 */
public class Java2DRasterRenderer implements RasterRenderer {

    private static final Logger LOG = getLogger( Java2DRenderer.class );

    private Graphics2D graphics;

    private AffineTransform worldToScreen = new AffineTransform();

    /**
     * @param graphics
     * @param width
     * @param height
     * @param bbox
     */
    public Java2DRasterRenderer( Graphics2D graphics, int width, int height, Envelope bbox ) {
        this.graphics = graphics;

        if ( bbox != null ) {
            double scalex = width / bbox.getSpan0();
            double scaley = height / bbox.getSpan1();

            // we have to flip horizontally, so invert y scale and add the screen height
            worldToScreen.translate( -bbox.getMin().get0() * scalex, bbox.getMin().get1() * scaley + height );
            worldToScreen.scale( scalex, -scaley );

            LOG.debug( "For coordinate transformations, scaling by x = {} and y = {}", scalex, -scaley );
            LOG.trace( "Final transformation was {}", worldToScreen );
        } else {
            LOG.warn( "No envelope given, proceeding with a scale of 1." );
        }
    }

    /**
     * @param graphics
     */
    public Java2DRasterRenderer( Graphics2D graphics ) {
        this.graphics = graphics;
    }

    public void render( RasterStyling styling, AbstractRaster raster ) {
        LOG.trace( "Rendering raster with style..." );
        BufferedImage img = null;
        if ( raster == null ) {
            LOG.warn( "Trying to render null raster." );
            return;
        }
        if ( styling == null ) {
            LOG.warn( "Raster style is null, rendering without style" );
            render( raster );
            return;
        }

        checkChannelNames( styling, raster );
        raster = evaluateChannelSelections( styling, raster );
        raster = evaluateConstrastEnhancements( styling, raster );

        if ( styling.shaded != null ) {
            if ( styling.grayChannel == null && styling.redChannel != null )
                throw new RasterRenderingException(
                                                    "Hill-shading output raster is grayscale, cannot create color image." );
            raster = performHillShading( raster, styling );
        }

        if ( styling.categorize != null || styling.interpolate != null ) {
            LOG.trace( "Creating raster ColorMap..." );
            if ( styling.categorize != null )
                img = styling.categorize.evaluateRaster( raster, styling );
            else if ( styling.interpolate != null )
                img = styling.interpolate.evaluateRaster( raster, styling );
        }

        if ( styling.opacity != 1 ) {
            LOG.trace( "Using opacity: " + styling.opacity );
            graphics.setComposite( AlphaComposite.getInstance( AlphaComposite.SRC_OVER, (float) styling.opacity ) );
        }

        LOG.trace( "Rendering raster..." );
        if ( img != null )
            render( img );
        else
            render( raster );
        LOG.trace( "Done rendering raster." );
    }

    private AbstractRaster evaluateChannelSelections( RasterStyling style, AbstractRaster raster ) {
        if ( style.redChannel == null && style.grayChannel == null )
            return raster;
        LOG.trace( "Evaluating channel selections ..." );
        long start = System.nanoTime();
        RasterData data = raster.getAsSimpleRaster().getRasterData();
        int cols = data.getWidth(), rows = data.getHeight();
        RasterData newData = raster.getAsSimpleRaster().getRasterData();
        BandType[] bandTypes = null;
        Raster2RawData conv = new Raster2RawData( raster, style );
        if ( style.redChannel != null && data.getBands() > 1 ) {
            bandTypes = new BandType[] { BandType.RED, BandType.GREEN, BandType.BLUE };
            newData = RasterDataFactory.createRasterData( cols, rows, bandTypes, data.getDataType(),
                                                          data.getDataInfo().interleaveType );
            newData.setSubset( 0, 0, cols, rows, 0, data, conv.getRedChannelIndex() );
            newData.setSubset( 0, 0, cols, rows, 1, data, conv.getGreenChannelIndex() );
            newData.setSubset( 0, 0, cols, rows, 2, data, conv.getBlueChannelIndex() );
        }
        if ( style.grayChannel != null ) {
            bandTypes = new BandType[] { BandType.BAND_0 };
            newData = RasterDataFactory.createRasterData( cols, rows, bandTypes, data.getDataType(),
                                                          data.getDataInfo().interleaveType );
            newData.setSubset( 0, 0, cols, rows, 0, data, conv.getGrayChannelIndex() );
        }
        AbstractRaster newRaster = new SimpleRaster( newData, raster.getEnvelope(), raster.getRasterReference() );
        long end = System.nanoTime();
        LOG.trace( "Output channels successfully created in {} miliseconds", ( end - start ) / 1000000 );
        return newRaster;
    }

    private AbstractRaster evaluateConstrastEnhancements( RasterStyling styling, AbstractRaster raster ) {
        // TODO
        return raster;
    }

    /** Make sure that the selected channels are valid for a raster. Throws an exception in case of error. */
    private void checkChannelNames( RasterStyling styling, AbstractRaster raster )
                            throws RasterRenderingException {
        BandType[] bands = raster.getRasterDataInfo().getBandInfo();

        if ( styling.grayChannel != null )
            if ( validateChannelName( styling.grayChannel, bands ) == false ) {
                LOG.error( "Invalid name for gray channel: {}", styling.grayChannel );
                throw new RasterRenderingException( "Invalid name for gray channel: " + styling.grayChannel );
            }
        if ( styling.redChannel != null )
            if ( validateChannelName( styling.redChannel, bands ) == false ) {
                LOG.error( "Invalid name for red channel: {}", styling.redChannel );
                throw new RasterRenderingException( "Invalid name for red channel: " + styling.redChannel );
            }
        if ( styling.greenChannel != null )
            if ( validateChannelName( styling.greenChannel, bands ) == false ) {
                LOG.error( "Invalid name for green channel: {}", styling.greenChannel );
                throw new RasterRenderingException( "Invalid name for green channel: " + styling.greenChannel );
            }
        if ( styling.blueChannel != null )
            if ( validateChannelName( styling.blueChannel, bands ) == false ) {
                LOG.error( "Invalid name for blue channel: {}", styling.blueChannel );
                throw new RasterRenderingException( "Invalid name for blue channel: " + styling.blueChannel );
            }

        LOG.trace( "Channel names are valid." );
    }

    /** Validate a channel name as a number (1,2,...etc) or by name. */
    private boolean validateChannelName( String name, BandType[] bands ) {
        try {
            int c = Integer.parseInt( name );
            return ( c > 0 && c <= bands.length );
        } catch ( NumberFormatException e ) {
            for ( int i = 0; i < bands.length; i++ )
                if ( bands[i].name().equals( name ) )
                    return true;
        }
        return false;
    }

    /**
     * Perform the hill-shading algorithm on a DEM raster. Based on algorithm presented at
     * http://edndoc.esri.com/arcobjects/9.2/net/shared/geoprocessing/spatial_analyst_tools/how_hillshade_works.htm
     * 
     * @param raster
     *            Input raster, containing a DEM, with R rows and C columns
     * @return a gray-scale raster (with bytes), with R-2 rows and C-2 columns
     */
    public AbstractRaster performHillShading( AbstractRaster raster, RasterStyling style ) {
        LOG.trace( "Performing Hill-Shading ... " );
        long start = System.nanoTime();
        int cols = raster.getColumns(), rows = raster.getRows();
        Raster2RawData data = new Raster2RawData( raster, style );
        RasterData shadeData = RasterDataFactory.createRasterData( cols - 2, rows - 2, DataType.BYTE );
        SimpleRaster hillShade = new SimpleRaster( shadeData, raster.getEnvelope(), raster.getRasterReference() );

        final double Zenith_rad = Math.toRadians( 90 - style.shaded.Alt );
        final double Azimuth_rad = Math.toRadians( 90 - style.shaded.azimuthAngle );
        final double sinZenith = Math.sin( Zenith_rad );
        final double cosZenith = Math.cos( Zenith_rad );
        double Slope_rad;
        double Aspect_rad = 0;
        byte shade = 0;
        double dx, dy;
        float m[][] = new float[3][3];

        for ( int row = 1; row < rows - 1; row++ ) {
            for ( int col = 1; col < cols - 1; col++ ) {
                m[0][0] = data.get( col - 1, row - 1 );
                m[0][1] = data.get( col, row - 1 );
                m[0][2] = data.get( col + 1, row - 1 );
                m[1][0] = data.get( col - 1, row );
                m[1][1] = data.get( col, row );
                m[1][2] = data.get( col + 1, row );
                m[2][0] = data.get( col - 1, row + 1 );
                m[2][1] = data.get( col, row + 1 );
                m[2][2] = data.get( col + 1, row + 1 );

                dx = ( ( m[0][2] + 2 * m[1][2] + m[2][2] ) - ( m[0][0] + 2 * m[1][0] + m[2][0] ) ) / 8;
                dy = ( ( m[2][0] + 2 * m[2][1] + m[2][2] ) - ( m[0][0] + 2 * m[0][1] + m[0][2] ) ) / 8;
                Slope_rad = Math.atan( style.shaded.reliefFactor * Math.sqrt( dx * dx + dy * dy ) );
                if ( dx != 0 ) {
                    Aspect_rad = Math.atan2( dy, -dx );
                    if ( Aspect_rad < 0 )
                        Aspect_rad += Math.PI * 2;
                }
                if ( dx == 0 ) {
                    if ( dy > 0 )
                        Aspect_rad = Math.PI / 2;
                    else if ( dy < 0 )
                        Aspect_rad = 2 * Math.PI - Math.PI / 2;
                    else
                        Aspect_rad = 0;
                }

                long val = Math.round( 255.0 * ( ( cosZenith * Math.cos( Slope_rad ) ) + ( sinZenith
                                                                                           * Math.sin( Slope_rad ) * Math.cos( Azimuth_rad
                                                                                                                               - Aspect_rad ) ) ) );
                if ( val < 0 )
                    val = 0;
                shade = (byte) val;

                shadeData.setByteSample( col - 1, row - 1, 0, shade );
            }
        }
        long end = System.nanoTime();

        LOG.trace( "Performed Hill shading in {} ms ", ( end - start ) / 1000000 );
        return hillShade;
    }

    private void render( AbstractRaster raster ) {
        render( RasterFactory.imageFromRaster( raster ) );
    }

    private void render( BufferedImage img ) {
        graphics.drawImage( img, worldToScreen, null );
    }
}
