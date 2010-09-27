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
package org.deegree.feature;

import java.util.List;

import javax.xml.namespace.QName;

import org.deegree.commons.tom.TypedObjectNode;
import org.deegree.feature.property.Property;
import org.deegree.feature.types.FeatureType;
import org.deegree.geometry.Envelope;
import org.deegree.gml.GMLObject;
import org.deegree.gml.GMLVersion;
import org.deegree.gml.feature.StandardGMLFeatureProps;

/**
 * A feature is a structured object with named properties, an identifier and type information. Properties may have
 * geometric and non-geometric values and may be (nested) features.
 * <p>
 * The {@link Feature} interface and related types are designed to be compatible with the following specifications:
 * <p>
 * <ul>
 * <li><a href="http://www.opengeospatial.org/standards/as">Abstract Feature specification</a></li>
 * <li><a href="http://www.opengeospatial.org/standards/sfa">Simple Features Interface Standard (SFS)</a></li>
 * <li><a href="http://www.opengeospatial.org/standards/gml">GML features: XML encoding for features</a></li>
 * <li>ISO 19109</li>
 * </ul>
 * </p>
 * <p>
 * <h4>Notes on the representation of GML features</h4>
 * <p>
 * The interface supports two views on the features' properties: GML-agnostic and GML (and version) specific. Blabla...
 * </p>
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public interface Feature extends GMLObject {

    /**
     * Returns the id of the feature.
     * <p>
     * In a GML representation of the feature, this corresponds to the <code>gml:id</code> (GML 3 and later) or
     * <code>fid</code> (GML 2) attribute of the feature element.
     * </p>
     * 
     * @return the id of the feature, may be <code>null</code>
     */
    public String getId();

    /**
     * Sets the id of the feature.
     * <p>
     * In a GML representation of the feature, this corresponds to the <code>gml:id</code> (GML 3 and later) or
     * <code>fid</code> (GML 2) attribute of the feature element.
     * </p>
     * 
     * @param id
     *            the id of the feature instance, may be <code>null</code>
     */
    public void setId( String id );

    /**
     * Returns the name of the feature.
     * <p>
     * In a GML representation of the feature, this corresponds to the feature element's name.
     * </p>
     * 
     * @return the name of the feature instance, never <code>null</code>
     */
    public QName getName();

    /**
     * Returns the type information for this feature.
     * 
     * @return the type information, never <code>null</code>
     */
    public FeatureType getType();

    /**
     * Returns all properties in order, excluding standard GML properties such as <code>gml:name</code>.
     * 
     * @return all properties, excluding standard GML properties, may be empty, but never <code>null</code>
     */
    public Property[] getProperties();

    /**
     * Returns all properties in order, including standard GML properties.
     * 
     * @param version
     *            determines the names and types of the standard GML properties, must not be <code>null</code>
     * @return all properties, including standard GML properties, may be empty, but never <code>null</code>
     */
    public Property[] getProperties( GMLVersion version );

    /**
     * Returns the properties with the given name, in order.
     * 
     * @param propName
     *            name of the requested properties
     * @return the properties with the given name, in order, may be empty, but never <code>null</code>
     */
    public Property[] getProperties( QName propName );

    /**
     * Returns the properties with the given name, in order.
     * 
     * @param propName
     *            name of the requested properties
     * @param version
     *            determines the names and types of the standard GML properties, must not be <code>null</code>
     * @return the properties with the given name, in order, may be empty, but never <code>null</code>
     */
    public Property[] getProperties( QName propName, GMLVersion version );

    /**
     * Returns the property with the given name.
     * 
     * @param propName
     *            name of the requested property
     * @return the property with the given name
     * @throws IllegalArgumentException
     *             if the feature has more than one property with the given name
     */
    public Property getProperty( QName propName );

    /**
     * Returns the property with the given name.
     * 
     * @param propName
     *            name of the requested property
     * @param version
     *            determines the names and types of the standard GML properties, must not be <code>null</code>
     * @return the property with the given name
     * @throws IllegalArgumentException
     *             if the feature has more than one property with the given name
     */
    public Property getProperty( QName propName, GMLVersion version );

    /**
     * Returns all geometry-valued properties in order.
     * 
     * @return all geometry properties
     */
    public Property[] getGeometryProperties();

    /**
     * Returns the envelope of the feature.
     * 
     * @return the envelope of the feature, or <code>null</code> if the feature has no envelope information / geometry
     *         properties
     */
    public Envelope getEnvelope();

    /**
     * Sets the value of a specific occurrence of a property with a given name (or removes the property feature).
     * 
     * @param propName
     *            property name
     * @param occurence
     *            index of the property, starting with zero. If the property is not a multi-property (i.e. maxOccurs=1),
     *            this is always zero.
     * @param value
     *            new value of the property or <code>null</code> (removes the property)
     * @throws IllegalArgumentException
     *             if the property names or values are not compatible with the feature type
     */
    public void setPropertyValue( QName propName, int occurence, TypedObjectNode value );

    /**
     * Sets the value of a specific occurrence of a property with a given name (or removes the property from the
     * feature).
     * 
     * @param propName
     *            property name
     * @param occurence
     *            index of the property, starting with zero. If the property is not a multi-property (i.e. maxOccurs=1),
     *            this is always zero.
     * @param value
     *            new value of the property or <code>null</code> (removes the property)
     * @param version
     *            determines the names and types of the standard GML properties, must not be <code>null</code>
     * @throws IllegalArgumentException
     *             if the property names or values are not compatible with the feature type
     */
    public void setPropertyValue( QName propName, int occurence, TypedObjectNode value, GMLVersion version );

    /**
     * Called during construction to initialize the properties of the feature.
     * 
     * @param props
     * @throws IllegalArgumentException
     *             if the property names or values are not compatible with the feature type
     */
    public void setProperties( List<Property> props )
                            throws IllegalArgumentException;

    /**
     * Called during construction to initialize the properties of the feature.
     * 
     * @param props
     * @param version
     *            determines the names and types of the standard GML properties, must not be <code>null</code>
     * @throws IllegalArgumentException
     *             if the property names or values are not compatible with the feature type
     */
    public void setProperties( List<Property> props, GMLVersion version )
                            throws IllegalArgumentException;

    @Override
    public StandardGMLFeatureProps getGMLProperties();
}