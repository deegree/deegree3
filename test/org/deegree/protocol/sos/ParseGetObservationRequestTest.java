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
package org.deegree.protocol.sos;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.utils.time.DateUtils;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.XMLProcessingException;
import org.deegree.protocol.sos.filter.DurationFilter;
import org.deegree.protocol.sos.filter.SpatialBBOXFilter;
import org.deegree.protocol.sos.filter.SpatialFilter;
import org.deegree.protocol.sos.filter.TimeFilter;
import org.deegree.protocol.sos.getobservation.GetObservation;
import org.deegree.protocol.sos.getobservation.GetObservation100XMLAdapter;
import org.junit.Test;

/**
 * 
 * 
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
public class ParseGetObservationRequestTest {

    /**
     * parse example from http://schemas.opengis.net/sos/1.0.0/examples/
     * 
     * @throws Exception
     */
    @Test
    public void parseGetObservationRequest1()
                            throws Exception {
        InputStream requestStream = this.getClass().getResourceAsStream( "sosGetObservation1.xml" );

        GetObservation request = new GetObservation100XMLAdapter( getRootElementFromStream( requestStream ) ).parse();

        assertEquals( "EPSG:4326", request.getSRSName() );
        assertEquals( "urn:MyOrg:offering:3", request.getOffering() );
        assertEquals( "urn:ogc:def:property:MyOrg:AggregateChemicalPresence",
                      request.getObservedProperties().get( 0 ).getPropertyName() );
        assertEquals( "text/xml; subtype=\"om/1.0.0\"", request.getResponseFormat() );
        assertEquals( "om:Observation", request.getResultModel() );
        assertEquals( "inline", request.getResponseMode() );

    }

    /**
     * parse example from http://schemas.opengis.net/sos/1.0.0/examples/
     * 
     * @throws Exception
     */
    @Test
    public void parseGetObservationRequest2()
                            throws Exception {
        InputStream requestStream = this.getClass().getResourceAsStream( "sosGetObservation2.xml" );
        GetObservation request = new GetObservation100XMLAdapter( getRootElementFromStream( requestStream ) ).parse();

        assertEquals( "", request.getSRSName() );
        assertEquals( "urn:MyOrg:offering:1", request.getOffering() );
        assertEquals( "urn:ogc:def:phenomenon:OGC:windspeed",
                      request.getObservedProperties().get( 0 ).getPropertyName() );
        assertEquals( "urn:ogc:def:phenomenon:OGC:temperature",
                      request.getObservedProperties().get( 1 ).getPropertyName() );
        assertEquals( "text/xml; subtype=\"om/1.0.0\"", request.getResponseFormat() );
        List<TimeFilter> timeFilters = request.getEventTime();
        assertEquals( 1, timeFilters.size() );
        TimeFilter timeFilter = timeFilters.get( 0 );
        if ( timeFilter instanceof DurationFilter ) {
            DurationFilter durationFilter = (DurationFilter) timeFilter;
            assertTrue( DateUtils.parseISO8601Date( "2008-11-05T17:18:58.000-06:00" ).equals( durationFilter.getBegin() ) );
            assertTrue( DateUtils.parseISO8601Date( "2008-11-05T21:18:59.000-06:00" ).equals( durationFilter.getEnd() ) );
        } else {
            fail( "filter is not parsed as DurationFilter" );
        }

    }

    /**
     * parse example from http://schemas.opengis.net/sos/1.0.0/examples/
     * 
     * @throws Exception
     */
    @Test
    public void parseGetObservationRequest3()
                            throws Exception {
        InputStream requestStream = this.getClass().getResourceAsStream( "sosGetObservation3.xml" );
        GetObservation request = new GetObservation100XMLAdapter( getRootElementFromStream( requestStream ) ).parse();

        assertEquals( "EPSG:4326", request.getSRSName() );
        assertEquals( "urn:MyOrg:offering:3", request.getOffering() );
        assertEquals( "urn:ogc:def:property:MyOrg:AggregateChemicalPresence",
                      request.getObservedProperties().get( 0 ).getPropertyName() );
        assertEquals( "text/xml; subtype=\"om/1.0.0\"", request.getResponseFormat() );
        assertEquals( "om:Observation", request.getResultModel() );
        assertEquals( "inline", request.getResponseMode() );
        assertSpatialBBOXFilter( request.getFeatureOfInterest().second, 38.11, -78.6, 38.14, -78.4 );
    }

    /**
     * parse example from http://schemas.opengis.net/sos/1.0.0/examples/
     * 
     * @throws Exception
     */
    @Test
    public void parseGetObservationRequest4()
                            throws Exception {
        InputStream requestStream = this.getClass().getResourceAsStream( "sosGetObservation4.xml" );
        GetObservation request = new GetObservation100XMLAdapter( getRootElementFromStream( requestStream ) ).parse();

        assertEquals( "", request.getSRSName() );
        assertEquals( "urn:MyOrg:offering:3", request.getOffering() );
        assertEquals( "urn:ogc:object:Sensor:MyOrg:12349", request.getProcedures().get( 0 ).getProcedureName() );
        assertEquals( "urn:ogc:def:property:MyOrg:WindSpeed",
                      request.getObservedProperties().get( 0 ).getPropertyName() );
        assertEquals( "text/xml; subtype=\"om/1.0.0\"", request.getResponseFormat() );
        assertEquals( "om:Observation", request.getResultModel() );
        assertEquals( "resultTemplate", request.getResponseMode() );
        assertSpatialBBOXFilter( request.getFeatureOfInterest().second, 38.11, -78.6, 38.14, -78.4 );
    }

    /**
     * parse example from http://schemas.opengis.net/sos/1.0.0/examples/
     * 
     * @throws Exception
     */
    // @Test don't test. the eventTime uses a format that is not in the schema.
    // the example comes from an annex
    public void parseGetObservationRequest5()
                            throws Exception {
        InputStream requestStream = this.getClass().getResourceAsStream( "sosGetObservation5.xml" );
        GetObservation request = new GetObservation100XMLAdapter( getRootElementFromStream( requestStream ) ).parse();

        assertEquals( "", request.getSRSName() );
        assertEquals( "urn:MyOrg:offering:3", request.getOffering() );
        assertEquals( "urn:ogc:def:property:MyOrg:AggregateChemicalPresence",
                      request.getObservedProperties().get( 0 ).getPropertyName() );
        assertEquals( "text/xml; subtype=\"om/1.0.0\"", request.getResponseFormat() );
        assertEquals( "om:Observation", request.getResultModel() );
        assertEquals( "inline", request.getResponseMode() );
        fail( "parsing of eventTime is not implemented" );
    }

    private void assertSpatialBBOXFilter( SpatialFilter filter, double xmin, double ymin, double xmax, double ymax ) {
        assertTrue( filter.getClass().equals( SpatialBBOXFilter.class ) );
        SpatialBBOXFilter bboxFilter = (SpatialBBOXFilter) filter;
        assertEquals( xmin, bboxFilter.getBBOX().getMin().get0(), 0.001 );
        assertEquals( xmax, bboxFilter.getBBOX().getMax().get0(), 0.001 );
        assertEquals( ymin, bboxFilter.getBBOX().getMin().get1(), 0.001 );
        assertEquals( ymax, bboxFilter.getBBOX().getMax().get1(), 0.001 );
    }

    /**
     * @param stream
     * @return the root element of the input stream
     * @throws XMLProcessingException
     */
    public static OMElement getRootElementFromStream( InputStream stream )
                            throws XMLProcessingException {
        XMLAdapter xmladapter = new XMLAdapter( new InputStreamReader( stream ) );
        return xmladapter.getRootElement();
    }

}
