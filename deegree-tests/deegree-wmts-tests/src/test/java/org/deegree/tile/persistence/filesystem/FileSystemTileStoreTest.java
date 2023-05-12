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

package org.deegree.tile.persistence.filesystem;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Iterator;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.deegree.tile.Tile;
import org.deegree.tile.TileDataLevel;
import org.deegree.tile.persistence.TileStore;
import org.deegree.tile.persistence.TileStoreProvider;
import org.deegree.tile.persistence.TileStoreTransaction;
import org.deegree.workspace.Workspace;
import org.deegree.workspace.standard.DefaultWorkspace;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * <code>FileSystemTileStoreTest</code>
 * 
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @author last edited by: $Author: mschneider $
 * 
 * @version $Revision: 31882 $, $Date: 2011-09-15 02:05:04 +0200 (Thu, 15 Sep 2011) $
 */
public class FileSystemTileStoreTest {

    private Workspace workspace;

    @Before
    public void setup()
                            throws URISyntaxException, IOException {
        URL u = FileSystemTileStoreTest.class.getResource( "FileSystemTileStoreTest.class" );
        File dir = new File( new File( u.toURI() ).getParentFile(),
                             "../../../../../../../src/main/webapp/WEB-INF/workspace" );
        dir = dir.getCanonicalFile();
        workspace = new DefaultWorkspace( dir );
        workspace.initAll();
    }

    @After
    public void shutdown() {
        workspace.destroy();
    }

    @Test
    public void testTileStoreCopy()
                            throws InterruptedException {
        TileStore src = workspace.getResource( TileStoreProvider.class, "pyramid" );
        TileStore dest = workspace.getResource( TileStoreProvider.class, "filesystem" );

        TileStoreTransaction ta = dest.acquireTransaction( "filesystem" );

        ExecutorService exec = Executors.newFixedThreadPool( Runtime.getRuntime().availableProcessors() );

        Iterator<TileDataLevel> iter = dest.getTileDataSet( "filesystem" ).getTileDataLevels().iterator();
        for ( TileDataLevel tm : src.getTileDataSet( "utah" ).getTileDataLevels() ) {
            String id = iter.next().getMetadata().getIdentifier();

            int maxx = (int) tm.getMetadata().getNumTilesX();
            int maxy = (int) tm.getMetadata().getNumTilesY();
            for ( int x = 0; x < maxx; ++x ) {
                Worker w = new Worker( x, maxy, tm, ta, id );
                exec.submit( w );
            }
        }

        exec.shutdown();
        exec.awaitTermination( 1, TimeUnit.HOURS );
    }

    static class Worker implements Callable<Object> {

        private final int x;

        private final int maxy;

        private final TileDataLevel src;

        private final TileStoreTransaction dest;

        private final String destId;

        Worker( int x, int maxy, TileDataLevel src, TileStoreTransaction dest, String destId ) {
            this.x = x;
            this.maxy = maxy;
            this.src = src;
            this.dest = dest;
            this.destId = destId;
        }

        @Override
        public Object call() {
            for ( int y = 0; y < maxy; ++y ) {
                Tile t = src.getTile( x, y );
                dest.put( destId, t, x, y );
            }
            return null;
        }
    }

}
