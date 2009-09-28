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
    RasterRendererPanel panel = new RasterRendererPanel();
    private int i;
    private int j;
    private double x;

    @Test
    public void testThisPanel()
    {
        for (i = 1 ; i <= 30000; i ++)
            for (j = 1 ; j <= 30000; j ++)
                x = (9999999.04-i) * (12.55555555+j);
        assert panel.isShowing() == true;
    }

    @Before
    public void setUp()
    {
        panel.init();
        panel.setSize(700, 700);
        panel.setVisible(true);
    }

    @After
    public void tearDown()
    {
        panel.setVisible(false);
    }

}