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
package org.deegree.model.feature;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.deegree.datatypes.QualifiedName;
import org.deegree.io.datastore.PropertyPathResolvingException;
import org.deegree.model.spatialschema.Envelope;
import org.deegree.model.spatialschema.GeometryException;
import org.deegree.ogcbase.PropertyPath;

/**
 * Represents a {@link FeatureCollection} that only contains <code>wfs:featureTuple</code> elements (as introduced by
 * the draft WFS 1.2 spec).
 * <p>
 * NOTE: Some concepts of ordinary feature collections (like adding and removing of features) do not match well to this
 * special kind of feature collection, mostly because it uses a <code>FeatureArrayPropertyType</code> for the
 * <code>wfs:featureTuple</code> element. Thus, many methods inherited from {@link AbstractFeatureCollection} have no
 * clear semantic and are not available.
 *
 * !!!Please note that most methods are not implemented yet!!!
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @version $Revision$ $Date$
 */
public class FeatureTupleCollection extends AbstractFeatureCollection implements Serializable {

    private static final long serialVersionUID = 2651676067288914826L;

    private int tupleLength;

    private List<Feature[]> tuples;

    private Set<Feature> allFeatures = new HashSet<Feature>();

    private Envelope envelope;

    FeatureTupleCollection( String id, List<Feature[]> tuples, int tupleLength ) {
        super( id );
        this.tuples = tuples;
        this.tupleLength = tupleLength;
        for ( Feature[] features : tuples ) {
            assert features.length == tupleLength;
            for ( Feature feature : features ) {
                allFeatures.add( feature );
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.deegree.model.feature.FeatureCollection#clear()
     */
    public void clear() {
        allFeatures.clear();
    }

    /**
     * Returns the feature tuple at the given index.
     *
     * @param index
     * @return the feature tuple at the given index
     */
    public Feature[] getTuple( int index ) {
        return this.tuples.get( index );
    }

    /**
     * Returns the number of feature tuples contained in this collection.
     *
     * @return the number of feature tuples contained in this collection
     */
    public int numTuples() {
        return this.tuples.size();
    }

    /**
     * Returns the length (number of features) of each tuple.
     *
     * @return the length (number of features) of each tuple.
     */
    public int tupleLength() {
        return this.tupleLength;
    }

    @Override
    public synchronized Envelope getBoundedBy()
                            throws GeometryException {

        Envelope combinedEnvelope = this.envelope;

        if ( combinedEnvelope == null && this.allFeatures.size() > 0 ) {
            for ( Feature feature : this.allFeatures ) {
                Envelope nextFeatureEnvelope = feature.getBoundedBy();
                if ( combinedEnvelope == null ) {
                    combinedEnvelope = nextFeatureEnvelope;
                } else if ( nextFeatureEnvelope != null ) {
                    combinedEnvelope = combinedEnvelope.merge( nextFeatureEnvelope );
                }
            }
            this.envelope = combinedEnvelope;
        }
        return combinedEnvelope;
    }

    public int size() {
        return this.tuples.size();
    }

    public void add( Feature feature ) {
        throw new NoSuchMethodError();
    }

    public Feature getFeature( int index ) {
        return null;
    }

    public Feature getFeature( String id ) {
        throw new NoSuchMethodError();
    }

    public Iterator<Feature> iterator() {
        throw new NoSuchMethodError();
    }

    public Feature remove( Feature feature ) {
        throw new NoSuchMethodError();
    }

    public Feature remove( int index ) {
        throw new NoSuchMethodError();
    }

    public Feature[] toArray() {
        throw new NoSuchMethodError();
    }

    public void addProperty( FeatureProperty property ) {
        throw new NoSuchMethodError();
    }

    public FeatureProperty getDefaultProperty( PropertyPath path )
                            throws PropertyPathResolvingException {
        throw new NoSuchMethodError();
    }

    public void removeProperty( QualifiedName propertyName ) {
        throw new NoSuchMethodError();
    }

    public void replaceProperty( FeatureProperty oldProperty, FeatureProperty newProperty ) {
        throw new NoSuchMethodError();
    }

    public void setProperty( FeatureProperty property, int index ) {
        throw new NoSuchMethodError();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.deegree.model.feature.Feature#cloneDeep()
     */
    public Feature cloneDeep()
                            throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

}
