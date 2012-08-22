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
package org.deegree.graphics.displayelements;

import static java.lang.Math.max;
import static java.lang.Math.min;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.Pair;
import org.deegree.graphics.sld.Categorize;
import org.deegree.graphics.sld.Interpolate;
import org.deegree.graphics.sld.RasterSymbolizer;
import org.deegree.graphics.transformation.GeoTransform;
import org.deegree.model.coverage.grid.GridCoverage;
import org.deegree.model.coverage.grid.ImageGridCoverage;
import org.deegree.model.spatialschema.Envelope;
import org.deegree.ogcwebservices.wms.operation.DimensionValues;
import org.deegree.ogcwebservices.wms.operation.GetMap;
import org.deegree.processing.raster.converter.Image2RawData;

/**
 * 
 * 
 * 
 * @version $Revision$
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version 1.0. $Revision$, $Date$
 * 
 * @since 2.0
 */
public class RasterDisplayElement extends AbstractDisplayElement {

    private static final long serialVersionUID = -5195730721807600940L;

    private static ILogger LOG = LoggerFactory.getLogger( RasterDisplayElement.class );

    private RasterSymbolizer symbolizer = null;

    private GridCoverage gc = null;

    private GetMap request;

    /**
     * Creates a new RasterDisplayElement_Impl object.
     * 
     * @param gc
     *            raster
     */
    RasterDisplayElement( GridCoverage gc ) {
        setRaster( gc );
        symbolizer = new RasterSymbolizer();
    }

    /**
     * Creates a new RasterDisplayElement_Impl object.
     * 
     * @param gc
     *            raster
     * @param symbolizer
     */
    RasterDisplayElement( GridCoverage gc, RasterSymbolizer symbolizer ) {
        setRaster( gc );
        this.symbolizer = symbolizer;
    }

    RasterDisplayElement( GridCoverage gc, RasterSymbolizer symbolizer, GetMap request ) {
        this.request = request;
        setRaster( gc );
        this.symbolizer = symbolizer;
    }

    private void paintCoverage( Graphics2D g, float[][] data, int minx, int miny, int maxx, int maxy,
                                BufferedImage image ) {
        Categorize categorize = symbolizer == null ? null : symbolizer.getCategorize();
        Interpolate interpolate = symbolizer == null ? null : symbolizer.getInterpolate();

        LOG.logDebug( categorize == null ? "Not using categorization symbolizer." : "Using categorization symbolizer." );
        LOG.logDebug( interpolate == null ? "Not using interpolation symbolizer." : "Using interpolation symbolizer." );

        int opac = symbolizer == null ? 1 : ( (int) ( 255 * symbolizer.getOpacity() ) << 24 );

        int width = maxx - minx;
        int height = maxy - miny;

        BufferedImage img = new BufferedImage( data[0].length, data.length, BufferedImage.TYPE_INT_ARGB );

        DimensionValues vals = request == null ? null : request.getDimElev();

        for ( int x = 0; x < data[0].length; ++x ) {
            for ( int y = 0; y < data.length; ++y ) {
                float d = data[y][x];
                int val = 0;

                if ( ( vals != null && vals.includesValue( d ) ) || vals == null ) {
                    if ( categorize == null && interpolate == null ) {
                        val = image.getRGB( x, y );
                        val = val & 0xffffff + opac;
                    } else if ( categorize != null ) {
                        val = categorize.categorize( d, opac );
                    } else if ( interpolate != null ) {
                        val = interpolate.interpolate( d, opac );
                    }
                }

                img.setRGB( x, y, val );
            }
        }

        if ( symbolizer != null && symbolizer.getShaded() ) {
            BufferedImage old = img;

            Pair<Integer, float[]> pair = symbolizer.getShadeKernel();
            Kernel kernel = new Kernel( 3, 3, new float[] { -1, -1, -1, -1, 10, -1, -1, -1, -1 } );
            ConvolveOp op = new ConvolveOp( kernel );
            img = op.filter( img, null );
            kernel = new Kernel( pair.first, pair.first, pair.second );
            op = new ConvolveOp( kernel );
            img = op.filter( img, null );

            // this post processing is necessary to remove pixels that were filtered out by the
            // ELEVATION parameter
            // value and to fix up artifacts from the shade operations above
            for ( int y = 0; y < old.getHeight(); ++y ) {
                for ( int x = 0; x < old.getWidth(); ++x ) {
                    int oldVal = old.getRGB( x, y );
                    int newVal = img.getRGB( x, y );
                    if ( oldVal == 0 ) {
                        img.setRGB( x, y, 0 );
                    } else {
                        if ( ( newVal & 0xff000000 ) == 0 ) {
                            img.setRGB( x, y, oldVal );
                        } else if ( ( newVal & 0xffffff ) == 0xffffff ) {
                            img.setRGB( x, y, oldVal );
                        }
                    }
                }
            }

        }

        // attention: this is not gamma, but brightness (and a hack for iGeoDesktop)
        if ( symbolizer != null && symbolizer.getGamma() != 0 ) {
            for ( int y = 0; y < img.getHeight(); ++y ) {
                for ( int x = 0; x < img.getWidth(); ++x ) {
                    int oldVal = img.getRGB( x, y );
                    int gamma = (int) symbolizer.getGamma();
                    Color c = new Color( oldVal, true );
                    Color newC = new Color( min( 255, max( 0, c.getRed() + gamma ) ), min( 255, max( 0, c.getGreen()
                                                                                                        + gamma ) ),
                                            min( 255, max( 0, c.getBlue() + gamma ) ), c.getAlpha() );
                    img.setRGB( x, y, newC.getRGB() );
                }
            }
        }

        g.drawImage( img, minx, miny, width, height, null );
    }

