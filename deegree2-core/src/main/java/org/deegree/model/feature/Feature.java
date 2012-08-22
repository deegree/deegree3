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

import java.util.Map;

import org.deegree.datatypes.QualifiedName;
import org.deegree.io.datastore.PropertyPathResolvingException;
import org.deegree.model.feature.schema.FeatureType;
import org.deegree.model.spatialschema.Envelope;
import org.deegree.model.spatialschema.Geometry;
import org.deegree.model.spatialschema.GeometryException;
import org.deegree.ogcbase.PropertyPath;

/**
 * Features are, according to the Abstract Specification, digital representations of real world
 * entities. Feature Identity thus refers to mechanisms to identify such representations: not to
 * identify the real world entities that are the subject of a representation. Thus two different
 * representations of a real world entity (say the Mississippi River) will be two different features
 * with distinct identities. Real world identification systems, such as title numbers, while
 * possibly forming a sound basis for an implementation of a feature identity mechanism, are not of
 * themselves such a mechanism.
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author <a href="mailto:mschneider@lat-lon.de">Markus Schneider</a>
 * @version $Revision$ $Date$
 */
public interface Feature {

    /**
     * Returns the qualified name of the feature.
     *
     * @return the qualified name of the feature
     */
    QualifiedName getName();
  
    /**
     * Returns the id of the feature.
     *
     * @return the id of the feature
     */
    String getId();

    /**
     * Sets the id of the feature.
     *
     * @param fid
     *            the id of the feature to be set
     */
    void setId( String fid );

    /**
     * Returns the feature type of this feature.
     *
     * @return the feature type of this feature
     */
    FeatureType getFeatureType();

    /**
     * Sets the feature type of this feature.
     *
     * @param ft
     *            feature type to set
     */
    void setFeatureType( FeatureType ft );

    /**
     * Returns all properties of the feature in their original order.
     *
     * @return all properties of the feature
     */
    FeatureProperty[] getProperties();

    /**
     * Returns the first property of the feature with the given name.
     *
     * @param name
     *            name of the property to look up
     * @return the first property of the feature with the given name or null if the feature has no
     *         such property
     */
    FeatureProperty getDefaultProperty( QualifiedName name );

    /**
     * Returns the property of the feature identified by the given {@link PropertyPath}.
     *
     * NOTE: Current implementation does not handle multiple properties (on the path) or index
     * addressing in the path.
     *
     * @param path
     *            the path of the property to look up
     * @return the property of the feature identified by the given PropertyPath
     * @throws PropertyPathResolvingException
     *
     * @see PropertyPath
     */
    FeatureProperty getDefaultProperty( PropertyPath path )
                            throws PropertyPathResolvingException;

    /**
     * Returns the properties of the feature with the given name in their original order.
     *
     * @param name
     *            name of the properties to look up
     * @return the properties of the feature with the given name or null if the feature has no
     *         property with that name
     */
    FeatureProperty[] getProperties( QualifiedName name );

  
    /**
     * Returns the values of all geometry properties of the feature.
     *
     * @return the values of all geometry properties of the feature, or a zero-length array if the
     *         feature has no geometry properties
     */
    Geometry[] getGeometryPropertyValues();

    /**
     * Returns the value of the default geometry property of the feature. If the feature has no
     * geometry property, this is a Point at the coordinates (0,0).
     *
     * @return default geometry or Point at (0,0) if feature has no geometry
     */
    Geometry getDefaultGeometryPropertyValue();

    /**
     * Sets the value for the given property. The index is needed to specify the occurences of the
     * property that is to be replaced. Set to 0 for properties that may only occur once.
     *
     * @param property
     *            property name and the property's new value
     * @param index
     *            position of the property that is to be replaced
     */
    void setProperty( FeatureProperty property, int index );

    /**
     * Adds the given property to the feature's properties. The position of the property is
     * determined by the feature type. If the feature already has a property with this name, it is
     * inserted behind it.
     *
     * @param property
     *            property to insert
     */
    void addProperty( FeatureProperty property );

    /**
     * Removes the properties with the given name.
     *
     * @param propertyName
     *            name of the properties to remove
     */
    void removeProperty( QualifiedName propertyName );

    /**
     * Replaces the given property with a new one.
     *
     * @param oldProperty
     *            property to be replaced
     * @param newProperty
     *            new property
     */
    void replaceProperty( FeatureProperty oldProperty, FeatureProperty newProperty );

    /**
     * Returns the envelope / bounding box of the feature.
     *
     * @return the envelope / bounding box of the feature
     * @throws GeometryException
     */
    Envelope getBoundedBy()
                            throws GeometryException;

    /**
     * Returns the owner of the feature. This is the feature property that has this feature as value
     * or null if this feature is a root feature.
     *
     * @return the owner of the feature, or null if the feature does not belong to a feature
     *         property
     */
    FeatureProperty getOwner();

    /**
     * Returns the attribute value of the attribute with the specified name.
     *
     * @param name
     *            name of the attribute
     * @return the attribute value
     */
    String getAttribute( String name );

    /**
     * Returns all attributes of the feature.
     *
     * @return all attributes, keys are names, values are attribute values
     */
    Map<String, String> getAttributes();

    /**
     * Sets the value of the attribute with the given name.
     *
     * @param name
     *            name of the attribute
     * @param value
     *            value to set
     */
    void setAttribute( String name, String value );

    /**
     * Signals that the envelopes of the geometry properties have been updated.
     */
    public void setEnvelopesUpdated();

    /**
     * @return a shallow clone of a feature. Property values will not be cloned except for
     *         properties that are features (DefaultFeature) or feature collections
     *         (DefaultFeatureCollection)
     */
    public Object clone() throws CloneNotSupportedException;

    /**
     * @return a deep clone of a feature. All not simple properties (incl. geometries) will be cloned
     */
    public Feature cloneDeep() throws CloneNotSupportedException;

}
