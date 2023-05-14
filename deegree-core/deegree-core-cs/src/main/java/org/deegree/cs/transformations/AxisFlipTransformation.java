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
package org.deegree.cs.transformations;

import java.util.List;

import javax.vecmath.Point3d;

import org.deegree.cs.CRSResource;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.exceptions.TransformationException;

/**
 * Transformation means flipping of the first and second axis!
 *
 * @author <a href="mailto:goltz@lat-lon.org">Lyn Goltz</a>
 */
public class AxisFlipTransformation extends Transformation {

	public AxisFlipTransformation(ICRS sourceCRS, ICRS targetCRS, CRSResource id) {
		super(sourceCRS, targetCRS, id);
	}

	@Override
	public String getImplementationName() {
		return "AxisFlipTransformation";
	}

	@Override
	public List<Point3d> doTransform(List<Point3d> srcPts) throws TransformationException {
		for (Point3d p : srcPts) {
			double tmpX = p.x;
			p.x = p.y;
			p.y = tmpX;
		}
		return srcPts;
	}

	@Override
	public boolean isIdentity() {
		return false;
	}

}
