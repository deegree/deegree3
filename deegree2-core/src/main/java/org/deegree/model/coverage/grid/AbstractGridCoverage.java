// $HeadURL$
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
package org.deegree.model.coverage.grid;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.renderable.ParameterBlock;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.media.jai.InterpolationNearest;
import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.BootLogger;
import org.deegree.graphics.transformation.GeoTransform;
import org.deegree.graphics.transformation.WorldToScreenTransform;
import org.deegree.model.coverage.AbstractCoverage;
import org.deegree.model.coverage.Coverage;
import org.deegree.model.crs.CoordinateSystem;
import org.deegree.model.spatialschema.Envelope;
import org.deegree.ogcwebservices.wcs.configuration.Extension;
import org.deegree.ogcwebservices.wcs.describecoverage.CoverageOffering;
import org.deegree.processing.raster.converter.Image2RawData;

/**
 * Represent the basic implementation which provides access to grid coverage data. A <code>GC_GridCoverage</code>
 * implementation may provide the ability to update grid values.
 * 
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @version 2.11.2002
 */

public abstract class AbstractGridCoverage extends AbstractCoverage implements GridCoverage {

    private static final long serialVersionUID = 7719709130950357397L;

    private static final ILogger LOG = LoggerFactory.getLogger( AbstractGridCoverage.class );

    private final GridGeometry gridGeometry = null;

    private boolean isEditable = false;

    protected static float offset;

    protected static float scaleFactor;

    static {
        // 16 bit coverage probably does not contain original values but scaled values
        // with an offset to enable handling of float values. For correct handling of
        // these coverages offset and scale factor must be known
        InputStream is = ShortGridCoverage.class.getResourceAsStream( "16bit.properties" );
        Properties props = new Properties();
        try {
            props.load( is );
        } catch ( IOException e ) {
            BootLogger.logError( e.getMessage(), e );
        }
        offset = Float.parseFloat( props.getProperty( "offset" ) );
        scaleFactor = Float.parseFloat( props.getProperty( "scaleFactor" ) );
    }

    /**
     * @param coverageOffering
     * @param envelope
     */
    public AbstractGridCoverage( CoverageOffering coverageOffering, Envelope envelope ) {
        super( coverageOffering, envelope );
    }

    /**
     * @param coverageOffering
     * @param sources
     * @param envelope
     */
    public AbstractGridCoverage( CoverageOffering coverageOffering, Envelope envelope, Coverage[] sources ) {
        super( coverageOffering, envelope, sources );
    }

    /**
     * 
     * @param coverageOffering
     * @param envelope
     * @param isEditable
     */
    public AbstractGridCoverage( CoverageOffering coverageOffering, Envelope envelope, boolean isEditable ) {
        super( coverageOffering, envelope );
        this.isEditable = isEditable;
    }

    /**
     * 
     * @param coverageOffering
     * @param envelope
     * @param crs
     * @param isEditable
     */
    public AbstractGridCoverage( CoverageOffering coverageOffering, Envelope envelope, CoordinateSystem crs,
                                 boolean isEditable ) {
        super( coverageOffering, envelope, null, crs );
        this.isEditable = isEditable;
    }

    /**
     * 
     * @param coverageOffering
     * @param envelope
     * @param sources
     * @param isEditable
     */
    public AbstractGridCoverage( CoverageOffering coverageOffering, Envelope envelope, Coverage[] sources,
                                 boolean isEditable ) {
        super( coverageOffering, envelope, sources );
        this.isEditable = isEditable;
    }

    /**
     * 
     * @param coverageOffering
     * @param envelope
     * @param sources
     * @param crs
     * @param isEditable
     */
    public AbstractGridCoverage( CoverageOffering coverageOffering, Envelope envelope, Coverage[] sources,
                                 CoordinateSystem crs, boolean isEditable ) {
        super( coverageOffering, envelope, sources, crs );
        this.isEditable = isEditable;
    }

