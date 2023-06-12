/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2011 by:
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
package org.deegree.sqldialect.oracle.sdo;

/**
 * List of valid last two digits of SDO_GTYPE
 *
 * SDO_GTYPE of Geometry Types as available in Oracle 10.x
 *
 * <p>
 * The Oracle SDO_GTYPE is 4 digits long number which is build from <code>DLTT</code>.
 * <ul>
 * <li><code>D</code> is the dimension.</li>
 * <li><code>L</code> is the linear referencing measure dimension.</li>
 * <li><code>TT</code> is the geometry type as described in this class.</li>
 * </ul>
 * </p>
 *
 * @see Oracle Spatial User's Guide and Reference / Section SDO_GEOMETRY Object Type
 * @author <a href="mailto:reichhelm@grit.de">Stephan Reichhelm</a>
 */
public interface SDOGTypeTT {

	/** Unknown Geometry (ignored by spatial) */
	public static final int UNKNOWN = 0;

	/** Point Type */
	public static final int POINT = 1;

	/** Line (or Curve) */
	public static final int LINE = 2;

	/** Polygon (or Surface) */
	public static final int POLYGON = 3;

	/** Collection of any of the other Types */
	public static final int COLLECTION = 4;

	/** Multiple Point Types */
	public static final int MULTIPOINT = 5;

	/** Multiple Line (or Curve) Types */
	public static final int MULTILINE = 6;

	/** Multiple Polygon (or Surface) Types */
	public static final int MULTIPOLYGON = 7;

	/* Oracle 11g Types are currently not supported */
	// Solid Type
	// public static final int SOLID = 8;
	// Multiple Solid Types
	// public static final int MULTISOLID = 9;

}
