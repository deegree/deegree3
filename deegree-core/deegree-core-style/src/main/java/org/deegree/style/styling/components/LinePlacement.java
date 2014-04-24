//$HeadURL: svn+ssh://aschmitz@deegree.wald.intevation.de/deegree/deegree3/trunk/deegree-core/deegree-core-rendering-2d/src/main/java/org/deegree/rendering/r2d/styling/components/LinePlacement.java $
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

package org.deegree.style.styling.components;

import static org.deegree.commons.utils.JavaUtils.generateToString;

import org.deegree.style.styling.Copyable;

/**
 * <code>LinePlacement</code>
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author: aschmitz $
 * 
 * @version $Revision: 21443 $, $Date: 2009-12-15 09:25:22 +0100 (Tue, 15 Dec 2009) $
 */
public class LinePlacement implements Copyable<LinePlacement> {

    /**
     * Default is 0.
     */
    public double perpendicularOffset;

    /** Default is Standard. */
    public PerpendicularOffsetType perpendicularOffsetType = new PerpendicularOffsetType();

    /**
     * Default is false.
     */
    public boolean repeat;

    /**
     * Default is 0.
     */
    public double initialGap;

    /**
     * Default is 0.
     */
    public double gap;

    /**
     * Default is true.
     */
    public boolean isAligned = true;

    /**
     * Default is false.
     */
    public boolean generalizeLine;

    /**
     * Default is false. deegree specific extension.
     */
    public boolean preventUpsideDown = false;
    
    /**
     * Default is false. deegree specific extension.
     */
    public boolean center = false;
    
    /**
     * Default is true. deegree specific extension.
     */
    public boolean wordWise = true;

    @Override
    public LinePlacement copy() {
        LinePlacement copy = new LinePlacement();
        copy.perpendicularOffset = perpendicularOffset;
        copy.perpendicularOffsetType = perpendicularOffsetType.copy();
        copy.repeat = repeat;
        copy.initialGap = initialGap;
        copy.gap = gap;
        copy.isAligned = isAligned;
        copy.generalizeLine = generalizeLine;
        copy.preventUpsideDown = preventUpsideDown;
        return copy;
    }

    @Override
    public String toString() {
        return generateToString( this );
    }

}
