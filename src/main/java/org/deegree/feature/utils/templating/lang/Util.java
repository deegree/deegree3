//$HeadURL$
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
package org.deegree.feature.utils.templating.lang;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

import java.util.LinkedList;
import java.util.List;

import org.deegree.commons.tom.primitive.PrimitiveValue;
import org.deegree.feature.Feature;
import org.deegree.feature.property.GenericProperty;
import org.deegree.feature.property.Property;
import org.deegree.feature.types.property.PropertyType;
import org.deegree.geometry.Geometry;

/**
 * <code>Util</code>
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class Util {

    static <T> List<T> getMatchingObjects( T[] os, List<String> patterns, boolean negate, boolean geometries ) {
        if ( !negate && patterns.contains( "*" ) ) {
            return asList( os );
        }
        LinkedList<T> list = new LinkedList<T>();
        for ( T o : os ) {
            // hack to split pseudo multiple properties into actual multiple properties...
            // yes, it's ugly, thanks a bunch for noticing
            List<T> tmp;
            if ( o instanceof Property ) {
                Property p = (Property) o;

                if ( p.getValue() instanceof Geometry && !geometries ) {
                    continue;
                }

                if ( p.getValue() instanceof PrimitiveValue ) {
                    String s = p.getValue().toString();
                    // skip empty string properties completely
                    if ( s.isEmpty() ) {
                        continue;
                    }
                    PropertyType pt = p.getType();
                    // this is some great parsing really, maybe GML 4 does not need multiple properties at all and can
                    // revert to this?
                    if ( s.startsWith( "[" ) && s.endsWith( "]" ) ) {
                        tmp = new LinkedList<T>();
                        StringBuffer sb = new StringBuffer( s.substring( 1, s.length() - 1 ) );
                        StringBuilder next = new StringBuilder();
                        int cnt = 0;
                        while ( sb.length() != 0 ) {
                            if ( sb.length() > 1 && sb.charAt( 0 ) == ']' && sb.charAt( 1 ) == '[' ) {
                                ++cnt;
                                sb.delete( 0, 2 );
                                continue;
                            }
                            if ( cnt == 0 ) {
                                next.append( sb.charAt( 0 ) );
                                sb.delete( 0, 1 );
                                continue;
                            }
                            if ( cnt == 1 ) {
                                tmp.add( (T) new GenericProperty( pt, new PrimitiveValue( next.toString() ) ) );
                                next = new StringBuilder();
                                cnt = 0;
                                continue;
                            }
                            for ( int i = 0; i < cnt / 2; ++i ) {
                                next.append( "][" );
                            }
                            if ( cnt % 2 == 0 ) {
                                cnt = 0;
                                continue;
                            }
                            tmp.add( (T) new GenericProperty( pt, new PrimitiveValue( next.toString() ) ) );
                            next = new StringBuilder();
                            cnt = 0;
                        }
                        // add trailing escaped ][
                        if ( cnt > 0 ) {
                            for ( int i = 0; i < cnt / 2; ++i ) {
                                next.append( "][" );
                            }
                        }
                        // add last piece if not empty
                        if ( next.length() != 0 ) {
                            tmp.add( (T) new GenericProperty( pt, new PrimitiveValue( next.toString() ) ) );
                        }
                    } else {
                        tmp = singletonList( o );
                    }
                } else {
                    tmp = singletonList( o );
                }
            } else {
                tmp = singletonList( o );
            }

            for ( T o2 : tmp ) {
                String nm = o2 instanceof Property ? ( (Property) o2 ).getName().getLocalPart()
                                                     : ( (Feature) o2 ).getName().getLocalPart();
                if ( negate ) {
                    list.add( o2 );
                }
                inner: for ( String p : patterns ) {
                    if ( p.endsWith( "*" ) && nm.startsWith( p.substring( 0, p.length() - 1 ) ) || p.startsWith( "*" )
                         && nm.endsWith( p.substring( 1 ) ) || nm.equals( p ) ) {
                        if ( negate ) {
                            list.removeLast();
                        } else {
                            list.add( o2 );
                        }
                        break inner;
                    }
                }
            }
        }
        return list;
    }

}
