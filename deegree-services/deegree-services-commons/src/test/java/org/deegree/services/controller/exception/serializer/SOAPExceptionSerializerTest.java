//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2015 by:
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
package org.deegree.services.controller.exception.serializer;

import java.io.ByteArrayOutputStream;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axiom.soap.impl.llom.soap11.SOAP11Factory;
import org.apache.axiom.soap.impl.llom.soap12.SOAP12Factory;
import org.deegree.commons.ows.exception.OWSException;
import org.deegree.commons.tom.ows.Version;
import org.deegree.commons.utils.kvp.InvalidParameterValueException;
import org.deegree.services.controller.exception.SOAPException;
import org.deegree.services.ows.OWS110ExceptionReportSerializer;
import org.junit.Test;

/**
 * currently it is only tested that no exception occurs
 * 
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 */
public class SOAPExceptionSerializerTest {

    @SuppressWarnings("deprecation")
    @Test
    public void testSerializeExceptionToXML_SOAP11()
                            throws Exception {
        SOAP11Factory factory = new SOAP11Factory();
        XMLExceptionSerializer detailSerializer = new OWS110ExceptionReportSerializer( new Version( 2, 0, 0 ) );
        SOAPExceptionSerializer soapExceptionSerializer = new SOAPExceptionSerializer( factory.getSOAPVersion(), null,
                                                                                       factory, detailSerializer );
        SOAPException soapException = createException();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        XMLStreamWriter writer = XMLOutputFactory.newInstance().createXMLStreamWriter( stream );
        writer.writeStartDocument();
        soapExceptionSerializer.serializeExceptionToXML( writer, soapException );
        writer.writeEndDocument();
        writer.close();
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testSerializeExceptionToXML_SOAP12()
                            throws Exception {
        SOAP12Factory factory = new SOAP12Factory();
        SOAPExceptionSerializer soapExceptionSerializer = createExceptionSerializer( factory );
        SOAPException soapException = createException();

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        XMLStreamWriter writer = XMLOutputFactory.newInstance().createXMLStreamWriter( stream );
        writer.writeStartDocument();
        soapExceptionSerializer.serializeExceptionToXML( writer, soapException );
        writer.writeEndDocument();
        writer.close();
    }

    private SOAPException createException() {
        OWSException owsException = new OWSException( new InvalidParameterValueException( "version" ) );
        return new SOAPException( "reason", "code", owsException );
    }

    private SOAPExceptionSerializer createExceptionSerializer( SOAP12Factory factory ) {
        XMLExceptionSerializer detailSerializer = new OWS110ExceptionReportSerializer( new Version( 2, 0, 0 ) );
        return new SOAPExceptionSerializer( factory.getSOAPVersion(), null, factory, detailSerializer );
    }

}