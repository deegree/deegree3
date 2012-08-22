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
import java.util.Hashtable;

/**
 * <p>
 * A memory based implementation of a PageFile.<br>
 * Implemented as a Hashtable with keys representing the page file numbers of the saved nodes.
 * </p>
 *
 * @author Wolfgang Baer - WBaer@gmx.de
 */
class MemoryPageFile extends PageFile implements Serializable {

    private Hashtable<Integer, Node> file = new Hashtable<Integer, Node>( 500 );

    /**
     * Constructor
     */
    protected MemoryPageFile() {
        super();
        file.clear();
    }

    /**
     * @see PageFile#readNode(int)
     */
    protected Node readNode( int pageFile )
                            throws PageFileException {
        return file.get( new Integer( pageFile ) );
    }

    /**
     * @see PageFile#writeNode(Node)
     */
    protected int writeNode( Node node )
                            throws PageFileException {

        int i = 0;
        if ( node.getPageNumber() < 0 ) {
            while ( true ) {
                if ( !file.containsKey( new Integer( i ) ) ) {
                    break;
                }
                i++;
            }
            node.setPageNumber( i );
        } else
            i = node.getPageNumber();

        file.put( new Integer( i ), node );

        return i;
    }

    /**
     * @see PageFile#deleteNode(int)
     */
    protected Node deleteNode( int pageNumber ) {
        return file.remove( new Integer( pageNumber ) );
    }

    /**
     * @see PageFile#close()
     */
    protected void close()
                            throws PageFileException {
        // nothing to do
    }
}