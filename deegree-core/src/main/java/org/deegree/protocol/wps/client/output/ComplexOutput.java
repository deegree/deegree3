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
package org.deegree.protocol.wps.client.output;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.deegree.commons.tom.ows.CodeType;
import org.deegree.commons.utils.io.StreamBufferStore;
import org.deegree.commons.xml.stax.StAXParsingHelper;
import org.deegree.protocol.wps.client.param.ComplexFormat;

/**
 * {@link ExecutionOutput} that encapsulates an XML or a binary value.
 * 
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class ComplexOutput extends ExecutionOutput {

    private final ComplexFormat complexAttribs;

    private final URL url;

    private final StreamBufferStore store;

    /**
     * Creates a new {@link ComplexOutput} instance.
     * 
     * @param id
     *            output parameter identifier, must not be <code>null</code>
     * @param url
     *            web-accessible URL for accessing the resource, must not be <code>null</code>
     * @param mimeType
     *            mime type of the complex data, can be <code>null</code> (unspecified)
     * @param encoding
     *            encoding of the complex data, can be <code>null</code> (unspecified)
     * @param schema
     *            XML schema of the complex data, can be <code>null</code> (unspecified)
     */
    public ComplexOutput( CodeType id, URL url, String mimeType, String encoding, String schema ) {
        super( id );
        this.url = url;
        this.store = null;
        this.complexAttribs = new ComplexFormat( mimeType, null, schema );
    }

    /**
     * Creates a new {@link ComplexOutput} instance.
     * 
     * @param id
     *            output parameter identifier, must not be <code>null</code>
     * @param store
     *            stream that holds the complex data, must not be <code>null</code>
     * @param mimeType
     *            mime type of the complex data, can be <code>null</code> (unspecified)
     * @param encoding
     *            encoding of the complex data, can be <code>null</code> (unspecified)
     * @param schema
     *            XML schema of the complex data, can be <code>null</code> (unspecified)
     */
    public ComplexOutput( CodeType id, StreamBufferStore store, String mimeType, String encoding, String schema ) {
        super( id );
        this.url = null;
        this.store = store;
        this.complexAttribs = new ComplexFormat( mimeType, encoding, schema );
    }

    /**
     * Creates a new {@link ComplexOutput} instance.
     * 
     * @param id
     *            output parameter identifier, must not be <code>null</code>
     * @param is
     *            input stream to the complex data, must not be <code>null</code>
     * @param mimeType
     *            mime type of the complex data, can be <code>null</code> (unspecified)
     * @param encoding
     *            encoding of the complex data, can be <code>null</code> (unspecified)
     * @param schema
     *            XML schema of the complex data, can be <code>null</code> (unspecified)
     * @throws IOException
     */
    public ComplexOutput( CodeType id, InputStream is, String mimeType, String encoding, String schema )
                            throws IOException {
        super( id );
        store = new StreamBufferStore();
        byte[] b = new byte[1024];
        int read = -1;
        while ( ( read = is.read( b ) ) != -1 ) {
            store.write( b, 0, read );
        }
        store.close();
        is.close();
        this.complexAttribs = new ComplexFormat( mimeType, encoding, schema );
        this.url = null;
    }

    /**
     * Returns the format of the output.
     * 
     * @return the format of the output, never <code>null</code>
     */
    public ComplexFormat getFormat() {
        return complexAttribs;
    }

    /**
     * Returns the web-accessible URL for the complex data (as provided by the process).
     * <p>
     * This method is only applicable if the parameter has been requested as reference.
     * </p>
     * 
     * @return the web-accessible URL, or <code>null</code> if the parameter has been returned in the response document
     *         or raw
     */
    public URL getWebAccessibleURL() {
        return url;
    }

    /**
     * Returns an {@link XMLStreamReader} for accessing the complex value as an XML event stream.
     * <p>
     * NOTE: Never use this method if the input parameter is a binary value -- use {@link #getAsBinaryStream()} instead.
     * </p>
     * 
     * @return an {@link XMLStreamReader} instance, positioned after the START_DOCUMENT element
     * @throws IOException
     *             if accessing the value fails
     * @throws XMLStreamException
     */
    public XMLStreamReader getAsXMLStream()
                            throws XMLStreamException, IOException {
        XMLStreamReader xmlReader = null;
        XMLInputFactory inFactory = XMLInputFactory.newInstance();
        if ( url != null ) {
            xmlReader = inFactory.createXMLStreamReader( url.openStream() );
        } else {
            xmlReader = inFactory.createXMLStreamReader( store.getInputStream() );
        }
        StAXParsingHelper.skipStartDocument( xmlReader );
        return xmlReader;
    }

    /**
     * Returns an {@link InputStream} for accessing the complex value as a binary stream.
     * <p>
     * NOTE: Don't use this method if the input parameter is encoded in XML -- use {@link #getAsXMLStream()} instead.
     * Otherwise erroneous behaviour has to be expected (e.g. if the input value is given embedded in the execute
     * request document).
     * </p>
     * The returned stream will point at the first START_ELEMENT event of the data.
     * 
     * @return the input value as an XML event stream, current event is START_ELEMENT (the root element of the data
     *         object)
     * @throws IOException
     *             if accessing the value fails
     */
    public InputStream getAsBinaryStream()
                            throws IOException {
        InputStream is = null;
        if ( url != null ) {
            is = url.openStream();
        } else {
            is = store.getInputStream();
        }
        return is;
    }
}