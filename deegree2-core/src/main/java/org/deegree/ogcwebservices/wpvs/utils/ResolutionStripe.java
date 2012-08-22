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

package org.deegree.ogcwebservices.wpvs.utils;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Callable;

import javax.imageio.ImageIO;
import javax.media.j3d.OrderedGroup;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3f;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.MapUtils;
import org.deegree.framework.util.StringTools;
import org.deegree.i18n.Messages;
import org.deegree.model.crs.CoordinateSystem;
import org.deegree.model.spatialschema.Envelope;
import org.deegree.model.spatialschema.GeometryException;
import org.deegree.model.spatialschema.Position;
import org.deegree.model.spatialschema.Surface;
import org.deegree.model.spatialschema.WKTAdapter;
import org.deegree.ogcwebservices.OGCWebServiceException;
import org.deegree.ogcwebservices.wpvs.GetViewServiceInvoker;
import org.deegree.ogcwebservices.wpvs.WCSInvoker;
import org.deegree.ogcwebservices.wpvs.WFSInvoker;
import org.deegree.ogcwebservices.wpvs.WMSInvoker;
import org.deegree.ogcwebservices.wpvs.configuration.AbstractDataSource;
import org.deegree.ogcwebservices.wpvs.configuration.WPVSConfiguration;
import org.deegree.ogcwebservices.wpvs.j3d.DefaultSurface;
import org.deegree.ogcwebservices.wpvs.j3d.TerrainModel;
import org.deegree.ogcwebservices.wpvs.j3d.TexturedHeightMapTerrain;
import org.deegree.ogcwebservices.wpvs.j3d.TriangleTerrain;
import org.deegree.processing.raster.converter.Image2RawData;
import org.deegree.processing.raster.filter.Convolve;
import org.deegree.processing.raster.filter.RasterFilterException;
import org.j3d.geom.GeometryData;

/**
 * The <code>ResolutionStripe</code> class encapsulates a Surface with a maximum Resolution, which is convenient for the
 * creation of a quadtree.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * 
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */

public class ResolutionStripe implements Callable<ResolutionStripe>, Comparable<ResolutionStripe> {
    private final static ILogger LOG = LoggerFactory.getLogger( ResolutionStripe.class );

    /**
     * No information about the elevationmodel is known (e.g. no elevationModel is set)
     */
    public final static int ELEVATION_MODEL_UNKNOWN = 0;

    /**
     * The elevationmodel uses a grid data serving datasource (e.g. CSW)
     */
    public final static int ELEVATION_MODEL_GRID = 1;

    /**
     * The elevationmodel uses a point data serving datasource (e.g WFS)
     */
    public final static int ELEVATION_MODEL_POINTS = 2;

    private final double maxResolution;

    private final double minResolution;

    private Surface surface;

    private TerrainModel elevationModel = null;

    private AbstractDataSource elevationModelDataSource = null;

    private HashMap<String, BufferedImage> textures;

    private HashMap<String, OGCWebServiceException> textureExceptions;

    private ArrayList<AbstractDataSource> texturesDataSources;

    private HashMap<String, DefaultSurface> features;

    private ArrayList<AbstractDataSource> featureCollectionDataSources;

    private double minimalHeightlevel;

    private String outputFormat = null;

    private OrderedGroup resultingJ3DScene;

    private double scale;

    private int dgmType;

    private BufferedImage resultTexture = null;

    private List<Point3d> pointList;

    // a string for debugging containing the old resolution, if the resolution was fixed because of the maxRequestSize.
    private String debugString = null;

