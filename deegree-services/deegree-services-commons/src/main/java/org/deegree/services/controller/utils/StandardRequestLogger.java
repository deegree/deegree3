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
package org.deegree.services.controller.utils;

import static org.deegree.services.controller.FrontControllerStats.incomingKVP;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.deegree.services.controller.Credentials;
import org.deegree.services.controller.RequestLogger;
import org.slf4j.Logger;

/**
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 */
public class StandardRequestLogger implements RequestLogger {

	private static final Logger LOG = getLogger(StandardRequestLogger.class);

	public void logKVP(String address, String queryString, long startTime, long endTime, Credentials creds) {
		// store address as well?
		incomingKVP(queryString, startTime);
	}

	public void logXML(String address, File logFile, long startTime, long endTime, Credentials creds) {
		try {
			File tmp = File.createTempFile("request", ".xml", logFile.getParentFile());
			FileUtils.copyFile(logFile, tmp);
			LOG.debug("Logging request to {}", tmp);
		}
		catch (IOException e) {
			LOG.trace("Stack trace:", e);
			LOG.warn("Could not log to directory {}", logFile.getParentFile());
		}
	}

}
