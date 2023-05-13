/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2013 by:
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
package org.deegree.console;

import static javax.faces.application.FacesMessage.SEVERITY_ERROR;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

import org.deegree.services.controller.OGCFrontController;
import org.deegree.workspace.Workspace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Static utility methods for JSF stuff.
 *
 * @author <a href="mailto:schneider@occamlabs.de">Markus Schneider</a>
 * @since 3.4
 */
public class JsfUtils {

	private static Logger LOG = LoggerFactory.getLogger(JsfUtils.class);

	/**
	 * Returns the active {@link Workspace}.
	 * @return active workspace, never <code>null</code>
	 */
	public static Workspace getWorkspace() {
		return OGCFrontController.getServiceWorkspace().getNewWorkspace();
	}

	/**
	 * Provides information on a failed action to the user.
	 * @param failedAction description of the action that failed, e.g. "Workspace
	 * startup", must not be <code>null</code>
	 * @param cause cause, must not be <code>null</code>
	 */
	public static void indicateException(String failedAction, Throwable cause) {
		String msg = failedAction + " failed: " + cause.getMessage() + "(" + cause.getClass() + ")";
		FacesMessage fm = new FacesMessage(SEVERITY_ERROR, msg, null);
		FacesContext.getCurrentInstance().addMessage(null, fm);
		LOG.error(msg, cause);
	}

	/**
	 * Provides information on a failed action to the user.
	 * @param failedAction description of the action that failed, e.g. "Workspace
	 * startup", must not be <code>null</code>
	 * @param cause cause, must not be <code>null</code>
	 */
	public static void indicateException(String failedAction, String cause) {
		String msg = failedAction + " failed: " + cause;
		FacesMessage fm = new FacesMessage(SEVERITY_ERROR, msg, null);
		FacesContext.getCurrentInstance().addMessage(null, fm);
		LOG.error(cause);
	}

}
