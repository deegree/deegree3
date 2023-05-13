/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2022 by:
 Department of Geography, University of Bonn
 and
 lat/lon GmbH

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
package org.deegree.geometry.metadata;

import org.deegree.geometry.Envelope;
import org.deegree.geometry.metadata.jaxb.EnvelopeType;
import org.junit.Test;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import java.io.InputStream;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class SpatialMetadataConverterTest {

	@Test
	public void testConvertEnvelopeFromJaxb() throws JAXBException {
		EnvelopeType env = createEnvelopeType();
		Envelope envelope = SpatialMetadataConverter.fromJaxb(env);
		assertThat(envelope.getMin().get(0), is(8.3));
		assertThat(envelope.getMin().get(1), is(53.2));
		assertThat(envelope.getMax().get(0), is(10.4));
		assertThat(envelope.getMax().get(1), is(54.0));

	}

	private EnvelopeType createEnvelopeType() throws JAXBException {
		JAXBContext jc = JAXBContext.newInstance(EnvelopeType.class);
		Unmarshaller unmarshaller = jc.createUnmarshaller();
		InputStream resourceAsStream = getClass().getResourceAsStream("envelope.xml");
		return unmarshaller.unmarshal(new StreamSource(resourceAsStream), EnvelopeType.class).getValue();
	}

}
