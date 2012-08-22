//$HeadURL$
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

package org.deegree.io.shpapi;

/**
 * Class containing all constants needed for reading of a shape file <BR>
 *
 * @version 14.12.1999
 * @author Andreas Poth
 *
 */
public class ShapeConst {

    /**
     * The length of a shape file record header in bytes. (8)
     */
    public static final int SHAPE_FILE_RECORD_HEADER_LENGTH = 8;

    /**
     * The length of a shape file header in bytes. (100)
     */
    public static final int SHAPE_FILE_HEADER_LENGTH = 100;

    /**
     * A Shape File's magic number.
     */
    public static final int SHAPE_FILE_CODE = 9994;

    /**
     * The currently handled version of Shape Files.
     */
    public static final int SHAPE_FILE_VERSION = 1000;

    /**
     * The indicator for a null shape type. (0)
     */
    public static final int SHAPE_TYPE_NULL = 0;

    /**
     * The indicator for a point shape type. (1)
     */
    public static final int SHAPE_TYPE_POINT = 1;

    /**
     * The indicator for an polyline shape type. (3)
     */
    public static final int SHAPE_TYPE_POLYLINE = 3;

    /**
     * The indicator for a polygon shape type. (5)
     */
    public static final int SHAPE_TYPE_POLYGON = 5;

    /**
     * The indicator for a multipoint shape type. (8)
     */
    public static final int SHAPE_TYPE_MULTIPOINT = 8;

    /**
     * The indicator for a polygonz shape type. (15)
     */
    public static final int SHAPE_TYPE_POLYGONZ = 15;

    /**
     * start point of field parts in ESRI shape record
     */
    public static final int PARTS_START = 44;

}
