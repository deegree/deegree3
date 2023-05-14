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
package org.deegree.layer;

import org.deegree.commons.utils.CollectionUtils.Mapper;

/**
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 */
public class LayerRef {

	private String name;

	private Layer layer;

	public LayerRef(String name) {
		this.name = name;
	}

	public LayerRef(Layer layer) {
		this.layer = layer;
		this.name = layer.getMetadata().getName();
	}

	public boolean isResolved() {
		return layer != null;
	}

	public Layer getLayer() {
		return layer;
	}

	public String getName() {
		return name;
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof LayerRef)) {
			return false;
		}
		return name.equals(((LayerRef) other).getName());
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	@Override
	public String toString() {
		return name;
	}

	public static final Mapper<LayerRef, String> FROM_NAMES = new Mapper<LayerRef, String>() {
		@Override
		public LayerRef apply(String u) {
			return new LayerRef(u);
		}
	};

}
