//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
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

 Occam Labs UG (haftungsbeschränkt)
 Godesberger Allee 139, 53175 Bonn
 Germany
 http://www.occamlabs.de/

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/

package org.deegree.services.wms;

import org.apache.commons.io.IOUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.slf4j.Logger;
import org.xmlunit.matchers.CompareMatcher;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.deegree.commons.utils.net.HttpUtils.UTF8STRING;
import static org.deegree.commons.utils.net.HttpUtils.retrieve;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * <code>RemoteWMSIntegrationTest</code>
 *
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @author last edited by: $Author: mschneider $
 * @version $Revision: 31882 $, $Date: 2011-09-15 02:05:04 +0200 (Thu, 15 Sep 2011) $
 */

@Ignore
@RunWith(Parameterized.class)
public class RemoteWMSGfiIntegrationTest {

    private static final Logger LOG = getLogger( RemoteWMSGfiIntegrationTest.class );

    private final String resourceName;

    private final String request;

    private final String expected;

    public RemoteWMSGfiIntegrationTest( String resourceName )
                    throws IOException {
        this.resourceName = resourceName;
        this.request = IOUtils.toString(
                        RemoteWMSGfiIntegrationTest.class.getResourceAsStream(
                                        "/requests/" + resourceName + ".kvp" ) );
        this.expected = IOUtils.toString(
                        RemoteWMSGfiIntegrationTest.class.getResourceAsStream(
                                        "/requests/" + resourceName + ".xml" ) );
    }

    @Parameters(name = "{index}: {0}")
    public static Collection<Object[]> getParameters() {
        List<Object[]> requests = new ArrayList<>();
        requests.add( new Object[] { "featureinfofromdeegree" } );
        return requests;
    }

    @Test
    public void testGfiResponse()
                    throws
                    Exception {
        String request = createRequest();
        LOG.info( "Requesting {}", request );
        String actual = retrieve( UTF8STRING, request );

        assertThat( "GFI Response for " + resourceName + " does not match expected response", actual,
                    CompareMatcher.isSimilarTo( expected ).ignoreWhitespace() );
    }

    private String createRequest() {
        StringBuffer sb = new StringBuffer();
        sb.append( "http://localhost:" );
        sb.append( "8090" );
        sb.append( "/" );
        sb.append( System.getProperty( "deegree-wms-remoteows-webapp", "deegree-wms-remoteows-tests" ) );
        sb.append( "/services/wms" );
        if ( !request.startsWith( "?" ) )
            sb.append( "?" );
        sb.append( request );
        return sb.toString();
    }
}
