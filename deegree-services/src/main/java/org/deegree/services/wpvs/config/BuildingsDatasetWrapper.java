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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.deegree.commons.datasource.configuration.AbstractGeospatialDataSourceType;
import org.deegree.commons.utils.Pair;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.geometry.Envelope;
import org.deegree.rendering.r3d.opengl.rendering.model.geometry.DirectGeometryBuffer;
import org.deegree.rendering.r3d.opengl.rendering.model.manager.BuildingRenderer;
import org.deegree.rendering.r3d.opengl.rendering.model.manager.LODSwitcher;
import org.deegree.rendering.r3d.opengl.rendering.model.prototype.PrototypePool;
import org.deegree.rendering.r3d.opengl.rendering.model.prototype.RenderablePrototype;
import org.deegree.services.wpvs.configuration.Buildings;
import org.deegree.services.wpvs.configuration.DatasetDefinitions;
import org.deegree.services.wpvs.configuration.SwitchLevels;
import org.deegree.services.wpvs.configuration.SwitchLevels.Level;
import org.deegree.services.wpvs.io.ModelBackend;
import org.deegree.services.wpvs.io.ModelBackendInfo;

/**
 * The <code>ModelDatasetWrapper</code> class TODO add class documentation here.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 * @version $Revision$, $Date$
 * 
 */
public class BuildingsDatasetWrapper extends ModelDatasetWrapper<BuildingRenderer> {
    private final static org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger( BuildingsDatasetWrapper.class );

    /**
     * Analyzes the datasets from the {@link DatasetDefinitions}, fills the renderers with data from the defined
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
        List<Buildings> buildingDatsets = dsd.getBuildings();
        if ( !buildingDatsets.isEmpty() ) {
            sceneEnvelope = analyseAndExtractConstraints( buildingDatsets, sceneEnvelope, toLocalCRS,
                                                          dsd.getMaxPixelError(), configAdapter );
        } else {
            LOG.info( "No building model dataset has been configured, no buildings and prototypes will be available." );
        }
        return sceneEnvelope;
    }

    private Envelope analyseAndExtractConstraints( List<Buildings> buildingsDatasets, Envelope sceneEnvelope,
                                                   double[] toLocalCRS, Double parentMaxPixelError, XMLAdapter adapter ) {
        if ( buildingsDatasets != null && !buildingsDatasets.isEmpty() ) {
            for ( Buildings bds : buildingsDatasets ) {
                if ( bds != null ) {
                    // ModelDataset t = configuredModelDatasets.put( tds.getTitle(), tds );
                    if ( isUnAmbiguous( bds.getTitle() ) ) {
                        LOG.info( "The feature dataset with name: " + bds.getName() + " and title: " + bds.getTitle()
                                  + " had multiple definitions in your service configuration." );
                    } else {
                        clarifyInheritance( bds, parentMaxPixelError );
                        loadTextureDirs( bds );
                        List<Pair<AbstractGeospatialDataSourceType, ModelBackend<?>>> backends = initializeDatasources(
                                                                                                                        bds,
                                                                                                                        toLocalCRS,
                                                                                                                        sceneEnvelope.getCoordinateSystem(),
                                                                                                                        adapter );
                        sceneEnvelope = initBuildings( sceneEnvelope, toLocalCRS, bds, backends );
                        sceneEnvelope = analyseAndExtractConstraints( bds.getBuildings(), sceneEnvelope, toLocalCRS,
                                                                      bds.getMaxPixelError(), adapter );
                    }
                }
            }
        }
        return sceneEnvelope;
    }

    /**
     * @param toLocalCRS
     * @param parentBBox
     * @param mb
     * @return
     */
    private Envelope initBuildings( Envelope sceneEnvelope, double[] toLocalCRS, Buildings configuredBuildingsDS,
                                    List<Pair<AbstractGeospatialDataSourceType, ModelBackend<?>>> backends ) {
        ModelBackendInfo info = new ModelBackendInfo();
        updateBuildingAndPrototypeBackendInfo( info, backends );
        Envelope dsEnvelope = info.getDatasetEnvelope();
        if ( dsEnvelope == null ) {
            dsEnvelope = super.createDefaultEnvelope( toLocalCRS, sceneEnvelope.getCoordinateSystem() );
        }

        /**
         * assuming each building has 10 geometries (in average), each geometry has 6 vertices (2 triangles ) and each
         * vertex has 3 ordinates 10*6*3 = 180
         */
        int numberOfObjectsInLeaf = (int) Math.max( ( info.getOrdinateCount() / 180 ) * 0.01, 25 );
        DirectGeometryBuffer geometryBuffer = new DirectGeometryBuffer( info.getOrdinateCount(),
                                                                        info.getTextureOrdinateCount() );

        // Envelope domain = createEnvelope( parentBBox, getDefaultCRS(), null );
        BuildingRenderer allBuildings = new BuildingRenderer( dsEnvelope, numberOfObjectsInLeaf, geometryBuffer,
                                                              configuredBuildingsDS.getMaxPixelError(),
                                                              createLevels( configuredBuildingsDS.getSwitchLevels() ) );

        List<RenderablePrototype> prototypes = new LinkedList<RenderablePrototype>();

        for ( Pair<AbstractGeospatialDataSourceType, ModelBackend<?>> pair : backends ) {
            if ( pair != null && pair.second != null ) {
                ModelBackend<?> mb = pair.second;
                mb.loadBuildings( allBuildings, sceneEnvelope.getCoordinateSystem() );
                List<RenderablePrototype> mbPrototypes = mb.loadProtoTypes( geometryBuffer,
                                                                            sceneEnvelope.getCoordinateSystem() );
                if ( mbPrototypes != null && !mbPrototypes.isEmpty() ) {
                    prototypes.addAll( mbPrototypes );
                }
            }
        }

        // Add the prototypes to the pool.
        for ( RenderablePrototype rp : prototypes ) {
            if ( rp != null ) {
                PrototypePool.addPrototype( rp.getId(), rp );
            }
        }

        // BuildingRenderer configuredBuildings = initConfiguredBuildings( numberOfObjectsInLeaf, geometryBuffer,
        // allBuildings, configuredBuildingsDS, backends );

        dsEnvelope = allBuildings.getValidDomain();
        sceneEnvelope = super.mergeGlobalWithScene( toLocalCRS, dsEnvelope, sceneEnvelope );
        addConstraint( configuredBuildingsDS.getTitle(), allBuildings, dsEnvelope );
        return sceneEnvelope;
        // }
    }

