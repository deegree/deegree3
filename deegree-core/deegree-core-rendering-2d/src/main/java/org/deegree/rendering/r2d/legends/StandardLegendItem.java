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
import static java.lang.Math.min;
import static org.deegree.commons.utils.CollectionUtils.AND;
import static org.deegree.commons.utils.CollectionUtils.map;
import static org.deegree.commons.utils.CollectionUtils.reduce;
import static org.deegree.rendering.r2d.legends.Legends.paintLegendText;
import static org.deegree.style.styling.components.UOM.Metre;

import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.deegree.commons.utils.CollectionUtils;
import org.deegree.commons.utils.CollectionUtils.Mapper;
import org.deegree.commons.utils.Pair;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.persistence.CRSManager;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.GeometryFactory;
import org.deegree.geometry.primitive.LineString;
import org.deegree.geometry.primitive.Point;
import org.deegree.geometry.primitive.Polygon;
import org.deegree.rendering.r2d.Renderer;
import org.deegree.rendering.r2d.TextRenderer;
import org.deegree.style.se.unevaluated.Continuation;
import org.deegree.style.se.unevaluated.Symbolizer;
import org.deegree.style.styling.LineStyling;
import org.deegree.style.styling.PointStyling;
import org.deegree.style.styling.Styling;

/**
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class StandardLegendItem implements LegendItem {

    private static final GeometryFactory geofac = new GeometryFactory();

    private static final ICRS mapcs = CRSManager.getCRSRef( "CRS:1" );

    private LinkedList<Styling> stylings;

    private Continuation<LinkedList<Symbolizer<?>>> rule;

    private Class<?> ruleType;

    private String text;

    private Renderer renderer;

    private TextRenderer textRenderer;

    public StandardLegendItem( LinkedList<Styling> stylings, Continuation<LinkedList<Symbolizer<?>>> rule,
                               Class<?> ruleType, String text, Renderer renderer, TextRenderer textRenderer ) {
        this.stylings = stylings;
        this.rule = rule;
        this.ruleType = ruleType;
        this.text = text;
        this.renderer = renderer;
        this.textRenderer = textRenderer;
    }

    @Override
    public int getHeight() {
        return 1;
    }

    @Override
    public void paint( int origin, LegendOptions opts ) {
        Mapper<Boolean, Styling> pointStylingMapper = CollectionUtils.<Styling> getInstanceofMapper( PointStyling.class );
        Mapper<Boolean, Styling> lineStylingMapper = CollectionUtils.<Styling> getInstanceofMapper( LineStyling.class );

        boolean isPoint = ruleType.equals( Point.class ) || reduce( true, map( stylings, pointStylingMapper ), AND );
        boolean isLine = ruleType.equals( LineString.class ) || reduce( true, map( stylings, lineStylingMapper ), AND );

        Geometry geom;
        if ( isPoint ) {
            geom = geofac.createPoint( null, opts.spacing + opts.baseWidth / 2, origin - opts.spacing - opts.baseHeight
                                                                                / 2, mapcs );
        } else if ( isLine ) {
            geom = getLegendLine( opts.spacing, origin - opts.spacing, opts.baseWidth, opts.baseHeight );
        } else {
            // something better?
            geom = getLegendRect( opts.spacing, origin - opts.spacing, opts.baseWidth, opts.baseHeight );
        }

        paintLegendText( origin, opts, text, textRenderer );

        // normalize symbol sizes
        double maxSize = 0;
        if ( isPoint ) {
            for ( Styling s : stylings ) {
                if ( s instanceof PointStyling ) {
                    maxSize = max( ( (PointStyling) s ).graphic.size, maxSize );
                }
            }
        }

        if ( rule == null ) {
            for ( Styling s : stylings ) {
                renderer.render( s, geom );
            }
            return;
        }

        LinkedList<Symbolizer<?>> syms = new LinkedList<Symbolizer<?>>();
        rule.evaluate( syms, null, null );

        Iterator<Styling> bases = stylings.iterator();
        for ( Symbolizer<?> s : syms ) {
            Styling styling = bases.next();
            Pair<?, LinkedList<Geometry>> evald = s.evaluate( null, null );
            if ( evald.second.isEmpty() ) {
                // normalize relative point symbol sizes
                if ( styling instanceof PointStyling && isPoint ) {
                    PointStyling ps = ( (PointStyling) styling ).copy();
                    ps.uom = Metre;
                    ps.graphic.size = ps.graphic.size / maxSize * min( opts.baseWidth, opts.baseHeight );
                    styling = ps;
                }
                renderer.render( styling, geom );
            } else {
                for ( Geometry gm : evald.second ) {
                    if ( gm == null ) {
                        gm = geom;
                    }
                    renderer.render( (Styling) evald.first, gm );
                }
            }
        }
    }

    @Override
    public int getMaxWidth( LegendOptions opts ) {
        int res = 2 * opts.spacing + opts.baseWidth;

        Font font = new Font( "Arial", PLAIN, opts.textSize );

        if ( text != null && text.length() > 0 ) {
            TextLayout layout = new TextLayout( text, font, new FontRenderContext( new AffineTransform(), true, false ) );
            res = (int) max( layout.getBounds().getWidth() + ( 2 * opts.baseWidth ), res );
        }
        return res;
    }

    private static Polygon getLegendRect( int xpos, int ypos, int xsize, int ysize ) {
        Point p1 = geofac.createPoint( null, xpos, ypos, mapcs );
        Point p2 = geofac.createPoint( null, xpos + xsize, ypos, mapcs );
        Point p3 = geofac.createPoint( null, xpos + xsize, ypos - ysize, mapcs );
        Point p4 = geofac.createPoint( null, xpos, ypos - ysize, mapcs );
        List<Point> ps = new ArrayList<Point>( 5 );
        ps.add( p1 );
        ps.add( p2 );
        ps.add( p3 );
        ps.add( p4 );
        ps.add( p1 );

        return geofac.createPolygon( null, mapcs, geofac.createLinearRing( null, null, geofac.createPoints( ps ) ),
                                     null );
    }

    private static LineString getLegendLine( int xpos, int ypos, int xsz, int ysz ) {
        Point p1 = geofac.createPoint( null, xpos, ypos - ysz, mapcs );
        Point p2 = geofac.createPoint( null, xpos + xsz / 3, ypos - ysz / 3, mapcs );
        Point p3 = geofac.createPoint( null, xpos + xsz / 3 * 2, ypos - ysz / 3 * 2, mapcs );
        Point p4 = geofac.createPoint( null, xpos + xsz, ypos, mapcs );
        List<Point> ps = new ArrayList<Point>( 4 );
        ps.add( p1 );
        ps.add( p2 );
        ps.add( p3 );
        ps.add( p4 );
        return geofac.createLineString( null, mapcs, geofac.createPoints( ps ) );
    }

}
