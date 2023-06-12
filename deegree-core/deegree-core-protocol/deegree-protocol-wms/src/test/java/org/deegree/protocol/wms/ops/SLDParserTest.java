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
package org.deegree.protocol.wms.ops;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.deegree.commons.utils.Pair;
import org.deegree.commons.utils.Triple;
import org.deegree.filter.OperatorFilter;
import org.deegree.layer.LayerRef;
import org.deegree.protocol.wms.sld.StyleContainer;
import org.deegree.protocol.wms.sld.StylesContainer;
import org.deegree.style.StyleRef;
import org.junit.Test;

/**
 * TODO add class documentation here
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 */
public class SLDParserTest {

	@Test
	public void testParse() throws Exception {
		XMLStreamReader in = retrieveSldAsStream("example-sld.xml");
		RequestBase gm = mockRequestBase();
		Triple<LinkedList<LayerRef>, LinkedList<StyleRef>, LinkedList<OperatorFilter>> sld = SLDParser.parse(in, gm);

		LinkedList<LayerRef> layerRefs = sld.first;
		assertThat(layerRefs.size(), is(1));
		assertThat(layerRefs.get(0).getName(), is("OCEANSEA_1M:Foundation"));

		LinkedList<StyleRef> styleRefs = sld.second;
		assertThat(styleRefs.size(), is(1));
		assertThat(styleRefs.get(0).getStyle().getFeatureType(), is(new QName("Foundation")));
		assertThat(styleRefs.get(0).getStyle().getRules().size(), is(1));

		LinkedList<OperatorFilter> operatorFilters = sld.third;
		assertThat(operatorFilters.size(), is(1));
		assertThat(operatorFilters.get(0), is(nullValue()));
	}

	@Test
	public void testParse_StyleInformations() throws Exception {
		XMLStreamReader in = retrieveSldAsStream("example-sld.xml");
		StylesContainer sld = SLDParser.parse(in);
		List<Pair<String, List<?>>> dimensions = sld.getDimensions();
		assertThat(dimensions.size(), is(0));

		List<StyleContainer> styles = sld.getStyles();
		assertThat(styles.size(), is(1));

		StyleContainer styleInformation = styles.get(0);

		LayerRef layerRefs = styleInformation.getLayerRef();
		assertThat(layerRefs.getName(), is("OCEANSEA_1M:Foundation"));

		StyleRef layerRef = styleInformation.getStyleRef();
		assertThat(layerRef.getStyle().getFeatureType(), is(new QName("Foundation")));
		assertThat(layerRef.getStyle().getRules().size(), is(1));

		OperatorFilter filter = styleInformation.getFilter();
		assertThat(filter, is(nullValue()));
	}

	private XMLStreamReader retrieveSldAsStream(String resource) throws XMLStreamException, FactoryConfigurationError {
		InputStream resourceAsStream = SLDParserTest.class.getResourceAsStream(resource);
		return XMLInputFactory.newInstance().createXMLStreamReader(resourceAsStream);
	}

	private RequestBase mockRequestBase() {
		return mock(RequestBase.class);
	}

}
