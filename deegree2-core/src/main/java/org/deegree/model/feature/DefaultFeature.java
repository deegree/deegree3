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
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.deegree.datatypes.QualifiedName;
import org.deegree.io.datastore.PropertyPathResolvingException;
import org.deegree.model.feature.schema.FeatureType;
import org.deegree.model.feature.schema.PropertyType;
import org.deegree.model.spatialschema.Envelope;
import org.deegree.model.spatialschema.Geometry;
import org.deegree.model.spatialschema.GeometryFactory;
import org.deegree.model.spatialschema.GeometryImpl;
import org.deegree.ogcbase.CommonNamespaces;
import org.deegree.ogcbase.PropertyPath;

/**
 * Features are, according to the Abstract Specification, digital representations of real world entities. Feature
 * Identity thus refers to mechanisms to identify such representations: not to identify the real world entities that are
 * the subject of a representation. Thus two different representations of a real world entity (say the Mississippi
 * River) will be two different features with distinct identities. Real world identification systems, such as title
 * numbers, while possibly forming a sound basis for an implementation of a feature identity mechanism, are not of
 * themselves such a mechanism.
 * 
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author <a href="mailto:mschneider@lat-lon.de">Markus Schneider</a>
 * 
 * @author last edited by: $Author$
 * 
 * @version $Revision$ $Date$
 */
public class DefaultFeature extends AbstractFeature implements Serializable {

    private static final long serialVersionUID = -1636744791597960465L;

    // key class: QualifiedName
    // value class: FeatureProperty []
    protected Map<QualifiedName, Object> propertyMap;

    protected FeatureProperty[] properties;

    /**
     * Creates a new instance of <code>DefaultFeature</code> from the given parameters.
     * 
     * @param id
     * @param featureType
     * @param properties
     *            properties of the new feature, given in their intended order
     */
    protected DefaultFeature( String id, FeatureType featureType, FeatureProperty[] properties ) {
        this( id, featureType, properties, null );
    }

    /**
     * Creates a new instance of <code>DefaultFeature</code> from the given parameters.
     * 
     * @param id
     * @param featureType
     * @param properties
     *            properties of the new feature, given in their intended order
     * @param owner
     */
    protected DefaultFeature( String id, FeatureType featureType, FeatureProperty[] properties, FeatureProperty owner ) {
        super( id, featureType, owner );
        for ( int i = 0; i < properties.length; i++ ) {
            FeatureProperty property = properties[i];
            URI namespace = property.getName().getNamespace();
            if ( ( namespace == null ) ) {
                PropertyType propertyType = featureType.getProperty( property.getName() );
                if ( propertyType == null ) {
                    throw new IllegalArgumentException( "Unknown property '" + property.getName()
                                                        + "' for feature with type '" + featureType.getName()
                                                        + "': the feature type has no such property." );
                }
            } else if ( ( !namespace.equals( CommonNamespaces.GMLNS ) ) ) {
                PropertyType propertyType = featureType.getProperty( property.getName() );
                if ( propertyType == null ) {
                    throw new IllegalArgumentException( "Unknown property '" + property.getName()
                                                        + "' for feature with type '" + featureType.getName()
                                                        + "': the feature type has no such property." );
                }
            }
        }
        this.properties = properties;
    }

    /**
     * TODO type checks!
     * 
     * @throws FeatureException
     */
    @SuppressWarnings("unchecked")
    public void validate()
                            throws FeatureException {
        if ( this.propertyMap == null ) {
            this.propertyMap = buildPropertyMap();
        }
        PropertyType[] propertyTypes = featureType.getProperties();
        for ( int i = 0; i < propertyTypes.length; i++ ) {
            List<Object> propertyList = (List<Object>) this.propertyMap.get( propertyTypes[i].getName() );
            if ( propertyList == null ) {
                if ( propertyTypes[i].getMinOccurs() != 0 ) {
                    throw new FeatureException( "Feature is not a valid instance of type '" + featureType.getName()
                                                + "', mandatory property '" + propertyTypes[i].getName()
                                                + "' is missing." );
                }
            } else {
                if ( propertyTypes[i].getMinOccurs() > propertyList.size() ) {
                    throw new FeatureException( "Feature is not a valid instance of type '" + featureType.getName()
                                                + "', property '" + propertyTypes[i].getName() + "' has minOccurs="
                                                + propertyTypes[i].getMinOccurs() + ", but is only present "
                                                + propertyList.size() + " times." );
                }
                if ( propertyTypes[i].getMaxOccurs() != -1 && propertyTypes[i].getMaxOccurs() < propertyList.size() ) {
                    throw new FeatureException( "Feature is not a valid instance of type '" + featureType.getName()
                                                + "', property '" + propertyTypes[i].getName() + "' has maxOccurs="
                                                + propertyTypes[i].getMaxOccurs() + ", but is present "
                                                + propertyList.size() + " times." );
                }
            }
        }
    }

