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

import static org.deegree.commons.tom.primitive.PrimitiveType.STRING;

import java.util.Collections;
import java.util.List;

import org.deegree.commons.tom.primitive.PrimitiveType;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.filter.expression.PropertyName;

/**
 * Represents a {@link PropertyName} that's mapped to a relational model.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class PropertyNameMapping {

    private final DBField valueField;

    private final List<Join> joins;

    private final PrimitiveType pt;

    private final ICRS crs;

    private final String srid;

    private final boolean isConcatenated;

    public PropertyNameMapping( String table, String column, ICRS crs, String srid ) {
        this.valueField = new DBField( table, column );
        this.joins = Collections.emptyList();
        this.pt = STRING;
        this.crs = crs;
        this.srid = srid;
        isConcatenated = false;
    }

    /**
     * @param aliasManager
     * @param valueField
     * @param joins
     * @param crs
     * @param srid
     */
    public PropertyNameMapping( TableAliasManager aliasManager, DBField valueField, List<Join> joins, ICRS crs,
                                String srid, boolean isConcatenated ) {
        this.valueField = valueField;
        this.joins = joins;
        this.pt = STRING;
        this.crs = crs;
        this.srid = srid;
        this.isConcatenated = isConcatenated;

        String currentAlias = aliasManager.getRootTableAlias();
        if ( joins != null ) {
            for ( Join join : joins ) {
                join.getFrom().setAlias( currentAlias );
                currentAlias = aliasManager.generateNew();
                join.getTo().setAlias( currentAlias );
            }
        }
        valueField.setAlias( currentAlias );
    }

    public ICRS getCRS() {
        return crs;
    }

    public String getSRID() {
        return srid;
    }

    public DBField getTargetField() {
        return valueField;
    }

    public PrimitiveType getTargetFieldType() {
        return pt;
    }

    public List<Join> getJoins() {
        return joins;
    }

    public int getSQLType() {
        return -1;
    }

    public boolean isSpatial() {
        return true;
    }

    public boolean isConcatenated() {
        return isConcatenated;
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