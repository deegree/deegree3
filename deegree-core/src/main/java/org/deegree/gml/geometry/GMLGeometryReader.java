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
package org.deegree.gml.geometry;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.deegree.commons.xml.XMLParsingException;
import org.deegree.commons.xml.stax.XMLStreamReaderWrapper;
import org.deegree.cs.CRS;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.Geometry;

/**
 * Interface for all version of GML geometry decoders. Any new geometry decoder should implement this interface.
 * 
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 * 
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
public interface GMLGeometryReader {

    /**
     * @param xmlStream
     * @return
     * @throws XMLParsingException
     * @throws XMLStreamException
     * @throws UnknownCRSException
     */
    public Geometry parse( XMLStreamReaderWrapper xmlStream )
                            throws XMLParsingException, XMLStreamException, UnknownCRSException;

    /**
     * @param xmlStream
     * @param defaultCRS
     * @return
     * @throws XMLParsingException
     * @throws XMLStreamException
     * @throws UnknownCRSException
     */
    public Geometry parse( XMLStreamReaderWrapper xmlStream, CRS defaultCRS )
                            throws XMLParsingException, XMLStreamException, UnknownCRSException;

    /**
     * @param xmlStream
     * @param defaultCRS
     * @return
     * @throws XMLParsingException
     * @throws XMLStreamException
     */
    public Envelope parseEnvelope( XMLStreamReaderWrapper xmlStream, CRS defaultCRS )
                            throws XMLParsingException, XMLStreamException;

    /**
     * @param xmlReader
     * @return true if stream's event is an {@link XMLStreamConstants#START_ELEMENT} && the current element's name is a
     *         known geometry.
     */
    public boolean isGeometryElement( XMLStreamReader xmlReader );

    /**
     * @param xmlReader
     * @return true if stream's event is an {@link XMLStreamConstants#START_ELEMENT} && the current element's name is an
     *         envelope.
     */
    public boolean isGeometryOrEnvelopeElement( XMLStreamReader xmlReader );

    /**
     * Parse the current geometry or envelope the given stream is pointing to.
     * 
     * @param xmlStream
     * @return the Geometry (or Envelope) the given stream is pointing to.
     * @throws XMLParsingException
     * @throws XMLStreamException
     * @throws UnknownCRSException
     */
    public Geometry parseGeometryOrEnvelope( XMLStreamReaderWrapper xmlStream, CRS defaultCRS )
                            throws XMLParsingException, XMLStreamException, UnknownCRSException;
}
