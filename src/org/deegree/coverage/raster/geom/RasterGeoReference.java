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
import static java.lang.Math.ceil;
import static java.lang.Math.cos;
import static java.lang.Math.floor;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.round;
import static java.lang.Math.sin;
import static org.deegree.coverage.raster.geom.RasterGeoReference.OriginLocation.CENTER;
import static org.slf4j.LoggerFactory.getLogger;

import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;

import org.deegree.crs.CRS;
import org.deegree.crs.coordinatesystems.CoordinateSystem;
import org.deegree.crs.exceptions.TransformationException;
import org.deegree.crs.exceptions.UnknownCRSException;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.GeometryFactory;
import org.deegree.geometry.GeometryTransformer;
import org.slf4j.Logger;

/**
 * The <code>RasterGeoReference</code> defines methods for transformations between a raster crs and a world crs. For
 * this purpose the origin of the upper left raster grid and the size in world coordinate units of a raster grid (pixel)
 * must be specified.
 * 
 * Alternatively an angle between the x axis and a rectangular base axis (as well as y rotation between the y axis and
 * the rectangular basis) may be defined.
 * 
 * This implementation is world crs axis order aware. See
 * http://wiki.deegree.org/deegreeWiki/deegree3/RasterInnerOuterIssue for a discussion on the location of the origin on
 * a raster grid.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 * @version $Revision$, $Date$
 * 
 */
public class RasterGeoReference {

    private static final Logger LOG = getLogger( RasterGeoReference.class );

    private static final GeometryFactory geomFactory = new GeometryFactory();

    private final AffineTransform transform;

    private final AffineTransform invTransform;

    private final OriginLocation location;

    private final GeometryTransformer transformer;

    private final CRS crs;

    private final double resX;

    private final double resY;

    private final double rotX;

    private final double rotY;

    /**
     * The <code>OriginLocation</code> defines the mapping location of the world origin to the underlying raster.
     * 
     * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
     * @author last edited by: $Author$
     * @version $Revision$, $Date$
     * 
     */
    public enum OriginLocation {
        /** The origin lies in the middle of a pixel */
        CENTER,
        /** The origin lies in on the of upper left corner of a pixel */
        OUTER
    }

    /**
     * Constructs a raster reference with given resolutions, origin and rotations. The origin maps to the pixel location
     * given by location.
     * 
     * @param location
     *            of the origin on the upper left pixel.
     * @param resolutionX
     *            the resolution of one pixel on the x axis in the raster
     * @param resolutionY
     *            the resolution of one pixel on the y axis in the raster
     * @param rotationX
     *            rotation about x-axis
     * @param rotationY
     *            rotation about y-axis
     * @param origin0
     *            ordinate of the origin of the first axis defined in the world crs
     * @param origin1
     *            ordinate of the origin of the second axis defined in the world crs
     * @param crs
     *            in which the origin is defined.
     */
    public RasterGeoReference( OriginLocation location, double resolutionX, double resolutionY, double rotationX,
                               double rotationY, double origin0, double origin1, CRS crs ) {

        CoordinateSystem cs = null;
        if ( crs != null ) {
            try {
                cs = crs.getWrappedCRS();
            } catch ( UnknownCRSException e ) {
                // ok. just leave it
            }
        }
        this.crs = crs;
        if ( cs != null ) {
            transformer = new GeometryTransformer( cs );
        } else {
            transformer = null;
        }

        transform = new AffineTransform( cos( rotationX ) * resolutionX, sin( rotationY ), -sin( rotationX ),
                                         cos( rotationY ) * resolutionY, origin0, origin1 );
        try {
            invTransform = transform.createInverse();
        } catch ( NoninvertibleTransformException e ) {
            LOG.debug( "No inverse transform available, this means the supplies values are not valid.", e );
            throw new IllegalArgumentException(
                                                "Could not create Raster Geo reference, the given values do not specify an affine transform: "
                                                                        + e.getLocalizedMessage() );
        }
        this.location = location;
        this.resX = resolutionX;
        this.resY = resolutionY;
        this.rotX = rotationX;
        this.rotY = rotationY;
    }

