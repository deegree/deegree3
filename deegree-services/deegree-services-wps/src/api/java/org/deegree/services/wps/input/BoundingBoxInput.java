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

package org.deegree.services.wps.input;

import org.deegree.geometry.Envelope;

/**
 * A {@link Process} input parameter with a bounding box value.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 *
 */
public interface BoundingBoxInput extends ProcessletInput {

    /**
     * Returns the lower corner point of the bounding box.
     *
     * @return the lower corner point
     */
    public double[] getLower();

    /**
     * Returns the upper corner point of the bounding box.
     *
     * @return the upper corner point
     */
    public double[] getUpper();

    /**
     * Returns the CRS (coordinate reference system) name of the bounding box.
     *
     * @return the CRS (coordinate reference system) name or null if unspecified
     */
    public String getCRSName();

    /**
     * Returns the bounding box value, it is guaranteed that the SRS (spatial reference system) of the returned
     * {@link Envelope} is supported for this parameter (according to the process description).
     *
     * @return the value
     */
    public Envelope getValue();
}
