//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/deegree3/commons/trunk/src/org/deegree/rendering/r3d/QualityModel.java $
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
package org.deegree.model.multiresolution.crit;

import org.deegree.model.multiresolution.Arc;
import org.deegree.model.multiresolution.MultiresolutionMesh;
import org.deegree.model.multiresolution.SelectiveRefinement;
import org.deegree.model.multiresolution.SpatialSelection;

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
