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

import static org.deegree.record.persistence.MappingInfo.ColumnType.DATE;
import static org.deegree.record.persistence.MappingInfo.ColumnType.STRING;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.deegree.filter.Expression;
import org.deegree.filter.Operator;
import org.deegree.filter.expression.Literal;
import org.deegree.filter.expression.PropertyName;
import org.deegree.record.persistence.MappingInfo;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author: thomas $
 * 
 * @version $Revision: $, $Date: $
 */
public class ExpressionFilterHandling {
    
    private Set<String> table;

    private Set<String> column;
    
    private String expression;
    
    private static Map<String, MappingInfo> propToTableAndCol = new HashMap<String, MappingInfo>();

    static {
        propToTableAndCol.put( "Title", new MappingInfo( "isoqp_title", "title", STRING ) );
        propToTableAndCol.put( "Abstract", new MappingInfo( "isoqp_abstract", "abstract", STRING ) );
        propToTableAndCol.put( "BoundingBox", new MappingInfo( "isoqp_BoundingBox", "bbox", STRING ) );
        propToTableAndCol.put( "Type", new MappingInfo( "isoqp_type", "type", STRING ) );
        propToTableAndCol.put( "Format", new MappingInfo( "isoqp_format", "format", STRING ) );
        // propToTableAndCol.put( "Language", new MappingInfo( "datasets", "language", STRING ) );
        propToTableAndCol.put( "Subject", new MappingInfo( "isoqp_topiccategory", "topiccategory", STRING ) );
        propToTableAndCol.put( "AnyText", new MappingInfo( "datasets", "anytext", STRING ) );
        propToTableAndCol.put( "Identifier", new MappingInfo( "datasets", "identifier", STRING ) );
        propToTableAndCol.put( "apiso:identifier", new MappingInfo( "datasets", "identifier", STRING ) );
        propToTableAndCol.put( "Modified", new MappingInfo( "datasets", "modified", DATE ) );
        propToTableAndCol.put( "CRS", new MappingInfo( "isoqp_crs", "crs", STRING ) );
        propToTableAndCol.put( "Association", new MappingInfo( "isoqp_association", "relation", STRING ) );
        propToTableAndCol.put( "Source", new MappingInfo( "datasets", "source", STRING ) );

    }
    
    
    
    /**
     * Handles the {@link Expression} that is identified during the parsing of the {@link Operator}s
     * 
     * @param typeExpression
     * @param exp
     */
    public ExpressionFilterObject expressionFilterHandling( org.deegree.filter.Expression.Type typeExpression, Expression exp ) {
        
        table = new HashSet<String>();

        column = new HashSet<String>();
        
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

            for ( String s : propToTableAndCol.keySet() ) {
                if ( propertyName.getPropertyName().equals( s ) ) {
                    MappingInfo m = propToTableAndCol.get( s );

                    table.add( m.getTable() );
                    column.add( m.getColumn() );
                    expression = m.getTable() + "." + m.getColumn();

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
        return new ExpressionFilterObject(expression, table, column);
        
    }
    
    
    

}