    /**
     * Returns <code>true</code> if grid data can be edited.
     * 
     * @return <code>true</code> if grid data can be edited.
     */
    public boolean isDataEditable() {
        return isEditable;
    }

    /**
     * Information for the grid coverage geometry. Grid geometry includes the valid range of grid coordinates and the
     * georeferencing.
     * 
     * @return the information for the grid coverage geometry.
     * 
     */
    public GridGeometry getGridGeometry() {
        return gridGeometry;
    }

    /**
     * this is a deegree convenience method which returns the source image of an <tt>ImageGridCoverage</tt>. In procipal
     * the same can be done with the getRenderableImage(int xAxis, int yAxis) method. but creating a
     * <tt>RenderableImage</tt> image is very slow.
     * 
     * @param xAxis
     *            Dimension to use for the <var>x</var> axis.
     * @param yAxis
     *            Dimension to use for the <var>y</var> axis.
     * @return the image
     */
    abstract public BufferedImage getAsImage( int xAxis, int yAxis );

    protected BufferedImage paintImage( BufferedImage targetImg, Envelope targetEnv, BufferedImage sourceImg,
                                        Envelope sourceEnv ) {
        return this.paintImage( targetImg, null, targetEnv, sourceImg, sourceEnv );
    }

    /**
     * renders a source image onto the correct position of a target image according to threir geographic extends
     * (Envelopes).
     * 
     * @param targetImg
     * @param targetEnv
     * @param sourceImg
     * @param sourceEnv
     * @return targetImg with sourceImg rendered on
     */
    protected BufferedImage paintImage( BufferedImage targetImg, float[][] data, Envelope targetEnv,
                                        BufferedImage sourceImg, Envelope sourceEnv ) {

        int targetImgWidth = targetImg.getWidth();
        int targetImgHeight = targetImg.getHeight();
        GeoTransform gt = new WorldToScreenTransform( targetEnv.getMin().getX(), targetEnv.getMin().getY(),
                                                      targetEnv.getMax().getX(), targetEnv.getMax().getY(), 0, 0,
                                                      targetImgWidth - 1, targetImgHeight - 1 );

        // border pixel coordinates of the source image in the target coordinate system
        int x1 = (int) Math.round( gt.getDestX( sourceEnv.getMin().getX() ) );
        int y1 = (int) Math.round( gt.getDestY( sourceEnv.getMax().getY() ) );
        int x2 = (int) Math.round( gt.getDestX( sourceEnv.getMax().getX() ) );
        int y2 = (int) Math.round( gt.getDestY( sourceEnv.getMin().getY() ) );

        if ( Math.abs( x2 - x1 ) <= 0 && Math.abs( y2 - y1 ) <= 0 || sourceImg.getWidth() == 1
             || sourceImg.getHeight() == 1 ) {
            // nothing to copy, return targetImg unchanged
            return targetImg;
        }

        sourceImg = scale( sourceImg, targetImg, sourceEnv, targetEnv );

        int srcPs = sourceImg.getColorModel().getPixelSize();
        int targetPs = targetImg.getColorModel().getPixelSize();
        if ( targetPs == 16 && srcPs == 16 ) {
            LOG.logDebug( "Painting from 16bpp to 16bpp" );
            paintImageT16S16( targetImg, data, sourceImg, x1, y1, x2, y2 );
        } else if ( targetPs == 16 && srcPs == 32 ) {
            LOG.logDebug( "Painting from 32bpp to 16bpp" );
            paintImageT16S32( targetImg, sourceImg, x1, y1, x2, y2 );
        } else if ( targetPs == 32 && srcPs == 16 ) {
            LOG.logDebug( "Painting from 16bpp to 32bpp" );
            paintImageT32S16( targetImg, sourceImg, x1, y1, x2, y2 );
        } else {
            LOG.logDebug( "Painting 'default'" );
            paintImageDefault( targetImg, sourceImg, x1, y1, x2, y2 );
        }

        return targetImg;
    }

