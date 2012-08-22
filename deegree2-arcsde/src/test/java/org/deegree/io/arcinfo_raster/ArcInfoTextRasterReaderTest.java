//$HeadURL: svn+ssh://developername@svn.wald.intevation.org/deegree/base/trunk/test/junit/org/deegree/io/arcinfo_raster/ArcInfoTextRasterReaderTest.java $
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
package org.deegree.io.arcinfo_raster;

import java.net.URL;

import junit.framework.TestCase;

import org.deegree.model.coverage.grid.WorldFile;

/**
 *
 *
 *
 * @version $Revision: 18195 $
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author: mschneider $
 *
 * @version 1.0. $Revision: 18195 $, $Date: 2009-06-18 17:55:39 +0200 (Do, 18 Jun 2009) $
 *
 * @since 2.0
 */
public class ArcInfoTextRasterReaderTest extends TestCase {

    private URL url = ArcInfoTextRasterReaderTest.class.getResource( "testdata.dat" );

    protected void setUp()
                            throws Exception {
        super.setUp();
    }

    protected void tearDown()
                            throws Exception {
        super.tearDown();
    }

    public void testReadMetadata()
                            throws Exception {
        assertNotNull( url );
        ArcInfoTextRasterReader reader = new ArcInfoTextRasterReader( url.getFile() );
        WorldFile wf = reader.readMetadata();
        assertEquals( reader.getNoDataValue(), -9999d );
        assertEquals( wf.getResx(), 2d );
        assertEquals( wf.getResy(), 2d );
        assertEquals( wf.getEnvelope().getMin().getX(), 3500001d );
        assertEquals( wf.getEnvelope().getMin().getY(), 6090001d );
        assertEquals( wf.getEnvelope().getMax().getX(), 3500009d );
        assertEquals( wf.getEnvelope().getMax().getY(), 6090013d );
    }

    public void testReadData()
                            throws Exception {
        assertNotNull( url );
        ArcInfoTextRasterReader reader = new ArcInfoTextRasterReader( url.getFile() );
        float[][] data = reader.readData();
        assertEquals( data[0][0], -9999f );
        assertEquals( data[0][4], 10f );
        assertEquals( data[5][3], 10f );
        assertEquals( data[6][1], -9999f );
    }

}