    /**
     * @param surface
     *            of this resolution stripe, after the stripeFactory it is a trapezium, after the quad-splitter an
     *            axis-alligned bbox, which will be a request.
     * @param maximumResolution
     *            the largest resolution value, resulting in the smallest map Resolution, e.g. farthest away from the
     *            viewer.
     * @param minimumResolution
     *            the smallest resolution value, resulting in the highest map Resolution, e.g. nearest to the viewer.
     * @param minimalHeight
     *            the terrain height which will be taken if an error occurred while retrieving height data.
     * @param scale
     *            of the heights ( 1.0 means no scaling )
     */
    public ResolutionStripe( Surface surface, double maximumResolution, double minimumResolution, double minimalHeight,
                             double scale ) {
        this.surface = surface;
        // calculate the requestWidth and requestHeight.
        int rH = (int) Math.round( surface.getEnvelope().getHeight() / Math.abs( minimumResolution ) );
        int rW = (int) Math.round( surface.getEnvelope().getWidth() / Math.abs( minimumResolution ) );

        if ( rH > WPVSConfiguration.MAX_REQUEST_SIZE || rH < 0 || rW > WPVSConfiguration.MAX_REQUEST_SIZE || rW < 0 ) {
            minResolution = ( ( rH > rW ) ? surface.getEnvelope().getHeight() : surface.getEnvelope().getWidth() )
                            / WPVSConfiguration.MAX_REQUEST_SIZE;
            maxResolution = minResolution;
            debugString = "fixed resolution: " + minimumResolution;
            LOG.logDebug( "Setting maxResolution to " + minResolution + " instead of " + minimumResolution
                          + " because the request width or height would be larger as + "
                          + WPVSConfiguration.MAX_REQUEST_SIZE + " pixel." );
        } else {
            this.minResolution = minimumResolution;
            this.maxResolution = maximumResolution;
        }
        this.minimalHeightlevel = minimalHeight;
        this.scale = scale;
        featureCollectionDataSources = new ArrayList<AbstractDataSource>( 5 );
        texturesDataSources = new ArrayList<AbstractDataSource>( 5 );
        textures = new HashMap<String, BufferedImage>( 10 );
        textureExceptions = new HashMap<String, OGCWebServiceException>( 10 );
        features = new HashMap<String, DefaultSurface>( 1000 );
        // resultingJ3DScene = new BranchGroup();
        resultingJ3DScene = null;
        dgmType = ResolutionStripe.ELEVATION_MODEL_UNKNOWN;
    }

    /**
     * @param surface
     *            of this resolution stripe, after the stripeFactory it is a trapezium, after the quad-splitter an
     *            axis-alligned bbox, which will be a request.
     * @param maximumResolution
     *            the largest resolution value, resulting in the smallest map Resolution, e.g. farthest away from the
     *            viewer.
     * @param minimumResolution
     *            the smallest resolution value, resulting in the highest map Resolution, e.g. nearest to the viewer.
     * @param minimalHeight
     *            the terrain height which will be taken if an error occurred while retrieving height data.
     * @param outputFormat
     *            of the requests.
     * @param scale
     *            of the heights ( 1.0 means no scaling )
     */
    public ResolutionStripe( Surface surface, double maximumResolution, double minimumResolution, double minimalHeight,
                             String outputFormat, double scale ) {
        this( surface, maximumResolution, minimumResolution, minimalHeight, scale );
        this.outputFormat = outputFormat;
    }

    /**
     * @return the CRS of this ResolutionStripe
     */
    public CoordinateSystem getCRSName() {
        return surface.getCoordinateSystem();
    }

    /**
     * @return the largest resolution value, resulting in the smallest map Resolution, e.g. farthest away from the
     *         viewer.
     */
    public double getMaxResolution() {
        return maxResolution;
    }

    /**
     * @return the (always possitive) resolution of the largest (away from the viewer) side of the surface as scale
     *         denominator, which means divide by {@link MapUtils#DEFAULT_PIXEL_SIZE}.
     */
    public double getMaxResolutionAsScaleDenominator() {
        return Math.abs( maxResolution ) / MapUtils.DEFAULT_PIXEL_SIZE;
    }

    /**
     * @return the (always possitive) resolution of the smallest (towards the viewer) side of the surface as scale
     *         denominator, which means divide by {@link MapUtils#DEFAULT_PIXEL_SIZE}.
     */
    public double getMinResolutionAsScaleDenominator() {
        return Math.abs( minResolution ) / MapUtils.DEFAULT_PIXEL_SIZE;
    }

