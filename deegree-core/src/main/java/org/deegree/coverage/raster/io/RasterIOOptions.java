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
package org.deegree.coverage.raster.io;

import java.io.File;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.deegree.commons.utils.FileUtils;
import org.deegree.coverage.raster.data.container.RasterDataContainerFactory;
import org.deegree.coverage.raster.data.container.RasterDataContainerFactory.LoadingPolicy;
import org.deegree.coverage.raster.data.info.DataType;
import org.deegree.coverage.raster.geom.RasterGeoReference;
import org.deegree.coverage.raster.geom.RasterGeoReference.OriginLocation;
import org.deegree.cs.CRS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is a container for various RasterIO options.
 * 
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
public class RasterIOOptions implements Serializable {

    private static final long serialVersionUID = 6804424511435114774L;

    private final static Logger LOG = LoggerFactory.getLogger( RasterIOOptions.class );

    /** Separator for text base files */
    public final static String OPT_TEXT_SEPARATOR = "value_separator";

    /**
     * This key stores the (output) format.
     */
    public static final String OPT_FORMAT = "FORMAT";

    /**
     * This key is contained if the RasterReader should try to read the world file.
     */
    public static final String READ_WLD_FILE = "WLD_FILE";

    /**
     * This key will get the default loading policy (resulting in a RasterDataContainer) of the RasterDataFactory.
     */
    public static final String DATA_LOADING_POLICY = "LOADING_POLICIY";

    /**
     * This key will get the location of the origin of a raster geo reference see {@link OriginLocation}
     */
    public static final String GEO_ORIGIN_LOCATION = "ORIGIN";

    /**
     * This key will get the crs of the raster.
     */
    public static final String CRS = "CRS";

    /**
     * This key will get the local raster cache directory to be used inside the raster cache directory for the given
     * raster. For example the raster cache directory is /tmp/ and the LOCAL_RASTER_CACHE_DIR is set to 'some_name' all
     * raster caches will reside in /tmp/some_name/
     */
    public static final String LOCAL_RASTER_CACHE_DIR = "local_raster_cache";

    /** The raster cache dir to be used. */
    public static final String RASTER_CACHE_DIR = "raster_cache_dir";

    /** A key to signal the creation of missing raster dirs. */
    public static final String CREATE_RASTER_MISSING_CACHE_DIR = "create_raster_dir_if_missing";

    /**
     * Use this key to add the origin of the raster read, this might be handy if reading rasters from an URL (Stream)
     * and the cache can use this id to load data from cache.
     */
    public static final String ORIGIN_OF_RASTER = "raster_origin";

    private final Map<String, String> options = new HashMap<String, String>();

    private RasterGeoReference geoRef;

    private byte[] noData;

    /**
     * An empty constructor, nothing is set. The loading policy is the default value taken from the
     * {@link RasterDataContainerFactory}. Worldfile reading is on.
     */
    public RasterIOOptions() {
        add( READ_WLD_FILE, "yes" );
        add( DATA_LOADING_POLICY, RasterDataContainerFactory.getDefaultLoadingPolicy().name() );
    }

    /**
     * Use this constructor if you read your raster reference some place else, or if you are reading from a stream. Be
     * aware to set the filetype of the data as well. The loading policy is the default value taken from the
     * {@link RasterDataContainerFactory}.
     * 
     * @param reference
     *            of the file/stream to read.
     */
    public RasterIOOptions( RasterGeoReference reference ) {
        this();
        this.geoRef = reference;
    }

    /**
     * Set the default loading policy to one configured in the {@link RasterDataContainerFactory}
     * 
     * @param ref
     * @param format
     *            of the raster to read, e.g. png, jpg, tiff..., may be <code>null</code>
     */
    public RasterIOOptions( RasterGeoReference ref, String format ) {
        this( ref );
        if ( format != null && !"".equals( format ) ) {
            add( OPT_FORMAT, format );
        }
    }

    /**
     * @param originLocation
     *            to be used for reading worldfiles.
     */
    public RasterIOOptions( OriginLocation originLocation ) {
        this();
        options.put( GEO_ORIGIN_LOCATION, ( ( originLocation == null ) ? OriginLocation.CENTER.name()
                                                                      : originLocation.name() ) );
    }

