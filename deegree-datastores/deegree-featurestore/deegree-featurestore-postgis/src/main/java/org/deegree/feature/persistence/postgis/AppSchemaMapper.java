//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
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
package org.deegree.feature.persistence.postgis;

import static org.deegree.commons.tom.primitive.PrimitiveType.STRING;
import static org.deegree.feature.persistence.BlobCodec.Compression.NONE;

import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import org.deegree.commons.jdbc.QTableName;
import org.deegree.cs.CRS;
import org.deegree.feature.persistence.BlobCodec;
import org.deegree.feature.persistence.mapping.BBoxTableMapping;
import org.deegree.feature.persistence.mapping.BlobMapping;
import org.deegree.feature.persistence.mapping.FeatureTypeMapping;
import org.deegree.feature.persistence.mapping.MappedApplicationSchema;
import org.deegree.feature.persistence.mapping.id.FIDMapping;
import org.deegree.feature.persistence.mapping.id.IDGenerator;
import org.deegree.feature.persistence.mapping.id.UUIDGenerator;
import org.deegree.feature.persistence.mapping.property.Mapping;
import org.deegree.feature.types.ApplicationSchema;
import org.deegree.feature.types.FeatureType;
import org.deegree.feature.types.property.CodePropertyType;
import org.deegree.feature.types.property.CustomPropertyType;
import org.deegree.feature.types.property.FeaturePropertyType;
import org.deegree.feature.types.property.GenericGMLObjectPropertyType;
import org.deegree.feature.types.property.GeometryPropertyType;
import org.deegree.feature.types.property.MeasurePropertyType;
import org.deegree.feature.types.property.PropertyType;
import org.deegree.feature.types.property.SimplePropertyType;
import org.deegree.gml.GMLVersion;
import org.deegree.gml.schema.GMLSchemaInfoSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Automatically creates {@link MappedApplicationSchema} instances from {@link ApplicationSchema}s.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class AppSchemaMapper {

    private static Logger LOG = LoggerFactory.getLogger( AppSchemaMapper.class );

    private final MappingContextManager mcManager;

    private final ApplicationSchema appSchema;

    private MappedApplicationSchema mappedSchema;

    public AppSchemaMapper( ApplicationSchema appSchema ) {
        this.appSchema = appSchema;

        FeatureType[] fts = appSchema.getFeatureTypes();
        Map<FeatureType, FeatureType> ftToSuperFt = appSchema.getFtToSuperFt();
        Map<String, String> prefixToNs = appSchema.getNamespaceBindings();
        GMLSchemaInfoSet xsModel = appSchema.getXSModel();

        mcManager = new MappingContextManager( xsModel.getNamespacePrefixes() );

        FeatureTypeMapping[] ftMappings = generateFtMappings( fts );
        // TODO
        CRS storageCRS = CRS.EPSG_4326;
        BBoxTableMapping bboxMapping = generateBBoxMapping();
        BlobMapping blobMapping = generateBlobMapping();

        this.mappedSchema = new MappedApplicationSchema( fts, ftToSuperFt, prefixToNs, xsModel, ftMappings, storageCRS,
                                                         bboxMapping, blobMapping );
    }

    public MappedApplicationSchema getMappedSchema() {
        return mappedSchema;
    }

    private BlobMapping generateBlobMapping() {
        // TODO
        String table = "GML_OBJECTS";
        // TODO
        CRS storageCRS = CRS.EPSG_4326;
        // TODO
        BlobCodec codec = new BlobCodec( GMLVersion.GML_32, NONE );
        return new BlobMapping( table, storageCRS, codec );
    }

    private BBoxTableMapping generateBBoxMapping() {
        // TODO
        String ftTable = "FEATURE_TYPES";
        // TODO
        CRS crs = CRS.EPSG_4326;
        return new BBoxTableMapping( ftTable, crs );
    }

    private FeatureTypeMapping[] generateFtMappings( FeatureType[] fts ) {
        FeatureTypeMapping[] ftMappings = new FeatureTypeMapping[fts.length];
        for ( int i = 0; i < fts.length; i++ ) {
            ftMappings[i] = generateFtMapping( fts[i] );
        }
        return ftMappings;
    }

    private FeatureTypeMapping generateFtMapping( FeatureType ft ) {
        LOG.info( "Mapping feature type '" + ft.getName() + "'" );
        MappingContext mc = mcManager.newContext( ft.getName() );

        // TODO
        QTableName table = new QTableName( mc.getTable() );
        // TODO
        IDGenerator generator = new UUIDGenerator();
        // TODO
        FIDMapping fidMapping = new FIDMapping( "", "gml_id", STRING, generator );

        Map<QName, Mapping> propToMapping = new HashMap<QName, Mapping>();
        // TODO: gml properties
        for ( PropertyType pt : ft.getPropertyDeclarations() ) {
            Mapping propMapping = generatePropMapping( pt, mc );
            propToMapping.put( pt.getName(), propMapping );
        }
        return new FeatureTypeMapping( ft.getName(), table, fidMapping, propToMapping );
    }

    private Mapping generatePropMapping( PropertyType pt, MappingContext mc ) {
        LOG.info( "Mapping property '" + pt.getName() + "'" );
        Mapping mapping = null;
        if ( pt instanceof SimplePropertyType ) {
            generatePropMapping( (SimplePropertyType) pt, mc );
        } else {
            LOG.warn( "Unhandled property type '" + pt.getClass() + "'" );
            // throw new RuntimeException( );
        }
        return mapping;
    }

    private Mapping generatePropMapping( SimplePropertyType pt, MappingContext mc ) {
        LOG.info( "Mapping property '" + pt.getName() + "'" );
        Mapping mapping = null;
        return mapping;
    }
}