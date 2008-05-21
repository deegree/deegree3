//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------
 This file is part of deegree.
 Copyright (C) 2001-2008 by:
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
package org.deegree.model.crs.transformations.helmert;

import static org.deegree.model.crs.projections.ProjectionUtils.EPS11;

import javax.vecmath.Matrix4d;

import org.deegree.model.crs.CRSIdentifiable;

/**
 * Parameters for a geographic transformation into WGS84. The Bursa Wolf parameters should be applied to geocentric
 * coordinates, where the X axis points towards the Greenwich Prime Meridian, the Y axis points East, and the Z axis
 * points North.
 * 
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * 
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
public class WGS84ConversionInfo extends CRSIdentifiable {

    private static final long serialVersionUID = 3609096054318456918L;

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

    /**
     * Construct a conversion info with all parameters set to 0;
     * 
     * @param identifier
     */
    public WGS84ConversionInfo( String identifier ) {
        this( new String[] { identifier } );
    }

    /**
     * Construct a conversion info with all parameters set to 0;
     * 
     * @param identifiers
     */
    public WGS84ConversionInfo( String[] identifiers ) {
        super( identifiers );
    }

    /**
     * Construct a conversion info with all parameters set to 0;
     * 
     * @param identifiers
     * @param names
     * @param versions
     * @param descriptions
     * @param areasOfUse
     */
    public WGS84ConversionInfo( String[] identifiers, String[] names, String[] versions, String[] descriptions,
                                String[] areasOfUse ) {
        super( identifiers, names, versions, descriptions, areasOfUse );

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
     * @param identifiers
     * @param names
     * @param versions
     * @param descriptions
     * @param areaOfUses
     */
    public WGS84ConversionInfo( double dx, double dy, double dz, double ex, double ey, double ez, double ppm,
                                String[] identifiers, String[] names, String[] versions, String[] descriptions,
                                String[] areaOfUses ) {
        super( identifiers, names, versions, descriptions, areaOfUses );
        this.dx = dx;
        this.dy = dy;
        this.dz = dz;
        this.ex = ex;
        this.ey = ey;
        this.ez = ez;
        this.ppm = ppm;
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
     * @param identifier
     * @param name
     * @param version
     * @param description
     * @param areaOfUse
     */
    public WGS84ConversionInfo( double dx, double dy, double dz, double ex, double ey, double ez, double ppm,
                                String identifier, String name, String version, String description, String areaOfUse ) {
        this( dx, dy, dz, ex, ey, ez, ppm, new String[] { identifier }, new String[] { name },
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
     * @param identifiers
     */
    public WGS84ConversionInfo( double dx, double dy, double dz, double ex, double ey, double ez, double ppm,
                                String[] identifiers ) {
        this( dx, dy, dz, ex, ey, ez, ppm, identifiers, null, null, null, null );
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
     * @param identifier
     */
    public WGS84ConversionInfo( double dx, double dy, double dz, double ex, double ey, double ez, double ppm,
                                String identifier ) {
        this( dx, dy, dz, ex, ey, ez, ppm, new String[] { identifier } );
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
     * @param identifiable
     *            object containing all relevant id.
     */
    public WGS84ConversionInfo( double dx, double dy, double dz, double ex, double ey, double ez, double ppm,
                                CRSIdentifiable identifiable ) {
        super( identifiable );
        this.dx = dx;
        this.dy = dy;
        this.dz = dz;
        this.ex = ex;
        this.ey = ey;
        this.ez = ez;
        this.ppm = ppm;
    }

    /**
     * Returns an affine transformation also known as the "Helmert" transformation. The matrix representation of this
     * transformation (also known as "Bursa Wolf" formula) is as follows:
     * 
     * <blockquote>
     * 
     * <pre>
     *       S = 1 + {@link #ppm}/1000000
     *      
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
        // Note: (ex, ey, ez) is a rotation in arc seconds. We need to convert it into radians (the
        // R factor in RS).
        final double S = 1 + ( ppm * 1E-6 );
        final double RS = ( Math.PI / ( 180. * 3600. ) ) * S;
        return new Matrix4d( S, -ez * RS, +ey * RS, dx, +ez * RS, S, -ex * RS, dy, -ey * RS, +ex * RS, S, dz, 0, 0, 0,
                             1. );
    }

    /**
     * @return true if any of the helmert parameters were set.
     */
    public boolean hasValues() {
        return !( ex == 0 && ey == 0 && ez == 0 && dx == 0 && dy == 0 && dz == 0 && ppm == 0 );
    }

    @Override
    public boolean equals( final Object other ) {
        if ( other != null && other instanceof WGS84ConversionInfo ) {
            final WGS84ConversionInfo that = (WGS84ConversionInfo) other;
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
        final StringBuffer buffer = new StringBuffer( "[\"" );
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
     * <li>byte, char, short, int -- code = (int)f </li>
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
}
