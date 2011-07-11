//$HeadURL: svn+ssh://lbuesching@svn.wald.intevation.de/deegree/base/trunk/resources/eclipse/files_template.xml $
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
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
package org.deegree.metadata.iso.persistence;

import org.deegree.commons.jdbc.ConnectionManager.Type;
import org.deegree.filter.sql.AbstractWhereBuilder;
import org.deegree.filter.sql.Join;
import org.deegree.filter.sql.PropertyNameMapping;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:goltz@lat-lon.org">Lyn Goltz</a>
 * @author last edited by: $Author: lyn $
 * 
 * @version $Revision: $, $Date: $
 */
abstract class SqlHelper {

    protected String idColumn;

    protected String fileIdColumn;

    protected String recordColumn;

    protected String fk_main;

    protected Type connectionType;

    protected String mainTable;

    protected String crsTable;

    protected String keywordTable;

    protected String constraintTable;

    protected String opOnTable;

    SqlHelper( Type connectionType ) {
        this.connectionType = connectionType;
        idColumn = ISOPropertyNameMapper.CommonColumnNames.id.name();
        fk_main = ISOPropertyNameMapper.CommonColumnNames.fk_main.name();
        recordColumn = ISOPropertyNameMapper.CommonColumnNames.recordfull.name();
        fileIdColumn = ISOPropertyNameMapper.CommonColumnNames.fileidentifier.name();
        mainTable = ISOPropertyNameMapper.DatabaseTables.idxtb_main.name();
        crsTable = ISOPropertyNameMapper.DatabaseTables.idxtb_crs.name();
        keywordTable = ISOPropertyNameMapper.DatabaseTables.idxtb_keyword.name();
        opOnTable = ISOPropertyNameMapper.DatabaseTables.idxtb_operatesondata.name();
        constraintTable = ISOPropertyNameMapper.DatabaseTables.idxtb_constraint.name();
    }

    protected StringBuilder getPreparedStatementDatasetIDs( AbstractWhereBuilder builder ) {

        StringBuilder getDatasetIDs = new StringBuilder( 300 );
        String rootTableAlias = builder.getAliasManager().getRootTableAlias();
        getDatasetIDs.append( "SELECT DISTINCT " );
        getDatasetIDs.append( rootTableAlias );
        getDatasetIDs.append( '.' );
        getDatasetIDs.append( idColumn );

        // for SELECT DISTINCT, all ORDER BY columns have to be SELECTed as well
        if ( builder.getOrderBy() != null ) {
            // hack to re-use the ORDER BY column list 
            String orderByClause = builder.getOrderBy().getSQL().toString().replaceAll( " ASC| DESC", "" );
            getDatasetIDs.append( ',' );
            getDatasetIDs.append( orderByClause );
        }

        return getDatasetIDs;
    }

    protected void getPSBody( AbstractWhereBuilder builder, StringBuilder getDatasetIDs ) {

        String rootTableAlias = builder.getAliasManager().getRootTableAlias();
        getDatasetIDs.append( " FROM " );
        getDatasetIDs.append( mainTable );
        getDatasetIDs.append( " " );
        getDatasetIDs.append( rootTableAlias );

        for ( PropertyNameMapping mappedPropName : builder.getMappedPropertyNames() ) {
            for ( Join join : mappedPropName.getJoins() ) {
                getDatasetIDs.append( " LEFT OUTER JOIN " );
                getDatasetIDs.append( join.getToTable() );
                getDatasetIDs.append( ' ' );
                getDatasetIDs.append( join.getToTableAlias() );
                getDatasetIDs.append( " ON " );
                getDatasetIDs.append( join.getSQLJoinCondition() );
            }
        }

        if ( builder.getWhere() != null ) {
            getDatasetIDs.append( " WHERE " );
            getDatasetIDs.append( builder.getWhere().getSQL() );
        }

    }
}
