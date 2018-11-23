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
package org.deegree.gml;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.io.IOUtils;
import org.deegree.commons.proxy.ProxySettings;
import org.deegree.commons.tom.gml.GMLObject;
import org.deegree.commons.xml.stax.XMLStreamReaderWrapper;

/**
 * Factory for creating {@link GMLStreamReader} instances.
 * 
 * @see GMLObject
 * @see GMLStreamReader
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class GMLInputFactory {

    /**
     * Creates a new {@link GMLStreamReader} instance for reading GML of the specified version.
     * 
     * @param version
     *            GML version, must not be <code>null</code>
     * @param xmlStream
     *            XML stream used to read the input, must not be <code>null</code> and either point to a
     *            <code>START_DOCUMENT</code> event or a <code>START_ELEMENT</code> event of a GML object element
     * @return initialized {@link GMLStreamReader}
     * @throws XMLStreamException
     */
    public static GMLStreamReader createGMLStreamReader( GMLVersion version, XMLStreamReader xmlStream )
                            throws XMLStreamException {
        // TODO remove this (get rid of deprecated GML3GeometryReader constructor first)
        if (xmlStream == null) {
            return new GMLStreamReader( version, null );
        }
        if ( xmlStream.getEventType() == XMLStreamConstants.START_DOCUMENT ) {
            xmlStream.nextTag();
        }
        if ( xmlStream instanceof XMLStreamReaderWrapper ) {
            return new GMLStreamReader( version, (XMLStreamReaderWrapper) xmlStream );
        }
        return new GMLStreamReader( version, new XMLStreamReaderWrapper( xmlStream, null ) );
    }

    /**
     * Creates a new {@link GMLStreamReader} instance for reading GML of the specified version.
     * 
     * @param version
     *            GML version, must not be <code>null</code>
     * @param url
     *            URL used to read the input, must not be <code>null</code>
     * @return initialized {@link GMLStreamReader}
     * @throws IOException
     * @throws FactoryConfigurationError
     * @throws XMLStreamException
     */
    public static GMLStreamReader createGMLStreamReader( GMLVersion version, URL url )
                            throws XMLStreamException, FactoryConfigurationError, IOException {

        URLConnection conn = ProxySettings.openURLConnection( url );
        InputStream is = conn.getInputStream();
        try {
            XMLStreamReader xmlStream = XMLInputFactory.newInstance().createXMLStreamReader( is );
            // skip START_DOCUMENT event
            xmlStream.nextTag();
            return new GMLStreamReader( version, new XMLStreamReaderWrapper( xmlStream, url.toString() ) );
        } catch ( XMLStreamException | FactoryConfigurationError e ) {
            IOUtils.closeQuietly( is );
            throw e;
        }
    }
}