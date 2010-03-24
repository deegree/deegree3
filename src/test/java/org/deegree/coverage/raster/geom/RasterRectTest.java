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

package org.deegree.coverage.raster.geom;

import junit.framework.Assert;

import org.junit.Test;

/**
 * The <code>RasterRect</code> class TODO add class documentation here.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 * @version $Revision$, $Date$
 * 
 */
public class RasterRectTest {

    @Test
    public void intersectionOverlap() {
        RasterRect first = new RasterRect( 2, 2, 2, 2 );
        RasterRect second = new RasterRect( 1, 3, 4, 5 );
        RasterRect result = RasterRect.intersection( first, second );
        Assert.assertEquals( 2, result.x );
        Assert.assertEquals( 3, result.y );
        Assert.assertEquals( 2, result.width );
        Assert.assertEquals( 1, result.height );
    }

    @Test
    public void intersectionTotal() {
        RasterRect first = new RasterRect( 2, 2, 4, 4 );
        RasterRect second = new RasterRect( 3, 3, 2, 2 );
        RasterRect result = RasterRect.intersection( first, second );
        Assert.assertEquals( 3, result.x );
        Assert.assertEquals( 3, result.y );
        Assert.assertEquals( 2, result.width );
        Assert.assertEquals( 2, result.height );
    }

    @Test
    public void intersectionTotal2() {
        RasterRect first = new RasterRect( 3, 3, 2, 2 );
        RasterRect second = new RasterRect( 2, 2, 4, 4 );
        RasterRect result = RasterRect.intersection( first, second );
        Assert.assertEquals( 3, result.x );
        Assert.assertEquals( 3, result.y );
        Assert.assertEquals( 2, result.width );
        Assert.assertEquals( 2, result.height );
    }

    @Test
    public void outsideLeft() {
        RasterRect first = new RasterRect( 2, 2, 2, 2 );
        RasterRect second = new RasterRect( 0, 3, 1, 5 );
        RasterRect result = RasterRect.intersection( first, second );

        Assert.assertNull( result );
    }

    @Test
    public void outsideRight() {
        RasterRect first = new RasterRect( 2, 2, 2, 2 );
        RasterRect second = new RasterRect( 5, 3, 1, 5 );
        RasterRect result = RasterRect.intersection( first, second );
        Assert.assertNull( result );
    }

    @Test
    public void outsideTop() {
        RasterRect first = new RasterRect( 2, 2, 2, 2 );
        RasterRect second = new RasterRect( 2, 0, 5, 2 );
        RasterRect result = RasterRect.intersection( first, second );
        Assert.assertNull( result );
    }

    @Test
    public void outsideBottom() {
        RasterRect first = new RasterRect( 2, 2, 2, 2 );
        RasterRect second = new RasterRect( 2, 5, 5, 2 );
        RasterRect result = RasterRect.intersection( first, second );
        Assert.assertNull( result );
    }

}
