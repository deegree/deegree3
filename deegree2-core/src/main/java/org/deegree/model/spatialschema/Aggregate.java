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

import java.util.Iterator;

/**
 *
 * This interface defines the basis functionallity of all geometry aggregations. it will be specialized for the use of
 * primitive, and solid geometries.
 *
 * <p>
 * -----------------------------------------------------
 * </p>
 *
 * @author Andreas Poth
 * @version $Revision$ $Date$
 *          <p>
 */

public interface Aggregate extends Geometry {

    /**
     * @return the number of Geometry within the aggregation
     */
    int getSize();

    /**
     * merges two aggregation.
     *
     * @param aggregate
     *
     * @exception GeometryException
     *                a GeometryException will be thrown if the submitted isn't the same type as the recieving one.
     */
    void merge( Aggregate aggregate )
                            throws GeometryException;

    /**
     * adds an Geometry to the aggregation
     *
     * @param gmo
     *            Geometry to the aggregation
     */
    void add( Geometry gmo );

    /**
     * inserts a Geometry in the aggregation. all elements with an index equal or larger index will be moved. if index
     * is larger then getSize() - 1 an exception will be thrown.
     *
     * @param gmo
     *            Geometry to insert.
     * @param index
     *            position where to insert the new Geometry
     * @throws GeometryException
     */
    void insertObjectAt( Geometry gmo, int index )
                            throws GeometryException;

    /**
     * sets the submitted Geometry at the submitted index. the element at the position <code>index</code> will be
     * removed. if index is larger then getSize() - 1 an exception will be thrown.
     *
     * @param gmo
     *            Geometry to set.
     * @param index
     *            position where to set the new Geometry
     * @throws GeometryException
     */
    void setObjectAt( Geometry gmo, int index )
                            throws GeometryException;

    /**
     * removes the submitted Geometry from the aggregation
     *
     * @param gmo
     *            to remove
     *
     * @return the removed Geometry
     */
    Geometry removeObject( Geometry gmo );

    /**
     * removes the Geometry at the submitted index from the aggregation. if index is larger then getSize() - 1 an
     * exception will be thrown.
     *
     * @param index
     *
     * @return the removed Geometry
     * @throws GeometryException
     */
    Geometry removeObjectAt( int index )
                            throws GeometryException;

    /**
     * removes all Geometry from the aggregation.
     */
    void removeAll();

    /**
     * @param index
     *            to get
     * @return the Geometry at the submitted index.
     */
    Geometry getObjectAt( int index );

    /**
     * @return all Geometries as array
     */
    Geometry[] getAll();

    /**
     * @param gmo
     * @return true if the submitted Geometry is within the aggregation
     */
    boolean isMember( Geometry gmo );

    /**
     * @return the aggregation as an iterator
     */
    Iterator<Geometry> getIterator();

}
