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

package org.deegree.ogcwebservices.wmps;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperPrintManager;

import org.deegree.datatypes.values.Values;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.ImageUtils;
import org.deegree.framework.util.MapUtils;
import org.deegree.framework.util.StringTools;
import org.deegree.framework.xml.NamespaceContext;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.framework.xml.XMLTools;
import org.deegree.graphics.sld.StyledLayerDescriptor;
import org.deegree.graphics.transformation.WorldToScreenTransform;
import org.deegree.i18n.Messages;
import org.deegree.model.coverage.grid.WorldFile;
import org.deegree.model.crs.CRSFactory;
import org.deegree.model.crs.CoordinateSystem;
import org.deegree.model.crs.UnknownCRSException;
import org.deegree.model.spatialschema.Envelope;
import org.deegree.model.spatialschema.GeometryFactory;
import org.deegree.model.spatialschema.Point;
import org.deegree.ogcbase.CommonNamespaces;
import org.deegree.ogcwebservices.InconsistentRequestException;
import org.deegree.ogcwebservices.OGCWebServiceException;
import org.deegree.ogcwebservices.wmps.configuration.PrintMapParam;
import org.deegree.ogcwebservices.wmps.configuration.WMPSConfiguration;
import org.deegree.ogcwebservices.wmps.operation.PrintMap;
import org.deegree.ogcwebservices.wmps.operation.PrintMapResponseDocument;
import org.deegree.ogcwebservices.wmps.operation.TextArea;
import org.deegree.ogcwebservices.wms.capabilities.Layer;
import org.deegree.ogcwebservices.wms.capabilities.LegendURL;
import org.deegree.ogcwebservices.wms.capabilities.ScaleHint;
import org.deegree.ogcwebservices.wms.capabilities.Style;
import org.deegree.ogcwebservices.wms.configuration.AbstractDataSource;
import org.deegree.ogcwebservices.wms.configuration.LocalWCSDataSource;
import org.deegree.ogcwebservices.wms.configuration.RemoteWCSDataSource;
import org.deegree.ogcwebservices.wms.configuration.RemoteWMSDataSource;
import org.deegree.ogcwebservices.wms.operation.GetMap;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Handles the PrintMap request. Retrieves the request from the DB and creates a pdf file.
 * 
 * @author <a href="mailto:deshmukh@lat-lon.de">Anup Deshmukh</a>
 * @author last edited by: $Author$
 * 
 * @version 2.0, $Revision$, $Date$
 */
public class PrintMapHandler implements Runnable {

    private static final ILogger LOG = LoggerFactory.getLogger( PrintMapHandler.class );

    private final double TILE_MAX_SIZE = 800;

    private final String FORMAT = ".png";

    private final String MIMETYPE = "image/png";

    private final String EXCEPTION = "application/vnd.ogc.se_inimage";

    private WMPSConfiguration configuration;

    private String message;

    /**
     * Creates a new instance of the PrintMapHandler for the current configuration.
     * 
     * @param configuration
     */
    public PrintMapHandler( WMPSConfiguration configuration ) {
        this.configuration = configuration;
    }

    /**
     * Run a new thread for each PrintMap request. This Thread runs till no more PrintMap requests are available in the
     * DB.
     * 
     * @see java.lang.Runnable#run()
     */
    public void run() {

        RequestManager manager = null;
        PrintMapResponseDocument response = null;
        WMPSDatabase wmpsDB = null;
        Connection connection = null;
        try {
            wmpsDB = new WMPSDatabase( this.configuration.getDeegreeParams().getCacheDatabase() );

            while ( true ) {
                connection = wmpsDB.acquireConnection();
                // get request from DB
                PrintMap printMap = wmpsDB.selectPrintMapRequest( connection );
                if ( printMap == null ) {
                    break;
                }

                try {
                    LOG.logDebug( "Performing print map" );
                    manager = new DefaultRequestManager( this.configuration, printMap );
                    performPrintMap( printMap, false );
                    wmpsDB.updateDB( connection, printMap.getId(), printMap.getTimestamp(), "TRUE" );
                    response = manager.createFinalResponse( this.message, null );
                    manager.sendEmail( response );
                } catch ( Exception e ) {
                    LOG.logError( e.getMessage(), e );
                    wmpsDB.updateDB( connection, printMap.getId(), printMap.getTimestamp(), "FAILED" );
                    sendExceptionMail( manager, response, e );
                } finally {
                    releaseConnection( wmpsDB, connection );
                }
                LOG.logDebug( "Done performing PrintMap request." );
            }
        } catch ( Exception e ) {
            LOG.logError( e.getMessage(), e );
            sendExceptionMail( manager, response, e );
        } finally {
            releaseConnection( wmpsDB, connection );
        }
    }

    private void sendExceptionMail( RequestManager manager, PrintMapResponseDocument response, Exception e ) {
        if ( manager != null ) {
            try {
                response = manager.createFinalResponse( "print map failed", e.getMessage() );
                manager.sendEmail( response );
            } catch ( Exception e1 ) {
                // should just happen if mail server is not reachable
                // in this case the error just can be logged
                XMLFragment doc = new XMLFragment( response.getRootElement() );
                LOG.logDebug( doc.getAsString() );
                LOG.logError( e.getMessage(), e );
            }
        }
    }

    private void releaseConnection( WMPSDatabase wmpsDB, Connection connection ) {
        try {
            if ( !connection.isClosed() ) {
                wmpsDB.releaseConnection( connection );
            }
        } catch ( SQLException e ) {
            // should never happen
            LOG.logError( e.getMessage(), e );
        }
    }

