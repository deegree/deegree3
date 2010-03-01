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
package org.deegree.record.persistence.sqltransform;

import java.util.HashSet;
import java.util.Set;

import javax.xml.namespace.QName;

import org.deegree.filter.Expression;
import org.deegree.filter.Operator;
import org.deegree.filter.expression.Literal;
import org.deegree.filter.expression.PropertyName;
import org.deegree.record.persistence.MappingInfo;
import org.deegree.record.persistence.Profile_DB_Mappings;
import org.deegree.record.persistence.genericrecordstore.ISO_DC_Mappings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles the expression that is identified. This means the building of the tables and the columns that has to be used
 * and the expression that can be put to the database.<br>
 * If there is anywhere a <Code>null</Code> expression returned then proof the input parameter (element 'PropertyName'
 * in the filterexpression), firstly. If there is no matching between this paramater and any parameter in the
 * {@link ISO_DC_Mappings} <Code>null</Code> will return.
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author: thomas $
 * 
 * @version $Revision: $, $Date: $
 */
public class ExpressionFilterHandling {

    private static final Logger LOG = LoggerFactory.getLogger( ExpressionFilterHandling.class );

    private Set<String> table;

    private Set<String> column;

    private String expression;

    private QName propName;

    private Profile_DB_Mappings iso_dc_mapping;

    // private PostGISMapping iso_dc_mapping;

    /**
     * Handles the {@link Expression} that is identified during the parsing of the {@link Operator}s
     * 
     * @param typeExpression
     * @param exp
     * @return {@link ExpressionFilterObject}
     */
    public ExpressionFilterObject expressionFilterHandling( org.deegree.filter.Expression.Type typeExpression,
                                                            Expression exp ) {

        table = new HashSet<String>();

        column = new HashSet<String>();

        iso_dc_mapping = new ISO_DC_Mappings();

        boolean isMatching = false;

        switch ( typeExpression ) {

        case ADD:
            // TODO
            break;

        case SUB:
            // TODO
            break;

        case MUL:
            // TODO
            break;

        case DIV:
            // TODO
            break;

        case PROPERTY_NAME:
            PropertyName propertyName = (PropertyName) exp;

            for ( String s : iso_dc_mapping.getPropToTableAndCol().keySet() ) {
                if ( propertyName.getPropertyName().equals( s ) ) {
                    MappingInfo m = iso_dc_mapping.getPropToTableAndCol().get( s );
                    propName = propertyName.getAsQName();
                    table.add( m.getTables() );
                    column.add( m.getColumn() );
                    expression = m.getTables() + "." + m.getColumn();
                    isMatching = true;
                }
            }

            break;

        case LITERAL:
            Literal<?> literal = (Literal<?>) exp;
            String value = "'" + literal.getValue().toString() + "'";
            expression = value;
            break;
        case FUNCTION:
            // TODO
            break;

        }

        return new ExpressionFilterObject( expression, table, column, propName, isMatching );

    }

}
