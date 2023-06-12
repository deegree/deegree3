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
package org.deegree.client.wpsprinter;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import org.deegree.commons.utils.ArrayUtils;
import org.slf4j.Logger;

/**
 * TODO add class documentation here
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 */
public class Configuration {

	private static final Logger LOG = getLogger(Configuration.class);

	private static Properties props;

	static {
		props = new Properties();
		InputStream is = Configuration.class.getResourceAsStream("/wpsprinter.properties");
		try {
			props.load(is);
		}
		catch (IOException e) {
			LOG.error("Could not load properties file!");
		}
	}

	public static String getWpsUrl() {
		return props.getProperty("wpsurl");
	}

	/**
	 * @return the list of template ids to be used in the client or null, if no template
	 * ids are configured
	 */
	public static List<String> getTemplates() {
		String templates = props.getProperty("templates");
		if (templates == null || templates.length() == 0)
			return null;
		return ArrayUtils.toList(templates, ",", true);
	}

	public static String getDatePattern() {
		return props.getProperty("datePattern");
	}

	public static String getDateTimePattern() {
		return props.getProperty("dateTimePattern");
	}

	public static String gettimePattern() {
		return props.getProperty("timePattern");
	}

}
