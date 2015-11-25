//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2015 by:
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
package org.deegree.gml.reference;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 */
public class DefaultGmlXlinkStrategyTest {

    private final DefaultGmlXlinkStrategy defaultGmlXlinkStrategy = new DefaultGmlXlinkStrategy();

    @Test
    public void testExportedId_WithoutVersion() {
        String gmlId = "gmlId1";

        assertThat( defaultGmlXlinkStrategy.isObjectExported( gmlId ), is( false ) );
        defaultGmlXlinkStrategy.addExportedId( gmlId );
        assertThat( defaultGmlXlinkStrategy.isObjectExported( gmlId ), is( true ) );
    }

    @Test
    public void testExportedId_WithVersion() {
        String gmlId = "gmlId2";
        int version = 10;

        assertThat( defaultGmlXlinkStrategy.isObjectExported( gmlId, version ), is( false ) );
        defaultGmlXlinkStrategy.addExportedId( gmlId, version );
        assertThat( defaultGmlXlinkStrategy.isObjectExported( gmlId, version ), is( true ) );
    }

    @Test
    public void testExportedId_Mixed() {
        String gmlId = "gmlId3";
        int version = 5;

        defaultGmlXlinkStrategy.addExportedId( gmlId, version );
        assertThat( defaultGmlXlinkStrategy.isObjectExported( gmlId, version ), is( true ) );
        assertThat( defaultGmlXlinkStrategy.isObjectExported( gmlId ), is( false ) );
        assertThat( defaultGmlXlinkStrategy.isObjectExported( null ), is( false ) );
        assertThat( defaultGmlXlinkStrategy.isObjectExported( null, version ), is( false ) );
        assertThat( defaultGmlXlinkStrategy.isObjectExported( "otherGmlId", version ), is( false ) );
        assertThat( defaultGmlXlinkStrategy.isObjectExported( gmlId, 10 ), is( false ) );
    }

}