package org.deegree.model.gml;

import java.io.IOException;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

import junit.framework.Assert;

import org.deegree.commons.xml.stax.XMLStreamReaderWrapper;
import org.deegree.model.geometry.GeometryFactory;
import org.deegree.model.geometry.GeometryFactoryCreator;
import org.deegree.model.geometry.primitive.surfacepatches.PolygonPatch;
import org.deegree.model.geometry.primitive.surfacepatches.Rectangle;
import org.deegree.model.geometry.primitive.surfacepatches.Triangle;
import org.junit.Before;
import org.junit.Test;

public class GML311SurfacePatchParserTest {

    private GeometryFactory geomFac;

    @Before
    public void setUp()
                            throws Exception {
        geomFac = GeometryFactoryCreator.getInstance().getGeometryFactory();
    }

    @Test
    public void parsePolygonPatch()
                            throws XMLStreamException, FactoryConfigurationError, IOException {
        GML311SurfacePatchParser parser = getParser( "PolygonPatch.gml" );
        PolygonPatch patch = (PolygonPatch) parser.parseSurfacePatch( "EPSG:4326" );
        Assert.assertEquals( 2.0, patch.getExteriorRing().getStartPoint().getX() );
        Assert.assertEquals( 0.0, patch.getExteriorRing().getStartPoint().getY() );
        Assert.assertEquals( 2.0, patch.getExteriorRing().getEndPoint().getX() );
        Assert.assertEquals( 0.0, patch.getExteriorRing().getEndPoint().getY() );
        Assert.assertEquals( 2, patch.getInteriorRings().size() );
    }

    @Test
    public void parseTriangle()
                            throws XMLStreamException, FactoryConfigurationError, IOException {
        GML311SurfacePatchParser parser = getParser( "Triangle.gml" );
        Triangle patch = (Triangle) parser.parseSurfacePatch( "EPSG:4326" );
        Assert.assertEquals( 4, patch.getExteriorRing().getControlPoints().size() );
    }    

    @Test
    public void parseRectangle()
                            throws XMLStreamException, FactoryConfigurationError, IOException {
        GML311SurfacePatchParser parser = getParser( "Rectangle.gml" );
        Rectangle patch = (Rectangle) parser.parseSurfacePatch( "EPSG:4326" );
        Assert.assertEquals( 5, patch.getExteriorRing().getControlPoints().size() );
    }     
    
    private GML311SurfacePatchParser getParser( String fileName )
                            throws XMLStreamException, FactoryConfigurationError, IOException {
        XMLStreamReaderWrapper xmlReader = new XMLStreamReaderWrapper(
                                                                       GML311SurfacePatchParserTest.class.getResource( "testdata/patches/"
                                                                                                                       + fileName ) );
        xmlReader.nextTag();
        GML311GeometryParser geomParser = new GML311GeometryParser( geomFac, xmlReader );
        return new GML311SurfacePatchParser( geomParser, geomFac, xmlReader );
    }
}
