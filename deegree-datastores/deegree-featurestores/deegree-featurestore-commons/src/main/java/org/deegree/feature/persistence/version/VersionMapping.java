//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2015 by:
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
package org.deegree.feature.persistence.version;

import java.util.Collections;
import java.util.Map;

import org.deegree.commons.jdbc.SQLIdentifier;
import org.deegree.commons.tom.primitive.PrimitiveType;
import org.deegree.commons.tom.sql.DefaultPrimitiveConverter;
import org.deegree.commons.utils.Pair;
import org.deegree.feature.FeatureState;

/**
 * Encapsulates the mapping of the version columns.
 * 
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 */
public class VersionMapping {

    private final Pair<SQLIdentifier, PrimitiveType> versionColumn;

    private final Pair<SQLIdentifier, PrimitiveType> stateColumn;

    private final Map<String, FeatureState> stateMapping;

    private final String versionMetadataTable;

    private final Pair<SQLIdentifier, PrimitiveType> versionColumnInMetadataTable;

    private final Pair<SQLIdentifier, PrimitiveType> timestampColumn;

    /**
     * @param versionColumn
     *            the column containing the version of a feature, never <code>null</code>
     * @param stateColumn
     *            the column containing the state of a feature, never <code>null</code>
     * @param stateMapping
     *            the mapping between content of the state column from db an official states, may be <code>null</code>
     *            or empty
     */
    public VersionMapping( Pair<SQLIdentifier, PrimitiveType> versionColumn,
                           Pair<SQLIdentifier, PrimitiveType> stateColumn, Map<String, FeatureState> stateMapping,
                           String versionMetadataTable,
                           Pair<SQLIdentifier, PrimitiveType> versionColumnInMetadataTable,
                           Pair<SQLIdentifier, PrimitiveType> timestampColumn ) {
        this.versionColumn = versionColumn;
        this.stateColumn = stateColumn;
        this.stateMapping = stateMapping != null ? stateMapping : Collections.<String, FeatureState> emptyMap();
        this.versionMetadataTable = versionMetadataTable;
        this.versionColumnInMetadataTable = versionColumnInMetadataTable;
        this.timestampColumn = timestampColumn;
    }

    /**
     * @return the versionColumn the metadata of the column containing the version, never <code>null</code>
     */
    public Pair<SQLIdentifier, PrimitiveType> getVersionColumn() {
        return versionColumn;
    }

    /**
     * @return the converter for the version column, never <code>null</code>
     */
    public DefaultPrimitiveConverter getVersionColumnConverter() {
        return new DefaultPrimitiveConverter( versionColumn.getSecond(), versionColumn.getFirst().getName() );
    }

    /**
     * @return the versionColumn the metadata of the column containing the version, never <code>null</code>
     */
    public Pair<SQLIdentifier, PrimitiveType> getStateColumn() {
        return stateColumn;
    }

    /**
     * @return the converter for the state column, never <code>null</code>
     */
    public DefaultPrimitiveConverter getStateColumnConverter() {
        return new DefaultPrimitiveConverter( stateColumn.getSecond(), stateColumn.getFirst().getName() );
    }

    /**
     * @return the mapping between the content of the column from db an official states, may be empty but never
     *         <code>null</code>
     */
    public Map<String, FeatureState> getStateMapping() {
        return stateMapping;
    }

    public String getVersionMetadataTable() {
        return versionMetadataTable;
    }

    /**
     * @return the versionColumnInMetadataTable
     */
    public Pair<SQLIdentifier, PrimitiveType> getVersionColumnInMetadataTable() {
        return versionColumnInMetadataTable;
    }

    /**
     * @return the timestampColumn
     */
    public Pair<SQLIdentifier, PrimitiveType> getTimestampColumn() {
        return timestampColumn;
    }

}