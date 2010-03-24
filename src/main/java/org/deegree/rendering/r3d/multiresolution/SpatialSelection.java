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
package org.deegree.rendering.r3d.multiresolution;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.deegree.rendering.r3d.ViewFrustum;
import org.deegree.rendering.r3d.multiresolution.crit.LODCriterion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of <i>spatial selection</i> algorithm for {@link MultiresolutionMesh}.
 * <p>
 * This is a variant of the {@link SelectiveRefinement} algorithm that only extracts a region of an LOD that lies inside
 * a certain region of interest (ROI). Mesh fragments outside the roi are clipped.
 * </p>
 *
 * @see SelectiveRefinement
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$
 */
public class SpatialSelection {

    private static Logger LOG = LoggerFactory.getLogger( SpatialSelection.class );

    // associated multiresolution model
    private MultiresolutionMesh mt;

    // associated LODCriterion
    private LODCriterion crit;

    // associated region of interest
    private ViewFrustum roi;

    // determines the current lod (contains arcs)
    private Set<Arc> lod;

    // bitfield to mark which nodes are above the current cut
    private boolean[] applied;

    private final float zScale;

    /**
     * Creates a new <code>SpatialSelection</code> instance for the given {@link MultiresolutionMesh},
     * {@link LODCriterion} and region of interest.
     *
     * @param mt
     * @param crit
     * @param roi
     * @param zScale
     *            of the dem.
     */
    public SpatialSelection( MultiresolutionMesh mt, LODCriterion crit, ViewFrustum roi, float zScale ) {
        this.mt = mt;
        this.crit = crit;
        this.roi = roi;
        this.zScale = zScale;
    }

    /**
     * Determines the LOD fragment that corresponds to the associated {@link LODCriterion} and region of interest.
     *
     * @return PatchInfo objects of all patches that make up the LOD fragment
     */
    public List<MeshFragment> determineLODFragment() {

        List<MeshFragment> fragments = new ArrayList<MeshFragment>();

        adaptTopDown();

        for ( Arc arc : lod ) {
            for ( int fragmentId = arc.lowestPatch; fragmentId <= arc.highestPatch; fragmentId++ ) {
                MeshFragment fragment = mt.fragments[fragmentId];
                fragments.add( fragment );
            }
        }

        return fragments;
    }

    private void adaptTopDown() {

        long begin = System.currentTimeMillis();

        initializeCut();

        // initialize toDo with all arc ids in the current lod
        List<Arc> toDo = new ArrayList<Arc>( lod );

        while ( !toDo.isEmpty() ) {
            Arc region = toDo.remove( toDo.size() - 1 );
            Node modification = mt.nodes[region.destinationNode];

            // do not move the cut below the drain
            if ( modification.lowestOutgoingArc == -1 ) {
                continue;
            }

            // only process arc if it points to a node below the cut
            if ( !applied[modification.id] ) {
                if ( crit.needsRefinement( region ) ) {
                    int incomingArc = modification.lowestIncomingArc;
                    while ( incomingArc != -1 ) {
                        if ( applied[mt.arcs[incomingArc].sourceNode] ) {
                            lod.remove( mt.arcs[incomingArc] );
                        } else {
                            forceRefinement( mt.arcs[incomingArc], toDo );
                        }
                        incomingArc = mt.arcs[incomingArc].nextArcWithSameDestination;
                    }

                    if ( modification.lowestOutgoingArc > 0 ) {
                        for ( int arcId = modification.lowestOutgoingArc; arcId <= modification.highestOutgoingArc; arcId++ ) {
                            Arc arc = mt.arcs[arcId];
                            if ( arc.interferes( roi, zScale ) ) {
                                lod.add( arc );
                                toDo.add( arc );
                            }
                        }
                    }
                    applied[modification.id] = true;
                }
            }
        }

        long elapsed = System.currentTimeMillis() - begin;
        LOG.debug( "Spatial selection (top-down): " + elapsed + " ms" );
    }

    /**
     * Initializes the cut, so it is just below the root node.
     */
    private void initializeCut() {
        lod = new HashSet<Arc>();
        applied = new boolean[mt.nodes.length];
        Node root = mt.nodes[0];

        int[] outgoingArcs = new int[root.highestOutgoingArc - root.lowestOutgoingArc + 1];
        for ( int i = 0; i < outgoingArcs.length; i++ ) {
            Arc arc = mt.arcs[i + root.lowestOutgoingArc];
            if ( arc.interferes( roi, zScale ) ) {
                lod.add( arc );
            }
        }
        applied[0] = true;
    }

    private void forceRefinement( Arc region, List<Arc> toDo ) {

        Node modification = mt.nodes[region.sourceNode];

        int incomingArc = modification.lowestIncomingArc;
        while ( incomingArc != -1 ) {
            if ( applied[mt.arcs[incomingArc].sourceNode] ) {
                lod.remove( mt.arcs[incomingArc] );
            } else {
                forceRefinement( mt.arcs[incomingArc], toDo );
            }
            incomingArc = mt.arcs[incomingArc].nextArcWithSameDestination;
        }

        for ( int arcId = modification.lowestOutgoingArc; arcId <= modification.highestOutgoingArc; arcId++ ) {
            if ( arcId != region.id ) {
                Arc arc2 = mt.arcs[arcId];
                if ( arc2.interferes( roi, zScale ) ) {
                    lod.add( arc2 );
                    toDo.add( arc2 );
                }
            }
        }
        applied[modification.id] = true;
    }
}