    /**
     * @param key
     * @param value
     */
    public void add( String key, String value ) {
        options.put( key, value );
    }

    /**
     * @param key
     * @return true if it contains the option
     */
    public boolean contains( String key ) {
        return options.containsKey( key );
    }

    /**
     * @param key
     * @return the option value or <code>null</code>
     */
    public String get( String key ) {
        return options.get( key );
    }

    /**
     * Return a RasterIOOption object with the format set according to the given file.
     * 
     * @param file
     * @return RasterIOOption proper format.
     */
    public static RasterIOOptions forFile( File file ) {
        RasterIOOptions result = new RasterIOOptions();
        String ext = FileUtils.getFileExtension( file );
        result.add( OPT_FORMAT, ext );
        return result;
    }

    /**
     * Return a RasterIOOption object with the format set according to the given file with an optional
     * {@link RasterGeoReference}.
     * 
     * @param file
     * @param envelope
     * @return RasterIOOption proper format.
     */
    public static RasterIOOptions forFile( File file, RasterGeoReference envelope ) {
        RasterIOOptions result = new RasterIOOptions( envelope );
        String ext = FileUtils.getFileExtension( file );
        result.add( OPT_FORMAT, ext );
        result.add( READ_WLD_FILE, null );
        return result;
    }

    @Override
    public String toString() {
        return options.toString();
    }

    /**
     * @return true if the RasterReader should read the corresponding worldfile.
     */
    public boolean readWorldFile() {
        return options.get( READ_WLD_FILE ) != null;
    }

    /**
     * @return the loading policy (and thus the raster data container) of the RasterDataFactory.
     */
    public LoadingPolicy getLoadingPolicy() {
        LoadingPolicy result = RasterDataContainerFactory.getDefaultLoadingPolicy();
        try {
            result = LoadingPolicy.valueOf( options.get( DATA_LOADING_POLICY ) );
        } catch ( IllegalArgumentException ia ) {
            LOG.error( "Unable to map loading policy, using memory instead." );
        }
        return result;
    }

    /**
     * @return true if the options contain a raster geo reference.
     */
    public boolean hasRasterGeoReference() {
        return geoRef != null;
    }

    /**
     * @return the raster geo reference
     */
    public RasterGeoReference getRasterGeoReference() {
        return geoRef;
    }

    /**
     * @param geoRef
     *            the raster geo reference to use for the loaded raster.
     */
    public void setRasterGeoReference( RasterGeoReference geoRef ) {
        this.geoRef = geoRef;
    }

    /**
     * Returns the no data value. A no data value should be added to the {@link RasterIOOptions} by using
     * {@link RasterIOOptions#setNoData(byte[])}. The byte[] can be created from an array of Strings by using the
     * {@link RasterIOOptions#createNoData(String[], DataType)}.
     * 
     * @return the no data values for the bands of the raster or <code>null</code> if no data was specified.
     */
    public byte[] getNoDataValue() {
        return noData == null ? null : Arrays.copyOf( noData, noData.length );
    }

    /**
     * @return the location of the origin of the read file. If not defined this method will return return
     *         {@link RasterGeoReference.OriginLocation#CENTER};
     * 
     */
    public RasterGeoReference.OriginLocation getRasterOriginLocation() {
        String s = options.get( GEO_ORIGIN_LOCATION );
        if ( "outer".equalsIgnoreCase( s ) ) {
            return RasterGeoReference.OriginLocation.OUTER;
        }
        return RasterGeoReference.OriginLocation.CENTER;
    }

    /**
     * @return the defined crs or null if no crs was defined.
     */
    public CRS getCRS() {
        String s = options.get( CRS );
        if ( s == null || "".equals( s.trim() ) ) {
            return null;
        }
        return new CRS( s );
    }

    /**
     * no data value. The byte[] can be created from an array of Strings by using the
     * {@link RasterIOOptions#createNoData(String[], DataType)}.
     * 
     * @param noDataValue
     *            containing byte representations of the rasters no data value.
     */
    public void setNoData( byte[] noDataValue ) {
        if ( noDataValue != null ) {
            this.noData = new byte[noDataValue.length];
            System.arraycopy( noDataValue, 0, noData, 0, noDataValue.length );
        }
    }

