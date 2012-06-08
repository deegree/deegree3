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

import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.commons.config.Resource;
import org.deegree.commons.config.ResourceInitException;
import org.deegree.geometry.metadata.SpatialMetadata;

/**
 * Metadata on a {@link TileMatrixSet}.
 * 
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @author last edited by: $Author: mschneider $
 * 
 * @version $Revision: 31882 $, $Date: 2011-09-15 02:05:04 +0200 (Thu, 15 Sep 2011) $
 */
public class TileMatrixSetMetadata implements Resource {

    private final String identifier;

    private final String mimeType;

    private final SpatialMetadata spatialMetadata;

    /**
     * Creates a new {@link TileMatrixSetMetadata} instance.
     * 
     * @param identifier
     *            identifier for the {@link TileMatrixSet}, must not be <code>null</code>
     * @param mimeType
     *            mime type of the tiles, must not be <code>null</code>
     * @param spatialMetadata
     *            envelope and reference system used by the tiles, must not be <code>null</code>
     */
    public TileMatrixSetMetadata( String identifier, String mimeType, SpatialMetadata spatialMetadata ) {
        this.identifier = identifier;
        this.mimeType = mimeType;
        this.spatialMetadata = spatialMetadata;
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
     * @return the mime type, never <code>null</code>
     */
    public String getMimeType() {
        return mimeType;
    }

    /**
     * Returns the envelope and reference system used by the tiles.
     * 
     * @return envelope and reference system, never <code>null</code>
     */
    public SpatialMetadata getSpatialMetadata() {
        return spatialMetadata;
    }

    @Override
    public void init( DeegreeWorkspace workspace )
                            throws ResourceInitException {
        // nothing to do
    }

    @Override
    public void destroy() {
        // nothing to do
    }
}
