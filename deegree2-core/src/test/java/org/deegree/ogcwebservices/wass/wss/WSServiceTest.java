//$HeadURL: svn+ssh://developername@svn.wald.intevation.org/deegree/base/trunk/test/junit/org/deegree/ogcwebservices/wass/wss/WSServiceTest.java $
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
package org.deegree.ogcwebservices.wass.wss;

import java.io.File;
import java.net.URL;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.ogcwebservices.OGCWebServiceException;
import org.deegree.ogcwebservices.wass.common.WASServiceFactory;
import org.deegree.ogcwebservices.wass.common.XMLFactory;
import org.deegree.ogcwebservices.wass.wss.capabilities.WSSCapabilities;

/**
 * General test class for the WSS.
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author: aschmitz $
 * 
 * @version 2.0, $Revision: 29294 $, $Date: 2011-01-13 11:44:57 +0100 (Do, 13 Jan 2011) $
 * 
 * @since 2.0
 */

public class WSServiceTest extends TestCase {

    private static final ILogger LOG = LoggerFactory.getLogger( WSServiceTest.class );

    private WSService service;

    private final String resourceLocation = "./resources/wass/wss/example/deegree/";

    private boolean skip;

    /**
     * Constructor for WSServiceTest.
     * 
     * @param arg0
     */
    public WSServiceTest( String arg0 ) {
        super( arg0 );
    }

    /*
     * @see TestCase#setUp()
     */
    @Override
    protected void setUp()
                            throws Exception {
        super.setUp();
        try {
            // hardcoded, but not to the local file system ;-)
            File file = new File( resourceLocation + "example_wss_capabilities.xml" );
            URL url = file.toURL();
            WASServiceFactory.setConfiguration( url );
            service = WASServiceFactory.getUncachedWSService();
            LOG.logInfo( "Setting up WSS...done." );
        } catch ( OGCWebServiceException e ) {
            skip = true;
        } catch ( Exception e ) {
            throw e;
        }
    }

    /*
     * @see TestCase#tearDown()
     */
    @Override
    protected void tearDown()
                            throws Exception {
        super.tearDown();
    }

    /**
     * @return the Test
     */
    public static Test suite() {
        return new TestSuite( WSServiceTest.class );
    }

    /**
     * @throws Exception
     */
    public void testGetCapabilities()
                            throws Exception {
        if ( skip ) {
            LOG.logInfo( "Skipping WSS service test (no database available?)." );
            return;
        }
        assertNotNull( "Service not initialized", service );
        WSSCapabilities capabilities = (WSSCapabilities) service.getCapabilities();
        assertNotNull( "Capabilities are null", capabilities );
        XMLFragment xml = XMLFactory.export( capabilities );
        assertNotNull( "XMLFragment is null", xml );
        xml.prettyPrint( System.out );
    }

}
