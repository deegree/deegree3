//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2015 by:
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
package org.deegree.protocol.csw.client;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.MalformedURLException;
import java.net.URL;

import org.deegree.protocol.csw.client.CSWClient.GetRecordsRequestType;
import org.junit.Test;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 */
public class CSWClientTest {

    @Test
    public void testDetectType_SoapOnly()
                            throws Exception {
        CSWClient cswClient = mockClient( "http://soap.test", null, null );
        GetRecordsRequestType type = cswClient.detectType();

        assertThat( type, is( GetRecordsRequestType.SOAP ) );
    }

    @Test
    public void testDetectType_PostOnly()
                            throws Exception {
        CSWClient cswClient = mockClient( null, "http://post.test", null );
        GetRecordsRequestType type = cswClient.detectType();

        assertThat( type, is( GetRecordsRequestType.POST ) );
    }

    @Test
    public void testDetectType_GetOnly()
                            throws Exception {
        CSWClient cswClient = mockClient( null, null, "http://get.test" );
        GetRecordsRequestType type = cswClient.detectType();

        assertThat( type, is( GetRecordsRequestType.GET ) );
    }

    @Test
    public void testDetectType_GetAndPost()
                            throws Exception {
        CSWClient cswClient = mockClient( null, "http://post.test", "http://get.test" );
        GetRecordsRequestType type = cswClient.detectType();

        assertThat( type, is( GetRecordsRequestType.POST ) );
    }

    @Test
    public void testDetectType_GetAndSoap()
                            throws Exception {
        CSWClient cswClient = mockClient( "http://soap.test", null, "http://get.test" );
        GetRecordsRequestType type = cswClient.detectType();

        assertThat( type, is( GetRecordsRequestType.SOAP ) );
    }

    @Test
    public void testDetectType_PostAndSoap()
                            throws Exception {
        CSWClient cswClient = mockClient( "http://soap.test", "http://post.test", null );
        GetRecordsRequestType type = cswClient.detectType();

        assertThat( type, is( GetRecordsRequestType.POST ) );
    }

    @Test
    public void testDetectType_PostAndSoapAndGet()
                            throws Exception {
        CSWClient cswClient = mockClient( "http://soap.test", "http://post.test", "http://get.test" );
        GetRecordsRequestType type = cswClient.detectType();

        assertThat( type, is( GetRecordsRequestType.POST ) );
    }

    private CSWClient mockClient( String soapUrl, String postUrl, String getUrl )
                            throws Exception {
        CSWClient mockedClient = mock( CSWClient.class );
        when( mockedClient.detectType() ).thenCallRealMethod();
        doReturn( asUrl( soapUrl ) ).when( mockedClient ).getPostEndpointUrlByType( "soap" );
        doReturn( asUrl( postUrl ) ).when( mockedClient ).getPostEndpointUrlByType( "xml" );
        doReturn( asUrl( getUrl ) ).when( mockedClient ).getGetUrl( "GetRecords" );
        return mockedClient;
    }

    private URL asUrl( String url )
                            throws MalformedURLException {
        if ( url == null )
            return null;
        return new URL( url );
    }

}