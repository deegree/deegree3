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

import static org.slf4j.LoggerFactory.getLogger;

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
import org.deegree.record.persistence.sqltransform.ExpressionFilterHandling;
import org.deegree.record.persistence.sqltransform.ExpressionFilterObject;
import org.slf4j.Logger;

/**
 * Transforms the spatial query into a PostGIS SQL statement. It encapsulates the required methods.
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author: thomas $
 * 
 * @version $Revision: $, $Date: $
 */
public class SpatialOperatorTransformingPostGIS {

    private static final Logger LOG = getLogger( SpatialOperatorTransformingPostGIS.class );

    private Set<String> tables;

    private Set<String> columns;

    private DecimalCoordinateFormatter decimalFormatter;

    private SpatialOperator spaOp;

    private Set<WKTFlag> flag;

    private WKTWriter wktWriter;

    private Writer stringSpatialProperty;

    private Writer writerGeometry;

    private Writer writerSpatial;

    /**
     * Creates a new {@link SpatialOperatorTransformingPostGIS} instance.
     * 
     * @param spaOp
     *            the spatialOperator that is requested in the filter-constraint
     * @param writer
     *            the writer to write the output to
     */
    public SpatialOperatorTransformingPostGIS( SpatialOperator spaOp ) {
        this.spaOp = spaOp;

        try {

            writerSpatial = doSpatialOperatorToPostGreSQL();

        } catch ( IOException e ) {

            e.printStackTrace();
        }

    }

    /**
     * @return the table
     */
    public Set<String> getTables() {
        return tables;
    }

    /**
     * @return the column
     */
    public Set<String> getColumns() {
        return columns;
    }

    /**
     * @return the writerSpatial
     */
    public Writer getWriterSpatial() {
        return writerSpatial;
    }

