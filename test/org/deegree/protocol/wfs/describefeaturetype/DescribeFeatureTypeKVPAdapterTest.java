//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/base/trunk/resources/eclipse/files_template.xml $
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

package org.deegree.protocol.wfs.describefeaturetype;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.deegree.protocol.wfs.WFSConstants;
import org.junit.Before;
import org.junit.Test;

/**
 * The <code>DescribeFeatureTypeKVPAdapterTest</code> class tests the parsing of the DescribeFeatureType
 * request in the case the content is in KVP format.
 * 
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 * 
 * @author last edited by: $Author: ionita $
 * 
 * @version $Revision: $, $Date: $
 *
 */
public class DescribeFeatureTypeKVPAdapterTest extends TestCase {

    final String EXAMPLE_1 = "test/org/deegree/protocol/wfs/describefeaturetype/examples_kvp/v110/example1.kvp";
    
    final String EXAMPLE_2 = "test/org/deegree/protocol/wfs/describefeaturetype/examples_kvp/v110/example2.kvp";
    
    Map<String, String> kvpParams1;
    
    Map<String, String> kvpParams2;
    
    @Before
    public void setUp() throws Exception {
        BufferedReader reader = new BufferedReader( new FileReader( EXAMPLE_1 ) );
        String line = null;
        kvpParams1 = new HashMap<String, String>();
        while ( ( line = reader.readLine() ) != null ) {
            if ( line.contains( "=" ) ) {
                String[] parts = line.split( "=|&" );
                if ( parts.length == 2 ) {
                    kvpParams1.put( parts[0], parts[1] );
                }
            }
        }
        
        reader = new BufferedReader( new FileReader( EXAMPLE_2 ) );
        line = null;
        kvpParams2 = new HashMap<String, String>();
        while ( ( line = reader.readLine() ) != null ) {
            if ( line.contains( "=" ) ) {
                String[] parts = line.split( "=|&" );
                if ( parts.length == 2 ) {
                    kvpParams2.put( parts[0], parts[1] );
                }
            }
        }
    }
    
    @Test
    public void testEXAMPLE_1() {
        DescribeFeatureType dft = DescribeFeatureTypeKVPAdapter.parse110( kvpParams1 );
        assertEquals( dft.getHandle(), null );
        assertEquals( dft.getOutputFormat(), null );
        assertEquals( dft.getTypeNames().length, 1 );
        assertEquals( dft.getTypeNames()[0], new QName( "TreesA_1M" ) );
        assertEquals( dft.getVersion(), WFSConstants.VERSION_110 );        
    }
    
    @Test
    public void testEXAMPLE_2() {
        DescribeFeatureType dft = DescribeFeatureTypeKVPAdapter.parse110( kvpParams2 );
        assertEquals( dft.getHandle(), null );
        assertEquals( dft.getOutputFormat(), null );
        assertEquals( dft.getTypeNames().length, 2 );
        assertEquals( dft.getTypeNames()[0], new QName( "TreesA_1M" ) );
        assertEquals( dft.getTypeNames()[1], new QName( "BuiltUpA_1M" ) );
        assertEquals( dft.getVersion(), WFSConstants.VERSION_110 );
    }

}
