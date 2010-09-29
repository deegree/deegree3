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
package org.deegree.feature.persistence.postgis;

import static org.deegree.feature.persistence.BlobCodec.Compression.NONE;
import static org.deegree.feature.types.property.GeometryPropertyType.CoordinateDimension.DIM_2;
import static org.deegree.feature.types.property.GeometryPropertyType.GeometryType.GEOMETRY;
import static org.deegree.feature.types.property.ValueRepresentation.BOTH;
import static org.deegree.gml.GMLVersion.GML_32;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.deegree.commons.jdbc.ConnectionManager;
import org.deegree.commons.tom.primitive.PrimitiveType;
import org.deegree.commons.utils.Pair;
import org.deegree.cs.CRS;
import org.deegree.feature.persistence.BlobCodec;
import org.deegree.feature.persistence.mapping.BBoxTableMapping;
import org.deegree.feature.persistence.mapping.BlobMapping;
import org.deegree.feature.persistence.mapping.DBField;
import org.deegree.feature.persistence.mapping.FeatureTypeMapping;
import org.deegree.feature.persistence.mapping.MappedApplicationSchema;
import org.deegree.feature.persistence.mapping.MappingExpression;
import org.deegree.feature.persistence.mapping.antlr.FMLLexer;
import org.deegree.feature.persistence.mapping.antlr.FMLParser;
import org.deegree.feature.persistence.postgis.jaxb.AbstractPropertyDecl;
import org.deegree.feature.persistence.postgis.jaxb.FeaturePropertyDecl;
import org.deegree.feature.persistence.postgis.jaxb.FeatureTypeDecl;
import org.deegree.feature.persistence.postgis.jaxb.GeometryPropertyDecl;
import org.deegree.feature.persistence.postgis.jaxb.SimplePropertyDecl;
import org.deegree.feature.types.ApplicationSchema;
import org.deegree.feature.types.FeatureType;
import org.deegree.feature.types.GenericFeatureType;
import org.deegree.feature.types.property.FeaturePropertyType;
import org.deegree.feature.types.property.GeometryPropertyType;
import org.deegree.feature.types.property.PropertyType;
import org.deegree.feature.types.property.SimplePropertyType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates an {@link MappedApplicationSchema} from feature type declarations.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
class PostGISApplicationSchemaBuilder {

    private static final Logger LOG = LoggerFactory.getLogger( PostGISApplicationSchemaBuilder.class );

    private Map<QName, FeatureType> ftNameToFt = new HashMap<QName, FeatureType>();

    private Map<QName, FeatureTypeMapping> ftNameToMapping = new HashMap<QName, FeatureTypeMapping>();

    private DatabaseMetaData md;

    /**
     * Creates a new {@link MappedApplicationSchema} instance.
     * 
     * @param appSchema
     *            application schema, can be <code>null</code> (for relational only mappings)
     * @param ftDecls
     *            feature type declarations, can be <code>null</code> (for BLOB-only mappings)
     * @param jdbcConnId
     *            identifier of the JDBC connection, must not be <code>null</code>
     * @param dbSchema
     *            PostgreSQL database schema, can be <code>null</code>
     * @param storageCRS
     *            CRS used for storing geometries, must not be <code>null</code>
     * @return mapped application schema, never <code>null</code>
     * @throws SQLException
     */
    static MappedApplicationSchema build( ApplicationSchema appSchema, List<FeatureTypeDecl> ftDecls,
                                          String jdbcConnId, String dbSchema, CRS storageCRS )
                            throws SQLException {

        MappedApplicationSchema mappedSchema = null;

        // TODO hybrid mapping

        if ( appSchema != null ) {
            BBoxTableMapping bboxMapping = new BBoxTableMapping();
            BlobMapping blobMapping = new BlobMapping( "GML_OBJECTS", new BlobCodec( GML_32, NONE ) );
            mappedSchema = new MappedApplicationSchema( appSchema.getFeatureTypes(), appSchema.getFtToSuperFt(),
                                                        appSchema.getNamespaceBindings(), appSchema.getXSModel(), null,
                                                        storageCRS, bboxMapping, blobMapping );
        } else {
            PostGISApplicationSchemaBuilder builder = new PostGISApplicationSchemaBuilder( ftDecls, jdbcConnId,
                                                                                           dbSchema );
            FeatureType[] fts = builder.ftNameToFt.values().toArray( new FeatureType[builder.ftNameToFt.size()] );
            FeatureTypeMapping[] ftMappings = builder.ftNameToMapping.values().toArray( new FeatureTypeMapping[builder.ftNameToMapping.size()] );
            mappedSchema = new MappedApplicationSchema( fts, null, null, null, ftMappings, storageCRS, null, null );
        }

        return mappedSchema;
    }

    private PostGISApplicationSchemaBuilder( List<FeatureTypeDecl> ftDecls, String connId, String dbSchema )
                            throws SQLException {

        Connection conn = ConnectionManager.getConnection( connId );
        md = conn.getMetaData();

        for ( FeatureTypeDecl ftDecl : ftDecls ) {
            process( ftDecl );
        }
        // schema = new ApplicationSchema( ftNameToFt.values().toArray( new FeatureType[ftNameToFt.size()] ), null );
    }

