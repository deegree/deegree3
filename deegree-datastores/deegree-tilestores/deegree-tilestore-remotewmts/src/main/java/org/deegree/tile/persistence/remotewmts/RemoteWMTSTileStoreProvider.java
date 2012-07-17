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
package org.deegree.tile.persistence.remotewmts;

import static org.deegree.commons.xml.jaxb.JAXBUtils.unmarshall;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.commons.config.ResourceInitException;
import org.deegree.commons.config.ResourceManager;
import org.deegree.protocol.wmts.client.WMTSClient;
import org.deegree.remoteows.RemoteOWS;
import org.deegree.remoteows.RemoteOWSManager;
import org.deegree.remoteows.wmts.RemoteWMTS;
import org.deegree.tile.DefaultTileDataSet;
import org.deegree.tile.TileDataLevel;
import org.deegree.tile.TileDataSet;
import org.deegree.tile.TileMatrix;
import org.deegree.tile.TileMatrixSet;
import org.deegree.tile.persistence.GenericTileStore;
import org.deegree.tile.persistence.TileStore;
import org.deegree.tile.persistence.TileStoreProvider;
import org.deegree.tile.persistence.remotewmts.jaxb.RemoteWMTSTileStoreJAXB;
import org.deegree.tile.persistence.remotewmts.jaxb.RemoteWMTSTileStoreJAXB.TileDataSet.RequestParams;
import org.slf4j.Logger;

/**
 * {@link TileStoreProvider} for <code>RemoteWMTSTileStore</code>
 * 
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$
 */
public class RemoteWMTSTileStoreProvider implements TileStoreProvider {

    private static final Logger LOG = getLogger( RemoteWMTSTileStoreProvider.class );

    private static final String CONFIG_NAMESPACE = "http://www.deegree.org/datasource/tile/remotewmts";

    private static final URL CONFIG_SCHEMA = RemoteWMTSTileStoreProvider.class.getResource( "/META-INF/schemas/datasource/tile/remotewmts/3.2.0/remotewmts.xsd" );

    private static final String JAXB_PACKAGE = "org.deegree.tile.persistence.remotewmts.jaxb";

    private DeegreeWorkspace workspace;

    @Override
    public void init( DeegreeWorkspace workspace ) {
        this.workspace = workspace;
    }

    @Override
    public TileStore create( URL configUrl )
                            throws ResourceInitException {
        try {
            RemoteWMTSTileStoreJAXB config = unmarshallConfig( configUrl );
            RemoteWMTS wmts = getRemoteWmts( config.getRemoteWMTSId() );
            Map<String, TileDataSet> map = buildTileDataSetMap( config, wmts );
            return new GenericTileStore( map );
        } catch ( ResourceInitException e ) {
            throw e;
        } catch ( Throwable e ) {
            String msg = "Unable to create RemoteWMSTileStore: " + e.getMessage();
            LOG.error( msg, e );
            throw new ResourceInitException( msg, e );
        }
    }

    private RemoteWMTSTileStoreJAXB unmarshallConfig( URL configUrl )
                            throws JAXBException {
        return (RemoteWMTSTileStoreJAXB) unmarshall( JAXB_PACKAGE, CONFIG_SCHEMA, configUrl, workspace );
    }

    private RemoteWMTS getRemoteWmts( String remoteWmtsId )
                            throws ResourceInitException {
        RemoteOWSManager owsMgr = workspace.getSubsystemManager( RemoteOWSManager.class );
        RemoteOWS wmts = owsMgr.get( remoteWmtsId );
        if ( wmts == null ) {
            throw new ResourceInitException( "No remote WMTS with id " + remoteWmtsId + " available." );
        }
        if ( !( wmts instanceof RemoteWMTS ) ) {
            String msg = "The remote WMTS id " + remoteWmtsId + " must correspond to a WMTS instance (was "
                         + wmts.getClass().getSimpleName() + ")";
            throw new ResourceInitException( msg );
        }
        return (RemoteWMTS) wmts;
    }

    private Map<String, TileDataSet> buildTileDataSetMap( RemoteWMTSTileStoreJAXB config, RemoteWMTS wmts ) {

        Map<String, TileDataSet> map = new HashMap<String, TileDataSet>();
        for ( RemoteWMTSTileStoreJAXB.TileDataSet tileDataSetConfig : config.getTileDataSet() ) {
            String tileDataSetId = tileDataSetConfig.getIdentifier();
            String outputFormat = tileDataSetConfig.getOutputFormat();
            WMTSClient client = wmts.getClient();
            TileDataSet tileDataSet = buildTileDataSet( tileDataSetConfig, client, outputFormat );
            map.put( tileDataSetId, tileDataSet );
        }
        return map;
    }

    private TileDataSet buildTileDataSet( RemoteWMTSTileStoreJAXB.TileDataSet tileDataSetConfig, WMTSClient client,
                                          String outputFormat ) {

        if ( outputFormat.startsWith( "image/" ) ) {
            outputFormat = outputFormat.substring( 6 );
        }

        RequestParams requestParams = tileDataSetConfig.getRequestParams();
        String tileMatrixSetId = requestParams.getTileMatrixSet();
        TileMatrixSet tileMatrixSet = client.getTileMatrixSet( tileMatrixSetId );
        List<TileDataLevel> dataLevels = buildTileDataLevels( tileMatrixSet, requestParams, client, outputFormat );

        return new DefaultTileDataSet( dataLevels, tileMatrixSet, "image/" + outputFormat );
    }

    private List<TileDataLevel> buildTileDataLevels( TileMatrixSet tileMatrixSet, RequestParams requestParams,
                                                     WMTSClient client, String outputFormat ) {
        String layer = requestParams.getLayer();
        String style = requestParams.getStyle();
        String format = requestParams.getFormat();
        String tileMatrixSetId = requestParams.getTileMatrixSet();

        List<TileDataLevel> dataLevels = new ArrayList<TileDataLevel>();
        for ( TileMatrix tileMatrix : tileMatrixSet.getTileMatrices() ) {
            TileDataLevel level = buildTileDataLevel( tileMatrix, tileMatrixSetId, layer, style, format, client,
                                                      outputFormat );
            dataLevels.add( level );
        }
        return dataLevels;
    }

    private TileDataLevel buildTileDataLevel( TileMatrix tileMatrix, String tileMatrixSetId, String layer,
                                              String style, String format, WMTSClient client, String outputFormat ) {
        return new RemoteWMTSTileDataLevel( tileMatrix, tileMatrixSetId, format, layer, style, client, outputFormat );
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
}
