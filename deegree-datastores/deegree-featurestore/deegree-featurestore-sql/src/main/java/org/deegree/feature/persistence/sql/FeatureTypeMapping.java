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
package org.deegree.feature.persistence.sql;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.deegree.commons.jdbc.QTableName;
import org.deegree.feature.persistence.sql.id.FIDMapping;
import org.deegree.feature.persistence.sql.rules.Mapping;
import org.deegree.feature.types.FeatureType;

/**
 * Defines the mapping between a {@link FeatureType} and tables in a relational database.
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

    private final List<Mapping> particles = new ArrayList<Mapping>();

    /**
     * Creates a new {@link FeatureTypeMapping} instance.
     * 
     * @param ftName
     *            name of the mapped feature type, must not be <code>null</code>
     * @param table
     *            name of the database table that the feature type is mapped to, must not be <code>null</code>
     * @param fidMapping
     *            mapping for the feature id, must not be <code>null</code>
     * @param particleMappings
     *            particle mappings for the feature type, must not be <code>null</code>
     */
    public FeatureTypeMapping( QName ftName, QTableName table, FIDMapping fidMapping, List<Mapping> particleMappings ) {
        this.ftName = ftName;
        this.table = table;
        this.fidMapping = fidMapping;
        this.propToMapping = new HashMap<QName, Mapping>();
        // TODO cope with non-QName XPaths as well
        for ( Mapping mapping : particleMappings ) {
            if ( mapping != null && mapping.getPath().getAsQName() != null ) {
                propToMapping.put( mapping.getPath().getAsQName(), mapping );
            }
        }
        for ( Mapping mapping : particleMappings ) {
            if ( mapping != null ) {
                this.particles.add( mapping );
            }
        }
    }

    /**
     * Returns the name of the feature type.
     * 
     * @return name of the feature type, never <code>null</code>
     */
    public QName getFeatureType() {
        return ftName;
    }

    /**
     * Returns the identifier of the table that the feature type is mapped to.
     * 
     * @return identifier of the table, never <code>null</code>
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
    @Deprecated
    public Mapping getMapping( QName propName ) {
        return propToMapping.get( propName );
    }

    /**
     * Returns the {@link Mapping} particles.
     * 
     * @return mapping particles, may be empty, but never <code>null</code>
     */
    public List<Mapping> getMappings() {
        return particles;
    }
}