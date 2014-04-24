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

package org.deegree.cs.coordinatesystems;

import static org.slf4j.LoggerFactory.getLogger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.vecmath.Point3d;

import org.deegree.cs.CRSCodeType;
import org.deegree.cs.CRSIdentifiable;
import org.deegree.cs.CRSResource;
import org.deegree.cs.CoordinateTransformer;
import org.deegree.cs.components.Axis;
import org.deegree.cs.components.IAxis;
import org.deegree.cs.components.IDatum;
import org.deegree.cs.components.IGeodeticDatum;
import org.deegree.cs.components.IUnit;
import org.deegree.cs.components.Unit;
import org.deegree.cs.persistence.CRSManager;
import org.deegree.cs.refs.coordinatesystem.CRSRef;
import org.deegree.cs.transformations.Transformation;
import org.slf4j.Logger;

/**
 * Three kinds of <code>CoordinateSystem</code>s (in this class abbreviated with CRS) are supported in this lib.
 * <ul>
 * <li>Geographic CRS: A position (on the ellipsoid) is given in Lattitude / Longitude (Polar Cooridnates) given in rad°
 * min''sec. The order of the position's coordinates are to be contrued to the axis order of the CRS. These lat/lon
 * coordinates are to be tranformed to x,y,z values to define their location on the underlying datum.</li>
 * <li>GeoCentric CRS: A position (on the ellipsoid) is given in x, y, z (cartesian) coordinates with the same units
 * defined as the ones in the underlying datum. The order of the position's coordinates are to be contrued to the axis
 * order of the datum.</li>
 * <li>Projected CRS: The position (on the map) is given in a 2D-tuple in pre-defined units. The Axis of the CRS are
 * defined (through a transformation) for an underlying Datum, which can have it's own axis with their own units. The
 * order of the position's coordinates are to be contrued to the axis order of the CRS</li>
 * </ul>
 * 
 * Summarizing it can be said, that each CRS has following features
 * <ul>
 * <li>A reference code (an casesensitive String identifying this CRS, for example 'EPGS:4326' or
 * 'urn:ogc:def:crs:OGC:2:84' or 'luref')</li>
 * <li>An optional version.</li>
 * <li>A humanly readable name.</li>
 * <li>An optional description.</li>
 * <li>An optional area of use, describing where this CRS is used.</li>
 * <li>The order in which the axis of ther crs are defined.</li>
 * <li>The underlying Datum</li>
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * 
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */

public abstract class CRS extends CRSIdentifiable implements ICRS {

    private static final Logger LOG = getLogger( ICRS.class );

    private final Object LOCK = new Object();

    private IAxis[] axisOrder;

    private IDatum usedDatum;

    private final List<Transformation> transformations;

    private transient double[] validDomain = null;

    /**
     * 
     * Simple enum defining the currently known Coordinate System types.
     * 
     * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
     * @author last edited by: $Author: rutger $
     * 
     * @version $Revision: $, $Date: $
     */
    public enum CRSType {
        /** Defines this CRS as a GeoCentric one. */
        GEOCENTRIC( "Geocentric CRS" ),

        /** Defines this CRS as a Geographic one. */
        GEOGRAPHIC( "Geographic CRS" ),
        /** Defines this CRS as a Projected one. */
        PROJECTED( "Projected CRS" ),
        /** Defines this CRS as a Compound one. */
        COMPOUND( "Compound CRS" ),
        /** Defines this CRS as a Vertical one. */
        VERTICAL( "Vertical CRS" );

        private String name;

        private CRSType( String name ) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    /**
     * @param datum
     *            of this coordinate system.
     * @param axisOrder
     *            the axisorder of this coordinate system.
     * @param identity
     */
    public CRS( IDatum datum, IAxis[] axisOrder, CRSResource identity ) {
        this( null, datum, axisOrder, identity );
    }

    /**
     * @param datum
     *            of this coordinate system.
     * @param axisOrder
     *            the axisorder of this coordinate system.
     * @param codes
     *            of this coordinate system.
     * @param names
     * @param versions
     * @param descriptions
     * @param areasOfUse
     */
    public CRS( IDatum datum, IAxis[] axisOrder, CRSCodeType[] codes, String[] names, String[] versions,
                String[] descriptions, String[] areasOfUse ) {
        super( codes, names, versions, descriptions, areasOfUse );
        this.axisOrder = axisOrder;
        this.usedDatum = datum;
        this.transformations = new LinkedList<Transformation>();
    }

