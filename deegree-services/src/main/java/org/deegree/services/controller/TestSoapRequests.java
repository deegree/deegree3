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

package org.deegree.services.controller;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.serialize.StreamingOMSerializer;
import org.apache.axiom.om.util.StAXUtils;
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
import org.apache.axiom.soap.impl.llom.soap11.SOAP11Factory;
import org.apache.axiom.soap.impl.llom.soap12.SOAP12Factory;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.stax.FormattingXMLStreamWriter;
import org.deegree.services.controller.ows.OWSException;
import org.deegree.services.controller.ows.OWSException110XMLAdapter;

/**
 * The <code>TestSoapRequests</code> class TODO add class documentation here.
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 *
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 *
 */
public class TestSoapRequests {

    public static void main( String[] args )
                            throws FileNotFoundException, XMLStreamException, UnsupportedEncodingException {
        HttpClient client = new HttpClient();
        File f = new File( System.getProperty( "user.home" ) + "/tra_merc.xml" );
        PostMethod filePost = new PostMethod( "http://localhost:8082/d3_services/services" );
        SOAP11Factory soapFactory = new SOAP11Factory();
        SOAPEnvelope env = soapFactory.getDefaultEnvelope();
        SOAPBody body = env.getBody();

        XMLAdapter someCoolRequest = new XMLAdapter( new File( System.getProperty( "user.home" ) + "/unit.xml" ) );
        body.addChild( someCoolRequest.getRootElement() );
        StreamingOMSerializer ser = new StreamingOMSerializer();
        StringWriter w = new StringWriter();
        XMLStreamWriter wr = new FormattingXMLStreamWriter( StAXUtils.createXMLStreamWriter( w ) );
        // ser.serialize( someCoolRequest.getRootElement().getXMLStreamReader(), wr );
        ser.serialize( env.getXMLStreamReader(), wr );

        //
        System.out.println( w.toString() );
        File f2 = new File( System.getProperty( "user.home" ) + "/create_wpvs_table.sql" );
        Part[] parts = { new StringPart( "param_name", w.toString() ), new FilePart( f2.getName(), f2 ),
                        new FilePart( f.getName(), f ) };
        filePost.setRequestEntity( new MultipartRequestEntity( parts, filePost.getParams() ) );
        // filePost.setRequestEntity( new FileRequestEntity( f, "application/xml" ) );
        // filePost.setRequestEntity( new StringRequestEntity( w.toString(), "application/xml", "UTF-8" ) );
        try {
            int status = client.executeMethod( filePost );
            System.out.println( "Status: " + status );
            System.out.println( filePost.getResponseBodyAsString() );

        } catch ( HttpException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch ( IOException e ) {
            // TODO Auto-generated catch blockOWS_NS, "ExceptionReport"
            e.printStackTrace();
        }
        ser.serialize( env.getXMLStreamReader(), wr );
    }

    protected static void testSOAPVersions()
                            throws XMLStreamException {
        XMLOutputFactory outFac = XMLOutputFactory.newInstance();
        outFac.setProperty( XMLOutputFactory.IS_REPAIRING_NAMESPACES, Boolean.TRUE );
        StringWriter w = new StringWriter();
        XMLStreamWriter writer = new FormattingXMLStreamWriter( outFac.createXMLStreamWriter( w ) );

        SOAPFactory factory = new SOAP12Factory();
        createSoap( factory, writer );
        factory = new SOAP11Factory();
        createSoap( factory, writer );
        System.out.println( w.toString() );
    }

    private static void createSoap( SOAPFactory factory, XMLStreamWriter writer )
                            throws XMLStreamException {

        String ns = factory.getNamespace().getNamespaceURI();
        SOAPEnvelope env = factory.getDefaultEnvelope();

        writer.setPrefix( "soapenv", ns );
        writer.writeStartElement( ns, env.getLocalName() );
        writeAttributes( writer, env );
        SOAPBody body = env.getBody();
        writer.writeStartElement( ns, body.getLocalName() );
        writeAttributes( writer, body );

        SOAPFault fault = factory.createSOAPFault( body );
        writer.writeStartElement( ns, fault.getLocalName() );
        writeAttributes( writer, fault );

        SOAPFaultCode code = factory.createSOAPFaultCode( fault );
        writer.writeStartElement( ns, code.getLocalName() );
        writeAttributes( writer, code );

        SOAPFaultValue val = factory.createSOAPFaultValue( code );
        val.setText( "TEST" );
        writer.writeStartElement( ns, val.getLocalName() );
        writeAttributes( writer, val );
        writer.writeCharacters( "TEST" );
        writer.writeEndElement(); // value
        writer.writeEndElement(); // code

        String[] subCodes = { "sub 1", "sub 2" };
        if ( subCodes != null && subCodes.length > 0 ) {
            for ( String subCode : subCodes ) {
                if ( subCode != null || "".equals( subCode.trim() ) ) {
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
        writeAttributes( writer, text );
        writer.writeCharacters( "a reason" );
        writer.writeEndElement(); // text
        writer.writeEndElement(); // reason

        OWSException detail = new OWSException( "blubber", OWSException.INVALID_PARAMETER_VALUE ); // exception.getDetail();
        if ( detail != null ) {

            SOAPFaultDetail dElement = factory.createSOAPFaultDetail( fault );
            writer.writeStartElement( ns, dElement.getLocalName() );
            writeAttributes( writer, reason );

            OWSException110XMLAdapter adapt = new OWSException110XMLAdapter();
            adapt.serializeExceptionToXML( writer, detail );

            writer.writeEndElement(); // reason
        }
        writer.writeEndElement(); // fault
        writer.writeEndElement(); // body
        writer.writeEndElement(); // envelope
        writer.writeEndDocument(); // document

    }

    private static void writeAttributes( XMLStreamWriter writer, OMElement elem )
                            throws XMLStreamException {
        Iterator attribs = elem.getAllAttributes();
        while ( attribs.hasNext() ) {
            writeAttribute( writer, (OMAttribute) attribs.next() );
        }
    }

    private static void writeAttribute( XMLStreamWriter writer, OMAttribute attrib )
                            throws XMLStreamException {
        writer.writeAttribute( attrib.getNamespace().getNamespaceURI(), attrib.getAttributeValue() );
    }
}
