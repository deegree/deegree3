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

package org.deegree.crs;

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
        CRSCodeType code1 = new CRSCodeType( "EPSG:4326" );
        assertEquals( code1.getEquivalentString(), "EPSG:4326" );
        assertEquals( CRSCodeType.valueOf( "EPSG:4326" ).getEquivalentString(), "EPSG:4326" );
        
        CRSCodeType code2 = new CRSCodeType( "URN:X-OGC:DEF:CRS:EPSG:6.11:4326" );
        assertEquals( code2.getEquivalentString(), "EPSG:6.11:4326" );
        System.out.println( code2 );
        assertEquals( CRSCodeType.valueOf( "URN:X-OGC:DEF:CRS:EPSG:6.11:4326" ).getEquivalentString(), "EPSG:6.11:4326" );
        
        CRSCodeType code3 = new CRSCodeType( "URN:X-OGC:DEF:CRS:EPSG:6.11.2:4326" );
        assertEquals( code3.getEquivalentString(), "EPSG:6.11.2:4326" );
        assertEquals( CRSCodeType.valueOf( "URN:X-OGC:DEF:CRS:EPSG:6.11.2:4326" ).getEquivalentString(), "EPSG:6.11.2:4326" );
        
        CRSCodeType code4 = new CRSCodeType( "URN:OGC:DEF:CRS:EPSG::4326" );
        assertEquals( code4.getEquivalentString(), "EPSG:4326" );
        assertEquals( CRSCodeType.valueOf( "URN:OGC:DEF:CRS:EPSG::4326" ).getEquivalentString(), "EPSG:4326" );
        
        CRSCodeType code5 = new CRSCodeType( "URN:OGC:DEF:CRS:EPSG:4326" );
        assertEquals( code5.getEquivalentString(), "EPSG:4326" );
        assertEquals( CRSCodeType.valueOf( "URN:OGC:DEF:CRS:EPSG:4326" ).getEquivalentString(), "EPSG:4326" );
        
        CRSCodeType code6 = new CRSCodeType( "HTTP://WWW.OPENGIS.NET/GML/SRS/EPSG.XML#4326" );
        assertEquals( code6.getEquivalentString(), "EPSG:4326" );
        assertEquals( CRSCodeType.valueOf( "HTTP://WWW.OPENGIS.NET/GML/SRS/EPSG.XML#4326" ).getEquivalentString(), "EPSG:4326" );
        
        CRSCodeType code7 = new CRSCodeType( "URN:OPENGIS:DEF:CRS:EPSG::4326" );
        assertEquals( code7.getEquivalentString(), "EPSG:4326" );
        assertEquals( CRSCodeType.valueOf( "URN:OPENGIS:DEF:CRS:EPSG::4326" ).getEquivalentString(), "EPSG:4326" );
        
        CRSCodeType code8 = new CRSCodeType( "URN:X-OGC:DEF:CRS:EPSG:4326" );        
        assertEquals( code8.getEquivalentString(), "EPSG:4326" );
        assertEquals( CRSCodeType.valueOf( "URN:X-OGC:DEF:CRS:EPSG:4326" ).getEquivalentString(), "EPSG:4326" );
    }

}
