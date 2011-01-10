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
package org.deegree.geometry.primitive.segments;

/**
 * Used to define the basis functions of a {@link BSpline}.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author$
 * 
 * @version $Revision: 33706 $, $Date: 2009-08-07 00:49:16 +0200 (Fr, 07 Aug 2009) $
 */
public class Knot {

    private double value;

    private int multiplicity;

    private double weight;

    /**
     * Creates a new {@link Knot} instance.
     * 
     * @param value
     * @param multiplicity
     * @param weight
     */
    public Knot( double value, int multiplicity, double weight ) {
        this.value = value;
        this.multiplicity = multiplicity;
        this.weight = weight;
    }

    /**
     * Returns the knot's value.
     * 
     * @return the knot's value
     */
    public double getValue() {
        return value;
    }

    /**
     * Returns the knot's multiplicity.
     * 
     * @return the knot's multiplicity
     */    
    public int getMultiplicity() {
        return multiplicity;
    }

    /**
     * Returns the knot's weight.
     * 
     * @return the knot's weight
     */    
    public double getWeight() {
        return weight;
    }
}
