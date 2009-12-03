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
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.deegree.commons.utils.Pair;
import org.deegree.feature.persistence.postgis.jaxbconfig.DBColumn;
import org.deegree.feature.persistence.postgis.jaxbconfig.FeatureJoinTable;
import org.deegree.feature.persistence.postgis.jaxbconfig.FeaturePropertyMappingType;
import org.deegree.feature.persistence.postgis.jaxbconfig.GeometryDBColumn;
import org.deegree.feature.persistence.postgis.jaxbconfig.GeometryPropertyMappingType;
import org.deegree.feature.persistence.postgis.jaxbconfig.GeometryPropertyTable;
import org.deegree.feature.persistence.postgis.jaxbconfig.GlobalMappingHints;
import org.deegree.feature.persistence.postgis.jaxbconfig.MeasurePropertyMappingType;
import org.deegree.feature.persistence.postgis.jaxbconfig.PropertyMappingType;
import org.deegree.feature.persistence.postgis.jaxbconfig.PropertyTable;
import org.deegree.feature.persistence.postgis.jaxbconfig.SimplePropertyMappingType;
import org.deegree.feature.types.ApplicationSchema;
import org.deegree.feature.types.FeatureType;
import org.deegree.feature.types.property.FeaturePropertyType;
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
     * @param dbSchema
     *            optional db schema, can be null
     * @param writer
     */
    public void writeCreateScript( String dbSchema, PrintWriter writer ) {

        if ( dbSchema == null ) {
            dbSchema = "";
        }

        if ( dbSchema.length() != 0 ) {
            writer.println( "/* --- BEGIN schema setup --- */" );
            writer.println();
            writer.println( "CREATE SCHEMA " + dbSchema + ";" );
            writer.println( "SET search_path TO " + dbSchema + ",public;" );
            writer.println();
            writer.println( "/* --- END schema setup --- */" );
            writer.println();
        }

        FeatureType[] fts = appSchema.getFeatureTypes();
        Arrays.sort( fts, new Comparator<FeatureType>() {
            @Override
            public int compare( FeatureType o1, FeatureType o2 ) {
                return o1.getName().toString().compareTo( o2.getName().toString() );
            }
        } );

        if ( getGlobalHints().isUseObjectLookupTable() ) {
            writeCreateGeneral( fts, writer, dbSchema );
        }

        for ( FeatureType ft : fts ) {
            if ( !ft.isAbstract() ) {
                writer.println();
                writeCreateFeatureType( ft, writer, dbSchema );
            }
        }

        writer.flush();
    }

    private void writeCreateGeneral( FeatureType[] fts, PrintWriter writer, String dbSchema ) {

        writer.println( "/* --- BEGIN global section --- */" );
        writer.println();
        writer.println( "CREATE SEQUENCE internal_id_seq;" );
        writer.println();
        writer.println( "CREATE TABLE feature_types (" );
        writer.println( "    id smallint PRIMARY KEY," );
        writer.println( "    qname text NOT NULL," );
        writer.println( "    tablename varchar(32) NOT NULL" );
        writer.println( ");" );
        writer.println( "COMMENT ON TABLE feature_types IS 'All concrete feature types and their tables';" );
        writer.println();
        writer.println( "SELECT ADDGEOMETRYCOLUMN('" + dbSchema
                        + "', 'feature_types','wgs84bbox','4326','GEOMETRY',2);" );
        writer.println( "ALTER TABLE feature_types ADD CONSTRAINT feature_types_check_bbox CHECK (isvalid(wgs84bbox));" );
        writer.println( "/* (no spatial index needed, as envelope is only used for keeping track of feature type extents) */" );

        int typeId = 0;
        for ( FeatureType ft : fts ) {
            if ( !ft.isAbstract() ) {
                QName qName = ft.getName();
                String tableName = ftNamesToHints.get( qName ).getFeatureTypeHints().getDBTable();
                writer.println( "INSERT INTO feature_types (id,qname,tablename) VALUES (" + ( typeId++ ) + ",'" + qName
                                + "', '" + tableName + "');" );
            }
        }

        writer.println();
        writer.println( "CREATE TABLE gml_objects (" );
        writer.println( "    id SERIAL PRIMARY KEY," );
        writer.println( "    gml_id text UNIQUE NOT NULL," );
        writer.println( "    gml_description text," );
        writer.println( "    ft_type smallint REFERENCES feature_types," );
        writer.println( "    binary_object bytea" );
        writer.println( ");" );
        writer.println( "COMMENT ON TABLE gml_objects IS 'All objects (features and geometries)';" );

        writer.println( "SELECT ADDGEOMETRYCOLUMN('" + dbSchema
                        + "', 'gml_objects','gml_bounded_by','-1','GEOMETRY',2);" );
        writer.println( "ALTER TABLE gml_objects ADD CONSTRAINT gml_objects_geochk CHECK (isvalid(gml_bounded_by));" );
        writer.println( "CREATE INDEX gml_objects_sidx ON gml_objects USING GIST ( gml_bounded_by GIST_GEOMETRY_OPS );" );
        writer.println();

        writer.println( "CREATE TABLE gml_names (" );
        writer.println( "    gml_object_id integer REFERENCES GML_OBJECTS," );
        writer.println( "    name text NOT NULL," );
        writer.println( "    codespace text," );
        writer.println( "    prop_idx smallint NOT NULL" );
        writer.println( ");" );
        writer.println();
        writer.println( "/* --- END global section --- */" );
    }

    private void writeCreateFeatureType( FeatureType ft, PrintWriter writer, String dbSchema ) {

        List<String> additionalCreates = new ArrayList<String>();
        List<Pair<String, String>> comments = new ArrayList<Pair<String, String>>();
        FeatureTypeMapping ftMapping = getFtMapping( ft.getName() );
        String tableName = ftMapping.getFeatureTypeHints().getDBTable().toLowerCase();
        int i = 1;

        writer.println( "/* --- BEGIN Feature type '" + ft.getName() + "' --- */" );
        writer.println();
        writer.println( "CREATE TABLE " + tableName + " (" );
        writer.print( "    id integer PRIMARY KEY REFERENCES GML_OBJECTS" );
        comments.add( new Pair<String, String>( "id", "Internal id" ) );
        for ( PropertyType<?> pt : ft.getPropertyDeclarations() ) {
            PropertyMappingType propMapping = ftMapping.getPropertyHints( pt.getName() );
            if ( propMapping instanceof SimplePropertyMappingType ) {
                SimplePropertyMappingType simplePropMapping = (SimplePropertyMappingType) propMapping;
                if ( simplePropMapping.getDBColumn() != null ) {
                    DBColumn dbColumn = simplePropMapping.getDBColumn();
                    comments.add( new Pair<String, String>( dbColumn.getName().toLowerCase(),
                                                            "Property " + pt.getName().getLocalPart() + " (simple)") );
                    writer.print( ",\n    " + dbColumn.getName().toLowerCase() + " " + dbColumn.getSqlType() );
                    if ( pt.getMinOccurs() > 0 ) {
                        writer.print( " NOT NULL" );
                    }
                } else {
                    additionalCreates.add( createPropertyTable( tableName, simplePropMapping.getPropertyTable() ) );
                    String comment = "COMMENT ON TABLE " + simplePropMapping.getPropertyTable().getTable() + " IS '"
                                     + ft.getName().getLocalPart() + ", property " + pt.getName().getLocalPart()
                                     + " (simple)';\n";
                    additionalCreates.add( comment );
                }
            } else if ( propMapping instanceof GeometryPropertyMappingType ) {
                GeometryPropertyMappingType geometryPropMapping = (GeometryPropertyMappingType) propMapping;
                if ( geometryPropMapping.getGeometryDBColumn() != null ) {
                    GeometryDBColumn dbColumn = geometryPropMapping.getGeometryDBColumn();
                    comments.add( new Pair<String, String>( dbColumn.getName().toLowerCase(),
                                                            "Property " + pt.getName().getLocalPart() + " (geometry)") );
                    comments.add( new Pair<String, String>( dbColumn.getName().toLowerCase() + "_ID",
                                            "Property " + pt.getName().getLocalPart() + " (geometry id)") );
                    writer.print( ",\n    " + dbColumn.getName() + "_ID integer REFERENCES gml_objects" );
                    String create = "SELECT ADDGEOMETRYCOLUMN('" + dbSchema + "','" + tableName + "','"
                                    + dbColumn.getName().toLowerCase() + "','" + dbColumn.getSrid() + "','"
                                    + dbColumn.getSqlType() + "'," + dbColumn.getDimension() + ");\n";
                    create += "ALTER TABLE " + tableName + " ADD CONSTRAINT " + tableName + "_geochk" + i
                              + " CHECK (isvalid(" + dbColumn.getName().toLowerCase() + "));\n";
                    create += "CREATE INDEX " + tableName + "_sidx" + i + " ON " + tableName + " USING GIST ( "
                              + dbColumn.getName().toLowerCase() + " GIST_GEOMETRY_OPS );\n";
                    additionalCreates.add( create );
                    comments.add( new Pair<String, String>( dbColumn.getName().toLowerCase() + "_xlink",
                                            "Property " + pt.getName().getLocalPart() + " (geometry URI)") );                    
                    writer.print( ",\n    " + dbColumn.getName().toLowerCase() + "_xlink text" );
                    i++;
                } else {
                    additionalCreates.add( createPropertyTable( tableName,
                                                                geometryPropMapping.getGeometryPropertyTable(),
                                                                dbSchema ) );
                    String comment = "COMMENT ON TABLE " + geometryPropMapping.getGeometryPropertyTable().getTable()
                                     + " IS '" + ft.getName().getLocalPart() + ", property "
                                     + pt.getName().getLocalPart() + " (geometry)';\n";
                    additionalCreates.add( comment );
                }
            } else if ( propMapping instanceof FeaturePropertyMappingType ) {
                FeaturePropertyMappingType featurePropMapping = (FeaturePropertyMappingType) propMapping;

                FeatureType targetFt = ( (FeaturePropertyType) pt ).getValueFt();
                // TODO also non-abstract types may have derived types
                boolean isTargetTypeUnique = targetFt == null ? false : !targetFt.isAbstract();

                if ( featurePropMapping.getDBColumn() != null ) {
                    DBColumn dbColumn = featurePropMapping.getDBColumn();
                    String valueFeatureTypeName = targetFt == null ? "Any feature" : targetFt.getName().getLocalPart();
                    if ( !isTargetTypeUnique ) {
                        valueFeatureTypeName += " (abstract)";
                    }
                    comments.add( new Pair<String, String>( dbColumn.getName().toLowerCase(),
                                                            "Property " + pt.getName().getLocalPart()
                                                                                    + " (feature), value feature type: "
                                                                                    + valueFeatureTypeName ) );
                    writer.print( ",\n    " + dbColumn.getName().toLowerCase() + " integer REFERENCES gml_objects" );
                    if ( !isTargetTypeUnique ) {
                        writer.print( ",\n    " + dbColumn.getName().toLowerCase()
                                      + "_ft smallint REFERENCES feature_types" );
                        comments.add( new Pair<String, String>( dbColumn.getName().toLowerCase() + "_ft",
                                                "Property " + pt.getName().getLocalPart()
                                                                        + " (feature type id)" ) );                
                    }

                    comments.add( new Pair<String, String>( dbColumn.getName().toLowerCase() + "_xlink",
                                            "Property " + pt.getName().getLocalPart() + " (feature URI)") );                    
                    writer.print( ",\n    " + dbColumn.getName().toLowerCase() + "_xlink text" );
                } else {
                    additionalCreates.add( createPropertyTable( tableName, featurePropMapping.getFeatureJoinTable(),
                                                                isTargetTypeUnique ) );
                    String comment = "COMMENT ON TABLE " + featurePropMapping.getFeatureJoinTable().getTable()
                                     + " IS '" + ft.getName().getLocalPart() + ", property "
                                     + pt.getName().getLocalPart() + " (feature)';\n";
                    additionalCreates.add( comment );
                }
            } else if ( propMapping instanceof MeasurePropertyMappingType ) {
                MeasurePropertyMappingType measurePropMapping = (MeasurePropertyMappingType) propMapping;
                if ( measurePropMapping.getDBColumn() != null ) {
                    DBColumn dbColumn = measurePropMapping.getDBColumn();
                    comments.add( new Pair<String, String>( dbColumn.getName().toLowerCase(),
                                            "Property " + pt.getName().getLocalPart() + "(measure, text)" ) );                       
                    writer.print( ",\n    " + dbColumn.getName().toLowerCase() + " double precision" );
                    if ( pt.getMinOccurs() > 0 ) {
                        writer.print( " NOT NULL" );
                    }
                    comments.add( new Pair<String, String>( dbColumn.getName().toLowerCase(),
                                            "Property " + pt.getName().getLocalPart() + "_uom (measure, uom attribute)" ) );                    
                    writer.print( ",\n    " + dbColumn.getName().toLowerCase() + "_uom text" );                    
                } else {
                    additionalCreates.add( createMeasurePropertyTable( tableName, measurePropMapping.getPropertyTable() ) );
                    String comment = "COMMENT ON TABLE " + measurePropMapping.getPropertyTable().getTable() + " IS '"
                                     + ft.getName().getLocalPart() + ", property " + pt.getName().getLocalPart()
                                     + " (measure)';\n";
                    additionalCreates.add( comment );
                }
            } else {
                throw new RuntimeException( "Unhandled property mapping: " + propMapping.getClass() );
            }
        }
        writer.println( "\n);" );
        writer.println( "COMMENT ON TABLE " + tableName + " IS '" + ft.getName().getLocalPart() + "';" );

        // write collected creates for property and join tables
        for ( String create : additionalCreates ) {
            writer.println();
            writer.print( create );
        }
        for ( Pair<String, String> comment : comments ) {
            writer.println( "COMMENT ON COLUMN " + tableName + "." + comment.first + " IS '" + comment.second + "';" );
        }
        writer.println();
        writer.println( "/* --- END Feature type '" + ft.getName() + "' --- */" );
    }

    private String createPropertyTable( String sourcefeatureTable, PropertyTable propTable ) {
        String tableName = propTable.getTable().toLowerCase();
        String s = "";
        s += "CREATE TABLE " + tableName + " (\n";
        s += "    feature_id integer NOT NULL REFERENCES " + sourcefeatureTable + ",\n";
        s += "    " + propTable.getColumn().toLowerCase() + " " + propTable.getSqlType() + " NOT NULL,\n";
        s += "    prop_idx smallint NOT NULL\n";
        s += ");\n";
        return s;
    }

    private String createPropertyTable( String sourcefeatureTable, GeometryPropertyTable propTable, String dbSchema ) {
        String tableName = propTable.getTable().toLowerCase();
        String s = "";
        s += "CREATE TABLE " + tableName + " (\n";
        s += "    feature_id integer NOT NULL REFERENCES " + sourcefeatureTable + " ,\n";
        s += "    geometry_id integer,\n";
        s += "    geometry_xlink text,\n";
        s += "    prop_idx smallint NOT NULL\n";
        s += ");\n";
        s += "SELECT ADDGEOMETRYCOLUMN('" + dbSchema + "','" + tableName + "','geometry','" + propTable.getSrid()
             + "','" + propTable.getSqlType() + "'," + propTable.getDimension() + ");\n";
        s += "ALTER TABLE " + tableName + " ADD CONSTRAINT " + tableName + "_geo_check CHECK (isvalid(geometry));\n";
        s += "CREATE INDEX " + tableName + "_sidx ON " + tableName + " USING GIST ( geometry GIST_GEOMETRY_OPS );\n";
        return s;
    }

    private String createPropertyTable( String sourcefeatureTable, FeatureJoinTable joinTable,
                                        boolean isTargetTypeUnique ) {
        String tableName = joinTable.getTable().toLowerCase();
        String s = "";
        s += "CREATE TABLE " + tableName + " (\n";
        s += "    feature_from_id integer NOT NULL REFERENCES " + sourcefeatureTable + ",\n";
        s += "    feature_to_id integer,\n";
        if ( !isTargetTypeUnique ) {
            s += "    feature_to_ft smallint REFERENCES feature_types,\n";
        }
        s += "    feature_id_to_xlink text,\n";
        s += "    prop_idx smallint NOT NULL\n";
        s += ");\n";
        return s;
    }

    private String createMeasurePropertyTable( String sourcefeatureTable, PropertyTable propTable ) {
        String tableName = propTable.getTable().toLowerCase();
        String s = "";
        s += "CREATE TABLE " + tableName + " (\n";
        s += "    feature_id integer NOT NULL REFERENCES " + sourcefeatureTable + ",\n";
        s += "    measure double precision NOT NULL,\n";
        s += "    uom text NOT NULL,\n";
        s += "    prop_idx smallint NOT NULL\n";
        s += ");\n";
        return s;
    }
}