    /**
     * Create a noData array from the given strings. Each string will be interpreted as the given type.
     * 
     * @param bandValues
     *            String representations of each bands no data value e.g {10.0, 80.5, -100} for a 3 band float raster.
     * @param type
     *            of the no data values.
     * @return a byte array containing the no data values for each band, the bytes can be 'reversed' by using the
     *         {@link ByteBuffer} methods. If either the type or the array is <code>null</code> <code>null</code> will
     *         be returned.
     * @throws NumberFormatException
     *             if one of the Strings could not be decoded.
     */
    public static byte[] createNoData( String[] bandValues, DataType type )
                            throws NumberFormatException {
        byte[] result = null;
        if ( bandValues != null && type != null ) {
            int size = type.getSize();
            result = new byte[size * bandValues.length];
            byte[] bandResult = new byte[size];
            ByteBuffer wrap = ByteBuffer.wrap( bandResult );
            for ( int i = 0; i < bandValues.length; ++i ) {
                String val = bandValues[i];
                if ( val != null ) {
                    val = val.trim();
                    switch ( type ) {
                    case BYTE:
                        wrap.put( Byte.decode( val ) );
                        break;
                    case DOUBLE:
                        Double d = Double.valueOf( val );
                        wrap.putDouble( d );
                        break;
                    case FLOAT:
                        Float f = Float.valueOf( val );
                        wrap.putFloat( f );
                        break;
                    case INT:
                        wrap.putInt( Integer.decode( val ) );
                        break;
                    case SHORT:
                    case USHORT:
                        wrap.putShort( Short.decode( val ) );
                        break;
                    case UNDEFINED:
                        // what to do here?
                        break;
                    }
                    System.arraycopy( bandResult, 0, result, i * size, size );
                    wrap.position( 0 );
                }
            }
        }
        return result;
    }

    /**
     * Copies the the values from the given options.
     * 
     * @param otherOptions
     */
    public void copyOf( RasterIOOptions otherOptions ) {

        if ( otherOptions != null ) {
            if ( getLoadingPolicy() != otherOptions.getLoadingPolicy() ) {
                add( DATA_LOADING_POLICY, otherOptions.getLoadingPolicy().name() );
            }

            if ( readWorldFile() != otherOptions.readWorldFile() ) {
                add( READ_WLD_FILE, otherOptions.readWorldFile() + "" );
            }

            if ( getRasterGeoReference() == null ) {
                setRasterGeoReference( otherOptions.getRasterGeoReference() );
            }
            setNoData( otherOptions.getNoDataValue() );
            if ( getRasterOriginLocation() == OriginLocation.CENTER ) {
                add( GEO_ORIGIN_LOCATION, otherOptions.getRasterOriginLocation().name() );
            }
            if ( getCRS() == null && otherOptions.getCRS() != null ) {
                add( CRS, otherOptions.getCRS().getName() );
            }
            for ( String key : otherOptions.options.keySet() ) {
                if ( !( key.equals( DATA_LOADING_POLICY ) || key.equals( READ_WLD_FILE )
                        || key.equals( GEO_ORIGIN_LOCATION ) || key.equals( CRS ) ) ) {
                    String val = otherOptions.get( key );
                    if ( val != null && options.get( key ) == null ) {
                        options.put( key, val );
                    }
                }
            }
            // if ( get( OPT_FORMAT ) == null ) {
            // add( OPT_FORMAT, otherOptions.get( OPT_FORMAT ) );
            // }
            // if ( get( LOCAL_RASTER_CACHE_DIR ) == null ) {
            // add( LOCAL_RASTER_CACHE_DIR, otherOptions.get( LOCAL_RASTER_CACHE_DIR ) );
            // }
            // if ( get( RASTER_CACHE_DIR ) == null ) {
            // add( RASTER_CACHE_DIR, otherOptions.get( RASTER_CACHE_DIR ) );
            // }
            // if ( get( CREATE_RASTER_MISSING_CACHE_DIR ) == null ) {
            // add( CREATE_RASTER_MISSING_CACHE_DIR, otherOptions.get( CREATE_RASTER_MISSING_CACHE_DIR ) );
            // }
            // if ( get( ORIGIN_OF_RASTER ) == null ) {
            // add( ORIGIN_OF_RASTER, otherOptions.get( ORIGIN_OF_RASTER ) );
            // }

        }
    }

}
