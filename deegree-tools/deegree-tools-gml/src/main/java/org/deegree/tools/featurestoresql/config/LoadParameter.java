package org.deegree.tools.featurestoresql.config;

import org.deegree.feature.persistence.sql.mapper.ReferenceData;

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

    private ReferenceData referenceData;

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

    public ReferenceData getReferenceData() {
        return this.referenceData;
    }

    public void setReferenceData( ReferenceData referenceData ) {
        this.referenceData = referenceData;
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

}