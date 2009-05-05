//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------
 This file is part of deegree.
 Copyright (C) 2001-2008 by:
 Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 Lesser General Public License for more details.
 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 Contact:

 Andreas Poth
 lat/lon GmbH
 Aennchenstr. 19
 53177 Bonn
 Germany
 E-Mail: poth@lat-lon.de

 Prof. Dr. Klaus Greve
 Department of Geography
 University of Bonn
 Meckenheimer Allee 166
 53115 Bonn
 Germany
 E-Mail: greve@giub.uni-bonn.de
 ---------------------------------------------------------------------------*/

package org.deegree.rendering.r2d.se.unevaluated;

import static org.slf4j.LoggerFactory.getLogger;

import java.util.HashMap;

import javax.xml.namespace.QName;

import org.deegree.commons.utils.Pair;
import org.deegree.feature.Feature;
import org.deegree.feature.Property;
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
    private HashMap<String, Pair<T, Geometry>> cache = new HashMap<String, Pair<T, Geometry>>();

    private Continuation<T> next;

    private QName geometry;

    /**
     * @param evaluated
     * @param geometry
     */
    public Symbolizer( T evaluated, QName geometry ) {
        this.evaluated = evaluated;
        this.geometry = geometry == null ? new QName( "geometry" ) : geometry;
    }

    /**
     * @param base
     * @param next
     * @param geometry
     */
    public Symbolizer( T base, Continuation<T> next, QName geometry ) {
        this.base = base;
        this.next = next;
        this.geometry = geometry == null ? new QName( "geometry" ) : geometry;
    }

    /**
     * @param f
     * @return an appropriate PointStyling
     */
    public Pair<T, Geometry> evaluate( Feature f ) {
        if ( f == null ) {
            return new Pair<T, Geometry>( evaluated == null ? base.copy() : evaluated.copy(), null );
        }

        Geometry geom = null;

        for ( Property<Geometry> p : f.getGeometryProperties() ) {
            if ( p.getName().equals( geometry ) ) {
                geom = p.getValue();
            }
        }
        String id = f.getId();
        if ( cache.containsKey( id ) ) {
            return cache.get( id );
        }

        if ( evaluated != null ) {
            Pair<T, Geometry> pair = new Pair<T, Geometry>( evaluated, geom );
            cache.put( id, pair );
            return pair;
        }

        T evald = base.copy();
        Pair<T, Geometry> pair = new Pair<T, Geometry>( evald, geom );
        if ( next == null ) {
            LOG.warn( "Something wrong with SE/SLD parsing. No continuation found, and no evaluated style." );
            return pair;
        }

        next.evaluate( evald, f );
        cache.put( id, pair );

        return pair;
    }

}