    /**
     * @return the smallest resolution value, resulting in the highest map Resolution, e.g. nearest to the viewer.
     */
    public double getMinResolution() {
        return minResolution;
    }

    /**
     * @return the geometric surface which is defines this resolutionStripe.
     */
    public Surface getSurface() {
        return surface;
    }

    /**
     * @return the minimalTerrainHeight.
     */
    public double getMinimalTerrainHeight() {
        return minimalHeightlevel;
    }

    /**
     * @return the requestwidth (in pixels) for the bbox containing this resolutionsstripe or -1 if the resolution is to
     *         high (e.g. int overload)
     */
    public int getRequestWidthForBBox() {
        int result = (int) Math.round( surface.getEnvelope().getWidth() / Math.abs( minResolution ) );
        if ( result > 8000 ) {
            LOG.logDebug( "Returning -1 for the requestImageWidth, the maxResolution: " + maxResolution
                          + " the env.getHeight(): " + surface.getEnvelope().getHeight() );
            return -1;
        }
        return result;
    }

    /**
     * @return the height (in pixels) of the request envelope or -1 if the resolution is to high
     */
    public int getRequestHeightForBBox() {
        int result = (int) Math.round( surface.getEnvelope().getHeight() / Math.abs( minResolution ) );
        if ( result > 80000 ) {
            LOG.logDebug( "Returning -1 for the requestImageHeight, the maxResolution: " + maxResolution
                          + " the env.getHeight(): " + surface.getEnvelope().getHeight() );
            return -1;
        }
        return result;
    }

    /**
     * @return the elevationModel if no elevationModel is created jet, an {@link TriangleTerrain} elevation model of the
     *         bbox of this stripe (with heightvalues set to minimalheigt) is returned.
     */
    public TerrainModel getElevationModel() {
        if ( elevationModel == null ) {
            elevationModel = createTriangleTerrainFromBBox();
        }
        return elevationModel;
    }

    /**
     * @param elevationModel
     *            An other elevationModel.
     */
    public void setElevationModel( TerrainModel elevationModel ) {
        this.elevationModel = elevationModel;
    }

    /**
     * @param pointList
     *            containing Points which represents the heights of measures points (normally aquired from a wfs).
     */
    public void setElevationModelFromMeassurePoints( List<Point3d> pointList ) {
        // LOG.logDebug( "Found following meassure points from a wfs: " + pointList );
        this.pointList = pointList;
        // A little hack.
        // add the heightvalues of the nearest meassurepoint to the corner points of the bbox.
        Point3d ll = new Point3d( Double.MAX_VALUE, Double.MAX_VALUE, 0 );
        Point3d lr = new Point3d( Double.MIN_VALUE, Double.MAX_VALUE, 0 );
        Point3d ur = new Point3d( Double.MIN_VALUE, Double.MIN_VALUE, 0 );
        Point3d ul = new Point3d( Double.MAX_VALUE, Double.MIN_VALUE, 0 );
        for ( Point3d p : pointList ) {
            if ( p.x < ll.x && p.y < ll.y ) {
                ll.x = p.x;
                ll.y = p.y;
                ll.z = p.z;
            }
            if ( p.x > lr.x && p.y < lr.y ) {
                lr.x = p.x;
                lr.y = p.y;
                lr.z = p.z;
            }
            if ( p.x > ur.x && p.y > ur.y ) {
                ur.x = p.x;
                ur.y = p.y;
                ur.z = p.z;
            }
            if ( p.x < ul.x && p.y > ul.y ) {
                ul.x = p.x;
                ul.y = p.y;
                ul.z = p.z;
            }
        }
        Position min = surface.getEnvelope().getMin();
        Position max = surface.getEnvelope().getMax();
        this.pointList.add( new Point3d( min.getX(), min.getY(), ll.z ) );
        this.pointList.add( new Point3d( max.getX(), min.getY(), lr.z ) );
        this.pointList.add( new Point3d( max.getX(), max.getY(), ur.z ) );
        this.pointList.add( new Point3d( min.getX(), max.getY(), ul.z ) );
    }

