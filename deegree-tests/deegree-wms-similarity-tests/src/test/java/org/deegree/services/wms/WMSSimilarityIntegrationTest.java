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
import org.deegree.commons.utils.test.IntegrationTestUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.deegree.commons.utils.io.Utils.determineSimilarity;
import static org.deegree.commons.utils.net.HttpUtils.IMAGE;
import static org.deegree.commons.utils.net.HttpUtils.retrieve;
import static org.junit.Assert.assertEquals;

/**
 * <code>WMSSimilarityIntegrationTest</code>
 *
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @author last edited by: $Author: mschneider $
 * @version $Revision: 31882 $, $Date: 2011-09-15 02:05:04 +0200 (Thu, 15 Sep 2011) $
 */

@RunWith(Parameterized.class)
public class WMSSimilarityIntegrationTest {

    private static int numFailed = 0;

    private final String resourceName;

    private final String format;

    private final String request;

    private final double tolerance;

    private final BufferedImage expected;

    public WMSSimilarityIntegrationTest( String resourceName, String format, double tolerance )
                    throws IOException {
        this.resourceName = resourceName;
        this.request = IOUtils.toString(
                        WMSSimilarityIntegrationTest.class.getResourceAsStream(
                                        "/requests/" + resourceName + ".kvp" ) );
        this.tolerance = tolerance;
        this.expected = ImageIO.read(
                        WMSSimilarityIntegrationTest.class.getResourceAsStream(
                                        "/requests/" + resourceName + "." + format ) );
        this.format = format;
    }

    @Parameters(name = "{index}: {0}")
    public static Collection<Object[]> getParameters() {
        List<Object[]> requests = new ArrayList<>();
        requests.add( new Object[] { "lines/lines_capbutt", "png", 0.01 } );
        requests.add( new Object[] { "lines/lines_capround", "png", 0.01 } );
        requests.add( new Object[] { "lines/lines_capsquare", "png", 0.01 } );
        requests.add( new Object[] { "lines/lines_centroid", "png", 0.01 } );
        requests.add( new Object[] { "lines/lines_dasharray", "png", 0.01 } );
        requests.add( new Object[] { "lines/lines_dasharrayandoffset", "tif", 0.01 } );
        requests.add( new Object[] { "lines/lines_divmod", "png", 0.01 } );
        requests.add( new Object[] { "lines/lines_filtersamelayer", "png", 0.01 } );
        requests.add( new Object[] { "lines/lines_getcurrentscale", "tif", 0.24 } );
        requests.add( new Object[] { "lines/lines_graphicfill", "png", 0.01 } );
        requests.add( new Object[] { "lines/lines_graphicstroke", "tif", 0.01 } );
        requests.add( new Object[] { "lines/lines_joinbevel", "png", 0.01 } );
        requests.add( new Object[] { "lines/lines_joinmitre", "png", 0.01 } );
        requests.add( new Object[] { "lines/lines_joinround", "png", 0.01 } );
        requests.add( new Object[] { "lines/lines_offset", "png", 0.01 } );
        requests.add( new Object[] { "lines/lines_opacity", "png", 0.01 } );
        requests.add( new Object[] { "lines/lines_pixelsize", "png", 0.01 } );
        requests.add( new Object[] { "lines/lines_width5", "png", 0.01 } );
        requests.add( new Object[] { "points/points_anchor0", "png", 0.01 } );
        requests.add( new Object[] { "points/points_anchor1", "png", 0.01 } );
        requests.add( new Object[] { "points/points_circle16", "png", 0.01 } );
        requests.add( new Object[] { "points/points_circle32", "png", 0.01 } );
        requests.add( new Object[] { "points/points_cross32", "png", 0.01 } );
        requests.add( new Object[] { "points/points_defaultsquare", "png", 0.01 } );
        requests.add( new Object[] { "points/points_displacementandanchorpoint", "png", 0.01 } );
        requests.add( new Object[] { "points/points_displacementx", "png", 0.01 } );
        requests.add( new Object[] { "points/points_displacementxnegative", "png", 0.01 } );
        requests.add( new Object[] { "points/points_displacementy", "png", 0.01 } );
        requests.add( new Object[] { "points/points_displacementynegative", "png", 0.01 } );
        requests.add( new Object[] { "points/points_rotation", "png", 0.01 } );
        requests.add( new Object[] { "points/points_star32", "png", 0.01 } );
        requests.add( new Object[] { "points/points_triangle32", "png", 0.01 } );
        requests.add( new Object[] { "points/points_x32", "png", 0.01 } );
        requests.add( new Object[] { "polygons/polygons_edgedandsubstraction", "tif", 0.01 } );
        requests.add( new Object[] { "polygons/polygons_offset", "tif", 0.01 } );
        requests.add( new Object[] { "polygons/polygons_typepredicatetest", "tif", 0.01 } );
        requests.add( new Object[] { "resolution/contours_parameter_dpi", "png", 0.01 } );
        requests.add( new Object[] { "resolution/contours_parameter_format_options", "png", 0.01 } );
        requests.add( new Object[] { "resolution/contours_parameter_map_resolution", "png", 0.01 } );
        requests.add( new Object[] { "resolution/contours_parameter_pixelsize", "png", 0.01 } );
        requests.add( new Object[] { "resolution/contours_parameter_res", "png", 0.01 } );
        requests.add( new Object[] { "resolution/contours_parameter_x-dpi", "png", 0.01 } );
        requests.add( new Object[] { "resolution/contours_vector_dpi_96", "png", 0.01 } );
        requests.add( new Object[] { "resolution/contours_vector_dpi_100_empty", "png", 0.01 } );
        requests.add( new Object[] { "resolution/contours_vector_dpi_192", "png", 0.01 } );
        requests.add( new Object[] { "resolution/contours_vector_dpi_default_empty", "png", 0.01 } );
        requests.add( new Object[] { "resolution/satellite_provo_dpi_96", "png", 0.01 } );
        requests.add( new Object[] { "resolution/satellite_provo_dpi_100_empty", "png", 0.01 } );
        requests.add( new Object[] { "resolution/satellite_provo_dpi_192", "png", 0.01 } );
        requests.add( new Object[] { "resolution/satellite_provo_dpi_default_empty", "png", 0.01 } );
        return requests;
    }

