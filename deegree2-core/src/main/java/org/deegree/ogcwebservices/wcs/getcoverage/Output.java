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
package org.deegree.ogcwebservices.wcs.getcoverage;

import org.deegree.datatypes.Code;
import org.deegree.ogcbase.ExceptionCode;
import org.deegree.ogcwebservices.wcs.WCSException;

/**
 * @version $Revision$
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version 1.0. $Revision$, $Date$
 *
 * @since 2.0
 */

public class Output {
    private Code crs = null;

    private Code format = null;

    /**
     * @param crs
     * @param format
     * @throws WCSException
     *             if one of the parameters is null
     */
    public Output( Code crs, Code format ) throws WCSException {
        if ( crs == null ) {
            ExceptionCode code = ExceptionCode.MISSINGPARAMETERVALUE;
            throw new WCSException( "GetCoverage", "'crs' is missing", code );
        }
        if ( format == null ) {
            ExceptionCode code = ExceptionCode.MISSINGPARAMETERVALUE;
            throw new WCSException( "GetCoverage", "'format' is missing", code );
        }
        this.crs = crs;
        this.format = format;
    }

    /**
     * @return Returns the crs.
     */
    public Code getCrs() {
        return crs;
    }

    /**
     * @return Returns the format.
     */
    public Code getFormat() {
        return format;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer( 300 );
        sb.append( "CRS=" );
        sb.append( crs );
        sb.append( ", format=" );
        sb.append( format );
        return sb.toString();
    }
}
