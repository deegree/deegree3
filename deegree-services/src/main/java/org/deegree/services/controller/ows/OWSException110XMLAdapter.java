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
package org.deegree.services.controller.ows;

import static org.deegree.commons.xml.CommonNamespaces.XSINS;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.services.controller.exception.serializer.XMLExceptionSerializer;

/**
 * {@link XMLExceptionSerializer} for OWS Commons 1.1.0 ExceptionReports.
 *
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class OWSException110XMLAdapter extends XMLExceptionSerializer<OWSException> {

    private static final String OWS_NS = "http://www.opengis.net/ows/1.1";

    private static final String OWS_SCHEMA = "http://schemas.opengis.net/ows/1.1.0/owsExceptionReport.xsd";

    /**
     * Export an ExceptionReport to the ows 1.1.0 format.
     */
    @Override
    public void serializeExceptionToXML( XMLStreamWriter writer, OWSException ex )
                            throws XMLStreamException {
        if ( ex == null || writer == null ) {
            return;
        }
        writer.setPrefix( "ows", OWS_NS );
        writer.setPrefix( "xsi", XSINS );
        writer.writeStartElement( OWS_NS, "ExceptionReport" );
        writer.writeAttribute( XSINS, "schemaLocation", OWS_NS + " " + OWS_SCHEMA );
        writer.writeAttribute( "version", "1.1.0" );
        writer.writeStartElement( OWS_NS, "Exception" );
        writer.writeAttribute( "exceptionCode", ex.getExceptionCode() );
        if ( ex.getLocator() != null && !"".equals( ex.getLocator().trim() ) ) {
            writer.writeAttribute( "locator", ex.getLocator() );
        }
        writer.writeStartElement( OWS_NS, "ExceptionText" );
        writer.writeCharacters( ex.getMessage() );
        writer.writeEndElement();
        writer.writeEndElement(); // Exception
        writer.writeEndElement(); // ExceptionReport
    }
}
