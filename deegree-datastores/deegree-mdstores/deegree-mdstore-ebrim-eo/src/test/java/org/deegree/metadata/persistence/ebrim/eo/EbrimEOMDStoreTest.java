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
package org.deegree.metadata.persistence.ebrim.eo;

import static org.deegree.metadata.persistence.ebrim.eo.Helper.getConnection;
import static org.deegree.metadata.persistence.ebrim.eo.Helper.setUpTables;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.deegree.commons.tom.primitive.PrimitiveValue;
import org.deegree.commons.utils.JDBCUtils;
import org.deegree.commons.xml.CommonNamespaces;
import org.deegree.commons.xml.NamespaceBindings;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.filter.Expression;
import org.deegree.filter.Filter;
import org.deegree.filter.Operator;
import org.deegree.filter.OperatorFilter;
import org.deegree.filter.comparison.PropertyIsEqualTo;
import org.deegree.filter.expression.Literal;
import org.deegree.filter.expression.ValueReference;
import org.deegree.metadata.ebrim.RegistryObject;
import org.deegree.metadata.ebrim.RegistryPackage;
import org.deegree.metadata.persistence.MetadataInspectorException;
import org.deegree.metadata.persistence.MetadataQuery;
import org.deegree.metadata.persistence.MetadataResultSet;
import org.deegree.metadata.persistence.MetadataStoreProvider;
import org.deegree.metadata.persistence.transaction.InsertOperation;
import org.deegree.protocol.csw.MetadataStoreException;
import org.deegree.workspace.Workspace;
import org.deegree.workspace.standard.DefaultWorkspace;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;

/**
 * TODO add class documentation here
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
public class EbrimEOMDStoreTest {

	private static final Logger LOG = getLogger(EbrimEOMDStoreTest.class);

	private static final String MDSTORE_ID = "ebrimEO";

	private EbrimEOMDStore store = null;

	private Workspace ws;

	private static final NamespaceBindings ns = CommonNamespaces.getNamespaceContext();

	private static boolean insertSuccess = false;

	private static final String id_rec1 = "urn:ogc:def:EOP:RE00:IMG_MSI_3A:5230420:RP";

	static {
		ns.addNamespace("rim", "urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0");
		ns.addNamespace("wrs", "http://www.opengis.net/cat/wrs/1.0");
	}

	@Before
	public void setUp() throws Exception {
		File wsDir = new File(EbrimEOMDStore.class.getResource("eotest").toURI());
		ws = new DefaultWorkspace(wsDir);
		ws.initAll();
		store = (EbrimEOMDStore) ws.getResource(MetadataStoreProvider.class, MDSTORE_ID);
	}

	@After
	public void tearDown() throws Exception {
		ws.destroy();
	}

	@Test
	public void testGetRecordById() throws SQLException, MetadataStoreException, UnsupportedEncodingException,
			IOException, MetadataInspectorException {
		if (insertSuccess) {
			LOG.info("Could not insert test datasets, skip testGetRecordById");
			return;
		}

		Connection conn = getConnection();
		if (conn == null) {
			LOG.info("Could not get database connection, skip testGetRecordById");
			return;
		}
		MetadataResultSet<RegistryObject> recordById = store.getRecordById(Collections.singletonList(id_rec1), null);

		assertNotNull(recordById);

		recordById.next();

		RegistryObject record = recordById.getRecord();
		Assert.assertNotNull(record);

		assertEquals(id_rec1, record.getIdentifier());
	}

	// @Test
	// public void testGetRecordCountAll()
	// throws SQLException, MetadataStoreException, UnsupportedEncodingException,
	// IOException,
	// MetadataInspectorException {
	// if ( insertSuccess ) {
	// LOG.info( "Could not insert test datasets, skip testGetRecordCountAll" );
	// return;
	// }
	// Connection conn = getConnection();
	// if ( conn == null ) {
	// LOG.info( "Could not get database connection, skip testGetRecordCountAll" );
	// return;
	// }
	// QueryHandler qh = new QueryHandler( conn, Type.PostgreSQL, null );
	// int countRecords = qh.countRecords( null );
	// assertEquals( 1, countRecords );
	// }

	@Test
	public void testGetRecordsAll() throws SQLException, MetadataStoreException, UnsupportedEncodingException,
			IOException, MetadataInspectorException {
		if (insertSuccess) {
			LOG.info("Could not insert test datasets, skip testGetRecordsAll");
			return;
		}
		Connection conn = getConnection();
		if (conn == null) {
			LOG.info("Could not get database connection, skip testGetRecordsAll");
			return;
		}
		MetadataQuery query = new MetadataQuery(null, null, null, null, 1, 20);
		MetadataResultSet<RegistryObject> records = store.getRecords(query);

		Assert.assertNotNull(records);

		int count = 0;
		while (records.next()) {
			count++;
		}
		assertEquals(2, count);
	}

	@Test
	public void testGetRecordsFilter() throws SQLException, MetadataStoreException, UnsupportedEncodingException,
			IOException, MetadataInspectorException {
		if (insertSuccess) {
			LOG.info("Could not insert test datasets, skip testGetRecordsFilter");
			return;
		}
		Connection conn = getConnection();
		if (conn == null) {
			LOG.info("Could not get database connection, skip testGetRecordsFilter");
			return;
		}

		Expression propName = new ValueReference("/rim:RegistryPackage/@id", ns);
		Expression lit = new Literal<PrimitiveValue>(id_rec1);
		Operator rootOperator = new PropertyIsEqualTo(propName, lit, true, null);
		Filter filter = new OperatorFilter(rootOperator);
		MetadataQuery query = new MetadataQuery(null, null, filter, null, 1, 20);
		MetadataResultSet<RegistryObject> recordById = store.getRecords(query);

		Assert.assertNotNull(recordById);

		recordById.next();

		RegistryObject record = recordById.getRecord();
		Assert.assertNotNull(record);

		Assert.assertEquals(id_rec1, record.getIdentifier());
	}

	@BeforeClass
	public static void insertTestDatasets() throws UnsupportedEncodingException, SQLException, IOException,
			MetadataStoreException, MetadataInspectorException {

		Connection conn = getConnection();
		if (conn == null) {
			LOG.info("Could nor get database connection, skip testInsert");
			return;
		}
		setUpTables(conn);
		EbrimEOMDStoreTransaction t = new EbrimEOMDStoreTransaction(conn,
				JDBCUtils.useLegayPostGISPredicates(conn, LOG));
		InputStream is1 = EbrimEOTransactionTest.class.getResourceAsStream("io/ebrimRecord1.xml");
		InputStream is2 = EbrimEOTransactionTest.class.getResourceAsStream("io/ebrimRecord2.xml");
		List<RegistryPackage> recs = new ArrayList<RegistryPackage>();
		recs.add(new RegistryPackage(new XMLAdapter(is1).getRootElement()));
		recs.add(new RegistryPackage(new XMLAdapter(is2).getRootElement()));
		InsertOperation io = new InsertOperation(recs, null, null);
		t.performInsert(io);
		t.commit();
		insertSuccess = true;
	}

}
