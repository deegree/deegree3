//$HeadURL: svn+ssh://developername@svn.wald.intevation.org/deegree/base/trunk/test/junit/alltests/ResourceTest.java $
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
package alltests;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import junit.framework.TestCase;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;

/**
 * Test for all resource files.
 *
 * @author <a href="mailto:tfr@users.sourceforge.net">Torsten Friebe </a>
 *
 * @author last edited by: $Author: mschneider $
 *
 * @version 2.0, $Revision: 18195 $, $Date: 2009-06-18 17:55:39 +0200 (Do, 18 Jun 2009) $
 *
 * @since 2.0
 */
public class ResourceTest extends TestCase {
    private static ILogger LOG = LoggerFactory.getLogger( ResourceTest.class );

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testUserBuildProperties() throws MalformedURLException {
        assertNotNull(Configuration.PROTOCOL);
        assertNotNull(Configuration.HOST);
        assertTrue(Configuration.PORT>0);
        URL testServerUrl = new URL(Configuration.PROTOCOL,Configuration.HOST,Configuration.PORT,"");
        assertNotNull(testServerUrl);
        LOG.logInfo("deegree test server: "+testServerUrl.toString());
    }

    public void testAllResourcesAvailable() throws MalformedURLException {
        //CSW
        assertNotNull(Configuration.getCSWConfigurationURL());
        File cswCapabilitiesFile = new File(Configuration.getCSWConfigurationURL().getFile());
        assertTrue(cswCapabilitiesFile.exists());
        assertTrue(cswCapabilitiesFile.canRead());
        File internalWFSConfigFile = new File(Configuration.getCSWInternalWFSConfigurationURL()
                .getFile());
        assertTrue(internalWFSConfigFile.exists());
        assertTrue(internalWFSConfigFile.canRead());
        //WFS
        assertNotNull(Configuration.getWFSConfigurationURL());
        File wfsCapabilitiesFile = new File(Configuration.getWFSConfigurationURL().getFile());
        assertTrue(wfsCapabilitiesFile.exists());
        assertTrue(wfsCapabilitiesFile.canRead());
        //WMS
        assertNotNull(Configuration.getWMSConfigurationURL());
        File wmsCapabilitiesFile = new File(Configuration.getWMSConfigurationURL().getFile());
        assertTrue(wmsCapabilitiesFile.exists());
        assertTrue(wmsCapabilitiesFile.canRead());
        //WCS
        assertNotNull(Configuration.getWCSConfigurationURL());
        File wcsCapabilitiesFile = new File(Configuration.getWCSConfigurationURL().getFile());
        assertTrue(wcsCapabilitiesFile.exists());
        assertTrue(wcsCapabilitiesFile.canRead());
    }

}
