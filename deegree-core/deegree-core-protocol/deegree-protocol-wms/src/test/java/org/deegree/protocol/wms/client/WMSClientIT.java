/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2012 by:
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
package org.deegree.protocol.wms.client;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.deegree.commons.ows.exception.OWSException;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.XMLProcessingException;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.cs.persistence.CRSManager;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.primitive.Point;
import org.deegree.geometry.standard.DefaultEnvelope;
import org.deegree.geometry.standard.primitive.DefaultPoint;
import org.deegree.protocol.ows.exception.OWSExceptionReport;
import org.deegree.protocol.wms.ops.GetMap;
import org.junit.Test;

public class WMSClientIT {

	@Test
	public void test130() throws XMLProcessingException, IOException, UnknownCRSException, OWSExceptionReport,
			XMLStreamException, OWSException {

		InputStream is = WMSClientTest.class.getResourceAsStream("wms130-capabilities.xml");
		WMSClient client = new WMSClient(new WMS130CapabilitiesAdapter(new XMLAdapter(is).getRootElement()));
		GetMap getMap = createGetMap();
		InputStream response = client.getMap(getMap);
		assertNotNull(response);
	}

	@Test
	public void test130GetMapEndpointNoQuestionMark() throws XMLProcessingException, IOException, UnknownCRSException,
			OWSExceptionReport, XMLStreamException, OWSException {

		InputStream is = WMSClientTest.class
			.getResourceAsStream("wms130-capabilities-getmap-endpoint-noquestionmark.xml");
		WMSClient client = new WMSClient(new WMS130CapabilitiesAdapter(new XMLAdapter(is).getRootElement()));
		GetMap getMap = createGetMap();
		InputStream response = client.getMap(getMap);
		assertNotNull(response);
	}

	@Test
	public void test130GetMapEndpointExtraParamsEndsWithAmp() throws XMLProcessingException, IOException,
			UnknownCRSException, OWSExceptionReport, XMLStreamException, OWSException {

		InputStream is = WMSClientTest.class
			.getResourceAsStream("wms130-capabilities-getmap-endpoint-extraparams-amp.xml");
		WMSClient client = new WMSClient(new WMS130CapabilitiesAdapter(new XMLAdapter(is).getRootElement()));
		GetMap getMap = createGetMap();
		InputStream response = client.getMap(getMap);
		assertNotNull(response);
	}

	@Test
	public void test130GetMapEndpointExtraParamsNoAmpAtEnd() throws XMLProcessingException, IOException,
			UnknownCRSException, OWSExceptionReport, XMLStreamException, OWSException {

		InputStream is = WMSClientTest.class
			.getResourceAsStream("wms130-capabilities-getmap-endpoint-extra-params-no-amp-at-end.xml");
		WMSClient client = new WMSClient(new WMS130CapabilitiesAdapter(new XMLAdapter(is).getRootElement()));
		GetMap getMap = createGetMap();
		InputStream response = client.getMap(getMap);
		assertNotNull(response);
	}

	private GetMap createGetMap() throws UnknownCRSException {
		List<String> layers = Collections.singletonList("StateBoundary");
		int width = 1896;
		int height = 1344;
		Point min = new DefaultPoint(null, null, null, new double[] { -13576802.349306, 3950355.8690833 });
		Point max = new DefaultPoint(null, null, null, new double[] { -11258008.659664, 5594057.7250333 });
		Envelope envelope = new DefaultEnvelope(min, max);
		ICRS crs = CRSManager.lookup("EPSG:900913");
		String format = "image/png";
		boolean transparent = false;
		return new GetMap(layers, width, height, envelope, crs, format, transparent);
	}

}
