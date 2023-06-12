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
package org.deegree.protocol.wfs.storedquery.xml;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.deegree.protocol.wfs.storedquery.QueryExpressionText;
import org.deegree.protocol.wfs.storedquery.StoredQueryDefinition;
import org.junit.Test;
import org.xmlunit.matchers.CompareMatcher;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.deegree.protocol.wfs.WFSConstants.WFS_200_NS;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.xmlunit.matchers.EvaluateXPathMatcher.hasXPath;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 */
public class StoredQueryDefinition200EncoderTest {

	@Test
	public void testExport() throws Exception {
		String storedQueryResource = "storedQuery.xml";
		StoredQueryDefinition queryDefinition = parseStoredQueryDefinition(storedQueryResource);

		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		XMLStreamWriter writer = XMLOutputFactory.newInstance().createXMLStreamWriter(stream);
		StoredQueryDefinition200Encoder.export(queryDefinition, writer);
		writer.close();

		String actual = stream.toString(UTF_8);
		assertThat(actual, hasXPath("/wfs:StoredQueryDefinition/wfs:QueryExpressionText/wfs:Query/@typeNames",
				is("cp:CadastralParcel"))
			.withNamespaceContext(nsContext()));
		assertThat(actual, hasXPath("/wfs:StoredQueryDefinition/wfs:Parameter/@name", is("label"))
			.withNamespaceContext(nsContext()));
		assertThat(actual,
				CompareMatcher.isSimilarTo(IOUtils.toString(getClass().getResourceAsStream(storedQueryResource), UTF_8))
					.ignoreWhitespace());
	}

	@Test
	public void testExport_ServedFeatureTypes() throws Exception {
		String storedQueryResource = "storedQuery.xml";
		StoredQueryDefinition queryDefinition = parseStoredQueryDefinition(storedQueryResource);
		QueryExpressionText queryExpressionText = queryDefinition.getQueryExpressionTextEls().get(0);
		queryExpressionText.getReturnFeatureTypes().clear();
		queryExpressionText.getReturnFeatureTypes().add(new QName("${deegreewfs:ServedFeatureTypes}"));
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		XMLStreamWriter writer = XMLOutputFactory.newInstance().createXMLStreamWriter(stream);
		StoredQueryDefinition200Encoder.export(queryDefinition, writer);
		writer.close();

		String actual = stream.toString(UTF_8);
		assertThat(actual, hasXPath("/wfs:StoredQueryDefinition/wfs:QueryExpressionText/@returnFeatureTypes",
				is("${deegreewfs:ServedFeatureTypes}"))
			.withNamespaceContext(nsContext()));
	}

	private StoredQueryDefinition parseStoredQueryDefinition(String resource) throws IOException {
		InputStream storedQueryResource = StoredQueryDefinition200EncoderTest.class.getResourceAsStream(resource);
		StoredQueryDefinitionXMLAdapter storedQueryXMLAdapter = new StoredQueryDefinitionXMLAdapter();
		storedQueryXMLAdapter.load(storedQueryResource);
		StoredQueryDefinition queryDefinition = storedQueryXMLAdapter.parse();
		storedQueryResource.close();
		return queryDefinition;
	}

	private Map<String, String> nsContext() {
		return Collections.singletonMap("wfs", WFS_200_NS);
	}

}