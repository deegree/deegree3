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

import java.util.Set;

import javax.xml.namespace.QName;

/**
 * Object that encapsules methods for the expression of a filter
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author: thomas $
 * 
 * @version $Revision: $, $Date: $
 */
public class ExpressionFilterObject {
    
    private String expression;
    
    private Set<String> table;
    
    private Set<String> column;
    
    private QName propName;
    
    private boolean isMatching;
    
    public ExpressionFilterObject(String expression, Set<String> table, Set<String> column, QName propName, boolean isMatching){
        this.expression = expression;
        this.table = table;
        this.column = column;
        this.propName = propName;
        this.isMatching = isMatching;
    }

    /**
     * @return the expression
     */
    public String getExpression() {
        return expression;
    }

    /**
     * @param expression the expression to set
     */
    public void setExpression( String expression ) {
        this.expression = expression;
    }

    /**
     * @return the table
     */
    public Set<String> getTable() {
        return table;
    }

    /**
     * @param table the table to set
     */
    public void setTable( Set<String> table ) {
        this.table = table;
    }

    /**
     * @return the column
     */
    public Set<String> getColumn() {
        return column;
    }

    /**
     * @param column the column to set
     */
    public void setColumn( Set<String> column ) {
        this.column = column;
    }

    /**
     * @return the propName
     */
    public QName getPropName() {
        return propName;
    }

    /**
     * @param propName the propName to set
     */
    public void setPropName( QName propName ) {
        this.propName = propName;
    }

    /**
     * @return the isMatching
     */
    public boolean isMatching() {
        return isMatching;
    }

    /**
     * @param isMatching the isMatching to set
     */
    public void setMatching( boolean isMatching ) {
        this.isMatching = isMatching;
    }
    
    
    

}
