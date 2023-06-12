/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
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
package org.deegree.services.controller;

import java.io.IOException;
import java.util.zip.GZIPOutputStream;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;

/**
 * {@link ServletOutputStream} used by {@link GZipHttpServletResponse}.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * 
 */
public class GZipServletOutputStream extends ServletOutputStream {

    private final GZipHttpServletResponse response;

    private final GZIPOutputStream gos;

    private final ServletOutputStream sos;

    private boolean closed;

    public GZipServletOutputStream( GZipHttpServletResponse response, ServletOutputStream os ) throws IOException {
        this.response = response;
        gos = new GZIPOutputStream( os );
        sos = os;
    }

    @Override
    public void close()
                            throws IOException {
        if ( !closed ) {
            gos.finish();
            sos.flush();
            sos.close();
            closed = true;
        }
    }

    boolean isClosed() {
        return closed;
    }

    @Override
    public void flush()
                            throws IOException {
        gos.flush();
        sos.flush();
    }

    @Override
    public void write( byte[] b )
                            throws IOException {
        gos.write( b );
    }

    @Override
    public void write( byte[] b, int off, int len )
                            throws IOException {
        gos.write( b, off, len );
    }

    @Override
    public void write( int b )
                            throws IOException {
        gos.write( b );
    }

    @Override
    public boolean isReady() {
        return false;
    }

    @Override
    public void setWriteListener(WriteListener writeListener) {

    }
}