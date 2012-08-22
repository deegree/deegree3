//$$HeadURL$$
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

package org.deegree.ogcwebservices.wpvs.configuration;

import java.awt.GraphicsConfigTemplate;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.GraphicsConfigTemplate3D;
import javax.media.j3d.View;
import javax.vecmath.Point3d;

import org.deegree.datatypes.Code;
import org.deegree.datatypes.QualifiedName;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.CharsetUtils;
import org.deegree.framework.util.IDGenerator;
import org.deegree.framework.xml.XMLTools;
import org.deegree.i18n.Messages;
import org.deegree.model.coverage.grid.ImageGridCoverage;
import org.deegree.model.crs.CRSTransformationException;
import org.deegree.model.crs.CoordinateSystem;
import org.deegree.model.crs.GeoTransformer;
import org.deegree.model.crs.UnknownCRSException;
import org.deegree.model.feature.FeatureCollection;
import org.deegree.model.filterencoding.ComplexFilter;
import org.deegree.model.filterencoding.FeatureFilter;
import org.deegree.model.filterencoding.FeatureId;
import org.deegree.model.filterencoding.Filter;
import org.deegree.model.spatialschema.Envelope;
import org.deegree.model.spatialschema.GMLGeometryAdapter;
import org.deegree.model.spatialschema.Position;
import org.deegree.ogcbase.PropertyPath;
import org.deegree.ogcwebservices.OGCWebServiceException;
import org.deegree.ogcwebservices.getcapabilities.Contents;
import org.deegree.ogcwebservices.getcapabilities.OperationsMetadata;
import org.deegree.ogcwebservices.getcapabilities.ServiceIdentification;
import org.deegree.ogcwebservices.getcapabilities.ServiceProvider;
import org.deegree.ogcwebservices.wcs.WCSException;
import org.deegree.ogcwebservices.wcs.WCService;
import org.deegree.ogcwebservices.wcs.getcoverage.DomainSubset;
import org.deegree.ogcwebservices.wcs.getcoverage.GetCoverage;
import org.deegree.ogcwebservices.wcs.getcoverage.Output;
import org.deegree.ogcwebservices.wcs.getcoverage.ResultCoverage;
import org.deegree.ogcwebservices.wcs.getcoverage.SpatialSubset;
import org.deegree.ogcwebservices.wfs.WFService;
import org.deegree.ogcwebservices.wfs.operation.FeatureResult;
import org.deegree.ogcwebservices.wfs.operation.GetFeature;
import org.deegree.ogcwebservices.wpvs.capabilities.Dataset;
import org.deegree.ogcwebservices.wpvs.capabilities.ElevationModel;
import org.deegree.ogcwebservices.wpvs.capabilities.WPVSCapabilities;
import org.deegree.ogcwebservices.wpvs.j3d.PointsToPointListFactory;
import org.deegree.processing.raster.converter.Image2RawData;
import org.w3c.dom.Document;

/**
 * This class represents a <code>WPVSConfiguration</code> object.
 * 
 * @author <a href="mailto:taddei@lat-lon.de">Ugo Taddei</a>
 * @author last edited by: $Author$
 * 
 *         $Revision$, $Date$
 * 
 */
public class WPVSConfiguration extends WPVSCapabilities {

    private static ILogger LOG = LoggerFactory.getLogger( WPVSConfiguration.class );

    /**
     *
     */
    private static final long serialVersionUID = 3699085834869705611L;

    private WPVSDeegreeParams deegreeParams;

    private double smallestMinimalScaleDenomiator;

    // set if the configured datasets were searched.
    private static boolean searchedForElevModel = false;

    // pool for the canvasses
    private static List<Canvas3D> canvasPool = new ArrayList<Canvas3D>();

    private static Vector<Canvas3D> inUseCanvases = new Vector<Canvas3D>();

    // just some strings for getting the gpu-properties
    private final static String tusm = "textureUnitStateMax";

