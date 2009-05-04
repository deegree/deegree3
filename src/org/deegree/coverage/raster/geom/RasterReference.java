//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------
 This file is part of deegree.
 Copyright (C) 2001-2009 by:
 Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 Lesser General Public License for more details.
 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 Contact:

 Andreas Poth
 lat/lon GmbH
 Aennchenstr. 19
 53177 Bonn
 Germany
 E-Mail: poth@lat-lon.de

 Prof. Dr. Klaus Greve
 Department of Geography
 University of Bonn
 Meckenheimer Allee 166
 53115 Bonn
 Germany
 E-Mail: greve@giub.uni-bonn.de
 ---------------------------------------------------------------------------*/
package org.deegree.coverage.raster.geom;

import static java.lang.Math.abs;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.signum;

import org.deegree.crs.CRS;
import org.deegree.crs.components.Axis;
import org.deegree.crs.exceptions.UnknownCRSException;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.GeometryFactory;
import org.deegree.geometry.GeometryFactoryCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class maps a 2D raster to another cartesian world coordinate system.
 * <p>
 * Please note that there are two different ways to associate the corner points of the raster envelope with the raster
 * points in use:
 * <ul>
 * <li>CENTER: The envelope corner points coincide with the center position of the raster corner points. This
 * interpretation is used by the WCS specification.</li>
 * <li>OUTER: The envelope corner points coincide with the outer boundary of the "area" that the raster corner points
 * cover. This interpretation is image-oriented and used by the WMS specification.</li>
 * </ul>
 * </p>
 * 
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class RasterReference {

    private static Logger LOG = LoggerFactory.getLogger( RasterReference.class );

    private double x0;

    private double y0;

    private double xRes;

    private double yRes;

    private boolean axisSwitched; //TODO see if the axisSwitching signaling (when getEnvelope() should be placed here) 

    private double delta;

    private static final double DELTA_SCALE = 10e-6;

    private static final double INV_DELTA_SCALE = 10e6;

    private RasterReference() {
        this( 0.0, 0.0, 1.0, -1.0 );
    }

    /**
     * This type specifies where the upper-left coordinate lies.
     */
    public enum Type {
        /** upper left corner of the pixel */
        OUTER,
        /** center of the pixel */
        CENTER
    }

    /**
     * Creates a new RasterReference with origin and resolution
     * 
     * @param x0
     *            x world coordinate of the upper-left pixel (center)
     * @param y0
     *            y world coordinate of the upper-left pixel (center)
     * @param xRes
     *            width of a pixel in world coordinates
     * @param yRes
     *            height of a pixel in world coordinates
     */
    public RasterReference( double x0, double y0, double xRes, double yRes ) {
        this( Type.CENTER, x0, y0, xRes, yRes );
    }

    /**
     * Creates a new RasterReference with origin and resolution
     * 
     * @param type
     *            type where the x, y coordinates lies
     * @param x0
     *            x world coordinate of the upper-left pixel
     * @param y0
     *            y world coordinate of the upper-left pixel
     * @param xRes
     *            width of a pixel in world coordinates
     * @param yRes
     *            height of a pixel in world coordinates
     */
    public RasterReference( Type type, double x0, double y0, double xRes, double yRes ) {
        if ( type == Type.CENTER ) {
            this.x0 = x0 - xRes / 2;
            this.y0 = y0 - yRes / 2;
        } else if ( type == Type.OUTER ) {
            this.x0 = x0;
            this.y0 = y0;
        }
        this.xRes = xRes;
        this.yRes = yRes;
        this.delta = Math.abs( xRes * DELTA_SCALE );
    }

    /**
     * Creates a new RasterReference for given Envelope and size (yRes is negative)
     * 
     * @param env
     * @param width
     * @param height
     */
    public RasterReference( Envelope env, int width, int height ) {
        //TODO take in account the envelope's CRS axes orientation & order
        this.x0 = env.getMin().getX();
        this.y0 = env.getMax().getY();



        this.xRes = env.getWidth() / width;
        this.yRes = -1 * env.getHeight() / height;
        this.delta = Math.abs( xRes * DELTA_SCALE );
    }

    /**
     * Returns a new RasterReference with new raster size (resolution)
     * 
     * @param env
     *            envelope for the RasterReference (origin and world size)
     * @param width
     *            raster width in pixel
     * @param height
     *            raster height in pixel
     * @return new resized RasterReference
     */
    public RasterReference createResizedEnvelope( Envelope env, int width, int height ) {
      //TODO take in account the envelope's CRS axes orientation & order
        RasterReference result = new RasterReference();
        result.xRes = env.getWidth() / width * signum( this.xRes );
        result.yRes = env.getHeight() / height * signum( this.yRes );
        // move origin to scaled center
        result.x0 = this.x0; // - this.xRes/2 + result.xRes/2;
        result.y0 = this.y0; // - this.yRes/2 + result.yRes/2;

        return result;
    }

    /**
     * Returns a new scaled RasterReference.
     * 
     * @param env
     *            new Envelope for the RasterReference (origin)
     * @param xRes
     *            x resolution
     * @param yRes
     *            y resolution
     * @return new RasterReference
     */
    public RasterReference createScaledEnvelope( Envelope env, double xRes, double yRes ) {
      //TODO take in account the envelope's CRS axes orientation & order
        double[] origin = calculateNewOrigin( env );
        return new RasterReference( Type.OUTER, origin[0], origin[1], xRes, yRes );
    }

    /**
     * Returns new RasterReference for calculations within envelope.
     * 
     * @param envelope
     * @return new RasterReference
     */
    public RasterReference createSubEnvelope( Envelope envelope ) {
      //TODO take in account the envelope's CRS axes orientation & order
        double[] origin = calculateNewOrigin( envelope );
        return new RasterReference( Type.OUTER, origin[0], origin[1], this.xRes, this.yRes );
    }

    /**
     * Returns the new origin of a RasterReference with given Envelope.
     * 
     * @param envelope
     * @return array with origin (x0, y0)
     */
    private double[] calculateNewOrigin( Envelope envelope ) {
      //TODO take in account the envelope's CRS axes orientation & order
        // +-delta so an evelope on the border of a pixel gets the inner pixel
        int[] min = convertToRasterCRS( envelope.getMin().getX() + delta, envelope.getMin().getY() + delta );
        int[] max = convertToRasterCRS( envelope.getMax().getX() - delta, envelope.getMax().getY() - delta );
        double[] origin = convertToCRS( min( min[0], max[0] ), min( min[1], max[1] ) );

        return origin;
    }

    /**
     * Converts raster coordinates to world coordinates (outer bound of pixel).
     * 
     * @param x
     *            x coordinate
     * @param y
     *            y coordinate
     * @return array with world coordinates (x, y)
     */
    public double[] convertToCRS( int x, int y ) {
        return new double[] { x0 + x * xRes, y0 + y * yRes };
    }

    /**
     * Converts world coordinates to raster coordinates
     * 
     * @param x
     *            x coordinate
     * @param y
     *            y coordinate
     * @return array with raster coordinates (x, y)
     */
    // TODO round?
    public int[] convertToRasterCRS( double x, double y ) {
        double xVal = ( x - x0 ) / xRes;
        double yVal = ( y - y0 ) / yRes;

        return new int[] { (int) ( xVal + ( delta / INV_DELTA_SCALE ) ), (int) ( yVal + ( delta / INV_DELTA_SCALE ) ) };
    }

    /**
     * Converts world coordinates to raster coordinates.
     * 
     * @param x
     *            x coordinate
     * @param y
     *            y coordinate
     * @return array with raster coordinates (x, y)
     */
    public double[] convertToRasterCRSDouble( double x, double y ) {
        return new double[] { ( ( x - x0 ) / xRes ), ( ( y - y0 ) / yRes ) };
    }

    /**
     * Converts envelope in world coordinates to raster coordinates
     * 
     * @param envelope
     *            envelope in world coordinates
     * @return RasterRect
     */
    public RasterRect convertEnvelopeToRasterCRS( Envelope envelope ) {
      //TODO take in account the envelope's CRS axes orientation & order
        RasterRect result = new RasterRect();

        int[] min = convertToRasterCRS( envelope.getMin().getX() + delta, envelope.getMax().getY() - delta );
        int[] max = convertToRasterCRS( envelope.getMax().getX() - delta, envelope.getMin().getY() + delta );

        result.x = abs( min( min[0], max[0] ) );
        result.y = abs( min( min[1], max[1] ) );

        // rb: why +1?????
        result.width = ( abs( max[0] - min[0] ) ) + 1;
        result.height = ( abs( max[1] - min[1] ) ) + 1;

        return result;
    }

    /**
     * Returns an Envelope for a raster with given size.
     * 
     * The calculation considers the origin and resolution of the raster.
     * 
     * @param width
     * @param height
     * 
     * @return the calculated envelope
     */
    public Envelope getEnvelope( int width, int height ) {
        return getEnvelope( width, height, null );
    }

    /**
     * Returns an Envelope for a raster with given size.
     * 
     * The calculation considers the origin and resolution of the raster.
     * 
     * @param width
     * @param height
     * @param crs
     *            the coordinate system for the envelope
     * 
     * @return the calculated envelope
     * @throws UnknownCRSException 
     */
    public Envelope getEnvelope( int width, int height, CRS crs ) {
        return getEnvelope( width, height, crs, RasterReference.Type.OUTER );
    }

    /**
     * Returns an Envelope for a raster with given size.
     * 
     * The calculation considers the origin and resolution of the raster.
     * 
     * @param width
     * @param height
     * @param crs
     *            the coordinate system for the envelope
     * @param type
     *            if the result envelope should span from pixel center or the outer pixel edge
     * 
     * @return the calculated envelope
     */
    public Envelope getEnvelope( int width, int height, CRS crs, RasterReference.Type type ) {
        double xmin, xmax, ymin, ymax;
        xmin = x0;
        xmax = x0 + xRes * width;
        ymin = y0;
        ymax = y0 + yRes * height;
        
        GeometryFactory geomFactory = GeometryFactoryCreator.getInstance().getGeometryFactory();
        
        // if the CRS is not null, adjust xRes and yRes so that they reflect the CRS' axes orientation & order 
        Axis[] axes = null;
        try {
            if ( crs != null )
                axes = crs.getWrappedCRS().getAxis();
        } catch ( UnknownCRSException e ) {
            LOG.error( e.getMessage(), e );
        }
        if ( axes != null ) {            
            if ( ( axes[0].getOrientation() == Axis.AO_NORTH || axes[0].getOrientation() == Axis.AO_SOUTH ) && 
                                    ( axes[1].getOrientation() == Axis.AO_EAST || axes[1].getOrientation() == Axis.AO_WEST ) ) {
                // if the order is swapped (latitude, longitude) instead of vice-versa
                
                if ( axes[0].getOrientation() == Axis.AO_NORTH ) {
                    yRes = Math.abs( yRes );
                    xmin = y0;
                    xmax = y0 + yRes * height;
                } else if ( axes[0].getOrientation() == Axis.AO_SOUTH ) {
                    yRes = - Math.abs( yRes );                    
                    xmin = y0 + yRes * height;
                    xmax = y0;
                }
        
                if ( axes[1].getOrientation() == Axis.AO_EAST ) {
                    xRes = Math.abs( xRes );
                    ymin = x0;
                    ymax = x0 + xRes * width;
                } else if ( axes[1].getOrientation() == Axis.AO_WEST ) {
                    xRes = - Math.abs( xRes );                    
                    ymin = x0 + xRes * width;
                    ymax = x0;
                }
            } else if ( ( axes[0].getOrientation() == Axis.AO_EAST || axes[0].getOrientation() == Axis.AO_WEST ) && 
                                    ( axes[1].getOrientation() == Axis.AO_NORTH || axes[1].getOrientation() == Axis.AO_SOUTH ) ) {
                if ( axes[0].getOrientation() == Axis.AO_EAST ) {
                    xRes = Math.abs( xRes );
                    xmin = x0;
                    xmax = x0 + xRes * width;
                } else if ( axes[0].getOrientation() == Axis.AO_WEST ) {
                    xRes = - Math.abs( xRes );                    
                    xmin = x0 + xRes * width;
                    xmax = x0;
                }
        
                if ( axes[1].getOrientation() == Axis.AO_NORTH ) {
                    yRes = Math.abs( yRes );
                    ymin = y0;
                    ymax = y0 + yRes * height;
                } else if ( axes[1].getOrientation() == Axis.AO_SOUTH ) {
                    yRes = - Math.abs( yRes );                    
                    ymin = y0 + yRes * height;
                    ymax = y0;
                }
            }
        }
        
        if ( type == RasterReference.Type.CENTER ) {
            xmin = xmin + Math.abs( xRes / 2 );
            ymin = ymin + Math.abs( yRes / 2 );
            xmax = xmax - Math.abs( xRes / 2 );
            ymax = ymax - Math.abs( yRes / 2 );
        }
        
        return geomFactory.createEnvelope( new double[] { xmin, ymin }, new double[] { xmax, ymax },
                                           delta, crs );
    }
    
    /**
     * Returns the size in pixel of a raster that extends within given Envelope.
     * 
     * @param env
     *            Envelope for the
     * @return array with width and height of the raster
     */
    public int[] getSize( Envelope env ) {
        int[] result = new int[2];
        double width = env.getWidth() - delta;
        double height = env.getHeight() - delta;

        result[0] = (int) ( abs( width / getXRes() ) ) + 1;
        result[1] = (int) ( abs( height / getYRes() ) ) + 1;

        return result;
    }

    /**
     * @return the xRes
     */
    public double getXRes() {
        return xRes;
    }

    /**
     * Returns the x-coordinate of the upper-left pixel.
     * 
     * @param type
     *            Return the center or outer pixel coordinate.
     * @return x coordinate
     */
    public double getX0( Type type ) {
        if ( type == Type.CENTER ) {
            return x0 + xRes / 2;
        }// (type == Type.OUTER)
        return x0;
    }

    /**
     * Returns the y-coordinate of the upper-left pixel.
     * 
     * @param type
     *            Return the center or outer pixel coordinate.
     * @return y coordinate
     */
    public double getY0( Type type ) {
        if ( type == Type.CENTER ) {
            return y0 + yRes / 2;
        }// (type == Type.OUTER)
        return y0;
    }

    /**
     * @return the yRes
     */
    public double getYRes() {
        return yRes;
    }

    /**
     * @return the dELTA
     */
    public double getDelta() {
        return delta;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer( 200 );
        sb.append( "{x0=" ).append( x0 ).append( "," );
        sb.append( "y0=" ).append( y0 ).append( "," );
        sb.append( "xRes=" ).append( xRes ).append( "," );
        sb.append( "yRes=" ).append( yRes ).append( "}" );
        return sb.toString();
    }

    /**
     * @param resolution
     */
    public void setXRes( double resolution ) {
        this.xRes = resolution * signum( this.xRes );
    }

    /**
     * @param resolution
     */
    public void setYRes( double resolution ) {
        this.yRes = resolution * signum( this.yRes );
    }

    /**
     * Merge two RasterEnvelopes. Returns a new RasterReference where the upper-left corner is set to the values of the
     * furthest upper and furthest left corner. The resolution is set to the minimum value (i.e. the highest resolution
     * [unit/pixel])
     * 
     * @param rasterEnv
     *            RasterReference to merge
     * @return new RasterReference
     */
    public RasterReference merger( RasterReference rasterEnv ) {
        RasterReference result = new RasterReference();
        // set upper left pixel position
        if ( this.xRes > 0 ) {
            result.x0 = min( this.x0, rasterEnv.x0 );
        } else {
            result.x0 = max( this.x0, rasterEnv.x0 );
        }
        if ( this.yRes > 0 ) {
            result.y0 = min( this.y0, rasterEnv.y0 );
        } else {
            result.y0 = max( this.y0, rasterEnv.y0 );
        }

        result.xRes = min( abs( this.xRes ), abs( rasterEnv.xRes ) ) * signum( this.xRes );
        result.yRes = min( abs( this.yRes ), abs( rasterEnv.yRes ) ) * signum( this.yRes );
        result.delta = min( rasterEnv.delta, this.delta );

        return result;
    }        
}