    /**
     * performs a synchronous printMap processing
     * 
     * @param printMap
     * @return byte[]
     * @throws Exception
     */
    public byte[] runSynchronous( PrintMap printMap )
                            throws Exception {
        return performPrintMap( printMap, true );
    }

    /**
     * From each PrintMap request run the WMS GetMap request.
     * 
     * @param printMap
     * @param synchronous
     * @return byte[]
     * @throws PrintMapServiceException
     * @throws IOException
     */
    private byte[] performPrintMap( PrintMap printMap, boolean synchronous )
                            throws PrintMapServiceException, IOException {

        Map<String, Layer> config_layers = retrieveLayersFromConfig( printMap );

        int[] mapParams = getMapParamsFromTemplate( printMap );
        int scaleDenominator = printMap.getScaleDenominator();
        Envelope bbox = printMap.getBBOX();
        if ( bbox == null ) {
            LOG.logDebug( "BBOX not defined. Using the center and scale to calculate a new BBOX." );
            Point center = printMap.getCenter();
            bbox = createBBOX( printMap, center, scaleDenominator, mapParams[0], mapParams[1] );
        } else { // Adjust aspect ratio of bbox to aspect ratio of template map area
            double bboxAspectRatio = bbox.getWidth() / bbox.getHeight();
            double printAspectRatio = ( (double) mapParams[0] ) / mapParams[1];
            if ( bboxAspectRatio > printAspectRatio ) {
                double centerY = bbox.getMin().getY() + ( ( bbox.getMax().getY() - bbox.getMin().getY() ) / 2d );
                double height = bbox.getWidth() * ( 1.0 / printAspectRatio );
                double minY = centerY - ( height / 2d );
                double maxY = centerY + ( height / 2d );
                bbox = GeometryFactory.createEnvelope( bbox.getMin().getX(), minY, bbox.getMax().getX(), maxY,
                                                       bbox.getCoordinateSystem() );
            } else {
                double centerX = bbox.getMin().getX() + ( ( bbox.getMax().getX() - bbox.getMin().getX() ) / 2d );
                double width = bbox.getHeight() * printAspectRatio;
                double minX = centerX - ( width / 2d );
                double maxX = centerX + ( width / 2d );
                bbox = GeometryFactory.createEnvelope( minX, bbox.getMin().getY(), maxX, bbox.getMax().getY(),
                                                       bbox.getCoordinateSystem() );
            }
        }
        if ( scaleDenominator == -1 ) {
            LOG.logDebug( "Scale not defined. Using MapUtil to calculate the scale denominator for the current bbox and map sizes" );
            double pixelSize = WMPSConfiguration.INCH2M
                               / configuration.getDeegreeParams().getPrintMapParam().getTargetResolution();
            scaleDenominator = (int) MapUtils.calcScale( mapParams[0], mapParams[1], bbox, bbox.getCoordinateSystem(),
                                                         pixelSize );
            LOG.logDebug( "calculated map scale denominator: ", scaleDenominator );
        }
        BufferedImage mapImage = null;
        try {
            mapImage = performBuildMapImage( config_layers, printMap, bbox, mapParams[0], mapParams[1] );
            saveImageToDisk( printMap, mapImage, "MAP", bbox );
            String s = StringTools.concat( 100, "Retrieved PrintMap request '", printMap.getId(), "' and saved to disk" );
            LOG.logDebug( s );
        } catch ( OGCWebServiceException e ) {
            LOG.logError( e.getMessage(), e );
            throw new PrintMapServiceException( Messages.getMessage( "WMPS_ERROR_PERFORMING_PRINTMAP" ) );
        } catch ( IOException e ) {
            LOG.logError( e.getMessage(), e );
            throw new PrintMapServiceException( Messages.getMessage( "WMPS_ERROR_WRITING_MAP_TMP_FILE" ) );
        }
        if ( printMap.getLegend() ) {
            try {
                BufferedImage legendImage = performGetLegend( config_layers, printMap, mapParams );
                saveImageToDisk( printMap, legendImage, "LEGEND", null );
                LOG.logDebug( "Saved the legend image file to disk." );
            } catch ( IOException e ) {
                LOG.logError( e.getMessage(), e );
                throw new PrintMapServiceException( Messages.getMessage( "WMPS_ERROR_WRITING_LEGEND_TMP_FILE" ) );
            }
        }
        return exportOutput( printMap, scaleDenominator, synchronous );
    }

    /**
     * Build the Map image for the current PrintMap request. Vector and Raster layers are handled seperately.
     * 
     * @param config_layers
     * @param printMap
     * @param bbox
     * @param width
     * @param height
     * @return BufferedImage
     * @throws OGCWebServiceException
     */
    private BufferedImage performBuildMapImage( Map<String, Layer> config_layers, PrintMap printMap, Envelope bbox,
                                                double width, double height )
                            throws OGCWebServiceException {

        BufferedImage targetImage = new BufferedImage( (int) width, (int) height, BufferedImage.TYPE_INT_ARGB );
        Graphics2D g = (Graphics2D) targetImage.getGraphics();

        if ( !printMap.getTransparent() ) {
            g.setBackground( printMap.getBGColor() );
        }

        handleLayerDatasources( config_layers, printMap, bbox, width, height, g );
        g.dispose();

        return targetImage;
    }

