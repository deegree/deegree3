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

package org.deegree.rendering.r2d.se.unevaluated;

import static java.awt.Color.black;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.deegree.commons.utils.Pair;
import org.deegree.commons.utils.Triple;
import org.deegree.feature.Feature;
import org.deegree.filter.MatchableObject;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.multi.MultiCurve;
import org.deegree.geometry.multi.MultiLineString;
import org.deegree.geometry.multi.MultiPoint;
import org.deegree.geometry.multi.MultiPolygon;
import org.deegree.geometry.multi.MultiSurface;
import org.deegree.geometry.primitive.Curve;
import org.deegree.geometry.primitive.Point;
import org.deegree.geometry.primitive.Surface;
import org.deegree.rendering.r2d.styling.LineStyling;
import org.deegree.rendering.r2d.styling.PointStyling;
import org.deegree.rendering.r2d.styling.PolygonStyling;
import org.deegree.rendering.r2d.styling.Styling;
import org.deegree.rendering.r2d.styling.TextStyling;
import org.deegree.rendering.r2d.styling.components.Fill;
import org.deegree.rendering.r2d.styling.components.Stroke;
import org.slf4j.Logger;

/**
 * <code>Style</code>
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class Style {

    private static final Logger LOG = getLogger( Style.class );

    private LinkedList<Continuation<LinkedList<Symbolizer<?>>>> rules = new LinkedList<Continuation<LinkedList<Symbolizer<?>>>>();

    private HashMap<Symbolizer<TextStyling>, Continuation<StringBuffer>> labels = new HashMap<Symbolizer<TextStyling>, Continuation<StringBuffer>>();

    private String name;

    private boolean useDefault;

    private PointStyling defaultPointStyle;

    private LineStyling defaultLineStyle;

    private PolygonStyling defaultPolygonStyle;

    /**
     * @param rules
     * @param labels
     * @param name
     */
    public Style( Collection<Continuation<LinkedList<Symbolizer<?>>>> rules,
                  Map<Symbolizer<TextStyling>, Continuation<StringBuffer>> labels, String name ) {
        this.rules.addAll( rules );
        this.labels.putAll( labels );
        this.name = name;
    }

    /**
     * @param symbolizer
     * @param label
     * @param name
     */
    public Style( Symbolizer<?> symbolizer, Continuation<StringBuffer> label, String name ) {
        rules.add( new InsertContinuation<LinkedList<Symbolizer<?>>, Symbolizer<?>>( symbolizer ) );
        if ( label != null ) {
            labels.put( (Symbolizer) symbolizer, label );
        }
        this.name = name;
    }

    /**
     * Uses first geometry and default style.
     */
    public Style() {
        useDefault = true;
        defaultPointStyle = new PointStyling();
        defaultLineStyle = new LineStyling();
        defaultPolygonStyle = new PolygonStyling();

        defaultPolygonStyle.fill = new Fill();
        defaultPolygonStyle.stroke = new Stroke();
        defaultPolygonStyle.stroke.color = black;
    }

    /**
     * @param f
     * @return a pair suitable for rendering
     */
    public LinkedList<Triple<Styling, Geometry, String>> evaluate( Feature f ) {
        if ( useDefault ) {
            LinkedList<Triple<Styling, Geometry, String>> list = new LinkedList<Triple<Styling, Geometry, String>>();

            Geometry geom = f.getGeometryProperties()[0].getValue();
            if ( geom instanceof Point || geom instanceof MultiPoint ) {
                list.add( new Triple<Styling, Geometry, String>( defaultPointStyle, geom, null ) );
            } else if ( geom instanceof Curve || geom instanceof MultiCurve || geom instanceof MultiLineString ) {
                list.add( new Triple<Styling, Geometry, String>( defaultLineStyle, geom, null ) );
            } else if ( geom instanceof Surface || geom instanceof MultiSurface || geom instanceof MultiPolygon ) {
                list.add( new Triple<Styling, Geometry, String>( defaultPolygonStyle, geom, null ) );
            } else {
                LOG.error( "Geometries of type '{}' are not supported/known. Please report!", geom.getClass() );
            }

            return list;
        }
        StringBuffer sb = new StringBuffer();
        LinkedList<Object> res = new LinkedList<Object>();

        LinkedList<Symbolizer<?>> list = new LinkedList<Symbolizer<?>>();
        for ( Continuation<LinkedList<Symbolizer<?>>> rule : rules ) {
            rule.evaluate( list, f );
        }

        String text = null;
        for ( Symbolizer<?> s : list ) {
            Pair<?, ?> p = s.evaluate( f );

            if ( labels.containsKey( s ) ) {
                sb.setLength( 0 );
                labels.get( s ).evaluate( sb, f );
                text = sb.toString();
            }
            res.add( new Triple<Object, Object, String>( p.first, p.second, text ) );
        }

        return (LinkedList) res;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name
     */
    public void setName( String name ) {
        this.name = name;
    }

    class InsertContinuation<T extends Collection<U>, U> extends Continuation<T> {
        U value;

        InsertContinuation( U value ) {
            this.value = value;
        }

        @Override
        public void updateStep( T base, MatchableObject f ) {
            base.add( value );
        }
    }

}
