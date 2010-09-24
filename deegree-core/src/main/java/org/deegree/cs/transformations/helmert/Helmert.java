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
package org.deegree.cs.transformations.helmert;

import static org.deegree.cs.transformations.TransformationFactory.createWGSAlligned;
import static org.deegree.cs.transformations.coordinate.ConcatenatedTransform.concatenate;
import static org.deegree.cs.transformations.coordinate.MatrixTransform.createMatrixTransform;
import static org.deegree.cs.utilities.Matrix.swapAndRotateGeoAxis;
import static org.deegree.cs.utilities.ProjectionUtils.EPS11;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.List;

import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;

import org.deegree.cs.CRSCodeType;
import org.deegree.cs.CRSIdentifiable;
import org.deegree.cs.components.GeodeticDatum;
import org.deegree.cs.coordinatesystems.CoordinateSystem;
import org.deegree.cs.coordinatesystems.GeocentricCRS;
import org.deegree.cs.coordinatesystems.GeographicCRS;
import org.deegree.cs.exceptions.TransformationException;
import org.deegree.cs.transformations.Transformation;
import org.deegree.cs.transformations.coordinate.GeocentricTransform;
import org.deegree.cs.utilities.Matrix;
import org.slf4j.Logger;

/**
 * Parameters for a geographic transformation into another datum. The Bursa Wolf parameters should be applied to
 * geocentric coordinates, where the X axis points towards the Greenwich Prime Meridian, the Y axis points East, and the
 * Z axis points North.
 * 
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * 
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
public class Helmert extends Transformation {

    private static final Logger LOG = getLogger( Helmert.class );

    /** Bursa Wolf shift in meters. */
    public double dx;

    /** Bursa Wolf shift in meters. */
    public double dy;

    /** Bursa Wolf shift in meters. */
    public double dz;

    /** Bursa Wolf rotation in arc seconds, which is 1/3600 of a degree. */
    public double ex;

    /** Bursa Wolf rotation in arc seconds. */
    public double ey;

    /** Bursa Wolf rotation in arc seconds. */
    public double ez;

    /** Bursa Wolf scaling in parts per million. */
    public double ppm;

    private Matrix4d transformMatrix = null;

    private Matrix4d inverseMatrix = null;

    private boolean rotationInRadians = false;

    /**
     * @param dx
     *            Bursa Wolf shift in meters.
     * @param dy
     *            Bursa Wolf shift in meters.
     * @param dz
     *            Bursa Wolf shift in meters.
     * @param ex
     *            Bursa Wolf rotation in arc seconds or in radians (by setting the flag).
     * @param ey
     *            Bursa Wolf rotation in arc seconds or in radians (by setting the flag).
     * @param ez
     *            Bursa Wolf rotation in arc seconds or in radians (by setting the flag).
     * @param ppm
     *            Bursa Wolf scaling in parts per million.
     * @param sourceCRS
     *            of this helmert transformation
     * @param targetCRS
     *            of this helmert transformation
     * @param identifiable
     *            object containing all relevant id.
     * @param inRadians
     *            true if the rotation parameters are in radians
     */
    public Helmert( double dx, double dy, double dz, double ex, double ey, double ez, double ppm,
                    CoordinateSystem sourceCRS, CoordinateSystem targetCRS, CRSIdentifiable identifiable,
                    boolean inRadians ) {
        super( sourceCRS, targetCRS, identifiable );
        this.dx = dx;
        this.dy = dy;
        this.dz = dz;
        this.ex = ex;
        this.ey = ey;
        this.ez = ez;
        this.ppm = ppm;
        rotationInRadians = inRadians;
    }

    /**
     * @param dx
     *            Bursa Wolf shift in meters.
     * @param dy
     *            Bursa Wolf shift in meters.
     * @param dz
     *            Bursa Wolf shift in meters.
     * @param ex
     *            Bursa Wolf rotation in arc seconds.
     * @param ey
     *            Bursa Wolf rotation in arc seconds.
     * @param ez
     *            Bursa Wolf rotation in arc seconds.
     * @param ppm
     *            Bursa Wolf scaling in parts per million.
     * @param sourceCRS
     *            of this helmert transformation
     * @param targetCRS
     *            of this helmert transformation
     * @param identifiable
     *            object containing all relevant id.
     */
    public Helmert( double dx, double dy, double dz, double ex, double ey, double ez, double ppm,
                    CoordinateSystem sourceCRS, CoordinateSystem targetCRS, CRSIdentifiable identifiable ) {
        this( dx, dy, dz, ex, ey, ez, ppm, sourceCRS, targetCRS, identifiable, false );
    }

    /**
     * Construct a conversion info with all parameters set to 0;
     * 
     * @param sourceCRS
     *            of this helmert transformation
     * @param targetCRS
     *            of this helmert transformation
     * 
     * @param codes
     * @param names
     * @param versions
     * @param descriptions
     * @param areasOfUse
     */
    public Helmert( CoordinateSystem sourceCRS, CoordinateSystem targetCRS, CRSCodeType[] codes, String[] names,
                    String[] versions, String[] descriptions, String[] areasOfUse ) {
        this( 0, 0, 0, 0, 0, 0, 0, sourceCRS, targetCRS, new CRSIdentifiable( codes, names, versions, descriptions,
                                                                              areasOfUse ) );
    }

    /**
     * Construct a conversion info with all parameters set to 0;
     * 
     * @param sourceCRS
     *            of this helmert transformation
     * @param targetCRS
     *            of this helmert transformation
     * @param code
     */
    public Helmert( CoordinateSystem sourceCRS, CoordinateSystem targetCRS, CRSCodeType code ) {
        this( sourceCRS, targetCRS, new CRSCodeType[] { code } );
    }

    /**
     * Construct a conversion info with all parameters set to 0;
     * 
     * @param sourceCRS
     *            of this helmert transformation
     * @param targetCRS
     *            of this helmert transformation
     * @param codes
     */
    public Helmert( CoordinateSystem sourceCRS, CoordinateSystem targetCRS, CRSCodeType[] codes ) {
        this( sourceCRS, targetCRS, codes, null, null, null, null );
    }

    /**
     * Construct a conversion info with all parameters set to 0, the target crs is {@link GeographicCRS#WGS84}
     * 
     * @param sourceCRS
     *            of this helmert transformation
     * @param codes
     */
    public Helmert( CoordinateSystem sourceCRS, CRSCodeType[] codes ) {
        this( sourceCRS, GeographicCRS.WGS84, codes, null, null, null, null );
    }

    /**
     * @param dx
     *            Bursa Wolf shift in meters.
     * @param dy
     *            Bursa Wolf shift in meters.
     * @param dz
     *            Bursa Wolf shift in meters.
     * @param ex
     *            Bursa Wolf rotation in arc seconds.
     * @param ey
     *            Bursa Wolf rotation in arc seconds.
     * @param ez
     *            Bursa Wolf rotation in arc seconds.
     * @param ppm
     *            Bursa Wolf scaling in parts per million.
     * @param sourceCRS
     *            of this helmert transformation
     * @param targetCRS
     *            of this helmert transformation
     * @param codes
     * @param names
     * @param versions
     * @param descriptions
     * @param areaOfUses
     */
    public Helmert( double dx, double dy, double dz, double ex, double ey, double ez, double ppm,
                    CoordinateSystem sourceCRS, CoordinateSystem targetCRS, CRSCodeType[] codes, String[] names,
                    String[] versions, String[] descriptions, String[] areaOfUses ) {
        this( dx, dy, dz, ex, ey, ez, ppm, sourceCRS, targetCRS, new CRSIdentifiable( codes, names, versions,
                                                                                      descriptions, areaOfUses ) );
    }

    /**
     * @param dx
     *            Bursa Wolf shift in meters.
     * @param dy
     *            Bursa Wolf shift in meters.
     * @param dz
     *            Bursa Wolf shift in meters.
     * @param ex
     *            Bursa Wolf rotation in arc seconds.
     * @param ey
     *            Bursa Wolf rotation in arc seconds.
     * @param ez
     *            Bursa Wolf rotation in arc seconds.
     * @param ppm
     *            Bursa Wolf scaling in parts per million.
     * @param sourceCRS
     *            of this helmert transformation
     * @param targetCRS
     *            of this helmert transformation
     * @param code
     * @param name
     * @param version
     * @param description
     * @param areaOfUse
     */
    public Helmert( double dx, double dy, double dz, double ex, double ey, double ez, double ppm,
                    CoordinateSystem sourceCRS, CoordinateSystem targetCRS, CRSCodeType code, String name,
                    String version, String description, String areaOfUse ) {
        this( dx, dy, dz, ex, ey, ez, ppm, sourceCRS, targetCRS, new CRSCodeType[] { code }, new String[] { name },
              new String[] { version }, new String[] { description }, new String[] { areaOfUse } );
    }

    /**
     * @param dx
     *            Bursa Wolf shift in meters.
     * @param dy
     *            Bursa Wolf shift in meters.
     * @param dz
     *            Bursa Wolf shift in meters.
     * @param ex
     *            Bursa Wolf rotation in arc seconds.
     * @param ey
     *            Bursa Wolf rotation in arc seconds.
     * @param ez
     *            Bursa Wolf rotation in arc seconds.
     * @param ppm
     *            Bursa Wolf scaling in parts per million.
     * @param sourceCRS
     *            of this helmert transformation
     * @param targetCRS
     *            of this helmert transformation
     * @param codes
     */
    public Helmert( double dx, double dy, double dz, double ex, double ey, double ez, double ppm,
                    CoordinateSystem sourceCRS, CoordinateSystem targetCRS, CRSCodeType[] codes ) {
        this( dx, dy, dz, ex, ey, ez, ppm, sourceCRS, targetCRS, codes, null, null, null, null );
    }

    /**
     * @param dx
     *            Bursa Wolf shift in meters.
     * @param dy
     *            Bursa Wolf shift in meters.
     * @param dz
     *            Bursa Wolf shift in meters.
     * @param ex
     *            Bursa Wolf rotation in arc seconds.
     * @param ey
     *            Bursa Wolf rotation in arc seconds.
     * @param ez
     *            Bursa Wolf rotation in arc seconds.
     * @param ppm
     *            Bursa Wolf scaling in parts per million.
     * @param sourceCRS
     *            of this helmert transformation
     * @param targetCRS
     *            of this helmert transformation
     * @param code
     */
    public Helmert( double dx, double dy, double dz, double ex, double ey, double ez, double ppm,
                    CoordinateSystem sourceCRS, CoordinateSystem targetCRS, CRSCodeType code ) {
        this( dx, dy, dz, ex, ey, ez, ppm, sourceCRS, targetCRS, new CRSCodeType[] { code } );
    }

    /**
     * Returns an affine transformation also known as the "Helmert" transformation. The matrix representation of this
     * transformation (also known as "Bursa Wolf" formula) is as follows:
     * 
     * <blockquote>
     * 
     * <pre>
     *       S = 1 + {@link #ppm}*1E-6
     *       [ X ]     [ S          -{@link #ez}*S  +{@link #ey}*S   {@link #dx} ]  [ X ]
     *       [ Y ]  = [ +{@link #ez}*S  S          -{@link #ex}*S   {@link #dy} ]  [ Y ]
     *       [ Z ]     [ -{@link #ey}*S   +{@link #ex}*S   S         {@link #dz} ]  [ Z ]
     *       [ 1 ]     [ 0           0           0           1 ]  [ 1 ]
     * </pre>
     * 
     * </blockquote>
     * 
     * This affine transform can be applied to transform <code>geocentric</code> coordinates from one datum into
     * <code>geocentric</code> coordinates of an other datum. see <a
     * href="http://www.posc.org/Epicentre.2_2/DataModel/ExamplesofUsage/eu_cs35.html#CS3523_helmert">
     * http://www.posc.org/Epicentre.2_2/DataModel/ExamplesofUsage/eu_cs35.html</a> for more information.
     * 
     * @return the affine "Helmert" transformation as a Matrix4d.
     */
    public Matrix4d getAsAffineTransform() {
        if ( transformMatrix != null ) {
            return new Matrix4d( transformMatrix );
        }
        // Note: (ex, ey, ez) is a rotation in arc seconds. We need to convert it into radians (the
        // R factor in RS).
        final double S = 1 + ( ppm * 1E-6 );
        double arcToRad = .00000484813681109535; // Math.PI / ( 180. * 3600. )
        if ( rotationInRadians ) {
            arcToRad = 1;
        }
        final double RS = arcToRad * S;

        transformMatrix = new Matrix4d( S, -ez * RS, +ey * RS, dx, +ez * RS, S, -ex * RS, dy, -ey * RS, +ex * RS, S,
                                        dz, 0, 0, 0, 1. );
        return new Matrix4d( transformMatrix );
    }

    /**
     * @return true if any of the helmert parameters were set.
     */
    public boolean hasValues() {
        return !( ex == 0 && ey == 0 && ez == 0 && dx == 0 && dy == 0 && dz == 0 && ppm == 0 );
    }

    @Override
    public boolean equals( final Object other ) {
        if ( other != null && other instanceof Helmert ) {
            final Helmert that = (Helmert) other;
            return ( Math.abs( this.dx - that.dx ) < EPS11 ) && ( Math.abs( this.dy - that.dy ) < EPS11 )
                   && ( Math.abs( this.dz - that.dz ) < EPS11 ) && ( Math.abs( this.ex - that.ex ) < EPS11 )
                   && ( Math.abs( this.ey - that.ey ) < EPS11 ) && ( Math.abs( this.ez - that.ez ) < EPS11 )
                   && ( Math.abs( this.ppm - that.ppm ) < EPS11 ) && super.equals( that );

        }
        return false;
    }

    /**
     * Returns the Well Know Text (WKT) for this object. The WKT is part of OpenGIS's specification and looks like
     * <code>TOWGS84[dx, dy, dz, ex, ey, ez, ppm]</code>.
     * 
     * @return the Well Know Text (WKT) for this object.
     */
    @Override
    public String toString() {
        final StringBuffer buffer = new StringBuffer( super.getCodeAndName() );
        buffer.append( "\n[\"" );
        buffer.append( dx );
        buffer.append( ", " );
        buffer.append( dy );
        buffer.append( ", " );
        buffer.append( dz );
        buffer.append( ", " );
        buffer.append( ex );
        buffer.append( ", " );
        buffer.append( ey );
        buffer.append( ", " );
        buffer.append( ez );
        buffer.append( ", " );
        buffer.append( ppm );
        buffer.append( ']' );
        return buffer.toString();
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
        // the 2nd millionth prime, :-)
        long code = 32452843;
        long tmp = Double.doubleToLongBits( dx );
        code = code * 37 + (int) ( tmp ^ ( tmp >>> 32 ) );

        tmp = Double.doubleToLongBits( dy );
        code = code * 37 + (int) ( tmp ^ ( tmp >>> 32 ) );

        tmp = Double.doubleToLongBits( dz );
        code = code * 37 + (int) ( tmp ^ ( tmp >>> 32 ) );

        tmp = Double.doubleToLongBits( ex );
        code = code * 37 + (int) ( tmp ^ ( tmp >>> 32 ) );

        tmp = Double.doubleToLongBits( ey );
        code = code * 37 + (int) ( tmp ^ ( tmp >>> 32 ) );

        tmp = Double.doubleToLongBits( ez );
        code = code * 37 + (int) ( tmp ^ ( tmp >>> 32 ) );

        tmp = Double.doubleToLongBits( ppm );
        code = code * 37 + (int) ( tmp ^ ( tmp >>> 32 ) );
        return (int) ( code >>> 32 ) ^ (int) code;
    }

    @Override
    public synchronized List<Point3d> doTransform( List<Point3d> srcPts )
                            throws TransformationException {

        if ( srcPts == null || srcPts.size() == 0 ) {
            return srcPts;
        }

        // lazy instantiation
        if ( transformMatrix == null ) {
            transformMatrix = getAsAffineTransform();
        }
        Matrix4d matrix = transformMatrix;

        // create the inverse matrix
        if ( isInverseTransform() ) {
            if ( inverseMatrix == null ) {
                inverseMatrix = getAsAffineTransform();
                inverseMatrix.invert();
            }
            matrix = inverseMatrix;
        }

        for ( Point3d p : srcPts ) {
            boolean zIsNaN = Double.isNaN( p.z );
            if ( zIsNaN ) {
                p.z = 1;
            }
            matrix.transform( p );
            if ( zIsNaN ) {
                p.z = Double.NaN;
            }
        }

        return srcPts;
    }

    @Override
    public String getImplementationName() {
        return "Helmert";
    }

    @Override
    public boolean isIdentity() {
        return !hasValues();
    }

    /**
     * @return
     */
    public boolean areRotationsInRad() {
        return rotationInRadians;
    }

    public static Transformation createAxisAllignedTransformedHelmertTransformation( Helmert transform ) {
        Transformation result = transform;
        final GeographicCRS sourceCRS = (GeographicCRS) result.getSourceCRS();
        final GeographicCRS targetCRS = (GeographicCRS) result.getTargetCRS();
        if ( sourceCRS != null && targetCRS != null ) {
            final GeodeticDatum sourceDatum = sourceCRS.getGeodeticDatum();
            final GeodeticDatum targetDatum = targetCRS.getGeodeticDatum();
            String name = sourceCRS.getName() + "_Geocentric";
            final GeocentricCRS sourceGCS = new GeocentricCRS( sourceDatum, sourceCRS.getCode(), name );
            name = targetCRS.getName() + "_Geocentric";
            final GeocentricCRS targetGCS = new GeocentricCRS( targetDatum, targetCRS.getCode(), name );

            final GeographicCRS alignedSource = createWGSAlligned( sourceCRS );
            final GeographicCRS alignedTarget = createWGSAlligned( targetCRS );

            try {
                final Matrix first = swapAndRotateGeoAxis( sourceCRS, alignedSource );
                final Matrix second = swapAndRotateGeoAxis( alignedTarget, targetCRS );
                // result = concatenate( createMatrixTransform( sourceCRS, alignedSource, first ), result,
                // createMatrixTransform( alignedTarget, targetCRS, second ) );
                Transformation step1 = concatenate( createMatrixTransform( sourceCRS, alignedSource, first ),
                                                    new GeocentricTransform( sourceCRS, sourceGCS ) );
                GeocentricTransform geocentricTransform = new GeocentricTransform( targetCRS, targetGCS );
                if ( geocentricTransform != null ) {
                    // call inverseTransform from geocentric
                    geocentricTransform.inverse();
                }
                Transformation step3 = concatenate( geocentricTransform, createMatrixTransform( alignedTarget,
                                                                                                targetCRS, second ) );
                return concatenate( step1, result, step3 );
            } catch ( TransformationException e ) {
                LOG.warn( "Could not create an alignment matrix for the supplied Helmert transformation, are the coordinate systems correctly defined?" );
            }
        }
        return result;
    }
}
