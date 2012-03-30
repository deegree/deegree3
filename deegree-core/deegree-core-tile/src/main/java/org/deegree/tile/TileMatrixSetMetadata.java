//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
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

 Occam Labs UG (haftungsbeschränkt)
 Godesberger Allee 139, 53175 Bonn
 Germany
 http://www.occamlabs.de/

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/

package org.deegree.tile;

import org.deegree.cs.coordinatesystems.ICRS;

/**
 * Metadata on a {@link TileMatrixSet}.
 * 
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @author last edited by: $Author: mschneider $
 * 
 * @version $Revision: 31882 $, $Date: 2011-09-15 02:05:04 +0200 (Thu, 15 Sep 2011) $
 */
public class TileMatrixSetMetadata {

    private final String identifier;

    private final String format;

    private final ICRS crs;

    /**
     * Creates a new {@link TileMatrixSetMetadata} instance.
     * 
     * @param identifier
     *            identifier for the {@link TileMatrixSet}, must not be <code>null</code>
     * @param nativeFormat
     *            image format of the tiles, can be <code>null</code>
     * @param crs
     *            reference system used by the tiles, must not be <code>null</code>
     */
    public TileMatrixSetMetadata( String identifier, String nativeFormat, ICRS crs ) {
        this.identifier = identifier;
        this.format = nativeFormat;
        this.crs = crs;
    }

    /**
     * Returns the identifier for the {@link TileMatrixSet}.
     * 
     * @return identifier, never <code>null</code>
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Returns the image format of the tiles.
     * 
     * @return format, never <code>null</code>
     */
    public String getFormat() {
        return format;
    }

    /**
     * Returns the reference system used by the tiles.
     * 
     * @return reference system, never <code>null</code>
     */
    public ICRS getCrs() {
        return crs;
    }
}