    /**
     * Determines the datasource for each layer(vector, raster).
     * 
     * @param config_layers
     * @param printMap
     * @param bbox
     * @param width
     * @param height
     * @param g
     * @throws OGCWebServiceException
     * @throws InconsistentRequestException
     */
    private void handleLayerDatasources( Map<String, Layer> config_layers, PrintMap printMap, Envelope bbox,
                                         double width, double height, Graphics g )
                            throws OGCWebServiceException, InconsistentRequestException {

        int dpi = printMap.getDpi();
        if ( dpi < 0 ) {
            dpi = configuration.getDeegreeParams().getPrintMapParam().getTargetResolution();
        }
        double px = WMPSConfiguration.INCH2M / dpi;
        CoordinateSystem crs;
        try {
            crs = CRSFactory.create( printMap.getSRS() );
        } catch ( UnknownCRSException e1 ) {
            throw new InconsistentRequestException( e1.getMessage() );
        }
        double scale = MapUtils.calcScale( (int) width, (int) height, bbox, crs, px );
        GetMap.Layer[] printMapLayers = printMap.getLayers();
        for ( int i = 0; i < printMapLayers.length; i++ ) {
            String name = printMapLayers[i].getName();

            Layer configLayer = config_layers.get( name );
            ScaleHint scaleHint = configLayer.getScaleHint();
            if ( LOG.getLevel() == ILogger.LOG_DEBUG ) {
                LOG.logDebug( "current scale", scale );
                LOG.logDebug( "valid layer min scale", scaleHint.getMin() );
                LOG.logDebug( "valid layer max scale", scaleHint.getMax() );
            }
            if ( scale >= scaleHint.getMin() && scale < scaleHint.getMax() ) {
                String type = determineDatasourceType( configLayer, scale );
                if ( type != null && "vector".equalsIgnoreCase( type ) ) {

                    GetMap.Layer[] lay = null;
                    if ( ( printMapLayers.length - i ) > 1 ) {
                        lay = getContinuousVectorLayer( config_layers, printMapLayers, scale, i );
                    } else {
                        lay = new GetMap.Layer[] { printMapLayers[i] };
                    }
                    try {
                        for ( int j = 0; j < lay.length; j++ ) {
                            LOG.logDebug( "handle as vector layer: ", lay[j].getName() );
                        }
                        handleVectorDataLayer( printMap, bbox, width, height, g, lay );
                    } catch ( OGCWebServiceException e ) {
                        LOG.logError( e.getMessage(), e );
                        throw new OGCWebServiceException( Messages.getMessage( "WMPS_ERROR_HANDLING_GETMAP", name ) );
                    }
                    // Skip the number of layers already handled.
                    if ( lay.length != 1 ) {
                        i = i + ( lay.length - 1 );
                    }
                } else {
                    // must be a raster data layer
                    GetMap.Layer[] lay = new GetMap.Layer[] { printMapLayers[i] };
                    LOG.logDebug( "handle as raster layer: ", printMapLayers[i].getName() );
                    try {
                        handleRasterDataLayer( printMap, bbox, width, height, g, lay );
                    } catch ( OGCWebServiceException e ) {
                        LOG.logError( e.getMessage(), e );
                        throw new OGCWebServiceException( Messages.getMessage( "WMPS_ERROR_HANDLING_GETMAP", name ) );
                    }
                }
            } else {
                String s = StringTools.concat( 100, "No Datasource available for layer: ", name, " at scale: ", scale );
                LOG.logInfo( s );
            }
        }

    }

    /**
     * returns an array of layers that:
     * <ul>
     * <li>a) made of vector data
     * <li>b) are continous in the requested list of layers
     * </ul>
     * 
     * @param config_layers
     * @param printMapLayers
     * @param mapScale
     *            scale of the entire map
     * @param i
     * @return Layer[]
     */
    private GetMap.Layer[] getContinuousVectorLayer( Map<String, Layer> config_layers, GetMap.Layer[] printMapLayers,
                                                     double mapScale, int i ) {

        List<GetMap.Layer> layers = new ArrayList<GetMap.Layer>( printMapLayers.length );
        int counter = 0;
        for ( ; i < printMapLayers.length; i++ ) {
            String name = printMapLayers[i].getName();
            Layer configLayer = config_layers.get( name );
            String type = determineDatasourceType( configLayer, mapScale );
            if ( "vector".equals( type ) ) {
                layers.add( counter, printMapLayers[i] );
                counter++;
            } else {
                break;
            }
        }

        return layers.toArray( new GetMap.Layer[layers.size()] );
    }

    /**
     * Perform the GetMap request for vector layers.
     * 
     * @param printMap
     * @param bbox
     * @param width
     * @param height
     * @param g
     * @param lay
     * @throws OGCWebServiceException
     */
    private void handleVectorDataLayer( PrintMap printMap, Envelope bbox, double width, double height, Graphics g,
                                        GetMap.Layer[] lay )
                            throws OGCWebServiceException {

        URL sldURL = null;
        StyledLayerDescriptor sld = null;

        GetMap getMap = GetMap.create( printMap.getVersion(), printMap.getId(), lay, null, null, this.MIMETYPE,
                                       (int) width, (int) height, printMap.getSRS(), bbox, printMap.getTransparent(),
                                       printMap.getBGColor(), this.EXCEPTION, null, sldURL, sld,
                                       printMap.getVendorSpecificParameters() );
        DefaultGetMapHandler gmh = new DefaultGetMapHandler( this.configuration, getMap );
        gmh.performGetMap( printMap, g );

    }

