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

/**
 * TODO add documentation here
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth </a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
interface Node<T> {

    /**
     * @return the id of the Node
     */
    public String getId();

    /**
     * inserts a new item into the quadtree
     *
     * @param item
     *            (or it's id) which shall be inserted into the quadtree.
     * @param itemEnv
     *            the bbox of the item
     * @return true if the insertion occurred false otherwise.
     * @throws Exception
     *             if an error occurred while inserting the specified node.
     */
    public boolean insert( T item, Envelope itemEnv )
                            throws Exception;

    /**
     * returns a List containing all items whose envelope intersects with the passed one
     *
     * @param searchEnv
     * @param visitor
     * @param level
     *
     * @return a List containing all items whose envelope intersects with the passed one
     * @throws Exception
     *             if an error occurred while acquiring all nodes.
     */
    public List<T> query( Envelope searchEnv, List<T> visitor, int level )
                            throws Exception;

    /**
     * deletes a specific item from the tree (not the item itself will be deleted, just its
     * reference will be.
     *
     * @param item
     *            (or it's ide) to be deleted
     * @param itemsEnvelope
     *            bbox of the item
     * @return true if the deletion occurred false otherwise.
     * @throws Exception
     *             if an error occurred while deleting the specified node.
     */
    public boolean delete( T item, Envelope itemsEnvelope )
                            throws Exception;

    /**
     * Updates the spatial reference of the given item.
     *
     * @param item
     *            which spatial reference in the quadtree should be updated.
     * @param newBBox
     *            newBBox the new BBoundingbox of the item.
     * @return true if the update occurred, false otherwise
     */
    public boolean update( T item, Envelope newBBox );

    /**
     * deletes all references of items whose envelope intersects with the passed one (
     *
     * @see #delete )
     * @param envelope
     */
    public void deleteRange( Envelope envelope );

}
