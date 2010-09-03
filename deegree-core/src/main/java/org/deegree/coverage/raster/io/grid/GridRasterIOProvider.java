//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------
 This file is part of deegree.
 Copyright (C) 2001-2009 by:
 Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 Lesser General Public License for more details.
 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
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

package org.deegree.coverage.raster.io.grid;

import java.util.HashSet;
import java.util.Set;

import org.deegree.coverage.raster.io.RasterIOProvider;
import org.deegree.coverage.raster.io.RasterReader;
import org.deegree.coverage.raster.io.RasterWriter;

/**
 * The <code>XYZRasterIOProvider</code> class defines the access to xyz files
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 * @version $Revision$, $Date$
 * 
 */
public class GridRasterIOProvider implements RasterIOProvider {

    /**
     * Supported formats
     */
    final static Set<String> FORMATS = new HashSet<String>();
    static {
        FORMATS.add( "grid" );
        FORMATS.add( "bin" );
    }

    @Override
    public RasterReader getRasterReader( String type ) {
        if ( type != null && FORMATS.contains( type.toLowerCase() ) ) {
            return new GridFileReader();
        }
        return null;
    }

    @Override
    public Set<String> getRasterReaderFormats() {
        return new HashSet<String>( FORMATS );
    }

    @Override
    public RasterWriter getRasterWriter( String type ) {
        if ( type != null && FORMATS.contains( type.toLowerCase() ) ) {
            return new GridWriter();
        }
        return null;
    }

    @Override
    public Set<String> getRasterWriterFormats() {
        return new HashSet<String>( FORMATS );
    }
}
