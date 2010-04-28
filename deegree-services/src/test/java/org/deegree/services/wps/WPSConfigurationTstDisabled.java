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

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.xml.NamespaceContext;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.XPath;
import org.deegree.services.jaxb.wps.PublishedInformation;
import org.deegree.services.jaxb.wps.ServiceConfiguration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 *
 *
 * @author <a href="mailto:apadberg@uni-bonn.de">Alexander Padberg</a>
 * @author last edited by: $Author: $
 *
 * @version $Revision: $, $Date: $
 *
 */
public class WPSConfigurationTstDisabled {

    /**
     * @throws Exception
     */
    @Before
    public void setUp()
                            throws Exception {
        // nothing to do
    }

    /**
     * @throws Exception
     */
    @After
    public void tearDown()
                            throws Exception {
        // nothing to do
    }

    /**
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testConfigurationParsing()
                            throws Exception {
        NamespaceContext nsContext = new NamespaceContext();
        nsContext.addNamespace( "wps", "http://www.deegree.org/schemas/services/wps" );

        XMLAdapter xmlAdapter = new XMLAdapter(
                                                new File(
                                                          "resources/schema/example/conf/wps/wps_configuration.xml" ) );


        OMElement publishedInformationElement = xmlAdapter.getRequiredElement( xmlAdapter.getRootElement(),
                                                                        new XPath( "wps:PublishedInformation",
                                                                                   nsContext ) );
        JAXBContext jc = JAXBContext.newInstance( "org.deegree.services.controller.wps.configuration" );
        Unmarshaller u = jc.createUnmarshaller();
        PublishedInformation pi = (PublishedInformation) u.unmarshal( publishedInformationElement.getXMLStreamReaderWithoutCaching() );
        System.out.println( pi.getOfferedVersions().getVersion().get( 0 ) );

        OMElement serviceConfigurationElement = xmlAdapter.getRequiredElement( xmlAdapter.getRootElement(),
                                                                               new XPath( "wps:ServiceConfiguration",
                                                                                          nsContext ) );
        jc = JAXBContext.newInstance( "org.deegree.services.wps.configuration" );
        u = jc.createUnmarshaller();
        ServiceConfiguration serviceConfig = (ServiceConfiguration) u.unmarshal( serviceConfigurationElement.getXMLStreamReaderWithoutCaching() );
//        System.out.println( serviceConfig.getProcess().get( 0 ).getClazz() );
    }
}
