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
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.stream.XMLInputFactory;

import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.commons.utils.nio.DirectByteBufferPool;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.coverage.AbstractCoverage;
import org.deegree.coverage.persistence.CoverageBuilderManager;
import org.deegree.coverage.raster.AbstractRaster;
import org.deegree.coverage.raster.MultiResolutionRaster;
import org.deegree.coverage.raster.SimpleRaster;
import org.deegree.coverage.raster.TiledRaster;
import org.deegree.coverage.raster.cache.RasterCache;
import org.deegree.feature.persistence.FeatureStore;
import org.deegree.feature.persistence.FeatureStoreException;
import org.deegree.feature.persistence.FeatureStoreManager;
import org.deegree.geometry.Envelope;
import org.deegree.rendering.r2d.se.parser.SymbologyParser;
import org.deegree.rendering.r2d.se.unevaluated.Style;
import org.deegree.rendering.r3d.opengl.rendering.dem.manager.TextureManager;
import org.deegree.rendering.r3d.opengl.rendering.dem.manager.TextureTileManager;
import org.deegree.rendering.r3d.opengl.rendering.dem.texturing.RasterAPITextureTileProvider;
import org.deegree.rendering.r3d.opengl.rendering.dem.texturing.StyledGeometryTTProvider;
import org.deegree.rendering.r3d.opengl.rendering.dem.texturing.TextureTileProvider;
import org.deegree.services.jaxb.wpvs.DEMTextureDatasetConfig;
import org.deegree.services.jaxb.wpvs.DatasetDefinitions;
import org.deegree.services.jaxb.wpvs.StyledGeometryProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The <code>TextureDatasetWrapper</code> extracts data from jaxb configuration elements and creates texture managers,
 * from them. Following hierarchy is used: a {@link TextureManager} holds a {@link TextureTileManager} which can hold
 * one or more {@link TextureTileProvider}.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class DEMTextureDataset extends Dataset<TextureManager> {

    private final static Logger LOG = LoggerFactory.getLogger( DEMTextureDataset.class );

    private final int maxCachedTextureTiles;

    private final DirectByteBufferPool textureByteBufferPool;

    private final int maxTexturesInGPU;

    private final DeegreeWorkspace workspace;
    
    /**
     * 
     * @param textureByteBufferPool
     * @param maxTexturesInGPU
     *            the number of textures in gpu cache
     * @param maxCachedTextureTiles
     *            the number of texture tiles in cache.
     * @param workspace the workspace to be used to load data
     */
    public DEMTextureDataset( DirectByteBufferPool textureByteBufferPool, int maxTexturesInGPU,
                              int maxCachedTextureTiles, DeegreeWorkspace workspace ) {
        // super( sceneEnvelope, translationToLocalCRS, configAdapter );
        this.textureByteBufferPool = textureByteBufferPool;
        this.maxTexturesInGPU = maxTexturesInGPU;
        this.maxCachedTextureTiles = maxCachedTextureTiles;
        this.workspace = workspace;
    }

    @Override
    public Envelope fillFromDatasetDefinitions( Envelope sceneEnvelope, double[] toLocalCRS, XMLAdapter configAdapter,
                                                DatasetDefinitions dsd ) {
        List<DEMTextureDatasetConfig> demTextureDatsets = dsd.getDEMTextureDataset();
        if ( !demTextureDatsets.isEmpty() ) {
            sceneEnvelope = initDatasets( demTextureDatsets, sceneEnvelope, toLocalCRS, dsd.getMaxPixelError(),
                                          configAdapter );
        } else {
            LOG.info( "No texture dataset has been configured." );
        }
        return sceneEnvelope;
    }

    private Envelope initDatasets( List<DEMTextureDatasetConfig> textureDatasets, Envelope sceneEnvelope,
                                   double[] toLocalCRS, Double parentMaxPixelError, XMLAdapter adapter ) {
        if ( textureDatasets != null && !textureDatasets.isEmpty() ) {
            for ( DEMTextureDatasetConfig dts : textureDatasets ) {
                if ( dts != null ) {
                    if ( isUnAmbiguous( dts.getTitle() ) ) {
                        LOG.info( "The feature dataset with name: " + dts.getName() + " and title: " + dts.getTitle()
                                  + " had multiple definitions in your service configuration." );
                    } else {
                        clarifyInheritance( dts, parentMaxPixelError );
                        try {
                            sceneEnvelope = handleTextureDataset( dts, sceneEnvelope, toLocalCRS, adapter );
                        } catch ( IOException e ) {
                            LOG.error( "Failed to initialize configured demTexture dataset: " + dts.getName() + ": "
                                       + dts.getTitle() + " because: " + e.getLocalizedMessage(), e );
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
    private void clarifyInheritance( DEMTextureDatasetConfig datatype, Double parentMaxPixelError ) {
        datatype.setMaxPixelError( clarifyMaxPixelError( parentMaxPixelError, datatype.getMaxPixelError() ) );
    }

    /**
     * @param adapter
     * @param toLocalCRS
     * @param sceneEnvelope
     * @throws IOException
     */
    private Envelope handleTextureDataset( DEMTextureDatasetConfig textureDataset, Envelope sceneEnvelope,
                                           double[] toLocalCRS, XMLAdapter adapter )
                            throws IOException {

        List<TextureTileProvider> tileProviders = new ArrayList<TextureTileProvider>();
        Envelope datasetEnvelope = null;
        if ( textureDataset.getCoverageStoreId() != null ) {
            datasetEnvelope = fillFromCoverage( textureDataset.getCoverageStoreId(), tileProviders );
        } else if ( textureDataset.getStyledGeometryProvider() != null ) {
            datasetEnvelope = fillFromStyledGeometries( textureDataset.getStyledGeometryProvider(), tileProviders,
                                                        sceneEnvelope, toLocalCRS, adapter );
        } else {
            LOG.warn( "No texture dataset found for texture dataset: " + textureDataset.getTitle() );
            return sceneEnvelope;
        }

        // WMSDataSourceType levelSource = (WMSDataSourceType) ds;
        // URL capabilitiesURL = new URL( levelSource.getCapabilitiesDocumentLocation().getLocation() );
        // int maxWidth = -1;
        // int maxHeight = -1;
        // if ( levelSource.getMaxMapDimensions() != null ) {
        // maxWidth = levelSource.getMaxMapDimensions().getWidth().intValue();
        // maxHeight = levelSource.getMaxMapDimensions().getWidth().intValue();
        // }
        // int requestTimeout = -1;
        // if ( levelSource.getRequestTimeout() != null ) {
        // requestTimeout = levelSource.getRequestTimeout().intValue();
        // }
        // String requestFormat = "image/png";
        // boolean transparent = false;
        // if ( levelSource.getRequestedFormat() != null ) {
        // requestFormat = levelSource.getRequestedFormat().getValue();
        // transparent = levelSource.getRequestedFormat().isTransparent();
        // }
        // String[] layers = new String[] { levelSource.getRequestedLayers() };
        // // TODO how to handle differing CRS
        // // TODO remove hard code crs reference (But note: 'EPSG' must be in uppercase, which differs
        // // from
        // // getDefaultCRS())
        //
        // // rb: don't get the envelope, because wms's tend to have wrong or falsely specified
        // // boundingboxes.
        // try {
        // ttProv = new WMSTextureTileProvider( capabilitiesURL, layers,
        // sceneEnvelope.getCoordinateSystem(), requestFormat,
        // transparent, res.getRes(), maxWidth, maxHeight,
        // requestTimeout );
        // } catch ( Exception e ) {
        // LOG.warn( "Could not create wms dataset from " + capabilitiesURL + " because: " + e );
        // }
        // } else {
        // StyledGeometryProvider ctDS = textureDataset.getStyledGeometryProvider();
        //
        // // TODO configure / lookup feature store from FeatureStoreManager by id!!
        //

        if ( !tileProviders.isEmpty() ) {
            TextureTileManager tileManager = new TextureTileManager(
                                                                     tileProviders.toArray( new TextureTileProvider[tileProviders.size()] ),
                                                                     maxCachedTextureTiles );

            TextureManager result = new TextureManager( this.textureByteBufferPool, tileManager, toLocalCRS,
                                                        maxTexturesInGPU, textureDataset.getRequestTimeout() );

            // these dataset envelopes are in realworld 2d coordinates
            if ( datasetEnvelope != null ) {
                if ( datasetEnvelope.getCoordinateDimension() == 2 ) {
                    double[] min = datasetEnvelope.getMin().getAsArray();
                    double[] max = datasetEnvelope.getMax().getAsArray();
                    double[] tMin = Arrays.copyOf( min, 3 );
                    double[] tMax = Arrays.copyOf( max, 3 );
                    tMin[2] = sceneEnvelope.getMin().get2();
                    tMax[2] = sceneEnvelope.getMax().get2();
                    datasetEnvelope = geomFac.createEnvelope( tMin, tMax, datasetEnvelope.getCoordinateSystem() );
                }
                sceneEnvelope = sceneEnvelope.merge( datasetEnvelope );
            }
            // will always have 3d.
            Envelope constraintEnv = datasetEnvelope == null ? sceneEnvelope : datasetEnvelope;
            // adding this constraint will result in never finding it in the scene, because it needs to be converted to
            // local crs... so lets convert.
            double[] min = constraintEnv.getMin().getAsArray();
            double[] max = constraintEnv.getMax().getAsArray();
            double[] tMin = Arrays.copyOf( min, min.length );
            double[] tMax = Arrays.copyOf( max, max.length );
            tMin[0] += toLocalCRS[0];
            tMin[1] += toLocalCRS[1];
            tMax[0] += toLocalCRS[0];
            tMax[1] += toLocalCRS[1];
            Envelope constEnv = geomFac.createEnvelope( tMin, tMax, constraintEnv.getCoordinateSystem() );
            addConstraint( textureDataset.getTitle(), result, constEnv );
        } else {
            LOG.warn( "Ignoring texture dataset: " + textureDataset.getName() + ": " + textureDataset.getTitle()
                      + " because no texture providers could be initialized." );
        }
        return sceneEnvelope;
    }

    /**
     * @param coverageStoreId
     * @param tileProviders
     */
    private Envelope fillFromCoverage( String coverageStoreId, List<TextureTileProvider> tileProviders ) {
        AbstractCoverage coverage = workspace.getCoverageBuilderManager().get( coverageStoreId );
        if ( coverage == null ) {
            LOG.warn( "The coverage builder with id: " + coverageStoreId
                      + " could not create a coverage, ignoring dataset." );
            return null;
        }
        // JAXBElement<? extends AbstractGeospatialDataSourceType> abstractRasterDataSource = null;//
        // textureDataset.getAbstractRasterDataSource();
        // todo init the raster datasource over the coverage manager
        if ( coverage instanceof MultiResolutionRaster ) {
            getTileProviders( (MultiResolutionRaster) coverage, tileProviders );
        } else if ( coverage instanceof TiledRaster || coverage instanceof SimpleRaster ) {
            addTileProvider( (AbstractRaster) coverage, tileProviders );
        }
        return coverage.getEnvelope();
    }

    /**
     * @param styledGeometryProvider
     * @param tileProviders
     * @throws IOException
     */
    private Envelope fillFromStyledGeometries( StyledGeometryProvider styledGeometryProvider,
                                               List<TextureTileProvider> tileProviders, Envelope sceneEnvelope,
                                               double[] toLocalCRS, XMLAdapter adapter )
                            throws IOException {
        // JAXBElement<? extends FeatureStoreType> featureStore = ctDS.getFeatureStore();
        // FeatureStore store = null;
        String featureStoreId = styledGeometryProvider.getFeatureStoreId();

        FeatureStore store = FeatureStoreManager.get( featureStoreId );

        // String styleId = styledGeometryProvider.getStyleId();
        // Styl
        String unresolved = styledGeometryProvider.getStyleId();
        // todo get it from the style manager?
        if ( unresolved == null ) {
            LOG.warn( "The se-style file was not defined, could not create a closeup layer." );
            return null;
        }
        URL styleFile = resolve( adapter, unresolved );
        InputStream styleStream = null;
        Style style;
        try {
            styleStream = styleFile.openStream();
            style = SymbologyParser.INSTANCE.parse( XMLInputFactory.newInstance().createXMLStreamReader(
                                                                                                         styleFile.toExternalForm(),
                                                                                                         styleStream ) );
        } catch ( Exception e ) {
            throw new IOException( "Could not read symbology encoding file because: " + e.getLocalizedMessage(), e );
        } finally {
            if ( styleStream != null ) {
                styleStream.close();
            }
        }

        double unitsPerPixel = styledGeometryProvider.getMinimumUnitsPerPixel();
        org.deegree.services.jaxb.wpvs.StyledGeometryProvider.TextureCacheDir rasterCache = styledGeometryProvider.getTextureCacheDir();
        File cacheDir = RasterCache.DEFAULT_CACHE_DIR;
        double cacheSize = -1;
        if ( rasterCache != null ) {
            String cd = rasterCache.getValue();
            if ( cd != null ) {
                try {
                    URL resolve = adapter.resolve( cd );
                    cd = new File( resolve.toURI() ).toString();
                    cacheDir = new File( resolve.toURI() );
                } catch ( URISyntaxException e ) {
                    LOG.error( "The cache dir '{}' could not be resolved.", cd );
                    LOG.trace( "Stack trace:", e );
                }
            }
            cacheSize = rasterCache.getCacheSize();
        }
        StyledGeometryTTProvider tProv;
        try {
            tProv = new StyledGeometryTTProvider( toLocalCRS, sceneEnvelope.getCoordinateSystem(), store, style,
                                                  unitsPerPixel, cacheDir, Math.round( cacheSize * 1024 * 1024 * 1024 ) );
        } catch ( FeatureStoreException e ) {
            LOG.error( "Error retrieving envelope from FeatureStore: " + e.getMessage(), e );
            throw new IOException( "Error retrieving envelope from FeatureStore: " + e.getMessage(), e );
        }
        tileProviders.add( tProv );
        return tProv.getEnvelope();

    }

    /**
     * @param coverage
     * @param tileProviders
     */
    private void getTileProviders( MultiResolutionRaster coverage, List<TextureTileProvider> tileProviders ) {
        for ( double res : coverage.getResolutions() ) {
            AbstractRaster raster = coverage.getRaster( res );
            addTileProvider( raster, tileProviders );
        }
    }

    private void addTileProvider( AbstractRaster raster, List<TextureTileProvider> tileProviders ) {
        TextureTileProvider ttProv = new RasterAPITextureTileProvider( raster );
        tileProviders.add( ttProv );

    }
}
