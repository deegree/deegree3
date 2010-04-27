//$HeadURL$
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

package org.deegree.services.wps.example;

import static org.deegree.commons.xml.CommonNamespaces.GMLNS;
import static org.deegree.gml.GMLVersion.GML_31;

import java.math.BigDecimal;

import javax.xml.stream.XMLStreamReader;

import org.deegree.commons.uom.Measure;
import org.deegree.commons.xml.stax.XMLStreamWriterWrapper;
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
import org.deegree.services.wps.annotations.ProcessDescription;
import org.deegree.services.wps.annotations.commons.ComplexFormat;
import org.deegree.services.wps.annotations.commons.Metadata;
import org.deegree.services.wps.annotations.commons.ReferenceType;
import org.deegree.services.wps.annotations.commons.Type;
import org.deegree.services.wps.annotations.input.CmplxInput;
import org.deegree.services.wps.annotations.input.InputParameter;
import org.deegree.services.wps.annotations.input.LitInput;
import org.deegree.services.wps.annotations.input.Range;
import org.deegree.services.wps.annotations.input.ValueType;
import org.deegree.services.wps.annotations.output.CmplxOutput;
import org.deegree.services.wps.annotations.output.OutputParameter;
import org.deegree.services.wps.input.ComplexInput;
import org.deegree.services.wps.input.LiteralInput;
import org.deegree.services.wps.output.ComplexOutput;

/**
 * The <code>BufferProcesslet</code> demonstrates a process which calculates a buffer around an arbitrary geometry.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author: rbezema $
 * @version $Revision: $, $Date: $
 * 
 */
@ProcessDescription
(
 statusSupported = false,
 storeSupported = false, 
 version = "1.0.0",
 id = "BufferProcess",
 title = "Process for creating a buffer around a GML geometry.",
 abs = "The purpose of this process is to create a buffer around an existing geometry with a buffer distance specified by the user.",
 input = {
  @InputParameter( type=Type.Complex,
                   minOccurs=1,
                   id=BufferProcesslet.IN_GEOM, 
                   title="GML Geometry", 
                   abs="Input dataset as GML geometry.",
                   complex = @CmplxInput(
                                        formats = { @ComplexFormat(mimeType = "text/xml; subtype=gml/3.1.1", schema = "http://schemas.opengis.net/gml/3.1.1/base/geometryAggregates.xsd") }
                   ) 
  ),           
   @InputParameter( type=Type.Literal, 
                    id=BufferProcesslet.IN_BUF_DIST, 
                    title="Buffer distance", 
                    abs="Distance to the geometry.",
                    metadata={@Metadata( about="geometries", href="http://www.vividsolutions.com/jts/jtshome.htm" ) },
                    literal = @LitInput(
                            dataType=@ReferenceType(  reference="http://www.w3.org/TR/xmlschema-2/#double", value="double" ),
                            uoms={@ReferenceType( value="unity" ) },
                            defaultValue="0.1",
                            allowedValues={@ValueType( range= @Range( minimum="0", maximum="10", spacing="0.01" ) ) }
                    ) 
   )
}, 
output = { 
   @OutputParameter( type=Type.Complex, 
                     id=BufferProcesslet.OUT_GEOM,
                     title="GML output",
                     abs="GML stream containing the resulting buffered geometry",
                     complex = @CmplxOutput(
                                         formats = { @ComplexFormat(mimeType = "text/xml; subtype=gml/3.1.1", schema = "http://schemas.opengis.net/gml/3.1.1/base/geometryAggregates.xsd") }
   ))
}
)
public class BufferProcesslet implements Processlet {

    static final String IN_BUF_DIST = "BufferDistance";

    static final String IN_GEOM = "GMLInput";

    static final String OUT_GEOM = "GMLOutput";

    @Override
    public void destroy() {
        // TODO Auto-generated method stub

    }

    @Override
    public void init() {
        // TODO Auto-generated method stub

    }

    @Override
    public void process( ProcessletInputs in, ProcessletOutputs out, ProcessletExecutionInfo info )
                            throws ProcessletException {
        double bufferDistance = Double.parseDouble( ( (LiteralInput) in.getParameter( IN_BUF_DIST ) ).getValue() );
        ComplexInput gmlInputGeometry = (ComplexInput) in.getParameter( IN_GEOM );

        Geometry geom = null;
        Geometry bufferedGeom = null;
        try {
            XMLStreamReader xmlReader = gmlInputGeometry.getValueAsXMLStream();
            GMLStreamReader gmlReader = GMLInputFactory.createGMLStreamReader( GML_31, xmlReader );
            geom = gmlReader.readGeometry();

            bufferedGeom = geom.getBuffer( new Measure( new BigDecimal( bufferDistance ), "unity" ) );
        } catch ( Exception e ) {
            throw new ProcessletException( "Error parsing parameter " + gmlInputGeometry.getIdentifier() + ": "
                                           + e.getMessage() );
        }

        ComplexOutput gmlOutputGeometry = (ComplexOutput) out.getParameter( OUT_GEOM );

        try {
            XMLStreamWriterWrapper sw = new XMLStreamWriterWrapper( gmlOutputGeometry.getXMLStreamWriter(),
                                                                    "http://www.opengis.net/gml http://schemas.opengis.net/gml/3.1.1/base/geometryAggregates.xsd" );
            sw.setPrefix( "gml", GMLNS );
            GMLStreamWriter gmlWriter = GMLOutputFactory.createGMLStreamWriter( GML_31, sw );
            gmlWriter.write( bufferedGeom );
        } catch ( Exception e ) {
            throw new ProcessletException( "Error exporting geometry: " + e.getMessage() );
        }
    }
}
