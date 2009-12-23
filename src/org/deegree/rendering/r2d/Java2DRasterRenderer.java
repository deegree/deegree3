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

import org.deegree.commons.utils.Pair;
import org.deegree.coverage.raster.AbstractRaster;
import org.deegree.coverage.raster.SimpleRaster;
import org.deegree.coverage.raster.data.RasterData;
import org.deegree.coverage.raster.data.RasterDataFactory;
import org.deegree.coverage.raster.data.info.BandType;
import org.deegree.coverage.raster.data.info.DataType;
import org.deegree.coverage.raster.utils.RasterFactory;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.Geometry;
import org.deegree.rendering.r2d.styling.RasterChannelSelection;
import org.deegree.rendering.r2d.styling.RasterStyling;
import org.deegree.rendering.r2d.styling.Styling;
import org.deegree.rendering.r2d.styling.RasterChannelSelection.ChannelSelectionMode;
import org.deegree.rendering.r2d.styling.RasterStyling.ContrastEnhancement;
import org.deegree.rendering.r2d.utils.Raster2Feature;
import org.deegree.rendering.r2d.utils.RasterDataUtility;
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

    private static final Logger LOG = getLogger( Java2DRasterRenderer.class );

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

    /**
     * Render a raster with a styling.
     * 
     * @param styling
     * @param raster
     */
    public void render( RasterStyling styling, AbstractRaster raster ) {
        LOG.trace( "Rendering raster with style '{}'.", styling );
        BufferedImage img = null;
        if ( raster == null ) {
            LOG.warn( "Trying to render null raster." );
            return;
        }
        if ( styling == null ) {
            LOG.debug( "Raster style is null, rendering without style" );
            render( raster );
            return;
        }

        if ( styling.channelSelection != null ) {
            // Compute channel selection indexes on current raster
            styling.channelSelection.evaluate( raster.getRasterDataInfo().bandInfo );
        }

        if ( styling.categorize != null || styling.interpolate != null || styling.shaded != null ) {
            LOG.trace( "Creating raster ColorMap..." );
            if ( styling.categorize != null ) {
                img = styling.categorize.evaluateRaster( raster, styling );
            } else if ( styling.interpolate != null )
                img = styling.interpolate.evaluateRaster( raster, styling );

            if ( styling.shaded != null ) {
                raster = performHillShading( raster, styling );
            }
        } else if ( styling.channelSelection.isEnabled() ) {
            raster = evaluateChannelSelections( styling.channelSelection, raster );
        }

        if ( styling.contrastEnhancement != null ) {
            raster = performContrastEnhancement( raster, styling.contrastEnhancement );
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

        if ( styling.imageOutline != null ) {
            LOG.trace( "Rendering image outline..." );
            Geometry geom = Raster2Feature.createPolygonGeometry( raster );
            Java2DRenderer vectorRenderer = new Java2DRenderer( graphics );
            Pair<Styling, Geometry> pair = (Pair) styling.imageOutline.evaluate( null );
            Styling ls = pair.first;
            vectorRenderer.render( ls, geom );
            LOG.trace( "Done rendering image outline." );
        }
    }

    /**
     * Performs contrast enhancement on all bands of a raster and returns the modified raster.
     * 
     * @param raster
     *            initial raster
     * @param contrastEnhancement
     * @return the enhanced raster
     */
    private AbstractRaster performContrastEnhancement( AbstractRaster raster, ContrastEnhancement contrastEnhancement ) {
        if ( contrastEnhancement == null )
            return raster;

        long start = System.nanoTime();
        LOG.trace( "Enhancing contrast for overall raster..." );
        RasterData data = raster.getAsSimpleRaster().getRasterData(), newData = data;
        RasterDataUtility rasutil = new RasterDataUtility( raster );
        rasutil.setContrastEnhancement( contrastEnhancement );
        rasutil.precomputeContrastEnhancements( -1, contrastEnhancement );
        for ( int band = 0; band < data.getBands(); band++ )
            newData = setEnhancedChannelData( newData, rasutil, band, band, contrastEnhancement );

        AbstractRaster newRaster = new SimpleRaster( newData, raster.getEnvelope(), raster.getRasterReference() );
        long end = System.nanoTime();
        LOG.trace( "Enhancing contrast for overall raster done. ({} ms)", ( end - start ) / 1000000 );
        return newRaster;
    }

    /**
     * Create a new raster according to the specified channel selections (after performing needed contrast
     * enhancements).
     * 
     * @param channels
     * @param raster
     */
    private AbstractRaster evaluateChannelSelections( RasterChannelSelection channels, AbstractRaster raster ) {
        if ( channels.getMode() == ChannelSelectionMode.NONE )
            return raster;
        LOG.trace( "Evaluating channel selections ..." );
        long start = System.nanoTime();
        RasterData data = raster.getAsSimpleRaster().getRasterData();
        int cols = data.getWidth(), rows = data.getHeight();
        int redIndex = channels.getRedChannelIndex(), greenIndex = channels.getGreenChannelIndex();
        int blueIndex = channels.getBlueChannelIndex(), grayIndex = channels.getGrayChannelIndex();
        RasterDataUtility rasutil = new RasterDataUtility( raster, channels );
        RasterData newData = raster.getAsSimpleRaster().getRasterData();
        BandType[] bandTypes = null;
        if ( channels.getMode() == ChannelSelectionMode.RGB && data.getBands() > 1 ) {
            bandTypes = new BandType[] { BandType.RED, BandType.GREEN, BandType.BLUE };
            newData = RasterDataFactory.createRasterData( cols, rows, bandTypes, DataType.BYTE,
                                                          data.getDataInfo().interleaveType, false );

            rasutil.precomputeContrastEnhancements( redIndex, channels.channelContrastEnhancements.get( "red" ) );
            newData = setEnhancedChannelData( newData, rasutil, redIndex, 0,
                                              channels.channelContrastEnhancements.get( "red" ) );
            rasutil.precomputeContrastEnhancements( greenIndex, channels.channelContrastEnhancements.get( "green" ) );
            newData = setEnhancedChannelData( newData, rasutil, greenIndex, 1,
                                              channels.channelContrastEnhancements.get( "green" ) );
            rasutil.precomputeContrastEnhancements( blueIndex, channels.channelContrastEnhancements.get( "blue" ) );
            newData = setEnhancedChannelData( newData, rasutil, blueIndex, 2,
                                              channels.channelContrastEnhancements.get( "blue" ) );

        }
        if ( channels.getMode() == ChannelSelectionMode.GRAY ) {
            bandTypes = new BandType[] { BandType.BAND_0 };
            newData = RasterDataFactory.createRasterData( cols, rows, bandTypes, DataType.BYTE,
                                                          data.getDataInfo().interleaveType, false );

            newData = setEnhancedChannelData( newData, rasutil, grayIndex, 0,
                                              channels.channelContrastEnhancements.get( "gray" ) );

        }
        AbstractRaster newRaster = new SimpleRaster( newData, raster.getEnvelope(), raster.getRasterReference() );
        long end = System.nanoTime();
        LOG.trace( "Output channels successfully created in {} miliseconds", ( end - start ) / 1000000 );
        return newRaster;
    }

    /**
     * Perform contrast enhancement on one channel and copy the result to a RasterData object.
     * 
     * @param newData
     *            RasterData output container
     * @param rasutil
     *            channel data source
     * @param inIndex
     *            input channel index
     * @param outIndex
     *            output channel index
     * @param enhancement
     *            ContrastEnhancement to perform
     * @return modified RasterData container
     */
    private RasterData setEnhancedChannelData( RasterData newData, RasterDataUtility rasutil, int inIndex,
                                               int outIndex, ContrastEnhancement enhancement ) {
        int i = 0, j = 0, val = 0, cols = newData.getWidth(), rows = newData.getHeight();

        rasutil.setContrastEnhancement( enhancement );
        if ( enhancement != null )
            LOG.trace( "Using gamma {} for channel '{}'...", enhancement.gamma, inIndex );
        for ( i = 0; i < cols; i++ )
            for ( j = 0; j < rows; j++ ) {
                val = (int) rasutil.getEnhanced( i, j, inIndex );
                newData.setByteSample( i, j, outIndex, int2byte( val ) );
            }

        return newData;
    }

    private static final byte int2byte( final int val ) {
        // if ( val < 0 )
        // return int2byte( val - 2 * Byte.MIN_VALUE );
        return ( val < 128 ? (byte) val : (byte) ( val + 2 * Byte.MIN_VALUE ) );
    }

    /**
     * Perform the hill-shading algorithm on a DEM raster. Based on algorithm presented at
     * http://edndoc.esri.com/arcobjects/9.2/net/shared/geoprocessing/spatial_analyst_tools/how_hillshade_works.htm
     * 
     * @param raster
     *            Input raster, containing a DEM, with R rows and C columns
     * @param style
     * @return a gray-scale raster (with bytes), with R-2 rows and C-2 columns
     */
    public AbstractRaster performHillShading( AbstractRaster raster, RasterStyling style ) {
        LOG.trace( "Performing Hill-Shading ... " );
        long start = System.nanoTime();
        int cols = raster.getColumns(), rows = raster.getRows();
        RasterDataUtility data = new RasterDataUtility( raster, style.channelSelection );
        RasterData shadeData = RasterDataFactory.createRasterData( cols - 2, rows - 2, DataType.BYTE, false );
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
