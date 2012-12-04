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
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.validation.event.CurveDiscontinuity;
import org.deegree.geometry.validation.event.CurveSelfIntersection;
import org.deegree.geometry.validation.event.ExteriorRingOrientation;
import org.deegree.geometry.validation.event.InteriorRingIntersectsExterior;
import org.deegree.geometry.validation.event.InteriorRingOrientation;
import org.deegree.geometry.validation.event.InteriorRingOutsideExterior;
import org.deegree.geometry.validation.event.InteriorRingsIntersect;
import org.deegree.geometry.validation.event.RingNotClosed;
import org.deegree.geometry.validation.event.GeometryValidationEvent;
import org.deegree.gml.GMLInputFactory;
import org.deegree.gml.GMLVersion;
import org.deegree.gml.geometry.GML3GeometryReaderTest;
import org.junit.Test;

/**
 * Testcases that check the correct determination of topological properties by the {@link GeometryValidator}.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
public class GeometryValidatorTest {

    private static final String BASE_DIR = "../../gml/misc/geometry/";

    @Test
    public void validateCurve()
                            throws XMLStreamException, FactoryConfigurationError, IOException, XMLParsingException,
                            UnknownCRSException {
        DummyValidationEventHandler eventHandler = new DummyValidationEventHandler();
        GeometryValidator validator = new GeometryValidator( eventHandler );
        Geometry geom = parseGeometry( "Curve.gml" );
        Assert.assertTrue( validator.validateGeometry( geom ) );
        Assert.assertTrue( eventHandler.getEvents().isEmpty() );
    }

    @Test
    public void validateCurveDiscontinuity()
                            throws XMLStreamException, FactoryConfigurationError, IOException, XMLParsingException,
                            UnknownCRSException {
        DummyValidationEventHandler eventHandler = new DummyValidationEventHandler();
        GeometryValidator validator = new GeometryValidator( eventHandler );
        Geometry geom = parseGeometry( "invalid/Curve_discontinuity.gml" );
        Assert.assertTrue( validator.validateGeometry( geom ) );
        Assert.assertEquals( 1, eventHandler.getEvents().size() );
        Assert.assertEquals( CurveDiscontinuity.class, eventHandler.getEvents().get( 0 ).getClass() );
    }

    @Test
    public void validateRing()
                            throws XMLStreamException, FactoryConfigurationError, IOException, XMLParsingException,
                            UnknownCRSException {
        DummyValidationEventHandler eventHandler = new DummyValidationEventHandler();
        GeometryValidator validator = new GeometryValidator( eventHandler );
        Geometry geom = parseGeometry( "Ring.gml" );
        Assert.assertTrue( validator.validateGeometry( geom ) );
        Assert.assertTrue( eventHandler.getEvents().isEmpty() );
    }

    @Test
    public void validateRingNotClosed()
                            throws XMLStreamException, FactoryConfigurationError, IOException, XMLParsingException,
                            UnknownCRSException {
        DummyValidationEventHandler eventHandler = new DummyValidationEventHandler();
        GeometryValidator validator = new GeometryValidator( eventHandler );
        Geometry geom = parseGeometry( "invalid/Ring_not_closed.gml" );
        Assert.assertTrue( "Geometry must be recognized as invalid.", validator.validateGeometry( geom ) );
        Assert.assertEquals( 1, eventHandler.getEvents().size() );
        Assert.assertEquals( RingNotClosed.class, eventHandler.getEvents().get( 0 ).getClass() );
    }

    @Test
    public void validateRingSelfIntersection()
                            throws XMLStreamException, FactoryConfigurationError, IOException, XMLParsingException,
                            UnknownCRSException {
        DummyValidationEventHandler eventHandler = new DummyValidationEventHandler();
        GeometryValidator validator = new GeometryValidator( eventHandler );
        Geometry geom = parseGeometry( "invalid/Ring_self_intersection.gml" );
        Assert.assertTrue( "Geometry must be recognized as invalid.", validator.validateGeometry( geom ) );
        Assert.assertEquals( 1, eventHandler.getEvents().size() );
        Assert.assertEquals( CurveSelfIntersection.class, eventHandler.getEvents().get( 0 ).getClass() );
    }

    @Test
    public void validateInvalidRingNotClosedAndSelfIntersection()
                            throws XMLStreamException, FactoryConfigurationError, IOException, XMLParsingException,
                            UnknownCRSException {
        DummyValidationEventHandler eventHandler = new DummyValidationEventHandler();
        GeometryValidator validator = new GeometryValidator( eventHandler );
        Geometry geom = parseGeometry( "invalid/Ring_not_closed_and_self_intersection.gml" );
        Assert.assertTrue( "Geometry must be recognized as invalid.", validator.validateGeometry( geom ) );
        Assert.assertEquals( 2, eventHandler.getEvents().size() );
        Assert.assertEquals( CurveSelfIntersection.class, eventHandler.getEvents().get( 0 ).getClass() );
        Assert.assertEquals( RingNotClosed.class, eventHandler.getEvents().get( 1 ).getClass() );
    }

    @Test
    public void validatePolygon()
                            throws XMLStreamException, FactoryConfigurationError, IOException, XMLParsingException,
                            UnknownCRSException {
        DummyValidationEventHandler eventHandler = new DummyValidationEventHandler();
        GeometryValidator validator = new GeometryValidator( eventHandler );
        Geometry geom = parseGeometry( "Polygon.gml" );
        Assert.assertTrue( validator.validateGeometry( geom ) );
        Assert.assertEquals( 3, eventHandler.getEvents().size() );
        Assert.assertFalse( ( (ExteriorRingOrientation) eventHandler.getEvents().get( 0 ) ).isClockwise() );
        Assert.assertTrue( ( (InteriorRingOrientation) eventHandler.getEvents().get( 1 ) ).isClockwise() );
        Assert.assertTrue( ( (InteriorRingOrientation) eventHandler.getEvents().get( 2 ) ).isClockwise() );
    }

    @Test
    public void validatePolygonExteriorClockwise()
                            throws XMLStreamException, FactoryConfigurationError, IOException, XMLParsingException,
                            UnknownCRSException {
        DummyValidationEventHandler eventHandler = new DummyValidationEventHandler();
        GeometryValidator validator = new GeometryValidator( eventHandler );
        Geometry geom = parseGeometry( "invalid/Polygon_exterior_clockwise.gml" );
        Assert.assertTrue( validator.validateGeometry( geom ) );
        Assert.assertEquals( 3, eventHandler.getEvents().size() );
        Assert.assertTrue( ( (ExteriorRingOrientation) eventHandler.getEvents().get( 0 ) ).isClockwise() );
        Assert.assertTrue( ( (InteriorRingOrientation) eventHandler.getEvents().get( 1 ) ).isClockwise() );
        Assert.assertTrue( ( (InteriorRingOrientation) eventHandler.getEvents().get( 2 ) ).isClockwise() );
    }

    @Test
    public void validatePolygonInteriorsCounterClockwise()
                            throws XMLStreamException, FactoryConfigurationError, IOException, XMLParsingException,
                            UnknownCRSException {
        DummyValidationEventHandler eventHandler = new DummyValidationEventHandler();
        GeometryValidator validator = new GeometryValidator( eventHandler );
        Geometry geom = parseGeometry( "invalid/Polygon_interiors_counterclockwise.gml" );
        Assert.assertTrue( validator.validateGeometry( geom ) );
        Assert.assertEquals( 3, eventHandler.getEvents().size() );
        Assert.assertFalse( ( (ExteriorRingOrientation) eventHandler.getEvents().get( 0 ) ).isClockwise() );
        Assert.assertFalse( ( (InteriorRingOrientation) eventHandler.getEvents().get( 1 ) ).isClockwise() );
        Assert.assertFalse( ( (InteriorRingOrientation) eventHandler.getEvents().get( 2 ) ).isClockwise() );
    }

    @Test
    public void validatePolygonExteriorNotClosed()
                            throws XMLStreamException, FactoryConfigurationError, IOException, XMLParsingException,
                            UnknownCRSException {
        DummyValidationEventHandler eventHandler = new DummyValidationEventHandler();
        GeometryValidator validator = new GeometryValidator( eventHandler );
        Geometry geom = parseGeometry( "invalid/Polygon_exterior_not_closed.gml" );
        Assert.assertTrue( validator.validateGeometry( geom ) );
        Assert.assertEquals( 1, eventHandler.getEvents().size() );
        Assert.assertEquals( RingNotClosed.class, eventHandler.getEvents().get( 0 ).getClass() );
    }

    @Test
    public void validatePolygonInteriorOutsideExterior()
                            throws XMLStreamException, FactoryConfigurationError, IOException, XMLParsingException,
                            UnknownCRSException {
        DummyValidationEventHandler eventHandler = new DummyValidationEventHandler();
        GeometryValidator validator = new GeometryValidator( eventHandler );
        Geometry geom = parseGeometry( "invalid/Polygon_interior_outside_exterior.gml" );
        Assert.assertTrue( validator.validateGeometry( geom ) );
        Assert.assertEquals( 3, eventHandler.getEvents().size() );
        Assert.assertEquals( InteriorRingOutsideExterior.class, eventHandler.getEvents().get( 2 ).getClass() );
    }

    // @Test
    // public void validatePolygonInteriorsTouch()
    // throws XMLStreamException, FactoryConfigurationError, IOException, XMLParsingException,
    // UnknownCRSException {
    // DummyValidationEventHandler eventHandler = new DummyValidationEventHandler();
    // GeometryValidator validator = new GeometryValidator( eventHandler );
    // Geometry geom = parseGeometry( "invalid/Polygon_interiors_touch.gml" );
    // Assert.assertTrue( validator.validateGeometry( geom ) );
    // Assert.assertEquals( 4, eventHandler.getEvents().size() );
    // Assert.assertEquals( InteriorRingsTouch.class, eventHandler.getEvents().get( 3 ).getClass() );
    // }

    @Test
    public void validateInvalidPolygonInteriorsIntersect()
                            throws XMLStreamException, FactoryConfigurationError, IOException, XMLParsingException,
                            UnknownCRSException {
        DummyValidationEventHandler eventHandler = new DummyValidationEventHandler();
        GeometryValidator validator = new GeometryValidator( eventHandler );
        Geometry geom = parseGeometry( "invalid/Polygon_interiors_intersect.gml" );
        Assert.assertTrue( validator.validateGeometry( geom ) );
        Assert.assertEquals( 4, eventHandler.getEvents().size() );
        Assert.assertEquals( InteriorRingsIntersect.class, eventHandler.getEvents().get( 3 ).getClass() );
    }

    // @Test
    // public void validatePolygonInteriorTouchesExterior()
    // throws XMLStreamException, FactoryConfigurationError, IOException, XMLParsingException,
    // UnknownCRSException {
    // DummyValidationEventHandler eventHandler = new DummyValidationEventHandler();
    // GeometryValidator validator = new GeometryValidator( eventHandler );
    // Geometry geom = parseGeometry( "invalid/Polygon_interior_touches_exterior.gml" );
    // Assert.assertTrue( validator.validateGeometry( geom ) );
    // Assert.assertEquals( 4, eventHandler.getEvents().size() );
    // Assert.assertEquals( InteriorRingTouchesExterior.class, eventHandler.getEvents().get( 3 ).getClass() );
    // }

    @Test
    public void validatePolygonInteriorIntersectsExterior()
                            throws XMLStreamException, FactoryConfigurationError, IOException, XMLParsingException,
                            UnknownCRSException {
        DummyValidationEventHandler eventHandler = new DummyValidationEventHandler();
        GeometryValidator validator = new GeometryValidator( eventHandler );
        Geometry geom = parseGeometry( "invalid/Polygon_interior_intersects_exterior.gml" );
        Assert.assertTrue( validator.validateGeometry( geom ) );
        Assert.assertEquals( InteriorRingIntersectsExterior.class, eventHandler.getEvents().get( 3 ).getClass() );
    }

    private Geometry parseGeometry( String fileName )
                            throws XMLStreamException, FactoryConfigurationError, IOException, XMLParsingException,
                            UnknownCRSException {
        URL docURL = GML3GeometryReaderTest.class.getResource( BASE_DIR + fileName );
        return GMLInputFactory.createGMLStreamReader( GMLVersion.GML_31, docURL ).readGeometry();
    }
}

class DummyValidationEventHandler implements GeometryValidationEventHandler {

    private final List<GeometryValidationEvent> events = new ArrayList<GeometryValidationEvent>();

    @Override
    public boolean fireEvent( GeometryValidationEvent event ) {
        events.add( event );
        return true;
    }

    List<GeometryValidationEvent> getEvents() {
        return events;
    }

}
