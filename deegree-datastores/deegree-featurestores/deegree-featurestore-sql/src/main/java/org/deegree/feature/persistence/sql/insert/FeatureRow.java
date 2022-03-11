/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2022 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -
 and
 - grit graphische Informationstechnik Beratungsgesellschaft mbH -

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

 grit graphische Informationstechnik Beratungsgesellschaft mbH
 Landwehrstr. 143, 59368 Werne
 Germany
 http://www.grit.de/

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.feature.persistence.sql.insert;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.deegree.commons.jdbc.SQLIdentifier;
import org.deegree.commons.tom.primitive.BaseType;
import org.deegree.commons.tom.primitive.PrimitiveType;
import org.deegree.commons.tom.primitive.PrimitiveValue;
import org.deegree.commons.tom.sql.PrimitiveParticleConverter;
import org.deegree.commons.utils.Pair;
import org.deegree.feature.Feature;
import org.deegree.feature.persistence.FeatureStoreException;
import org.deegree.feature.persistence.sql.FeatureTypeMapping;
import org.deegree.feature.persistence.sql.id.FIDMapping;
import org.deegree.feature.persistence.sql.id.IDGenerator;
import org.deegree.feature.persistence.sql.id.IdAnalysis;
import org.deegree.protocol.wfs.transaction.action.IDGenMode;

/**
 * An {@link InsertRow} for a feature type root table (deals with feature id generation).
 * <p>
 * The final value of the feature id is usually not known during construction of the object (i.e. when
 * {@link IDGenMode#GENERATE_NEW} is used). Also, the feature type may not be known (in case an instances gets created,
 * because a reference to a feature occurred, but not the feature itself).
 * </p>
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author <a href="mailto:reichhelm@grit.de">Stephan Reichhelm</a>
 */
public class FeatureRow extends InsertRow {

    private final String origFid;

    private FIDMapping fidMapping;

    private String newId;

    /**
     * Creates a new {@link FeatureRow} instance.
     * 
     * @param mgr
     *            manager for the insert rows, must not be <code>null</code>
     * @param origFid
     *            original feature id (before id generation), may be <code>null</code> (feature without id)
     */
    public FeatureRow( InsertRowManager mgr, String origFid ) {
        super( mgr );
        this.origFid = origFid;
    }

    /**
     * Returns the original id value of the {@link Feature}.
     * 
     * @return original id, can be <code>null</code> (feature without id)
     */
    public String getOriginalId() {
        return origFid;
    }

    /**
     * Returns the final (insert) id value of the {@link Feature}.
     * 
     * @return insert id, can be <code>null</code> (not assigned yet)
     */
    public String getNewId() {
        return newId;
    }

    @Override
    void performInsert( Connection conn, boolean propagateAutoGenColumns )
                            throws SQLException, FeatureStoreException {

        super.performInsert( conn, propagateAutoGenColumns );

        newId = buildNewFid();
        if ( newId == null ) {
            String msg = "Internal/configuration error. Feature id must be assignable after feature row INSERT.";
            throw new FeatureStoreException( msg );
        }

        // clear everything, but keep key columns (values may still be needed by referencing rows)
        Map<SQLIdentifier, Object> keyColumnToValue = new HashMap<SQLIdentifier, Object>();
        Set<SQLIdentifier> genColumns = mgr.getKeyColumns( table );
        if ( genColumns != null ) {
            for ( SQLIdentifier genColumn : genColumns ) {
                keyColumnToValue.put( genColumn, get( genColumn ) );
            }
        }

        columnToLiteral.clear();
        columnToObject.clear();
        columnToObject.putAll( keyColumnToValue );
    }

