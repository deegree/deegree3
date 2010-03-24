//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/base/trunk/resources/eclipse/files_template.xml $
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

package org.deegree.rendering.r3d.multiresolution.crit;

import org.deegree.rendering.r3d.multiresolution.Arc;
import org.deegree.rendering.r3d.multiresolution.MultiresolutionMesh;

/**
 * {@link LODCriterion} that requests the smallest LOD in a {@link MultiresolutionMesh} with an approximation error that
 * does not exceed a given bound.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: schneider $
 *
 * @version $Revision: $, $Date: $
 */
public class MaxError implements LODCriterion {

    private float c;

    /**
     * Creates a new {@link MaxError} instance.
     *
     * @param maxTolerableError
     *            the maximum tolerable error for the LOD (in world units)
     */
    public MaxError( float maxTolerableError ) {
        this.c = maxTolerableError;
    }

    /**
     * Returns true, iff the geometric error associated with the arc is greater than the maximum tolerable error.
     *
     * @param arc
     *            arc to be checked
     * @return true, iff the arc's geometric error is greater than the maximum tolerable error
     */
    @Override
    public boolean needsRefinement( Arc arc ) {
        return arc.geometricError > c;
    }
}
