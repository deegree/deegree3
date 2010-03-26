//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------
 This file is part of deegree.
 Copyright (C) 2001-2009 by:
 Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 Lesser General Public License for more details.
 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 Contact:

 Andreas Poth
 lat/lon GmbH
 Aennchenstr. 19
 53177 Bonn
 Germany
 E-Mail: poth@lat-lon.de

 Prof. Dr. Klaus Greve
 Department of Geography
 University of Bonn
 Meckenheimer Allee 166
 53115 Bonn
 Germany
 E-Mail: greve@giub.uni-bonn.de
 ---------------------------------------------------------------------------*/

package org.deegree.commons.index;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import junit.framework.Assert;

import org.deegree.commons.utils.GraphvizDot;
import org.deegree.geometry.GeometryFactory;
import org.junit.Test;

/**
 * The <code>QTreeTest</code> class TODO add class documentation here.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 * @version $Revision$, $Date$
 * 
 */
public class QTreeTest {
    private final static GeometryFactory geomFac = new GeometryFactory();

    private static final float[] env = new float[] { -5, -5, 5, 5 };

    /**
     * <code>
     *   2 | 3
     *   ------
     *   0 | 1
     *   </code>
     */

    private final static float[][] TEST = new float[][] {
    // id1: area: 0
                                                         new float[] { -3, -2, -1, -1 },

                                                         // id2: aerea:1
                                                         new float[] { 4.5f, -3.5f, 4.8f, -2.2f },

                                                         // id3: area 3
                                                         new float[] { 1.5f, 1, 2.5f, 2 },

                                                         // id4: area 2
                                                         new float[] { -4.5f, 3, -2.8f, 5 },

                                                         // id5: area: 3 with overlap
                                                         new float[] { 1.5f, 1.5f, 6, 6 },

                                                         // id6: area: 1-3
                                                         new float[] { 2, -4.8f, 4, 1.5f },

                                                         // id7, area 0-2
                                                         new float[] { -2, -2, -1.5f, 5 },

                                                         // id8, area: 2-3 (small stripe)
                                                         new float[] { -3.5f, 1.8f, 4.3f, 1.9f },

                                                         // id9, area: 0-1 (large)
                                                         new float[] { -4.8f, -4.8f, 3.7f, -0.2f },

                                                         // id10, area: whole
                                                         new float[] { -4.9f, -4.9f, 4.9f, 4.9f },

                                                         // id11, area:repeated 3/ur
                                                         new float[] { 2.6f, 2.6f, 4.9f, 4.9f },

                                                         // id12, area: repeated 3/ur
                                                         new float[] { 2.6f, 2.6f, 4.9f, 4.9f },

                                                         // id13, area: repeated 3/ur
                                                         new float[] { 2.6f, 2.6f, 4.9f, 4.9f },

                                                         // id14, area: repeated 3/ur
                                                         new float[] { 2.6f, 2.6f, 4.9f, 4.9f },

                                                         // id15, area: one more at the top-right of 3
                                                         new float[] { 2.9f, 2.9f, 4.3f, 3.9f },

                                                         // id16, area: and even one more at UR 3
                                                         new float[] { 3.1f, 3.5f, 4.1f, 4.3f }

    // // id17, area: almost equals LL of 2
    // new float[] { -4f, 1f, -3.5f, 1.5f },
    //
    // // id18, area: almost equals LL of 2
    // new float[] { -4.001f, 1.001f, -3.5001f, 1.499f },
    //
    // // id19, area: almost equals LL of 2
    // new float[] { -4.002f, 1.002f, -3.5002f, 1.498f },
    //
    // // id20, area: almost equals LL of 2
    // new float[] { -4.003f, 1.003f, -3.5003f, 1.497f }
    };

    private static final boolean outputTree = false;

    private QTree<Integer> fillTree( boolean output )
                            throws IOException {
        QTree<Integer> qTree = new QTree<Integer>( env, 3 );
        for ( int i = 1; i <= TEST.length; ++i ) {
            Assert.assertTrue( qTree.insert( TEST[i - 1], i ) );
            if ( output ) {
                output( qTree, "add_" + i );
            }
        }
        return qTree;

    }