    /**
     * Builds a map for efficient lookup of the feature's properties by name.
     * 
     * @return key class: QualifiedName, value class: FeatureProperty []
     */
    @SuppressWarnings("unchecked")
    private Map<QualifiedName, Object> buildPropertyMap() {
        Map<QualifiedName, Object> propertyMap = new HashMap<QualifiedName, Object>();
        for ( int i = 0; i < this.properties.length; i++ ) {
            List<Object> propertyList = (List<Object>) propertyMap.get( this.properties[i].getName() );
            if ( propertyList == null ) {
                propertyList = new ArrayList<Object>();
            }
            propertyList.add( properties[i] );
            propertyMap.put( properties[i].getName(), propertyList );
        }
        Iterator<QualifiedName> propertyNameIter = propertyMap.keySet().iterator();
        while ( propertyNameIter.hasNext() ) {
            QualifiedName propertyName = (QualifiedName) propertyNameIter.next();
            List<Object> propertyList = (List<Object>) propertyMap.get( propertyName );
            propertyMap.put( propertyName, propertyList.toArray( new FeatureProperty[propertyList.size()] ) );
        }
        return propertyMap;
    }

    /**
     * Builds a map for efficient lookup of the feature's properties by name.
     * 
     * @return key class: QualifiedName, value class: FeatureProperty []
     */
    private Geometry[] extractGeometryPropertyValues() {
        List<Object> geometryPropertiesList = new ArrayList<Object>();
        for ( int i = 0; i < this.properties.length; i++ ) {
            if ( this.properties[i].getValue() instanceof Geometry ) {
                geometryPropertiesList.add( this.properties[i].getValue() );
            } else if ( this.properties[i].getValue() instanceof Object[] ) {
                Object[] objects = (Object[]) this.properties[i].getValue();
                for ( int j = 0; j < objects.length; j++ ) {
                    if ( objects[j] instanceof Geometry ) {
                        geometryPropertiesList.add( objects[j] );
                    }
                }

            }
        }
        Geometry[] geometryPropertyValues = new Geometry[geometryPropertiesList.size()];
        geometryPropertyValues = (Geometry[]) geometryPropertiesList.toArray( geometryPropertyValues );
        return geometryPropertyValues;
    }

    /**
     * Returns all properties of the feature in their original order.
     * 
     * @return all properties of the feature
     */
    public FeatureProperty[] getProperties() {
        return this.properties;
    }

    /**
     * Returns the properties of the feature with the given name in their original order.
     * 
     * @return the properties of the feature with the given name or null if the feature has no property with that name
     */
    public FeatureProperty[] getProperties( QualifiedName name ) {
        if ( this.propertyMap == null ) {
            this.propertyMap = buildPropertyMap();
        }
        FeatureProperty[] properties = (FeatureProperty[]) propertyMap.get( name );
        return properties;
    }

