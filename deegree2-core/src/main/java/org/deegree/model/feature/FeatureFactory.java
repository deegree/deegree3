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

import java.net.URI;
import java.util.List;

import org.deegree.datatypes.QualifiedName;
import org.deegree.datatypes.Types;
import org.deegree.datatypes.UnknownTypeException;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.model.feature.schema.DefaultFeatureType;
import org.deegree.model.feature.schema.FeaturePropertyType;
import org.deegree.model.feature.schema.FeatureType;
import org.deegree.model.feature.schema.GeometryPropertyType;
import org.deegree.model.feature.schema.MultiGeometryPropertyType;
import org.deegree.model.feature.schema.PropertyType;
import org.deegree.model.feature.schema.SimplePropertyType;
import org.deegree.ogcbase.CommonNamespaces;

/**
 * This factory offers methods for creating Features, FeatureCollection and all direct related classes/interfaces that
 * are part of the org.deegree.model.feature package.
 * 
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class FeatureFactory {

    private final static ILogger LOG = LoggerFactory.getLogger( FeatureFactory.class );

    private static URI GMLNS = CommonNamespaces.GMLNS;

    /**
     * Creates an instance of a <code>PropertyType</code> from the given parameters.
     * <p>
     * Determines the right type from the given type name.
     * 
     * @param name
     *            name of the property type
     * @param typeName
     *            type name of the property type
     * @param nullable
     *            set to true, if property type may be omitted
     * @return corresponding PropertyType instance
     * @throws UnknownTypeException
     */
    public static PropertyType createPropertyType( QualifiedName name, QualifiedName typeName, boolean nullable )
                            throws UnknownTypeException {
        return createPropertyType( name, typeName, nullable ? 0 : 1, 1 );
    }

    /**
     * Creates an instance of a <code>PropertyType</code> from the given parameters.
     * <p>
     * Determines the right type from the given type name.
     * 
     * @param name
     *            name of the property type
     * @param typeName
     *            type name of the property type
     * @param minOccurs
     * @param maxOccurs
     * @return corresponding PropertyType instance
     * @throws UnknownTypeException
     */
    public static PropertyType createPropertyType( QualifiedName name, QualifiedName typeName, int minOccurs,
                                                   int maxOccurs )
                            throws UnknownTypeException {

        PropertyType type = null;

        int typeCode = determinePropertyType( typeName );
        switch ( typeCode ) {
        case Types.FEATURE: {
            type = new FeaturePropertyType( name, typeName, typeCode, minOccurs, maxOccurs );
            break;
        }
        case Types.GEOMETRY: {
            type = new GeometryPropertyType( name, typeName, typeCode, minOccurs, maxOccurs );
            break;
        }
        case Types.MULTIGEOMETRY: {
            type = new MultiGeometryPropertyType( name, typeName, typeCode, minOccurs, maxOccurs );
            break;
        }
        default: {
            type = new SimplePropertyType( name, typeCode, minOccurs, maxOccurs );
        }
        }
        return type;
    }

    /**
     * Creates an instance of a <code>SimplePropertyType</code> from the given parameters.
     * 
     * @param name
     *            name of the property type
     * @param typeCode
     *            type code of the property type
     * @param nullable
     *            set to true, if property type may be omitted
     * @return generated SimplePropertyType instance
     * @see Types
     */
    public static PropertyType createSimplePropertyType( QualifiedName name, int typeCode, boolean nullable ) {
        return createSimplePropertyType( name, typeCode, nullable ? 0 : 1, 1 );
    }

    /**
     * Creates an instance of a <code>SimplePropertyType</code> from the given parameters.
     * 
     * @param name
     *            name of the property type
     * @param typeCode
     *            type code of the property type
     * @param minOccurs
     * @param maxOccurs
     * @return generated SimplePropertyType instance
     * @see Types
     */
    public static SimplePropertyType createSimplePropertyType( QualifiedName name, int typeCode, int minOccurs,
                                                               int maxOccurs ) {
        return new SimplePropertyType( name, typeCode, minOccurs, maxOccurs );
    }

    /**
     * Creates an instance of a <code>GeometryPropertyType</code> from the given parameters.
     * 
     * @param name
     *            name of the property type
     * @param typeName
     *            typeName of the property type
     * @param minOccurs
     * @param maxOccurs
     * @return generated GeometryPropertyType instance
     * @see Types
     */
    public static GeometryPropertyType createGeometryPropertyType( QualifiedName name, QualifiedName typeName,
                                                                   int minOccurs, int maxOccurs ) {
        return new GeometryPropertyType( name, typeName, Types.GEOMETRY, minOccurs, maxOccurs );
    }

    /**
     * Creates an instance of a <code>FeaturePropertyType</code> from the given parameters.
     * 
     * @param name
     *            name of the property type
     * @param minOccurs
     * @param maxOccurs
     * @return generated FeaturePropertyType instance
     * @see Types
     */
    public static FeaturePropertyType createFeaturePropertyType( QualifiedName name, int minOccurs, int maxOccurs ) {
        return new FeaturePropertyType( name, Types.FEATURE_PROPERTY_NAME, Types.FEATURE, minOccurs, maxOccurs );
    }

    /**
     * Determines the type code for the given type name.
     * 
     * @param typeName
     *            name to determine
     * @return type code for the given type name
     * @throws UnknownTypeException
     *             if the type name cannot be determined
     * @see Types
     */
    public static int determinePropertyType( QualifiedName typeName )
                            throws UnknownTypeException {
        LOG.logDebug( "Determining property type code for property type='" + typeName + "'..." );
        int type = Types.FEATURE;
        if ( typeName.isInNamespace( CommonNamespaces.XSNS ) ) {
            LOG.logDebug( "Must be a basic XSD type." );
            try {
                type = Types.getJavaTypeForXSDType( typeName.getLocalName() );
            } catch ( UnknownTypeException e ) {
                throw new UnknownTypeException( e );
            }
        } else if ( typeName.isInNamespace( GMLNS ) ) {
            LOG.logDebug( "Maybe a geometry property type?" );
            try {
                type = Types.getJavaTypeForGMLType( typeName.getLocalName() );
                LOG.logDebug( "Yes." );
            } catch ( UnknownTypeException e ) {
                LOG.logDebug( "No. Should be a generic GML feature of some kind." );
                // TODO check all possible GML types here, feature array property type
            }
        } else {
            throw new UnknownTypeException( "Cannot determine property type for type '" + typeName + "'." );
        }
        return type;
    }

    /**
     * creates an instance of a FeatureType from an array of FeatureTypeProperties, and its name but without parents and
     * childs
     * 
     * @param name
     *            name of the <code>FeatureType</code>
     * @param isAbstract
     *            <code>true</code> if the feature type to create is abstract, <code>false</code> otherwise
     * @param properties
     *            properties containing the <code>FeatureType</code>s content
     * @return instance of a <code>FeatureType</code>
     */
    public static FeatureType createFeatureType( QualifiedName name, boolean isAbstract, PropertyType[] properties ) {
        return new DefaultFeatureType( name, isAbstract, properties );
    }

    /**
     * creates an instance of a FeatureType from an array of FeatureTypeProperties, and its name but without parents and
     * childs
     * 
     * @param name
     *            name of the <code>FeatureType</code>
     * @param isAbstract
     *            <code>true</code> if the feature type to create is abstract, <code>false</code> otherwise
     * @param properties
     *            properties containing the <code>FeatureType</code>s content
     * @return instance of a <code>FeatureType</code>
     */
    public static FeatureType createFeatureType( String name, boolean isAbstract, PropertyType[] properties ) {
        return new DefaultFeatureType( new QualifiedName( name ), isAbstract, properties );
    }

    /**
     * creates an instance of a FeatureType from an array of FeatureTypeProperties, and its name but without parents and
     * childs
     * 
     * @param name
     *            name of the <code>FeatureType</code>
     * @param isAbstract
     *            <code>true</code> if the feature type to create is abstract, <code>false</code> otherwise
     * @param schemaLocation
     * @param properties
     *            properties containing the <code>FeatureType</code>s content
     * @return instance of a <code>FeatureType</code>
     */
    public static FeatureType createFeatureType( QualifiedName name, boolean isAbstract, URI schemaLocation,
                                                 PropertyType[] properties ) {
        return new DefaultFeatureType( name, isAbstract, schemaLocation, properties );
    }

    /**
     * creates an instance of a FeatureType from an array of FeatureTypeProperties, and its name but without parents and
     * childs
     * 
     * @param name
     *            name of the <code>FeatureType</code>
     * @param isAbstract
     *            <code>true</code> if the feature type to create is abstract, <code>false</code> otherwise
     * @param schemaLocation
     * @param properties
     *            properties containing the <code>FeatureType</code>s content
     * @return instance of a <code>FeatureType</code>
     */
    public static FeatureType createFeatureType( String name, boolean isAbstract, URI schemaLocation,
                                                 PropertyType[] properties ) {
        QualifiedName gName = new QualifiedName( name );
        return new DefaultFeatureType( gName, isAbstract, schemaLocation, properties );
    }

    /**
     * creates an instance of a FeatureProperty from its name and the data (value) it contains
     * 
     * @param name
     *            name of the <code>FeatureProperty</code>
     * @return an instance of a <code>FeatureProperty</code>
     * @param value
     *            value of the <code>FeatureProperty</code>
     */
    public static FeatureProperty createFeatureProperty( QualifiedName name, Object value ) {
        return new DefaultFeatureProperty( name, value );
    }

   

    /**
     * creates an instance of a @see {@link FeatureProperty} from its name. The created @see {@link FeatureProperty}
     * does takes a value generator instead of a value.
     * 
     * @param name
     *            name of the <code>FeatureProperty</code>
     * @return an instance of a <code>FeatureProperty</code>
     * @param value
     *            value of the <code>FeatureProperty</code>
     */
    public static FeatureProperty createGeneratedValueFeatureProperty( QualifiedName name, ValueGenerator valueGenerator ) {
        return new GeneratedValueFeatureProperty( name, valueGenerator );
    }

    /**
     * creates an instance of a Feature from its FeatureType and an array of Objects that represents it properties. It
     * is assumed that the order of the properties is identical to the order of the FeatureTypeProperties of the the
     * FeatureType.
     * 
     * @param id
     *            unique id of the <code>Feature</code>
     * @param featureType
     *            <code>FeatureType</code> of the <code>Feature</code>
     * @param properties
     *            properties (content) of the <code>Feature</code>
     * @return instance of a <code>Feature</code>
     */
    public static Feature createFeature( String id, FeatureType featureType, FeatureProperty[] properties ) {
        return new DefaultFeature( id, featureType, properties );
    }

    /**
     * creates an instance of a Feature from its FeatureType and an array of Objects that represents it properties. It
     * is assumed that the order of the properties is identical to the order of the FeatureTypeProperties of the the
     * FeatureType.
     * 
     * @param id
     *            unique id of the <code>Feature</code>
     * @param featureType
     *            <code>FeatureType</code> of the <code>Feature</code>
     * @param properties
     *            properties (content) of the <code>Feature</code>
     * @return instance of a <code>Feature</code>
     */
    public static Feature createFeature( String id, FeatureType featureType, List<FeatureProperty> properties ) {
        FeatureProperty[] fps = properties.toArray( new FeatureProperty[properties.size()] );
        return new DefaultFeature( id, featureType, fps );
    }

    /**
     * creates an instance of a FeatureCollection with an initial capacity. The returned FeatureCollection doesn't have
     * a FeatureType nor properties. It is just a collection of Features.
     * 
     * @param id
     *            unique id of the <code>FeatureCollection</code>
     * @param initialCapacity
     *            initial capacity of the <code>FeatureCollection</code>
     * @return instance of an empty <code>FeatureCollection</code>
     */
    public static FeatureCollection createFeatureCollection( String id, int initialCapacity ) {
        return new DefaultFeatureCollection( id, initialCapacity );
    }

    /**
     * creates an instance of a FeatureCollection from an array of Features. The returned FeatureCollection doesn't have
     * a FeatureType nor properties. It is just a collection of Features.
     * 
     * @param id
     *            unique id of the <code>FeatureCollection</code> instance
     * @param features
     *            <code>Feature</code>s to fill in into the <code>FeatureCollection</code>
     * @return instance of a <code>FeatureCollection</code> containing the submitted features
     */
    public static FeatureCollection createFeatureCollection( String id, Feature[] features ) {
        return new DefaultFeatureCollection( id, features );
    }

    /**
     * creates an instance of a FeatureCollection from an array of Features. The returned FeatureCollection doesn't have
     * a FeatureType nor properties. It is just a collection of Features. With it's name set to the given qualifiedName
     * 
     * @param id
     *            unique id of the <code>FeatureCollection</code> instance
     * @param features
     *            <code>Feature</code>s to fill in into the <code>FeatureCollection</code>
     * @param qName
     *            name of the featureCollection
     * @return instance of a <code>FeatureCollection</code> containing the submitted features
     */
    public static FeatureCollection createFeatureCollection( String id, Feature[] features, QualifiedName qName ) {
        return new DefaultFeatureCollection( id, features, qName );
    }

    /**
     * Creates a {@link FeatureTupleCollection} for the given feature tuples.
     * 
     * @param id
     *            id for the <code>FeatureCollection</code> instance
     * @param featureTuples
     *            list of feature tuples (array length must match tupleLength parameter)
     * @param tupleLength
     *            number of features per tuple
     * @return <code>FeatureTupleCollection</code> containing the given feature tuples
     */
    public static FeatureTupleCollection createFeatureCollection( String id, List<Feature[]> featureTuples,
                                                                  int tupleLength ) {
        return new FeatureTupleCollection( id, featureTuples, tupleLength );
    }
}
