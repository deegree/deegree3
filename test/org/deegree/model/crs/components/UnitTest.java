//$HeadURL: $
/*----------------    FILE HEADER  ------------------------------------------
 This file is part of deegree.
 Copyright (C) 2001-2008 by:
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

package org.deegree.model.crs.components;

import static org.deegree.model.crs.components.Unit.ARC_SEC;
import static org.deegree.model.crs.components.Unit.BRITISHYARD;
import static org.deegree.model.crs.components.Unit.DEGREE;
import static org.deegree.model.crs.components.Unit.METRE;
import static org.deegree.model.crs.components.Unit.MILLISECOND;
import static org.deegree.model.crs.components.Unit.RADIAN;
import static org.deegree.model.crs.components.Unit.SECOND;
import static org.deegree.model.crs.components.Unit.USFOOT;
import static org.deegree.model.crs.projections.ProjectionUtils.DTR;
import junit.framework.TestCase;

import org.junit.Test;

/**
 * <code>UnitTest</code> tests the conversion of different units into each other.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * 
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
public class UnitTest extends TestCase {

    /**
     * Tests the conversion of known units and the conversion of incompatible units.
     */
    @Test
    public void testUnitConversion() {
        assertEquals( 1.0, RADIAN.getScale() );
        assertEquals( DTR, DEGREE.getScale() );
        assertTrue( DEGREE.canConvert( RADIAN ) );
        assertTrue( RADIAN.canConvert( DEGREE ) );
        assertTrue( RADIAN.canConvert( Unit.ARC_SEC ) );
        assertTrue( DEGREE.canConvert( ARC_SEC ) );
        assertTrue( !DEGREE.canConvert( METRE ) );
        assertTrue( !METRE.canConvert( DEGREE ) );
        assertTrue( METRE.canConvert( BRITISHYARD ) );
        assertTrue( USFOOT.canConvert( BRITISHYARD ) );
        assertTrue( BRITISHYARD.canConvert( METRE ) );
        double test = 6.8;
        assertEquals( Math.toRadians( test ), DEGREE.convert( test, RADIAN ) );
        assertEquals( Math.toDegrees( test ), RADIAN.convert( test, DEGREE ) );
        assertEquals( test * 1000, SECOND.convert( test, MILLISECOND ) );
        assertEquals( test / 0.3048006096012192, METRE.convert( test, USFOOT ) );

    }
}
