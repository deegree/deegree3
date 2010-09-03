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

import static java.awt.Color.BLACK;
import static org.deegree.commons.utils.JavaUtils.generateToString;
import static org.deegree.rendering.r2d.styling.components.Mark.SimpleMark.SQUARE;

import java.awt.Font;
import java.awt.Shape;

import org.deegree.rendering.r2d.styling.Copyable;

/**
 * <code>Mark</code>
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class Mark implements Copyable<Mark> {

    /**
     * Default is SQUARE.
     */
    public SimpleMark wellKnown = SQUARE;

    /**
     * Default is null. If non null, wellKnown is ignored.
     */
    public Font font;

    /**
     * Is used when font is set.
     */
    public int markIndex;

    /**
     * Default is a gray fill.
     */
    public Fill fill = new Fill();

    /**
     * Default is a black stroke.
     */
    public Stroke stroke = new Stroke();

    /** Default is null. */
    public Shape shape = null;

    /**
     *
     */
    public Mark() {
        stroke.color = BLACK;
    }

    public Mark copy() {
        Mark copy = new Mark();
        copy.wellKnown = wellKnown;
        copy.fill = fill.copy();
        copy.stroke = stroke.copy();
        copy.markIndex = markIndex;
        // these two should be safe to just copy the references:
        copy.shape = shape;
        copy.font = font;
        return copy;
    }

    /**
     * <code>SimpleMark</code>
     * 
     * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
     * @author last edited by: $Author$
     * 
     * @version $Revision$, $Date$
     */
    public static enum SimpleMark {
        /** * */
        SQUARE, /** * */
        CIRCLE, /** * */
        TRIANGLE, /** * */
        STAR, /** * */
        CROSS, /** * */
        X
    }

    @Override
    public String toString() {
        return generateToString( this );
    }

}
