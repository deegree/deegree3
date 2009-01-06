//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------
 This file is part of deegree.
 Copyright (C) 2001-2009 by:
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

package org.deegree.model.styling.components;

import java.awt.Color;
import java.util.Arrays;

/**
 * <code>Stroke</code>
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class Stroke {

    /**
     * Default is gray (#808080).
     */
    public Color color = new Color( 128, 128, 128, 255 );

    /**
     * Default is 1.
     */
    public double width = 1;

    /**
     * Default is backend specific.
     */
    public LineJoin linejoin;

    /**
     * Default is backend specific.
     */
    public LineCap linecap;

    /**
     * Default is null.
     */
    public double[] dasharray;

    /**
     * Default is 0.
     */
    public double dashoffset;

    /**
     * Default is null.
     */
    public Graphic stroke;

    /**
     * Default is 0.
     */
    public double strokeGap;

    /**
     * Default is 0.
     */
    public double strokeInitialGap;

    /**
     * Default is null.
     */
    public Graphic fill;

    /**
     * <code>LineJoin</code>
     * 
     * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
     * @author last edited by: $Author$
     * 
     * @version $Revision$, $Date$
     */
    public static enum LineJoin {
        /**
         * 
         */
        MITRE,
        /**
         * 
         */
        ROUND,
        /**
         * 
         */
        BEVEL
    }

    /**
     * <code>LineCap</code>
     * 
     * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
     * @author last edited by: $Author$
     * 
     * @version $Revision$, $Date$
     */
    public static enum LineCap {
        /**
         * 
         */
        BUTT,
        /**
         * 
         */
        ROUND,
        /**
         * 
         */
        SQUARE
    }

    @Override
    public String toString() {
        return "Stroke [color: "
               + color
               + ( linejoin == null ? "" : ( ", linejoin: " + linejoin ) )
               + ( linecap == null ? "" : ( ", linecap: " + linecap ) )
               + ", width: "
               + width
               + ( dasharray == null ? ""
                                    : ( ", dashoffset: " + dashoffset + ", dasharray: " + Arrays.toString( dasharray ) ) )
               + ( stroke == null ? ""
                                 : ( stroke + ", stroke-gap: " + strokeGap + ", stroke-initial-gap" + strokeInitialGap ) )
               + ( fill == null ? "" : fill ) + "]";
    }

}
