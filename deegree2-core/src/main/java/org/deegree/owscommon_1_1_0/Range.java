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

package org.deegree.owscommon_1_1_0;

/**
 * <code>Range</code> encapsulation of an ows 1.1.0 Range, which can hold a max, min, opening and
 * closure (e.g '(' or '[' ) value.
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 *
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 *
 */

public class Range {

    private final String minimumValue;

    private final String maximumValue;

    private final String spacing;

    private final String rangeClosure;

    /**
     * @param minimumValue
     * @param maximumValue
     * @param spacing
     * @param rangeClosure
     */
    public Range( String minimumValue, String maximumValue, String spacing, String rangeClosure ) {
        this.minimumValue = minimumValue;
        // TODO Auto-generated constructor stub
        this.maximumValue = maximumValue;
        this.spacing = spacing;
        this.rangeClosure = rangeClosure;
    }

    /**
     * @return the minimumValue.
     */
    public final String getMinimumValue() {
        return minimumValue;
    }

    /**
     * @return the maximumValue.
     */
    public final String getMaximumValue() {
        return maximumValue;
    }

    /**
     * @return the spacing.
     */
    public final String getSpacing() {
        return spacing;
    }

    /**
     * @return the rangeClosure.
     */
    public final String getRangeClosure() {
        return rangeClosure;
    }

}
