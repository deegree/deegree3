/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2014 by:
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
package org.deegree.layer.config;

import static java.util.Collections.singletonList;
import static org.deegree.layer.config.ConfigUtils.parseStyles;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.deegree.commons.utils.Pair;
import org.deegree.layer.persistence.base.jaxb.StyleRefType;
import org.deegree.style.persistence.StyleStore;
import org.deegree.style.persistence.StyleStoreProvider;
import org.deegree.style.se.unevaluated.Style;
import org.deegree.workspace.Workspace;
import org.junit.Test;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 */
public class ConfigUtilsTest {

	@Test
	public void testParseStyles_EqualDefaultAndSimple() throws Exception {
		StyleStore store = mockStyleStoreWithThreeStyles_EqualDefaultAndSimple();
		Workspace workspace = mockWorkspace(store);

		List<StyleRefType> styles = singletonList(readStyleRef("styleRefs_EqualDefaultAndSimple.xml"));

		Pair<Map<String, Style>, Map<String, Style>> selectedStyles = parseStyles(workspace, "layer", styles);

		Map<String, Style> styleMap = selectedStyles.getFirst();
		Map<String, Style> legendStyleMap = selectedStyles.getSecond();

		assertThat(styleMap.size(), is(3));
		assertThat(legendStyleMap.size(), is(3));

		Style defaultLegendStyle = legendStyleMap.get("default");
		assertThat(defaultLegendStyle.getName(), is("default"));
		assertThat(defaultLegendStyle.getLegendURL(), is(new URL("http://test.de/legende.png")));
	}

	@Test
	public void testParseStyles_DifferentDefaultAndSimple() throws Exception {
		StyleStore store = mockStyleStoreWithThreeStyles_DifferentDefaultAndSimple();
		Workspace workspace = mockWorkspace(store);

		List<StyleRefType> styles = singletonList(readStyleRef("styleRefs_DifferentDefaultAndSimple.xml"));

		Pair<Map<String, Style>, Map<String, Style>> selectedStyles = parseStyles(workspace, "layer", styles);

		Map<String, Style> styleMap = selectedStyles.getFirst();
		Map<String, Style> legendStyleMap = selectedStyles.getSecond();

		assertThat(styleMap.size(), is(3));
		assertThat(legendStyleMap.size(), is(3));

		Style defaultLegendStyle = legendStyleMap.get("default");
		assertThat(defaultLegendStyle.getName(), is("default"));
		assertThat(defaultLegendStyle.getLegendURL(), is(new URL("http://test.de/legende2.png")));
	}

	@Test
	public void testParseStyles_OnlyOneWithLegendStyle() throws Exception {
		StyleStore store = mockStyleStoreWithThreeStyles_DifferentDefaultAndSimple();
		Workspace workspace = mockWorkspace(store);

		List<StyleRefType> styles = singletonList(readStyleRef("styleRefs_OnlyOneWithLegendStyle.xml"));

		Pair<Map<String, Style>, Map<String, Style>> selectedStyles = parseStyles(workspace, "layer", styles);

		Map<String, Style> styleMap = selectedStyles.getFirst();
		Map<String, Style> legendStyleMap = selectedStyles.getSecond();

		assertThat(styleMap.size(), is(3));
		assertThat(legendStyleMap.size(), is(2));

		Style defaultLegendStyle = legendStyleMap.get("default");
		assertThat(defaultLegendStyle.getName(), is("legendStyle"));
		assertThat(defaultLegendStyle.getLegendURL(), nullValue());
	}

	@Test
	public void testParseStyless_DefaultWithoutLegendStyle() throws Exception {
		StyleStore store = mockStyleStoreWithThreeStyles_DifferentDefaultAndSimple();
		Workspace workspace = mockWorkspace(store);

		List<StyleRefType> styles = singletonList(readStyleRef("styleRefs_DefaultWithoutLegendStyle.xml"));

		Pair<Map<String, Style>, Map<String, Style>> selectedStyles = parseStyles(workspace, "layer", styles);

		Map<String, Style> styleMap = selectedStyles.getFirst();
		Map<String, Style> legendStyleMap = selectedStyles.getSecond();

		assertThat(styleMap.size(), is(3));
		assertThat(legendStyleMap.size(), is(3));

		Style defaultLegendStyle = legendStyleMap.get("default");
		assertThat(defaultLegendStyle.getName(), is("simpleStyle"));
		assertThat(defaultLegendStyle.getLegendURL(), is(new URL("http://test.de/legende.png")));
	}

	private StyleStore mockStyleStoreWithThreeStyles_EqualDefaultAndSimple() {
		StyleStore mockedStyleStore = mock(StyleStore.class);
		addStyle(mockedStyleStore, "simpleStyle", "layer", "simpleStyle");
		addStyle(mockedStyleStore, "default", "layer", "simpleStyle");
		addStyle(mockedStyleStore, "legendStyle", "layer", "legende");
		return mockedStyleStore;
	}

	private StyleStore mockStyleStoreWithThreeStyles_DifferentDefaultAndSimple() {
		StyleStore mockedStyleStore = mock(StyleStore.class);
		addStyle(mockedStyleStore, "simpleStyle", "layer", "simpleStyle1");
		addStyle(mockedStyleStore, "default", "layer", "simpleStyle2");
		addStyle(mockedStyleStore, "legendStyle", "layer", "legende");
		return mockedStyleStore;
	}

	private void addStyle(StyleStore mockedStyleStore, String styleName, String layerNameRef, String styleNameRef) {
		Style simpleStyle = mockStyle(styleName);
		when(mockedStyleStore.getStyle(layerNameRef, styleNameRef)).thenReturn(simpleStyle);
	}

	private Style mockStyle(String styleName) {
		Style style = new Style();
		Style spiedStyle = spy(style);
		when(spiedStyle.getName()).thenReturn(styleName);
		return spiedStyle;
	}

	private StyleRefType readStyleRef(String name) throws JAXBException {
		JAXBContext jaxbContext = JAXBContext.newInstance(StyleRefType.class);
		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
		InputStream resourceAsStream = ConfigUtilsTest.class.getResourceAsStream(name);
		return (StyleRefType) unmarshaller.unmarshal(new StreamSource(resourceAsStream), StyleRefType.class).getValue();
	}

	private Workspace mockWorkspace(StyleStore store) {
		Workspace mockedWorkspace = mock(Workspace.class);
		when(mockedWorkspace.getResource(StyleStoreProvider.class, "sldStoreId")).thenReturn(store);
		return mockedWorkspace;
	}

}