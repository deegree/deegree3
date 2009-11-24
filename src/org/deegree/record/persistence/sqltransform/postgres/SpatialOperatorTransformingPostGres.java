//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
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
package org.deegree.record.persistence.sqltransform.postgres;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.deegree.filter.expression.PropertyName;
import org.deegree.filter.spatial.BBOX;
import org.deegree.filter.spatial.Beyond;
import org.deegree.filter.spatial.Contains;
import org.deegree.filter.spatial.Crosses;
import org.deegree.filter.spatial.DWithin;
import org.deegree.filter.spatial.Disjoint;
import org.deegree.filter.spatial.Equals;
import org.deegree.filter.spatial.Intersects;
import org.deegree.filter.spatial.Overlaps;
import org.deegree.filter.spatial.SpatialOperator;
import org.deegree.filter.spatial.Touches;
import org.deegree.filter.spatial.Within;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.WKTWriter;
import org.deegree.geometry.Geometry.GeometryType;
import org.deegree.geometry.composite.CompositeCurve;
import org.deegree.geometry.primitive.Curve;
import org.deegree.geometry.primitive.GeometricPrimitive;
import org.deegree.geometry.primitive.LineString;
import org.deegree.geometry.primitive.OrientableCurve;
import org.deegree.geometry.primitive.Point;
import org.deegree.geometry.primitive.Ring;
import org.deegree.geometry.primitive.Solid;
import org.deegree.geometry.primitive.Surface;
import org.deegree.geometry.standard.composite.DefaultCompositeGeometry;
import org.deegree.geometry.standard.multi.DefaultMultiGeometry;

/**
 * Transforms the spatial query into a PostGreSQL statement. It encapsules the required methods.
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author: thomas $
 * 
 * @version $Revision: $, $Date: $
 */
public class SpatialOperatorTransformingPostGres {

    ExpressionFilterHandling expressionFilterHandling = new ExpressionFilterHandling();

    private ExpressionFilterObject expressObject;

    private int counter;

    private Set<String> table;

    private Set<String> column;

    private String spatialOperation;

    private SpatialOperator spaOp;

    private String stringSpatialPropertyName;

    private String stringSpatialGeometry;

    private List<String> stringSpatialGeom;

    public SpatialOperatorTransformingPostGres( SpatialOperator spaOp ) {
        this.spaOp = spaOp;

        spatialOperation = doSpatialOperatorToPostGreSQL();

    }

    /**
     * @return the spatialOperation
     */
    public String getSpatialOperation() {
        return spatialOperation;
    }

    /**
     * @return the table
     */
    public Set<String> getTable() {
        return table;
    }

    /**
     * @return the column
     */
    public Set<String> getColumn() {
        return column;
    }

