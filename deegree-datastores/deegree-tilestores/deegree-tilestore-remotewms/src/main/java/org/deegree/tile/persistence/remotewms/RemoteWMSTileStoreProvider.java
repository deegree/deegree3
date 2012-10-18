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

 Occam Labs UG (haftungsbeschränkt)
 Godesberger Allee 139, 53175 Bonn
 Germany
 http://www.occamlabs.de/

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.tile.persistence.remotewms;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.deegree.commons.xml.jaxb.JAXBUtils.unmarshall;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.commons.config.ResourceInitException;
import org.deegree.commons.config.ResourceManager;
import org.deegree.commons.utils.StringUtils;
import org.deegree.protocol.wms.client.WMSClient;
import org.deegree.remoteows.RemoteOWS;
import org.deegree.remoteows.RemoteOWSManager;
import org.deegree.remoteows.wms.RemoteWMS;
import org.deegree.tile.DefaultTileDataSet;
import org.deegree.tile.TileDataLevel;
import org.deegree.tile.TileDataSet;
import org.deegree.tile.TileMatrix;
import org.deegree.tile.TileMatrixSet;
import org.deegree.tile.persistence.GenericTileStore;
import org.deegree.tile.persistence.TileStore;
import org.deegree.tile.persistence.TileStoreProvider;
import org.deegree.tile.persistence.remotewms.jaxb.RemoteWMSTileStoreJAXB;
import org.deegree.tile.persistence.remotewms.jaxb.RemoteWMSTileStoreJAXB.TileDataSet.RequestParams;
import org.deegree.tile.tilematrixset.TileMatrixSetManager;
import org.slf4j.Logger;

/**
 * {@link TileStoreProvider} for the {@link RemoteWMSTileStore}.
 * 
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$
 */
public class RemoteWMSTileStoreProvider implements TileStoreProvider {

    private static final Logger LOG = getLogger( RemoteWMSTileStoreProvider.class );

    private static final String CONFIG_NAMESPACE = "http://www.deegree.org/datasource/tile/remotewms";

    private static final URL CONFIG_SCHEMA = RemoteWMSTileStoreProvider.class.getResource( "/META-INF/schemas/datasource/tile/remotewms/3.2.0/remotewms.xsd" );

    private static final String JAXB_PACKAGE = "org.deegree.tile.persistence.remotewms.jaxb";

    private DeegreeWorkspace workspace;

    @Override
    public void init( DeegreeWorkspace workspace ) {
        this.workspace = workspace;
    }

    private Map<String, TileDataSet> extractTileDataSets( RemoteWMSTileStoreJAXB config, RemoteWMS wms )
                            throws ResourceInitException {
        TileMatrixSetManager tmsMgr = workspace.getSubsystemManager( TileMatrixSetManager.class );
        Map<String, TileDataSet> map = new HashMap<String, TileDataSet>();
        for ( RemoteWMSTileStoreJAXB.TileDataSet cfg : config.getTileDataSet() ) {
            String id = cfg.getIdentifier();
            String tmsId = cfg.getTileMatrixSetId();
            TileMatrixSet tms = tmsMgr.get( tmsId );
            if ( tms == null ) {
                throw new ResourceInitException( "The tile matrix set with id " + tmsId + " was not available." );
            }

            RequestParams params = cfg.getRequestParams();
            map.put( id, buildTileDataSet( params, tms, wms.getClient(), cfg.getOutputFormat() ) );
        }
        return map;
    }

    @Override
    public TileStore create( URL configUrl )
                            throws ResourceInitException {
        try {

            RemoteWMSTileStoreJAXB config = (RemoteWMSTileStoreJAXB) unmarshall( JAXB_PACKAGE, CONFIG_SCHEMA,
                                                                                 configUrl, workspace );

            String wmsId = config.getRemoteWMSId();
            RemoteOWSManager wmsMgr = workspace.getSubsystemManager( RemoteOWSManager.class );
            RemoteOWS wms = wmsMgr.get( wmsId );
            if ( !( wms instanceof RemoteWMS ) ) {
                if ( wms == null ) {
                    throw new ResourceInitException( "The remote wms with id " + wmsId + " was not available." );
                }
                throw new ResourceInitException( "The remote wms id " + wmsId
                                                 + " must correspond to a WMS instance (was "
                                                 + wms.getClass().getSimpleName() + ")" );
            }

            Map<String, TileDataSet> map = extractTileDataSets( config, (RemoteWMS) wms );

            return new GenericTileStore( map );
        } catch ( ResourceInitException e ) {
            throw e;
        } catch ( Throwable e ) {
            String msg = "Unable to create RemoteWMSTileStore: " + e.getMessage();
            LOG.error( msg, e );
            throw new ResourceInitException( "Unable to create RemoteWMSTileStore: " + e.getMessage(), e );
        }
    }

    private DefaultTileDataSet buildTileDataSet( RequestParams requestParams, TileMatrixSet tms, WMSClient client,
                                                 String outputFormat ) {
        List<String> layers = splitNullSafe( requestParams.getLayers() );
        List<String> styles = splitNullSafe( requestParams.getStyles() );
        String format = requestParams.getFormat();

        if ( outputFormat.startsWith( "image/" ) ) {
            outputFormat = outputFormat.substring( 6 );
        }

        List<TileDataLevel> dataLevels = new ArrayList<TileDataLevel>();
        for ( TileMatrix tm : tms.getTileMatrices() ) {
            TileDataLevel m = new RemoteWMSTileDataLevel( tm, format, layers, styles, client, outputFormat );
            dataLevels.add( 0, m );
        }

        return new DefaultTileDataSet( dataLevels, tms, "image/" + outputFormat );
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<? extends ResourceManager>[] getDependencies() {
        return new Class[] { RemoteOWSManager.class };
    }

    @Override
    public String getConfigNamespace() {
        return CONFIG_NAMESPACE;
    }

    @Override
    public URL getConfigSchema() {
        return CONFIG_SCHEMA;
    }

    @Override
    public List<File> getTileStoreDependencies( File config ) {
        return Collections.<File> emptyList();
    }

    private static List<String> splitNullSafe( String csv ) {
        if ( csv == null ) {
            return emptyList();
        }
        String[] tokens = StringUtils.split( csv, "," );
        return asList( tokens );
    }

}