    /**
     * Constructs a raster reference with given resolutions and origin. The origin maps to the pixel location given by
     * location.
     * 
     * @param location
     *            of the origin on the upper left pixel.
     * @param resolutionX
     *            the resolution of one pixel on the x axis in the raster
     * @param resolutionY
     *            the resolution of one pixel on the y axis in the raster
     * @param origin0
     *            ordinate of the origin of the first axis defined in the world crs
     * @param origin1
     *            ordinate of the origin of the second axis defined in the world crs
     * @param crs
     *            in which the origin is defined.
     */
    public RasterGeoReference( OriginLocation location, double resolutionX, double resolutionY, double origin0,
                               double origin1, CRS crs ) {
        this( location, resolutionX, resolutionY, 0, 0, origin0, origin1, crs );
    }

    /**
     * Constructs a raster reference with given resolutions and origin. The origin maps to the pixel location given by
     * location. No world CRS is defined.
     * 
     * @param location
     *            of the origin on the upper left pixel.
     * @param resolutionX
     *            the resolution of one pixel on the x axis in the raster
     * @param resolutionY
     *            the resolution of one pixel on the y axis in the raster
     * @param origin0
     *            ordinate of the origin of the first axis defined in the world crs
     * @param origin1
     *            ordinate of the origin of the second axis defined in the world crs
     */
    public RasterGeoReference( OriginLocation location, double resolutionX, double resolutionY, double origin0,
                               double origin1 ) {
        this( location, resolutionX, resolutionY, 0, 0, origin0, origin1, null );
    }

    /**
     * Create a raster reference which has it's origin at the min[0] and max[1] of the given Envelope. The resolution
     * will be determined by getting the easting axis/width and northing-axis/height. If no CRS is available from the
     * Envelope axisorder XY is assumed.
     * 
     * @param location
     *            of the origin can be center or outer
     * @param envelope
     *            to get the appropriate values from.
     * @param width
     *            of the underlying raster needed to calculate the resolution.
     * @param height
     *            of the underlying raster needed to calculate the resolution.
     * @return a new RasterGeoReference of <code>null</code> if the given envelope is <code>null</code>
     */
    public static RasterGeoReference create( OriginLocation location, Envelope envelope, int width, int height ) {
        if ( envelope != null ) {
            CRS crs = envelope.getCoordinateSystem();
            int xAxis = 0;
            int yAxis = 1;
            if ( crs != null ) {
                try {
                    CoordinateSystem cs = crs.getWrappedCRS();
                    xAxis = cs.getEasting();
                    yAxis = cs.getNorthing();
                } catch ( UnknownCRSException e ) {
                    // assume xaxis is first.
                }
            }
            double resX = envelope.getSpan( xAxis ) / width;
            double resY = -envelope.getSpan( yAxis ) / height;
            double origin0 = envelope.getMin().get0();
            double origin1 = envelope.getMax().get1();
            return new RasterGeoReference( location, resX, resY, origin0, origin1, crs );
        }
        return null;
    }

    /**
     * Return the raster coordinate denoted by the given world coordinate. This method is CENTER and OUTER aware.
     * 
     * @param worldX
     *            x position in the world coordinate system, for which raster coordinates should be calculated.
     * @param worldY
     *            y position in the world coordinate system, for which raster coordinates should be calculated.
     * @return the (rounded) raster coordinate which the given world coordinate maps to.
     */
    public int[] getRasterCoordinate( double worldX, double worldY ) {
        Point2D rslt = invTransform.transform( new Point2D.Double( worldX, worldY ), null );
        if ( location == CENTER ) {
            return new int[] { (int) round( rslt.getX() ), (int) round( rslt.getY() ) };
        }
        return new int[] { (int) floor( rslt.getX() ), (int) floor( rslt.getY() ) };

    }

