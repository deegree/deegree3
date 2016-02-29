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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.xml.namespace.QName;

import org.deegree.commons.tom.gml.GMLObjectType;
import org.deegree.feature.persistence.sql.blob.BlobMapping;
import org.deegree.feature.persistence.sql.id.IdAnalysis;
import org.deegree.feature.persistence.sql.id.IdAnalyzer;
import org.deegree.feature.persistence.sql.id.TableDependencies;
import org.deegree.feature.types.AppSchema;
import org.deegree.feature.types.FeatureType;
import org.deegree.feature.types.GenericAppSchema;
import org.deegree.gml.schema.GMLSchemaInfoSet;
import org.deegree.sqldialect.table.RelationalModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An {@link AppSchema} augmented with relational and / or BLOB mapping information.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class MappedAppSchema extends GenericAppSchema {

    private static final Logger LOG = LoggerFactory.getLogger( MappedAppSchema.class );

    private final BBoxTableMapping bboxMapping;

    private final BlobMapping blobMapping;

    // key: id, value: (non-abstract) feature type name
    private final Map<Short, QName> ftIdToName = new TreeMap<Short, QName>();

    // key: (non-abstract) feature type name, value: id
    private final Map<QName, Short> ftNameToId = new HashMap<QName, Short>();

    private final Map<QName, FeatureTypeMapping> ftNameToFtMapping = new HashMap<QName, FeatureTypeMapping>();

    private final GeometryStorageParams geometryParams;

    private final IdAnalyzer idAnalyzer;

    private final TableDependencies keyDependencies;

    private final RelationalModel relationalModel;

    /**
     * Creates a new {@link MappedAppSchema} from the given parameters.
     *
     * @param fts
     *            all application feature types (abstract and non-abstract), this must not include the GML base feature
     *            types (e.g. <code>gml:_Feature</code> or <code>gml:FeatureCollection</code>), must not be
     *            <code>null</code>
     * @param ftToSuperFt
     *            key: feature type A, value: feature type B (A extends B), this must not include any GML base feature
     *            types (e.g. <code>gml:_Feature</code> or <code>gml:FeatureCollection</code>), can be <code>null</code>
     * @param prefixToNs
     *            preferred namespace prefixes to use, key: prefix, value: namespace, may be <code>null</code>
     * @param xsModel
     *            the underlying GML schema infoset, may be <code>null</code>
     * @param ftMappings
     *            relational mapping information for feature types, can be <code>null</code> (for BLOB-only mappings)
     * @param bboxMapping
     *            BBOX mapping parameters, may be <code>null</code> (for relational-only mappings)
     * @param blobMapping
     *            BLOB mapping parameters, may be <code>null</code> (for relational-only mappings)
     * @param deleteCascadingByDB
     *            if <code>true</code>, the DB automatically deletes joined tables when a feature type table row is
     *            deleted, <code>false</code> otherwise
     * @param relationalModel
     *            table detail information, may be <code>null</code>
     * @throws IllegalArgumentException
     *             if a feature type cannot be resolved (i.e. it is referenced in a property type, but not defined)
     */
    public MappedAppSchema( FeatureType[] fts, Map<FeatureType, FeatureType> ftToSuperFt,
                            Map<String, String> prefixToNs, GMLSchemaInfoSet xsModel, FeatureTypeMapping[] ftMappings,
                            BBoxTableMapping bboxMapping, BlobMapping blobMapping,
                            GeometryStorageParams geometryParams, boolean deleteCascadingByDB,
                            RelationalModel relationalModel, List<GMLObjectType> gmlObjectTypes,
                            Map<GMLObjectType, GMLObjectType> typeToSuperType ) {

        super( fts, ftToSuperFt, prefixToNs, xsModel, gmlObjectTypes, typeToSuperType );
        if ( ftMappings != null ) {
            for ( FeatureTypeMapping ftMapping : ftMappings ) {
                ftNameToFtMapping.put( ftMapping.getFeatureType(), ftMapping );
            }
        }
        this.geometryParams = geometryParams;
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
        this.keyDependencies = new TableDependencies( ftMappings, deleteCascadingByDB );
        if ( LOG.isDebugEnabled() ) {
            LOG.debug( "Key dependencies: " + keyDependencies );
        }
        this.relationalModel = relationalModel;
    }

    /**
     * Returns all relational feature type mappings.
     *
     * @return relational mappings, never <code>null</code>
     */
    public Map<QName, FeatureTypeMapping> getFtMappings() {
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
    public FeatureTypeMapping getFtMapping( QName ftName ) {
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

    /**
     * @return
     */
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
     * Returns the parameters used for storing geometries in the backend.
     *
     * @return the storage parameters, never <code>null</code>
     */
    public GeometryStorageParams getGeometryParams() {
        return geometryParams;
    }

    /**
     * Returns the name of the global id lookup table (that allows the lookup of features / geometries by id).
     *
     * @return the name of the lookup table, can be <code>null</code> (no global lookup table, only per feature type)
     */
    public String getIdLookupTable() {
        if ( blobMapping == null ) {
            return null;
        }
        return blobMapping.getTable().toString();
    }

    /**
     * Returns an analysis of the given feature or geometry id.
     *
     * @param featureOrGeomId
     *            id to be analyzed, must not be <code>null</code>
     * @return id analysis, never <code>null</code>
     * @throws IllegalArgumentException
     *             if the id does not denote a feature or geometry id
     */
    public IdAnalysis analyzeId( String featureOrGeomId )
                            throws IllegalArgumentException {
        return idAnalyzer.analyze( featureOrGeomId );
    }

    /**
     * Returns an analysis of the given feature id.
     *
     * @param fid
     *            id to be analyzed, must not be <code>null</code>
     * @return id analysis, never <code>null</code>
     * @throws IllegalArgumentException
     *             if the id does not match the prefix for the feature type
     */
    public IdAnalysis analyzeId( final String fid, final QName ftName )
                            throws IllegalArgumentException {
        return idAnalyzer.analyze( fid, ftName );
    }

    /**
     * Returns the dependencies between key columns for the involved database tables.
     *
     * @return dependencies between key columns, never <code>null</code>
     */
    public TableDependencies getKeyDependencies() {
        return keyDependencies;
    }

    /**
     * Returns details on the relational model.
     *
     * @return details on the relational model, may be <code>null</code>
     */
    public RelationalModel getRelationalModel() {
        return relationalModel;
    }
}
