/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
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
package org.deegree.wps.jts;

import static org.deegree.commons.xml.CommonNamespaces.GMLNS;
import static org.deegree.gml.GMLVersion.GML_31;

import javax.xml.stream.XMLStreamReader;

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
import org.deegree.services.wps.output.ComplexOutput;

/**
 * The <code></code> class TODO add class documentation here.
 *
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 *
 */
public class UnionProcesslet implements Processlet {

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
		ComplexInput gmlInput1 = (ComplexInput) in.getParameter("GMLInput1");
		ComplexInput gmlInput2 = (ComplexInput) in.getParameter("GMLInput2");

		Geometry geometry1 = readGeometry(gmlInput1);
		Geometry geometry2 = readGeometry(gmlInput2);

		Geometry union = geometry1.getUnion(geometry2);
		ComplexOutput unionOutput = (ComplexOutput) out.getParameter("Union");
		writeGeometry(unionOutput, union);
	}

	private void writeGeometry(ComplexOutput gmlOutput, Geometry geometry) throws ProcessletException {
		try {
			SchemaLocationXMLStreamWriter sw = new SchemaLocationXMLStreamWriter(gmlOutput.getXMLStreamWriter(),
					"http://www.opengis.net/gml http://schemas.opengis.net/gml/3.1.1/base/geometryAggregates.xsd");
			sw.setPrefix("gml", GMLNS);
			GMLStreamWriter gmlWriter = GMLOutputFactory.createGMLStreamWriter(GML_31, sw);
			gmlWriter.write(geometry);
		}
		catch (Exception e) {
			throw new ProcessletException("Error exporting geometry: " + e.getMessage());
		}
	}

	private Geometry readGeometry(ComplexInput gmlInput) throws ProcessletException {
		try {
			XMLStreamReader xmlReader = gmlInput.getValueAsXMLStream();
			GMLStreamReader gmlReader = GMLInputFactory.createGMLStreamReader(GML_31, xmlReader);
			return gmlReader.readGeometry();
		}
		catch (Exception e) {
			throw new ProcessletException(
					"Error parsing parameter " + gmlInput.getIdentifier() + ": " + e.getMessage());
		}
	}

}
