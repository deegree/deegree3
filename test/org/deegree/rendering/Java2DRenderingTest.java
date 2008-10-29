//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------
 This file is part of deegree.
 Copyright (C) 2001-2008 by:
 Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 Lesser General Public License for more details.
 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 Contact:

 Andreas Poth
 lat/lon GmbH
 Aennchenstr. 19
 53177 Bonn
 Germany
 E-Mail: poth@lat-lon.de

 Prof. Dr. Klaus Greve
 Department of Geography
 University of Bonn
 Meckenheimer Allee 166
 53115 Bonn
 Germany
 E-Mail: greve@giub.uni-bonn.de
 ---------------------------------------------------------------------------*/

package org.deegree.rendering;

import static java.awt.Color.black;
import static java.awt.Color.green;
import static java.awt.Color.red;
import static java.awt.Color.white;
import static java.awt.image.BufferedImage.TYPE_INT_ARGB;
import static java.io.File.createTempFile;
import static java.lang.Math.PI;
import static java.lang.System.currentTimeMillis;
import static java.lang.System.getProperty;
import static javax.imageio.ImageIO.read;
import static javax.imageio.ImageIO.write;
import static org.deegree.commons.utils.GeometryUtils.move;
import static org.deegree.model.geometry.GeometryFactoryCreator.getInstance;
import static org.deegree.model.styling.components.Font.Style.ITALIC;
import static org.deegree.model.styling.components.Font.Style.NORMAL;
import static org.deegree.model.styling.components.Font.Style.OBLIQUE;
import static org.deegree.model.styling.components.Stroke.LineCap.BUTT;
import static org.deegree.model.styling.components.Stroke.LineCap.ROUND;
import static org.deegree.model.styling.components.Stroke.LineCap.SQUARE;
import static org.deegree.model.styling.components.Stroke.LineJoin.BEVEL;
import static org.deegree.model.styling.components.Stroke.LineJoin.MITRE;
import static org.deegree.rendering.GeometryGenerator.randomCurve;
import static org.deegree.rendering.GeometryGenerator.randomQuad;
import static org.slf4j.LoggerFactory.getLogger;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import junit.framework.TestCase;

import org.deegree.model.geometry.GeometryFactory;
import org.deegree.model.geometry.primitive.Curve;
import org.deegree.model.geometry.primitive.Point;
import org.deegree.model.geometry.primitive.Surface;
import org.deegree.model.styling.LineStyling;
import org.deegree.model.styling.PointStyling;
import org.deegree.model.styling.PolygonStyling;
import org.deegree.model.styling.TextStyling;
import org.deegree.model.styling.components.Fill;
import org.deegree.model.styling.components.Graphic;
import org.deegree.model.styling.components.Halo;
import org.deegree.model.styling.components.LinePlacement;
import org.deegree.model.styling.components.Mark;
import org.deegree.model.styling.components.Stroke;
import org.deegree.model.styling.components.Stroke.LineJoin;
import org.slf4j.Logger;

