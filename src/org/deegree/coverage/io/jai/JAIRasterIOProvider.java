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
package org.deegree.coverage.io.jai;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.deegree.coverage.raster.io.RasterIOProvider;
import org.deegree.coverage.raster.io.RasterReader;
import org.deegree.coverage.raster.io.RasterWriter;

/**
 * This class is a RasterIOProvider and makes the JAIRasterReader and JAIRasterWriter available to the deegree raster
 * IO.
 * 
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class JAIRasterIOProvider implements RasterIOProvider {

    private static final Set<String> SUPPORTED_TYPES;

    // maps a file extension to the JAI type name
    private static Map<String, String> EXT_TO_FORMAT;

    static {
        EXT_TO_FORMAT = new HashMap<String, String>();
        EXT_TO_FORMAT.put( "tif", "TIFF" );
        EXT_TO_FORMAT.put( "tiff", "TIFF" );
        EXT_TO_FORMAT.put( "jpg", "JPEG" );
        EXT_TO_FORMAT.put( "jpeg", "JPEG" );
        EXT_TO_FORMAT.put( "gif", "GIF" );
        EXT_TO_FORMAT.put( "png", "PNG" );
        EXT_TO_FORMAT.put( "jp2", "JPEG2000" );
        EXT_TO_FORMAT.put( "j2k", "JPEG2000" );

        SUPPORTED_TYPES = new HashSet<String>();
        SUPPORTED_TYPES.add( JAIRasterReader.class.getCanonicalName() );
        SUPPORTED_TYPES.add( "jai" );
        SUPPORTED_TYPES.add( "jai-reader" );
        SUPPORTED_TYPES.addAll( EXT_TO_FORMAT.keySet() );
    }

    /**
     * @param extension
     * @return the JAI format name for extension
     */
    static String getJAIFormat( String extension ) {
        return EXT_TO_FORMAT.get( extension );
    }

    @Override
    public RasterReader getRasterReader( String type ) {
        if ( !SUPPORTED_TYPES.contains( type.toLowerCase() ) ) {
            return null;
        }
        return new JAIRasterReader();
    }

    @Override
    public RasterWriter getRasterWriter( String type ) {
        if ( !SUPPORTED_TYPES.contains( type.toLowerCase() ) ) {
            return null;
        }
        return new JAIRasterWriter();
    }

}
