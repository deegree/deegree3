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
package org.deegree.feature.types;

import java.util.List;

import javax.xml.namespace.QName;

import org.deegree.feature.Feature;
import org.deegree.feature.Property;
import org.deegree.feature.types.property.GeometryPropertyType;
import org.deegree.feature.types.property.PropertyType;
import org.deegree.gml.GMLVersion;

/**
 * Provides the type information for an (application-defined) class of {@link Feature}s.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
public interface FeatureType {

    /**
     * Returns the name that features of this type have.
     * <p>
     * In the GML representation, this corresponds to the feature's element name.
     * </p>
     * 
     * @return the name of the feature instance
     */
    public QName getName();

    /**
     * Returns the declaration of the property with the given name.
     * 
     * @param propName
     *            name of the property
     * @return the declaration of the property, or <code>null</code> if no such property is defined
     */
    public PropertyType<?> getPropertyDeclaration( QName propName );

    /**
     * Returns the declaration of the property with the given name.
     * 
     * @param propName
     *            name of the property
     * @param version
     *            GML version that determines the standard GML properties, must not be <code>null</code>
     * @return the declaration of the property, or <code>null</code> if no such property is defined
     */
    public PropertyType<?> getPropertyDeclaration( QName propName, GMLVersion version );

    /**
     * Returns all property declarations of the feature type, excluding those that any GML feature allows for.
     * 
     * @return property declarations (in order)
     */
    public List<PropertyType<?>> getPropertyDeclarations();

    /**
     * Returns all property declarations of the feature type, including those that any GML feature allows for.
     * 
     * @param version
     *            GML version that determines the standard GML properties, must not be <code>null</code>
     * @return property declarations (in order)
     */
    public List<PropertyType<?>> getPropertyDeclarations( GMLVersion version );

    /**
     * Returns the first geometry property declaration of the feature type.
     * 
     * @return first geometry property declaration or <code>null</code> if no such declaration exists
     */
    public GeometryPropertyType getDefaultGeometryPropertyDeclaration();

    /**
     * Returns whether this type is abstract or not.
     * 
     * @return true, if this feature type is abstract, false otherwise
     */
    public boolean isAbstract();

    /**
     * Creates a new {@link Feature} instance (that is of this type).
     * 
     * @param fid
     *            feature id, or null if the feature doesn't have an id
     * @param props
     *            properties
     * @param version
     *            determines the names and types of the standard GML properties, can be <code>null</code> (no GML
     *            properties)
     * @return a new <code>Feature</code> instance
     */
    public Feature newFeature( String fid, List<Property<?>> props, GMLVersion version );

    /**
     * Returns the {@link ApplicationSchema} that this feature type belongs to.
     * 
     * @return the corresponding <code>ApplicationSchema</code> or null if this feature type has none
     */
    public ApplicationSchema getSchema();

    public void setSchema( ApplicationSchema applicationSchema );
}
