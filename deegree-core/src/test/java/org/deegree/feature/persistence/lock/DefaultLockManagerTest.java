//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
 Department of Geography, University of Bonn
 and
 lat/lon GmbH

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

package org.deegree.feature.persistence.lock;

import java.io.IOException;
import java.net.URL;

import javax.xml.bind.JAXBException;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

import org.deegree.commons.xml.XMLParsingException;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.feature.persistence.FeatureStore;
import org.deegree.feature.persistence.FeatureStoreException;
import org.deegree.feature.persistence.memory.MemoryFeatureStore;
import org.deegree.feature.types.ApplicationSchema;
import org.deegree.gml.GMLVersion;
import org.deegree.gml.ReferenceResolvingException;
import org.deegree.gml.feature.schema.ApplicationSchemaXSDDecoder;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the {@link DefaultLockManager}.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class DefaultLockManagerTest {

    private static final String BASE_DIR = "../../../gml/feature/testdata/features/";

    private LockManager lockManager;

    @Before
    public void setUp()
                            throws XMLParsingException, XMLStreamException, UnknownCRSException,
                            FactoryConfigurationError, IOException, JAXBException, FeatureStoreException, ReferenceResolvingException, ClassCastException, ClassNotFoundException, InstantiationException, IllegalAccessException {

        String schemaURL = this.getClass().getResource( "/org/deegree/gml/feature/testdata/schema/Philosopher.xsd" ).toString();
        ApplicationSchemaXSDDecoder adapter = new ApplicationSchemaXSDDecoder( GMLVersion.GML_31, null, schemaURL );        
        ApplicationSchema schema = adapter.extractFeatureTypeSchema();

        URL docURL = DefaultLockManagerTest.class.getResource( BASE_DIR + "Philosopher_FeatureCollection.xml" );
        FeatureStore store = new MemoryFeatureStore( docURL, schema );
        lockManager = store.getLockManager();
    }

    @Test
    public void testLockAllAndRelease()
                            throws FeatureStoreException {

//        // acquire lock on all Philosopher features
//        QName ftName = new QName( "http://www.deegree.org/app", "Philosopher" );
//        LockOperation lockRequest = new FilterLock( null, new TypeName( ftName, null ), null );
//        Lock lock = lockManager.acquireLock( new LockOperation[] { lockRequest }, true, 600 * 1000 );
//
//        // check that all seven instances are locked
//        List<String> lockedFids = lock.getLockedFeatures().getAsListAndClose();
//        assertEquals( 7, lockedFids.size() );
//        for ( String fid : lockedFids ) {
//            assertTrue( lock.isLocked( fid ) );
//            assertTrue( lockManager.isFeatureLocked( fid ) );
//            assertTrue( lockManager.isFeatureModifiable( fid, lock.getId() ) );
//            // must not be modifiable when specifying any other lock id
//            assertFalse( lockManager.isFeatureModifiable( fid, lock.getId() + "42" ) );
//        }
//
//        // unlock all features
//        lock.release();
//        for ( String fid : lockedFids ) {
//            assertFalse( lockManager.isFeatureLocked( fid ) );
//        }
//
//        // now zero features must be locked
//        assertEquals( 0, lock.getLockedFeatures().getAsListAndClose().size() );
    }
}