    private final static String twm = "textureWidthMax";

    /**
     * Is set to the maximum number of texture units accessable to the wpvs.
     */
    public static int availableTextureUnitStates = 1;

    /**
     * Is set to the maximum size of a texture supported by the canvas3d.
     */
    public static int texture2DMaxSize = 1024;

    /**
     * The size of a wfs/wms/wcs request if larger as the available texture size it will be the texture size (read from
     * the gpu).
     */
    public static int MAX_REQUEST_SIZE;

    /**
     * The elevation model to check for the height above the terrain.
     */
    public static AbstractDataSource largestElevModel = null;

    // holding the z values with heightMap[y][x]
    private static float[][] heightMap;

    // scale mapping the width of the configured bbox to the width of the heightmap
    private static double scaleMapWidth;

    // scale mapping the height of the configured bbox to the height of the heightmap
    private static double scaleMapHeight;

    // actual width of the heightMap == heightmap[0].length
    private static int mapWidth;

    // actual height of the heightMap == heightmap.length
    private static int mapHeight;

    // the found minimalHeight for the terrain or the configuredMinTerrainHeight.
    private static double globalMinimalHeight;

    // the configured bbox of the top dataset.
    private static Envelope configuredBBox;

    static {
        canvasPool.add( createOffscreenCanvas3D() );
    }

