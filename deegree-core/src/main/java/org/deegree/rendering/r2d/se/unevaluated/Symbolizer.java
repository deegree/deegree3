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

import static org.slf4j.LoggerFactory.getLogger;

import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.deegree.commons.tom.TypedObjectNode;
import org.deegree.commons.tom.genericxml.GenericXMLElement;
import org.deegree.commons.utils.Pair;
import org.deegree.feature.Feature;
import org.deegree.feature.property.Property;
import org.deegree.filter.Expression;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.XPathEvaluator;
import org.deegree.geometry.Geometry;
import org.deegree.rendering.r2d.styling.Copyable;
import org.slf4j.Logger;

/**
 * <code>Symbolizer</code>
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * @param <T>
 */
public class Symbolizer<T extends Copyable<T>> {

    private static final Logger LOG = getLogger( Symbolizer.class );

    private T evaluated;

    private T base;

    // TODO improve the caching, eg. implement a real cache with a limit etc.
    // NOTE: Using a synchronized map here is strictly necessary (race condition), RB / MS
    private Map<String, T> cache = new ConcurrentHashMap<String, T>();

    private Continuation<T> next;

    private Expression geometry;

    private String name;

    private String file;

    private final int line;

    private final int col;

    /**
     * @param evaluated
     * @param geometry
     * @param name
     * @param file
     * @param line
     * @param col
     */
    public Symbolizer( T evaluated, Expression geometry, String name, String file, int line, int col ) {
        this( null, null, geometry, name, file, line, col );
        this.evaluated = evaluated;
    }

    /**
     * @param base
     * @param next
     * @param geometry
     * @param name
     * @param file
     * @param line
     * @param col
     */
    public Symbolizer( T base, Continuation<T> next, Expression geometry, String name, String file, int line, int col ) {
        if ( geometry == null ) {
            LOG.debug(
                       "In file '{}', line {}, column {}: no geometry property defined, using first geometry property as default.",
                       new Object[] { file, line, col } );
        }
        this.base = base;
        this.next = next;
        this.geometry = geometry;
        this.name = name;
        this.file = file;
        this.line = line;
        this.col = col;
    }

    /**
     * @return the name of the symbolizer
     */
    public String getName() {
        return name;
    }

    /**
     * @return whether the symbolizer is already evaluated
     */
    public boolean isEvaluated() {
        return evaluated != null;
    }

    private Geometry tryGeometry( TypedObjectNode p ) {
        return p instanceof Geometry ? (Geometry) p : null;
    }

    private Geometry tryProperty( TypedObjectNode n ) {
        if ( n instanceof Property ) {
            TypedObjectNode val = ( (Property) n ).getValue();
            if ( val instanceof Geometry ) {
                return (Geometry) val;
            }
            return tryCustomXML( val );
        }
        return null;
    }

    private Geometry tryCustomXML( TypedObjectNode n ) {
        if ( n instanceof GenericXMLElement ) {
            GenericXMLElement elem = (GenericXMLElement) n;
            if ( elem.getChildren().isEmpty() ) {
                LOG.warn( "The geometry expression in file '{}', line {}, column {} evaluated"
                          + " to a custom property with no children.", new Object[] { file, line, col } );
                return null;
            }
            TypedObjectNode maybeGeom = elem.getChildren().get( 0 );
            if ( maybeGeom instanceof Geometry ) {
                return (Geometry) maybeGeom;
            }
            return tryProperty( maybeGeom );
        }
        return null;
    }

    /**
     * @param f
     * @return the styling with the geometries, p.second may be null if no geoms were found
     */
    public Pair<T, LinkedList<Geometry>> evaluate( Feature f, XPathEvaluator<Feature> evaluator ) {
        if ( f == null ) {
            return new Pair<T, LinkedList<Geometry>>( evaluated == null ? base.copy() : evaluated.copy(), null );
        }

        LinkedList<Geometry> geoms = new LinkedList<Geometry>();
        if ( geometry != null ) {
            try {
                TypedObjectNode[] os = geometry.evaluate( f, evaluator );

                if ( os.length == 0 ) {
                    LOG.warn( "The geometry expression in file '{}', line {}, column {} evaluated to nothing.",
                              new Object[] { file, line, col } );
                } else {
                    for ( TypedObjectNode node : os ) {
                        Geometry geom = null;
                        geom = tryGeometry( node );
                        if ( geom == null ) {
                            geom = tryProperty( node );
                        }
                        if ( geom == null ) {
                            geom = tryCustomXML( node );
                        }
                        if ( geom != null ) {
                            geoms.add( geom );
                        } else {
                            LOG.warn( "The geometry expression in file '{}', line {}, column {} "
                                      + "evaluated to something where no geometry"
                                      + " could be found. Actual type was '{}'.", new Object[] { file, line, col,
                                                                                                node.getClass() } );
                        }
                    }
                    if ( geoms.isEmpty() ) {
                        LOG.warn( "The geometry expression in file '{}', line {}, column {} "
                                  + "evaluated to no geometry could be found.", new Object[] { file, line, col } );
                    }
                }
            } catch ( FilterEvaluationException e ) {
                LOG.warn( "Could not evaluate a geometry expression." );
            }
        } else {
            Property[] gs = f.getGeometryProperties();
            if ( gs.length > 0 ) {
                for ( Property p : gs ) {
                    if ( p.getValue() instanceof Geometry ) {
                        geoms.add( (Geometry) p.getValue() );
                    }
                }
            } else {
                LOG.warn( "Style was applied to a feature without a geometry." );
            }
        }

        String id = f.getId();
        if ( id != null && cache.containsKey( id ) ) {
            return new Pair<T, LinkedList<Geometry>>( cache.get( id ), geoms );
        }

        if ( evaluated != null ) {
            Pair<T, LinkedList<Geometry>> pair = new Pair<T, LinkedList<Geometry>>( evaluated, geoms );
            if ( id != null ) {
                cache.put( id, pair.first );
            }
            return pair;
        }

        T evald = base.copy();
        Pair<T, LinkedList<Geometry>> pair = new Pair<T, LinkedList<Geometry>>( evald, geoms );
        if ( next == null ) {
            LOG.warn( "Something wrong with SE/SLD parsing. No continuation found, and no evaluated style." );
            return pair;
        }

        next.evaluate( evald, f, evaluator );
        if ( id != null ) {
            cache.put( id, pair.first );
        }

        return pair;
    }

    /**
     * @return the base object or the evaluated one, if already available
     */
    public T getBase() {
        return evaluated == null ? base : evaluated;
    }
}