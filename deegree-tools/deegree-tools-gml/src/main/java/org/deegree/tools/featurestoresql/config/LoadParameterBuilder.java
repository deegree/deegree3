package org.deegree.tools.featurestoresql.config;

import java.util.List;

import javax.xml.namespace.QName;

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

    private void validateSrid( String srid ) {
        try {
            Integer.valueOf( srid );
        } catch ( NumberFormatException e ) {
            throw new IllegalArgumentException( "Could not parse srid " + srid + " as integer" );
        }
    }

}