    /**
     * @param version
     *            the Version of this wpvs
     * @param updateSequence
     *            optional needed for clients who want to do caching (ogc-spec)
     * @param serviceIdentification
     * @param serviceProvider
     * @param operationsMetadata
     * @param contents
     * @param dataset
     * @param wpvsParams
     *            deegree specific parameters.
     * @param smallestMinimalScaleDenomiator
     *            of all datasources, it is needed to calculate the smallest resolutionstripe possible.
     */
    public WPVSConfiguration( String version, String updateSequence, ServiceIdentification serviceIdentification,
                              ServiceProvider serviceProvider, OperationsMetadata operationsMetadata,
                              Contents contents, Dataset dataset, WPVSDeegreeParams wpvsParams,
                              double smallestMinimalScaleDenomiator ) {

        super( version, updateSequence, serviceIdentification, serviceProvider, operationsMetadata, contents, dataset );
        this.deegreeParams = wpvsParams;
        // int size = Integer.MAX_VALUE;// deegreeParams.getMaxRequestSize();
        int size = deegreeParams.getMaxTextureDimension(); // deegreeParams.getMaxRequestSize();
        if ( size == Integer.MAX_VALUE ) {
            MAX_REQUEST_SIZE = texture2DMaxSize;
        } else {
            MAX_REQUEST_SIZE = size;
            if ( MAX_REQUEST_SIZE > texture2DMaxSize ) {
                LOG.logWarning( "The specified max request size value (of the deeegree params section) is larger then the possible texture size of your graphics-card, therefore setting it to: "
                                + texture2DMaxSize );
                MAX_REQUEST_SIZE = texture2DMaxSize;
            }
        }
        // set the minmalScaleDenominator according to the request-size
        this.smallestMinimalScaleDenomiator = ( (double) deegreeParams.getMaxViewWidth() ) / MAX_REQUEST_SIZE;
        LOG.logDebug( "Smallest denomi: " + this.smallestMinimalScaleDenomiator );
        if ( Double.isInfinite( smallestMinimalScaleDenomiator ) || smallestMinimalScaleDenomiator < 0
             || smallestMinimalScaleDenomiator > 1 ) {
            this.smallestMinimalScaleDenomiator = 1;
        }

        // create the global height map if it does not exist already
        if ( !searchedForElevModel && largestElevModel == null ) {
            synchronized ( canvasPool ) {
                if ( !searchedForElevModel ) {
                    largestElevModel = findLargestElevModel();
                    searchedForElevModel = true;
                    if ( largestElevModel != null ) {
                        Dataset topSet = getDataset();
                        Envelope env = topSet.getWgs84BoundingBox();
                        CoordinateSystem[] definedCRSs = topSet.getCrs();
                        CoordinateSystem defaultCRS = null;
                        if ( definedCRSs != null && definedCRSs.length > 0 ) {
                            defaultCRS = definedCRSs[0];
                            if ( definedCRSs.length > 1 ) {
                                LOG.logInfo( "Using first defined crs: " + defaultCRS
                                             + " to convert latlon (wgs84) coordinates to." );
                            }
                            try {
                                GeoTransformer gt = new GeoTransformer( defaultCRS );
                                configuredBBox = gt.transform( env, "EPSG:4326" );
                            } catch ( UnknownCRSException e ) {
                                LOG.logError( e.getMessage(), e );
                            } catch ( CRSTransformationException e ) {
                                LOG.logError( e.getMessage(), e );
                            }
                            // check if the admin has configured a minimal dgm resolution, if so the request and
                            // response resolution for the dgm must be set.
                            int requestWidth = MAX_REQUEST_SIZE;
                            int requestHeight = MAX_REQUEST_SIZE;
                            if ( configuredBBox.getWidth() >= configuredBBox.getHeight() ) {
                                requestHeight = (int) Math.floor( ( configuredBBox.getHeight() / configuredBBox.getWidth() )
                                                                  * MAX_REQUEST_SIZE );
                            } else {
                                requestWidth = (int) Math.floor( ( configuredBBox.getWidth() / configuredBBox.getHeight() )
                                                                 * MAX_REQUEST_SIZE );
                            }
                            LOG.logDebug( "Setting globalHeightmap requestWidth: " + requestWidth );
                            LOG.logDebug( "Setting globalHeightmap requestHeight: " + requestHeight );

                            // Set the static members for easy global access
                            mapHeight = requestHeight;
                            mapWidth = requestWidth;

                            scaleMapWidth = mapWidth / configuredBBox.getWidth();
                            scaleMapHeight = mapHeight / configuredBBox.getHeight();

                            if ( largestElevModel instanceof LocalWCSDataSource ) {
                                heightMap = invokeWCS( (LocalWCSDataSource) largestElevModel, configuredBBox,
                                                       requestWidth, requestHeight );
                            } else if ( largestElevModel instanceof LocalWFSDataSource ) {
                                heightMap = invokeWFS( (LocalWFSDataSource) largestElevModel, configuredBBox,
                                                       requestWidth, requestHeight );
                            }

                            if ( heightMap.length == 0 ) {
                                LOG.logWarning( "The creation of the global heightmap has failed." );
                                globalMinimalHeight = getDeegreeParams().getMinimalTerrainHeight();
                            } else {
                                float max = Float.MIN_VALUE;
                                globalMinimalHeight = Double.MAX_VALUE;
                                for ( int y = 0; y < requestHeight; ++y ) {
                                    for ( int x = 0; x < requestWidth; ++x ) {
                                        float height = heightMap[y][x];
                                        globalMinimalHeight = Math.min( globalMinimalHeight, height );
                                        max = Math.max( max, height );
                                    }
                                }
                                if ( globalMinimalHeight < getDeegreeParams().getMinimalTerrainHeight() ) {
                                    LOG.logDebug( "Setting found globalMinimalHeight: " + globalMinimalHeight
                                                  + " to configured minmalTerrainHeight of: "
                                                  + getDeegreeParams().getMinimalTerrainHeight() );
                                    globalMinimalHeight = getDeegreeParams().getMinimalTerrainHeight();
                                }
                                if ( LOG.getLevel() == ILogger.LOG_DEBUG ) {
                                    BufferedImage img = new BufferedImage( requestWidth, requestHeight,
                                                                           BufferedImage.TYPE_INT_RGB );
                                    LOG.logDebug( "global maximumHeight: " + max );
                                    LOG.logDebug( "global minimalHeight: " + globalMinimalHeight );

                                    double scale = ( 1 / ( max - globalMinimalHeight ) ) * 255;
                                    for ( int y = 0; y < requestHeight; ++y ) {
                                        for ( int x = 0; x < requestWidth; ++x ) {
                                            float height = heightMap[y][x];
                                            byte first = (byte) Math.floor( height * scale );
                                            int color = first;
                                            color |= ( color << 8 );
                                            color |= ( color << 16 );

                                            img.setRGB( x, y, color );
                                        }

                                    }
                                    try {
                                        File f = File.createTempFile( "global_heightmap_response", ".png" );
                                        LOG.logDebug( "creating tmpfile for global heightmap with name: "
                                                      + f.toString() );
                                        f.deleteOnExit();
                                        ImageIO.write( img, "png", f );
                                    } catch ( IOException e ) {
                                        LOG.logError( e.getMessage(), e );
                                    }
                                }

                            }

                        }
                    }
                }
                canvasPool.notifyAll();
            }
            if ( LOG.getLevel() == ILogger.LOG_DEBUG ) {
                if ( largestElevModel != null ) {
                    LOG.logDebug( "found elev-model: " + largestElevModel );
                }
            }
        }
    }