    /**
     * @param transformations
     *            to use instead of the helmert transformation(s).
     * @param datum
     *            of this crs
     * @param axisOrder
     * @param identity
     */
    public CRS( List<Transformation> transformations, IDatum datum, IAxis[] axisOrder, CRSResource identity ) {
        super( identity );
        if ( axisOrder != null ) {
            this.axisOrder = new Axis[axisOrder.length];
            System.arraycopy( axisOrder, 0, this.axisOrder, 0, axisOrder.length );
        } else {
            // rb: what to do
            this.axisOrder = null;
        }

        this.usedDatum = datum;
        if ( transformations == null ) {
            transformations = new LinkedList<Transformation>();
        }
        this.transformations = transformations;
    }

    /**
     * @return (all) axis' in their defined order.
     */
    public IAxis[] getAxis() {
        IAxis[] result = new Axis[axisOrder.length];
        System.arraycopy( axisOrder, 0, result, 0, axisOrder.length );
        return result;
    }

    /**
     * @return the usedDatum or <code>null</code> if the datum was not a Geodetic one.
     */
    public final IGeodeticDatum getGeodeticDatum() {
        return ( usedDatum instanceof IGeodeticDatum ) ? (IGeodeticDatum) usedDatum : null;
    }

    /**
     * @return the datum of this coordinate system.
     */
    public final IDatum getDatum() {
        return usedDatum;
    }

    /**
     * @return the units of all axis of the ICoordinateSystem.
     */
    public IUnit[] getUnits() {
        IAxis[] allAxis = getAxis();
        IUnit[] result = new Unit[allAxis.length];
        for ( int i = 0; i < allAxis.length; ++i ) {
            result[i] = allAxis[i].getUnits();
        }
        return result;
    }

