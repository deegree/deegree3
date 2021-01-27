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

import static org.deegree.commons.utils.io.Utils.determineSimilarity;
import static org.deegree.commons.utils.net.HttpUtils.STREAM;
import static org.deegree.commons.utils.net.HttpUtils.retrieve;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;

import javax.imageio.ImageIO;

import org.apache.commons.io.IOUtils;
import org.deegree.commons.utils.math.MathUtils;
import org.deegree.commons.utils.test.IntegrationTestUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * <code>WMSSimilarityIntegrationTest</code>
 * 
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @author last edited by: $Author: mschneider $
 * 
 * @version $Revision: 31882 $, $Date: 2011-09-15 02:05:04 +0200 (Thu, 15 Sep 2011) $
 */

@RunWith(Parameterized.class)
public class WMSSimilarityIntegrationTest {

    private static int numFailed = 0;

    private String request;

    private List<byte[]> response;

    private String name;

    public WMSSimilarityIntegrationTest( Object wasXml, String request, List<byte[]> response, String name ) {
        // we only use .kvp for WMS
        this.request = request;
        this.name = name;
        if ( !this.request.contains( "?" ) ) {
            this.request = "?" + this.request;
        }
        this.response = response;
    }

    @Parameters(name = "{index}: {3}")
    public static Collection<Object[]> getParameters() {
        return IntegrationTestUtils.getTestRequests();
    }

    @Test
    public void testSimilarity()
                            throws IOException {
        String base = "http://localhost:" + System.getProperty( "portnumber", "8080" ) + "/";
        base += System.getProperty( "deegree-wms-similarity-webapp", "deegree-wms-similarity-tests" );
        base += "/services" + request;
        InputStream in = retrieve( STREAM, base );

        byte[] bs = null;

        ListIterator<byte[]> iter = response.listIterator();
        while ( iter.hasNext() ) {
            byte[] resp = iter.next();
            try {
                BufferedImage img2 = ImageIO.read( new ByteArrayInputStream( resp ) );
                bs = IOUtils.toByteArray( in );
                BufferedImage img1 = ImageIO.read( new ByteArrayInputStream( bs ) );
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ImageIO.write( img1, "tif", bos );
                bos.close();
                in = new ByteArrayInputStream( bs = bos.toByteArray() );
                bos = new ByteArrayOutputStream();
                bos.close();
                ImageIO.write( img2, "tif", bos );
                iter.set( bos.toByteArray() );
            } catch ( Throwable t ) {
                t.printStackTrace();
                // just compare initial byte arrays
            }
        }

        double sim = 0;
        for ( byte[] response : this.response ) {
            in = new ByteArrayInputStream( bs );
            if ( MathUtils.isZero( sim ) || Math.abs( 1.0 - sim ) > 0.01 || Double.isNaN( sim ) ) {
                sim = Math.max( sim, determineSimilarity( in, new ByteArrayInputStream( response ) ) );
            }
        }

        if ( Math.abs( 1.0 - sim ) > 0.01 ) {
            System.out.println( "Trying to store request/response for " + name + " in tempdir: expected/response"
                                + ++numFailed + ".tif" );
            
            try {
                int idx = 0;
                for ( byte[] response : this.response ) {
                    IOUtils.write( response, new FileOutputStream( System.getProperty( "java.io.tmpdir" ) + "/expected"
                                                                   + ++idx + "_" + numFailed + ".tif" ) );
                }
                IOUtils.write( bs, new FileOutputStream( System.getProperty( "java.io.tmpdir" ) + "/response"
                                                         + numFailed + ".tif" ) );

                System.out.println( "Result returned for " + name + " (base64 -di encoded.dat > failed-test.zip)" );
                System.out.println( IntegrationTestUtils.toBase64Zip( bs, name + ".tif" ) );
            } catch ( Throwable t ) {
            }
        }
        Assert.assertEquals( "Images are not similar enough for " + name + ". Request: " + request, 1.0, sim, 0.01 );
    }
}