    /**
     * Paint overlapping area from sourceImg into targetImg. This method works with 16bit target pixel and 32bit source
     * pixel.
     * 
     * @param targetImg
     *            the target image.
     * @param sourceImg
     *            the source image.
     * @param xt1
     *            x-coordinate of the first Pixel of sourceImg in targetImg
     * @param yt1
     *            y-coordinate of the first Pixel of sourceImg in targetImg
     * @param xt2
     *            x-coordinate of the last Pixel of sourceImg in targetImg
     * @param yt2
     *            y-coordinate of the last Pixel of sourceImg in targetImg
     */
    private void paintImageT16S32( BufferedImage targetImg, BufferedImage sourceImg, int xt1, int yt1, int xt2, int yt2 ) {

        assert sourceImg.getColorModel().getPixelSize() == 32;
        assert targetImg.getColorModel().getPixelSize() == 16;

        // locals for performance reasons
        int targetImgWidth = targetImg.getWidth();
        int targetImgHeight = targetImg.getHeight();
        int sourceImgWidth = sourceImg.getWidth();
        int sourceImgHeight = sourceImg.getHeight();

        Raster raster = targetImg.getData();
        DataBuffer targetBuffer = raster.getDataBuffer();

        // i is the running x coordinate of the overlapping area in the source image
        // j is the running y coordinate of the overlapping area in the source image
        // targetX is the running x coordinate of the overlapping area in the target image
        // targetY is the running y coordinate of the overlapping area in the target image
        Extension extension = getCoverageOffering().getExtension();
        float scaleFactor = (float) extension.getScaleFactor();
        float offset = (float) extension.getOffset();
        for ( int i = Math.max( 0, sourceImgWidth - 1 - xt2 ); i <= Math.min( targetImgWidth - 1 - xt1,
                                                                              sourceImgWidth - 1 ); i++ ) {
            int targetX = xt1 + i;
            for ( int j = Math.max( 0, sourceImgHeight - 1 - yt2 ); j <= Math.min( targetImgHeight - 1 - yt1,
                                                                                   sourceImgHeight - 1 ); j++ ) {
                int targetY = yt1 + j;
                int targetPos = targetImgWidth * targetY + targetX;
                int v = sourceImg.getRGB( i, j );
                float f = Float.intBitsToFloat( v ) * scaleFactor + offset;
                targetBuffer.setElem( targetPos, Math.round( f ) );
            }
        }
        targetImg.setData( Raster.createRaster( targetImg.getSampleModel(), targetBuffer, null ) );
    }

    /**
     * Paint overlapping area from sourceImg into targetImg. This method works with 32bit target pixel and 16bit source
     * pixel.
     * 
     * @param targetImg
     *            the target image.
     * @param sourceImg
     *            the source image.
     * @param xt1
     *            x-coordinate of the first Pixel of sourceImg in targetImg
     * @param yt1
     *            y-coordinate of the first Pixel of sourceImg in targetImg
     * @param xt2
     *            x-coordinate of the last Pixel of sourceImg in targetImg
     * @param yt2
     *            y-coordinate of the last Pixel of sourceImg in targetImg
     */
    private void paintImageT32S16( BufferedImage targetImg, BufferedImage sourceImg, int xt1, int yt1, int xt2, int yt2 ) {

        assert sourceImg.getColorModel().getPixelSize() == 16;
        assert targetImg.getColorModel().getPixelSize() == 32;

        // locals for performance reasons
        int targetImgWidth = targetImg.getWidth();
        int targetImgHeight = targetImg.getHeight();
        int sourceImgWidth = sourceImg.getWidth();
        int sourceImgHeight = sourceImg.getHeight();

        Raster raster = targetImg.getData();
        DataBuffer targetBuffer = raster.getDataBuffer();
        // Rectangle targetRect = raster.getBounds(); // I assume that the created image ALWAYS has a 0, 0, x, y rect.
        // If not, use this one...
        raster = sourceImg.getData();
        DataBuffer srcBuffer = raster.getDataBuffer();
        Rectangle srcRect = raster.getBounds(); // need to use this for data access, as it may not be 0, 0, x, y but -5,
        // -29, x+5, y+29 or something and the srcPos would be messed up

        // i is the running x coordinate of the overlapping area in the source image
        // j is the running y coordinate of the overlapping area in the source image
        // targetX is the running x coordinate of the overlapping area in the target image
        // targetY is the running y coordinate of the overlapping area in the target image
        Extension extension = getCoverageOffering().getExtension();
        float scaleFactor = (float) extension.getScaleFactor();
        float offset = (float) extension.getOffset();

        int dstX = Math.max( 0, sourceImgWidth - 1 - xt2 );
        int maxWidth = Math.min( targetImgWidth - xt1 - 1, sourceImgWidth - 1 );
        int dstY = Math.max( 0, sourceImgHeight - 1 - yt2 );
        int maxHeight = Math.min( targetImgHeight - yt1 - 1, sourceImgHeight - 1 );

        for ( int i = dstX; i <= maxWidth; i++ ) {
            int targetX = xt1 + i;
            for ( int j = dstY; j <= maxHeight; j++ ) {
                int targetY = yt1 + j;
                int srcPos = srcRect.width * j + i - srcRect.x - srcRect.y * srcRect.width;
                int targetPos = targetImgWidth * targetY + targetX;

                float f = srcBuffer.getElem( srcPos ) / scaleFactor - offset;
                targetBuffer.setElem( targetPos, Float.floatToIntBits( f ) );
            }
        }
        targetImg.setData( Raster.createRaster( targetImg.getSampleModel(), targetBuffer, null ) );
    }

