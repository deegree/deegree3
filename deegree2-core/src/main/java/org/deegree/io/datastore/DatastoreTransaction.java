//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
 Department of Geography, University of Bonn
 and
 lat/lon GmbH

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
package org.deegree.io.datastore;

import java.util.List;
import java.util.Map;

import org.deegree.io.datastore.idgenerator.FeatureIdAssigner;
import org.deegree.io.datastore.schema.MappedFeatureType;
import org.deegree.model.feature.Feature;
import org.deegree.model.feature.FeatureProperty;
import org.deegree.model.filterencoding.Filter;
import org.deegree.ogcbase.PropertyPath;
import org.deegree.ogcwebservices.wfs.operation.transaction.Delete;
import org.deegree.ogcwebservices.wfs.operation.transaction.Insert;
import org.deegree.ogcwebservices.wfs.operation.transaction.Native;
import org.deegree.ogcwebservices.wfs.operation.transaction.TransactionOperation;
import org.deegree.ogcwebservices.wfs.operation.transaction.Update;

/**
 * Handler for {@link TransactionOperation}s ({@link Insert}, {@link Update}, {@link Delete}, {@link Native}). One
 * instance is bound to exactly one {@link Datastore} instance (and one {@link Datastore} has no more than one active
 * <code>DatastoreTransaction</code> at a time.
 * 
 * @author <a href="mailto:deshmukh@lat-lon.de">Anup Deshmukh</a>
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public interface DatastoreTransaction {

    /**
     * Returns the associated {@link Datastore} instance.
     * 
     * @return the associated Datastore instance
     */
    public Datastore getDatastore();

    /**
     * Makes the changes persistent that have been performed in this transaction.
     * 
     * @throws DatastoreException
     */
    public void commit()
                            throws DatastoreException;

    /**
     * Aborts the changes that have been performed in this transaction.
     * 
     * @throws DatastoreException
     */
    public void rollback()
                            throws DatastoreException;

    /**
     * Releases the transaction instance so other clients may acquire a transaction (and underlying resources, such as
     * JDBCConnections can be cleaned up).
     * 
     * @throws DatastoreException
     */
    public void release()
                            throws DatastoreException;

    /**
     * Inserts the given feature instances into the datastore.
     * <p>
     * Please note that the features to be inserted must have suitable feature ids at this point.
     * 
     * @param features
     * @return feature ids of the inserted (root) features
     * @throws DatastoreException
     * @see FeatureIdAssigner
     */
    public List<FeatureId> performInsert( List<Feature> features )
                            throws DatastoreException;

    /**
     * Performs an update operation against the datastore.
     * 
     * @param mappedFeatureType
     *            feature type that is to be updated
     * @param replacementProps
     *            properties and their replacement values
     * @param filter
     *            selects the feature instances that are to be updated
     * @param lockId
     *            optional id of associated lock (may be null)
     * @return number of updated feature instances
     * @throws DatastoreException
     */
    public int performUpdate( MappedFeatureType mappedFeatureType, Map<PropertyPath, FeatureProperty> replacementProps,
                              Filter filter, String lockId )
                            throws DatastoreException;

    /**
     * Performs a replace-update operation against the datastore.
     * 
     * @param mappedFeatureType
     *            feature type that is to be replaced
     * @param replacementFeature
     *            feature instance that will be used to replace the properties of the selected features
     * @param filter
     *            selects the feature instances that are to be replaced
     * @param lockId
     *            optional id of associated lock (may be null)
     * @return number of replaced feature instances
     * @throws DatastoreException
     */
    public int performUpdate( MappedFeatureType mappedFeatureType, Feature replacementFeature, Filter filter,
                              String lockId )
                            throws DatastoreException;

    /**
     * Deletes the features from the datastore that are matched by the given filter and type.
     * 
     * @param mappedFeatureType
     * @param filter
     * @param lockId
     *            optional id of associated lock (may be null)
     * @return number of deleted feature instances
     * @throws DatastoreException
     */
    public int performDelete( MappedFeatureType mappedFeatureType, Filter filter, String lockId )
                            throws DatastoreException;

    /**
     * Performs a native operation against the datastore.
     * 
     * @param operation
     *            operation to perform
     * @return int
     * @throws DatastoreException
     */
    public int performNative( Native operation )
                            throws DatastoreException;

}
