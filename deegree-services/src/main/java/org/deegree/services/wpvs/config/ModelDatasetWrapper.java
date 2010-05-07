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

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.JAXBElement;

import org.deegree.commons.configuration.BoundingBoxType;
import org.deegree.commons.configuration.ScaleDenominatorsType;
import org.deegree.commons.utils.Pair;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.cs.CRS;
import org.deegree.geometry.Envelope;
import org.deegree.rendering.r3d.opengl.rendering.model.geometry.DirectGeometryBuffer;
import org.deegree.rendering.r3d.opengl.rendering.model.manager.BuildingRenderer;
import org.deegree.rendering.r3d.opengl.rendering.model.manager.LODSwitcher;
import org.deegree.rendering.r3d.opengl.rendering.model.manager.RenderableManager;
import org.deegree.rendering.r3d.opengl.rendering.model.manager.TreeRenderer;
import org.deegree.rendering.r3d.opengl.rendering.model.prototype.PrototypePool;
import org.deegree.rendering.r3d.opengl.rendering.model.prototype.RenderablePrototype;
import org.deegree.rendering.r3d.opengl.rendering.model.texture.TexturePool;
import org.deegree.services.jaxb.wpvs.DatasetDefinitions;
import org.deegree.services.jaxb.wpvs.RenderableDataset;
import org.deegree.services.jaxb.wpvs.SwitchLevels;
import org.deegree.services.jaxb.wpvs.SwitchLevels.Level;
import org.deegree.services.wpvs.exception.DatasourceException;
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
public class ModelDatasetWrapper extends DatasetWrapper<RenderableManager<?>> {
    private final static org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger( ModelDatasetWrapper.class );

    /** span of the default envelope */
    public final static double DEFAULT_SPAN = 0.001;

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
        List<RenderableDataset> datsets = dsd.getRenderableDataset();
        if ( !datsets.isEmpty() ) {
//            sceneEnvelope = analyseAndExtractConstraints( datsets, sceneEnvelope, toLocalCRS, dsd.getMaxPixelError(),
//                                                          configAdapter );
        } else {
            LOG.info( "No tree model dataset has been configured, no trees will be available." );
        }
        return sceneEnvelope;
    }

