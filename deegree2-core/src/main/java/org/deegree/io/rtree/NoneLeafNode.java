//$HeadURL$
//----------------------------------------
// RTree implementation.
// Copyright (C) 2002-2004 Wolfgang Baer - WBaer@gmx.de
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//----------------------------------------

package org.deegree.io.rtree;

/**
 * <p>
 * Implementation of a NoneLeafNode. Inherits methods from the abstract class Node filling the
 * defined abstract methods with life.
 * </p>
 *
 * @author Wolfgang Baer - WBaer@gmx.de
 */
class NoneLeafNode extends Node {

    protected int[] childNodes;

    /**
     * Constructor.
     *
     * @param pageNumber -
     *            number of this node in page file
     * @param file -
     *            the PageFile of this node
     */
    protected NoneLeafNode( int pageNumber, PageFile file ) {
        super( pageNumber, file );
        childNodes = new int[file.getCapacity()];

        for ( int i = 0; i < file.getCapacity(); i++ )
            childNodes[i] = -1;
    }

    /**
     * @see Node#getData(int)
     */
    protected Object getData( int index ) {
        Object obj = null;

        try {
            obj = file.readNode( childNodes[index] );
        } catch ( PageFileException e ) {
            // PageFileException NoneLeafNode.getData
            e.printStackTrace();
        }

        return obj;
    }

    /**
     * @see Node#insertData(java.lang.Object, HyperBoundingBox)
     */
    protected void insertData( Object node, HyperBoundingBox box ) {
        childNodes[counter] = ( (Node) node ).getPageNumber();
        hyperBBs[counter] = box;
        unionMinBB = unionMinBB.unionBoundingBox( box );
        ( (Node) node ).parentNode = this.pageNumber;
        ( (Node) node ).place = this.counter;
        counter++;

        try {
            file.writeNode( (Node) node );
        } catch ( PageFileException e ) {
            // PageFileException NoneLeafNode.insertData - at writeNode(Node)
            e.printStackTrace();
        }
    }

    /**
     * @see Node#insertData(java.lang.Object, HyperBoundingBox)
     */
    protected void deleteData( int index ) {

        if ( this.getUsedSpace() == 1 ) {
            // only one element is a special case.
            hyperBBs[0] = HyperBoundingBox.getNullHyperBoundingBox( file.getDimension() );
            childNodes[0] = -1;
            counter--;
        } else {
            System.arraycopy( hyperBBs, index + 1, hyperBBs, index, counter - index - 1 );
            System.arraycopy( childNodes, index + 1, childNodes, index, counter - index - 1 );
            hyperBBs[counter - 1] = HyperBoundingBox.getNullHyperBoundingBox( file.getDimension() );
            childNodes[counter - 1] = -1;
            counter--;

            for ( int i = 0; i < counter; i++ ) {
                Node help = (Node) this.getData( i );
                help.place = i;

                try {
                    file.writeNode( help );
                } catch ( PageFileException e ) {
                    // "PageFileException NoneLeafNode.deleteData - at writeNode(Node)
                    e.printStackTrace();
                }
            }
        }

        updateNodeBoundingBox();
    }

    /**
     * Computes the index of the entry with least enlargement if the given HyperBoundingBox would be
     * added.
     *
     * @param box -
     *            HyperBoundingBox to be added
     * @return int - index of entry with least enlargement
     */
    protected int getLeastEnlargement( HyperBoundingBox box ) {

        double[] area = new double[counter];

        for ( int i = 0; i < counter; i++ )
            area[i] = ( hyperBBs[i].unionBoundingBox( box ) ).getArea() - hyperBBs[i].getArea();

        double min = area[0];
        int minnr = 0;

        for ( int i = 1; i < counter; i++ ) {
            if ( area[i] < min ) {
                min = area[i];
                minnr = i;
            }
        }

        return minnr;
    }

    /**
     * @see Node#clone()
     */
    protected Object clone() {
        NoneLeafNode clone = new NoneLeafNode( this.pageNumber, this.file );
        clone.counter = this.counter;
        clone.place = this.place;
        clone.unionMinBB = (HyperBoundingBox) this.unionMinBB.clone();
        clone.parentNode = this.parentNode;

        for ( int i = 0; i < file.getCapacity(); i++ )
            clone.hyperBBs[i] = (HyperBoundingBox) this.hyperBBs[i].clone();

        return clone;
    }
}