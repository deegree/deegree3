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

import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.renderable.RenderableImage;

import org.deegree.model.spatialschema.Envelope;
import org.deegree.ogcwebservices.wcs.describecoverage.CoverageOffering;

/**
 *
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class FloatGridCoverage extends AbstractGridCoverage {

    private static final long serialVersionUID = -3642652899429594623L;

    private float[][][] data = null;

    /**
     * @param coverageOffering
     * @param envelope
     * @param data
     */
    public FloatGridCoverage( CoverageOffering coverageOffering, Envelope envelope, float[][][] data ) {
        this( coverageOffering, envelope, false, data );
    }

    /**
     * @param coverageOffering
     * @param envelope
     * @param isEditable
     * @param data
     */
    public FloatGridCoverage( CoverageOffering coverageOffering, Envelope envelope, boolean isEditable, float[][][] data ) {
        super( coverageOffering, envelope, isEditable );
        this.data = data;
    }

    /**
     * @param coverageOffering
     * @param envelope
     * @param sources
     */
    public FloatGridCoverage( CoverageOffering coverageOffering, Envelope envelope, FloatGridCoverage[] sources ) {
        super( coverageOffering, envelope, sources );
    }

    /**
     * The number of sample dimensions in the coverage. For grid coverages, a sample dimension is a band.
     *
     * @return The number of sample dimensions in the coverage.
     * @UML mandatory numSampleDimensions
     */
    public int getNumSampleDimensions() {
        if ( data != null ) {
            return data.length;
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
        if ( data != null ) {

            return null;
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
     * @return the image
     */
    @Override
    public BufferedImage getAsImage( int xAxis, int yAxis ) {

        if ( xAxis <= 0 || yAxis <= 0 ) {
            // get default size if passed target size is <= 0
            Rectangle rect = calculateOriginalSize();
            xAxis = rect.width;
            yAxis = rect.height;
        }

        BufferedImage bi = new BufferedImage( xAxis, yAxis, BufferedImage.TYPE_INT_ARGB );

        if ( data != null ) {
            BufferedImage img = new BufferedImage( data[0][0].length, data[0].length, BufferedImage.TYPE_INT_ARGB );
            Raster raster = img.getData();
            DataBuffer buf = raster.getDataBuffer();
            for ( int i = 0; i < data[0][0].length; i++ ) {
                for ( int j = 0; j < data[0].length; j++ ) {
                    buf.setElem( j * data[0][0].length + i, Float.floatToIntBits( data[0][j][i] ) );
                }
            }
            img.setData( raster );
            if ( img.getWidth() != bi.getWidth() || img.getHeight() != bi.getHeight() ) {
                // just resize if source image size is different from
                // target image size
                Graphics g = bi.getGraphics();
                g.drawImage( img, 0, 0, bi.getWidth(), bi.getHeight(), null );
                g.dispose();
            }
        } else {
            // it's a complex FloatGridCoverage made up of different
            // source coverages
            for ( int i = 0; i < sources.length; i++ ) {
                BufferedImage sourceImg = ( (AbstractGridCoverage) sources[i] ).getAsImage( -1, -1 );

                bi = paintImage( bi, this.getEnvelope(), sourceImg, sources[i].getEnvelope() );
            }
        }

        return bi;
    }

    /**
     * calculates the original size of a gridcoverage based on its resolution and the envelope(s) of its source(s).
     *
     * @return the size
     */
    private Rectangle calculateOriginalSize() {
        if ( data != null ) {
            return new Rectangle( data[0][0].length, data[0].length );
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