    /**
     * Return the raster coordinate denoted by the given world coordinate. This method is CENTER and OUTER aware.
     * 
     * @param worldX
     *            x position in the world coordinate system, for which raster coordinates should be calculated.
     * @param worldY
     *            y position in the world coordinate system, for which raster coordinates should be calculated.
     * @return the raster coordinate which the given world coordinate maps to.
     */
    public double[] getRasterCoordinateUnrounded( double worldX, double worldY ) {
        double[] incoming = new double[] { worldX, worldY };
        double[] result = new double[2];
        invTransform.transform( incoming, 0, result, 0, 1 );
        if ( location == CENTER ) {
            // add 0.5, because the world origin has a center offset of 0.5
            result[0] += 0.5;
            result[1] += 0.5;
        }
        return result;
    }

    /**
     * Return the world coordinate denoted by the given raster position. This method is CENTER and OUTER aware.
     * 
     * @param rasterX
     *            x position in the raster for which world coordinates should be calculated.
     * @param rasterY
     *            y position in the raster for which world coordinates should be calculated.
     * @return the world coordinate which the given raster coordinate maps to.
     */
    public double[] getWorldCoordinate( double rasterX, double rasterY ) {
        double[] result = new double[2];
        double[] input = new double[] { rasterX, rasterY };
        if ( location == CENTER ) {
            // if the origin is located on the center of the pixel raster coordinates should be substracted 0.5, or
            // world coordinates must be subtracted half a resolution.
            input[0] -= 0.5;
            input[1] -= 0.5;
        }
        transform.transform( input, 0, result, 0, 1 );
        return result;
    }

    /**
     * Converts an envelope in world coordinates to raster coordinates, note the envelope in world coordinates is
     * defined over min/max (lower left/upper right), whereas a RasterRect is defined as min(upperleft) with a width and
     * height.
     * 
     * @param envelope
     *            envelope in world coordinates
     * @return RasterRect
     */
    public RasterRect convertEnvelopeToRasterCRS( Envelope envelope ) {
        RasterRect result = new RasterRect();
        if ( envelope != null ) {
            Envelope transformedEnv = envelope;
            if ( transformer != null ) {
                try {
                    transformedEnv = transformer.transform( envelope ).getEnvelope();
                } catch ( IllegalArgumentException e ) {
                    // just don't transform and go ahead without.
                } catch ( TransformationException e ) {
                    // just don't transform and go ahead without.
                } catch ( UnknownCRSException e ) {
                    // just don't transform and go ahead without.
                }
            }

            double[] min = transformedEnv.getMin().getAsArray();
            double[] max = transformedEnv.getMax().getAsArray();

            // upper left point
            double ulX = min[0];
            double ulY = max[1];

            // lower right point
            double lrX = max[0];
            double lrY = min[1];

            // finding easting and northing ordinates is not necessary because the transform is aligned with the defined
            // coordinatesystem.

            int[] rrUpperLeft = getRasterCoordinate( ulX, ulY );
            int[] rrLowerRight = getRasterCoordinate( lrX, lrY );

            // get the minimal raster x value.
            result.x = min( rrUpperLeft[0], rrLowerRight[0] );

            // and the minimal raster y value
            result.y = min( rrUpperLeft[1], rrLowerRight[1] );

            // find 'unrounded' location raster locations.
            double[] rrUL = getRasterCoordinateUnrounded( ulX, ulY );
            double[] rrLR = getRasterCoordinateUnrounded( lrX, lrY );

            // if ( location == CENTER ) {
            // floor the unrounded min, ceil the unrounded max, this equals the outer representation, for example the
            // point 1.1, 3.1 must result in a span of 1-4. (Draw it out :-) ) e.g a width of 3
            result.width = (int) abs( floor( rrUL[0] ) - ceil( rrLR[0] ) );
            result.height = (int) abs( floor( rrUL[1] ) - ceil( rrLR[1] ) );
            // } else {
            // result.width = ( abs( rrUpperLeft[0] - rrLowerRight[0] ) );
            // result.height = ( abs( rrUpperLeft[1] - rrLowerRight[1] ) );
            // }
        }
        return result;
    }

