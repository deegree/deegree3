//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
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
package org.deegree.geometry.io;

import java.io.IOException;
import java.io.Reader;
import java.sql.SQLException;

import org.apache.commons.io.IOUtils;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.geometry.Geometry;
import org.postgis.binary.BinaryWriter;

import org.locationtech.jts.io.ParseException;

/**
 * Reads {@link Geometry} objects encoded as Well-Known Text (WKT).
 * 
 * TODO re-implement without delegating to JTS TODO add support for non-SFS geometries (e.g. non-linear curves) TODO
 * TODO TODO do not go about using PostGIS for parsing the WKT, generate WKB and then parse it back using JTS TODO TODO
 * TODO repeat after me s/TODO/TODO TODO/g
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class WKTReader {

    private ICRS crs;

    public WKTReader( ICRS crs ) {
        this.crs = crs;
    }

    public Geometry read( Reader reader )
                            throws ParseException {
        try {
            return read( IOUtils.toString( reader ) );
        } catch ( IOException e ) {
            // wrap the exception nicely as to not break 172643521 API calls
            throw new ParseException( e );
        }
    }

    public Geometry read( String wkt )
                            throws ParseException {
        try {
            org.postgis.Geometry g = org.postgis.PGgeometry.geomFromString( wkt );
            byte[] bs = new BinaryWriter().writeBinary( g );
            return WKBReader.read( bs, crs );
        } catch ( SQLException e ) {
            e.printStackTrace();
            // wrap the exception nicely as to not break 172643521 API calls
            throw new ParseException( e );
        }
    }

}
