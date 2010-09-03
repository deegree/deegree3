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

import static org.deegree.commons.xml.CommonNamespaces.XSINS;

import java.util.Iterator;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.serialize.StreamingOMSerializer;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPFault;
import org.apache.axiom.soap.SOAPFaultCode;
import org.apache.axiom.soap.SOAPFaultDetail;
import org.apache.axiom.soap.SOAPFaultReason;
import org.apache.axiom.soap.SOAPFaultSubCode;
import org.apache.axiom.soap.SOAPFaultText;
import org.apache.axiom.soap.SOAPFaultValue;
import org.apache.axiom.soap.SOAPHeader;
import org.apache.axiom.soap.SOAPVersion;
import org.deegree.services.controller.exception.SOAPException;
import org.deegree.services.controller.ows.OWSException;

/**
 * The <code>SoapExceptionSerializer</code> class TODO add class documentation here.
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class SOAPExceptionSerializer extends XMLExceptionSerializer<SOAPException> {

    private SOAPFactory factory;

    private SOAPEnvelope envelope;

    private final XMLExceptionSerializer<OWSException> detailSerializer;

    private SOAPHeader header;

    /**
     * @param version
     * @param header
     * @param factory
     *            initialized to the correct soap version.
     * @param detailSerializer
     *            which is designed to receive an OWSException
     */
    public SOAPExceptionSerializer( SOAPVersion version, SOAPHeader header, SOAPFactory factory,
                                    XMLExceptionSerializer<OWSException> detailSerializer ) {
        this.detailSerializer = detailSerializer;
        this.factory = factory;
        envelope = factory.getDefaultFaultEnvelope();

        this.header = header;

    }

    @Override
    public void serializeExceptionToXML( XMLStreamWriter writer, SOAPException exception )
                            throws XMLStreamException {
        if ( exception == null || writer == null ) {
            return;
        }
        String ns = factory.getNamespace().getNamespaceURI();

        writer.setPrefix( "soapenv", ns );
        writer.setPrefix( "xsi", XSINS );

        writer.writeStartElement( ns, envelope.getLocalName() );
        writer.writeAttribute( XSINS, "schemaLocation",
                               "http://www.w3.org/2003/05/soap-envelope http://www.w3.org/2003/05/soap-envelope" );

        writeAttributes( writer, envelope );
        writeHeader( writer, header );

        SOAPBody body = envelope.getBody();
        writer.writeStartElement( ns, body.getLocalName() );
        writeAttributes( writer, body );

        SOAPFault fault = factory.createSOAPFault( body );
        writer.writeStartElement( ns, fault.getLocalName() );
        writeAttributes( writer, fault );

        SOAPFaultCode code = factory.createSOAPFaultCode( fault );
        writer.writeStartElement( ns, code.getLocalName() );
        writeAttributes( writer, code );

        SOAPFaultValue val = factory.createSOAPFaultValue( code );
        writer.writeStartElement( ns, val.getLocalName() );
        writeAttributes( writer, val );
        writer.writeCharacters( exception.getExceptionCode() );
        writer.writeEndElement(); // value
        writer.writeEndElement(); // code

        String[] subCodes = exception.getSubcodes();
        if ( subCodes != null && subCodes.length > 0 ) {
            for ( String subCode : subCodes ) {
                if ( subCode != null && !"".equals( subCode.trim() ) ) {
                    SOAPFaultSubCode sc = factory.createSOAPFaultSubCode( code );
                    writer.writeStartElement( ns, sc.getLocalName() );
                    writeAttributes( writer, sc );
                    SOAPFaultValue scVal = factory.createSOAPFaultValue( sc );
                    writer.writeStartElement( ns, scVal.getLocalName() );
                    writeAttributes( writer, scVal );
                    writer.writeCharacters( subCode );
                    writer.writeEndElement(); // value
                    writer.writeEndElement(); // code
                }
            }
        }
        SOAPFaultReason reason = factory.createSOAPFaultReason( fault );
        writer.writeStartElement( ns, reason.getLocalName() );
        writeAttributes( writer, reason );

        SOAPFaultText text = factory.createSOAPFaultText( reason );
        writer.writeStartElement( ns, text.getLocalName() );
        writer.writeAttribute( "xml:lang", "en" );
        writeAttributes( writer, text );
        writer.writeCharacters( exception.getReason() );
        writer.writeEndElement(); // text
        writer.writeEndElement(); // reason

        OWSException detail = exception.getDetail();
        if ( detail != null && detailSerializer != null ) {

            SOAPFaultDetail dElement = factory.createSOAPFaultDetail( fault );
            writer.writeStartElement( ns, dElement.getLocalName() );
            writeAttributes( writer, reason );

            detailSerializer.serializeExceptionToXML( writer, detail );

            writer.writeEndElement(); // reason
        }
        writer.writeEndElement(); // fault
        writer.writeEndElement(); // body
        writer.writeEndElement(); // envelope
    }

    private void writeHeader( XMLStreamWriter writer, SOAPHeader header )
                            throws XMLStreamException {
        if ( header == null ) {
            return;
        }
        StreamingOMSerializer ser = new StreamingOMSerializer();
        ser.serialize( header.getXMLStreamReader(), writer );
    }

    private void writeAttributes( XMLStreamWriter writer, OMElement elem )
                            throws XMLStreamException {
        Iterator<?> attribs = elem.getAllAttributes();
        while ( attribs.hasNext() ) {
            writeAttribute( writer, (OMAttribute) attribs.next() );
        }
    }

    private static void writeAttribute( XMLStreamWriter writer, OMAttribute attrib )
                            throws XMLStreamException {
        writer.writeAttribute( attrib.getNamespace().getNamespaceURI(), attrib.getAttributeValue() );
    }
}
