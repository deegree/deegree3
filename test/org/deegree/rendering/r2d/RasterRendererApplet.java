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
package org.deegree.rendering.r2d;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.LookupOp;
import java.awt.image.LookupTable;
import java.awt.image.ShortLookupTable;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.imageio.ImageIO;
import javax.swing.JApplet;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

import org.deegree.coverage.raster.AbstractRaster;
import org.deegree.coverage.raster.SimpleRaster;
import org.deegree.coverage.raster.data.RasterData;
import org.deegree.coverage.raster.utils.RasterFactory;
import org.deegree.crs.CRS;
import org.deegree.filter.function.Categorize;
import org.deegree.filter.function.Interpolate;
import org.deegree.geometry.SimpleGeometryFactory;
import org.deegree.geometry.primitive.Point;
import org.deegree.rendering.r2d.se.parser.SymbologyParser;
import org.deegree.rendering.r2d.se.parser.SymbologyParserTest;
import org.deegree.rendering.r2d.se.unevaluated.Symbolizer;
import org.deegree.rendering.r2d.styling.RasterStyling;
import org.deegree.rendering.r2d.styling.TextStyling;
import org.deegree.rendering.r2d.styling.components.Halo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Andrei Aiordachioaie
 */

public class RasterRendererApplet extends JApplet {
    public static final Logger LOG = LoggerFactory.getLogger( RasterRendererApplet.class );

    private Categorize cat = null;
    private Interpolate interp = null;

    private AbstractRaster image, car, small_car;

    @Override
    public void paint( Graphics g ) {
        g.drawString( "Hello World", 10, 10 );
        loadRasters();
        // renderTwoTransparentRasters();
        // invertImageLookupTable();
        // applyCategorizeOnImage();
        // renderTextWithStyle();
//        renderRasterWithStyle();
        renderRasterWithStyleReloaded();
        // testCategorize();
    }

    @Override
    public void init() {
        try {
            System.out.println( "Loading XML ... " );
            cat = loadCategorizeFromXml("setest17.xml");
            interp = loadInterpolateFromXml("setest16.xml");
            // cat.buildLookupArrays();
            System.out.println( "Finished loading XML ... " );
            if ( cat != null )
                System.out.println( "Found Categorize: " + cat );
        } catch ( Exception e ) {
            e.printStackTrace();
        }

        this.setSize( 800, 500 );
    }

    public void invertImageLookupTable() {
        Graphics2D g2d = (Graphics2D) this.getGraphics();
        Java2DRasterRenderer renderer = new Java2DRasterRenderer( g2d );
        RasterStyling style = new RasterStyling();
        style.categorize = this.cat;

        BufferedImage img = RasterFactory.imageFromRaster( car );
        short data[] = new short[256];
        for ( int i = 0; i < 256; i++ )
            data[i] = (short) ( 255 - i );
        LookupTable table = new ShortLookupTable( 0, data );
        BufferedImageOp op = new LookupOp( table, null );
        BufferedImage img2 = op.filter( img, null );
        g2d.drawImage( img2, null, 50, 50 );
        // renderer.render(style, raster);
    }

    public void applyCategorizeOnImage() {
        Graphics2D g2d = (Graphics2D) this.getGraphics();
        System.out.println( "Using categorize: " + cat );
        Java2DRasterRenderer renderer = new Java2DRasterRenderer( g2d );
        RasterStyling style = new RasterStyling();
        style.categorize = cat;
        BufferedImage img = RasterFactory.imageFromRaster( image );
        // renderer.render(style, raster);
        // BufferedImage img = cat.buildImage(raster);
        g2d.drawImage( img, null, 480, 50 );
        renderer.render( style, image );
    }

    public void invertImageColors() {
        short[] inv = new short[256], norm = new short[256];
        for ( int i = 0; i < 256; i++ ) {
            norm[i] = (short) i;
            inv[i] = (short) ( 255 - i );
        }
        // short[][] f = new short[][] {norm, norm, inv};
        LookupTable table = new ShortLookupTable( 0, inv );
        BufferedImageOp categ = new LookupOp( table, null );
        try {
            LOG.info( "Rendering image with inverted colors..." );
            URI uri = RasterRendererApplet.class.getResource( "lady.jpg" ).toURI();
            BufferedImage img = ImageIO.read( uri.toURL() );
            // BufferedImage img2 = categ.filter(img, null);
            Graphics2D g2d = (Graphics2D) this.getGraphics();
            // Java2DRasterRenderer renderer = new Java2DRasterRenderer(g2d);
            g2d.drawImage( img, categ, 100, 100 );
            LOG.info( "Done rendering inverted image !" );
        } catch ( Exception e ) {
            e.printStackTrace();
        }
    }

    public void renderTwoTransparentRasters() {
        Graphics2D g2d = (Graphics2D) this.getGraphics();
        URI uri;
        try {
            Java2DRasterRenderer renderer = new Java2DRasterRenderer( g2d );
            AbstractRaster raster = null;

            RasterStyling style = new RasterStyling();
            style.opacity = 0.05;
            renderer.render( style, raster );
            uri = RasterRendererApplet.class.getResource( "image.png" ).toURI();
            AbstractRaster raster2 = RasterFactory.loadRasterFromFile( new File( uri ) );
            style.opacity = 0.8;
            renderer.render( style, raster2 );

        } catch ( IOException ex ) {
            ex.printStackTrace();
        } catch ( URISyntaxException e ) {
            e.printStackTrace();
        }
    }

