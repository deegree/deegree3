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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.deegree.datatypes.Types;
import org.deegree.datatypes.UnknownTypeException;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.framework.xml.schema.XMLSchemaException;
import org.deegree.model.crs.UnknownCRSException;
import org.xml.sax.SAXException;

/**
 * Generator for Oracle DDL (CREATE / DROP) operations to create Oracle database schemas from annotated GML schema
 * files.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class OracleDDLGenerator extends DDLGenerator {

    /**
     * Generates a new instance of <code>OracleDDLGenerator</code>, ready to generate DDL for the given schema.
     *
     * @param schemaURL
     * @throws MalformedURLException
     * @throws IOException
     * @throws SAXException
     * @throws XMLParsingException
     * @throws XMLSchemaException
     * @throws UnknownCRSException
     */
    public OracleDDLGenerator( URL schemaURL ) throws MalformedURLException, IOException, SAXException,
                            XMLParsingException, XMLSchemaException, UnknownCRSException {
        super( schemaURL );
    }

    @Override
    protected StringBuffer generateSetSchemaStmt( String dbSchema ) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected StringBuffer generateDropTableStmt( TableDefinition table ) {
        StringBuffer sb = new StringBuffer();
        sb.append( "DROP TABLE " );
        sb.append( table.getName() );
        sb.append( " CASCADE CONSTRAINTS;\n" );

        ColumnDefinition[] columns = table.getColumns();
        for ( int i = 0; i < columns.length; i++ ) {
            if ( columns[i].isGeometry() ) {
                sb.append( "DELETE FROM USER_SDO_GEOM_METADATA WHERE TABLE_NAME='" );
                sb.append( table.getName() );
                sb.append( "';\n" );
                break;
            }
        }
        return sb;
    }

    @Override
    protected StringBuffer generateCreateTableStmt( TableDefinition table ) {

        Collection<ColumnDefinition> geometryColumns = new ArrayList<ColumnDefinition>();
        StringBuffer sb = new StringBuffer( "CREATE TABLE " );
        sb.append( table.getName() );
        sb.append( " (" );
        ColumnDefinition[] columns = table.getColumns();
        boolean needComma = false;
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
                    } else if ( typeName.equals( "VARCHAR" ) ) {
                        // always prefer VARCHAR2 to VARCHAR:
                        // http://www.orafaq.com/faq/what_is_the_difference_between_varchar_varchar2_and_char_data_types
                        typeName = "VARCHAR2(2000)";
                    } else if ( typeName.equals( "BOOLEAN" ) ) {
                        typeName = "CHAR(1)";
                    } else if ( typeName.equals( "BIGINT" ) ) {
                        // not available in all Oracle versions. using the same precision as Oracle 9i Lite
                        // http://download.oracle.com/docs/html/A90108_01/sqdatyp.htm#631368
                        typeName = "DECIMAL(19)";
                    } else if ( typeName.equals( "NUMERIC" ) ) {
                        typeName = "NUMBER";
                    }
                } catch ( UnknownTypeException e ) {
                    typeName = "" + columns[i].getType();
                }
                sb.append( typeName );
                if ( !columns[i].isNullable() ) {
                    sb.append( " NOT NULL" );
                }
            } else {
                if ( needComma ) {
                    sb.append( ',' );
                } else {
                    needComma = true;
                }
                sb.append( "\n    " );
                sb.append( columns[i].getName() );
                sb.append( " MDSYS.SDO_GEOMETRY" );
                if ( !columns[i].isNullable() ) {
                    sb.append( " NOT NULL" );
                }

                geometryColumns.add( columns[i] );
            }
        }
        ColumnDefinition[] pkColumns = table.getPKColumns();
        if ( pkColumns.length > 0 ) {
            sb.append( ",\n    PRIMARY KEY (" );
            for ( int i = 0; i < pkColumns.length; i++ ) {
                sb.append( pkColumns[i].getName() );
                if ( i != pkColumns.length - 1 ) {
                    sb.append( ',' );
                }
            }
            sb.append( ')' );
        }
        sb.append( "\n);\n" );

        // add geometry columns
        for ( ColumnDefinition column : geometryColumns ) {
            sb.append( "INSERT INTO USER_SDO_GEOM_METADATA VALUES ('" );
            sb.append( table.getName() );
            sb.append( "', '" );
            sb.append( column.getName() );
            sb.append( "', SDO_DIM_ARRAY ( SDO_DIM_ELEMENT ( 'LONGITUDE', -180, 180, 0.00005), SDO_DIM_ELEMENT ('LATITUDE',-90,90,0.00005))," );
            sb.append( column.getSRS() );
            sb.append( ");\n" );
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
            String idxSuffix = ( spatialIdxCnt++ ) + "_SPIDX";
            String idxName = truncate( table.getName().toUpperCase(), idxSuffix );
            sb.append( idxName );
            sb.append( " ON " );
            sb.append( table.getName() );
            sb.append( "(" );
            sb.append( column.getName() );
            sb.append( ") INDEXTYPE IS MDSYS.SPATIAL_INDEX;" );
            sb.append( '\n' );
        }

        // build create statements for indexes on all fk columns
        ColumnDefinition[] columns = table.getColumns();
        for ( int i = 0; i < columns.length; i++ ) {
            if ( columns[i].isFK() ) {
                sb.append( "CREATE INDEX " );
                String idxSuffix = '_' + columns[i].getName() + "_IDX";
                String idxName = truncate( table.getName().toUpperCase(), idxSuffix );
                sb.append( idxName );
                sb.append( " ON " );
                sb.append( table.getName() );
                sb.append( '(' );
                sb.append( columns[i].getName() );
                sb.append( ");" );
                sb.append( '\n' );
            }
        }
        return sb;
    }

    @Override
    protected StringBuffer generateDropIndexStmts( TableDefinition table ) {
        StringBuffer sb = new StringBuffer();

        // build drop statements for geometry indexes
        Collection<ColumnDefinition> geometryColumns = new ArrayList<ColumnDefinition>();
        for ( ColumnDefinition column : table.getColumns() ) {
            if ( column.isGeometry() ) {
                geometryColumns.add( column );
            }
        }

        Iterator<ColumnDefinition> iter = geometryColumns.iterator();
        int spatialIdxCnt = 1;
        while ( iter.hasNext() ) {
            iter.next();
            sb.append( "DROP INDEX " );
            String idxSuffix = ( spatialIdxCnt++ ) + "_SPIDX";
            String idxName = truncate( table.getName().toUpperCase(), idxSuffix );
            sb.append( idxName );
            sb.append( ";\n" );
        }

        // build table type specific drop index statements
        switch ( table.getType() ) {
        case JOIN_TABLE: {
            // create an index on every column
            ColumnDefinition[] columns = table.getColumns();
            for ( int i = 0; i < columns.length; i++ ) {
                sb.append( "DROP INDEX " );
                String idxSuffix = '_' + columns[i].getName() + "_IDX";
                String idxName = truncate( table.getName().toUpperCase(), idxSuffix );
                sb.append( idxName );
                sb.append( ";\n" );
            }
            break;
        }
        default: {
            break;
        }
        }
        return sb;
    }

    Map<String, String> idToTruncatedId = new HashMap<String, String>();

    int currentId;

    /**
     * Ensures that the given identifier does not exceed Oracle's limit (30 characters).
     *
     * @param identifier
     * @param suffix
     * @return the identifier (may be truncated)
     */
    private String truncate( String identifier, String suffix ) {
        String truncatedIdentifier = idToTruncatedId.get( identifier + suffix );
        if ( truncatedIdentifier == null ) {
            truncatedIdentifier = identifier + suffix;
            if ( truncatedIdentifier.length() > 30 ) {
                System.out.print( "Generated identifier name '" + truncatedIdentifier
                                  + "' exceeds 30 characters (Oracle limit)." );
                truncatedIdentifier = identifier.substring( 0, 30 - suffix.length() - ( "" + currentId ).length() )
                                      + ( currentId++ ) + suffix;
                System.out.println( " Truncated to: '" + truncatedIdentifier + "'." );
            }
            idToTruncatedId.put( identifier + suffix, truncatedIdentifier );
        }
        return truncatedIdentifier;
    }
}
