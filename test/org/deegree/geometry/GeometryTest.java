package org.deegree.geometry;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.deegree.geometry.Envelope;
import org.deegree.geometry.GeometryFactory;
import org.deegree.geometry.GeometryFactoryCreator;
import org.junit.Before;
import org.junit.Test;

/**
 * Some very basic tests. Just to make sure we can create a GeometryFactory and do some simple
 * operations.
 * 
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$ }
 */
public class GeometryTest {

    private static GeometryFactory geomFactory = GeometryFactoryCreator.getInstance().getGeometryFactory();

    private static double DELTA = 0.001;

    Envelope env1, env2, env3, env4, env5, env6, env7, env8, env9, env10;

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
        env6 = createEnvelope( 15, 15, 25, 25 ); // intersects env1, env3, env4, env5
        env7 = createEnvelope( 25, 34, 45, 36 ); // intersects env2, env8, env9
        env8 = createEnvelope( 25, 35, 45, 45 ); // intersects env2, env7, env9
        env9 = createEnvelope( 34, 10, 45, 45 ); // intersects env2, 3nv7, env8
        env10 = createEnvelope( 45, 10, 50, 45 ); // intersects env9

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
        assertFalse( env2.intersects( env1 ) );
        assertTrue( env1.intersects( env3 ) );
        assertTrue( env3.intersects( env1 ) );
        assertTrue( env1.intersects( env4 ) );
        assertTrue( env4.intersects( env1 ) );
        assertTrue( env1.intersects( env5 ) );
        assertTrue( env5.intersects( env1 ) );
        assertTrue( env6.intersects( env1 ) );
        assertTrue( env6.intersects( env3 ) );
        assertTrue( env6.intersects( env4 ) );
        assertTrue( env6.intersects( env5 ) );
        assertTrue( env7.intersects( env2 ) );
        assertTrue( env7.intersects( env9 ) );
        assertTrue( env8.intersects( env2 ) );
        assertTrue( env8.intersects( env7 ) );
        assertTrue( env9.intersects( env2 ) );
        assertTrue( env9.intersects( env7 ) );
        assertTrue( env9.intersects( env8 ) );

        assertFalse( env9.intersects( env5 ) );
        assertFalse( env5.intersects( env9 ) );
    }

    /**
     * 
     */
    @Test
    public void testIntersection() {
        assertTrue( env1.intersection( env4 ).equals( env4 ) );
        assertTrue( env4.intersection( env1 ).equals( env4 ) );
        assertTrue( env1.intersection( env6 ).equals( createEnvelope( 15, 15, 20, 20 ) ) );
        assertTrue( env2.intersection( env9 ).equals( createEnvelope( 34, 30, 40, 40 ) ) );
        assertTrue( env1.intersection( env1 ).equals( env1 ) );
        assertTrue( env2.intersection( env7 ).equals( createEnvelope( 30, 34, 40, 36 ) ) );
        assertTrue( env5.intersection( env2 ).equals( createEnvelope( 30, 30, 30, 30 ) ) );
        assertTrue( env10.intersection( env9 ).equals( createEnvelope( 45, 10, 45, 45 ) ) );
        assertTrue( env9.intersection( env10 ).equals( createEnvelope( 45, 10, 45, 45 ) ) );
    }
}
