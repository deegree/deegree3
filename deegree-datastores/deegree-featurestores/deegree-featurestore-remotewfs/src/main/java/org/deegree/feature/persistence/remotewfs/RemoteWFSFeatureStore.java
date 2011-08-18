//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2011 by:
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

package org.deegree.feature.persistence.remotewfs;

import javax.xml.namespace.QName;

import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.commons.config.ResourceInitException;
import org.deegree.feature.persistence.FeatureStore;
import org.deegree.feature.persistence.FeatureStoreException;
import org.deegree.feature.persistence.FeatureStoreTransaction;
import org.deegree.feature.persistence.lock.LockManager;
import org.deegree.feature.persistence.query.FeatureResultSet;
import org.deegree.feature.persistence.query.Query;
import org.deegree.feature.types.AppSchema;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.geometry.Envelope;
import org.deegree.gml.GMLObject;

/**
 * {@link FeatureStore} implementation that accesses a remote WFS instance.
 * 
 * @see FeatureStore
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class RemoteWFSFeatureStore implements FeatureStore {

    private final String url;

    /**
     * Creates a new {@link RemoteWFSFeatureStore} for the given capabilities URL.
     * 
     * @param url
     *            WFS capabilities URL, must not be <code>null</code>
     * @throws FeatureStoreException
     */
    RemoteWFSFeatureStore( String url ) throws FeatureStoreException {
        this.url = url;
    }

    @Override
    public void init( DeegreeWorkspace workspace )
                            throws ResourceInitException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void destroy() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public boolean isAvailable() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public AppSchema getSchema() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Envelope getEnvelope( QName ftName )
                            throws FeatureStoreException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Envelope calcEnvelope( QName ftName )
                            throws FeatureStoreException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public FeatureResultSet query( Query query )
                            throws FeatureStoreException, FilterEvaluationException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public FeatureResultSet query( Query[] queries )
                            throws FeatureStoreException, FilterEvaluationException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int queryHits( Query query )
                            throws FeatureStoreException, FilterEvaluationException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int queryHits( Query[] queries )
                            throws FeatureStoreException, FilterEvaluationException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public GMLObject getObjectById( String id )
                            throws FeatureStoreException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public FeatureStoreTransaction acquireTransaction()
                            throws FeatureStoreException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public LockManager getLockManager()
                            throws FeatureStoreException {
        // TODO Auto-generated method stub
        return null;
    }
}