    void assign( Feature feature )
                            throws FeatureStoreException {

        FeatureTypeMapping ftMapping = mgr.getSchema().getFtMapping( feature.getName() );

        this.table = ftMapping.getFtTable();
        this.fidMapping = ftMapping.getFidMapping();

        switch ( mgr.getIdGenMode() ) {
        case GENERATE_NEW: {
            Map<SQLIdentifier, IDGenerator> keyColumnToGenerator = new HashMap<SQLIdentifier, IDGenerator>();
            for ( Pair<SQLIdentifier, BaseType> columnAndType : ftMapping.getFidMapping().getColumns() ) {
                SQLIdentifier fidColumn = columnAndType.first;
                keyColumnToGenerator.put( fidColumn, ftMapping.getFidMapping().getIdGenerator() );
                generateImmediateKeys( keyColumnToGenerator );
            }
            break;
        }
        case USE_EXISTING: {
            preInsertUseExisting( ftMapping );
            break;
        }
        case REPLACE_DUPLICATE: {
            throw new UnsupportedOperationException( "REPLACE_DUPLICATE id generation mode is not implemented yet." );
        }
        }

        newId = buildNewFid();
    }

    boolean isAssigned() {
        return fidMapping != null;
    }

    @Override
    protected Set<SQLIdentifier> getAutogenColumns( boolean propagateNonFidAutoGenColumns ) {
        Set<SQLIdentifier> cols = super.getAutogenColumns( propagateNonFidAutoGenColumns );
        for ( Pair<SQLIdentifier, BaseType> fidColumn : fidMapping.getColumns() ) {
            cols.add( fidColumn.first );
        }
        return cols;
    }

    private void preInsertUseExisting( FeatureTypeMapping ftMapping )
                            throws FeatureStoreException {

        if ( origFid == null || origFid.isEmpty() ) {
            String msg = "Cannot insert features without id and id generation mode 'UseExisting'.";
            throw new FeatureStoreException( msg );
        }
        String[] idKernels = null;
        try {
            IdAnalysis analysis = mgr.getSchema().analyzeId( getOriginalId() );
            idKernels = analysis.getIdKernels();
            if ( !analysis.getFeatureType().getName().equals( ftMapping.getFeatureType() ) ) {
                String msg = "Cannot insert feature with id '" + origFid + "' and id generation mode 'UseExisting'. "
                             + "Id does not match configured feature id pattern for feature type '"
                             + ftMapping.getFeatureType() + "'.";
                throw new FeatureStoreException( msg );
            }
        } catch ( IllegalArgumentException e ) {
            String msg = "Cannot insert feature with id '" + getOriginalId()
                         + "' and id generation mode 'UseExisting'. "
                         + "Id does not match configured feature id pattern.";
            throw new FeatureStoreException( msg );
        }
        for ( int i = 0; i < fidMapping.getColumns().size(); i++ ) {
            Pair<SQLIdentifier, BaseType> idColumn = fidMapping.getColumns().get( i );
            Object value = idKernels[i];
            BaseType baseType = idColumn.second != null ? idColumn.second : BaseType.STRING;
            PrimitiveType type = new PrimitiveType( baseType );
            PrimitiveValue primitiveValue = new PrimitiveValue( value, type );
            PrimitiveParticleConverter primitiveConverter = mgr.getDialect().getPrimitiveConverter( idColumn.first.getName(),
                                                                                                    type );
            // TRICKY
            // do not require primitive conversation for single string value identifier when batching is enabled
            // this prevents that the identifier has to be set on the statement and retrieved afterwards
            if ( mgr.isBatchingEnabled() && fidMapping.getColumns().size() == 1 && BaseType.STRING == baseType ) {
                addPreparedArgument( idColumn.getFirst(), value );
            } else {
            addPreparedArgument( idColumn.getFirst(), primitiveValue, primitiveConverter );
            }
        }
    }

    private String buildNewFid()
                            throws FeatureStoreException {

        if ( get( fidMapping.getColumns().get( 0 ).getFirst() ) == null ) {
            // fid columns not available yet
            return null;
        }

        String newId = fidMapping.getPrefix();
        List<Pair<SQLIdentifier, BaseType>> fidColumns = fidMapping.getColumns();
        newId += checkFIDParticle( fidColumns.get( 0 ).first );
        for ( int i = 1; i < fidColumns.size(); i++ ) {
            newId += fidMapping.getDelimiter() + checkFIDParticle( fidColumns.get( i ).first );
        }
        return newId;
    }

    private Object checkFIDParticle( SQLIdentifier column )
                            throws FeatureStoreException {
        Object value = get( column );
        if ( value == null ) {
            throw new FeatureStoreException( "FIDMapping error: No value for feature id column '" + column + "'." );
        }
        return value;
    }

}