    /**
     * Perform the GetMap request for each raster layer. Here the raster layer is divided into tiles for memory handling
     * efficiency.
     * 
     * @param printMap
     * @param bbox
     * @param width
     * @param height
     * @param g
     * @param lay
     * @throws OGCWebServiceException
     */
    private void handleRasterDataLayer( PrintMap printMap, Envelope bbox, double width, double height, Graphics g,
                                        GetMap.Layer[] lay )
                            throws OGCWebServiceException {

        // Get Map (missing) parameters.
        Values elevation = null;
        Map<String, Values> sampleDimension = null;
        Values time = null;
        URL sldURL = null;
        StyledLayerDescriptor sld = null;

        boolean xRemainder = false;
        int wtx = (int) width % (int) this.TILE_MAX_SIZE;
        int nkx = (int) width / (int) this.TILE_MAX_SIZE;
        if ( wtx > 0 ) {
            xRemainder = true;
            nkx++;
        }

        boolean yRemainder = false;
        int wty = (int) height % (int) this.TILE_MAX_SIZE;
        int nky = (int) height / (int) this.TILE_MAX_SIZE;
        if ( wty > 0 ) {
            yRemainder = true;
            nky++;
        }

        WorldToScreenTransform trans = new WorldToScreenTransform( bbox.getMin().getX(), bbox.getMin().getY(),
                                                                   bbox.getMax().getX(), bbox.getMax().getY(), 0d, 0d,
                                                                   width - 1, height - 1 );

        for ( int x = 0; x < nkx; x++ ) {
            double tileWidth = this.TILE_MAX_SIZE;
            if ( xRemainder ) {
                if ( x == nkx - 1 ) {
                    tileWidth = wtx;
                }
            }
            for ( int y = 0; y < nky; y++ ) {
                double tileHeight = this.TILE_MAX_SIZE;
                if ( yRemainder ) {
                    if ( y == nky - 1 ) {
                        tileHeight = wty;
                    }
                }
                BufferedImage bi = new BufferedImage( (int) tileWidth, (int) tileHeight, BufferedImage.TYPE_INT_ARGB );
                Graphics tileg = bi.getGraphics();
                // calc bbox
                Envelope bb = calculateTileBBOX( trans, x, y, tileWidth, tileHeight, bbox.getCoordinateSystem() );
                // create GetMap
                GetMap getMap = GetMap.create( printMap.getVersion(), printMap.getId(), lay, elevation,
                                               sampleDimension, this.MIMETYPE, (int) tileWidth, (int) tileHeight,
                                               printMap.getSRS(), bb, printMap.getTransparent(), printMap.getBGColor(),
                                               this.EXCEPTION, time, sldURL, sld,
                                               printMap.getVendorSpecificParameters() );

                // performGetMap( tileg );
                DefaultGetMapHandler gmh = new DefaultGetMapHandler( this.configuration, getMap );
                gmh.performGetMap( printMap, tileg );
                tileg.dispose();
                g.drawImage( bi, (int) Math.round( x * this.TILE_MAX_SIZE ),
                             (int) Math.round( y * this.TILE_MAX_SIZE ), (int) Math.round( tileWidth ),
                             (int) Math.round( tileHeight + 1 ), null );
            }
        }

    }

    /**
     * Calculate the tile BBOX for the raster datalayer.
     * 
     * @param trans
     * @param x
     * @param y
     * @param tileWidth
     * @param tileHeight
     * @param crs
     * @return Envelope
     */
    private Envelope calculateTileBBOX( WorldToScreenTransform trans, int x, int y, double tileWidth,
                                        double tileHeight, CoordinateSystem crs ) {

        double x1 = x * this.TILE_MAX_SIZE;
        double y1 = y * this.TILE_MAX_SIZE;
        double x2 = x1 + tileWidth;
        double y2 = y1 + tileHeight;

        double minX = trans.getSourceX( x1 );
        double maxX = trans.getSourceX( x2 );
        double minY = trans.getSourceY( y2 );
        double maxY = trans.getSourceY( y1 );

        return GeometryFactory.createEnvelope( minX, minY, maxX, maxY, crs );
    }

    /**
     * Parses the Layer datastores to determine the type of the layer. Layers having a vector datasource as well as a
     * raster datasource for the passed mapScale will be treated as raster layers
     * 
     * @param layer
     * @param mapScale
     *            scale of the entire map
     * @return String either raster, vector or nodatasource
     */
    private String determineDatasourceType( Layer layer, double mapScale ) {

        AbstractDataSource[] ads = layer.getDataSource();
        String type = null;

        boolean[] mixed = new boolean[] { false, false };
        for ( int i = 0; i < ads.length; i++ ) {
            ScaleHint scaleHint = ads[i].getScaleHint();
            if ( LOG.getLevel() == ILogger.LOG_DEBUG ) {
                LOG.logDebug( "mapscale", mapScale );
                LOG.logDebug( "valid datasource min scale", scaleHint.getMin() );
                LOG.logDebug( "valid datasource max scale", scaleHint.getMax() );
            }
            if ( mapScale >= scaleHint.getMin() && mapScale < scaleHint.getMax() ) {
                if ( ( ads[i] instanceof RemoteWMSDataSource ) || ( ads[i] instanceof RemoteWCSDataSource )
                     || ( ads[i] instanceof LocalWCSDataSource ) ) {
                    type = "raster";
                    mixed[0] = true;
                } else {
                    type = "vector";
                    mixed[1] = true;
                }
            }
        }
        if ( mixed[0] && mixed[1] ) {
            // Layers having a vector datasource as well as a raster datasource
            // for the passed mapScale will be treated as raster layers
            type = "raster";
        }

        return type;
    }

