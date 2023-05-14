/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
   Department of Geography, University of Bonn
 and
   lat/lon GmbH

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

package org.deegree.tools.rendering.viewer;

import java.util.Map;

import org.deegree.commons.utils.memory.MemoryAware;

/**
 * The <code>J3DExporter</code> Inteface allows for easy access to some common the export
 * methods.
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 *
 */

public interface J3DExporter {

	/**
	 * A method which can be called to export to the given format.
	 * @param result a reference to which the implementing class should write.
	 * @param j3dScene the scene to be exported.
	 */
	public void export(StringBuilder result, MemoryAware j3dScene);

	/**
	 * @return A Name which can be presented to a calling client;
	 */
	public String getName();

	/**
	 * @return a simple description which describes the function of the implementing
	 * class.
	 */
	public String getShortDescription();

	/**
	 * Should return a list of parameter names (keys) with their description (values) of
	 * all parameters this Exporter supports. It is up to the requester to use the given
	 * parameters and call the required constructor with the map<String, String>,
	 * containing the keys that have values.
	 * @return a map containing the parameter names (keys) and their description (values)
	 */
	public Map<String, String> getParameterMap();

}
