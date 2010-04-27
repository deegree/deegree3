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

import java.util.List;

import org.deegree.commons.datasource.configuration.AbstractGeospatialDataSourceType;
import org.deegree.commons.utils.Pair;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.geometry.Envelope;
import org.deegree.rendering.r3d.opengl.rendering.model.manager.TreeRenderer;
import org.deegree.services.wpvs.configuration.DatasetDefinitions;
import org.deegree.services.wpvs.configuration.Trees;
import org.deegree.services.wpvs.io.ModelBackend;
import org.deegree.services.wpvs.io.ModelBackendInfo;

/**
 * The <code>ModelDatasetWrapper</code> class initilializes the trees from the backend.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 * @version $Revision$, $Date$
 * 
 */
public class TreesDatasetWrapper extends ModelDatasetWrapper<TreeRenderer> {
    private final static org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger( TreesDatasetWrapper.class );

    /**
     * Analyzes the ModelDataset from the {@link DatasetDefinitions}, fills the renderers with data from the defined
     * modelbackends and builds up a the constraint vectors for retrieval of the appropriate renderers.
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
        List<Trees> treesDatsets = dsd.getTrees();
        if ( !treesDatsets.isEmpty() ) {
            sceneEnvelope = analyseAndExtractConstraints( treesDatsets, sceneEnvelope, toLocalCRS,
                                                          dsd.getMaxPixelError(), configAdapter );
        } else {
            LOG.info( "No tree model dataset has been configured, no trees will be available." );
        }
        return sceneEnvelope;
    }

    private Envelope analyseAndExtractConstraints( List<Trees> treesDatasets, Envelope sceneEnvelope,
                                                   double[] toLocalCRS, double parentMaxPixelError, XMLAdapter adapter ) {
        if ( treesDatasets != null && !treesDatasets.isEmpty() ) {
            for ( Trees treeDS : treesDatasets ) {
                if ( treeDS != null ) {
                    // ModelDataset t = configuredModelDatasets.put( tds.getTitle(), tds );
                    if ( isUnAmbiguous( treeDS.getTitle() ) ) {
                        LOG.info( "The feature dataset with name: " + treeDS.getName() + " and title: "
                                  + treeDS.getTitle() + " had multiple definitions in your service configuration." );
                    } else {
                        clarifyInheritance( treeDS, parentMaxPixelError );
                        loadTextureDirs( treeDS );
                        List<Pair<AbstractGeospatialDataSourceType, ModelBackend<?>>> backends = initializeDatasources(
                                                                                                                        treeDS,
                                                                                                                        toLocalCRS,
                                                                                                                        sceneEnvelope.getCoordinateSystem(),
                                                                                                                        adapter );
                        sceneEnvelope = initTrees( sceneEnvelope, toLocalCRS, treeDS, backends );
                        sceneEnvelope = analyseAndExtractConstraints( treeDS.getTrees(), sceneEnvelope, toLocalCRS,
                                                                      treeDS.getMaxPixelError(), adapter );
                    }
                }
            }
        }
        return sceneEnvelope;
    }

    /**
     * Add the prototypes and building ordinates to the given backendinfo
     * 
     * @param result
     *            to add the information to
     * @param backends
     *            to get the information from.
     */
    private void updateBackendInfo( ModelBackendInfo result,
                                    List<Pair<AbstractGeospatialDataSourceType, ModelBackend<?>>> backends,
                                    ModelBackend.Type infoType ) {
        for ( Pair<AbstractGeospatialDataSourceType, ModelBackend<?>> pair : backends ) {
            if ( pair != null && pair.second != null ) {
                ModelBackend<?> mb = pair.second;
                ModelBackendInfo backendInfo = mb.getBackendInfo( infoType );
                result.add( backendInfo );
            }
        }
    }

    /**
     * Read and add the trees from the modelbackend and fill the renderer with them.
     * 
     * @param toLocalCRS
     * @param sceneEnvelope
     * 
     * @param configuredTreeDatasets
     * @param backends
     */
    private Envelope initTrees( Envelope sceneEnvelope, double[] toLocalCRS, Trees configuredTreeDatasets,
                                List<Pair<AbstractGeospatialDataSourceType, ModelBackend<?>>> backends ) {
        ModelBackendInfo info = new ModelBackendInfo();
        updateBackendInfo( info, backends, ModelBackend.Type.TREE );
        Envelope dsEnvelope = info.getDatasetEnvelope();
        if ( dsEnvelope == null ) {
            dsEnvelope = super.createDefaultEnvelope( toLocalCRS, sceneEnvelope.getCoordinateSystem() );
        }

        // RB: Todo configure this value.
        int numberOfObjectsInLeaf = 250;
        if ( configuredTreeDatasets != null ) {
            // Envelope buildingDomain = createEnvelope( configuredTreeDatasets.getBoundingBox(), getDefaultCRS(), null
            // );
            TreeRenderer configuredRender = new TreeRenderer( dsEnvelope, numberOfObjectsInLeaf,
                                                              configuredTreeDatasets.getMaxPixelError() );
            // Iterate over all configured datasources and add the trees from the datasources which match the scale
            // and envelope of the configured tree dataset.
            for ( Pair<AbstractGeospatialDataSourceType, ModelBackend<?>> pair : backends ) {
                if ( pair != null && pair.first != null ) {
                    // AbstractGeospatialDataSourceType ds = pair.first;
                    // Envelope dsEnv = createEnvelope( ds.getBBoxConstraint().getBoundingBox(), getDefaultCRS(), null
                    // );
                    // if ( buildingDomain.intersects( dsEnv )
                    // && scalesFit( configuredTreeDatasets.getScaleDenominators(),
                    // ds.getScaleConstraint().getScaleDenominators() ) ) {
                    ModelBackend<?> modelBackend = pair.second;
                    if ( modelBackend != null ) {
                        modelBackend.loadTrees( configuredRender, sceneEnvelope.getCoordinateSystem() );
                    }

                }
            }
            dsEnvelope = configuredRender.getValidDomain();
            sceneEnvelope = super.mergeGlobalWithScene( toLocalCRS, dsEnvelope, sceneEnvelope );

            addConstraint( configuredTreeDatasets.getTitle(), configuredRender, dsEnvelope );
        }
        return sceneEnvelope;
    }
}