    private void process( FeatureTypeDecl ftDecl ) {

        QName ftName = ftDecl.getName();
        LOG.debug( "Processing feature type '" + ftName + "'" );
        boolean isAbstract = ftDecl.isAbstract() == null ? false : ftDecl.isAbstract();

        String mapping = ftDecl.getMapping();
        if ( mapping == null ) {
            mapping = ftName.getLocalPart().toUpperCase();
            LOG.debug( "No explicit mapping for feature type " + ftName
                       + " specified, defaulting to local feature type name '" + mapping + "'." );
        }

        String fidMapping = ftDecl.getFidMapping();
        String backendSrs = "-1";

        List<PropertyType> pts = new ArrayList<PropertyType>();
        Map<QName, MappingExpression> propToColumn = new HashMap<QName, MappingExpression>();
        for ( JAXBElement<? extends AbstractPropertyDecl> propDeclEl : ftDecl.getAbstractProperty() ) {
            AbstractPropertyDecl propDecl = propDeclEl.getValue();
            Pair<PropertyType, MappingExpression> pt = process( propDecl );
            pts.add( pt.first );
            propToColumn.put( pt.first.getName(), pt.second );

            // TODO what about different srids for multiple geometry properties?
            if ( propDecl instanceof GeometryPropertyDecl ) {
                GeometryPropertyDecl geoPropDecl = (GeometryPropertyDecl) propDecl;
                if ( geoPropDecl.getSrid() != null ) {
                    backendSrs = geoPropDecl.getSrid().toString();
                }
            }
        }

        FeatureType ft = new GenericFeatureType( ftName, pts, isAbstract );
        ftNameToFt.put( ftName, ft );

        FeatureTypeMapping ftMapping = new FeatureTypeMapping( ftName, mapping, fidMapping, propToColumn, backendSrs );
        ftNameToMapping.put( ftName, ftMapping );
    }

    private Pair<PropertyType, MappingExpression> process( AbstractPropertyDecl propDecl ) {

        QName ptName = propDecl.getName();

        int minOccurs = propDecl.getMinOccurs() == null ? 1 : propDecl.getMinOccurs().intValue();
        int maxOccurs = 1;

        if ( propDecl.getMaxOccurs() != null ) {
            if ( propDecl.getMaxOccurs().equals( "unbounded" ) ) {
                maxOccurs = -1;
            } else {
                maxOccurs = Integer.parseInt( propDecl.getMaxOccurs() );
            }
        }

        PropertyType pt = null;
        if ( propDecl instanceof SimplePropertyDecl ) {
            SimplePropertyDecl spt = (SimplePropertyDecl) propDecl;
            PrimitiveType primType = getPrimitiveType( spt.getType() );
            pt = new SimplePropertyType( ptName, minOccurs, maxOccurs, primType, false, false, null );
        } else if ( propDecl instanceof GeometryPropertyDecl ) {
            GeometryPropertyDecl gpt = (GeometryPropertyDecl) propDecl;
            pt = new GeometryPropertyType( ptName, minOccurs, maxOccurs, false, false, null, GEOMETRY, DIM_2, BOTH );
        } else if ( propDecl instanceof FeaturePropertyDecl ) {
            FeaturePropertyDecl fpt = (FeaturePropertyDecl) propDecl;
            pt = new FeaturePropertyType( ptName, minOccurs, maxOccurs, false, false, null, fpt.getType(), BOTH );
        } else {
            throw new RuntimeException( "Internal error: Unhandled property JAXB property type: " + propDecl.getClass() );
        }

        MappingExpression mapping = null;
        if ( propDecl.getMapping() == null ) {
            mapping = new DBField( pt.getName().getLocalPart().toUpperCase() );
            LOG.debug( "No explicit mapping for property " + pt.getName()
                       + " specified, defaulting to local property name '" + mapping + "'." );
        } else {
            ANTLRStringStream in = new ANTLRStringStream( propDecl.getMapping() );
            FMLLexer lexer = new FMLLexer( in );
            CommonTokenStream tokens = new CommonTokenStream( lexer );
            FMLParser parser = new FMLParser( tokens );
            try {
                mapping = parser.mappingExpr().value;
            } catch ( RecognitionException e ) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        return new Pair<PropertyType, MappingExpression>( pt, mapping );
    }

    private PrimitiveType getPrimitiveType( org.deegree.feature.persistence.postgis.jaxb.PrimitiveType type ) {
        switch ( type ) {
        case BOOLEAN:
            return PrimitiveType.BOOLEAN;
        case DATE:
            return PrimitiveType.DATE;
        case DATE_TIME:
            return PrimitiveType.DATE_TIME;
        case DECIMAL:
            return PrimitiveType.DECIMAL;
        case DOUBLE:
            return PrimitiveType.DOUBLE;
        case INTEGER:
            return PrimitiveType.INTEGER;
        case STRING:
            return PrimitiveType.STRING;
        case TIME:
            return PrimitiveType.TIME;
        }
        throw new RuntimeException( "Internal error: Unhandled JAXB primitive type: " + type );
    }
}
