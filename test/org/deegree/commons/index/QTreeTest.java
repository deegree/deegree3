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
import org.deegree.geometry.Envelope;
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

    private static final Envelope env = new org.deegree.geometry.standard.DefaultEnvelope( geomFac.createPoint( null,
                                                                                                                -5, -5,
                                                                                                                null ),
                                                                                           geomFac.createPoint( null,
                                                                                                                5, 5,
                                                                                                                null ) );

    /**
     * <code>
     *   2 | 3
     *   ------
     *   0 | 1
     *   </code>
     */

    private final static Envelope[] TEST = new Envelope[] {
    // id1: area: 0
                                                           geomFac.createEnvelope( -3, -2, -1, -1, null ),

                                                           // id2: aerea:1
                                                           geomFac.createEnvelope( 4.5f, -3.5f, 4.8f, -2.2f, null ),

                                                           // id3: area 3
                                                           geomFac.createEnvelope( 1.5f, 1, 2.5f, 2, null ),

                                                           // id4: area 2
                                                           geomFac.createEnvelope( -4.5f, 3, -2.8f, 5, null ),

                                                           // id5: area: 3 with overlap
                                                           geomFac.createEnvelope( 1.5f, 1.5f, 6, 6, null ),

                                                           // id6: area: 1-3
                                                           geomFac.createEnvelope( 2, -4.8f, 4, 1.5f, null ),

                                                           // id7, area 0-2
                                                           geomFac.createEnvelope( -2, -2, -1.5f, 5, null ),

                                                           // id8, area: 2-3 (small stripe)
                                                           geomFac.createEnvelope( -3.5f, 1.8f, 4.3f, 1.9f, null ),

                                                           // id9, area: 0-1 (large)
                                                           geomFac.createEnvelope( -4.8f, -4.8f, 3.7f, -0.2f, null ),

                                                           // id10, area: whole
                                                           geomFac.createEnvelope( -4.9f, -4.9f, 4.9f, 4.9f, null ),

                                                           // id11, area:repeated 3/ur
                                                           geomFac.createEnvelope( 2.6f, 2.6f, 4.9f, 4.9f, null ),

                                                           // id12, area: repeated 3/ur
                                                           geomFac.createEnvelope( 2.6f, 2.6f, 4.9f, 4.9f, null ),

                                                           // id13, area: repeated 3/ur
                                                           geomFac.createEnvelope( 2.6f, 2.6f, 4.9f, 4.9f, null ),

                                                           // id14, area: repeated 3/ur
                                                           geomFac.createEnvelope( 2.6f, 2.6f, 4.9f, 4.9f, null ),

                                                           // id15, area: one more at the top-right of 3
                                                           geomFac.createEnvelope( 2.9f, 2.9f, 4.3f, 3.9f, null ),

                                                           // id16, area: and even one more at UR 3
                                                           geomFac.createEnvelope( 3.1f, 3.5f, 4.1f, 4.3f, null )

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
        FileWriter fw = new FileWriter( new File( "/tmp/out_" + i + ".dot" ) );
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
        List<Integer> objects = qTree.query( geomFac.createEnvelope( -4, .5, -3, .9, null ) );
        Assert.assertEquals( 1, objects.size() );
        Collections.sort( objects );
        Assert.assertEquals( 10, (int) objects.get( 0 ) );

        // test for difficult intersects with 7
        objects = qTree.query( geomFac.createEnvelope( -2.5, 4.91, -1, 4.99, null ) );
        Assert.assertEquals( 1, objects.size() );
        Assert.assertEquals( 7, (int) objects.get( 0 ) );

        // test for total
        objects = qTree.query( geomFac.createEnvelope( -7, -7, 6.1, 6.1, null ) );
        Assert.assertEquals( TEST.length, objects.size() );
        Collections.sort( objects );
        for ( int i = 0; i < objects.size(); ++i ) {
            Assert.assertEquals( i + 1, (int) objects.get( i ) );
        }

        // test for very small intersection on cross from 3, 6, 5 and because of it's size 10
        objects = qTree.query( geomFac.createEnvelope( 2.49999, 1.49999, 2.50001, 1.50001, null ) );
        Assert.assertEquals( 4, objects.size() );
        Collections.sort( objects );
        Assert.assertEquals( 3, (int) objects.get( 0 ) );
        Assert.assertEquals( 5, (int) objects.get( 1 ) );
        Assert.assertEquals( 6, (int) objects.get( 2 ) );
        Assert.assertEquals( 10, (int) objects.get( 3 ) );

        // test for the same envelope as e6
        objects = qTree.query( geomFac.createEnvelope( 2, -4.8, 4, 1.5, null ) );
        Assert.assertEquals( 5, objects.size() );
        Collections.sort( objects );
        Assert.assertEquals( 3, (int) objects.get( 0 ) );
        Assert.assertEquals( 5, (int) objects.get( 1 ) );
        Assert.assertEquals( 6, (int) objects.get( 2 ) );
        Assert.assertEquals( 9, (int) objects.get( 3 ) );
        Assert.assertEquals( 10, (int) objects.get( 4 ) );

        // test for the equal envelopes 11,12,13,14 and of course 5, 10
        objects = qTree.query( geomFac.createEnvelope( 4.5, 4.5, 4.7, 4.7, null ) );
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