    /**
     * Returns the property of the feature identified by the given PropertyPath in their original order.
     * 
     * NOTE: Current implementation does not handle multiple properties (on the path) or index addressing in the path.
     * 
     * @see PropertyPath
     * @return the properties of the feature identified by the given PropertyPath or null if the feature has no such
     *         properties
     * 
     */
    public FeatureProperty getDefaultProperty( PropertyPath path )
                            throws PropertyPathResolvingException {

        Feature currentFeature = this;
        FeatureProperty currentProperty = null;

        // check if path begins with the name of the feature type
        int firstPropIdx = 0;
        if ( path.getStep( 0 ).getPropertyName().equals( getName() ) ) {
            firstPropIdx = 1;
        }

        for ( int i = firstPropIdx; i < path.getSteps(); i += 2 ) {
            QualifiedName propertyName = path.getStep( i ).getPropertyName();
            currentProperty = currentFeature.getDefaultProperty( propertyName );
            if ( currentProperty == null ) {
                return null;
            }
            if ( i + 1 < path.getSteps() ) {
                QualifiedName featureName = path.getStep( i + 1 ).getPropertyName();
                Object value = currentProperty.getValue();
                if ( !( value instanceof Feature ) ) {
                    String msg = "PropertyPath '" + path + "' cannot be matched to feature. Value of property '"
                                 + propertyName + "' is not a feature, but the path does not stop there.";
                    throw new PropertyPathResolvingException( msg );
                }
                currentFeature = (Feature) value;
                if ( !featureName.equals( currentFeature.getName() ) ) {
                    String msg = "PropertyPath '" + path + "' cannot be matched to feature. Property '" + propertyName
                                 + "' contains a feature with name '" + currentFeature.getName()
                                 + "', but requested was: '" + featureName + "'.";
                    throw new PropertyPathResolvingException( msg );
                }
            }
        }
        return currentProperty;
    }

    /**
     * Returns the first property of the feature with the given name.
     * 
     * @return the first property of the feature with the given name or null if the feature has no such property
     */
    public FeatureProperty getDefaultProperty( QualifiedName name ) {
        FeatureProperty[] properties = getProperties( name );
        if ( properties != null ) {
            return properties[0];
        }
        return null;
    }   

    /**
     * Returns the values of all geometry properties of the feature.
     * 
     * @return the values of all geometry properties of the feature, or a zero-length array if the feature has no
     *         geometry properties
     */
    public Geometry[] getGeometryPropertyValues() {
        return extractGeometryPropertyValues();
    }

    /**
     * Returns the value of the default geometry property of the feature. If the feature has no geometry property, this
     * is a Point at the coordinates (0,0).
     * 
     * @return default geometry or Point at (0,0) if feature has no geometry
     */
    public Geometry getDefaultGeometryPropertyValue() {
        Geometry[] geometryValues = getGeometryPropertyValues();
        if ( geometryValues.length < 1 ) {
            return GeometryFactory.createPoint( 0, 0, null );
        }
        return geometryValues[0];
    }

    /**
     * Sets the value for the given property. The index is needed to specify the occurences of the property that is to
     * be replaced. Set to 0 for properties that may only occur once.
     * 
     * @param property
     *            property name and the property's new value
     * @param index
     *            position of the property that is to be replaced
     */
    public void setProperty( FeatureProperty property, int index ) {

        if ( "boundedBy".equals( ( property.getName().getLocalName() ) ) ) {
            if ( property.getValue() instanceof Envelope ) {
                envelope = (Envelope) property.getValue();
                envelopeCalculated = true;
            }
            return;
        }

        FeatureProperty[] oldProperties = getProperties( property.getName() );
        if ( oldProperties == null ) {
            throw new IllegalArgumentException( "Cannot set property '" + property.getName()
                                                + "': feature has no property with that name." );
        }
        if ( index > oldProperties.length - 1 ) {
            throw new IllegalArgumentException( "Cannot set property '" + property.getName() + "' with index " + index
                                                + ": feature has only " + oldProperties.length
                                                + " properties with that name." );
        }
        oldProperties[index].setValue( property.getValue() );
    }

    /**
     * Adds the given property to the feature's properties. The position of the property is determined by the feature
     * type. If the feature already has properties with this name, the new one is inserted directly behind them.
     * 
     * @param property
     *            property to insert
     */
    public void addProperty( FeatureProperty property ) {

        FeatureProperty[] newProperties;

        if ( this.properties == null ) {
            newProperties = new FeatureProperty[] { property };
        } else {
            newProperties = new FeatureProperty[this.properties.length + 1];
            for ( int i = 0; i < this.properties.length; i++ ) {
                newProperties[i] = this.properties[i];
            }
            // TODO insert at correct position
            newProperties[this.properties.length] = property;
        }
        this.properties = newProperties;
        this.propertyMap = buildPropertyMap();
    }