    /**
     * Paint overlapping area from sourceImg into targetImg. This method works with 16bit target pixel and 16bit source
     * pixel.
     * 
     * @param targetImg
     *            the target image.
     * @param data
     * @param sourceImg
     *            the source image.
     * @param xt1
     *            x-coordinate of the first Pixel of sourceImg in targetImg
     * @param yt1
     *            y-coordinate of the first Pixel of sourceImg in targetImg
     * @param xt2
     *            x-coordinate of the last Pixel of sourceImg in targetImg
     * @param yt2
     *            y-coordinate of the last Pixel of sourceImg in targetImg
     */
    private void paintImageT16S16( BufferedImage targetImg, float[][] data, BufferedImage sourceImg, int xt1, int yt1,
                                   int xt2, int yt2 ) {
        assert sourceImg.getColorModel().getPixelSize() == 16;
        assert targetImg.getColorModel().getPixelSize() == 16;

        // locals for performance reasons
        int targetImgWidth = targetImg.getWidth();
        int targetImgHeight = targetImg.getHeight();
        int sourceImgWidth = sourceImg.getWidth();
        int sourceImgHeight = sourceImg.getHeight();

        Raster raster = targetImg.getData();
        DataBuffer targetBuffer = raster.getDataBuffer();
        raster = sourceImg.getData();
        float[][] newData = null;
        Image2RawData i2r = new Image2RawData( sourceImg, 1f / scaleFactor, -1 * offset );
        newData = i2r.parse();

        // i is the running x coordinate of the overlapping area in the source image
        // j is the running y coordinate of the overlapping area in the source image
        // targetX is the running x coordinate of the overlapping area in the target image
        // targetY is the running y coordinate of the overlapping area in the target image
        for ( int i = Math.max( 0, sourceImgWidth - xt2 - 1 ); i <= Math.min( targetImgWidth - xt1 - 1,
                                                                              sourceImgWidth - 1 ); i++ ) {
            int targetX = xt1 + i;
            for ( int j = Math.max( 0, sourceImgHeight - yt2 - 1 ); j <= Math.min( targetImgHeight - yt1 - 1,
                                                                                   sourceImgHeight - 1 ); j++ ) {
                int targetY = yt1 + j;
                // int v = srcBuffer.getElem( srcPos );
                // targetBuffer.setElem( targetPos, v );
                data[targetY][targetX] = newData[j][i];
            }
        }
        targetImg.setData( Raster.createRaster( targetImg.getSampleModel(), targetBuffer, null ) );
    }

