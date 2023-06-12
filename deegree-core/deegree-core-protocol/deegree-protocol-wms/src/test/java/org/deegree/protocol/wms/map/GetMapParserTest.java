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
package org.deegree.protocol.wms.map;

import static java.awt.Color.BLACK;
import static java.awt.Color.WHITE;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.deegree.commons.tom.datetime.DateTime;
import org.deegree.cs.persistence.CRSManager;
import org.deegree.geometry.Envelope;
import org.deegree.layer.LayerRef;
import org.deegree.layer.dims.DimensionInterval;
import org.deegree.protocol.wms.ops.GetMap;
import org.deegree.style.StyleRef;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Test;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 */
public class GetMapParserTest {

	@SuppressWarnings("unchecked")
	@Test
	public void testParse() throws Exception {
		GetMapParser getMapXMLAdapter = new GetMapParser();
		XMLStreamReader xmlStreamReader = createXmlStreamReader("wms-1.3.0-GetMap.xml");
		GetMap getMap = getMapXMLAdapter.parse(xmlStreamReader);

		LinkedList<LayerRef> layers = getMap.getLayers();
		assertThat(layers.size(), is(3));
		assertThat(layers, hasLayerRef("municipalities"));
		assertThat(layers, hasLayerRef("counties"));
		assertThat(layers, hasLayerRef("zipcodes"));

		LinkedList<StyleRef> styles = getMap.getStyles();
		assertThat(styles.size(), is(3));
		assertThat(styles, hasStyleRef("Municipalities"));
		assertThat(styles, hasStyleRef("CountyBoundary"));
		assertThat(styles, hasStyleRef("default"));

		assertThat(getMap.getWidth(), is(1024));
		assertThat(getMap.getHeight(), is(512));
		assertThat(getMap.getFormat(), is("image/png"));
		assertThat(getMap.getTransparent(), is(true));
		assertThat(getMap.getBgColor(), is(BLACK));

		assertThat(getMap.getCoordinateSystem(), is(CRSManager.lookup("EPSG:4326")));
		Envelope boundingBox = getMap.getBoundingBox();
		assertThat(boundingBox.getMin().get0(), is(-115.4));
		assertThat(boundingBox.getMin().get1(), is(35.0));
		assertThat(boundingBox.getMax().get0(), is(-108.0));
		assertThat(boundingBox.getMax().get1(), is(44.0));

		Map<String, String> parameterMap = getMap.getParameterMap();
		assertThat(parameterMap.size(), is(1));
		assertThat(parameterMap.get("EXCEPTIONS"), is("INIMAGE"));

		HashMap<String, List<?>> dimensions = getMap.getDimensions();
		assertThat(dimensions.size(), is(2));

		assertThat(((List<DateTime>) dimensions.get("time")).size(), is(1));
		assertThat(((List<DateTime>) dimensions.get("time")).get(0).getDate(), is(expectedDateTime()));

		assertThat(((List<Double>) dimensions.get("elevation")).size(), is(1));
		assertThat(((List<Double>) dimensions.get("elevation")).get(0), is(5d));
	}

