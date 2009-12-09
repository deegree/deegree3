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
import static java.lang.Double.MAX_VALUE;
import static java.lang.Double.MIN_VALUE;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import javax.xml.namespace.QName;

import org.deegree.commons.utils.DoublePair;
import org.deegree.commons.utils.Pair;
import org.deegree.commons.utils.Triple;
import org.deegree.feature.Feature;
import org.deegree.feature.Property;
import org.deegree.filter.MatchableObject;
import org.deegree.filter.function.geometry.IsCurve;
import org.deegree.filter.function.geometry.IsPoint;
import org.deegree.filter.function.geometry.IsSurface;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.multi.MultiCurve;
import org.deegree.geometry.multi.MultiLineString;
import org.deegree.geometry.multi.MultiPoint;
import org.deegree.geometry.multi.MultiPolygon;
import org.deegree.geometry.multi.MultiSurface;
import org.deegree.geometry.primitive.Curve;
import org.deegree.geometry.primitive.LineString;
import org.deegree.geometry.primitive.Point;
import org.deegree.geometry.primitive.Polygon;
import org.deegree.geometry.primitive.Surface;
import org.deegree.rendering.r2d.se.parser.SymbologyParser.FilterContinuation;
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

    private LinkedList<Pair<Continuation<LinkedList<Symbolizer<?>>>, DoublePair>> rules = new LinkedList<Pair<Continuation<LinkedList<Symbolizer<?>>>, DoublePair>>();

    private HashMap<Symbolizer<TextStyling>, Continuation<StringBuffer>> labels = new HashMap<Symbolizer<TextStyling>, Continuation<StringBuffer>>();

    private String name;

    private boolean useDefault;

    private PointStyling defaultPointStyle;

    private LineStyling defaultLineStyle;

    private PolygonStyling defaultPolygonStyle;

    private QName featureType;

    /**
     * @param rules
     * @param labels
     * @param name
     * @param featureTypeName
     */
    public Style( Collection<Pair<Continuation<LinkedList<Symbolizer<?>>>, DoublePair>> rules,
                  Map<Symbolizer<TextStyling>, Continuation<StringBuffer>> labels, String name, QName featureTypeName ) {
        this.rules.addAll( rules );
        this.labels.putAll( labels );
        this.name = name;
        featureType = featureTypeName;
    }

    /**
     * @param symbolizer
     * @param label
     * @param name
     */
    public Style( Symbolizer<?> symbolizer, Continuation<StringBuffer> label, String name ) {
        InsertContinuation<LinkedList<Symbolizer<?>>, Symbolizer<?>> contn = new InsertContinuation<LinkedList<Symbolizer<?>>, Symbolizer<?>>(
                                                                                                                                               symbolizer );
        rules.add( new Pair<Continuation<LinkedList<Symbolizer<?>>>, DoublePair>( contn, new DoublePair( MIN_VALUE,
                                                                                                         MAX_VALUE ) ) );
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
     * @param scale
     * @return a filtered list of symbolizers
     */
    public Style filter( double scale ) {
        if ( useDefault ) {
            return this;
        }

        LinkedList<Pair<Continuation<LinkedList<Symbolizer<?>>>, DoublePair>> rules = new LinkedList<Pair<Continuation<LinkedList<Symbolizer<?>>>, DoublePair>>();
        for ( Pair<Continuation<LinkedList<Symbolizer<?>>>, DoublePair> p : this.rules ) {
            if ( p.second.first <= scale && p.second.second >= scale ) {
                rules.add( p );
            } else {
                LOG.debug( "Not using rule because of scale constraints, in style with name '{}'.", name );
            }
        }
        return new Style( rules, labels, name, featureType );
    }

    /**
     * @param f
     * @return a pair suitable for rendering
     */
    public LinkedList<Triple<Styling, Geometry, String>> evaluate( Feature f ) {
        if ( useDefault ) {
            LinkedList<Triple<Styling, Geometry, String>> list = new LinkedList<Triple<Styling, Geometry, String>>();

            Property<Geometry>[] geoms = f.getGeometryProperties();
            if ( geoms != null ) {
                for ( Property<Geometry> p : geoms ) {
                    Geometry geom = p.getValue();
                    if ( geom instanceof Point || geom instanceof MultiPoint ) {
                        list.add( new Triple<Styling, Geometry, String>( defaultPointStyle, geom, null ) );
                    } else if ( geom instanceof Curve || geom instanceof MultiCurve || geom instanceof MultiLineString ) {
                        list.add( new Triple<Styling, Geometry, String>( defaultLineStyle, geom, null ) );
                    } else if ( geom instanceof Surface || geom instanceof MultiSurface || geom instanceof MultiPolygon ) {
                        list.add( new Triple<Styling, Geometry, String>( defaultPolygonStyle, geom, null ) );
                    } else {
                        LOG.error( "Geometries of type '{}' are not supported/known. Please report!", geom.getClass() );
                    }
                }
            }

            return list;
        }

        LinkedList<Object> res = new LinkedList<Object>();
        if ( featureType != null && !f.getType().getName().equals( featureType ) ) {
            LOG.debug( "Not using style because feature type constraint does not match." );
            return (LinkedList) res;
        }

        StringBuffer sb = new StringBuffer();
        LinkedList<Symbolizer<?>> list = new LinkedList<Symbolizer<?>>();
        for ( Pair<Continuation<LinkedList<Symbolizer<?>>>, DoublePair> pair : rules ) {
            pair.first.evaluate( list, f );
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

    /**
     * @return the name of the feature type (or null if not constrained)
     */
    public QName getFeatureType() {
        return featureType;
    }

    /**
     * @return the base stylings for all symbolizers sorted by rules
     */
    public LinkedList<LinkedList<Styling>> getBases() {
        LinkedList<LinkedList<Styling>> list = new LinkedList<LinkedList<Styling>>();
        for ( Pair<Continuation<LinkedList<Symbolizer<?>>>, DoublePair> rule : rules ) {
            LinkedList<Symbolizer<?>> base = new LinkedList<Symbolizer<?>>();
            rule.first.evaluate( base, null );
            LinkedList<Styling> stylings = new LinkedList<Styling>();
            for ( Symbolizer<?> s : base ) {
                stylings.add( (Styling) s.getBase() );
            }
            if ( !stylings.isEmpty() ) {
                list.add( stylings );
            }
        }
        return list;
    }

    /**
     * @return Polygon.class, if the IsSurface function is used, Point.class for IsPoint and LineString.class for
     *         IsCurve
     */
    public LinkedList<Class<?>> getRuleTypes() {
        LinkedList<Class<?>> list = new LinkedList<Class<?>>();

        for ( Pair<?, ?> rule : rules ) {
            if ( rule.first instanceof FilterContinuation ) {
                FilterContinuation cont = (FilterContinuation) rule.first;
                if ( cont.filter instanceof IsSurface ) {
                    list.add( Polygon.class );
                } else if ( cont.filter instanceof IsCurve ) {
                    list.add( LineString.class );
                } else if ( cont.filter instanceof IsPoint ) {
                    list.add( Point.class );
                } else {
                    list.add( Polygon.class );
                }
            } else {
                list.add( Polygon.class );
            }
        }

        return list;
    }

    /**
     * @return "", if no title was set
     */
    public LinkedList<String> getRuleTitles() {
        LinkedList<String> list = new LinkedList<String>();

        for ( Pair<?, ?> rule : rules ) {
            if ( rule.first instanceof FilterContinuation ) {
                FilterContinuation cont = (FilterContinuation) rule.first;
                list.add( cont.common.title );
            } else {
                list.add( "" );
            }
        }

        return list;
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
