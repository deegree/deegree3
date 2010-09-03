//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -

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
package org.deegree.commons.index;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.deegree.commons.index.RTree.Entry;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 * 
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
public class RTreeTest {

    public int bigM = 4;

    float[] rootEnvelope = new float[] { 0, 0, 200, 200 };

    public RTree<Integer> tree = new RTree<Integer>( rootEnvelope, bigM );

    /**
     * With the sample tree from the wikipedia page http://en.wikipedia.org/wiki/File:R-tree.svg
     * 
     * @param args
     */
    @Before
    public void loadWikipediaTree() {
        float[] box = new float[] { 20, 70, 35, 85 };
        tree.insert( box, new Integer( 8 ) );
        box = new float[] { 50, 90, 65, 105 };
        tree.insert( box, new Integer( 9 ) );
        box = new float[] { 50, 75, 65, 85 };
        tree.insert( box, new Integer( 10 ) );
        box = new float[] { 85, 15, 95, 110 };
        tree.insert( box, new Integer( 11 ) );
        box = new float[] { 40, 45, 90, 65 };
        tree.insert( box, new Integer( 12 ) );
        box = new float[] { 125, 40, 135, 95 };
        tree.insert( box, new Integer( 13 ) );
        box = new float[] { 115, 60, 130, 85 };
        tree.insert( box, new Integer( 14 ) );
        box = new float[] { 0, 0, 15, 30 };
        tree.insert( box, new Integer( 15 ) );
        box = new float[] { 20, 0, 80, 40 };
        tree.insert( box, new Integer( 16 ) );
        box = new float[] { 140, 20, 180, 45 };
        tree.insert( box, new Integer( 17 ) );
        box = new float[] { 150, 5, 165, 55 };
        tree.insert( box, new Integer( 18 ) );
        box = new float[] { 155, 10, 175, 25 };
        tree.insert( box, new Integer( 19 ) );
        box = new float[] { 145, 75, 160, 105 };
        tree.insert( box, new Integer( 20 ) );
        box = new float[] { 150, 60, 170, 75 };
        tree.insert( box, new Integer( 21 ) );
        box = new float[] { 155, 85, 180, 95 };
        tree.insert( box, new Integer( 22 ) );
        printOut( tree );

        System.out.println();
        System.out.println();
    }

    public static void printOut( RTree tree ) {
        if ( tree.root == null ) {
            throw new RuntimeException( "Tree is empty. Nothing to print." );
        }

        List<Entry<Integer>[]> queue = new ArrayList<Entry<Integer>[]>();
        queue.add( tree.root );
        int index = 0;
        int firstIndexOfNextLevel = 0;
        while ( index < queue.size() ) {
            Entry<Integer>[] cur = queue.get( index );
            if ( index == firstIndexOfNextLevel ) {
                firstIndexOfNextLevel = queue.size();
                System.out.println();
            }
            for ( int i = 0; i < cur.length; i++ ) {
                if ( cur[i] != null ) {
                    if ( cur[i].next == null || cur[i].entryValue != null ) {
                        System.out.print( "| " + cur[i].entryValue + " " );
                    } else {
                        System.out.print( "| " + Arrays.toString( cur[i].bbox ) + " " );
                    }
                    if ( cur[i].next != null ) {
                        queue.add( cur[i].next );
                    }
                }
            }
            System.out.print( " ||| " );
            index++;
        }
    }

    private void myQuery( float[] box, Entry<Integer>[] entries ) {
        for ( int i = 0; i < entries.length; i++ ) {
            if ( entries[i] != null ) {
                if ( tree.intersects( box, entries[i].bbox, 2 ) ) {
                    if ( entries[i].next == null ) {
                        System.out.println( entries[i].entryValue );
                    } else {
                        myQuery( box, entries[i].next );
                    }
                }
            }
        }
    }

    @Test
    public void queryTest() {
        System.out.println();
        System.out.println( "---------------------" );
        myQuery( new float[] { 0, 0, 50, 50 }, tree.root );
    }

    @Test
    public void testRemove() {
        tree.remove( new Integer( 11 ) );
        printOut( tree );
    }
}
