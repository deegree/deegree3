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

import static junit.framework.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import junit.framework.Assert;

import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.commons.config.ResourceInitException;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.geometry.metadata.SpatialMetadata;
import org.deegree.tile.persistence.TileStoreManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * <code>GeoTIFFTileStoreTest</code>
 * 
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$
 */
public class RemoteWMSTileStoreTest {

    private DeegreeWorkspace ws;

    @Before
    public void setup()
                            throws UnknownCRSException, IOException, URISyntaxException, ResourceInitException {

        URL wsUrl = RemoteWMSTileStoreTest.class.getResource( "workspace" );
        ws = DeegreeWorkspace.getInstance( "remotewmstilestoretest", new File( wsUrl.toURI() ) );
        ws.initAll();
    }

    @After
    public void tearDown() {
        ws.destroyAll();
    }

    @Test
    public void testGetMetdata() {
        RemoteWMSTileStore store = (RemoteWMSTileStore) ws.getSubsystemManager( TileStoreManager.class ).get( "tile1" );
        SpatialMetadata metadata = store.getMetadata();
        assertEquals( 1, metadata.getCoordinateSystems().size() );
        assertEquals( "EPSG:4326", metadata.getCoordinateSystems().get( 0 ).getAlias() );
        assertEquals( -114.2766, metadata.getEnvelope().getMin().get0(), 0.0001 );
        assertEquals( 36.96, metadata.getEnvelope().getMin().get1(), 0.0001 );
        assertEquals( -108.8986, metadata.getEnvelope().getMax().get0(), 0.0001 );
        assertEquals( 42.0343, metadata.getEnvelope().getMax().get1(), 0.0001 );        
    }

    
}
