//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2011 by:
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

import static org.deegree.commons.xml.CommonNamespaces.OGCNS;
import static org.deegree.commons.xml.CommonNamespaces.XLNNS;
import static org.deegree.commons.xml.CommonNamespaces.XSINS;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import org.deegree.feature.persistence.FeatureStore;
import org.deegree.feature.persistence.FeatureStoreException;
import org.deegree.feature.persistence.FeatureStoreGMLIdResolver;
import org.deegree.feature.persistence.cache.FeatureStoreCache;
import org.deegree.feature.persistence.cache.SimpleFeatureStoreCache;
import org.deegree.feature.persistence.lock.LockManager;
import org.deegree.feature.persistence.query.Query;
import org.deegree.feature.persistence.sql.blob.BlobMapping;
import org.deegree.feature.types.FeatureType;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.geometry.Envelope;
import org.deegree.gml.GMLObject;
import org.deegree.gml.GMLReferenceResolver;

/**
 * Provides common base functionality for {@link FeatureStore} implementations that use {@link MappedApplicationSchema}
 * as mapping configuration and use JDBC for connecting to the backend.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public abstract class AbstractSQLFeatureStore implements SQLFeatureStore {

    private MappedApplicationSchema schema;

    protected BlobMapping blobMapping;

    private String jdbcConnId;

    // TODO make this configurable
    private final FeatureStoreCache cache = new SimpleFeatureStoreCache( 10000 );

    private final FeatureStoreGMLIdResolver resolver = new FeatureStoreGMLIdResolver( this );

    // cache for feature type bounding boxes
    private final Map<FeatureType, Envelope> ftToBBox = Collections.synchronizedMap( new HashMap<FeatureType, Envelope>() );

    private Map<String, String> nsContext;

    protected void init( MappedApplicationSchema schema, String jdbcConnId ) {
        this.schema = schema;
        this.blobMapping = schema.getBlobMapping();
        this.jdbcConnId = jdbcConnId;
    }

    @Override
    public MappedApplicationSchema getSchema() {
        return schema;
    }

    @Override
    public String getConnId() {
        return jdbcConnId;
    }

    /**
     * Returns the relational mapping for the given feature type name.
     * 
     * @param ftName
     *            name of the feature type, must not be <code>null</code>
     * @return relational mapping for the feature type, may be <code>null</code> (no relational mapping)
     */
    public FeatureTypeMapping getMapping( QName ftName ) {
        return schema.getFtMapping( ftName );
    }

    @Override
    public Envelope getEnvelope( QName ftName )
                            throws FeatureStoreException {
        Envelope env = null;
        FeatureType ft = schema.getFeatureType( ftName );
        if ( ft != null ) {
            if ( !ftToBBox.containsKey( ft ) ) {
                // TODO what should be favored for hybrid mappings?
                if ( blobMapping != null ) {
                    env = getEnvelope( ftName, blobMapping );
                } else {
                    FeatureTypeMapping ftMapping = schema.getFtMapping( ft.getName() );
                    if ( ftMapping == null ) {
                        String msg = "Unable to determine envelope for feature type '" + ftName
                                     + "': neither feature type nor BLOB mapping defined.";
                        throw new FeatureStoreException( msg );
                    }
                    env = getEnvelope( ftMapping );
                }
                ftToBBox.put( ft, env );
            } else {
                env = ftToBBox.get( ft );
            }
        }
        return env;
    }

    public void clearEnvelopeCache() {
        ftToBBox.clear();
    }

    protected abstract Envelope getEnvelope( QName ftName, BlobMapping blobMapping )
                            throws FeatureStoreException;

    protected abstract Envelope getEnvelope( FeatureTypeMapping ftMapping )
                            throws FeatureStoreException;

    @Override
    public GMLObject getObjectById( String id )
                            throws FeatureStoreException {

        GMLObject geomOrFeature = getCache().get( id );
        if ( geomOrFeature == null ) {
            if ( getSchema().getBlobMapping() != null ) {
                geomOrFeature = getObjectByIdBlob( id, getSchema().getBlobMapping() );
            } else {
                geomOrFeature = getObjectByIdRelational( id );
            }
        }
        return geomOrFeature;
    }

    protected abstract GMLObject getObjectByIdBlob( String id, BlobMapping blobMapping )
                            throws FeatureStoreException;

    protected abstract GMLObject getObjectByIdRelational( String id )
                            throws FeatureStoreException;

    @Override
    public LockManager getLockManager()
                            throws FeatureStoreException {
        // TODO
        return null;
    }

    /**
     * Returns the {@link FeatureStoreCache}.
     * 
     * @return feature store cache, never <code>null</code>
     */
    public FeatureStoreCache getCache() {
        return cache;
    }

    /**
     * Returns a resolver instance for retrieving objects that are stored in this feature store.
     * 
     * @return resolver, never <code>null</code>
     */
    public GMLReferenceResolver getResolver() {
        return resolver;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public void destroy() {
        // nothing to do
    }

    @Override
    public int queryHits( Query query )
                            throws FeatureStoreException, FilterEvaluationException {
        // TODO
        return query( query ).toCollection().size();
    }

    @Override
    public int queryHits( final Query[] queries )
                            throws FeatureStoreException, FilterEvaluationException {
        // TODO
        return query( queries ).toCollection().size();
    }

    public Map<String, String> getNamespaceContext() {
        if ( nsContext == null ) {
            nsContext = new HashMap<String, String>( getSchema().getNamespaceBindings() );
            nsContext.put( "xlink", XLNNS );
            nsContext.put( "xsi", XSINS );
            nsContext.put( "ogc", OGCNS );
        }
        return nsContext;
    }
}