    /**
     * Paint overlapping area from sourceImg into targetImg. This method works with every combination of pixel size in
     * the target and source image.
     * 
     * @param targetImg
     * @param sourceImg
     * @param xt1
     *            x-coordinate of the first Pixel of sourceImg in targetImg
     * @param yt1
     *            y-coordinate of the first Pixel of sourceImg in targetImg
     * @param xt2
     *            x-coordinate of the last Pixel of sourceImg in targetImg
     * @param yt2
     *            y-coordinate of the last Pixel of sourceImg in targetImg
     */
    private void paintImageDefault( BufferedImage targetImg, BufferedImage sourceImg, int xt1, int yt1, int xt2, int yt2 ) {
        // int xs1 = max( 0, sourceImg.getWidth() - 1 - xt2 );
        // int xs2 = min( targetImg.getWidth() - 1 - xt1, sourceImg.getWidth() - 1 );
        // int ys1 = max( 0, sourceImg.getHeight() - 1 - yt2 );
        // int ys2 = min( targetImg.getHeight() - 1 - yt1, sourceImg.getHeight() - 1 );
        // int copyWidth = ( xs2 - xs1 + 1 );
        // int copyHeight = ( ys2 - ys1 + 1 );
        // int[] rgbs = new int[copyWidth * copyHeight];
        // sourceImg.getRGB( xs1, ys1, copyWidth, copyHeight, rgbs, 0, copyWidth );
        // targetImg.setRGB( xt1, yt1, copyWidth, copyHeight, rgbs, 0, copyWidth );

        Graphics g = targetImg.getGraphics();
        /*
         * if ( sourceImg.getColorModel().getPixelSize() == 32 && targetImg.getColorModel().getPixelSize() == 32 ) { (
         * (Graphics2D) g ).setComposite( AlphaComposite.DstOver ); }
         */
        g.drawImage( sourceImg, xt1, yt1, sourceImg.getWidth(), sourceImg.getHeight(), null );
    }

    private BufferedImage scale( BufferedImage sourceImg, BufferedImage targetImg, Envelope srcEnv, Envelope trgEnv ) {
        double sw = sourceImg.getWidth() - 1;
        if ( sourceImg.getWidth() == 1 ) {
            sw = 1f;
        }
        double sh = sourceImg.getHeight() - 1;
        if ( sourceImg.getHeight() == 1 ) {
            sh = 1f;
        }
        double srcXres = srcEnv.getWidth() / sw;
        double srcYres = srcEnv.getHeight() / sh;

        double tw = targetImg.getWidth() - 1;
        if ( targetImg.getWidth() == 1 ) {
            tw = 1;
        }
        double th = targetImg.getHeight() - 1;
        if ( targetImg.getHeight() == 1 ) {
            th = 1;
        }
        double trgXres = trgEnv.getWidth() / tw;
        double trgYres = trgEnv.getHeight() / th;

        float sx = (float) ( srcXres / trgXres );
        float sy = (float) ( srcYres / trgYres );

        if ( ( sy < 0.9999 ) || ( sy > 1.0001 ) || ( sx < 0.9999 ) || ( sx > 1.0001 ) ) {
            try {
                ParameterBlock pb = new ParameterBlock();
                pb.addSource( sourceImg );

                LOG.logDebug( "Scale image: by factors: " + sx + ' ' + sy );
                pb.add( sx ); // The xScale
                pb.add( sy ); // The yScale
                pb.add( 0.0F ); // The x translation
                pb.add( 0.0F ); // The y translation
                pb.add( new InterpolationNearest() ); // The interpolation
                // pb.add( new InterpolationBilinear() ); // The interpolation
                // Create the scale operation
                RenderedOp ro = JAI.create( "scale", pb, null );
                sourceImg = ro.getAsBufferedImage();
            } catch ( Exception e ) {
                LOG.logDebug( e.getMessage(), e );
            }
        }
        return sourceImg;
    }
}
