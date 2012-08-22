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

package org.deegree.ogcwebservices.wpvs;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;

import javax.imageio.ImageIO;
import javax.media.j3d.Background;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.ImageComponent2D;
import javax.media.j3d.Node;
import javax.media.j3d.OrderedGroup;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;

import org.deegree.framework.concurrent.ExecutionFinishedEvent;
import org.deegree.framework.concurrent.Executor;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.StringTools;
import org.deegree.framework.util.TimeTools;
import org.deegree.i18n.Messages;
import org.deegree.model.crs.CRSFactory;
import org.deegree.model.crs.CRSTransformationException;
import org.deegree.model.crs.CoordinateSystem;
import org.deegree.model.crs.GeoTransformer;
import org.deegree.model.crs.UnknownCRSException;
import org.deegree.model.spatialschema.Envelope;
import org.deegree.model.spatialschema.GeometryException;
import org.deegree.model.spatialschema.Surface;
import org.deegree.model.spatialschema.WKTAdapter;
import org.deegree.ogcbase.ExceptionCode;
import org.deegree.ogcwebservices.InconsistentRequestException;
import org.deegree.ogcwebservices.OGCWebServiceException;
import org.deegree.ogcwebservices.wpvs.capabilities.Dataset;
import org.deegree.ogcwebservices.wpvs.capabilities.ElevationModel;
import org.deegree.ogcwebservices.wpvs.configuration.AbstractDataSource;
import org.deegree.ogcwebservices.wpvs.configuration.WPVSConfiguration;
import org.deegree.ogcwebservices.wpvs.configuration.WPVSDeegreeParams;
import org.deegree.ogcwebservices.wpvs.j3d.OffScreenWPVSRenderer;
import org.deegree.ogcwebservices.wpvs.j3d.TriangleTerrain;
import org.deegree.ogcwebservices.wpvs.j3d.ViewPoint;
import org.deegree.ogcwebservices.wpvs.j3d.WPVSScene;
import org.deegree.ogcwebservices.wpvs.operation.GetView;
import org.deegree.ogcwebservices.wpvs.operation.GetViewResponse;
import org.deegree.ogcwebservices.wpvs.utils.QuadTreeSplitter;
import org.deegree.ogcwebservices.wpvs.utils.ResolutionStripe;
import org.deegree.ogcwebservices.wpvs.utils.StripeFactory;

import com.sun.j3d.utils.image.TextureLoader;

/**
 * Default handler for WPVS GetView requests. This Class is the central position where the {@link GetView} request lands
 * the configured Datasources are gathered and the scene is put together.
 * 
 * @author <a href="mailto:taddei@lat-lon.de">Ugo Taddei</a>
 * @author last edited by: $Author$
 * 
 *         $Revision$, $Date$
 * 
 */
public class DefaultGetViewHandler extends GetViewHandler {

    static private final ILogger LOG = LoggerFactory.getLogger( DefaultGetViewHandler.class );

    private WPVSConfiguration config;

    private URL backgroundImgURL;

    private WPVSScene theScene;

    private OffScreenWPVSRenderer renderer;

    /**
     * Constructor for DefaultGetViewHandler.
     * 
     * @param owner
     *            the service creating this handler
     */
    public DefaultGetViewHandler( WPVService owner ) {
        super( owner );
        this.config = owner.getConfiguration();
    }

