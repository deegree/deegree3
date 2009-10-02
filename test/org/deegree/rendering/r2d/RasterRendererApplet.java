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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.JApplet;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.deegree.coverage.raster.AbstractRaster;
import org.deegree.coverage.raster.SimpleRaster;
import org.deegree.coverage.raster.data.RasterData;
import org.deegree.coverage.raster.utils.RasterFactory;
import org.deegree.crs.CRS;
import org.deegree.filter.function.Categorize;
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

    Categorize cat = null;

    public void init() {
        try {
            System.out.println( "Loading XML ... " );
            cat = loadCategorizeFromXml();
            System.out.println( "Finished loading XML ... " );
            if ( cat != null )
                System.out.println( "Found Categorize: " + cat );
            this.setSize( 800, 800 );
        } catch ( Exception e ) {
            e.printStackTrace();
        }
    }

    public void paint( Graphics g ) {
        g.drawString( "Hello World", 10, 10 );
        renderTwoTransparentRasters();
        applyCategorizeOnImage();
        renderTextWithStyle();
    }

    public void applyCategorizeOnImage() {
        URI uri;
        Graphics2D g2d = (Graphics2D) this.getGraphics();
        try {
            System.out.println( "Using categorize: " + cat );
            Java2DRasterRenderer renderer = new Java2DRasterRenderer( g2d );
            RasterStyling style = new RasterStyling();
            style.categorize = cat;
            uri = Java2DRenderingTest.class.getResource( "car.jpg" ).toURI();
            AbstractRaster raster = RasterFactory.loadRasterFromFile( new File( uri ) );
            renderer.render( style, raster );
            BufferedImage img = RasterFactory.imageFromRaster( raster );
            g2d.drawImage( img, null, 400, 0 );
        } catch ( IOException ex ) {
            ex.printStackTrace();
        } catch ( URISyntaxException e ) {
            e.printStackTrace();
        }
    }

    public void renderTwoTransparentRasters() {
        Graphics2D g2d = (Graphics2D) this.getGraphics();
        URI uri;
        try {
            Java2DRasterRenderer renderer = new Java2DRasterRenderer( g2d );
            AbstractRaster raster = null;

            uri = Java2DRenderingTest.class.getResource( "car.jpg" ).toURI();
            raster = RasterFactory.loadRasterFromFile( new File( uri ) );
            RasterStyling style = new RasterStyling();
            style.opacity = 0.05;
            renderer.render( style, raster );
            uri = SymbologyParserTest.class.getResource( "image.png" ).toURI();
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
        textRenderer.render( style1, "Please wait for a while", p );
        p = new SimpleGeometryFactory().createPoint( "id1", 100, 130, new CRS( "WGS84" ) );
        textRenderer.render( style1, "this panel will close automatically...", p );
        System.out.println( "Finished text rendering ..." );
    }

    public void simplyRenderOneRaster() {
        try {
            Java2DRasterRenderer renderer = new Java2DRasterRenderer( (Graphics2D) this.getGraphics() );
            URI uri = SymbologyParserTest.class.getResource( "image.png" ).toURI();
            AbstractRaster raster = RasterFactory.loadRasterFromFile( new File( uri ) );
            // and render
            SimpleRaster r1 = raster.getAsSimpleRaster();
            RasterData data = r1.getRasterData();
            this.getGraphics().drawImage( RasterFactory.rasterDataToImage( data ), 10, 10, null );

            renderer.render( null, raster );
            System.out.println( "Finished raster rendering ..." );
        } catch ( Exception ex ) {
            ex.printStackTrace();
            System.out.println( "Error while rendering raster" );
        }

    }

    public Categorize loadCategorizeFromXml()
                            throws URISyntaxException, XMLStreamException, FileNotFoundException {
        URI uri = SymbologyParserTest.class.getResource( "setest17.xml" ).toURI();
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
        RasterStyling rs = symb.getBase();
        cat = rs.categorize;

        return cat;
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

}