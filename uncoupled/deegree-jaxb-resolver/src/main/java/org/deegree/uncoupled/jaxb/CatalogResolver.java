/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2022 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -
 and
 - grit graphische Informationstechnik Beratungsgesellschaft mbH -

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

 grit graphische Informationstechnik Beratungsgesellschaft mbH
 Landwehrstr. 143, 59368 Werne
 Germany
 http://www.grit.de/

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
package org.deegree.uncoupled.jaxb;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * JAXB Catalog Resolver
 * 
 * The Resolver is build to allow JAXB to find deegree bundled schemas inside
 * the build classpath instead of resolving schema files online.
 * 
 * <blockquote> <b>Note:</b> Eclipse only builds this class if the following
 * setting is changed, because of internal API:
 * 
 * <pre>
 * Window &gt; Preferences &gt; Java &gt; Compiler &gt; Errors/Warnings
 *  - Deprecated and restricted API
 *    Set "Forbidden reference (access rules)" to "Warning"
 * </pre>
 * 
 * </blockquote>
 * 
 * @author <a href="mailto:reichhelm@grit.de">Stephan Reichhelm</a>
 */
@SuppressWarnings("restriction")
public class CatalogResolver extends org.apache.xml.resolver.tools.CatalogResolver {

	private Map<String, String> cacheRelatives = new HashMap<String, String>();
	
	private static final String PUBLIC_FROM_HTTP = "http://www.deegree.org/";

	private static final String REWRITE_FROM_HTTP = "http://schemas.deegree.org/";

	private static final String REWRITE_FROM_HTTPS = "https://schemas.deegree.org/";

	private static final Pattern PAT_VERSION = Pattern.compile("^[0-9]+\\.[0-9]+");

	private static final String REWRITE_TO = "META-INF/schemas/";

	private boolean debug = false;

	public CatalogResolver() {
		String dbg = System.getProperty("deegree.jaxb.debug");
		if (dbg != null && !dbg.isEmpty()) {
			debug = true;
		}
	}

	@Override
	public String getResolvedEntity(String publicId, String systemId) {

		if (systemId == null) {
		} else if (systemId.toLowerCase().startsWith(REWRITE_FROM_HTTP)) {
			// TODO seems to be not required anymore
			debug("*** deegree JAXB CatalogResolver: publicId: " + publicId + " systemId: " + systemId);

			String path = systemId.substring(REWRITE_FROM_HTTP.length());
			String res = lookup(path);
			if (res != null) {
				return res;
			}
		} else if (systemId.toLowerCase().startsWith(REWRITE_FROM_HTTPS)) {
			// TODO seems to be not required anymore
			debug("*** deegree JAXB CatalogResolver: publicId: " + publicId + " systemId: " + systemId);
			
			String path = systemId.substring(REWRITE_FROM_HTTPS.length());
			String res = lookup(path);
			if (res != null) {
				return res;
			}
		} else if (systemId.startsWith("../") && publicId.toLowerCase().startsWith(PUBLIC_FROM_HTTP)) {
			debug("*** deegree JAXB CatalogResolver: publicId: " + publicId + " systemId: " + systemId);
			
			String path = systemId;
			while (path.startsWith("../")) {
				path = path.substring(3);
			}
			String res = lookup(path);
			if (res != null) {
				debug("--> " + res);
				cacheRelatives.put(publicId, res);
				return res;
			}
		} else {
			debug("*** deegree JAXB CatalogResolver: publicId: " + publicId + " systemId: " + systemId);
			if (publicId != null) {
				String res = cacheRelatives.get(publicId);
				if ( res != null ) {
					debug("==>: " + res + " [cache]");
					return res;
				}
			}
		}

		// Use default lookup
		return super.getResolvedEntity(publicId, systemId);
	}

	private void debug(String msg) {
		if (!debug) {
			return;
		}
		System.err.println(msg);
	}

	private String lookup(String path) {

		try {
			int posSlash = path.indexOf('/');
			if (posSlash > 0 && PAT_VERSION.matcher(path).find()) {
				// Ignore version for internal lookup
				path = path.substring(posSlash + 1);
			}
			String newid = REWRITE_TO + path;

			URL resource = Thread.currentThread().getContextClassLoader().getResource(newid);
			if (resource == null) {
				resource = getClass().getResource("/" + newid);
			}
			if (resource != null) {
				if (debug) {
					System.err.println("Resolved localy to: " + resource);
				}
				return resource.toString();
			}
		} catch (Exception ex) {
			System.err.println("Error: " + ex.getMessage());
		}
		return null;
	}
}