    /**
     * This Method handles a clients GetView request by creating the appropriate (configured) Datasources, the
     * {@link ResolutionStripe}s, the requeststripes (which are actually axisalligned Resolutionsripes) and finally
     * putting them all together in a java3d scene. The creation of the Shape3D Objects (by requesting the
     * ResolutionStripe relevant Datasources) is done in separate Threads for each ResolutionStripe (which is in
     * conflict with the deegree styleguides) using the {@link Executor} class.
     * 
     * @see org.deegree.ogcwebservices.wpvs.GetViewHandler#handleRequest(org.deegree.ogcwebservices.wpvs.operation.GetView)
     */
    @Override
    public GetViewResponse handleRequest( final GetView request )
                            throws OGCWebServiceException {

        // request = req;
        long totalTime = System.currentTimeMillis();
        validateImageSize( request );

        List<Dataset> validDatasets = new ArrayList<Dataset>();
        String errorMessage = getValidRequestDatasets( request, validDatasets );
        if ( validDatasets.size() == 0 ) {
            throw new OGCWebServiceException(
                                              StringTools.concat(
                                                                  errorMessage.length() + 100,
                                                                  "Your request yields no results for given reasons:\n",
                                                                  errorMessage ) );

        }

        ElevationModel elevationModel = getValidElevationModel( request.getElevationModel() );
        LOG.logDebug( "Requested elevationModel: " + elevationModel );

        if ( request.getFarClippingPlane() > config.getDeegreeParams().getMaximumFarClippingPlane() ) {
            request.setFarClippingPlane( config.getDeegreeParams().getMaximumFarClippingPlane() );
        }

        ViewPoint viewPoint = new ViewPoint( request, getTerrainHeightAboveSeaLevel( request.getPointOfInterest() ) );
        LOG.logDebug( "Viewpoint: " + viewPoint );

        ArrayList<ResolutionStripe> resolutionStripes = createRequestBoxes( request, viewPoint,
                                                                            config.getSmallestMinimalScaleDenomiator() );
        LOG.logDebug( "Found number of resolutionStripes: " + resolutionStripes.size() );

        if ( resolutionStripes.size() == 0 ) {
            throw new OGCWebServiceException(
                                              StringTools.concat( 200,
                                                                  "There were no RequestBoxes found, therefor this WPVS-request is invalid" ) );

        }

        Surface visibleArea = viewPoint.getVisibleArea();

        if ( LOG.getLevel() == ILogger.LOG_DEBUG ) {
            try {
                LOG.logDebug( "Visible Area:\n" + WKTAdapter.export( visibleArea ).toString() );
            } catch ( GeometryException e1 ) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }

        findValidDataSourcesFromDatasets( validDatasets, resolutionStripes, request.getOutputFormat(), visibleArea );
        LOG.logDebug( "Using validDatasets: " + validDatasets );

        findValidEMDataSourceFromElevationModel( elevationModel, resolutionStripes, visibleArea );
        LOG.logDebug( "Using elevationModel: " + elevationModel );

        // will also check is background is valid, before doing any hard work
        this.backgroundImgURL = createBackgroundImageURL( request );

        LOG.logDebug( "Backgroundurl : " + backgroundImgURL );

        Executor exec = Executor.getInstance();
        List<ExecutionFinishedEvent<ResolutionStripe>> resultingBoxes = null;
        try {
            LOG.logDebug( "Trying to synchronously contact datasources." );
            resultingBoxes = exec.performSynchronously( new ArrayList<Callable<ResolutionStripe>>( resolutionStripes ),
                                                        config.getDeegreeParams().getRequestTimeLimit() );
        } catch ( InterruptedException ie ) {
            throw new OGCWebServiceException(
                                              StringTools.concat( 200,
                                                                  "A Threading-Error occurred while placing your request." ) );

        }

        LOG.logDebug( "All resolutionstripes finished executing." );

        if ( resultingBoxes != null && resultingBoxes.size() > 0 ) {
            // check if some of the resolutionstripes finished execution before the configured max
            // life time.
            int k = 0;
            for ( ExecutionFinishedEvent<ResolutionStripe> efe : resultingBoxes ) {
                try {
                    efe.getResult();
                } catch ( CancellationException ce ) {
                    k++;
                } catch ( Throwable e ) {
                    // not interested in something else
                }
            }
            if ( k == resultingBoxes.size() ) {
                throw new OGCWebServiceException(
                                                  Messages.getMessage(
                                                                       "WPVS_EXCEEDED_REQUEST_TIME",
                                                                       new Double(
                                                                                   config.getDeegreeParams().getRequestTimeLimit() * 0.001 ) ).toString() );
            }
            double dataRetrievalTime = ( System.currentTimeMillis() - totalTime ) / 1000d;
            long creationTime = System.currentTimeMillis();
            theScene = createScene( request, viewPoint, resolutionStripes, visibleArea.getEnvelope() );
            // Get a canvas3D from the pool.
            Canvas3D canvas = WPVSConfiguration.getCanvas3D();
            renderer = new OffScreenWPVSRenderer( canvas, config.getDeegreeParams().getNearClippingPlane(), theScene,
                                                  request.getImageDimension().width,
                                                  request.getImageDimension().height,
                                                  config.getDeegreeParams().isAntialiasingEnabled() );
            double creationT = ( ( System.currentTimeMillis() - creationTime ) / 1000d );
            LOG.logDebug( "Trying to render Scene." );

            long renderTime = System.currentTimeMillis();
            BufferedImage output = renderScene();
            // Release the canvas so it may be reused by another request.
            /**
             * If using the TestWPVS class option NO_NEW_REQUEST, make sure to comment following line as well as the
             * universe.cleanup(); and view.removeAllCanvas3Ds(); lines in the
             * {@link OffscreenWPVSRenderer#renderScene()}.
             */
            WPVSConfiguration.releaseCanvas3D( canvas );
            double renderT = ( ( System.currentTimeMillis() - renderTime ) / 1000d );

            if ( LOG.getLevel() == ILogger.LOG_DEBUG ) {
                StringBuilder sb = new StringBuilder(
                                                      "\n-----------TIMING INFORMATION----------\nThe handling of the request took: " );
                sb.append( ( System.currentTimeMillis() - totalTime ) / 1000d );
                sb.append( " seconds to return." );
                sb.append( "\n- Retreiving data took: " ).append( dataRetrievalTime ).append( "seconds." );
                sb.append( "\n- Creating j3d scene took: " ).append( creationT ).append( "seconds." );
                sb.append( "\n- Rendering the j3d scene took: " ).append( renderT ).append( "seconds." );
                sb.append( "\n--------------------------------------\n\n Outputting all retreived textures for " ).append(
                                                                                                                           resolutionStripes.size() ).append(
                                                                                                                                                              " finished resolutionsSripes." );
                LOG.logDebug( sb.toString() );
                for ( ResolutionStripe stripe : resolutionStripes ) {
                    stripe.outputTextures();
                }
                LOG.logDebug( "Number of resolutionStripes: " + resolutionStripes.size() );
            }

            return new GetViewResponse( output, request.getOutputFormat() );
        }
        return null;
    }

