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
package org.deegree.framework.util;

import java.awt.Color;

/**
 * offeres some methods for handling colors
 *
 *
 * @version $Revision$
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version 1.0. $Revision$, $Date$
 *
 * @since 2.0
 */
public class ColorUtils {

    /**
     * returns a random color
     *
     * @param useTransparency
     * @return a random color
     */
    public static Color getRandomColor( boolean useTransparency ) {
        float r = (float) Math.random();
        float g = (float) Math.random();
        float b = (float) Math.random();
        float a = 0;
        if ( useTransparency ) {
            a = (float) Math.random() / 1.5f;
        }
        return new Color( r, g, b, a );
    }

    /**
     * transforms the color of the request from java.awt.Color to the hexadecimal representation as
     * in an OGC conform WMS-GetMap request (e.g. white == "#ffffff").
     * @param prefix to add to the hex
     * @param color to get hex code from
     *
     * @return the color as hexadecimal representation
     */
    public static String toHexCode( String prefix, Color color ) {
        String r = Integer.toHexString( color.getRed() );
        if ( r.length() < 2 )
            r = "0" + r;
        String g = Integer.toHexString( color.getGreen() );
        if ( g.length() < 2 )
            g = "0" + g;
        String b = Integer.toHexString( color.getBlue() );
        if ( b.length() < 2 )
            b = "0" + b;
        return prefix + r + g + b;
    }

}
