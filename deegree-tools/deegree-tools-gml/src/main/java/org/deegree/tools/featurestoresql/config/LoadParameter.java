package org.deegree.tools.featurestoresql.config;

import java.util.List;

import javax.xml.namespace.QName;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class LoadParameter {

    private String schemaUrl;

    private String format;

    private String dialect;

    private String srid;

    private boolean useIntegerFids;

    private boolean relationalMapping;

    private List<QName> propertiesWithPrimitiveHref;

    private int depth;

    LoadParameter() {
    }

    public String getSchemaUrl() {
        return schemaUrl;
    }

    public void setSchemaUrl( String schemaUrl ) {
        this.schemaUrl = schemaUrl;
    }

    public void setSrid( String srid ) {
        this.srid = srid;
    }

    public String getSrid() {
        return this.srid;
    }

    public boolean isUseIntegerFids() {
        return useIntegerFids;
    }

    public void setUseIntegerFids( boolean useIntegerFids ) {
        this.useIntegerFids = useIntegerFids;
    }

    public boolean isRelationalMapping() {
        return relationalMapping;
    }

    public void setRelationalMapping( boolean relationalMapping ) {
        this.relationalMapping = relationalMapping;
    }

    public List<QName> getPropertiesWithPrimitiveHref() {
        return propertiesWithPrimitiveHref;
    }

    public void setPropertiesWithPrimitiveHref( List<QName> propertiesWithPrimitiveHref ) {
        this.propertiesWithPrimitiveHref = propertiesWithPrimitiveHref;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat( String format ) {
        this.format = format;
    }

    public String getDialect() {
        return dialect;
    }

    public void setDialect( String dialect ) {
        this.dialect = dialect;
    }

    public void setDepth( int depth ) {
        this.depth = depth;
    }

    public int getDepth() {
        return depth;
    }

    /**
    public static void parseParameter( String[] args ) {
        for ( String arg : args ) {
            if ( arg.startsWith( "--format" ) ) {
                format = arg.split( "=" )[1];
                System.out.println( "Using format=" + format );
            } else if ( arg.startsWith( "--srid" ) ) {
                srid = Integer.valueOf( arg.split( "=" )[1] );
                System.out.println( "Using srid=" + srid );
            } else if ( arg.startsWith( "--idtype" ) ) {
                String idMappingArg = arg.split( "=" )[1];
                useIntegerFids = idMappingArg.equals( "uuid" ) ? false : true;
                System.out.println( "Using idtype=" + idMappingArg );
            } else if ( arg.startsWith( "--mapping" ) ) {
                String mapping = arg.split( "=" )[1];
                relationalMapping = mapping.equalsIgnoreCase( "blob" ) ? false : true;
                System.out.println( "Using mapping=" + mapping );
            } else if ( arg.startsWith( "--dialect" ) ) {
                dialect = arg.split( "=" )[1];
                System.out.println( "Using dialect=" + dialect );
            } else if ( arg.startsWith( "--listOfPropertiesWithPrimitiveHref" ) ) {
                String pathToFile = arg.split( "=" )[1];
                propertiesWithPrimitiveHref = propertyNameParser.parsePropertiesWithPrimitiveHref( pathToFile );
                System.out.println( "Using listOfPropertiesWithPrimitiveHref=" + propertiesWithPrimitiveHref );
            } else {
                schemaUrl = arg;
            }
        }
    }
    **/

}