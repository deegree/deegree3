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
package org.deegree.protocol.csw.client.getrecords;

import static junit.framework.Assert.assertEquals;
import static org.deegree.metadata.iso.ISORecord.ISO_RECORD_NS;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.util.Iterator;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.deegree.metadata.MetadataRecord;
import org.deegree.metadata.iso.ISORecord;
import org.deegree.protocol.ows.exception.OWSExceptionReport;
import org.deegree.protocol.ows.http.OwsHttpResponseImpl;
import org.deegree.protocol.ows.http.OwsHttpResponseImpl;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Basic tests for {@link GetRecordsResponse}.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
public class GetRecordsResponseTest {

	@Test
	public void testGeoNetworksResponse() throws XMLStreamException, FactoryConfigurationError, OWSExceptionReport {

		OwsHttpResponseImpl mock = Mockito.mock(OwsHttpResponseImpl.class);
		XMLStreamReader xmlStream = getExampleResponseReader("geonetworks_response.invalid");
		Mockito.when(mock.getAsXMLStream()).thenReturn(xmlStream);

		GetRecordsResponse response = new GetRecordsResponse(mock);
		Iterator<MetadataRecord> iter = response.getRecords();
		int i = 0;
		while (iter.hasNext()) {
			assertTrue(xmlStream.isStartElement());
			assertEquals(ISO_RECORD_NS, xmlStream.getNamespaceURI());
			assertEquals("MD_Metadata", xmlStream.getLocalName());
			ISORecord isoRecord = null;
			isoRecord = (ISORecord) iter.next();
			assertNotNull(isoRecord.getIdentifier());
			i++;
		}
		assertEquals(10, i);
	}

	private XMLStreamReader getExampleResponseReader(String name) throws XMLStreamException, FactoryConfigurationError {
		InputStream is = GetRecordsResponseTest.class.getResourceAsStream(name);
		return XMLInputFactory.newInstance().createXMLStreamReader(is);
	}

}
