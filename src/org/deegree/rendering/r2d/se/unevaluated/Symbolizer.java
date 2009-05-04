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

import org.deegree.feature.Feature;
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
    private HashMap<String, T> cache = new HashMap<String, T>();

    private Continuation<T> next;

    /**
     * @param evaluated
     */
    public Symbolizer( T evaluated ) {
        this.evaluated = evaluated;
    }

    /**
     * @param base
     * @param next
     */
    public Symbolizer( T base, Continuation<T> next ) {
        this.base = base;
        this.next = next;
    }

    /**
     * @param f
     * @return an appropriate PointStyling
     */
    public T evaluate( Feature f ) {
        if ( evaluated != null ) {
            return evaluated;
        }

        if ( f == null ) {
            return base.copy();
        }

        String id = f.getId();
        if ( cache.containsKey( id ) ) {
            return cache.get( id );
        }

        T evald = base.copy();
        if ( next == null ) {
            LOG.warn( "Something wrong with SE/SLD parsing. No continuation found, and no evaluated style." );
            return evald;
        }

        next.evaluate( evald, f );
        cache.put( id, evald );

        return evald;
    }

}
