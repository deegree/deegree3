package org.deegree.model.geometry;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.deegree.model.geometry.primitive.Envelope;
import org.junit.Before;
import org.junit.Test;

/**
 * Some very basic tests. Just to make sure we can create a GeometryFactory and do some simple operations.
 * 
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author: $
 * 
 * @version $Revision: $, $Date: $ }
 */
public class GeometryTest {

    private static GeometryFactory geomFactory = GeometryFactoryCreator.getInstance().getGeometryFactory();

    private static double DELTA = 0.001;

    Envelope env1, env2, env3, env4, env5;

    /**
     * common envelopes as test geometry
     */
    @Before
    public void setUp() {
        env1 = createEnvelope( 10, 10, 20, 20 );
        env2 = createEnvelope( 30, 30, 40, 40 ); // disjoint
        env3 = createEnvelope( 15, 15, 20, 20 ); // covers env1
        env4 = createEnvelope( 12, 12, 18, 18 ); // inside env1
        env5 = createEnvelope( 20, 20, 30, 30 ); // touch env1

    }

    private Envelope createEnvelope( int x1, int y1, int x2, int y2 ) {
        return geomFactory.createEnvelope( new double[] { x1, y1 }, new double[] { x2, y2 }, DELTA, null );
    }

    /**
     * 
     */
    @Test
    public void testContains() {
        assertTrue( env1.contains( env1 ) );
        assertTrue( env1.contains( env4 ) );
        assertTrue( env1.contains( env3 ) );
        assertFalse( env4.contains( env1 ) );
        assertFalse( env3.contains( env1 ) );
        assertFalse( env1.contains( env2 ) );
        assertFalse( env1.contains( env5 ) );
    }

    /**
     * 
     */
    @Test
    public void testIntersects() {
        assertTrue( env1.intersects( env1 ) );
        assertFalse( env1.intersects( env2 ) );
        assertTrue( env1.intersects( env3 ) );
        assertTrue( env1.intersects( env4 ) );
        assertTrue( env1.intersects( env5 ) );
    }
}