    /**
     * test if the inserts result in 16 ids
     * 
     * @throws IOException
     */
    @Test
    public void testInsert()
                            throws IOException {
        QTree<Integer> qTree = fillTree( outputTree );
        List<Integer> objects = qTree.getObjects();
        Assert.assertEquals( TEST.length, objects.size() );
    }

    private void output( QTree<Integer> tree, String i )
                            throws IOException {
        FileWriter fw = new FileWriter( new File( System.getProperty( "java.io.tmpdir" ) + File.separatorChar + "out_"
                                                  + i + ".dot" ) );
        GraphvizDot.startDiGraph( fw );
        tree.outputAsDot( fw, "", 0, -1 );
        GraphvizDot.endGraph( fw );
        fw.close();
    }

    /**
     * 
     * 
     * @throws IOException
     */
    @Test
    public void testIntersection()
                            throws IOException {
        QTree<Integer> qTree = fillTree( outputTree );
        // request 10
        List<Integer> objects = qTree.query( new float[] { -4, .5f, -3, .9f } );
        Assert.assertEquals( 1, objects.size() );
        Collections.sort( objects );
        Assert.assertEquals( 10, (int) objects.get( 0 ) );

        // test for difficult intersects with 7
        objects = qTree.query( new float[] { -2.5f, 4.91f, -1, 4.99f } );
        Assert.assertEquals( 1, objects.size() );
        Assert.assertEquals( 7, (int) objects.get( 0 ) );

        // test for total
        objects = qTree.query( new float[] { -7, -7, 6.1f, 6.1f } );
        Assert.assertEquals( TEST.length, objects.size() );
        Collections.sort( objects );
        for ( int i = 0; i < objects.size(); ++i ) {
            Assert.assertEquals( i + 1, (int) objects.get( i ) );
        }

        // test for very small intersection on cross from 3, 6, 5 and because of it's size 10
        objects = qTree.query( new float[] { 2.49999f, 1.49999f, 2.50001f, 1.50001f } );
        Assert.assertEquals( 4, objects.size() );
        Collections.sort( objects );
        Assert.assertEquals( 3, (int) objects.get( 0 ) );
        Assert.assertEquals( 5, (int) objects.get( 1 ) );
        Assert.assertEquals( 6, (int) objects.get( 2 ) );
        Assert.assertEquals( 10, (int) objects.get( 3 ) );

        // test for the same envelope as e6
        objects = qTree.query( new float[] { 2, -4.8f, 4, 1.5f } );
        Assert.assertEquals( 5, objects.size() );
        Collections.sort( objects );
        Assert.assertEquals( 3, (int) objects.get( 0 ) );
        Assert.assertEquals( 5, (int) objects.get( 1 ) );
        Assert.assertEquals( 6, (int) objects.get( 2 ) );
        Assert.assertEquals( 9, (int) objects.get( 3 ) );
        Assert.assertEquals( 10, (int) objects.get( 4 ) );

        // test for the equal envelopes 11,12,13,14 and of course 5, 10
        objects = qTree.query( new float[] { 4.5f, 4.5f, 4.7f, 4.7f } );
        Assert.assertEquals( 6, objects.size() );
        Collections.sort( objects );
        Assert.assertEquals( 5, (int) objects.get( 0 ) );
        Assert.assertEquals( 10, (int) objects.get( 1 ) );
        Assert.assertEquals( 11, (int) objects.get( 2 ) );
        Assert.assertEquals( 12, (int) objects.get( 3 ) );
        Assert.assertEquals( 13, (int) objects.get( 4 ) );
        Assert.assertEquals( 14, (int) objects.get( 5 ) );

    }

    /**
     * test if the delete results in an empty qtree
     * 
     * @throws IOException
     */
    @Test
    public void testDelete()
                            throws IOException {
        QTree<Integer> qTree = fillTree( outputTree );
        List<Integer> objects = qTree.getObjects();
        Assert.assertEquals( TEST.length, objects.size() );
        for ( int i = TEST.length; i > 0; --i ) {
            Assert.assertTrue( remove( qTree, i ) );
        }
    }

    private boolean remove( QTree<Integer> qTree, int i )
                            throws IOException {
        boolean result = qTree.remove( i );
        List<Integer> objects = qTree.getObjects();
        Assert.assertEquals( i - 1, objects.size() );
        if ( outputTree ) {
            output( qTree, "remove_" + ( i - 1 ) );
        }
        return result;
    }

}
