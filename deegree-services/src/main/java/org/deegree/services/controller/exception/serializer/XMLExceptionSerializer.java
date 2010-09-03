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

package org.deegree.services.controller.exception.serializer;

import java.io.IOException;
import java.io.OutputStream;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.stax.FormattingXMLStreamWriter;
import org.deegree.services.controller.exception.ControllerException;

/**
 * The <code>XMLExceptionSerializer</code> class TODO add class documentation here.
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 *
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 * @param <T>
 *            the exception which will be serialized, a subtype of {@link ControllerException}
 *
 */
public abstract class XMLExceptionSerializer<T extends ControllerException> extends XMLAdapter implements
                                                                                              ExceptionSerializer<T> {

    /**
     * Wraps a {@link FormattingXMLStreamWriter} around the given output stream and calls
     * {@link #serializeExceptionToXML(XMLStreamWriter, ControllerException)}. The writer will prepare namespaces and
     * will start and end the XML document.
     *
     * @param outputStream
     *            which will be wrapped.
     * @param exception
     *            which must be serialized
     */
    @Override
    public final void serializeException( OutputStream outputStream, T exception, String requestedEncoding )
                            throws IOException {
        XMLOutputFactory factory = XMLOutputFactory.newInstance();
        factory.setProperty( XMLOutputFactory.IS_REPAIRING_NAMESPACES, Boolean.TRUE );
        try {
            FormattingXMLStreamWriter xmlWriter = new FormattingXMLStreamWriter(
                                                                                 factory.createXMLStreamWriter(
                                                                                                                outputStream,
                                                                                                                requestedEncoding ) );
            xmlWriter.writeStartDocument("UTF-8", "1.0");
            serializeExceptionToXML( xmlWriter, exception );
            xmlWriter.writeEndDocument();
        } catch ( XMLStreamException e ) {
            throw new IOException( e );
        }
    }

    /**
     * Implementations can use the xml writer to serialize the given exception as a specific xml representation.
     *
     * @param writer
     *            a formatting xml writer, wrapped around an output stream.
     * @param exception
     *            to serialize
     * @throws XMLStreamException
     *             if an error occurred while serializing the given exception.
     */
    public abstract void serializeExceptionToXML( XMLStreamWriter writer, T exception )
                            throws XMLStreamException;

}
