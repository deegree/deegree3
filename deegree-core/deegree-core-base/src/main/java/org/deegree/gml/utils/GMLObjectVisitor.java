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
package org.deegree.gml.utils;

import org.deegree.commons.tom.Reference;
import org.deegree.commons.tom.gml.GMLObject;
import org.deegree.feature.Feature;
import org.deegree.geometry.Geometry;

/**
 * Visitor interface for the {@link GMLObjectWalker}.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
public interface GMLObjectVisitor {

	/**
	 * Called when a {@link Geometry} node is encountered.
	 * @param geom geometry, never <code>null</code>
	 * @return <code>true</code>, if children of this node shall be traversed,
	 * <code>false</code> otherwise
	 */
	public boolean visitGeometry(Geometry geom);

	/**
	 * Called when a {@link Feature} node is encountered.
	 * @param feature feature, never <code>null</code>
	 * @return <code>true</code>, if children of this node shall be traversed,
	 * <code>false</code> otherwise
	 */
	public boolean visitFeature(Feature feature);

	/**
	 * Called when a {@link GMLObject} node (not geometry, not feature) is encountered.
	 * @param o object, never <code>null</code>
	 * @return <code>true</code>, if children of this node shall be traversed,
	 * <code>false</code> otherwise
	 */
	public boolean visitObject(GMLObject o);

	/**
	 * Called when a {@link Reference} node is encountered.
	 * @param ref reference, never <code>null</code>
	 * @return <code>true</code>, if the referenced object shall be traversed,
	 * <code>false</code> otherwise
	 */
	public boolean visitReference(Reference<?> ref);

}
