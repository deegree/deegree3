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
 ----------------------------------------------------------------------------*/
package org.deegree.tile.persistence.filesystem;

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
import org.deegree.tile.DefaultTileDataSet;
import org.deegree.tile.TileDataLevel;
import org.deegree.tile.TileDataSet;
import org.deegree.tile.TileMatrix;
import org.deegree.tile.TileMatrixSet;
import org.deegree.tile.persistence.TileStoreProvider;
import org.deegree.tile.persistence.filesystem.jaxb.FileSystemTileStoreJAXB;
import org.deegree.tile.persistence.filesystem.layout.TileCacheDiskLayout;
import org.deegree.tile.tilematrixset.TileMatrixSetProvider;
import org.slf4j.Logger;

/**
 * {@link TileStoreProvider} for the {@link FileSystemTileStore}.
 * 
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class FileSystemTileStoreProvider implements TileStoreProvider {

    private static final Logger LOG = getLogger( FileSystemTileStoreProvider.class );

    private static final String CONFIG_NAMESPACE = "http://www.deegree.org/datasource/tile/filesystem";

    private static final URL CONFIG_SCHEMA = FileSystemTileStoreProvider.class.getResource( "/META-INF/schemas/datasource/tile/filesystem/3.2.0/filesystem.xsd" );

    private static final String JAXB_PACKAGE = "org.deegree.tile.persistence.filesystem.jaxb";

    private DeegreeWorkspace workspace;

    @Override
    public void init( DeegreeWorkspace workspace ) {
        this.workspace = workspace;
    }

    @Override
    public FileSystemTileStore create( URL configUrl )
                            throws ResourceInitException {
        try {

            FileSystemTileStoreJAXB config = (FileSystemTileStoreJAXB) unmarshall( JAXB_PACKAGE, CONFIG_SCHEMA,
                                                                                   configUrl, workspace );

            Map<String, TileDataSet> map = new HashMap<String, TileDataSet>();

            for ( FileSystemTileStoreJAXB.TileDataSet tds : config.getTileDataSet() ) {
                String id = tds.getIdentifier();
                String tmsId = tds.getTileMatrixSetId();
                org.deegree.tile.persistence.filesystem.jaxb.FileSystemTileStoreJAXB.TileDataSet.TileCacheDiskLayout lay = tds.getTileCacheDiskLayout();

                File parent = new File( configUrl.toURI() ).getParentFile();
                File baseDir = new File( lay.getLayerDirectory() );
                if ( !baseDir.isAbsolute() ) {
                    baseDir = new File( parent, lay.getLayerDirectory() );
                }

                TileCacheDiskLayout layout = new TileCacheDiskLayout( baseDir, lay.getFileType() );

                TileMatrixSet tms = workspace.getNewWorkspace().getResource( TileMatrixSetProvider.class, tmsId );
                if ( tms == null ) {
                    throw new ResourceInitException( "No tile matrix set with id " + tmsId + " is available!" );
                }

                List<TileDataLevel> list = new ArrayList<TileDataLevel>( tms.getTileMatrices().size() );

                for ( TileMatrix tm : tms.getTileMatrices() ) {
                    list.add( new FileSystemTileDataLevel( tm, layout ) );
                }

                String format = "image/" + layout.getFileType();

                DefaultTileDataSet dataset = new DefaultTileDataSet( list, tms, format );
                layout.setTileMatrixSet( dataset );
                map.put( id, dataset );
            }

            return new FileSystemTileStore( map );
        } catch ( ResourceInitException e ) {
            throw e;
        } catch ( Throwable e ) {
            String msg = "Unable to create FileSystemTileStore: " + e.getMessage();
            LOG.error( msg, e );
            throw new ResourceInitException( msg, e );
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<? extends ResourceManager>[] getDependencies() {
        return new Class[] {};
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
