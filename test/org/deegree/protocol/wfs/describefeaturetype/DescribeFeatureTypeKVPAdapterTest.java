//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/base/trunk/resources/eclipse/files_template.xml $
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
   Department of Geography, University of Bonn
 and
   lat/lon GmbH

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

package org.deegree.protocol.wfs.describefeaturetype;

import java.io.IOException;
import java.util.Map;

import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.deegree.commons.utils.kvp.KVPUtils;
import org.deegree.protocol.wfs.WFSConstants;
import org.junit.Test;

/**
 * The <code>DescribeFeatureTypeKVPAdapterTest</code> class tests the parsing of the DescribeFeatureType request in the
 * case the content is in KVP format.
 *
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 * @author last edited by: $Author: ionita $
 *
 * @version $Revision: $, $Date: $
 */
public class DescribeFeatureTypeKVPAdapterTest extends TestCase {

    private final String EXAMPLE_1 = "examples_kvp/v110/example1.kvp";

    private final String EXAMPLE_2 = "examples_kvp/v110/example2.kvp";

    @Test
    public void testEXAMPLE_1() throws IOException {
        Map<String,String> kvpParams = KVPUtils.readFileIntoMap( this.getClass().getResource( EXAMPLE_1 ) );
        DescribeFeatureType dft = DescribeFeatureTypeKVPAdapter.parse110( kvpParams );
        assertEquals( dft.getHandle(), null );
        assertEquals( dft.getOutputFormat(), null );
        assertEquals( dft.getTypeNames().length, 1 );
        assertEquals( dft.getTypeNames()[0], new QName( "TreesA_1M" ) );
        assertEquals( dft.getVersion(), WFSConstants.VERSION_110 );
    }

    @Test
    public void testEXAMPLE_2() throws IOException {
        Map<String,String> kvpParams = KVPUtils.readFileIntoMap( this.getClass().getResource( EXAMPLE_2 ) );
        DescribeFeatureType dft = DescribeFeatureTypeKVPAdapter.parse110( kvpParams );
        assertEquals( dft.getHandle(), null );
        assertEquals( dft.getOutputFormat(), null );
        assertEquals( dft.getTypeNames().length, 2 );
        assertEquals( dft.getTypeNames()[0], new QName( "TreesA_1M" ) );
        assertEquals( dft.getTypeNames()[1], new QName( "BuiltUpA_1M" ) );
        assertEquals( dft.getVersion(), WFSConstants.VERSION_110 );
    }
}
