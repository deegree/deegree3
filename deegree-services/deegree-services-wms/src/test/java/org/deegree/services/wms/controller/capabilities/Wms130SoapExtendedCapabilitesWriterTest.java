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
package org.deegree.services.wms.controller.capabilities;

import static org.deegree.commons.xml.CommonNamespaces.WMSNS;
import static org.deegree.commons.xml.CommonNamespaces.WMS_PREFIX;
import static org.deegree.commons.xml.CommonNamespaces.XLINK_PREFIX;
import static org.deegree.commons.xml.CommonNamespaces.XLNNS;
import static org.deegree.services.wms.controller.capabilities.Wms130SoapExtendedCapabilitesWriter.SOAPWMS_NS;
import static org.deegree.services.wms.controller.capabilities.Wms130SoapExtendedCapabilitesWriter.SOAPWMS_PREFIX;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.xmlmatchers.XmlMatchers.hasXPath;
import static org.xmlmatchers.transform.XmlConverters.xml;

import java.io.ByteArrayOutputStream;
import java.net.URL;

import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.validation.Schema;

import org.junit.Ignore;
import org.junit.Test;
import org.xml.sax.SAXException;
import org.xmlmatchers.XmlMatchers;
import org.xmlmatchers.namespace.SimpleNamespaceContext;
import org.xmlmatchers.validation.SchemaFactory;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 */
public class Wms130SoapExtendedCapabilitesWriterTest {

    @Test
    public void testWriteSoapWmsExtendedCapabilites_ContainsPostUrl()
                            throws Exception {
        Wms130SoapExtendedCapabilitesWriter writer = new Wms130SoapExtendedCapabilitesWriter();

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        XMLStreamWriter streamWriter = XMLOutputFactory.newInstance().createXMLStreamWriter( stream );
        String postUrl = "http://post.url/soap";
        writer.writeSoapWmsExtendedCapabilites( streamWriter, postUrl );
        streamWriter.close();

        assertThat( xml( stream.toString() ),
                    hasXPath( "//soapwms:ExtendedCapabilities/soapwms:SOAP/wms:OnlineResource/@xlink:href",
                              nsBindings(), equalTo( postUrl ) ) );
    }

    @Ignore("Requires access to referenced schema")
    @Test
    public void testWriteSoapWmsExtendedCapabilites_SchemaValid()
                            throws Exception {
        Wms130SoapExtendedCapabilitesWriter writer = new Wms130SoapExtendedCapabilitesWriter();

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        XMLStreamWriter streamWriter = XMLOutputFactory.newInstance().createXMLStreamWriter( stream );
        writer.writeSoapWmsExtendedCapabilites( streamWriter, "http://post.url/soap" );
        streamWriter.close();

        assertThat( xml( stream.toString() ), XmlMatchers.conformsTo( schema() ) );
    }

    private Schema schema()
                            throws SAXException {
        URL schemaResource = Wms130SoapExtendedCapabilitesWriterTest.class.getResource( "soapwms.xsd" );
        return SchemaFactory.w3cXmlSchemaFrom( schemaResource );
    }

    private NamespaceContext nsBindings() {
        SimpleNamespaceContext simpleNamespaceContext = new SimpleNamespaceContext();
        simpleNamespaceContext.withBinding( SOAPWMS_PREFIX, SOAPWMS_NS );
        simpleNamespaceContext.withBinding( WMS_PREFIX, WMSNS );
        simpleNamespaceContext.withBinding( XLINK_PREFIX, XLNNS );
        return simpleNamespaceContext;
    }

}