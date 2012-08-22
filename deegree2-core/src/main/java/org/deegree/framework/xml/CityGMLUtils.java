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
package org.deegree.framework.xml;

import org.deegree.framework.util.StringTools;
import org.w3c.dom.Node;

/**
 *
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class CityGMLUtils {

    /**
     * Returns the color string.
     *
     * @param color
     * @param transparency
     * @return the color string
     */
    public static String getColor( Node color, Node transparency ) {
        StringBuffer clr = new StringBuffer( 10 );

        if ( transparency != null ) {
            String s = XMLTools.getStringValue( transparency );
            float f = Float.parseFloat( s );
            int v = Math.round( f * 255 );
            clr.append( Integer.toHexString( v ) );
        } else {
            clr.append( "e5" );
        }

        if ( color != null ) {
            String s = XMLTools.getStringValue( color );
            float[] tmp = StringTools.toArrayFloat( s, " " );
            for ( int i = 0; i < tmp.length; i++ ) {
                int v = Math.round( tmp[i] * 255 );
                s = Integer.toHexString( v );
                if ( s.length() == 1 ) {
                    s = '0' + s;
                }
                clr.append( s );
            }
        } else {
            clr.append( "ff0000" );
        }

        return clr.toString();
    }
}
