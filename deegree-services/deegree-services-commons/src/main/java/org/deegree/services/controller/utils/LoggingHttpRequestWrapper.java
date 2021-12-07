//$HeadURL$
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
package org.deegree.services.controller.utils;

import org.apache.commons.io.IOUtils;
import org.deegree.services.controller.RequestLogger;
import org.slf4j.Logger;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static java.io.File.createTempFile;
import static java.lang.System.currentTimeMillis;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * @version $Revision$, $Date$
 */
public class LoggingHttpRequestWrapper extends HttpServletRequestWrapper {

    private static final Logger LOG = getLogger( LoggingHttpRequestWrapper.class );

    private final String kvp;

    private final String logDirectory;

    private final RequestLogger logger;

    private final long entryTime;

    private boolean logged;

    private byte[] requestBody;

    /**
     * If XML request should possibly be logged.
     *
     * @param request
     * @param logDirectory
     * @param logger
     *                 is closed before logging is initiated if not null
     */
    public LoggingHttpRequestWrapper( HttpServletRequest request, String logDirectory,
                                      RequestLogger logger ) {
        super( request );
        this.kvp = request.getQueryString();
        this.logDirectory = logDirectory;
        this.logger = logger;
        this.entryTime = System.currentTimeMillis();
        try {
            if ( request.getInputStream() != null )
                requestBody = IOUtils.toByteArray( request.getInputStream() );
        } catch ( IOException ex ) {
            requestBody = new byte[0];
        }
    }

    @Override
    public ServletInputStream getInputStream()
                    throws IOException {
        return new DelegatingServletInputStream( new ByteArrayInputStream( requestBody ) );
    }

    /**
     * kvp: possibly writes the entry into the log, XML/SOAP: possibly deletes the logged request file
     */
    public void finalizeLogging()
                    throws IOException {
        if ( logged ) {
            return;
        }
        logged = true;
        if ( kvp != null ) {
            logger.logKVP( getRequestURL().toString(), kvp, entryTime, currentTimeMillis(), null );
        }
        if ( requestBody != null && requestBody.length > 0 ) {
            File tmpLogFile = createTmpLogFile();
            IOUtils.copy( new ByteArrayInputStream( requestBody ), new FileOutputStream( tmpLogFile ) );
            logger.logXML( getRequestURL().toString(), tmpLogFile, entryTime, currentTimeMillis(), null );
            if ( !tmpLogFile.delete() ) {
                LOG.warn( "Could not delete temporary file {}.", tmpLogFile );
            }
        }
    }

    private File createTmpLogFile()
                    throws IOException {
        if ( logDirectory == null ) {
            return createTempFile( "request", ".body" );
        }
        File directory = new File( logDirectory );
        if ( !directory.exists() ) {
            directory.mkdirs();
        }
        return createTempFile( "request", ".body", directory );
    }

    private class DelegatingServletInputStream extends ServletInputStream {

        private final InputStream sourceStream;

        /**
         * Create a DelegatingServletInputStream for the given source stream.
         *
         * @param sourceStream
         *                 the source stream (never {@code null})
         */
        public DelegatingServletInputStream( InputStream sourceStream ) {
            this.sourceStream = sourceStream;
        }

        /**
         * Return the underlying source stream (never {@code null}).
         */
        public final InputStream getSourceStream() {
            return this.sourceStream;
        }

        @Override
        public int read()
                        throws IOException {
            return this.sourceStream.read();
        }

        @Override
        public int available()
                        throws IOException {
            return this.sourceStream.available();
        }

        @Override
        public void close()
                        throws IOException {
            super.close();
            this.sourceStream.close();
        }
    }

}