/**
 * <code>Java2DRenderingTest</code>
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class Java2DRenderingTest extends TestCase {

    private static final Logger LOG = getLogger( Java2DRenderingTest.class );

    private static File textFile, perfFile;

    // setting this to true will delete all rendering_* files in your temporary directory!
    private static final boolean INTERACTIVE_TESTS = true;

    private static BufferedImage fill;

    static {
        if ( INTERACTIVE_TESTS ) {
            String tmp = getProperty( "java.io.tmpdir" );
            textFile = new File( tmp, "rendering.txt" );
            textFile.delete();
            perfFile = new File( tmp, "performance.txt" );
            perfFile.delete();

            File[] fs = new File( tmp ).listFiles();
            if ( fs != null ) {
                for ( File f : fs ) {
                    if ( f.getName().startsWith( "rendering_" ) ) {
                        f.delete();
                    }
                }
            }
        }

        try {
            fill = read( Java2DRenderingTest.class.getResource( "logo-deegree.png" ) );
        } catch ( MalformedURLException e ) {
            LOG.error( "Unknown error", e );
        } catch ( IOException e ) {
            LOG.error( "Unknown error", e );
        }
    }

    private void writeTestImage( RenderedImage img, List<String> expectedTexts, long ms )
                            throws IOException {
        if ( INTERACTIVE_TESTS ) {
            File tmp = createTempFile( "rendering_", ".png" );
            PrintWriter out = new PrintWriter( new OutputStreamWriter( new FileOutputStream( textFile, true ), "UTF-8" ) );
            for ( String str : expectedTexts ) {
                out.println( tmp.getName() + ": " + str );
            }
            out.close();
            out = new PrintWriter( new OutputStreamWriter( new FileOutputStream( perfFile, true ), "UTF-8" ) );
            out.println( tmp.getName() + " was created in " + ms + "ms" );
            out.close();
            write( img, "png", tmp );
        }
    }

    /**
     * @throws Exception
     */
    public void testPointStyling()
                            throws Exception {
        BufferedImage img = new BufferedImage( 1000, 1000, TYPE_INT_ARGB );
        long time = currentTimeMillis();
        Graphics2D g = img.createGraphics();
        GeometryFactory geomFac = getInstance().getGeometryFactory();
        Java2DRenderer r = new Java2DRenderer( g, img.getWidth(), img.getHeight(),
                                               geomFac.createEnvelope( new double[] { 0, 0 }, new double[] { 5000d,
                                                                                                            5000d },
                                                                       null ) );

        PointStyling style = new PointStyling();
        r.render( style, geomFac.createPoint( null, new double[] { 100, 100 }, null ) );
        style.graphic.mark.wellKnown = Mark.SimpleMark.SQUARE;
        r.render( style, geomFac.createPoint( null, new double[] { 200, 100 }, null ) );
        style.graphic.mark.wellKnown = Mark.SimpleMark.CIRCLE;
        r.render( style, geomFac.createPoint( null, new double[] { 300, 100 }, null ) );
        style.graphic.mark.wellKnown = Mark.SimpleMark.TRIANGLE;
        r.render( style, geomFac.createPoint( null, new double[] { 400, 100 }, null ) );
        style.graphic.mark.wellKnown = Mark.SimpleMark.STAR;
        r.render( style, geomFac.createPoint( null, new double[] { 500, 100 }, null ) );
        style.graphic.mark.wellKnown = Mark.SimpleMark.CROSS;
        r.render( style, geomFac.createPoint( null, new double[] { 600, 100 }, null ) );
        style.graphic.mark.wellKnown = Mark.SimpleMark.X;
        r.render( style, geomFac.createPoint( null, new double[] { 700, 100 }, null ) );

        style = new PointStyling();
        style.graphic.size = 32;
        style.graphic.mark.fill.color = red;
        style.graphic.mark.stroke.color = green;
        r.render( style, geomFac.createPoint( null, new double[] { 500, 500 }, null ) );
        style.graphic.mark.wellKnown = Mark.SimpleMark.SQUARE;
        r.render( style, geomFac.createPoint( null, new double[] { 1000, 500 }, null ) );
        style.graphic.mark.wellKnown = Mark.SimpleMark.CIRCLE;
        r.render( style, geomFac.createPoint( null, new double[] { 1500, 500 }, null ) );
        style.graphic.mark.wellKnown = Mark.SimpleMark.TRIANGLE;
        r.render( style, geomFac.createPoint( null, new double[] { 2000, 500 }, null ) );
        style.graphic.mark.wellKnown = Mark.SimpleMark.STAR;
        r.render( style, geomFac.createPoint( null, new double[] { 2500, 500 }, null ) );
        style.graphic.mark.wellKnown = Mark.SimpleMark.CROSS;
        r.render( style, geomFac.createPoint( null, new double[] { 3000, 500 }, null ) );
        style.graphic.mark.wellKnown = Mark.SimpleMark.X;
        r.render( style, geomFac.createPoint( null, new double[] { 3500, 500 }, null ) );

        style = new PointStyling();
        style.graphic.size = 32;
        r.render( style, geomFac.createPoint( null, new double[] { 500, 1000 }, null ) );
        style.graphic.size = 16;
        r.render( style, geomFac.createPoint( null, new double[] { 500, 1000 }, null ) );

        style = new PointStyling();
        style.graphic.size = 32;
        r.render( style, geomFac.createPoint( null, new double[] { 1000, 1000 }, null ) );
        style.graphic.size = 16;
        style.graphic.anchorPointX = 0;
        style.graphic.anchorPointY = 0;
        r.render( style, geomFac.createPoint( null, new double[] { 1000, 1000 }, null ) );

        style = new PointStyling();
        style.graphic.size = 32;
        r.render( style, geomFac.createPoint( null, new double[] { 1500, 1000 }, null ) );
        style.graphic.size = 16;
        style.graphic.anchorPointX = 1;
        style.graphic.anchorPointY = 1;
        r.render( style, geomFac.createPoint( null, new double[] { 1500, 1000 }, null ) );

        style = new PointStyling();
        style.graphic.size = 32;
        r.render( style, geomFac.createPoint( null, new double[] { 2000, 1000 }, null ) );
        style.graphic.size = 16;
        style.graphic.anchorPointX = 0;
        style.graphic.anchorPointY = 0;
        style.graphic.displacementX = -16;
        style.graphic.displacementY = -16;
        r.render( style, geomFac.createPoint( null, new double[] { 2000, 1000 }, null ) );

        style = new PointStyling();
        style.graphic.size = 32;
        r.render( style, geomFac.createPoint( null, new double[] { 2500, 1000 }, null ) );
        style.graphic.size = 16;
        style.graphic.anchorPointX = 1;
        style.graphic.anchorPointY = 1;
        style.graphic.displacementX = 16;
        style.graphic.displacementY = 16;
        r.render( style, geomFac.createPoint( null, new double[] { 2500, 1000 }, null ) );

        g.dispose();
        long time2 = currentTimeMillis();
        List<String> texts = new LinkedList<String>();
        texts.add( "first line: all the marks in the first line (size 6, gray fill, black line)" );
        texts.add( "second line: all the marks in the second line (size 32, red fill, green line)" );
        texts.add( "third line: 32 pixel default square, inside centered a 16 pixel default square" );
        texts.add( "third line: 32 pixel default square, inside a 16 pixel default square (lower right corner)" );
        texts.add( "third line: 32 pixel default square, inside a 16 pixel default square (upper left corner), two times" );
        texts.add( "third line: 32 pixel default square, inside a 16 pixel default square (lower right corner) again" );
        writeTestImage( img, texts, time2 - time );
    }

    /**
     * @throws Exception
     */
    public void testLineStyling()
                            throws Exception {
        BufferedImage img = new BufferedImage( 1000, 1000, TYPE_INT_ARGB );
        long time = currentTimeMillis();
        Graphics2D g = img.createGraphics();
        GeometryFactory geomFac = getInstance().getGeometryFactory();
        Java2DRenderer r = new Java2DRenderer( g, img.getWidth(), img.getHeight(),
                                               geomFac.createEnvelope( new double[] { 0, 0 }, new double[] { 5000d,
                                                                                                            5000d },
                                                                       null ) );

        List<Curve> curves = new LinkedList<Curve>();
        for ( int i = 0; i < 10; ++i ) {
            curves.add( randomCurve( 500, i * 500, 0 ) );
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
                Curve curve = (Curve) move( iterator.next(), 0, y * 500 );
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
        writeTestImage( img, texts, time2 - time );
    }

    /**
     * @throws Exception
     */
    public void testLineStyling2()
                            throws Exception {
        BufferedImage img = new BufferedImage( 1000, 1000, TYPE_INT_ARGB );
        long time = currentTimeMillis();
        Graphics2D g = img.createGraphics();
        GeometryFactory geomFac = getInstance().getGeometryFactory();
        Java2DRenderer r = new Java2DRenderer( g, img.getWidth(), img.getHeight(),
                                               geomFac.createEnvelope( new double[] { 0, 0 }, new double[] { 5000d,
                                                                                                            5000d },
                                                                       null ) );
        List<Curve> curves = new LinkedList<Curve>();
        for ( int i = 0; i < 10; ++i ) {
            curves.add( randomCurve( 500, i * 500, 0 ) );
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
                Curve curve = (Curve) move( iterator.next(), 0, y * 500 );
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
        // texts.add( "line 6: default style lines with line width 0, 1, ..., 9, join mitre" );
        // texts.add( "line 7: default style lines with line width 0, 1, ..., 9, join round" );
        // texts.add( "line 8: default style lines with line width 0, 1, ..., 9, dashed with pattern 15, 15, 17, 5" );
        // texts.add(
        // "line 9: default style lines with line width 0, 1, ..., 9, dashed with pattern 15, 15, 17, 5, ending square"
        // );
        // texts.add(
        // "line 10: default style lines with line width 0, 1, ..., 9, dashed with pattern 15, 15, 17, 5, ending round"
        // );
        writeTestImage( img, texts, time2 - time );
    }

    /**
     * @throws Exception
     */
    public void testPolygonStyling()
                            throws Exception {
        BufferedImage img = new BufferedImage( 1000, 1000, TYPE_INT_ARGB );

        long time = currentTimeMillis();
        Graphics2D g = img.createGraphics();
        GeometryFactory geomFac = getInstance().getGeometryFactory();
        Java2DRenderer r = new Java2DRenderer( g, img.getWidth(), img.getHeight(),
                                               geomFac.createEnvelope( new double[] { 0, 0 }, new double[] { 5000d,
                                                                                                            5000d },
                                                                       null ) );

        List<Surface> polygons = new LinkedList<Surface>();
        for ( int i = 0; i < 10; ++i ) {
            polygons.add( randomQuad( 500, i * 500, 0 ) );
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
                Surface polygon = (Surface) move( surface.next(), 0, y * 500 );
                r.render( styling, polygon );
            }
        }

        g.dispose();
        long time2 = currentTimeMillis();
        List<String> texts = new LinkedList<String>();
        texts.add( "first line: polygon style with black lines with line width 0, 1, ..., 9 and external image fill" );
        texts.add( "other lines: polygon style with black lines with line width 0, 1, ..., 9 and increasingly red fill" );
        writeTestImage( img, texts, time2 - time );
    }

    /**
     * @throws Exception
     */
    public void testTextStyling()
                            throws Exception {
        BufferedImage img = new BufferedImage( 1000, 1000, TYPE_INT_ARGB );

        long time = currentTimeMillis();
        Graphics2D g = img.createGraphics();
        GeometryFactory geomFac = getInstance().getGeometryFactory();
        Java2DRenderer r = new Java2DRenderer( g, img.getWidth(), img.getHeight(),
                                               geomFac.createEnvelope( new double[] { 0, 0 }, new double[] { 5000d,
                                                                                                            5000d },
                                                                       null ) );

        LinkedList<Point> points = new LinkedList<Point>();
        for ( int i = 0; i < 50; ++i ) {
            points.add( geomFac.createPoint( null, new double[] { 2500, ( i + 1 ) * 150 }, null ) );
        }

        String text = "This is a sample text with Umläütß";
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
        styling.rotation = PI;
        r.render( styling, text, points.poll() );
        styling.rotation = PI / 2;
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
        writeTestImage( img, texts, time2 - time );
    }

    /**
     * @throws Exception
     */
    public void testTextStyling2()
                            throws Exception {
        BufferedImage img = new BufferedImage( 1000, 1000, TYPE_INT_ARGB );

        long time = currentTimeMillis();
        Graphics2D g = img.createGraphics();
        GeometryFactory geomFac = getInstance().getGeometryFactory();
        Java2DRenderer r = new Java2DRenderer( g, img.getWidth(), img.getHeight(),
                                               geomFac.createEnvelope( new double[] { 0, 0 }, new double[] { 5000d,
                                                                                                            5000d },
                                                                       null ) );

        LinkedList<Curve> curves = new LinkedList<Curve>();
        for ( int i = 0; i < 6; ++i ) {
            curves.add( randomCurve( 700, i * 800, 100 ) );
        }
        for ( int i = 0; i < 6; ++i ) {
            curves.add( randomCurve( 700, i * 800, 1000 ) );
        }

        LineStyling lineStyle = new LineStyling();
        lineStyle.stroke.width = 20;
        lineStyle.stroke.linecap = BUTT;
        String text = "geeky Street";
        TextStyling styling = new TextStyling();
        styling.linePlacement = new LinePlacement();
        styling.font.fontSize = 15;
        styling.font.fontFamily.add( "Lucida Sans" );
        styling.font.fontFamily.add( "Comic Sans" );
        r.render( lineStyle, curves.peek() );
        r.render( styling, text, curves.poll() );
        styling.linePlacement.repeat = true;
        r.render( lineStyle, curves.peek() );
        r.render( styling, text, curves.poll() );
        r.render( lineStyle, curves.peek() );
        r.render( styling, text, curves.poll() );
        r.render( lineStyle, curves.peek() );
        r.render( styling, text, curves.poll() );
        r.render( lineStyle, curves.peek() );
        r.render( styling, text, curves.poll() );
        r.render( lineStyle, curves.peek() );
        r.render( styling, text, curves.poll() );
        styling.linePlacement.perpendicularOffset = 17.5;
        r.render( lineStyle, curves.peek() );
        r.render( styling, text, curves.poll() );
        r.render( lineStyle, curves.peek() );
        r.render( styling, text, curves.poll() );
        r.render( lineStyle, curves.peek() );
        r.render( styling, text, curves.poll() );
        r.render( lineStyle, curves.peek() );
        r.render( styling, text, curves.poll() );
        r.render( lineStyle, curves.peek() );
        r.render( styling, text, curves.poll() );
        r.render( lineStyle, curves.peek() );
        r.render( styling, text, curves.poll() );

        g.dispose();
        long time2 = currentTimeMillis();
        List<String> texts = new LinkedList<String>();
        texts.add( "first line: renders gray lines width 20, inside a text with size 15." );
        texts.add( "Text should start immediately inside the line. First geometry should include" );
        texts.add( "the text once, the others repeated." );
        texts.add( "second line: renders the same text directly above the gray line (perpendicular offset)" );
        writeTestImage( img, texts, time2 - time );
    }
}
