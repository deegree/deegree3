//$HeadURL$
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

package org.deegree.protocol.wfs.describefeaturetype.xml;

import static junit.framework.Assert.assertEquals;
import static org.deegree.protocol.wfs.WFSConstants.VERSION_200;

import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.namespace.QName;

import org.deegree.commons.xml.schema.RedirectingEntityResolver;
import org.deegree.protocol.wfs.describefeaturetype.DescribeFeatureType;
import org.junit.Test;

/**
 * Tests for {@link DescribeFeatureTypeXMLAdapter}.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class DescribeFeatureTypeXMLAdapterTest {

    // files are not really fetched from this URL, but taken from cached version (module deegree-ogcschemas)
    private static final String WFS200_EXAMPLES_BASE_URL = "http://schemas.opengis.net/wfs/2.0/examples/DescribeFeatureType/";

    @Test
    public void test200Example01()
                            throws Exception {

        DescribeFeatureTypeXMLAdapter parser = new DescribeFeatureTypeXMLAdapter();
        parser.load( DescribeFeatureTypeXMLAdapterTest.class.getResource(
                                "wfs200/DescribeFeatureType_Example01_Request.xml" ) );
        DescribeFeatureType request = parser.parse();
        assertEquals( VERSION_200, request.getVersion() );
        assertEquals( null, request.getHandle() );
        assertEquals( null, request.getOutputFormat() );
        assertEquals( 2, request.getTypeNames().length );
        assertEquals( QName.valueOf( "{http://www.myserver.com/myns}TreesA_1M" ), request.getTypeNames()[0] );
        assertEquals( QName.valueOf( "{http://www.myserver.com/myns}RoadL_1M" ), request.getTypeNames()[1] );
    }

    @Test
    public void test200Example02()
                            throws Exception {

        DescribeFeatureTypeXMLAdapter parser = new DescribeFeatureTypeXMLAdapter();
        parser.load( DescribeFeatureTypeXMLAdapterTest.class.getResource(
                                "wfs200/DescribeFeatureType_Example02_Request.xml" ) );
        DescribeFeatureType request = parser.parse();
        assertEquals( VERSION_200, request.getVersion() );
        assertEquals( null, request.getHandle() );
        assertEquals( "text/xml; subtype=gml/3.2", request.getOutputFormat() );
        assertEquals( 1, request.getTypeNames().length );
    }

    /**
     * TODO: Unused until https://github.com/deegree/deegree3/issues/1091 is implemented.
     * @param name
     * @return
     */
    private URL get200ExampleUrl( String name ) {
        try {
            String url = new RedirectingEntityResolver().redirect( WFS200_EXAMPLES_BASE_URL + name );
            return new URL( url );
        } catch ( MalformedURLException e ) {
            // should never happen
        }
        return null;
    }
}
