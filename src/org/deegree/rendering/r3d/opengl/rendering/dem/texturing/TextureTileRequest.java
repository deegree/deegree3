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

/**
 * Represents the request for a {@link TextureTile}.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: schneider $
 *
 * @version $Revision: $, $Date: $
 */
public class TextureTileRequest {

    private float minX;

    private float minY;

    private float maxX;

    private float maxY;

    private float metersPerPixel;

    /**
     * @param textureRequest
     */
    public TextureTileRequest( TextureRequest textureRequest ) {
        this.minX = textureRequest.getMinX();
        this.minY = textureRequest.getMinY();
        this.maxX = textureRequest.getMaxX();
        this.maxY = textureRequest.getMaxY();
        this.metersPerPixel = textureRequest.getMetersPerPixel();
    }

    /**
     * Returns whether this {@link TextureTileRequest} supersedes another {@link TextureTileRequest}.
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
               && this.metersPerPixel == that.metersPerPixel;
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
    public boolean supersedes( TextureRequest that ) {
        return this.minX <= that.minX && this.minY <= that.minY && this.maxX >= that.maxX && this.maxY >= that.maxY
               && this.metersPerPixel == that.metersPerPixel;
    }

    /**
     * Merge two requests.
     *
     * @param otherRequest
     */
    public void merge( TextureTileRequest otherRequest ) {
        if ( this.metersPerPixel > otherRequest.metersPerPixel ) {
            this.metersPerPixel = otherRequest.metersPerPixel;
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
    public float getMinX() {
        return minX;
    }

    /**
     * @return min y position
     */
    public float getMinY() {
        return minY;
    }

    /**
     * @return max x position
     */
    public float getMaxX() {
        return maxX;
    }

    /**
     * @return max y position
     */
    public float getMaxY() {
        return maxY;
    }

    /**
     * @return number of meters per pixel (a scale).
     */
    public float getMetersPerPixel() {
        return metersPerPixel;
    }

    @Override
    public int hashCode() {
        return (int) minX + (int) minY + (int) maxX + (int) maxY;
    }

    /**
     *
     * @param that
     * @return true if two request share a corner.
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
        return this.minX == that.minX && this.maxY == that.maxY;
    }

    /**
     *
     * @param that
     * @return true if the two request share the North-East corner.
     */
    public boolean shareCornerNE( TextureTileRequest that ) {
        return this.maxX == that.maxX && this.maxY == that.maxY;
    }

    /**
     *
     * @param that
     * @return true if the two request share the South-West corner.
     */
    public boolean shareCornerSW( TextureTileRequest that ) {
        return this.minX == that.minX && this.minY == that.minY;
    }

    /**
     *
     * @param that
     * @return true if the two request share the South-East corner.
     */
    public boolean shareCornerSE( TextureTileRequest that ) {
        return this.maxX == that.maxX && this.minY == that.minY;
    }

    @Override
    public boolean equals( Object o ) {
        if ( !( o instanceof TextureTileRequest ) ) {
            return false;
        }
        TextureTileRequest that = (TextureTileRequest) o;
        return this.minX == that.minX && this.maxX == that.maxX && this.minY == that.minY && this.maxY == that.maxY
               && ( this.metersPerPixel - that.metersPerPixel ) < 0.001f;

    }

    @Override
    public String toString() {
        return "(" + minX + "," + minY + "," + maxX + "," + maxY + "), meter/pixel: " + metersPerPixel;
    }

    /**
     * @param candidate
     * @return true if this request covers the total area of the given 'candiate'.
     */
    public boolean isFullfilled( TextureTile candidate ) {
        return this.minX >= candidate.getMinX() && this.minY >= candidate.getMinY() && this.maxX <= candidate.getMaxX()
               && this.maxY <= candidate.getMaxY() && this.metersPerPixel >= candidate.getMetersPerPixel();
    }
}
