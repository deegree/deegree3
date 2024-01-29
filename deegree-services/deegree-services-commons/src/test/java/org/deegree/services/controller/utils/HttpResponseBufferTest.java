/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2023 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -
 and
 - grit graphische Informationstechnik Beratungsgesellschaft mbH -

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

 grit graphische Informationstechnik Beratungsgesellschaft mbH
 Landwehrstr. 143, 59368 Werne
 Germany
 https://www.grit.de/

 lat/lon GmbH
 Aennchenstr. 19, 53177 Bonn
 Germany
 https://lat-lon.de/

 Department of Geography, University of Bonn
 Prof. Dr. Klaus Greve
 Postfach 1147, 53001 Bonn
 Germany
 https://www.geographie.uni-bonn.de/deegree/

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.services.controller.utils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import jakarta.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLStreamException;

import org.deegree.commons.utils.io.StreamBufferStore;
import org.junit.Test;
import org.mockito.Mockito;

public class HttpResponseBufferTest {

	@Test
	public void testPrintWriterFlushing() throws IOException {

		HttpServletResponse response = mockHttpRequest();

		HttpResponseBuffer buf = new HttpResponseBuffer(response, null);
		StreamBufferStore store = (StreamBufferStore) buf.getBuffer();
		assertThat(store, is(notNullValue()));
		assertThat(store.size(), is(0));

		buf.getWriter().write("ABC");
		assertThat(store.size(), is(0));

		buf.flushBuffer();
		assertThat(store.size(), is(3));
	}

	@Test
	public void testXMLWriterFlushing() throws IOException, XMLStreamException {

		HttpServletResponse response = mockHttpRequest();

		HttpResponseBuffer buf = new HttpResponseBuffer(response, null);
		StreamBufferStore store = (StreamBufferStore) buf.getBuffer();
		assertThat(store, is(notNullValue()));
		assertThat(store.size(), is(0));

		buf.getXMLWriter(); // will create a start document
		assertThat(store.size(), is(0));

		buf.flushBuffer();
		assertThat(store.size(), is(greaterThan(0)));
	}

	private HttpServletResponse mockHttpRequest() throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();

		HttpServletResponse mock = Mockito.mock(HttpServletResponse.class);
		Mockito.when(mock.getOutputStream()).thenReturn(new HttpResponseBuffer.BufferedServletOutputStream(bos));
		return mock;
	}

}
