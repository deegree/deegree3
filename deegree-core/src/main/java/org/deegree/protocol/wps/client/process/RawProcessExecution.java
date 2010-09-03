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
package org.deegree.protocol.wps.client.process;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.commons.tom.ows.CodeType;
import org.deegree.commons.xml.stax.StAXParsingHelper;
import org.deegree.protocol.ows.OWSExceptionReader;
import org.deegree.protocol.ows.exception.OWSException;
import org.deegree.protocol.wps.client.WPSClient;
import org.deegree.protocol.wps.client.output.ComplexOutput;
import org.deegree.protocol.wps.client.process.execute.OutputFormat;
import org.deegree.protocol.wps.client.process.execute.ResponseFormat;
import org.deegree.protocol.wps.client.wps100.ExecuteRequest100Writer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents an execution context for a {@link Process} that uses the <code>RawOutput</code> mode.
 * <p>
 * NOTE: This class is not thread-safe.
 * </p>
 * 
 * @see Process
 * @see ProcessExecution
 * 
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class RawProcessExecution extends AbstractProcessExecution {

    private static Logger LOG = LoggerFactory.getLogger( RawProcessExecution.class );

    private final List<OutputFormat> outputDefs = new ArrayList<OutputFormat>();

    /**
     * Creates a new {@link RawProcessExecution} instance.
     * 
     * @param client
     *            associated WPS client instance, must not be <code>null</code>
     * @param process
     *            associated process instance, must not be <code>null</code>
     */
    RawProcessExecution( WPSClient client, Process process ) {
        super( client, process );
    }

    /**
     * Executes the process and returns the specified complex output.
     * 
     * @param id
     *            identifier of the output parameter to be returned, must not be <code>null</code>
     * @param idCodeSpace
     *            codespace of the parameter identifier, may be <code>null</code> (for identifiers without codespace)
     * @param mimeType
     *            mimeType of the format, may be null
     * @param encoding
     *            encoding of the format, may be null
     * @param schema
     *            schema of the format, in case it is an XML format
     * @return requested process output, never <code>null</code>
     * @throws IOException
     *             if a communication/network problem occured
     * @throws OWSException
     *             if the server replied with an exception
     * @throws XMLStreamException
     */
    public ComplexOutput executeComplexOutput( String id, String idCodeSpace, String mimeType, String encoding,
                                               String schema )
                            throws OWSException, IOException, XMLStreamException {
        outputDefs.add( new OutputFormat( new CodeType( id ), null, false, mimeType, encoding, schema ) );
        return sendExecute();
    }

    private ComplexOutput sendExecute()
                            throws OWSException, XMLStreamException, IOException {

        ResponseFormat responseFormat = new ResponseFormat( true, false, false, false, outputDefs );

        // TODO what if server only supports Get?
        URL url = client.getExecuteURL( true );

        URLConnection conn = url.openConnection();
        conn.setDoOutput( true );
        conn.setUseCaches( false );
        // TODO does this need configurability?
        conn.setRequestProperty( "Content-Type", "application/xml" );

        XMLOutputFactory outFactory = XMLOutputFactory.newInstance();

        OutputStream os = conn.getOutputStream();
        XMLInputFactory inFactory = XMLInputFactory.newInstance();

        if ( LOG.isDebugEnabled() ) {
            File logFile = File.createTempFile( "wpsclient", "request.xml" );
            XMLStreamWriter logWriter = outFactory.createXMLStreamWriter( new FileOutputStream( logFile ), "UTF-8" );
            ExecuteRequest100Writer executer = new ExecuteRequest100Writer( logWriter );
            executer.write100( process.getId(), inputs, responseFormat );
            logWriter.close();
            LOG.debug( "WPS request can be found at " + logFile );

            InputStream is = new FileInputStream( logFile );
            byte[] buffer = new byte[1024];
            int read = 0;
            while ( ( read = is.read( buffer ) ) != -1 ) {
                os.write( buffer, 0, read );
            }
            is.close();
            os.close();
        } else {
            XMLStreamWriter writer = outFactory.createXMLStreamWriter( os );
            ExecuteRequest100Writer executer = new ExecuteRequest100Writer( writer );
            executer.write100( process.getId(), inputs, responseFormat );
            writer.close();
        }

        InputStream responseStream = conn.getInputStream();

        if ( LOG.isDebugEnabled() ) {
            File logFile = File.createTempFile( "wpsclient", "response" );
            OutputStream logStream = new FileOutputStream( logFile );

            byte[] buffer = new byte[1024];
            int read = 0;
            while ( ( read = responseStream.read( buffer ) ) != -1 ) {
                logStream.write( buffer, 0, read );
            }
            logStream.close();

            responseStream = new FileInputStream( logFile );
            LOG.debug( "WPS response can be found at " + logFile );
        }

        String outputContent = conn.getContentType();
        if ( outputContent.startsWith( "text/xml" ) || outputContent.startsWith( "application/xml" ) ) {
            XMLStreamReader reader = inFactory.createXMLStreamReader( responseStream );
            StAXParsingHelper.nextElement( reader );
            if ( OWSExceptionReader.isException( reader ) ) {
                throw OWSExceptionReader.parseException( reader );
            }
        }
        return new ComplexOutput( null, responseStream, outputContent, null, null );
    }
}