    /**
     * @param heightMap
     *            a BufferedImage which contains height values, normally aquired from a wcs.
     */
    public void setElevationModelFromHeightMap( BufferedImage heightMap ) {
        Image2RawData i2rd = new Image2RawData( heightMap, (float) scale );
        float[][] heights = i2rd.parse();

        // smooth the outcome of the rasterdata, by applying a lowpass filter.
        float[][] kernel = new float[5][5];
        for ( int i = 0; i < kernel.length; i++ ) {
            for ( int j = 0; j < kernel[i].length; j++ ) {
                kernel[i][j] = 1;
            }
        }

        try {
            heights = Convolve.perform( heights, kernel );
        } catch ( RasterFilterException e ) {
            e.printStackTrace();
        }

        Envelope env = surface.getEnvelope();

        Position lowerLeft = surface.getEnvelope().getMin();
        Vector3f lLeft = new Vector3f( (float) lowerLeft.getX(), (float) lowerLeft.getY(), 0 );

        // Triangles won't work -> an error in org.j3d.geom.terrain.ElevationGridGenerator therefor
        // using TRIANGLE_STRIPS
        LOG.logDebug( "Trying to create elevationmodel from the points received from a wcs" );
        elevationModel = new TexturedHeightMapTerrain( (float) env.getWidth(), (float) env.getHeight(), heights, lLeft,
                                                       GeometryData.TRIANGLE_STRIPS, false );
        LOG.logDebug( "From the point list of the wcs created following elevationModel: " + elevationModel );
    }

    /**
     * @return the features of this resolutionstripe
     */
    public HashMap<String, DefaultSurface> getFeatures() {
        return features;
    }

    /**
     * @param key
     *            the name of the feature to be added.
     * @param feature
     *            (e.g a building, tree etc.) as a DefaultSurface (derived frome Shape3D) to be added to the hashmap.
     * @return true if the feature wasn't allready defined in the hashmap and could therefore be inserted, or if the key
     *         or feature are null.
     */
    public boolean addFeature( String key, DefaultSurface feature ) {
        if ( feature != null && key != null ) {
            DefaultSurface tmp = features.get( key );
            if ( tmp == null && !features.containsKey( key ) ) {
                features.put( key, feature );
                return true;
            }
        }
        return false;
    }

    /**
     * @return the textures value.
     */
    public HashMap<String, BufferedImage> getTextures() {
        return textures;
    }

    /**
     * @param key
     *            the name of the texture to be added.
     * @param texture
     *            to be added to the hashmap.
     * @return true if the texture wasn't allready defined in the hashmap and could therefore be inserted, or if the key
     *         or texture are null.
     */
    public boolean addTexture( String key, BufferedImage texture ) {
        if ( texture != null && key != null ) {
            BufferedImage tmp = textures.get( key );
            if ( tmp == null && !textures.containsKey( key ) ) {
                textures.put( key, texture );
                return true;
            }
        }
        return false;
    }

    /**
     * @return the elevationModelDataSource value.
     */
    public AbstractDataSource getElevationModelDataSource() {
        return elevationModelDataSource;
    }

    /**
     * @param elevationModelDataSource
     *            An other elevationModelDataSource value.
     */
    public void setElevationModelDataSource( AbstractDataSource elevationModelDataSource ) {
        this.elevationModelDataSource = elevationModelDataSource;
        if ( elevationModelDataSource.getServiceType() == AbstractDataSource.LOCAL_WFS
             || elevationModelDataSource.getServiceType() == AbstractDataSource.REMOTE_WFS ) {
            dgmType = ResolutionStripe.ELEVATION_MODEL_POINTS;
        } else if ( elevationModelDataSource.getServiceType() == AbstractDataSource.LOCAL_WCS
                    || elevationModelDataSource.getServiceType() == AbstractDataSource.REMOTE_WCS ) {
            dgmType = ResolutionStripe.ELEVATION_MODEL_GRID;
        }
    }