    /**
     * Building the SQL statement for the requested spatial query
     * 
     * @throws IOException
     */
    private Writer doSpatialOperatorToPostGreSQL()
                            throws IOException {
        Writer writer = new StringWriter( 200 );
        decimalFormatter = new DecimalCoordinateFormatter( 2 );
        flag = new HashSet<WKTFlag>();
        stringSpatialProperty = new StringWriter();
        int counter;
        writerGeometry = new StringWriter( 300 );

        org.deegree.filter.spatial.SpatialOperator.SubType typeSpatial = spaOp.getSubType();

        switch ( typeSpatial ) {

        case BBOX:

            BBOX bboxOp = (BBOX) spaOp;
            Object[] paramsBBox = bboxOp.getParams();
            // flag.add( WKTFlag.USE_DKT );
            wktWriter = new WKTWriter( flag, decimalFormatter );

            for ( Object opParam : paramsBBox ) {
                if ( opParam != bboxOp.getBoundingBox() ) {

                    writer.append( propertyNameBuild( opParam ).toString() );
                    writer.append( " && " );
                } else {
                    writer.append( '\'' );
                    wktWriter.writeGeometry( bboxOp.getBoundingBox(), writer );
                    writer.append( '\'' );
                }

            }
            break;

        case BEYOND:
            Beyond beyondOp = (Beyond) spaOp;
            Object[] paramsBeyond = beyondOp.getParams();
            writer.append( "DISTANCE(" );
            // flag.add( WKTFlag.USE_DKT );
            wktWriter = new WKTWriter( flag, decimalFormatter );

            counter = 0;
            for ( Object opParam : paramsBeyond ) {

                if ( opParam != beyondOp.getGeometry() ) {
                    counter++;
                    stringSpatialProperty = propertyNameBuild( opParam );
                    writer.append( stringSpatialProperty.toString() );
                } else {
                    counter++;
                    wktWriter.writeGeometry( beyondOp.getGeometry(), writerGeometry );

                    writer.append( '\'' );
                    writer.append( writerGeometry.toString() );
                    writer.append( '\'' );

                }
                if ( counter < paramsBeyond.length ) {
                    writer.append( ',' );
                } else {
                    writer.append( ") <= " + beyondOp.getDistance().getValue().toString() + " AND " );
                }
            }
            operatorBuild( "DISJOINT", writerGeometry, writer, stringSpatialProperty );
            break;

        case CONTAINS:

            Contains containsOp = (Contains) spaOp;
            Object[] paramsContains = containsOp.getParams();
            wktWriter = new WKTWriter( flag, decimalFormatter );
            for ( Object opParam : paramsContains ) {

                if ( opParam != containsOp.getGeometry() ) {
                    propertyNameBuild( opParam );
                    stringSpatialProperty = propertyNameBuild( opParam );
                } else {
                    wktWriter.writeGeometry( containsOp.getGeometry(), writerGeometry );

                }

            }

            operatorBuild( "CONTAINS", writerGeometry, writer, stringSpatialProperty );
            LOG.debug( writer.toString() );

            break;

        case CROSSES:
            Crosses crossesOp = (Crosses) spaOp;
            Object[] paramsCrosses = crossesOp.getParams();

            wktWriter = new WKTWriter( flag, decimalFormatter );
            for ( Object opParam : paramsCrosses ) {

                if ( opParam != crossesOp.getGeometry() ) {
                    propertyNameBuild( opParam );
                    stringSpatialProperty = propertyNameBuild( opParam );
                } else {
                    wktWriter.writeGeometry( crossesOp.getGeometry(), writerGeometry );

                }

            }

            operatorBuild( "CROSSES", writerGeometry, writer, stringSpatialProperty );
            LOG.debug( writer.toString() );

            break;

        case DISJOINT:

            Disjoint disjointOp = (Disjoint) spaOp;
            Object[] paramsDisjoint = disjointOp.getParams();

            wktWriter = new WKTWriter( flag, decimalFormatter );
            for ( Object opParam : paramsDisjoint ) {

                if ( opParam != disjointOp.getGeometry() ) {
                    propertyNameBuild( opParam );
                    stringSpatialProperty = propertyNameBuild( opParam );
                } else {
                    wktWriter.writeGeometry( disjointOp.getGeometry(), writerGeometry );

                }

            }

            operatorBuild( "DISJOINT", writerGeometry, writer, stringSpatialProperty );
            LOG.debug( writer.toString() );

            break;

        case DWITHIN:
            DWithin dWithinOp = (DWithin) spaOp;
            Object[] paramsDWithin = dWithinOp.getParams();
            writer.append( "DISTANCE(" );
            flag.add( WKTFlag.USE_DKT );
            wktWriter = new WKTWriter( flag, decimalFormatter );

            counter = 0;
            for ( Object opParam : paramsDWithin ) {

                if ( opParam != dWithinOp.getGeometry() ) {
                    counter++;
                    stringSpatialProperty = propertyNameBuild( opParam );
                    writer.append( stringSpatialProperty.toString() );
                } else {
                    counter++;
                    wktWriter.writeGeometry( dWithinOp.getGeometry(), writerGeometry );

                    writer.append( '\'' );
                    writer.append( writerGeometry.toString() );
                    writer.append( '\'' );
                }
                if ( counter < paramsDWithin.length ) {
                    writer.append( ',' );
                } else {
                    writer.append( ") <= " + dWithinOp.getDistance().getValue().toString() + " AND " );
                }
            }

            operatorBuild( "DWITHIN", writerGeometry, writer, stringSpatialProperty );
            LOG.debug( writer.toString() );

            break;

        case EQUALS:

            Equals equalsOp = (Equals) spaOp;
            Object[] paramsEquals = equalsOp.getParams();

            wktWriter = new WKTWriter( flag, decimalFormatter );
            for ( Object opParam : paramsEquals ) {

                if ( opParam != equalsOp.getGeometry() ) {
                    propertyNameBuild( opParam );
                    stringSpatialProperty = propertyNameBuild( opParam );
                } else {
                    wktWriter.writeGeometry( equalsOp.getGeometry(), writerGeometry );

                }

            }

            operatorBuild( "EQUALS", writerGeometry, writer, stringSpatialProperty );
            LOG.debug( writer.toString() );

            break;

        case INTERSECTS:
            Intersects intersectsOp = (Intersects) spaOp;
            Object[] paramsIntersects = intersectsOp.getParams();

            wktWriter = new WKTWriter( flag, decimalFormatter );
            for ( Object opParam : paramsIntersects ) {

                if ( opParam != intersectsOp.getGeometry() ) {
                    propertyNameBuild( opParam );
                    stringSpatialProperty = propertyNameBuild( opParam );
                } else {
                    wktWriter.writeGeometry( intersectsOp.getGeometry(), writerGeometry );

                }

            }

            operatorBuild( "INTERSECTS", writerGeometry, writer, stringSpatialProperty );
            LOG.debug( writer.toString() );

            break;

        case OVERLAPS:
            Overlaps overlapsOp = (Overlaps) spaOp;
            Object[] paramsOverlaps = overlapsOp.getParams();

            wktWriter = new WKTWriter( flag, decimalFormatter );
            for ( Object opParam : paramsOverlaps ) {

                if ( opParam != overlapsOp.getGeometry() ) {
                    propertyNameBuild( opParam );
                    stringSpatialProperty = propertyNameBuild( opParam );
                } else {
                    wktWriter.writeGeometry( overlapsOp.getGeometry(), writerGeometry );

                }

            }

            operatorBuild( "OVERLAPS", writerGeometry, writer, stringSpatialProperty );
            LOG.debug( writer.toString() );

            break;

        case TOUCHES:
            Touches touchesOp = (Touches) spaOp;
            Object[] paramsTouches = touchesOp.getParams();

            wktWriter = new WKTWriter( flag, decimalFormatter );
            for ( Object opParam : paramsTouches ) {

                if ( opParam != touchesOp.getGeometry() ) {
                    propertyNameBuild( opParam );
                    stringSpatialProperty = propertyNameBuild( opParam );
                } else {
                    wktWriter.writeGeometry( touchesOp.getGeometry(), writerGeometry );

                }

            }

            operatorBuild( "TOUCHES", writerGeometry, writer, stringSpatialProperty );
            LOG.debug( writer.toString() );

            break;

        case WITHIN:
            Within withinOp = (Within) spaOp;
            Object[] paramsWithin = withinOp.getParams();

            wktWriter = new WKTWriter( flag, decimalFormatter );
            for ( Object opParam : paramsWithin ) {

                if ( opParam != withinOp.getGeometry() ) {
                    propertyNameBuild( opParam );
                    stringSpatialProperty = propertyNameBuild( opParam );
                } else {
                    wktWriter.writeGeometry( withinOp.getGeometry(), writerGeometry );

                }

            }

            operatorBuild( "WITHIN", writerGeometry, writer, stringSpatialProperty );
            LOG.debug( writer.toString() );

            break;

        }

        return writer;

    }