    /**
     * Returns an Envelope for a raster with given size.
     * 
     * The calculation considers the origin and resolution of the raster.
     * 
     * @param width
     *            in raster coordinates
     * @param height
     *            in raster coordinates
     * @param crs
     *            the coordinate system for the envelope
     * 
     * @return the calculated envelope
     */
    public Envelope getEnvelope( int width, int height, CRS crs ) {

        double tw = width;
        double th = height;
        if ( location == CENTER ) {
            // add a half pixel, because we need to get the world coordinate of the center of the pixel.
            tw += 0.5;
            th += 0.5;
        }

        double[] widthHeightPos = getWorldCoordinate( tw, th );
        double[] origin = getOrigin();

        // convert to lower-left and upper-right for the envelope
        double min0 = min( widthHeightPos[0], origin[0] );
        double min1 = min( widthHeightPos[1], origin[1] );
        double max0 = max( widthHeightPos[0], origin[0] );
        double max1 = max( widthHeightPos[1], origin[1] );

        // coordinates are in the crs, so axis order is available.
        Envelope result = geomFactory.createEnvelope( min0, min1, max0, max1, this.crs );
        if ( crs != null && this.crs != null ) {
            CoordinateSystem cs = null;
            try {
                cs = crs.getWrappedCRS();
            } catch ( UnknownCRSException e ) {
                // just do not do anything.
            }
            if ( cs != null ) {
                GeometryTransformer trans = new GeometryTransformer( cs );
                try {
                    result = trans.transform( result ).getEnvelope();
                } catch ( IllegalArgumentException e ) {
                    // let the envelope be.
                } catch ( TransformationException e ) {
                    // let the envelope be.
                } catch ( UnknownCRSException e ) {
                    // let the envelope be.
                }
            }
        }
        return result;
    }

    /**
     * Returns the size in pixel of a raster that extends within given Envelope.
     * 
     * @param env
     *            Envelope for the
     * @return array with width and height of the raster
     */
    public int[] getSize( Envelope env ) {
        RasterRect rr = convertEnvelopeToRasterCRS( env );
        return new int[] { rr.width, rr.height };
    }

    /**
     * Merge two RasterEnvelopes. Returns a new RasterReference where the upper-left corner is set to the values of the
     * smallest upper and smallest left ordinate. The resolution is set to the minimum value (i.e. the highest
     * resolution [unit/pixel]). Some assumptions are made (not checked):
     * <ul>
     * <li>The pixel location (center/outer) of the origin are equal</li>
     * <li>Crs is identical</li>
     * <li>rotation around axis are equal</li>
     * </ul>
     * 
     * @param geoRef1
     * @param geoRef2
     * 
     * @return new RasterReference
     */
    public static RasterGeoReference merger( RasterGeoReference geoRef1, RasterGeoReference geoRef2 ) {

        if ( geoRef1 == null ) {
            return geoRef2;
        }
        if ( geoRef2 == null ) {
            return geoRef1;
        }
        double[] origin1 = geoRef1.getOrigin();
        double[] origin2 = geoRef2.getOrigin();

        double res1x = geoRef1.getResolutionX();
        double res1y = geoRef1.getResolutionY();

        double res2x = geoRef2.getResolutionX();
        double res2y = geoRef2.getResolutionY();

        double nResx = ( res1x < 0 ) ? max( res1x, res2x ) : min( res1x, res2x );
        double nResy = ( res1y < 0 ) ? max( res1y, res2y ) : min( res1y, res2y );

        double nOrigx = ( res1x < 0 ) ? max( origin1[0], origin2[0] ) : min( origin1[0], origin2[0] );
        double nOrigy = ( res1y < 0 ) ? max( origin1[1], origin2[1] ) : min( origin1[1], origin2[1] );

        return new RasterGeoReference( geoRef1.location, nResx, nResy, geoRef1.getRotationX(), geoRef1.getRotationY(),
                                       nOrigx, nOrigy, geoRef1.crs );
    }

