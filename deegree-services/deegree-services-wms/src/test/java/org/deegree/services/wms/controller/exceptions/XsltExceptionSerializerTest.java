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
package org.deegree.services.wms.controller.exceptions;

import static org.deegree.commons.ows.exception.OWSException.NO_APPLICABLE_CODE;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;

import javax.servlet.ServletOutputStream;

import org.deegree.commons.ows.exception.OWSException;
import org.deegree.services.controller.exception.serializer.XMLExceptionSerializer;
import org.deegree.services.controller.utils.HttpResponseBuffer;
import org.deegree.services.ows.PreOWSExceptionReportSerializer;
import org.deegree.workspace.Workspace;
import org.junit.Test;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 */
public class XsltExceptionSerializerTest {

    private static final String EXCEPTION = "EXCEPTION";

    @Test
    public void testSerializeException()
                            throws Exception {
        XsltExceptionSerializer xsltExceptionSerializer = createXsltExceptionSerializer();

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        OWSException ex = new OWSException( EXCEPTION, NO_APPLICABLE_CODE );

        serialize( xsltExceptionSerializer, os, ex );

        String htmlException = os.toString();

        assertThat( htmlException, containsString( EXCEPTION ) );
        assertThat( htmlException, containsString( NO_APPLICABLE_CODE ) );
    }

    private void serialize( XsltExceptionSerializer xsltExceptionSerializer, ByteArrayOutputStream os, OWSException ex )
                            throws Exception {
        XMLExceptionSerializer exceptionSerializer = new PreOWSExceptionReportSerializer( "text/xml" );
        ServletOutputStream outputStream = createServletStream( os );
        HttpResponseBuffer response = mockResponse( outputStream );
        xsltExceptionSerializer.serializeException( response, ex, exceptionSerializer, null );
    }

    private XsltExceptionSerializer createXsltExceptionSerializer() {
        URL xsltUrl = XsltExceptionSerializerTest.class.getResource( "exceptions2html.xsl" );
        Workspace workspace = mockWorkspace();
        return new XsltExceptionSerializer( xsltUrl, workspace );
    }

    private HttpResponseBuffer mockResponse( ServletOutputStream outputStream )
                            throws IOException {
        HttpResponseBuffer mockedResponse = mock( HttpResponseBuffer.class );
        when( mockedResponse.getOutputStream() ).thenReturn( outputStream );
        return mockedResponse;
    }

    private Workspace mockWorkspace() {
        return mock( Workspace.class );
    }

    private ServletOutputStream createServletStream( final ByteArrayOutputStream os ) {
        return new ServletOutputStream() {

            @Override
            public void write( int b )
                                    throws IOException {
                os.write( b );
            }
        };
    }
}