    /**
     * Retrieve the legend images from the URL given in the configuration layers.
     * 
     * 
     * @param layerDefs
     * @param printMap
     * @param mapParams
     * @return BufferedImage
     * @throws OGCWebServiceException
     * @throws InconsistentRequestException
     */
    private BufferedImage performGetLegend( Map<String, Layer> layerDefs, PrintMap printMap, int[] mapParams ) {

        GetMap.Layer[] layers = printMap.getLayers();

        Map<String, Image> legendImg = new HashMap<String, Image>( layers.length );
        int height = 0;
        int maxWidth = 0;
        for ( int i = 0; i < layers.length; i++ ) {
            String name = layers[i].getName();
            String styleName = layers[i].getStyleName();

            Layer configLayer = layerDefs.get( name );
            Style style = null;
            if ( "$DEFAULT".equals( styleName ) ) {
                style = configLayer.getStyleResource( "default:" + name );
            } else {
                style = configLayer.getStyleResource( styleName );
            }
            LegendURL[] lu = style.getLegendURL();
            if ( lu != null && lu.length > 0 ) {
                int k = 0;
                boolean drawn = false;
                while ( k < lu.length && !drawn ) {
                    URL url = lu[k++].getOnlineResource();
                    try {
                        Image img = ImageUtils.loadImage( url );
                        legendImg.put( name, img );
                        drawn = true;
                    } catch ( IOException e ) {
                        // we do not throw the exception bacause there are maybe
                        // further URLs we can try and even if not the user will
                        // be informed by a special legend symbol that no correct
                        // symbol can be accessed
                        LOG.logError( "can not access LegendURL: " + url, e );
                    } catch ( Exception e ) {
                        LOG.logError( "can not read image from LegendURL: " + url, e );
                    }
                }
                if ( !drawn ) {
                    // if legend URL(s) are defined but none of them can
                    // be accessed
                    String s = StringTools.concat( 100, "no legend URL accessable for layer: ", name, "; style: ",
                                                   styleName, " using dummy legend image" );
                    LOG.logError( s );
                    BufferedImage img = drawMissingLegendURLImage( s );
                    legendImg.put( name, img );
                }
            } else {
                // if no legend URL has been defined which probably is the case
                // for WMS no supporting GetLegendGraphic operation
                String s = StringTools.concat( 100, "no legend URL available for layer: ", name, "; style: ",
                                               styleName, " using dummy legend image" );
                LOG.logError( s );
                BufferedImage img = drawMissingLegendURLImage( s );
                legendImg.put( name, img );
            }
            // update all over legend height and width
            BufferedImage img = (BufferedImage) legendImg.get( name );
            if ( img.getWidth() > maxWidth ) {
                maxWidth = img.getWidth();
            }
            height += img.getHeight();
        }

        // depending on the size of the legend all legend symbols must scaled by
        // the same factor to fit the legend size defined in the current template
        double dh = calcDeltaLegend( mapParams[2], mapParams[3], height, maxWidth );

        // create an empty basic image as target for painting all legend symbols
        BufferedImage actualLegendImage = null;
        if ( mapParams[2] > 0 && mapParams[3] > 0 ) {
            actualLegendImage = new BufferedImage( mapParams[2], mapParams[3], BufferedImage.TYPE_INT_ARGB );

            Graphics2D g = (Graphics2D) actualLegendImage.getGraphics();

            int y = 0;
            for ( int i = layers.length; i > 0; i-- ) {
                // draw all legend symbols in correct order
                String name = layers[i - 1].getName();
                BufferedImage img = scaleImage( (BufferedImage) legendImg.get( name ), dh );
                g.drawImage( img, 0, y, null );
                y += img.getHeight();
            }

            g.dispose();
        } else {
            // create empty legend image if size is not valid
            actualLegendImage = new BufferedImage( 10, 10, BufferedImage.TYPE_INT_ARGB );
        }

        return actualLegendImage;
    }

    private BufferedImage drawMissingLegendURLImage( String text ) {
        BufferedImage img = new BufferedImage( 550, 50, BufferedImage.TYPE_INT_ARGB );
        Graphics g = img.getGraphics();
        g.setColor( Color.YELLOW );
        g.fillRect( 0, 0, img.getWidth(), img.getHeight() );
        g.setColor( Color.RED );
        g.drawString( text, 10, 20 );
        g.dispose();
        return img;
    }

    /**
     * calculates factor for resizing legend images
     * 
     * @param legendWidth
     *            The width of the legend area
     * @param legendHeight
     *            The height of the legend area
     * @param height
     *            The height of all legends put together
     * @param maxWidth
     *            The width of the wides legend
     * @return Returns the factor for resizing legend images
     */
    private double calcDeltaLegend( int legendWidth, int legendHeight, int height, int maxWidth ) {
        double dh = legendHeight / (double) height;
        double dw = legendWidth / (double) maxWidth;
        if ( dw < dh ) {
            return dw;
        }
        return dh;
    }

    /**
     * Scale Image.
     * 
     * @param image
     * @param ratio
     * @return BufferedImage
     */
    private BufferedImage scaleImage( BufferedImage image, double ratio ) {

        AffineTransform tx = new AffineTransform();
        tx.scale( ratio, ratio );
        AffineTransformOp op = new AffineTransformOp( tx, AffineTransformOp.TYPE_BILINEAR );

        return op.filter( image, null );
    }

    /**
     * Save the GetMap image to the disk.
     * 
     * @param printMap
     * @param image
     * @param type
     * @param bbox
     * @throws IOException
     */
    private void saveImageToDisk( PrintMap printMap, BufferedImage image, String type, Envelope bbox )
                            throws IOException {

        PrintMapParam printMapParam = this.configuration.getDeegreeParams().getPrintMapParam();
        String fileName = null;
        String templateName = printMap.getTemplate();
        if ( type.equalsIgnoreCase( "MAP" ) ) {
            fileName = StringTools.concat( 200, "Map_", templateName, '_', printMap.getId(), this.FORMAT );
            WorldFile wf = new WorldFile( bbox.getWidth() / image.getWidth(), bbox.getHeight() / image.getHeight(), 0,
                                          0, bbox );
            String wffn = StringTools.concat( 200, "Map_", templateName, '_', printMap.getId(), ".wld" );
            URL url = new URL( printMapParam.getPlotImageDir() + '/' + wffn );
            OutputStream os = null;
            try {
                os = new FileOutputStream( new File( url.toURI() ) );
            } catch ( URISyntaxException e ) {
                // should never happen because each valid URL is a valid URI too
                LOG.logError( e.getMessage(), e );
            }
            WorldFile.writeWorldFile( os, wf );
        } else if ( type.equalsIgnoreCase( "LEGEND" ) ) {
            fileName = StringTools.concat( 200, "Legend_", templateName, '_', printMap.getId(), this.FORMAT );
        }

        String path = printMapParam.getPlotImageDir() + '/' + fileName;
        URL downloadDirectory = new URL( path );

        try {
            ImageUtils.saveImage( image, new File( downloadDirectory.toURI() ), 1 );
        } catch ( URISyntaxException e ) {
            // should never happen because each valid URL is a valid URI too
            LOG.logError( e.getMessage(), e );
        }

    }

