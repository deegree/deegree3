package org.deegree.db.datasource;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.InvocationTargetException;

import javax.sql.DataSource;

import org.deegree.db.datasource.jaxb.DataSourceConnectionProvider;
import org.deegree.db.datasource.jaxb.DataSourceConnectionProvider.Property;
import org.junit.Assert;
import org.junit.Test;

public class DataSourceInitializerTest {

	private DataSourceInitializer initializer = new DataSourceInitializer(null);

	@Test
	public void testGetDataSourceInstanceConstructor() throws ClassNotFoundException, NoSuchMethodException,
			IllegalAccessException, InvocationTargetException, InstantiationException {
		DataSourceConnectionProvider.DataSource config = new DataSourceConnectionProvider.DataSource();
		config.setJavaClass(DataSourceMock.class.getCanonicalName());
		DataSource ds = initializer.getDataSourceInstance(config);
		assertNotNull(ds);
		assertTrue(ds instanceof DataSourceMock);
	}

	@Test
	public void testGetDataSourceInstanceConstructorWithArguments() throws ClassNotFoundException,
			NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
		DataSourceConnectionProvider.DataSource config = new DataSourceConnectionProvider.DataSource();
		config.setJavaClass(DataSourceMock.class.getCanonicalName());
		DataSourceConnectionProvider.DataSource.Argument stringArgument = new DataSourceConnectionProvider.DataSource.Argument();
		stringArgument.setJavaClass("java.lang.String");
		stringArgument.setValue("aeiou");
		config.getArgument().add(stringArgument);
		DataSourceConnectionProvider.DataSource.Argument intArgument = new DataSourceConnectionProvider.DataSource.Argument();
		intArgument.setJavaClass("java.lang.Integer");
		intArgument.setValue("4711");
		config.getArgument().add(intArgument);
		DataSourceMock ds = (DataSourceMock) initializer.getDataSourceInstance(config);
		assertNotNull(ds);
		assertTrue(ds instanceof DataSourceMock);
		Assert.assertEquals("aeiou", ds.getStringConstructorArg());
		Assert.assertEquals(4711, ds.getIntConstructorArg());
	}

	@Test
	public void testGetDataSourceInstanceStaticFactory() throws ClassNotFoundException, NoSuchMethodException,
			IllegalAccessException, InvocationTargetException, InstantiationException {
		DataSourceConnectionProvider.DataSource config = new DataSourceConnectionProvider.DataSource();
		config.setJavaClass(DataSourceMockFactory.class.getCanonicalName());
		config.setFactoryMethod("create");
		DataSource ds = initializer.getDataSourceInstance(config);
		assertNotNull(ds);
		assertTrue(ds instanceof DataSourceMock);
	}

	@Test
	public void testGetDataSourceInstanceStaticFactoryWithArguments() throws ClassNotFoundException,
			NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
		DataSourceConnectionProvider.DataSource config = new DataSourceConnectionProvider.DataSource();
		config.setJavaClass(DataSourceMockFactory.class.getCanonicalName());
		config.setFactoryMethod("create");
		DataSourceConnectionProvider.DataSource.Argument stringArgument = new DataSourceConnectionProvider.DataSource.Argument();
		stringArgument.setJavaClass("java.lang.String");
		stringArgument.setValue("aeiou");
		config.getArgument().add(stringArgument);
		DataSourceConnectionProvider.DataSource.Argument intArgument = new DataSourceConnectionProvider.DataSource.Argument();
		intArgument.setJavaClass("java.lang.Integer");
		intArgument.setValue("4711");
		config.getArgument().add(intArgument);
		DataSourceMock ds = (DataSourceMock) initializer.getDataSourceInstance(config);
		assertNotNull(ds);
		assertTrue(ds instanceof DataSourceMock);
		Assert.assertEquals("aeiou", ds.getStringConstructorArg());
		Assert.assertEquals(4711, ds.getIntConstructorArg());
	}

	@Test
	public void testSetPropertyString() {
		final DataSourceMock ds = new DataSourceMock();
		final Property property = new Property();
		property.setName("stringProperty");
		property.setValue("value");
		initializer.setProperty(ds, property);
		Assert.assertEquals("value", ds.getStringProperty());
	}

	@Test
	public void testSetPropertyInt() {
		final DataSourceMock ds = new DataSourceMock();
		final Property property = new Property();
		property.setName("intProperty");
		property.setValue("4711");
		initializer.setProperty(ds, property);
		Assert.assertEquals(4711, ds.getIntProperty());
	}

}