    /**
     * Returns new RasterGeoReference with the origin set to the min[0],max[1] of the envelope but other values are
     * taken from this instance. Attention, the resulting origin is snapped to the location (center/outer) of the
     * underlying grid, so the min[0] and max[1] values are only approximations to the new origin!
     * 
     * @param envelope
     *            to get the origin from.
     * @return new RasterGeoReference or <code>null</code> if the envelope is <code>null</code>
     */
    public RasterGeoReference createRelocatedReference( Envelope envelope ) {
        if ( envelope != null ) {
            Envelope transformedEnv = envelope;
            if ( transformer != null ) {
                try {
                    transformedEnv = transformer.transform( envelope ).getEnvelope();
                } catch ( IllegalArgumentException e ) {
                    // just don't transform and go ahead without.
                } catch ( TransformationException e ) {
                    // just don't transform and go ahead without.
                } catch ( UnknownCRSException e ) {
                    // just don't transform and go ahead without.
                }
            }

            double[] min = transformedEnv.getMin().getAsArray();
            double[] max = transformedEnv.getMax().getAsArray();

            int[] rasterCoordinate = getRasterCoordinate( min[0], max[1] );
            double world0 = rasterCoordinate[0];
            double world1 = rasterCoordinate[1];
            if ( location == CENTER ) {
                // take the 'upper' left raster position as the new origin.
                world0 += 0.5;
                world1 += 0.5;
            }
            double[] worldCoordinate = getWorldCoordinate( world0, world1 );

            return new RasterGeoReference( this.location, this.getResolutionX(), this.getResolutionY(),
                                           this.getRotationX(), this.getRotationY(), worldCoordinate[0],
                                           worldCoordinate[1], this.crs );
        }
        return null;
    }

    /**
     * @return the world coordinate of the origin of the geo referenced raster.
     */
    public double[] getOrigin() {
        return new double[] { transform.getTranslateX(), transform.getTranslateY() };
    }

    /**
     * @return the resolution on the X axis of the raster, e.g. the units/pixel.
     */
    public double getResolutionX() {
        /* rb: one could get the resolution from the matrices. */
        /* if the shear is null, the rotation matrix was not instantiated, just return the scale value. */
        // if ( abs( transform.getShearX() ) < 1E-10 ) {
        // return transform.getScaleX();
        // }
        /* find axis rotation angle and remove it from the scale. */
        // double val = asin( transform.getShearX() );
        // return transform.getScaleX() / acos( val );
        return resX;
    }

    /**
     * @return the resolution on the Y axis of the raster, e.g. the units/pixel.
     */
    public double getResolutionY() {
        /* rb: one could get the resolution from the matrices. */
        /* if the shear is null, the rotation matrix was not instantiated, just return the scale value. */
        // if ( abs( transform.getShearY() ) < 1E-10 ) {
        // return transform.getScaleY();
        // }
        /* find axis rotation angle and remove it from the scale. */
        // double val = -asin( transform.getShearY() );
        // return transform.getScaleY() / acos( val );
        return resY;
    }

    /**
     * @return the rotation angle (in radians) for the x axis of the raster.
     */
    public double getRotationX() {
        /* rb: one could get the rotation from the matrices. */
        // return asin( transform.getScaleX() );
        return rotX;
    }

    /**
     * @return the rotation angle (in radians) for the y axis of the raster.
     */
    public double getRotationY() {
        /* rb: one could get the rotation from the matrices. */
        // return -asin( transform.getScaleY() );
        return rotY;
    }

    /**
     * @return the location of the origin on the upper left pixel of a raster.
     */
    public OriginLocation getOriginLocation() {
        return location;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer( 200 );
        double[] orig = getOrigin();
        sb.append( "{location=" ).append( location ).append( "," );
        sb.append( "orig_0=" ).append( orig[0] ).append( "," );
        sb.append( "orig_1=" ).append( orig[1] ).append( "," );
        sb.append( "xRes=" ).append( getResolutionX() ).append( "," );
        sb.append( "yRes=" ).append( getResolutionY() ).append( "," );
        sb.append( "xRpt=" ).append( getRotationX() ).append( "," );
        sb.append( "yRot=" ).append( getRotationY() ).append( "}" );
        return sb.toString();
    }

}