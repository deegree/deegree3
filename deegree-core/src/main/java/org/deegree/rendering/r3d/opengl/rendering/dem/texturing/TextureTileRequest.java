//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/base/trunk/resources/eclipse/files_template.xml $
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

package org.deegree.rendering.r3d.opengl.rendering.dem.texturing;

import static java.lang.Double.doubleToLongBits;
import static java.lang.Math.round;

/**
 * Represents the request for a {@link TextureTile}.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: schneider $
 * 
 * @version $Revision: $, $Date: $
 */
public class TextureTileRequest {

    private static final double HASH_CODE_FLOOR = 1000000;

    private static final double EPS = 1E-6;

    private double minX;

    private double minY;

    private double maxX;

    private double maxY;

    private float unitsPerPixel;

    /**
     * @param request
     *            to copy the values from.
     */
    public TextureTileRequest( TextureRequest request ) {
        this( request.getMinX(), request.getMinY(), request.getMaxX(), request.getMaxY(), request.getUnitsPerPixel() );
    }

    /**
     * @param minX
     * @param minY
     * @param maxX
     * @param maxY
     * @param metersPerPixel
     */
    public TextureTileRequest( double minX, double minY, double maxX, double maxY, float metersPerPixel ) {
        this.minX = round( minX * HASH_CODE_FLOOR ) / HASH_CODE_FLOOR;
        this.minY = round( minY * HASH_CODE_FLOOR ) / HASH_CODE_FLOOR;
        this.maxX = round( maxX * HASH_CODE_FLOOR ) / HASH_CODE_FLOOR;
        this.maxY = round( maxY * HASH_CODE_FLOOR ) / HASH_CODE_FLOOR;
        this.unitsPerPixel = (float) ( round( metersPerPixel * HASH_CODE_FLOOR ) / HASH_CODE_FLOOR );
    }

    /**
     * 
     * @param that
     * @return true if two request share a border.
     */
    public boolean shareCorner( TextureTileRequest that ) {
        return this.shareCornerNE( that ) || this.shareCornerNW( that ) || this.shareCornerSE( that )
               || this.shareCornerSW( that );
    }

    /**
     * 
     * @param that
     * @return true if the two request share the North-West corner.
     */
    public boolean shareCornerNW( TextureTileRequest that ) {
        return ( Math.abs( this.minX - that.minX ) < EPS ) && ( Math.abs( this.maxY - that.maxY ) < EPS );
    }

    /**
     * 
     * @param that
     * @return true if the two request share the North-East corner.
     */
    public boolean shareCornerNE( TextureTileRequest that ) {
        return ( Math.abs( this.maxX - that.maxX ) < EPS ) && ( Math.abs( this.maxY - that.maxY ) < EPS );
    }

    /**
     * 
     * @param that
     * @return true if the two request share the South-West corner.
     */
    public boolean shareCornerSW( TextureTileRequest that ) {
        // return this.minX == that.minX && this.minY == that.minY;
        return ( Math.abs( this.minX - that.minX ) < EPS ) && ( Math.abs( this.minY - that.minY ) < EPS );
    }

    /**
     * 
     * @param that
     * @return true if the two request share the South-East corner.
     */
    public boolean shareCornerSE( TextureTileRequest that ) {
        // return this.maxX == that.maxX && this.minY == that.minY;
        return ( Math.abs( this.maxX - that.maxX ) < EPS ) && ( Math.abs( this.minY - that.minY ) < EPS );
    }

    /**
     * Merge two requests.
     * 
     * @param otherRequest
     */
    public void merge( TextureTileRequest otherRequest ) {
        if ( this.unitsPerPixel > otherRequest.unitsPerPixel ) {
            this.unitsPerPixel = otherRequest.unitsPerPixel;
        }
        if ( this.minX > otherRequest.minX ) {
            this.minX = otherRequest.minX;
        }
        if ( this.maxX < otherRequest.maxX ) {
            this.maxX = otherRequest.maxX;
        }
        if ( this.minY > otherRequest.minY ) {
            this.minY = otherRequest.minY;
        }
        if ( this.maxY < otherRequest.maxY ) {
            this.maxY = otherRequest.maxY;
        }
    }

