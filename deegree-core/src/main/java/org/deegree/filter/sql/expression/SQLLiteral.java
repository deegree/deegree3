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
package org.deegree.filter.sql.expression;

import java.sql.Types;
import java.util.Collections;
import java.util.List;

import org.deegree.commons.tom.primitive.PrimitiveValue;
import org.deegree.cs.CRS;
import org.deegree.filter.expression.Literal;
import org.deegree.filter.sql.UnmappableException;
import org.deegree.geometry.Geometry;

/**
 * {@link SQLExpression} that represents a constant value, e.g. a string, a number or a geometry.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class SQLLiteral implements SQLExpression {

    private int sqlType;

    private boolean isSpatial;

    private Object value;

    public SQLLiteral( Geometry geom ) {
        this.value = geom;
        this.sqlType = Types.OTHER;
        this.isSpatial = true;
    }

    public SQLLiteral( Object value, int sqlType ) {
        this.value = value;
        this.sqlType = sqlType;
        this.isSpatial = false;
    }

    public SQLLiteral( Literal<?> literal ) throws UnmappableException {

        this.value = literal.getValue();        
        if ( value != null ) {
            if ( value instanceof PrimitiveValue ) {
                // TODO what about differentiating column types?
                value = ( (PrimitiveValue) value ).getAsText();
            } else {
                throw new UnmappableException( "Unhandled literal content '" + value.getClass() + "'" );
            }
        }
        
        this.sqlType = -1;
        this.isSpatial = false;
    }

    @Override
    public int getSQLType() {
        return sqlType;
    }

    public Object getValue() {
        return value;
    }
    
    @Override
    public boolean isSpatial() {
        return isSpatial;
    }

    @Override
    public String toString() {
        return "'" + value + "'";
    }

    @Override
    public List<SQLLiteral> getLiterals() {
        return Collections.singletonList( this );
    }

    @Override
    public StringBuilder getSQL() {
        return new StringBuilder ("?");
    }

    @Override
    public CRS getSRS() {
        return isSpatial ? ((Geometry) value).getCoordinateSystem() : null;
    }
}
