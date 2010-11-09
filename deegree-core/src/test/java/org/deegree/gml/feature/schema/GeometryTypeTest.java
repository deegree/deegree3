//$HeadURL$
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
package org.deegree.gml.feature.schema;

import static org.deegree.feature.types.property.GeometryPropertyType.GeometryType.COMPOSITE;
import static org.deegree.feature.types.property.GeometryPropertyType.GeometryType.COMPOSITE_CURVE;
import static org.deegree.feature.types.property.GeometryPropertyType.GeometryType.COMPOSITE_SOLID;
import static org.deegree.feature.types.property.GeometryPropertyType.GeometryType.COMPOSITE_SURFACE;
import static org.deegree.feature.types.property.GeometryPropertyType.GeometryType.CURVE;
import static org.deegree.feature.types.property.GeometryPropertyType.GeometryType.GEOMETRY;
import static org.deegree.feature.types.property.GeometryPropertyType.GeometryType.LINEAR_RING;
import static org.deegree.feature.types.property.GeometryPropertyType.GeometryType.LINE_STRING;
import static org.deegree.feature.types.property.GeometryPropertyType.GeometryType.MULTI_CURVE;
import static org.deegree.feature.types.property.GeometryPropertyType.GeometryType.MULTI_POINT;
import static org.deegree.feature.types.property.GeometryPropertyType.GeometryType.MULTI_SOLID;
import static org.deegree.feature.types.property.GeometryPropertyType.GeometryType.MULTI_SURFACE;
import static org.deegree.feature.types.property.GeometryPropertyType.GeometryType.ORIENTABLE_SURFACE;
import static org.deegree.feature.types.property.GeometryPropertyType.GeometryType.POINT;
import static org.deegree.feature.types.property.GeometryPropertyType.GeometryType.POLYGON;
import static org.deegree.feature.types.property.GeometryPropertyType.GeometryType.PRIMITIVE;
import static org.deegree.feature.types.property.GeometryPropertyType.GeometryType.RING;
import static org.deegree.feature.types.property.GeometryPropertyType.GeometryType.SOLID;
import static org.deegree.feature.types.property.GeometryPropertyType.GeometryType.SURFACE;
import static org.deegree.feature.types.property.GeometryPropertyType.GeometryType.TRIANGULATED_SURFACE;
import static org.deegree.feature.types.property.GeometryPropertyType.GeometryType.determineMinimalBaseGeometry;
import static org.deegree.feature.types.property.GeometryPropertyType.GeometryType.fromGMLTypeName;

import java.util.HashSet;

import junit.framework.Assert;

import org.deegree.feature.types.property.GeometryPropertyType.GeometryType;
import org.junit.Test;

/**
 * Tests some of the geometrytype mapping functions.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class GeometryTypeTest {

    /**
     * Grid is not known
     */
    @Test(expected = IllegalArgumentException.class)
    public void testGrid() {
        fromGMLTypeName( "Grid" );
    }

    /**
     * Rectifiedgrid is not known
     */
    @Test(expected = IllegalArgumentException.class)
    public void testRectifiedGrid() {
        fromGMLTypeName( "RectifiedGrid" );
    }

    /**
     * Test some incoming names
     */
    @Test
    public void mapFromString() {
        Assert.assertEquals( GEOMETRY, fromGMLTypeName( "_Geometry" ) );
        Assert.assertEquals( MULTI_CURVE, fromGMLTypeName( "MultiCurve" ) );
        Assert.assertEquals( MULTI_POINT, fromGMLTypeName( "MultiPoint" ) );
        Assert.assertEquals( MULTI_SOLID, fromGMLTypeName( "MultiSolid" ) );
        Assert.assertEquals( MULTI_SURFACE, fromGMLTypeName( "MultiSurface" ) );
        Assert.assertEquals( POINT, fromGMLTypeName( "Point" ) );
        Assert.assertEquals( POLYGON, fromGMLTypeName( "Polygon" ) );
        Assert.assertEquals( SOLID, fromGMLTypeName( "_Solid" ) );
        Assert.assertEquals( SURFACE, fromGMLTypeName( "_Surface" ) );
        Assert.assertEquals( TRIANGULATED_SURFACE, fromGMLTypeName( "TriangulatedSurface" ) );

        Assert.assertEquals( GEOMETRY, fromGMLTypeName( "AbstractGeometry" ) );
        Assert.assertEquals( SOLID, fromGMLTypeName( "AbstractSolid" ) );
        Assert.assertEquals( SURFACE, fromGMLTypeName( "AbstractSurface" ) );
    }

    /**
     * Find the parents of two different geometry types
     */
    @Test
    public void findParents() {
        Assert.assertEquals( CURVE, LINEAR_RING.findCommonBaseType( COMPOSITE_CURVE ) );
        Assert.assertEquals( CURVE, COMPOSITE_CURVE.findCommonBaseType( LINEAR_RING ) );
        Assert.assertEquals( PRIMITIVE, POINT.findCommonBaseType( COMPOSITE_SOLID ) );
        Assert.assertEquals( PRIMITIVE, COMPOSITE_SOLID.findCommonBaseType( POINT ) );

        Assert.assertEquals( RING, RING.findCommonBaseType( LINEAR_RING ) );
        Assert.assertEquals( RING, LINEAR_RING.findCommonBaseType( RING ) );

        Assert.assertEquals( GEOMETRY, MULTI_CURVE.findCommonBaseType( COMPOSITE ) );

        Assert.assertEquals( GEOMETRY, COMPOSITE.findCommonBaseType( MULTI_CURVE ) );

        Assert.assertEquals( SURFACE, ORIENTABLE_SURFACE.findCommonBaseType( COMPOSITE_SURFACE ) );

        Assert.assertEquals( SURFACE, COMPOSITE_SURFACE.findCommonBaseType( ORIENTABLE_SURFACE ) );

        Assert.assertEquals( ORIENTABLE_SURFACE, ORIENTABLE_SURFACE.findCommonBaseType( ORIENTABLE_SURFACE ) );

        Assert.assertEquals( GEOMETRY, ORIENTABLE_SURFACE.findCommonBaseType( GEOMETRY ) );
        Assert.assertEquals( GEOMETRY, GEOMETRY.findCommonBaseType( ORIENTABLE_SURFACE ) );

    }

    /**
     * Find the base of different geometry types
     */
    @Test
    public void findBaseGeometry() {
        HashSet<GeometryType> set = new HashSet<GeometryType>();
        set.add( LINEAR_RING );
        Assert.assertEquals( LINEAR_RING, determineMinimalBaseGeometry( set ) );

        set.add( RING );
        Assert.assertEquals( RING, determineMinimalBaseGeometry( set ) );

        set.add( COMPOSITE_CURVE );
        set.add( LINE_STRING );
        Assert.assertEquals( CURVE, determineMinimalBaseGeometry( set ) );

        set.add( COMPOSITE_SOLID );
        set.add( POINT );
        Assert.assertEquals( PRIMITIVE, determineMinimalBaseGeometry( set ) );

        set.add( MULTI_CURVE );
        Assert.assertEquals( GEOMETRY, determineMinimalBaseGeometry( set ) );
    }
}
