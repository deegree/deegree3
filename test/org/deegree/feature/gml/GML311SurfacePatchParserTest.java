//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------

 This file is part of deegree.
 Copyright (C) 2001-2009 by:
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
package org.deegree.feature.gml;

import java.io.IOException;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

import junit.framework.Assert;

import org.deegree.commons.xml.XMLParsingException;
import org.deegree.commons.xml.stax.XMLStreamReaderWrapper;
import org.deegree.crs.CRS;
import org.deegree.crs.exceptions.UnknownCRSException;
import org.deegree.geometry.GeometryFactory;
import org.deegree.geometry.GeometryFactoryCreator;
import org.deegree.geometry.primitive.surfacepatches.PolygonPatch;
import org.deegree.geometry.primitive.surfacepatches.Rectangle;
import org.deegree.geometry.primitive.surfacepatches.Triangle;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests that check the correct parsing of GML 3.1.1 surface patches, i.e. of elements that are substitutable for
 * <code>gml:_SurfacePatch</code>.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
public class GML311SurfacePatchParserTest {

    private GeometryFactory geomFac;

    @Before
    public void setUp()
                            throws Exception {
        geomFac = GeometryFactoryCreator.getInstance().getGeometryFactory();
    }

    @Test
    public void parsePolygonPatch()
                            throws XMLStreamException, FactoryConfigurationError, IOException, XMLParsingException,
                            UnknownCRSException {
        XMLStreamReaderWrapper parser = getParser( "PolygonPatch.gml" );
        PolygonPatch patch = (PolygonPatch) getPatchParser().parseSurfacePatch( parser, new CRS( "EPSG:4326" ) );
        Assert.assertEquals( 2.0, patch.getExteriorRing().getStartPoint().getX() );
        Assert.assertEquals( 0.0, patch.getExteriorRing().getStartPoint().getY() );
        Assert.assertEquals( 2.0, patch.getExteriorRing().getEndPoint().getX() );
        Assert.assertEquals( 0.0, patch.getExteriorRing().getEndPoint().getY() );
        Assert.assertEquals( 2, patch.getInteriorRings().size() );
    }

    @Test
    public void parseTriangle()
                            throws XMLStreamException, FactoryConfigurationError, IOException, XMLParsingException,
                            UnknownCRSException {
        XMLStreamReaderWrapper parser = getParser( "Triangle.gml" );
        Triangle patch = (Triangle) getPatchParser().parseSurfacePatch( parser, new CRS( "EPSG:4326" ) );
        Assert.assertEquals( 4, patch.getExteriorRing().getControlPoints().size() );
    }

    @Test
    public void parseRectangle()
                            throws XMLStreamException, FactoryConfigurationError, IOException, XMLParsingException,
                            UnknownCRSException {
        XMLStreamReaderWrapper parser = getParser( "Rectangle.gml" );
        Rectangle patch = (Rectangle) getPatchParser().parseSurfacePatch( parser, new CRS( "EPSG:4326" ) );
        Assert.assertEquals( 5, patch.getExteriorRing().getControlPoints().size() );
    }

    private XMLStreamReaderWrapper getParser( String fileName )
                            throws XMLStreamException, FactoryConfigurationError, IOException {
        XMLStreamReaderWrapper xmlReader = new XMLStreamReaderWrapper(
                                                                       GML311SurfacePatchParserTest.class.getResource( "testdata/patches/"
                                                                                                                       + fileName ) );
        xmlReader.nextTag();
        return xmlReader;
    }

    private GML311SurfacePatchParser getPatchParser() {
        GeometryFactory geomFac = GeometryFactoryCreator.getInstance().getGeometryFactory();
        return new GML311SurfacePatchParser( new GML311GeometryParser(), geomFac );
    }
}
