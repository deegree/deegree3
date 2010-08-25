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
package org.deegree.protocol.wps.client.input;

import org.deegree.commons.tom.ows.CodeType;

/**
 * {@link ExecutionInput} that contains a bounding box value with optional information on the coordinate reference
 * system.
 * 
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class BBoxInput extends ExecutionInput {

    private final double[] lower;

    private final double[] upper;

    private final String crs;

    private final int dim;

    /**
     * Creates a new {@link BBoxInput} instance.
     * 
     * @param id
     *            input parameter identifier, must not be <code>null</code>
     * @param lower
     *            coordinates of the lower corner, must not be <code>null</code>
     * @param upper
     *            coordinates of the upper corner, must not be <code>null</code> and have the same length as the lower
     *            array
     * @param crs
     *            identifier of the coordinate reference system, can be <code>null</code> (unspecified)
     */
    public BBoxInput( CodeType id, double[] lower, double[] upper, String crs ) {
        super( id );
        this.lower = lower;
        this.upper = upper;
        this.dim = lower.length;
        this.crs = crs;
    }

    /**
     * Returns the coordinates of the lower corner.
     * 
     * @return the coordinates of the lower corner, never <code>null</code>
     */
    public double[] getLower() {
        return lower;
    }

    /**
     * Returns the coordinates of the upper corner.
     * 
     * @return the coordinates of the upper corner, never <code>null</code>
     */
    public double[] getUpper() {
        return upper;
    }

    /**
     * Returns the dimension (number of coordinates of lower/upper corner).
     * 
     * @return coordinate dimension
     */
    public int getDimension() {
        return dim;
    }

    /**
     * Returns the coordinate system identifier.
     * 
     * @return coordinate system identifier, or <code>null</code> if unspecified
     */
    public String getCrs() {
        return crs;
    }
}
