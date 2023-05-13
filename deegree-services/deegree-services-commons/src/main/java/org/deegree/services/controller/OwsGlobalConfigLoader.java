/*----------------------------------------------------------------------------
 This file is part of deegree
 Copyright (C) 2001-2013 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -
 and
 - Occam Labs UG (haftungsbeschränkt) -
 and others

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

 e-mail: info@deegree.org
 website: http://www.deegree.org/
----------------------------------------------------------------------------*/
package org.deegree.services.controller;

import static java.lang.Class.forName;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;

import javax.xml.bind.JAXBElement;

import org.deegree.commons.utils.net.DURL;
import org.deegree.commons.xml.jaxb.JAXBUtils;
import org.deegree.services.controller.utils.StandardRequestLogger;
import org.deegree.services.jaxb.controller.DeegreeServiceControllerType;
import org.deegree.services.jaxb.controller.DeegreeServiceControllerType.RequestLogging;
import org.deegree.services.jaxb.metadata.DeegreeServicesMetadataType;
import org.deegree.workspace.Initializable;
import org.deegree.workspace.Workspace;
import org.deegree.workspace.standard.DefaultWorkspace;
import org.slf4j.Logger;

/**
 * This class is responsible for reading any existing main.xml and metadata.xml upon
 * startup.
 *
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @since 3.4
 */
public class OwsGlobalConfigLoader implements Initializable {

	private static final Logger LOG = getLogger(OwsGlobalConfigLoader.class);

	private static final String CONTROLLER_JAXB_PACKAGE = "org.deegree.services.jaxb.controller";

	private static final URL CONTROLLER_CONFIG_SCHEMA = OwsGlobalConfigLoader.class
		.getResource("/META-INF/schemas/services/controller/controller.xsd");

	private static final String METADATA_JAXB_PACKAGE = "org.deegree.services.jaxb.metadata";

	private static final URL METADATA_CONFIG_SCHEMA = OwsGlobalConfigLoader.class
		.getResource("/META-INF/schemas/services/metadata/metadata.xsd");

	private static final URL METADATA_CONFIG_EXAMPLE = OwsGlobalConfigLoader.class
		.getResource("/META-INF/schemas/services/metadata/example.xml");

	private DeegreeServicesMetadataType metadataConfig;

	private DeegreeServiceControllerType mainConfig;

	private RequestLogger requestLogger;

	private boolean logOnlySuccessful;

	@Override
	public void init(Workspace workspace) {
		File wsDir = ((DefaultWorkspace) workspace).getLocation();
		File metadata = new File(wsDir, "services" + File.separator + "metadata.xml");
		File main = new File(wsDir, "services" + File.separator + "main.xml");

		try {
			URL mdurl;
			if (!metadata.exists()) {
				mdurl = METADATA_CONFIG_EXAMPLE;
				String msg = "No 'services/metadata.xml' file, assuming defaults.";
				LOG.debug(msg);
			}
			else {
				mdurl = metadata.toURI().toURL();
			}
			setMetadataConfig(
					(DeegreeServicesMetadataType) ((JAXBElement<?>) JAXBUtils.unmarshall(METADATA_JAXB_PACKAGE,
							METADATA_CONFIG_SCHEMA, new DURL(mdurl.toExternalForm()).openStream(), workspace))
						.getValue());
		}
		catch (Exception e) {
			String msg = "Could not unmarshall frontcontroller configuration: " + e.getMessage();
			LOG.error(msg);
		}
		if (!main.exists()) {
			LOG.debug("No 'services/main.xml' file, assuming defaults.");
			setMainConfig(new DeegreeServiceControllerType());
		}
		else {
			try {
				setMainConfig(
						(DeegreeServiceControllerType) ((JAXBElement<?>) JAXBUtils.unmarshall(CONTROLLER_JAXB_PACKAGE,
								CONTROLLER_CONFIG_SCHEMA, new DURL(main.toURI().toURL().toExternalForm()).openStream(),
								workspace))
							.getValue());
			}
			catch (Exception e) {
				setMainConfig(new DeegreeServiceControllerType());
				LOG.info("main.xml could not be loaded. Proceeding with defaults.");
				LOG.debug("Error was: '{}'.", e.getLocalizedMessage());
				LOG.trace("Stack trace:", e);
			}
		}

		initRequestLogger();
	}

