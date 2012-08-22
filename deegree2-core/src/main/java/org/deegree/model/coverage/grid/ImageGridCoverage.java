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
package org.deegree.model.coverage.grid;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.renderable.ParameterBlock;
import java.awt.image.renderable.RenderableImage;

import javax.media.jai.Interpolation;
import javax.media.jai.InterpolationNearest;
import javax.media.jai.JAI;
import javax.media.jai.RenderableGraphics;
import javax.media.jai.RenderedOp;

import org.deegree.model.crs.CoordinateSystem;
import org.deegree.model.spatialschema.Envelope;
import org.deegree.ogcwebservices.wcs.describecoverage.CoverageOffering;

/**
 * GridCoverage implementation for holding grids stored in an <tt>BufferedImage</tt> or in a set of
 * <tt>ImageGridCoverage</tt>s
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class ImageGridCoverage extends AbstractGridCoverage {

    private static final long serialVersionUID = -531939507044569726L;

    private transient BufferedImage image = null;

    /**
     *
     * @param coverageOffering
     * @param envelope
     * @param image
     */
    public ImageGridCoverage( CoverageOffering coverageOffering, Envelope envelope, BufferedImage image ) {
        this( coverageOffering, envelope, false, image );
    }

    /**
     *
     * @param coverageOffering
     * @param envelope
     * @param isEditable
     * @param image
     */
    public ImageGridCoverage( CoverageOffering coverageOffering, Envelope envelope, boolean isEditable,
                              BufferedImage image ) {
        super( coverageOffering, envelope, isEditable );
        this.image = image;
    }

    /**
     *
     * @param coverageOffering
     * @param envelope
     * @param crs
     * @param isEditable
     * @param image
     */
    public ImageGridCoverage( CoverageOffering coverageOffering, Envelope envelope, CoordinateSystem crs,
                              boolean isEditable, BufferedImage image ) {
        super( coverageOffering, envelope, crs, isEditable );
        this.image = image;
    }

    /**
     *
     * @param coverageOffering
     * @param envelope
     * @param sources
     */
    public ImageGridCoverage( CoverageOffering coverageOffering, Envelope envelope, ImageGridCoverage[] sources ) {
        super( coverageOffering, envelope, sources );
    }

    /**
     * The number of sample dimensions in the coverage. For grid coverages, a sample dimension is a band.
     *
     * @return The number of sample dimensions in the coverage.
     */
    public int getNumSampleDimensions() {
        if ( image != null ) {
            return image.getData().getNumBands();
        }
        return sources[0].getNumSampleDimensions();
    }

    /**
     * Returns 2D view of this coverage as a renderable image. This optional operation allows interoperability with <A
     * HREF="http://java.sun.com/products/java-media/2D/">Java2D</A>. If this coverage is a
     * {@link "org.opengis.coverage.grid.GridCoverage"} backed by a {@link java.awt.image.RenderedImage}, the underlying
     * image can be obtained with:
     *
     * <code>getRenderableImage(0,1).{@linkplain RenderableImage#createDefaultRendering()
     * createDefaultRendering()}</code>
     *
     * @param xAxis
     *            Dimension to use for the <var>x</var> axis.
     * @param yAxis
     *            Dimension to use for the <var>y</var> axis.
     * @return A 2D view of this coverage as a renderable image.
     * @throws UnsupportedOperationException
     *             if this optional operation is not supported.
     * @throws IndexOutOfBoundsException
     *             if <code>xAxis</code> or <code>yAxis</code> is out of bounds.
     */
    @Override
    public RenderableImage getRenderableImage( int xAxis, int yAxis )
                            throws UnsupportedOperationException, IndexOutOfBoundsException {
        if ( image != null ) {
            if ( xAxis > 0 && yAxis > 0 ) {
                Rectangle rect = new Rectangle( xAxis, yAxis );
                RenderableGraphics rg = new RenderableGraphics( rect );
                rg.drawImage( image, 0, 0, xAxis, yAxis, null );
                return rg;
            }
            Rectangle rect = new Rectangle( image.getWidth(), image.getHeight() );
            RenderableGraphics rg = new RenderableGraphics( rect );
            rg.drawImage( image, 0, 0, null );
            return rg;
        }
        // TODO if multi images -> sources.length > 0
        return null;
    }

    /**
     * this is a deegree convenience method which returns the source image of an <tt>ImageGridCoverage</tt>. In procipal
     * the same can be done with the getRenderableImage(int xAxis, int yAxis) method. but creating a
     * <tt>RenderableImage</tt> image is very slow. I xAxis or yAxis <= 0 then the size of the returned image will be
     * calculated from the source images of the coverage.
     *
     * @param xAxis
     *            Dimension to use for the <var>x</var> axis.
     * @param yAxis
     *            Dimension to use for the <var>y</var> axis.
     * @return the source image of an <tt>ImageGridCoverage</tt>.
     */
    @Override
    public BufferedImage getAsImage( int xAxis, int yAxis ) {

        if ( xAxis <= 0 || yAxis <= 0 ) {
            // get default size if passed target size is <= 0
            Rectangle rect = calculateOriginalSize();
            xAxis = rect.width;
            yAxis = rect.height;
        }

        BufferedImage bi = null;
        if ( image != null ) {
            if ( xAxis == image.getWidth() && yAxis == image.getHeight() ) {
                bi = image;
            } else {
                // it's a simple ImageGridCoverage just made up of one image
                ParameterBlock pb = new ParameterBlock();
                pb.addSource( image );
                pb.add( xAxis / (float) image.getWidth() ); // The xScale
                pb.add( yAxis / (float) image.getHeight() ); // The yScale
                pb.add( 0.0F ); // The x translation
                pb.add( 0.0F ); // The y translation
                Interpolation interpolation = new InterpolationNearest();
                pb.add( interpolation ); // The interpolation
                // Create the scale operation
                RenderedOp ro = JAI.create( "scale", pb, null );
                bi = ro.getAsBufferedImage();
            }
        } else {
            String natFrm = coverageOffering.getSupportedFormats().getNativeFormat().getCode();
            if ( "jpg".equalsIgnoreCase( natFrm ) || "jpeg".equalsIgnoreCase( natFrm )
                 || "bmp".equalsIgnoreCase( natFrm ) ) {
                bi = new BufferedImage( xAxis, yAxis, BufferedImage.TYPE_INT_RGB );
            } else {
                bi = new BufferedImage( xAxis, yAxis, BufferedImage.TYPE_INT_ARGB );
            }
            // it's a complex ImageGridCoverage made up of different
            // source coverages
            if ( sources == null || sources.length == 0 ) {
                return bi;
            }

            for ( int i = 0; i < sources.length; i++ ) {
                BufferedImage sourceImg = ( (ImageGridCoverage) sources[i] ).getAsImage( -1, -1 );
                bi = paintImage( bi, getEnvelope(), sourceImg, sources[i].getEnvelope() );
            }
        }

        return bi;
    }

    /**
     * calculates the original size of a gridcoverage based on its resolution and the envelope(s) of its source(s).
     *
     */
    private Rectangle calculateOriginalSize() {
        if ( image != null ) {
            return new Rectangle( image.getWidth(), image.getHeight() );
        }
        BufferedImage bi = ( (ImageGridCoverage) sources[0] ).getAsImage( -1, -1 );
        Envelope env = sources[0].getEnvelope();
        double dx = env.getWidth() / bi.getWidth();
        double dy = env.getHeight() / bi.getHeight();
        env = this.getEnvelope();
        int w = (int) Math.round( env.getWidth() / dx );
        int h = (int) Math.round( env.getHeight() / dy );
        return new Rectangle( w, h );
    }

}
