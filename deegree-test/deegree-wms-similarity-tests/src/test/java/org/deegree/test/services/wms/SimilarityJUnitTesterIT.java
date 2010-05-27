//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
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

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.test.services.wms;

import static java.lang.Math.abs;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.util.LinkedList;
import java.util.List;

import javax.imageio.ImageIO;

import junit.framework.TestCase;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.deegree.commons.utils.PixelCounter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * <code>SimilarityJUnitTesterIT</code>
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
@RunWith(Parameterized.class)
public class SimilarityJUnitTesterIT extends TestCase {

    private final String url;

    private final BigInteger[] vals;

    private static final String baseurl = System.getProperty( "wms.baseurl" );

    /**
     * @param url
     * @param vals
     */
    public SimilarityJUnitTesterIT( String url, BigInteger[] vals ) {
        this.url = url;
        this.vals = vals;
    }

    /**
     * @return the url/biginteger values pairs
     * @throws IOException
     */
    @Parameters
    public static List<Object[]> getResultSnippets()
                            throws IOException {

        List<Object[]> snippets = new LinkedList<Object[]>();

        InputStream ftpin = SimilarityJUnitTesterIT.class.getResourceAsStream( "footprints.txt" );
        BufferedReader in = new BufferedReader( new InputStreamReader( ftpin ) );
        InputStream tstin = SimilarityJUnitTesterIT.class.getResourceAsStream( "similaritytests.txt" );
        BufferedReader urls = new BufferedReader( new InputStreamReader( tstin ) );
        String url;
        while ( ( url = urls.readLine() ) != null ) {
            String[] footprints = in.readLine().split( " " );
            BigInteger[] vals = new BigInteger[] { new BigInteger( footprints[0] ), new BigInteger( footprints[1] ),
                                                  new BigInteger( footprints[2] ), new BigInteger( footprints[3] ) };
            snippets.add( new Object[] { url, vals } );
        }
        in.close();
        urls.close();

        return snippets;
    }

    /**
     * @throws IOException
     * @throws MalformedURLException
     * 
     */
    @Test
    public void singleTest()
                            throws MalformedURLException, IOException {
        // TODO find a better value here once some java instance (locally or remote) has been updated and it yields
        // different values. Currently my tests always yield the same images (value == 1.0f).
        HttpClient client = new HttpClient();
        PostMethod post = new PostMethod( baseurl );
        post.setRequestEntity( new StringRequestEntity( url, "application/x-www-form-urlencoded", "UTF-8" ) );
        client.executeMethod( post );
        InputStream in = post.getResponseBodyAsStream();
        BufferedImage image = ImageIO.read( in );
        in.close();
        assertNotNull( "Image was null, probably the request image could not be read.", image );
        double lvl = PixelCounter.similarityLevel( image, vals );
        assertTrue( "The test failed with an actual similarity of " + lvl + ", the request was " + url,
                    abs( 1 - lvl ) < 0.00001 );
    }

}
