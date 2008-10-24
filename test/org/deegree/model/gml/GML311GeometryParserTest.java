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

import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

import org.deegree.commons.xml.stax.XMLStreamReaderWrapper;
import org.deegree.model.geometry.GeometryFactory;
import org.deegree.model.geometry.GeometryFactoryCreator;
import org.deegree.model.geometry.composite.CompositeSolid;
import org.deegree.model.geometry.composite.CompositeSurface;
import org.deegree.model.geometry.primitive.Curve;
import org.deegree.model.geometry.primitive.OrientableSurface;
import org.deegree.model.geometry.primitive.Point;
import org.deegree.model.geometry.primitive.Polygon;
import org.deegree.model.geometry.primitive.PolyhedralSurface;
import org.deegree.model.geometry.primitive.Ring;
import org.deegree.model.geometry.primitive.Solid;
import org.deegree.model.geometry.primitive.Surface;
import org.deegree.model.geometry.primitive.TriangulatedSurface;
import org.deegree.model.geometry.primitive.Ring.RingType;
import org.deegree.model.geometry.primitive.Solid.SolidType;
import org.deegree.model.geometry.primitive.Surface.SurfaceType;
import org.deegree.model.geometry.primitive.curvesegments.Arc;
import org.deegree.model.geometry.primitive.curvesegments.LineStringSegment;
import org.deegree.model.geometry.primitive.curvesegments.CurveSegment.Interpolation;
import org.deegree.model.geometry.primitive.surfacepatches.PolygonPatch;
import org.junit.Assert;
import org.junit.Test;