    /**
     * @param dataSource
     * @param env
     * @param requestHeight
     * @param requestWidth
     * @return the wfs-feature-points mapped to the heightmap, or an empty map if no features were found.
     */
    private float[][] invokeWFS( LocalWFSDataSource dataSource, Envelope env, int requestWidth, int requestHeight ) {
        float[][] result = new float[0][0];
        WFService service = null;
        try {
            service = (WFService) dataSource.getOGCWebService();
        } catch ( OGCWebServiceException ogcwe ) {
            LOG.logError( ogcwe.getMessage() );
            // throw new RuntimeException( ogcwe );
        }
        if ( service == null ) {
            LOG.logError( "No Web Feature Service instance available for creation of the Global height map." );
        } else {
            Object response = null;
            try {

                // create the GetFeature request.
                QualifiedName qn = dataSource.getName();

                StringBuilder sb = new StringBuilder( 5000 );
                sb.append( "<?xml version='1.0' encoding='" + CharsetUtils.getSystemCharset() + "'?>" );
                sb.append( "<wfs:GetFeature xmlns:wfs='http://www.opengis.net/wfs' " );
                sb.append( "xmlns:ogc='http://www.opengis.net/ogc' " );
                sb.append( "xmlns:gml='http://www.opengis.net/gml' " );
                sb.append( "xmlns:" ).append( qn.getPrefix() ).append( '=' );
                sb.append( "'" ).append( qn.getNamespace() ).append( "' " );

                if ( dataSource.getServiceType() == AbstractDataSource.LOCAL_WFS ) {
                    sb.append( "outputFormat='FEATURECOLLECTION'>" );
                } else {
                    sb.append( "outputFormat='text/xml; subtype=gml/3.1.1'>" );
                }

                /**
                 * To make things a little clearer compare with this SQL-Statement: SELECT ( !texture )? geoProperty : *
                 * FROM qn.getLocalName() WHERE geoPoperty intersects with resolutionStripe.getSurface() AND
                 * FilterConditions.
                 */
                PropertyPath geoProperty = dataSource.getGeometryProperty();

                // FROM
                sb.append( "<wfs:Query typeName='" ).append( qn.getPrefix() ).append( ":" );
                sb.append( qn.getLocalName() ).append( "'>" );

                // SELECT
                StringBuffer sbArea = GMLGeometryAdapter.exportAsEnvelope( env );

                // WHERE
                sb.append( "<ogc:Filter>" );

                // AND
                Filter filter = dataSource.getFilter();
                if ( filter != null ) {
                    if ( filter instanceof ComplexFilter ) {
                        sb.append( "<ogc:And>" );
                        sb.append( "<ogc:Intersects>" );
                        sb.append( "<wfs:PropertyName>" );
                        sb.append( geoProperty.getAsString() );
                        sb.append( "</wfs:PropertyName>" );
                        sb.append( sbArea );
                        sb.append( "</ogc:Intersects>" );
                        // add filter as defined in the layers datasource description
                        // to the filter expression
                        org.deegree.model.filterencoding.Operation op = ( (ComplexFilter) filter ).getOperation();
                        sb.append( op.to110XML() ).append( "</ogc:And>" );
                    } else {
                        if ( filter instanceof FeatureFilter ) {
                            ArrayList<FeatureId> featureIds = ( (FeatureFilter) filter ).getFeatureIds();
                            if ( featureIds.size() != 0 )
                                sb.append( "<ogc:And>" );
                            for ( FeatureId fid : featureIds ) {
                                sb.append( fid.toXML() );
                            }
                            if ( featureIds.size() != 0 )
                                sb.append( "</ogc:And>" );
                        }
                    }
                } else {
                    sb.append( "<ogc:Intersects>" );
                    sb.append( "<wfs:PropertyName>" );
                    sb.append( geoProperty.getAsString() );
                    sb.append( "</wfs:PropertyName>" );
                    sb.append( sbArea );
                    sb.append( "</ogc:Intersects>" );
                }

                sb.append( "</ogc:Filter></wfs:Query></wfs:GetFeature>" );

                Document doc = null;
                try {
                    doc = XMLTools.parse( new StringReader( sb.toString() ) );
                } catch ( Exception e ) {
                    LOG.logError( e.getMessage(), e );
                    // throw new OGCWebServiceException( e.getMessage() );
                }
                if ( doc != null ) {
                    IDGenerator idg = IDGenerator.getInstance();
                    GetFeature getFeature = GetFeature.create( String.valueOf( idg.generateUniqueID() ),
                                                               doc.getDocumentElement() );
                    LOG.logDebug( "WFS request: " + getFeature );

                    // send the request
                    response = service.doService( getFeature );
                }
            } catch ( OGCWebServiceException ogcwse ) {
                if ( !Thread.currentThread().isInterrupted() ) {
                    LOG.logError( "Exception while performing WFS-GetFeature: ", ogcwse );
                }
            }

            if ( response != null && response instanceof FeatureResult ) {
                FeatureCollection fc = (FeatureCollection) ( (FeatureResult) response ).getResponse();
                if ( fc != null ) {
                    PointsToPointListFactory ptpFac = new PointsToPointListFactory();
                    List<Point3d> heights = ptpFac.createFromFeatureCollection( fc );

                    // find the min value.
                    float min = Float.MAX_VALUE;
                    for ( Point3d p : heights ) {
                        min = Math.min( min, (float) p.z );
                    }

                    result = new float[requestHeight][requestWidth];
                    for ( float[] t : result ) {
                        Arrays.fill( t, min );
                    }

                    double scaleX = requestWidth / env.getWidth();
                    double scaleY = requestHeight / env.getHeight();
                    for ( Point3d height : heights ) {
                        int x = (int) Math.round( ( height.x - env.getMin().getX() ) * scaleX );
                        int y = (int) Math.round( requestHeight - ( ( height.y - env.getMin().getY() ) * scaleY ) );
                        float savedHeight = result[y][x];
                        if ( Math.abs( savedHeight - min ) > 1E-10 ) {
                            savedHeight += height.z;
                            result[y][x] = savedHeight * 0.5f;
                        } else {
                            result[y][x] = (float) height.z;
                        }
                    }
                }
            } else {
                LOG.logError( "ERROR creating a global heightmap while invoking wfs-datasource : "
                              + dataSource.getName() + " the result was no WFS-response or no FeatureResult instance" );
            }
        }
        return result;
    }

