/*----------------------------------------------------------------------------
 This file is part of deegree
 Copyright (C) 2001-2026 by:
 - Department of Geography, University of Bonn -
 and
 - grit graphische Informationstechnik Beratungsgesellschaft mbH -
 and others

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

 e-mail: info@deegree.org

 website: https://www.deegree.org/
----------------------------------------------------------------------------*/
package org.deegree.geometry.io.ewkb;

public interface EWKBTypes {

	int POINT_TYPE = 1;

	int LINESTRING_TYPE = 2;

	int POLYGON_TYPE = 3;

	int MULTIPOINT_TYPE = 4;

	int MULTILINESTRING_TYPE = 5;

	int MULTIPOLYGON_TYPE = 6;

	int GEOMETRY_COLLECTION_TYPE = 7;

	int CIRCULAR_STRING_TYPE = 8;

	int COMPOUND_CURVE_TYPE = 9;

	int CURVE_POLYGON_TYPE = 10;

	int MULTICURVE_TYPE = 11;

	int MULTISURFACE_TYPE = 12;

	int TIN_TYPE = 17;

	int TRIANGLE_TYPE = 17;

}
