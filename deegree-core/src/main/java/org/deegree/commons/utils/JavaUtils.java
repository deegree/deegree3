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

package org.deegree.commons.utils;

import static java.lang.Integer.toHexString;
import static java.lang.reflect.Modifier.STATIC;
import static org.slf4j.LoggerFactory.getLogger;

import java.awt.Color;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import org.slf4j.Logger;

/**
 * <code>JavaUtils</code>
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class JavaUtils {

    private static final Logger LOG = getLogger( JavaUtils.class );

    private static void generateToString( Object o, HashSet<Object> visited, StringBuilder sb ) {
        if ( o == null ) {
            return;
        }

        if ( visited.contains( o ) ) {
            sb.append( o.getClass().getSimpleName() ).append( "[recursive instance]" );
            return;
        }

        Class<?> c = o.getClass();
        sb.append( c.getSimpleName() ).append( " [" );
        boolean first = true;
        do {
            for ( Field f : c.getDeclaredFields() ) {
                Class<?> t = f.getType();
                try {
                    if ( ( f.getModifiers() & STATIC ) != 0 ) {
                        continue;
                    }
                    f.setAccessible( true );
                    Object instance = f.get( o );
                    if ( instance instanceof double[] ) {
                        if ( first ) {
                            first = false;
                        } else {
                            sb.append( ", " );
                        }
                        sb.append( f.getName() ).append( ": " ).append( Arrays.toString( (double[]) instance ) );
                    } else if ( t.isPrimitive() || t.isEnum() || instance instanceof Collection<?>
                                || instance instanceof Font || instance instanceof BufferedImage ) {
                        if ( first ) {
                            first = false;
                        } else {
                            sb.append( ", " );
                        }
                        sb.append( f.getName() ).append( ": " ).append( instance );
                    } else if ( instance instanceof Color ) {
                        if ( first ) {
                            first = false;
                        } else {
                            sb.append( ", " );
                        }
                        sb.append( f.getName() ).append( ": #" ).append( toHexString( ( (Color) instance ).getRGB() ) );
                    } else if ( instance instanceof CharSequence ) {
                        if ( first ) {
                            first = false;
                        } else {
                            sb.append( ", " );
                        }
                        sb.append( f.getName() ).append( ": " ).append( (CharSequence) instance );
                    } else {
                        String s = generateToString( instance );
                        if ( s != null ) {
                            if ( first ) {
                                first = false;
                            } else {
                                sb.append( ", " );
                            }
                            sb.append( f.getName() ).append( ": " ).append( s );
                        }
                    }
                } catch ( IllegalArgumentException e ) {
                    LOG.debug( "Stack trace while trying to output an object: ", e );
                } catch ( IllegalAccessException e ) {
                    LOG.debug( "Stack trace while trying to output an object: ", e );
                }
            }
        } while ( ( c = c.getSuperclass() ) != null );

        sb.append( "]" );
    }

    /**
     * @param o
     * @return a string describing the object's fields' values
     */
    public static String generateToString( Object o ) {
        StringBuilder sb = new StringBuilder();
        generateToString( o, new HashSet<Object>(), sb );
        return sb.toString();
    }

}
