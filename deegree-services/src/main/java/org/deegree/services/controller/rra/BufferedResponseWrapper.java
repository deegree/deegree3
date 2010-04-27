//$HeadURL$
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

package org.deegree.services.controller.rra;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/**
 * <code>BufferedResponseWrapper</code>
 *
 * This is a custom {@link HttpServletResponseWrapper}. It will buffer all data internally and will only send the result
 * after {@link #flushBuffer()} is called.
 *
 * This allows two things:
 * <ul>
 * <li>The header can be changed after the response is generated.</li>
 * <li>The whole response and the header can be discarded.</li>
 * </ul>
 *
 * With the first the service is able to set the Content-length. The second allows to discard the generated response and
 * start the response from scratch. This is useful if an exception occurred and a ExceptionReport should be returned and
 * not the partial original result.
 *
 * This wrapper allows the change between {@link #getWriter()} and {@link #getOutputStream()} after {@link #reset()} was
 * called. This is unlike the original servlet API that throws an {@link IllegalStateException} when getWriter is called
 * after getOutputStream, or vice versa.
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class BufferedResponseWrapper extends HttpServletResponseWrapper {

    private final ByteArrayOutputStream buffer;

    /**
     * The servlet api only allows a call to either getWriter or getOutputStream. This enum will protocol the current
     * state.
     */
    private enum ReturnType {
        NOT_DEFINED_YET, PRINT_WRITER, OUTPUT_STREAM
    }

    private ReturnType returnType = ReturnType.NOT_DEFINED_YET;

    private PrintWriter printWriter;

    private ServletOutputStream outputStream;

    /**
     * @param response
     */
    public BufferedResponseWrapper( HttpServletResponse response ) {
        super( response );
        buffer = new ByteArrayOutputStream();
        outputStream = new BufferedServletOutputStream( buffer );
    }

    @Override
    public PrintWriter getWriter()
                            throws IOException {
        if ( returnType == ReturnType.NOT_DEFINED_YET ) {
            OutputStreamWriter writer = new OutputStreamWriter( outputStream, getCharacterEncoding() );
            printWriter = new PrintWriter( writer );
            returnType = ReturnType.PRINT_WRITER;
        }
        if ( returnType == ReturnType.OUTPUT_STREAM ) {
            throw new IllegalStateException( "getOutputStream() has already been called for this response" );
        }
        return printWriter;
    }

    @Override
    public ServletOutputStream getOutputStream()
                            throws IOException {
        if ( returnType == ReturnType.NOT_DEFINED_YET ) {
            returnType = ReturnType.OUTPUT_STREAM;
        }
        if ( returnType == ReturnType.PRINT_WRITER ) {
            throw new IllegalStateException( "getWriter() has already been called for this response" );
        }
        return outputStream;
    }

    @Override
    public void flushBuffer()
                            throws IOException {
        buffer.writeTo( super.getOutputStream() );
        buffer.reset();
        super.flushBuffer();
    }

    @Override
    public void reset() {
        if ( !isCommitted() ) {
            buffer.reset();
            super.reset();
            returnType = ReturnType.NOT_DEFINED_YET;
        } else {
            super.reset(); // throws IllegalStateException
        }
    }
}
