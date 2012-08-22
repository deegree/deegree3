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

import javax.vecmath.Point3d;

/**
 * A sequence of decimals numbers which when written on a width are a sequence of coordinate positions. The width is
 * derived from the CRS or coordinate dimension of the container.
 *
 * <p>
 * -----------------------------------------------------------------------
 * </p>
 *
 * @version 5.6.2001
 * @author Andreas Poth
 *         <p>
 */
public interface Position {
    /**
     * @return the x-value of the point
     */
    public double getX();

    /**
     * @return the y-value of the point
     */
    public double getY();

    /**
     * @return the z-value of the point
     */
    public double getZ();

    /**
     * @return the coordinate dimension of the position
     */
    public int getCoordinateDimension();

    /**
     * NOTE: The returned array always has a length of 3, regardless of the dimension. This is due to a limitation in
     * the coordinate transformation package (proj4), which expects coordinates to have 3 dimensions.
     *
     * @return the x- and y-value of the point as a two dimensional array the first field contains the x- the second
     *         field the y-value.
     *
     */
    public double[] getAsArray();

    /**
     * translates the coordinates of the position.
     *
     * @param d
     *            the first coordinate of the position will be translated by the first field of <tt>d</tt> the second
     *            coordinate by the second field of <tt>d</tt> and so on...
     */
    public void translate( double[] d );

    /**
     * returns the accuracy the position is defined. The accuracy is measured in values of the CRS the positions
     * coordinates are stored
     *
     * @return the accuracy the position is defined
     */
    public double getAccuracy();

    /**
     * @see #getAccuracy()
     * @param accuracy
     */
    public void setAccuracy( double accuracy );

    /**
     * @return the position as a point3d
     */
    public Point3d getAsPoint3d();

}
