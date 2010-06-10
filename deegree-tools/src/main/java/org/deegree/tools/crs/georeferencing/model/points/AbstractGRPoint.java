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
package org.deegree.tools.crs.georeferencing.model.points;

import javax.vecmath.Point2d;

/**
 * Abstract class for special points in the georeferencing tool.
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public abstract class AbstractGRPoint extends Point2d {

    /**
     * Creates a new instance of <Code>AbstractPoint</Code>.
     * 
     * @param x
     *            the X-axis value of the point
     * @param y
     *            the Y-axis value of the point
     */
    public AbstractGRPoint( double x, double y ) {
        super( x, y );
    }

    /**
     * 
     * Enumeration for the specified types that are used in this component.
     * 
     * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
     * @author last edited by: $Author$
     * 
     * @version $Revision$, $Date$
     */
    public enum PointType {

        /**
         * points that are identified as footprint
         */
        FootprintPoint,

        /**
         * points that are identified as georeference
         */
        GeoreferencedPoint
    }

    /**
     * 
     * @return the type of the point
     */
    public abstract PointType getPointType();

}
