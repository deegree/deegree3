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
package org.deegree.rendering.r3d.multiresolution;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.List;

import org.deegree.rendering.r3d.ViewFrustum;
import org.deegree.rendering.r3d.multiresolution.crit.LODCriterion;
import org.deegree.rendering.r3d.multiresolution.io.MeshFragmentDataReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of a fragment based multiresolution model for massive triangle meshes (e.g. large terrain surfaces,
 * but also suitable for free-form surfaces) based on the <a
 * href="http://vcg.isti.cnr.it/Publications/2005/CGGMPS05/BatchedMT_Vis05.pdf">Batched Multi-Triangulation</a> as
 * described by Cignoni et al.
 * <p>
 * This class encapsulates the augmented multiresolution-DAG:
 * <ul>
 * <li>nodes: each node corresponds to a modification (except the root and drain nodes)</li>
 * <li>arcs: each arc describes the refinement of a certain region</li>
 * <li>fragments: each fragment describes a certain region of the geometry at a certain LOD</li>
 * </ul>
 * </p>
 * 
 * @see SelectiveRefinement
 * @see SpatialSelection
 * @see LODCriterion
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$
 */
public class MultiresolutionMesh {

    private static Logger LOG = LoggerFactory.getLogger( MultiresolutionMesh.class );

    /** Default name for the binary file that contains the mesh fragments. */
    public static final String FRAGMENTS_FILE_NAME = "fragments.bin";

    /** Default name for the binary index file that contains the multiresolution DAG. */
    public static final String INDEX_FILE_NAME = "mrindex.bin";

    /** Contains all {@link Node}s of the DAG, the array index corresponds to its id. */
    public Node[] nodes;

    /** Contains all {@link Arc}s of the DAG, the array index corresponds to its id. */
    public Arc[] arcs;

    /** Contains all {@link MeshFragment}s of the DAG, the array index corresponds to its id. */
    public MeshFragment[] fragments;

    private static final int INDEX_HEADER_SIZE = 4 * 4;

    /**
     * Creates a new {@link MultiresolutionMesh} by reading the fragment ({@link #FRAGMENTS_FILE_NAME}) and index (
     * {@link #INDEX_FILE_NAME}) blobs in the given directory.
     * 
     * @param dir
     *            directory that contains the fragment and index blobs to be read
     * @throws IOException
     */
    public MultiresolutionMesh( File dir ) throws IOException {
        this( new File( dir, INDEX_FILE_NAME ), new File( dir, FRAGMENTS_FILE_NAME ) );
    }

    /**
     * Creates a new {@link MultiresolutionMesh} by reading the specified fragment ({@link #FRAGMENTS_FILE_NAME}) and
     * index ( {@link #INDEX_FILE_NAME}) blobs.
     * 
     * @param mrIndex
     *            index blob
     * @param meshFragments
     *            fragment blob
     * @throws IOException
     */
    public MultiresolutionMesh( File mrIndex, File meshFragments ) throws IOException {

        long begin = System.currentTimeMillis();

        // load multiresolution index
        FileChannel mrIndexChannel = new FileInputStream( mrIndex ).getChannel();
        ByteBuffer blobBuffer = ByteBuffer.allocateDirect( (int) mrIndexChannel.size() );
        mrIndexChannel.read( blobBuffer );
        mrIndexChannel.close();
        blobBuffer.rewind();

        long elapsed = System.currentTimeMillis() - begin;

        // extract (slice) nodes, arcs and fragmentInfo segments
        begin = System.currentTimeMillis();
        int flags = blobBuffer.getInt();
        int numNodes = blobBuffer.getInt();
        int numArcs = blobBuffer.getInt();
        int numFragments = blobBuffer.getInt();

        int nodesSegmentStart = INDEX_HEADER_SIZE;
        int arcsSegmentStart = nodesSegmentStart + numNodes * Node.SIZE;
        int fragmentsSegmentStart = arcsSegmentStart + numArcs * Arc.SIZE;

        blobBuffer.position( fragmentsSegmentStart );
        ByteBuffer fragmentsInfoBuffer = blobBuffer.slice();

        blobBuffer.position( arcsSegmentStart );
        blobBuffer.limit( fragmentsSegmentStart );
        ByteBuffer arcsBuffer = blobBuffer.slice();

        blobBuffer.position( nodesSegmentStart );
        blobBuffer.limit( arcsSegmentStart );
        ByteBuffer nodesBuffer = blobBuffer.slice();

        LOG.info( "MultiresolutionMesh: flags=" + flags + ", #fragments: " + numFragments + ", #nodes: " + numNodes
                  + ", #arcs: " + numArcs );
        LOG.debug( "- nodesBuffer: [" + nodesSegmentStart + '-' + ( arcsSegmentStart - 1 ) + "]" );
        LOG.debug( "- arcsBuffer: [" + arcsSegmentStart + '-' + ( fragmentsSegmentStart - 1 ) + "]" );
        LOG.debug( "- patchesBuffer: [" + fragmentsSegmentStart + '-' + ( blobBuffer.capacity() - 1 ) + "]" );
        elapsed = System.currentTimeMillis() - begin;
        LOG.debug( "mrindex_init=" + elapsed );

        nodes = createNodes( nodesBuffer );
        arcs = createArcs( arcsBuffer );
        fragments = createFragmentInfos( fragmentsInfoBuffer, new MeshFragmentDataReader( meshFragments ) );
    }

    /**
     * Extracts the smallest LOD that satisfies the given LOD criterion.
     * 
     * @param crit
     *            criterion that the LOD must satisfies
     * @return smallest LOD that satisfies the given LOD criterion
     */
    public List<MeshFragment> extractLOD( LODCriterion crit ) {
        return new SelectiveRefinement( this, crit ).determineLOD();
    }

    /**
     * Extracts the smallest LOD fragment that satisfies the given LOD criterion .
     * 
     * @param crit
     *            criterion that the LOD must satisfies
     * @param roi
     * @return smallest LOD that satisfies the given LOD criterion
     */
    public List<MeshFragment> extractLODFragment( LODCriterion crit, ViewFrustum roi ) {
        return new SpatialSelection( this, crit, roi, 1.0f ).determineLODFragment();
    }

    private Node[] createNodes( ByteBuffer nodesBuffer ) {
        Node[] nodes = new Node[nodesBuffer.capacity() / Node.SIZE];
        for ( int i = 0; i < nodes.length; i++ ) {
            nodes[i] = new Node( this, i, nodesBuffer, i * Node.SIZE );
        }
        return nodes;
    }

    private Arc[] createArcs( ByteBuffer arcsBuffer ) {
        Arc[] arcs = new Arc[arcsBuffer.capacity() / Arc.SIZE];
        for ( int i = 0; i < arcs.length; i++ ) {
            arcs[i] = new Arc( this, i, arcsBuffer, i * Arc.SIZE );
        }
        return arcs;
    }

    private MeshFragment[] createFragmentInfos( ByteBuffer patchBuffer, MeshFragmentDataReader patchDataReader ) {
        MeshFragment[] patches = new MeshFragment[patchBuffer.capacity() / MeshFragment.SIZE];
        for ( int i = 0; i < patches.length; i++ ) {
            int baseOffset = i * MeshFragment.SIZE;
            patchBuffer.position( baseOffset ).limit( baseOffset + MeshFragment.SIZE );
            ByteBuffer singlePatchBuffer = patchBuffer.slice();
            patches[i] = new MeshFragment( i, singlePatchBuffer, patchDataReader );
        }
        return patches;
    }
}
