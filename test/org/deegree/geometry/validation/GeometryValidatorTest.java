//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/base/trunk/resources/eclipse/files_template.xml $
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
package org.deegree.geometry.validation;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

import junit.framework.Assert;

import org.deegree.commons.xml.XMLParsingException;
import org.deegree.commons.xml.stax.XMLStreamReaderWrapper;
import org.deegree.crs.exceptions.UnknownCRSException;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.GeometryFactory;
import org.deegree.geometry.primitive.Curve;
import org.deegree.geometry.primitive.Point;
import org.deegree.geometry.primitive.Ring;
import org.deegree.geometry.primitive.patches.PolygonPatch;
import org.deegree.gml.GMLDocumentIdContext;
import org.deegree.gml.GMLInputFactory;
import org.deegree.gml.GMLVersion;
import org.deegree.gml.geometry.GML3GeometryReader;
import org.deegree.gml.geometry.GML3GeometryReaderTest;
import org.junit.Test;

/**
 * Testcases that check the correct determination of topological errors by the {@link GeometryValidator}.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
public class GeometryValidatorTest {

    private static GeometryFactory geomFac = new GeometryFactory();

    private static final String BASE_DIR = "../../geometry/gml/testdata/geometries/";

    @Test
    public void validateCurve()
                            throws XMLStreamException, FactoryConfigurationError, IOException, XMLParsingException,
                            UnknownCRSException {
        TestValidationEventHandler eventHandler = new TestValidationEventHandler();
        GeometryValidator validator = new GeometryValidator( eventHandler );
        Geometry geom = parseGeometry( "Curve.gml" );
        Assert.assertTrue( validator.validateGeometry( geom ) );
        Assert.assertTrue( eventHandler.getEvents().isEmpty() );
    }

    @Test
    public void validateInvalidCurve()
                            throws XMLStreamException, FactoryConfigurationError, IOException, XMLParsingException,
                            UnknownCRSException {
        TestValidationEventHandler eventHandler = new TestValidationEventHandler();
        GeometryValidator validator = new GeometryValidator( eventHandler );
        Geometry geom = parseGeometry( "invalid/Curve_discontinuity.gml" );
        Assert.assertFalse( validator.validateGeometry( geom ) );
        Assert.assertEquals( 1, eventHandler.getEvents().size() );
        Assert.assertEquals( ValidationEventType.CURVE_DISCONTINUITY, eventHandler.getEvents().get( 0 ) );
    }

    @Test
    public void validateRing()
                            throws XMLStreamException, FactoryConfigurationError, IOException, XMLParsingException,
                            UnknownCRSException {
        TestValidationEventHandler eventHandler = new TestValidationEventHandler();
        GeometryValidator validator = new GeometryValidator( eventHandler );
        Geometry geom = parseGeometry( "Ring.gml" );
        Assert.assertTrue( validator.validateGeometry( geom ) );
        Assert.assertTrue( eventHandler.getEvents().isEmpty() );
    }

    @Test
    public void validateInvalidRing()
                            throws XMLStreamException, FactoryConfigurationError, IOException, XMLParsingException,
                            UnknownCRSException {
        TestValidationEventHandler eventHandler = new TestValidationEventHandler();
        GeometryValidator validator = new GeometryValidator( eventHandler );
        Geometry geom = parseGeometry( "invalid/Ring_not_closed.gml" );
        Assert.assertFalse( "Geometry must be recognized as invalid.", validator.validateGeometry( geom ) );
        Assert.assertEquals( 1, eventHandler.getEvents().size() );
        Assert.assertEquals( ValidationEventType.RING_NOT_CLOSED, eventHandler.getEvents().get( 0 ) );
    }

    @Test
    public void validateInvalidRing2()
                            throws XMLStreamException, FactoryConfigurationError, IOException, XMLParsingException,
                            UnknownCRSException {
        TestValidationEventHandler eventHandler = new TestValidationEventHandler();
        GeometryValidator validator = new GeometryValidator( eventHandler );
        Geometry geom = parseGeometry( "invalid/Ring_self_intersection.gml" );
        Assert.assertFalse( "Geometry must be recognized as invalid.", validator.validateGeometry( geom ) );
        Assert.assertEquals( 2, eventHandler.getEvents().size() );
        Assert.assertEquals( ValidationEventType.CURVE_SELF_INTERSECTION, eventHandler.getEvents().get( 0 ) );
        Assert.assertEquals( ValidationEventType.RING_SELF_INTERSECTION, eventHandler.getEvents().get( 1 ) );
    }

    @Test
    public void validateInvalidRing3()
                            throws XMLStreamException, FactoryConfigurationError, IOException, XMLParsingException,
                            UnknownCRSException {
        TestValidationEventHandler eventHandler = new TestValidationEventHandler();
        GeometryValidator validator = new GeometryValidator( eventHandler );
        Geometry geom = parseGeometry( "invalid/Ring_not_closed_and_self_intersection.gml" );
        Assert.assertFalse( "Geometry must be recognized as invalid.", validator.validateGeometry( geom ) );
        Assert.assertEquals( 3, eventHandler.getEvents().size() );
        Assert.assertEquals( ValidationEventType.CURVE_SELF_INTERSECTION, eventHandler.getEvents().get( 0 ) );
        Assert.assertEquals( ValidationEventType.RING_SELF_INTERSECTION, eventHandler.getEvents().get( 1 ) );
        Assert.assertEquals( ValidationEventType.RING_NOT_CLOSED, eventHandler.getEvents().get( 2 ) );
    }

    @Test
    public void validatePolygon()
                            throws XMLStreamException, FactoryConfigurationError, IOException, XMLParsingException,
                            UnknownCRSException {
        TestValidationEventHandler eventHandler = new TestValidationEventHandler();
        GeometryValidator validator = new GeometryValidator( eventHandler );
        Geometry geom = parseGeometry( "Polygon.gml" );
        Assert.assertTrue( validator.validateGeometry( geom ) );
        Assert.assertTrue( eventHandler.getEvents().isEmpty() );
    }

    @Test
    public void validateInvalidPolygon1()
                            throws XMLStreamException, FactoryConfigurationError, IOException, XMLParsingException,
                            UnknownCRSException {
        TestValidationEventHandler eventHandler = new TestValidationEventHandler();
        GeometryValidator validator = new GeometryValidator( eventHandler );
        Geometry geom = parseGeometry( "invalid/Polygon_exterior_clockwise.gml" );
        Assert.assertFalse( validator.validateGeometry( geom ) );
        Assert.assertEquals( 1, eventHandler.getEvents().size() );
        Assert.assertEquals( ValidationEventType.SURFACE_EXTERIOR_RING_CW, eventHandler.getEvents().get( 0 ) );
    }

    @Test
    public void validateInvalidPolygon2()
                            throws XMLStreamException, FactoryConfigurationError, IOException, XMLParsingException,
                            UnknownCRSException {
        TestValidationEventHandler eventHandler = new TestValidationEventHandler();
        GeometryValidator validator = new GeometryValidator( eventHandler );
        Geometry geom = parseGeometry( "invalid/Polygon_interiors_counterclockwise.gml" );
        Assert.assertFalse( validator.validateGeometry( geom ) );
        Assert.assertEquals( 2, eventHandler.getEvents().size() );
        Assert.assertEquals( ValidationEventType.SURFACE_INTERIOR_RING_CCW, eventHandler.getEvents().get( 0 ) );
        Assert.assertEquals( ValidationEventType.SURFACE_INTERIOR_RING_CCW, eventHandler.getEvents().get( 1 ) );
    }

    @Test
    public void validateInvalidPolygon3()
                            throws XMLStreamException, FactoryConfigurationError, IOException, XMLParsingException,
                            UnknownCRSException {
        TestValidationEventHandler eventHandler = new TestValidationEventHandler();
        GeometryValidator validator = new GeometryValidator( eventHandler );
        Geometry geom = parseGeometry( "invalid/Polygon_exterior_not_closed.gml" );
        Assert.assertFalse( validator.validateGeometry( geom ) );
        Assert.assertEquals( 1, eventHandler.getEvents().size() );
        Assert.assertEquals( ValidationEventType.RING_NOT_CLOSED, eventHandler.getEvents().get( 0 ) );
    }

    @Test
    public void validateInvalidPolygon4()
                            throws XMLStreamException, FactoryConfigurationError, IOException, XMLParsingException,
                            UnknownCRSException {
        TestValidationEventHandler eventHandler = new TestValidationEventHandler();
        GeometryValidator validator = new GeometryValidator( eventHandler );
        Geometry geom = parseGeometry( "invalid/Polygon_interior_outside_exterior.gml" );
        Assert.assertFalse( validator.validateGeometry( geom ) );
        Assert.assertEquals( 1, eventHandler.getEvents().size() );
        Assert.assertEquals( ValidationEventType.SURFACE_INTERIOR_RING_OUTSIDE_EXTERIOR,
                             eventHandler.getEvents().get( 0 ) );
    }

    @Test
    public void validateInvalidPolygon5()
                            throws XMLStreamException, FactoryConfigurationError, IOException, XMLParsingException,
                            UnknownCRSException {
        TestValidationEventHandler eventHandler = new TestValidationEventHandler();
        GeometryValidator validator = new GeometryValidator( eventHandler );
        Geometry geom = parseGeometry( "invalid/Polygon_interiors_touch.gml" );
        Assert.assertFalse( validator.validateGeometry( geom ) );
        Assert.assertEquals( ValidationEventType.SURFACE_INTERIOR_RINGS_INTERSECT, eventHandler.getEvents().get( 0 ) );
    }

    @Test
    public void validateInvalidPolygon6()
                            throws XMLStreamException, FactoryConfigurationError, IOException, XMLParsingException,
                            UnknownCRSException {
        TestValidationEventHandler eventHandler = new TestValidationEventHandler();
        GeometryValidator validator = new GeometryValidator( eventHandler );
        Geometry geom = parseGeometry( "invalid/Polygon_interiors_intersect.gml" );
        Assert.assertFalse( validator.validateGeometry( geom ) );
        Assert.assertEquals( ValidationEventType.SURFACE_INTERIOR_RINGS_INTERSECT, eventHandler.getEvents().get( 0 ) );
    }

    @Test
    public void validateInvalidPolygon7()
                            throws XMLStreamException, FactoryConfigurationError, IOException, XMLParsingException,
                            UnknownCRSException {
        TestValidationEventHandler eventHandler = new TestValidationEventHandler();
        GeometryValidator validator = new GeometryValidator( eventHandler );
        Geometry geom = parseGeometry( "invalid/Polygon_interior_outside_exterior.gml" );
        Assert.assertFalse( validator.validateGeometry( geom ) );
        Assert.assertEquals( ValidationEventType.SURFACE_INTERIOR_RING_OUTSIDE_EXTERIOR,
                             eventHandler.getEvents().get( 0 ) );
    }

    @Test
    public void validateInvalidPolygon8()
                            throws XMLStreamException, FactoryConfigurationError, IOException, XMLParsingException,
                            UnknownCRSException {
        TestValidationEventHandler eventHandler = new TestValidationEventHandler();
        GeometryValidator validator = new GeometryValidator( eventHandler );
        Geometry geom = parseGeometry( "invalid/Polygon_interior_touches_exterior.gml" );
        Assert.assertFalse( validator.validateGeometry( geom ) );
        Assert.assertEquals( ValidationEventType.SURFACE_INTERIOR_RING_INTERSECTS_EXTERIOR,
                             eventHandler.getEvents().get( 0 ) );
    }

    @Test
    public void validateInvalidPolygon9()
                            throws XMLStreamException, FactoryConfigurationError, IOException, XMLParsingException,
                            UnknownCRSException {
        TestValidationEventHandler eventHandler = new TestValidationEventHandler();
        GeometryValidator validator = new GeometryValidator( eventHandler );
        Geometry geom = parseGeometry( "invalid/Polygon_interior_intersects_exterior.gml" );
        Assert.assertFalse( validator.validateGeometry( geom ) );
        Assert.assertEquals( ValidationEventType.SURFACE_INTERIOR_RING_INTERSECTS_EXTERIOR,
                             eventHandler.getEvents().get( 0 ) );
    }

    private Geometry parseGeometry( String fileName ) throws XMLStreamException, FactoryConfigurationError, IOException, XMLParsingException, UnknownCRSException {
        URL docURL = GML3GeometryReaderTest.class.getResource( BASE_DIR + fileName );
        return GMLInputFactory.createGMLStreamReader( GMLVersion.GML_31, docURL ).readGeometry();
    }
}

class TestValidationEventHandler implements GeometryValidationEventHandler {

    private List<ValidationEventType> events = new ArrayList<ValidationEventType>();

    @Override
    public boolean curveDiscontinuity( Curve curve, int segmentIdx, List<Object> affectedGeometryParticles ) {
        events.add( ValidationEventType.CURVE_DISCONTINUITY );
        printAffectedGeometryParticles( affectedGeometryParticles );
        return false;
    }

    @Override
    public boolean curvePointDuplication( Curve curve, Point point, List<Object> affectedGeometryParticles ) {
        events.add( ValidationEventType.CURVE_DUPLICATE_POINT );
        printAffectedGeometryParticles( affectedGeometryParticles );
        return false;
    }

    @Override
    public boolean curveSelfIntersection( Curve curve, Point location, List<Object> affectedGeometryParticles ) {
        events.add( ValidationEventType.CURVE_SELF_INTERSECTION );
        printAffectedGeometryParticles( affectedGeometryParticles );
        return false;
    }

    @Override
    public boolean exteriorRingCW( PolygonPatch patch, List<Object> affectedGeometryParticles ) {
        events.add( ValidationEventType.SURFACE_EXTERIOR_RING_CW );
        printAffectedGeometryParticles( affectedGeometryParticles );
        return false;
    }

    @Override
    public boolean interiorRingCCW( PolygonPatch patch, List<Object> affectedGeometryParticles ) {
        events.add( ValidationEventType.SURFACE_INTERIOR_RING_CCW );
        printAffectedGeometryParticles( affectedGeometryParticles );
        return false;
    }

    @Override
    public boolean interiorRingIntersectsExterior( PolygonPatch patch, int ringIdx,
                                                   List<Object> affectedGeometryParticles ) {
        events.add( ValidationEventType.SURFACE_INTERIOR_RING_INTERSECTS_EXTERIOR );
        printAffectedGeometryParticles( affectedGeometryParticles );
        return false;
    }

    @Override
    public boolean interiorRingOutsideExterior( PolygonPatch patch, int ringIdx, List<Object> affectedGeometryParticles ) {
        events.add( ValidationEventType.SURFACE_INTERIOR_RING_OUTSIDE_EXTERIOR );
        printAffectedGeometryParticles( affectedGeometryParticles );
        return false;
    }

    @Override
    public boolean interiorRingTouchesExterior( PolygonPatch patch, int ringIdx, List<Object> affectedGeometryParticles ) {
        events.add( ValidationEventType.SURFACE_INTERIOR_RING_TOUCHES_EXTERIOR );
        printAffectedGeometryParticles( affectedGeometryParticles );
        return false;
    }

    @Override
    public boolean interiorRingsIntersect( PolygonPatch patch, int ring1Idx, int ring2Idx,
                                           List<Object> affectedGeometryParticles ) {
        events.add( ValidationEventType.SURFACE_INTERIOR_RINGS_INTERSECT );
        printAffectedGeometryParticles( affectedGeometryParticles );
        return false;
    }

    @Override
    public boolean interiorRingsTouch( PolygonPatch patch, int ring1Idx, int ring2Idx,
                                       List<Object> affectedGeometryParticles ) {
        events.add( ValidationEventType.SURFACE_INTERIOR_RINGS_TOUCH );
        printAffectedGeometryParticles( affectedGeometryParticles );
        return false;
    }

    @Override
    public boolean interiorRingsWithin( PolygonPatch patch, int ring1Idx, int ring2Idx,
                                        List<Object> affectedGeometryParticles ) {
        events.add( ValidationEventType.SURFACE_INTERIOR_RINGS_NESTED );
        printAffectedGeometryParticles( affectedGeometryParticles );
        return false;
    }

    @Override
    public boolean ringNotClosed( Ring ring, List<Object> affectedGeometryParticles ) {
        events.add( ValidationEventType.RING_NOT_CLOSED );
        printAffectedGeometryParticles( affectedGeometryParticles );
        return false;
    }

    @Override
    public boolean ringSelfIntersection( Ring ring, Point location, List<Object> affectedGeometryParticles ) {
        events.add( ValidationEventType.RING_SELF_INTERSECTION );
        printAffectedGeometryParticles( affectedGeometryParticles );
        return false;
    }

    List<ValidationEventType> getEvents() {
        return events;
    }

    private void printAffectedGeometryParticles( List<Object> affectedGeometryParticles ) {
        String indent = "";
        for ( Object object : affectedGeometryParticles ) {
            System.out.println( indent + "-" + object );
            indent += "  ";
        }
    }
}