/**
 * TODO add documentation here
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
public class GML311GeometryParserTest {

    private static GeometryFactory geomFac = GeometryFactoryCreator.getInstance().getGeometryFactory( "Standard" );

    private static final String BASE_DIR = "testdata/geometries/";

    @Test
    public void parsePointPos()
                            throws XMLStreamException, FactoryConfigurationError, IOException {

        XMLStreamReaderWrapper xmlReader = getParser( "Point1_pos.gml" );
        Assert.assertEquals( XMLStreamConstants.START_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "Point" ), xmlReader.getName() );
        Point point = (Point) new GML311GeometryParser( geomFac, xmlReader ).parseGeometry( null );
        Assert.assertEquals( XMLStreamConstants.END_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "Point" ), xmlReader.getName() );
        Assert.assertEquals( 7.12, point.getX() );
        Assert.assertEquals( 50.72, point.getY() );
        Assert.assertEquals( 2, point.getCoordinateDimension() );
        Assert.assertEquals( "EPSG:4326", point.getCoordinateSystem().getIdentifier() );
    }

    @Test
    public void parsePointCoordinates()
                            throws XMLStreamException, FactoryConfigurationError, IOException {
        XMLStreamReaderWrapper xmlReader = getParser( "Point1_coordinates.gml" );
        Assert.assertEquals( XMLStreamConstants.START_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "Point" ), xmlReader.getName() );
        Point point = (Point) new GML311GeometryParser( geomFac, xmlReader ).parseGeometry( null );
        Assert.assertEquals( XMLStreamConstants.END_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "Point" ), xmlReader.getName() );
        Assert.assertEquals( 7.12, point.getX() );
        Assert.assertEquals( 50.72, point.getY() );
        Assert.assertEquals( 2, point.getCoordinateDimension() );
        Assert.assertEquals( "EPSG:4326", point.getCoordinateSystem().getIdentifier() );
    }

    @Test
    public void parsePointCoord()
                            throws XMLStreamException, FactoryConfigurationError, IOException {
        XMLStreamReaderWrapper xmlReader = getParser( "Point1_coord.gml" );
        Assert.assertEquals( XMLStreamConstants.START_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "Point" ), xmlReader.getName() );
        Point point = (Point) new GML311GeometryParser( geomFac, xmlReader ).parseGeometry( null );
        Assert.assertEquals( XMLStreamConstants.END_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "Point" ), xmlReader.getName() );
        Assert.assertEquals( 7.12, point.getX() );
        Assert.assertEquals( 50.72, point.getY() );
        Assert.assertEquals( 2, point.getCoordinateDimension() );
        Assert.assertEquals( "EPSG:4326", point.getCoordinateSystem().getIdentifier() );
    }

    @Test
    public void parseLineStringPos()
                            throws XMLStreamException, FactoryConfigurationError, IOException {
        XMLStreamReaderWrapper xmlReader = getParser( "LineString1_pos.gml" );
        Assert.assertEquals( XMLStreamConstants.START_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "LineString" ), xmlReader.getName() );
        Curve curve = (Curve) new GML311GeometryParser( geomFac, xmlReader ).parseGeometry( null );
        Assert.assertEquals( XMLStreamConstants.END_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "LineString" ), xmlReader.getName() );
    }

    @Test
    public void parseLineStringPosList()
                            throws XMLStreamException, FactoryConfigurationError, IOException {
        XMLStreamReaderWrapper xmlReader = getParser( "LineString1_posList.gml" );
        Assert.assertEquals( XMLStreamConstants.START_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "LineString" ), xmlReader.getName() );
        Curve curve = (Curve) new GML311GeometryParser( geomFac, xmlReader ).parseGeometry( null );
        Assert.assertEquals( XMLStreamConstants.END_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "LineString" ), xmlReader.getName() );
        Assert.assertEquals( 1, curve.getCurveSegments().size() );
        Assert.assertEquals( Interpolation.linear, curve.getCurveSegments().get( 0 ).getInterpolation() );
        Assert.assertEquals( 3, curve.getAsLineString().getControlPoints().size() );
        Assert.assertEquals( 7.12, curve.getAsLineString().getControlPoints().get( 0 ).getX() );
        Assert.assertEquals( 50.72, curve.getAsLineString().getControlPoints().get( 0 ).getY() );
        Assert.assertEquals( 9.98, curve.getAsLineString().getControlPoints().get( 1 ).getX() );
        Assert.assertEquals( 53.55, curve.getAsLineString().getControlPoints().get( 1 ).getY() );
        Assert.assertEquals( 13.42, curve.getAsLineString().getControlPoints().get( 2 ).getX() );
        Assert.assertEquals( 52.52, curve.getAsLineString().getControlPoints().get( 2 ).getY() );
    }

    @Test
    public void parseLineStringCoordinates()
                            throws XMLStreamException, FactoryConfigurationError, IOException {
        XMLStreamReaderWrapper xmlReader = getParser( "LineString1_coordinates.gml" );
        Assert.assertEquals( XMLStreamConstants.START_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "LineString" ), xmlReader.getName() );
        Curve curve = (Curve) new GML311GeometryParser( geomFac, xmlReader ).parseGeometry( null );
        Assert.assertEquals( XMLStreamConstants.END_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "LineString" ), xmlReader.getName() );
        Assert.assertEquals( 1, curve.getCurveSegments().size() );
        Assert.assertEquals( Interpolation.linear, curve.getCurveSegments().get( 0 ).getInterpolation() );
        Assert.assertEquals( 3, curve.getAsLineString().getControlPoints().size() );
        Assert.assertEquals( 7.12, curve.getAsLineString().getControlPoints().get( 0 ).getX() );
        Assert.assertEquals( 50.72, curve.getAsLineString().getControlPoints().get( 0 ).getY() );
        Assert.assertEquals( 9.98, curve.getAsLineString().getControlPoints().get( 1 ).getX() );
        Assert.assertEquals( 53.55, curve.getAsLineString().getControlPoints().get( 1 ).getY() );
        Assert.assertEquals( 13.42, curve.getAsLineString().getControlPoints().get( 2 ).getX() );
        Assert.assertEquals( 52.52, curve.getAsLineString().getControlPoints().get( 2 ).getY() );
    }

    @Test
    public void parseLineStringPointProperty()
                            throws XMLStreamException, FactoryConfigurationError, IOException {
        XMLStreamReaderWrapper xmlReader = getParser( "LineString1_pointProperty.gml" );
        Assert.assertEquals( XMLStreamConstants.START_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "LineString" ), xmlReader.getName() );
        Curve curve = (Curve) new GML311GeometryParser( geomFac, xmlReader ).parseGeometry( null );
        Assert.assertEquals( XMLStreamConstants.END_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "LineString" ), xmlReader.getName() );
        Assert.assertEquals( 1, curve.getCurveSegments().size() );
        Assert.assertEquals( Interpolation.linear, curve.getCurveSegments().get( 0 ).getInterpolation() );
        Assert.assertEquals( 3, curve.getAsLineString().getControlPoints().size() );
        Assert.assertEquals( 7.12, curve.getAsLineString().getControlPoints().get( 0 ).getX() );
        Assert.assertEquals( 50.72, curve.getAsLineString().getControlPoints().get( 0 ).getY() );
        Assert.assertEquals( 9.98, curve.getAsLineString().getControlPoints().get( 1 ).getX() );
        Assert.assertEquals( 53.55, curve.getAsLineString().getControlPoints().get( 1 ).getY() );
        Assert.assertEquals( 13.42, curve.getAsLineString().getControlPoints().get( 2 ).getX() );
        Assert.assertEquals( 52.52, curve.getAsLineString().getControlPoints().get( 2 ).getY() );
    }

    @Test
    public void parseLineStringPointRep()
                            throws XMLStreamException, FactoryConfigurationError, IOException {
        XMLStreamReaderWrapper xmlReader = getParser( "LineString1_pointRep.gml" );
        Assert.assertEquals( XMLStreamConstants.START_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "LineString" ), xmlReader.getName() );
        Curve curve = (Curve) new GML311GeometryParser( geomFac, xmlReader ).parseGeometry( null );
        Assert.assertEquals( XMLStreamConstants.END_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "LineString" ), xmlReader.getName() );
        Assert.assertEquals( 1, curve.getCurveSegments().size() );
        Assert.assertEquals( Interpolation.linear, curve.getCurveSegments().get( 0 ).getInterpolation() );
        Assert.assertEquals( 3, curve.getAsLineString().getControlPoints().size() );
        Assert.assertEquals( 7.12, curve.getAsLineString().getControlPoints().get( 0 ).getX() );
        Assert.assertEquals( 50.72, curve.getAsLineString().getControlPoints().get( 0 ).getY() );
        Assert.assertEquals( 9.98, curve.getAsLineString().getControlPoints().get( 1 ).getX() );
        Assert.assertEquals( 53.55, curve.getAsLineString().getControlPoints().get( 1 ).getY() );
        Assert.assertEquals( 13.42, curve.getAsLineString().getControlPoints().get( 2 ).getX() );
        Assert.assertEquals( 52.52, curve.getAsLineString().getControlPoints().get( 2 ).getY() );
    }

    @Test
    public void parseCurve()
                            throws XMLStreamException, FactoryConfigurationError, IOException {
        XMLStreamReaderWrapper xmlReader = getParser( "Curve.gml" );
        Assert.assertEquals( XMLStreamConstants.START_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "Curve" ), xmlReader.getName() );
        Curve curve = (Curve) new GML311GeometryParser( geomFac, xmlReader ).parseGeometry( null );
        Assert.assertEquals( XMLStreamConstants.END_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "Curve" ), xmlReader.getName() );
        Assert.assertEquals( 2, curve.getCurveSegments().size() );
        Assert.assertEquals( Interpolation.circularArc3Points, curve.getCurveSegments().get( 0 ).getInterpolation() );
        Assert.assertEquals( Interpolation.linear, curve.getCurveSegments().get( 1 ).getInterpolation() );
    }

    @Test
    public void parseOrientableCurve()
                            throws XMLStreamException, FactoryConfigurationError, IOException {
        XMLStreamReaderWrapper xmlReader = getParser( "OrientableCurve.gml" );
        Assert.assertEquals( XMLStreamConstants.START_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "OrientableCurve" ), xmlReader.getName() );
        Curve curve = (Curve) new GML311GeometryParser( geomFac, xmlReader ).parseGeometry( null );
        Assert.assertEquals( XMLStreamConstants.END_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "OrientableCurve" ), xmlReader.getName() );
        Assert.assertEquals( 2, curve.getCurveSegments().size() );
        Assert.assertEquals( Interpolation.circularArc3Points, curve.getCurveSegments().get( 0 ).getInterpolation() );
        Assert.assertEquals( Interpolation.linear, curve.getCurveSegments().get( 1 ).getInterpolation() );
    }

    @Test
    public void parseLinearRing()
                            throws XMLStreamException, FactoryConfigurationError, IOException {
        XMLStreamReaderWrapper xmlReader = getParser( "LinearRing.gml" );
        Assert.assertEquals( XMLStreamConstants.START_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "LinearRing" ), xmlReader.getName() );
        Ring ring = (Ring) new GML311GeometryParser( geomFac, xmlReader ).parseGeometry( null );
        Assert.assertEquals( XMLStreamConstants.END_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "LinearRing" ), xmlReader.getName() );
        Assert.assertEquals( 1, ring.getMembers().size() );
        Assert.assertEquals( 1, ring.getMembers().get( 0 ).getCurveSegments().size() );
        Assert.assertTrue( ring.getMembers().get( 0 ).getCurveSegments().get( 0 ) instanceof LineStringSegment );
        Assert.assertEquals( 7, ring.getMembers().get( 0 ).getAsLineString().getControlPoints().size() );
    }

    @Test
    public void parseRing()
                            throws XMLStreamException, FactoryConfigurationError, IOException {
        XMLStreamReaderWrapper xmlReader = getParser( "Ring.gml" );
        Assert.assertEquals( XMLStreamConstants.START_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "Ring" ), xmlReader.getName() );
        Ring ring = (Ring) new GML311GeometryParser( geomFac, xmlReader ).parseGeometry( null );
        Assert.assertEquals( XMLStreamConstants.END_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "Ring" ), xmlReader.getName() );
        Assert.assertEquals( 2, ring.getMembers().size() );
        Assert.assertEquals( 2, ring.getMembers().get( 0 ).getCurveSegments().size() );
        Assert.assertTrue( ring.getMembers().get( 0 ).getCurveSegments().get( 0 ) instanceof Arc );
        Assert.assertTrue( ring.getMembers().get( 0 ).getCurveSegments().get( 1 ) instanceof Arc );
        Assert.assertEquals( 1, ring.getMembers().get( 1 ).getCurveSegments().size() );
        Assert.assertTrue( ring.getMembers().get( 1 ).getCurveSegments().get( 0 ) instanceof LineStringSegment );
    }

    @Test
    public void parsePolygon()
                            throws XMLStreamException, FactoryConfigurationError, IOException {
        XMLStreamReaderWrapper xmlReader = getParser( "Polygon.gml" );
        Assert.assertEquals( XMLStreamConstants.START_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "Polygon" ), xmlReader.getName() );
        Polygon polygon = (Polygon) new GML311GeometryParser( geomFac, xmlReader ).parseGeometry( null );
        Assert.assertEquals( SurfaceType.Polygon, polygon.getSurfaceType() );
        Assert.assertEquals( XMLStreamConstants.END_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "Polygon" ), xmlReader.getName() );
        Assert.assertEquals( RingType.LinearRing, polygon.getExteriorRing().getRingType() );
        Assert.assertEquals( 2, polygon.getInteriorRings().size() );
        Assert.assertEquals( RingType.LinearRing, polygon.getInteriorRings().get( 0 ).getRingType() );
        Assert.assertEquals( RingType.LinearRing, polygon.getInteriorRings().get( 1 ).getRingType() );
    }

    @Test
    public void parseSurface()
                            throws XMLStreamException, FactoryConfigurationError, IOException {
        XMLStreamReaderWrapper xmlReader = getParser( "Surface.gml" );
        Assert.assertEquals( XMLStreamConstants.START_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "Surface" ), xmlReader.getName() );
        Surface surface = (Surface) new GML311GeometryParser( geomFac, xmlReader ).parseGeometry( null );
        Assert.assertEquals( XMLStreamConstants.END_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "Surface" ), xmlReader.getName() );
        Assert.assertEquals( SurfaceType.Surface, surface.getSurfaceType() );
        Assert.assertEquals( 2, surface.getPatches().size() );
    }

    @Test
    public void parsePolyhedralSurface()
                            throws XMLStreamException, FactoryConfigurationError, IOException {
        XMLStreamReaderWrapper xmlReader = getParser( "PolyhedralSurface.gml" );
        Assert.assertEquals( XMLStreamConstants.START_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "PolyhedralSurface" ), xmlReader.getName() );
        PolyhedralSurface surface = (PolyhedralSurface) new GML311GeometryParser( geomFac, xmlReader ).parseGeometry( null );
        Assert.assertEquals( XMLStreamConstants.END_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "PolyhedralSurface" ), xmlReader.getName() );
        Assert.assertEquals( SurfaceType.PolyhedralSurface, surface.getSurfaceType() );
        Assert.assertEquals( 2, surface.getPatches().size() );
    }

    @Test
    public void parseTriangulatedSurface()
                            throws XMLStreamException, FactoryConfigurationError, IOException {
        XMLStreamReaderWrapper xmlReader = getParser( "TriangulatedSurface.gml" );
        Assert.assertEquals( XMLStreamConstants.START_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "TriangulatedSurface" ), xmlReader.getName() );
        TriangulatedSurface surface = (TriangulatedSurface) new GML311GeometryParser( geomFac, xmlReader ).parseGeometry( null );
        Assert.assertEquals( XMLStreamConstants.END_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "TriangulatedSurface" ), xmlReader.getName() );
        Assert.assertEquals( SurfaceType.TriangulatedSurface, surface.getSurfaceType() );
        Assert.assertEquals( 3, surface.getPatches().size() );
    }

    @Test
    public void parseOrientableSurface()
                            throws XMLStreamException, FactoryConfigurationError, IOException {
        XMLStreamReaderWrapper xmlReader = getParser( "OrientableSurface.gml" );
        Assert.assertEquals( XMLStreamConstants.START_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "OrientableSurface" ), xmlReader.getName() );
        OrientableSurface surface = (OrientableSurface) new GML311GeometryParser( geomFac, xmlReader ).parseGeometry( null );
        Assert.assertEquals( XMLStreamConstants.END_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "OrientableSurface" ), xmlReader.getName() );
        Assert.assertEquals( SurfaceType.OrientableSurface, surface.getSurfaceType() );
        Assert.assertEquals( 2, surface.getPatches().size() );
    }

    @Test
    public void parseSolid()
                            throws XMLStreamException, FactoryConfigurationError, IOException {
        XMLStreamReaderWrapper xmlReader = getParser( "Solid.gml" );
        Assert.assertEquals( XMLStreamConstants.START_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "Solid" ), xmlReader.getName() );
        Solid solid = (Solid) new GML311GeometryParser( geomFac, xmlReader ).parseGeometry( null );
        Assert.assertEquals( XMLStreamConstants.END_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "Solid" ), xmlReader.getName() );
        Assert.assertEquals( SolidType.Solid, solid.getSolidType() );
        Assert.assertEquals( "EPSG:31466", solid.getCoordinateSystem().getIdentifier() );
        Assert.assertEquals( 8, solid.getExteriorSurface().getPatches().size() );
        Assert.assertEquals( 2568786.096, ((PolygonPatch) solid.getExteriorSurface().getPatches().get( 7 )).getExteriorRing().getStartPoint().getX() );
        Assert.assertEquals( 5662881.386, ((PolygonPatch) solid.getExteriorSurface().getPatches().get( 7 )).getExteriorRing().getStartPoint().getY() );
        Assert.assertEquals( 60.3842642785516, ((PolygonPatch) solid.getExteriorSurface().getPatches().get( 7 )).getExteriorRing().getStartPoint().getZ() );        
        Assert.assertEquals( 2568786.096, ((PolygonPatch) solid.getExteriorSurface().getPatches().get( 7 )).getExteriorRing().getEndPoint().getX() );
        Assert.assertEquals( 5662881.386, ((PolygonPatch) solid.getExteriorSurface().getPatches().get( 7 )).getExteriorRing().getEndPoint().getY() );
        Assert.assertEquals( 60.3842642785516, ((PolygonPatch) solid.getExteriorSurface().getPatches().get( 7 )).getExteriorRing().getEndPoint().getZ() );
    }

    @Test
    public void parseCompositeCurve()
                            throws XMLStreamException, FactoryConfigurationError, IOException {
        XMLStreamReaderWrapper xmlReader = getParser( "CompositeCurve.gml" );
        Assert.assertEquals( XMLStreamConstants.START_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "CompositeCurve" ), xmlReader.getName() );
        Curve curve = (Curve) new GML311GeometryParser( geomFac, xmlReader ).parseGeometry( null );
        Assert.assertEquals( XMLStreamConstants.END_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "CompositeCurve" ), xmlReader.getName() );
        Assert.assertEquals( 3, curve.getCurveSegments().size() );
        Assert.assertEquals( Interpolation.circularArc3Points, curve.getCurveSegments().get( 0 ).getInterpolation() );
        Assert.assertEquals( Interpolation.linear, curve.getCurveSegments().get( 1 ).getInterpolation() );
        Assert.assertEquals( Interpolation.linear, curve.getCurveSegments().get( 2 ).getInterpolation() );
    }

    @Test
    public void parseCompositeSurface()
                            throws XMLStreamException, FactoryConfigurationError, IOException {
        XMLStreamReaderWrapper xmlReader = getParser( "CompositeSurface.gml" );
        Assert.assertEquals( XMLStreamConstants.START_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "CompositeSurface" ), xmlReader.getName() );
        CompositeSurface surface = (CompositeSurface) new GML311GeometryParser( geomFac, xmlReader ).parseGeometry( null );
        Assert.assertEquals( XMLStreamConstants.END_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "CompositeSurface" ), xmlReader.getName() );
        Assert.assertEquals( 3, surface.getPatches().size() );
    }

    @Test
    public void parseCompositeSolid()
                            throws XMLStreamException, FactoryConfigurationError, IOException {
        XMLStreamReaderWrapper xmlReader = getParser( "CompositeSolid.gml" );
        Assert.assertEquals( XMLStreamConstants.START_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "CompositeSolid" ), xmlReader.getName() );
        CompositeSolid solid = (CompositeSolid) new GML311GeometryParser( geomFac, xmlReader ).parseGeometry( null );
        Assert.assertEquals( XMLStreamConstants.END_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "CompositeSolid" ), xmlReader.getName() );
        Assert.assertEquals( SolidType.CompositeSolid, solid.getSolidType() );
    }    

    private XMLStreamReaderWrapper getParser( String fileName )
                            throws XMLStreamException, FactoryConfigurationError, IOException {
        XMLStreamReaderWrapper xmlReader = new XMLStreamReaderWrapper(
                                                                       GML311SurfacePatchParserTest.class.getResource( BASE_DIR
                                                                                                                       + fileName ) );
        xmlReader.nextTag();
        return xmlReader;
    }
}
