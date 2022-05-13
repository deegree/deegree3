/*-
 * #%L
 * deegree-cli-utility
 * %%
 * Copyright (C) 2016 - 2021 lat/lon GmbH
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */
package org.deegree.tools.featurestoresql.loader;

import static org.deegree.protocol.wfs.transaction.action.IDGenMode.USE_EXISTING;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.List;

import org.deegree.feature.Feature;
import org.deegree.feature.FeatureCollection;
import org.deegree.feature.GenericFeatureCollection;
import org.deegree.feature.persistence.sql.SQLFeatureStore;
import org.deegree.feature.persistence.sql.SQLFeatureStoreTransaction;
import org.deegree.protocol.wfs.transaction.action.IDGenMode;
import org.slf4j.Logger;
import org.springframework.batch.item.ItemWriter;
import org.springframework.util.Assert;

/**
 * Inserts Feature in the SQLFeatureStore.
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class FeatureStoreWriter implements ItemWriter<Feature> {

    private static final Logger LOG = getLogger( FeatureStoreWriter.class );

    private SQLFeatureStore sqlFeatureStore;

    private Summary summary;

    /**
     * @param sqlFeatureStore
     *            SQLFeatureStore to insert the features, never <code>null</code>
     * @param summary
     *            writing the report, never <code>null</code>
     */
    public FeatureStoreWriter( SQLFeatureStore sqlFeatureStore, Summary summary ) {
        Assert.notNull( sqlFeatureStore, "sqlFeatureStore  must not be null" );
        Assert.notNull( summary, "summary  must not be null" );
        this.sqlFeatureStore = sqlFeatureStore;
        this.summary = summary;
    }

    @Override
    public void write( List features )
                            throws Exception {

        FeatureCollection featureCollection = new GenericFeatureCollection();
        for ( Object feature : features ) {
            Feature featureToAdd = (Feature) feature;
            LOG.info( "Adding feature with GML ID '"+featureToAdd.getId()+"' of type '"+featureToAdd.getType().getName()+"' to chunk" );
            featureCollection.add( featureToAdd );
        }
        LOG.info( "Trying to write " + featureCollection.size() + " features" );
        SQLFeatureStoreTransaction transaction = (SQLFeatureStoreTransaction) sqlFeatureStore.getTransaction();
        transaction.performInsert( featureCollection, USE_EXISTING.withSkipResolveReferences( true ) );
        LOG.info( "Insert performed." );
        summary.increaseNumberOfFeatures( featureCollection.size() );
    }

}