    /**
     * @param elevationModelName
     * @return an elevationModell or <code>null</code> if no name was given
     * @throws OGCWebServiceException
     *             if no elevationModel is found for the given elevationmodelname
     */
    private ElevationModel getValidElevationModel( String elevationModelName )
                            throws OGCWebServiceException {
        ElevationModel resultEMModel = null;
        if ( elevationModelName != null ) {
            resultEMModel = config.findElevationModel( elevationModelName );// dataset.getElevationModel();
            if ( resultEMModel == null ) {
                throw new OGCWebServiceException( StringTools.concat( 150, "ElevationModel '", elevationModelName,
                                                                      "' is not known to the WPVS" ) );
            }
        }
        return resultEMModel;
    }

    /**
     * Finds the datasets which can handle the requested crs's, and check if they are defined inside the requested bbox
     * 
     * @param request
     *            the GetView request
     * @param resultDatasets
     *            a list to which the found datasets will be added.
     * @return an ArrayList containing all the datasets which comply with the requested crs.
     * @throws OGCWebServiceException
     */
    private String getValidRequestDatasets( GetView request, List<Dataset> resultDatasets )
                            throws OGCWebServiceException {
        // ArrayList<Dataset> resultDatasets = new ArrayList<Dataset>();
        Envelope bbox = request.getBoundingBox();
        List<String> datasets = request.getDatasets();
        CoordinateSystem coordSys = request.getCrs();

        // If the BoundingBox request not is
        try {
            if ( !"EPSG:4326".equalsIgnoreCase( coordSys.getFormattedString() ) ) {
                // transform the bounding box of the request to EPSG:4326/WGS 84
                GeoTransformer gt = new GeoTransformer( CRSFactory.create( "EPSG:4326" ) );
                bbox = gt.transform( bbox, coordSys );
            }
        } catch ( CRSTransformationException e ) {
            LOG.logError( e.getMessage(), e );
            throw new OGCWebServiceException( e.getMessage() );
        } catch ( UnknownCRSException e ) {
            LOG.logError( e.getMessage(), e );
            throw new OGCWebServiceException( e.getMessage() );
        }
        StringBuffer errorMessage = new StringBuffer( 1000 );
        for ( String dset : datasets ) {
            Dataset configuredDataset = config.findDataset( dset );
            if ( configuredDataset != null ) {
                CoordinateSystem[] dataSetCRS = configuredDataset.getCrs();
                boolean isCoordSysSupported = false;
                boolean requestIntersectsWithDataset = false;
                for ( CoordinateSystem crs : dataSetCRS ) {
                    if ( crs.equals( coordSys ) ) {
                        isCoordSysSupported = true;
                        // lookslike compatible crs therefor check if bbox intersect
                        if ( configuredDataset.getWgs84BoundingBox().intersects( bbox ) ) {
                            requestIntersectsWithDataset = true;
                            if ( !resultDatasets.contains( configuredDataset ) ) {
                                resultDatasets.add( configuredDataset );
                            }
                        }
                    }
                }
                if ( !isCoordSysSupported ) {
                    String msg = new StringBuilder( "Requested Dataset -" ).append( dset ).append(
                                                                                                   "- does not support requested crs: " ).append(
                                                                                                                                                  coordSys ).append(
                                                                                                                                                                     ".\n" ).toString();
                    LOG.logDebug( msg );
                    errorMessage.append( msg );
                } else if ( !requestIntersectsWithDataset ) {
                    String msg = new StringBuilder( "Requested Dataset -" ).append( dset ).append(
                                                                                                   "- does not intersect with the requested bbox.\n" ).toString();
                    LOG.logDebug( msg );
                    errorMessage.append( msg );
                }
            } else {
                throw new InconsistentRequestException( Messages.getMessage( "WPVS_GETVIEW_INVALID_DATASET", dset ) );
            }
        }

        return errorMessage.toString();
    }

