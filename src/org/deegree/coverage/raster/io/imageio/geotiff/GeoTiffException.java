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
package org.deegree.coverage.raster.io.imageio.geotiff;

/**
 * The <code>GeoTiffException</code> helps to identify errors in a GeoTIFF file while reading or writing.
 * 
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 * @version $Revision$, $Date$
 * 
 */
public class GeoTiffException extends IllegalArgumentException {

    /**
     * 
     */
    private static final long serialVersionUID = 4300658996822294090L;

    private final static String message = "Error while reading GeoTiff information";

    /**
     * using a default message
     */
    public GeoTiffException() {
        super( message );
    }

    /**
     * 
     * @param message
     *            to be displayed
     */
    public GeoTiffException( String message ) {
        super( message );
    }

    /**
     * 
     * @param message
     *            to be displayed
     * @param cause
     *            of this exception to be thrown.
     */
    public GeoTiffException( String message, Throwable cause ) {
        super( message, cause );
    }

}