    public void renderTextWithStyle() {
        Java2DRenderer textRenderer = new Java2DRenderer( (Graphics2D) this.getGraphics() );
        TextStyling style1 = new TextStyling();
        style1.halo = new Halo();
        style1.fill.color = Color.red;
        style1.halo.radius = 3;

        System.out.println( "Rendering text ..." );
        Point p = new SimpleGeometryFactory().createPoint( "id1", 100, 100, new CRS( "WGS84" ) );
        textRenderer.render( style1, "This is an example text...", p );
        // p = new SimpleGeometryFactory().createPoint("id1", 100, 130, new CRS("WGS84"));
        // textRenderer.render(style1, "this panel will close automatically...", p);
        System.out.println( "Finished text rendering ..." );
    }

    public void simplyRenderOneRaster() {
        try {
            Java2DRasterRenderer renderer = new Java2DRasterRenderer( (Graphics2D) this.getGraphics() );
            // depending on the raster loader and the raster file, the may contain a crs...
            SimpleRaster r1 = image.getAsSimpleRaster();
            RasterData data = r1.getRasterData();
            this.getGraphics().drawImage( RasterFactory.rasterDataToImage( data ), 10, 10, null );

            renderer.render( null, image );
            System.out.println( "Finished raster rendering ..." );
        } catch ( Exception ex ) {
            ex.printStackTrace();
            System.out.println( "Error while rendering raster" );
        }

    }

    public Categorize loadCategorizeFromXml(String name) {
        RasterStyling style = loadRasterStylingFromXml(name);
        if ( style != null )
            return style.categorize;
        else
            return null;
    }
    
    public Interpolate loadInterpolateFromXml(String name) {
        RasterStyling style = loadRasterStylingFromXml(name);
        if ( style != null )
            return style.interpolate;
        else
            return null;
    }

    public RasterStyling loadRasterStylingFromXml(String fname) {
        RasterStyling rs = null;
        try {
            LOG.debug( "Loading SE XML..." );
            URI uri = SymbologyParserTest.class.getResource( fname ).toURI();
            System.out.println( "Loading resource: " + uri );
            File f = new File( uri );
            final XMLInputFactory fac = XMLInputFactory.newInstance();
            XMLStreamReader in = fac.createXMLStreamReader( f.toString(), new FileInputStream( f ) );
            in.next();
            if ( in.getEventType() == XMLStreamConstants.START_DOCUMENT ) {
                in.nextTag();
            }
            in.require( XMLStreamConstants.START_ELEMENT, null, "RasterSymbolizer" );
            Symbolizer<RasterStyling> symb = SymbologyParser.parseRasterSymbolizer( in );
            rs = symb.getBase();
            LOG.debug( "Loaded SE XML" );
        } catch ( Exception e ) {
            LOG.error( "Could not load XML file...", e );
        }

        return rs;
    }

    public static void printFileContents( File f ) {
        try {
            String strCat = "", line;
            BufferedReader buf = new BufferedReader( new FileReader( f ) );
            while ( ( line = buf.readLine() ) != null ) {
                strCat += line;
                System.err.println( line );
            }
            LOG.info( strCat );
        } catch ( IOException e ) {
            e.printStackTrace();
        }
    }

    private void testCategorize() {
        LOG.info( "lookup1: {}, lookup2: {}", cat.lookup( 100 ), cat.lookup2( 100 ) );
        LOG.info( "lookup1: {}, lookup2: {}", cat.lookup( -1 ), cat.lookup2( -1 ) );
        LOG.info( "lookup1: {}, lookup2: {}", cat.lookup( -0.2 ), cat.lookup2( -0.2 ) );
        LOG.info( "lookup1: {}, lookup2: {}", cat.lookup( 0 ), cat.lookup2( 0 ) );
    }

    /* Load a RasterStyle that contains a Categorize operation for the ColorMap */
    private void renderRasterWithStyle() {
        RasterStyling style = loadRasterStylingFromXml("setest17.xml");
        LOG.debug( "Found opacity: {}", style.opacity );
        Graphics2D g2d = (Graphics2D) this.getGraphics();
        Java2DRasterRenderer r = new Java2DRasterRenderer( g2d );

        r.render( style, car );
    }
    
    /* Load a RasterStyle, that contains an Interpolation operation for the ColorMap */
    private void renderRasterWithStyleReloaded() {
        RasterStyling style = loadRasterStylingFromXml("setest16.xml");
        LOG.debug( "Found interpolate: {}", style.interpolate );
        Graphics2D g2d = (Graphics2D) this.getGraphics();
        Java2DRasterRenderer r = new Java2DRasterRenderer( g2d );

        r.render( style, car );
    }

    private void loadRasters() {
        try {
            LOG.debug( "Loading images..." );
            URI uri = SymbologyParserTest.class.getResource( "image.png" ).toURI();
            image = RasterFactory.loadRasterFromFile( new File( uri ) );

            uri = RasterRendererApplet.class.getResource( "car.jpg" ).toURI();
            car = RasterFactory.loadRasterFromFile( new File( uri ) );

            uri = RasterRendererApplet.class.getResource( "small_car.jpg" ).toURI();
            small_car = RasterFactory.loadRasterFromFile( new File( uri ) );

//            uri = RasterRendererApplet.class.getResource( "lady.jpg" ).toURI();
//            lady = RasterFactory.loadRasterFromFile( new File( uri ) );
            LOG.debug( "Loaded images" );
        } catch ( Exception e ) {
            LOG.error( "Could not load images...", e );
        }
    }

}