    /**
     * Use JasperReports to create a pdf file. The Jasper Template will be loaded and a dynamic link will be created to
     * the image on the disk.
     * 
     * @param printMap
     * @param scaleDenominator
     * @param synchronous
     * @return byte[]
     * @throws PrintMapServiceException
     */
    private byte[] exportOutput( PrintMap printMap, int scaleDenominator, boolean synchronous )
                            throws PrintMapServiceException {

        // generate a file using JasperReports.
        byte[] b = null;
        try {
            JasperPrint print = fillJasperTemplate( printMap, scaleDenominator );
            b = doJasperPrintExport( printMap, print, synchronous );
        } catch ( Exception e ) {
            LOG.logError( e.getMessage(), e );
            throw new PrintMapServiceException( Messages.getMessage( "WMPS_ERROR_SAVING_PDF" ) );
        }

        return b;
    }

    /**
     * Open the JasperAPI to process the PrintMap request.
     * 
     * @param printMap
     * @param scaleDenominator
     * @return JasperPrint
     * @throws InconsistentRequestException
     * @throws JRException
     * @throws MalformedURLException
     */
    private JasperPrint fillJasperTemplate( PrintMap printMap, int scaleDenominator )
                            throws JRException, MalformedURLException {

        URL templatePath = null;
        try {
            String templateName = printMap.getTemplate();
            templatePath = getTemplatePath( templateName, true );
        } catch ( IOException e ) {
            LOG.logError( e.getMessage(), e );
            throw new MalformedURLException( Messages.getMessage( "WMPS_ERROR_CREATING_TEMPLATEPATH",
                                                                  printMap.getTemplate() ) );
        }

        Map<String, Object> parameters = new HashMap<String, Object>();
        URL mapImagePath = getResultImagePath( printMap, "Map" );
        parameters.put( "MAP", mapImagePath.getFile() );
        if ( printMap.getLegend() ) {
            URL legendImagePath = getResultImagePath( printMap, "Legend" );
            parameters.put( "LEGEND", legendImagePath.getFile() );
        }

        String scale = "1:" + scaleDenominator;

        if ( printMap.getScaleBar() == true ) {
            parameters.put( "SCALE", scale );
        }

        TextArea[] textAreas = printMap.getTextAreas();
        if ( textAreas != null && textAreas.length > 0 ) {
            for ( int i = 0; i < textAreas.length; i++ ) {
                TextArea textArea = textAreas[i];
                LOG.logDebug( "Names and text fields entered, extracted." );
                String name = textArea.getName();
                String text = textArea.getText();
                if ( name != null ) {
                    LOG.logDebug( "If name is not null, allocate it to the hashmap 'parameters' in uppercase." );
                    parameters.put( name.toUpperCase(), text );
                    parameters.put( name, text );
                }
            }
        }
        String title = printMap.getTitle();
        if ( title != null ) {
            parameters.put( "TITLE", title );
        }
        String copyright = printMap.getCopyright();
        if ( copyright != null ) {
            parameters.put( "COPYRIGHT", copyright );
        }
        String note = printMap.getNote();
        if ( note != null ) {
            parameters.put( "NOTE", note );
        }
        LOG.logDebug( "JASPER Parameter: ", parameters );
        JasperPrint print = null;
        try {
            print = JasperFillManager.fillReport( templatePath.getFile(), parameters, new JREmptyDataSource() );
        } catch ( JRException e ) {
            LOG.logError( e.getMessage(), e );
            throw new JRException( Messages.getMessage( "WMPS_ERROR_BUILDING_TEMPLATE", templatePath ) );
        }

        return print;
    }

    /**
     * Retrieve the result map image file path for the current request.
     * 
     * @param printMap
     * @param type
     * @return URL
     * @throws MalformedURLException
     */
    private URL getResultImagePath( PrintMap printMap, String type )
                            throws MalformedURLException {

        String templateName = printMap.getTemplate();
        String fileName = type + "_" + templateName + "_" + printMap.getId() + this.FORMAT;
        PrintMapParam printMapParam = this.configuration.getDeegreeParams().getPrintMapParam();
        String path = printMapParam.getPlotImageDir() + '/' + fileName;
        URL imagePath = new URL( path );

        return imagePath;

    }

    /**
     * Print the layer to a the specified format.
     * 
     * @param printMap
     * @param print
     * @param synchronous
     * @return byte[]
     * @throws JRException
     * @throws IOException
     */
    private byte[] doJasperPrintExport( PrintMap printMap, JasperPrint print, boolean synchronous )
                            throws JRException, IOException {

        String format = this.configuration.getDeegreeParams().getPrintMapParam().getFormat();
        String templateName = printMap.getTemplate();
        String filename = StringTools.concat( 200, format, '_', templateName, '_', printMap.getId(), '.', format );
        String directory = this.configuration.getDeegreeParams().getPrintMapParam().getPlotDirectory();

        URL downloadFile = new URL( directory + '/' + filename );

        byte[] b = null;
        try {
            if ( synchronous ) {
                b = doSynchronousProcessing( print, format );
            } else {
                doSaveResultDocument( print, format, downloadFile );
                createMailLink( filename );
            }
        } catch ( JRException e ) {
            LOG.logError( e.getMessage(), e );
            throw new JRException( Messages.getMessage( "WMPS_ERROR_PRINTING_REPORT", format, downloadFile.getFile() ) );
        }

        return b;
    }

