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

        writer.println( "/* --- BEGIN global section --- */" );
        writer.println();
        writer.println( "CREATE SEQUENCE internal_id_seq;" );
        writer.println();
        writer.println( "CREATE TABLE feature_types (" );
        writer.println( "    id SMALLINT PRIMARY KEY," );
        writer.println( "    qname VARCHAR(64) NOT NULL," );
        writer.println( "    tablename VARCHAR(32) NOT NULL" );
        writer.println( ");" );
        writer.println ("COMMENT ON TABLE feature_types IS 'All concrete feature types and their tables';");
        writer.println();
        writer.println( "SELECT ADDGEOMETRYCOLUMN('', 'feature_types','wgs84bbox','4326','POLYGON',2);" );
        writer.println( "ALTER TABLE feature_types ADD CONSTRAINT feature_types_check_bbox CHECK (isvalid(wgs84bbox));" );
        writer.println( "/* (no spatial index needed, as envelope is only used for keeping track of feature type extents) */" );

        int typeId = 0;
        for ( FeatureType ft : appSchema.getFeatureTypes() ) {
            if ( !ft.isAbstract() ) {
                QName qName = ft.getName();
                String tableName = ftNamesToHints.get( qName ).getFeatureTypeHints().getDBTable();
                writer.println( "INSERT INTO feature_types (id,qname,tablename) VALUES (" + ( typeId++ ) + ",'" + qName
                                + "', '" + tableName + "');" );
            }
        }

        writer.println();
        writer.println( "CREATE TABLE gml_objects (" );
        writer.println( "    id INTEGER PRIMARY KEY," );
        writer.println( "    gml_id VARCHAR(32) UNIQUE NOT NULL," );
        writer.println( "    gml_description VARCHAR(256)," );
        writer.println( "    ft_type SMALLINT REFERENCES feature_types," );
        writer.println( "    binary_object OID NOT NULL" );
        writer.println( ");" );
        writer.println ("COMMENT ON TABLE gml_objects IS 'All objects (features and geometries)';");

        writer.println( "SELECT ADDGEOMETRYCOLUMN('', 'gml_objects','gml_bounded_by','-1','POLYGON',2);" );
        writer.println( "ALTER TABLE gml_objects ADD CONSTRAINT gml_objects_check_bounded_by CHECK (isvalid(gml_bounded_by));" );
        writer.println( "CREATE INDEX gml_objects_idx_spatial ON gml_objects USING GIST ( gml_bounded_by GIST_GEOMETRY_OPS );" );
        writer.println();

        writer.println( "CREATE TABLE gml_names (" );
        writer.println( "    gml_object_id INTEGER REFERENCES GML_OBJECTS," );
        writer.println( "    name VARCHAR(32) NOT NULL," );
        writer.println( "    prop_idx SMALLINT NOT NULL" );
        writer.println( ");" );
        writer.println();
        writer.println( "/* --- END global section --- */" );
    }

    private void writeCreateFeatureType( FeatureType ft, PrintWriter writer ) {

        List<String> additionalCreates = new ArrayList<String>();
        FeatureTypeMapping ftMapping = getFtMapping( ft.getName() );
        String tableName = ftMapping.getFeatureTypeHints().getDBTable().toLowerCase();
        int i = 1;

        writer.println( "/* --- BEGIN Feature type '" + ft.getName() + "' --- */" );
        writer.println();
        writer.println( "CREATE TABLE " + tableName + " (" );
        writer.print( "    id INTEGER PRIMARY KEY REFERENCES GML_OBJECTS" );
        for ( PropertyType pt : ft.getPropertyDeclarations() ) {
            PropertyMappingType propMapping = ftMapping.getPropertyHints( pt.getName() );
            if ( propMapping instanceof SimplePropertyMappingType ) {
                SimplePropertyMappingType simplePropMapping = (SimplePropertyMappingType) propMapping;
                if ( simplePropMapping.getDBColumn() != null ) {
                    DBColumn dbColumn = simplePropMapping.getDBColumn();
                    writer.print( ",\n    " + dbColumn.getName().toLowerCase() + " " + dbColumn.getSqlType() );
                } else {
                    additionalCreates.add( createPropertyTable( tableName, simplePropMapping.getPropertyTable() ) );
                }
            } else if ( propMapping instanceof GeometryPropertyMappingType ) {
                GeometryPropertyMappingType geometryPropMapping = (GeometryPropertyMappingType) propMapping;
                if ( geometryPropMapping.getGeometryDBColumn() != null ) {
                    GeometryDBColumn dbColumn = geometryPropMapping.getGeometryDBColumn();
                    writer.print( ",\n    " + dbColumn.getName() + "_ID INTEGER REFERENCES GML_OBJECTS" );
                    String create = "SELECT ADDGEOMETRYCOLUMN('','" + tableName + "','" + dbColumn.getName().toLowerCase()
                                    + "','" + dbColumn.getSrid() + "','" + dbColumn.getSqlType() + "',"
                                    + dbColumn.getDimension() + ");\n";
                    create += "ALTER TABLE " + tableName + " ADD CONSTRAINT " + tableName + "_check_geom" + i
                              + " CHECK (isvalid(" + dbColumn.getName().toLowerCase() + "));\n";
                    create += "CREATE INDEX " + tableName + "_idx_spatial" + i + " ON " + tableName + " USING GIST ( "
                              + dbColumn.getName().toLowerCase() + " GIST_GEOMETRY_OPS );\n";
                    additionalCreates.add( create );
                    writer.print( ",\n    " + dbColumn.getName().toLowerCase() + "_xlink VARCHAR(1024)" );
                    i++;
                } else {
                    additionalCreates.add( createPropertyTable( tableName, geometryPropMapping.getGeometryPropertyTable() ) );
                }
            } else if ( propMapping instanceof FeaturePropertyMappingType ) {
                FeaturePropertyMappingType featurePropMapping = (FeaturePropertyMappingType) propMapping;
                if ( featurePropMapping.getDBColumn() != null ) {
                    DBColumn dbColumn = featurePropMapping.getDBColumn();
                    writer.print( ",\n    " + dbColumn.getName().toLowerCase() + " INTEGER REFERENCES GML_OBJECTS" );
                    writer.print( ",\n    " + dbColumn.getName().toLowerCase() + "_xlink VARCHAR(1024)" );
                } else {
                    additionalCreates.add( createPropertyTable( tableName, featurePropMapping.getFeatureJoinTable() ) );
                }
            } else {
                throw new RuntimeException( "Unhandled property mapping: " + propMapping.getClass() );
            }
        }
        writer.println( "\n);" );
        writer.println ("COMMENT ON TABLE " + tableName + " IS '" + ft.getName()+ " main table';");

        // write collected creates for property and join tables
        for ( String create : additionalCreates ) {
            writer.println();
            writer.print( create );
        }
        writer.println();
        writer.println( "/* --- END Feature type '" + ft.getName() + "' --- */" );
    }

    private String createPropertyTable(String sourcefeatureTable, PropertyTable propTable ) {
        String tableName = propTable.getTable().toLowerCase();
        String s = "";
        s += "CREATE TABLE " + tableName + " (\n";
        s += "    feature_id INTEGER NOT NULL REFERENCES " + sourcefeatureTable + ",\n";
        s += "    " + propTable.getColumn().toLowerCase() + " " + propTable.getSqlType() + " NOT NULL,\n";
        s += "    prop_idx SMALLINT NOT NULL\n";
        s += ");\n";
        return s;
    }

    private String createPropertyTable(String sourcefeatureTable, GeometryPropertyTable propTable ) {
        String tableName = propTable.getTable().toLowerCase();
        String s = "";
        s += "CREATE TABLE " + tableName + " (\n";
        s += "    feature_id INTEGER NOT NULL REFERENCES " + sourcefeatureTable + " ,\n";
        s += "    geometry_id INTEGER NOT NULL REFERENCES " + sourcefeatureTable + ",\n";
        s += "    geometry_xlink VARCHAR(1024),\n";
        s += "    prop_idx SMALLINT NOT NULL\n";
        s += ");\n";
        s += "SELECT ADDGEOMETRYCOLUMN('','" + tableName + "','geometry','" + propTable.getSrid() + "','"
             + propTable.getSqlType() + "'," + propTable.getDimension() + ");\n";
        s += "ALTER TABLE " + tableName + " ADD CONSTRAINT " + tableName
             + "_check_geometry CHECK (isvalid(geometry));\n";
        s += "CREATE INDEX " + tableName + "_idx_spatial ON " + tableName
             + " USING GIST ( geometry GIST_GEOMETRY_OPS );\n";
        return s;
    }

    private String createPropertyTable(String sourcefeatureTable, FeatureJoinTable joinTable ) {
        String tableName = joinTable.getTable().toLowerCase();
        String s = "";
        s += "CREATE TABLE " + tableName + " (\n";
        s += "    feature_id_from INTEGER NOT NULL REFERENCES " + sourcefeatureTable + ",\n";
        s += "    feature_id_to INTEGER REFERENCES gml_objects,\n";
        s += "    feature_id_to_xlink VARCHAR(1024),\n";
        s += "    prop_idx SMALLINT NOT NULL\n";
        s += ");\n";
        return s;
    }
}
