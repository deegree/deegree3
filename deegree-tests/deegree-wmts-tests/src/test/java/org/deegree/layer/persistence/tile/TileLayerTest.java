//$HeadURL: svn+ssh://aschmitz@wald.intevation.org/deegree/base/trunk/resources/eclipse/files_template.xml $
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2011 by:
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
package org.deegree.layer.persistence.tile;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.commons.config.ResourceInitException;
import org.deegree.layer.Layer;
import org.deegree.layer.persistence.OldLayerStoreManager;
import org.deegree.tile.persistence.filesystem.FileSystemTileStoreTest;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author: stranger $
 * 
 * @version $Revision: $, $Date: $
 */
public class TileLayerTest {

    private DeegreeWorkspace workspace;

    @Before
    public void setup()
                            throws URISyntaxException, IOException, ResourceInitException {
        URL u = FileSystemTileStoreTest.class.getResource( "FileSystemTileStoreTest.class" );
        File dir = new File( new File( u.toURI() ).getParentFile(),
                             "../../../../../../../src/main/webapp/WEB-INF/workspace" );
        dir = dir.getCanonicalFile();
        workspace = DeegreeWorkspace.getInstance( "deegree-wmts-tests", dir );
        workspace.initAll();
    }

    @Test
    public void testMetadataId() {
        OldLayerStoreManager mgr = workspace.getSubsystemManager( OldLayerStoreManager.class );
        Layer l = mgr.get( "tilelayers" ).get( "pyramid" );
        Assert.assertEquals( "mdsetid", l.getMetadata().getMetadataId() );
    }

    @After
    public void shutdown() {
        workspace.destroyAll();
    }

}
