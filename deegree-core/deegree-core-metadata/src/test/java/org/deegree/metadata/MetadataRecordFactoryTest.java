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
package org.deegree.metadata;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.InputStream;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.deegree.metadata.iso.ISORecord;
import org.junit.Test;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 */
public class MetadataRecordFactoryTest {

	@Test
	public void testCreate() throws Exception {
		XMLStreamReader xmlStream = createStream("metadataRecord.xml");
		MetadataRecord record = MetadataRecordFactory.create(xmlStream);

		assertThat(record, instanceOf(ISORecord.class));
		assertThat(record.getIdentifier(), is("655e5998-a20e-66b5-c888-00005553421"));
	}

	@Test
	public void testCreate_DuplicatedNamespace() throws Exception {
		XMLStreamReader xmlStream = createStream("metadataRecord_namespaceDuplicated.xml");
		MetadataRecord record = MetadataRecordFactory.create(xmlStream);

		assertThat(record, instanceOf(ISORecord.class));
		assertThat(record.getIdentifier(), is("655e5998-a20e-66b5-c888-00005553499"));
	}

	private XMLStreamReader createStream(String name) throws XMLStreamException, FactoryConfigurationError {
		InputStream resourceAsStream = MetadataRecordFactoryTest.class.getResourceAsStream(name);
		XMLStreamReader xmlStream = XMLInputFactory.newInstance().createXMLStreamReader(resourceAsStream);
		xmlStream.nextTag();
		return xmlStream;
	}

}