package org.deegree.db.datasource;

public class DataSourceMockFactory {

	public static final DataSourceMock create() {
		return new DataSourceMock();
	}

	public static final DataSourceMock create(String stringArg, int intArg) {
		return new DataSourceMock(stringArg, intArg);
	}

}
