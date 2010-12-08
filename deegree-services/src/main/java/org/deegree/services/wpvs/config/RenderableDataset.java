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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

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
import org.deegree.rendering.r3d.persistence.RenderableStore;
import org.deegree.rendering.r3d.persistence.RenderableStoreManager;
import org.deegree.services.jaxb.wpvs.DatasetDefinitions;
import org.deegree.services.jaxb.wpvs.RenderableDatasetConfig;
import org.deegree.services.jaxb.wpvs.SwitchLevels;
import org.deegree.services.jaxb.wpvs.SwitchLevels.Level;
import org.deegree.services.wpvs.io.ModelBackend;
import org.deegree.services.wpvs.io.ModelBackendInfo;
import org.slf4j.Logger;

/**
 * The <code>ModelDatasetWrapper</code> loads the entities and the billboards from the configured backends.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class RenderableDataset extends Dataset<RenderableManager<?>> {

    private final static Logger LOG = getLogger( RenderableDataset.class );

    /** span of the default envelope */
    public final static double DEFAULT_SPAN = 0.001;

    /**
     * Analyzes the ModelDataset from the {@link DatasetDefinitions}, fills the renderers with data from the defined
     * {@link RenderableStore} and builds up a the constraint vectors for retrieval of the appropriate renderers.
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
        List<RenderableDatasetConfig> datsets = dsd.getRenderableDataset();
        if ( !datsets.isEmpty() ) {
            sceneEnvelope = initDatasets( datsets, sceneEnvelope, toLocalCRS, dsd.getMaxPixelError() );
        } else {
            LOG.info( "No tree model dataset has been configured, no trees will be available." );
        }
        return sceneEnvelope;
    }

    private Envelope initDatasets( List<RenderableDatasetConfig> datasets, Envelope sceneEnvelope, double[] toLocalCRS,
                                   Double parentMaxPixelError ) {
        if ( datasets != null ) {
            for ( RenderableDatasetConfig bds : datasets ) {
                if ( bds != null ) {
                    if ( isUnAmbiguous( bds.getTitle() ) ) {
                        LOG.info( "The feature dataset with name: " + bds.getName() + " and title: " + bds.getTitle()
                                  + " had multiple definitions in your service configuration." );
                    } else {
                        clarifyInheritance( bds, parentMaxPixelError );
                        String renderableStoreId = bds.getRenderableStoreId();
                        if ( renderableStoreId != null ) {
                            RenderableStore renderableStore = RenderableStoreManager.get( renderableStoreId );
                            if ( renderableStore != null ) {
                                if ( renderableStore.isBillboard() ) {
                                    sceneEnvelope = initBillboards( sceneEnvelope, toLocalCRS, bds, renderableStore );
                                } else {
                                    sceneEnvelope = initEntities( sceneEnvelope, toLocalCRS, bds, renderableStore );
                                }
                            }
                        }
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
    private void updateBackendInfo( ModelBackendInfo result, ModelBackend<?> backend, ModelBackend.Type infoType ) {
        if ( backend != null ) {
            ModelBackendInfo backendInfo = backend.getBackendInfo( infoType );
            result.add( backendInfo );
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
    private Envelope initBillboards( Envelope sceneEnvelope, double[] toLocalCRS,
                                     RenderableDatasetConfig configuredTreeDatasets, RenderableStore backend ) {

        ModelBackendInfo info = new ModelBackendInfo();
        updateBackendInfo( info, (ModelBackend<?>) backend, ModelBackend.Type.TREE );
        Envelope dsEnvelope = info.getDatasetEnvelope();
        if ( dsEnvelope == null ) {
            dsEnvelope = createDefaultEnvelope( toLocalCRS, sceneEnvelope.getCoordinateSystem() );
        }

        // RB: Todo configure this value.
        int numberOfObjectsInLeaf = 250;
        if ( configuredTreeDatasets != null ) {
            TreeRenderer configuredRender = new TreeRenderer( dsEnvelope, numberOfObjectsInLeaf,
                                                              configuredTreeDatasets.getMaxPixelError() );
            backend.loadEntities( configuredRender, sceneEnvelope.getCoordinateSystem() );

            dsEnvelope = configuredRender.getValidDomain();
            sceneEnvelope = mergeGlobalWithScene( toLocalCRS, dsEnvelope, sceneEnvelope );
            addConstraint( configuredTreeDatasets.getTitle(), configuredRender, dsEnvelope );
        }
        return sceneEnvelope;
    }

    /**
     * @param toLocalCRS
     * @param parentBBox
     * @param mb
     * @return
     */
    private Envelope initEntities( Envelope sceneEnvelope, double[] toLocalCRS,
                                   RenderableDatasetConfig configuredBuildingsDS, RenderableStore backend ) {
        ModelBackendInfo info = new ModelBackendInfo();
        updateBackendInfo( info, (ModelBackend<?>) backend, ModelBackend.Type.BUILDING );
        updateBackendInfo( info, (ModelBackend<?>) backend, ModelBackend.Type.PROTOTYPE );
        Envelope dsEnvelope = info.getDatasetEnvelope();
        if ( dsEnvelope == null ) {
            dsEnvelope = createDefaultEnvelope( toLocalCRS, sceneEnvelope.getCoordinateSystem() );
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

        backend.loadEntities( allBuildings, sceneEnvelope.getCoordinateSystem() );
        List<RenderablePrototype> mbPrototypes = backend.loadProtoTypes( geometryBuffer,
                                                                         sceneEnvelope.getCoordinateSystem() );
        if ( mbPrototypes != null && !mbPrototypes.isEmpty() ) {
            prototypes.addAll( mbPrototypes );
        }

        // Add the prototypes to the pool.
        for ( RenderablePrototype rp : prototypes ) {
            if ( rp != null ) {
                PrototypePool.addPrototype( rp.getId(), rp );
            }
        }

        dsEnvelope = allBuildings.getValidDomain();
        sceneEnvelope = mergeGlobalWithScene( toLocalCRS, dsEnvelope, sceneEnvelope );
        addConstraint( configuredBuildingsDS.getTitle(), allBuildings, dsEnvelope );
        return sceneEnvelope;
    }

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
     * 
     * @param datatype
     * @param parentMaxPixelError
     */
    private void clarifyInheritance( RenderableDatasetConfig datatype, Double parentMaxPixelError ) {
        datatype.setMaxPixelError( clarifyMaxPixelError( parentMaxPixelError, datatype.getMaxPixelError() ) );
    }

    /**
     * @param translationVector
     * @param coordinateSystem
     * @return a default 3d envelope at 0,0 from the scene.
     */
    private static Envelope createDefaultEnvelope( double[] translationVector, CRS coordinateSystem ) {
        return geomFac.createEnvelope( new double[] { 0, 0, 0 }, new double[] { DEFAULT_SPAN, DEFAULT_SPAN,
                                                                               DEFAULT_SPAN }, coordinateSystem );
    }

    /**
     * @param toLocalCRS
     * @param dsEnvelope
     * @param sceneEnvelope
     * @return the merged scene envelope (in realworld coordinates).
     */
    private Envelope mergeGlobalWithScene( double[] toLocalCRS, Envelope dsEnvelope, Envelope sceneEnvelope ) {
        if ( dsEnvelope != null && ( Math.abs( dsEnvelope.getSpan0() - RenderableDataset.DEFAULT_SPAN ) > 1E-8 ) ) {

            // convert the global dataset (in wpvs world coordinates) to real world coordinates.
            double[] min = dsEnvelope.getMin().getAsArray();
            double[] max = dsEnvelope.getMax().getAsArray();
            double[] tMin = Arrays.copyOf( min, min.length );
            double[] tMax = Arrays.copyOf( max, max.length );
            tMin[0] += ( -toLocalCRS[0] );
            tMin[1] += ( -toLocalCRS[1] );
            tMax[0] += ( -toLocalCRS[0] );
            tMax[1] += ( -toLocalCRS[1] );
            Envelope tEnv = geomFac.createEnvelope( tMin, tMax, dsEnvelope.getCoordinateSystem() );
            sceneEnvelope = sceneEnvelope.merge( tEnv );
        }
        return sceneEnvelope;
    }

}