    /**
     * Removes the properties with the given name.
     * 
     * @param propertyName
     *            name of the (possibly multiple) property to remove
     */
    public void removeProperty( QualifiedName propertyName ) {

        List<FeatureProperty> newProperties = new ArrayList<FeatureProperty>( this.properties.length );
        for ( FeatureProperty property : this.properties ) {
            if ( !property.getName().equals( propertyName ) ) {
                newProperties.add( property );
            }
        }
        this.properties = newProperties.toArray( new FeatureProperty[newProperties.size()] );
        this.propertyMap = buildPropertyMap();
    }

    /**
     * Replaces the given property with a new one.
     * 
     * @param oldProperty
     *            property to be replaced
     * @param newProperty
     *            new property
     */
    public void replaceProperty( FeatureProperty oldProperty, FeatureProperty newProperty ) {
        for ( int i = 0; i < properties.length; i++ ) {
            if ( properties[i] == oldProperty ) {
                properties[i] = newProperty;
            }
        }
    }

    @Override
    public Object clone()
                            throws CloneNotSupportedException {
        FeatureProperty[] fp = new FeatureProperty[properties.length];
        for ( int i = 0; i < fp.length; i++ ) {
            if ( properties[i].getValue() instanceof DefaultFeatureCollection ) {
                Object v = ( (DefaultFeatureCollection) properties[i].getValue() ).clone();
                fp[i] = FeatureFactory.createFeatureProperty( properties[i].getName(), v );
            } else if ( properties[i].getValue() instanceof DefaultFeature ) {
                Object v = ( (DefaultFeature) properties[i].getValue() ).clone();
                fp[i] = FeatureFactory.createFeatureProperty( properties[i].getName(), v );
            } else {
                fp[i] = FeatureFactory.createFeatureProperty( properties[i].getName(), properties[i].getValue() );
            }
        }
        return FeatureFactory.createFeature( "UUID_" + UUID.randomUUID().toString(), featureType, fp );
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.model.feature.Feature#cloneDeep()
     */
    public Feature cloneDeep()
                            throws CloneNotSupportedException {
        FeatureProperty[] fp = new FeatureProperty[properties.length];
        for ( int i = 0; i < fp.length; i++ ) {
            if ( properties[i].getValue() instanceof DefaultFeatureCollection ) {
                Object v = ( (DefaultFeatureCollection) properties[i].getValue() ).clone();
                fp[i] = FeatureFactory.createFeatureProperty( properties[i].getName(), v );
            } else if ( properties[i].getValue() instanceof DefaultFeature ) {
                Object v = ( (DefaultFeature) properties[i].getValue() ).clone();
                fp[i] = FeatureFactory.createFeatureProperty( properties[i].getName(), v );
            }
            if ( properties[i].getValue() instanceof Geometry ) {
                Geometry geom = (Geometry) ( (GeometryImpl) properties[i].getValue() ).clone();
                fp[i] = FeatureFactory.createFeatureProperty( properties[i].getName(), geom );
            } else {
                fp[i] = FeatureFactory.createFeatureProperty( properties[i].getName(), properties[i].getValue() );
            }
        }
        return FeatureFactory.createFeature( "UUID_" + UUID.randomUUID().toString(), featureType, fp );
    }

    @Override
    public String toString() {
        String ret = getClass().getName();
        ret = "";
        for ( int i = 0; i < properties.length; i++ ) {
            if ( properties[i].getValue() instanceof DefaultFeatureCollection ) {
                ret += ( "  " + properties[i].getName() + ": ");
                ret += "\n";
                ret += ( "  " +(DefaultFeatureCollection) properties[i].getValue() ).toString();
                ret += "\n";
            } else if ( properties[i].getValue() instanceof DefaultFeature ) {
                ret += ( "  " +properties[i].getName() + ": ");
                ret += "\n";
                ret += ( "  " +(DefaultFeature) properties[i].getValue() ).toString();
                ret += "\n";
            } else  if ( properties[i].getValue() instanceof Geometry ) {
                ret += properties[i].getName();
                ret += "\n";
            } else {
                String o = "null";
                if ( properties[i].getValue()  != null ) {
                    o = properties[i].getValue().toString();
                }
                ret += (properties[i].getName() + " = " + o );
                ret += "\n";
            }
        }
        return ret;
    }
}
