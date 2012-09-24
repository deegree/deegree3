//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/deegree3/trunk/deegree-core/deegree-core-metadata/src/main/java/org/deegree/metadata/iso/persistence/ISOMetadataStoreProvider.java $
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

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.metadata.iso.persistence.memory;

import static java.util.Collections.singletonList;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.deegree.metadata.iso.ISORecord;
import org.deegree.metadata.persistence.MetadataResultSet;
import org.junit.Test;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 * @author last edited by: $Author: lyn $
 * 
 * @version $Revision: 30992 $, $Date: 2011-05-31 16:09:20 +0200 (Di, 31. Mai 2011) $
 */
public class StoredISORecordsTest {

    @Test
    public void testGetRecordById()
                            throws Exception {
        StoredISORecords storedIsoRecords = getStoredIsoRecords();
        MetadataResultSet<ISORecord> recordResultSet = storedIsoRecords.getRecordById( singletonList( "f90258d9a412aa5f3ba679b4997bb176" ),
                                                                                       null );

        assertTrue( recordResultSet.next() );
        assertNotNull( recordResultSet.getRecord() );
        assertFalse( recordResultSet.next() );
    }

    @Test
    public void testGetRecordByIdWithUnknownId()
                            throws Exception {
        StoredISORecords storedIsoRecords = getStoredIsoRecords();
        MetadataResultSet<ISORecord> recordResultSet = storedIsoRecords.getRecordById( singletonList( "UNKNOWN_ID" ),
                                                                                       null );

        assertFalse( recordResultSet.next() );
    }

    @Test
    public void testGetRecordByIds()
                            throws Exception {
        StoredISORecords storedIsoRecords = getStoredIsoRecords();
        List<String> ids = new ArrayList<String>();
        ids.add( "f90258d9a412aa5f3ba679b4997bb176" );
        ids.add( "15c1c1bbe5b4409c2fe10639bb54330f" );
        MetadataResultSet<ISORecord> recordResultSet = storedIsoRecords.getRecordById( ids, null );

        assertTrue( recordResultSet.next() );
        assertNotNull( recordResultSet.getRecord() );
        assertTrue( recordResultSet.next() );
        assertNotNull( recordResultSet.getRecord() );
        assertFalse( recordResultSet.next() );
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetRecordByIdNullList()
                            throws Exception {
        StoredISORecords storedIsoRecords = getStoredIsoRecords();
        storedIsoRecords.getRecordById( null, null );
    }

    private StoredISORecords getStoredIsoRecords()
                            throws Exception {
        StoredISORecords storedRecords = new StoredISORecords();

        List<ISORecord> allRecords = GetTestRecordsUtils.getAllRecords();
        for ( ISORecord record : allRecords ) {
            storedRecords.addRecord( record );
        }
        return storedRecords;
    }

}
