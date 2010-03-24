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

package org.deegree.rendering.r2d.styling.components;

import static org.deegree.rendering.r2d.styling.components.Font.Style.NORMAL;

import java.util.LinkedList;
import java.util.List;

import org.deegree.rendering.r2d.styling.Copyable;

/**
 * <code>Font</code>
 *
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class Font implements Copyable<Font> {

    /**
     * Default is empty list.
     */
    public List<String> fontFamily = new LinkedList<String>();

    /**
     * Default is NORMAL.
     */
    public Style fontStyle = NORMAL;

    /**
     * Default is false.
     */
    public boolean bold;

    /**
     * Default is 10.
     */
    public int fontSize = 10;

    /**
     * <code>Style</code>
     *
     * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
     * @author last edited by: $Author$
     *
     * @version $Revision$, $Date$
     */
    public static enum Style {
        /** * */
        OBLIQUE, /** * */
        ITALIC, /** * */
        NORMAL
    }

    public Font copy() {
        Font copy = new Font();
        copy.fontFamily.addAll( fontFamily );
        copy.fontStyle = fontStyle;
        copy.bold = bold;
        copy.fontSize = fontSize;
        return copy;
    }

}
