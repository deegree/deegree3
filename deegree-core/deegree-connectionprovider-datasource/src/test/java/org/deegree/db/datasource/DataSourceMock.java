package org.deegree.db.datasource;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

import javax.sql.DataSource;

public class DataSourceMock implements DataSource {

	private String stringProperty;

	private int intProperty;

	private String stringConstructorArg;

	private int intConstructorArg;

	public DataSourceMock() {

	}

	public DataSourceMock(String stringConstructorArg, int intConstructorArg) {
		this.stringConstructorArg = stringConstructorArg;
		this.intConstructorArg = intConstructorArg;
	}

	public String getStringConstructorArg() {
		return stringConstructorArg;
	}

	public int getIntConstructorArg() {
		return intConstructorArg;
	}

	public String getStringProperty() {
		return stringProperty;
	}

	public void setStringProperty(String stringProperty) {
		this.stringProperty = stringProperty;
	}

	public int getIntProperty() {
		return intProperty;
	}

	public void setIntProperty(int intProperty) {
		this.intProperty = intProperty;
	}

	@Override
	public PrintWriter getLogWriter() throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getLoginTimeout() throws SQLException {
		throw new UnsupportedOperationException();
	}

	// Overrides a method of the CommonDataSource interface in Java 1.7.
	public Logger getParentLogger() throws SQLFeatureNotSupportedException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setLogWriter(PrintWriter out) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setLoginTimeout(int seconds) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Connection getConnection() throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Connection getConnection(String username, String password) throws SQLException {
		throw new UnsupportedOperationException();
	}

}
