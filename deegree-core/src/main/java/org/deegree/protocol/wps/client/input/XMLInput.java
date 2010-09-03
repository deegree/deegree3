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
package org.deegree.protocol.wps.client.input;

import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

import java.io.IOException;
import java.net.URL;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.deegree.commons.tom.ows.CodeType;
import org.deegree.commons.xml.stax.StAXParsingHelper;
import org.deegree.protocol.wps.client.param.ComplexFormat;

/**
 * {@link ExecutionInput} that encapsulates an XML value.
 * 
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class XMLInput extends ExecutionInput {

    private final ComplexFormat complexAttribs;

    private URL url;

    private XMLStreamReader reader;

    private boolean isWebAccessible;

    /**
     * Creates a new {@link XMLInput} instance.
     * 
     * @param id
     *            parameter identifier, must not be <code>null</code>
     * @param url
     *            URL for accessing the XML resource, must not be <code>null</code>
     * @param isWebAccessible
     *            if true, the data will be submitted to the process as reference, otherwise it will be encoded in the
     *            request
     * @param mimeType
     *            mime type of the XML resource, may be <code>null</code> (unspecified)
     * @param encoding
     *            encoding, may be <code>null</code> (unspecified)
     * @param schema
     *            XML schema, may be <code>null</code> (unspecified)
     */
    public XMLInput( CodeType id, URL url, boolean isWebAccessible, String mimeType, String encoding, String schema ) {
        super( id );
        this.url = url;
        this.isWebAccessible = isWebAccessible;
        this.complexAttribs = new ComplexFormat( mimeType, null, schema );
    }

    /**
     * Creates a new {@link XMLInput} instance.
     * 
     * @param id
     *            parameter identifier, must not be <code>null</code>
     * @param reader
     *            xml stream that provides the data, must not be <code>null</code> and point to a START_ELEMENT event
     * @param mimeType
     *            mime type of the XML resource, may be <code>null</code> (unspecified)
     * @param encoding
     *            encoding, may be <code>null</code> (unspecified)
     * @param schema
     *            XML schema, may be <code>null</code> (unspecified)
     */
    public XMLInput( CodeType id, XMLStreamReader reader, String mimeType, String encoding, String schema ) {
        super( id );
        if ( reader.getEventType() != START_ELEMENT ) {
            String msg = "The given XML stream does not point to a START_ELEMENT event.";
            throw new IllegalArgumentException( msg );
        }
        this.reader = reader;
        this.isWebAccessible = false;
        this.complexAttribs = new ComplexFormat( mimeType, encoding, schema );
    }

    /**
     * Returns the format of the input.
     * 
     * @return the format of the input, never <code>null</code>
     */
    public ComplexFormat getFormat() {
        return complexAttribs;
    }

    /**
     * Returns the XML value as an {@link XMLStreamReader}.
     * 
     * @return an xml stream, current event is START_ELEMENT
     * @throws IOException
     *             if accessing the value fails
     * @throws XMLStreamException
     */
    public XMLStreamReader getAsXMLStream()
                            throws XMLStreamException, IOException {
        if ( reader == null ) {
            XMLInputFactory inFactory = XMLInputFactory.newInstance();
            reader = inFactory.createXMLStreamReader( url.openStream() );
        }
        if ( reader.getEventType() == XMLStreamConstants.START_DOCUMENT ) {
            StAXParsingHelper.nextElement( reader );
        }
        return reader;
    }

    @Override
    public URL getWebAccessibleURL() {
        return isWebAccessible ? url : null;
    }
}
