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
package org.deegree.feature.persistence.postgis;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.deegree.feature.persistence.postgis.jaxbconfig.DBColumn;
import org.deegree.feature.persistence.postgis.jaxbconfig.FeatureJoinTable;
import org.deegree.feature.persistence.postgis.jaxbconfig.FeaturePropertyMappingType;
import org.deegree.feature.persistence.postgis.jaxbconfig.GeometryDBColumn;
import org.deegree.feature.persistence.postgis.jaxbconfig.GeometryPropertyMappingType;
import org.deegree.feature.persistence.postgis.jaxbconfig.GeometryPropertyTable;
import org.deegree.feature.persistence.postgis.jaxbconfig.GlobalMappingHints;
import org.deegree.feature.persistence.postgis.jaxbconfig.PropertyMappingType;
import org.deegree.feature.persistence.postgis.jaxbconfig.PropertyTable;
import org.deegree.feature.persistence.postgis.jaxbconfig.SimplePropertyMappingType;
import org.deegree.feature.types.ApplicationSchema;
import org.deegree.feature.types.FeatureType;
import org.deegree.feature.types.property.PropertyType;

/**
 * Encapsulates an {@link ApplicationSchema} and mapping information that map the feature types to a relational schema
 * stored in a PostGIS database.
 * 
 * @see ApplicationSchema
 * @see FeatureTypeMapping
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class PostGISApplicationSchema {

    private final ApplicationSchema appSchema;

    private final GlobalMappingHints globalHints;

    private final Map<QName, FeatureTypeMapping> ftNamesToHints;

    /**
     * @param appSchema
     * @param globalHints
     * @param ftNamesToHints
     */
    public PostGISApplicationSchema( ApplicationSchema appSchema, GlobalMappingHints globalHints,
                                     Map<QName, FeatureTypeMapping> ftNamesToHints ) {
        this.appSchema = appSchema;
        this.globalHints = globalHints;
        this.ftNamesToHints = ftNamesToHints;
    }

    /**
     * Returns the application schema.
     * 
     * @return the application schema
     */
    public ApplicationSchema getSchema() {
        return appSchema;
    }

    /**
     * Returns the global mapping hints that apply to all feature types of the schema.
     * 
     * @return the global mapping hints
     */
    public GlobalMappingHints getGlobalHints() {
        return globalHints;
    }

    /**
     * Returns the mapping hints for the specified feature type.
     * 
     * @param ftName
     *            name of the feature type, must not be <code>null</code>
     * @return the mapping hints for the specified feature type or <code>null</code> if the feature type does not exist
     *         in the schema
     */
    public FeatureTypeMapping getFtMapping( QName ftName ) {
        return ftNamesToHints.get( ftName );
    }

    /**
     * Prints out an SQL create script for the relational schema.
     * 
     * @param writer
     */
    public void writeCreateScript( PrintWriter writer ) {

        if ( getGlobalHints().isUseObjectLookupTable() ) {
            writeCreateGeneral( writer );
        }

        for ( FeatureType ft : appSchema.getFeatureTypes() ) {
            if ( !ft.isAbstract() ) {
                writer.println();
                writeCreateFeatureType( ft, writer );
            }
        }

        writer.flush();
    }

    private void writeCreateGeneral( PrintWriter writer ) {
        writer.println( "/* Global tables and sequence for internal ids */" );
        writer.println( "CREATE SEQUENCE INTERNAL_ID_SEQ;" );
        writer.println();
        writer.println( "CREATE TABLE OBJECT_TYPES (" );
        writer.println( "    ID SMALLINT PRIMARY KEY," );
        writer.println( "    QNAME VARCHAR(64) NOT NULL," );
        writer.println( "    TABLENAME VARCHAR(32) NOT NULL" );
        writer.println( ");" );
        writer.println();

        int typeId = 0;
        for ( FeatureType ft : appSchema.getFeatureTypes() ) {
            if ( !ft.isAbstract() ) {
                QName qName = ft.getName();
                String tableName = ftNamesToHints.get( qName ).getFeatureTypeHints().getDBTable();
                writer.println( "INSERT INTO OBJECT_TYPES (ID,QNAME,TABLENAME) VALUES (" + ( typeId++ ) + ",'" + qName
                                + "', '" + tableName + "')" );
            }
        }

        writer.println();        
        writer.println( "CREATE TABLE OBJECT_IDS (" );
        writer.println( "    ID INTEGER PRIMARY KEY," );
        writer.println( "    GML_ID VARCHAR(32) UNIQUE NOT NULL," );
        writer.println( "    TYPE SMALLINT NOT NULL REFERENCES OBJECT_TYPES" );
        writer.println( ");" );        
    }

    private void writeCreateFeatureType( FeatureType ft, PrintWriter writer ) {

        List<String> additionalCreates = new ArrayList<String>();

        FeatureTypeMapping ftMapping = getFtMapping( ft.getName() );
        writer.println( "/* Feature type '" + ft.getName() + "' */" );
        writer.println( "CREATE TABLE " + ftMapping.getFeatureTypeHints().getDBTable() + " (" );
        writer.print( "    ID INTEGER PRIMARY KEY REFERENCES OBJECT_IDS" );
        for ( PropertyType pt : ft.getPropertyDeclarations() ) {
            PropertyMappingType propMapping = ftMapping.getPropertyHints( pt.getName() );
            if ( propMapping instanceof SimplePropertyMappingType ) {
                SimplePropertyMappingType simplePropMapping = (SimplePropertyMappingType) propMapping;
                if ( simplePropMapping.getDBColumn() != null ) {
                    DBColumn dbColumn = simplePropMapping.getDBColumn();
                    writer.print( ",\n    " + dbColumn.getName() + " " + dbColumn.getSqlType() );
                } else {
                    additionalCreates.add( createPropertyTable( simplePropMapping.getPropertyTable() ) );
                }
            } else if ( propMapping instanceof GeometryPropertyMappingType ) {
                GeometryPropertyMappingType geometryPropMapping = (GeometryPropertyMappingType) propMapping;
                if ( geometryPropMapping.getGeometryDBColumn() != null ) {
                    GeometryDBColumn dbColumn = geometryPropMapping.getGeometryDBColumn();
                    String create = "ADDGEOMETRYCOLUMN('','" + ftMapping.getFeatureTypeHints().getDBTable() + "','"
                                    + dbColumn.getName() + "','" + dbColumn.getSrid() + "','" + dbColumn.getSqlType()
                                    + "'," + dbColumn.getDimension() + ");\n";
                    additionalCreates.add( create );
                } else {
                    additionalCreates.add( createPropertyTable( geometryPropMapping.getGeometryPropertyTable() ) );
                }
            } else if ( propMapping instanceof FeaturePropertyMappingType ) {
                FeaturePropertyMappingType featurePropMapping = (FeaturePropertyMappingType) propMapping;
                if ( featurePropMapping.getDBColumn() != null ) {
                    DBColumn dbColumn = featurePropMapping.getDBColumn();
                    writer.print( ",\n    " + dbColumn.getName() + " INTEGER REFERENCES OBJECT_IDS" );
                    writer.print( ",\n    " + dbColumn.getName() + "_XLINK VARCHAR(1024)" );
                } else {
                    additionalCreates.add( createPropertyTable( featurePropMapping.getFeatureJoinTable() ) );
                }
            } else {
                throw new RuntimeException( "Unhandled property mapping: " + propMapping.getClass() );
            }
        }
        writer.println( "\n);" );

        // writer collected creates for property and join tables
        for ( String create : additionalCreates ) {
            writer.println();
            writer.print( create );
        }
    }

    private String createPropertyTable( PropertyTable propTable ) {
        String s = "";
        s += "CREATE TABLE " + propTable.getTable() + " (\n";
        s += "    FEATURE INTEGER NOT NULL REFERENCES OBJECT_IDS,\n";
        s += "    " + propTable.getColumn() + " " + propTable.getSqlType() + " NOT NULL\n";
        s += ");\n";
        return s;
    }

    private String createPropertyTable( GeometryPropertyTable propTable ) {
        String s = "";
        s += "CREATE TABLE " + propTable.getTable() + " (\n";
        s += "    FEATURE INTEGER NOT NULL REFERENCES OBJECT_IDS\n";
        s += "    GEOM_EXT" + " BLOB\n";
        s += ");\n";
        s += "ADDGEOMETRYCOLUMN('','" + propTable.getTable() + "','GEOM','" + propTable.getSrid() + "','"
             + propTable.getSqlType() + "'," + propTable.getDimension() + ");\n";
        return s;
    }

    private String createPropertyTable( FeatureJoinTable joinTable ) {
        String s = "";
        s += "CREATE TABLE " + joinTable.getTable() + " (\n";
        s += "    FROM_FEATURE INTEGER NOT NULL REFERENCES OBJECT_IDS,\n";
        s += "    TO_FEATURE INTEGER REFERENCES OBJECT_IDS,\n";
        s += "    TO_FEATURE_XLINK VARCHAR(1024)\n";
        s += ");\n";
        return s;
    }
}
