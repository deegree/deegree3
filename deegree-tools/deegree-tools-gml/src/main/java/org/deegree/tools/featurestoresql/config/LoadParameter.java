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

import org.deegree.feature.persistence.sql.mapper.ReferenceData;

import java.util.List;

import javax.xml.namespace.QName;

/**
 * Load parameter.
 *
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