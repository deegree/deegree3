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

package org.deegree.services.wpvs.config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.deegree.commons.utils.nio.DirectByteBufferPool;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.geometry.Envelope;
import org.deegree.rendering.r3d.multiresolution.MultiresolutionMesh;
import org.deegree.rendering.r3d.multiresolution.io.MeshFragmentDataReader;
import org.deegree.rendering.r3d.opengl.rendering.dem.manager.TerrainRenderingManager;
import org.deegree.services.jaxb.wpvs.DatasetDefinitions;
import org.deegree.services.jaxb.wpvs.ElevationDataset;

/**
 * The <code>DemDatasetWrapper</code> class TODO add class documentation here.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 * @version $Revision$, $Date$
 * 
 */
public class DemDatasetWrapper extends DatasetWrapper<TerrainRenderingManager> {

    private final static org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger( DemDatasetWrapper.class );

    private final int numberOfDEMFragmentsCached;

    private DirectByteBufferPool directMeshfragmentPool;

    /**
     * @param numberOfDEMFragmentsCached
     *            defines the number of dem fragments to be cached on the gpu.
     * @param directMeshfragmentPoolSize
     *            the size (in MB) of the pool used for allocating direct byte buffers for reading mesh fragments, used
     *            in the {@link MeshFragmentDataReader}.
     */
    public DemDatasetWrapper( int numberOfDEMFragmentsCached, int directMeshfragmentPoolSize ) {
        this.numberOfDEMFragmentsCached = numberOfDEMFragmentsCached;
        this.directMeshfragmentPool = new DirectByteBufferPool( directMeshfragmentPoolSize * 1024 * 1024,
                                                                "fragment_data" );
    }

    /**
     * Analyzes the {@link ElevationDataset} from the {@link DatasetDefinitions}, fills the renderers with data from the
     * defined modelbackends and builds up a the constraint vectors for retrieval of the appropriate renderers.
     * 
     * @param sceneEnvelope
     * @param toLocalCRS
     * @param configAdapter
     * 
     * @param dsd
     */
    @Override
    public Envelope fillFromDatasetDefinitions( Envelope sceneEnvelope, double[] toLocalCRS, XMLAdapter configAdapter,
                                                DatasetDefinitions dsd ) {
        List<ElevationDataset> demDatsets = new ArrayList<ElevationDataset>();
        ElevationDataset ed = dsd.getElevationDataset();
        demDatsets.add( ed );
        if ( !demDatsets.isEmpty() ) {
            sceneEnvelope = analyseAndExtractConstraints( demDatsets, sceneEnvelope, toLocalCRS,
                                                          dsd.getMaxPixelError(), configAdapter );
        } else {
            LOG.info( "No elevation model dataset has been configured, no buildings, trees and prototypes will be available." );
        }
        return sceneEnvelope;
    }

    private Envelope analyseAndExtractConstraints( List<ElevationDataset> demDatsets, Envelope sceneEnvelope,
                                                   double[] toLocalCRS, Double parentMaxPixelError,
                                                   XMLAdapter configAdapter ) {
        if ( demDatsets != null && !demDatsets.isEmpty() ) {
            for ( ElevationDataset eds : demDatsets ) {
                if ( eds != null ) {
                    if ( isUnAmbiguous( eds.getTitle() ) ) {
                        LOG.info( "The feature dataset with name: " + eds.getName() + " and title: " + eds.getTitle()
                                  + " had multiple definitions in your service configuration." );
                    } else {
                        clarifyInheritance( eds, parentMaxPixelError );
                        try {
                            sceneEnvelope = handleElevationDataset( eds, sceneEnvelope, toLocalCRS, configAdapter );
                        } catch ( IOException e ) {
                            LOG.error( "Failed to initialize configured demTexture dataset: " + eds.getName() + ": "
                                       + eds.getTitle() + " because: " + e.getLocalizedMessage(), e );
                        }
                    }
                }
            }
        }
        return sceneEnvelope;
    }

    /**
     * @param datatype
     * @param parentMaxPixelError
     */
    private void clarifyInheritance( ElevationDataset datatype, Double parentMaxPixelError ) {
        datatype.setMaxPixelError( clarifyMaxPixelError( parentMaxPixelError, datatype.getMaxPixelError() ) );
    }

    /**
     * @param configAdapter
     * @param toLocalCRS
     * @param sceneEnvelope
     * @param mds
     * @throws IOException
     */
    private Envelope handleElevationDataset( ElevationDataset elevationDataset, Envelope sceneEnvelope,
                                             double[] toLocalCRS, XMLAdapter configAdapter )
                            throws IOException {

        if ( elevationDataset != null ) {
            String storeId = elevationDataset.getBatchedMTStoreId();
            // TOOD lookup from manager / initialization
            MultiresolutionMesh mrModel = null;
            int i = 0;
//            while ( mrModel == null && i < abstractFiles.size() ) {
//                JAXBElement<? extends FileType> abstractFile = abstractFiles.get( i );
//                if ( abstractFile != null ) {
//                    FileType value = abstractFile.getValue();
//                    if ( value != null ) {
//                        String unresolvedDEMURL = value.getValue();
//                        URL demURL = resolve( configAdapter, unresolvedDEMURL );
//                        if ( demURL != null ) {
//                            LOG.info( "Using configured file: " + i + " for the dem file location: " + demURL.getFile() );
//                            mrModel = new MultiresolutionMesh( new File( demURL.getFile() ),
//                                                               this.directMeshfragmentPool );
//                        }
//                    }
//                }
//                i++;
//            }
//            if ( mrModel != null ) {
//                RenderFragmentManager fragmentManager = new RenderFragmentManager( mrModel, numberOfDEMFragmentsCached );
//
//                TerrainRenderingManager result = new TerrainRenderingManager( fragmentManager,
//                                                                              elevationDataset.getMaxPixelError(), 1,
//                                                                              ambientColor, diffuseColor,
//                                                                              specularColor, shininess );
//                // the fragment manager is in wpvs scene coordinates.
//                double[][] env = fragmentManager.getMultiresolutionMesh().getBBox();
//                double[] min = Arrays.copyOf( env[0], 3 );
//                double[] max = Arrays.copyOf( env[1], 3 );
//                min[0] += ( -toLocalCRS[0] );
//                min[1] += ( -toLocalCRS[1] );
//                max[0] += ( -toLocalCRS[0] );
//                max[1] += ( -toLocalCRS[1] );
//                Envelope datasetEnv = geomFac.createEnvelope( min, max, sceneEnvelope.getCoordinateSystem() );
//                sceneEnvelope = sceneEnvelope.merge( datasetEnv );
//
//                // adding the constraint to the wrapper.
//                min = Arrays.copyOf( env[0], 3 );
//                max = Arrays.copyOf( env[1], 3 );
//                datasetEnv = geomFac.createEnvelope( min, max, sceneEnvelope.getCoordinateSystem() );
//                addConstraint( elevationDataset.getTitle(), result, datasetEnv );
//            } else {
//                LOG.warn( "Enable to instantiate elevation model: " + elevationDataset.getName() + ": "
//                          + elevationDataset.getTitle()
//                          + " because no files (pointing to a Multiresolution Mesh file) could be resolved." );
//            }
//        } else {
//            LOG.warn( "Enable to instantiate elevation model: "
//                      + elevationDataset.getName()
//                      + ": "
//                      + elevationDataset.getTitle()
//                      + " because no files (pointing to a Multiresolution Mesh file) were configured in the elevationmodel datasource element." );
        }
        return sceneEnvelope;
    }
}
