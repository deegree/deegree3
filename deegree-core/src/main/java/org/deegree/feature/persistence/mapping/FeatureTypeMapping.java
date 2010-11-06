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

import org.deegree.commons.jdbc.QTableName;
import org.deegree.feature.persistence.mapping.id.FIDMapping;
import org.deegree.feature.persistence.mapping.property.Mapping;
import org.deegree.feature.types.FeatureType;

/**
 * Defines the mapping between a {@link FeatureType} and a table in a relational database.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class FeatureTypeMapping {

    private final QName ftName;

    private final QTableName table;

    private final FIDMapping fidMapping;

    private final Map<QName, Mapping> propToMapping;

    /**
     * Creates a new {@link FeatureTypeMapping} instance.
     * 
     * @param ftName
     *            name of the mapped feature type, must not be <code>null</code>
     * @param table
     *            name of the database table that the feature type is mapped to, must not be <code>null</code>
     * @param fidMapping
     *            mapping for the feature id, must not be <code>null</code>
     * @param propToMapping
     *            mapping parameters for the properties of the feature type, must not be <code>null</code>
     */
    public FeatureTypeMapping( QName ftName, QTableName table, FIDMapping fidMapping, Map<QName, Mapping> propToMapping ) {
        this.ftName = ftName;
        this.table = table;
        this.fidMapping = fidMapping;
        this.propToMapping = propToMapping;
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
     * Returns the identifier of the table that the feature type is mapped to.
     * 
     * @return the identifier of the table, never <code>null</code>
     */
    public QTableName getFtTable() {
        return table;
    }

    /**
     * Returns the feature id mapping.
     * 
     * @return mapping for the feature id, never <code>null</code>
     */
    public FIDMapping getFidMapping() {
        return fidMapping;
    }

    /**
     * Returns the mapping parameters for the specified property.
     * 
     * @param propName
     *            name of the property, must not be <code>null</code>
     * @return mapping, may be <code>null</code> (if the property is not mapped)
     */
    public Mapping getMapping( QName propName ) {
        return propToMapping.get( propName );
    }
}