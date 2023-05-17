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
 * List of valid SDO_ETYPE
 *
 * SDO_ETYPE of Geometry Types as available in Oracle 10.x
 *
 * @see Oracle Spatial User's Guide and Reference / Section SDO_GEOMETRY Object Type
 * @author <a href="mailto:reichhelm@grit.de">Stephan Reichhelm</a>
 */
public interface SDOEType {

	public static final int UNKNOWN = 0;

	public static final int POINT = 1;

	public static final int LINESTRING = 2;

	public static final int POLYGON_RING_UNKNOWN = 3;

	public static final int COMPOUND_LINESTRING = 4;

	public static final int POLYGON_RING_EXTERIOR = 1003;

	public static final int POLYGON_RING_INTERIOR = 2003;

	public static final int COMPOUND_POLYGON_RING_EXTERIOR = 1005;

	public static final int COMPOUND_POLYGON_RING_INTERIOR = 2005;

	public static final int COMPOUND_POLYGON_RING_UNKNOWN = 5;

	/* Oracle 11g Types are currently not supported */
	// public static final int SURFACE_EXTERIOR = 1006;
	// public static final int SURFACE_INTERIOR = 2006;
	// public static final int SOLID = 1007;

}
