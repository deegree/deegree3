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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.slf4j.Logger;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.deegree.commons.utils.io.Utils.determineSimilarity;
import static org.deegree.commons.utils.net.HttpUtils.STREAM;
import static org.deegree.commons.utils.net.HttpUtils.retrieve;
import static org.junit.Assert.assertEquals;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * <code>WMTSIntegrationTest</code>
 *
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @author last edited by: $Author: mschneider $
 * @version $Revision: 31882 $, $Date: 2011-09-15 02:05:04 +0200 (Thu, 15 Sep 2011) $
 */

@RunWith(Parameterized.class)
public class WmtsGetTileSimilarityIT extends AbstractWmtsSimilarityIT {

    private static final Logger LOG = getLogger( WmtsGetTileSimilarityIT.class );

    protected final BufferedImage expected;

    public WmtsGetTileSimilarityIT( String resourceName )
                    throws IOException {
        super( resourceName, "/getTile" );
        this.expected = ImageIO.read(
                        WmtsGetFeatureInfoSimilarityIT.class.getResourceAsStream(
                                        "/getTile/" + resourceName + ".response" ) );
    }

    @Parameters
    public static Collection<Object[]> getParameters() {
        List<Object[]> requests = new ArrayList<>();
        requests.add( new Object[] { "cache1" } );
        requests.add( new Object[] { "cache2" } );
        requests.add( new Object[] { "filesystem1" } );
        requests.add( new Object[] { "filesystem2" } );
        requests.add( new Object[] { "remotewms1" } );
        requests.add( new Object[] { "remotewms130" } );
        requests.add( new Object[] { "remotewms_cache" } );
        requests.add( new Object[] { "remotewms_cache2" } );
        //requests.add( new Object[] { "remotewms_gif" } );
        requests.add( new Object[] { "remotewmts" } );
        requests.add( new Object[] { "transparent" } );
        requests.add( new Object[] { "uncached1" } );
        requests.add( new Object[] { "uncached2" } );
        requests.add( new Object[] { "utah4326" } );
        requests.add( new Object[] { "utah4326_130" } );
        return requests;
    }

    @Test
    public void testSimilarity()
                    throws IOException {
        String request = createRequest();
        InputStream in = retrieve( STREAM, request );
        LOG.info( "Requesting {}", request );
        double sim = determineSimilarity( ImageIO.read( in ), expected );
        assertEquals( "Images are not similar enough for " + resourceName + ", request: " + request + ".", 1.0, sim,
                      0.01 );
    }

}
