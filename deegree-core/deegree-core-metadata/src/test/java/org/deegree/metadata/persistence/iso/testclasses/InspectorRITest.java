//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
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
package org.deegree.metadata.persistence.iso.testclasses;

import java.util.List;

import org.deegree.commons.config.WorkspaceInitializationException;
import org.deegree.metadata.persistence.MetadataInspectorException;
import org.deegree.metadata.persistence.MetadataStoreTransaction;
import org.deegree.metadata.persistence.iso.ISOMetadataStore;
import org.deegree.metadata.persistence.iso.ISOMetadataStoreProvider;
import org.deegree.metadata.persistence.iso.helper.AbstractISOTest;
import org.deegree.metadata.persistence.iso.helper.TstConstants;
import org.deegree.metadata.persistence.iso.helper.TstUtils;
import org.deegree.protocol.csw.MetadataStoreException;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class InspectorRITest extends AbstractISOTest {

    private static Logger LOG = LoggerFactory.getLogger( InspectorRITest.class );

    /**
     * If the ResourceIdentifier shouldn't be generated automaticaly and <br>
     * if there is RS_ID set and id-attribute set
     * <p>
     * Output should be 1
     * 
     * @throws MetadataStoreException
     * @throws MetadataInspectorException
     * @throws WorkspaceInitializationException
     */

    @Test
    public void testResourceIdentifierGenerateFALSE_With_ID_Attrib_RSID_Equals()
                            throws MetadataStoreException, MetadataInspectorException, WorkspaceInitializationException {
        LOG.info( "START Test: test if the configuration inserts the right ResourceIdentifier-combination while there is no automatic generating." );

        if ( jdbcURL != null && jdbcUser != null && jdbcPass != null ) {
            store = (ISOMetadataStore) new ISOMetadataStoreProvider().create( TstConstants.configURL_RS_GEN_FALSE );
        }
        if ( store == null ) {
            LOG.warn( "Skipping test (needs configuration)." );
            return;
        }
        List<String> ids = TstUtils.insertMetadata( store, TstConstants.tst_6 );
        resultSet = store.getRecordById( ids );
        int size = 0;
        while ( resultSet.next() ) {
            size++;
        }
        Assert.assertEquals( 1, size );

    }

    /**
     * Metadata that is false regarding the ResourceIdentifier
     * <p>
     * 3.xml has got an valid combination -> autmatic generating<br>
     * 4.xml has no valid combination -> autmatic generating <br>
     * 5.xml has no valid combination -> autmatic generating <br>
     * 6.xml has a valid combination -> so nothing should be generated <br>
     * 7.xml has no valid combination -> auotmatic generating <br>
     * Output should be 5 valid metadataRecords in backend
     * 
     * @throws MetadataStoreException
     * @throws MetadataInspectorException
     * @throws WorkspaceInitializationException
     */
    @Test
    public void testResourceIdentifierGenerateTRUE()
                            throws MetadataStoreException, MetadataInspectorException, WorkspaceInitializationException {
        LOG.info( "START Test: test for automaticaly generated ResourceIdentifier-combination." );

        if ( jdbcURL != null && jdbcUser != null && jdbcPass != null ) {
            store = (ISOMetadataStore) new ISOMetadataStoreProvider().create( TstConstants.configURL_RS_GEN_TRUE );
        }
        if ( store == null ) {
            LOG.warn( "Skipping test (needs configuration)." );
            return;
        }
        List<String> ids = TstUtils.insertMetadata( store, TstConstants.tst_3, TstConstants.tst_4, TstConstants.tst_5,
                                                    TstConstants.tst_6, TstConstants.tst_7, TstConstants.tst_8 );

        resultSet = store.getRecordById( ids );
        int size = 0;
        while ( resultSet.next() ) {
            size++;
        }
        Assert.assertEquals( 6, size );

    }

    /**
     * If the ResourceIdentifier shouldn't be generated automaticaly and <br>
     * if there is no neither RS_ID not id-attribute set
     * <p>
     * Output should be generate a MetadataStoreException
     * 
     * @throws MetadataStoreException
     * @throws MetadataInspectorException
     * @throws WorkspaceInitializationException
     */
    @Test
    public void testResourceIdentifierGenerateFALSE_NO_RS_ID()
                            throws MetadataStoreException, MetadataInspectorException, WorkspaceInitializationException {
        LOG.info( "START Test: test if the configuration throws an exception because of the wrong ResourceIdentifier-combination while there is no automatic generating." );

        if ( jdbcURL != null && jdbcUser != null && jdbcPass != null ) {
            store = (ISOMetadataStore) new ISOMetadataStoreProvider().create( TstConstants.configURL_RS_GEN_FALSE );
        }
        if ( store == null ) {
            LOG.warn( "Skipping test (needs configuration)." );
            return;
        }
        List<String> ids = TstUtils.insertMetadata( store, TstConstants.tst_2 );

        resultSet = store.getRecordById( ids );
        int size = 0;
        while ( resultSet.next() ) {
            size++;
        }
        Assert.assertEquals( 1, size );

    }

    /**
     * If the ResourceIdentifier shouldn't be generated automaticaly and <br>
     * there is no RS_ID set but id-attribute set
     * <p>
     * Output should be generate a MetadataStoreException
     * 
     * @throws MetadataStoreException
     * @throws MetadataInspectorException
     * @throws WorkspaceInitializationException
     */
    @Test(expected = MetadataInspectorException.class)
    public void testResourceIdentifierGenerateFALSE_With_ID_Attrib()
                            throws MetadataStoreException, MetadataInspectorException, WorkspaceInitializationException {
        LOG.info( "START Test: test if the configuration throws an exception because of the wrong ResourceIdentifier-combination while there is no automatic generating." );
        MetadataStoreTransaction ta = null;
        if ( jdbcURL != null && jdbcUser != null && jdbcPass != null ) {
            store = (ISOMetadataStore) new ISOMetadataStoreProvider().create( TstConstants.configURL_RS_GEN_FALSE );
        }
        if ( store == null ) {
            LOG.warn( "Skipping test (needs configuration)." );
            throw new MetadataInspectorException( "skipping test (needs configuration)" );
        }
        TstUtils.insertMetadata( store, TstConstants.tst_4 );

    }

    /**
     * If the ResourceIdentifier shouldn't be generated automaticaly and <br>
     * there is no RS_ID set but id-attribute and uuid-attribute set
     * <p>
     * Output should be generate a MetadataStoreException
     * 
     * @throws MetadataStoreException
     * @throws MetadataInspectorException
     * @throws WorkspaceInitializationException
     */
    @Test(expected = MetadataInspectorException.class)
    public void testResourceIdentifierGenerateFALSE_With_ID_UUID_Attrib()
                            throws MetadataStoreException, MetadataInspectorException, WorkspaceInitializationException {
        LOG.info( "START Test: test if the configuration throws an exception because of the wrong ResourceIdentifier-combination while there is no automatic generating." );
        MetadataStoreTransaction ta = null;
        if ( jdbcURL != null && jdbcUser != null && jdbcPass != null ) {
            store = (ISOMetadataStore) new ISOMetadataStoreProvider().create( TstConstants.configURL_RS_GEN_FALSE );
        }
        if ( store == null ) {
            LOG.warn( "Skipping test (needs configuration)." );
            throw new MetadataInspectorException( "skipping test (needs configuration)" );
        }
        TstUtils.insertMetadata( store, TstConstants.tst_5 );

    }

    /**
     * If the ResourceIdentifier shouldn't be generated automaticaly and <br>
     * if there is RS_ID set and id-attribute set and equals but not uuid compliant
     * <p>
     * Output should be generate a MetadataStoreException
     * 
     * @throws MetadataStoreException
     * @throws MetadataInspectorException
     * @throws WorkspaceInitializationException
     */
    @Test(expected = MetadataInspectorException.class)
    public void testResourceIdentifierGenerateFALSE_With_ID_Attrib_RSID_NOT_Equals_NO_UUID()
                            throws MetadataStoreException, MetadataInspectorException, WorkspaceInitializationException {
        MetadataStoreTransaction ta = null;
        if ( jdbcURL != null && jdbcUser != null && jdbcPass != null ) {
            store = (ISOMetadataStore) new ISOMetadataStoreProvider().create( TstConstants.configURL_RS_GEN_FALSE );
        }
        if ( store == null ) {
            LOG.warn( "Skipping test (needs configuration)." );
            throw new MetadataInspectorException( "skipping test (needs configuration)" );
        }
        TstUtils.insertMetadata( store, TstConstants.tst_8 );

    }

    /**
     * If the ResourceIdentifier shouldn't be generated automaticaly and <br>
     * if there is RS_ID set and id-attribute set but not equals
     * <p>
     * Output should be generate a MetadataStoreException
     * 
     * @throws MetadataStoreException
     * @throws MetadataInspectorException
     * @throws WorkspaceInitializationException
     */
    @Test(expected = MetadataInspectorException.class)
    public void testResourceIdentifierGenerateFALSE_With_ID_Attrib_RSID_NOT_Equals()
                            throws MetadataStoreException, MetadataInspectorException, WorkspaceInitializationException {
        LOG.info( "START Test: test if the configuration throws an exception because of the wrong ResourceIdentifier-combination while there is no automatic generating." );
        MetadataStoreTransaction ta = null;
        if ( jdbcURL != null && jdbcUser != null && jdbcPass != null ) {
            store = (ISOMetadataStore) new ISOMetadataStoreProvider().create( TstConstants.configURL_RS_GEN_FALSE );
        }
        if ( store == null ) {
            LOG.warn( "Skipping test (needs configuration)." );
            throw new MetadataInspectorException( "skipping test (needs configuration)" );
        }
        TstUtils.insertMetadata( store, TstConstants.tst_7 );

    }

}
