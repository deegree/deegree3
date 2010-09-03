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

    private final QName ftName;

    private final String table;

    private final String fidColumn;

    private final String backendSrs;

    private final Map<QName, MappingExpression> propToColumn;

    /**
     * Creates a new {@link FeatureTypeMapping} instance.
     * 
     * @param ftName
     *            name of the mapped feature type, must not be <code>null</code>
     * @param table
     *            name of the database table that the feature type is mapped to, may be <code>null</code> (for BLOB-only
     *            mappings)
     * @param fidColumn
     *            name of the columns where the feature id is stored, may be <code>null</code> (for BLOB-only mappings)
     * @param propToColumn
     *            mapping parameters for the properties of the feature type, may be <code>null</code> (for BLOB-only
     *            mappings)
     * @param backendSrs
     *            the native SRS identifier used by the backend, may be <code>null</code> (for BLOB-only mappings)
     */
    public FeatureTypeMapping( QName ftName, String table, String fidColumn,
                               Map<QName, MappingExpression> propToColumn, String backendSrs ) {
        this.ftName = ftName;
        this.table = table;
        this.fidColumn = fidColumn;
        this.propToColumn = propToColumn;
        this.backendSrs = backendSrs;

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
     * Returns the mapping parameters for the specified column.
     * 
     * @param propName
     *            name of the property, must not be <code>null</code>
     * @return mapping, may be <code>null</code> (if the property is not mapped)
     */
    public MappingExpression getMapping( QName propName ) {
        return propToColumn.get( propName );
    }

    /**
     * Returns the native SRS identifier used by the backend.
     * 
     * @return the native SRS identifier used by the backend, may be <code>null</code> (for BLOB-only mappings)
     */
    public String getBackendSrs() {
        return backendSrs;
    }
}
