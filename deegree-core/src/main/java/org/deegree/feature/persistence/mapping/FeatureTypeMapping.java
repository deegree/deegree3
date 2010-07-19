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
package org.deegree.feature.persistence.mapping;

import static org.deegree.feature.persistence.mapping.FeatureTypeMapping.MappingType.RELATIONAL;

import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import org.deegree.feature.types.FeatureType;

/**
 * Defines the mapping between a {@link FeatureType} and a relational database.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class FeatureTypeMapping {

    public enum MappingType {
        /** Each property of a feature is decomposed to an individual table column or a property table. */
        RELATIONAL,
        /** Each feature is stored as a BLOB */
        BLOB,
        /** Feature type use BLOB and (partial) RELATIONAL mapping. */
        DUAL
    }

    private MappingType type;

    private QName ftName;

    private String table;

    private BlobMapping blobMapping;

    private String fidColumn;

    private String backendSrs;

    // TODO: enable more mapping possibilities (e.g. app:Country/gmlName[1]/text()='Blabla')
    private Map<QName, MappingExpression> propToColumn = new HashMap<QName, MappingExpression>();

    public FeatureTypeMapping( QName ftName, String table, String fidColumn,
                               Map<QName, MappingExpression> propToColumn2, String backendSrs ) {
        this.ftName = ftName;
        this.table = table;
        this.fidColumn = fidColumn;
        this.propToColumn = propToColumn2;
        this.backendSrs = backendSrs;
        // TODO make this configurable
        this.type = RELATIONAL;
    }

    public MappingType getMappingType() {
        return type;
    }

    /**
     * Returns the name of the feature type.
     * 
     * @return the name of the feature type, never <code>null</code>
     */
    public QName getFeatureType() {
        return ftName;
    }

    /**
     * Returns the name of the table that the feature type is mapped to.
     * 
     * @return the name of the table, may be <code>null</code> (for BLOB-only mappings)
     */
    public String getFtTable() {
        return table;
    }

    /**
     * Returns the names of the column that stores the id of the feature.
     * 
     * @return the names of the columns that stores the id of the feature, may be <code>null</code> (for BLOB-only
     *         mappings)
     */
    public String getFidColumn() {
        return fidColumn;
    }

    /**
     * Returns the mapping for the specified column.
     * 
     * @param propName
     *            name of the property, must not be <code>null</code>
     * @return mapping, may be <code>null</code> (if the property is not mapped)
     */
    public MappingExpression getMapping( QName propName ) {
        return propToColumn.get( propName );
    }

    public String getBackendSrs() {
        return backendSrs;
    }
}
