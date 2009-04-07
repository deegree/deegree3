//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------

 This file is part of deegree.
 Copyright (C) 2001-2009 by:
 EXSE, Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

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
package org.deegree.commons.utils.math;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * The <code>MathUtilsTest</code> test some basic methods
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 * @version $Revision$, $Date$
 * 
 */
public class MathUtilsTest {

    /**
     * Test method for {@link org.deegree.commons.utils.math.MathUtils#previousPowerOfTwo(double)}.
     */
    @Test
    public void testNearestPowerOfTwo() {
        assertEquals( 9, MathUtils.previousPowerOfTwo( 511 ) );
        assertEquals( 9, MathUtils.previousPowerOfTwo( 512 ) );
        assertEquals( 10, MathUtils.previousPowerOfTwo( 513 ) );
    }

    /**
     * Test method for {@link org.deegree.commons.utils.math.MathUtils#nextPowerOfTwoValue(double)}.
     */
    @Test
    public void testNextPowerOfTwo() {
        assertEquals( 512, MathUtils.nextPowerOfTwoValue( 511 ) );
        assertEquals( 512, MathUtils.nextPowerOfTwoValue( 512 ) );
        assertEquals( 1024, MathUtils.nextPowerOfTwoValue( 513 ) );
    }

}