    @Test
    public void testSimilarity()
                    throws IOException {
        String base = createRequest();
        BufferedImage actual = retrieve( IMAGE, base );
        double sim = determineSimilarity( actual, expected );
        if ( Math.abs( 1.0 - sim ) > tolerance ) {
            String tmpdir = System.getProperty( "java.io.tmpdir" );
            System.out.println( "Trying to store expected and actual response for " + resourceName + " in " + tmpdir
                                + ": expected_" + ++numFailed + "." + format + "/actual_" + numFailed + "." + format );
            try {
                ImageIO.write( expected, format,
                               new FileOutputStream( tmpdir + "/expected_" + numFailed + "." + format ) );
                ImageIO.write( actual, format, new FileOutputStream( tmpdir + "/actual_" + numFailed + "." + format ) );

                System.out.println(
                                "Result returned for " + resourceName + " (base64 -di encoded.dat > failed-test.zip)" );
                System.out.println( IntegrationTestUtils.toBase64Zip( parseAsBytes( actual ),
                                                                      resourceName + "." + format ) );
            } catch ( Throwable t ) {
            }
        }
        assertEquals( "Images are not similar enough for " + resourceName + ". Request: " + request, 1.0, sim, tolerance );
    }

    private String createRequest() {
        StringBuffer sb = new StringBuffer();
        sb.append( "http://localhost:" );
        sb.append( System.getProperty( "portnumber", "8080" ) );
        sb.append( "/" );
        sb.append( System.getProperty( "deegree-wms-similarity-webapp", "deegree-wms-similarity-tests" ) );
        sb.append( "/services" );
        if ( !request.startsWith( "?" ) )
            sb.append( "?" );
        sb.append( request );
        return sb.toString();
    }

    private byte[] parseAsBytes( RenderedImage actual )
                    throws IOException {
        ByteArrayOutputStream bosActual = new ByteArrayOutputStream();
        ImageIO.write( actual, format, bosActual );
        bosActual.flush();
        bosActual.close();
        return bosActual.toByteArray();
    }
}
