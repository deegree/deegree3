//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
   Department of Geography, University of Bonn
 and
   lat/lon GmbH

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
package org.deegree.tools.datastore;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.deegree.datatypes.Types;
import org.deegree.datatypes.UnknownTypeException;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.framework.xml.schema.XMLSchemaException;
import org.deegree.model.crs.UnknownCRSException;
import org.xml.sax.SAXException;

/**
 * Generator for PostGIS DDL (CREATE / DROP) operations to create PostGIS database schemas from annotated GML schema
 * files.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class PostGISDDLGenerator extends DDLGenerator {

    /**
     * Generates a new instance of <code>PostGISDDLGenerator</code>, ready to generate DDL for the given schema.
     *
     * @param schemaURL
     * @throws MalformedURLException
     * @throws IOException
     * @throws SAXException
     * @throws XMLParsingException
     * @throws XMLSchemaException
     * @throws UnknownCRSException
     */
    public PostGISDDLGenerator( URL schemaURL ) throws MalformedURLException, IOException, SAXException,
                            XMLParsingException, XMLSchemaException, UnknownCRSException {
        super( schemaURL );
    }

    @Override
    protected StringBuffer generateSetSchemaStmt( String dbSchema ) {
        StringBuffer sb = new StringBuffer( "SET search_path TO " );
        sb.append( dbSchema );
        sb.append( ",public;\n" );
        return sb;
    }

    @Override
    protected StringBuffer generateCreateTableStmt( TableDefinition table ) {

        List<ColumnDefinition> geometryColumns = new ArrayList<ColumnDefinition>();
        StringBuffer sb = new StringBuffer( "CREATE TABLE " );
        sb.append( table.getName() );
        sb.append( '(' );
        ColumnDefinition[] columns = table.getColumns();
        boolean needComma = false;

        if ( table.getType() == MULTI_PROPERTY_TABLE ) {
            sb.append( "\n    " );
            sb.append( "PK SERIAL" );
            needComma = true;
        }

        for ( int i = 0; i < columns.length; i++ ) {
            if ( !columns[i].isGeometry() ) {
                if ( needComma ) {
                    sb.append( ',' );
                } else {
                    needComma = true;
                }
                sb.append( "\n    " );
                sb.append( columns[i].getName() );
                sb.append( ' ' );
                String typeName;
                try {
                    typeName = Types.getTypeNameForSQLTypeCode( columns[i].getType() );
                    if ( typeName.equals( "DOUBLE" ) ) {
                        typeName = "DOUBLE PRECISION";
                    }
                } catch ( UnknownTypeException e ) {
                    typeName = "" + columns[i].getType();
                }
                sb.append( typeName );
                if ( !columns[i].isNullable() ) {
                    sb.append( " NOT NULL" );
                }
            } else {
                geometryColumns.add( columns[i] );
            }
        }

        // add primary key information (forces index generation)
        ColumnDefinition[] pkColumns = table.getPKColumns();
        if ( pkColumns.length > 0 ) {
            sb.append( ",\n    PRIMARY KEY (" );
            for ( int i = 0; i < pkColumns.length; i++ ) {
                sb.append( pkColumns[i].getName() );
                if ( i != pkColumns.length - 1 ) {
                    sb.append( ',' );
                }
            }
            sb.append( ")\n);\n" );
        } else {
            sb.append( "\n);\n" );
        }

        // build addGeometryColumn() statements
        for ( int i = 0; i < geometryColumns.size(); i++ ) {
            ColumnDefinition column = geometryColumns.get( i );
            sb.append( "SELECT AddGeometryColumn ('', '" );
            sb.append( table.getName().toLowerCase() );
            sb.append( "', '" );
            sb.append( column.getName().toLowerCase() );
            sb.append( "', " );
            sb.append( column.getSRS() );
            sb.append( ", '" );
            sb.append( "GEOMETRY" );
            sb.append( "', '2');\n" );
        }
        return sb;
    }

    @Override
    protected StringBuffer generateCreateIndexStmts( TableDefinition table ) {
        StringBuffer sb = new StringBuffer();

        // build create statements for spatial indexes
        Collection<ColumnDefinition> geometryColumns = new ArrayList<ColumnDefinition>();
        for ( ColumnDefinition column : table.getColumns() ) {
            if ( column.isGeometry() ) {
                geometryColumns.add( column );
            }
        }

        Iterator<ColumnDefinition> iter = geometryColumns.iterator();
        int spatialIdxCnt = 1;
        while ( iter.hasNext() ) {
            ColumnDefinition column = iter.next();
            sb.append( "CREATE INDEX " );
            sb.append( table.getName() + ( spatialIdxCnt++ ) );
            sb.append( "_SPATIAL_IDX ON " );
            sb.append( table.getName() );
            sb.append( " USING GIST ( " );
            sb.append( column.getName() );
            sb.append( " GIST_GEOMETRY_OPS );" );
            sb.append( '\n' );
        }

        // build create statements for indexes on all fk columns
        ColumnDefinition[] columns = table.getColumns();
        for ( int i = 0; i < columns.length; i++ ) {
            if ( columns[i].isFK() ) {
                sb.append( "CREATE INDEX " );
                sb.append( table.getName() );
                sb.append( '_' );
                sb.append( columns[i].getName() );
                sb.append( "_IDX ON " );
                sb.append( table.getName() );
                sb.append( '(' );
                sb.append( columns[i].getName() );
                sb.append( ");\n" );
            }
        }

        return sb;
    }

    @Override
    protected StringBuffer generateDropTableStmt( TableDefinition table ) {
        StringBuffer sb = new StringBuffer();
        for ( ColumnDefinition column : table.getColumns() ) {
            if ( column.isGeometry() ) {
                sb.append( "SELECT DropGeometryColumn ('','" );
                sb.append( table.getName().toLowerCase() );
                sb.append( "', '" );
                sb.append( column.getName().toLowerCase() );
                sb.append( "');\n" );
            }
        }
        sb.append( super.generateDropTableStmt( table ) );
        return sb;
    }
}