    /**
     * @return the featureCollectionDataSources value.
     */
    public ArrayList<AbstractDataSource> getFeatureCollectionDataSources() {
        return featureCollectionDataSources;
    }

    /**
     * @param featureCollectionDataSource
     *            a DataSources for a specific featureCollection.
     */
    public void addFeatureCollectionDataSource( AbstractDataSource featureCollectionDataSource ) {
        if ( featureCollectionDataSource != null ) {
            if ( !featureCollectionDataSources.contains( featureCollectionDataSource ) ) {
                featureCollectionDataSources.add( featureCollectionDataSource );
            }
        }
    }

    /**
     * @return the texturesDataSources value.
     */
    public ArrayList<AbstractDataSource> getTexturesDataSources() {
        return texturesDataSources;
    }

    /**
     * @param textureDataSource
     *            An other texturesDataSources value.
     */
    public void addTextureDataSource( AbstractDataSource textureDataSource ) {
        if ( textureDataSource != null ) {
            if ( !texturesDataSources.contains( textureDataSource ) ) {
                texturesDataSources.add( textureDataSource );
            }
        }
    }

    /**
     * 
     * @return the OutputFormat of the resultImage
     */
    public String getOutputFormat() {
        return outputFormat;
    }

    /**
     * @param outputFormat
     *            the mime type of the resultimage
     */
    public void setOutputFormat( String outputFormat ) {
        this.outputFormat = outputFormat;
    }

    /**
     * After a call to this class call method, it is possible to get a Java3D representation --in form of a
     * BranchGroup-- of this resolutionStripe. In this BranchGroup all the textures and requested features are added to
     * the ElevationModel.
     * 
     * @return a Java3D representation of this ResolutionStripe.
     */
    public OrderedGroup getJava3DRepresentation() {
        if ( resultingJ3DScene == null ) {
            createJava3DRepresentation();
        }
        return resultingJ3DScene;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder( 512 );
        sb.append( "Resolution: " ).append( maxResolution ).append( "\n" );

        try {
            sb.append( "Surface: " ).append( WKTAdapter.export( this.surface ) ).append( "\n" );
        } catch ( GeometryException e ) {
            e.printStackTrace();
        }
        sb.append( "FeatureCollectionDataSources:\n " );
        if ( featureCollectionDataSources.size() == 0 ) {
            sb.append( " - No feature collection datasources defined.\n" );
        } else {
            for ( int i = 0; i < featureCollectionDataSources.size(); ++i ) {
                sb.append( " - " ).append( i ).append( ") " ).append( featureCollectionDataSources.get( i ) ).append(
                                                                                                                      "\n" );
            }
        }
        sb.append( "TexturesDataSources:\n" );
        if ( texturesDataSources.size() == 0 ) {
            sb.append( " - No texture datasources defined.\n" );
        } else {
            for ( int i = 0; i < texturesDataSources.size(); ++i ) {
                sb.append( " - " ).append( i ).append( ") " ).append( texturesDataSources.get( i ) ).append( "\n" );
            }
        }

        sb.append( "ElevationDataSource: \n" );
        if ( elevationModelDataSource == null ) {
            sb.append( " - No elevation model datasources defined.\n" );
        } else {
            sb.append( " - " ).append( elevationModelDataSource ).append( "\n" );
        }
        return sb.toString();
    }

    /**
     * @return a well known representation of the geometry of this Resolutionstripe
     */
    public String toWKT() {
        try {
            return new StringBuffer( WKTAdapter.export( this.surface ) ).toString();
        } catch ( GeometryException e ) {
            e.printStackTrace();
            return new String( "" );
        }
    }

