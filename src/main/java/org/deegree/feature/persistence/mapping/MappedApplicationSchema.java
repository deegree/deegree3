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
import java.util.SortedMap;
import java.util.TreeMap;

import javax.xml.namespace.QName;

import org.deegree.cs.CRS;
import org.deegree.feature.types.ApplicationSchema;
import org.deegree.feature.types.FeatureType;
import org.deegree.gml.schema.GMLSchemaAnalyzer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An {@link ApplicationSchema} augmented with relational and or BLOB mapping information.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class MappedApplicationSchema extends ApplicationSchema {

    private static final Logger LOG = LoggerFactory.getLogger( MappedApplicationSchema.class );

    private final BBoxTableMapping bboxMapping;

    private final BlobMapping blobMapping;

    // key: id, value: (non-abstract) feature type name
    private final Map<Short, QName> ftIdToName = new TreeMap<Short, QName>();

    // key: (non-abstract) feature type name, value: id
    private final Map<QName, Short> ftNameToId = new HashMap<QName, Short>();

    private final Map<QName, FeatureTypeMapping> ftNameToFtMapping = new HashMap<QName, FeatureTypeMapping>();

    private final CRS storageCRS;

    private final IdAnalyzer idAnalyzer;

    /**
     * Creates a new {@link MappedApplicationSchema} from the given parameters.
     * 
     * @param fts
     *            all application feature types (abstract and non-abstract), this must not include the GML base feature
     *            types (e.g. <code>gml:_Feature</code> or <code>gml:FeatureCollection</code>), must not be
     *            <code>null</code>
     * @param ftToSuperFt
     *            key: feature type A, value: feature type B (A extends B), this must not include any GML base feature
     *            types (e.g. <code>gml:_Feature</code> or <code>gml:FeatureCollection</code>), can be <code>null</code>
     * @param xsModel
     *            the underlying XML schema, may be <code>null</code>
     * @param ftMappings
     *            relational mapping information for the feature types, can be <code>null</code> (for BLOB-only
     *            mappings)
     * @param storageSRS
     *            CRS used for storing geometries, must not be <code>null</code>
     * @param bboxMapping
     *            BBOX mapping parameters, may be <code>null</code> (for RELATIONAL-only mappings)
     * @param blobMapping
     *            BLOB mapping parameters, may be <code>null</code> (for RELATIONAL-only mappings)
     * @throws IllegalArgumentException
     *             if a feature type cannot be resolved (i.e. it is referenced in a property type, but not defined)
     */
    public MappedApplicationSchema( FeatureType[] fts, Map<FeatureType, FeatureType> ftToSuperFt,
                                    GMLSchemaAnalyzer xsModel, FeatureTypeMapping[] ftMappings, CRS storageSRS,
                                    BBoxTableMapping bboxMapping, BlobMapping blobMapping ) {

        super( fts, ftToSuperFt, xsModel );
        if ( ftMappings != null ) {
            for ( FeatureTypeMapping ftMapping : ftMappings ) {
                ftNameToFtMapping.put( ftMapping.getFeatureType(), ftMapping );
            }
        }
        this.storageCRS = storageSRS;
        this.idAnalyzer = new IdAnalyzer( this );

        // sort by QName first
        SortedMap<String, QName> ftNames = new TreeMap<String, QName>();
        for ( FeatureType ft : fts ) {
            if ( !ft.isAbstract() ) {
                ftNames.put( ft.getName().toString(), ft.getName() );
            }
        }
        short ftId = 0;
        for ( String ftName : ftNames.keySet() ) {
            QName qName = ftNames.get( ftName );
            LOG.debug( "Feature type [{}]:'{}'", ftId, qName );
            ftNameToId.put( qName, ftId );
            ftIdToName.put( ftId++, qName );
        }

        this.bboxMapping = bboxMapping;
        this.blobMapping = blobMapping;
    }

    /**
     * Returns all relational feature type mappings.
     * 
     * @return relational mappings, never <code>null</code>
     */
    public Map<QName, FeatureTypeMapping> getMappings() {
        return ftNameToFtMapping;
    }

    /**
     * Returns the relational mapping for the specified feature type.
     * 
     * @param ftName
     *            name of the feature type, must not be <code>null</code>
     * @return the corresponding mapping, may be <code>null</code> (if the feature type does not have a relational
     *         mapping)
     */
    public FeatureTypeMapping getMapping( QName ftName ) {
        return ftNameToFtMapping.get( ftName );
    }

    /**
     * Returns the id of the specified (non-abstract) feature type.
     * 
     * @param ftName
     *            name of the feature type, must denote a non-abstract feature type that's part of the schema and not
     *            <code>null</code>
     * @return id of the feature type
     */
    public short getFtId( QName ftName ) {
        return ftNameToId.get( ftName );
    }

    /**
     * Returns the name of the (non-abstract) feature type with the given id.
     * 
     * @param ftId
     *            id of the feature type
     * @return name of the feature type, denotes a non-abstract feature type that's part of the schema
     */
    public QName getFtName( short ftId ) {
        return ftIdToName.get( ftId );
    }

    public short getFts() {
        return (short) ftIdToName.size();
    }

    public BBoxTableMapping getBBoxMapping() {
        return bboxMapping;
    }

    /**
     * Returns the BLOB mapping parameters.
     * 
     * @return the BLOB mapping parameters, may be <code>null</code> (for RELATIONAL-only mappings)
     */
    public BlobMapping getBlobMapping() {
        return blobMapping;
    }

    /**
     * Returns the CRS used for storing geometries in the backend.
     * 
     * @return the storage CRS, never <code>null</code>
     */
    public CRS getStorageCRS() {
        return storageCRS;
    }

    /**
     * Returns the name of the global id lookup table (that allows the lookup of features / geometries by id).
     * 
     * @return the name of the lookup table, can be <code>null</code> (no global lookup table, only per feature type)
     */
    public String getIdLookupTable() {
        // TODO
        return "GML_OBJECTS";
    }

    /**
     * Returns an analysis of the given feature or geometry id.
     * 
     * @param featureOrGeomId
     * @return
     * @throws IllegalArgumentException
     *             if the id does not denote a feature or geometry id
     */
    public IdAnalysis analyzeId( String featureOrGeomId )
                            throws IllegalArgumentException {
        return idAnalyzer.analyze( featureOrGeomId );
    }
}
