//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/deegree3/trunk/deegree-core/src/test/java/org/deegree/protocol/wps/client/WPSClientTest.java $
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
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
package org.deegree;

import java.io.File;
import java.io.FileReader;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides environment parameters for tests that rely on external resources.  
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: aionita $
 * 
 * @version $Revision: 26187 $, $Date: 2010-08-26 15:29:17 +0200 (Do, 26 Aug 2010) $
 */
public class CoreTstProperties {

    private static Logger LOG = LoggerFactory.getLogger( CoreTstProperties.class );
	
	private static final Properties props = new Properties ();
	
	static {
		File file = new File (System.getProperty("user.home"), ".deegree-coretest.properties");
		if (file.exists()) {
			LOG.info("Using test properties from file {}.", file);
			try {
				props.load(new FileReader(file));
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			LOG.info("File {} does not exist. Some tests will be skipped.", file);
		}
	}
	
	/**
	 * Returns the property with the given name.
	 * 
	 * @param key
	 *      name of the property
	 * @return the property with the given name, or <code>null</code> if it is not available
	 */
	public static String getProperty (String key) {
		return props.getProperty(key);
	}
}