    /**
     * renders the DisplayElement to the submitted graphic context
     * 
     */
    public void paint( Graphics g, GeoTransform projection, double scale ) {
        synchronized ( symbolizer ) {
            try {
                if ( doesScaleConstraintApply( scale ) ) {
                    Envelope env = gc.getEnvelope();
                    int minx = (int) ( projection.getDestX( env.getMin().getX() ) );
                    int maxy = (int) ( projection.getDestY( env.getMin().getY() ) );
                    int maxx = (int) ( projection.getDestX( env.getMax().getX() ) );
                    int miny = (int) ( projection.getDestY( env.getMax().getY() ) );

                    if ( gc instanceof ImageGridCoverage ) {
                        ImageGridCoverage igc = (ImageGridCoverage) gc;
                        Graphics2D g2 = (Graphics2D) g;
                        BufferedImage image = igc.getAsImage( -1, -1 );

                        if ( symbolizer.isDefault() && ( request == null || request.getDimElev() == null ) ) {
                            if ( symbolizer.getGamma() != 1 ) {
                                Kernel kernel = new Kernel( 1, 1, new float[] { (float) symbolizer.getGamma() } );
                                image = new ConvolveOp( kernel ).filter( image, null );
                            }
                            g2.drawImage( image, minx, miny, maxx - minx, maxy - miny, null );
                        } else {
                            if ( symbolizer.scaleValid( scale ) ) {
                                paintCoverage( g2, new Image2RawData( image ).parse(), minx, miny, maxx, maxy, image );
                            }
                        }
                    } else {
                        // TODO
                        // handle other grid coverages
                    }
                }
            } catch ( Exception e ) {
                LOG.logError( e.getMessage(), e );
                throw new RuntimeException( e.getMessage(), e );
            }
        }
    }

    /**
     * returns the content of the <code>RasterDisplayElement</code>
     * 
     * @return gird coverage
     */
    public GridCoverage getRaster() {
        return gc;
    }

    /**
     * sets the grid coverage that represents the content of the <code>RasterDisplayElement</code>
     * 
     * @param gc
     * 
     */
    public void setRaster( GridCoverage gc ) {
        this.gc = gc;
    }

    @Override
    public boolean doesScaleConstraintApply( double scale ) {
        return symbolizer.scaleValid( scale );
    }

}
