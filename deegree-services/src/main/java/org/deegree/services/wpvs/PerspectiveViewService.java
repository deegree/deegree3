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

package org.deegree.services.wpvs;

import java.awt.image.BufferedImage;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.media.opengl.GLPbuffer;

import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.commons.utils.nio.DirectByteBufferPool;
import org.deegree.commons.utils.nio.PooledByteBuffer;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.cs.CRS;
import org.deegree.cs.components.Axis;
import org.deegree.cs.components.Unit;
import org.deegree.cs.coordinatesystems.CoordinateSystem;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.GeometryFactory;
import org.deegree.rendering.r3d.ViewParams;
import org.deegree.rendering.r3d.opengl.rendering.RenderContext;
import org.deegree.rendering.r3d.opengl.rendering.dem.Colormap;
import org.deegree.rendering.r3d.opengl.rendering.dem.manager.TerrainRenderingManager;
import org.deegree.rendering.r3d.opengl.rendering.dem.manager.TextureManager;
import org.deegree.rendering.r3d.opengl.rendering.model.manager.RenderableManager;
import org.deegree.rendering.r3d.opengl.rendering.model.texture.TexturePool;
import org.deegree.services.controller.exception.ControllerException;
import org.deegree.services.controller.ows.OWSException;
import org.deegree.services.exception.ServiceInitException;
import org.deegree.services.jaxb.wpvs.Copyright;
import org.deegree.services.jaxb.wpvs.DatasetDefinitions;
import org.deegree.services.jaxb.wpvs.ServiceConfiguration;
import org.deegree.services.jaxb.wpvs.SkyImages;
import org.deegree.services.jaxb.wpvs.TranslationToLocalCRS;
import org.deegree.services.jaxb.wpvs.Copyright.Image;
import org.deegree.services.jaxb.wpvs.SkyImages.SkyImage;
import org.deegree.services.wpvs.config.ColormapDataset;
import org.deegree.services.wpvs.config.DEMDataset;
import org.deegree.services.wpvs.config.DEMTextureDataset;
import org.deegree.services.wpvs.config.Dataset;
import org.deegree.services.wpvs.config.RenderableDataset;
import org.deegree.services.wpvs.controller.getview.GetView;
import org.deegree.services.wpvs.rendering.jogl.ConfiguredOpenGLInitValues;
import org.deegree.services.wpvs.rendering.jogl.GLPBufferPool;
import org.deegree.services.wpvs.rendering.jogl.GetViewRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Performs the setup of a {@link Dataset}s from a configuration document and provides the {@link #getImage(GetView)}
 * method for retrieving rendered images.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class PerspectiveViewService {

    private final static Logger LOG = LoggerFactory.getLogger( PerspectiveViewService.class );

    private final static GeometryFactory geomFactory = new GeometryFactory();

    private final ServiceConfiguration serviceConfiguration;

    private DirectByteBufferPool textureByteBufferPool;

    private RenderableDataset renderableDatasets;

    private DEMTextureDataset textureDatasets;

    private ColormapDataset colormapDatasets;

    private DEMDataset demDatasets;

    private double[] translationToLocalCRS;

    private CRS defaultCRS;

    private GLPbuffer offscreenBuffer;

    private TerrainRenderingManager defaultDEMRenderer;

    private String copyrightKey;

    private double copyrighScale;

    private DirectByteBufferPool resultImagePool;

    private final int resultImageSize;

    private final int maxRequestWidth;

    private final int maxRequestHeight;

    private final int maxTextureSize;

    private final GLPBufferPool pBufferPool;

    private ConfiguredOpenGLInitValues configuredOpenGLInitValues;

    private final double nearClippingPlane;

    private final double farClippingPlane;

    private Envelope boundingBox;

    private double latitudeOfScene;

    private DeegreeWorkspace workspace;

    /**
     * Creates a new {@link PerspectiveViewService} from the given parameters.
     * 
     * @param configAdapter
     *            needed for the resolving of any relative urls in the configuration documents
     * @param sc
     *            the service configuration created with jaxb
     * @param workspace
     *            the workspace used to load data
     * @throws ServiceInitException
     */
    public PerspectiveViewService( XMLAdapter configAdapter, ServiceConfiguration sc, DeegreeWorkspace workspace )
                            throws ServiceInitException {
        DatasetDefinitions dsd = sc.getDatasetDefinitions();
        serviceConfiguration = sc;
        this.nearClippingPlane = ( sc.getNearClippingPlane() == null ) ? 0.1 : sc.getNearClippingPlane();
        this.farClippingPlane = ( sc.getMaxRequestFarClippingPlane() == null ) ? 100000
                                                                              : sc.getMaxRequestFarClippingPlane();
        pBufferPool = new GLPBufferPool( 1, serviceConfiguration.getMaxViewWidth(),
                                         serviceConfiguration.getMaxViewHeight() );
        this.maxRequestWidth = pBufferPool.getMaxWidth();
        this.maxRequestHeight = pBufferPool.getMaxHeight();
        this.maxTextureSize = pBufferPool.getMaxTextureSize();
        this.resultImageSize = maxRequestHeight * maxRequestWidth * 3;
        this.workspace = workspace;
        resultImagePool = new DirectByteBufferPool(
                                                    resultImageSize
                                                                            * ( sc.getNumberOfResultImageBuffers() == null ? 25
                                                                                                                          : sc.getNumberOfResultImageBuffers() ),
                                                    "pvs" );
        this.latitudeOfScene = sc.getLatitudeOfScene() == null ? 51.7 : sc.getLatitudeOfScene();
        if ( dsd != null ) {
            initValuesFromDatasetDefinitions( dsd );
            this.boundingBox = initDatasets( configAdapter, sc, dsd );
            LOG.debug( "The scene envelope after loading all datasets: {} ", this.boundingBox );
            initBackgroundImages( configAdapter );
            initCopyright( configAdapter );
            initGL();
        } else {
            throw new ServiceInitException( "Datasetdefinitions must be provided." );
        }

    }

    // http://localhost:8080/services/services?service=WPVS&request=GetView&version=0.4.0&crs=epsg:31466&ELEVATIONMODEL=Elevation&OUTPUTFORMAT=image%2Fjpeg&EXCEPTIONS=application/vnd.ogc.se_xml&ROLL=0&SPLITTER=QUAD&Boundingbox=2579816.5%2C5616304.5%2C2582519.5%2C5619007.5&DATETIME=2006-06-21T12:30:00&YAW=73&PITCH=5&DISTANCE=442&AOV=60&SCALE=1.0&BACKGROUND=cirrus&WIDTH=800&HEIGHT=600&BACKGROUNDCOLOR=0xc6d6e5&datasets=buildings,trees,aerophoto-2007&POI=2589778,5621865,166

    /**
     * @param configAdapter
     */
    private void initCopyright( XMLAdapter configAdapter ) {
        if ( serviceConfiguration.getCopyright() != null ) {
            Copyright copy = serviceConfiguration.getCopyright();
            Image image = copy.getImage();
            if ( image != null ) {
                String url = image.getUrl();
                if ( url != null && !"".equals( url ) ) {
                    try {
                        URL resolved = configAdapter.resolve( url );
                        File f = new File( resolved.toURI() );
                        this.copyrightKey = TexturePool.addTexture( f );
                        Double cs = copy.getPercentageOfResult();
                        if ( cs != null ) {
                            // this.copyrighScale = copy.getPercentageOfResult() * 0.01;
                            LOG.warn( "Copyright scaling will be ignored, please make your copyright image your preferred size." );
                        }
                    } catch ( MalformedURLException e ) {
                        LOG.error( "Unable to load copyright image from: " + url + " because: "
                                   + e.getLocalizedMessage() );
                        LOG.trace( "Stack trace:", e );
                    } catch ( URISyntaxException e ) {
                        LOG.error( "Unable to load copyright image from: " + url + " because: "
                                   + e.getLocalizedMessage() );
                        LOG.trace( "Stack trace:", e );
                    }

                }
            }
        }
    }

    /**
     * @param dsd
     * @throws ServiceInitException
     */
    private void initValuesFromDatasetDefinitions( DatasetDefinitions dsd )
                            throws ServiceInitException {
        defaultCRS = new CRS( dsd.getBaseCRS() );
        if ( defaultCRS == null ) {
            throw new ServiceInitException( "A default crs must be given." );
        }
        try {
            CoordinateSystem crs = defaultCRS.getWrappedCRS();
            if ( crs != null ) {
                Axis[] axis = crs.getAxis();
                if ( axis == null || axis.length == 0 ) {
                    throw new ServiceInitException( "The crs with code: " + crs.getCode()
                                                    + " does not have any axis. Hence it is invalid." );
                }
                if ( !axis[0].getUnits().canConvert( Unit.METRE ) ) {
                    throw new ServiceInitException(
                                                    "The crs with code: "
                                                                            + crs.getCode()
                                                                            + " is not based on a Metric system (projected crs), the WPVS only supports base types of metric coordinate systems." );
                }
            }

        } catch ( UnknownCRSException e ) {
            LOG.debug( "No crs: ", e );
            throw new ServiceInitException( e.getLocalizedMessage(), e );
        }

        TranslationToLocalCRS translationToLocalCRS = dsd.getTranslationToLocalCRS();
        if ( translationToLocalCRS != null ) {
            this.translationToLocalCRS = new double[] { translationToLocalCRS.getX(), translationToLocalCRS.getY() };
        } else {
            this.translationToLocalCRS = new double[] { 0, 0 };
        }
        // translateDDSBBox( dsd );
    }

    /**
     * @param configAdapter
     */
    private void initBackgroundImages( XMLAdapter configAdapter ) {
        SkyImages images = serviceConfiguration.getSkyImages();
        if ( images != null ) {
            List<SkyImage> skyImage = images.getSkyImage();
            if ( !skyImage.isEmpty() ) {
                for ( SkyImage image : skyImage ) {
                    if ( image != null && image.getName() != null && image.getFile() != null ) {
                        String name = image.getName();
                        String file = image.getFile();
                        try {
                            URL fileURL = configAdapter.resolve( file );
                            File f = new File( fileURL.toURI() );
                            TexturePool.addTexture( name, f );
                        } catch ( MalformedURLException e ) {
                            LOG.error( "Unable to load sky image: " + name + " because: " + e.getLocalizedMessage() );
                            LOG.trace( "Stack trace:", e );
                        } catch ( URISyntaxException e ) {
                            LOG.error( "Unable to load sky image: " + name + " because: " + e.getLocalizedMessage() );
                            LOG.trace( "Stack trace:", e );
                        }
                    }
                }
            }
        }
    }

    /**
     * @param dsd
     * @throws ServiceInitException
     */
    private Envelope initDatasets( XMLAdapter configAdapter, ServiceConfiguration sc, DatasetDefinitions dsd )
                            throws ServiceInitException {
        // create a minimal bounding box
        Envelope sceneEnvelope = geomFactory.createEnvelope(
                                                             new double[] { -this.translationToLocalCRS[0],
                                                                           -this.translationToLocalCRS[1], 0 },
                                                             new double[] {
                                                                           -this.translationToLocalCRS[0]
                                                                                                   + RenderableDataset.DEFAULT_SPAN,
                                                                           -this.translationToLocalCRS[1]
                                                                                                   + RenderableDataset.DEFAULT_SPAN,
                                                                           RenderableDataset.DEFAULT_SPAN }, defaultCRS );
        renderableDatasets = new RenderableDataset();
        sceneEnvelope = renderableDatasets.fillFromDatasetDefinitions( sceneEnvelope, this.translationToLocalCRS,
                                                                       configAdapter, dsd );

        LOG.debug( "The scene envelope after loading the renderables: {} ", sceneEnvelope );

        LOG.debug( "The scene envelope after loading the trees: {} ", sceneEnvelope );

        int noDFC = sc.getNumberOfDEMFragmentsCached() == null ? 1000 : sc.getNumberOfDEMFragmentsCached();
        int dIOM = sc.getDirectIOMemory() == null ? 500 : sc.getDirectIOMemory();
        demDatasets = new DEMDataset( noDFC, dIOM, ConfiguredOpenGLInitValues.getTerrainAmbient(),
                                      ConfiguredOpenGLInitValues.getTerrainDiffuse(),
                                      ConfiguredOpenGLInitValues.getTerrainSpecular(),
                                      ConfiguredOpenGLInitValues.getTerrainShininess() );
        sceneEnvelope = demDatasets.fillFromDatasetDefinitions( sceneEnvelope, this.translationToLocalCRS,
                                                                configAdapter, dsd );

        LOG.debug( "The scene envelope after loading the dem: {} ", sceneEnvelope );

        List<TerrainRenderingManager> matchingDatasourceObjects = demDatasets.getMatchingDatasourceObjects(
                                                                                                            demDatasets.datasetTitles(),
                                                                                                            null );
        if ( matchingDatasourceObjects.isEmpty() ) {
            throw new ServiceInitException( "No elevationmodels configured, this may not be." );
        }
        defaultDEMRenderer = matchingDatasourceObjects.get( 0 );

        // the colormap
        this.colormapDatasets = new ColormapDataset();
        this.colormapDatasets.fillFromDatasetDefinitions( sceneEnvelope, translationToLocalCRS, configAdapter, dsd );

        int dTM = sc.getDirectTextureMemory() == null ? 400 : sc.getDirectTextureMemory();
        textureByteBufferPool = new DirectByteBufferPool( dTM * 1024 * 1024, "texture coordinates buffer pool." );
        int tIG = sc.getTexturesInGPUMem() == null ? 300 : sc.getTexturesInGPUMem();
        int cTT = sc.getCachedTextureTiles() == null ? 400 : sc.getCachedTextureTiles();
        textureDatasets = new DEMTextureDataset( textureByteBufferPool, cTT, tIG, workspace );
        sceneEnvelope = textureDatasets.fillFromDatasetDefinitions( sceneEnvelope, this.translationToLocalCRS,
                                                                    configAdapter, dsd );

        return sceneEnvelope;
    }

    private void initGL() {
        int usedTextureUnits = Math.max( 8, textureDatasets.size() );
        configuredOpenGLInitValues = new ConfiguredOpenGLInitValues( usedTextureUnits );
        offscreenBuffer = this.pBufferPool.getOffscreenBuffer( null );
        offscreenBuffer.addGLEventListener( configuredOpenGLInitValues );
        // call display to make the configured opengl values active.
        try {
            offscreenBuffer.display();
        } catch ( Throwable t ) {
            LOG.debug( "Error while initializing opengl values stack track.", t );
            LOG.error( "Error while initializing opengl values (may not be important): " + t.getLocalizedMessage() );
        }

    }

    /**
     * @return the configured max far clipping plane.
     */
    public double getFarClippingPlane() {
        return farClippingPlane;
    }

    /**
     * @return the serviceConfiguration
     */
    public final ServiceConfiguration getServiceConfiguration() {
        return serviceConfiguration;
    }

    /**
     * @param datasets
     * @param viewParams
     * @return the first matching colormap.
     */
    public Colormap getColormap( List<String> datasets, ViewParams viewParams ) {
        List<Colormap> matchingDatasourceObjects = colormapDatasets.getMatchingDatasourceObjects( datasets, viewParams );
        Colormap result = null;
        if ( !matchingDatasourceObjects.isEmpty() ) {
            result = matchingDatasourceObjects.get( 0 );
        }
        return result;
    }

    /**
     * @return all building Renderers (independent of dataset name).
     */
    public List<RenderableManager<?>> getAllRenderableRenderers() {
        return renderableDatasets.getAllDatasourceObjects();
    }

    /**
     * @param datasetNames
     * @param viewParams
     * @return all modelRenderers which intersect with the given parameters and have the given names.
     */
    public List<RenderableManager<?>> getBuildingRenderers( Collection<String> datasetNames, ViewParams viewParams ) {
        return renderableDatasets.getMatchingDatasourceObjects( datasetNames, viewParams );
    }

    /**
     * @param viewParams
     * @return all modelRenderers (independent of dataset name) which intersect with the given parameters.
     */
    public List<RenderableManager<?>> getRenderableRenderers( ViewParams viewParams ) {
        return getBuildingRenderers( renderableDatasets.datasetTitles(), viewParams );
    }

    /**
     * @param datasetNames
     * @param viewParams
     * @return all Texture Managers which intersect with the given parameters and have the given names.
     */
    public List<TextureManager> getTextureManagers( Collection<String> datasetNames, ViewParams viewParams ) {
        return textureDatasets.getMatchingDatasourceObjects( datasetNames, viewParams );
    }

    /**
     * @param viewParams
     * @return all texture managers (independent of dataset name) which intersect with the given parameters.
     */
    public List<TextureManager> getTextureManagers( ViewParams viewParams ) {
        return getTextureManagers( textureDatasets.datasetTitles(), viewParams );
    }

    /**
     * @return all configured colormap datasets.
     */
    public ColormapDataset getColormapDatasets() {
        return colormapDatasets;
    }

    /**
     * @return the configured texture datasets.
     */
    public DEMTextureDataset getTextureDataSets() {
        return this.textureDatasets;
    }

    /**
     * @param elevationModelName
     * @param viewParams
     * @return all Texture Managers which intersect with the given parameters and have the given names.
     */
    public List<TerrainRenderingManager> getDEMRenderers( String elevationModelName, ViewParams viewParams ) {
        List<String> elevNames = new ArrayList<String>( 1 );
        elevNames.add( elevationModelName );
        return demDatasets.getMatchingDatasourceObjects( elevNames, viewParams );
    }

    /**
     * Renders an image for requested datasets.
     * 
     * @param request
     *            encapsulates the view parameters and the requested datasets, must not be <code>null</code>
     * @return the rendered image, never <code>null</code>
     * @throws OWSException
     */
    public final BufferedImage getImage( GetView request )
                            throws OWSException {

        ViewParams viewParams = request.getViewParameters();
        LOG.debug( "Requested datasets: " + request.getDatasets() );
        updateMaxWidthAndHeight( viewParams );
        TerrainRenderingManager demRenderer = defaultDEMRenderer;
        List<TextureManager> textureManagers = getTextureManagers( request.getDatasets(), viewParams );
        LOG.debug( "Texturemanagers: " + textureManagers );
        List<RenderableManager<?>> buildingRenders = getBuildingRenderers( request.getDatasets(), viewParams );
        LOG.debug( "Buildings : " + buildingRenders );
        Colormap colormap = getColormap( request.getDatasets(), viewParams );
        LOG.debug( "Colormap: " + colormap );
        PooledByteBuffer imageBuffer = this.resultImagePool.allocate( resultImageSize );
        RenderContext context = new RenderContext( viewParams, request.getSceneParameters().getScale(),
                                                   this.maxTextureSize,
                                                   configuredOpenGLInitValues.getCompositingTextureShaderPrograms() );
        GetViewRenderer renderer = new GetViewRenderer( request, context, imageBuffer, demRenderer, colormap,
                                                        textureManagers, buildingRenders, this.copyrightKey,
                                                        this.copyrighScale, this.latitudeOfScene );

        synchronized ( offscreenBuffer ) {
            offscreenBuffer.addGLEventListener( renderer );
            try {
                offscreenBuffer.display();
            } catch ( Throwable t ) {
                LOG.debug( "An eroor occurred while rendering the scene.", t );
                throw new OWSException( "An error occurred while rendering the GetView requested scene: "
                                        + t.getLocalizedMessage(), t, ControllerException.NO_APPLICABLE_CODE );
            } finally {
                offscreenBuffer.removeGLEventListener( renderer );
            }

            offscreenBuffer.notifyAll();
        }
        this.resultImagePool.deallocate( imageBuffer );
        return renderer.getResultImage();
    }

    /**
     * Update the width and height to the configured ones.
     * 
     * @param viewParams
     * @throws OWSException
     */
    private void updateMaxWidthAndHeight( ViewParams viewParams )
                            throws OWSException {
        int width = viewParams.getScreenPixelsX();
        int height = viewParams.getScreenPixelsY();
        if ( width > this.maxRequestWidth || height > this.maxRequestHeight ) {
            StringBuilder errorMessage = new StringBuilder( "Requested" );
            if ( width > this.maxRequestWidth ) {
                errorMessage.append( " width: " ).append( width ).append( " exceeds maximum request width: " ).append(
                                                                                                                       maxRequestWidth );
            }
            if ( height > this.maxRequestHeight ) {
                if ( width > this.maxRequestWidth ) {
                    errorMessage.append( "," );
                }
                errorMessage.append( " height: " ).append( height ).append( " exceeds maximum request height: " ).append(
                                                                                                                          maxRequestHeight );
            }
            throw new OWSException( errorMessage.toString(), OWSException.INVALID_PARAMETER_VALUE );
            // double scale = ( width > height ) ? ( ( (double) this.maxRequestWidth ) / width )
            // : ( ( (double) this.maxRequestHeight ) / height );
            // viewParams.setScreenDimensions( (int) Math.floor( width * scale ), (int) Math.floor( height * scale ) );
        }

    }

    /**
     * @return the configured translation vector.
     */
    public double[] getTranslationVector() {
        return this.translationToLocalCRS;
    }

    /**
     * @return the configured near clippingplane.
     */
    public double getNearClippingPlane() {
        return nearClippingPlane;
    }

    /**
     * @return the copyrightKey
     */
    public final String getCopyrightKey() {
        return copyrightKey;
    }

    /**
     * @return the default terrain renderer.
     */
    public TerrainRenderingManager getDefaultDEMRenderer() {
        return defaultDEMRenderer;
    }

    /**
     * @return all available texture managers.
     */
    public TextureManager[] getAllTextureManagers() {
        List<TextureManager> managers = this.textureDatasets.getAllDatasourceObjects();
        return managers.toArray( new TextureManager[managers.size()] );
    }

    /**
     * @return the configured dems
     */
    public DEMDataset getDEMDatasets() {
        return this.demDatasets;
    }
}
