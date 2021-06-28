/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -
 and
 - grit graphische Informationstechnik Beratungsgesellschaft mbH -

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

 grit graphische Informationstechnik Beratungsgesellschaft mbH
 Landwehrstr. 143, 59368 Werne
 Germany
 http://www.grit.de/

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
package org.deegree.rendering.r2d;

import static java.util.Arrays.asList;
import static org.deegree.commons.utils.io.Utils.determineSimilarity;

import java.awt.image.RenderedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.commons.io.IOUtils;
import org.deegree.commons.utils.test.IntegrationTestUtils;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.persistence.CRSManager;
import org.deegree.geometry.GeometryFactory;
import org.deegree.geometry.primitive.Curve;
import org.deegree.geometry.primitive.Point;
import org.deegree.geometry.primitive.Polygon;
import org.deegree.geometry.standard.points.PointsList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractSimilarityTest {

    private static final Logger LOG = LoggerFactory.getLogger( AbstractSimilarityTest.class );

    private static final ICRS mapcs = CRSManager.getCRSRef( "CRS:1" );

    private static final GeometryFactory fac = new GeometryFactory();

    boolean isImageSimilar( RenderedImage expected, RenderedImage actual, double maximumDifference, String name )
                            throws Exception {
        byte[] expectedData;
        byte[] actualData;

        ByteArrayOutputStream bos;
        bos = new ByteArrayOutputStream();
        ImageIO.write( expected, "tif", bos );
        bos.flush();
        bos.close();
        expectedData = bos.toByteArray();

        bos = new ByteArrayOutputStream();
        ImageIO.write( actual, "tif", bos );
        bos.flush();
        bos.close();
        actualData = bos.toByteArray();

        double sim;
        sim = determineSimilarity( new ByteArrayInputStream( expectedData ), new ByteArrayInputStream( actualData ) );

        if ( Math.abs( 1.0 - sim ) > maximumDifference ) {
            LOG.error( "Images for test '{}' are not similar enough ({}>{})", name, sim, maximumDifference );

            try {
                LOG.error( "Trying to store rendering_{}_expected/rendering_{}_actual in java.io.tmpdir", name, name );
                IOUtils.write( expectedData, new FileOutputStream( System.getProperty( "java.io.tmpdir" )
                                                                   + "/rendering_" + name + "_expected.png" ) );
                IOUtils.write( actualData, new FileOutputStream( System.getProperty( "java.io.tmpdir" ) + "/rendering_"
                                                                 + name + "_actual.png" ) );

                System.out.println( "Result returned for " + name + " (base64 -di encoded.dat > failed-test.zip)" );
                System.out.println( IntegrationTestUtils.toBase64Zip( actualData, name + ".png" ) );
            } catch ( Throwable t ) {
            }

            return false;
        } else {
            LOG.info( "Similarity test '{}' ok ({})", name, 1.0 - sim );
            return true;
        }
    }

    /**
     * @param max
     * @param offx
     * @param offy
     * @return a curve similar to the points of #randomQuad (but without the last)
     */
    static Curve testCurve( double offx, double offy, double sizex, double sizey ) {
        Point[] ps = { fac.createPoint( null, offx + 0, offy, mapcs ),
                       fac.createPoint( null, offx + sizex * 0.2d, offy + sizey, mapcs ),
                       fac.createPoint( null, offx + sizex * 0.8d, offy + sizey, mapcs ),
                       fac.createPoint( null, offx + sizex, offy, mapcs ) };
        return fac.createLineString( null, mapcs, new PointsList( asList( ps ) ) );
    }
    
    /**
     * @param max
     * @param offx
     * @param offy
     * @return a curve similar to the points of #randomQuad (but without the last)
     */
    static Polygon testPolygon( double offx, double offy, double sizex, double sizey ) {
        Point[] ps = { fac.createPoint( null, offx + 0, offy, mapcs ),
                       fac.createPoint( null, offx + sizex * 0.2d, offy + sizey, mapcs ),
                       fac.createPoint( null, offx + sizex * 0.8d, offy + sizey, mapcs ),
                       fac.createPoint( null, offx + sizex, offy, mapcs ),
                       fac.createPoint( null, offx + 0, offy, mapcs )};
        return fac.createPolygon( null, mapcs, fac.createLinearRing( null, mapcs, new PointsList( asList( ps ) ) ),
                                  null );
    }

    void writeTestImageTemp( RenderedImage img, List<String> expectedTexts, long ms )
                            throws IOException {
        File tmp = File.createTempFile( "rendering_", ".png" );
        ImageIO.write( img, "png", tmp );
    }
}
