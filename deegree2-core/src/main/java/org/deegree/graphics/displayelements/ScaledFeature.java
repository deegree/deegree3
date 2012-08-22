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
package org.deegree.graphics.displayelements;

import java.util.HashMap;
import java.util.Map;

import org.deegree.datatypes.QualifiedName;
import org.deegree.datatypes.Types;
import org.deegree.io.datastore.PropertyPathResolvingException;
import org.deegree.model.feature.DefaultFeature;
import org.deegree.model.feature.Feature;
import org.deegree.model.feature.FeatureCollection;
import org.deegree.model.feature.FeatureFactory;
import org.deegree.model.feature.FeatureProperty;
import org.deegree.model.feature.schema.FeatureType;
import org.deegree.model.feature.schema.PropertyType;
import org.deegree.model.spatialschema.Envelope;
import org.deegree.model.spatialschema.Geometry;
import org.deegree.model.spatialschema.GeometryException;
import org.deegree.ogcbase.PropertyPath;

/**
 * This class is a wrapper for a Feature and a Feature itself.
 * <p>
 * It adds a special behavior/property to a feature that is required by deegree DisplayElements. This special behavior
 * is an additional property named "$SCALE". In opposite to conventional properties this one can change its value during
 * lifetime of a feature without changing the underlying feature itself. <br>
 * The class is use to offer users the opportunity to use the scale of a map within expressions embedded in SLD
 * rules/symbolizers, i.e. this enables a user to define that a symbol shall appear in 10m size independ of a map's
 * scale.
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class ScaledFeature implements Feature {

    private Feature feature;

    private FeatureType ft;

    private FeatureProperty[] props;

    private Map<String, String> attributeMap = new HashMap<String, String>();

    /**
     *
     * @param feature
     *            feature wrap
     * @param scale
     *            maps scale (should be -1 if not known)
     */
    public ScaledFeature( Feature feature, double scale ) {
        this.feature = feature;
        PropertyType[] ftp = feature.getFeatureType().getProperties();
        PropertyType[] ftp2 = new PropertyType[ftp.length + 1];
        for ( int i = 0; i < ftp.length; i++ ) {
            ftp2[i] = ftp[i];
        }
        QualifiedName qn = new QualifiedName( feature.getName().getPrefix(), "$SCALE", feature.getName().getNamespace() );
        ftp2[ftp2.length - 1] = FeatureFactory.createSimplePropertyType( qn, Types.DOUBLE, false );
        FeatureProperty[] o = feature.getProperties();
        props = new FeatureProperty[o.length + 1];
        for ( int i = 0; i < o.length; i++ ) {
            props[i] = o[i];
        }
        props[props.length - 1] = FeatureFactory.createFeatureProperty( qn, new Double( scale ) );
        ft = FeatureFactory.createFeatureType( feature.getFeatureType().getName(), false, ftp2 );
    }

    /**
     * @return features owner
     */
    public FeatureProperty getOwner() {
        return feature.getOwner();
    }
   
    /**
     * @return features name
     */
    public QualifiedName getName() {
        return feature.getName();
    }

    /**
     * @see Feature#getDefaultGeometryPropertyValue()
     */
    public Geometry getDefaultGeometryPropertyValue() {
        return feature.getDefaultGeometryPropertyValue();
    }

    /**
     * @return features envelope
     */
    public Envelope getBoundedBy()
                            throws GeometryException {
        return feature.getBoundedBy();
    }

    /**
     * @see Feature#getFeatureType() the returned feature type contains all properties of the wrapped feature plus a
     *      property named '$SCALE'
     */
    public FeatureType getFeatureType() {
        return ft;
    }

    /**
     * @see Feature#getGeometryPropertyValues()
     */
    public Geometry[] getGeometryPropertyValues() {
        return feature.getGeometryPropertyValues();
    }

    /**
     * @see Feature#getId()
     */
    public String getId() {
        return feature.getId();
    }

    /**
     * @see Feature#getProperties() the returned array contains all properties of the wrapped feature plus a property
     *      named '$SCALE'
     */
    public FeatureProperty[] getProperties() {
        return props;
    }

    /**
     * The property '$SCALE' has the highest valid index
     */
    public FeatureProperty[] getProperties( int index ) {
        return new FeatureProperty[] { props[index] };
    }

    /**
     * use '$SCALE' to access the scale property value
     */
    public FeatureProperty getDefaultProperty( QualifiedName name ) {
        QualifiedName qn = new QualifiedName( "$SCALE" );
        if ( name.equals( qn ) ) {
            return props[props.length - 1];
        }
        return feature.getDefaultProperty( name );
    }

    /**
     * @param name
     * @return property array
     */
    public FeatureProperty[] getProperties( QualifiedName name ) {
        if ( name.getLocalName().equalsIgnoreCase( "$SCALE" ) ) {
            return new FeatureProperty[] { props[props.length - 1] };
        }
        return feature.getProperties( name );
    }

    /**
     * @param path
     * @return property
     */
    public FeatureProperty getDefaultProperty( PropertyPath path )
                            throws PropertyPathResolvingException {
        if ( path.getStep( 0 ).getPropertyName().getLocalName().equalsIgnoreCase( "$SCALE" ) ) {
            return props[props.length - 1];
        }
        return feature.getDefaultProperty( path );
    }

    public void setProperty( FeatureProperty property, int index ) {
        feature.setProperty( property, index );
    }

    /**
     * sets the features scale. Expected is the scale denominator as defined by OGC SLD specification
     *
     * @param scale
     */
    public void setScale( double scale ) {
        // must be multiplied with pixel size to get scale as length
        // of a pixels diagonal measured in meter
        props[props.length - 1].setValue( scale * 0.00028 );
    }

    /**
     * returns the features scale
     *
     * @return the features scale
     */
    public double getScale() {
        return ( (Double) props[props.length - 1].getValue() ).doubleValue();
    }

    /**
     * @param property
     */
    public void addProperty( FeatureProperty property ) {
        this.feature.addProperty( property );
    }

    /**
     * @param propertyName
     */
    public void removeProperty( QualifiedName propertyName ) {
        this.feature.removeProperty( propertyName );
    }

    /**
     * @param oldProperty
     * @param newProperty
     */
    public void replaceProperty( FeatureProperty oldProperty, FeatureProperty newProperty ) {
        this.feature.replaceProperty( oldProperty, newProperty );
    }

    /**
     * @param fid
     */
    public void setId( String fid ) {
        feature.setId( fid );
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
        this.attributeMap.put( name, value );
    }

    /**
     * Sets the feature type of this feature.
     *
     * @param ft
     *            feature type to set
     */
    public void setFeatureType( FeatureType ft ) {
        feature.setFeatureType( ft );
    }

    /*
     * (non-Javadoc)
     *
     * @see org.deegree.model.feature.Feature#setEnvelopesUpdated()
     */
    public void setEnvelopesUpdated() {
        feature.setEnvelopesUpdated();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.deegree.model.feature.Feature#cloneDeep()
     */
    public Feature cloneDeep()
                            throws CloneNotSupportedException {
        Feature tmp = feature.cloneDeep();
        return new ScaledFeature( tmp, getScale() );
    }

    @Override
    public Object clone()
                            throws CloneNotSupportedException {
        Feature tmp = (Feature) feature.clone();
        return new ScaledFeature( tmp, getScale() );
    }
    
    @Override
    public String toString() {
        String ret = getClass().getName();
        ret = "";
        for ( int i = 0; i < props.length; i++ ) {
            if ( props[i].getValue() instanceof FeatureCollection ) {
                ret += ( "  " + props[i].getName() + ": ");
                ret += "\n";
                ret += ( "  " +(FeatureCollection) props[i].getValue() ).toString();
                ret += "\n";
            } else if ( props[i].getValue() instanceof DefaultFeature ) {
                ret += ( "  " +props[i].getName() + ": ");
                ret += "\n";
                ret += ( "  " +(DefaultFeature) props[i].getValue() ).toString();
                ret += "\n";
            } else  if ( props[i].getValue() instanceof Geometry ) {
                ret += props[i].getName();
                ret += "\n";
            } else {
                String o = "null";
                if ( props[i].getValue()  != null ) {
                    o = props[i].getValue().toString();
                }
                ret += (props[i].getName() + " = " + o );
                ret += "\n";
            }
        }
        return ret;
    }

}
