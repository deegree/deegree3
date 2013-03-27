//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2012 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -
 and
 - Occam Labs UG (haftungsbeschränkt) -

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

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.protocol.wmts.ops;

import static org.deegree.commons.ows.exception.OWSException.INVALID_PARAMETER_VALUE;
import static org.deegree.commons.ows.exception.OWSException.MISSING_PARAMETER_VALUE;

import java.util.Map;

import org.deegree.commons.ows.exception.OWSException;

/**
 * Request bean containing WMTS GetFeatureInfo values.
 * 
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @author last edited by: $Author: stranger $
 * 
 * @version $Revision: $, $Date: $
 */
public class GetFeatureInfo {

    private final String layer;

    private final String style;

    private String infoFormat;

    private final String tileMatrixSet;

    private final String tileMatrix;

    private long tileRow;

    private long tileCol;

    private int i, j;

    private Map<String, String> overriddenParameters;

    /**
     * The parameter values correspond to what's described in the spec.
     * 
     * @param layer
     * @param style
     * @param infoFormat
     * @param tileMatrixSet
     * @param tileMatrix
     * @param tileRow
     * @param tileCol
     * @param i
     * @param j
     */
    public GetFeatureInfo( String layer, String style, String infoFormat, String tileMatrixSet, String tileMatrix,
                           long tileRow, long tileCol, int i, int j, Map<String, String> overriddenParameters ) {
        this.layer = layer;
        this.style = style;
        this.infoFormat = infoFormat;
        this.tileMatrixSet = tileMatrixSet;
        this.tileMatrix = tileMatrix;
        this.tileRow = tileRow;
        this.tileCol = tileCol;
        this.i = i;
        this.j = j;
        this.overriddenParameters = overriddenParameters;
    }

    public GetFeatureInfo( Map<String, String> map ) throws OWSException {
        this.layer = map.get( "LAYER" );
        this.style = map.get( "STYLE" );
        this.infoFormat = map.get( "INFOFORMAT" );
        if ( infoFormat == null ) {
            // for friends of the WMS...
            infoFormat = map.get( "INFO_FORMAT" );
        }
        if ( infoFormat == null ) {
            throw new OWSException( "The INFOFORMAT parameter is missing.", MISSING_PARAMETER_VALUE, "INFOFORMAT" );
        }
        this.tileMatrixSet = map.get( "TILEMATRIXSET" );
        this.tileMatrix = map.get( "TILEMATRIX" );
        parseTileRowCol( map );
        parseIj( map );
    }

    private void parseIj( Map<String, String> map )
                            throws OWSException {
        String i = map.get( "I" );
        if ( i == null ) {
            throw new OWSException( "The I parameter is missing.", MISSING_PARAMETER_VALUE, "I" );
        }
        try {
            this.i = Integer.parseInt( i );
        } catch ( NumberFormatException e ) {
            throw new OWSException( "The I parameter value of '" + i + "' is not valid.", INVALID_PARAMETER_VALUE, "I" );
        }
        String j = map.get( "J" );
        if ( j == null ) {
            throw new OWSException( "The J parameter is missing.", MISSING_PARAMETER_VALUE, "J" );
        }
        try {
            this.j = Integer.parseInt( j );
        } catch ( NumberFormatException e ) {
            throw new OWSException( "The J parameter value of '" + j + "' is not valid.", INVALID_PARAMETER_VALUE, "J" );
        }
    }

    private void parseTileRowCol( Map<String, String> map )
                            throws OWSException {
        String row = map.get( "TILEROW" );
        if ( row == null ) {
            throw new OWSException( "The TILEROW parameter is missing.", MISSING_PARAMETER_VALUE, "TILEROW" );
        }
        try {
            this.tileRow = Integer.parseInt( row );
        } catch ( NumberFormatException e ) {
            throw new OWSException( "The TILEROW parameter value of '" + row + "' is not a valid index.",
                                    INVALID_PARAMETER_VALUE, "TILEROW" );
        }
        String col = map.get( "TILECOL" );
        if ( col == null ) {
            throw new OWSException( "The TILECOL parameter is missing.", MISSING_PARAMETER_VALUE, "TILECOL" );
        }
        try {
            this.tileCol = Integer.parseInt( col );
        } catch ( NumberFormatException e ) {
            throw new OWSException( "The TILECOL parameter value of '" + col + "' is not a valid index.",
                                    INVALID_PARAMETER_VALUE, "TILECOL" );
        }
    }

    /**
     * @return the layer
     */
    public String getLayer() {
        return layer;
    }

    /**
     * @return the style
     */
    public String getStyle() {
        return style;
    }

    /**
     * @return the infoFormat
     */
    public String getInfoFormat() {
        return infoFormat;
    }

    /**
     * @return the tileMatrixSet
     */
    public String getTileMatrixSet() {
        return tileMatrixSet;
    }

    /**
     * @return the tileMatrix
     */
    public String getTileMatrix() {
        return tileMatrix;
    }

    /**
     * @return the tileRow
     */
    public long getTileRow() {
        return tileRow;
    }

    /**
     * @return the tileCol
     */
    public long getTileCol() {
        return tileCol;
    }

    /**
     * @return the i
     */
    public int getI() {
        return i;
    }

    /**
     * @return the j
     */
    public int getJ() {
        return j;
    }

    /**
     * @return null, or parameters to override when used in client requests
     */
    public Map<String, String> getOverriddenParameters() {
        return overriddenParameters;
    }

}
