//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/base/trunk/resources/eclipse/files_template.xml $
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

package org.deegree.feature.persistence.gml;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.deegree.commons.gml.GMLIdContext;
import org.deegree.commons.xml.XMLParsingException;
import org.deegree.commons.xml.stax.XMLStreamReaderWrapper;
import org.deegree.crs.exceptions.UnknownCRSException;
import org.deegree.feature.Feature;
import org.deegree.feature.FeatureCollection;
import org.deegree.feature.GenericFeatureCollection;
import org.deegree.feature.gml.GMLFeatureDecoder;
import org.deegree.feature.i18n.Messages;
import org.deegree.feature.persistence.FeatureStore;
import org.deegree.feature.persistence.FeatureStoreException;
import org.deegree.feature.persistence.FeatureStoreTransaction;
import org.deegree.feature.persistence.lock.LockManager;
import org.deegree.feature.types.ApplicationSchema;
import org.deegree.feature.types.FeatureType;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.geometry.Geometry;
import org.deegree.protocol.wfs.getfeature.FilterQuery;
import org.deegree.protocol.wfs.getfeature.Query;

/**
 * {@link FeatureStore} implementation that is backed by a GML file which is kept in memory.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: schneider $
 * 
 * @version $Revision: $, $Date: $
 */
public class GMLMemoryStore implements FeatureStore {

    private final ApplicationSchema schema;

    private final Map<String, Object> idToObject = new HashMap<String, Object>();

    private final Map<FeatureType, FeatureCollection> ftToFeatures = new HashMap<FeatureType, FeatureCollection>();

    private GMLMemoryStoreTransaction activeTransaction;

    private Thread transactionHolder;

    /**
     * Creates a new {@link GMLMemoryStore} for the given {@link ApplicationSchema}.
     * 
     * @param schema
     *            application schema, must not be null
     */
    public GMLMemoryStore( ApplicationSchema schema ) {
        this.schema = schema;
        for ( FeatureType ft : schema.getFeatureTypes() ) {
            ftToFeatures.put( ft, new GenericFeatureCollection() );
        }
    }

    /**
     * Creates a new {@link GMLMemoryStore} that is backed by the given GML file.
     * 
     * @param docURL
     * @param schema
     * @throws XMLStreamException
     * @throws XMLParsingException
     * @throws UnknownCRSException
     * @throws FactoryConfigurationError
     * @throws IOException
     */
    public GMLMemoryStore( URL docURL, ApplicationSchema schema ) throws XMLStreamException, XMLParsingException,
                            UnknownCRSException, FactoryConfigurationError, IOException {

        this( schema );
        GMLIdContext idContext = new GMLIdContext();
        GMLFeatureDecoder parser = new GMLFeatureDecoder( schema, idContext );
        XMLStreamReader xmlReader = XMLInputFactory.newInstance().createXMLStreamReader( docURL.toString(),
                                                                                         docURL.openStream() );
        xmlReader.next();
        parser.parseFeature( new XMLStreamReaderWrapper( xmlReader, docURL.toString() ), null );
        idContext.resolveXLinks( schema );
        xmlReader.close();

        // add features
        Map<String, Feature> idToFeature = idContext.getFeatures();
        for ( String id : idToFeature.keySet() ) {
            Feature feature = idToFeature.get( id );
            FeatureType ft = feature.getType();
            FeatureCollection fc2 = ftToFeatures.get( ft );
            fc2.add( feature );
            idToObject.put( id, feature );
        }

        // add geometries
        Map<String, Geometry> idToGeometry = idContext.getGeometries();
        for ( String id : idToGeometry.keySet() ) {
            idToObject.put( id, idToGeometry.get( id ) );
        }
    }

    /**
     * Adds the given {@link Feature} instances.
     * 
     * @param features
     *            features
     */
    void addFeatures( Collection<Feature> features ) {
        for ( Feature feature : features ) {
            FeatureType ft = feature.getType();
            // TODO check if served
            FeatureCollection fc2 = ftToFeatures.get( ft );
            fc2.add( feature );
            if ( feature.getId() != null ) {
                idToObject.put( feature.getId(), feature );
            }
        }
    }

