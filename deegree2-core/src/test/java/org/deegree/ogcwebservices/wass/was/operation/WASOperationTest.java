//$HeadURL: svn+ssh://developername@svn.wald.intevation.org/deegree/base/trunk/test/junit/org/deegree/ogcwebservices/wass/was/operation/WASOperationTest.java $
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

package org.deegree.ogcwebservices.wass.was.operation;

import static org.deegree.ogcwebservices.OGCRequestFactory.createFromKVP;
import static org.deegree.ogcwebservices.wass.common.WASServiceFactory.getUncachedWAService;
import static org.deegree.ogcwebservices.wass.common.WASServiceFactory.setConfiguration;

import java.io.File;
import java.net.URL;
import java.util.HashMap;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.ogcwebservices.OGCWebServiceException;
import org.deegree.ogcwebservices.wass.common.CloseSession;
import org.deegree.ogcwebservices.wass.common.GetSession;
import org.deegree.ogcwebservices.wass.common.SessionOperationsDocument;
import org.deegree.ogcwebservices.wass.common.XMLFactory;
import org.deegree.ogcwebservices.wass.was.WAService;
import org.deegree.ogcwebservices.wass.was.capabilities.WASCapabilities;
import org.deegree.ogcwebservices.wass.was.capabilities.WASCapabilitiesDocument;
import org.deegree.security.session.MemoryBasedSessionManager;
import org.deegree.security.session.Session;
import org.deegree.security.session.SessionID;

/**
 * A <code>WASOperationTest</code> class test the various operation of a wass.waservice. The services include
 * <ul>
 * <li>SamlRespons (not implemented yet), if you are looking for an implementation please contact Andreas Poth lat/lon
 * GmbH Aennchenstrasse 19 53177 Bonn Germany E-Mail: poth@lat-lon.de</li>
 * <li>GetSession</li>
 * <li>CloseSession</li>
 * <li>GetCapabilities</li>
 * </ul>
 * 
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * 
 * @author last edited by: $Author: aschmitz $
 * 
 * @version 2.0, $Revision: 29296 $, $Date: 2011-01-13 11:52:34 +0100 (Do, 13 Jan 2011) $
 * 
 * @since 2.0
 */

public class WASOperationTest extends TestCase {

    private final String resourceLocation = "resources/wass/was/example/deegree/";

    private static final ILogger LOG = LoggerFactory.getLogger( WASOperationTest.class );

    private WAService service;

    private boolean skip;

    @Override
    protected void setUp()
                            throws Exception {
        super.setUp();
        try {
            // hardcoded, but not to the local file system ;-)
            File file = new File( resourceLocation + "example_was_capabilities.xml" );
            URL url = file.toURL();
            setConfiguration( url );
            service = getUncachedWAService();
            LOG.logInfo( "Setting up WAS...done." );
        } catch ( OGCWebServiceException e ) {
            skip = true;
        } catch ( Exception e ) {
            throw e;
        }
    }

    @Override
    protected void tearDown()
                            throws Exception {
        super.tearDown();
    }

    /**
     * @return the Test
     */
    public static Test suite() {
        return new TestSuite( WASOperationTest.class );
    }

    /**
     * @param request
     * @throws Exception
     */
    public void doGetSession( GetSession request )
                            throws Exception {
        if ( skip ) {
            LOG.logInfo( "Skipping WAS service test (no database available?)." );
            return;
        }
        assertNotNull( "GetSession request has not been instantiated", request );
        String id = service.doService( request ).toString();
        assertNotNull( "Didn't get a Session ID", id );
    }

    /**
     * @throws Exception
     */
    public void testGetSession()
                            throws Exception {
        if ( skip ) {
            LOG.logInfo( "Skipping WAS service test (no database available?)." );
            return;
        }
        assertNotNull( "Service not initialized", service );
        SessionOperationsDocument doc = new SessionOperationsDocument();
        assertNotNull( "Could not create empty GetSession document", doc );
        File xml = new File( resourceLocation + "requests/GetSession/GetSessionExample_1.xml" );
        doc.load( xml.toURL() );
        GetSession request = doc.parseGetSession( "2", doc.getRootElement() );
        doGetSession( request );
    }

