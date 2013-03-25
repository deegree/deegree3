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
package org.deegree.workspace;

import java.io.File;
import java.net.URISyntaxException;
import java.util.Set;

import org.deegree.db.ConnectionProvider;
import org.deegree.feature.persistence.FeatureStore;
import org.deegree.feature.persistence.NewFeatureStoreProvider;
import org.deegree.workspace.standard.DefaultWorkspace;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for simplesql feature store dependency management.
 * 
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @author last edited by: $Author: stranger $
 * 
 * @version $Revision: $, $Date: $
 */
public class SimpleSqlFeatureStoreTest {

    private DefaultWorkspace workspace;

    @Before
    public void setup()
                            throws URISyntaxException {
        File dir = new File( SimpleSqlFeatureStoreTest.class.getResource( "/workspace/" ).toURI() );
        this.workspace = new DefaultWorkspace( dir );
        workspace.init();
    }

    @Test
    public void testWorkspaceInitialized() {
        Assert.assertNotNull( "The feature store is expected to have a location.", workspace.getLocation() );
    }

    @Test
    public void testMissingDependency() {
        FeatureStore fs = workspace.getResource( NewFeatureStoreProvider.class, "simplesql-fail-missing-dep" );
        Assert.assertNull( "Feature store is expected not to be created.", fs );
        ResourceMetadata<FeatureStore> md = workspace.getResourceMetadata( NewFeatureStoreProvider.class,
                                                                           "simplesql-fail-missing-dep" );
        Assert.assertNotNull( "Resource metadata object is expected to be available.", md );
    }

    @Test
    public void testInvalidConfiguration() {
        FeatureStore fs = workspace.getResource( NewFeatureStoreProvider.class, "simplesql-fail-invalid-config" );
        Assert.assertNull( "Feature store is expected not to be created.", fs );
        ResourceMetadata<FeatureStore> md = workspace.getResourceMetadata( NewFeatureStoreProvider.class,
                                                                           "simplesql-fail-invalid-config" );
        Assert.assertNotNull( "Resource metadata object is expected to be available.", md );
    }

    @Test
    public void testValidConfiguration() {
        FeatureStore fs = workspace.getResource( NewFeatureStoreProvider.class, "simplesql-ok" );
        Assert.assertNotNull( "Feature store is expected to be created.", fs );
        ResourceMetadata<FeatureStore> md = workspace.getResourceMetadata( NewFeatureStoreProvider.class,
                                                                           "simplesql-ok" );
        Assert.assertNotNull( "Resource metadata object is expected to be available.", md );
        Set<ResourceIdentifier<? extends Resource>> deps = md.getDependencies();
        Assert.assertEquals( "Expected one dependency.", 1, deps.size() );
        ResourceIdentifier<? extends Resource> id = deps.iterator().next();
        Resource r = workspace.getResource( id.getProvider(), id.getId() );
        Assert.assertNotNull( "Expected dependency to be available.", r );
        Assert.assertTrue( "Expected dependency to be of type ConnectionProvider.", r instanceof ConnectionProvider );
    }

}
