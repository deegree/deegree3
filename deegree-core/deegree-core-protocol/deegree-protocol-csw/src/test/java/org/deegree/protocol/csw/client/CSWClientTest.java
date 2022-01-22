package org.deegree.protocol.csw.client;

import static org.deegree.commons.xml.CommonNamespaces.APISO;
import static org.deegree.commons.xml.CommonNamespaces.APISO_PREFIX;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Iterator;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import org.deegree.commons.ows.metadata.OperationsMetadata;
import org.deegree.commons.ows.metadata.ServiceIdentification;
import org.deegree.commons.ows.metadata.ServiceProvider;
import org.deegree.commons.ows.metadata.operation.Operation;
import org.deegree.commons.ows.metadata.party.ContactInfo;
import org.deegree.commons.ows.metadata.party.ResponsibleParty;
import org.deegree.commons.tom.primitive.PrimitiveValue;
import org.deegree.commons.utils.test.TestProperties;
import org.deegree.filter.Expression;
import org.deegree.filter.Filter;
import org.deegree.filter.MatchAction;
import org.deegree.filter.Operator;
import org.deegree.filter.OperatorFilter;
import org.deegree.filter.comparison.PropertyIsLike;
import org.deegree.filter.expression.Literal;
import org.deegree.filter.expression.ValueReference;
import org.deegree.filter.logical.Or;
import org.deegree.metadata.MetadataRecord;
import org.deegree.protocol.csw.client.getrecords.GetRecordsResponse;
import org.deegree.protocol.ows.exception.OWSExceptionReport;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

