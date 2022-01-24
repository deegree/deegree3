//$HeadURL$
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

package org.deegree.rendering.r2d;

import static java.awt.Color.black;
import static java.awt.Color.green;
import static java.awt.Color.red;
import static java.awt.Color.white;
import static java.awt.image.BufferedImage.TYPE_INT_ARGB;
import static java.lang.System.currentTimeMillis;
import static javax.imageio.ImageIO.read;
import static org.deegree.geometry.utils.GeometryUtils.move;
import static org.deegree.style.styling.components.Font.Style.ITALIC;
import static org.deegree.style.styling.components.Font.Style.NORMAL;
import static org.deegree.style.styling.components.Font.Style.OBLIQUE;
import static org.deegree.style.styling.components.Stroke.LineCap.BUTT;
import static org.deegree.style.styling.components.Stroke.LineCap.ROUND;
import static org.deegree.style.styling.components.Stroke.LineCap.SQUARE;
import static org.deegree.style.styling.components.Stroke.LineJoin.BEVEL;
import static org.deegree.style.styling.components.Stroke.LineJoin.MITRE;
import static org.slf4j.LoggerFactory.getLogger;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.imageio.ImageIO;

import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.persistence.CRSManager;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.GeometryFactory;
import org.deegree.geometry.points.Points;
import org.deegree.geometry.primitive.Curve;
import org.deegree.geometry.primitive.LineString;
import org.deegree.geometry.primitive.Point;
import org.deegree.geometry.primitive.Surface;
import org.deegree.geometry.standard.points.PointsArray;
import org.deegree.style.styling.LineStyling;
import org.deegree.style.styling.PointStyling;
import org.deegree.style.styling.PolygonStyling;
import org.deegree.style.styling.TextStyling;
import org.deegree.style.styling.components.Fill;
import org.deegree.style.styling.components.Graphic;
import org.deegree.style.styling.components.Halo;
import org.deegree.style.styling.components.LinePlacement;
import org.deegree.style.styling.components.Mark;
import org.deegree.style.styling.components.Mark.SimpleMark;
import org.deegree.style.styling.components.Stroke;
import org.deegree.style.styling.components.Stroke.LineJoin;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;

