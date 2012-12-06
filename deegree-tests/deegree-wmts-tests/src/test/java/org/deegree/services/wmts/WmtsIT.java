//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2012 by:
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
package org.deegree.services.wmts;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import javax.xml.stream.XMLStreamException;

import org.deegree.commons.ows.metadata.operation.Operation;
import org.deegree.protocol.wmts.client.Layer;
import org.deegree.protocol.wmts.client.WMTSClient;
import org.junit.Test;

/**
 * Various integration tests for the WMTS.
 * 
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 * @author last edited by: $Author: schneider $
 * 
 * @version $Revision: $, $Date: $
 */
public class WmtsIT {

    private static final String ENDPOINT_BASE_URL = "http://localhost:" + 8080 + "/deegree-webservices/services?";

    private static final String ENDPOINT_URL = "http://localhost:" + 8080 + "/deegree-webservices/services?"
                                               + "service=WMTS&request=GetCapabilities&version=1.0.0";

    @Test
    public void testCapabilitiesOperationGetFeatureInfoListed()
                            throws XMLStreamException {
        WMTSClient client = initClient();
        Operation operation = client.getOperations().getOperation( "GetFeatureInfo" );
        assertNotNull( operation );
    }

    @Test
    public void testCapabilitiesPyramidLayerNoFeatureInfoFormats()
                            throws XMLStreamException {
        WMTSClient client = initClient();
        Layer pyramidLayer = client.getLayer( "pyramid" );
        assertNotNull( pyramidLayer );
        assertEquals( 0, pyramidLayer.getInfoFormats().size() );
    }

    @Test
    public void testCapabilitiesRemoteWmsLayerHasFeatureInfoFormats()
                            throws XMLStreamException {
        WMTSClient client = initClient();
        Layer pyramidLayer = client.getLayer( "remotewms" );
        assertNotNull( pyramidLayer );
        assertEquals( 2, pyramidLayer.getInfoFormats().size() );
        assertEquals( "application/gml+xml; version=3.1", pyramidLayer.getInfoFormats().get( 0 ) );
        assertEquals( "text/html", pyramidLayer.getInfoFormats().get( 1 ) );
    }

    @Test
    public void testGetFeatureInfoNonGfiLayer()
                            throws MalformedURLException, IOException {
        doGetFeatureInfo( "pyramid", "utah", "57142.857142857145", "text/html" );
    }
    
    @Test
    public void testGetFeatureInfoRemoteWmsGml()
                            throws MalformedURLException, IOException {
        InputStream response = doGetFeatureInfo( "remotewms", "utah", "57142.857142857145", "application/gml+xml; version=3.1" );
    }

    @Test
    public void testGetFeatureInfoRemoteWmsHtml()
                            throws MalformedURLException, IOException {
        InputStream response = doGetFeatureInfo( "remotewms", "utah", "57142.857142857145", "text/html" );
    }   

    private InputStream doGetFeatureInfo( String layer, String tileMatrixSet, String tileMatrixId, String infoFormat )
                            throws MalformedURLException, IOException {
        String request = "service=WMTS&version=1.0.0&request=GetFeatureInfo&style=default&tilerow=1&tilecol=1&i=1&j=1";
        request += "&layer=" + URLEncoder.encode( layer, "UTF-8" );
        request += "&tilematrixset=" + URLEncoder.encode( tileMatrixSet, "UTF-8" );
        request += "&tilematrix=" + URLEncoder.encode( tileMatrixId, "UTF-8" );
        request += "&infoformat=" + URLEncoder.encode( infoFormat, "UTF-8" );
        return performGetRequest( request );
    }

    private InputStream performGetRequest( String request )
                            throws MalformedURLException, IOException {
        return new URL( ENDPOINT_BASE_URL + request ).openStream();
    }

    private WMTSClient initClient() {
        try {
            return new WMTSClient( new URL( ENDPOINT_URL ), null );
        } catch ( Throwable t ) {
            throw new RuntimeException( "Unable to initialize WMTSClient: " + t.getMessage(), t );
        }
    }
}
