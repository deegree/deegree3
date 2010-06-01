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

import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.deegree.commons.xml.XMLAdapter;
import org.deegree.geometry.Envelope;
import org.deegree.rendering.r3d.multiresolution.MultiresolutionMesh;
import org.deegree.rendering.r3d.multiresolution.io.MeshFragmentDataReader;
import org.deegree.rendering.r3d.multiresolution.persistence.BatchedMTStore;
import org.deegree.rendering.r3d.multiresolution.persistence.BatchedMTStoreManager;
import org.deegree.rendering.r3d.opengl.rendering.dem.manager.RenderFragmentManager;
import org.deegree.rendering.r3d.opengl.rendering.dem.manager.TerrainRenderingManager;
import org.deegree.services.jaxb.wpvs.DEMDatasetConfig;
import org.deegree.services.jaxb.wpvs.DatasetDefinitions;
import org.slf4j.Logger;

/**
 * The <code>DemDatasetWrapper</code> class TODO add class documentation here.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class DEMDataset extends Dataset<TerrainRenderingManager> {

    private final static Logger LOG = getLogger( DEMDataset.class );

    private final int numberOfDEMFragmentsCached;

    private float[] ambientColor;

    private float[] specularColor;

    private float[] diffuseColor;

    private float shininess;

    /**
     * @param numberOfDEMFragmentsCached
     *            defines the number of dem fragments to be cached on the gpu.
     * @param directMeshfragmentPoolSize
     *            the size (in MB) of the pool used for allocating direct byte buffers for reading mesh fragments, used
     *            in the {@link MeshFragmentDataReader}.
     * @param ambientColor
     * @param diffuseColor
     * @param specularColor
     * @param shininess
     */
    public DEMDataset( int numberOfDEMFragmentsCached, int directMeshfragmentPoolSize, float[] ambientColor,
                              float[] diffuseColor, float[] specularColor, float shininess ) {
        this.numberOfDEMFragmentsCached = numberOfDEMFragmentsCached;
        this.ambientColor = ambientColor;
        this.diffuseColor = diffuseColor;
        this.specularColor = specularColor;
        this.shininess = shininess;
        // this.directMeshfragmentPool = new DirectByteBufferPool( directMeshfragmentPoolSize * 1024 * 1024,
        // "fragment_data" );
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
        List<DEMDatasetConfig> demDatsets = new ArrayList<DEMDatasetConfig>();
        DEMDatasetConfig ed = dsd.getDEMDataset();
        demDatsets.add( ed );
        if ( !demDatsets.isEmpty() ) {
            sceneEnvelope = initDatasets( demDatsets, sceneEnvelope, toLocalCRS, dsd.getMaxPixelError(), configAdapter );
        } else {
            LOG.info( "No elevation model dataset has been configured, no buildings, trees and prototypes will be available." );
        }
        return sceneEnvelope;
    }

    private Envelope initDatasets( List<DEMDatasetConfig> demDatsets, Envelope sceneEnvelope, double[] toLocalCRS,
                                   Double parentMaxPixelError, XMLAdapter configAdapter ) {
        if ( demDatsets != null && !demDatsets.isEmpty() ) {
            for ( DEMDatasetConfig eds : demDatsets ) {
                if ( eds != null ) {
                    if ( isUnAmbiguous( eds.getTitle() ) ) {
                        LOG.info( "The elevation model dataset with name: " + eds.getName() + " and title: "
                                  + eds.getTitle() + " had multiple definitions in your service configuration." );
                    } else {
                        clarifyInheritance( eds, parentMaxPixelError );
                        try {
                            sceneEnvelope = handleDEMDataset( eds, sceneEnvelope, toLocalCRS, configAdapter );
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
    private void clarifyInheritance( DEMDatasetConfig datatype, Double parentMaxPixelError ) {
        datatype.setMaxPixelError( clarifyMaxPixelError( parentMaxPixelError, datatype.getMaxPixelError() ) );
    }

    /**
     * @param configAdapter
     * @param toLocalCRS
     * @param sceneEnvelope
     * @param mds
     * @throws IOException
     */
    private Envelope handleDEMDataset( DEMDatasetConfig demDataset, Envelope sceneEnvelope, double[] toLocalCRS,
                                       XMLAdapter configAdapter )
                            throws IOException {

        if ( demDataset != null ) {
            String storeId = demDataset.getBatchedMTStoreId();
            BatchedMTStore store = BatchedMTStoreManager.get( storeId );
            MultiresolutionMesh mrModel = store.getMesh();

            if ( mrModel != null ) {
                RenderFragmentManager fragmentManager = new RenderFragmentManager( mrModel, numberOfDEMFragmentsCached );

                TerrainRenderingManager result = new TerrainRenderingManager( fragmentManager,
                                                                              demDataset.getMaxPixelError(), 1,
                                                                              ambientColor, diffuseColor,
                                                                              specularColor, shininess );
                // the fragment manager is in wpvs scene coordinates.
                double[][] env = fragmentManager.getMultiresolutionMesh().getBBox();
                double[] min = Arrays.copyOf( env[0], 3 );
                double[] max = Arrays.copyOf( env[1], 3 );
                min[0] += ( -toLocalCRS[0] );
                min[1] += ( -toLocalCRS[1] );
                max[0] += ( -toLocalCRS[0] );
                max[1] += ( -toLocalCRS[1] );
                Envelope datasetEnv = geomFac.createEnvelope( min, max, sceneEnvelope.getCoordinateSystem() );
                sceneEnvelope = sceneEnvelope.merge( datasetEnv );

                // adding the constraint to the wrapper.
                min = Arrays.copyOf( env[0], 3 );
                max = Arrays.copyOf( env[1], 3 );
                datasetEnv = geomFac.createEnvelope( min, max, sceneEnvelope.getCoordinateSystem() );
                addConstraint( demDataset.getTitle(), result, datasetEnv );
            } else {
                LOG.warn( "Enable to instantiate elevation model: " + demDataset.getName() + ": "
                          + demDataset.getTitle()
                          + " because no files (pointing to a Multiresolution Mesh file) could be resolved." );
            }
        } else {
            LOG.warn( "Enable to instantiate elevation model: "
                      + demDataset.getName()
                      + ": "
                      + demDataset.getTitle()
                      + " because no files (pointing to a Multiresolution Mesh file) were configured in the elevationmodel datasource element." );
        }
        return sceneEnvelope;
    }
}