    /**
     * Finds the valid datasources in the configured (and allready checked) datasets.
     * 
     * @param datasets
     *            the datasets which are valid for this request
     */
    private void findValidDataSourcesFromDatasets( List<Dataset> datasets, ArrayList<ResolutionStripe> stripes,
                                                   String outputFormat, Surface visibleArea ) {
        double resolution = 0;
        for ( ResolutionStripe stripe : stripes ) {
            stripe.setOutputFormat( outputFormat );
            resolution = stripe.getMaxResolutionAsScaleDenominator();
            for ( int i = 0; i < datasets.size(); ++i ) {
                Dataset dset = datasets.get( i );
                AbstractDataSource[] dataSources = dset.getDataSources();

                for ( AbstractDataSource ads : dataSources ) {
                    // System.out.println( "AbstractDataSource: " + ads );
                    if ( resolution >= dset.getMinimumScaleDenominator()
                         && resolution < dset.getMaximumScaleDenominator()
                         && resolution >= ads.getMinScaleDenominator() && resolution < ads.getMaxScaleDenominator()
                         && ( ( ads.getValidArea() != null ) ? ads.getValidArea().intersects( visibleArea ) : true ) ) {
                        if ( ads.getServiceType() == AbstractDataSource.LOCAL_WFS
                             || ads.getServiceType() == AbstractDataSource.REMOTE_WFS ) {
                            stripe.addFeatureCollectionDataSource( ads );
                        } else if ( ads.getServiceType() == AbstractDataSource.LOCAL_WMS
                                    || ads.getServiceType() == AbstractDataSource.REMOTE_WMS
                                    || ads.getServiceType() == AbstractDataSource.LOCAL_WCS
                                    || ads.getServiceType() == AbstractDataSource.REMOTE_WCS ) {
                            stripe.addTextureDataSource( ads );
                        }
                    }
                }
            }
        }
    }

    /**
     * @param elevationModel
     *            and it's datasources.
     */
    private void findValidEMDataSourceFromElevationModel( ElevationModel elevationModel,
                                                          ArrayList<ResolutionStripe> stripes, Surface visibleArea ) {
        if ( elevationModel != null ) {
            AbstractDataSource[] emDataSources = elevationModel.getDataSources();
            Dataset dataset = elevationModel.getParentDataset();
            for ( ResolutionStripe stripe : stripes ) {
                double resolution = stripe.getMaxResolutionAsScaleDenominator();// getMaxResolution();
                for ( AbstractDataSource ads : emDataSources ) {
                    if ( resolution >= dataset.getMinimumScaleDenominator()
                         && resolution < dataset.getMaximumScaleDenominator()
                         && resolution >= ads.getMinScaleDenominator() && resolution < ads.getMaxScaleDenominator()
                         && ( ( ads.getValidArea() != null ) ? ads.getValidArea().intersects( visibleArea ) : true ) ) {
                        stripe.setElevationModelDataSource( ads );
                    }
                }
            }
        }
    }

    /**
     * Extracts from the request and the configuration the URL behind teh name of a given BACKGROUND
     * 
     * @return the URL, under which the background image is found
     * @throws OGCWebServiceException
     *             if no URL with the name given by 'BACKGROUND' can be found.
     */
    private URL createBackgroundImageURL( GetView request )
                            throws OGCWebServiceException {

        String imageName = request.getVendorSpecificParameter( "BACKGROUND" );
        URL imgURL = null;
        if ( imageName != null ) {
            imgURL = config.getDeegreeParams().getBackgroundMap().get( imageName );
            if ( imgURL == null ) {
                String s = StringTools.concat( 100, "Cannot find any image referenced", "by parameter BACKGROUND=",
                                               imageName );
                throw new OGCWebServiceException( s );
            }
        }

        return imgURL;
    }

    /**
     * Creates a Java3D Node representing the background.
     * 
     * @param viewPoint
     *            the viewersposition
     * @return a new Node containing a geometry representing the background
     * @throws OGCWebServiceException
     */
    private Node createBackground( ViewPoint viewPoint, GetView request )
                            throws OGCWebServiceException {

        Point3d observer = viewPoint.getObserverPosition();

        Background bg = new Background( new Color3f( request.getBackgroundColor() ) );
        BoundingSphere bounds = new BoundingSphere( observer,
                                                    config.getDeegreeParams().getMaximumFarClippingPlane() * 1.1 );

        bg.setApplicationBounds( bounds );
        try {
            if ( backgroundImgURL != null ) {
                BufferedImage buffImg = ImageIO.read( backgroundImgURL );

                // scale image to fill the whole background
                BufferedImage tmpImg = new BufferedImage( request.getImageDimension().width,
                                                          request.getImageDimension().height, buffImg.getType() );
                Graphics g = tmpImg.getGraphics();
                g.drawImage( buffImg, 0, 0, tmpImg.getWidth() - 1, tmpImg.getHeight() - 1, null );
                g.dispose();

                ImageComponent2D img = new TextureLoader( tmpImg ).getImage();
                bg.setImage( img );
            }
        } catch ( IOException e ) {
            LOG.logError( e.getMessage(), e );
            String s = StringTools.concat( 100, "Could not create backgound image: ", e.getMessage() );
            throw new OGCWebServiceException( s );
        }

        return bg;
    }

