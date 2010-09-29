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
package org.deegree.feature.types.property;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;

import org.deegree.commons.utils.StringUtils;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.composite.CompositeCurve;
import org.deegree.geometry.composite.CompositeGeometry;
import org.deegree.geometry.composite.CompositeSolid;
import org.deegree.geometry.composite.CompositeSurface;
import org.deegree.geometry.multi.MultiCurve;
import org.deegree.geometry.multi.MultiGeometry;
import org.deegree.geometry.multi.MultiLineString;
import org.deegree.geometry.multi.MultiPoint;
import org.deegree.geometry.multi.MultiPolygon;
import org.deegree.geometry.multi.MultiSolid;
import org.deegree.geometry.multi.MultiSurface;
import org.deegree.geometry.primitive.Curve;
import org.deegree.geometry.primitive.GeometricPrimitive;
import org.deegree.geometry.primitive.LineString;
import org.deegree.geometry.primitive.LinearRing;
import org.deegree.geometry.primitive.OrientableCurve;
import org.deegree.geometry.primitive.OrientableSurface;
import org.deegree.geometry.primitive.Point;
import org.deegree.geometry.primitive.Polygon;
import org.deegree.geometry.primitive.PolyhedralSurface;
import org.deegree.geometry.primitive.Ring;
import org.deegree.geometry.primitive.Solid;
import org.deegree.geometry.primitive.Surface;
import org.deegree.geometry.primitive.Tin;
import org.deegree.geometry.primitive.TriangulatedSurface;

