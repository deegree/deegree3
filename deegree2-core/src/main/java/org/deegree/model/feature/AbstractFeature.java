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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.deegree.datatypes.QualifiedName;
import org.deegree.model.feature.schema.FeatureType;
import org.deegree.model.spatialschema.Envelope;
import org.deegree.model.spatialschema.Geometry;
import org.deegree.model.spatialschema.GeometryException;
import org.deegree.model.spatialschema.GeometryFactory;
import org.deegree.model.spatialschema.Point;

/**
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
abstract class AbstractFeature implements Feature {

    private String id;

    protected FeatureType featureType;

    protected Envelope envelope;

    protected boolean envelopeCalculated;

    protected FeatureProperty owner;

    private Map<String, String> attributeMap = null;

    /**
     * @param id
     * @param featureType
     */
    AbstractFeature( String id, FeatureType featureType ) {
        this.id = id;
        this.featureType = featureType;
    }

    /**
     * @param id
     * @param featureType
     */
    AbstractFeature( String id, FeatureType featureType, FeatureProperty owner ) {
        this.id = id;
        this.featureType = featureType;
        this.owner = owner;
    }   

    public QualifiedName getName() {
        return featureType.getName();
    }

    /**
     * Returns the envelope / boundingbox of the feature. The bounding box will be created in a recursion. That means if
     * a property of the feature (a) contains another feature (b) the bounding box of b will be merged with the bounding
     * box calculated from the geometry properties of a. A feature has no geometry properties and no properties that are
     * features (and having a bounding box theirself) <code>null</code> will be returned.
     */
    public Envelope getBoundedBy()
                            throws GeometryException {
        if ( !this.envelopeCalculated ) {
            getBoundedBy( new HashSet<Feature>() );
        }
        return this.envelope;
    }

    /**
     * Signals that the envelopes of the geometry properties have been updated.
     */
    public void setEnvelopesUpdated() {
        envelopeCalculated = false;
    }

    public FeatureProperty getOwner() {
        return this.owner;
    }

    /**
     * returns the id of the Feature. the id has to be a name space that must be unique for each feature. use the adress
     * of the datasource in addition to a number for example .
     */
    public String getId() {
        return id;
    }

    public void setId( String fid ) {
        this.id = fid;
    }

    /**
     * returns the FeatureType of this Feature
     *
     */
    public FeatureType getFeatureType() {
        return featureType;
    }

    /**
     * Sets the feature type of this feature.
     *
     * @param ft
     *            feature type to set
     */
    public void setFeatureType( FeatureType ft ) {
        this.featureType = ft;
    }

    /**
     * resets the envelope of the feature so the next call of getBounds() will force a re-calculation
     *
     */
    protected void resetBounds() {
        envelope = null;
    }

    /**
     * Returns the attribute value of the attribute with the specified name.
     *
     * @param name
     *            name of the attribute
     * @return the attribute value
     */
    public String getAttribute( String name ) {
        return this.attributeMap.get( name );
    }

    /**
     * Returns all attributes of the feature.
     *
     * @return all attributes, keys are names, values are attribute values
     */
    public Map<String, String> getAttributes() {
        return this.attributeMap;
    }

    /**
     * Sets the value of the attribute with the given name.
     *
     * @param name
     *            name of the attribute
     * @param value
     *            value to set
     */
    public void setAttribute( String name, String value ) {
        if ( attributeMap == null ) {
            attributeMap = new HashMap<String, String>();
        }
        this.attributeMap.put( name, value );
    }

    /**
     * Returns the {@link Envelope} of the feature. If the envelope has not been calculated yet, it is calculated
     * recursively, using the given feature set to detected cycles in the feature structure.
     *
     * @param features
     *            used to detected cycles in feature structure and prevent endless recursion
     * @return envelope of the feature
     * @throws GeometryException
     */
    private Envelope getBoundedBy( Set<Feature> features )
                            throws GeometryException {
        if ( !this.envelopeCalculated ) {
            this.envelope = calcEnvelope( features );
            this.envelopeCalculated = true;
        }
        return this.envelope;
    }

    /**
     * Calculates the envelope of the feature recursively. Respects all geometry properties and sub features of the
     * feature.
     *
     * @param features
     *            used to detected cycles in feature structure and prevent endless recursion
     * @return envelope of the feature
     * @throws GeometryException
     */
    private Envelope calcEnvelope( Set<Feature> features )
                            throws GeometryException {

        Envelope combinedEnvelope = null;
        if ( features.contains( this ) ) {
            return combinedEnvelope;
        }
        features.add( this );

        FeatureProperty[] props = getProperties();
        for ( FeatureProperty prop : props ) {
            if ( prop != null ) {
                Object propValue = prop.getValue();
                if ( propValue instanceof Geometry ) {
                    Geometry geom = (Geometry) propValue;
                    Envelope env = null;
                    if ( geom instanceof Point ) {
                        env = GeometryFactory.createEnvelope( ( (Point) geom ).getPosition(),
                                                              ( (Point) geom ).getPosition(),
                                                              geom.getCoordinateSystem() );
                    } else {
                        env = geom.getEnvelope();
                    }
                    if ( combinedEnvelope == null ) {
                        combinedEnvelope = env;
                    } else {
                        combinedEnvelope = combinedEnvelope.merge( env );
                    }
                } else if ( propValue instanceof AbstractFeature ) {
                    Envelope subEnvelope = ( (AbstractFeature) propValue ).getBoundedBy( features );
                    if ( combinedEnvelope == null ) {
                        combinedEnvelope = subEnvelope;
                    } else if ( subEnvelope != null ) {
                        combinedEnvelope = combinedEnvelope.merge( subEnvelope );
                    }
                }
            }
        }
        return combinedEnvelope;
    }

    @Override
    public Object clone()
                            throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

}
