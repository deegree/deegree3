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
package org.deegree.io.quadtree;

import java.util.List;

import org.deegree.model.spatialschema.Envelope;
import org.deegree.model.spatialschema.Point;

/**
 *
 *
 *
 * @version $Revision$
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version 1.0. $Revision$, $Date$
 * @param <T>
 *            the datatype to be used as id
 *
 * @since 2.0
 */
public interface Quadtree<T> {

    /**
     * inserts a new item into the quadtree
     *
     * @param itemKey
     *            key of the Item to be inserted.
     * @param envelope
     *            bbox of the item.
     * @throws IndexException
     */
    public abstract void insert( T itemKey, Envelope envelope )
                            throws IndexException;

    /**
     * inserts a new item into the quadtree
     *
     * @param itemKey
     *            key of the Item to be inserted.
     * @param point
     *            if the item is a point.
     * @throws IndexException
     */
    public abstract void insert( T itemKey, Point point )
                            throws IndexException;

    /**
     * returns a List containing all items whose envelope intersects with the passed one
     *
     * @param envelope
     * @return a List containing all items whose envelope intersects with the passed one
     * @throws IndexException
     */
    public abstract List query( Envelope envelope )
                            throws IndexException;

    /**
     * deletes a specific item from the tree (not the item itself will be deleted, just its
     * reference will be)
     *
     * @param itemKey
     *            key of item to be deleted.
     * @throws IndexException
     *             if the rootnode cannot be loaded, or the given item does not exist.
     */
    public abstract void deleteItem( T itemKey )
                            throws IndexException;

    /**
     * updates spacial index of the specified item in the quadtree.
     *
     * @param itemKey
     *            the ID of the item.
     * @param newBBox
     *            the new BBoundingbox of the item.
     * @throws IndexException
     *             if the rootnode cannot be loaded, or the given item does not exist.
     */
    public abstract void update( T itemKey, Envelope newBBox )
                            throws IndexException;

    /**
     * deletes all references of items whose envelope intersects with the passed one (
     *
     * @see #deleteItem(Object) )
     * @param envelope
     */
    public abstract void deleteRange( Envelope envelope );

    /**
     *
     * @return the maxium depth of the tree (which was configured at instantiation of the db ).
     */
    public abstract int getDepth();

    /**
     * returns the bounding box covered by the quadtrees root node
     *
     * @return the bounding box covered by the quadtrees root node
     * @throws IndexException
     */
    public abstract Envelope getRootBoundingBox()
                            throws IndexException;

}