    /**
     * Building the SQL statement for the operator that is requested
     * 
     * @param operator
     * @param writerGeometry
     * @param writerSpatial
     * @param stringSpatialProperty
     */
    private void operatorBuild( String operator, Writer writerGeometry, Writer writerSpatial,
                                Writer stringSpatialProperty ) {
        try {
            writerSpatial.append( operator + "(" + stringSpatialProperty + "," );
            writerSpatial.append( "\'" + writerGeometry.toString() + "\'" + ")" );
        } catch ( IOException e ) {

            e.printStackTrace();
        }

    }

    /**
     * Building the SQL statement for the propertyName
     * 
     * @param opParam
     */
    private StringWriter propertyNameBuild( Object opParam ) {

        StringWriter stringSpatialPropertyName = new StringWriter( 100 );
        ExpressionFilterHandling expressionFilterHandling = new ExpressionFilterHandling();

        String exp = ( (PropertyName) opParam ).getPropertyName();

        stringSpatialPropertyName.append( "GeomFromText(AsText(" );

        ExpressionFilterObject expressObject = expressionFilterHandling.expressionFilterHandling(
                                                                                                  ( (PropertyName) opParam ).getType(),
                                                                                                  new PropertyName(
                                                                                                                    exp,
                                                                                                                    ( (PropertyName) opParam ).getNsContext() ) );

        stringSpatialPropertyName.append( expressObject.getExpression() );
        tables = expressObject.getTables();
        columns = expressObject.getColumns();
        stringSpatialPropertyName.append( "))" );
        return stringSpatialPropertyName;

    }

}
