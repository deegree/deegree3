//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2012 by:
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
package org.deegree.feature.persistence;

import java.util.List;

import javax.xml.namespace.QName;

import org.deegree.feature.Feature;
import org.deegree.feature.FeatureCollection;
import org.deegree.feature.persistence.lock.Lock;
import org.deegree.filter.Filter;
import org.deegree.filter.IdFilter;
import org.deegree.filter.OperatorFilter;
import org.deegree.protocol.wfs.transaction.action.IDGenMode;
import org.deegree.protocol.wfs.transaction.action.ParsedPropertyReplacement;

/**
 * Provides transactional access to a {@link FeatureStore}.
 * <p>
 * Please note that a transaction must always be ended by calling either {@link #commit()} or {@link #rollback()}.
 * </p>
 * 
 * @see FeatureStore#acquireTransaction()
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public interface FeatureStoreTransaction {

    /**
     * Returns the underlying {@link FeatureStore} instance.
     * 
     * @return the underlying {@link FeatureStore} instance, never <code>null</code>
     */
    public FeatureStore getStore();

    /**
     * Makes the changes persistent that have been performed in this transaction and releases the transaction instance
     * so other clients may acquire a transaction on the {@link FeatureStore}.
     * 
     * @throws FeatureStoreException
     *             if the committing fails
     */
    public void commit()
                            throws FeatureStoreException;

    /**
     * Aborts the changes that have been performed in this transaction and releases the transaction instance so other
     * clients may acquire a transaction on the {@link FeatureStore}.
     * 
     * @throws FeatureStoreException
     *             if the rollback fails
     */
    public void rollback()
                            throws FeatureStoreException;

    /**
     * Inserts the given {@link FeatureCollection} into the {@link FeatureStore} (including subfeatures).
     * 
     * @param fc
     *            features to be inserted, must not be <code>null</code>
     * @param mode
     *            mode for deriving the ids of the inserted objects, must not be <code>null</code>
     * @return effective ids of the inserted feature and subfeatures (in document order)
     * @throws FeatureStoreException
     *             if the insertion fails
     */
    public List<String> performInsert( FeatureCollection fc, IDGenMode mode )
                            throws FeatureStoreException;

    /**
     * Performs an update operation against the {@link FeatureStore}.
     * 
     * @param ftName
     *            feature type of the features to be updated, must not be <code>null</code>
     * @param replacementProps
     *            properties and their replacement values plus the action to take, must not be <code>null</code>
     * @param filter
     *            selects the feature instances that are to be updated, must not be <code>null</code>
     * @param lock
     *            optional lock object, may be <code>null</code>
     * @return ids of updated feature instances, never <code>null</code>
     * @throws FeatureStoreException
     *             if the update fails
     */
    public List<String> performUpdate( QName ftName, List<ParsedPropertyReplacement> replacementProps, Filter filter,
                                       Lock lock )
                            throws FeatureStoreException;

    /**
     * Performs a replace operation against the {@link FeatureStore}.
     * 
     * @param replacement
     *            replacement feature, must not be <code>null</code>
     * @param filter
     *            selects the feature instance to be replaced, must not be <code>null</code>
     * @param lock
     *            optional lock object, may be <code>null</code>
     * @param idGenMode
     *            never <code>null</code>
     * @return identifier of the replaced feature, can be <code>null</code> (filter didn't match)
     * @throws FeatureStoreException
     *             if the replace fails
     */
    public List<String> performReplace( Feature replacement, Filter filter, Lock lock, IDGenMode idGenMode )
                            throws FeatureStoreException;

    /**
     * Deletes the features from the {@link FeatureStore} that are matched by the given filter and type.
     * 
     * @param ftName
     *            feature type of the features to be deleted, must not be <code>null</code>
     * @param filter
     *            filter that determines the features to be deleted, must not be <code>null</code>
     * @param lock
     *            optional lock object, may be <code>null</code>
     * @return number of deleted feature instances
     * @throws FeatureStoreException
     *             if the deletion fails
     */
    public int performDelete( QName ftName, OperatorFilter filter, Lock lock )
                            throws FeatureStoreException;

    /**
     * Deletes the features from the {@link FeatureStore} that are matched by the given filter.
     * 
     * @param filter
     *            filter that determines the features to be deleted, must not be <code>null</code>
     * @param lock
     *            optional lock object, may be <code>null</code>
     * @return number of deleted feature instances
     * @throws FeatureStoreException
     *             if the deletion fails (*not* if a specified id does not exist)
     */
    public int performDelete( IdFilter filter, Lock lock )
                            throws FeatureStoreException;
}
