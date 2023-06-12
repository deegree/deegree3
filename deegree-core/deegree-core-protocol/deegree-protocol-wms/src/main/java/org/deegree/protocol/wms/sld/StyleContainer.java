/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2014 by:
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
package org.deegree.protocol.wms.sld;

import org.deegree.filter.OperatorFilter;
import org.deegree.layer.LayerRef;
import org.deegree.style.StyleRef;

/**
 * Encapsulates a style parsed from SLD.
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 */
public class StyleContainer {

	private final LayerRef layerRef;

	private final StyleRef styleRef;

	private final OperatorFilter filter;

	/**
	 * @param layerRef the parsed layer, never <code>null</code>
	 * @param styleRef the parsed style, never <code>null</code>
	 * @param filter the parsed filter, may be <code>null</code>
	 */
	public StyleContainer(LayerRef layerRef, StyleRef styleRef, OperatorFilter filter) {
		this.layerRef = layerRef;
		this.styleRef = styleRef;
		this.filter = filter;
	}

	/**
	 * @return the parsed layer, never <code>null</code>
	 */
	public LayerRef getLayerRef() {
		return layerRef;
	}

	/**
	 * @return the parsed style, never <code>null</code>
	 */
	public StyleRef getStyleRef() {
		return styleRef;
	}

	/**
	 * @return the parsed filter, may be <code>null</code>
	 */
	public OperatorFilter getFilter() {
		return filter;
	}

}