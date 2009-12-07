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

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
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
import org.deegree.geometry.WKTWriterNG;
import org.deegree.geometry.WKTWriterNG.WKTFlag;

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

    WKTWriterNG wktWriter;
    

    public SpatialOperatorTransformingPostGres( SpatialOperator spaOp ) {
        this.spaOp = spaOp;

        try {
            spatialOperation = doSpatialOperatorToPostGreSQL();
        } catch ( IOException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

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
     * @throws IOException 
     */
    private String doSpatialOperatorToPostGreSQL() throws IOException {

        org.deegree.filter.spatial.SpatialOperator.SubType typeSpatial = spaOp.getSubType();

        Writer writer = new StringWriter();
        
        Writer writerSpatial = new StringWriter();

        table = new HashSet<String>();

        column = new HashSet<String>();

        stringSpatialPropertyName = "";

        stringSpatialGeometry = "";

        StringBuilder geometryString = new StringBuilder();

        Set<WKTFlag> flag;

        switch ( typeSpatial ) {

        case BBOX:

            BBOX bboxOp = (BBOX) spaOp;
            Object[] paramsBBox = bboxOp.getParams();

            stringSpatialGeom = new LinkedList<String>();
            flag = new HashSet<WKTFlag>();

            wktWriter = new WKTWriterNG( flag, writerSpatial );

            for ( Object opParam : paramsBBox ) {
                if ( opParam != bboxOp.getBoundingBox() ) {
                    propertyNameBuild( writerSpatial, opParam );
                    writerSpatial.append( " && " );
                } else {
                    writerSpatial.append( '\'' );
                    wktWriter.writeGeometry( bboxOp.getBoundingBox(), writerSpatial );
                    writerSpatial.append( '\'' );
                }

            }

            return writerSpatial.toString();

        case BEYOND:
            Beyond beyondOp = (Beyond) spaOp;
            Object[] paramsBeyond = beyondOp.getParams();
            writerSpatial.append( "DISTANCE(" );
            stringSpatialGeom = new LinkedList<String>();
            flag = new HashSet<WKTFlag>();
            flag.add( WKTFlag.USE_DKT );
            
            wktWriter = new WKTWriterNG( flag, writerSpatial );

            counter = 0;

            for ( Object opParam : paramsBeyond ) {

                if ( opParam != beyondOp.getGeometry() ) {
                    counter++;
                    propertyNameBuild( writerSpatial, opParam );

                } else {
                    counter++;
                    wktWriter.writeGeometry( beyondOp.getGeometry(), writer ); 

                    writerSpatial.append( '\'' );
                    writerSpatial.append( writer.toString() );
                    writerSpatial.append( '\'' );

                    geometryString.append( '\'' );
                    geometryString.append( writer.toString() );
                    geometryString.append( '\'' );

                }
                if ( counter < paramsBeyond.length ) {
                    writerSpatial.append( ',' );
                } else {
                    writerSpatial.append( ") <= " + beyondOp.getDistance().getValue().toString() + " AND " );
                }
            }
            operatorBuild( "DISJOINT", geometryString, writerSpatial );

            return writerSpatial.toString();
/*
        case CONTAINS:

            Contains containsOp = (Contains) spaOp;
            Object[] paramsContains = containsOp.getParams();

            counter = 0;
            stringSpatialGeom = new LinkedList<String>();

            for ( Object opParam : paramsContains ) {

                if ( opParam != containsOp.getGeometry() ) {
                    counter++;
                    propertyNameBuild( stringSpatial, opParam );

                } else {
                    counter++;

                    stringSpatial.append( '\'' );
                    stringSpatial.append( wktWriter.writeGeometry( containsOp.getGeometry() ) );
                    stringSpatial.append( '\'' );

                    geometryString.append( '\'' );
                    geometryString.append( wktWriter.writeGeometry( containsOp.getGeometry() ) );
                    geometryString.append( '\'' );
                }

            }

            operatorBuild( "CONTAINS", geometryString, stringSpatial );
            return stringSpatial.toString();

        case CROSSES:
            Crosses crossesOp = (Crosses) spaOp;
            Object[] paramsCrosses = crossesOp.getParams();

            counter = 0;
            stringSpatialGeom = new LinkedList<String>();

            for ( Object opParam : paramsCrosses ) {

                if ( opParam != crossesOp.getGeometry() ) {
                    counter++;
                    propertyNameBuild( stringSpatial, opParam );

                } else {
                    counter++;

                    stringSpatial.append( '\'' );
                    stringSpatial.append( wktWriter.writeGeometry( crossesOp.getGeometry() ) );
                    stringSpatial.append( '\'' );

                    geometryString.append( '\'' );
                    geometryString.append( wktWriter.writeGeometry( crossesOp.getGeometry() ) );
                    geometryString.append( '\'' );
                }

            }

            operatorBuild( "CROSSES", geometryString, stringSpatial );

            return stringSpatial.toString();

        case DISJOINT:

            Disjoint disjointOp = (Disjoint) spaOp;
            Object[] paramsDisjoint = disjointOp.getParams();

            counter = 0;
            stringSpatialGeom = new LinkedList<String>();

            for ( Object opParam : paramsDisjoint ) {

                if ( opParam != disjointOp.getGeometry() ) {
                    counter++;
                    propertyNameBuild( stringSpatial, opParam );

                } else {
                    counter++;

                    stringSpatial.append( '\'' );
                    stringSpatial.append( wktWriter.writeGeometry( disjointOp.getGeometry() ) );
                    stringSpatial.append( '\'' );

                    geometryString.append( '\'' );
                    geometryString.append( wktWriter.writeGeometry( disjointOp.getGeometry() ) );
                    geometryString.append( '\'' );
                }

            }

            operatorBuild( "DISJOINT", geometryString, stringSpatial );
            return stringSpatial.toString();

        case DWITHIN:
            DWithin dWithinOp = (DWithin) spaOp;
            Object[] paramsDWithin = dWithinOp.getParams();
            stringSpatial.append( "DISTANCE(" );
            stringSpatialGeom = new LinkedList<String>();

            counter = 0;

            for ( Object opParam : paramsDWithin ) {

                if ( opParam != dWithinOp.getGeometry() ) {
                    counter++;
                    propertyNameBuild( stringSpatial, opParam );

                } else {
                    counter++;

                    stringSpatial.append( '\'' );
                    stringSpatial.append( wktWriter.writeGeometry( dWithinOp.getGeometry() ) );
                    stringSpatial.append( '\'' );

                    geometryString.append( '\'' );
                    geometryString.append( wktWriter.writeGeometry( dWithinOp.getGeometry() ) );
                    geometryString.append( '\'' );
                }
                if ( counter < paramsDWithin.length ) {
                    stringSpatial.append( ',' );
                } else {
                    stringSpatial.append( ") <= " + dWithinOp.getDistance().getValue().toString() + " AND " );
                }
            }

            operatorBuild( "DWITHIN", geometryString, stringSpatial );
            return stringSpatial.toString();

        case EQUALS:

            Equals equalsOp = (Equals) spaOp;
            Object[] paramsEquals = equalsOp.getParams();

            counter = 0;
            stringSpatialGeom = new LinkedList<String>();

            for ( Object opParam : paramsEquals ) {

                if ( opParam != equalsOp.getGeometry() ) {
                    counter++;
                    propertyNameBuild( stringSpatial, opParam );

                } else {
                    counter++;

                    stringSpatial.append( '\'' );
                    stringSpatial.append( wktWriter.writeGeometry( equalsOp.getGeometry() ) );
                    stringSpatial.append( '\'' );

                    geometryString.append( '\'' );
                    geometryString.append( wktWriter.writeGeometry( equalsOp.getGeometry() ) );
                    geometryString.append( '\'' );
                }

            }

            operatorBuild( "EQUALS", geometryString, stringSpatial );
            return stringSpatial.toString();

        case INTERSECTS:
            Intersects intersectsOp = (Intersects) spaOp;
            Object[] paramsIntersects = intersectsOp.getParams();

            counter = 0;
            stringSpatialGeom = new LinkedList<String>();

            for ( Object opParam : paramsIntersects ) {

                if ( opParam != intersectsOp.getGeometry() ) {
                    counter++;
                    propertyNameBuild( stringSpatial, opParam );

                } else {
                    counter++;

                    stringSpatial.append( '\'' );
                    stringSpatial.append( wktWriter.writeGeometry( intersectsOp.getGeometry() ) );
                    stringSpatial.append( '\'' );

                    geometryString.append( '\'' );
                    geometryString.append( wktWriter.writeGeometry( intersectsOp.getGeometry() ) );
                    geometryString.append( '\'' );
                }

            }

            operatorBuild( "INTERSECTS", geometryString, stringSpatial );
            return stringSpatial.toString();

        case OVERLAPS:
            Overlaps overlapsOp = (Overlaps) spaOp;
            Object[] paramsOverlaps = overlapsOp.getParams();

            counter = 0;
            stringSpatialGeom = new LinkedList<String>();

            for ( Object opParam : paramsOverlaps ) {

                if ( opParam != overlapsOp.getGeometry() ) {
                    counter++;
                    propertyNameBuild( stringSpatial, opParam );

                } else {
                    counter++;

                    stringSpatial.append( '\'' );
                    stringSpatial.append( wktWriter.writeGeometry( overlapsOp.getGeometry() ) );
                    stringSpatial.append( '\'' );

                    geometryString.append( '\'' );
                    geometryString.append( wktWriter.writeGeometry( overlapsOp.getGeometry() ) );
                    geometryString.append( '\'' );
                }

            }

            operatorBuild( "OVERLAPS", geometryString, stringSpatial );
            return stringSpatial.toString();

        case TOUCHES:
            Touches touchesOp = (Touches) spaOp;
            Object[] paramsTouches = touchesOp.getParams();

            counter = 0;
            stringSpatialGeom = new LinkedList<String>();

            for ( Object opParam : paramsTouches ) {

                if ( opParam != touchesOp.getGeometry() ) {
                    counter++;
                    propertyNameBuild( stringSpatial, opParam );

                } else {
                    counter++;

                    stringSpatial.append( '\'' );
                    stringSpatial.append( wktWriter.writeGeometry( touchesOp.getGeometry() ) );
                    stringSpatial.append( '\'' );

                    geometryString.append( '\'' );
                    geometryString.append( wktWriter.writeGeometry( touchesOp.getGeometry() ) );
                    geometryString.append( '\'' );
                }

            }

            operatorBuild( "TOUCHES", geometryString, stringSpatial );
            return stringSpatial.toString();

        case WITHIN:
            Within withinOp = (Within) spaOp;
            Object[] paramsWithin = withinOp.getParams();

            counter = 0;
            stringSpatialGeom = new LinkedList<String>();

            for ( Object opParam : paramsWithin ) {

                if ( opParam != withinOp.getGeometry() ) {
                    counter++;
                    propertyNameBuild( stringSpatial, opParam );

                } else {
                    counter++;

                    stringSpatial.append( '\'' );
                    stringSpatial.append( wktWriter.writeGeometry( withinOp.getGeometry() ) );
                    stringSpatial.append( '\'' );

                    geometryString.append( '\'' );
                    geometryString.append( wktWriter.writeGeometry( withinOp.getGeometry() ) );
                    geometryString.append( '\'' );
                }

            }

            operatorBuild( "WITHIN", geometryString, stringSpatial );
            return stringSpatial.toString();
*/
        }
        return writerSpatial.toString();

    }

    /**
     * Building the SQL statement for the operator that is requested
     * 
     * @param operator
     * @return
     */
    private void operatorBuild( String operator, StringBuilder stringSpatialGeom, Writer writerSpatial ) {
        try {
            writerSpatial.append( operator + "(" + stringSpatialPropertyName + "," );
            writerSpatial.append( stringSpatialGeom + ")" );
        } catch ( IOException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // TODO handling and counter if there are more stringSpatial
        
    }

    /**
     * Building the SQL statement for the propertyName
     * 
     * @param opParam
     * @return
     */
    private void propertyNameBuild( Writer writerSpatial, Object opParam ) {

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
        try {
            writerSpatial.append( stringSpatialPropertyName );
        } catch ( IOException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

}
