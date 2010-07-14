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

import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import org.deegree.cs.CRS;
import org.deegree.feature.types.ApplicationSchema;
import org.deegree.feature.types.FeatureType;

/**
 * An {@link ApplicationSchema} augmented with relational mapping information.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class MappedApplicationSchema extends ApplicationSchema {

    private final Map<QName, FeatureTypeMapping> ftNameToFtMapping = new HashMap<QName, FeatureTypeMapping>();

    private final CRS storageSRS;

    /**
     * Creates a new {@link MappedApplicationSchema} from the given parameters.
     * 
     * @param fts
     *            all application feature types (abstract and non-abstract), this must not include any GML base feature
     *            types (e.g. <code>gml:_Feature</code> or <code>gml:FeatureCollection</code>), must not be
     *            <code>null</code>
     * @param ftToSuperFt
     *            key: feature type A, value: feature type B (A extends B), this must not include any GML base feature
     *            types (e.g. <code>gml:_Feature</code> or <code>gml:FeatureCollection</code>), can be <code>null</code>
     * @param ftMappings
     *            mapping information for the feature types, must not be <code>null</code> and contain an entry for
     *            every non-abstract feature type
     * @param storageSRS
     *            CRS used for storing geometries, must not be <code>null</code>
     * @throws IllegalArgumentException
     *             if a feature type cannot be resolved (i.e. it is referenced in a property type, but not defined)
     */
    public MappedApplicationSchema( FeatureType[] fts, Map<FeatureType, FeatureType> ftToSuperFt,
                                    FeatureTypeMapping[] ftMappings, CRS storageSRS ) {
        super( fts, ftToSuperFt );
        for ( FeatureTypeMapping ftMapping : ftMappings ) {
            ftNameToFtMapping.put( ftMapping.getFeatureType(), ftMapping );
        }
        this.storageSRS = storageSRS;
    }

    /**
     * Returns all mappings.
     * 
     * @return mappings, never <code>null</code>
     */
    public Map<QName, FeatureTypeMapping> getMappings() {
        return ftNameToFtMapping;
    }

    /**
     * Returns the mapping for the specified feature type.
     * 
     * @param ftName
     *            name of the feature type, must not be <code>null</code>
     * @return the corresponding mapping, may be <code>null</code> (if a feature type was specified that does not belong
     *         to the schema)
     */
    public FeatureTypeMapping getMapping( QName ftName ) {
        return ftNameToFtMapping.get( ftName );
    }

    /**
     * Returns the SRS used for storing geometries in the backend.
     * 
     * @return the storage SRS, never <code>null</code>
     */
    public CRS getStorageSRS() {
        return storageSRS;
    }
}
