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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.deegree.datatypes.Types;
import org.deegree.datatypes.UnknownTypeException;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.framework.xml.schema.XMLSchemaException;
import org.deegree.io.datastore.schema.MappedFeaturePropertyType;
import org.deegree.io.datastore.schema.MappedFeatureType;
import org.deegree.io.datastore.schema.MappedGMLId;
import org.deegree.io.datastore.schema.MappedGMLSchema;
import org.deegree.io.datastore.schema.MappedGMLSchemaDocument;
import org.deegree.io.datastore.schema.MappedGeometryPropertyType;
import org.deegree.io.datastore.schema.MappedPropertyType;
import org.deegree.io.datastore.schema.MappedSimplePropertyType;
import org.deegree.io.datastore.schema.TableRelation;
import org.deegree.io.datastore.schema.content.MappingField;
import org.deegree.io.datastore.schema.content.MappingGeometryField;
import org.deegree.io.datastore.schema.content.SimpleContent;
import org.deegree.io.datastore.sql.idgenerator.DBSeqIdGenerator;
import org.deegree.model.crs.UnknownCRSException;
import org.deegree.model.feature.schema.FeatureType;
import org.deegree.model.feature.schema.PropertyType;
import org.xml.sax.SAXException;

/**
 * Abstract base class for DDL generation from annotated GML schema files.
 * <p>
 * This abstract base class only implements the functionality needed to retrieve the necessary tables and columns used
 * in an annotated GML schema. Some DDL generation may be dependent on the specific SQL backend to be used, so this is
 * implemented in concrete extensions of this class.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public abstract class DDLGenerator {

    protected static final String FT_PREFIX = "FT_";

    protected static final int FEATURE_TYPE_TABLE = 0;

    protected static final int JOIN_TABLE = 1;

    protected static final int MULTI_PROPERTY_TABLE = 2;

    protected MappedGMLSchema schema;

    // key type: String (table names), value type: TableDefinition
    protected Map<String, TableDefinition> tables = new HashMap<String, TableDefinition>();

    // names of sequences (for id generation)
    protected Set<String> sequences = new HashSet<String>();

    /**
     * Generates the SQL statements necessary for setting the schema search path. Must be overwritten by the concrete
     * implementation.
     *
     * @param dbSchemaName
     * @return the SQL statements necessary for setting the schema search path accordingly
     */
    protected abstract StringBuffer generateSetSchemaStmt( String dbSchemaName );

    /**
     * Generates the DDL statements necessary for the creation of the given schema. May be overwritten by a concrete
     * implementation.
     *
     * @param dbSchemaName
     * @return the DDL statements necessary for the creation of the given db schema
     */
    protected StringBuffer generateCreateSchemaStmts( String dbSchemaName ) {
        StringBuffer sb = new StringBuffer( "CREATE SCHEMA " );
        sb.append( dbSchemaName );
        sb.append( ";\n" );
        return sb;
    }

    /**
     * Generates the DDL statements necessary for the creation of the given table definition. Must be overwritten by the
     * concrete implementation.
     *
     * @param table
     * @return the DDL statements necessary for the creation of the given table definition
     */
    protected abstract StringBuffer generateCreateTableStmt( TableDefinition table );

    /**
     * Generates the DDL statements necessary for the creation of standard indexes for the given table definition. Must
     * be overwritten by the concrete implementation.
     *
     * @param table
     * @return the DDL statements necessary for the creation of standard indexes for the given table definition
     */
    protected abstract StringBuffer generateCreateIndexStmts( TableDefinition table );

    /**
     * Generates the DDL statements necessary for the creation of the given sequence. May be overwritten by a concrete
     * implementation.
     *
     * @param sequenceName
     * @return the DDL statements necessary for the creation of the given sequence definition
     */
    protected StringBuffer generateCreateSequenceStmt( String sequenceName ) {
        StringBuffer sb = new StringBuffer( "CREATE SEQUENCE " );
        sb.append( sequenceName );
        sb.append( ";\n" );
        return sb;
    }

    /**
     * Generates the DDL statements necessary for the removal of the given schema. May be overwritten by a concrete
     * implementation.
     *
     * @param dbSchemaName
     * @return the DDL statements necessary for the removal of the given db schema
     */
    protected StringBuffer generateDropSchemaStmt( String dbSchemaName ) {
        StringBuffer sb = new StringBuffer();
        sb.append( "DROP SCHEMA " );
        sb.append( dbSchemaName );
        sb.append( " CASCADE;\n" );
        return sb;
    }

    /**
     * Generates the DDL statements necessary for the removal of the given table definition. May be overwritten by a
     * concrete implementation.
     *
     * @param table
     * @return the DDL statements necessary for the removal of the given table definition
     */
    protected StringBuffer generateDropTableStmt( TableDefinition table ) {
        StringBuffer sb = new StringBuffer();
        sb.append( "DROP TABLE " );
        sb.append( table.getName() );
        sb.append( " CASCADE;\n" );
        return sb;
    }

    /**
     * Generates the DDL statements necessary for the dropping of standard indexes for the given table definition. May
     * be overwritten by a concrete implementation.
     *
     * @param table
     * @return the DDL statements necessary for the dropping of standard indexes for the given table definition
     */
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
            sb.append( table.getName() + ( spatialIdxCnt++ ) );
            sb.append( "_SPATIAL_IDX;" );
            sb.append( '\n' );
        }

        // build table type specific drop index statements
        switch ( table.getType() ) {
        case JOIN_TABLE: {
            // create an index on every column
            ColumnDefinition[] columns = table.getColumns();
            for ( int i = 0; i < columns.length; i++ ) {
                if ( columns[i].isFK() ) {
                    sb.append( "DROP INDEX " );
                    sb.append( table.getName().toUpperCase() );
                    sb.append( "_" );
                    sb.append( columns[i].getName() + "_IDX" );
                    sb.append( ';' );
                    sb.append( '\n' );
                }
            }
            break;
        }
        default: {
            break;
        }
        }
        return sb;
    }

    /**
     * Generates the DDL statements necessary for the removal of the given sequence. May be overwritten by a concrete
     * implementation.
     *
     * @param sequenceName
     * @return the DDL statements necessary for the removal of the given sequence definition
     */
    protected StringBuffer generateDropSequenceStmt( String sequenceName ) {
        StringBuffer sb = new StringBuffer( "DROP SEQUENCE " );
        sb.append( sequenceName );
        sb.append( ";\n" );
        return sb;
    }

    /**
     * Creates a new instance of <code>DDLGenerator</code> from the given parameters.
     *
     * @param schemaURL
     * @throws MalformedURLException
     * @throws IOException
     * @throws SAXException
     * @throws XMLParsingException
     * @throws XMLSchemaException
     * @throws UnknownCRSException
     */
    protected DDLGenerator( URL schemaURL ) throws MalformedURLException, IOException, SAXException,
                            XMLParsingException, XMLSchemaException, UnknownCRSException {

        System.out.println( Messages.format( "LOADING_SCHEMA_FILE", schemaURL ) );
        MappedGMLSchemaDocument schemaDoc = new MappedGMLSchemaDocument();
        schemaDoc.load( schemaURL );
        schema = schemaDoc.parseMappedGMLSchema();
        FeatureType[] featureTypes = schema.getFeatureTypes();
        int concreteCount = 0;
        for ( int i = 0; i < featureTypes.length; i++ ) {
            if ( !featureTypes[i].isAbstract() ) {
                concreteCount++;
            }
        }
        System.out.println( Messages.format( "SCHEMA_INFO", new Integer( featureTypes.length ),
                                             new Integer( featureTypes.length - concreteCount ),
                                             new Integer( concreteCount ) ) );
        System.out.println( Messages.getString( "RETRIEVING_TABLES" ) );
        buildTableMap();
    }

    /**
     * Returns all table definitions of the given type.
     *
     * @param type
     *            FEATURE_TYPE_TABLE, JOIN_TABLE or MULTI_PROPERTY_TABLE
     * @return all table definitions of the given type.
     */
    protected TableDefinition[] getTables( int type ) {
        Collection<TableDefinition> tableList = new ArrayList<TableDefinition>();
        Iterator<String> iter = this.tables.keySet().iterator();
        while ( iter.hasNext() ) {
            String tableName = iter.next();
            TableDefinition table = this.tables.get( tableName );
            if ( table.getType() == type ) {
                tableList.add( table );
            }
        }
        return tableList.toArray( new TableDefinition[tableList.size()] );
    }

    /**
     * Returns the table definition for the table with the given name. If no such definition exists, a new table
     * definition is created and added to the internal <code>tables</code> map.
     *
     * @param tableName
     *            table definition to look up
     * @param type
     *            type of the table (only respected, if a new TableDefinition instance is created)
     * @return the table definition for the table with the given name.
     */
    private TableDefinition lookupTableDefinition( String tableName, int type ) {
        TableDefinition table = this.tables.get( tableName );
        if ( table == null ) {
            table = new TableDefinition( tableName, type );
            this.tables.put( tableName, table );
        }
        return table;
    }

    /**
     * Collects the referenced tables and their columns from the input schema. Builds the member map <code>tables</code>
     * from this data.
     */
    private void buildTableMap() {
        FeatureType[] featureTypes = schema.getFeatureTypes();
        for ( int i = 0; i < featureTypes.length; i++ ) {
            if ( !featureTypes[i].isAbstract() ) {
                buildTableMap( (MappedFeatureType) featureTypes[i] );
            }
        }
    }

    /**
     * Collects the tables and their columns used in the annotation of the given feature type. Builds the member map
     * <code>tables</code> from this data.
     *
     * @param ft
     *            feature type to process
     */
    private void buildTableMap( MappedFeatureType ft ) {
        TableDefinition table = lookupTableDefinition( ft.getTable(), FEATURE_TYPE_TABLE );

        MappedGMLId gmlId = ft.getGMLId();
        addGMLIdColumns( gmlId, table );

        if ( gmlId.getIdGenerator() instanceof DBSeqIdGenerator ) {
            extractSequence( (DBSeqIdGenerator) ft.getGMLId().getIdGenerator() );
        }

        PropertyType[] properties = ft.getProperties();
        for ( int i = 0; i < properties.length; i++ ) {
            MappedPropertyType property = (MappedPropertyType) properties[i];
            if ( property instanceof MappedSimplePropertyType ) {
                buildTableMap( (MappedSimplePropertyType) property, table );
            } else if ( property instanceof MappedGeometryPropertyType ) {
                buildTableMap( (MappedGeometryPropertyType) property, table );
            } else if ( property instanceof MappedFeaturePropertyType ) {
                buildTableMap( (MappedFeaturePropertyType) property, table );
            } else {
                throw new RuntimeException( Messages.format( "ERROR_UNEXPECTED_PROPERTY_TYPE",
                                                             property.getClass().getName() ) );
            }
        }
    }

    /**
     * Adds the name of the sequence that the given {@link DBSeqIdGenerator} refers to.
     *
     * @param idGenerator
     *            generator instance
     */
    private void extractSequence( DBSeqIdGenerator idGenerator ) {
        this.sequences.add( idGenerator.getSequenceName() );
    }

    /**
     * Adds the columns used in the given <code>MappedGMLId</code> to the also given <code>TableDefinition</code>.
     *
     * @param gmlId
     *            columns are taken from this gmlId mapping
     * @param table
     *            columns are added to this table definition
     */
    private void addGMLIdColumns( MappedGMLId gmlId, TableDefinition table ) {
        MappingField[] idFields = gmlId.getIdFields();
        for ( int i = 0; i < idFields.length; i++ ) {
            ColumnDefinition column = new ColumnDefinition( idFields[i].getField(), idFields[i].getType(), false, true,
                                                            false, -1, false );
            table.addColumn( column );
        }
    }

    /**
     * Collects the tables and their columns used in the annotation of the given simple property type. Builds the
     * <code>table</code> member map from this data.
     * <p>
     * If the data for the property is stored in a related table, the table and column information used on the path to
     * this table is also added to the <code>tables</code> member map.
     *
     * @param simpleProperty
     *            simple property type to process
     * @param table
     *            table definition associated with the property definition
     */
    private void buildTableMap( MappedSimplePropertyType simpleProperty, TableDefinition table ) {
        Collection<ColumnDefinition> newColumns = new ArrayList<ColumnDefinition>();
        // array must always have length 1
        TableRelation[] relations = simpleProperty.getTableRelations();
        if ( simpleProperty.getMaxOccurs() != 1 && ( relations == null || relations.length < 1 ) ) {
            throw new RuntimeException( Messages.format( "ERROR_INVALID_PROPERTY_DEFINITION", simpleProperty.getName() ) );
        }

        SimpleContent content = simpleProperty.getContent();
        if ( content instanceof MappingField ) {
            MappingField mf = (MappingField) content;
            if ( relations == null || relations.length == 0 ) {
                newColumns.add( new ColumnDefinition( mf.getField(), mf.getType(), simpleProperty.getMinOccurs() == 0,
                                                      false, -1, false ) );
            } else {
                TableRelation firstRelation = relations[0];
                MappingField[] fromFields = firstRelation.getFromFields();
                for ( int i = 0; i < fromFields.length; i++ ) {
                    MappingField fromField = fromFields[i];
                    newColumns.add( new ColumnDefinition( fromField.getField(), fromField.getType(), false, false, -1,
                                                          true ) );
                }
                buildTableMap( relations, mf );
            }
        } else {
            String msg = "Ignoring property '" + simpleProperty + "' - has virtual content.";
            System.out.println( msg );
        }
        table.addColumns( newColumns );
    }

    /**
     * Collects the tables and their columns used in the annotation of the given geometry property type. Builds the
     * <code>table</code> member map from this data.
     * <p>
     * If the geometry for the property is stored in a related table, the table and column information used on the path
     * to this table is also added to the <code>tables</code> member map.
     *
     * @param geometryProperty
     *            feature property type to process
     * @param table
     *            table definition associated with the property definition
     */
    private void buildTableMap( MappedGeometryPropertyType geometryProperty, TableDefinition table ) {
        Collection<ColumnDefinition> newColumns = new ArrayList<ColumnDefinition>();
        TableRelation[] relations = geometryProperty.getTableRelations();
        if ( geometryProperty.getMaxOccurs() != 1 && ( relations == null || relations.length < 1 ) ) {
            throw new RuntimeException( Messages.format( "ERROR_INVALID_PROPERTY_DEFINITION",
                                                         geometryProperty.getName() ) );
        }
        if ( relations == null || relations.length == 0 ) {
            newColumns.add( new ColumnDefinition( geometryProperty.getMappingField().getField(),
                                                  geometryProperty.getMappingField().getType(),
                                                  geometryProperty.getMinOccurs() == 0, true,
                                                  geometryProperty.getMappingField().getSRS(), false ) );
        } else {
            TableRelation firstRelation = relations[0];
            MappingField[] fromFields = firstRelation.getFromFields();
            for ( int i = 0; i < fromFields.length; i++ ) {
                MappingField fromField = fromFields[i];
                newColumns.add( new ColumnDefinition( fromField.getField(), fromField.getType(), false, true,
                                                      geometryProperty.getMappingField().getSRS(), true ) );
            }
            buildTableMap( relations, geometryProperty.getMappingField() );
        }
        table.addColumns( newColumns );
    }

    /**
     * Collects the tables and their columns used in the annotation of the given feature property type. Builds the
     * <code>table</code> member map from this data.
     * <p>
     * The table and column information used on the path to the table of the feature type is also added to the
     * <code>tables</code> member map.
     *
     * @param featureProperty
     *            feature property type to process
     * @param table
     *            table definition associated with the property definition
     */
    private void buildTableMap( MappedFeaturePropertyType featureProperty, TableDefinition table ) {
        Collection<ColumnDefinition> newColumns = new ArrayList<ColumnDefinition>();

        // array must always have length 1
        TableRelation[] relations = featureProperty.getTableRelations();

        // target feature type table must always be accessed via 'Relation'-elements
        if ( relations == null || relations.length < 1 ) {
            throw new RuntimeException( Messages.format( "ERROR_INVALID_FEATURE_PROPERTY_DEFINITION_1",
                                                         featureProperty.getName() ) );
        }

        // maxOccurs > 1: target feature type table must be accessed via join table
        if ( featureProperty.getMaxOccurs() != 1 && ( relations.length < 2 ) ) {
            throw new RuntimeException( Messages.format( "ERROR_INVALID_FEATURE_PROPERTY_DEFINITION_2",
                                                         featureProperty.getName() ) );
        }

        // add this feature type's key columns to current table
        TableRelation firstRelation = relations[0];
        MappingField[] fromFields = firstRelation.getFromFields();
        boolean isNullable = featureProperty.getMinOccurs() == 0 && relations.length == 1;
        for ( int i = 0; i < fromFields.length; i++ ) {
            MappingField fromField = fromFields[i];
            if ( featureProperty.externalLinksAllowed() ) {
                newColumns.add( new ColumnDefinition( fromField.getField(), fromField.getType(), true, false, -1, true ) );
                newColumns.add( new ColumnDefinition( fromField.getField() + "_external", fromField.getType(), true,
                                                      false, -1, true ) );
            } else {
                newColumns.add( new ColumnDefinition( fromField.getField(), fromField.getType(), isNullable, false, -1,
                                                      true ) );
            }
        }
        table.addColumns( newColumns );

        MappedFeatureType contentType = featureProperty.getFeatureTypeReference().getFeatureType();
        buildTableMap( relations, featureProperty, contentType );
    }

    /**
     * Collects the tables and their columns used in the relation tables from a simple/geometry property to it's content
     * table. Builds the <code>table</code> member map from this data.
     *
     * @param relations
     *            relation tables from annotation of property type
     * @param targetField
     *            holds the properties data
     */
    private void buildTableMap( TableRelation[] relations, MappingField targetField ) {

        // process tables used in 'To'-element of each 'Relation'-element
        for ( int i = 0; i < relations.length; i++ ) {
            String tableName = relations[i].getToTable();
            TableDefinition table = lookupTableDefinition( tableName, MULTI_PROPERTY_TABLE );
            MappingField[] toFields = relations[i].getToFields();
            for ( int j = 0; j < toFields.length; j++ ) {
                boolean toIsFK = relations[i].getFKInfo() == TableRelation.FK_INFO.fkIsToField;
                ColumnDefinition column = new ColumnDefinition( toFields[j].getField(), toFields[j].getType(), false,
                                                                !toIsFK, false, -1, toIsFK );
                // schmitz: assuming not part of PK
                table.addColumn( column );
            }
        }

        // process table used in 'To'-element of last 'Relation'-element (targetField refers to
        // this)
        ColumnDefinition column = null;
        if ( targetField instanceof MappingGeometryField ) {
            column = new ColumnDefinition( targetField.getField(), targetField.getType(), false, true,
                                           ( (MappingGeometryField) targetField ).getSRS(), false );
        } else {
            column = new ColumnDefinition( targetField.getField(), targetField.getType(), false, false, -1, false );
        }

        TableDefinition table = lookupTableDefinition( relations[relations.length - 1].getToTable(),
                                                       MULTI_PROPERTY_TABLE );
        table.addColumn( column );
    }

    /**
     * Collects the tables and their columns used in the relation tables from a feature property to it's content feature
     * type. Builds the <code>table</code> member map from this data.
     *
     * @param relations
     *            relation tables from annotation of feature property type
     * @param property
     * @param targetType
     *            type contained in the feature property
     */
    private void buildTableMap( TableRelation[] relations, MappedPropertyType property, MappedFeatureType targetType ) {

        TableDefinition table = lookupTableDefinition( relations[0].getFromTable(), FEATURE_TYPE_TABLE );

        // process tables used in 'To'-element of each 'Relation'-element (except the last)
        for ( int i = 0; i < relations.length - 1; i++ ) {
            String tableName = relations[i].getToTable();
            table = lookupTableDefinition( tableName, JOIN_TABLE );
            MappingField[] toFields = relations[i].getToFields();
            for ( int j = 0; j < toFields.length; j++ ) {
                boolean toIsFK = relations[i].getFKInfo() == TableRelation.FK_INFO.fkIsToField;
                ColumnDefinition column = new ColumnDefinition( toFields[j].getField(), toFields[j].getType(), false,
                                                                true, false, -1, toIsFK );
                // schmitz: assuming NOT part of PK
                table.addColumn( column );
            }
        }

        // process table used in 'To'-element of last 'Relation'-element
        MappedFeatureType[] concreteTypes = targetType.getConcreteSubstitutions();
        MappingField[] toFields = relations[relations.length - 1].getToFields();

        // if it refers to several target tables (target feature type is abstract), an additional
        // column is needed (which determines the target feature type)
        if ( concreteTypes.length > 1 ) {
            String typeColumn = "featuretype";
            if ( relations.length == 1 ) {
                typeColumn = FT_PREFIX + property.getTableRelations()[0].getFromFields()[0].getField();
            }
            ColumnDefinition column = new ColumnDefinition( typeColumn, Types.VARCHAR, property.getMinOccurs() == 0,
                                                            false, -1, false );
            table.addColumn( column );
        }
        for ( int i = 0; i < concreteTypes.length; i++ ) {
            MappedFeatureType concreteType = concreteTypes[i];
            String tableName = concreteType.getTable();
            table = lookupTableDefinition( tableName, FEATURE_TYPE_TABLE );
            for ( int j = 0; j < toFields.length; j++ ) {
                ColumnDefinition column = new ColumnDefinition( toFields[j].getField(), toFields[j].getType(), false,
                                                                false, -1, false );
                table.addColumn( column );
            }
        }

        // process tables used in 'From'-element of each 'Relation'-element (except the first)
        for ( int i = 1; i < relations.length; i++ ) {
            String tableName = relations[i].getFromTable();
            if ( i != relations.length - 1 ) {
                table = lookupTableDefinition( tableName, JOIN_TABLE );
            } else {
                table = lookupTableDefinition( tableName, FEATURE_TYPE_TABLE );
            }
            MappingField[] fromFields = relations[i].getFromFields();
            for ( int j = 0; j < fromFields.length; j++ ) {
                boolean fromIsFK = relations[i].getFKInfo() == TableRelation.FK_INFO.fkIsFromField;
                ColumnDefinition column = new ColumnDefinition( fromFields[j].getField(), fromFields[j].getType(),
                                                                false, true, false, -1, fromIsFK );
                table.addColumn( column );
            }
        }
    }

    /**
     * @param outputFile
     * @throws IOException
     */
    public void generateCreateScript( String outputFile )
                            throws IOException {
        generateCreateScript( outputFile, null );
    }

    /**
     * Generates the DDL statements to create a relational schema that backs the GML schema.
     *
     * @param outputFile
     * @param dbSchema
     *            (may be null)
     * @throws IOException
     */
    public void generateCreateScript( String outputFile, String dbSchema )
                            throws IOException {
        PrintWriter writer = new PrintWriter( new OutputStreamWriter( new FileOutputStream( outputFile ), "UTF-8" ) );

        if ( dbSchema != null ) {
            writer.print( "/* CREATE DB SCHEMA (" + dbSchema + ") */\n\n" );
            writer.print( generateCreateSchemaStmts( dbSchema ) );
            writer.print( generateSetSchemaStmt( dbSchema ) );
            writer.println();
            writer.println();
        }

        System.out.println( Messages.format( "CREATE_SEQUENCES", new Integer( sequences.size() ) ) );
        if ( sequences.size() > 0 ) {
            writer.print( "/* CREATE SEQUENCES (" + sequences.size() + ") */\n" );
            for ( String sequenceName : sequences ) {
                writer.print( '\n' );
                writer.print( generateCreateSequenceStmt( sequenceName ) );
            }
        }

        TableDefinition[] tables = getTables( FEATURE_TYPE_TABLE );
        System.out.println( Messages.format( "CREATE_FEATURE_TYPE", new Integer( tables.length ) ) );
        writer.print( "\n\n/* CREATE FEATURE TABLES (" + tables.length + ") */\n" );
        for ( int i = 0; i < tables.length; i++ ) {
            System.out.println( tables[i].tableName );
            writer.print( '\n' );
            writer.print( generateCreateTableStmt( tables[i] ) );
            writer.print( generateCreateIndexStmts( tables[i] ) );
        }

        tables = getTables( JOIN_TABLE );
        if ( tables.length != 0 ) {
            writer.print( "\n\n/* CREATE JOIN TABLES (" + tables.length + ") */\n" );
        }
        System.out.println( Messages.format( "CREATE_JOIN_TABLES", new Integer( tables.length ) ) );
        for ( int i = 0; i < tables.length; i++ ) {
            System.out.println( tables[i].tableName );
            writer.print( '\n' );
            writer.print( generateCreateTableStmt( tables[i] ) );
            writer.print( generateCreateIndexStmts( tables[i] ) );
        }

        tables = getTables( MULTI_PROPERTY_TABLE );
        if ( tables.length != 0 ) {
            writer.print( "\n\n/* CREATE PROPERTY TABLES (" + tables.length + ") */\n" );
        }
        System.out.println( Messages.format( "CREATE_PROPERTY_TABLES", new Integer( tables.length ) ) );
        for ( int i = 0; i < tables.length; i++ ) {
            System.out.println( tables[i].tableName );
            writer.print( '\n' );
            writer.print( generateCreateTableStmt( tables[i] ) );
            writer.print( generateCreateIndexStmts( tables[i] ) );
        }
        writer.close();
    }

    /**
     * Generates the DDL statements that can be used to remove the relational schema again.
     *
     * @param outputFile
     * @param dbSchema
     *            (may be null)
     * @throws IOException
     */
    public void generateDropScript( String outputFile, String dbSchema )
                            throws IOException {
        PrintWriter writer = new PrintWriter( new OutputStreamWriter( new FileOutputStream( outputFile ), "UTF-8" ) );

        if ( dbSchema != null ) {
            writer.println( generateSetSchemaStmt( dbSchema ) );
            writer.println();
            writer.println();
        }

        TableDefinition[] tables = getTables( FEATURE_TYPE_TABLE );
        System.out.println( Messages.format( "DROP_FEATURE_TYPE", new Integer( tables.length ) ) );
        writer.print( "/* DROP FEATURE TABLES (" + tables.length + ") */\n" );
        for ( int i = 0; i < tables.length; i++ ) {
            writer.print( '\n' );
            writer.print( generateDropIndexStmts( tables[i] ) );
            writer.print( generateDropTableStmt( tables[i] ) );
        }

        tables = getTables( JOIN_TABLE );
        writer.print( "\n\n/* DROP JOIN TABLES (" + tables.length + ") */\n" );
        System.out.println( Messages.format( "DROP_JOIN_TABLES", new Integer( tables.length ) ) );
        for ( int i = 0; i < tables.length; i++ ) {
            writer.print( '\n' );
            writer.print( generateDropIndexStmts( tables[i] ) );
            writer.print( generateDropTableStmt( tables[i] ) );
        }

        tables = getTables( MULTI_PROPERTY_TABLE );
        writer.print( "\n\n/* DROP PROPERTY TABLES (" + tables.length + ") */\n" );
        System.out.println( Messages.format( "DROP_PROPERTY_TABLES", new Integer( tables.length ) ) );
        for ( int i = 0; i < tables.length; i++ ) {
            writer.print( '\n' );
            writer.print( generateDropIndexStmts( tables[i] ) );
            writer.print( generateDropTableStmt( tables[i] ) );
        }

        System.out.println( Messages.format( "DROP_SEQUENCES", new Integer( sequences.size() ) ) );
        if ( sequences.size() > 0 ) {
            writer.print( "\n\n/* DROP SEQUENCES (" + sequences.size() + ") */\n" );
            for ( String sequenceName : sequences ) {
                writer.print( '\n' );
                writer.print( generateDropSequenceStmt( sequenceName ) );
            }
        }

        if ( dbSchema != null ) {
            writer.print( "\n\n/* DROP DB SCHEMA (" + dbSchema + ") */\n" );
            writer.print( generateDropSchemaStmt( dbSchema ) );
            writer.println();
        }

        writer.close();
    }

    /**
     * @param args
     * @throws IOException
     * @throws SAXException
     * @throws XMLParsingException
     * @throws XMLSchemaException
     * @throws UnknownCRSException
     */
    public static void main( String[] args )
                            throws IOException, SAXException, XMLParsingException, XMLSchemaException,
                            UnknownCRSException {

        if ( args.length < 4 || args.length > 5 ) {
            System.out.println( "Usage: DDLGenerator <FLAVOUR> <input.xsd> <create.sql> <drop.sql> [DB_SCHEMA]" );
            System.exit( 0 );
        }

        String flavour = args[0];
        String schemaFile = args[1];
        String createFile = args[2];
        String dropFile = args[3];
        String dbSchema = args.length == 4 ? null : args[4];

        DDLGenerator generator = null;
        if ( "POSTGIS".equals( flavour ) ) {
            generator = new PostGISDDLGenerator( new File( schemaFile ).toURI().toURL() );
        } else if ( "ORACLE".equals( flavour ) ) {
            generator = new OracleDDLGenerator( new File( schemaFile ).toURI().toURL() );
        } else {
            System.out.println( Messages.format( "ERROR_UNSUPPORTED_FLAVOUR", flavour ) );
        }

        generator.generateCreateScript( createFile, dbSchema );
        generator.generateDropScript( dropFile, dbSchema );
    }

    /**
     * Returns a string representation of the object.
     *
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer( Messages.getString( "RELATIONAL_SCHEMA" ) );
        sb.append( '\n' );

        TableDefinition[] tables = getTables( FEATURE_TYPE_TABLE );
        sb.append( '\n' );
        sb.append( tables.length );
        sb.append( " feature type tables\n\n" );
        for ( int i = 0; i < tables.length; i++ ) {
            sb.append( tables[i] );
            sb.append( '\n' );
        }

        sb.append( '\n' );
        tables = getTables( JOIN_TABLE );
        sb.append( tables.length );
        sb.append( " join tables\n\n" );
        for ( int i = 0; i < tables.length; i++ ) {
            sb.append( tables[i] );
            sb.append( '\n' );
        }

        sb.append( '\n' );
        tables = getTables( MULTI_PROPERTY_TABLE );
        sb.append( tables.length );
        sb.append( " property tables\n\n" );
        for ( int i = 0; i < tables.length; i++ ) {
            sb.append( tables[i] );
            sb.append( '\n' );
        }
        return sb.toString();
    }

    class TableDefinition {

        private int type;

        String tableName;

        private Map<String, ColumnDefinition> columnsMap = new LinkedHashMap<String, ColumnDefinition>();

        TableDefinition( String tableName, int type ) {
            this.type = type;
            this.tableName = tableName;
        }

        String getName() {
            return this.tableName;
        }

        int getType() {
            return this.type;
        }

        ColumnDefinition[] getColumns() {
            Collection<ColumnDefinition> columns = new ArrayList<ColumnDefinition>();
            Iterator<String> iter = columnsMap.keySet().iterator();
            while ( iter.hasNext() ) {
                String columnName = iter.next();
                columns.add( columnsMap.get( columnName ) );
            }
            return columns.toArray( new ColumnDefinition[columns.size()] );
        }

        ColumnDefinition[] getPKColumns() {
            Collection<ColumnDefinition> columns = new ArrayList<ColumnDefinition>();
            Iterator<String> iter = columnsMap.keySet().iterator();
            while ( iter.hasNext() ) {
                String columnName = iter.next();
                ColumnDefinition column = columnsMap.get( columnName );
                if ( column.isPartOfPK() ) {
                    columns.add( columnsMap.get( columnName ) );
                }
            }
            return columns.toArray( new ColumnDefinition[columns.size()] );
        }

        ColumnDefinition getColumn( String name ) {
            return columnsMap.get( name );
        }

        void addColumn( ColumnDefinition column ) {
            ColumnDefinition oldColumn = columnsMap.get( column.getName() );
            if ( oldColumn != null ) {
                if ( !( column.getType() == oldColumn.getType() ) ) {
                    String msg = null;
                    try {
                        msg = Messages.format( "ERROR_COLUMN_DEFINITION_TYPES", column.getName(),
                                               Types.getTypeNameForSQLTypeCode( oldColumn.getType() ),
                                               Types.getTypeNameForSQLTypeCode( column.getType() ) );
                    } catch ( UnknownTypeException e ) {
                        msg = e.getMessage();
                        e.printStackTrace();
                    }
                    throw new RuntimeException( msg );

                }
                if ( oldColumn.isPartOfPK() ) {
                    column = oldColumn;
                }
            }
            columnsMap.put( column.getName(), column );
        }

        void addColumns( Collection<ColumnDefinition> columns ) {
            Iterator<ColumnDefinition> iter = columns.iterator();
            while ( iter.hasNext() ) {
                ColumnDefinition column = iter.next();
                addColumn( column );
            }
        }

        @Override
        public String toString() {
            StringBuffer sb = new StringBuffer();
            sb.append( Messages.format( "TABLE", this.tableName ) );
            sb.append( Messages.getString( "PRIMARY_KEY" ) );
            ColumnDefinition[] pkColumns = getPKColumns();
            for ( int i = 0; i < pkColumns.length; i++ ) {
                sb.append( '"' );
                sb.append( pkColumns[i].getName() );
                sb.append( '"' );
                if ( i != pkColumns.length - 1 ) {
                    sb.append( ", " );
                }
            }
            sb.append( '\n' );
            Iterator<String> columnNameIter = this.columnsMap.keySet().iterator();
            while ( columnNameIter.hasNext() ) {
                String columnName = columnNameIter.next();
                ColumnDefinition column = this.columnsMap.get( columnName );
                try {
                    sb.append( Messages.format( "COLUMN", columnName,
                                                Types.getTypeNameForSQLTypeCode( column.getType() ) + ":"
                                                                        + column.getType(),
                                                new Boolean( column.isNullable() ) ) );
                } catch ( UnknownTypeException e ) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                sb.append( '\n' );
            }
            return sb.toString();
        }
    }

    class ColumnDefinition {

        private String columnName;

        private int type;

        private boolean isNullable;

        private boolean isGeometryColumn;

        private int srsCode;

        private boolean isPartOfPK;

        private boolean isFK;

        ColumnDefinition( String columnName, int type, boolean isNullable, boolean isGeometryColumn, int srsCode,
                          boolean isFK ) {
            this.columnName = columnName;
            this.type = type;
            this.isNullable = isNullable;
            this.isGeometryColumn = isGeometryColumn;
            this.srsCode = srsCode;
            this.isFK = isFK;
        }

        ColumnDefinition( String columnName, int type, boolean isNullable, boolean isPartOfPK,
                          boolean isGeometryColumn, int srsCode, boolean isFK ) {
            this( columnName, type, isNullable, isGeometryColumn, srsCode, isFK );
            this.isPartOfPK = isPartOfPK;
        }

        String getName() {
            return this.columnName;
        }

        int getType() {
            return this.type;
        }

        boolean isNullable() {
            return this.isNullable;
        }

        boolean isGeometry() {
            return this.isGeometryColumn;
        }

        int getSRS() {
            return this.srsCode;
        }

        boolean isPartOfPK() {
            return this.isPartOfPK;
        }

        boolean isFK() {
            return isFK;
        }
    }
}
