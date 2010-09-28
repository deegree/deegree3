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
package org.deegree.metadata.persistence.iso;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.om.OMElement;
import org.deegree.CoreTstProperties;
import org.deegree.commons.jdbc.ConnectionManager;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.metadata.ISORecord;
import org.deegree.metadata.MetadataRecord;
import org.deegree.metadata.persistence.MetadataStoreException;
import org.deegree.metadata.persistence.MetadataStoreTransaction;
import org.deegree.metadata.publication.InsertTransaction;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class ISOMetadataStoreTest {

    private static final Logger LOG = getLogger( ISOMetadataStoreTest.class );

    private static final URL configURL = ISOMetadataStoreTest.class.getResource( "iso19115.xml" );

    private ISOMetadataStore store;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp()
                            throws Exception {
        String jdbcURL = CoreTstProperties.getProperty( "iso_store_url" );
        String jdbcUser = CoreTstProperties.getProperty( "iso_store_user" );
        String jdbcPass = CoreTstProperties.getProperty( "iso_store_pass" );
        if ( jdbcURL != null && jdbcUser != null && jdbcPass != null ) {
            ConnectionManager.addConnection( "iso_pg_test", jdbcURL, jdbcUser, jdbcPass, 5, 20 );
            Connection conn = null;
            try {
                conn = ConnectionManager.getConnection( "iso_pg_test" );
                setUpTables( conn );
            } finally {
                conn.close();
            }
        }
        // store = (ISOMetadataStore) MetadataStoreManager.create( configURL );
    }

    private void setUpTables( Connection conn )
                            throws SQLException, UnsupportedEncodingException, IOException, MetadataStoreException {

        Statement stmt = null;
        try {
            stmt = conn.createStatement();
            for ( String sql : new ISOMetadataStoreProvider().getDropStatements( configURL ) ) {
                stmt.executeUpdate( sql );
            }
            for ( String sql : new ISOMetadataStoreProvider().getCreateStatements( configURL ) ) {
                stmt.execute( sql );
            }
            store = (ISOMetadataStore) new ISOMetadataStoreProvider().getMetadataStore( configURL );
        } finally {
            if ( stmt != null ) {
                stmt.close();
            }
        }
    }

    @Test
    public void testBasic()
                            throws MetadataStoreException, XMLStreamException, FactoryConfigurationError, IOException {

        if ( store == null ) {
            LOG.warn( "Skipping test (needs configuration)." );
            return;
        }

        URL url = new URL( "file:/home/thomas/Dokumente/metadata_inspire/GEW_GEWAESSER_ACHSE.layer.xml" );
        OMElement record = new XMLAdapter( url ).getRootElement();
        // MetadataRecord record = loadRecord( url );

        MetadataStoreTransaction ta = store.acquireTransaction();
        List<OMElement> records = new ArrayList<OMElement>();
        records.add( record );
        InsertTransaction insert = new InsertTransaction( records, record.getQName(), "insert" );
        List<String> ids = ta.performInsert( insert );
        ta.commit();

        // store.getRecordsById( ids, outputSchema, elementSetName )

        // TODO test various queries

    }

    private MetadataRecord loadRecord( URL url )
                            throws XMLStreamException, FactoryConfigurationError, IOException {
        XMLStreamReader xmlStream = XMLInputFactory.newInstance().createXMLStreamReader( url.openStream() );
        return new ISORecord( xmlStream );
    }
}