    /**
     * @param pos
     *            the position to get the height value
     * @return the height value of the given position or the globalMinimalHeight value if the position was outside the
     *         heightmap.
     */
    public static double getHeightForPosition( Point3d pos ) {
        int posX = (int) Math.floor( ( pos.x - configuredBBox.getMin().getX() ) * scaleMapWidth );
        int posY = (int) Math.floor( mapHeight - ( ( pos.y - configuredBBox.getMin().getY() ) * scaleMapHeight ) );
        if ( posY < 0 || posY > heightMap.length || posX < 0 || posX > heightMap[0].length ) {
            LOG.logDebug( "Given position " + pos + " is outside the global height lookup, returning minimal value: "
                          + globalMinimalHeight );
            return globalMinimalHeight;
        }
        LOG.logDebug( "The looked up value for postion: " + pos + " (mapped to: " + posX + ", " + posY + ") is: "
                      + heightMap[posY][posX] );
        return heightMap[posY][posX];
    }

    /**
     * @param dataSource
     * @param env
     * @return the heightmap created from the wcs elevation model or an empty array if no such heightmap could be
     *         created.
     */
    private float[][] invokeWCS( LocalWCSDataSource dataSource, Envelope env, int requestWidth, int requestHeight ) {
        float[][] result = new float[0][0];
        WCService service = null;
        try {
            service = (WCService) dataSource.getOGCWebService();
        } catch ( OGCWebServiceException e1 ) {
            e1.printStackTrace();
        }

        if ( service == null ) {
            LOG.logError( "No Web Coverage Service instance available for creation of the Global height map." );
        } else {

            Object coverageResponse = null;

            try {
                String crsString = env.getCoordinateSystem().getFormattedString();
                Output output = GetCoverage.createOutput( crsString, null, dataSource.getDefaultFormat(), null );

                // put missing parts in this map:
                Map<String, String> map = new HashMap<String, String>( 5 );
                StringBuffer sb = new StringBuffer( 1000 );
                Position p = env.getMin();
                sb.append( p.getX() ).append( "," ).append( p.getY() ).append( "," );
                p = env.getMax();
                sb.append( p.getX() ).append( "," ).append( p.getY() );
                map.put( "BBOX", sb.toString() );

                map.put( "WIDTH", String.valueOf( requestWidth ) );
                map.put( "HEIGHT", String.valueOf( requestHeight ) );

                SpatialSubset sps = GetCoverage.createSpatialSubset( map, crsString );

                GetCoverage filterCondition = dataSource.getCoverageFilterCondition();

                Code code = filterCondition.getDomainSubset().getRequestSRS();
                DomainSubset domainSubset = new DomainSubset( code, sps, null );

                IDGenerator idg = IDGenerator.getInstance();

                GetCoverage getCoverageRequest = new GetCoverage( String.valueOf( idg.generateUniqueID() ),
                                                                  filterCondition.getVersion(),
                                                                  filterCondition.getSourceCoverage(), domainSubset,
                                                                  null, filterCondition.getInterpolationMethod(),
                                                                  output );
                LOG.logDebug( "Sending wcs request:" + dataSource.getName() );
                coverageResponse = service.doService( getCoverageRequest );
            } catch ( WCSException wcse ) {
                if ( !Thread.currentThread().isInterrupted() ) {
                    LOG.logError( Messages.getMessage( "WPVS_WCS_REQUEST_ERROR", "WCSException", dataSource.getName(),
                                                       wcse.getMessage() ) );
                }
            } catch ( OGCWebServiceException ogcwse ) {
                if ( !Thread.currentThread().isInterrupted() ) {
                    LOG.logError( Messages.getMessage( "WPVS_WCS_REQUEST_ERROR", "OGCWebServiceException",
                                                       dataSource.getName(), ogcwse.getMessage() ) );
                }
            } catch ( Throwable t ) {
                if ( !Thread.currentThread().isInterrupted() ) {
                    t.printStackTrace();
                }
            }
            if ( coverageResponse != null && coverageResponse instanceof ResultCoverage ) {

                LOG.logDebug( "\t -> a valid response\n" );
                ResultCoverage response = (ResultCoverage) coverageResponse;
                if ( response.getCoverage() != null && response.getCoverage() instanceof ImageGridCoverage ) {
                    ImageGridCoverage igc = (ImageGridCoverage) response.getCoverage();
                    BufferedImage image = igc.getAsImage( requestWidth, requestHeight );
                    // the heightdata is in x and -y coordinates, they must be flipped before using
                    // PlanarImage im2 = JAI.create( "transpose", image, TransposeDescriptor.FLIP_VERTICAL );
                    if ( LOG.getLevel() == ILogger.LOG_DEBUG ) {
                        try {
                            File f = File.createTempFile( "global_wcs_dgm_response", ".jpg" );
                            LOG.logDebug( "creating tmpfile for global wcs elevationmodel response with name: "
                                          + f.toString() );
                            f.deleteOnExit();
                            ImageIO.write( image, "jpg", f );
                        } catch ( Exception e ) {
                            LOG.logError( e.getMessage(), e );
                        }
                    }
                    // BufferedImage heightMap = image.get;
                    Image2RawData i2rd = new Image2RawData( image, 1 );
                    result = i2rd.parse();
                }
            } else {
                LOG.logWarning( Messages.getMessage( "WPVS_IVALID_WCS_RESPONSE", dataSource.getName(),
                                                     "an ImageGridCoverage" ) );
            }
        }
        return result;
    }

