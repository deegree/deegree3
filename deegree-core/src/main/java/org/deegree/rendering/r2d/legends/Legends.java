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

import static java.lang.Math.max;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.deegree.commons.utils.DoublePair;
import org.deegree.commons.utils.Pair;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.GeometryFactory;
import org.deegree.rendering.r2d.Java2DRenderer;
import org.deegree.rendering.r2d.Java2DTextRenderer;
import org.deegree.rendering.r2d.se.unevaluated.Continuation;
import org.deegree.rendering.r2d.se.unevaluated.Style;
import org.deegree.rendering.r2d.se.unevaluated.Symbolizer;
import org.deegree.rendering.r2d.styling.RasterStyling;
import org.deegree.rendering.r2d.styling.Styling;

/**
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class Legends {

    private static final GeometryFactory geofac = new GeometryFactory();

    private LegendOptions opts;

    /**
     * New legend renderer with default legend options
     */
    public Legends() {
        opts = new LegendOptions();
    }

    /**
     * @param opts
     */
    public Legends( LegendOptions opts ) {
        this.opts = opts;
    }

    public LegendOptions getLegendOptions() {
        return opts;
    }

    private List<LegendItem> prepareLegend( Style style, Graphics2D g, Java2DRenderer renderer,
                                            Java2DTextRenderer textRenderer ) {
        List<LegendItem> items = new LinkedList<LegendItem>();
        LinkedList<Class<?>> ruleTypes = style.getRuleTypes();
        Iterator<Class<?>> types = ruleTypes.iterator();
        LinkedList<String> ruleTitles = style.getRuleTitles();
        Iterator<String> titles = ruleTitles.iterator();
        LinkedList<Pair<Continuation<LinkedList<Symbolizer<?>>>, DoublePair>> rules;
        rules = new LinkedList<Pair<Continuation<LinkedList<Symbolizer<?>>>, DoublePair>>( style.getRules() );
        Iterator<Pair<Continuation<LinkedList<Symbolizer<?>>>, DoublePair>> ruleIterator = rules.iterator();
        ArrayList<LinkedList<Styling>> bases = style.getBases();

        for ( LinkedList<Styling> styles : bases ) {
            boolean raster = false;
            for ( Styling s : styles ) {
                if ( s instanceof RasterStyling ) {
                    items.add( new RasterLegendItem( (RasterStyling) s, g ) );
                    raster = true;
                }
            }
            if ( !raster ) {
                LegendItem item = new StandardLegendItem( styles, ruleIterator.next().first, types.next(),
                                                          titles.next(), renderer, textRenderer );
                items.add( item );
            }
        }

        return items;
    }

    public List<LegendItem> prepareLegend( Style style, Graphics2D g, int width, int height ) {
        Pair<Integer, Integer> p = getLegendSize( style );
        Envelope box = geofac.createEnvelope( 0, 0, p.first, p.second, null );
        Java2DRenderer renderer = new Java2DRenderer( g, width, height, box );
        Java2DTextRenderer textRenderer = new Java2DTextRenderer( renderer );
        return prepareLegend( style, g, renderer, textRenderer );
    }

    /**
     * @param style
     * @param width
     * @param height
     * @param g
     */
    public void paintLegend( Style style, int width, int height, Graphics2D g ) {
        List<LegendItem> items = prepareLegend( style, g, width, height );
        int rowHeight = 2 * opts.spacing + opts.baseHeight;
        int pos = getLegendSize( style ).second;

        for ( LegendItem item : items ) {
            item.paint( pos, opts );
            pos -= rowHeight * item.getHeight();
        }

        g.dispose();
    }

    public Pair<Integer, Integer> getLegendSize( Style style ) {
        Pair<Integer, Integer> res = new Pair<Integer, Integer>( 2 * opts.spacing + opts.baseWidth, 0 );

        for ( LegendItem item : prepareLegend( style, null, null, null ) ) {
            res.second += item.getHeight() * ( 2 * opts.spacing + opts.baseHeight );
            res.first = max( res.first, item.getMaxWidth( opts ) );
        }

        return res;
    }

}