    /**
     * @param targetCRS
     *            to get the alternative Transformation for.
     * @return true if this crs has an alternative transformation for the given ICoordinateSystem, false otherwise.
     */
    public boolean hasDirectTransformation( ICRS targetCRS ) {
        if ( targetCRS == null ) {
            return false;
        }
        for ( Transformation transformation : transformations ) {
            if ( transformation != null && transformation.canTransform( this, targetCRS ) ) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param targetCRS
     *            to get the alternative transformation for.
     * @return the transformation associated with the given crs, <code>null</code> otherwise.
     */
    public Transformation getDirectTransformation( ICRS targetCRS ) {
        if ( targetCRS == null ) {
            return null;
        }
        for ( Transformation transformation : transformations ) {
            if ( transformation.canTransform( this, targetCRS ) ) {
                return transformation;
            }
        }
        return null;
    }

    /**
     * Converts the given coordinates in given to the unit of the respective axis.
     * 
     * @param coordinates
     *            to convert to.
     * @param units
     *            in which the coordinates were given.
     * @param invert
     *            if the operation should be inverted, e.g. the coordinates are given in the axis units and should be
     *            converted to the given units.
     * @return the converted coordinates.
     */
    public Point3d convertToAxis( Point3d coordinates, IUnit[] units, boolean invert ) {
        if ( units != null && units.length < getDimension() && units.length > 0 ) {
            IUnit[] axisUnits = getUnits();
            for ( int i = 0; i < axisUnits.length; i++ ) {
                IUnit axisUnit = axisUnits[i];
                double value = ( i == 0 ) ? coordinates.x : ( i == 1 ) ? coordinates.y : coordinates.z;
                if ( i < units.length ) {
                    IUnit coordinateUnit = units[i];
                    if ( invert ) {
                        value = axisUnit.convert( value, coordinateUnit );
                    } else {
                        value = coordinateUnit.convert( value, axisUnit );
                    }
                }
                if ( i == 0 ) {
                    coordinates.x = value;
                } else if ( i == 1 ) {
                    coordinates.y = value;
                } else {
                    coordinates.z = value;
                }
            }
        }
        return coordinates;
    }

    /**
     * Helper function to get the typename as a String.
     * 
     * @return either the type as a name or 'Unknown' if the type is not known.
     */
    protected String getTypeName() {
        return getType().toString();
    }

    /**
     * Checks if the given axis match this.axisOrder[] in length, but flipped x/y ([0]/[1]) order.
     * 
     * @param otherAxis
     *            the axis to check
     * @return true if the given axis match this.axisOrder[] false otherwise.
     */
    private boolean matchAxisWithFlippedOrder( IAxis[] otherAxis ) {
        IAxis[] allAxis = getAxis();
        if ( otherAxis.length != allAxis.length || otherAxis.length < 2 ) {
            return false;
        }
        IAxis aX = allAxis[0];
        IAxis bY = otherAxis[0];
        IAxis aY = allAxis[1];
        IAxis bX = otherAxis[1];
        if ( !aX.equals( bX ) && !aY.equals( bY ) ) {
            return false;
        }
        for ( int i = 2; i < allAxis.length; ++i ) {
            IAxis a = allAxis[i];
            IAxis b = otherAxis[i];
            if ( !a.equals( b ) ) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if the given axis match this.axisOrder[] in length and order.
     * 
     * @param otherAxis
     *            the axis to check
     * @return true if the given axis match this.axisOrder[] false otherwise.
     */
    private boolean matchAxis( IAxis[] otherAxis ) {
        IAxis[] allAxis = getAxis();
        if ( otherAxis.length != allAxis.length ) {
            return false;
        }
        for ( int i = 0; i < allAxis.length; ++i ) {
            IAxis a = allAxis[i];
            IAxis b = otherAxis[i];
            if ( !a.equals( b ) ) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean equals( Object other ) {
        if ( other instanceof CRSRef ) {
            other = ( (CRSRef) other ).getReferencedObject();
        }
        if ( other != null && other instanceof ICRS ) {
            final ICRS that = (CRS) other;
            return that.getType() == this.getType() && that.getDimension() == this.getDimension()
                   && matchAxis( that.getAxis() ) && super.equals( that ) && that.getDatum().equals( this.getDatum() );
        }
        return false;
    }

    public boolean equalsWithFlippedAxis( Object other ) {
        if ( other instanceof CRSRef ) {
            other = ( (CRSRef) other ).getReferencedObject();
        }
        if ( other != null && other instanceof ICRS ) {
            final ICRS that = (CRS) other;
            return that.getType() == this.getType() && that.getDimension() == this.getDimension()
                   && matchAxisWithFlippedOrder( that.getAxis() ) && that.getDatum().equals( this.getDatum() );
        }
        return false;
    }

    /**
     * Implementation as proposed by Joshua Block in Effective Java (Addison-Wesley 2001), which supplies an even
     * distribution and is relatively fast. It is created from field <b>f</b> as follows:
     * <ul>
     * <li>boolean -- code = (f ? 0 : 1)</li>
     * <li>byte, char, short, int -- code = (int)f</li>
     * <li>long -- code = (int)(f ^ (f &gt;&gt;&gt;32))</li>
     * <li>float -- code = Float.floatToIntBits(f);</li>
     * <li>double -- long l = Double.doubleToLongBits(f); code = (int)(l ^ (l &gt;&gt;&gt; 32))</li>
     * <li>all Objects, (where equals(&nbsp;) calls equals(&nbsp;) for this field) -- code = f.hashCode(&nbsp;)</li>
     * <li>Array -- Apply above rules to each element</li>
     * </ul>
     * <p>
     * Combining the hash code(s) computed above: result = 37 * result + code;
     * </p>
     * 
     * @return (int) ( result >>> 32 ) ^ (int) result;
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        // the 2.nd million th. prime, :-)
        long code = 32452843;
        if ( getAxis() != null ) {
            for ( IAxis ax : getAxis() ) {
                code = code * 37 + ax.hashCode();
            }
        }
        if ( usedDatum != null ) {
            code = code * 37 + usedDatum.hashCode();
        }
        code = getType().name().hashCode();
        code = code * 37 + getDimension();
        return (int) ( code >>> 32 ) ^ (int) code;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder( super.toString() );
        sb.append( "\n - type: " ).append( getTypeName() );
        sb.append( "\n - datum: " ).append( usedDatum );
        sb.append( "\n - dimension: " ).append( getDimension() );
        for ( IAxis a : getAxis() ) {
            sb.append( "\n - axis: " ).append( a.toString() );
        }
        return sb.toString();

    }

    /**
     * @return the polynomial transformations.
     */
    public final List<Transformation> getTransformations() {
        return transformations;
    }

    // public void setDefaultIdentifier( CRSCodeType crsCode ) {
    // super.setDefaultIdentifier( crsCode );
    // }

    /**
     * Return the axis index associated with an easting value, if the axis could not be determined {@link Axis#AO_OTHER}
     * 0 will be returned.
     * 
     * @return the index of the axis which represents the easting/westing component of a coordinate tuple.
     */
    public int getEasting() {
        IAxis[] axis = getAxis();
        for ( int i = 0; i < axis.length; ++i ) {
            IAxis a = axis[i];
            if ( a != null ) {
                if ( a.getOrientation() == Axis.AO_EAST || a.getOrientation() == Axis.AO_WEST ) {
                    return i;
                }
            }
        }
        return 0;
    }

    /**
     * Return the axis index associated with a northing value, if the axis could not be determined (e.g not is
     * {@link Axis#AO_NORTH} {@link Axis#AO_SOUTH} or {@link Axis#AO_UP} or {@link Axis#AO_DOWN}) 1 will be returned.
     * 
     * @return the index of the axis which represents the easting/westing component of a coordinate tuple.
     */
    public int getNorthing() {
        IAxis[] axis = getAxis();
        for ( int i = 0; i < axis.length; ++i ) {
            IAxis a = axis[i];
            if ( a != null ) {
                if ( a.getOrientation() == Axis.AO_NORTH || a.getOrientation() == Axis.AO_SOUTH
                     || a.getOrientation() == Axis.AO_DOWN || a.getOrientation() == Axis.AO_UP ) {
                    return i;
                }
            }
        }
        return 1;
    }

    /**
     * Returns the approximate domain of validity of this coordinate system. The returned array will contain the values
     * in the appropriate coordinate system and with the appropriate axis order.
     * 
     * @return the real world coordinates of the domain of validity of this crs, or <code>null</code> if the valid
     *         domain could not be determined
     */
    public double[] getValidDomain() {
        synchronized ( LOCK ) {
            if ( this.validDomain == null ) {
                double[] bbox = getAreaOfUseBBox();
                // transform world to coordinates in sourceCRS;
                CoordinateTransformer t = new CoordinateTransformer( this );
                try {
                    ICRS defWGS = GeographicCRS.WGS84;
                    try {
                        // rb: lookup the default WGS84 in the registry, it may be, that the axis are swapped.
                        defWGS = CRSManager.lookup( GeographicCRS.WGS84.getCode() );
                    } catch ( Exception e ) {
                        // catch any exceptions and use the default.
                    }
                    int xAxis = defWGS.getEasting();
                    int yAxis = 1 - xAxis;

                    int pointsPerSide = 5;

                    double axis0Min = bbox[xAxis];
                    double axis1Min = bbox[yAxis];
                    double axis0Max = bbox[xAxis + 2];
                    double axis1Max = bbox[yAxis + 2];

                    double span0 = Math.abs( axis0Max - axis0Min );
                    double span1 = Math.abs( axis1Max - axis1Min );

                    double axis0Step = span0 / ( pointsPerSide + 1 );
                    double axis1Step = span1 / ( pointsPerSide + 1 );

                    List<Point3d> points = new ArrayList<Point3d>( pointsPerSide * 4 + 4 );
                    double zValue = getDimension() == 3 ? 0 : Double.NaN;

                    for ( int i = 0; i <= pointsPerSide + 1; i++ ) {
                        points.add( new Point3d( axis0Min + i * axis0Step, axis1Min, zValue ) );
                        points.add( new Point3d( axis0Min + i * axis0Step, axis1Max, zValue ) );
                        points.add( new Point3d( axis0Min, axis1Min + i * axis1Step, zValue ) );
                        points.add( new Point3d( axis0Max, axis1Min + i * axis1Step, zValue ) );
                    }

                    points = t.transform( defWGS, points );
                    axis0Min = Double.MAX_VALUE;
                    axis1Min = Double.MAX_VALUE;
                    axis0Max = Double.NEGATIVE_INFINITY;
                    axis1Max = Double.NEGATIVE_INFINITY;
                    for ( Point3d p : points ) {
                        axis0Min = Math.min( p.x, axis0Min );
                        axis1Min = Math.min( p.y, axis1Min );
                        axis0Max = Math.max( p.x, axis0Max );
                        axis1Max = Math.max( p.y, axis1Max );
                    }

                    this.validDomain = new double[4];
                    validDomain[0] = axis0Min;
                    validDomain[1] = axis1Min;
                    validDomain[2] = axis0Max;
                    validDomain[3] = axis1Max;
                } catch ( IllegalArgumentException e ) {
                    LOG.debug( "Exception occurred: " + e.getLocalizedMessage(), e );
                    LOG.debug( "Exception occurred: " + e.getLocalizedMessage() );

                } catch ( org.deegree.cs.exceptions.TransformationException e ) {
                    LOG.debug( "Exception occurred: " + e.getLocalizedMessage(), e );
                    LOG.debug( "Exception occurred: " + e.getLocalizedMessage() );
                }
            }
        }
        return validDomain != null ? Arrays.copyOf( validDomain, 4 ) : null;
    }

    /**
     * @return the alias of a concrete CRS is the first Code
     */
    @Override
    public String getAlias() {
        return getCode().getOriginal();
    }
}
