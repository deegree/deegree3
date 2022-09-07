package org.deegree.rendering.r2d;

import static org.junit.Assert.assertEquals;

import org.junit.Test;


public class SvgRendererCacheTest {

    @Test
    public void testCacheKeyGeneration() {
        SvgRenderer sr = new SvgRenderer();
        assertEquals( "abc_1_2", sr.createCacheKey( "abc", 1.2d, 2.0000001d ) );
        assertEquals( "abc_1_2", sr.createCacheKey( "abc", 1.0d, 2.0d ) );
        assertEquals( "abc_1_2", sr.createCacheKey( "abc", 1.499999d, 2.499999d ) );
        assertEquals( "abc_2_3", sr.createCacheKey( "abc", 1.51d, 2.51d ) );
    }
}
