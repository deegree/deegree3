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
package org.deegree.model.geometry.validation;

import java.io.IOException;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

import junit.framework.Assert;

import org.deegree.commons.xml.stax.XMLStreamReaderWrapper;
import org.deegree.model.geometry.Geometry;
import org.deegree.model.geometry.GeometryFactory;
import org.deegree.model.geometry.GeometryFactoryCreator;
import org.deegree.model.geometry.validation.GeometryValidator;
import org.deegree.model.gml.GML311GeometryParser;
import org.deegree.model.gml.GML311GeometryParserTest;
import org.junit.Test;

/**
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
public class GeometryValidatorTest {

    private static GeometryFactory geomFac = GeometryFactoryCreator.getInstance().getGeometryFactory( "Standard" );

    private static final String BASE_DIR = "testdata/geometries/";

    @Test
    public void validateCurve()
                            throws XMLStreamException, FactoryConfigurationError, IOException {
        GeometryValidator validator = new GeometryValidator();
        Geometry geom = parseGeometry( "Curve.gml" );
        Assert.assertTrue( validator.validateGeometry( geom ) );
    }

    @Test
    public void validateInvalidCurve()
                            throws XMLStreamException, FactoryConfigurationError, IOException {
        GeometryValidator validator = new GeometryValidator();
        Geometry geom = parseGeometry( "invalid/Curve.gml" );
        Assert.assertFalse( "Discontinuity between curve segments not detected.", validator.validateGeometry( geom ) );
    }

    @Test
    public void validatePolygon()
                            throws XMLStreamException, FactoryConfigurationError, IOException {
        GeometryValidator validator = new GeometryValidator();
        Geometry geom = parseGeometry( "Polygon_exterior_not_closed.gml" );
        Assert.assertFalse( validator.validateGeometry( geom ) );
    }

    private Geometry parseGeometry( String fileName )
                            throws XMLStreamException, FactoryConfigurationError, IOException {
        XMLStreamReaderWrapper xmlReader = new XMLStreamReaderWrapper(
                                                                       GML311GeometryParserTest.class.getResource( BASE_DIR
                                                                                                                   + fileName ) );
        xmlReader.nextTag();
        return new GML311GeometryParser( geomFac, xmlReader ).parseGeometry( null );
    }
}
