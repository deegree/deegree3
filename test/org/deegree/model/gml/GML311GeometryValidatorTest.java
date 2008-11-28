//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------

 This file is part of deegree.
 Copyright (C) 2001-2008 by:
 EXSE, Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 Contact:

 Andreas Poth  
 lat/lon GmbH 
 Aennchenstr. 19
 53115 Bonn
 Germany
 E-Mail: poth@lat-lon.de

 Prof. Dr. Klaus Greve
 Department of Geography
 University of Bonn
 Meckenheimer Allee 166
 53115 Bonn
 Germany
 E-Mail: greve@giub.uni-bonn.de


 ---------------------------------------------------------------------------*/
package org.deegree.model.gml;

import java.io.IOException;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

import org.deegree.commons.xml.stax.XMLStreamReaderWrapper;
import org.deegree.model.geometry.Geometry;
import org.deegree.model.geometry.GeometryFactory;
import org.deegree.model.geometry.GeometryFactoryCreator;
import org.deegree.model.geometry.primitive.Polygon;
import org.deegree.model.geometry.primitive.Ring.RingType;
import org.deegree.model.geometry.primitive.Surface.SurfaceType;
import org.deegree.model.gml.validation.GML311GeometryValidator;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests that check the correct parsing of GML 3.1.1 geometry elements (elements substitutable for
 * <code>gml:_Geometry</code> and <code>gml:Envelope</code>).
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
public class GML311GeometryValidatorTest {

    private static GeometryFactory geomFac = GeometryFactoryCreator.getInstance().getGeometryFactory( "Standard" );

    private static final String BASE_DIR = "testdata/geometries/";

    @Test
    public void validatePolygon()
                            throws XMLStreamException, FactoryConfigurationError, IOException {
        Geometry geom = parseGeometry( "Polygon.gml" );
        
    }    
    
    @Test
    public void testParsingIMRO2008FeatureCollection()
                            throws XMLStreamException, FactoryConfigurationError, IOException, ClassCastException,
                            ClassNotFoundException, InstantiationException, IllegalAccessException {

        XMLStreamReaderWrapper xmlReader = new XMLStreamReaderWrapper(
                                                                       new URL(
                                                                                "file:///home/schneider/workspace/prvlimburg_nlrpp/resources/testplans/NL.IMRO.0964.000matrixplan1-0003.gml" ) );
        xmlReader.nextTag();
        GML311GeometryValidator validator = new GML311GeometryValidator( xmlReader );
        validator.validateGeometries();
    }

    private Geometry parseGeometry( String fileName )
                            throws XMLStreamException, FactoryConfigurationError, IOException {
        XMLStreamReaderWrapper xmlReader = new XMLStreamReaderWrapper(
                                                                       GML311SurfacePatchParserTest.class.getResource( BASE_DIR
                                                                                                                       + fileName ) );
        xmlReader.nextTag();
        return new GML311GeometryParser( geomFac, xmlReader ).parseGeometry( null );
    }
}
