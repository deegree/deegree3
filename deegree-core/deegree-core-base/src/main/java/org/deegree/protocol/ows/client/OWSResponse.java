//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2011 by:
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
package org.deegree.protocol.ows.client;

import static org.deegree.commons.xml.stax.XMLStreamUtils.skipStartDocument;
import static org.deegree.protocol.ows.exception.OWSExceptionReader.isExceptionReport;
import static org.deegree.protocol.ows.exception.OWSExceptionReader.parseExceptionReport;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.deegree.protocol.ows.exception.OWSExceptionReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:name@company.com">Your Name</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class OWSResponse {

    private static Logger LOG = LoggerFactory.getLogger( OWSResponse.class );

    private static final XMLInputFactory xmlFac = XMLInputFactory.newInstance();

    private final URI uri;

    private final InputStream is;

    public OWSResponse( URI uri, HttpResponse httpResponse ) throws IllegalStateException, IOException {
        this.uri = uri;
        HttpEntity entity = httpResponse.getEntity();
        if ( entity == null ) {
            // TODO exception
        }
        is = entity.getContent();
    }

    public InputStream getBinaryStream() {
        return is;
    }

    public XMLStreamReader getXMLStream()
                            throws OWSExceptionReport, XMLStreamException {
        XMLStreamReader xmlStream = xmlFac.createXMLStreamReader( uri.toString(), is );
        assertNoExceptionReport( xmlStream );
        LOG.debug( "Response root element: " + xmlStream.getName() );
        String version = xmlStream.getAttributeValue( null, "version" );
        LOG.trace( "Response version attribute: " + version );
        return xmlStream;
    }

    private void assertNoExceptionReport( XMLStreamReader xmlStream )
                            throws OWSExceptionReport, XMLStreamException {
        skipStartDocument( xmlStream );
        if ( isExceptionReport( xmlStream.getName() ) ) {
            throw parseExceptionReport( xmlStream );
        }
    }

    public void close()
                            throws IOException {
        is.close();
    }
}
