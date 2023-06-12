/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2014 by:
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
package org.deegree.gml.geometry;

import static org.deegree.gml.GMLVersion.GML_2;
import static org.deegree.gml.GMLVersion.GML_31;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.commons.xml.XMLParsingException;
import org.deegree.cs.exceptions.TransformationException;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.SFSProfiler;
import org.deegree.geometry.linearization.LinearizationCriterion;
import org.deegree.geometry.linearization.MaxErrorCriterion;
import org.deegree.gml.GMLInputFactory;
import org.deegree.gml.GMLOutputFactory;
import org.deegree.gml.GMLStreamReader;
import org.deegree.gml.GMLStreamWriter;
import org.deegree.junit.XMLAssert;
import org.deegree.junit.XMLMemoryStreamWriter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link Geometry} simplification in {@link GML2GeometryWriter}.
 *
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 * @since 3.4
 */
public class Gml2GeometryWriterSimplificationTest {

	private final static String SCHEMA_LOCATION = "http://schemas.opengis.net/gml/2.1.2/geometry.xsd";

	XMLMemoryStreamWriter memoryWriter;

	GML2GeometryWriter gmlWriterWithoutSimplification;

	GML2GeometryWriter gmlWriterWithSimplification;

	@Before
	public void setup() throws XMLStreamException, FactoryConfigurationError {
		gmlWriterWithoutSimplification = getGmlWriterWithoutSimplification();
		memoryWriter = new XMLMemoryStreamWriter();
		gmlWriterWithSimplification = getGmlWriterWithSimplification();
	}

	private GML2GeometryWriter getGmlWriterWithoutSimplification()
			throws XMLStreamException, FactoryConfigurationError {
		XMLStreamWriter xmlWriter = XMLOutputFactory.newInstance().createXMLStreamWriter(new ByteArrayOutputStream());
		GMLStreamWriter gmlWriter = GMLOutputFactory.createGMLStreamWriter(GML_2, xmlWriter);
		return new GML2GeometryWriter(gmlWriter);
	}

	private GML2GeometryWriter getGmlWriterWithSimplification() throws XMLStreamException {
		XMLStreamWriter writer = memoryWriter.getXMLStreamWriter();
		GMLStreamWriter gmlWriter = GMLOutputFactory.createGMLStreamWriter(GML_2, writer);
		LinearizationCriterion crit = new MaxErrorCriterion(0.1, 1000);
		SFSProfiler simplifier = new SFSProfiler(crit);
		gmlWriter.setGeometrySimplifier(simplifier);
		return new GML2GeometryWriter(gmlWriter);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCurveWithArcsNoSimplification()
			throws XMLParsingException, XMLStreamException, UnknownCRSException, TransformationException {
		Geometry geom = readGml31Geometry("../misc/geometry/Curve.gml");
		gmlWriterWithoutSimplification.export(geom);
	}

	@Test
	public void testCurveWithArcsWithSimplification()
			throws XMLParsingException, XMLStreamException, UnknownCRSException, TransformationException {
		Geometry geom = readGml31Geometry("../misc/geometry/Curve.gml");
		gmlWriterWithSimplification.export(geom);
		XMLAssert.assertValidity(memoryWriter.getReader(), SCHEMA_LOCATION);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSurfaceNoSimplification()
			throws XMLParsingException, XMLStreamException, UnknownCRSException, TransformationException {
		Geometry geom = readGml31Geometry("../misc/geometry/Surface.gml");
		gmlWriterWithoutSimplification.export(geom);
	}

	@Test
	public void testSurfaceWithSimplification()
			throws XMLParsingException, XMLStreamException, UnknownCRSException, TransformationException {
		Geometry geom = readGml31Geometry("../misc/geometry/Surface.gml");
		gmlWriterWithSimplification.export(geom);
		XMLAssert.assertValidity(memoryWriter.getReader(), SCHEMA_LOCATION);
	}

	private Geometry readGml31Geometry(final String resourceName) {
		Geometry geometry = null;
		try {
			final InputStream is = Gml2GeometryWriterSimplificationTest.class.getResourceAsStream(resourceName);
			final XMLStreamReader xmlReader = XMLInputFactory.newInstance().createXMLStreamReader(is);
			final GMLStreamReader gmlReader = GMLInputFactory.createGMLStreamReader(GML_31, xmlReader);
			geometry = gmlReader.readGeometry();
		}
		catch (Exception e) {
			Assert.fail("Creation of geometry failed.");
		}
		return geometry;
	}

}
