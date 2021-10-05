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

import static java.awt.image.BufferedImage.TYPE_INT_ARGB;
import static java.lang.System.currentTimeMillis;
import static javax.imageio.ImageIO.read;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.LinkedList;
import java.util.List;

import javax.imageio.ImageIO;

import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.persistence.CRSManager;
import org.deegree.geometry.GeometryFactory;
import org.deegree.geometry.primitive.Curve;
import org.deegree.style.styling.LineStyling;
import org.deegree.style.styling.components.Graphic;
import org.deegree.style.styling.components.Stroke.LineJoin;
import org.deegree.style.styling.mark.BoundedShape;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AnchoredPointTest extends AbstractSimilarityTest {

    private static final Logger LOG = LoggerFactory.getLogger( AnchoredPointTest.class );

    private static final ICRS mapcs = CRSManager.getCRSRef( "CRS:1" );
    
    private static BufferedImage svg;
    
    private static BufferedImage symbol;
    
    private static Shape COARROW;
    
    private static Shape VLINE = new Line2D.Double( 0, -0.5, 0, 0.5 );
    
    private static Shape HLINE = new Line2D.Double( -0.5, 0, 0.5, 0 ); 
    
    static {
        try {
            symbol = read( AnchoredPointTest.class.getResource( "01.png" ) );
            
            symbol = read( AnchoredPointTest.class.getResource( "arrow.png" ) );
            
            SvgRenderer sr = new SvgRenderer();
            Graphic g = new Graphic();
            g.imageURL = AnchoredPointTest.class.getResource( "arrow.svg" ).toExternalForm();
            svg = sr.prepareSvg( new Rectangle2D.Double( 0, 0, 50, 50 ), g );
        } catch ( MalformedURLException e ) {
            LOG.error( "Unknown error", e );
        } catch ( IOException e ) {
            LOG.error( "Unknown error", e );
        }
        
        GeneralPath gp = new GeneralPath();
        gp.moveTo( -0.5f, 0.3f );
        gp.lineTo( 0.5, 0 );
        gp.lineTo( -0.5f, -0.3f );
        COARROW = BoundedShape.inv( gp, new Rectangle2D.Double( -0.32, 0.3, 0.6, 0.6 ) );
    }
    
    private void validateImage( RenderedImage img, double time, String testName )
                            throws Exception {
        LOG.debug( "Test {} ran in {} ms", testName, time );
        RenderedImage expected = ImageIO.read( this.getClass().getResource( "./anchoredpointtest/" + testName + ".png" ) );
        Assert.assertTrue( "Iamge for " + testName + "are not similar enough",
                           isImageSimilar( expected, img, 0.01, testName ) );
    }
    
    @Test
    public void testAnchoredPointMark()
                            throws Exception {
        BufferedImage img = new BufferedImage( 1000, 1000, TYPE_INT_ARGB );
        long time = currentTimeMillis();
        Graphics2D g = img.createGraphics();
        GeometryFactory geomFac = new GeometryFactory();
        Java2DRenderer r = new Java2DRenderer( g, img.getWidth(), img.getHeight(),
                                               geomFac.createEnvelope( new double[] { 0, 0 },
                                                                       new double[] { 5000d, 5000d }, mapcs ) );
        List<Curve> curves = new LinkedList<Curve>();
        for ( int i = 0; i < 12; ++i ) {
            // Left to Right
            //curves.add( testCurve(100 + i*480,100, 300,250));
            // down to up
            curves.add( testCurve(200,200 + i*380, 4400,500));
        }
        LineStyling sline = new LineStyling();
        sline.stroke.color =Color.blue;
        sline.stroke.width=1;
        
        LineStyling tpl = new LineStyling();
        tpl.stroke.stroke = new Graphic();
        tpl.stroke.stroke.size = 20;
        tpl.stroke.stroke.mark.shape = COARROW;
        tpl.stroke.stroke.mark.stroke.color = Color.RED;
        tpl.stroke.stroke.mark.stroke.width = 5;
        tpl.stroke.stroke.mark.stroke.linejoin = LineJoin.MITRE;
        tpl.stroke.stroke.mark.fill.color = Color.GREEN;
        
        for ( int y = 0; y < 12; ++y ) {
            Curve curve = curves.get( y );
            r.render( sline, curve );
            LineStyling sa = tpl.copy();
            // rendering one symbol at start, middle, end, 20% of length and 80% of length
            switch ( y ) {
            case 0:
                sa.stroke.anchoredSymbol = 1001;
                r.render( sa, curve );
                sa.stroke.anchoredSymbol = 1002;
                r.render( sa, curve );
                sa.stroke.anchoredSymbol = 1003;
                r.render( sa, curve );
                sa.stroke.anchoredSymbol = 1005;
                sa.stroke.positionPercentage = 20;
                r.render( sa, curve );
                sa.stroke.positionPercentage = 80;
                r.render( sa, curve );
                break;
            case 1:
                sa.stroke.anchoredSymbol = 1011;
                r.render( sa, curve );
                sa.stroke.anchoredSymbol = 1012;
                r.render( sa, curve );
                sa.stroke.anchoredSymbol = 1013;
                r.render( sa, curve );
                sa.stroke.anchoredSymbol = 1015;
                sa.stroke.positionPercentage = 20;
                r.render( sa, curve );
                sa.stroke.positionPercentage = 80;
                r.render( sa, curve );
                break;
            case 2:
                sa.stroke.stroke.rotation= -90;
                sa.stroke.anchoredSymbol = 1001;
                r.render( sa, curve );
                sa.stroke.anchoredSymbol = 1002;
                r.render( sa, curve );
                sa.stroke.anchoredSymbol = 1003;
                r.render( sa, curve );
                sa.stroke.anchoredSymbol = 1005;
                sa.stroke.positionPercentage = 20;
                r.render( sa, curve );
                sa.stroke.positionPercentage = 80;
                r.render( sa, curve );
                break;
            case 3:
                sa.stroke.stroke.rotation =-90;
                sa.stroke.anchoredSymbol = 1011;
                r.render( sa, curve );
                sa.stroke.anchoredSymbol = 1012;
                r.render( sa, curve );
                sa.stroke.anchoredSymbol = 1013;
                r.render( sa, curve );
                sa.stroke.anchoredSymbol = 1015;
                sa.stroke.positionPercentage = 20;
                r.render( sa, curve );
                sa.stroke.positionPercentage = 80;
                r.render( sa, curve );
                break;
            case 4:
                sa.stroke.anchoredSymbol = 2001;
                r.render( sa, curve );
                sa.stroke.anchoredSymbol = 2002;
                r.render( sa, curve );
                sa.stroke.anchoredSymbol = 2003;
                r.render( sa, curve );
                sa.stroke.anchoredSymbol = 2005;
                sa.stroke.positionPercentage = 20;
                r.render( sa, curve );
                sa.stroke.positionPercentage = 80;
                r.render( sa, curve );
                break;
            case 5:
                sa.stroke.anchoredSymbol = 2011;
                r.render( sa, curve );
                sa.stroke.anchoredSymbol = 2012;
                r.render( sa, curve );
                sa.stroke.anchoredSymbol = 2013;
                r.render( sa, curve );
                sa.stroke.anchoredSymbol = 2015;
                sa.stroke.positionPercentage = 20;
                r.render( sa, curve );
                sa.stroke.positionPercentage = 80;
                r.render( sa, curve );
                break;
            case 6:
                sa.stroke.stroke.rotation= -90;
                sa.stroke.anchoredSymbol = 2001;
                r.render( sa, curve );
                sa.stroke.anchoredSymbol = 2002;
                r.render( sa, curve );
                sa.stroke.anchoredSymbol = 2003;
                r.render( sa, curve );
                sa.stroke.anchoredSymbol = 2005;
                sa.stroke.positionPercentage = 20;
                r.render( sa, curve );
                sa.stroke.positionPercentage = 80;
                r.render( sa, curve );
                break;
            case 7:
                sa.stroke.stroke.rotation =-90;
                sa.stroke.anchoredSymbol = 2011;
                r.render( sa, curve );
                sa.stroke.anchoredSymbol = 2012;
                r.render( sa, curve );
                sa.stroke.anchoredSymbol = 2013;
                r.render( sa, curve );
                sa.stroke.anchoredSymbol = 2015;
                sa.stroke.positionPercentage = 20;
                r.render( sa, curve );
                sa.stroke.positionPercentage = 80;
                r.render( sa, curve );
                break;
            case 8:
                /////
                sa.stroke.anchoredSymbol = 1;
                r.render( sa, curve );
                sa.stroke.anchoredSymbol = 2;
                r.render( sa, curve );
                sa.stroke.anchoredSymbol = 3;
                r.render( sa, curve );
                sa.stroke.anchoredSymbol = 5;
                sa.stroke.positionPercentage = 20;
                r.render( sa, curve );
                sa.stroke.positionPercentage = 80;
                r.render( sa, curve );
                break;
            case 9:
                sa.stroke.anchoredSymbol = 11;
                r.render( sa, curve );
                sa.stroke.anchoredSymbol = 12;
                r.render( sa, curve );
                sa.stroke.anchoredSymbol = 13;
                r.render( sa, curve );
                sa.stroke.anchoredSymbol = 15;
                sa.stroke.positionPercentage = 20;
                r.render( sa, curve );
                sa.stroke.positionPercentage = 80;
                r.render( sa, curve );
                break;
            case 10:
                sa.stroke.stroke.rotation= -90;
                sa.stroke.anchoredSymbol = 1;
                r.render( sa, curve );
                sa.stroke.anchoredSymbol = 2;
                r.render( sa, curve );
                sa.stroke.anchoredSymbol = 3;
                r.render( sa, curve );
                sa.stroke.anchoredSymbol = 5;
                sa.stroke.positionPercentage = 20;
                r.render( sa, curve );
                sa.stroke.positionPercentage = 80;
                r.render( sa, curve );
                break;
            case 11:
                sa.stroke.stroke.rotation =-90;
                sa.stroke.anchoredSymbol = 11;
                r.render( sa, curve );
                sa.stroke.anchoredSymbol = 12;
                r.render( sa, curve );
                sa.stroke.anchoredSymbol = 13;
                r.render( sa, curve );
                sa.stroke.anchoredSymbol = 15;
                sa.stroke.positionPercentage = 20;
                r.render( sa, curve );
                sa.stroke.positionPercentage = 80;
                r.render( sa, curve );
                break;
            }
        }
        
        g.dispose();
        long time2 = currentTimeMillis();
        validateImage( img, time2 - time, "mark" );
    }
    
    @Test
    public void testAnchoredPointImageOrSvg()
                            throws Exception {
        BufferedImage img = new BufferedImage( 1000, 1000, TYPE_INT_ARGB );
        long time = currentTimeMillis();
        Graphics2D g = img.createGraphics();
        GeometryFactory geomFac = new GeometryFactory();
        Java2DRenderer r = new Java2DRenderer( g, img.getWidth(), img.getHeight(),
                                               geomFac.createEnvelope( new double[] { 0, 0 },
                                                                       new double[] { 5000d, 5000d }, mapcs ) );
        List<Curve> curves = new LinkedList<Curve>();
        for ( int i = 0; i < 8; ++i ) {
            // Left to Right
            //curves.add( testCurve(100 + i*480,100, 300,250));
            // down to up
            curves.add( testCurve(200,200 + i*550, 4400,500));
        }
        LineStyling sline = new LineStyling();
        sline.stroke.color =Color.blue;
        sline.stroke.width=1;
        
        LineStyling tpl = new LineStyling();
        tpl.stroke.stroke = new Graphic();
        tpl.stroke.stroke.mark.stroke.color = Color.RED;
        tpl.stroke.stroke.mark.stroke.width = 5;
        tpl.stroke.stroke.mark.stroke.linejoin = LineJoin.MITRE;
        tpl.stroke.stroke.mark.fill.color = Color.GREEN;
        
        for ( int y = 0; y < 8; ++y ) {
            LineStyling sa = tpl.copy();
            switch( y ) {
            case 0:
            case 1:
            case 2:
            case 3:
                sa.stroke.stroke.size = 20;
                sa.stroke.stroke.image = symbol;
                break;
            case 4:
            case 5:
            case 6:
            case 7:
                sa.stroke.stroke.size = 50;
                sa.stroke.stroke.image = svg;
            }
            Curve curve = curves.get( y );
            r.render( sline, curve );
            // rendering one symbol at start, middle, end, 20% of length and 80% of length
            switch ( y ) {
            case 0:
            case 4:
                sa.stroke.anchoredSymbol = 1;
                r.render( sa, curve );
                sa.stroke.anchoredSymbol = 2;
                r.render( sa, curve );
                sa.stroke.anchoredSymbol = 3;
                r.render( sa, curve );
                sa.stroke.anchoredSymbol = 5;
                sa.stroke.positionPercentage = 20;
                r.render( sa, curve );
                sa.stroke.positionPercentage = 80;
                r.render( sa, curve );
                break;
            case 1:
            case 5:
                sa.stroke.anchoredSymbol = 11;
                r.render( sa, curve );
                sa.stroke.anchoredSymbol = 12;
                r.render( sa, curve );
                sa.stroke.anchoredSymbol = 13;
                r.render( sa, curve );
                sa.stroke.anchoredSymbol = 15;
                sa.stroke.positionPercentage = 20;
                r.render( sa, curve );
                sa.stroke.positionPercentage = 80;
                r.render( sa, curve );
                break;
            case 2            :
            case 6:
                sa.stroke.stroke.rotation= -90;
                sa.stroke.anchoredSymbol = 1;
                r.render( sa, curve );
                sa.stroke.anchoredSymbol = 2;
                r.render( sa, curve );
                sa.stroke.anchoredSymbol = 3;
                r.render( sa, curve );
                sa.stroke.anchoredSymbol = 5;
                sa.stroke.positionPercentage = 20;
                r.render( sa, curve );
                sa.stroke.positionPercentage = 80;
                r.render( sa, curve );
                break;
            case 3:
            case 7:
                sa.stroke.stroke.rotation =-90;
                sa.stroke.anchoredSymbol = 11;
                r.render( sa, curve );
                sa.stroke.anchoredSymbol = 12;
                r.render( sa, curve );
                sa.stroke.anchoredSymbol = 13;
                r.render( sa, curve );
                sa.stroke.anchoredSymbol = 15;
                sa.stroke.positionPercentage = 20;
                r.render( sa, curve );
                sa.stroke.positionPercentage = 80;
                r.render( sa, curve );
                break;
            }
        }
        
        g.dispose();
        long time2 = currentTimeMillis();
        
        validateImage( img, time2 - time, "pointimageorsvg" );
    }
 
    
    @Test
    public void testAnchoredPointPositionPercentage()
                            throws Exception {
        BufferedImage img = new BufferedImage( 1000, 1000, TYPE_INT_ARGB );
        long time = currentTimeMillis();
        Graphics2D g = img.createGraphics();
        GeometryFactory geomFac = new GeometryFactory();
        Java2DRenderer r = new Java2DRenderer( g, img.getWidth(), img.getHeight(),
                                               geomFac.createEnvelope( new double[] { 0, 0 },
                                                                       new double[] { 5000d, 5000d }, mapcs ) );
        List<Curve> curves = new LinkedList<Curve>();
        for ( int i = 0; i < 8; ++i ) {
            // Left to Right
            // curves.add( testCurve(100 + i*480,100, 300,250));
            // down to up
            curves.add( testCurve( 200, 200 + i * 550, 4400, 500 ) );
        }
        LineStyling sline = new LineStyling();
        sline.stroke.color = Color.blue;
        sline.stroke.width = 1;

        LineStyling tpl = new LineStyling();
        tpl.stroke.stroke = new Graphic();
        tpl.stroke.stroke.mark.stroke.color = Color.RED;
        tpl.stroke.stroke.mark.stroke.width = 5;
        tpl.stroke.stroke.mark.stroke.linejoin = LineJoin.MITRE;
        tpl.stroke.stroke.mark.fill.color = Color.GREEN;
        
        for ( int y = 0; y < 8; ++y ) {
            LineStyling sa = tpl.copy();
            switch ( y ) {
            case 0:
            case 2:
            case 4:
            case 6:
                sa.stroke.stroke.mark.shape = COARROW;
                sa.stroke.anchoredSymbol = 105;
                sa.stroke.stroke.size = 20;
                break;
            case 1:
            case 3:
            case 5:
            case 7:
                sa.stroke.anchoredSymbol = 105;
                sa.stroke.stroke.size = 20;
                sa.stroke.stroke.image = symbol;
                break;
            }
            
            switch ( y ) {
            case 0:
            case 1:
                sa.stroke.stroke.anchorPointX = 0;
                sa.stroke.stroke.anchorPointY = 0;
                break;
            case 2:
            case 3:
                sa.stroke.stroke.anchorPointX = 0;
                sa.stroke.stroke.anchorPointY = 1;
                break;
            case 4:
            case 5:
                sa.stroke.stroke.displacementY = -20;
                break;
            case 6:
            case 7:
                sa.stroke.stroke.displacementY = 20;
                break;
            }
            
            Curve curve = curves.get( y );
            r.render( sline, curve );
            
            for ( int i = 0; i <= 100 ; i += 20 ) {
                sa.stroke.positionPercentage = i;
                if( i > 25 && i < 75 ) {
                    sa.stroke.stroke.rotation = 45;
                } else {
                    sa.stroke.stroke.rotation = 0;
                }
                r.render( sa, curve );
            }
        }
        
        g.dispose();
        long time2 = currentTimeMillis();
        
        validateImage( img, time2 - time, "pointpositionpercentage" );
    }
    
    @Test
    public void testAnchoredPointZeroWidthHeight()
                            throws Exception {
        BufferedImage img = new BufferedImage( 1000, 1000, TYPE_INT_ARGB );
        long time = currentTimeMillis();
        Graphics2D g = img.createGraphics();
        GeometryFactory geomFac = new GeometryFactory();
        Java2DRenderer r = new Java2DRenderer( g, img.getWidth(), img.getHeight(),
                                               geomFac.createEnvelope( new double[] { 0, 0 },
                                                                       new double[] { 5000d, 5000d }, mapcs ) );
        List<Curve> curves = new LinkedList<Curve>();
        for ( int i = 0; i < 8; ++i ) {
            // Left to Right
            // curves.add( testCurve(100 + i*480,100, 300,250));
            // down to up
            curves.add( testCurve( 200, 200 + i * 550, 4400, 500 ) );
        }
        LineStyling sline = new LineStyling();
        sline.stroke.color = Color.blue;
        sline.stroke.width = 1;

        LineStyling tpl = new LineStyling();
        tpl.stroke.stroke = new Graphic();
        tpl.stroke.stroke.mark.stroke.color = Color.RED;
        tpl.stroke.stroke.mark.stroke.width = 5;
        tpl.stroke.stroke.mark.stroke.linejoin = LineJoin.MITRE;
        tpl.stroke.stroke.mark.fill.color = Color.GREEN;
        
        for ( int y = 0; y < 8; ++y ) {
            LineStyling sa = tpl.copy();
            switch ( y ) {
            case 0:
            case 2:
            case 4:
            case 6:
                sa.stroke.stroke.mark.shape = VLINE;
                sa.stroke.stroke.size = 20;
                break;
            case 1:
            case 3:
            case 5:
            case 7:
                sa.stroke.stroke.mark.shape = HLINE;
                sa.stroke.stroke.size = 20;
                break;
            }
            
            switch ( y ) {
            case 0:
            case 1:
                sa.stroke.stroke.anchorPointX = 0;
                sa.stroke.stroke.anchorPointY = 0;
                break;
            case 2:
            case 3:
                sa.stroke.stroke.anchorPointX = 0;
                sa.stroke.stroke.anchorPointY = 1;
                break;
            case 4:
            case 5:
                sa.stroke.stroke.displacementY = -20;
                break;
            case 6:
            case 7:
                sa.stroke.stroke.displacementY = 20;
                break;
            }
            
            Curve curve = curves.get( y );
            r.render( sline, curve );
            
            if (y < 3) { 
            for ( int i = 0; i <= 100 ; i += 20 ) {
                sa.stroke.anchoredSymbol = 1105;
                sa.stroke.positionPercentage = i;
                if( i > 25 && i < 75 ) {
                    sa.stroke.stroke.rotation = 45;
                } else {
                    sa.stroke.stroke.rotation = 0;
                }
                r.render( sa, curve );
            }
            } else {
                sa.stroke.anchoredSymbol = 1101;
                r.render( sa, curve );
                sa.stroke.anchoredSymbol = 1102;
                r.render( sa, curve );
                sa.stroke.anchoredSymbol = 1103;
                r.render( sa, curve );
            }
        }
        
        g.dispose();
        long time2 = currentTimeMillis();
        
        validateImage( img, time2 - time, "pointzerowidthheight" );
    }
}
