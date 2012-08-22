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

package org.deegree.model.feature.schema;

import java.net.URI;

import org.deegree.datatypes.QualifiedName;

/**
 * The FeatureType interface is intended to provide details of the type of a Feature that are
 * described as Feature Schema in the Abstract Specification's Essential Model, specifically the
 * names and types of the properties associated with each instance of a Feature of the given
 * FeatureType.
 * <p>
 * -----------------------------------------------------------------------
 * </p>
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @version $Revision$ $Date$
 */
public interface FeatureType {

    /**
     * returns the name of the FeatureType
     *
     * @return name of the FeatureType
     */
    public QualifiedName getName();

    /**
     * Returns whether this feature type is abstract or not.
     *
     * @return true, if the feature type is abstract, false otherwise
     */
    public boolean isAbstract();

    /**
     * returns the namespace of the feature type (maybe null)
     *
     * @return the namespace of the feature type (maybe <code>null</code>)
     */
    public URI getNameSpace();

    /**
     * returns the location of the XML schema defintion assigned to a namespace
     *
     * @return the location of the XML schema defintion assigned to a namespace
     */
    public URI getSchemaLocation();

    /**
     * returns the name of the property a the passed index position
     *
     * @param index
     * @return the name of the property a the passed index position
     */
    public QualifiedName getPropertyName( int index );

    /**
     * returns the properties of this feature type
     *
     * @return type properties
     */
    public PropertyType[] getProperties();

    /**
     * returns a property of this feature type identified by its name
     *
     * @param name
     *            name of the desired property
     * @return one named property
     */
    public PropertyType getProperty( QualifiedName name );

    /**
     * returns the FeatureTypeProperties of type GEOMETRY
     *
     * @see org.deegree.datatypes.Types
     * @return the FeatureTypeProperties of type GEOMETRY
     */
    public GeometryPropertyType[] getGeometryProperties();

    /**
     * returns true if the passed FeatureType equals this FeatureType. Two FeatureTypes are equal if
     * they have the same qualified name
     *
     * @param featureType
     *
     * @return <code>true</code> if the passed FeatureType equals this FeatureType.
     */
    public boolean equals( FeatureType featureType );

}