    /**
     * Outputs the textures to the tmp directory with following format:
     * <code>key_response:___res:_maxresolution__random_id.jpg</code> this file will be deleted at jvm termination.
     */
    public void outputTextures() {

        Set<String> keys = textures.keySet();
        Random rand = new Random( System.currentTimeMillis() );
        for ( String key : keys ) {
            try {
                // System.out.println( "saving image" );

                File f = new File( key + "_response_" + rand.nextInt() + "__res_" + maxResolution + "___.jpg" );
                f.deleteOnExit();
                LOG.logDebug( "Saving result texture ( in file: " + f.getAbsolutePath() + " for resolution stripe\n"
                              + this );
                // System.out.println( f );
                // ImageUtils.saveImage( responseImage, f, 1 );
                ImageIO.write( textures.get( key ), "jpg", f );
            } catch ( IOException e ) {
                e.printStackTrace();
            } catch ( Exception e ) {
                e.printStackTrace();
            }
        }
    }

    /**
     * @return the scale of the heights (1.0 means no scaling)
     */
    public double getScale() {
        return scale;
    }

    /**
     * @return one of the possible values {@link ResolutionStripe#ELEVATION_MODEL_UNKNOWN}
     *         {@link ResolutionStripe#ELEVATION_MODEL_GRID} {@link ResolutionStripe#ELEVATION_MODEL_POINTS}
     */
    public int getDGMType() {
        return dgmType;
    }

    /**
     * @return the resultTexture, which is build from all requested (wms/wcs) textures.
     */
    public BufferedImage getResultTexture() {
        return resultTexture;
    }

    /**
     * This call method is part of the Deegree Concurrent framework ({@link org.deegree.framework.concurrent.Executor})
     * . In this case it requests all the Data for a <code>ResolutionStripe</code> by invoking the necessary
     * webservices.
     * 
     * @see java.util.concurrent.Callable#call()
     */
    public ResolutionStripe call()
                            throws OGCWebServiceException {
        int invokeCounter = 0;
        // Strictly the different datasources must not be separated into two different
        // DataSourceList, it might be handy (for caching) to do so though.
        for ( AbstractDataSource textureDS : texturesDataSources ) {
            invokeDataSource( textureDS, invokeCounter++ );
        }
        // create one texture from all textures
        createTexture();

        // Create the buildings etc.
        for ( AbstractDataSource featureDS : featureCollectionDataSources ) {
            invokeDataSource( featureDS, invokeCounter++ );
        }

        // create the terrain, if no terrain was requested, just create a flat square.
        if ( elevationModelDataSource != null ) {
            LOG.logDebug( "Invoking terrain datasource, because an elevation model was given." );
            invokeDataSource( elevationModelDataSource, -1 );
        } else {
            LOG.logDebug( "Create flat triangle terrain, because no elevation model was given." );
            elevationModel = createTriangleTerrainFromBBox();
        }
        // let this thread create the model in advance.
        // if ( dgmType == ELEVATION_MODEL_GRID || dgmType == ELEVATION_MODEL_UNKNOWN) {
        createJava3DRepresentation();
        // }
        return this;
    }

    private void invokeDataSource( AbstractDataSource ads, int id ) {
        try {
            GetViewServiceInvoker invoker = null;
            if ( ads.getServiceType() == AbstractDataSource.LOCAL_WMS
                 || ads.getServiceType() == AbstractDataSource.REMOTE_WMS ) {
                invoker = new WMSInvoker( this, id );
            } else if ( ads.getServiceType() == AbstractDataSource.LOCAL_WCS
                        || ads.getServiceType() == AbstractDataSource.REMOTE_WCS ) {
                invoker = new WCSInvoker( this, id, outputFormat, ( ads == elevationModelDataSource ) );
            } else { // WFS -> was checked in DefaultGetViewHandler
                invoker = new WFSInvoker( this, id, ( ads == elevationModelDataSource ) );
            }
            invoker.invokeService( ads );
        } catch ( Throwable e ) {
            if ( !Thread.currentThread().isInterrupted() ) {
                LOG.logError( "WPVS: error while invoking a datasource: " + e.getMessage(), e );
            }
        }
    }