    /**
     * Building the SQL statement for the requested spatial query
     * 
     * @return
     */
    private String doSpatialOperatorToPostGreSQL() {

        org.deegree.filter.spatial.SpatialOperator.SubType typeSpatial = spaOp.getSubType();

        String stringSpatial = "";

        table = new HashSet<String>();

        column = new HashSet<String>();

        stringSpatialPropertyName = "";

        stringSpatialGeometry = "";

        switch ( typeSpatial ) {

        case BBOX:

            BBOX bboxOp = (BBOX) spaOp;
            Object[] paramsBBox = bboxOp.getParams();
            stringSpatial = "";

            for ( Object opParam : paramsBBox ) {

                if ( opParam != bboxOp.getBoundingBox() ) {
                    stringSpatial += propertyNameBuild( opParam );
                    stringSpatial += " && GeomFromText(AsText(SetSRID('BOX3D( ";
                } else {
                    Envelope envelope = (Envelope) opParam;
                    stringSpatialGeometry += WKTWriter.write( envelope );
                    stringSpatial += stringSpatialGeometry;
                }
                System.out.println( stringSpatial );

            }

            return stringSpatial;

        case BEYOND:
            Beyond beyondOp = (Beyond) spaOp;
            Object[] paramsBeyond = beyondOp.getParams();
            stringSpatial += "DISTANCE(";
            stringSpatialGeom = new LinkedList<String>();

            counter = 0;

            for ( Object opParam : paramsBeyond ) {

                if ( opParam != beyondOp.getGeometry() ) {
                    counter++;
                    stringSpatial += propertyNameBuild( opParam );

                } else {
                    counter++;
                    GeometryType geom = beyondOp.getGeometry().getGeometryType();
                    stringSpatial += geometryBuild( opParam, geom, stringSpatialGeom );
                }
                if ( counter < paramsBeyond.length ) {
                    stringSpatial += ",";
                } else {
                    stringSpatial += ") <= " + beyondOp.getDistance().getValue().toString() + " AND ";
                }
            }
            stringSpatial += operatorBuild( "DISJOINT" );

            return stringSpatial;

        case CONTAINS:

            Contains containsOp = (Contains) spaOp;
            Object[] paramsContains = containsOp.getParams();
            stringSpatial = "(";
            counter = 0;
            stringSpatialGeom = new LinkedList<String>();

            for ( Object opParam : paramsContains ) {

                if ( opParam != containsOp.getGeometry() ) {
                    counter++;
                    propertyNameBuild( opParam );

                } else {
                    counter++;
                    GeometryType geom = containsOp.getGeometry().getGeometryType();
                    geometryBuild( opParam, geom, stringSpatialGeom );
                }

            }
            stringSpatial += operatorBuild( "CONTAINS" );

            return stringSpatial;

        case CROSSES:
            Crosses crossesOp = (Crosses) spaOp;
            Object[] paramsCrosses = crossesOp.getParams();
            stringSpatial = "(";
            counter = 0;
            stringSpatialGeom = new LinkedList<String>();

            for ( Object opParam : paramsCrosses ) {

                if ( opParam != crossesOp.getGeometry() ) {
                    counter++;
                    propertyNameBuild( opParam );

                } else {
                    counter++;
                    GeometryType geom = crossesOp.getGeometry().getGeometryType();
                    geometryBuild( opParam, geom, stringSpatialGeom );
                }

            }
            stringSpatial += operatorBuild( "CROSSES" );

            return stringSpatial;

        case DISJOINT:

            Disjoint disjointOp = (Disjoint) spaOp;
            Object[] paramsDisjoint = disjointOp.getParams();
            stringSpatial = "";
            counter = 0;
            stringSpatialGeom = new LinkedList<String>();

            for ( Object opParam : paramsDisjoint ) {

                if ( opParam != disjointOp.getGeometry() ) {
                    counter++;
                    propertyNameBuild( opParam );

                } else {
                    counter++;
                    GeometryType geom = disjointOp.getGeometry().getGeometryType();
                    geometryBuild( opParam, geom, stringSpatialGeom );
                }

            }
            stringSpatial += operatorBuild( "DISJOINT" );

            return stringSpatial;

        case DWITHIN:
            DWithin dWithinOp = (DWithin) spaOp;
            Object[] paramsDWithin = dWithinOp.getParams();
            stringSpatial += "DISTANCE(";
            stringSpatialGeom = new LinkedList<String>();

            counter = 0;

            for ( Object opParam : paramsDWithin ) {

                if ( opParam != dWithinOp.getGeometry() ) {
                    counter++;
                    stringSpatial += propertyNameBuild( opParam );

                } else {
                    counter++;
                    GeometryType geom = dWithinOp.getGeometry().getGeometryType();
                    stringSpatial += geometryBuild( opParam, geom, stringSpatialGeom );
                }
                if ( counter < paramsDWithin.length ) {
                    stringSpatial += ",";
                } else {
                    stringSpatial += ") >= " + dWithinOp.getDistance().getValue().toString() + " AND ";
                }
            }
            stringSpatial += operatorBuild( "DWITHIN" );

            return stringSpatial;

        case EQUALS:

            Equals equalsOp = (Equals) spaOp;
            Object[] paramsEquals = equalsOp.getParams();
            stringSpatial = "(";
            counter = 0;
            stringSpatialGeom = new LinkedList<String>();

            for ( Object opParam : paramsEquals ) {

                if ( opParam != equalsOp.getGeometry() ) {
                    counter++;
                    propertyNameBuild( opParam );

                } else {
                    counter++;
                    GeometryType geom = equalsOp.getGeometry().getGeometryType();
                    geometryBuild( opParam, geom, stringSpatialGeom );
                }

            }
            stringSpatial += operatorBuild( "EQUALS" );

            return stringSpatial;

        case INTERSECTS:
            Intersects intersectsOp = (Intersects) spaOp;
            Object[] paramsIntersects = intersectsOp.getParams();
            stringSpatial = "(";
            counter = 0;
            stringSpatialGeom = new LinkedList<String>();

            for ( Object opParam : paramsIntersects ) {

                if ( opParam != intersectsOp.getGeometry() ) {
                    counter++;
                    propertyNameBuild( opParam );

                } else {
                    counter++;
                    GeometryType geom = intersectsOp.getGeometry().getGeometryType();
                    geometryBuild( opParam, geom, stringSpatialGeom );
                }

            }
            stringSpatial += operatorBuild( "INTERSECTS" );

            return stringSpatial;

        case OVERLAPS:
            Overlaps overlapsOp = (Overlaps) spaOp;
            Object[] paramsOverlaps = overlapsOp.getParams();
            stringSpatial = "(";
            counter = 0;
            stringSpatialGeom = new LinkedList<String>();

            for ( Object opParam : paramsOverlaps ) {

                if ( opParam != overlapsOp.getGeometry() ) {
                    counter++;
                    propertyNameBuild( opParam );

                } else {
                    counter++;
                    GeometryType geom = overlapsOp.getGeometry().getGeometryType();
                    geometryBuild( opParam, geom, stringSpatialGeom );
                }

            }
            stringSpatial += operatorBuild( "OVERLAPS" );

            return stringSpatial;

        case TOUCHES:
            Touches touchesOp = (Touches) spaOp;
            Object[] paramsTouches = touchesOp.getParams();
            stringSpatial = "(";
            counter = 0;
            stringSpatialGeom = new LinkedList<String>();

            for ( Object opParam : paramsTouches ) {

                if ( opParam != touchesOp.getGeometry() ) {
                    counter++;
                    propertyNameBuild( opParam );

                } else {
                    counter++;
                    GeometryType geom = touchesOp.getGeometry().getGeometryType();
                    geometryBuild( opParam, geom, stringSpatialGeom );
                }

            }
            stringSpatial += operatorBuild( "TOUCHES" );

            return stringSpatial;

        case WITHIN:
            Within withinOp = (Within) spaOp;
            Object[] paramsWithin = withinOp.getParams();
            stringSpatial = "(";
            counter = 0;
            stringSpatialGeom = new LinkedList<String>();

            for ( Object opParam : paramsWithin ) {

                if ( opParam != withinOp.getGeometry() ) {
                    counter++;
                    propertyNameBuild( opParam );

                } else {
                    counter++;
                    GeometryType geom = withinOp.getGeometry().getGeometryType();
                    geometryBuild( opParam, geom, stringSpatialGeom );
                }

            }
            stringSpatial += operatorBuild( "WITHIN" );

            return stringSpatial;

        }
        return stringSpatial;

    }

