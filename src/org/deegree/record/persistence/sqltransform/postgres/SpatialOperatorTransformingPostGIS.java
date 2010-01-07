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
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashSet;
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
import org.deegree.geometry.io.DecimalCoordinateFormatter;
import org.deegree.geometry.io.WKTWriter;
import org.deegree.geometry.io.WKTWriter.WKTFlag;

/**
 * Transforms the spatial query into a PostGIS SQL statement. It encapsules the required methods.
 * <p>
 * TODO Codeoptimization
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author: thomas $
 * 
 * @version $Revision: $, $Date: $
 */
public class SpatialOperatorTransformingPostGIS {

    private ExpressionFilterHandling expressionFilterHandling = new ExpressionFilterHandling();

    private ExpressionFilterObject expressObject;

    private int counter;

    private Set<String> table = new HashSet<String>();

    private Set<String> column = new HashSet<String>();

    private SpatialOperator spaOp;

    private String stringSpatialPropertyName = "";

    private DecimalCoordinateFormatter decimalFormatter = new DecimalCoordinateFormatter( 2 );

    private Set<WKTFlag> flag = new HashSet<WKTFlag>();

    private WKTWriter wktWriter;

    /**
     * Writes the geometry that has been parsed
     */
    private Writer writerGeometry = new StringWriter();