    /**
     * Creates the request boxes from the parameters available i teh incoming request.
     * 
     * @param viewPoint
     *            where the viewer is
     * @return a new array of surfaces representing the area in which data will be collected
     */
    private ArrayList<ResolutionStripe> createRequestBoxes( GetView request, ViewPoint viewPoint,
                                                            double smallestMinimalScaleDenominator ) {
        ArrayList<ResolutionStripe> requestStripes = new ArrayList<ResolutionStripe>();
        String splittingMode = request.getVendorSpecificParameter( "SPLITTER" );
        StripeFactory stripesFactory = new StripeFactory( viewPoint, smallestMinimalScaleDenominator );
        int imageWidth = request.getImageDimension().width;
        if ( "BBOX".equals( splittingMode ) ) {
            requestStripes = stripesFactory.createBBoxResolutionStripe(
                                                                        request.getBoundingBox(),
                                                                        imageWidth,
                                                                        getTerrainHeightAboveSeaLevel( viewPoint.getPointOfInterest() ),
                                                                        request.getScale() );
        } else {
            // Calculate the resolution stripes perpendicular to the viewdirection
            requestStripes = stripesFactory.createResolutionStripes(
                                                                     request.getImageDimension().width,
                                                                     getTerrainHeightAboveSeaLevel( viewPoint.getPointOfInterest() ),
                                                                     null, request.getScale() );
            if ( LOG.getLevel() == ILogger.LOG_DEBUG ) {
                StringBuilder sb = new StringBuilder( "The requestStripes (in WKT) before the quadtree: \n" );
                for ( ResolutionStripe stripe : requestStripes ) {
                    try {
                        sb.append( WKTAdapter.export( stripe.getSurface() ) ).append( "\n" );
                    } catch ( GeometryException e ) {
                        LOG.logError( "Error while exporting surface to wkt.", e );
                    }
                }
                LOG.logDebug( sb.toString() );
            }
            QuadTreeSplitter splittree = new QuadTreeSplitter( requestStripes, request.getImageDimension().width,
                                                               config.getDeegreeParams().isRequestQualityPreferred() );
            requestStripes = splittree.getRequestQuads( config.getDeegreeParams().getExtendRequestPercentage(),
                                                        config.getDeegreeParams().getQuadMergeCount() );
            if ( LOG.getLevel() == ILogger.LOG_DEBUG ) {
                StringBuilder sb = new StringBuilder( "The requestStripes (in WKT) after the quadtree: \n" );
                for ( ResolutionStripe stripe : requestStripes ) {
                    try {
                        sb.append( WKTAdapter.export( stripe.getSurface() ) ).append( "\n" );
                    } catch ( GeometryException e ) {
                        LOG.logError( "Error while exporting surface to wkt.", e );
                    }
                }
                LOG.logDebug( sb.toString() );
            }
        }
        return requestStripes;
    }

    /**
     * Should return the height of the terrain above the sealevel. TODO just returns a constant value, i.e. the
     * configured MinimalTerrainHeight.
     * 
     * @param eyePositionX
     *            to find the height of the sealevel for (not used yet)
     * @param eyePositionY
     *            to find the height of the sealevel for (not used yet)
     * @param eyePositionZ
     *            to find the height of the sealevel for (not used yet)
     * @return the height above the seaLevel.
     */
    protected double getTerrainHeightAboveSeaLevel( double eyePositionX, double eyePositionY, double eyePositionZ ) {
        return getTerrainHeightAboveSeaLevel( new Point3d( eyePositionX, eyePositionY, eyePositionZ ) );
    }

    /**
     * Should return the height of the terrain above the sealevel. TODO just returns a constant value, i.e. the
     * configured MinimalTerrainHeight.
     * 
     * @param eyePosition
     *            to find the height of the sealevel for (not used yet)
     * 
     * @return the height above the seaLevel.
     */
    protected double getTerrainHeightAboveSeaLevel( Point3d eyePosition ) {
        return WPVSConfiguration.getHeightForPosition( eyePosition );
    }

