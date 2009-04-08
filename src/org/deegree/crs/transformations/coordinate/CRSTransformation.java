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

package org.deegree.crs.transformations.coordinate;

import org.deegree.crs.CRSIdentifiable;
import org.deegree.crs.coordinatesystems.CoordinateSystem;
import org.deegree.crs.transformations.Transformation;

/**
 * The change of coordinates from one CRS to another CRS based on different datum is 'currently' only possible via a
 * coordinate <code>Transformation</code>.
 * <p>
 * The transformation parameters could only be derived empirically by a set of points common to both coordinate
 * reference systems it means by identical points. Choice, allocation, number and the quality of coordinates of the
 * points affect extensive the results and the accuracy. Therefore different realizations for transformations from one
 * datum to another exist.
 * </p>
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * 
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */

public abstract class CRSTransformation extends Transformation {

    /**
     * @param sourceCRS
     * @param targetCRS
     * @param id
     *            an identifiable instance containing information about this transformation
     */
    public CRSTransformation( CoordinateSystem sourceCRS, CoordinateSystem targetCRS, CRSIdentifiable id ) {
        super( sourceCRS, targetCRS, id );
    }
}
