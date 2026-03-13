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

import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.annotation.ManagedProperty;
import jakarta.inject.Named;

import java.io.Serializable;

/**
 * Utility class to provide access to multiple parameters for action expressions.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author <a href="mailto:friebe@lat-lon.de">Torsten Friebe</a>
 */
@Named("actionParams")
@RequestScoped
public class ActionParams implements Serializable {

	@ManagedProperty("#{param.param1}")
	private Object param1;

	@ManagedProperty("#{param.param2}")
	private Object param2;

	@ManagedProperty("#{param.param3}")
	private Object param3;

	@ManagedProperty("#{param.param4}")
	private Object param4;

	public void setParam1(Object param1) {
		this.param1 = param1;
	}

	public Object getParam1() {
		return param1;
	}

	public void setParam2(Object param2) {
		this.param2 = param2;
	}

	public Object getParam2() {
		return param2;
	}

	public void setParam3(Object param3) {
		this.param3 = param3;
	}

	public Object getParam3() {
		return param3;
	}

	public void setParam4(Object param4) {
		this.param4 = param4;
	}

	public Object getParam4() {
		return param4;
	}

	@Override
	public String toString() {
		return "ActionParams{" + "param1=" + param1 + ", param2=" + param2 + ", param3=" + param3 + ", param4=" + param4
				+ '}';
	}

}