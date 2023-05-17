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

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.metadata.persistence.ebrim.eo;

import static org.deegree.metadata.persistence.ebrim.eo.Helper.getConnection;
import static org.deegree.metadata.persistence.ebrim.eo.Helper.setUpTables;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.deegree.commons.tom.primitive.BaseType;
import org.deegree.commons.tom.primitive.PrimitiveType;
import org.deegree.commons.tom.primitive.PrimitiveValue;
import org.deegree.commons.utils.JDBCUtils;
import org.deegree.commons.xml.CommonNamespaces;
import org.deegree.commons.xml.NamespaceBindings;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.filter.Filter;
import org.deegree.filter.OperatorFilter;
import org.deegree.filter.comparison.PropertyIsEqualTo;
import org.deegree.filter.expression.Literal;
import org.deegree.filter.expression.ValueReference;
import org.deegree.metadata.ebrim.RegistryObject;
import org.deegree.metadata.ebrim.RegistryPackage;
import org.deegree.metadata.persistence.MetadataInspectorException;
import org.deegree.metadata.persistence.transaction.DeleteOperation;
import org.deegree.metadata.persistence.transaction.InsertOperation;
import org.deegree.protocol.csw.MetadataStoreException;
import org.junit.Test;
import org.slf4j.Logger;

/**
 * TODO add class documentation here
 *
 * @author <a href="mailto:goltz@deegree.org">Lyn Goltz</a>
 */
public class EbrimEOTransactionTest {

	private static final Logger LOG = getLogger(EbrimEOTransactionTest.class);

	private static final NamespaceBindings nsContext = CommonNamespaces.getNamespaceContext();

	static {
		nsContext.addNamespace("rim", RegistryObject.RIM_NS);
	}

	@Test
	public void testInsert() throws SQLException, MetadataStoreException, MetadataInspectorException,
			UnsupportedEncodingException, IOException {
		Connection conn = getConnection();
		if (conn == null) {
			LOG.info("Could nor get database connection, skip testInsert");
			return;
		}
		setUpTables(conn);
		EbrimEOMDStoreTransaction t = new EbrimEOMDStoreTransaction(conn,
				JDBCUtils.useLegayPostGISPredicates(conn, LOG));
		InputStream is = EbrimEOTransactionTest.class.getResourceAsStream("io/ebrimRecord2.xml");
		RegistryPackage rec = new RegistryPackage(new XMLAdapter(is).getRootElement());
		InsertOperation io = new InsertOperation(Collections.singletonList(rec), null, null);
		t.performInsert(io);
		t.commit();

		// TODO
		// assertEquals( 1, countRecsInDB() );
	}

	@Test
	public void testInsertProfile() throws SQLException, MetadataStoreException, MetadataInspectorException,
			UnsupportedEncodingException, IOException {
		Connection conn = getConnection();
		if (conn == null) {
			LOG.info("Could nor get database connection, skip testInsert");
			return;
		}
		setUpTables(conn);
		EbrimEOMDStoreTransaction t = new EbrimEOMDStoreTransaction(conn,
				JDBCUtils.useLegayPostGISPredicates(conn, LOG));
		InputStream is = EbrimEOTransactionTest.class.getResourceAsStream("io/eo_profile_extension_package.xml");
		RegistryPackage rec = new RegistryPackage(new XMLAdapter(is).getRootElement());
		InsertOperation io = new InsertOperation(Collections.singletonList(rec), null, null);
		t.performInsert(io);
		t.commit();

		// TODO
		// assertEquals( 1, countRecsInDB() );
	}

	@Test
	public void testDelete() throws SQLException, MetadataStoreException, MetadataInspectorException,
			UnsupportedEncodingException, IOException {
		Connection conn = getConnection();
		if (conn == null) {
			LOG.info("Could nor get database connection, skip testDelete");
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

		// TODO
		// assertEquals( 1, countRecsInDB() );

		conn = getConnection();
		EbrimEOMDStoreTransaction t1 = new EbrimEOMDStoreTransaction(conn,
				JDBCUtils.useLegayPostGISPredicates(conn, LOG));

		ValueReference propertyName = new ValueReference("rim:RegistryPackage/@id", nsContext);
		Literal<PrimitiveValue> lit = new Literal<PrimitiveValue>(
				new PrimitiveValue("urn:ogc:def:EOP:RE00:IMG_MSI_3A:5230420:RP", new PrimitiveType(BaseType.STRING)),
				null);
		Filter constraint = new OperatorFilter(new PropertyIsEqualTo(propertyName, lit, true, null));
		DeleteOperation deleteOp = new DeleteOperation(null, null, constraint);
		t1.performDelete(deleteOp);
		t1.commit();

		// TODO
		// assertEquals( 1, countRecsInDB() );
	}

}