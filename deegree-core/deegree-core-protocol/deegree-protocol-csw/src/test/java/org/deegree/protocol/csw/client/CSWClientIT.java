//$HeadURL: svn+ssh://lbuesching@svn.wald.intevation.de/deegree/base/trunk/resources/eclipse/files_template.xml $
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2015 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -

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
package org.deegree.protocol.csw.client;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.deegree.filter.Filter;
import org.deegree.filter.xml.Filter100XMLDecoder;
import org.deegree.protocol.csw.client.getrecords.GetRecords;
import org.deegree.protocol.csw.client.getrecords.GetRecordsResponse;
import org.deegree.protocol.ows.exception.OWSExceptionReport;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by tf on 14.01.15.
 */
public class CSWClientIT {

    private final String capabilitiesUrlWithSoapAndPostSupport = "http://www.paikkatietohakemisto.fi/geonetwork/srv/csw?service=CSW&request=GetCapabilities&version=2.0.2";

    private final String capabilitiesUrlWithGetSupport = "http://inspire-geoportal.ec.europa.eu/GeoportalProxyWebServices/resources/OGCCSW202/sandbox/INSPIRE-88351fbe-05f3-11e1-b7de-52540004b857_20140918-194100/services/1?service=CSW&request=GetCapabilities&version=2.0.2&preserveTemplateEndpoints=true";

    private CSWClient clientWithSoapAndPostSupport;

    private CSWClient clientWithGetSupport;

    private GetRecords requestWithDefaultValues;

    private GetRecords requestWithFilter;

    @Before
    public void setUp()
                            throws IOException, XMLStreamException, OWSExceptionReport {
        clientWithSoapAndPostSupport = new CSWClient( new URL( capabilitiesUrlWithSoapAndPostSupport ) );
        clientWithGetSupport = new CSWClient( new URL( capabilitiesUrlWithGetSupport ) );
        requestWithDefaultValues = new CSWClient.GetRecordsBuilder().startingAt( 1 ).withMax( 20 ).build();
        Filter contraint = readFilter();
        requestWithFilter = new CSWClient.GetRecordsBuilder().startingAt( 1 ).withMax( 20 ).withConstraint( contraint ).build();
    }

    @Test
    public void verifyThatGetRecordsRequestWithHttpPostWorks()
                            throws Exception {
        GetRecordsResponse response = clientWithSoapAndPostSupport.performGetRecordsRequest( requestWithDefaultValues,
                                                                                             CSWClient.GetRecordsRequestType.POST );
        assertEquals( 200, response.getResponse().getAsHttpResponse().getStatusLine().getStatusCode() );
    }

    @Test
    public void verifyThatGetRecordsRequestWithHttpGetWorks()
                            throws Exception {
        GetRecordsResponse response = clientWithGetSupport.performGetRecordsRequest( requestWithDefaultValues,
                                                                                     CSWClient.GetRecordsRequestType.GET );
        assertEquals( 200, response.getResponse().getAsHttpResponse().getStatusLine().getStatusCode() );
    }

    @Test
    public void verifyThatGetRecordsRequestWithSoapWorks()
                            throws Exception {
        GetRecordsResponse response = clientWithSoapAndPostSupport.performGetRecordsRequest( requestWithDefaultValues,
                                                                                             CSWClient.GetRecordsRequestType.SOAP );
        assertEquals( 200, response.getResponse().getAsHttpResponse().getStatusLine().getStatusCode() );
    }

    @Test
    public void verifyThatGetRecordsRequestWithHttpGetAndConstraintWorks()
                            throws Exception {
        GetRecordsResponse response = clientWithGetSupport.performGetRecordsRequest( requestWithFilter,
                                                                                     CSWClient.GetRecordsRequestType.GET );
        assertEquals( 200, response.getResponse().getAsHttpResponse().getStatusLine().getStatusCode() );
    }

    @Test
    public void verifyThatGetRecordsRequestWithPreferredEncodingWorks()
                            throws Exception {
        GetRecordsResponse response = clientWithSoapAndPostSupport.getRecords( requestWithDefaultValues );
        assertEquals( 200, response.getResponse().getAsHttpResponse().getStatusLine().getStatusCode() );
    }

    private Filter readFilter()
                            throws XMLStreamException, FactoryConfigurationError {
        InputStream filterAsStream = CSWClientIT.class.getResourceAsStream( "getrecords/simpleFilter.xml" );
        XMLStreamReader filterReader = XMLInputFactory.newInstance().createXMLStreamReader( filterAsStream );
        filterReader.nextTag();
        return Filter100XMLDecoder.parse( filterReader );
    }

}