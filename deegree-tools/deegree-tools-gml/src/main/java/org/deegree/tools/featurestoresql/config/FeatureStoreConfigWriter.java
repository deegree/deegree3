/*-
 * #%L
 * deegree-cli-utility
 * %%
 * Copyright (C) 2016 - 2017 weichand.de, lat/lon GmbH
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */
package org.deegree.tools.featurestoresql.config;

import static org.deegree.feature.types.property.GeometryPropertyType.CoordinateDimension.DIM_2;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.commons.xml.stax.IndentingXMLStreamWriter;
import org.deegree.cs.persistence.CRSManager;
import org.deegree.cs.refs.coordinatesystem.CRSRef;
import org.deegree.feature.persistence.sql.GeometryStorageParams;
import org.deegree.feature.persistence.sql.MappedAppSchema;
import org.deegree.feature.persistence.sql.config.SQLFeatureStoreConfigWriter;
import org.deegree.feature.persistence.sql.ddl.DDLCreator;
import org.deegree.feature.persistence.sql.mapper.AppSchemaMapper;
import org.deegree.feature.types.AppSchema;
import org.deegree.sqldialect.SQLDialect;
import org.deegree.sqldialect.oracle.OracleDialect;
import org.deegree.sqldialect.postgis.PostGISDialect;
import org.slf4j.Logger;
import org.springframework.batch.item.ItemWriter;

/**
 * Item writer creating FeatureStore config file.
 * 
 * @author Juergen Weichand
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class FeatureStoreConfigWriter implements ItemWriter<AppSchema> {

    private static final Logger LOG = getLogger( FeatureStoreConfigWriter.class );

    private final LoadParameter loadParameter;

    public FeatureStoreConfigWriter( LoadParameter loadParameter ) {
        this.loadParameter = loadParameter;
    }

    @Override
    public void write( List<? extends AppSchema> appSchemas )
                            throws Exception {
        if ( appSchemas.isEmpty() )
            return;

        AppSchema appSchema = appSchemas.get( 0 );

        CRSRef storageCrs = CRSManager.getCRSRef( "EPSG:" + loadParameter.getSrid() );
        GeometryStorageParams geometryParams = new GeometryStorageParams( storageCrs, loadParameter.getSrid(), DIM_2 );
        SQLDialect sqlDialect = instantiateDialect( loadParameter.getDialect() );
        AppSchemaMapper mapper = new AppSchemaMapper( appSchema, !loadParameter.isRelationalMapping(),
                                                      loadParameter.isRelationalMapping(), geometryParams,
                                                      sqlDialect.getMaxColumnNameLength(), true,
                                                      loadParameter.isUseIntegerFids(), loadParameter.getDepth(),
                                                      loadParameter.getReferenceData() );
        MappedAppSchema mappedSchema = mapper.getMappedSchema();
        SQLFeatureStoreConfigWriter configWriter = new SQLFeatureStoreConfigWriter(
                                                                                    mappedSchema,
                                                                                    loadParameter.getPropertiesWithPrimitiveHref() );
        String uriPathToSchema = new URI( loadParameter.getSchemaUrl() ).getPath();
        String schemaFileName = uriPathToSchema.substring( uriPathToSchema.lastIndexOf( '/' ) + 1 );
        String fileName = schemaFileName.replaceFirst( "[.][^.]+$", "" );

        String format = loadParameter.getFormat();
        if ( format.equals( "all" ) ) {
            writeSqlDdlFile( mappedSchema, fileName, sqlDialect );
            writeXmlConfigFile( configWriter, fileName );
        } else if ( format.equals( "deegree" ) ) {
            writeXmlConfigFile( configWriter, fileName );
        } else if ( format.equals( "ddl" ) ) {
            writeSqlDdlFile( mappedSchema, fileName, sqlDialect );
        }
    }

    private void writeSqlDdlFile( MappedAppSchema mappedSchema, String fileName, SQLDialect sqlDialect )
                            throws IOException {
        String[] createStmts = DDLCreator.newInstance( mappedSchema, sqlDialect ).getDDL();
        String sqlOutputFilename = fileName + ".sql";
        Path pathToSqlOutputFile = Paths.get( sqlOutputFilename );
        LOG.info( "Writing SQL DDL into file: " + pathToSqlOutputFile.toUri() );
        try (BufferedWriter writer = Files.newBufferedWriter( pathToSqlOutputFile )) {
            for ( String sqlStatement : createStmts ) {
                writer.write( sqlStatement + ";" + System.getProperty( "line.separator" ) );
            }
        }
    }

    private void writeXmlConfigFile( SQLFeatureStoreConfigWriter configWriter, String fileName )
                            throws XMLStreamException, IOException {
        List<String> configUrls = Collections.singletonList( loadParameter.getSchemaUrl() );
        String xmlOutputFilename = fileName + ".xml";
        Path pathToXmlOutputFile = Paths.get( xmlOutputFilename );
        LOG.info( "Writing deegree SQLFeatureStore configuration into file: " + pathToXmlOutputFile.toUri() );
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        XMLStreamWriter xmlWriter = XMLOutputFactory.newInstance().createXMLStreamWriter( bos );
        xmlWriter = new IndentingXMLStreamWriter( xmlWriter );
        configWriter.writeConfig( xmlWriter, fileName + "DS", configUrls );
        xmlWriter.close();
        Files.write( pathToXmlOutputFile, bos.toString().getBytes( StandardCharsets.UTF_8 ) );
    }

    private SQLDialect instantiateDialect( String dialect ) {
        if ( dialect != null && "oracle".equalsIgnoreCase( dialect ) )
            return new OracleDialect( "", 11, 2 );
        return new PostGISDialect( "2.0.0" );
    }

}