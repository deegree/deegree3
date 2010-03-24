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
import static java.lang.Math.cos;
import static java.lang.Math.max;
import static java.lang.Math.min;
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

    private final double delta;

    private final static int DECIMAL_ACCURACY = 1000000;

    private final static double INV_DECIMAL_ACCURACY = 0.000001;

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
        // define a delta to which calculations will be correct. It is dependent on the highest resolution. (smallest
        // value).
        delta = Math.min( Math.abs( resolutionX ), Math.abs( resolutionY ) ) * 1E-6;

        this.resX = resolutionX;
        this.resY = resolutionY;
        this.rotX = rotationX;
        this.rotY = rotationY;

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
            double span0 = envelope.getSpan( xAxis );
            double span1 = envelope.getSpan( yAxis );
            double resX = span0 / width;
            double resY = -span1 / height;
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
        result[0] = removeImprecisions( result[0] );
        result[1] = removeImprecisions( result[1] );
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
        // get rid of rounding errors
        result[0] = removeImprecisions( result[0] );
        result[1] = removeImprecisions( result[1] );

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

            // floor the unrounded min, ceil the unrounded max, this equals the outer representation, for example the
            // point 1.1, 3.1 must result in a span of 1-4. (Draw it out :-) ) e.g a width of 3
            result.width = (int) abs( floor( rrUL[0] ) - ceil( rrLR[0] ) );
            result.height = (int) abs( floor( rrUL[1] ) - ceil( rrLR[1] ) );
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
        return getEnvelope( location, width, height, crs );
    }

    /**
     * Returns an Envelope for a raster with given size and given x,y raster location.
     * 
     * The calculation considers the origin and resolution of the this raster.
     * 
     * @param rasterRect
     *            defining the x,y raster coordinates (as integers) as well as the width and height of the raster.
     * @param crs
     *            the coordinate system for the envelope
     * 
     * @return the calculated envelope
     */
    public Envelope getEnvelope( RasterRect rasterRect, CRS crs ) {
        return getEnvelope( location, rasterRect, crs );
    }

    /**
     * Returns an Envelope for a raster with given size and given x,y raster location.
     * 
     * The calculation considers the origin and resolution of the raster.
     * 
     * @param targetLocation
     *            of the origin, specifies if the the newly created envelope should consider the origin located at the
     *            OUTER or CENTER of a pixel.
     * @param rasterRect
     *            defining the x,y raster coordinates (as integers) as well as the width and height of the raster.
     * @param crs
     *            the coordinate system for the envelope
     * 
     * @return the calculated envelope
     */
    public Envelope getEnvelope( OriginLocation targetLocation, RasterRect rasterRect, CRS crs ) {
        // if the targetlocation must be center, we add half a pixel, because we need to get the world coordinate of the
        // center of the pixel.
        double nullX = rasterRect.x + ( targetLocation == CENTER ? 0.5 : 0 );
        double nullY = rasterRect.y + ( targetLocation == CENTER ? 0.5 : 0 );
        double tw = nullX + rasterRect.width;
        double th = nullY + rasterRect.height;

        // double tw = rasterRect.width;
        // double th = rasterRect.height;

        // if ( location == CENTER ) {
        // // if the targetlocation must be center, we add half a pixel, because we need to get the world coordinate of
        // // the center of the pixel.
        // tw += 0.5;
        // th += 0.5;
        // }
        //
        // if ( location != targetLocation ) {
        // if ( targetLocation == OUTER ) {
        // // this location is center, the target location is outer, subtract 0.5 pixel from the width|height.
        // // tw -= 0.5;
        // // th -= 0.5;
        // pixelAddUp -= 0.5;
        // } else {
        // // this location is outer, the target location is center, add 0.5 pixel to the width|height
        // // tw += 0.5;
        // // th += 0.5;
        // pixelAddUp += 0.5;
        // }
        // }

        // nullX += pixelAddUp;
        // nullY += pixelAddUp;
        // tw += pixelAddUp;
        // th += pixelAddUp;

        double[] widthHeightPos = getWorldCoordinate( tw, th );
        double[] origin = getWorldCoordinate( nullX, nullY );
        // double[] origin = getOrigin();
        // if ( location != targetLocation ) {
        // if ( targetLocation == OUTER ) {
        // // this location is center, the target location is outer, subtract 0.5 res to the origin.
        // origin[0] -= resX * 0.5;
        // origin[1] -= resY * 0.5;
        // } else {
        // // this location is outer, the target location is center, add 0.5 resolution to the origin
        // origin[0] += resX * 0.5;
        // origin[1] += resY * 0.5;
        // }
        // }

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
     * Returns an Envelope for a raster with given size.
     * 
     * The calculation considers the origin and resolution of the raster.
     * 
     * @param targetLocation
     *            of the origin, specifies if the the newly created envelope should consider the origin located at the
     *            OUTER or CENTER of a pixel.
     * @param width
     *            in raster coordinates
     * @param height
     *            in raster coordinates
     * @param crs
     *            the coordinate system for the envelope
     * 
     * @return the calculated envelope
     */
    public Envelope getEnvelope( OriginLocation targetLocation, int width, int height, CRS crs ) {
        return getEnvelope( targetLocation, new RasterRect( 0, 0, width, height ), crs );
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
     * Merge two Raster references. Returns a new RasterReference where the upper-left corner is set to the values of
     * the smallest upper and smallest left ordinate. The resolution is set to the minimum value (i.e. the highest
     * resolution [unit/pixel]). Some assumptions are made (not checked):
     * <ul>
     * <li>The pixel location (center/outer) of the origin are equal, if not, the location of the first reference will
     * be used (translated origin of second)</li>
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
        RasterGeoReference geoRef2Copy = geoRef2;
        if ( geoRef1.location != geoRef2.location ) {
            double[] orig = geoRef2.getOrigin( geoRef1.location );
            geoRef2Copy = new RasterGeoReference( geoRef1.location, geoRef2.getResolutionX(), geoRef2.getResolutionY(),
                                                  geoRef2.getRotationX(), geoRef2.getRotationY(), orig[0], orig[1],
                                                  geoRef2.crs );
        }
        double[] origin1 = geoRef1.getOrigin();
        double[] origin2 = geoRef2Copy.getOrigin();

        double res1x = geoRef1.getResolutionX();
        double res1y = geoRef1.getResolutionY();

        double res2x = geoRef2Copy.getResolutionX();
        double res2y = geoRef2Copy.getResolutionY();

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
        return this.createRelocatedReference( location, envelope );
    }

    /**
     * Returns new RasterGeoReference with the origin set to the min[0],max[1] of the envelope and the OriginLocation to
     * the given one. Other values are taken from this instance. Attention, the resulting origin is snapped to the
     * location (center/outer) of the underlying grid, so the min[0] and max[1] values are only approximations to the
     * new origin!
     * 
     * @param targetLocation
     * @param envelope
     *            to get the origin from.
     * @return new RasterGeoReference or <code>null</code> if the envelope is <code>null</code>
     */
    public RasterGeoReference createRelocatedReference( OriginLocation targetLocation, Envelope envelope ) {
        if ( envelope != null ) {
            OriginLocation tLoc = ( targetLocation == null ) ? location : targetLocation;
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
            // take the 'upper' left raster position as the new origin, thus add a half pixel.
            double raster0 = rasterCoordinate[0] + ( tLoc == CENTER ? 0.5 : 0 );
            double raster1 = rasterCoordinate[1] + ( tLoc == CENTER ? 0.5 : 0 );
            // if ( location == CENTER ) {
            // raster0 += 0.5;
            // raster1 += 0.5;
            // }
            // if ( tLoc != location ) {
            // if ( tLoc == OUTER ) {
            // // this location is center, the above subtracted 0.5 will get the raster location of the centered
            // // view, but to get an OUTER view we need to subtract another half a pixel.
            // raster0 -= 0.5;
            // raster1 -= 0.5;
            // } else {
            // // this location is OUTER, add 0.5 to get the center location
            // raster0 += 0.5;
            // raster1 += 0.5;
            // }
            // }
            double[] worldCoordinate = getWorldCoordinate( raster0, raster1 );

            return new RasterGeoReference( tLoc, this.getResolutionX(), this.getResolutionY(), this.getRotationX(),
                                           this.getRotationY(), worldCoordinate[0], worldCoordinate[1], this.crs );
        }
        return null;
    }

    /**
     * Returns new RasterGeoReference with the origin set to the given target location. Other values are taken from this
     * instance.
     * 
     * @param targetLocation
     *            of the new reference
     * @return new RasterGeoReference or this if the target location is <code>null</code> or equals this one.
     */
    public RasterGeoReference createRelocatedReference( OriginLocation targetLocation ) {
        if ( targetLocation == null || location == targetLocation ) {
            return this;
        }
        double[] newOrigin = getOrigin( targetLocation );
        return new RasterGeoReference( targetLocation, this.getResolutionX(), this.getResolutionY(),
                                       this.getRotationX(), this.getRotationY(), newOrigin[0], newOrigin[1], this.crs );
    }

    /**
     * Relocates the given minimum and maximum points of the given envelope to the target origin location definition.
     * This method does nothing if the given location equals this {@link RasterGeoReference}'s origin location. This
     * method effectively adds or subtracts half a resolution of the ordinates of the given Envelope. Different CRS's
     * are supported.
     * 
     * @param targetLocation
     *            the preferred location of the origin. *
     * @param envelope
     *            to relocate.
     * @return a new Envelope which is aligned with the target location or <code>null</code> if the envelope is
     *         <code>null</code>
     */
    public Envelope relocateEnvelope( OriginLocation targetLocation, Envelope envelope ) {
        if ( envelope == null ) {
            return null;
        }
        if ( targetLocation == location ) {
            return envelope;
        }
        // rb: the envelope will not create copies, neither does the geometryfactory ;-)
        double[] orig = envelope.getMin().getAsArray();
        double[] nMin = new double[orig.length];
        System.arraycopy( orig, 0, nMin, 0, orig.length );
        orig = envelope.getMax().getAsArray();
        double[] nMax = new double[orig.length];
        System.arraycopy( orig, 0, nMax, 0, orig.length );
        Envelope transformedEnv = geomFactory.createEnvelope( nMin, nMax, envelope.getCoordinateSystem() );
        if ( targetLocation != location ) {
            if ( transformer != null && envelope.getCoordinateSystem() != null ) {
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

            double[] rasterCoordinateMin = getRasterCoordinateUnrounded( min[0], min[1] );
            double[] rasterCoordinateMax = getRasterCoordinateUnrounded( max[0], max[1] );

            double world0Min = rasterCoordinateMin[0];
            double world1Min = rasterCoordinateMin[1];

            double world0Max = rasterCoordinateMax[0];
            double world1Max = rasterCoordinateMax[1];

            if ( location == CENTER ) {
                // the targetlocation is OUTER
                // take the 'upper' left raster position as the new origin.
                world0Min -= 0.5;
                world1Min -= 0.5;
                world0Max -= 0.5;
                world1Max -= 0.5;
            } else {
                // the targetlocation is CENTER
                world0Min += 0.5;
                world1Min += 0.5;
                world0Max += 0.5;
                world1Max += 0.5;
            }
            double[] worldMinCoordinate = getWorldCoordinate( world0Min, world1Min );
            double[] worldMaxCoordinate = getWorldCoordinate( world0Max, world1Max );

            min[0] = worldMinCoordinate[0];
            min[1] = worldMinCoordinate[1];
            max[0] = worldMaxCoordinate[0];
            max[1] = worldMaxCoordinate[1];
            transformedEnv = geomFactory.createEnvelope( min, max, transformedEnv.getCoordinateSystem() );

            // no convert back to the requested crs
            if ( transformer != null && envelope.getCoordinateSystem() != null ) {
                try {
                    GeometryTransformer invTrans = new GeometryTransformer(
                                                                            envelope.getCoordinateSystem().getWrappedCRS() );
                    transformedEnv = invTrans.transform( transformedEnv ).getEnvelope();
                } catch ( IllegalArgumentException e ) {
                    // just don't transform and go ahead without.
                } catch ( TransformationException e ) {
                    // just don't transform and go ahead without.
                } catch ( UnknownCRSException e ) {
                    // just don't transform and go ahead without.
                }
            }

        }
        return transformedEnv;
    }

    /**
     * @return the world coordinate of the origin of the geo referenced raster.
     */
    public double[] getOrigin() {
        return getOrigin( location );
    }

    /**
     * @param target
     *            the location the new origin, may be <code>null</code>
     * @return the world coordinate of the origin of the geo referenced raster.
     */
    public double[] getOrigin( OriginLocation target ) {
        double[] result = new double[2];
        if ( target != null && target != location ) {
            if ( location == CENTER ) {
                result = getWorldCoordinate( 0, 0 );
            } else {
                result = getWorldCoordinate( +0.5, +0.5 );
            }
        } else {
            result[0] = transform.getTranslateX();
            result[1] = transform.getTranslateY();
        }
        return result;
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

    /**
     * A dirty hacking method for getting rid of any imprecision errors in the floating point representation of the
     * given double value. As it is the {@link AffineTransform} creates lots of these imprecision errors, resulting in
     * the Math.floor/round/ceil functions being of by one more often than you think!!!
     * 
     * @param value
     *            to clean
     * @return the cleaned value.
     */
    private final double removeImprecisions( double value ) {
        // quick and dirty, a 0 will always be a 0 (yes it sometimes happens).
        if ( value == 0 ) {
            return value;
        }

        // check if no 'extra' values are given (e.g. 25.6).
        double result = Math.abs( ( value * DECIMAL_ACCURACY ) - ( Math.round( value * DECIMAL_ACCURACY ) ) );
        if ( result == 0 ) {
            // a perfect match, don't do any rounding e.g 25.6
            return value;
        }
        // check the decimal value against the delta (which depends on the resolution).
        result = ( value - Math.floor( value ) ) * delta;
        if ( Math.abs( result ) < delta ) {
            // almost 0, so round it up eg. 25.600000008, but not in sight an ulp
            return Math.round( value * DECIMAL_ACCURACY ) * INV_DECIMAL_ACCURACY;
        }
        // test if we are in reach of an ulp
        result = value;
        double nextup = Math.nextUp( value );
        double nextdown = Math.nextAfter( value, Double.NEGATIVE_INFINITY );

        double upRest = ( ( nextup * DECIMAL_ACCURACY ) - Math.floor( nextup * DECIMAL_ACCURACY ) );
        double downRest = ( nextdown * DECIMAL_ACCURACY - Math.floor( nextdown * DECIMAL_ACCURACY ) );
        upRest -= Math.floor( upRest );
        downRest -= Math.floor( downRest );
        if ( upRest == 0 ) {
            // 0 only if the result was of by an ulp
            result = nextup;
        } else if ( downRest == 0 ) {
            result = nextdown;
        }
        return result;
    }

    /**
     * First get rid of any rounding errors before passing the value to {@link Math#ceil(double)};
     * 
     * @param val
     *            to be ceiled
     * @return the {@link Math#ceil(double)} of the cleaned value.
     */
    private final double ceil( double val ) {
        val = removeImprecisions( val );
        return Math.ceil( val );
    }

    /**
     * First get rid of any rounding errors before passing the value to {@link Math#round(double)};
     * 
     * @param val
     *            to be rounded
     * @return the {@link Math#round(double)} of the cleaned value.
     */
    private final double round( double val ) {
        val = removeImprecisions( val );
        return Math.round( val );
    }

    /**
     * First get rid of any rounding errors before passing the value to {@link Math#floor(double)};
     * 
     * @param val
     *            to be floored
     * @return the {@link Math#floor(double)} of the cleaned value.
     */
    private final double floor( double val ) {
        val = removeImprecisions( val );
        return Math.floor( val );
    }

}