    // /**
    // * Initialize the buildings by checking their bbox with the scaledenomitors.
    // *
    // * @param numberObjectsInLeaves
    // * @param geometryBuffer
    // * @param allBuildings
    // * @param configuredBuildingDatasets
    // * @param backends
    // * @return
    // */
    // private BuildingRenderer initConfiguredBuildings(
    // int numberObjectsInLeaves,
    // DirectGeometryBuffer geometryBuffer,
    // BuildingRenderer allBuildings,
    // Buildings configuredBuildingsDS,
    // List<Pair<AbstractGeospatialDataSourceType, ModelBackend<?>>> backends ) {
    // BuildingRenderer configuredRender = null;
    // // assume equal spread in the domain.
    // if ( configuredBuildingsDS != null ) {
    // Envelope buildingDomain = createEnvelope( configuredBuildingsDS.getBoundingBox(), getDefaultCRS(), null );
    // configuredRender = new BuildingRenderer( buildingDomain, numberObjectsInLeaves, geometryBuffer,
    // configuredBuildingsDS.getMaxPixelError(),
    // createLevels( configuredBuildingsDS.getSwitchLevels() ) );
    // for ( Pair<AbstractGeospatialDataSourceType, ModelBackend<?>> pair : backends ) {
    // if ( pair != null && pair.first != null ) {
    // AbstractGeospatialDataSourceType ds = pair.first;
    // Envelope dsEnv = createEnvelope( ds.getBBoxConstraint().getBoundingBox(), getDefaultCRS(), null );
    // if ( buildingDomain.intersects( dsEnv )
    // && scalesFit( configuredBuildingsDS.getScaleDenominators(),
    // ds.getScaleConstraint().getScaleDenominators() ) ) {
    // List<WorldRenderableObject> intersectingBuildings = allBuildings.getObjects( dsEnv );
    // if ( intersectingBuildings != null && !intersectingBuildings.isEmpty() ) {
    // configuredRender.addAll( intersectingBuildings );
    // }
    // }
    // }
    // }
    // }
    //
    // return configuredRender;
    //
    // }

    /**
     * Create the Lod switch level class from the configured values.
     * 
     * @param configuredLevels
     * @return
     */
    private LODSwitcher createLevels( SwitchLevels configuredLevels ) {
        LODSwitcher result = null;
        if ( configuredLevels != null ) {
            List<Level> levels = configuredLevels.getLevel();
            List<Pair<Double, Double>> sl = new ArrayList<Pair<Double, Double>>( levels.size() );
            for ( Level l : levels ) {
                if ( l != null ) {
                    sl.add( new Pair<Double, Double>( l.getMin(), l.getMax() ) );
                }
            }
            result = new LODSwitcher( sl );
        }
        return result;
    }

    /**
     * Add the prototypes and building ordinates to the given backendinfo
     * 
     * @param result
     *            to add the information to
     * @param backends
     *            to get the information from.
     */
    private void updateBuildingAndPrototypeBackendInfo(
                                                        ModelBackendInfo result,
                                                        List<Pair<AbstractGeospatialDataSourceType, ModelBackend<?>>> backends ) {
        for ( Pair<AbstractGeospatialDataSourceType, ModelBackend<?>> pair : backends ) {
            if ( pair != null && pair.second != null ) {
                ModelBackend<?> mb = pair.second;
                ModelBackendInfo buildingInfo = mb.getBackendInfo( ModelBackend.Type.BUILDING );
                ModelBackendInfo protoInfo = mb.getBackendInfo( ModelBackend.Type.PROTOTYPE );
                result.add( buildingInfo );
                result.add( protoInfo );
            }
        }
    }
}
