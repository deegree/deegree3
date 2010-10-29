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
package org.deegree.tools.crs.georeferencing.model.datastructures;

/**
 * Base datastructure to cope with the different meaning of JAVA rectangle and deegree envelope definition.
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class GREnvelopeParameterCalculator {

    private final double minX;

    private final double maxY;

    private final double minY;

    private final double maxX;

    private GREnvelopeParameterCalculator( double minX, double maxY, double width, double height ) {
        this.minX = minX;
        this.maxY = maxY;
        this.minY = maxY - height;
        this.maxX = minX + width;
    }

    /**
     * Creates a new instance of <Code>GREnvelopeParameterCalculator</Code>
     * 
     * @param minX
     *            , the minimal point in x-direction (the lower-left corner), not <Code>null</Code>.
     * @param maxY
     *            , the maximal point in y-direction (the lower-left corner), not <Code>null</Code>.
     * @param width
     *            , the width, not <Code>null</Code>.
     * @param height
     *            , the height, not <Code>null</Code>.
     * @return the new calculated min- and maxPoints.
     */
    public static GREnvelopeParameterCalculator newInstance( double minX, double maxY, double width, double height ) {
        return new GREnvelopeParameterCalculator( minX, maxY, width, height );
    }

    public double getMinX() {
        return minX;
    }

    public double getMaxY() {
        return maxY;
    }

    public double getMinY() {
        return minY;
    }

    public double getMaxX() {
        return maxX;
    }

}
