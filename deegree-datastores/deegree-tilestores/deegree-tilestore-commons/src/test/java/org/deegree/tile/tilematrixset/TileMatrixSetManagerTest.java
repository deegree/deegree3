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

package org.deegree.tile.tilematrixset;

import java.io.File;

import org.deegree.commons.config.ResourceInitException;
import org.deegree.workspace.Workspace;
import org.deegree.workspace.standard.DefaultWorkspace;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * <code>TileMatrixSetManagerTest</code>
 * 
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @author last edited by: $Author: mschneider $
 * 
 * @version $Revision: 31882 $, $Date: 2011-09-15 02:05:04 +0200 (Thu, 15 Sep 2011) $
 */

public class TileMatrixSetManagerTest {

    private Workspace workspace;

    @Before
    public void setup()
                            throws ResourceInitException {
        workspace = new DefaultWorkspace( new File( "nix" ) );
        workspace.initAll();
    }

    @After
    public void shutdown() {
        workspace.destroy();
    }

    @Test
    public void testWellKnownScaleSets() {
        Assert.assertNotNull( "globalcrs84pixel not defined.",
                              workspace.getResource( TileMatrixSetProvider.class, "globalcrs84pixel" ) );
        Assert.assertNotNull( "globalcrs84scale not defined.",
                              workspace.getResource( TileMatrixSetProvider.class, "globalcrs84scale" ) );
        Assert.assertNotNull( "googlecrs84quad not defined.",
                              workspace.getResource( TileMatrixSetProvider.class, "googlecrs84quad" ) );
        Assert.assertNotNull( "googlemapscompatible not defined.",
                              workspace.getResource( TileMatrixSetProvider.class, "googlemapscompatible" ) );
        Assert.assertNotNull( "inspirecrs84quad not defined.",
                              workspace.getResource( TileMatrixSetProvider.class, "inspirecrs84quad" ) );
    }

}
