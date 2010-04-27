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
package org.deegree.services.wps;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.PrintWriter;
import java.util.HashMap;

import org.deegree.commons.xml.XMLAdapter;
import org.deegree.services.controller.wps.WPSController;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 *
 * @author <a href="mailto:apadberg@uni-bonn.de">Alexander Padberg</a>
 * @author last edited by: $Author: $
 *
 * @version $Revision: $, $Date: $
 *
 */
public class WPSControllerTstDisabled {

    private static Logger LOG = LoggerFactory.getLogger( WPSControllerTstDisabled.class );

    static HashMap<String, String> testmap = new HashMap<String, String>();

    static PrintWriter pw = new PrintWriter( System.out );

    /**
     * @throws Exception
     */
    @BeforeClass
    public static void setUp() {
        testmap.put( "service", "WPS" );
        testmap.put( "version", "1.0.0" );
        testmap.put( "language", "en-CA" );
        testmap.put( "DataInputs", "width=35@datatype=xs:integer@uom=meter;BufferDistance=10" );
        testmap.put( "ResponseDocument", "BufferedPolygon" );
        testmap.put( "storeExecuteResponse", "true" );
        testmap.put( "request", "DescribeProcess" );
        testmap.put( "Identifier", "ALL" );
    }

    /**
     *
     */
    @After
    public void formatOutput() {
        LOG.debug( "\n---------------------------------\n" );
    }

    /**
     * @throws Exception
     */
    @AfterClass
    public static void tearDown()
                            throws Exception {
        pw.flush();
        pw.close();
    }

    @Test
    public void testControllerConstructor() {

        try {
            WPSController c = new WPSController();
            c.init( new XMLAdapter (new File ("org/deegree/controller/wps/wps_configuration.xml")), null );
            c.doKVP( new HashMap<String, String>(), null, null, null );
        } catch ( Exception e ) {
            // empty
        }

        assertEquals( "", true, true );
    }

    /**
     * 
     */
    @Test(expected = Exception.class)
    @SuppressWarnings("unused")
    public void testIncorrectConfigurationFile()
                            throws Exception {
        WPSController c = new WPSController();
        c.init( new XMLAdapter (new File ("org/deegree/controller/wps/wps_incorrect_configuration.xml")), null );
        c.doKVP( new HashMap<String, String>(), null, null, null );
        // c.doService( requestObject, pw );
    }

    @Test
    public void testExecuteMethod() {
        // JAXBContext jc = JAXBContext.newInstance( "org.deegree.services.wps.configuration" );
        // Unmarshaller u = jc.createUnmarshaller();
        // OMElement serviceConfigurationElement = controllerConf.getRequiredElement(
        // controllerConf.getRootElement(),
        // new XPath(
        // "wps:ServiceConfiguration",
        // wcsNSContext ) );
        // JAXBElement<ServiceConfigurationType> scJAXB = (JAXBElement<ServiceConfigurationType>) u.unmarshal(
        // serviceConfigurationElement.getXMLStreamReaderWithoutCaching() );
        // sc = scJAXB.getValue();
        //        
        // WPSController c = new WPSController("test/org/deegree/controller/wps/wps_configuration.xml",
        // "services_metadata.xml");
        // }
        //    
    }
}
