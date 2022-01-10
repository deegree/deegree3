/*-
 * #%L
 * deegree-cli-utility
 * %%
 * Copyright (C) 2016 - 2021 lat/lon GmbH
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

import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.feature.persistence.sql.mapper.GmlReferenceData;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.List;

/**
 * Load parameter builder.
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class LoadParameterBuilder {

    private static final String DEFAULT_FORMAT = "deegree";

    private static final String DEFAULT_SRID = "4258";

    private static final boolean DEFAULT_USE_INTEGER_FIDS = true;

    private static final boolean DEFAULT_RELATIONAL_MAPPING = true;

    private static final String DEFAULT_DIALECT = "postgis";

    private static final int DEFAULT_DEPTH = 0;

    private final LoadParameter loadParameter;

    public LoadParameterBuilder() {
        this.loadParameter = new LoadParameter();
    }

    public LoadParameterBuilder setSchemaUrl( String schemaUrl ) {
        if (schemaUrl == null || schemaUrl.isEmpty())
            throw new IllegalArgumentException("Value for option 'schemaUrl' must not be null. Option is mandatory.");
        loadParameter.setSchemaUrl( schemaUrl );
        return this;
    }

    public LoadParameter build() {
        return loadParameter;
    }

    public LoadParameterBuilder setFormat( String format ) {
        if ( format == null )
            loadParameter.setFormat( DEFAULT_FORMAT );
        else
            loadParameter.setFormat( format );
        return this;
    }

    public LoadParameterBuilder setDialect( String dialect ) {
        if ( dialect == null )
            loadParameter.setDialect( DEFAULT_DIALECT );
        else
            loadParameter.setDialect( dialect );
        return this;
    }

    public LoadParameterBuilder setSrid( String srid ) {
        if ( srid == null )
            loadParameter.setSrid( DEFAULT_SRID );
        else {
            validateSrid( srid );
            loadParameter.setSrid( srid );
        }
        return this;
    }

    public LoadParameterBuilder setIdType( String idType ) {
        if ( idType == null )
            loadParameter.setUseIntegerFids( DEFAULT_USE_INTEGER_FIDS );
        else
            loadParameter.setUseIntegerFids( idType.equals( "uuid" ) ? false : true );
        return this;
    }

    public LoadParameterBuilder setMappingType( String mappingType ) {
        if ( mappingType == null )
            loadParameter.setRelationalMapping( DEFAULT_RELATIONAL_MAPPING );
        else
            loadParameter.setRelationalMapping( mappingType.equalsIgnoreCase( "blob" ) ? false : true );
        return this;
    }

    public LoadParameterBuilder setDepth( String depth ) {
        if ( depth == null || depth.isEmpty() ) {
            loadParameter.setDepth( DEFAULT_DEPTH );
        } else {
            try {
                loadParameter.setDepth( Integer.parseInt( depth ) );
            } catch ( NumberFormatException e ) {
                throw new IllegalArgumentException( "Invalid value of parameter cycledepth: " + depth
                                                    + ". Must be an integer value." );
            }
        }
        return this;
    }

    public LoadParameterBuilder setListOfPropertiesWithPrimitiveHref( String pathToFileWithPropertiesWithPrimitiveHref ) {
        if ( pathToFileWithPropertiesWithPrimitiveHref != null ) {
            PropertyNameParser propertyNameParser = new PropertyNameParser();
            List<QName> propertiesWithPrimitiveHref = propertyNameParser.parsePropertiesWithPrimitiveHref( pathToFileWithPropertiesWithPrimitiveHref );
            loadParameter.setPropertiesWithPrimitiveHref( propertiesWithPrimitiveHref );
        }
        return this;
    }

    public LoadParameterBuilder setReferenceData( String referenceData ) {
        if ( referenceData != null && !referenceData.isEmpty() ) {
            try {
                URL referenceDataUrl = Paths.get( referenceData ).toUri().toURL();
                GmlReferenceData gmlReferenceData = new GmlReferenceData( referenceDataUrl );
                loadParameter.setReferenceData( gmlReferenceData );
            } catch ( IOException e ) {
                throw new IllegalArgumentException( "Invalid value of parameter referenceData: " + referenceData
                                                    + ". Could not be read." );
            } catch ( XMLStreamException | UnknownCRSException e ) {
                throw new IllegalArgumentException( "Invalid value of parameter referenceData: " + referenceData
                                                    + ". Could not be parsed as GML 3.2." );
            }
        }
        return this;
    }

    private void validateSrid( String srid ) {
        try {
            Integer.valueOf( srid );
        } catch ( NumberFormatException e ) {
            throw new IllegalArgumentException( "Could not parse srid " + srid + " as integer" );
        }
    }
}