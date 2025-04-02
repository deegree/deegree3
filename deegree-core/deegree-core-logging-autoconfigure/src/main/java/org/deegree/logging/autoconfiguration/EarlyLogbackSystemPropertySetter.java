/*----------------------------------------------------------------------------
 This file is part of deegree
 Copyright (C) 2001-2025 by:
 - Department of Geography, University of Bonn -
 and
 - grit GmbH -
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
package org.deegree.logging.autoconfiguration;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.Configurator;
import ch.qos.logback.classic.spi.ConfiguratorRank;
import ch.qos.logback.core.spi.ContextAwareBase;
import ch.qos.logback.core.util.OptionHelper;

import static ch.qos.logback.classic.ClassicConstants.CONFIG_FILE_PROPERTY;

@ConfiguratorRank(Integer.MAX_VALUE)
/**
 *
 */
public class EarlyLogbackSystemPropertySetter extends ContextAwareBase implements Configurator {

	public static final String CONFIG_PROPERTY = "logging.config";

	public static final String CONFIG_ENVIRONMENT = "LOGGING_CONFIG";

	@Override
	public ExecutionStatus configure(LoggerContext context) {
		String logbackSystemProperty = OptionHelper.getSystemProperty(CONFIG_FILE_PROPERTY);

		String loggingProperty = OptionHelper.getSystemProperty(CONFIG_PROPERTY);

		String loggingEnvironment = OptionHelper.getEnv(CONFIG_ENVIRONMENT);

		if (logbackSystemProperty != null && !logbackSystemProperty.isBlank()) {
			this.addInfo("Logback configuration " + CONFIG_PROPERTY + " set.");
		}
		else if (loggingProperty != null && !loggingProperty.isBlank()) {
			this.addInfo("Logback configuration " + CONFIG_PROPERTY + " will be set to value of property "
					+ CONFIG_PROPERTY + ".");
			OptionHelper.setSystemProperty(this, CONFIG_FILE_PROPERTY, loggingProperty);
		}
		else if (loggingEnvironment != null && !loggingEnvironment.isBlank()) {
			this.addInfo("Logback configuration " + CONFIG_PROPERTY + " will be set to value of environment of  "
					+ CONFIG_ENVIRONMENT + ".");
			OptionHelper.setSystemProperty(this, CONFIG_FILE_PROPERTY, loggingEnvironment);
		}

		return ExecutionStatus.INVOKE_NEXT_IF_ANY;
	}

}