    private TriangleTerrain createTriangleTerrainFromBBox() {
        List<Point3d> measurePoints = new ArrayList<Point3d>( 0 );
        return new TriangleTerrain( measurePoints, surface.getEnvelope(), minimalHeightlevel, scale );
    }

    /**
     * Creates a java3d representation of the data wihtin this resolutionstripe. If the DGM is defined from
     * measurepoints, these points are triangualuated before createing the OrderedGroup.
     */
    private void createJava3DRepresentation() {
        resultingJ3DScene = new OrderedGroup();

        if ( resultTexture != null && elevationModel != null ) {
            LOG.logDebug( "Creating an elevationmodel for the resolutionstripe" );
            elevationModel.setTexture( resultTexture );
            elevationModel.createTerrain();
            resultingJ3DScene.addChild( elevationModel );
        }

        // add the features (e.g. buildings) to the j3d representation of this resolutionstripe.
        Collection<DefaultSurface> featureSurfaces = features.values();
        if ( featureSurfaces != null ) {
            for ( DefaultSurface ds : featureSurfaces ) {
                resultingJ3DScene.addChild( ds );
            }
        }
        // if( LOG.getLevel() == ILogger.LOG_DEBUG ){
        //
        //
        // try {
        // File f = File.createTempFile( "features", ".xml" );
        // J3DToCityGMLExporter exporter = new J3DToCityGMLExporter( "test", surface.getCoordinateSystem().getName(),
        // "bla", f.getParent()+File.separator+"textures", 0, 0, 0, false );
        // StringBuilder sb = new StringBuilder( 200000 );
        // exporter.export( sb, resultingJ3DScene );
        // LOG.logDebug( "Writing citygml file to: " + f.getAbsoluteFile() );
        // BufferedWriter out = new BufferedWriter( new FileWriter( f ) );
        // out.write( sb.toString() );
        // out.flush();
        // out.close();
        // } catch ( IOException e ) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }
        // }
    }

    /**
     * Paints all textures on the given bufferedImage
     */
    private void createTexture() {
        Collection<BufferedImage> textureImages = textures.values();
        Graphics2D g2d = null;
        if ( textureImages != null && !textureImages.isEmpty() ) {
            // create texture as BufferedImage
            if ( textureImages.size() > 0 ) {
                Iterator<BufferedImage> it = textureImages.iterator();
                resultTexture = it.next();
                // get the g2d from the resulttexture in advance.
                if ( resultTexture != null ) {
                    g2d = (Graphics2D) resultTexture.getGraphics();
                }
                while ( it.hasNext() ) {
                    if ( resultTexture == null ) {
                        resultTexture = it.next();
                    } else {
                        if ( g2d == null ) {
                            g2d = (Graphics2D) resultTexture.getGraphics();
                        }
                        // draw the next image on the g2d
                        g2d.drawImage( it.next(), 0, 0, null );
                    }
                }
            }
        }
        if ( resultTexture == null ) {
            // no images were found, so output the error messages or the default error message.
            LOG.logDebug( "No images were found (or all were null), therefore outputing the errormessages" );
            resultTexture = new BufferedImage( getRequestWidthForBBox(), getRequestHeightForBBox(),
                                               BufferedImage.TYPE_INT_ARGB );
            g2d = (Graphics2D) resultTexture.getGraphics();

            if ( texturesDataSources.size() > 0 ) {
                Collection<OGCWebServiceException> exceptions = textureExceptions.values();
                String[] exceptionStrings = new String[exceptions.size()];
                int count = 0;
                for ( OGCWebServiceException ogcwse : exceptions ) {

                    String message = StringTools.concat( 100, "error (", Integer.valueOf( count + 1 ), "): ",
                                                         ogcwse.getMessage() );
                    exceptionStrings[count++] = message;
                }
                paintString( g2d, exceptionStrings );
            } else {
                g2d.setColor( Color.WHITE );
                g2d.drawRect( 0, 0, resultTexture.getWidth(), resultTexture.getHeight() );
            }
        }
        if ( g2d != null ) {

            if ( LOG.getLevel() == ILogger.LOG_DEBUG ) {
                Stroke s = g2d.getStroke();
                Color c = g2d.getColor();
                g2d.setColor( Color.RED );
                g2d.setStroke( new BasicStroke( 3 ) );
                g2d.drawRect( 2, 2, resultTexture.getWidth() - 2, resultTexture.getHeight() - 2 );
                g2d.setColor( c );
                g2d.setStroke( s );

                paintString( g2d, new String[] { Double.toString( minResolution ),
                                                ( ( debugString != null ) ? debugString : "" ),
                                                Double.toString( maxResolution ) } );
            }
            g2d.dispose();
        }

    }

