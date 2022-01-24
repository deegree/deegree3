//$HeadURL: svn+ssh://aschmitz@wald.intevation.org/deegree/deegree3/trunk/deegree-core/deegree-core-base/src/main/java/org/deegree/protocol/wms/client/WMSClient111.java $
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
package org.deegree.protocol.wms.client;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.stream.XMLStreamException;

import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.XMLProcessingException;
import org.deegree.protocol.ows.exception.OWSExceptionReport;
import org.junit.Test;

/**
 * 
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 * @author last edited by: $Author: lgoltz $
 * 
 * @version $Revision: 31860 $, $Date: 2011-09-13 15:11:47 +0200 (Di, 13. Sep 2011) $
 */

public class WMSClientTest {

    @Test
    public void testWMS111InstantiationWithXMLAdapter()
                            throws XMLProcessingException, IOException, OWSExceptionReport, XMLStreamException {
        InputStream is = WMSClientTest.class.getResourceAsStream( "wms111.xml" );
        new WMSClient( new XMLAdapter( is ) );
    }

    @Test
    public void testWMS130InstantiationWithXMLAdapter()
                            throws XMLProcessingException, IOException, OWSExceptionReport, XMLStreamException {
        InputStream is = WMSClientTest.class.getResourceAsStream( "wms130.xml" );
        new WMSClient( new XMLAdapter( is ) );
    }

    @Test
    public void testWMS111InstantiationFromUrl()
                            throws OWSExceptionReport, XMLStreamException, MalformedURLException, IOException {
        URL capaUrl = new URL(
                               "https://deegree3-demo.deegree.org/utah-workspace/services?request=GetCapabilities&service=WMS&version=1.1.1" );
        // TODO: check if demo WMS available
        new WMSClient( capaUrl );
    }
}
