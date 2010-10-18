//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/deegree3/commons/trunk/src/org/deegree/rendering/r3d/QualityModel.java $
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
import org.deegree.rendering.r3d.multiresolution.SelectiveRefinement;
import org.deegree.rendering.r3d.multiresolution.SpatialSelection;

/**
 * Interface for LOD criteria, i.e. functions that determine whether {@link Arc}s in the DAG have to be "applied" (the
 * arcs correspond to mesh refinements) during <i>selective refinement</i> / <i>spatial selection</i>.
 *
 * @see MultiresolutionMesh
 * @see SelectiveRefinement
 * @see SpatialSelection
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 *
 * @version $Revision: 310 $
 */
public interface LODCriterion {

    /**
     * Static instance of a {@link Coarsest} criterion, use this to extract the coarsest LOD contained in a
     * {@link MultiresolutionMesh}.
     */
    public static final LODCriterion COARSEST = new Coarsest();

    /**
     * Static instance of a {@link Finest} criterion, use this to extract the finest LOD contained in a
     * {@link MultiresolutionMesh}.
     */
    public static final LODCriterion FINEST = new Finest();

    /**
     * Checks whether the given {@link Arc} is necessary in order to satisfy this LOD criterion.
     *
     * @param arc
     *            arc to be checked
     * @return true, if the arc is necessary, false otherwise
     */
    public abstract boolean needsRefinement( Arc arc );
}
