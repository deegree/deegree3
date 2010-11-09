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
package org.deegree.gml.geometry;

import java.io.IOException;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

import junit.framework.Assert;

import org.deegree.commons.xml.XMLParsingException;
import org.deegree.commons.xml.stax.XMLStreamReaderWrapper;
import org.deegree.cs.CRS;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.geometry.GeometryFactory;
import org.deegree.geometry.primitive.patches.Cone;
import org.deegree.geometry.primitive.patches.Cylinder;
import org.deegree.geometry.primitive.patches.PolygonPatch;
import org.deegree.geometry.primitive.patches.Rectangle;
import org.deegree.geometry.primitive.patches.Sphere;
import org.deegree.geometry.primitive.patches.Triangle;
import org.deegree.gml.GMLVersion;
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
public class GML3SurfacePatchReaderTest {

    private GeometryFactory geomFac;

    @Before
    public void setUp()
                            throws Exception {
        geomFac = new GeometryFactory();
    }

    @Test
    public void parsePolygonPatch()
                            throws XMLStreamException, FactoryConfigurationError, IOException, XMLParsingException,
                            UnknownCRSException {
        XMLStreamReaderWrapper parser = getParser( "PolygonPatch.gml" );
        PolygonPatch patch = (PolygonPatch) getPatchParser().parseSurfacePatch( parser, new CRS( "EPSG:4326" ) );
        Assert.assertEquals( 2.0, patch.getExteriorRing().getStartPoint().get0() );
        Assert.assertEquals( 0.0, patch.getExteriorRing().getStartPoint().get1() );
        Assert.assertEquals( 2.0, patch.getExteriorRing().getEndPoint().get0() );
        Assert.assertEquals( 0.0, patch.getExteriorRing().getEndPoint().get1() );
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

    @Test
    public void parseCone()
                            throws XMLStreamException, FactoryConfigurationError, IOException, XMLParsingException,
                            UnknownCRSException {
        XMLStreamReaderWrapper parser = getParser( "Cone.gml" );
        Cone patch = (Cone) getPatchParser().parseSurfacePatch( parser, new CRS( "EPSG:4326" ) );
        Assert.assertEquals( 3, patch.getNumColumns() );
        Assert.assertEquals( 2, patch.getNumRows() );
    }

    @Test
    public void parseCylinder()
                            throws XMLStreamException, FactoryConfigurationError, IOException, XMLParsingException,
                            UnknownCRSException {
        XMLStreamReaderWrapper parser = getParser( "Cylinder.gml" );
        Cylinder patch = (Cylinder) getPatchParser().parseSurfacePatch( parser, new CRS( "EPSG:4326" ) );
        Assert.assertEquals( 3, patch.getNumColumns() );
        Assert.assertEquals( 2, patch.getNumRows() );
    }

    @Test
    public void parseSphere()
                            throws XMLStreamException, FactoryConfigurationError, IOException, XMLParsingException,
                            UnknownCRSException {
        XMLStreamReaderWrapper parser = getParser( "Sphere.gml" );
        Sphere patch = (Sphere) getPatchParser().parseSurfacePatch( parser, new CRS( "EPSG:4326" ) );
        Assert.assertEquals( 3, patch.getNumColumns() );
        Assert.assertEquals( 2, patch.getNumRows() );
    }

    private XMLStreamReaderWrapper getParser( String fileName )
                            throws XMLStreamException, FactoryConfigurationError, IOException {
        XMLStreamReaderWrapper xmlReader = new XMLStreamReaderWrapper(
                                                                       GML3SurfacePatchReaderTest.class.getResource( "../../geometry/gml/testdata/patches/"
                                                                                                                     + fileName ) );
        xmlReader.nextTag();
        return xmlReader;
    }

    private GML3SurfacePatchReader getPatchParser() {
        GeometryFactory geomFac = new GeometryFactory();
        return new GML3SurfacePatchReader( new GML3GeometryReader( GMLVersion.GML_31, null, null, 2 ), geomFac, 2 );
    }
}