/**
 * <code>Java2DRenderingTest</code>
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class Java2DRendererTest extends AbstractSimilarityTest {

    private static final Logger LOG = getLogger( Java2DRendererTest.class );

    private static File textFile, perfFile;

    private static BufferedImage fill;

    private static final ICRS mapcs = CRSManager.getCRSRef( "CRS:1" );

    static {
        try {
            fill = read( Java2DRendererTest.class.getResource( "logo-deegree.png" ) );
        } catch ( MalformedURLException e ) {
            LOG.error( "Unknown error", e );
        } catch ( IOException e ) {
            LOG.error( "Unknown error", e );
        }
    }

    private void validateImage( RenderedImage img, double time, String testName )
                            throws Exception {
        LOG.debug( "Test {} ran in {} ms", testName, time );
        RenderedImage expected = ImageIO.read( this.getClass().getResource( "./renderertest/" + testName + ".png" ) );
        Assert.assertTrue( "Image for " + testName + "are not similar enough",
                           isImageSimilar( expected, img, 0.01, testName ) );
    }

    /**
     * @throws Exception
     */
    @Test
    public void testPointStyling()
                            throws Exception {
        BufferedImage img = new BufferedImage( 1000, 1000, TYPE_INT_ARGB );
        long time = currentTimeMillis();
        Graphics2D g = img.createGraphics();
        GeometryFactory geomFac = new GeometryFactory();
        Java2DRenderer r = new Java2DRenderer( g, img.getWidth(), img.getHeight(),
                                               geomFac.createEnvelope( new double[] { 0, 0 },
                                                                       new double[] { 5000d, 5000d }, mapcs ) );

        PointStyling style = new PointStyling();
        r.render( style, geomFac.createPoint( null, new double[] { 100, 4900 }, mapcs ) );
        style.graphic.mark.wellKnown = Mark.SimpleMark.SQUARE;
        r.render( style, geomFac.createPoint( null, new double[] { 200, 4900 }, mapcs ) );
        style.graphic.mark.wellKnown = Mark.SimpleMark.CIRCLE;
        r.render( style, geomFac.createPoint( null, new double[] { 300, 4900 }, mapcs ) );
        style.graphic.mark.wellKnown = Mark.SimpleMark.TRIANGLE;
        r.render( style, geomFac.createPoint( null, new double[] { 400, 4900 }, mapcs ) );
        style.graphic.mark.wellKnown = Mark.SimpleMark.STAR;
        r.render( style, geomFac.createPoint( null, new double[] { 500, 4900 }, mapcs ) );
        style.graphic.mark.wellKnown = Mark.SimpleMark.CROSS;
        r.render( style, geomFac.createPoint( null, new double[] { 600, 4900 }, mapcs ) );
        style.graphic.mark.wellKnown = Mark.SimpleMark.X;
        r.render( style, geomFac.createPoint( null, new double[] { 700, 4900 }, mapcs ) );

        style = new PointStyling();
        style.graphic.size = 32;
        style.graphic.mark.fill.color = red;
        style.graphic.mark.stroke.color = green;
        r.render( style, geomFac.createPoint( null, new double[] { 500, 4500 }, mapcs ) );
        style.graphic.mark.wellKnown = Mark.SimpleMark.SQUARE;
        r.render( style, geomFac.createPoint( null, new double[] { 1000, 4500 }, mapcs ) );
        style.graphic.mark.wellKnown = Mark.SimpleMark.CIRCLE;
        r.render( style, geomFac.createPoint( null, new double[] { 1500, 4500 }, mapcs ) );
        style.graphic.mark.wellKnown = Mark.SimpleMark.TRIANGLE;
        r.render( style, geomFac.createPoint( null, new double[] { 2000, 4500 }, mapcs ) );
        style.graphic.mark.wellKnown = Mark.SimpleMark.STAR;
        r.render( style, geomFac.createPoint( null, new double[] { 2500, 4500 }, mapcs ) );
        style.graphic.mark.wellKnown = Mark.SimpleMark.CROSS;
        r.render( style, geomFac.createPoint( null, new double[] { 3000, 4500 }, mapcs ) );
        style.graphic.mark.wellKnown = Mark.SimpleMark.X;
        r.render( style, geomFac.createPoint( null, new double[] { 3500, 4500 }, mapcs ) );

        style = new PointStyling();
        style.graphic.size = 32;
        r.render( style, geomFac.createPoint( null, new double[] { 500, 4000 }, mapcs ) );
        style.graphic.size = 16;
        r.render( style, geomFac.createPoint( null, new double[] { 500, 4000 }, mapcs ) );

        style = new PointStyling();
        style.graphic.size = 32;
        r.render( style, geomFac.createPoint( null, new double[] { 1000, 4000 }, mapcs ) );
        style.graphic.size = 16;
        style.graphic.anchorPointX = 0;
        style.graphic.anchorPointY = 0;
        r.render( style, geomFac.createPoint( null, new double[] { 1000, 4000 }, mapcs ) );

        style = new PointStyling();
        style.graphic.size = 32;
        r.render( style, geomFac.createPoint( null, new double[] { 1500, 4000 }, mapcs ) );
        style.graphic.size = 16;
        style.graphic.anchorPointX = 1;
        style.graphic.anchorPointY = 1;
        r.render( style, geomFac.createPoint( null, new double[] { 1500, 4000 }, mapcs ) );

        style = new PointStyling();
        style.graphic.size = 32;
        r.render( style, geomFac.createPoint( null, new double[] { 2000, 4000 }, mapcs ) );
        style.graphic.size = 16;
        style.graphic.anchorPointX = 0;
        style.graphic.anchorPointY = 0;
        style.graphic.displacementX = -16;
        style.graphic.displacementY = -16;
        r.render( style, geomFac.createPoint( null, new double[] { 2000, 4000 }, mapcs ) );

        style = new PointStyling();
        style.graphic.size = 32;
        r.render( style, geomFac.createPoint( null, new double[] { 2500, 4000 }, mapcs ) );
        style.graphic.size = 16;
        style.graphic.anchorPointX = 1;
        style.graphic.anchorPointY = 1;
        style.graphic.displacementX = 16;
        style.graphic.displacementY = 16;
        r.render( style, geomFac.createPoint( null, new double[] { 2500, 4000 }, mapcs ) );

        style = new PointStyling();
        style.graphic.size = 32;
        r.render( style, geomFac.createPoint( null, new double[] { 3000, 4000 }, mapcs ) );
        style.graphic.size = 16;
        style.graphic.rotation = 45;
        r.render( style, geomFac.createPoint( null, new double[] { 3000, 4000 }, mapcs ) );

        style = new PointStyling();
        style.graphic.size = 32;
        r.render( style, geomFac.createPoint( null, new double[] { 3500, 4000 }, mapcs ) );
        style.graphic.size = 16;
        style.graphic.rotation = 45;
        style.graphic.displacementX = 16;
        style.graphic.displacementY = 16;
        r.render( style, geomFac.createPoint( null, new double[] { 3500, 4000 }, mapcs ) );

        // TODO: fix required!
        style = new PointStyling();
        style.graphic.size = 32;
        r.render( style, geomFac.createPoint( null, new double[] { 4000, 4000 }, mapcs ) );
        style.graphic.size = 16;
        style.graphic.anchorPointX = 0.5;
        style.graphic.anchorPointY = 0;
        style.graphic.displacementX = 16;
        style.graphic.displacementY = 16;
        r.render( style, geomFac.createPoint( null, new double[] { 4000, 4000 }, mapcs ) );
        style.graphic.size = 16;
        style.graphic.anchorPointX = 0.5;
        style.graphic.anchorPointY = 0;
        style.graphic.rotation = 45;
        style.graphic.displacementX = 16;
        style.graphic.displacementY = 16;
        r.render( style, geomFac.createPoint( null, new double[] { 4000, 4000 }, mapcs ) );

        g.dispose();
        long time2 = currentTimeMillis();
        List<String> texts = new LinkedList<String>();
        texts.add( "first line: all the marks in the first line (size 6, gray fill, black line)" );
        texts.add( "second line: all the marks in the second line (size 32, red fill, green line)" );
        texts.add( "third line: 32 pixel default square, inside centered a 16 pixel default square" );
        texts.add( "third line: 32 pixel default square, inside a 16 pixel default square (lower right corner)" );
        texts.add( "third line: 32 pixel default square, inside a 16 pixel default square (upper left corner), two times" );
        texts.add( "third line: 32 pixel default square, inside a 16 pixel default square (lower right corner) again" );
        texts.add( "third line: 32 pixel default square, inside a 16 pixel rotated square" );
        texts.add( "third line: 32 pixel default square, outside a 16 pixel rotated square (lower right corner)" );
        texts.add( "third line: 32 pixel default square, outside a 16 pixel square (upper center at lower right corner of the large square) and a 16 pixel rotated square (lower right corner)" );
        validateImage( img, time2 - time, "pointstyling" );
    }

    /**
     * @throws Exception
     */
    @Test
    public void testLineStyling()
                            throws Exception {
        BufferedImage img = new BufferedImage( 1000, 1000, TYPE_INT_ARGB );
        long time = currentTimeMillis();
        Graphics2D g = img.createGraphics();
        GeometryFactory geomFac = new GeometryFactory();
        Java2DRenderer r = new Java2DRenderer( g, img.getWidth(), img.getHeight(),
                                               geomFac.createEnvelope( new double[] { 0, 0 },
                                                                       new double[] { 5000d, 5000d }, mapcs ) );

        List<Curve> curves = new LinkedList<Curve>();
        for ( int i = 0; i < 10; ++i ) {
            //curves.add( randomCurve( 500, i * 500, 0 ) );
            curves.add( testCurve( 100 + i * 480, 100, 300, 250 ) );
        }

        LineStyling styling = new LineStyling();
        for ( int y = 0; y < 10; ++y ) {
            switch ( y ) {
            case 0:
                break;
            case 1:
                styling.stroke.linecap = BUTT;
                break;
            case 2:
                styling.stroke.linecap = ROUND;
                break;
            case 3:
                styling.stroke.linecap = SQUARE;
                break;
            case 4:
                styling.stroke.linecap = BUTT;
                styling.stroke.linejoin = BEVEL;
                break;
            case 5:
                styling.stroke.linejoin = MITRE;
                break;
            case 6:
                styling.stroke.linejoin = LineJoin.ROUND;
                break;
            case 7:
                styling.stroke.dasharray = new double[] { 15, 15, 17, 5 };
                break;
            case 8:
                styling.stroke.linecap = SQUARE;
                break;
            case 9:
                styling.stroke.linecap = ROUND;
                break;
            }
            Iterator<Curve> iterator = curves.iterator();
            for ( int x = 0; x < 10; ++x ) {
                styling.stroke.width = x;
                Curve curve = (Curve) move( iterator.next(), 0, ( 9 - y ) * 500 );
                r.render( styling, curve );
            }
        }

        g.dispose();
        long time2 = currentTimeMillis();
        List<String> texts = new LinkedList<String>();
        texts.add( "line 1: default style lines with line width 0, 1, ..., 9, ending square" );
        texts.add( "line 2: default style lines with line width 0, 1, ..., 9, ending square" );
        texts.add( "line 3: default style lines with line width 0, 1, ..., 9, ending butt" );
        texts.add( "line 4: default style lines with line width 0, 1, ..., 9, ending round" );
        texts.add( "line 5: default style lines with line width 0, 1, ..., 9, join bevel" );
        texts.add( "line 6: default style lines with line width 0, 1, ..., 9, join mitre" );
        texts.add( "line 7: default style lines with line width 0, 1, ..., 9, join round" );
        texts.add( "line 8: default style lines with line width 0, 1, ..., 9, dashed with pattern 15, 15, 17, 5" );
        texts.add( "line 9: default style lines with line width 0, 1, ..., 9, dashed with pattern 15, 15, 17, 5, ending square" );
        texts.add( "line 10: default style lines with line width 0, 1, ..., 9, dashed with pattern 15, 15, 17, 5, ending round" );
        validateImage( img, time2 - time, "linestyling" );
    }

    /**
     * @throws Exception
     */
    @Test
    public void testLineStyling2()
                            throws Exception {
        BufferedImage img = new BufferedImage( 1000, 1000, TYPE_INT_ARGB );
        long time = currentTimeMillis();
        Graphics2D g = img.createGraphics();
        GeometryFactory geomFac = new GeometryFactory();
        Java2DRenderer r = new Java2DRenderer( g, img.getWidth(), img.getHeight(),
                                               geomFac.createEnvelope( new double[] { 0, 0 },
                                                                       new double[] { 5000d, 5000d }, mapcs ) );
        List<Curve> curves = new LinkedList<Curve>();
        for ( int i = 0; i < 10; ++i ) {
            //curves.add( randomCurve( 500, i * 500, 0 ) );
            curves.add( testCurve( 100 + i * 480, 100, 300, 250 ) );
        }

        LineStyling styling = new LineStyling();
        for ( int y = 0; y < 10; ++y ) {
            switch ( y ) {
            case 0:
                styling.stroke.linecap = BUTT;
                styling.stroke.fill = new Graphic();
                styling.stroke.fill.image = fill;
                break;
            case 1:
                styling.stroke.dasharray = new double[] { 15, 15, 17, 5 };
                break;
            case 2:
                styling.stroke.fill = null;
                break;
            case 3:
                styling.stroke.dashoffset = 10;
                break;
            case 4:
                styling.stroke.dasharray = null;
                break;
            case 7:
                break;
            case 8:
                styling.stroke.linecap = SQUARE;
                break;
            case 9:
                styling.stroke.linecap = ROUND;
                break;
            }
            Iterator<Curve> iterator = curves.iterator();
            for ( int x = 0; x < 10; ++x ) {
                styling.stroke.width = x;
                Curve curve = (Curve) move( iterator.next(), 0, ( 9 - y ) * 500 );
                switch ( y ) {
                case 4:
                    styling.perpendicularOffset = 0;
                    styling.stroke.color = black;
                    r.render( styling, curve );
                    styling.stroke.color = red;
                    styling.perpendicularOffset = 1;
                    break;
                case 5:
                    styling.perpendicularOffset = 0;
                    styling.stroke.color = black;
                    r.render( styling, curve );
                    styling.stroke.color = red;
                    styling.perpendicularOffset = 5;
                    break;
                case 6:
                    styling.perpendicularOffset = 0;
                    styling.stroke.color = black;
                    r.render( styling, curve );
                    styling.stroke.color = red;
                    styling.perpendicularOffset = -5;
                    break;
                }
                r.render( styling, curve );
            }
        }

        g.dispose();
        long time2 = currentTimeMillis();
        List<String> texts = new LinkedList<String>();
        texts.add( "line 1: graphic filled lines with line width 0, 1, ..., 9, ending butt" );
        texts.add( "line 2: graphic filled lines with line width 0, 1, ..., 9, dashed with pattern 15, 15, 17, 5, ending butt" );
        texts.add( "line 3: default style lines with line width 0, 1, ..., 9, dashed with pattern 15, 15, 17, 5, ending butt" );
        texts.add( "line 4: same as previous, but with offset = 10" );
        texts.add( "line 5: perpendicular offset of 1" );
        validateImage( img, time2 - time, "linestyling2" );
    }

    /**
     * @throws Exception
     */
    @Test
    public void testPolygonStyling()
                            throws Exception {
        BufferedImage img = new BufferedImage( 1000, 1000, TYPE_INT_ARGB );

        long time = currentTimeMillis();
        Graphics2D g = img.createGraphics();
        GeometryFactory geomFac = new GeometryFactory();
        Java2DRenderer r = new Java2DRenderer( g, img.getWidth(), img.getHeight(),
                                               geomFac.createEnvelope( new double[] { 0, 0 },
                                                                       new double[] { 5000d, 5000d }, mapcs ) );

        List<Surface> polygons = new LinkedList<Surface>();
        for ( int i = 0; i < 10; ++i ) {
            //polygons.add( randomQuad( 500, i * 500, 0 ) );
            polygons.add( testPolygon( 100 + i * 480, 100, 300, 250 ) );
        }

        PolygonStyling styling = new PolygonStyling();
        styling.fill = new Fill();
        styling.stroke = new Stroke();
        styling.stroke.color = black;
        for ( int y = 0; y < 10; ++y ) {
            if ( y == 0 ) {
                styling.fill.graphic = new Graphic();
                styling.fill.graphic.image = fill;
            } else {
                styling.fill.graphic = null;
            }
            styling.fill.color = new Color( 25 * y, 0, 0 );
            Iterator<Surface> surface = polygons.iterator();
            for ( int x = 0; x < 10; ++x ) {
                styling.stroke.width = x;
                Surface polygon = (Surface) move( surface.next(), 0, ( 9 - y ) * 500 );
                r.render( styling, polygon );
            }
        }

        g.dispose();
        long time2 = currentTimeMillis();
        List<String> texts = new LinkedList<String>();
        texts.add( "first line: polygon style with black lines with line width 0, 1, ..., 9 and external image fill" );
        texts.add( "other lines: polygon style with black lines with line width 0, 1, ..., 9 and increasingly red fill" );
        validateImage( img, time2 - time, "polygonstyling" );
    }

    /**
     * @throws Exception
     */
    @Test
    public void testTextStyling()
                            throws Exception {
        BufferedImage img = new BufferedImage( 1000, 1000, TYPE_INT_ARGB );

        long time = currentTimeMillis();
        Graphics2D g = img.createGraphics();
        GeometryFactory geomFac = new GeometryFactory();
        Java2DRenderer r2d = new Java2DRenderer( g, img.getWidth(), img.getHeight(),
                                                 geomFac.createEnvelope( new double[] { 0, 0 },
                                                                         new double[] { 5000d, 5000d }, mapcs ) );
        Java2DTextRenderer r = new Java2DTextRenderer( r2d );

        LinkedList<Point> points = new LinkedList<Point>();
        for ( int i = 0; i < 50; ++i ) {
            points.add( geomFac.createPoint( null, new double[] { 3500 - ( i + 1 ) * 100 , 4500 - ( i + 1 ) * 250 }, mapcs ) );
        }

        String text = "AbC \u00c4\u00fc\u00d6";
        TextStyling styling = new TextStyling();
        r.render( styling, text, points.poll() );
        styling.fill = new Fill();
        styling.fill.graphic = new Graphic();
        styling.fill.graphic.image = fill;
        r.render( styling, text, points.poll() );
        styling.halo = new Halo();
        styling.halo.radius = 3;
        r.render( styling, text, points.poll() );
        styling.font.fontSize = 18;
        styling.halo.fill = styling.fill;
        styling.fill = new Fill();
        styling.fill.color = black;
        r.render( styling, text, points.poll() );
        styling.halo.fill.graphic = null;
        styling.halo.fill.color = black;
        styling.halo.radius = 3;
        styling.fill.color = white;
        r.render( styling, text, points.poll() );
        styling.fill.graphic = new Graphic();
        styling.fill.graphic.image = fill;
        r.render( styling, text, points.poll() );
        styling.font.fontStyle = NORMAL;
        r.render( styling, text, points.poll() );
        styling.font.fontStyle = OBLIQUE;
        r.render( styling, text, points.poll() );
        styling.font.fontStyle = ITALIC;
        r.render( styling, text, points.poll() );
        styling.font.bold = true;
        r.render( styling, text, points.poll() );
        styling.font.fontFamily.add( "Times New Roman" );
        r.render( styling, text, points.poll() );
        r.render( styling, text, points.peek() );
        styling.rotation = 180;
        r.render( styling, text, points.poll() );
        styling.rotation = 90;
        r.render( styling, text, points.poll() );
        styling.rotation = 0;
        r.render( styling, text, points.peek() );
        styling.displacementX = -10;
        styling.displacementY = -10;
        r.render( styling, text, points.poll() );
        styling.displacementX = 0;
        styling.displacementY = 0;
        styling.anchorPointX = 0;
        r.render( styling, text, points.peek() );
        styling.anchorPointX = 1;
        r.render( styling, text, points.poll() );
        r.render( styling, text, points.poll() );

        g.dispose();
        long time2 = currentTimeMillis();
        List<String> texts = new LinkedList<String>();
        texts.add( "standard dialog font, size 10, black" );
        texts.add( "same, filled with 'image'" );
        texts.add( "same, with 3 pixel while halo" );
        texts.add( "same, size 18, color black, halo filled with 'image'" );
        texts.add( "same, black halo, white color" );
        texts.add( "same, filled with 'image'" );
        texts.add( "same" );
        texts.add( "same, italic font, warning message that 'oblique' is not supported" );
        texts.add( "same, italic font" );
        texts.add( "same, bold font" );
        texts.add( "same, Times New Roman font" );
        texts.add( "same, once normal, once rotated by 180°, should be on top on each other" );
        texts.add( "same, rotated by 90° clockwise" );
        texts.add( "same, no rotation, one normal, one displaced by 10 pixels to left and top" );
        texts.add( "same, one ending in the middle of the screen, one starting there" );
        texts.add( "same, only one ending in the middle of the screen" );
        validateImage( img, time2 - time, "textstyling" );
    }

    /**
     * @throws Exception
     */
    @Test
    @Ignore
    public void testTextStyling2()
                            throws Exception {
        BufferedImage img = new BufferedImage( 1000, 1000, TYPE_INT_ARGB );

        long time = currentTimeMillis();
        Graphics2D g = img.createGraphics();
        GeometryFactory geomFac = new GeometryFactory();
        Java2DRenderer r2d = new Java2DRenderer( g, img.getWidth(), img.getHeight(),
                                                 geomFac.createEnvelope( new double[] { 0, 0 },
                                                                         new double[] { 5000d, 5000d }, mapcs ) );
        Java2DTextRenderer r = new Java2DTextRenderer( r2d );

        LinkedList<Curve> curves = new LinkedList<Curve>();
        for ( int y = 0; y < 4; ++y ) {
            for ( int i = 0; i < 6; ++i ) {
                //curves.add( randomCurve( 700, i * 800, 4100 - 800 * y ) );
                curves.add( testCurve( 200+ i * 800, 4100 - 1200 * y, 300, 600 ) );
            }
        }

        LineStyling lineStyle = new LineStyling();
        lineStyle.stroke.width = 20;
        lineStyle.stroke.linecap = BUTT;
        String text = "5th Street";
        TextStyling styling = new TextStyling();
        styling.linePlacement = new LinePlacement();
        styling.font.fontSize = 25;
        styling.font.fontFamily.add( "Courier" );
        r2d.render( lineStyle, curves.peek() );
        r.render( styling, text, curves.poll() );
        styling.linePlacement.repeat = true;
        r2d.render( lineStyle, curves.peek() );
        r.render( styling, text, curves.poll() );
        r2d.render( lineStyle, curves.peek() );
        r.render( styling, text, curves.poll() );
        r2d.render( lineStyle, curves.peek() );
        r.render( styling, text, curves.poll() );
        r2d.render( lineStyle, curves.peek() );
        r.render( styling, text, curves.poll() );
        r2d.render( lineStyle, curves.peek() );
        r.render( styling, text, curves.poll() );
        styling.linePlacement.perpendicularOffset = 17.5;
        r2d.render( lineStyle, curves.peek() );
        r.render( styling, text, curves.poll() );
        r2d.render( lineStyle, curves.peek() );
        r.render( styling, text, curves.poll() );
        r2d.render( lineStyle, curves.peek() );
        r.render( styling, text, curves.poll() );
        r2d.render( lineStyle, curves.peek() );
        r.render( styling, text, curves.poll() );
        r2d.render( lineStyle, curves.peek() );
        r.render( styling, text, curves.poll() );
        r2d.render( lineStyle, curves.peek() );
        r.render( styling, text, curves.poll() );
        styling.linePlacement.perpendicularOffset = 0;
        styling.linePlacement.initialGap = 20;
        r2d.render( lineStyle, curves.peek() );
        r.render( styling, text, curves.poll() );
        r2d.render( lineStyle, curves.peek() );
        r.render( styling, text, curves.poll() );
        r2d.render( lineStyle, curves.peek() );
        r.render( styling, text, curves.poll() );
        r2d.render( lineStyle, curves.peek() );
        r.render( styling, text, curves.poll() );
        r2d.render( lineStyle, curves.peek() );
        r.render( styling, text, curves.poll() );
        r2d.render( lineStyle, curves.peek() );
        r.render( styling, text, curves.poll() );
        styling.linePlacement.gap = 10;
        styling.font.fontSize = 12;
        r2d.render( lineStyle, curves.peek() );
        r.render( styling, text, curves.poll() );
        r2d.render( lineStyle, curves.peek() );
        r.render( styling, text, curves.poll() );
        r2d.render( lineStyle, curves.peek() );
        r.render( styling, text, curves.poll() );
        r2d.render( lineStyle, curves.peek() );
        r.render( styling, text, curves.poll() );
        r2d.render( lineStyle, curves.peek() );
        r.render( styling, text, curves.poll() );
        r2d.render( lineStyle, curves.peek() );
        r.render( styling, text, curves.poll() );

        g.dispose();
        long time2 = currentTimeMillis();
        List<String> texts = new LinkedList<String>();
        texts.add( "Note: this text may not produce good tests every time (it depends on the length of the random line strings)." );
        texts.add( "Run the test again to see if the mentioned features cannot be found." );
        texts.add( "There is also a word wise text rendering and a character wise rendering." );
        texts.add( "The word wise rendering is only applied if possible, ie, if the words fit properly on the lines." );
        texts.add( "first line: renders gray lines width 20, inside a text with size 15." );
        texts.add( "Text should start immediately inside the line. First geometry should include" );
        texts.add( "the text once, the others repeated." );
        texts.add( "second line: renders the same text directly above the gray line (perpendicular offset)" );
        texts.add( "third line: renders with initial gap of 20" );
        texts.add( "fourth line: renders with initial gap of 20 and gap of 10 (with text size 12)" );
        validateImage( img, time2 - time, "textstyling2" );
    }

    @Test(timeout = 2500)
    public void testPolygonStylingSmallClipping()
                            throws Exception {
        BufferedImage img = new BufferedImage( 100, 100, TYPE_INT_ARGB );
        long time = currentTimeMillis();
        Graphics2D g = img.createGraphics();
        GeometryFactory geomFac = new GeometryFactory();
        Java2DRenderer r = new Java2DRenderer( g, img.getWidth(), img.getHeight(),
                                               geomFac.createEnvelope( new double[] { 0, 0 }, new double[] { 50d, 50d },
                                                                       mapcs ) );
        Envelope envelope = new GeometryFactory().createEnvelope( 0, 0, 100000000, 100000000, null );

        PolygonStyling styling = new PolygonStyling();
        styling.stroke = new Stroke();
        styling.stroke.color = red;
        styling.stroke.width = 5;
        styling.stroke.dasharray = new double[] { 15, 15, 17, 5 };
        styling.fill = new Fill();
        styling.fill.color = white;

        r.render( styling, envelope );

        g.dispose();
        long time2 = currentTimeMillis();
        List<String> texts = new LinkedList<String>();
        texts.add( "line: default style line dashed with pattern 15, 15, 17, 5" );
        validateImage( img, time2 - time, "polygonstylingsmallclipping" );
    }

    @Test(timeout = 2500)
    public void testLineStylingSmallClipping()
                            throws Exception {
        BufferedImage img = new BufferedImage( 100, 100, TYPE_INT_ARGB );
        long time = currentTimeMillis();
        Graphics2D g = img.createGraphics();
        GeometryFactory geomFac = new GeometryFactory();
        Java2DRenderer r = new Java2DRenderer( g, img.getWidth(), img.getHeight(),
                                               geomFac.createEnvelope( new double[] { 0, 0 }, new double[] { 50d, 50d },
                                                                       mapcs ) );
        Point p1 = geomFac.createPoint( "testP1", 0, 0, null );
        Point p2 = geomFac.createPoint( "testP1", 10000000, 100000000, null );
        Points points = new PointsArray( p1, p2 );
        LineString lineString = geomFac.createLineString( "testLineString", null, points );

        LineStyling styling = new LineStyling();
        styling.stroke.color = red;
        styling.stroke.width = 5;
        styling.stroke.dasharray = new double[] { 15, 15, 17, 5 };

        r.render( styling, lineString );

        g.dispose();
        long time2 = currentTimeMillis();
        List<String> texts = new LinkedList<String>();
        texts.add( "line: default style line dashed with pattern 15, 15, 25, 5" );
        validateImage( img, time2 - time, "linestylingsmallclipping" );
    }

    @Test(timeout = 2500)
    public void testPolygonStylingWithCirclesSmallClipping()
                            throws Exception {
        BufferedImage img = new BufferedImage( 100, 100, TYPE_INT_ARGB );
        long time = currentTimeMillis();
        Graphics2D g = img.createGraphics();
        GeometryFactory geomFac = new GeometryFactory();
        Java2DRenderer r = new Java2DRenderer( g, img.getWidth(), img.getHeight(),
                                               geomFac.createEnvelope( new double[] { 0, 0 },
                                                                       new double[] { 600d, 600d }, mapcs ) );
        Envelope envelope = geomFac.createEnvelope( 10, 10, 100000000, 100000000, null );

        PolygonStyling styling = new PolygonStyling();
        styling.stroke = new Stroke();
        styling.stroke.strokeGap = 7;
        styling.stroke.width = 1;

        styling.stroke.stroke = new Graphic();
        styling.stroke.stroke.size = 5;
        styling.stroke.stroke.mark.fill.color = red;
        styling.stroke.stroke.mark.wellKnown = SimpleMark.CIRCLE;
        styling.stroke.stroke.mark.stroke.color = red;
        styling.stroke.stroke.mark.stroke.width = 0;

        styling.fill = new Fill();
        styling.fill.color = white;

        r.render( styling, envelope );

        g.dispose();
        long time2 = currentTimeMillis();
        List<String> texts = new LinkedList<String>();
        texts.add( "polygon: black line with circles stroke vertical on the left and horizontal on the bottom" );
        validateImage( img, time2 - time, "polygonstylingwithcircelssmallclipping" );
    }

    @Test(timeout = 2500)
    public void testPolygonStylingWithStrokeWithoutSize()
                            throws Exception {
        BufferedImage img = new BufferedImage( 100, 100, TYPE_INT_ARGB );
        long time = currentTimeMillis();
        Graphics2D g = img.createGraphics();
        GeometryFactory geomFac = new GeometryFactory();
        Java2DRenderer r = new Java2DRenderer( g, img.getWidth(), img.getHeight(),
                                               geomFac.createEnvelope( new double[] { 0, 0 },
                                                                       new double[] { 100d, 100d }, mapcs ) );
        Envelope envelope = geomFac.createEnvelope( 10, 10, 90, 90, null );

        PolygonStyling styling = new PolygonStyling();
        styling.stroke = new Stroke();
        styling.stroke.stroke = new Graphic();
        styling.fill = new Fill();
        styling.fill.color = white;

        r.render( styling, envelope );

        g.dispose();
        long time2 = currentTimeMillis();
        List<String> texts = new LinkedList<String>();
        texts.add( "polygon: default style line with circles stroke" );
        validateImage( img, time2 - time, "polygonstylingwithstrokewithoutsize" );
    }

    @Test
    public void testPolygonStylingPerpendicularOffset()
                            throws Exception {
        BufferedImage img = new BufferedImage( 100, 100, TYPE_INT_ARGB );
        long time = currentTimeMillis();
        Graphics2D g = img.createGraphics();
        GeometryFactory geomFac = new GeometryFactory();
        Java2DRenderer r = new Java2DRenderer( g, img.getWidth(), img.getHeight(),
                                               geomFac.createEnvelope( new double[] { 0, 0 },
                                                                       new double[] { 100d, 100d }, mapcs ) );
        PolygonStyling styling = new PolygonStyling();
        styling.stroke = new Stroke();
        styling.stroke.strokeGap = 7;
        styling.stroke.width = 1;

        styling.stroke.stroke = new Graphic();
        styling.stroke.stroke.size = 5;

        styling.stroke.stroke.mark.fill.color = red;
        styling.stroke.stroke.mark.wellKnown = SimpleMark.TRIANGLE;
        styling.stroke.stroke.mark.stroke.color = red;
        styling.stroke.stroke.mark.stroke.width = 0;

        styling.fill = new Fill();
        styling.fill.color = white;
        styling.perpendicularOffset = -4;
        Envelope envelope = geomFac.createEnvelope( 10, 10, 300, 300, null );
        r.render( styling, envelope );

        g.dispose();
        long time2 = currentTimeMillis();
        List<String> texts = new LinkedList<String>();
        texts.add( "polygon: white rectangle with red triangle stroke and perpendicular offest of -4. Expected: triangles points to the INSIDE of the geometry!" );
        validateImage( img, time2 - time, "polygonstylingperpendicularoffset" );
    }

    /**
     * Prevent reintroducing of a clipping error on extra large geometries
     * 
     * Prevent endless dash generation in rendering of strokes (or JVM Crashes in Tomcat) if one of the coordinates of a
     * geometry has an invalid value (which is out of bounds) and the clipper does not clip the geometry
     * 
     * A timeout is required to prevent a endless runtime in JUnit test runner
     * 
     * @author <a href="mailto:reichhelm@grit.de">Stephan Reichhelm</a>
     * @throws Exception
     */
    @Test(timeout = 30000)
    public void testClipperJvmCrash()
                            throws Exception {
        BufferedImage img = new BufferedImage( 100, 100, TYPE_INT_ARGB );
        Graphics2D g = img.createGraphics();
        GeometryFactory geomFac = new GeometryFactory();

        double cen0 = 642800d;
        double cen1 = 5600049000d;

        Java2DRenderer r = new Java2DRenderer( g, img.getWidth(), img.getHeight(),
                                               geomFac.createEnvelope( new double[] { 0, 0 }, new double[] { 50d, 50d },
                                                                       mapcs ) );
        Point p1 = geomFac.createPoint( "testP1", 0, 0, null );
        Point p2 = geomFac.createPoint( "testP1", cen0, cen1, null );
        Points points = new PointsArray( p1, p2 );

        LineString lineString = geomFac.createLineString( "testLineString", null, points );

        LineStyling styling = new LineStyling();
        styling.stroke.color = red;
        styling.stroke.width = 3;
        styling.stroke.dasharray = new double[] { 10.0d, 10.0d };

        r.render( styling, lineString );

        g.dispose();
    }
}
