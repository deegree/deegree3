package org.deegree.rendering.r2d;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Andrei Aiordachioaie
 */

public class RasterRendererTestPanel
{
    RasterRendererApplet frame = new RasterRendererApplet();
    private int i;
    private int j;
    private double x;

    @Test
    public void testThisApplet()
    {
        for (i = 1 ; i <= 30000; i ++)
            for (j = 1 ; j <= 30000; j ++)
                x = (9999999.04-i) * (12.55555555+j);
        assert frame.isShowing() == true;
    }

    @Before
    public void setUp()
    {
        frame.init();
        frame.setSize(500, 500);
        frame.setVisible(true);
    }

    @After
    public void tearDown()
    {
        frame.setVisible(false);
    }

}