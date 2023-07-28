/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2014 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -
 and
 - Occam Labs UG (haftungsbeschränkt) -

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

 Occam Labs UG (haftungsbeschränkt)
 Godesberger Allee 139, 53175 Bonn
 Germany

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.db.datasource;

import static org.slf4j.LoggerFactory.getLogger;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.ConstructorUtils;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.MethodUtils;
import org.deegree.db.datasource.jaxb.DataSourceConnectionProvider;
import org.deegree.db.datasource.jaxb.DataSourceConnectionProvider.DataSource.Argument;
import org.slf4j.Logger;

/**
 * Creates/retrieves {@link DataSource} instances and applies configuration properties via
 * setters.
 *
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 * @since 3.4
 */
class DataSourceInitializer {

	private static final Logger LOG = getLogger(DataSourceInitializer.class);

	private final ClassLoader classLoader;

	/**
	 * Creates a new {@link DataSourceInitializer} instance.
	 * @param classLoader class loader to be used, can be <code>null</code> (use default
	 * class loader)
	 */
	DataSourceInitializer(final ClassLoader classLoader) {
		this.classLoader = classLoader;
	}

	/**
	 * Creates/retrieves a {@link DataSource} instance and applies configuration
	 * properties via setters.
	 * @param config JAXB configuration, must not be <code>null</code>
	 * @return configured DataSource instance, can be <code>null</code> (initialization
	 * failed)
	 * @throws InstantiationException
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 * @throws NoSuchMethodException
	 * @throws ClassNotFoundException
	 */
	DataSource getConfiguredDataSource(DataSourceConnectionProvider config) throws ClassNotFoundException,
			NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
		DataSource ds = getDataSourceInstance(config.getDataSource());
		for (DataSourceConnectionProvider.Property property : config.getProperty()) {
			setProperty(ds, property);
		}
		return ds;
	}

	@SuppressWarnings("unchecked")
	DataSource getDataSourceInstance(DataSourceConnectionProvider.DataSource config) throws ClassNotFoundException,
			NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
		DataSource ds = null;
		final Class<?> klass = getClass(config.getJavaClass());
		if (config.getFactoryMethod() != null) {
			ds = invokeStaticFactoryMethod(klass, config.getFactoryMethod(), config.getArgument());
		}
		else {
			ds = invokeDataSourceConstructor((Class<DataSource>) klass, config.getArgument());
		}
		return ds;
	}

	private DataSource invokeDataSourceConstructor(Class<DataSource> dsClass,
			List<DataSourceConnectionProvider.DataSource.Argument> arguments) throws NoSuchMethodException,
			IllegalAccessException, InvocationTargetException, InstantiationException, ClassNotFoundException {
		final Object[] javaArgs = getJavaArgs(arguments);
		return (DataSource) ConstructorUtils.invokeConstructor(dsClass, javaArgs);
	}

	private DataSource invokeStaticFactoryMethod(Class<?> factoryClass, String methodName,
			List<DataSourceConnectionProvider.DataSource.Argument> arguments)
			throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, ClassNotFoundException {
		final Object[] javaArgs = getJavaArgs(arguments);
		return (DataSource) MethodUtils.invokeStaticMethod(factoryClass, methodName, javaArgs);
	}

	private Object[] getJavaArgs(List<Argument> arguments) throws ClassNotFoundException {
		final Object[] args = new Object[arguments.size()];
		for (int i = 0; i < args.length; i++) {
			if (arguments.get(i) != null) {
				args[i] = getJavaArg(arguments.get(i));
			}
		}
		return args;
	}

	Object getJavaArg(Argument argument) throws ClassNotFoundException {
		Class<?> parameterClass = null;
		try {
			parameterClass = getClass(argument.getJavaClass());
			final Class<?> primitiveClass = MethodUtils.getPrimitiveType(parameterClass);
			if (primitiveClass != null) {
				parameterClass = primitiveClass;
			}
		}
		catch (ClassNotFoundException e) {
			throw e;
		}
		return ConvertUtils.convert(argument.getValue(), parameterClass);
	}

	void setProperty(final DataSource ds, final DataSourceConnectionProvider.Property property) {
		final String name = property.getName();
		final String value = property.getValue();
		try {
			BeanUtils.setProperty(ds, name, value);
		}
		catch (Exception e) {
			String msg = "Error setting DataSource property '" + name + "': " + e.getLocalizedMessage();
			LOG.warn(msg);
		}
	}

	private Class<?> getClass(String javaClass) throws ClassNotFoundException {
		if (classLoader == null) {
			return Class.forName(javaClass);
		}
		return classLoader.loadClass(javaClass);
	}

}
