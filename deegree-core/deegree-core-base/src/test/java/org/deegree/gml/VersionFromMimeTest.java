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

package org.deegree.gml;

import static junit.framework.Assert.assertEquals;
import static org.deegree.gml.GMLVersion.fromMimeType;

import org.junit.Test;

/**
 * The <code>VersionFromMime</code> tests the mappig of gml mime types to gml versions
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 * @version $Revision$, $Date$
 * 
 */
public class VersionFromMimeTest {

    @Test
    public void gml1_0_0() {
        assertEquals( GMLVersion.GML_2, fromMimeType( "text/xml; subtype=gml/1.0.0", null ) );
    }

    @Test
    public void gml2_0_0() {
        assertEquals( GMLVersion.GML_2, fromMimeType( "text/xml; subtype=gml/2.0.0", null ) );
    }

    @Test
    public void gml2_1_0() {
        assertEquals( GMLVersion.GML_2, fromMimeType( "text/xml; subtype=gml/2.1.0", null ) );
    }

    @Test
    public void gml2_1_2() {
        assertEquals( GMLVersion.GML_2, fromMimeType( "text/xml; subtype=gml/2.1.2", null ) );
    }

    @Test
    public void gml3_0_0() {
        assertEquals( GMLVersion.GML_30, fromMimeType( "text/xml; subtype=gml/3.0.0", null ) );
    }

    @Test
    public void gml3_0_1() {
        assertEquals( GMLVersion.GML_30, fromMimeType( "text/xml; subtype=gml/3.0.1", null ) );
    }

    @Test
    public void gml3_0_2() {
        // don't know if 3.0.2 should actually be 3.0 or 3.1
        assertEquals( GMLVersion.GML_31, fromMimeType( "text/xml; subtype=gml/3.0.2", null ) );
    }

    @Test
    public void gml3_1_0() {
        assertEquals( GMLVersion.GML_31, fromMimeType( "text/xml; subtype=gml/3.1.0", null ) );
    }

    @Test
    public void gml3_1_1() {
        assertEquals( GMLVersion.GML_31, fromMimeType( "text/xml; subtype=gml/3.1.1", null ) );
    }

    @Test
    public void gml3_2_0() {
        assertEquals( GMLVersion.GML_32, fromMimeType( "text/xml; subtype=gml/3.2.0", null ) );
    }

    @Test
    public void gml3_2_1() {
        assertEquals( GMLVersion.GML_32, fromMimeType( "text/xml; subtype=gml/3.2.1", null ) );
    }

    @Test
    public void gml3_2_2() {
        assertEquals( GMLVersion.GML_32, fromMimeType( "text/xml; subtype=gml/3.2.2", null ) );
    }

    @Test
    public void noGML() {
        assertEquals( null, fromMimeType( "text/xml", null ) );
    }

    @Test
    public void multipleSubtypes() {
        assertEquals( GMLVersion.GML_32, fromMimeType( "text/xml; subtype=gml/3.2; subtype=gml/3.2.1", null ) );
    }

    @Test
    public void strangeSubtypes() {
        assertEquals( null, fromMimeType( "text/xml; subtype gml/3.2", null ) );
    }

    @Test
    public void brokenVersion() {
        assertEquals( null, fromMimeType( "text/xml; subtype=gml/3.2", null ) );
    }
}
