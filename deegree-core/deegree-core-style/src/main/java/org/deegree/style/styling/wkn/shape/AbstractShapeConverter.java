/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2022 by:
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
package org.deegree.style.styling.wkn.shape;

import static java.awt.geom.Path2D.WIND_EVEN_ODD;

import java.awt.Shape;
import java.awt.geom.GeneralPath;

import org.deegree.geometry.Geometry;
import org.deegree.geometry.composite.CompositeGeometry;
import org.deegree.geometry.multi.MultiGeometry;
import org.deegree.geometry.primitive.Curve;
import org.deegree.geometry.primitive.GeometricPrimitive;
import org.deegree.geometry.primitive.Surface;
import org.deegree.geometry.primitive.patches.PolygonPatch;
import org.deegree.geometry.primitive.patches.SurfacePatch;

public abstract class AbstractShapeConverter {

	public Shape convert(Geometry geometry) {
		GeneralPath path = new GeneralPath(WIND_EVEN_ODD);
		toShape(path, geometry);
		return path;
	}

	protected abstract void toShape(GeneralPath path, Curve geometry);

	private void toShape(GeneralPath path, Geometry geometry) {
		switch (geometry.getGeometryType()) {
			case ENVELOPE:
				// will be ignored
				break;
			case COMPOSITE_GEOMETRY:
				@SuppressWarnings("unchecked")
				CompositeGeometry<? extends GeometricPrimitive> comp = (CompositeGeometry<? extends GeometricPrimitive>) geometry;
				toShape(path, comp);
			case MULTI_GEOMETRY:
				@SuppressWarnings("unchecked")
				MultiGeometry<? extends Geometry> multi = (MultiGeometry<? extends Geometry>) geometry;
				toShape(path, multi);
				break;
			case PRIMITIVE_GEOMETRY:
				switch (((GeometricPrimitive) geometry).getPrimitiveType()) {
					case Curve:
						toShape(path, (Curve) geometry);
						break;
					case Point:
					case Solid:
						// will be ignored
						break;
					case Surface:
						toShape(path, (Surface) geometry);
						break;
				}
				break;
		}
	}

	private void toShape(GeneralPath path, CompositeGeometry<? extends GeometricPrimitive> geometry) {
		for (Geometry geom : geometry) {
			toShape(path, geom);
		}
	}

	private void toShape(GeneralPath path, MultiGeometry<? extends Geometry> geometry) {
		for (Geometry geom : geometry) {
			toShape(path, geom);
		}
	}

	private void toShape(GeneralPath path, Surface surface) {
		for (SurfacePatch patch : surface.getPatches()) {
			if (patch instanceof PolygonPatch) {
				PolygonPatch polygonPatch = (PolygonPatch) patch;
				for (Curve curve : polygonPatch.getBoundaryRings()) {
					toShape(path, curve);
				}
			}
			else {
				throw new IllegalArgumentException("Cannot render non-planar surfaces.");
			}
		}
	}

}