/**
 * JUnit class tests the functionality of the CSW client.
 * You need to set demo_csw_url in $HOME/.deegree-test.properties to a 
 * CSW server getCapabilities URL for this test to work.
 * 
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class CSWClientTest {

    @Test
    public void testMetadata() throws OWSExceptionReport, XMLStreamException, IOException {

        String demoCSWURL = TestProperties.getProperty( "demo_csw_url" );
        Assume.assumeNotNull( demoCSWURL );
        
        URL serviceUrl = new URL( demoCSWURL );

        CSWClient client = new CSWClient(serviceUrl);
        Assert.assertNotNull( client );
        
        ServiceIdentification serviceId = client.getIdentification();
        Assert.assertNotNull( serviceId );
        Assert.assertEquals( 1 , serviceId.getTitles().size() );
        Assert.assertNotNull( serviceId.getTitles().get( 0 ).getString() );
        Assert.assertEquals( 1 , serviceId.getAbstracts().size() );
        Assert.assertNotNull( serviceId.getAbstracts().get( 0 ).getString() );

        Assert.assertEquals( "CSW" , serviceId.getServiceType().getCode() );
        Assert.assertEquals( "2.0.2" , serviceId.getServiceTypeVersion().get( 0 ).toString() );
        
        ServiceProvider serviceProvider = client.getProvider();
        Assert.assertNotNull( serviceProvider.getProviderName() );
        Assert.assertNotNull( serviceProvider.getProviderSite() );

        ResponsibleParty serviceContact = serviceProvider.getServiceContact();
        Assert.assertNotNull( serviceContact.getIndividualName() );
        Assert.assertNotNull( serviceContact.getPositionName() );

        ContactInfo contactInfo = serviceContact.getContactInfo();
        Assert.assertNotNull( contactInfo.getPhone().getVoice().get( 0 ) );
        Assert.assertNotNull( contactInfo.getPhone().getFacsimile().get( 0 ) );
        Assert.assertNotNull( contactInfo.getAddress().getDeliveryPoint().get( 0 ) );
        Assert.assertNotNull( contactInfo.getAddress().getCity() );
        Assert.assertNotNull( contactInfo.getAddress().getAdministrativeArea() );
        Assert.assertNotNull( contactInfo.getAddress().getPostalCode() );
        Assert.assertNotNull( contactInfo.getAddress().getCountry() );
        Assert.assertNotNull( contactInfo.getAddress().getElectronicMailAddress().get( 0 ) );
        Assert.assertNotNull( contactInfo.getHoursOfService() );
        Assert.assertNotNull( contactInfo.getContactInstruction() );

        Assert.assertNotNull( serviceContact.getRole().getCode() );
        
        OperationsMetadata opMetadata = client.getOperations();
        Operation op; 
        
        op = opMetadata.getOperation("GetCapabilities"); 
        Assert.assertNotNull( op.getGetUrls().get( 0 ).toExternalForm() );
        Assert.assertNotNull( op.getPostUrls().get( 0 ).toExternalForm() );
        
        op = opMetadata.getOperation("DescribeRecord"); 
        Assert.assertNotNull( op.getGetUrls().get( 0 ).toExternalForm() );
        Assert.assertNotNull( op.getPostUrls().get( 0 ).toExternalForm() );
        
        op = opMetadata.getOperation("GetRecords"); 
        Assert.assertNotNull( op.getGetUrls().get( 0 ).toExternalForm() );
        Assert.assertNotNull( op.getPostUrls().get( 0 ).toExternalForm() );
        
        op = opMetadata.getOperation("GetRecordById"); 
        Assert.assertNotNull( op.getGetUrls().get( 0 ).toExternalForm() );
        Assert.assertNotNull( op.getPostUrls().get( 0 ).toExternalForm() );

        op = opMetadata.getOperation("Transaction"); 
        Assert.assertNotNull(op);
    }
    
    private Operator createPropertyLikeFilter(String propertyPrefix, String propertyName, String namespaceURI, String value) {
        QName qname = new QName( namespaceURI, propertyName, propertyPrefix );
        Expression param1 = new ValueReference( qname );
        Expression param2 = new Literal<PrimitiveValue>( value );
        Operator rootOperator = new PropertyIsLike( param1, param2, "*", ".", "!", false, MatchAction.ANY );        
        return rootOperator;
    }
    
    @Test
    public void testGetIsoRecords() throws OWSExceptionReport, XMLStreamException, IOException {

        String demoCSWURL = TestProperties.getProperty( "demo_csw_url" );
        Assume.assumeNotNull( demoCSWURL );

        URL serviceUrl = new URL( demoCSWURL );

        CSWClient client = new CSWClient( serviceUrl );
        Assert.assertNotNull( client );
        
        Operator titleFilter1 = createPropertyLikeFilter( APISO_PREFIX, "Title", APISO, "%e%" );       
        Operator titleFilter2 = createPropertyLikeFilter( APISO_PREFIX, "Title", APISO, "%a%" );       
        Operator titleFilter = new Or(titleFilter1, titleFilter2);
        
        int startPosition = 1;
        int maxRecords = 20;
        Filter constraint = new OperatorFilter(titleFilter);        
        
        GetRecordsResponse recordsResponse = client.getIsoRecords(startPosition, maxRecords, constraint);
        Assert.assertNotNull( recordsResponse );        
        Assert.assertTrue( maxRecords >= recordsResponse.getNumberOfRecordsReturned() );
        Assert.assertTrue( recordsResponse.getNumberOfRecordsReturned() >= 1 );
        Assert.assertTrue( recordsResponse.getNumberOfRecordsMatched() >= 1 );
        
        int recordsCounter = 0;
        
        Iterator<MetadataRecord> iter = recordsResponse.getRecords();
        while(iter.hasNext()) {
            MetadataRecord record = iter.next();
            Assert.assertNotNull(record);
            
            System.out.println(String.format("%s * %s * %s * %s * %s",
                    Arrays.toString(record.getTitle()), 
                    record.getIdentifier(), record.getLanguage(), record.getModified(), record.getType()));

            Assert.assertNotNull(record.getTitle()[0]);
            Assert.assertNotNull(record.getAbstract()[0]);
            Assert.assertNotNull(record.getIdentifier());
            Assert.assertNotNull(record.getLanguage());
            Assert.assertNotNull(record.getModified());
            Assert.assertNotNull(record.getType());
            
            recordsCounter++;
        }
        
        Assert.assertTrue(recordsCounter > 0);
    }    
}
