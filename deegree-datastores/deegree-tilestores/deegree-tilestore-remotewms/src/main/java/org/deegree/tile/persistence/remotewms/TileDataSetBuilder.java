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
package org.deegree.tile.persistence.remotewms;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.deegree.commons.config.ResourceInitException;
import org.deegree.commons.utils.StringUtils;
import org.deegree.protocol.wms.client.WMSClient;
import org.deegree.remoteows.wms.RemoteWMS;
import org.deegree.tile.DefaultTileDataSet;
import org.deegree.tile.TileDataLevel;
import org.deegree.tile.TileDataSet;
import org.deegree.tile.TileMatrix;
import org.deegree.tile.TileMatrixSet;
import org.deegree.tile.persistence.remotewms.jaxb.RemoteWMSTileStoreJAXB;
import org.deegree.tile.persistence.remotewms.jaxb.RemoteWMSTileStoreJAXB.TileDataSet.RequestParams;
import org.deegree.tile.tilematrixset.TileMatrixSetManager;

/**
 * Builds tile data sets from jaxb config beans.
 * 
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @author last edited by: $Author: stranger $
 * 
 * @version $Revision: $, $Date: $
 */
class TileDataSetBuilder {

    private final RemoteWMSTileStoreJAXB config;

    private final RemoteWMS wms;

    private final TileMatrixSetManager tmsMgr;

    TileDataSetBuilder( RemoteWMSTileStoreJAXB config, RemoteWMS wms, TileMatrixSetManager tmsMgr ) {
        this.config = config;
        this.wms = wms;
        this.tmsMgr = tmsMgr;
    }

    Map<String, TileDataSet> extractTileDataSets()
                            throws ResourceInitException {
        Map<String, TileDataSet> map = new HashMap<String, TileDataSet>();
        for ( RemoteWMSTileStoreJAXB.TileDataSet cfg : config.getTileDataSet() ) {
            String id = cfg.getIdentifier();
            String tmsId = cfg.getTileMatrixSetId();
            TileMatrixSet tms = tmsMgr.get( tmsId );
            if ( tms == null ) {
                throw new ResourceInitException( "The tile matrix set with id " + tmsId + " was not available." );
            }

            RequestParams params = cfg.getRequestParams();
            boolean getFeaturesSupported = cfg.getGetFeatureInfo() == null ? true : cfg.getGetFeatureInfo().isEnabled();
            map.put( id, buildTileDataSet( params, tms, wms.getClient(), cfg.getOutputFormat(), getFeaturesSupported ) );
        }
        return map;
    }

    private DefaultTileDataSet buildTileDataSet( RequestParams requestParams, TileMatrixSet tms, WMSClient client,
                                                 String outputFormat, boolean getFeaturesSupported ) {
        List<String> layers = splitNullSafe( requestParams.getLayers() );
        List<String> styles = splitNullSafe( requestParams.getStyles() );
        String format = requestParams.getFormat();
        String crs = requestParams.getCRS();

        if ( outputFormat.startsWith( "image/" ) ) {
            outputFormat = outputFormat.substring( 6 );
        }

        List<TileDataLevel> dataLevels = new ArrayList<TileDataLevel>();
        for ( TileMatrix tm : tms.getTileMatrices() ) {
            TileDataLevel m = new RemoteWMSTileDataLevel( tm, format, layers, styles, client, outputFormat, crs );
            dataLevels.add( 0, m );
        }
        return new DefaultTileDataSet( dataLevels, tms, "image/" + outputFormat, getFeaturesSupported );
    }

    private static List<String> splitNullSafe( String csv ) {
        if ( csv == null ) {
            return emptyList();
        }
        String[] tokens = StringUtils.split( csv, "," );
        return asList( tokens );
    }

}
