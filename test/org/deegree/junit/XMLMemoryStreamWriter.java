//$HeadURL:$
/*----------------    FILE HEADER  ------------------------------------------

 This file is part of deegree.
 Copyright (C) 2001-2008 by:
 EXSE, Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 Contact:

 Andreas Poth
 lat/lon GmbH
 Aennchenstr. 19
 53177 Bonn
 Germany
 E-Mail: poth@lat-lon.de

 Prof. Dr. Klaus Greve
 Department of Geography
 University of Bonn
 Meckenheimer Allee 166
 53115 Bonn
 Germany
 E-Mail: greve@giub.uni-bonn.de

 ---------------------------------------------------------------------------*/
package org.deegree.junit;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import junit.framework.Assert;

/**
 * This class creates a {@link XMLStreamWriter} that writes into a temporary buffer and can create a {@link Reader} on
 * that buffer.
 * 
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
public class XMLMemoryStreamWriter {

    private XMLStreamWriter xmlWriter;

    private StringWriter writer;

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
     * Get a reader for the xml buffer. This will close the {@link XMLStreamWriter}!
     * 
     * @return a {@link Reader} for the buffer
     */
    public Reader getReader() {
        Reader reader = null;
        if ( xmlWriter == null ) {
            Assert.fail( "no XMLStreamWriter found for this MemoryStreamWriter" );
        }
        try {
            xmlWriter.flush();
            xmlWriter.close();
            xmlWriter = null;
            reader = new StringReader( writer.getBuffer().toString() );
            writer.close();
        } catch ( IOException e ) {
            Assert.fail( "error while closing StringWriter: " + e.getMessage() );
        } catch ( XMLStreamException e ) {
            Assert.fail( "error while closing XMLStreamWriter: " + e.getMessage() );
        }
        writer = null;

        return reader;
    }

}