    /**
     * @return an elevation datasource with the largest min scale denominator or <code>null</code> if no elevation model
     *         datasource was found.
     */
    private AbstractDataSource findLargestElevModel() {
        return findLargestElevModel( super.getDataset() );
    }

    /**
     * @param dataset
     *            to look for the elevationmodel datasources.
     * @return an elevation datasource with the largest min scale denominator or <code>null</code> if no elevation model
     *         datasource was found.
     */
    private AbstractDataSource findLargestElevModel( Dataset dataset ) {
        Dataset[] children = dataset.getDatasets();
        ElevationModel model = dataset.getElevationModel();
        AbstractDataSource elevSource = null;
        if ( model != null ) {
            AbstractDataSource[] sources = model.getDataSources();
            if ( sources != null ) {
                for ( AbstractDataSource source : sources ) {
                    if ( source != null
                         && ( elevSource == null || source.getMinScaleDenominator() > elevSource.getMinScaleDenominator() ) ) {
                        elevSource = source;
                    }
                }
            }
        }
        for ( Dataset set : children ) {
            AbstractDataSource tmpSource = findLargestElevModel( set );
            if ( tmpSource != null
                 && ( elevSource == null || tmpSource.getMinScaleDenominator() > elevSource.getMinScaleDenominator() ) ) {
                elevSource = tmpSource;
            }
        }
        return elevSource;
    }

