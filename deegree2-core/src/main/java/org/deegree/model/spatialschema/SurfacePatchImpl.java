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
package org.deegree.model.spatialschema;

import java.io.Serializable;

import javax.vecmath.Vector3d;

import org.deegree.model.crs.CoordinateSystem;

/**
 * default implementation of the SurfacePatch interface from package deegree.model.spatialschema. the class is abstract because it should
 * be specialized by derived classes <code>Polygon</code> for example
 *
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public abstract class SurfacePatchImpl implements GenericSurface, SurfacePatch, Serializable {
    /** Use serialVersionUID for interoperability. */
    private final static long serialVersionUID = 7641735268892225180L;

    protected CoordinateSystem crs = null;

    protected Point centroid = null;

    protected SurfaceInterpolation interpolation = null;

    protected Ring exteriorRing = null;

    protected Ring[] interiorRings = null;

    protected double area = 0;

    protected boolean valid = false;

    /**
     *
     * @param exteriorRing
     * @param interiorRings
     * @param crs
     * @throws GeometryException 
     */
    protected SurfacePatchImpl( Ring exteriorRing, Ring[] interiorRings, CoordinateSystem crs ) throws GeometryException {
        this.exteriorRing = exteriorRing;
        if ( interiorRings == null ) {
            this.interiorRings = new Ring[0];
        } else {
            this.interiorRings = interiorRings;
        }
     // check, if the exteriorRing of the polygon is closed
        // and if the interiorRings (if !=null) are closed
        Position[] pos = this.exteriorRing.getPositions();
        if ( !pos[0].equals(pos[pos.length - 1] ) ) {
            throw new GeometryException( "The exterior ring isn't closed!" );
        }
        
        if ( interiorRings != null ) {
            for ( int i = 0; i < interiorRings.length; i++ ) {
                pos = interiorRings[i].getPositions();
                if ( !pos[0].equals( pos[pos.length - 1] ) ) {
                    throw new GeometryException( "The interior ring " + i + " isn't closed!" );
                }
            }
        }
        
        this.crs = crs;
    }

    /**
     * Creates a new SurfacePatchImpl object.
     *
     * @param interpolation
     * @param exteriorRing
     * @param interiorRings
     * @param crs
     *
     * @throws GeometryException
     */
    protected SurfacePatchImpl( SurfaceInterpolation interpolation, Position[] exteriorRing,
                                Position[][] interiorRings, CoordinateSystem crs ) throws GeometryException {
        this.crs = crs;

        if ( ( exteriorRing == null ) || ( exteriorRing.length < 3 ) ) {
            throw new GeometryException( "The exterior ring doesn't contains enough point!" );
        }

        // check, if the exteriorRing of the polygon is closed
        // and if the interiorRings (if !=null) are closed
        if ( !exteriorRing[0].equals( exteriorRing[exteriorRing.length - 1] ) ) {
            throw new GeometryException( "The exterior ring isn't closed!" );
        }

        if ( interiorRings != null ) {
            for ( int i = 0; i < interiorRings.length; i++ ) {
                if ( !interiorRings[i][0].equals( interiorRings[i][interiorRings[i].length - 1] ) ) {
                    throw new GeometryException( "The interior ring " + i + " isn't closed!" );
                }
            }
        }

        this.interpolation = interpolation;
        this.exteriorRing = new RingImpl( exteriorRing, crs );
        if ( interiorRings != null ) {
            this.interiorRings = new Ring[interiorRings.length];
            for ( int i = 0; i < interiorRings.length; i++ ) {
                this.interiorRings[i] = new RingImpl( interiorRings[i], crs );
            }
        }

        setValid( false );
    }

    /**
     * invalidates the calculated parameters of the Geometry
     *
     * @param valid
     */
    protected void setValid( boolean valid ) {
        this.valid = valid;
    }

    /**
     * returns true if the calculated parameters of the Geometry are valid and false if they must be recalculated
     *
     * @return true if the calculated parameters of the Geometry are valid and false if they must be recalculated
     */
    protected boolean isValid() {
        return valid;
    }

    public SurfaceInterpolation getInterpolation() {
        return interpolation;
    }

    public Envelope getEnvelope() {
        return exteriorRing.getEnvelope();
    }

    public Position[] getExteriorRing() {
        return exteriorRing.getPositions();
    }

    public Position[][] getInteriorRings() {
        if ( interiorRings != null ) {
            Position[][] pos = new Position[interiorRings.length][];
            for ( int i = 0; i < pos.length; i++ ) {
                pos[i] = interiorRings[i].getPositions();
            }
            return pos;
        }
        return new Position[0][0];
    }

    public Ring getExterior() {
        return exteriorRing;
    }

    public Ring[] getInterior() {
        return interiorRings;
    }

    /**
     * @return -1
     */
    public double getPerimeter() {
        return -1;
    }

    public CoordinateSystem getCoordinateSystem() {
        return crs;
    }

    @Override
    public boolean equals( Object other ) {
        if ( ( other == null ) || !( other instanceof SurfacePatch ) ) {
            return false;
        }

        // Assuming envelope cannot be null (always calculated)
        if ( !getEnvelope().equals( ( (SurfacePatch) other ).getEnvelope() ) ) {
            return false;
        }

        // check positions of exterior ring
        Position[] pos1 = getExteriorRing();
        Position[] pos2 = ( (SurfacePatch) other ).getExteriorRing();
        if ( pos1.length != pos2.length ) {
            return false;            
        }
        for ( int i = 0; i < pos2.length; i++ ) {
            if ( !pos1[i].equals( pos2[i] ) ) {
                return false;
            }
        }

        // Assuming either can have interiorRings set to null (not checked
        // by Constructor)
        if ( getInterior() != null ) {
            if ( ( (SurfacePatch) other ).getInterior() == null ) {
                return false;
            }
            if ( getInterior().length != ( (SurfacePatch) other ).getInterior().length ) {
                return false;
            }
            for ( int i = 0; i < getInterior().length; i++ ) {
                // TODO
            }
        } else {
            if ( ( (SurfacePatch) other ).getInterior() != null ) {
                return false;
            }
        }

        return true;
    }

    public Point getCentroid() {
        if ( !isValid() ) {
            calculateParam();
        }
        return centroid;
    }

    public double getArea() {
        if ( !isValid() ) {
            calculateParam();
        }
        return area;
    }

    /**
     * calculates the centroid (2D) and area (2D + 3D) of the surface patch.
     */
    private void calculateCentroidArea() {

        double varea = calculateArea( exteriorRing.getPositions() );

        Position centroid_ = calculateCentroid( exteriorRing.getPositions() );

        double x = centroid_.getX();
        double y = centroid_.getY();

        x *= varea;
        y *= varea;

        double tmp = 0;
        if ( interiorRings != null ) {
            for ( int i = 0; i < interiorRings.length; i++ ) {
                double dum = calculateArea( interiorRings[i].getPositions() );
                tmp += dum;
                Position temp = calculateCentroid( interiorRings[i].getPositions() );
                x += ( temp.getX() * -dum );
                y += ( temp.getY() * -dum );
            }
        }

        area = varea - tmp;
        centroid = new PointImpl( x / area, y / area, crs );

    }

    /**
     * calculates the centroid and the area of the surface patch
     */
    protected void calculateParam() {
        calculateCentroidArea();
        setValid( true );
    }

    /**
     * calculates the area of the surface patch 2D: taken from gems iv (modified) 3D: see
     * http://geometryalgorithms.com/Archive/algorithm_0101/#3D%20Polygons
     */
    private double calculateArea( Position[] point ) {

        double calcArea = 0;

        // two-dimensional
        if ( point[0].getCoordinateDimension() == 2 ) {
            int i;
            int j;
            double ai;
            double atmp = 0;

            for ( i = point.length - 1, j = 0; j < point.length; i = j, j++ ) {
                double xi = point[i].getX() - point[0].getX();
                double yi = point[i].getY() - point[0].getY();
                double xj = point[j].getX() - point[0].getX();
                double yj = point[j].getY() - point[0].getY();
                ai = ( xi * yj ) - ( xj * yi );
                atmp += ai;
            }
            calcArea = Math.abs( atmp / 2 );

        }
        // three-dimensional
        else if ( point[0].getCoordinateDimension() == 3 ) {

            Vector3d planeNormal = new Vector3d();
            planeNormal.cross( sub( point[1], point[0] ), sub( point[2], point[1] ) );
            planeNormal.normalize();

            Vector3d resultVector = new Vector3d();
            for ( int i = 0; i < point.length - 1; ++i ) {
                Vector3d tmp = cross( point[i], point[i + 1] );
                resultVector.add( tmp );
            }
            calcArea = ( planeNormal.dot( resultVector ) ) * 0.5;
        }
        return calcArea;
    }

    /**
     * calculates the centroid of the surface patch
     * <p>
     * taken from gems iv (modified)
     * <p>
     * </p>
     * this method is only valid for the two-dimensional case.
     *
     * @param point
     * @return the centroid of given positions
     */
    protected Position calculateCentroid( Position[] point ) {

        int i;
        int j;
        double ai;
        double x;
        double y;
        double atmp = 0;
        double xtmp = 0;
        double ytmp = 0;

        // move points to the origin of the coordinate space
        // (to solve precision issues)
        double transX = point[0].getX();
        double transY = point[0].getY();

        for ( i = point.length - 1, j = 0; j < point.length; i = j, j++ ) {
            double x1 = point[i].getX() - transX;
            double y1 = point[i].getY() - transY;
            double x2 = point[j].getX() - transX;
            double y2 = point[j].getY() - transY;
            ai = ( x1 * y2 ) - ( x2 * y1 );
            atmp += ai;
            xtmp += ( ( x2 + x1 ) * ai );
            ytmp += ( ( y2 + y1 ) * ai );
        }

        if ( atmp != 0 ) {
            x = xtmp / ( 3 * atmp ) + transX;
            y = ytmp / ( 3 * atmp ) + transY;
        } else {
            x = point[0].getX();
            y = point[0].getY();
        }

        return new PositionImpl( x, y );
    }

    @Override
    public String toString() {
        String ret = "SurfacePatch: ";
        ret = "interpolation = " + interpolation + "\n";
        ret += "exteriorRing = \n";
        ret += ( exteriorRing + "\n" );
        ret += ( "interiorRings = " + interiorRings + "\n" );
        ret += ( "envelope = " + getEnvelope() + "\n" );
        return ret;
    }

    /**
     * this(x,y,z) = a(x,y,z) - b(x,y,z)
     */
    private Vector3d sub( Position a, Position b ) {
        Vector3d result = new Vector3d( a.getX() - b.getX(), a.getY() - b.getY(), a.getZ() - b.getZ() );
        return result;
    }

    /**
     * this(x,y,z) = a(x,y,z) x b(x,y,z)
     */
    private Vector3d cross( Position a, Position b ) {
        Vector3d result = new Vector3d( ( a.getY() * b.getZ() ) - ( a.getZ() * b.getY() ), ( a.getZ() * b.getX() )
                                                                                           - ( a.getX() * b.getZ() ),
                                        ( a.getX() * b.getY() ) - ( a.getY() * b.getX() ) );
        return result;
    }
}