	@Test
	public void testParse_defaultValues() throws Exception {
		GetMapParser getMapXMLAdapter = new GetMapParser();
		XMLStreamReader xmlStreamReader = createXmlStreamReader("wms-1.3.0-GetMap_simple.xml");
		GetMap getMap = getMapXMLAdapter.parse(xmlStreamReader);

		LinkedList<LayerRef> layers = getMap.getLayers();
		assertThat(layers.size(), is(1));
		assertThat(layers, hasLayerRef("municipalities"));

		LinkedList<StyleRef> styles = getMap.getStyles();
		assertThat(styles.size(), is(1));
		assertThat(styles, hasStyleRef("Municipalities"));

		assertThat(getMap.getWidth(), is(10));
		assertThat(getMap.getHeight(), is(50));
		assertThat(getMap.getFormat(), is("image/jpeg"));
		assertThat(getMap.getTransparent(), is(false));
		assertThat(getMap.getBgColor(), is(WHITE));

		assertThat(getMap.getCoordinateSystem(), is(CRSManager.lookup("EPSG:4326")));
		Envelope boundingBox = getMap.getBoundingBox();
		assertThat(boundingBox.getMin().get0(), is(-115.4));
		assertThat(boundingBox.getMin().get1(), is(35.0));
		assertThat(boundingBox.getMax().get0(), is(-108.0));
		assertThat(boundingBox.getMax().get1(), is(44.0));

		Map<String, String> parameterMap = getMap.getParameterMap();
		assertThat(parameterMap.size(), is(1));
		assertThat(parameterMap.get("EXCEPTIONS"), is("XML"));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testParse_elevationValues() throws Exception {
		GetMapParser getMapXMLAdapter = new GetMapParser();
		XMLStreamReader xmlStreamReader = createXmlStreamReader("wms-1.3.0-GetMap_elevationValues.xml");
		GetMap getMap = getMapXMLAdapter.parse(xmlStreamReader);

		HashMap<String, List<?>> dimensions = getMap.getDimensions();
		assertThat(dimensions.size(), is(1));

		List<Double> elevationValues = (List<Double>) dimensions.get("elevation");
		assertThat(elevationValues.size(), is(5));
		assertThat(elevationValues, hasItems(-1.5, -0.5, 0d, 0.5, 1.5));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testParse_elevationInterval() throws Exception {
		GetMapParser getMapXMLAdapter = new GetMapParser();
		XMLStreamReader xmlStreamReader = createXmlStreamReader("wms-1.3.0-GetMap_elevationInterval.xml");
		GetMap getMap = getMapXMLAdapter.parse(xmlStreamReader);

		HashMap<String, List<?>> dimensions = getMap.getDimensions();
		assertThat(dimensions.size(), is(1));

		List<DimensionInterval<Double, Double, Double>> elevationValues = (List<DimensionInterval<Double, Double, Double>>) dimensions
			.get("elevation");
		assertThat(elevationValues.size(), is(1));
		assertThat(elevationValues.get(0).min, is(-5d));
		assertThat(elevationValues.get(0).max, is(5d));
		assertThat(elevationValues.get(0).res, is(0d));
	}

	private XMLStreamReader createXmlStreamReader(String resource)
			throws XMLStreamException, FactoryConfigurationError {
		InputStream getMapResource = GetMapParserTest.class.getResourceAsStream(resource);
		return XMLInputFactory.newInstance().createXMLStreamReader(getMapResource);
	}

	private Date expectedDateTime() {
		Calendar expectedCalendar = Calendar.getInstance();
		expectedCalendar.set(Calendar.YEAR, 2015);
		expectedCalendar.set(Calendar.MONTH, 7);
		expectedCalendar.set(Calendar.DAY_OF_MONTH, 24);
		expectedCalendar.set(Calendar.HOUR_OF_DAY, 9);
		expectedCalendar.set(Calendar.MINUTE, 30);
		expectedCalendar.set(Calendar.SECOND, 0);
		expectedCalendar.set(Calendar.MILLISECOND, 0);
		expectedCalendar.setTimeZone(TimeZone.getTimeZone("UTC"));
		return expectedCalendar.getTime();
	}

	@SuppressWarnings("unchecked")
	private Matcher<LinkedList<LayerRef>> hasLayerRef(final String layer) {
		return new BaseMatcher<LinkedList<LayerRef>>() {

			@Override
			public boolean matches(Object item) {
				LinkedList<LayerRef> layers = (LinkedList<LayerRef>) item;
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

	@SuppressWarnings("unchecked")
	private Matcher<LinkedList<StyleRef>> hasStyleRef(final String style) {
		return new BaseMatcher<LinkedList<StyleRef>>() {

			@Override
			public boolean matches(Object item) {
				LinkedList<StyleRef> styles = (LinkedList<StyleRef>) item;
				for (StyleRef styleRef : styles) {
					if (style.equals(styleRef.getName()))
						return true;
				}
				return false;
			}

			@Override
			public void describeTo(Description description) {
				description.appendText("List should contain a style with name " + style);
			}
		};
	}

}