//    private Envelope analyseAndExtractConstraints( List<RenderableDataset> datasets, Envelope sceneEnvelope,
//                                                   double[] toLocalCRS, Double parentMaxPixelError, XMLAdapter adapter ) {
//        if ( datasets != null ) {
//            for ( RenderableDataset bds : datasets ) {
//                if ( bds != null ) {
//                    // ModelDataset t = configuredModelDatasets.put( tds.getTitle(), tds );
//                    if ( isUnAmbiguous( bds.getTitle() ) ) {
//                        LOG.info( "The feature dataset with name: " + bds.getName() + " and title: " + bds.getTitle()
//                                  + " had multiple definitions in your service configuration." );
//                    } else {
//                        clarifyInheritance( bds, parentMaxPixelError );
//                        List<Pair<AbstractGeospatialDataSourceType, ModelBackend<?>>> backends = initializeDatasources(
//                                                                                                                        bds,
//                                                                                                                        toLocalCRS,
//                                                                                                                        sceneEnvelope.getCoordinateSystem(),
//                                                                                                                        adapter );
//                        if ( bds.isIsBillboard() ) {
//                            sceneEnvelope = initTrees( sceneEnvelope, toLocalCRS, bds, backends );
//                        } else {
//                            sceneEnvelope = initBuildings( sceneEnvelope, toLocalCRS, bds, backends );
//                        }
//                    }
//                }
//            }
//        }
//        return sceneEnvelope;
//    }
//
//    /**
//     * Add the prototypes and building ordinates to the given backendinfo
//     * 
//     * @param result
//     *            to add the information to
//     * @param backends
//     *            to get the information from.
//     */
//    private void updateBackendInfo( ModelBackendInfo result,
//                                    List<Pair<AbstractGeospatialDataSourceType, ModelBackend<?>>> backends,
//                                    ModelBackend.Type infoType ) {
//        for ( Pair<AbstractGeospatialDataSourceType, ModelBackend<?>> pair : backends ) {
//            if ( pair != null && pair.second != null ) {
//                ModelBackend<?> mb = pair.second;
//                ModelBackendInfo backendInfo = mb.getBackendInfo( infoType );
//                result.add( backendInfo );
//            }
//        }
//    }
//
//    /**
//     * Read and add the trees from the modelbackend and fill the renderer with them.
//     * 
//     * @param toLocalCRS
//     * @param sceneEnvelope
//     * 
//     * @param configuredTreeDatasets
//     * @param backends
//     */
//    private Envelope initTrees( Envelope sceneEnvelope, double[] toLocalCRS, RenderableDataset configuredTreeDatasets,
//                                List<Pair<AbstractGeospatialDataSourceType, ModelBackend<?>>> backends ) {
//        ModelBackendInfo info = new ModelBackendInfo();
//        updateBackendInfo( info, backends, ModelBackend.Type.TREE );
//        Envelope dsEnvelope = info.getDatasetEnvelope();
//        if ( dsEnvelope == null ) {
//            dsEnvelope = createDefaultEnvelope( toLocalCRS, sceneEnvelope.getCoordinateSystem() );
//        }
//
//        // RB: Todo configure this value.
//        int numberOfObjectsInLeaf = 250;
//        if ( configuredTreeDatasets != null ) {
//            // Envelope buildingDomain = createEnvelope( configuredTreeDatasets.getBoundingBox(), getDefaultCRS(), null
//            // );
//            TreeRenderer configuredRender = new TreeRenderer( dsEnvelope, numberOfObjectsInLeaf,
//                                                              configuredTreeDatasets.getMaxPixelError() );
//            // Iterate over all configured datasources and add the trees from the datasources which match the scale
//            // and envelope of the configured tree dataset.
//            for ( Pair<AbstractGeospatialDataSourceType, ModelBackend<?>> pair : backends ) {
//                if ( pair != null && pair.first != null ) {
//                    // AbstractGeospatialDataSourceType ds = pair.first;
//                    // Envelope dsEnv = createEnvelope( ds.getBBoxConstraint().getBoundingBox(), getDefaultCRS(), null
//                    // );
//                    // if ( buildingDomain.intersects( dsEnv )
//                    // && scalesFit( configuredTreeDatasets.getScaleDenominators(),
//                    // ds.getScaleConstraint().getScaleDenominators() ) ) {
//                    ModelBackend<?> modelBackend = pair.second;
//                    if ( modelBackend != null ) {
//                        modelBackend.loadTrees( configuredRender, sceneEnvelope.getCoordinateSystem() );
//                    }
//
//                }
//            }
//            dsEnvelope = configuredRender.getValidDomain();
//            sceneEnvelope = mergeGlobalWithScene( toLocalCRS, dsEnvelope, sceneEnvelope );
//
//            addConstraint( configuredTreeDatasets.getTitle(), configuredRender, dsEnvelope );
//        }
//        return sceneEnvelope;
//    }
//
//    /**
//     * @param toLocalCRS
//     * @param parentBBox
//     * @param mb
//     * @return
//     */
//    private Envelope initBuildings( Envelope sceneEnvelope, double[] toLocalCRS,
//                                    RenderableDataset configuredBuildingsDS,
//                                    List<Pair<AbstractGeospatialDataSourceType, ModelBackend<?>>> backends ) {
//        ModelBackendInfo info = new ModelBackendInfo();
//        updateBuildingAndPrototypeBackendInfo( info, backends );
//        Envelope dsEnvelope = info.getDatasetEnvelope();
//        if ( dsEnvelope == null ) {
//            dsEnvelope = createDefaultEnvelope( toLocalCRS, sceneEnvelope.getCoordinateSystem() );
//        }
//
//        /**
//         * assuming each building has 10 geometries (in average), each geometry has 6 vertices (2 triangles ) and each
//         * vertex has 3 ordinates 10*6*3 = 180
//         */
//        int numberOfObjectsInLeaf = (int) Math.max( ( info.getOrdinateCount() / 180 ) * 0.01, 25 );
//        DirectGeometryBuffer geometryBuffer = new DirectGeometryBuffer( info.getOrdinateCount(),
//                                                                        info.getTextureOrdinateCount() );
//
//        // Envelope domain = createEnvelope( parentBBox, getDefaultCRS(), null );
//        BuildingRenderer allBuildings = new BuildingRenderer( dsEnvelope, numberOfObjectsInLeaf, geometryBuffer,
//                                                              configuredBuildingsDS.getMaxPixelError(),
//                                                              createLevels( configuredBuildingsDS.getSwitchLevels() ) );
//
//        List<RenderablePrototype> prototypes = new LinkedList<RenderablePrototype>();
//
//        for ( Pair<AbstractGeospatialDataSourceType, ModelBackend<?>> pair : backends ) {
//            if ( pair != null && pair.second != null ) {
//                ModelBackend<?> mb = pair.second;
//                mb.loadBuildings( allBuildings, sceneEnvelope.getCoordinateSystem() );
//                List<RenderablePrototype> mbPrototypes = mb.loadProtoTypes( geometryBuffer,
//                                                                            sceneEnvelope.getCoordinateSystem() );
//                if ( mbPrototypes != null && !mbPrototypes.isEmpty() ) {
//                    prototypes.addAll( mbPrototypes );
//                }
//            }
//        }
//
//        // Add the prototypes to the pool.
//        for ( RenderablePrototype rp : prototypes ) {
//            if ( rp != null ) {
//                PrototypePool.addPrototype( rp.getId(), rp );
//            }
//        }
//
//        // BuildingRenderer configuredBuildings = initConfiguredBuildings( numberOfObjectsInLeaf, geometryBuffer,
//        // allBuildings, configuredBuildingsDS, backends );
//
//        dsEnvelope = allBuildings.getValidDomain();
//        sceneEnvelope = mergeGlobalWithScene( toLocalCRS, dsEnvelope, sceneEnvelope );
//        addConstraint( configuredBuildingsDS.getTitle(), allBuildings, dsEnvelope );
//        return sceneEnvelope;
//        // }
//    }
//
//    /**
//     * Create the Lod switch level class from the configured values.
//     * 
//     * @param configuredLevels
//     * @return
//     */
//    private LODSwitcher createLevels( SwitchLevels configuredLevels ) {
//        LODSwitcher result = null;
//        if ( configuredLevels != null ) {
//            List<Level> levels = configuredLevels.getLevel();
//            List<Pair<Double, Double>> sl = new ArrayList<Pair<Double, Double>>( levels.size() );
//            for ( Level l : levels ) {
//                if ( l != null ) {
//                    sl.add( new Pair<Double, Double>( l.getMin(), l.getMax() ) );
//                }
//            }
//            result = new LODSwitcher( sl );
//        }
//        return result;
//    }
//
//    /**
//     * Add the prototypes and building ordinates to the given backendinfo
//     * 
//     * @param result
//     *            to add the information to
//     * @param backends
//     *            to get the information from.
//     */
//    private void updateBuildingAndPrototypeBackendInfo(
//                                                        ModelBackendInfo result,
//                                                        List<Pair<AbstractGeospatialDataSourceType, ModelBackend<?>>> backends ) {
//        for ( Pair<AbstractGeospatialDataSourceType, ModelBackend<?>> pair : backends ) {
//            if ( pair != null && pair.second != null ) {
//                ModelBackend<?> mb = pair.second;
//                ModelBackendInfo buildingInfo = mb.getBackendInfo( ModelBackend.Type.BUILDING );
//                ModelBackendInfo protoInfo = mb.getBackendInfo( ModelBackend.Type.PROTOTYPE );
//                result.add( buildingInfo );
//                result.add( protoInfo );
//            }
//        }
//    }
//
//    /**
//     * Get the initialized datasources
//     * 
//     * @param mds
//     * @param translationToLocalCRS
//     *            to the scene 0
//     * @param defaultCRS
//     *            of the wpvs
//     * @param adapter
//     *            to use as resolver
//     * @return the initialized model datasources
//     */
//    protected List<Pair<AbstractGeospatialDataSourceType, ModelBackend<?>>> initializeDatasources(
//                                                                                                   RenderableDataset mds,
//                                                                                                   double[] translationToLocalCRS,
//                                                                                                   CRS defaultCRS,
//                                                                                                   XMLAdapter adapter ) {
//        List<AbstractGeospatialDataSourceType> datasources = null;// mds.getRenderableStoreId();
//        // todo get from the manager
//        List<Pair<AbstractGeospatialDataSourceType, ModelBackend<?>>> backends = new ArrayList<Pair<AbstractGeospatialDataSourceType, ModelBackend<?>>>(
//                                                                                                                                                         datasources.size() );
//        if ( !datasources.isEmpty() ) {
//
//            // BoundingBoxType parentBBox = mds.getBoundingBox();
//            // ScaleDenominatorsType parentScales = mds.getScaleDenominators();
//            // first initialize the backends.
//            for ( AbstractGeospatialDataSourceType ds : datasources ) {
//                if ( ds != null ) {
//                    // clarifyConstraints( parentBBox, parentScales, ds );
//                    ModelBackend<?> mb = getModelBackend( ds, adapter );
//                    if ( mb != null ) {
//                        // if ( mb instanceof PostgisBackend ) {
//                        // double[] toLocal = getTranslationToLocalCRS();
//                        mb.setWPVSTranslationVector( new double[] {
//                                                                   translationToLocalCRS[0],
//                                                                   translationToLocalCRS[1],
//                                                                   ( translationToLocalCRS.length == 3 ? translationToLocalCRS[2]
//                                                                                                      : 0 ) } );
//                        mb.setWPVSBaseCRS( defaultCRS );
//                        // }
//                        backends.add( new Pair<AbstractGeospatialDataSourceType, ModelBackend<?>>( ds, mb ) );
//                    }
//                }
//            }
//        }
//        return backends;
//    }
//
//    /**
//     * 
//     * @param datatype
//     * @param parentMaxPixelError
//     */
//    protected void clarifyInheritance( RenderableDataset datatype, Double parentMaxPixelError ) {
//        datatype.setMaxPixelError( clarifyMaxPixelError( parentMaxPixelError, datatype.getMaxPixelError() ) );
//    }
//
//    /**
//     * Checks and sets the constraints of the given datasource.
//     * 
//     * @param parentBBox
//     *            of the parent
//     * @param parentScales
//     *            of the parent
//     * @param agds
//     *            to be clarified.
//     */
//    protected void clarifyConstraints( BoundingBoxType parentBBox, ScaleDenominatorsType parentScales,
//                                       AbstractGeospatialDataSourceType agds ) {
//        BBoxConstraint bbc = agds.getBBoxConstraint();
//        if ( bbc == null ) {
//            bbc = new BBoxConstraint();
//            bbc.setBoundingBox( parentBBox );
//            agds.setBBoxConstraint( bbc );
//        }
//
//        ScaleConstraint sc = agds.getScaleConstraint();
//        if ( sc == null ) {
//            sc = new ScaleConstraint();
//            sc.setScaleDenominators( parentScales );
//            agds.setScaleConstraint( sc );
//        }
//
//    }
//
//    /**
//     * Creates a {@link ModelBackend} from the given datasource.
//     * 
//     * @param ds
//     *            to create the backend from.
//     * @return the initialized {@link ModelBackend}
//     */
//    private ModelBackend<?> getModelBackend( AbstractGeospatialDataSourceType ds, XMLAdapter adapter ) {
//        ModelBackend<?> mb = null;
//        if ( ds != null ) {
//            String id = null;
//            String hostURL = null;
//            if ( ds instanceof ConstrainedDatabaseDataSourceType ) {
//                ConstrainedDatabaseDataSourceType cds = (ConstrainedDatabaseDataSourceType) ds;
//                id = cds.getConnectionPoolId();
//            } else if ( ds instanceof GeospatialFileSystemDataSourceType ) {
//                GeospatialFileSystemDataSourceType gfds = (GeospatialFileSystemDataSourceType) ds;
//                List<JAXBElement<? extends FileType>> files = gfds.getAbstractFile();
//                if ( files.isEmpty() ) {
//                    JAXBElement<? extends FileSetType> fileSet = gfds.getAbstractFileSet();
//                    if ( fileSet != null ) {
//                        LOG.warn( "Filesets are currently not supported for modeltypes." );
//                    }
//                } else {
//                    id = "FileBackend";
//                    if ( files.size() > 1 ) {
//                        LOG.warn( "Multiple model filedatasource files declared, currently only the first directory is evaluated." );
//                    }
//                    // Only interested in the first one.
//                    JAXBElement<? extends FileType> dataSource = files.get( 0 );
//                    String file = dataSource.getValue().getValue();
//                    URL resolvedUrl = resolve( adapter, file );
//                    File f = new File( resolvedUrl.getFile() );
//                    if ( !f.exists() ) {
//                        LOG.warn( "Datasourcefile: " + file + " does not exist." );
//                    } else {
//                        if ( !f.isDirectory() ) {
//                            LOG.warn( "Datasourcefile: " + file + " is not a directory, this may not be." );
//                        } else {
//                            hostURL = f.getAbsolutePath();
//                        }
//                    }
//                }
//
//            }
//            try {
//                mb = ModelBackend.getInstance( id, hostURL );
//            } catch ( UnsupportedOperationException e ) {
//                LOG.error( "Ignoring datasource: " + ds.getDataSourceName() + " because: " + e.getLocalizedMessage(), e );
//            } catch ( DatasourceException e ) {
//                LOG.error( "Ignoring datasource because: " + e.getLocalizedMessage(), e );
//            }
//
//        }
//
//        return mb;
//    }
//
//    /**
//     * @param translationVector
//     * @param coordinateSystem
//     * @return a default 3d envelope at 0,0 from the scene.
//     */
//    public static Envelope createDefaultEnvelope( double[] translationVector, CRS coordinateSystem ) {
//        return geomFac.createEnvelope( new double[] { 0, 0, 0 }, new double[] { DEFAULT_SPAN, DEFAULT_SPAN,
//                                                                               DEFAULT_SPAN }, coordinateSystem );
//    }
//
//    /**
//     * @param toLocalCRS
//     * @param dsEnvelope
//     * @param sceneEnvelope
//     * @return the merged scene envelope (in realworld coordinates).
//     */
//    public Envelope mergeGlobalWithScene( double[] toLocalCRS, Envelope dsEnvelope, Envelope sceneEnvelope ) {
//        if ( dsEnvelope != null && ( Math.abs( dsEnvelope.getSpan0() - ModelDatasetWrapper.DEFAULT_SPAN ) > 1E-8 ) ) {
//
//            // convert the global dataset (in wpvs world coordinates) to real world coordinates.
//            double[] min = dsEnvelope.getMin().getAsArray();
//            double[] max = dsEnvelope.getMax().getAsArray();
//            double[] tMin = Arrays.copyOf( min, min.length );
//            double[] tMax = Arrays.copyOf( max, max.length );
//            tMin[0] += ( -toLocalCRS[0] );
//            tMin[1] += ( -toLocalCRS[1] );
//            tMax[0] += ( -toLocalCRS[0] );
//            tMax[1] += ( -toLocalCRS[1] );
//            Envelope tEnv = geomFac.createEnvelope( tMin, tMax, dsEnvelope.getCoordinateSystem() );
//            sceneEnvelope = sceneEnvelope.merge( tEnv );
//        }
//        return sceneEnvelope;
//    }

}