/**
 * {@link PropertyType} that defines a property with a {@link Geometry} value.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
public class GeometryPropertyType extends AbstractPropertyType {

    /** The flattened geometry hierarchy supported by deegree's ISO geometry model. */
    public enum GeometryType {
        /** Any kind of geometry (primitive, composite or aggregate). */
        GEOMETRY( Geometry.class, null ),
        /** A primitive geometry. */
        PRIMITIVE( GeometricPrimitive.class, GEOMETRY ),
        /** A composite geometry. */
        COMPOSITE( CompositeGeometry.class, GEOMETRY ),
        /** A composite geometry. */
        POINT( Point.class, PRIMITIVE ),
        /** A composite geometry. */
        CURVE( Curve.class, PRIMITIVE ),
        /** A composite geometry. */
        LINE_STRING( LineString.class, CURVE ),
        /** A composite geometry. */
        RING( Ring.class, CURVE ),
        /** A composite geometry. */
        LINEAR_RING( LinearRing.class, RING ),
        /** A composite geometry. */
        ORIENTABLE_CURVE( OrientableCurve.class, CURVE ),
        /** A composite geometry. */
        COMPOSITE_CURVE( CompositeCurve.class, CURVE ),
        /** A composite geometry. */
        SURFACE( Surface.class, PRIMITIVE ),
        /** A composite geometry. */
        POLYHEDRAL_SURFACE( PolyhedralSurface.class, SURFACE ),
        /** A composite geometry. */
        TRIANGULATED_SURFACE( TriangulatedSurface.class, SURFACE ),
        /** A composite geometry. */
        TIN( Tin.class, SURFACE ),
        /** A composite geometry. */
        POLYGON( Polygon.class, SURFACE ),
        /** A composite geometry. */
        ORIENTABLE_SURFACE( OrientableSurface.class, SURFACE ),
        /** A composite geometry. */
        COMPOSITE_SURFACE( CompositeSurface.class, SURFACE ),
        /** A composite geometry. */
        SOLID( Solid.class, PRIMITIVE ),
        /** A composite geometry. */
        COMPOSITE_SOLID( CompositeSolid.class, SOLID ),
        /** A composite geometry. */
        MULTI_GEOMETRY( MultiGeometry.class, GEOMETRY ),
        /** A composite geometry. */
        MULTI_POINT( MultiPoint.class, MULTI_GEOMETRY ),
        /** A composite geometry. */
        MULTI_CURVE( MultiCurve.class, MULTI_GEOMETRY ),
        /** A composite geometry. */
        MULTI_LINE_STRING( MultiLineString.class, MULTI_GEOMETRY ),
        /** A composite geometry. */
        MULTI_SURFACE( MultiSurface.class, MULTI_GEOMETRY ),
        /** A composite geometry. */
        MULTI_POLYGON( MultiPolygon.class, MULTI_GEOMETRY ),
        /** A composite geometry. */
        MULTI_SOLID( MultiSolid.class, MULTI_GEOMETRY );

        private final Class<? extends Geometry> classImpl;

        private final GeometryType baseType;

        private GeometryType( Class<? extends Geometry> classImpl, GeometryType baseType ) {
            this.classImpl = classImpl;
            this.baseType = baseType;
        }

        /**
         * @return the awaited {@link Geometry} type for this geometry.
         */
        public Class<? extends Geometry> getJavaType() {
            return classImpl;
        }

        /**
         * Find the common base type of this geometry type and another geometry type
         * 
         * @param other
         *            to get the base type for.
         * @return the common base
         */
        public GeometryType findCommonBaseType( GeometryType other ) {
            if ( other == null || this.baseType == null ) {
                return GEOMETRY;
            }
            if ( this == other ) {
                return this;
            }
            GeometryType current = this;
            boolean found = false;
            while ( !found && current != null ) {
                GeometryType check = other;
                while ( !found && check != null ) {
                    found = check == current;
                    check = check.baseType;
                }
                if ( !found && current != GEOMETRY ) {
                    current = current.baseType;
                }
            }
            return current;

        }

        /**
         * Get the geometry type from the given string, if the type could not be mapped, a {@link #GEOMETRY} will be
         * returned.
         * 
         * @param geomString
         *            to get the type for
         * @return the geometry type.
         */
        public static GeometryType fromGMLTypeName( String geomString ) {
            GeometryType result = GEOMETRY;
            if ( StringUtils.isSet( geomString ) ) {
                String t = geomString.toLowerCase();
                t = t.replace( "_", "" );
                t = t.replace( "abstract", "" );

                if ( "geometry".equals( t ) ) {
                    result = GEOMETRY;
                } else if ( "curve".equals( t ) ) {
                    result = CURVE;
                } else if ( "CompositeCurve".equalsIgnoreCase( t ) ) {
                    result = COMPOSITE_CURVE;
                } else if ( "LineString".equalsIgnoreCase( t ) ) {
                    result = LINE_STRING;
                } else if ( "OrientableCurve".equalsIgnoreCase( t ) ) {
                    result = ORIENTABLE_CURVE;
                } else if ( "Ring".equalsIgnoreCase( t ) ) {
                    result = RING;
                } else if ( "LinearRing".equalsIgnoreCase( t ) ) {
                    result = LINEAR_RING;
                } else if ( "point".equals( t ) ) {
                    result = POINT;
                } else if ( "solid".equals( t ) ) {
                    result = SOLID;
                } else if ( "CompositeSolid".equalsIgnoreCase( t ) ) {
                    result = COMPOSITE_SOLID;
                } else if ( "surface".equals( t ) ) {
                    result = SURFACE;
                } else if ( "CompositeSurface".equalsIgnoreCase( t ) ) {
                    result = COMPOSITE_SURFACE;
                } else if ( "OrientableSurface".equalsIgnoreCase( t ) ) {
                    result = ORIENTABLE_SURFACE;
                } else if ( "Polygon".equalsIgnoreCase( t ) ) {
                    result = POLYGON;
                } else if ( "PolyhedralSurface".equalsIgnoreCase( t ) ) {
                    result = POLYHEDRAL_SURFACE;
                } else if ( "Tin".equalsIgnoreCase( t ) ) {
                    result = TIN;
                } else if ( "TriangulatedSurface".equalsIgnoreCase( t ) ) {
                    result = TRIANGULATED_SURFACE;
                } else if ( "MultiGeometry".equalsIgnoreCase( t ) || "GeometricAggregate".equalsIgnoreCase( t ) ) {
                    result = MULTI_GEOMETRY;
                } else if ( "MultiCurve".equalsIgnoreCase( t ) ) {
                    result = MULTI_CURVE;
                } else if ( "MultiLineString".equalsIgnoreCase( t ) ) {
                    result = MULTI_LINE_STRING;
                } else if ( "MultiPoint".equalsIgnoreCase( t ) ) {
                    result = MULTI_POINT;
                } else if ( "MultiPolygon".equalsIgnoreCase( t ) ) {
                    result = MULTI_POLYGON;
                } else if ( "MultiSolid".equalsIgnoreCase( t ) ) {
                    result = MULTI_SOLID;
                } else if ( "MultiSurface".equalsIgnoreCase( t ) ) {
                    result = MULTI_SURFACE;
                } else {
                    throw new IllegalArgumentException( "Unknown type: " + t );
                }
            }
            return result;
        }

        /**
         * @param allowedTypes
         */
        public static GeometryType determineMinimalBaseGeometry( Set<GeometryType> allowedTypes ) {
            GeometryType result = null;
            Iterator<GeometryType> it = allowedTypes.iterator();
            while ( it.hasNext() ) {
                if ( result == null ) {
                    result = it.next();
                } else {
                    GeometryType current = it.next();
                    result = result.findCommonBaseType( current );
                }
            }
            return result;
        }

        public boolean isCompatible( Geometry geometry ) {
            switch ( this ) {
            case COMPOSITE:
                return geometry instanceof CompositeGeometry<?>;
            case COMPOSITE_CURVE:
                return geometry instanceof CompositeCurve;
            case COMPOSITE_SOLID:
                return geometry instanceof CompositeSolid;
            case COMPOSITE_SURFACE:
                return geometry instanceof CompositeSurface;
            case CURVE:
                return geometry instanceof Curve;
            case GEOMETRY:
                return true;
            case LINE_STRING:
                return geometry instanceof LineString;
            case LINEAR_RING:
                return geometry instanceof LinearRing;
            case MULTI_CURVE:
                return geometry instanceof MultiCurve;
            case MULTI_GEOMETRY:
                return geometry instanceof MultiGeometry<?>;
            case MULTI_LINE_STRING:
                return geometry instanceof MultiLineString;
            case MULTI_POINT:
                return geometry instanceof MultiPoint;
            case MULTI_POLYGON:
                return geometry instanceof MultiPolygon;
            case MULTI_SOLID:
                return geometry instanceof MultiSolid;
            case MULTI_SURFACE:
                return geometry instanceof MultiSurface;
            case ORIENTABLE_CURVE:
                return geometry instanceof OrientableCurve;
            case ORIENTABLE_SURFACE:
                return geometry instanceof OrientableSurface;
            case POINT:
                return geometry instanceof Point;
            case POLYGON:
                return geometry instanceof Polygon;
            case POLYHEDRAL_SURFACE:
                return geometry instanceof PolyhedralSurface;
            case PRIMITIVE:
                return geometry instanceof GeometricPrimitive;
            case RING:
                return geometry instanceof PolyhedralSurface;
            case SOLID:
                return geometry instanceof Solid;
            case SURFACE:
                return geometry instanceof Surface;
            case TIN:
                return geometry instanceof Tin;
            case TRIANGULATED_SURFACE:
                return geometry instanceof TriangulatedSurface;
            }
            return false;
        }
    }

    public enum CoordinateDimension {
        DIM_2, DIM_3, DIM_2_OR_3,
    }

    private GeometryType geomType;

    private CoordinateDimension dim;

    private final ValueRepresentation representation;

    private final Set<GeometryType> allowedGeometryTypes;

    public GeometryPropertyType( QName name, int minOccurs, int maxOccurs, boolean isAbstract,
                                 boolean isNillable, List<PropertyType> substitutions, GeometryType geomType,
                                 CoordinateDimension dim, ValueRepresentation representation ) {
        super( name, minOccurs, maxOccurs, isAbstract, isNillable, substitutions );
        this.geomType = geomType;
        this.allowedGeometryTypes = new HashSet<GeometryType>();
        this.allowedGeometryTypes.add( this.geomType );
        this.dim = dim;
        this.representation = representation;
    }

    /**
     * @param name
     *            of the geometry property
     * @param minOccurs
     * @param maxOccurs
     * @param isAbstract
     * @param substitutions
     * @param geomTypes
     *            allowed types for this geometry property (a choice declaration in the schema).
     * @param dim
     * @param representation
     */
    public GeometryPropertyType( QName name, int minOccurs, int maxOccurs, boolean isAbstract,
                                 boolean isNillable, List<PropertyType> substitutions, Set<GeometryType> geomTypes,
                                 CoordinateDimension dim, ValueRepresentation representation ) {
        super( name, minOccurs, maxOccurs, isAbstract, isNillable, substitutions );
        this.allowedGeometryTypes = geomTypes;
        this.geomType = GeometryType.determineMinimalBaseGeometry( geomTypes );
        this.dim = dim;
        this.representation = representation;
    }

    /**
     * @return the allowed geometries of this geometry property. If the property was defined as a 'choice' multiple
     *         types will be available.
     */
    public Set<GeometryType> getAllowedGeometryTypes() {
        return this.allowedGeometryTypes;
    }

    /**
     * @return true if this geometry property has multiple geometry declarations (was defined as a choice).
     */
    public boolean hasMultipleGeometryDeclarations() {
        return this.allowedGeometryTypes.size() > 1;
    }

    public GeometryType getGeometryType() {
        return geomType;
    }

    public CoordinateDimension getCoordinateDimension() {
        return dim;
    }

    /**
     * Returns the allowed representation form of the value object.
     * 
     * @return the allowed representation form, never <code>null</code>
     */
    public ValueRepresentation getAllowedRepresentation() {
        return representation;
    }

    @Override
    public String toString() {
        String s = "- geometry property type: '" + name + "', minOccurs=" + minOccurs + ", maxOccurs=" + maxOccurs
                   + ", geometry type: " + geomType;
        return s;
    }
}