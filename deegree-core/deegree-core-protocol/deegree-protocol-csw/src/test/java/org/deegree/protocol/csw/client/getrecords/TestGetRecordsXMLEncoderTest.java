/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
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

import static org.deegree.commons.xml.CommonNamespaces.APISO;
import static org.deegree.commons.xml.CommonNamespaces.APISO_PREFIX;
import static org.deegree.filter.MatchAction.ANY;
import static org.deegree.protocol.csw.CSWConstants.ResultType.results;
import static org.deegree.protocol.csw.CSWConstants.ReturnableElement.full;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.Collections;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.deegree.commons.tom.ows.Version;
import org.deegree.commons.tom.primitive.PrimitiveValue;
import org.deegree.commons.xml.CommonNamespaces;
import org.deegree.cs.exceptions.TransformationException;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.filter.Expression;
import org.deegree.filter.Filter;
import org.deegree.filter.Operator;
import org.deegree.filter.OperatorFilter;
import org.deegree.filter.comparison.PropertyIsEqualTo;
import org.deegree.filter.expression.Literal;
import org.deegree.filter.expression.ValueReference;
import org.deegree.protocol.csw.CSWConstants.ResultType;
import org.deegree.protocol.csw.CSWConstants.ReturnableElement;
import org.junit.Assume;
import org.junit.Test;
import org.xml.sax.SAXException;

/**
 * Validates the GetRecords requests.
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 */
public class TestGetRecordsXMLEncoderTest {

	private static final String OUTPUT_FORMAT = "http://www.isotc211.org/2005/gmd";

	private static final String OUTPUT_SCHEMA = "application/xml";

	private static final Version VERSION = new Version(2, 0, 2);

	private static final int START_POSITION = 10;

	private static final int MAX_RECORDS = 15;

	@Test
	public void testExportMin() throws XMLStreamException, FactoryConfigurationError, UnknownCRSException,
			TransformationException, IOException, SAXException {
		GetRecords getRecords = new GetRecords(VERSION, ResultType.results, ReturnableElement.full, null);
		writeAndvalidateGetRecordsRequest(getRecords);
	}

	@Test
	public void testExportMax() throws XMLStreamException, FactoryConfigurationError, UnknownCRSException,
			TransformationException, IOException, SAXException {
		GetRecords getRecords = initGetRecordsWithoutFilter();
		writeAndvalidateGetRecordsRequest(getRecords);
	}

	@Test
	public void testExportFilter() throws XMLStreamException, FactoryConfigurationError, UnknownCRSException,
			TransformationException, IOException, SAXException {
		GetRecords getRecords = initGetRecordsWithFilter();
		writeAndvalidateGetRecordsRequest(getRecords);
	}

	@Test
	public void testExportHopCountWithFilter() throws XMLStreamException, FactoryConfigurationError,
			UnknownCRSException, TransformationException, IOException, SAXException {
		GetRecords getRecords = initGetRecordsWithFilterAndHopCount();
		ByteArrayOutputStream getRecordsAsXml = writeGetRecordsAsXml(getRecords);
		String getRecordsAsXmlString = asString(getRecordsAsXml);
		assertTrue(getRecordsAsXmlString.contains("hopCount=\"2\""));
		validateGetRecordsXml(getRecordsAsXml);
	}

	@Test
	public void testExportHopCountWithoutFilter() throws XMLStreamException, FactoryConfigurationError,
			UnknownCRSException, TransformationException, IOException, SAXException {
		GetRecords getRecords = initGetRecordsWithHopCount();
		ByteArrayOutputStream getRecordsAsXml = writeGetRecordsAsXml(getRecords);
		String getRecordsAsXmlString = asString(getRecordsAsXml);
		assertTrue(getRecordsAsXmlString.contains("hopCount=\"2\""));
		validateGetRecordsXml(getRecordsAsXml);
	}

	private GetRecords initGetRecordsWithoutFilter() {
		return new GetRecords(VERSION, START_POSITION, MAX_RECORDS, OUTPUT_SCHEMA, OUTPUT_FORMAT, createTypeNames(),
				results, full, null);
	}

	private GetRecords initGetRecordsWithFilter() {
		Filter filter = createFilter();
		List<QName> typeNames = createTypeNames();
		return new GetRecords(VERSION, START_POSITION, MAX_RECORDS, OUTPUT_SCHEMA, OUTPUT_FORMAT, typeNames, results,
				full, filter);
	}

	private GetRecords initGetRecordsWithHopCount() {
		List<QName> typeNames = createTypeNames();
		return new GetRecords(VERSION, START_POSITION, MAX_RECORDS, OUTPUT_SCHEMA, OUTPUT_FORMAT, typeNames, results,
				full, null, 2);
	}

	private GetRecords initGetRecordsWithFilterAndHopCount() {
		Filter filter = createFilter();
		List<QName> typeNames = createTypeNames();
		return new GetRecords(VERSION, START_POSITION, MAX_RECORDS, OUTPUT_SCHEMA, OUTPUT_FORMAT, typeNames, results,
				full, filter, 2);
	}

	private Filter createFilter() {
		Expression param1 = new ValueReference(new QName(APISO, "Identifier", APISO_PREFIX));
		Expression param2 = new Literal<PrimitiveValue>("3528635identifer18745");
		Operator rootOperator = new PropertyIsEqualTo(param1, param2, true, ANY);
		return new OperatorFilter(rootOperator);
	}

	private List<QName> createTypeNames() {
		return Collections
			.singletonList(new QName(CommonNamespaces.ISOAP10GMDNS, "MD_Metadata", CommonNamespaces.ISOAP10GMD_PREFIX));
	}

	private void writeAndvalidateGetRecordsRequest(GetRecords getRecords) throws XMLStreamException,
			FactoryConfigurationError, UnknownCRSException, TransformationException, IOException, SAXException {
		ByteArrayOutputStream os = writeGetRecordsAsXml(getRecords);
		validateGetRecordsXml(os);
	}

	private ByteArrayOutputStream writeGetRecordsAsXml(GetRecords getRecords) throws XMLStreamException,
			FactoryConfigurationError, UnknownCRSException, TransformationException, IOException {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		XMLStreamWriter writer = XMLOutputFactory.newInstance().createXMLStreamWriter(os);

		GetRecordsXMLEncoder.export(getRecords, writer);

		writer.close();
		os.close();
		return os;
	}

	private void validateGetRecordsXml(ByteArrayOutputStream os) throws SAXException, IOException {
		InputStream getRecordsRequest = new ByteArrayInputStream(os.toByteArray());
		SchemaFactory factory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
		Validator validator = null;
		Source source = null;
		try {
			URL schemaLocation = new URL("http://schemas.opengis.net/csw/2.0.2/CSW-discovery.xsd");
			Schema schema = factory.newSchema(schemaLocation);
			validator = schema.newValidator();
			source = new StreamSource(getRecordsRequest);
		}
		catch (Exception e) {
			Assume.assumeNoException(e);
			return;
		}
		validator.validate(source);
	}

	private String asString(ByteArrayOutputStream getRecordsAsXml) throws UnsupportedEncodingException {
		return getRecordsAsXml.toString();
	}

}
