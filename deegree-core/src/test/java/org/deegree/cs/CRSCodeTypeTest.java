//$HeadURL: svn+ssh://aionita@svn.wald.intevation.org/deegree/base/trunk/resources/eclipse/files_template.xml $
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

package org.deegree.cs;

import junit.framework.TestCase;

import org.junit.Test;

/**
 * Test class for CRSCodeType
 * 
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 * 
 * @author last edited by: $Author: ionita $
 * 
 * @version $Revision: $, $Date: $
 * 
 */
public class CRSCodeTypeTest extends TestCase {

    @Test
    public void test1() {
        CRSCodeType code1 = new CRSCodeType( "URN:X-OGC:DEF:CRS:EPSG:6.11:4326" );
        assertTrue( "Should be: " + "epsg:6.11:4326" + " but was " + code1.toString(),
                    "epsg:6.11:4326".equals( code1.toString() ) );

        CRSCodeType code2 = new CRSCodeType( "URN:X-OGC:DEF:CRS:EPSG:6.11.2:4326" );
        assertEquals( "epsg:6.11.2:4326", code2.toString() );

        CRSCodeType code3 = new CRSCodeType( "HTTP://WWW.OPENGIS.NET/GML/SRS/EPSG.XML#4326" );
        assertTrue( code3.toString().equals( "epsg:4326" ) );

        CRSCodeType code4 = new CRSCodeType( "URN:OPENGIS:DEF:CRS:EPSG::4326" );
        assertTrue( code4.toString().equals( "epsg:4326" ) );

        CRSCodeType code5 = new CRSCodeType( "CRS:84" );
        assertTrue( code5.toString().equals( "CRS:84" ) );

        CRSCodeType code6 = new CRSCodeType( "URN:OGC:DEF:CRS:OGC:1.3:CRS84" );
        assertTrue( code6.toString().equals( "URN:OGC:DEF:CRS:OGC:1.3:CRS84" ) );

        CRSCodeType code7 = new CRSCodeType( "WGS84(DD)" );
        assertTrue( code7.toString().equals( "WGS84(DD)" ) );
    }
}
