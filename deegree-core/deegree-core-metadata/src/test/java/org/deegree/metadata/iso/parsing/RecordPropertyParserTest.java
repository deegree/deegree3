/*----------------------------------------------------------------------------
This file is part of deegree, http://deegree.org/
Copyright (C) 2001-2012 by:
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
package org.deegree.metadata.iso.parsing;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.xml.stax.XMLStreamUtils;
import org.deegree.metadata.MetadataRecord;
import org.deegree.metadata.MetadataRecordFactory;
import org.junit.Test;

/**
 * Basic tests for usage of {@link RecordPropertyParser}.
 *
 * @author <a href="mailto:stenger@lat-lon.de">Dirk Stenger</a>
 */
public class RecordPropertyParserTest {

	private final String metadataEmptyDateFile = "metadataEmptyDate.xml";

	@Test
	public void testRecordPropertyParserWithEmptyDate() throws IOException, XMLStreamException {
		RecordPropertyParser recordPropertyParser = new RecordPropertyParser(createOmElement(metadataEmptyDateFile));
		recordPropertyParser.parse();
	}

	private OMElement createOmElement(String inputFile) throws IOException, XMLStreamException {
		MetadataRecord record = MetadataRecordFactory.create(createXmlStream(inputFile));
		return record.getAsOMElement();
	}

	private XMLStreamReader createXmlStream(String inputFile) throws IOException, XMLStreamException {
		InputStream inputStream = RecordPropertyParserTest.class.getResourceAsStream(inputFile);
		XMLStreamReader xmlStream = XMLInputFactory.newInstance().createXMLStreamReader(inputStream);
		XMLStreamUtils.skipStartDocument(xmlStream);
		XMLStreamUtils.skipToRequiredElement(xmlStream, new QName("http://www.isotc211.org/2005/gmd", "MD_Metadata"));
		return xmlStream;
	}

}