    /**
     * @throws Exception
     */
    public void testGetSessionKVP()
                            throws Exception {
        if ( skip ) {
            LOG.logInfo( "Skipping WAS service test (no database available?)." );
            return;
        }
        HashMap<String, String> map = new HashMap<String, String>();
        map.put( "SERVICE", "WAS" );
        map.put( "VERSION", "1.0.0" );
        map.put( "REQUEST", "GETSESSION" );
        map.put( "AUTHMETHOD", "urn:x-gdi-nrw:authnMethod:1.0:password" );
        map.put( "CREDENTIALS", "poth,poth" );
        GetSession request = new GetSession( "2", map );
        doGetSession( request );
    }

    /**
     * @param request
     * @throws Exception
     */
    public void doCloseSession( CloseSession request )
                            throws Exception {
        if ( skip ) {
            LOG.logInfo( "Skipping WAS service test (no database available?)." );
            return;
        }
        assertNotNull( "CloseSession request has not been instantiated", request );
        MemoryBasedSessionManager sessionManager = MemoryBasedSessionManager.getInstance();
        sessionManager.addSession( new Session( new SessionID( request.getSessionID(), 200000 ) ) );
        assertNull( "Session was not closed.", service.doService( request ) );
    }

    /**
     * @throws Exception
     */
    public void testCloseSession()
                            throws Exception {
        if ( skip ) {
            LOG.logInfo( "Skipping WAS service test (no database available?)." );
            return;
        }
        assertNotNull( "Service not initialized", service );
        SessionOperationsDocument doc = new SessionOperationsDocument();
        assertNotNull( "Could not create empty session document", doc );
        File xml = new File( resourceLocation + "requests/CloseSession/CloseSessionExample_1.xml" );
        doc.load( xml.toURL() );
        CloseSession request = doc.parseCloseSession( "2", doc.getRootElement() );
        doCloseSession( request );
    }

    /**
     * @throws Exception
     */
    public void testCloseSessionKVP()
                            throws Exception {
        if ( skip ) {
            LOG.logInfo( "Skipping WAS service test (no database available?)." );
            return;
        }
        HashMap<String, String> map = new HashMap<String, String>();
        map.put( "SERVICE", "WAS" );
        map.put( "VERSION", "1.0.0" );
        map.put( "REQUEST", "CLOSESESSION" );
        map.put( "SESSIONID", "E0E7E9B1DCDF00B48DB887CC72B43896" );
        CloseSession request = new CloseSession( "2", map );
        doCloseSession( request );
    }

    /**
     * @param request
     * @throws Exception
     */
    public void doGetCapabilities( WASGetCapabilities request )
                            throws Exception {
        if ( skip ) {
            LOG.logInfo( "Skipping WAS service test (no database available?)." );
            return;
        }
        assertNotNull( "GetCapabilities request has not been instantiated", request );
        Object response = service.doService( request );
        assertNotNull( "Response was null", response );
        assertTrue( "Response is not of type WASCapabilities", response instanceof WASCapabilities );
        WASCapabilities cap = (WASCapabilities) response;
        WASCapabilitiesDocument resDoc = XMLFactory.export( cap );
        assertNotNull( "Could not parse back the capabilities", resDoc );
        resDoc.prettyPrint( System.out );
    }

    /**
     * @throws Exception
     */
    public void testGetCapabilities()
                            throws Exception {
        if ( skip ) {
            LOG.logInfo( "Skipping WAS service test (no database available?)." );
            return;
        }
        assertNotNull( "Service not initialized", service );
        WASGetCapabilitiesDocument doc = new WASGetCapabilitiesDocument();
        assertNotNull( "Could not create empty GetCapabilities document", doc );
        File xml = new File( resourceLocation + "requests/GetCapabilities/GetCapabilitiesExample_1.xml" );
        doc.load( xml.toURL() );
        WASGetCapabilities request = doc.parseCapabilities( "1", doc.getRootElement() );
        doGetCapabilities( request );
    }

    /**
     * @throws Exception
     */
    public void testGetCapabilitiesKVP()
                            throws Exception {
        if ( skip ) {
            LOG.logInfo( "Skipping WAS service test (no database available?)." );
            return;
        }
        HashMap<String, String> map = new HashMap<String, String>();
        map.put( "SERVICE", "WAS" );
        map.put( "VERSION", "1.0.0" );
        map.put( "REQUEST", "GetCapabilities" );
        WASGetCapabilities req = (WASGetCapabilities) createFromKVP( map );
        System.out.println( req );
    }

}