    /**
     * Adds the given identified {@link Geometry} instances.
     * 
     * @param geometries
     *            geometries with ids
     */
    void addGeometriesWithId( Collection<Geometry> geometries ) {
        for ( Geometry geometry : geometries ) {
            idToObject.put( geometry.getId(), geometry );
        }
    }

    @Override
    public void destroy() {
        // TODO Auto-generated method stub

    }

    @Override
    public void init() {
        // TODO Auto-generated method stub

    }

    @Override
    public ApplicationSchema getSchema() {
        return schema;
    }

    @Override
    public FeatureCollection performQuery( Query query ) {

        if ( query.getTypeNames() == null || query.getTypeNames().length != 1 ) {
            String msg = "Only queries with exactly one type name are supported.";
            throw new UnsupportedOperationException( msg );
        }

        QName ftName = query.getTypeNames()[0].getFeatureTypeName();
        FeatureType ft = schema.getFeatureType( ftName );
        if ( ft == null ) {
            String msg = "Feature type '" + ftName + "' is not served by this datastore.";
            throw new UnsupportedOperationException( msg );
        }

        FeatureCollection fc = ftToFeatures.get( ft );
        if ( query instanceof FilterQuery ) {
            if ( ( (FilterQuery) query ).getFilter() != null ) {
                try {
                    fc = fc.getMembers( ( (FilterQuery) query ).getFilter() );
                } catch ( FilterEvaluationException e ) {
                    throw new RuntimeException( e.getMessage() );
                }
            }
        }

        return fc;
    }

    @Override
    public int performHitsQuery( Query query ) {
        // TODO maybe implement this more efficiently
        return performQuery( query ).size();
    }

    @Override
    public Object getObjectById( String id ) {
        return idToObject.get( id );
    }

    @Override
    public synchronized FeatureStoreTransaction acquireTransaction()
                            throws FeatureStoreException {

        while ( this.activeTransaction != null ) {
            Thread holder = this.transactionHolder;
            // check if transaction holder variable has (just) been cleared or if the other thread
            // has been killed (avoid deadlocks)
            if ( holder == null || !holder.isAlive() ) {
                this.activeTransaction = null;
                this.transactionHolder = null;
                break;
            }

            try {
                // wait until the transaction holder wakes us, but not longer than 5000
                // milliseconds (as the transaction holder may very rarely get killed without
                // signalling us)
                wait( 5000 );
            } catch ( InterruptedException e ) {
                // nothing to do
            }
        }

        this.activeTransaction = new GMLMemoryStoreTransaction( this );
        this.transactionHolder = Thread.currentThread();
        return this.activeTransaction;
    }

    /**
     * Returns the transaction to the datastore. This makes the transaction available to other clients again (via
     * {@link #acquireTransaction()}.
     * <p>
     * The transaction should be terminated, i.e. commit() or rollback() must have been called before.
     * 
     * @param ta
     *            the DatastoreTransaction to be returned
     * @throws FeatureStoreException
     */
    void releaseTransaction( GMLMemoryStoreTransaction ta )
                            throws FeatureStoreException {
        if ( ta.getStore() != this ) {
            String msg = Messages.getMessage( "TA_NOT_OWNER" );
            throw new FeatureStoreException( msg );
        }
        if ( ta != this.activeTransaction ) {
            String msg = Messages.getMessage( "TA_NOT_ACTIVE" );
            throw new FeatureStoreException( msg );
        }
        this.activeTransaction = null;
        this.transactionHolder = null;
        // notifyAll();
    }

    FeatureCollection getCollection( FeatureType ft ) {
        return ftToFeatures.get( ft );
    }

    void setCollection( FeatureType ft, FeatureCollection fc ) {
        ftToFeatures.put( ft, fc );
    }

    void removeObject( String id )
                            throws FeatureStoreException {
        Object o = idToObject.remove( id );
        if ( o == null ) {
            throw new FeatureStoreException( "Cannot remove feature/geometry with id '" + id + "': no such object." );
        }
        if ( o instanceof Feature ) {
            Feature feature = (Feature) o;
            FeatureCollection fc = ftToFeatures.get( feature.getType() );
            fc.remove( feature );
        }
    }

    @Override
    public LockManager getLockManager()
                            throws FeatureStoreException {
        throw new FeatureStoreException( "Locking is not supported." );
    }
}
