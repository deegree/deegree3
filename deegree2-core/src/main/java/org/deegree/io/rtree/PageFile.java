//$HeadURL$
//----------------------------------------
//RTree implementation.
//Copyright (C) 2002-2004 Wolfgang Baer - WBaer@gmx.de
//
//This library is free software; you can redistribute it and/or
//modify it under the terms of the GNU Lesser General Public
//License as published by the Free Software Foundation; either
//version 2.1 of the License, or (at your option) any later version.
//
//This library is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
//Lesser General Public License for more details.
//
//You should have received a copy of the GNU Lesser General Public
//License along with this library; if not, write to the Free Software
//Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//----------------------------------------

package org.deegree.io.rtree;

import java.io.Serializable;

/**
 * <p>
 * Abstract class implementing general methods of a PageFile.
 * </p>
 *
 * @author Wolfgang Baer - WBaer@gmx.de
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
abstract class PageFile implements Serializable {

    /** dimension of saved data */
    protected int dimension;

    /** capacity of a node (= MaxLoad + 1) */
    protected int capacity;

    /** minimum load of a node */
    protected int minimum;

    /**
     * Returns the dimension of the PageFile
     *
     * @return int
     *
     */
    protected int getDimension() {
        return dimension;
    }

    /**
     * Returns the minimum load of a node
     *
     * @return int
     */
    protected int getMinimum() {
        return minimum;
    }

    /**
     * Returns the capacity of a node in the PageFile. Capacity is defined a maximum load of a node
     * plus 1 for overflow
     *
     * @return int
     */
    protected int getCapacity() {
        return capacity;
    }

    /**
     * Reads a node from the PageFile for given index
     *
     * @param pageFileNumber -
     *            index of page file number where node is saved
     * @return Node
     * @throws PageFileException
     */
    protected abstract Node readNode( int pageFileNumber )
                            throws PageFileException;

    /**
     * Writes a node into the PageFile Method tests if node has already a PageNumber, otherwise a
     * new page number is assigned and returned.
     *
     * @param node -
     *            Node to write
     * @return int - page number
     * @throws PageFileException
     */
    protected abstract int writeNode( Node node )
                            throws PageFileException;

    /**
     * Marks the node at given page number as deleted.
     *
     * @param pageFileNumber -
     *            page number
     * @return Node - deleted node
     * @throws PageFileException
     */
    protected abstract Node deleteNode( int pageFileNumber )
                            throws PageFileException;

    /**
     * Initializes the PageFile.
     *
     * @param dimension -
     *            dimension of the data
     * @param capacity -
     *            capacity of a node
     * @throws PageFileException
     */
    protected void initialize( int dimension, int capacity )
                            throws PageFileException {
        this.dimension = dimension;
        this.capacity = capacity;
        this.minimum = (int) Math.round( ( capacity - 1 ) * 0.5 );
        if ( this.minimum < 2 )
            this.minimum = 2;
    }

    /**
     * Closes the pagefile.
     *
     * @throws PageFileException
     */
    protected abstract void close()
                            throws PageFileException;

}