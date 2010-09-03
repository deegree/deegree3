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
package org.deegree.commons.utils.io;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Scalable alternative to {@link ByteArrayOutputStream} that automatically switches to file storage if the amount of
 * written output exceeds a given limit.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class StreamBufferStore extends OutputStream {

    private static final Logger LOG = LoggerFactory.getLogger( StreamBufferStore.class );

    /** Default limit */
    public static final int DEFAULT_LIMIT = 1024 * 1024;

    private final int limit;

    private int bytesWritten = 0;

    private OutputStream os;

    private File tmpFile;

    /**
     * Creates a new {@link StreamBufferStore} instance that switches to file storage when
     * {@link StreamBufferStore#DEFAULT_LIMIT} bytes have been written.
     */
    public StreamBufferStore() {
        this( DEFAULT_LIMIT );
    }

    /**
     * Creates a new {@link StreamBufferStore} instance that switches to file storage when the specified number of bytes
     * have been written.
     * 
     * @param limit
     *            number of bytes when switching to file will occur
     */
    public StreamBufferStore( int limit ) {
        this.limit = limit;
        os = new ByteArrayOutputStream( limit );
    }

    /**
     * Returns the current size of the buffer.
     * 
     * @return the number of valid bytes in this buffer
     */
    public int size() {
        return bytesWritten;
    }

    /**
     * Returns an {@link InputStream} for accessing the previously written bytes.
     * 
     * @return an {@link InputStream}, never <code>null</code>
     * @throws IOException
     */
    public InputStream getInputStream()
                            throws IOException {
        os.flush();
        if ( tmpFile == null ) {
            return new ByteArrayInputStream( ( (ByteArrayOutputStream) os ).toByteArray() );
        }
        return new FileInputStream( tmpFile );
    }

    @Override
    public void close()
                            throws IOException {
        os.close();
    }

    @Override
    public void flush()
                            throws IOException {
        os.flush();
    }

    @Override
    public void write( byte[] b )
                            throws IOException {
        if ( tmpFile == null && bytesWritten + b.length > DEFAULT_LIMIT ) {
            switchToFile();
        }
        os.write( b );
        bytesWritten += b.length;
    }

    @Override
    public void write( byte[] b, int off, int len )
                            throws IOException {
        if ( tmpFile == null && bytesWritten + len > DEFAULT_LIMIT ) {
            switchToFile();
        }
        os.write( b, off, len );
        bytesWritten += len;
    }

    @Override
    public void write( int b )
                            throws IOException {
        if ( tmpFile == null && bytesWritten == DEFAULT_LIMIT ) {
            switchToFile();
        }
        os.write( b );
        bytesWritten++;
    }

    /**
     * Writes the complete contents of this buffer to the specified output stream argument, as if by calling the output
     * stream's write method using <code>out.write(buf, 0, count)</code>.
     * 
     * @param outputStream
     *            stream to write to, must not be <code>null</code>
     * @throws IOException
     *             if an I/O error occurs
     */
    public void writeTo( OutputStream outputStream )
                            throws IOException {
        BufferedInputStream is = new BufferedInputStream( getInputStream() );
        byte[] buffer = new byte[10240];
        int read = -1;
        while ( ( read = is.read( buffer ) ) != -1 ) {
            outputStream.write( buffer, 0, read );
        }
        is.close();
    }

    /**
     * Clears any data that exists in the buffer.
     */
    public void reset() {
        try {
            os.close();
        } catch ( IOException e ) {
            LOG.error( "Error closing sink. Continuing anyway." );
        }
        if ( tmpFile != null ) {
            tmpFile.delete();
            os = new ByteArrayOutputStream( limit );
            tmpFile = null;
        } else {
            os = new ByteArrayOutputStream( limit );
        }
    }

    private void switchToFile()
                            throws IOException {
        LOG.info( "Limit of " + limit + " bytes reached. Switching to file based buffering." );
        tmpFile = File.createTempFile( "store", ".tmp" );
        LOG.info( "Using file: " + tmpFile );
        OutputStream fileOs = new BufferedOutputStream( new FileOutputStream( tmpFile ) );
        ( (ByteArrayOutputStream) os ).writeTo( fileOs );
        os = fileOs;
    }
}