    /**
     * Create a mail link to be sent to the user email address. The mail link allows the user to open the pdf document
     * for viewing and downloading purposes. Here 2 cases are taken into consideration
     * <ul>
     * <li>An authentification servlet link will be sent to the client.
     * <li>A direct access to the clients data file.
     * </ul>
     * 
     * @param printMap
     * @param filename
     */
    private void createMailLink( String filename ) {
        PrintMapParam pmp = this.configuration.getDeegreeParams().getPrintMapParam();
        String onlineResource = pmp.getOnlineResource();
        String template = pmp.getMailTextTemplate();
        this.message = MessageFormat.format( template, new Object[] { onlineResource, filename.trim() } );
    }

    /**
     * Save the result document using the JasperExportManager to the file specified.
     * 
     * @param print
     * @param format
     * @param downloadDirectory
     * @throws JRException
     * @throws IOException
     */
    private void doSaveResultDocument( JasperPrint print, String format, URL downloadDirectory )
                            throws JRException, IOException {
        if ( format.equalsIgnoreCase( "pdf" ) ) {
            LOG.logDebug( "Exporting as pdf to file " + downloadDirectory.getFile() );
            JasperExportManager.exportReportToPdfFile( print, downloadDirectory.getFile() );
        } else if ( format.equalsIgnoreCase( "html" ) ) {
            LOG.logDebug( "Exporting as html to file " + downloadDirectory.getFile() );
            JasperExportManager.exportReportToHtmlFile( print, downloadDirectory.getFile() );
        } else if ( format.equalsIgnoreCase( "xml" ) ) {
            LOG.logDebug( "Exporting as xml to file " + downloadDirectory.getFile() );
            JasperExportManager.exportReportToXmlFile( print, downloadDirectory.getFile(), false );
        } else if ( format.equalsIgnoreCase( "png" ) ) {
            LOG.logDebug( "Exporting as xml to file " + downloadDirectory.getFile() );
            Image image = JasperPrintManager.printPageToImage( print, 0, 1 );
            ImageUtils.saveImage( (BufferedImage) image, new File( downloadDirectory.getFile() ), 1f );
        }

    }

    /**
     * Export the result document to a stream to be returend to the waiting client.
     * 
     * @param print
     * @param format
     * @return byte[]
     * @throws JRException
     */
    private byte[] doSynchronousProcessing( JasperPrint print, String format )
                            throws JRException {

        byte[] b;
        ByteArrayOutputStream bos = new ByteArrayOutputStream( 100000 );
        if ( format.equalsIgnoreCase( "pdf" ) ) {
            JasperExportManager.exportReportToPdfStream( print, bos );
        } else if ( format.equalsIgnoreCase( "xml" ) ) {
            JasperExportManager.exportReportToXmlStream( print, bos );
        }
        b = bos.toByteArray();

        return b;
    }

    /**
     * Retrieve PrintMap request layers from the WMPSConfiguration file. Counter check if the layer has been defined and
     * also get the type of datasource used by the layer.
     * 
     * @param printMap
     * @return Map key-> layer name, value -> configLayer
     * @throws PrintMapServiceException
     */
    private Map<String, Layer> retrieveLayersFromConfig( PrintMap printMap )
                            throws PrintMapServiceException {

        GetMap.Layer[] requestedLayers = printMap.getLayers();
        Map<String, Layer> layers = new HashMap<String, Layer>();
        for ( int i = 0; i < requestedLayers.length; i++ ) {
            GetMap.Layer layer = requestedLayers[i];

            Layer configLayer = this.configuration.getLayer( layer.getName() );
            if ( configLayer != null ) {
                layers.put( layer.getName(), configLayer );
            } else {
                throw new PrintMapServiceException( Messages.getMessage( "WMPS_UNKNOWN_LAYER", layer.getName() ) );
            }
        }

        return layers;
    }

    /**
     * Create a bounding box if no bounding box has been passed along with the PrintMap request.
     * 
     * @param printMap
     * @param center
     * @param scaleDenominator
     * @param mapWidth
     * @param mapHeight
     * @return Envelope
     */
    private Envelope createBBOX( PrintMap printMap, Point center, int scaleDenominator, double mapWidth,
                                 double mapHeight ) {
        int dpi = printMap.getDpi();
        if ( dpi < 0 ) {
            dpi = configuration.getDeegreeParams().getPrintMapParam().getTargetResolution();
        }
        if ( LOG.getLevel() == ILogger.LOG_DEBUG ) {
            LOG.logDebug( "calculating BBOX using folloing parameter:" );
            LOG.logDebug( "map center: ", center );
            LOG.logDebug( "target scale: ", scaleDenominator );
            LOG.logDebug( "target resolution (dpi): ", dpi );
            LOG.logDebug( "map width: ", mapWidth );
            LOG.logDebug( "map height: ", mapHeight );
        }
        // screen -> world projection
        double pixelSize = WMPSConfiguration.INCH2M / dpi;
        double w2 = ( scaleDenominator * pixelSize * mapWidth ) / 2d;
        double x1 = center.getX() - w2;
        double x2 = center.getX() + w2;
        w2 = ( scaleDenominator * pixelSize * mapHeight ) / 2d;
        double y1 = center.getY() - w2;
        double y2 = center.getY() + w2;

        Envelope bbox = GeometryFactory.createEnvelope( x1, y1, x2, y2, center.getCoordinateSystem() );

        LOG.logDebug( "calculated BBOX: ", bbox );
        return bbox;

    }