    /**
     * @return an Offscreen canvas3D from a simple pool if it has no view and the renderer is not running.
     */
    public synchronized static Canvas3D getCanvas3D() {
        LOG.logDebug( "The pool now contains: " + canvasPool.size() + " canvasses" );
        LOG.logDebug( "The inuse pool now contains: " + inUseCanvases.size() + " canvasses" );
        for ( Canvas3D c : canvasPool ) {
            if ( !inUseCanvases.contains( c ) ) {
                if ( c != null ) {
                    View v = c.getView();
                    LOG.logDebug( "Canvas has view attached: " + v );
                    if ( v != null ) {
                        LOG.logDebug( "Removing the view from the pooled Canvas3D because it is not in use anymore." );
                        v.removeAllCanvas3Ds();
                    }
                    LOG.logDebug( "Using a pooled Canvas3D." );
                    inUseCanvases.add( c );
                    return c;
                }
            }
        }
        LOG.logDebug( "Creating a new Canvas3D, because all canvasses are in use." );
        Canvas3D tmp = createOffscreenCanvas3D();
        canvasPool.add( tmp );
        inUseCanvases.add( tmp );
        return tmp;
    }

    /**
     * @param canvas
     *            to be released.
     * @return true if the removal operation was successful, false if the given canvas3D was not in the list of used
     *         canvasses.
     */
    public synchronized static boolean releaseCanvas3D( Canvas3D canvas ) {
        if ( canvas != null ) {
            View v = canvas.getView();
            if ( v != null ) {
                LOG.logDebug( "Removing the view from the Canvas3D because it is not used anymore." );
                v.removeAllCanvas3Ds();
            }
            if ( inUseCanvases.contains( canvas ) ) {
                LOG.logDebug( "Removing the given Canvas3D from the list." );

                return inUseCanvases.remove( canvas );
            }
            LOG.logInfo( "The given canvas3D was not held by the configuration." );
        }
        LOG.logDebug( "The pool now contains: " + canvasPool.size() + " canvasses" );
        LOG.logDebug( "The inuse pool now contains: " + inUseCanvases.size() + " canvasses" );
        return false;
    }

