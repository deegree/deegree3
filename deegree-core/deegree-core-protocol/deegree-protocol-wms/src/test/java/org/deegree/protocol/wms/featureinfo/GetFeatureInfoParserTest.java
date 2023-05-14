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
package org.deegree.protocol.wms.featureinfo;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.deegree.cs.persistence.CRSManager;
import org.deegree.geometry.Envelope;
import org.deegree.layer.LayerRef;
import org.deegree.protocol.wms.ops.GetFeatureInfo;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Test;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 */
public class GetFeatureInfoParserTest {

	@Test
	public void testParse() throws Exception {
		GetFeatureInfoParser parser = new GetFeatureInfoParser();
		XMLStreamReader xmlStreamReader = createXmlStreamReader("wms-1.3.0-GetFeatureInfo.xml");
		GetFeatureInfo getFeatureInfo = parser.parse(xmlStreamReader);

		assertThat(getFeatureInfo.getWidth(), is(1024));
		assertThat(getFeatureInfo.getHeight(), is(512));

		assertThat(getFeatureInfo.getCoordinateSystem(), is(CRSManager.lookup("EPSG:4326")));
		Envelope boundingBox = getFeatureInfo.getEnvelope();
		assertThat(boundingBox.getMin().get0(), is(-115.4));
		assertThat(boundingBox.getMin().get1(), is(35.0));
		assertThat(boundingBox.getMax().get0(), is(-108.0));
		assertThat(boundingBox.getMax().get1(), is(44.0));

		HashMap<String, List<?>> dimensions = getFeatureInfo.getDimensions();
		assertThat(dimensions.size(), is(0));

		Map<String, String> parameterMap = getFeatureInfo.getParameterMap();
		assertThat(parameterMap.size(), is(1));
		assertThat(parameterMap.get("EXCEPTIONS"), is("XML"));

		assertThat(getFeatureInfo.getX(), is(5));
		assertThat(getFeatureInfo.getY(), is(10));
		assertThat(getFeatureInfo.getFeatureCount(), is(42));
		assertThat(getFeatureInfo.getInfoFormat(), is("text/html"));
		assertThat(getFeatureInfo.getQueryLayers().size(), is(2));
		assertThat(getFeatureInfo.getQueryLayers(), hasLayerRef("counties"));
		assertThat(getFeatureInfo.getQueryLayers(), hasLayerRef("municipalities"));
	}

	@Test
	public void testParse_defaultValues() throws Exception {
		GetFeatureInfoParser parser = new GetFeatureInfoParser();
		XMLStreamReader xmlStreamReader = createXmlStreamReader("wms-1.3.0-GetFeatureInfo_simple.xml");
		GetFeatureInfo getFeatureInfo = parser.parse(xmlStreamReader);

		Map<String, String> parameterMap = getFeatureInfo.getParameterMap();
		assertThat(parameterMap.size(), is(0));

		assertThat(getFeatureInfo.getX(), is(50));
		assertThat(getFeatureInfo.getY(), is(15));
		assertThat(getFeatureInfo.getFeatureCount(), is(1));
		assertThat(getFeatureInfo.getInfoFormat(), is("text/xml"));
		assertThat(getFeatureInfo.getQueryLayers().size(), is(1));
		assertThat(getFeatureInfo.getQueryLayers(), hasLayerRef("counties"));
	}

	private XMLStreamReader createXmlStreamReader(String resource)
			throws XMLStreamException, FactoryConfigurationError {
		InputStream getMapResource = GetFeatureInfoParserTest.class.getResourceAsStream(resource);
		return XMLInputFactory.newInstance().createXMLStreamReader(getMapResource);
	}

	@SuppressWarnings("unchecked")
	private Matcher<List<LayerRef>> hasLayerRef(final String layer) {
		return new BaseMatcher<List<LayerRef>>() {

			@Override
			public boolean matches(Object item) {
				List<LayerRef> layers = (List<LayerRef>) item;
				for (LayerRef layerRef : layers) {
					if (layer.equals(layerRef.getName()))
						return true;
				}
				return false;
			}

			@Override
			public void describeTo(Description description) {
				description.appendText("List should contain a layer with name " + layer);
			}
		};
	}

}