    /**
     * Returns the current configuration used to initialise the PrintMapHandler.
     * 
     * @return WMPSConfiguration
     */
    public WMPSConfiguration getConfiguration() {
        return this.configuration;
    }

    /**
     * Parse the Template and retrieve the page width, page height information.
     * 
     * @param printMap
     * @return int[]
     * @throws PrintMapServiceException
     * @throws IOException
     */
    private int[] getMapParamsFromTemplate( PrintMap printMap )
                            throws PrintMapServiceException, IOException {
        String templateName = printMap.getTemplate();
        int[] mapParams = null;
        URL file = null;
        // try {
        boolean isCompiled = false;
        file = getTemplatePath( templateName, isCompiled );
        if ( file != null ) {
            Document dom = null;
            try {
                InputStream is = file.openStream();
                int c = 0;
                StringBuffer sb = new StringBuffer( 10000 );
                while ( ( c = is.read() ) > -1 ) {
                    sb.append( (char) c );
                }
                is.close();
                // hack to ensure reporting engine is working even if the
                // jasper-report server is not available
                String s = StringTools.replace(
                                                sb.toString(),
                                                "<!DOCTYPE jasperReport PUBLIC \"//JasperReports//DTD Report Design//EN\" \"http://jasperreports.sourceforge.net/dtds/jasperreport.dtd\">",
                                                "", false );

                dom = XMLTools.parse( new StringReader( s ) );
                // XMLFragment xml = new XMLFragment( file );
                mapParams = parseImageNodes( printMap, dom.getDocumentElement() );
            } catch ( Exception e ) {
                LOG.logError( e.getMessage(), e );
                throw new PrintMapServiceException( Messages.getMessage( "WMPS_ERROR_PARSING_TEMPLATE", file ) );
            }
        }

        return mapParams;
    }

    /**
     * Return the url for the current (jasper reports) template.
     * 
     * @param templateName
     * @param isCompiled
     * @return URL
     * @throws PrintMapServiceException
     */
    private URL getTemplatePath( String templateName, boolean isCompiled )
                            throws IOException {

        if ( isCompiled ) {
            templateName = templateName + ".jasper";
        } else {
            templateName = templateName + ".jrxml";
        }

        PrintMapParam printMapParam = this.configuration.getDeegreeParams().getPrintMapParam();
        URL fileURL = new URL( printMapParam.getTemplateDirectory() + templateName );

        LOG.logDebug( "Retrieved the template file url. " + fileURL );

        return fileURL;
    }

    /**
     * Gets the Image node defined to hold the 'Map' and the node defined to hold the 'Legend'.
     * 
     * @param root
     * @return List
     * @throws PrintMapServiceException
     */
    private int[] parseImageNodes( PrintMap printMap, Element root )
                            throws PrintMapServiceException {

        int[] mapParams = new int[4];
        int mapCount = 0;
        try {
            NamespaceContext nsc = CommonNamespaces.getNamespaceContext();
            nsc.addNamespace( "jasper", URI.create( "http://jasperreports.sourceforge.net/jasperreports" ) );
            List<Node> images = XMLTools.getNodes( root, "detail/band/image", null );
            if ( images == null || images.size() == 0 ) {
                images = XMLTools.getNodes( root, "jasper:detail/jasper:band/jasper:image", nsc );
            }
            for ( int i = 0; i < images.size(); i++ ) {
                Node image = images.get( i );
                // e.g. $P{MAP}
                String value = XMLTools.getNodeAsString( image, "imageExpression", null, null );
                if ( value == null ) {
                    value = XMLTools.getRequiredNodeAsString( image, "jasper:imageExpression", nsc );
                }
                int idx = value.indexOf( "{" );
                if ( idx != -1 ) {

                    String tmp = value.substring( idx + 1, value.length() - 1 );
                    Element reportElement = (Element) XMLTools.getNode( image, "reportElement", null );
                    if ( reportElement == null ) {
                        reportElement = (Element) XMLTools.getRequiredNode( image, "jasper:reportElement", nsc );
                    }
                    String width = reportElement.getAttribute( "width" );
                    String height = reportElement.getAttribute( "height" );

                    double dpi = printMap.getDpi();
                    if ( dpi < 0 ) {
                        dpi = configuration.getDeegreeParams().getPrintMapParam().getTargetResolution();
                    }
                    // Templates created by iReport assumes a resolution of 72 dpi
                    if ( tmp.startsWith( "MAP" ) ) {
                        mapParams[0] = (int) ( Integer.parseInt( width ) / 72d * dpi );
                        mapParams[1] = (int) ( Integer.parseInt( height ) / 72d * dpi );
                        mapCount = mapCount + 1;
                    } else if ( tmp.startsWith( "LEGEND" ) ) {
                        mapParams[2] = (int) ( Integer.parseInt( width ) / 72d * dpi );
                        mapParams[3] = (int) ( Integer.parseInt( height ) / 72d * dpi );
                    }
                }
            }
            if ( ( mapCount == 0 ) || ( mapCount > 1 ) ) {
                throw new PrintMapServiceException( Messages.getMessage( "WMPS_TOO_MANY_MAPAREAS", mapCount ) );
            }
        } catch ( XMLParsingException e ) {
            LOG.logError( e.getMessage(), e );
            throw new PrintMapServiceException( Messages.getMessage( "WMPS_INVALID_JASPER_TEMPLATE" ) );
        }

        return mapParams;
    }

}