    public SpatialOperatorTransformingPostGIS( SpatialOperator spaOp, Writer writer ) {
        this.spaOp = spaOp;

        try {

            doSpatialOperatorToPostGreSQL( writer );
        } catch ( IOException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

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
    private void doSpatialOperatorToPostGreSQL( Writer writerSpatial )
                            throws IOException {

        org.deegree.filter.spatial.SpatialOperator.SubType typeSpatial = spaOp.getSubType();

        switch ( typeSpatial ) {

        case BBOX:

            BBOX bboxOp = (BBOX) spaOp;
            Object[] paramsBBox = bboxOp.getParams();
            // flag.add( WKTFlag.USE_DKT );
            wktWriter = new WKTWriter( flag, decimalFormatter );

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
            break;
        // return writerSpatial.toString();

        case BEYOND:
            Beyond beyondOp = (Beyond) spaOp;
            Object[] paramsBeyond = beyondOp.getParams();
            writerSpatial.append( "DISTANCE(" );
            // flag.add( WKTFlag.USE_DKT );
            wktWriter = new WKTWriter( flag, decimalFormatter );

            counter = 0;
            for ( Object opParam : paramsBeyond ) {

                if ( opParam != beyondOp.getGeometry() ) {
                    counter++;
                    propertyNameBuild( writerSpatial, opParam );

                } else {
                    counter++;
                    wktWriter.writeGeometry( beyondOp.getGeometry(), writerGeometry );

                    writerSpatial.append( '\'' );
                    writerSpatial.append( writerGeometry.toString() );
                    writerSpatial.append( '\'' );

                }
                if ( counter < paramsBeyond.length ) {
                    writerSpatial.append( ',' );
                } else {
                    writerSpatial.append( ") <= " + beyondOp.getDistance().getValue().toString() + " AND " );
                }
            }
            operatorBuild( "DISJOINT", writerGeometry, writerSpatial );
            break;
        // return writerSpatial.toString();

        case CONTAINS:

            Contains containsOp = (Contains) spaOp;
            Object[] paramsContains = containsOp.getParams();
            wktWriter = new WKTWriter( flag, decimalFormatter );
            for ( Object opParam : paramsContains ) {

                if ( opParam != containsOp.getGeometry() ) {
                    propertyNameBuild( writerSpatial, opParam );

                } else {
                    wktWriter.writeGeometry( containsOp.getGeometry(), writerGeometry );

                    writerSpatial.append( '\'' );
                    writerSpatial.append( writerGeometry.toString() );
                    writerSpatial.append( '\'' );
                }

            }

            operatorBuild( "CONTAINS", writerGeometry, writerSpatial );
            System.out.println( writerSpatial.toString() );
            // return writerSpatial.toString();
            break;

        case CROSSES:
            Crosses crossesOp = (Crosses) spaOp;
            Object[] paramsCrosses = crossesOp.getParams();

            wktWriter = new WKTWriter( flag, decimalFormatter );
            for ( Object opParam : paramsCrosses ) {

                if ( opParam != crossesOp.getGeometry() ) {
                    propertyNameBuild( writerSpatial, opParam );

                } else {
                    wktWriter.writeGeometry( crossesOp.getGeometry(), writerGeometry );

                    writerSpatial.append( '\'' );
                    writerSpatial.append( writerGeometry.toString() );
                    writerSpatial.append( '\'' );
                }

            }

            operatorBuild( "CROSSES", writerGeometry, writerSpatial );
            System.out.println( writerSpatial.toString() );
            // return writerSpatial.toString();
            break;

        case DISJOINT:

            Disjoint disjointOp = (Disjoint) spaOp;
            Object[] paramsDisjoint = disjointOp.getParams();

            wktWriter = new WKTWriter( flag, decimalFormatter );
            for ( Object opParam : paramsDisjoint ) {

                if ( opParam != disjointOp.getGeometry() ) {
                    propertyNameBuild( writerSpatial, opParam );

                } else {
                    wktWriter.writeGeometry( disjointOp.getGeometry(), writerGeometry );

                    writerSpatial.append( '\'' );
                    writerSpatial.append( writerGeometry.toString() );
                    writerSpatial.append( '\'' );
                }

            }

            operatorBuild( "DISJOINT", writerGeometry, writerSpatial );
            System.out.println( writerSpatial.toString() );
            // return writerSpatial.toString();
            break;

        case DWITHIN:
            DWithin dWithinOp = (DWithin) spaOp;
            Object[] paramsDWithin = dWithinOp.getParams();
            writerSpatial.append( "DISTANCE(" );
            flag.add( WKTFlag.USE_DKT );
            wktWriter = new WKTWriter( flag, decimalFormatter );

            counter = 0;
            for ( Object opParam : paramsDWithin ) {

                if ( opParam != dWithinOp.getGeometry() ) {
                    counter++;
                    propertyNameBuild( writerSpatial, opParam );

                } else {
                    counter++;
                    wktWriter.writeGeometry( dWithinOp.getGeometry(), writerGeometry );

                    writerSpatial.append( '\'' );
                    writerSpatial.append( writerGeometry.toString() );
                    writerSpatial.append( '\'' );

                }
                if ( counter < paramsDWithin.length ) {
                    writerSpatial.append( ',' );
                } else {
                    writerSpatial.append( ") <= " + dWithinOp.getDistance().getValue().toString() + " AND " );
                }
            }

            operatorBuild( "DWITHIN", writerGeometry, writerSpatial );
            System.out.println( writerSpatial.toString() );
            // return writerSpatial.toString();
            break;

        case EQUALS:

            Equals equalsOp = (Equals) spaOp;
            Object[] paramsEquals = equalsOp.getParams();

            wktWriter = new WKTWriter( flag, decimalFormatter );
            for ( Object opParam : paramsEquals ) {

                if ( opParam != equalsOp.getGeometry() ) {
                    propertyNameBuild( writerSpatial, opParam );

                } else {
                    wktWriter.writeGeometry( equalsOp.getGeometry(), writerGeometry );

                    writerSpatial.append( '\'' );
                    writerSpatial.append( writerGeometry.toString() );
                    writerSpatial.append( '\'' );
                }

            }

            operatorBuild( "EQUALS", writerGeometry, writerSpatial );
            System.out.println( writerSpatial.toString() );
            // return writerSpatial.toString();
            break;

        case INTERSECTS:
            Intersects intersectsOp = (Intersects) spaOp;
            Object[] paramsIntersects = intersectsOp.getParams();

            wktWriter = new WKTWriter( flag, decimalFormatter );
            for ( Object opParam : paramsIntersects ) {

                if ( opParam != intersectsOp.getGeometry() ) {
                    propertyNameBuild( writerSpatial, opParam );

                } else {
                    wktWriter.writeGeometry( intersectsOp.getGeometry(), writerGeometry );

                    writerSpatial.append( '\'' );
                    writerSpatial.append( writerGeometry.toString() );
                    writerSpatial.append( '\'' );
                }

            }

            operatorBuild( "INTERSECTS", writerGeometry, writerSpatial );
            System.out.println( writerSpatial.toString() );
            // return writerSpatial.toString();
            break;

        case OVERLAPS:
            Overlaps overlapsOp = (Overlaps) spaOp;
            Object[] paramsOverlaps = overlapsOp.getParams();

            wktWriter = new WKTWriter( flag, decimalFormatter );
            for ( Object opParam : paramsOverlaps ) {

                if ( opParam != overlapsOp.getGeometry() ) {
                    propertyNameBuild( writerSpatial, opParam );

                } else {
                    wktWriter.writeGeometry( overlapsOp.getGeometry(), writerGeometry );

                    writerSpatial.append( '\'' );
                    writerSpatial.append( writerGeometry.toString() );
                    writerSpatial.append( '\'' );
                }

            }

            operatorBuild( "OVERLAPS", writerGeometry, writerSpatial );
            System.out.println( writerSpatial.toString() );
            // return writerSpatial.toString();
            break;

        case TOUCHES:
            Touches touchesOp = (Touches) spaOp;
            Object[] paramsTouches = touchesOp.getParams();

            wktWriter = new WKTWriter( flag, decimalFormatter );
            for ( Object opParam : paramsTouches ) {

                if ( opParam != touchesOp.getGeometry() ) {
                    propertyNameBuild( writerSpatial, opParam );

                } else {
                    wktWriter.writeGeometry( touchesOp.getGeometry(), writerGeometry );

                    writerSpatial.append( '\'' );
                    writerSpatial.append( writerGeometry.toString() );
                    writerSpatial.append( '\'' );
                }

            }

            operatorBuild( "TOUCHES", writerGeometry, writerSpatial );
            System.out.println( writerSpatial.toString() );
            // return writerSpatial.toString();
            break;

        case WITHIN:
            Within withinOp = (Within) spaOp;
            Object[] paramsWithin = withinOp.getParams();

            wktWriter = new WKTWriter( flag, decimalFormatter );
            for ( Object opParam : paramsWithin ) {

                if ( opParam != withinOp.getGeometry() ) {
                    propertyNameBuild( writerSpatial, opParam );

                } else {
                    wktWriter.writeGeometry( withinOp.getGeometry(), writerGeometry );

                    writerSpatial.append( '\'' );
                    writerSpatial.append( writerGeometry.toString() );
                    writerSpatial.append( '\'' );
                }

            }

            operatorBuild( "WITHIN", writerGeometry, writerSpatial );
            System.out.println( writerSpatial.toString() );
            // return writerSpatial.toString();
            break;

        }
        // return writerSpatial.toString();

    }

    /**
     * Building the SQL statement for the operator that is requested
     * 
     * @param operator
     * @return
     */
    private void operatorBuild( String operator, Writer writerGeometry, Writer writerSpatial ) {
        try {
            writerSpatial.append( operator + "(" + stringSpatialPropertyName + "," );
            writerSpatial.append( "\'" + writerGeometry.toString() + "\'" + ")" );
        } catch ( IOException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

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
