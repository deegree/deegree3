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
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Set;

import org.deegree.coverage.raster.AbstractRaster;
import org.deegree.coverage.raster.data.container.BufferResult;
import org.deegree.coverage.raster.data.info.RasterDataInfo;
import org.deegree.coverage.raster.geom.RasterGeoReference;
import org.deegree.coverage.raster.geom.RasterRect;

/**
 * This interface is for abstraction of the raster loading handling.
 * 
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$
 * 
 */
public interface RasterReader {
    /**
     * Read the given raster file into an abstract raster.
     * 
     * @param filename
     * @param options
     * @return the loaded raster
     * @throws IOException
     *             may be thrown when there is a problem with reading the raster.
     */
    public AbstractRaster load( File filename, RasterIOOptions options )
                            throws IOException;

    /**
     * Read the given input stream into an abstract raster.
     * 
     * @param stream
     * @param options
     * @return the loaded raster
     * @throws IOException
     *             may be thrown when there is a problem with reading the raster.
     */
    public AbstractRaster load( InputStream stream, RasterIOOptions options )
                            throws IOException;

    /**
     * Check if the raster reader is able to read the given raster file.
     * 
     * @param filename
     * @return true if the class can read the raster
     */
    public boolean canLoad( File filename );

    /**
     * @return a {@link Set} of (image) formats mime/types the implementation is able to read.
     */
    public Set<String> getSupportedFormats();

    /**
     * @return true if a cache file should be created for the read raster.
     */
    public boolean shouldCreateCacheFile();

    /**
     * @return the file from which the raster was read.
     */
    public File file();

    /**
     * @return the width (in pixels) of the read raster 
     */
    public int getWidth();

    /**
     * @return the height (in pixels) of the read raster
     */
    public int getHeight();

    /**
     * @return the geo reference of the read raster
     */
    public RasterGeoReference getGeoReference();

    /**
     * @param rect to read.
     * @param result to store the result in
     * @return the read buffer and its domain
     * @throws IOException
     */
    public BufferResult read( RasterRect rect, ByteBuffer result )
                            throws IOException;

    /**
     * @return the raster data info
     */
    public RasterDataInfo getRasterDataInfo();

    /**
     * should return true if the given reader can easily read tiles,without consuming much more memory than needed.
     * 
     * @return true if the reader can easily read tiles.
     */
    public boolean canReadTiles();

    /**
     * @return a String which identifies this reader for used the data source, for example the file name, or an url.
     */
    public String getDataLocationId();

    /**
     * Signals the reader that it should try to dispose all in memory data.
     */
    public void dispose();

}
