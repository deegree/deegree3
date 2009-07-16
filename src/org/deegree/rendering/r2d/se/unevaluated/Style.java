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

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.deegree.commons.utils.Pair;
import org.deegree.feature.Feature;
import org.deegree.geometry.Geometry;
import org.deegree.rendering.r2d.styling.Styling;
import org.deegree.rendering.r2d.styling.TextStyling;

/**
 * <code>Style</code>
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class Style {

    private LinkedList<Continuation<LinkedList<Symbolizer<?>>>> rules = new LinkedList<Continuation<LinkedList<Symbolizer<?>>>>();

    private HashMap<Symbolizer<TextStyling>, Continuation<StringBuffer>> labels = new HashMap<Symbolizer<TextStyling>, Continuation<StringBuffer>>();

    private LinkedList<String> text = new LinkedList<String>();

    /**
     * @param rules
     * @param labels
     */
    public Style( Collection<Continuation<LinkedList<Symbolizer<?>>>> rules,
                  Map<Symbolizer<TextStyling>, Continuation<StringBuffer>> labels ) {
        this.rules.addAll( rules );
        this.labels.putAll( labels );
    }

    /**
     * @param symbolizer
     * @param label
     */
    public Style( Symbolizer<?> symbolizer, Continuation<StringBuffer> label ) {
        rules.add( new InsertContinuation<LinkedList<Symbolizer<?>>, Symbolizer<?>>( symbolizer ) );
        labels.put( (Symbolizer) symbolizer, label );
    }

    /**
     * @param f
     * @return a pair suitable for rendering
     */
    public LinkedList<Pair<Styling, Geometry>> evaluate( Feature f ) {
        StringBuffer sb = new StringBuffer();
        LinkedList<Object> res = new LinkedList<Object>();
        for ( Continuation<LinkedList<Symbolizer<?>>> rule : rules ) {
            LinkedList<Symbolizer<?>> list = new LinkedList<Symbolizer<?>>();
            rule.evaluate( list, f );
            for ( Symbolizer<?> s : list ) {
                res.add( s.evaluate( f ) );
                if ( labels.containsKey( s ) ) {
                    sb.setLength( 0 );
                    labels.get( s ).evaluate( sb, f );
                    text.add( sb.toString() );
                }
            }
        }
        return (LinkedList) res;
    }

    /**
     * When evaluate is called, and a label continuation corresponds to a symbolizer which is evaluated, then the String
     * of the label continuation is put in a queue. This method gives you the head of it.
     * 
     * @return the next label
     */
    public String getNextText() {
        System.out.println( text.size() );
        return text.poll();
    }

    class InsertContinuation<T extends Collection<U>, U> extends Continuation<T> {
        U value;

        InsertContinuation( U value ) {
            this.value = value;
        }

        @Override
        public void updateStep( T base, Feature f ) {
            base.add( value );
        }
    }

}
