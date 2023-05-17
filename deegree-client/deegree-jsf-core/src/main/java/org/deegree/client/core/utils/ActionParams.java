/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2011 by:
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

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;
import java.io.Serializable;

/**
 * Utility class to provide a workaround for the missing parameters for action expressions
 * in JSF versions before 2.2.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
@ManagedBean(name = "actionParams")
@RequestScoped
public class ActionParams implements Serializable {

	private Object param1;

	private Object param2;

	private Object param3;

	private Object param4;

	public void setParam1(Object param1) {
		this.param1 = param1;
	}

	public static Object getParam1() {
		return getInstance().param1;
	}

	public void setParam2(Object param2) {
		this.param2 = param2;
	}

	public static Object getParam2() {
		return getInstance().param2;
	}

	public void setParam3(Object param3) {
		this.param3 = param3;
	}

	public static Object getParam3() {
		return getInstance().param3;
	}

	public void setParam4(Object param4) {
		this.param4 = param4;
	}

	public static Object getParam4() {
		return getInstance().param4;
	}

	private static ActionParams getInstance() {
		return (ActionParams) FacesContext.getCurrentInstance()
			.getExternalContext()
			.getRequestMap()
			.get("actionParams");
	}

}