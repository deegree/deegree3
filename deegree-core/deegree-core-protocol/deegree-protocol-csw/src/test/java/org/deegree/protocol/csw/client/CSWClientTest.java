package org.deegree.protocol.csw.client;

import org.deegree.commons.tom.ows.Version;
import org.deegree.commons.xml.CommonNamespaces;
import org.deegree.protocol.csw.CSWConstants;
import org.deegree.protocol.csw.client.getrecords.GetRecords;
import org.deegree.protocol.csw.client.getrecords.GetRecordsResponse;
import org.deegree.protocol.ows.exception.OWSExceptionReport;
import org.junit.Before;
import org.junit.Test;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

/**
 * Created by tf on 14.01.15.
 */
public class CSWClientTest {

    //private final String capabilitiesUrl = "http://www.paikkatietohakemisto.fi/geonetwork/srv/csw?service=CSW&request=GetCapabilities&version=2.0.2";
    private final String capabilitiesUrl = "http://inspire-geoportal.ec.europa.eu/GeoportalProxyWebServices/resources/OGCCSW202/sandbox/INSPIRE-88351fbe-05f3-11e1-b7de-52540004b857_20140918-194100/services/1?service=CSW&request=GetCapabilities&version=2.0.2&preserveTemplateEndpoints=true";

    private CSWClient client;

    private GetRecords requestWithDefaultValues;

    @Before
    public void setUp() throws IOException, XMLStreamException, OWSExceptionReport {
        client = new CSWClient( new URL(capabilitiesUrl) );
        requestWithDefaultValues = new CSWClient.GetRecordsBuilder().startingAt( 1 ).withMax( 20 ).build();
    }

    @Test
    public void verifyThatGetRecordsRequestWithHttpPostWorks() throws XMLStreamException, IOException, OWSExceptionReport {
        GetRecordsResponse response = client.performGetRecordsRequest( requestWithDefaultValues, CSWClient.GetRecordsRequestType.POST );
        assertEquals( 200, response.getResponse().getAsHttpResponse().getStatusLine().getStatusCode() );
    }

    @Test
    public void verifyThatGetRecordsRequestWithHttpGetWorks() throws XMLStreamException, IOException, OWSExceptionReport {
        GetRecordsResponse response = client.performGetRecordsRequest( requestWithDefaultValues, CSWClient.GetRecordsRequestType.GET );
        assertEquals( 200, response.getResponse().getAsHttpResponse().getStatusLine().getStatusCode() );
    }

    @Test
    public void verifyThatGetRecordsRequestWithSoapWorks() throws XMLStreamException, IOException, OWSExceptionReport {
        GetRecordsResponse response = client.performGetRecordsRequest( requestWithDefaultValues, CSWClient.GetRecordsRequestType.SOAP );
        assertEquals( 200, response.getResponse().getAsHttpResponse().getStatusLine().getStatusCode() );
    }

    @Test
    public void verifyThatGetRecordsRequestWithPreferredEncodingWorks() throws XMLStreamException, IOException, OWSExceptionReport {
        GetRecordsResponse response = client.getRecords( requestWithDefaultValues );
        assertEquals( 200, response.getResponse().getAsHttpResponse().getStatusLine().getStatusCode() );
    }
}
