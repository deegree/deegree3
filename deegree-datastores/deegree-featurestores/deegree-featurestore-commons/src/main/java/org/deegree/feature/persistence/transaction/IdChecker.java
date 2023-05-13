/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2014 by:
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
package org.deegree.feature.persistence.transaction;

import java.util.HashSet;
import java.util.Set;

import org.deegree.commons.tom.Reference;
import org.deegree.commons.tom.gml.GMLObject;
import org.deegree.feature.Feature;
import org.deegree.geometry.Geometry;
import org.deegree.gml.utils.GMLObjectVisitor;

class IdChecker implements GMLObjectVisitor {

	final Set<String> ids = new HashSet<String>();

	@Override
	public boolean visitGeometry(final Geometry geom) {
		checkForDuplication(geom.getId());
		return true;
	}

	@Override
	public boolean visitFeature(final Feature feature) {
		checkForDuplication(feature.getId());
		return true;
	}

	@Override
	public boolean visitObject(final GMLObject o) {
		checkForDuplication(o.getId());
		return true;
	}

	@Override
	public boolean visitReference(final Reference<?> ref) {
		return false;
	}

	private void checkForDuplication(final String id) {
		if (id != null) {
			if (ids.contains(id)) {
				final String msg = "Duplication of object id '" + id + "'.";
				throw new IllegalArgumentException(msg);
			}
			ids.add(id);
		}
	}

}
