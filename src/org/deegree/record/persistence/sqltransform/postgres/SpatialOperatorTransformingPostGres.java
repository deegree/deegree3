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
import org.deegree.geometry.WKTWriter;

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

    WKTWriter wktWriter;

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

        String geometryString = "";

        switch ( typeSpatial ) {

        case BBOX:

            BBOX bboxOp = (BBOX) spaOp;
            Object[] paramsBBox = bboxOp.getParams();
            stringSpatial = "";
            stringSpatialGeom = new LinkedList<String>();
            wktWriter = new WKTWriter();

            for ( Object opParam : paramsBBox ) {
                if ( opParam != bboxOp.getBoundingBox() ) {
                    stringSpatial += propertyNameBuild( opParam );
                    stringSpatial += " && ";
                } else {
                    wktWriter.writeGeometry( bboxOp.getBoundingBox() );
                    stringSpatial += "'" + wktWriter.getGeometryString() + "'";

                }

            }

            return stringSpatial;

        case BEYOND:
            Beyond beyondOp = (Beyond) spaOp;
            Object[] paramsBeyond = beyondOp.getParams();
            stringSpatial += "DISTANCE(";
            stringSpatialGeom = new LinkedList<String>();
            wktWriter = new WKTWriter();

            counter = 0;

            for ( Object opParam : paramsBeyond ) {

                if ( opParam != beyondOp.getGeometry() ) {
                    counter++;
                    stringSpatial += propertyNameBuild( opParam );

                } else {
                    counter++;
                    wktWriter.writeGeometry( beyondOp.getGeometry() );
                    stringSpatial += "'" + wktWriter.getGeometryString() + "'";
                    geometryString = "'" + wktWriter.getGeometryString() + "'";

                }
                if ( counter < paramsBeyond.length ) {
                    stringSpatial += ",";
                } else {
                    stringSpatial += ") <= " + beyondOp.getDistance().getValue().toString() + " AND ";
                }
            }
            stringSpatial += operatorBuild( "DISJOINT", geometryString );

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
                    wktWriter.writeGeometry( containsOp.getGeometry() );
                    stringSpatial += "'" + wktWriter.getGeometryString() + "'";
                    geometryString = "'" + wktWriter.getGeometryString() + "'";
                }

            }
            stringSpatial += operatorBuild( "CONTAINS", geometryString );

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
                    wktWriter.writeGeometry( crossesOp.getGeometry() );
                    stringSpatial += "'" + wktWriter.getGeometryString() + "'";
                    geometryString = "'" + wktWriter.getGeometryString() + "'";
                }

            }
            stringSpatial += operatorBuild( "CROSSES", geometryString );

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
                    wktWriter.writeGeometry( disjointOp.getGeometry() );
                    stringSpatial += "'" + wktWriter.getGeometryString() + "'";
                    geometryString = "'" + wktWriter.getGeometryString() + "'";
                }

            }
            stringSpatial += operatorBuild( "DISJOINT", geometryString );

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
                    wktWriter.writeGeometry( dWithinOp.getGeometry() );
                    stringSpatial += "'" + wktWriter.getGeometryString() + "'";
                    geometryString = "'" + wktWriter.getGeometryString() + "'";
                }
                if ( counter < paramsDWithin.length ) {
                    stringSpatial += ",";
                } else {
                    stringSpatial += ") >= " + dWithinOp.getDistance().getValue().toString() + " AND ";
                }
            }
            stringSpatial += operatorBuild( "DWITHIN", geometryString );

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
                    wktWriter.writeGeometry( equalsOp.getGeometry() );
                    stringSpatial += "'" + wktWriter.getGeometryString() + "'";
                    geometryString = "'" + wktWriter.getGeometryString() + "'";
                }

            }
            stringSpatial += operatorBuild( "EQUALS", geometryString );

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
                    wktWriter.writeGeometry( intersectsOp.getGeometry() );
                    stringSpatial += "'" + wktWriter.getGeometryString() + "'";
                    geometryString = "'" + wktWriter.getGeometryString() + "'";
                }

            }
            stringSpatial += operatorBuild( "INTERSECTS", geometryString );

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
                    wktWriter.writeGeometry( overlapsOp.getGeometry() );
                    stringSpatial += "'" + wktWriter.getGeometryString() + "'";
                    geometryString = "'" + wktWriter.getGeometryString() + "'";
                }

            }
            stringSpatial += operatorBuild( "OVERLAPS", geometryString );

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
                    wktWriter.writeGeometry( touchesOp.getGeometry() );
                    stringSpatial += "'" + wktWriter.getGeometryString() + "'";
                    geometryString = "'" + wktWriter.getGeometryString() + "'";
                }

            }
            stringSpatial += operatorBuild( "TOUCHES", geometryString );

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
                    wktWriter.writeGeometry( withinOp.getGeometry() );
                    stringSpatial += "'" + wktWriter.getGeometryString() + "'";
                    geometryString = "'" + wktWriter.getGeometryString() + "'";
                }

            }
            stringSpatial += operatorBuild( "WITHIN", geometryString );

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
    private String operatorBuild( String operator, String stringSpatialGeom ) {
        String stringSpatial = "";

        stringSpatial += operator + "(" + stringSpatialPropertyName + ",";
        // TODO handling and counter if there are more stringSpatial
        stringSpatial += stringSpatialGeom + ")";

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

}
