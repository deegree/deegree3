//$HeadURL: svn+ssh://developername@svn.wald.intevation.org/deegree/base/trunk/test/junit/org/deegree/enterprise/servlet/WCSHandlerTest.java $
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
package org.deegree.enterprise.servlet;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;

import org.deegree.enterprise.ServiceException;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.xml.XMLTools;
import org.deegree.ogcwebservices.OGCWebServiceException;
import org.deegree.ogcwebservices.OGCWebServiceRequest;
import org.deegree.ogcwebservices.wcs.WCServiceFactory;
import org.deegree.ogcwebservices.wcs.configuration.WCSConfiguration;
import org.deegree.ogcwebservices.wcs.getcapabilities.WCSGetCapabilities;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import alltests.Configuration;

/**
 * Test with mock object to perform http get/post request.
 *
 * @author <a href="mailto:tfr@users.sourceforge.net">Torsten Friebe </a>
 *
 * @author last edited by: $Author: mschneider $
 *
 * @version 2.0, $Revision: 18195 $, $Date: 2009-06-18 17:55:39 +0200 (Do, 18 Jun 2009) $
 *
 * @since 2.0
 */
public class WCSHandlerTest extends TestCase {

    private static ILogger LOG = LoggerFactory.getLogger( WCSHandlerTest.class );

    private StringWriter writer;

    /*
     * @see TestCase#setUp()
     */
    @Override
    protected void setUp()
                            throws Exception {
        super.setUp();
        WCSConfiguration wcsConfiguration = WCSConfiguration.create( Configuration.getWCSConfigurationURL() );
        WCServiceFactory.setConfiguration( wcsConfiguration );
        writer = new StringWriter();
    }

    /*
     * @see TestCase#tearDown()
     */
    @Override
    protected void tearDown()
                            throws Exception {
        super.tearDown();
        writer.flush();
        writer.close();
        writer = null;
    }

    /**
     * Test the perform method with a GetCapabilities request
     *
     * @throws ServiceException
     * @throws OGCWebServiceException
     * @throws SAXException
     * @throws IOException
     *
     */
    public void testPerformGetCapabilities()
                            throws OGCWebServiceException, ServiceException, IOException, SAXException {
        WCSHandler handler = new WCSHandler();
        assertNotNull( handler );
        WCSGetCapabilities request = new WCSGetCapabilities( this.toString(), "1.0.0", "0", new String[] {}, null );
        HttpServletResponse response = new MockHttpServletResponse( writer );
        handler.perform( request, response );
        String responseAsXml = writer.toString();
        LOG.logDebug( responseAsXml );
        assertNotNull( responseAsXml );
        assertTrue( responseAsXml.length() > 0 );
        StringReader reader = new StringReader( responseAsXml );
        Document responseAsDom = XMLTools.parse( reader );
        assertNotNull( responseAsDom );
        assertEquals( "WCS_Capabilities", responseAsDom.getDocumentElement().getNodeName() );

    }

    /**
     * @throws OGCWebServiceException
     * @throws ServiceException
     * @throws IOException
     * @throws SAXException
     */
    public void testPerformGetCapabilitiesSection()
                            throws OGCWebServiceException, ServiceException, IOException, SAXException {
        WCSHandler handler = new WCSHandler();
        assertNotNull( handler );
        OGCWebServiceRequest request = new WCSGetCapabilities( this.toString(), "1.0.0", "0",
                                                               new String[] { "/WCS_Capabilities/Service" }, null );
        HttpServletResponse response = new MockHttpServletResponse( writer );
        handler.perform( request, response );
        String responseAsXml = writer.toString();
        LOG.logDebug( responseAsXml );
        assertNotNull( responseAsXml );
        assertTrue( responseAsXml.length() > 0 );
        StringReader reader = new StringReader( responseAsXml );
        Document responseAsDom = XMLTools.parse( reader );
        assertNotNull( responseAsDom );
        assertEquals( "Service", responseAsDom.getDocumentElement().getNodeName() );
    }

    /**
     * @throws ServiceException
     */
    public void testGetService()
                            throws ServiceException {
        WCSHandler handler = new WCSHandler();
        assertNotNull( handler.getService() );
    }

}