    /**
     * creates and returns a canvas for offscreen rendering
     * 
     * @return a offscreen Canvas3D on which the the scene will be rendered.
     */
    @SuppressWarnings("unchecked")
    protected static synchronized Canvas3D createOffscreenCanvas3D() {
        GraphicsDevice[] gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
        GraphicsConfigTemplate3D gc3D = new GraphicsConfigTemplate3D();
        gc3D.setSceneAntialiasing( GraphicsConfigTemplate.PREFERRED );
        gc3D.setDoubleBuffer( GraphicsConfigTemplate.REQUIRED );

        if ( gd != null && gd.length > 0 ) {
            GraphicsConfiguration gc = gd[0].getBestConfiguration( gc3D );
            if ( gc != null ) {
                Canvas3D offScreenCanvas3D = new Canvas3D( gc, true );
                Map<String, ?> props = offScreenCanvas3D.queryProperties();
                if ( props.containsKey( tusm ) ) {
                    Integer tus = (Integer) props.get( tusm );
                    if ( tus != null ) {
                        availableTextureUnitStates = tus.intValue();
                    }
                }

                if ( props.containsKey( twm ) ) {
                    Integer tw = (Integer) props.get( twm );
                    if ( tw != null ) {
                        texture2DMaxSize = tw.intValue();
                    }
                }

                if ( LOG.getLevel() == ILogger.LOG_DEBUG ) {
                    Set<String> keys = props.keySet();
                    StringBuilder sb = new StringBuilder( "Canvas3D has following properties:\n" );
                    for ( String key : keys ) {
                        sb.append( key ).append( " : " ).append( props.get( key ) ).append( "\n" );
                    }
                    LOG.logDebug( sb.toString() );
                }
                return offScreenCanvas3D;
            }
            LOG.logError( "Could not get a GraphicsConfiguration from the graphics environment, cannot create a canvas3d." );
        } else {
            LOG.logError( "Could not get a graphicsdevice to create a canvas3d." );
        }
        return null;
    }

    /**
     * @return Returns the deegreeParams.
     */
    public WPVSDeegreeParams getDeegreeParams() {
        return deegreeParams;
    }

    /**
     * @param deegreeParams
     *            The deegreeParams to set.
     */
    public void setDeegreeParams( WPVSDeegreeParams deegreeParams ) {
        this.deegreeParams = deegreeParams;
    }

    /**
     * @return the smallestMinimalScaleDenomiator value.
     */
    public double getSmallestMinimalScaleDenomiator() {
        return smallestMinimalScaleDenomiator;
    }
}
