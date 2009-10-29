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

package org.deegree.crs.coordinatesystems;

import java.util.LinkedList;
import java.util.List;

import javax.vecmath.Point3d;

import org.deegree.crs.CRSCodeType;
import org.deegree.crs.CRSIdentifiable;
import org.deegree.crs.components.Axis;
import org.deegree.crs.components.Datum;
import org.deegree.crs.components.GeodeticDatum;
import org.deegree.crs.components.Unit;
import org.deegree.crs.transformations.polynomial.PolynomialTransformation;

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

public abstract class CoordinateSystem extends CRSIdentifiable {

    private Axis[] axisOrder;

    private Datum usedDatum;

    private final List<PolynomialTransformation> transformations;

    /**
     * Defines this CRS as a GeoCentric one.
     */
    public static final int GEOCENTRIC_CRS = 0;

    /**
     * Defines this CRS as a Geographic one.
     */
    public static final int GEOGRAPHIC_CRS = 1;

    /**
     * Defines this CRS as a Projected one.
     */
    public static final int PROJECTED_CRS = 2;

    /**
     * Defines this CRS as a Compound one.
     */
    public static final int COMPOUND_CRS = 3;

    /**
     * Defines this CRS as a Vertical one.
     */
    public static final int VERTICAL_CRS = 4;

    /**
     * @param datum
     *            of this coordinate system.
     * @param axisOrder
     *            the axisorder of this coordinate system.
     * @param identity
     */
    public CoordinateSystem( Datum datum, Axis[] axisOrder, CRSIdentifiable identity ) {
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
    public CoordinateSystem( Datum datum, Axis[] axisOrder, CRSCodeType[] codes, String[] names, String[] versions,
                             String[] descriptions, String[] areasOfUse ) {
        super( codes, names, versions, descriptions, areasOfUse );
        this.axisOrder = axisOrder;
        this.usedDatum = datum;
        this.transformations = new LinkedList<PolynomialTransformation>();
    }

    /**
     * @param transformations
     *            to use instead of the helmert transformation(s).
     * @param datum
     *            of this crs
     * @param axisOrder
     * @param identity
     */
    public CoordinateSystem( List<PolynomialTransformation> transformations, Datum datum, Axis[] axisOrder,
                             CRSIdentifiable identity ) {
        super( identity );
        this.axisOrder = axisOrder;
        this.usedDatum = datum;
        if ( transformations == null ) {
            transformations = new LinkedList<PolynomialTransformation>();
        }
        this.transformations = transformations;
    }

    /**
     * @return (all) axis' in their defined order.
     */
    public Axis[] getAxis() {
        return axisOrder;
    }

    /**
     * @return the usedDatum or <code>null</code> if the datum was not a Geodetic one.
     */
    public final GeodeticDatum getGeodeticDatum() {
        return ( usedDatum instanceof GeodeticDatum ) ? (GeodeticDatum) usedDatum : null;
    }

    /**
     * @return the datum of this coordinate system.
     */
    public final Datum getDatum() {
        return usedDatum;
    }

    /**
     * @return the units of all axis of the coordinatesystem.
     */
    public Unit[] getUnits() {
        Axis[] allAxis = getAxis();
        Unit[] result = new Unit[allAxis.length];
        for ( int i = 0; i < allAxis.length; ++i ) {
            result[i] = allAxis[i].getUnits();
        }
        return result;
    }

    /**
     * @return the dimension of this CRS.
     */
    public abstract int getDimension();

    /**
     * @return one of the *_CRS types defined in this class.
     */
    public abstract int getType();

    /**
     * @param targetCRS
     *            to get the alternative Transformation for.
     * @return true if this crs has an alternative transformation for the given coordinatesystem, false otherwise.
     */
    public boolean hasDirectTransformation( CoordinateSystem targetCRS ) {
        if ( targetCRS == null ) {
            return false;
        }
        for ( PolynomialTransformation transformation : transformations ) {
            if ( transformation != null && targetCRS.equals( transformation.getTargetCRS() ) ) {
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
    public PolynomialTransformation getDirectTransformation( CoordinateSystem targetCRS ) {
        if ( targetCRS == null ) {
            return null;
        }
        for ( PolynomialTransformation transformation : transformations ) {
            if ( transformation.getTargetCRS().equals( targetCRS ) ) {
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
    public Point3d convertToAxis( Point3d coordinates, Unit[] units, boolean invert ) {
        if ( units != null && units.length < getDimension() && units.length > 0 ) {
            Unit[] axisUnits = getUnits();
            for ( int i = 0; i < axisUnits.length; i++ ) {
                Unit axisUnit = axisUnits[i];
                double value = ( i == 0 ) ? coordinates.x : ( i == 1 ) ? coordinates.y : coordinates.z;
                if ( i < units.length ) {
                    Unit coordinateUnit = units[i];
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
        switch ( getType() ) {
        case GEOCENTRIC_CRS:
            return "Geocentric CRS";
        case PROJECTED_CRS:
            return "Projected CRS";
        case GEOGRAPHIC_CRS:
            return "Geographic CRS";
        case COMPOUND_CRS:
            return "Compound CRS";
        default:
            return "Unknown CRS";
        }
    }

    /**
     * Checks if the given axis match this.axisOrder[] in length and order.
     * 
     * @param otherAxis
     *            the axis to check
     * @return true if the given axis match this.axisOrder[] false otherwise.
     */
    private boolean matchAxis( Axis[] otherAxis ) {
        Axis[] allAxis = getAxis();
        if ( otherAxis.length != allAxis.length ) {
            return false;
        }
        for ( int i = 0; i < allAxis.length; ++i ) {
            Axis a = allAxis[i];
            Axis b = otherAxis[i];
            if ( !a.equals( b ) ) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean equals( Object other ) {
        if ( other != null && other instanceof CoordinateSystem ) {
            final CoordinateSystem that = (CoordinateSystem) other;
            return that.getType() == this.getType() && that.getDimension() == this.getDimension()
                   && matchAxis( that.getAxis() ) && super.equals( that )
                   && that.getGeodeticDatum().equals( this.getGeodeticDatum() );
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
            for ( Axis ax : getAxis() ) {
                code = code * 37 + ax.hashCode();
            }
        }
        if ( usedDatum != null ) {
            code = code * 37 + usedDatum.hashCode();
        }
        code = code * 37 + getType();
        code = code * 37 + getDimension();
        return (int) ( code >>> 32 ) ^ (int) code;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder( super.toString() );
        sb.append( "\n - type: " ).append( getTypeName() );
        sb.append( "\n - datum: " ).append( usedDatum );
        sb.append( "\n - dimension: " ).append( getDimension() );
        for ( Axis a : getAxis() ) {
            sb.append( "\n - axis: " ).append( a.toString() );
        }
        return sb.toString();

    }

    /**
     * @return the polynomial transformations.
     */
    public final List<PolynomialTransformation> getTransformations() {
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
        Axis[] axis = getAxis();
        for ( int i = 0; i < axis.length; ++i ) {
            Axis a = axis[i];
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
        Axis[] axis = getAxis();
        for ( int i = 0; i < axis.length; ++i ) {
            Axis a = axis[i];
            if ( a != null ) {
                if ( a.getOrientation() == Axis.AO_NORTH || a.getOrientation() == Axis.AO_SOUTH
                     || a.getOrientation() == Axis.AO_DOWN || a.getOrientation() == Axis.AO_UP ) {
                    return i;
                }
            }
        }
        return 1;
    }
}