	/**
	 * @return the metadataConfig, may be <code>null</code> if not configured
	 */
	public DeegreeServicesMetadataType getMetadataConfig() {
		return metadataConfig;
	}

	/**
	 * @param metadataConfig the metadataConfig to set, may be <code>null</code>
	 */
	public void setMetadataConfig(DeegreeServicesMetadataType metadataConfig) {
		this.metadataConfig = metadataConfig;
	}

	/**
	 * @return the mainConfig, may be <code>null</code> if not configured
	 */
	public DeegreeServiceControllerType getMainConfig() {
		return mainConfig;
	}

	/**
	 * @param mainConfig the mainConfig to set, may be <code>null</code>
	 */
	public void setMainConfig(DeegreeServiceControllerType mainConfig) {
		this.mainConfig = mainConfig;
	}

	private void initRequestLogger() {
		RequestLogging requestLogging = mainConfig.getRequestLogging();
		if (requestLogging != null) {
			org.deegree.services.jaxb.controller.DeegreeServiceControllerType.RequestLogging.RequestLogger logger = requestLogging
				.getRequestLogger();
			setRequestLogger(instantiateRequestLogger(logger));
			this.setLogOnlySuccessful(requestLogging.isOnlySuccessful() != null && requestLogging.isOnlySuccessful());
		}
	}

	private static RequestLogger instantiateRequestLogger(RequestLogging.RequestLogger conf) {
		if (conf != null) {
			String cls = conf.getClazz();
			try {
				Object o = conf.getConfiguration();
				if (o == null) {
					return (RequestLogger) forName(cls).newInstance();
				}
				return (RequestLogger) forName(cls).getDeclaredConstructor(Object.class).newInstance(o);
			}
			catch (ClassNotFoundException e) {
				LOG.info("The request logger class '{}' could not be found on the classpath.", cls);
				LOG.trace("Stack trace:", e);
			}
			catch (ClassCastException e) {
				LOG.info("The request logger class '{}' does not implement the RequestLogger interface.", cls);
				LOG.trace("Stack trace:", e);
			}
			catch (InstantiationException e) {
				LOG.info(
						"The request logger class '{}' could not be instantiated"
								+ " (needs a default constructor without arguments if no configuration is given).",
						cls);
				LOG.trace("Stack trace:", e);
			}
			catch (IllegalAccessException e) {
				LOG.info("The request logger class '{}' could not be instantiated"
						+ " (default constructor needs to be accessible if no configuration is given).", cls);
				LOG.trace("Stack trace:", e);
			}
			catch (IllegalArgumentException e) {
				LOG.info("The request logger class '{}' could not be instantiated"
						+ " (constructor needs to take an object argument if configuration is given).", cls);
				LOG.trace("Stack trace:", e);
			}
			catch (java.lang.SecurityException e) {
				LOG.info("The request logger class '{}' could not be instantiated"
						+ " (JVM does have insufficient rights to instantiate the class).", cls);
				LOG.trace("Stack trace:", e);
			}
			catch (InvocationTargetException e) {
				LOG.info("The request logger class '{}' could not be instantiated"
						+ " (constructor call threw an exception).", cls);
				LOG.trace("Stack trace:", e);
			}
			catch (NoSuchMethodException e) {
				LOG.info("The request logger class '{}' could not be instantiated"
						+ " (constructor needs to take an object argument if configuration is given).", cls);
				LOG.trace("Stack trace:", e);
			}
		}
		return new StandardRequestLogger();
	}

	/**
	 * @return the requestLogger
	 */
	public RequestLogger getRequestLogger() {
		return requestLogger;
	}

	/**
	 * @param requestLogger the requestLogger to set
	 */
	public void setRequestLogger(RequestLogger requestLogger) {
		this.requestLogger = requestLogger;
	}

	/**
	 * @return the logOnlySuccessful
	 */
	public boolean isLogOnlySuccessful() {
		return logOnlySuccessful;
	}

	/**
	 * @param logOnlySuccessful the logOnlySuccessful to set
	 */
	public void setLogOnlySuccessful(boolean logOnlySuccessful) {
		this.logOnlySuccessful = logOnlySuccessful;
	}

}
