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
package org.deegree.geometry.gml.validation;

import static junit.framework.Assert.assertEquals;
import static org.deegree.gml.GMLVersion.GML_31;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

import junit.framework.Assert;

import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.geometry.validation.event.CurveDiscontinuity;
import org.deegree.geometry.validation.event.ExteriorRingOrientation;
import org.deegree.geometry.validation.event.InteriorRingOrientation;
import org.deegree.geometry.validation.event.RingNotClosed;
import org.deegree.gml.GMLInputFactory;
import org.deegree.gml.GMLStreamReader;
import org.deegree.gml.geometry.GML3GeometryReaderTest;
import org.deegree.gml.geometry.validation.GmlElementIdentifier;
import org.deegree.gml.geometry.validation.GmlGeometryValidationEvent;
import org.deegree.gml.geometry.validation.GmlGeometryValidationEventHandler;
import org.deegree.gml.geometry.validation.GmlStreamGeometryValidator;
import org.junit.Test;

/**
 * Tests that check the expected generation of validation events by the {@link GmlGeometryValidationEvent}.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
public class GmlGeometryValidatorTest {

    private static final String BASE_DIR = "../misc/geometry/";

    @Test
    public void validateCurve()
                            throws XMLStreamException, FactoryConfigurationError, IOException, ClassCastException,
                            UnknownCRSException {
        TestEventHandler eventHandler = validate( "Curve.gml" );
        assertEquals( 0, eventHandler.getEvents().size() );
    }

    @Test
    public void validateCurveDiscontinuity()
                            throws XMLStreamException, FactoryConfigurationError, IOException, ClassCastException,
                            UnknownCRSException {
        TestEventHandler eventHandler = validate( "invalid/Curve_discontinuity.gml" );
        assertEquals( 1, eventHandler.getEvents().size() );
        GmlGeometryValidationEvent event = eventHandler.getEvents().get( 0 );
        Assert.assertEquals( CurveDiscontinuity.class, event.getEvent().getClass() );
    }

    @Test
    public void validateRingNotClosed()
                            throws XMLStreamException, FactoryConfigurationError, IOException, UnknownCRSException {
        TestEventHandler eventHandler = validate( "invalid/Ring_not_closed.gml" );
        assertEquals( 1, eventHandler.getEvents().size() );
        GmlGeometryValidationEvent event = eventHandler.getEvents().get( 0 );
        Assert.assertEquals( RingNotClosed.class, event.getEvent().getClass() );
    }

    @Test
    public void validatePolygonExteriorClockwise()
                            throws XMLStreamException, FactoryConfigurationError, IOException, UnknownCRSException {
        TestEventHandler eventHandler = validate( "invalid/Polygon_exterior_clockwise.gml" );
        assertEquals( 3, eventHandler.getEvents().size() );
        Assert.assertTrue( ( (ExteriorRingOrientation) ( eventHandler.getEvents().get( 0 ).getEvent() ) ).isClockwise() );
        Assert.assertTrue( ( (InteriorRingOrientation) ( eventHandler.getEvents().get( 1 ).getEvent() ) ).isClockwise() );
        Assert.assertTrue( ( (InteriorRingOrientation) ( eventHandler.getEvents().get( 2 ).getEvent() ) ).isClockwise() );
    }

    private TestEventHandler validate( String resourceName )
                            throws XMLStreamException, UnknownCRSException, FactoryConfigurationError, IOException {
        TestEventHandler eventHandler = new TestEventHandler();
        URL resourceUrl = GML3GeometryReaderTest.class.getResource( BASE_DIR + resourceName );
        GMLStreamReader gmlStream = GMLInputFactory.createGMLStreamReader( GML_31, resourceUrl );
        GmlStreamGeometryValidator validator = new GmlStreamGeometryValidator( gmlStream, eventHandler );
        validator.validateGeometries();
        return eventHandler;
    }

}

class TestEventHandler implements GmlGeometryValidationEventHandler {

    private final List<GmlGeometryValidationEvent> events = new ArrayList<GmlGeometryValidationEvent>();

    @Override
    public void parsingError( GmlElementIdentifier geometryElement, Exception e ) {
        // TODO Auto-generated method stub
    }

    @Override
    public boolean topologicalEvent( GmlGeometryValidationEvent event ) {
        events.add( event );
        return false;
    }

    List<GmlGeometryValidationEvent> getEvents() {
        return events;
    }

}
