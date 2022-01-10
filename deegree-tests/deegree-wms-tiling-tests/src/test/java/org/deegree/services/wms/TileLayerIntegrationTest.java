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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

import static java.util.Arrays.asList;
import static org.deegree.commons.utils.io.Utils.determineSimilarity;
import static org.deegree.commons.utils.net.HttpUtils.STREAM;
import static org.deegree.commons.utils.net.HttpUtils.retrieve;
import static org.junit.Assert.assertEquals;

/**
 * <code>TileLayerIT</code>
 *
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @author last edited by: $Author: mschneider $
 * @version $Revision: 31882 $, $Date: 2011-09-15 02:05:04 +0200 (Thu, 15 Sep 2011) $
 */
@RunWith(Parameterized.class)
public class TileLayerIntegrationTest {

    private final BufferedImage expected;

    private final String resourceName;

    private String request;

    public TileLayerIntegrationTest( String resourceName, String request )
                    throws IOException {
        this.expected = ImageIO.read( TileLayerIntegrationTest.class.getResourceAsStream( resourceName ) );
        this.resourceName = resourceName;
        this.request = request;
    }

    @Parameters
    public static Collection<Object[]> getParameters() {
        return asList( new Object[][] {
                        {
                                        "maxextent.png",
                                        "?REQUEST=GetMap&SERVICE=WMS&VERSION=1.1.1&WIDTH=978&HEIGHT=645&LAYERS=pyramid&TRANSPARENT=TRUE&FORMAT=image%2Fpng&BBOX=438742.26976744185,4448455.0,450241.7302325581,4456039.0&SRS=urn:opengis:def:crs:epsg::26912&STYLES=" },
                        {
                                        "second.png",
                                        "?REQUEST=GetMap&SERVICE=WMS&VERSION=1.1.1&WIDTH=978&HEIGHT=645&LAYERS=pyramid&TRANSPARENT=TRUE&FORMAT=image%2Fpng&BBOX=442054.1850365361,4450977.860706333,445834.7259965481,4453471.162259716&SRS=urn:opengis:def:crs:epsg::26912&STYLES=" },
                        {
                                        "third.png",
                                        "?REQUEST=GetMap&SERVICE=WMS&VERSION=1.1.1&WIDTH=978&HEIGHT=645&LAYERS=pyramid&TRANSPARENT=TRUE&FORMAT=image%2Fpng&BBOX=442923.9194703415,4451577.9980723625,444353.2355232765,4452520.645162488&SRS=urn:opengis:def:crs:epsg::26912&STYLES=" } } );
    }

    @Test
    public void testSimilarity()
                    throws IOException {
        String base = createRequest();
        InputStream in = retrieve( STREAM, base );
        double sim = determineSimilarity( ImageIO.read( in ), expected );
        assertEquals( "Images are not similar enough for " + resourceName + ", request: " + request + ".", 1.0, sim,
                      0.001 );
    }

    private String createRequest() {
        StringBuffer sb = new StringBuffer();
        sb.append( "http://localhost:" );
        sb.append( System.getProperty( "portnumber" ) );
        sb.append( "/deegree-wms-tiling-tests/services" );
        sb.append( request );
        return sb.toString();
    }

}
