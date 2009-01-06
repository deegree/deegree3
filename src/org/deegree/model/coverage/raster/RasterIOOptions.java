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
package org.deegree.model.coverage.raster;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.deegree.commons.utils.FileUtils;

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
    
    /**
     * This key stores the (output) format.
     */
    public static final String OPT_FORMAT = "FORMAT";

    private final Map<String, String> options = new HashMap<String, String>();

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
    
    @Override
    public String toString() {
        return options.toString();
    }
}