    /**
     * Creates a WPVS scene
     * 
     * @param viewPoint
     *            position of the viewer
     * @return a new scene
     * @throws OGCWebServiceException
     *             if the background img cannot be read
     */
    private WPVSScene createScene( GetView request, ViewPoint viewPoint, List<ResolutionStripe> stripes,
                                   Envelope sceneBBox )
                            throws OGCWebServiceException {
        if ( stripes == null || stripes.size() == 0 ) {
            String msg = "No resolutionStripes were given, therefore no scene can be created.";
            LOG.logError( msg );
            throw new OGCWebServiceException( msg );
        }
        LOG.logDebug( "Creating scene with " + stripes.size() + " number of resolution stripes " );
        OrderedGroup scene = new OrderedGroup();
        int dgmType = ResolutionStripe.ELEVATION_MODEL_UNKNOWN;
        for ( int i = 0; i < stripes.size() && dgmType == ResolutionStripe.ELEVATION_MODEL_UNKNOWN; ++i ) {
            dgmType = stripes.get( i ).getDGMType();
        }
        if ( dgmType == ResolutionStripe.ELEVATION_MODEL_POINTS ) {
            LOG.logDebug( "The elevation model uses points, therefore generating one terrain." );

            List<Point3d> terrainPoints = new ArrayList<Point3d>();
            double sceneWidth = sceneBBox.getWidth();
            double sceneHeight = sceneBBox.getHeight();
            double sceneMinX = sceneBBox.getMin().getX();
            double sceneMinY = sceneBBox.getMin().getY();

            /*
             * First find the resolution stripe with the best (==highest) resolution, and while doing so, add all
             * measurepoints to the point array.
             */
            ResolutionStripe hasBestResolution = null;
            for ( ResolutionStripe stripe : stripes ) {
                if ( stripe.getMeassurepointsAsList() != null ) {
                    terrainPoints.addAll( stripe.getMeassurepointsAsList() );
                }
                if ( hasBestResolution == null || stripe.getMaxResolution() < hasBestResolution.getMaxResolution() ) {
                    if ( stripe.getRequestHeightForBBox() != -1 && stripe.getRequestWidthForBBox() != -1 ) {
                        hasBestResolution = stripe;
                    }
                }

            }
            // Allthough it will probably never happen, it might be better to check.
            if ( hasBestResolution == null ) {
                LOG.logError( "No best resolutionStripe was found, this can happen if none of the stripes has a resolution or if the the requestwidths /heights of all returned -1 (e.g. were to large to be handled ) therefore no scene can be created." );
                throw new OGCWebServiceException( "Could not create scene due to internal errors",
                                                  ExceptionCode.NOAPPLICABLECODE );
            }

            BufferedImage texture = hasBestResolution.getResultTexture();
            double stripeTextureWidth = hasBestResolution.getRequestWidthForBBox();
            double stripeTextureHeight = hasBestResolution.getRequestHeightForBBox();
            // if an error occurred the texture might be null, in which case we use the textureWidth
            // and height.
            if ( texture == null ) {
                LOG.logInfo( "The best ResolutionStripe has no texture (this normally means, an error occurred while invoking it's texture datasources) creating a new (empty) texture." );
                texture = new BufferedImage( (int) stripeTextureWidth, (int) stripeTextureHeight,
                                             BufferedImage.TYPE_INT_ARGB );
            }

            String splittingMode = request.getVendorSpecificParameter( "SPLITTER" );
            if ( "BBOX".equals( splittingMode ) ) {
                if ( stripes.size() != 1 ) {
                    LOG.logError( "Allthough the bbox splitter is used, we have more then one ResolutionStripe, this may not be, using only the first Stripe" );
                }
                LOG.logDebug( "The request is a bbox, just using the first (and only) resolution stripe's texture." );
                sceneBBox = request.getBoundingBox();
            } else {

                /**
                 * The goal is to create one large textures to which all other textures are upscaled and then painted
                 * upon. To do so, first get the largest texture, which scale will be used to create an image (the
                 * resulttexture) which has the the dimensions of the scenes bbox. All that has to be done then, is to
                 * calculate for each the stripe, the relative position of it's bbox, to the scene's bbox and the scale
                 * of it's texture compared to the result texture's dimensions. This only has one little drawback, if
                 * the scene is large (a steep few for example) the texture can get larger as the available texture
                 * width of the gpu, in this case another scaling has to be applied.
                 */
                Envelope stripeBBox = hasBestResolution.getSurface().getEnvelope();

                // calculate the relation between the scene and the stripes bbox. and calculate the
                // width and height of the resulting texture.
                double scaleW = sceneWidth / stripeBBox.getWidth();
                double scaleH = sceneHeight / stripeBBox.getHeight();
                double resultTextureWidth = stripeTextureWidth * scaleW;
                double resultTextureHeight = stripeTextureHeight * scaleH;

                // Check the scale against the maximum texture size.
                double scale = getTextureScale( resultTextureWidth, resultTextureHeight );
                texture = new BufferedImage( (int) Math.floor( resultTextureWidth * scale ),
                                             (int) Math.floor( resultTextureHeight * scale ), texture.getType() );

                resultTextureHeight = texture.getHeight();
                resultTextureWidth = texture.getWidth();

                // Get the graphics object and set the hints to maximum quality.
                Graphics2D g2d = (Graphics2D) texture.getGraphics();
                /**
                 * We tried to set the rendering hints to different values here, but it seems, the standard values
                 * result in the quickest and best quality, because j3d will do antialiasing itself again.
                 */

                // the transform will scale and translate the requested textures onto the one master
                // texture.
                AffineTransform origTransform = g2d.getTransform();

                long paintTime = System.currentTimeMillis();
                // now draw all available texture onto the result texture.

                for ( ResolutionStripe stripe : stripes ) {
                    BufferedImage stripeTexture = stripe.getResultTexture();

                    if ( stripeTexture != null ) {

                        /**
                         * First calculate the position of this stripe's texture as if it was to be drawn upon it's own
                         * all fitting texture.
                         */
                        stripeBBox = stripe.getSurface().getEnvelope();
                        // find the offset of the stripes bbox to the scene bbox.
                        double realDistX = ( stripeBBox.getMin().getX() - sceneMinX ) / sceneWidth;
                        double realDistY = ( stripeBBox.getMin().getY() - sceneMinY ) / sceneHeight;

                        /**
                         * calculate the scale of the stripes' bbox to the scene's bbox
                         */
                        scaleW = sceneWidth / stripeBBox.getWidth();
                        scaleH = sceneHeight / stripeBBox.getHeight();
                        /**
                         * Calculate the virtual all-fitting-texture for this stripe
                         */
                        double tmpTextureWidth = stripeTexture.getWidth() * scaleW;
                        double tmpTextureHeight = stripeTexture.getHeight() * scaleH;

                        /**
                         * It might happen, that the viratula all-fitting-texture of the upscaled texture would have
                         * been to large for the texture memory to fit, therefore this scaling factor has to be taken
                         * into account as well.
                         */
                        double maxTextureScale = getTextureScale( resultTextureWidth, resultTextureHeight );

                        /**
                         * Calculate the scale of the virtual all-fitting-texture [with width
                         * =(tmpTextureWidth*maxTextureScale) and height= (tmpTextureHeight*maxTextureScale) ], to the
                         * real result texture.
                         */
                        double scaleX = resultTextureWidth / ( tmpTextureWidth * maxTextureScale );
                        double scaleY = resultTextureHeight / ( tmpTextureHeight * maxTextureScale );

                        /**
                         * And calculate the position within the virtual all-fitting-texture.
                         */
                        double posX = tmpTextureWidth * realDistX;
                        double posY = tmpTextureHeight
                                      - ( ( tmpTextureHeight * realDistY ) + stripeTexture.getHeight() );
                        if ( posY < 0 ) {
                            posY = 0;
                        }
                        if ( posY > tmpTextureHeight ) {
                            posY = (int) Math.floor( tmpTextureHeight );
                        }
                        if ( posX < 0 ) {
                            posX = 0;
                        }
                        LOG.logDebug( "posX: " + posX );
                        LOG.logDebug( "posY: " + posY );

                        /**
                         * First translate then set the scale of the g2d and after the drawing reset the transformation.
                         */
                        g2d.translate( Math.round( posX * scaleX ), Math.round( posY * scaleY ) );
                        g2d.scale( scaleX, scaleY );
                        g2d.drawImage( stripeTexture, 0, 0, new Color( 0, 0, 0, 0 ), null );
                        g2d.setTransform( origTransform );
                    } else {
                        LOG.logError( "One of the resolutionStripes has no texture, so nothing to draw" );
                    }
                }

                if ( LOG.getLevel() == ILogger.LOG_DEBUG ) {
                    LOG.logDebug( "The actual drawing on the g2d with preferred took: "
                                  + ( ( System.currentTimeMillis() - paintTime ) / 1000d ) + "seconds." );
                    try {
                        File f = File.createTempFile( "resultTexture", ".png" );
                        f.deleteOnExit();
                        ImageIO.write( texture, "png", f );
                        LOG.logDebug( "Wrote texture: " + f.getAbsolutePath() );
                    } catch ( IOException e ) {
                        LOG.logDebug( "Could not write texture for debugging purposes because: ", e.getMessage() );
                    }
                }
                g2d.dispose();
            }
            /**
             * Create a triangle terrain which will receive the texture.
             */
            TriangleTerrain terrain = new TriangleTerrain( terrainPoints, sceneBBox,
                                                           config.getDeegreeParams().getMinimalTerrainHeight(),
                                                           request.getScale() );
            terrain.setTexture( texture );
            terrain.createTerrain();
            scene.addChild( terrain );
        }
        if ( LOG.getLevel() == ILogger.LOG_DEBUG ) {
            if ( dgmType == ResolutionStripe.ELEVATION_MODEL_POINTS ) {
                LOG.logDebug( "The elevationmodel uses meassurepoints." );
            } else if ( dgmType == ResolutionStripe.ELEVATION_MODEL_GRID ) {
                LOG.logDebug( "The elevationmodel uses a grid." );
            } else if ( dgmType == ResolutionStripe.ELEVATION_MODEL_UNKNOWN ) {
                LOG.logDebug( "The elevationmodel uses an unknown format." );
            }
        }
        for ( ResolutionStripe stripe : stripes ) {
            // create one large triangled Shape3D object.
            LOG.logDebug( "Getting Shape3D object from Stripe: " + stripe );
            scene.addChild( stripe.getJava3DRepresentation() );
        }

        Calendar date = TimeTools.createCalendar( request.getVendorSpecificParameters().get( "DATETIME" ) );
        return new WPVSScene( scene, viewPoint, date, null, createBackground( viewPoint, request ) );
    }

