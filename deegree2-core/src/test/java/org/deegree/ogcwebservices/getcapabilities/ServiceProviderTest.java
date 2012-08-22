//$HeadURL: svn+ssh://developername@svn.wald.intevation.org/deegree/base/trunk/test/junit/org/deegree/ogcwebservices/getcapabilities/ServiceProviderTest.java $
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
package org.deegree.ogcwebservices.getcapabilities;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import junit.framework.TestCase;

import org.deegree.datatypes.xlink.SimpleLink;
import org.deegree.model.metadata.iso19115.Address;
import org.deegree.model.metadata.iso19115.ContactInfo;
import org.deegree.model.metadata.iso19115.Linkage;
import org.deegree.model.metadata.iso19115.OnlineResource;
import org.deegree.model.metadata.iso19115.Phone;
import org.deegree.model.metadata.iso19115.TypeCode;

/**
 * @author Administrator
 *
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class ServiceProviderTest extends TestCase {

    public static void main(String[] args) {
    }

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

    public void testGetter() throws MalformedURLException, URISyntaxException {
        ServiceProvider bean = getTestInstance();
        assertEquals("lat/lon GmbH", bean.getProviderName());
        assertNotNull(bean.getContactInfo());
        assertNotNull(bean.getProviderSite());
        assertNotNull(bean.getIndividualName());
        assertNotNull(bean.getPositionName());
    }

    public static ServiceProvider getTestInstance() throws URISyntaxException,
            MalformedURLException {
        return new ServiceProvider("lat/lon GmbH", new SimpleLink(new URI(
                "http://www.latlon.de")), "", "", new ContactInfo(new Address(
                "NRW", "Bonn", "Germany", new String[] { "Aennchenstr. 19",
                        "basement" }, new String[] { "info@lat-lon.de" },
                "53177"), "personal", "9am-17pm", new OnlineResource(
                new Linkage(new URL("mailto:info@latlon.de"))), new Phone(
                new String[] { "++49 228 18496-29" }, new String[] {},
                new String[] {}, new String[] { "++49 228 18496-0" })),
                new TypeCode("PointOfContact", null));

    }

}