    /**
     * Building the SQL statement for the operator that is requested
     * 
     * @param operator
     * @return
     */
    private String operatorBuild( String operator ) {
        String stringSpatial = "";
        for ( String s : stringSpatialGeom ) {
            stringSpatial += operator + "(" + stringSpatialPropertyName + ",";
            // TODO handling and counter if there are more stringSpatial
            stringSpatial += s + ")";
        }
        return stringSpatial;
    }

    /**
     * Building the SQL statement for the propertyName
     * 
     * @param opParam
     * @return
     */
    private String propertyNameBuild( Object opParam ) {
        String stringSpatial = "";
        String exp = ( (PropertyName) opParam ).getPropertyName();

        stringSpatialPropertyName += "GeomFromText(AsText(";

        expressObject = expressionFilterHandling.expressionFilterHandling(
                                                                           ( (PropertyName) opParam ).getType(),
                                                                           new PropertyName(
                                                                                             exp,
                                                                                             ( (PropertyName) opParam ).getNsContext() ) );

        stringSpatialPropertyName += expressObject.getExpression();
        table.addAll( expressObject.getTable() );
        column.addAll( expressObject.getColumn() );
        stringSpatialPropertyName += "))";
        stringSpatial += stringSpatialPropertyName;
        return stringSpatial;
    }

    /**
     * Building the SQL statements for the geometries
     * 
     * @param opParam
     * @param geom
     * @param stringSpatialGeom
     * @return
     */
    private String geometryBuild( Object opParam, GeometryType geom, List<String> stringSpatialGeom ) {

        String stringSpatial = "";
        stringSpatialGeometry += "GeomFromText('";

        switch ( geom ) {

        case ENVELOPE:
            Envelope envelope = (Envelope) opParam;

            stringSpatialGeometry += WKTWriter.write( envelope );
            stringSpatial += stringSpatialGeometry;

            break;

        case PRIMITIVE_GEOMETRY:

            GeometricPrimitive geomPrim = (GeometricPrimitive) opParam;
            switch ( geomPrim.getPrimitiveType() ) {

            case Point:
                Point point = (Point) geomPrim;
                stringSpatialGeometry += WKTWriter.write( point );
                break;
            case Curve:
                Curve curveGeometry = (Curve) geomPrim;
                switch ( curveGeometry.getCurveType() ) {

                case Curve:
                    Curve curve = (Curve) curveGeometry;
                    stringSpatialGeometry += WKTWriter.write( curve );
                    break;

                case LineString:
                    LineString lineString = (LineString) curveGeometry;
                    stringSpatialGeometry += WKTWriter.write( lineString );
                    break;

                case OrientableCurve:
                    OrientableCurve orientalCurve = (OrientableCurve) curveGeometry;
                    stringSpatialGeometry += WKTWriter.write( orientalCurve );
                    break;

                case CompositeCurve:
                    CompositeCurve compositeCurve = (CompositeCurve) curveGeometry;
                    stringSpatialGeometry += WKTWriter.write( compositeCurve );
                    break;

                case Ring:
                    Ring ring = (Ring) curveGeometry;
                    stringSpatialGeometry += WKTWriter.write( ring );
                    break;

                }

                break;
            case Surface:
                Surface surface = (Surface) geomPrim;
                stringSpatialGeometry += WKTWriter.write( surface );
                break;
            case Solid:
                Solid solid = (Solid) geomPrim;
                stringSpatialGeometry += WKTWriter.write( solid );
                break;
            }

            break;

        case COMPOSITE_GEOMETRY:
            DefaultCompositeGeometry defCompGeo = (DefaultCompositeGeometry) opParam;

            break;

        case MULTI_GEOMETRY:
            DefaultMultiGeometry<Geometry> defMulGeo = (DefaultMultiGeometry<Geometry>) opParam;
            break;
        }

        stringSpatialGeometry += "')";
        stringSpatialGeom.add( stringSpatialGeometry );
        stringSpatial += stringSpatialGeometry;

        return stringSpatial;
    }

}