    /**
     * Calculates the scale to fit the largest of the two given params to the maximum texture size.
     * 
     * @param width
     *            the originalwidth of the texture
     * @param height
     *            the original height of the texture
     * @return a scale to fit the width and height in the configured maximumtexture width. or 1 if both are smaller.
     */
    private double getTextureScale( double width, double height ) {
        if ( width > WPVSConfiguration.texture2DMaxSize || height > WPVSConfiguration.texture2DMaxSize ) {
            return WPVSConfiguration.texture2DMaxSize / ( ( width > height ) ? width : height );
        }
        return 1;
    }

    /**
     * Renders the scene and the resulting image.
     * 
     * @return a new image representing a screen shot of the scene
     */
    public BufferedImage renderScene() {

        BufferedImage image = renderer.renderScene();

        if ( !config.getDeegreeParams().isWatermarked() ) {
            paintCopyright( image );
        }

        return image;

    }

    /**
     * prints a copyright note at left side of the map bottom. The copyright note will be extracted from the WMS
     * capabilities/configuration
     * 
     * @param image
     *            the image onto which to print the copyright message/image
     */
    private void paintCopyright( BufferedImage image ) {

        Graphics2D g2 = (Graphics2D) image.getGraphics();

        WPVSDeegreeParams dp = config.getDeegreeParams();
        String copyright = dp.getCopyright();
        if ( config.getDeegreeParams().getCopyrightImage() != null ) {
            g2.drawImage( config.getDeegreeParams().getCopyrightImage(), 0,
                          image.getHeight() - config.getDeegreeParams().getCopyrightImage().getHeight(), null );
        } else if ( copyright != null && !"".equals( copyright ) ) {

            g2.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
            g2.setRenderingHint( RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON );

            final int fontSize = 14;
            final int margin = 5;

            int imgHeight = image.getHeight();

            Font f = new Font( "SANSSERIF", Font.PLAIN, fontSize );
            g2.setFont( f );
            // draw text shadow
            g2.setColor( Color.black );
            g2.drawString( copyright, margin, imgHeight - margin );
            // draw text
            g2.setColor( Color.white );
            g2.drawString( copyright, margin - 1, imgHeight - margin - 1 );

        }
        g2.dispose();
    }

    /**
     * Checks if the image size is compatible with that given in the configuration
     * 
     * @throws OGCWebServiceException
     */
    private void validateImageSize( GetView request )
                            throws OGCWebServiceException {
        int width = request.getImageDimension().width;
        int maxWidth = config.getDeegreeParams().getMaxViewWidth();
        if ( width > maxWidth ) {
            throw new OGCWebServiceException(
                                              StringTools.concat(
                                                                  100,
                                                                  "Requested view width exceeds allowed maximum width of ",
                                                                  new Integer( maxWidth ), " pixels." ) );
        }
        int height = request.getImageDimension().height;
        int maxHeight = config.getDeegreeParams().getMaxViewHeight();
        if ( height > maxHeight ) {
            throw new OGCWebServiceException(
                                              StringTools.concat(
                                                                  100,
                                                                  "Requested view height exceeds allowed maximum height of ",
                                                                  new Integer( maxHeight ), " pixels." ) );
        }

    }

    /**
     * @return Returns the generated scene for this request... handy for debugging.
     */
    public WPVSScene getTheScene() {
        return theScene;
    }

    /**
     * @return the scene renderer.
     */
    public OffScreenWPVSRenderer getRenderer() {
        return renderer;
    }

}
