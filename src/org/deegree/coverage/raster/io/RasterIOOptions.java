//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------

 This file is part of deegree.
 Copyright (C) 2001-2009 by:
 EXSE, Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 Contact:

 Andreas Poth
 lat/lon GmbH
 Aennchenstr. 19
 53177 Bonn
 Germany
 E-Mail: poth@lat-lon.de

 Prof. Dr. Klaus Greve
 Department of Geography
 University of Bonn
 Meckenheimer Allee 166
 53115 Bonn
 Germany
 E-Mail: greve@giub.uni-bonn.de

 ---------------------------------------------------------------------------*/
package org.deegree.coverage.raster.io;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.deegree.commons.utils.FileUtils;
import org.deegree.coverage.raster.data.container.RasterDataContainerFactory;
import org.deegree.coverage.raster.data.container.RasterDataContainerFactory.LoadingPolicy;
import org.deegree.coverage.raster.geom.RasterReference;
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

    private final Map<String, String> options = new HashMap<String, String>();

    private RasterReference envelope;

    public RasterIOOptions() {
    }

    public RasterIOOptions( RasterReference envelope ) {
        this.envelope = envelope;
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
     * {@link RasterReference}.
     * 
     * @param file
     * @param envelope
     * @return RasterIOOption proper format.
     */
    public static RasterIOOptions forFile( File file, RasterReference envelope ) {
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
        return envelope != null;
    }

    public RasterReference getEnvelope() {
        return envelope;
    }
}
