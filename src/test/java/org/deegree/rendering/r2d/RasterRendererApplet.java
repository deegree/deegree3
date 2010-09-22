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

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;

import javax.swing.JApplet;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

import org.deegree.coverage.raster.AbstractRaster;
import org.deegree.coverage.raster.utils.RasterFactory;
import org.deegree.filter.expression.custom.se.Categorize;
import org.deegree.filter.expression.custom.se.Interpolate;
import org.deegree.rendering.r2d.se.parser.SymbologyParser;
import org.deegree.rendering.r2d.se.parser.SymbologyParserTest;
import org.deegree.rendering.r2d.se.unevaluated.Symbolizer;
import org.deegree.rendering.r2d.styling.RasterStyling;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test applet for raster rendering functions.
 * 
 * @author Andrei Aiordachioaie
 */
public class RasterRendererApplet extends JApplet {
    private static final long serialVersionUID = 5323930312991827270L;

    public static final Logger LOG = LoggerFactory.getLogger( RasterRendererApplet.class );

    private AbstractRaster image, car, dem, doll, snow;

    @Override
    public void paint( Graphics g ) {
        loadRasters();

        renderRasterWithCategorize();
        renderRasterWithInterpolate();
        renderHillShadedRaster();
        renderRasterSelectedChannels();
    }

    @Override
    public void init() {
        this.setSize( 840, 800 );
    }

    public Categorize loadCategorizeFromXml( String name ) {
        RasterStyling style = loadRasterStylingFromXml( name );
        if ( style != null )
            return style.categorize;
        else
            return null;
    }

    public Interpolate loadInterpolateFromXml( String name ) {
        RasterStyling style = loadRasterStylingFromXml( name );
        if ( style != null )
            return style.interpolate;
        else
            return null;
    }

    public RasterStyling loadRasterStylingFromXml( String fname ) {
        RasterStyling rs = null;
        try {
            // LOG.debug( "Loading SE XML..." );
            URI uri = SymbologyParserTest.class.getResource( fname ).toURI();
            LOG.debug( "Loading resource: " + uri );
            File f = new File( uri );
            final XMLInputFactory fac = XMLInputFactory.newInstance();
            XMLStreamReader in = fac.createXMLStreamReader( f.toString(), new FileInputStream( f ) );
            in.next();
            if ( in.getEventType() == XMLStreamConstants.START_DOCUMENT ) {
                in.nextTag();
            }
            in.require( XMLStreamConstants.START_ELEMENT, null, "RasterSymbolizer" );
            Symbolizer<RasterStyling> symb = SymbologyParser.INSTANCE.parseRasterSymbolizer( in, null );
            rs = symb.evaluate( null, null ).first;
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
            LOG.debug( strCat );
        } catch ( IOException e ) {
            e.printStackTrace();
        }
    }

    /* Load a RasterStyle that contains a Categorize operation for the ColorMap */
    private void renderRasterWithCategorize() {
        RasterStyling style = loadRasterStylingFromXml( "setest17.xml" );
        LOG.debug( "Found opacity: {}", style.opacity );
        Graphics2D g2d = (Graphics2D) this.getGraphics();
        Java2DRasterRenderer r = new Java2DRasterRenderer( g2d );

        r.render( style, car );
    }

    /* Render a raster after an Interpolation operation for the ColorMap */
    private void renderRasterWithInterpolate() {
        RasterStyling style = loadRasterStylingFromXml( "setest18.xml" );
        LOG.debug( "Found interpolate: {}", style.interpolate );
        Graphics2D g2d = (Graphics2D) this.getGraphics().create( 420, 0, 400, 380 );
        Java2DRasterRenderer r = new Java2DRasterRenderer( g2d );

        r.render( style, car );
    }

    /* Render a raster after hill-shading */
    private void renderHillShadedRaster() {
        RasterStyling style = loadRasterStylingFromXml( "setest21.xml" );
        LOG.debug( "Found hill-shading: {}", style.shaded );
        LOG.debug( "Found interpolate: {}", style.interpolate );
        Graphics2D g2d = (Graphics2D) this.getGraphics().create( 10, 310, 370, 370 );
        Java2DRasterRenderer r = new Java2DRasterRenderer( g2d );
        r.render( style, dem );
    }

    /* Render a raster after selecting (actually swapping) channels */
    private void renderRasterSelectedChannels() {
        RasterStyling style = loadRasterStylingFromXml( "setest20.xml" );
        Graphics2D g2d = (Graphics2D) this.getGraphics().create( 400, 310, 370, 370 );
        Java2DRasterRenderer r = new Java2DRasterRenderer( g2d );
        r.render( style, doll );
    }

    private void loadRasters() {
        try {
            LOG.trace( "Loading images..." );

            URI uri = SymbologyParserTest.class.getResource( "image.png" ).toURI();
            image = RasterFactory.loadRasterFromFile( new File( uri ) );

            uri = RasterRendererApplet.class.getResource( "car.jpg" ).toURI();
            car = RasterFactory.loadRasterFromFile( new File( uri ) );

            uri = RasterRendererApplet.class.getResource( "demimage.png" ).toURI();
            dem = RasterFactory.loadRasterFromFile( new File( uri ) );

            uri = RasterRendererApplet.class.getResource( "RussianDoll.jpg" ).toURI();
            doll = RasterFactory.loadRasterFromFile( new File( uri ) );

            uri = RasterRendererApplet.class.getResource( "snow.jpg" ).toURI();
            snow = RasterFactory.loadRasterFromFile( new File( uri ) );

            LOG.trace( "Loaded images" );
        } catch ( Exception e ) {
            LOG.error( "Could not load images...", e );
        }
    }

}