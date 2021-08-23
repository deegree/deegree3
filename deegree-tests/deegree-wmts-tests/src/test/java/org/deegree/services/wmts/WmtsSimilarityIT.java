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

package org.deegree.services.wmts;

import static org.deegree.commons.utils.io.Utils.determineSimilarity;
import static org.deegree.commons.utils.net.HttpUtils.STREAM;
import static org.deegree.commons.utils.net.HttpUtils.retrieve;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;

import org.deegree.commons.utils.test.IntegrationTestUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.slf4j.Logger;

/**
 * <code>WMTSIntegrationTest</code>
 * 
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @author last edited by: $Author: mschneider $
 * 
 * @version $Revision: 31882 $, $Date: 2011-09-15 02:05:04 +0200 (Thu, 15 Sep 2011) $
 */

@RunWith(Parameterized.class)
public class WmtsSimilarityIT {

    private static final Logger LOG = getLogger( WmtsSimilarityIT.class );

    private String request;

    private List<byte[]> response;

    private String name;

    public WmtsSimilarityIT( Object wasXml, String request, List<byte[]> response, String name ) {
        // we only use .kvp for WMTS
        this.request = request;
        this.response = response;
        this.name = name;
    }

    @Parameters
    public static Collection<Object[]> getParameters() {
        return IntegrationTestUtils.getTestRequests();
    }

    @Test
    public void testSimilarity()
                            throws IOException {
        String base = "http://localhost:" + System.getProperty( "portnumber", "8080" );
        base += "/deegree-wmts-tests/services" + request;
        InputStream in = retrieve( STREAM, base );
        LOG.info( "Requesting {}", base );
        double sim = 0;
        for ( byte[] response : this.response ) {
            sim = Math.max( sim, determineSimilarity( in, new ByteArrayInputStream( response ) ) );
        }
        Assert.assertEquals( "Images are not similar enough for " + name + ", request: " + base + ".", 1.0, sim, 0.01 );
    }

}
