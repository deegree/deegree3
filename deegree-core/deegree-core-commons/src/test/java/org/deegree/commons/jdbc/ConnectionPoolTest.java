package org.deegree.commons.jdbc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.SQLException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ConnectionPoolTest {
	
	private ConnectionPool pool;

	@Before
	public void setUp() throws Exception {
		pool = new ConnectionPool("testConn1","jdbc:hsqldb:mem:testdb","sa","",false,1,10 );
	}

	@After
	public void tearDown() throws Exception {
		pool.destroy();
	}

	@Test
	public void testGetConnection() throws SQLException {
		Connection connection = pool.getConnection();
		assertNotNull(connection);
		assertTrue(connection.isValid(1));
		assertEquals("HSQL Database Engine", connection.getMetaData().getDatabaseProductName());
	}
	
	@Test (expected=SQLException.class)
	public void testGetConnectionExceedingMaxPoolSize() throws SQLException {
		for (int i = 0; i < 100; i++) {
			Connection connection = pool.getConnection();
			assertNotNull(connection);
			assertTrue(connection.isValid(1));
			assertTrue(i<10);
			// not returning connection to pool on purpose
		}
	}

}
