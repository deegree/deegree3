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
package org.deegree.junit;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import junit.framework.Assert;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class creates a {@link XMLStreamWriter} that writes into a temporary buffer and can create a {@link Reader} on
 * that buffer. The {@link #toString()} method allows to print the content conveniently.
 * <p>
 * NOTE: This class is not thread-safe!
 * </p>
 * 
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class XMLMemoryStreamWriter {

    private static final Logger LOG = LoggerFactory.getLogger( XMLMemoryStreamWriter.class );

    private XMLStreamWriter xmlWriter;

    private StringWriter writer;

    private Reader reader;

    /**
     * Create a {@link XMLStreamWriter} that writes into a buffer. Call {@link #getReader()} to close the writer and get
     * a {@link Reader} on the buffer.
     * 
     * @return the XMLStreamWriter
     */
    public XMLStreamWriter getXMLStreamWriter() {
        if ( xmlWriter == null ) {
            writer = new StringWriter();
            XMLOutputFactory factory = XMLOutputFactory.newInstance();
            factory.setProperty( "javax.xml.stream.isRepairingNamespaces", Boolean.TRUE );
            try {
                xmlWriter = factory.createXMLStreamWriter( writer );
            } catch ( XMLStreamException e ) {
                Assert.fail( "error while creating the xml writer: " + e.getMessage() );
            }
        }
        return xmlWriter;
    }

    /**
     * Get a reader for the xml buffer. This will close the {@link XMLStreamWriter}.
     * 
     * @return a {@link Reader} for the buffer
     */
    public Reader getReader() {
        if ( xmlWriter != null ) {
            try {
                xmlWriter.flush();
                xmlWriter.close();
                xmlWriter = null;
                writer.close();
            } catch ( IOException e ) {
                throw new RuntimeException( "error while closing StringWriter: " + e.getMessage() );
            } catch ( XMLStreamException e ) {
                throw new RuntimeException( "error while closing XMLStreamWriter: " + e.getMessage() );
            }
        }
        return new StringReader( writer.getBuffer().toString() );
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        BufferedReader reader = new BufferedReader( getReader() );
        String line = null;
        try {
            while ( ( line = reader.readLine() ) != null ) {
                sb.append( line );
                sb.append( '\n' );
            }
        } catch ( IOException e ) {
            LOG.info( e.getMessage() );
        }
        return sb.toString();
    }
}