    /**
     * @return min x position
     */
    public double getMinX() {
        return minX;
    }

    /**
     * @return min y position
     */
    public double getMinY() {
        return minY;
    }

    /**
     * @return max x position
     */
    public double getMaxX() {
        return maxX;
    }

    /**
     * @return max y position
     */
    public double getMaxY() {
        return maxY;
    }

    /**
     * @return number of meters per pixel (a scale).
     */
    public float getUnitsPerPixel() {
        return unitsPerPixel;
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
        long tmp = doubleToLongBits( this.minX );
        code = code * 37 + (int) ( tmp ^ ( tmp >>> 32 ) );
        tmp = doubleToLongBits( this.minY );
        code = code * 37 + (int) ( tmp ^ ( tmp >>> 32 ) );
        tmp = Double.doubleToLongBits( this.maxX );
        code = code * 37 + (int) ( tmp ^ ( tmp >>> 32 ) );
        tmp = Double.doubleToLongBits( this.maxY );
        code = code * 37 + (int) ( tmp ^ ( tmp >>> 32 ) );
        tmp = Double.doubleToLongBits( unitsPerPixel );
        code = code * 37 + (int) ( tmp ^ ( tmp >>> 32 ) );
        return (int) ( code >>> 32 ) ^ (int) code;
    }

    @Override
    public boolean equals( Object o ) {
        if ( !( o instanceof TextureTileRequest ) ) {
            return false;
        }
        TextureTileRequest that = (TextureTileRequest) o;
        return Math.abs( this.minX - that.minX ) < EPS && Math.abs( this.maxX - that.maxX ) < EPS
               && Math.abs( this.minY - that.minY ) < EPS && Math.abs( this.maxY - that.maxY ) < EPS
               && ( this.unitsPerPixel - that.unitsPerPixel ) < EPS;

    }

    @Override
    public String toString() {
        return "(" + minX + "," + minY + "," + maxX + "," + maxY + "), meter/pixel: " + unitsPerPixel;
    }

    /**
     * @param candidate
     * @return true if this request covers the total area of the given 'candiate'.
     */
    public boolean isFullfilled( TextureTile candidate ) {
        return ( this.minX - candidate.getMinX() ) >= ( -EPS ) && ( this.minY - candidate.getMinY() ) >= ( -EPS )
               && ( this.maxX - candidate.getMaxX() ) <= ( EPS ) && ( this.maxY - candidate.getMaxY() ) <= ( EPS )
               && ( this.unitsPerPixel - candidate.getMetersPerPixel() ) >= ( -EPS );
    }

    /**
     * Returns whether this {@link TextureRequest} supersedes another {@link TextureRequest}.
     * <p>
     * This is true iff:
     * <ul>
     * <li>the bbox of the other request lies completely inside the bbox of this request (or coincides with it)</li>
     * <li>the meters per pixel of the other request is less than or equal to the meters per pixel of this request</li>
     * </ul>
     * </p>
     * 
     * @param that
     * @return true, if this request supersedes the given request, false otherwise
     */
    public boolean supersedes( TextureTileRequest that ) {
        return this.minX <= that.minX && this.minY <= that.minY && this.maxX >= that.maxX && this.maxY >= that.maxY
               && this.unitsPerPixel <= that.unitsPerPixel/* Math.abs( this.unitsPerPixel - that.unitsPerPixel ) <= EPS */;
    }

    /**
     * 
     * @param candidate
     *            which values are taken into consideration.
     * @param epsilon
     *            is used to determine the tolerance of the scale factor.
     * @return true if this request lies within the area of the given 'candiate' and the the resolution of this is
     *         larger or withing epsilon range of the given candidate.
     */
    public boolean isFullfilled( TextureTileRequest candidate, double epsilon ) {
        return this.minX >= candidate.minX
               && this.minY >= candidate.minY
               && this.maxX <= candidate.maxX
               && this.maxY <= candidate.maxY
               && ( ( this.unitsPerPixel >= candidate.unitsPerPixel ) || ( Math.abs( this.unitsPerPixel
                                                                                     - candidate.unitsPerPixel ) < epsilon ) );
    }

}