    private void paintString( Graphics2D g2d, String[] stringsToPaint ) {
        Font originalFont = g2d.getFont();
        float originalFontSize = originalFont.getSize();
        if ( stringsToPaint == null || stringsToPaint.length == 0 ) {
            LOG.logError( Messages.getMessage( "WPVS_NO_STRINGS" ) );
            return;
        }
        // find the largest string
        String testString = new String();
        for ( int i = 0; i < stringsToPaint.length; ++i ) {
            if ( stringsToPaint[i].length() > testString.length() ) {
                testString = stringsToPaint[i];
            }
        }
        // calculate the maximal height (with respect to the strings) of the font.
        float requestWidth = getRequestWidthForBBox();
        float requestHeight = getRequestHeightForBBox();
        float maxFontHeight = requestHeight / stringsToPaint.length;
        TextLayout tl = new TextLayout( testString, originalFont, g2d.getFontRenderContext() );
        Rectangle2D r2d = tl.getBounds();
        float width = (float) r2d.getWidth();
        float height = (float) r2d.getHeight();

        // little widther than the requestwidth ensures total readabillity
        float approx = requestWidth / ( width * 1.2f );
        if ( ( originalFontSize * approx ) < maxFontHeight ) {
            originalFont = originalFont.deriveFont( originalFontSize * approx );
        } else {
            originalFont = originalFont.deriveFont( maxFontHeight );
        }
        tl = new TextLayout( testString, originalFont, g2d.getFontRenderContext() );
        r2d = tl.getBounds();
        width = (float) r2d.getWidth();
        height = (float) r2d.getHeight();

        int x = (int) Math.round( ( requestWidth * 0.5 ) - ( width * 0.5 ) );
        int stringOffset = (int) Math.round( ( requestHeight * ( 1d / ( stringsToPaint.length + 1 ) ) )
                                             + ( height * 0.5 ) );
        g2d.setColor( Color.GRAY );
        // g2d.drawRect( 0, 0, (int) requestWidth, (int) requestHeight );
        g2d.setColor( Color.RED );
        g2d.setFont( originalFont );
        for ( int i = 0; i < stringsToPaint.length; ++i ) {
            int y = ( i + 1 ) * stringOffset;
            g2d.drawString( stringsToPaint[i], x, y );
        }
    }

    /**
     * @param datasourceID
     *            the id of the datasources which received an exception while retrieving a texture.
     * @param exception
     *            the exception that occured while invoking the datasource.
     */
    public void setTextureRetrievalException( String datasourceID, OGCWebServiceException exception ) {
        if ( datasourceID != null && exception != null ) {
            if ( !textureExceptions.containsKey( datasourceID ) ) {
                textureExceptions.put( datasourceID, exception );
            }
        }
    }

    /**
     * @return the pointList
     */
    public final List<Point3d> getMeassurepointsAsList() {
        return pointList;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo( ResolutionStripe other ) {
        if ( Math.abs( other.maxResolution - this.maxResolution ) < 0.0001 ) {
            return 0;
        }
        return ( ( this.maxResolution - other.maxResolution ) < 0 ) ? -1 : 1;

    }

}
