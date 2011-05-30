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
package org.deegree.filter.sql;

import java.util.Collections;
import java.util.List;

import org.deegree.commons.tom.sql.ParticleConverter;
import org.deegree.filter.expression.PropertyName;

/**
 * A {@link PropertyName} that's mapped to database column(s).
 * 
 * @see AbstractWhereBuilder
 *  
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public abstract class PropertyNameMapping {

    private final int sqlType;

    private final DBField valueField;

    private final List<Join> joins;

    // TODO
    private final ParticleConverter<?> converter = null;

    protected PropertyNameMapping( DBField valueField, int sqlType, List<Join> joins ) {
        this.valueField = valueField;
        this.sqlType = sqlType;
        if ( joins == null ) {
            this.joins = Collections.emptyList();
        } else {
            this.joins = joins;
        }
    }

    public DBField getTargetField() {
        return valueField;
    }

    public List<Join> getJoins() {
        return joins;
    }

    public ParticleConverter<?> getConverter() {
        return converter;
    }

    public int getSQLType() {
        return sqlType;
    }

    @Override
    public String toString() {
        String s = "";
        for ( Join join : joins ) {
            s += join;
            s += ",";
        }
        s += valueField;
        return s;
    }
}