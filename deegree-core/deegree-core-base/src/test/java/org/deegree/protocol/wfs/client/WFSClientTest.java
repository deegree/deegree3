//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2011 by:
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
package org.deegree.protocol.wfs.client;

import static org.deegree.protocol.wfs.WFSVersion.WFS_100;
import static org.deegree.protocol.wfs.WFSVersion.WFS_110;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.net.URL;

import org.deegree.commons.utils.test.TestProperties;
import org.deegree.protocol.ows.metadata.Address;
import org.deegree.protocol.ows.metadata.ContactInfo;
import org.deegree.protocol.ows.metadata.ServiceContact;
import org.deegree.protocol.ows.metadata.ServiceIdentification;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests the {@link WFSClient} against various WFS server instances.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class WFSClientTest {

    private static Logger LOG = LoggerFactory.getLogger( WFSClientTest.class );

    private static final String WFS_UTAH_DEMO_100_URL = "wfs.utahdemo100.url";

    private static final String WFS_UTAH_DEMO_110_URL = "wfs.utahdemo110.url";

    @Test
    public void testCapabilitiesExtraction100()
                            throws Exception {

        String wfsUtahDemo100Url = TestProperties.getProperty( WFS_UTAH_DEMO_100_URL );
        if ( wfsUtahDemo100Url == null ) {
            LOG.warn( "Skipping test, property '" + WFS_UTAH_DEMO_100_URL + "' not found in ~/.deegree-test.properties" );
            return;
        }

        URL wfsCapaUrl = new URL( wfsUtahDemo100Url );
        WFSClient client = new WFSClient( wfsCapaUrl );
        assertEquals( WFS_100, client.getServiceVersion() );

        // ServiceIdentification
        ServiceIdentification si = client.getIdentification();
        assertEquals( "deegree 3 Utah Demo", si.getDescription().getName() );
        assertEquals( 1, si.getDescription().getTitles().size() );
        assertEquals( "deegree 3 Utah Demo", si.getDescription().getTitles().get( 0 ).getString() );
        assertEquals( null, si.getDescription().getTitles().get( 0 ).getLanguage() );
        assertEquals( 1, si.getDescription().getAbstracts().size() );
        assertEquals( "WMS and WFS demonstration with Utah data",
                      si.getDescription().getAbstracts().get( 0 ).getString() );
        assertEquals( null, si.getDescription().getAbstracts().get( 0 ).getLanguage() );
        assertEquals( 0, si.getDescription().getKeywords().size() );
        assertNull( si.getFees() );

        // ServiceProvider
        assertEquals( "http://www.deegree.org", client.getProvider().getProviderSite() );

        // OperationMetadata (TODO)
    }

    @Test
    public void testCapabilitiesExtraction110()
                            throws Exception {

        String wfsUtahDemo110Url = TestProperties.getProperty( WFS_UTAH_DEMO_110_URL );
        if ( wfsUtahDemo110Url == null ) {
            LOG.warn( "Skipping test, property '" + WFS_UTAH_DEMO_110_URL + "' not found in ~/.deegree-test.properties" );
            return;
        }

        URL wfsCapaUrl = new URL( wfsUtahDemo110Url );
        WFSClient client = new WFSClient( wfsCapaUrl );
        assertEquals( WFS_110, client.getServiceVersion() );

        // ServiceIdentification
        ServiceIdentification si = client.getIdentification();
        assertNull( si.getDescription().getName() );
        assertEquals( 1, si.getDescription().getTitles().size() );
        assertEquals( "deegree 3 Utah Demo", si.getDescription().getTitles().get( 0 ).getString() );
        assertEquals( null, si.getDescription().getTitles().get( 0 ).getLanguage() );
        assertEquals( 1, si.getDescription().getAbstracts().size() );
        assertEquals( "WMS and WFS demonstration with Utah data",
                      si.getDescription().getAbstracts().get( 0 ).getString() );
        assertEquals( null, si.getDescription().getAbstracts().get( 0 ).getLanguage() );
        assertEquals( 0, si.getDescription().getKeywords().size() );
        assertNull( si.getFees() );

        // ServiceProvider
        assertEquals( "lat/lon GmbH", client.getProvider().getProviderName() );
        assertEquals( "http://www.lat-lon.de", client.getProvider().getProviderSite() );
        ServiceContact sc = client.getProvider().getServiceContact();
        assertEquals( "Andreas Schmitz", sc.getIndividualName() );
        assertEquals( "Software developer", sc.getPositionName() );
        assertEquals( "PointOfContact", sc.getRole().getCode() );
        ContactInfo ci = sc.getContactInfo();
        assertEquals( "http://www.deegree.org", ci.getOnlineResource().toString() );
        assertEquals( "24x7", ci.getHoursOfService() );
        assertEquals( "Do not hesitate to contact us", ci.getContactInstruction() );
        Address add = ci.getAddress();
        assertEquals( "NRW", add.getAdministrativeArea() );
        assertEquals( "Bonn", add.getCity());
        assertEquals( "Germany", add.getCountry());
        assertEquals( 1, add.getDeliveryPoint().size());
        assertEquals( "Aennchenstr. 19", add.getDeliveryPoint().get( 0 ));
        assertEquals( 1, add.getElectronicMailAddress().size());
        assertEquals( "info@lat-lon.de", add.getElectronicMailAddress().get( 0 ));
        assertEquals( "53177", add.getPostalCode());

        // OperationMetadata (TODO)
    }
}
