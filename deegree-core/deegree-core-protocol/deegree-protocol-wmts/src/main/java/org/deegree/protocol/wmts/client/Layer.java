//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2012 by:
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
package org.deegree.protocol.wmts.client;

import java.util.List;

/**
 * A layer offered by a WMTS server.
 * 
 * @see WMTSClient
 * 
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 * @author last edited by: $Author: markus $
 * 
 * @version $Revision: $, $Date: $
 */
public class Layer {

    private final String identifier;

    private final List<Style> styles;

    private final List<String> formats;

    private final List<String> infoFormats;

    private final List<String> tileMatrixSets;

    /**
     * Creates a new {@link Layer} instance.
     * 
     * @param identifier
     *            layer identifier, must not be <code>null</code>
     * @param styles
     *            available styles for the layer, must not be <code>null</code> and contain at least one entry
     * @param formats
     *            supported valid output MIME types for a tile, must not be <code>null</code> and contain at least one
     *            entry
     * @param infoFormats
     *            supported valid output FeatureInfo output formats, may be empty, but never <code>null</code>
     * @param tileMatrixSets
     *            identifiers of the tile matrix sets, must not be <code>null</code> and contain at least one entry
     */
    Layer( String identifier, List<Style> styles, List<String> formats, List<String> infoFormats,
           List<String> tileMatrixSets ) {
        this.identifier = identifier;
        this.styles = styles;
        this.formats = formats;
        this.infoFormats = infoFormats;
        this.tileMatrixSets = tileMatrixSets;
    }

    /**
     * Returns the layer identifier.
     * 
     * @return layer identifier, never <code>null</code>
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Returns the available styles for the layer.
     * 
     * @return available styles for the layer, never <code>null</code> and contains at least one entry
     */
    public List<Style> getStyles() {
        return styles;
    }

    /**
     * Returns the supported valid output MIME types for a tile.
     * 
     * @return supported valid output MIME types, never <code>null</code> and contains at least one entry
     */
    public List<String> getFormats() {
        return formats;
    }

    /**
     * Returns the supported valid output formats for a FeatureInfo document request.
     * 
     * @return supported valid output FeatureInfo output formats, may be empty, but never <code>null</code>
     */
    public List<String> getInfoFormats() {
        return infoFormats;
    }

    /**
     * Returns the identifiers of the tile matrix sets.
     * 
     * @return identifiers of the tile matrix sets, never <code>null</code> and contains at least one entry
     */
    public List<String> getTileMatrixSets() {
        return tileMatrixSets;
    }
}
