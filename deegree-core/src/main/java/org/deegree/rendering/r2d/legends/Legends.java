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

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.rendering.r2d.legends;

import static java.awt.Font.PLAIN;
import static java.lang.Math.max;
import static org.deegree.commons.utils.CollectionUtils.AND;
import static org.deegree.commons.utils.CollectionUtils.map;
import static org.deegree.commons.utils.CollectionUtils.reduce;
import static org.deegree.rendering.r2d.styling.components.UOM.Metre;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.deegree.commons.utils.CollectionUtils;
import org.deegree.commons.utils.Pair;
import org.deegree.commons.utils.CollectionUtils.Mapper;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.GeometryFactory;
import org.deegree.geometry.primitive.LineString;
import org.deegree.geometry.primitive.Point;
import org.deegree.geometry.primitive.Polygon;
import org.deegree.geometry.standard.DefaultEnvelope;
import org.deegree.rendering.r2d.Java2DRenderer;
import org.deegree.rendering.r2d.Java2DTextRenderer;
import org.deegree.rendering.r2d.se.unevaluated.Style;
import org.deegree.rendering.r2d.styling.LineStyling;
import org.deegree.rendering.r2d.styling.PointStyling;
import org.deegree.rendering.r2d.styling.Styling;
import org.deegree.rendering.r2d.styling.TextStyling;

/**
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class Legends {

    private int baseSizeX = 20, baseSizeY = 15, textSize = 12;

    private GeometryFactory geofac = new GeometryFactory();

    /**
     * New legend renderer with base size of 20x15, text size of 12
     */
    public Legends() {
        // default values
    }

    /**
     * @param baseSizeX
     * @param baseSizeY
     * @param textSize
     */
    public Legends( int baseSizeX, int baseSizeY, int textSize ) {
        this.baseSizeX = baseSizeX;
        this.baseSizeY = baseSizeY;
        this.textSize = textSize;
    }

    /**
     * @param xpos
     * @param ypos
     * @param xsize
     * @param ysize
     * @return a made up rectangle to be used in a legend
     */
    public Polygon getLegendRect( int xpos, int ypos, int xsize, int ysize ) {
        Point p1 = geofac.createPoint( null, xpos, ypos, null );
        Point p2 = geofac.createPoint( null, xpos + xsize, ypos, null );
        Point p3 = geofac.createPoint( null, xpos + xsize, ypos + ysize, null );
        Point p4 = geofac.createPoint( null, xpos, ypos + ysize, null );
        List<Point> ps = new ArrayList<Point>( 5 );
        ps.add( p1 );
        ps.add( p2 );
        ps.add( p3 );
        ps.add( p4 );
        ps.add( p1 );

        return geofac.createPolygon( null, null, geofac.createLinearRing( null, null, geofac.createPoints( ps ) ), null );
    }

    /**
     * @param xpos
     * @param ypos
     * @param xsz
     * @param ysz
     * @return a made up line string to be used in a legend
     */
    public LineString getLegendLine( int xpos, int ypos, int xsz, int ysz ) {
        Point p1 = geofac.createPoint( null, xpos, ypos, null );
        Point p2 = geofac.createPoint( null, xpos + xsz / 3, ypos + ysz / 3 * 2, null );
        Point p3 = geofac.createPoint( null, xpos + xsz / 3 * 2, ypos + ysz / 3, null );
        Point p4 = geofac.createPoint( null, xpos + xsz, ypos + ysz, null );
        List<Point> ps = new ArrayList<Point>( 4 );
        ps.add( p1 );
        ps.add( p2 );
        ps.add( p3 );
        ps.add( p4 );
        return geofac.createLineString( null, null, geofac.createPoints( ps ) );
    }

    /**
     * @param style
     * @param width
     * @param height
     * @param g
     */
    public void paintLegend( Style style, int width, int height, Graphics2D g ) {
        Pair<Integer, Integer> size = getLegendSize( style );

        Java2DRenderer renderer = new Java2DRenderer( g, width, height,
                                                      new DefaultEnvelope( geofac.createPoint( null, 0, 0, null ),
                                                                           geofac.createPoint( null, size.first,
                                                                                               size.second, null ) ) );
        Java2DTextRenderer textRenderer = new Java2DTextRenderer( renderer );

        int ypos = 6;
        int xpos = 6;

        Iterator<Class<?>> types = style.getRuleTypes().iterator();
        TextStyling textStyling = new TextStyling();
        textStyling.font = new org.deegree.rendering.r2d.styling.components.Font();
        textStyling.font.fontFamily.add( 0, "Arial" );
        textStyling.font.fontSize = textSize;
        textStyling.anchorPointX = 0;
        textStyling.anchorPointY = 0.5;
        textStyling.uom = Metre;

        Mapper<Boolean, Styling> pointStylingMapper = CollectionUtils.<Styling> getInstanceofMapper( PointStyling.class );
        Mapper<Boolean, Styling> lineStylingMapper = CollectionUtils.<Styling> getInstanceofMapper( LineStyling.class );

        Iterator<String> titles = style.getRuleTitles().iterator();
        for ( LinkedList<Styling> styles : style.getBases() ) {
            String title = titles.next();
            Class<?> c = types.next();
            boolean isPoint = c.equals( Point.class ) || reduce( true, map( styles, pointStylingMapper ), AND );
            boolean isLine = c.equals( LineString.class ) || reduce( true, map( styles, lineStylingMapper ), AND );

            Geometry geom;
            if ( isPoint ) {
                geom = geofac.createPoint( null, xpos + 10, ypos + 10, null );
            } else if ( isLine ) {
                geom = getLegendLine( xpos, ypos, baseSizeX, baseSizeY );
            } else {
                // something better?
                geom = getLegendRect( xpos, ypos, baseSizeX, baseSizeY );
            }
            if ( title != null && title.length() > 0 ) {
                textRenderer.render( textStyling, title, geofac.createPoint( null, 15 + baseSizeX,
                                                                             ypos + baseSizeY / 2, null ) );
            }
            ypos += 12 + baseSizeY;

            double maxSize = 0;
            if ( isPoint ) {
                for ( Styling s : styles ) {
                    if ( s instanceof PointStyling ) {
                        maxSize = max( ( (PointStyling) s ).graphic.size, maxSize );
                    }
                }
            }

            for ( Styling styling : styles ) {
                // normalize point symbols to 20 pixels
                if ( styling instanceof PointStyling && isPoint ) {
                    PointStyling s = ( (PointStyling) styling ).copy();
                    s.uom = Metre;
                    s.graphic.size = s.graphic.size / maxSize * Math.max( baseSizeX, baseSizeY );
                    styling = s;
                }
                renderer.render( styling, geom );
            }
        }
        g.dispose();
    }

    /**
     * @param style
     * @return the legend width/height given a base size of 32x32
     */
    public Pair<Integer, Integer> getLegendSize( Style style ) {
        Pair<Integer, Integer> res = new Pair<Integer, Integer>( 0, 0 );

        res.second = ( 12 + baseSizeY ) * style.getBases().size();
        res.first = 12 + baseSizeX;

        Font font = new Font( "Arial", PLAIN, textSize );

        for ( String s : style.getRuleTitles() ) {
            if ( s != null && s.length() > 0 ) {
                TextLayout layout = new TextLayout( s, font, new FontRenderContext( new AffineTransform(), true, false ) );
                res.first = (int) max( layout.getBounds().getWidth() + ( 2 * baseSizeX ), res.first );
            }
        }

        return res;
    }

}
