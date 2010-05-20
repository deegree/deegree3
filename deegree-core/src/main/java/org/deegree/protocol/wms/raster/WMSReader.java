//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -

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
package org.deegree.protocol.wms.raster;

import static org.deegree.commons.utils.StringUtils.isSet;
import static org.slf4j.LoggerFactory.getLogger;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.deegree.commons.utils.Pair;
import org.deegree.commons.utils.StringUtils;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.coverage.raster.AbstractRaster;
import org.deegree.coverage.raster.SimpleRaster;
import org.deegree.coverage.raster.cache.RasterCache;
import org.deegree.coverage.raster.data.container.BufferResult;
import org.deegree.coverage.raster.data.info.RasterDataInfo;
import org.deegree.coverage.raster.data.nio.ByteBufferRasterData;
import org.deegree.coverage.raster.geom.RasterGeoReference;
import org.deegree.coverage.raster.geom.RasterRect;
import org.deegree.coverage.raster.geom.RasterGeoReference.OriginLocation;
import org.deegree.coverage.raster.io.RasterIOOptions;
import org.deegree.coverage.raster.io.RasterReader;
import org.deegree.coverage.raster.utils.RasterFactory;
import org.deegree.cs.CRS;
import org.deegree.cs.components.Unit;
import org.deegree.cs.coordinatesystems.CoordinateSystem;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.GeometryTransformer;
import org.deegree.protocol.wms.WMSConstants.WMSRequestType;
import org.deegree.protocol.wms.client.WMSClient111;
import org.slf4j.Logger;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class WMSReader implements RasterReader {

    private static final Logger LOG = getLogger( WMSReader.class );

    /**
     * Formats of the reader
     */
    final static Set<String> supportedFormats;

    /** Defines the system id to be used as raster io key, for setting the system id or the capabilities url. */
    public static final String RIO_WMS_SYS_ID = "RASTERIO_WMS_SYS_ID";

    /** The refresh time of the capabilities */
    public static final String RIO_WMS_REFRESH_TIME = "RASTERIO_WMS_REFRESH_TIME";

    /** Defines the maximum width of a GetMap request. */
    public static final String RIO_WMS_MAX_WIDTH = "RASTERIO_WMS_MAX_WIDTH";

    /** Defines the maximum height of a GetMap request. */
    public static final String RIO_WMS_MAX_HEIGHT = "RASTERIO_WMS_MAX_HEIGHT";

    /** Defines the maximum height of a GetMap request. */
    public static final String RIO_WMS_LAYERS = "RASTERIO_WMS_REQUESTED_LAYERS";

    /** Defines the maximum scale of a WMS. */
    public static final String RIO_WMS_MAX_SCALE = "RASTERIO_WMS_MAX_SCALE";

    /** Defines the default (image) format of a get map request to a WMS. */
    public static final String RIO_WMS_DEFAULT_FORMAT = "RASTERIO_WMS_DEFAULT_FORMAT";

    /** Defines the key to set the GetMap retrieval to transparent. */
    public static final String RIO_WMS_ENABLE_TRANSPARENT = "RASTERIO_WMS_ENABLE_TRANSPARENCY";

    /** Defines the key to set the GetMap retrieval timeout. */
    public static final String RIO_WMS_TIMEOUT = "RASTERIO_WMS_GM_TIMEOUT";

    static {
        supportedFormats = new HashSet<String>();
        supportedFormats.add( WMSVersion.WMS_111.name() );
    }

    enum WMSVersion {
        WMS_111;
    }

    private WMSClient111 client;

    private RasterGeoReference geoRef;

    private WMSVersion wmsVersion;

    private int width;

    private int height;

    private RasterDataInfo rdi;

    private List<String> layers;

    private Envelope envelope;

    private String dataLocationId;

    private int maxWidth;

    private int maxHeight;

    private String format;

    private boolean transparent;

    private int timeout;

    private CRS crs;

    /**
     * @param version
     *            of the wms
     */
    public WMSReader( WMSVersion version ) {
        this.wmsVersion = version;
    }

    @Override
    public boolean canLoad( File filename ) {
        return false;
    }

    @Override
    public boolean canReadTiles() {
        return false;
    }

    @Override
    public void dispose() {
        // TODO Auto-generated method stub

    }

    @Override
    public File file() {
        // no file
        return null;
    }

    @Override
    public String getDataLocationId() {
        return dataLocationId;
    }

    @Override
    public RasterGeoReference getGeoReference() {
        return this.geoRef;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public RasterDataInfo getRasterDataInfo() {
        return rdi;
    }

    @Override
    public Set<String> getSupportedFormats() {
        return new HashSet<String>( supportedFormats );
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public AbstractRaster load( File filename, RasterIOOptions options )
                            throws IOException {
        throw new UnsupportedOperationException( "The wms reader does not support loading from file." );
    }

    @Override
    public AbstractRaster load( InputStream stream, RasterIOOptions options )
                            throws IOException {
        RasterIOOptions opts = new RasterIOOptions();
        opts.copyOf( options );
        String sysId = opts.get( RIO_WMS_SYS_ID );
        if ( sysId == null ) {
            LOG.error( "No system id available, please verify the location of the capabilities url, and set it in the rasterio options with the key: WMSReader.RIO_WMS_SYS_ID." );
            throw new IOException( "No capabilities url found to read from." );
        }
        this.dataLocationId = sysId;
        LOG.debug( "Creating xml adapter from stream to capabilities:{}", sysId );
        XMLAdapter capabilities = new XMLAdapter( stream );
        capabilities.setSystemId( sysId );

        try {
            this.client = new WMSClient111( capabilities );
        } catch ( Exception e ) {
            throw new IOException( "The given stream with system id( " + sysId
                                   + ") does not access a WMS Capabilities document." );
        }

        this.maxWidth = getInt( opts.get( RIO_WMS_MAX_WIDTH ), "max request width" );
        this.maxHeight = getInt( opts.get( RIO_WMS_MAX_HEIGHT ), "max request height" );

        this.client.setMaxMapDimensions( maxWidth, maxHeight );
        this.layers = getLayers( opts );
        crs = opts.getCRS();
        if ( crs == null ) {
            this.envelope = client.getLatLonBoundingBox( layers );
            LinkedList<String> coordinateSystems = client.getCoordinateSystems( layers.get( 0 ) );
            for ( int i = 0; i < coordinateSystems.size() && this.crs == null; ++i ) {
                String srs = coordinateSystems.get( i );
                if ( srs != null ) {
                    this.crs = new CRS( srs );
                    try {
                        CoordinateSystem cr = crs.getWrappedCRS();
                        if ( cr.getAxis()[0].getUnits().canConvert( Unit.METRE ) ) {
                            break;
                        }
                    } catch ( UnknownCRSException e ) {
                        LOG.debug( "(Stack) Unknown crs: " + e.getLocalizedMessage(), e );
                    }

                }
            }
        }
        if ( crs == null ) {
            throw new IOException( "Could not get the Default coordinate system for layers: " + layers
                                   + " does your WMS support the given layers? Unable to use this WMS ( "
                                   + dataLocationId + " as a raster data source." );
        }
        CoordinateSystem wCRS = null;
        try {
            wCRS = crs.getWrappedCRS();
        } catch ( UnknownCRSException e1 ) {
            throw new IOException( "The Default coordinate system for layers: " + layers
                                   + " are not supported by your deegree installation. Unable to use this WMS ( "
                                   + dataLocationId + " as a raster data source." );
        }
        // no bbox defined in the given crs
        this.envelope = client.getBoundingBox( crs.getName(), layers );
        if ( this.envelope == null ) {
            this.envelope = client.getLatLonBoundingBox( layers );
            GeometryTransformer transform = new GeometryTransformer( wCRS );

            try {
                Geometry tEnv = transform.transform( this.envelope );
                if ( tEnv == null ) {
                    envelope = null;
                } else {
                    envelope = tEnv.getEnvelope();
                }
            } catch ( Exception e ) {
                LOG.error( e.getLocalizedMessage() );
                envelope = null;
            }

        }
        if ( this.envelope == null ) {
            throw new IOException( "Could not get the BBox for layers: " + layers
                                   + " does your WMS support the given layers, unable to use this WMS ( "
                                   + dataLocationId + " as a raster data source." );
        }
        CoordinateSystem realCRS = null;
        if ( crs != null ) {
            try {
                realCRS = crs.getWrappedCRS();
            } catch ( UnknownCRSException e ) {
                if ( LOG.isDebugEnabled() ) {
                    LOG.debug( "(Stack) Could not get underlying crs: " + e.getLocalizedMessage(), e );
                } else {
                    LOG.error( "Could not get underlying crs: " + e.getLocalizedMessage() );
                }
            }
        }
        double scale = getScale( opts );
        int widthAxis = realCRS == null ? 0 : realCRS.getEasting();
        // double eW = this.envelope.getSpan( widthAxis );
        // double eH = this.envelope.getSpan( 1 - widthAxis );
        // this.width = (int) Math.ceil( eW * scale );
        // this.height = (int) Math.ceil( eH * scale );
        // this.geoRef = RasterGeoReference.create( OriginLocation.OUTER, envelope, width, height );
        this.geoRef = new RasterGeoReference( OriginLocation.OUTER, scale, -scale,
                                              this.envelope.getMin().get( widthAxis ),
                                              this.envelope.getMax().get( 1 - widthAxis ) );
        RasterRect rect = this.geoRef.convertEnvelopeToRasterCRS( this.envelope );
        this.width = rect.width;
        this.height = rect.height;

        this.transparent = enableTransparency( opts );
        this.format = getFormat( opts );
        if ( !isSet( format ) ) {
            throw new IOException(
                                   "No format set and unable to retrieve a format for a GetMap request to the WMS, unable to use this WMS ( "
                                                           + dataLocationId + " as a raster data source." );
        }

        this.timeout = getInt( opts.get( RIO_WMS_TIMEOUT ), "get map time out" );

        RasterCache cache = RasterCache.getInstance( opts );
        SimpleRaster result = cache.createFromCache( this, this.dataLocationId );
        if ( result == null ) {
            Envelope env = this.geoRef.getEnvelope( new RasterRect( 1, 1, 2, 2 ), null );
            Pair<BufferedImage, String> imageResponse = this.client.getMap( layers, maxWidth, maxHeight, env, crs,
                                                                            format, transparent, true, timeout, false,
                                                                            null );
            if ( imageResponse.first != null ) {
                BufferedImage img = imageResponse.first;
                try {
                    ByteBufferRasterData rd = RasterFactory.rasterDataFromImage( img );
                    if ( rd != null ) {
                        this.rdi = rd.getDataInfo();
                    }
                } catch ( Exception e ) {
                    if ( LOG.isDebugEnabled() ) {
                        LOG.debug( "Could not do an initial GetMap request to server: " + dataLocationId + " because: "
                                   + e.getMessage(), e );
                    } else {
                        LOG.error( "Could not do an initial GetMap request to server: " + dataLocationId + " because: "
                                   + e.getMessage() );
                    }
                }

            }
            if ( rdi == null ) {
                throw new IOException(
                                       "Could not determine the resulting Raster data information of a GetMap request, unable to use this WMS ( "
                                                               + dataLocationId + " as a raster data source." );
            }
            result = RasterFactory.createEmptyRaster( rdi, envelope, geoRef, this, false, opts );
        }

        WMSRaster raster = new WMSRaster( result );

        return raster;
    }

    /**
     * @param opts
     * @return
     */
    private boolean enableTransparency( RasterIOOptions opts ) {
        return opts.get( RIO_WMS_ENABLE_TRANSPARENT ) != null;
    }

    /**
     * @param opts
     * @return
     */
    private String getFormat( RasterIOOptions opts ) {
        String format = opts.get( RIO_WMS_DEFAULT_FORMAT );
        if ( isSet( format ) ) {
            LinkedList<String> formats = this.client.getFormats( WMSRequestType.GetMap );
            String pngFormat = null;
            String tiffFormat = null;
            String jpgFormat = null;
            for ( String f : formats ) {
                if ( f != null ) {
                    if ( pngFormat == null && hasFormat( "png", f ) ) {
                        pngFormat = f;
                    }
                    if ( tiffFormat == null && ( hasFormat( "tiff", f ) || hasFormat( "tif", f ) ) ) {
                        tiffFormat = f;
                    }
                    if ( jpgFormat == null && ( hasFormat( "jpg", f ) || hasFormat( "jpeg", f ) ) ) {
                        jpgFormat = f;
                    }
                }
            }
            if ( transparent ) {
                if ( pngFormat != null ) {
                    format = pngFormat;
                } else if ( tiffFormat != null ) {
                    format = tiffFormat;
                }
            }
            if ( format == null ) {
                if ( jpgFormat != null ) {
                    format = jpgFormat;
                } else {
                    // set the default format
                    format = formats.get( 0 );
                }
            }
        }
        return format;
    }

    private boolean hasFormat( String simpleImage, String defFormat ) {
        return simpleImage.equalsIgnoreCase( defFormat ) || ( "image/" + simpleImage ).equalsIgnoreCase( defFormat );
    }

    private int getInt( String val, String key ) {
        if ( isSet( val ) ) {
            try {
                return Integer.parseInt( val );
            } catch ( NumberFormatException e ) {
                LOG.debug( "Could not get integer value for : " + key );
            }
        }
        return -1;
    }

    /**
     * @param options
     * @return
     */
    private double getScale( RasterIOOptions options ) {
        String scale = options.get( RIO_WMS_MAX_SCALE );
        if ( !StringUtils.isSet( scale ) ) {
            throw new IllegalArgumentException(
                                                "Missing required maximum scale value for wms, please set the RasterIO options with the value WMSReader.RIO_WMS_LAYERS" );

        }
        try {
            return Double.parseDouble( scale );
        } catch ( NumberFormatException nfe ) {
            throw new IllegalArgumentException( "Required maximum scale value for wms, is not valid, supplied was: "
                                                + scale );
        }
    }

    /**
     * @param options
     */
    private List<String> getLayers( RasterIOOptions options ) {
        List<String> configuredLayers = new LinkedList<String>();
        String layers = options.get( RIO_WMS_LAYERS );
        if ( StringUtils.isSet( layers ) ) {
            String[] layer = layers.split( "," );
            for ( String l : layer ) {
                configuredLayers.add( l );
            }
        }
        if ( configuredLayers.isEmpty() ) {
            List<String> namedLayers = this.client.getNamedLayers();
            if ( namedLayers != null ) {
                configuredLayers.addAll( namedLayers );
            }
        }
        return configuredLayers;
    }

    @Override
    public BufferResult read( RasterRect rect, ByteBuffer result )
                            throws IOException {
        Envelope env = this.geoRef.getEnvelope( rect, null );
        Pair<BufferedImage, String> imageResponse = this.client.getMap( layers, rect.width, rect.height, env, this.crs,
                                                                        format, transparent, true, timeout, false, null );
        BufferResult res = null;
        if ( imageResponse.first != null ) {
            BufferedImage img = imageResponse.first;
            ByteBufferRasterData rd = RasterFactory.rasterDataFromImage( img, null, result );
            res = new BufferResult( rd.getView(), rd.getByteBuffer() );
        }
        return res;
    }

    @Override
    public boolean shouldCreateCacheFile() {
        return false;
    }

    public static void main( String[] args )
                            throws MalformedURLException {
        RasterIOOptions options = new RasterIOOptions();
        options.add( RIO_WMS_SYS_ID,
                     "http://www.wms.nrw.de/geobasis/adv_nrw500?REQUEST=GetCapabilities&VERSION=1.1.1&SERVICE=WMS" );

        options.add( RasterIOOptions.CRS, "EPSG:31466" );
        /** Defines the maximum width of a GetMap request. */
        options.add( RIO_WMS_MAX_WIDTH, "1001" );

        /** Defines the maximum height of a GetMap request. */
        options.add( RIO_WMS_MAX_HEIGHT, "1001" );

        /** Defines the maximum height of a GetMap request. */
        options.add( RIO_WMS_LAYERS, "NRW500" );

        /** Defines the maximum scale of a WMS. */
        options.add( RIO_WMS_MAX_SCALE, "0.1" );

        /** Defines the default (image) format of a get map request to a WMS. */
        options.add( RIO_WMS_DEFAULT_FORMAT, "png" );

        /** Defines the key to set the GetMap retrieval to transparent. */
        options.add( RIO_WMS_ENABLE_TRANSPARENT, "true" );

        /** Defines the key to set the GetMap retrieval timeout. */
        options.add( RIO_WMS_TIMEOUT, "1000" );

        options.add( RasterIOOptions.OPT_FORMAT, WMSReader.WMSVersion.WMS_111.name() );

        URL url = new URL(
                           "http://www.wms.nrw.de/geobasis/adv_nrw500?REQUEST=GetCapabilities&VERSION=1.1.1&SERVICE=WMS" );

        try {
            InputStream in = url.openStream();
            AbstractRaster raster = RasterFactory.loadRasterFromStream( in, options );
            in.close();
            System.out.println( raster.getEnvelope() );
            System.out.println( raster.getRasterDataInfo() );
            System.out.println( "c: " + raster.getColumns() );
            System.out.println( "r: " + raster.getRows() );
            Envelope env = raster.getRasterReference().getEnvelope( new RasterRect( 1520, 1520, 1000, 1000 ), null );
            System.out.println( "env: " + env );
            AbstractRaster subRaster = raster.getSubRaster( env );
            RasterFactory.saveRasterToFile( subRaster, new File( "/tmp/out.png" ) );

        } catch ( IOException e ) {
            if ( LOG.isDebugEnabled() ) {
                LOG.debug( "(Stack) Exception occurred: " + e.getLocalizedMessage(), e );
            } else {
                LOG.error( "Exception occurred: " + e.getLocalizedMessage() );
            }
        }

    }
}
