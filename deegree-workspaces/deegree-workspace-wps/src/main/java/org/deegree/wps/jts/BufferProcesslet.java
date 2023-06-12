/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
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

package org.deegree.wps.jts;

import static org.deegree.commons.xml.CommonNamespaces.GMLNS;
import static org.deegree.gml.GMLVersion.GML_31;

import java.math.BigDecimal;
import java.util.Map;

import javax.xml.stream.XMLStreamReader;

import org.deegree.commons.uom.Measure;
import org.deegree.commons.xml.stax.SchemaLocationXMLStreamWriter;
import org.deegree.geometry.Geometry;
import org.deegree.gml.GMLInputFactory;
import org.deegree.gml.GMLOutputFactory;
import org.deegree.gml.GMLStreamReader;
import org.deegree.gml.GMLStreamWriter;
import org.deegree.services.wps.Processlet;
import org.deegree.services.wps.ProcessletException;
import org.deegree.services.wps.ProcessletExecutionInfo;
import org.deegree.services.wps.ProcessletInputs;
import org.deegree.services.wps.ProcessletOutputs;
import org.deegree.services.wps.input.ComplexInput;
import org.deegree.services.wps.input.LiteralInput;
import org.deegree.services.wps.output.ComplexOutput;

/**
 * TODO add class documentation here
 *
 * @author <a href="mailto:name@deegree.org">Your Name</a>
 */
public class BufferProcesslet implements Processlet, GeometryHandler {

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

	@Override
	public void init() {
		// TODO Auto-generated method stub

	}

	@Override
	public void process(ProcessletInputs in, ProcessletOutputs out, ProcessletExecutionInfo info)
			throws ProcessletException {
		double bufferDistance = Double.parseDouble(((LiteralInput) in.getParameter("BufferDistance")).getValue());
		ComplexInput gmlInputGeometry = (ComplexInput) in.getParameter("GMLInput");

		Geometry geom = null;
		Geometry bufferedGeom = null;
		try {
			XMLStreamReader xmlReader = gmlInputGeometry.getValueAsXMLStream();
			GMLStreamReader gmlReader = GMLInputFactory.createGMLStreamReader(GML_31, xmlReader);
			geom = gmlReader.readGeometry();

			bufferedGeom = geom.getBuffer(new Measure(new BigDecimal(bufferDistance), "unity"));
		}
		catch (Exception e) {
			throw new ProcessletException(
					"Error parsing parameter " + gmlInputGeometry.getIdentifier() + ": " + e.getMessage());
		}

		ComplexOutput gmlOutputGeometry = (ComplexOutput) out.getParameter("BufferedGeometry");

		try {
			SchemaLocationXMLStreamWriter sw = new SchemaLocationXMLStreamWriter(gmlOutputGeometry.getXMLStreamWriter(),
					"http://www.opengis.net/gml http://schemas.opengis.net/gml/3.1.1/base/geometryAggregates.xsd");
			sw.setPrefix("gml", GMLNS);
			GMLStreamWriter gmlWriter = GMLOutputFactory.createGMLStreamWriter(GML_31, sw);
			gmlWriter.write(bufferedGeom);
		}
		catch (Exception e) {
			throw new ProcessletException("Error exporting geometry: " + e.getMessage());
		}
	}

	@Override
	public Geometry process(Geometry inputGeometry, Map<String, Object> params) {
		double bufferDistance = (Double) params.get("BufferDistance");
		return inputGeometry.getBuffer(new Measure(new BigDecimal(bufferDistance), "unity"));
	}

}
