//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2011 by:
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
package org.deegree.protocol.wmts.client;

import static junit.framework.Assert.assertEquals;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.deegree.protocol.ows.exception.OWSExceptionReport;
import org.deegree.tile.TileMatrix;
import org.deegree.tile.TileMatrixSet;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link WMTSCapabilitiesAdapter}.
 * 
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class WMTSCapabilitiesAdapterTest {

    private WMTSCapabilitiesAdapter adapter;

    @Before
    public void setup()
                            throws OWSExceptionReport, XMLStreamException, IOException {
        adapter = new WMTSCapabilitiesAdapter();
        URL capaUrl = WMTSClientTest.class.getResource( "wmts100_capabilities_example.xml" );
        adapter.load( capaUrl );
    }

    /**
     * Test method for {@link org.deegree.protocol.wmts.client.WMTSCapabilitiesAdapter#parseLayers()}.
     */
    @Test
    public void testParseLayers()
                            throws XMLStreamException {
        List<Layer> layers = adapter.parseLayers();
        assertEquals( 1, layers.size() );
    }

    /**
     * Test method for {@link org.deegree.protocol.wmts.client.WMTSCapabilitiesAdapter#parseTileMatrixSets()}.
     */
    @Test
    public void testParseTileMatrixSets()
                            throws XMLStreamException {
//        List<TileMatrixSet> tileMatrixSets = adapter.parseTileMatrixSets();
//        assertEquals( 1, tileMatrixSets.size() );
//
//        TileMatrixSet matrixSet = tileMatrixSets.get( 0 );
//        assertEquals( "Satellite_Provo", matrixSet.getIdentifier() );
//        List<TileMatrix> tileMatrices = matrixSet.getTileMatrices();
//        assertEquals( 4, tileMatrices.size() );
//
//        TileMatrix tileMatrix = tileMatrices.get( 0 );
//        assertEquals( "7142.857142857143", tileMatrix.getIdentifier() );
//        assertEquals( 7142.857142857143, tileMatrix.getResolution(), 0.00001 );
//        assertEquals( 256, tileMatrix.getTilePixelsX() );
//        assertEquals( 256, tileMatrix.getTilePixelsY() );
//        assertEquals( 13, tileMatrix.getNumTilesX() );
//        assertEquals( 15, tileMatrix.getNumTilesY() );
    }
}
