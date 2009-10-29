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
import java.util.HashMap;
import java.util.Map;

import org.deegree.commons.utils.FileUtils;
import org.deegree.coverage.raster.data.container.RasterDataContainerFactory;
import org.deegree.coverage.raster.data.container.RasterDataContainerFactory.LoadingPolicy;
import org.deegree.coverage.raster.geom.RasterGeoReference;
import org.deegree.coverage.raster.geom.RasterGeoReference.OriginLocation;
import org.deegree.crs.CRS;
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
public class RasterIOOptions {

    private final static Logger LOG = LoggerFactory.getLogger( RasterIOOptions.class );

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

    private final Map<String, String> options = new HashMap<String, String>();

    private RasterGeoReference geoRef;

    /**
     * An empty constructor, nothing is set. The loading policy is the default value taken from the
     * {@link RasterDataContainerFactory}.
     */
    public RasterIOOptions() {
        // nottin
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
        result.add( READ_WLD_FILE, "yes" );
        result.add( DATA_LOADING_POLICY, RasterDataContainerFactory.getDefaultLoadingPolicy().name() );
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
        result.add( DATA_LOADING_POLICY, RasterDataContainerFactory.getDefaultLoadingPolicy().name() );
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

    public boolean hasEnvelope() {
        return geoRef != null;
    }

    public RasterGeoReference getEnvelope() {
        return geoRef;
    }

    public void setRasterGeoReference( RasterGeoReference geoRef ) {
        this.geoRef = geoRef;
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
}
