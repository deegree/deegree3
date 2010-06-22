//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
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
package org.deegree.protocol.wps;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.deegree.protocol.wps.execute.ExecuteResponse;
import org.deegree.protocol.wps.tools.InputObject;
import org.junit.Assert;
import org.junit.Test;

/**
 * The <code></code> class TODO add class documentation here.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class WPSClientTest {

    private static final String BASE_URL = "http://ows7.lat-lon.de/d3WPS_JTS/services?";

    private static final String FULL_SERVICE_URL = "http://ows7.lat-lon.de/d3WPS_JTS/services?service=WPS&version=1.0.0&request=GetCapabilities";

   
    
    @Test
    public void testGetProcessIdentifiers()
                            throws MalformedURLException {
        URL processUrl = new URL( FULL_SERVICE_URL );
        WPSClient wpsClient = new WPSClient( processUrl );
        String[] processIds = wpsClient.getProcessIdentifiers();
        Assert.assertNotNull( processIds );
        Assert.assertEquals( 11, processIds.length );
    }

    @Test
    public void testGetProcessInfo()
                            throws MalformedURLException {
        URL processUrl = new URL( FULL_SERVICE_URL );
        WPSClient wpsClient = new WPSClient( processUrl );
        ProcessInfo pi = wpsClient.getProcessInfo( "Buffer" );
        Assert.assertNotNull( pi );
        Assert.assertEquals( "Buffer", pi.getIdentifier() );
        
        // TODO test abstract and input and output parameters.
    }
    
//    @Test
//    public void testGetProcessInfoBroken()
//                            throws MalformedURLException {
//        URL processUrl = new URL( FULL_SERVICE_URL );
//        WPSClient wpsClient = new WPSClient( processUrl );
//        ProcessInfo pi = wpsClient.getProcessInfo( "Buffers" );
//        Assert.assertNotNull( pi );
//        Assert.assertEquals( "Buffer", pi.getIdentifier() );
//        // TODO test abstract and input and output parameters.
//    }

    @Test
    public void testExecuteCentroid()
                            throws Exception {

        URL processUrl = new URL( FULL_SERVICE_URL );
        WPSClient wpsClient = new WPSClient( processUrl );

        // started process centroid
        InputObject[] inputObject = new InputObject[1];

        URL curveResource = WPSClientTest.class.getResource( "curve.xml" );
        File curveFile = new File( curveResource.toURI() );

        InputObject inputObject1 = wpsClient.setInputasFile( "GMLInput", curveFile.getPath() );
        inputObject[0] = inputObject1;
        Object ergebnis = wpsClient.executeProcessObejctResult( inputObject, "Centroid" );

        System.out.println( "ergebnis Centroid" );
        System.out.println( String.valueOf( ergebnis ) );
    }

    @Test
    public void testExecuteBuffer()
                            throws Exception {

        URL processUrl = new URL( FULL_SERVICE_URL );
        WPSClient wpsClient = new WPSClient( processUrl );

        // started process Buffer
        InputObject[] inputObjectBuffer = new InputObject[2];
        URL curveResource = WPSClientTest.class.getResource( "curve.xml" );
        File curveFile = new File( curveResource.toURI() );

        InputObject inputObject1 = wpsClient.setInputasFile( "GMLInput", curveFile.getPath() );
        inputObjectBuffer[0] = inputObject1;
        InputObject inputObject2 = wpsClient.setInputasObject( "BufferDistance", "43" );
        inputObjectBuffer[1] = inputObject2;

        ExecuteResponse ergebnis = wpsClient.executeProcessExecuteResponseResult( inputObjectBuffer, "Buffer" );

        
      Assert.assertEquals(ergebnis.getProcessOutputs().getOutputs().get( 0 ).getIdentifier(), "BufferedGeometry");
        
    }
}
