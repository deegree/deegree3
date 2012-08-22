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

import org.deegree.model.crs.CoordinateSystem;

/**
 * a boundingbox as child of a Polygon isn't part of the iso19107 spec but it simplifies the geometry handling
 *
 *
 * @version $Revision$
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version 1.0. $Revision$, $Date$
 *
 * @since 2.0
 */
public interface Envelope {

    /**
     * returns the spatial reference system of a geometry
     *
     * @return the spatial reference system of a geometry
     */
    CoordinateSystem getCoordinateSystem();

    /**
     * returns the width of bounding box
     *
     * @return the width of bounding box
     */
    double getWidth();

    /**
     * returns the height of bounding box
     *
     * @return the height of bounding box
     */
    double getHeight();

    /**
     * returns the minimum coordinates of bounding box
     *
     * @return the minimum coordinates of bounding box
     */
    Position getMin();

    /**
     * returns the maximum coordinates of bounding box
     *
     * @return the maximum coordinates of bounding box
     */
    Position getMax();

    /**
     * returns true if the bounding box contains the submitted position
     *
     * @param position
     *            the position to find
     * @return true if the bounding box contains the submitted position
     */
    boolean contains( Position position );

    /**
     * returns true if this envelope intersects the submitted envelope
     *
     * @param bb
     *            another Envelope
     * @return true if this envelope intersects the submitted envelope
     */
    boolean intersects( Envelope bb );

    /**
     * returns true if all positions of the submitted bounding box are within this bounding box
     *
     * @param bb
     *            another boundingbox
     * @return true if all positions of the submitted bounding box are within this bounding box
     */
    boolean contains( Envelope bb );

    /**
     * returns a new Envelope object representing the intersection of this Envelope with the specified Envelope.
     *
     * @param bb
     *            another boundingbox
     * @return a new Envelope object representing the intersection of this Envelope with the specified Envelope.
     */
    Envelope createIntersection( Envelope bb );

    /**
     * merges two Envelops and returns the minimum envelope containing both.
     *
     * @param envelope
     *            another envelope to merge with this one
     *
     * @return the minimum envelope containing both.
     * @throws GeometryException
     *             if the coordinatesystems are not equal
     */
    Envelope merge( Envelope envelope )
                            throws GeometryException;

    /**
     * creates a new envelope
     *
     * @param b
     *            an extra bound around the Envelope
     * @return a new Envelope
     */
    Envelope getBuffer( double b );

    /**
     * ensures that the passed Envepole is contained within this.Envelope
     *
     * @param other
     *            to expand this Envelope.
     */
    void expandToContain( Envelope other );

    /**
     * translate a envelope in the direction defined by the two passed values and retiurns the resulting envelope
     *
     * @param x
     *            coordinate
     * @param y
     *            coordinate
     * @return the resulting translated Envelope
     */
    Envelope translate( double x, double y );

    /**
     * returns the centroid of an Envelope
     *
     * @return centroid of an Envelope
     */
    Point getCentroid();

}
