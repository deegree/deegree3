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
package org.deegree.client.core.utils;

import static org.slf4j.LoggerFactory.getLogger;

import java.net.MalformedURLException;
import java.net.URL;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import org.slf4j.Logger;

/**
 * TODO add class documentation here
 *
 * @author <a href="mailto:buesching@lat-lon.de">Lyn Buesching</a>
 */
public final class FacesUtils {

	private static final Logger LOG = getLogger(FacesUtils.class);

	/**
	 * @param target
	 * @param fileName
	 * @return
	 */
	public static String getAbsolutePath(String target, String fileName) {
		if (target == null) {
			target = "";
		}
		if (!target.endsWith("/")) {
			target += "/";
		}
		return FacesContext.getCurrentInstance().getExternalContext().getRealPath(target + fileName);
	}

	/**
	 * @param target the name of an existing directory in the webapps directory, if null
	 * the webapps directory will is the target
	 * @param fileName the name of the file
	 * @return the current URL extended with the target and fileName, or null if an error
	 * occurred. Example: http://localhost:8080/context/target/fileName
	 */
	public static URL getWebAccessibleUrl(String target, String fileName) throws MalformedURLException {
		try {
			if (target == null) {
				target = "";
			}
			else if (!target.endsWith("/")) {
				target += "/";
			}
			return new URL(getServerURL() + target + fileName);
		}
		catch (MalformedURLException e) {
			LOG.debug("Constructing the url was a problem...");
			LOG.trace("Stack trace:", e);
		}
		return null;
	}

	/**
	 * @return the current URL, or null if an error occurred. Example:
	 * http://localhost:8080/context/
	 */
	public static String getServerURL() {
		ExternalContext ctx = FacesContext.getCurrentInstance().getExternalContext();
		URL url;
		try {
			url = new URL(ctx.getRequestScheme(), ctx.getRequestServerName(), ctx.getRequestServerPort(),
					ctx.getRequestContextPath());
			return url.toExternalForm() + "/";
		}
		catch (MalformedURLException e) {
			LOG.debug("Constructing the url was a problem...");
			LOG.trace("Stack trace:", e);
		}
		return null;
	}

}
