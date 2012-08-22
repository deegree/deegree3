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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.deegree.datatypes.QualifiedName;
import org.deegree.datatypes.Types;
import org.deegree.datatypes.UnknownTypeException;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.framework.xml.schema.ComplexTypeDeclaration;
import org.deegree.framework.xml.schema.ElementDeclaration;
import org.deegree.framework.xml.schema.SimpleTypeDeclaration;
import org.deegree.framework.xml.schema.UndefinedXSDTypeException;
import org.deegree.framework.xml.schema.XMLSchema;
import org.deegree.framework.xml.schema.XMLSchemaException;
import org.deegree.model.crs.UnknownCRSException;
import org.deegree.model.feature.FeatureFactory;
import org.deegree.ogcbase.CommonNamespaces;

/**
 * Represents a GML application schema document to provide easy access to it's components, especially the
 * {@link FeatureType} definitions.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class GMLSchema extends XMLSchema {

    private final static ILogger LOG = LoggerFactory.getLogger( GMLSchema.class );

    private static URI XSDNS = CommonNamespaces.XSNS;

    private static URI GMLNS = CommonNamespaces.GMLNS;

    private static final QualifiedName ABSTRACT_FEATURE = new QualifiedName( "_Feature", GMLNS );

    // keys: QualifiedNames (feature type names), values: FeatureTypes
    protected Map<QualifiedName, FeatureType> featureTypeMap = new HashMap<QualifiedName, FeatureType>();

    // keys: FeatureTypes, values: List (of FeatureTypes)
    protected Map<FeatureType, List<FeatureType>> substitutionMap = new HashMap<FeatureType, List<FeatureType>>();

    /**
     * Creates a new <code>GMLSchema</code> instance from the given parameters.
     * 
     * @param targetNamespace
     * @param simpleTypes
     * @param complexTypes
     * @param elementDeclarations
     * @throws XMLParsingException
     * @throws UnknownCRSException
     */
    public GMLSchema( URI targetNamespace, SimpleTypeDeclaration[] simpleTypes, ComplexTypeDeclaration[] complexTypes,
                      ElementDeclaration[] elementDeclarations ) throws XMLParsingException, UnknownCRSException {
        super( targetNamespace, simpleTypes, complexTypes, elementDeclarations );
        buildFeatureTypeMap( elementDeclarations );
        buildSubstitutionMap( elementDeclarations );
    }

    // TODO remove this constructor
    protected GMLSchema( ElementDeclaration[] elementDeclarations, URI targetNamespace,
                         SimpleTypeDeclaration[] simpleTypes, ComplexTypeDeclaration[] complexTypes )
                            throws XMLSchemaException {
        super( targetNamespace, simpleTypes, complexTypes, elementDeclarations );
    }

    /**
     * Returns all {@link FeatureType}s that are defined in the schema.
     * 
     * @return all FeatureTypes
     */
    public FeatureType[] getFeatureTypes() {
        return this.featureTypeMap.values().toArray( new FeatureType[this.featureTypeMap.size()] );
    }

    /**
     * Looks up the {@link FeatureType} with the given {@link QualifiedName}.
     * 
     * @param qName
     *            the QualifiedName to look up
     * @return the FeatureType, if it is defined in the document, null otherwise
     */
    public FeatureType getFeatureType( QualifiedName qName ) {
        return this.featureTypeMap.get( qName );
    }

    /**
     * Looks up the {@link FeatureType} with the given local name.
     * 
     * @param localName
     *            the name to look up
     * @return the FeatureType, if it is defined in the document, null otherwise
     */
    public FeatureType getFeatureType( String localName ) {
        return getFeatureType( new QualifiedName( localName, getTargetNamespace() ) );
    }

    /**
     * Return whether the given feature type has more than one concrete substitution.
     * <p>
     * Read as: Is there only one concrete feature type that all instances of this type must have? Or are there several
     * possible concrete subtypes?
     * 
     * @param ft
     *            feature type to check
     * @return true, if the feature type has more than once concrete implementations, false otherwise
     */
    public boolean hasSeveralImplementations( FeatureType ft ) {
        return getSubstitutions( ft ).length > 1;
    }

    /**
     * Returns all non-abstract implementations of a given feature type that are defined in this schema.
     * 
     * @param featureType
     * @return all non-abstract implementations of the feature type
     */
    public FeatureType[] getSubstitutions( FeatureType featureType ) {
        FeatureType[] substitutions = new FeatureType[0];
        List<FeatureType> featureTypeList = this.substitutionMap.get( featureType );
        if ( featureTypeList != null ) {
            substitutions = featureTypeList.toArray( new FeatureType[featureTypeList.size()] );
        }
        return substitutions;
    }

    /**
     * Returns whether the specified feature type is a valid substitution for the other specified feature type
     * (according to the schema).
     * 
     * @param ft
     * @param substitution
     * @return true, if it is valid substitution, false otherwise
     */
    public boolean isValidSubstitution( FeatureType ft, FeatureType substitution ) {
        FeatureType[] substitutions = getSubstitutions( ft );
        for ( int i = 0; i < substitutions.length; i++ ) {
            if ( substitutions[i].getName().equals( substitution.getName() ) ) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns all types (abstract or concrete) that are substitutable by the given type.
     * 
     * TODO implement this a better way
     * 
     * @param substitution
     * @return all types that are substitutable by <code>substitution</code>
     */
    public Set<FeatureType> getSubstitutables( FeatureType substitution ) {

        Set<FeatureType> ftSet = new HashSet<FeatureType>();
        FeatureType[] allFts = getFeatureTypes();
        for ( FeatureType ft : allFts ) {
            if ( isValidSubstitution( ft, substitution ) ) {
                ftSet.add( ft );
            }
        }
        return ftSet;
    }

    /**
     * Initializes the internal feature type map which is used to lookup feature types by name.
     * 
     * @param elementDeclarations
     *            element declarations to process, only element declarations that are substitutable for "gml:_Feature"
     *            are considered
     * @throws XMLParsingException
     * @throws UnknownCRSException
     */
    protected void buildFeatureTypeMap( ElementDeclaration[] elementDeclarations )
                            throws XMLParsingException, UnknownCRSException {
        for ( int i = 0; i < elementDeclarations.length; i++ ) {
            LOG.logDebug( "Is element '" + elementDeclarations[i].getName() + "' a feature type definition?" );
            if ( elementDeclarations[i].isSubstitutionFor( ABSTRACT_FEATURE ) ) {
                LOG.logDebug( "Yes." );
                FeatureType featureType = buildFeatureType( elementDeclarations[i] );
                featureTypeMap.put( featureType.getName(), featureType );
            } else {
                LOG.logDebug( "No." );
            }
        }
    }

    /**
     * Initializes the internal feature type substitution map which is used to lookup substitutions for feature types.
     * <p>
     * NOTE: As this method relies on the feature type map, #initializeFeatureTypeMap(ElementDeclaration[]) must have
     * been executed before.
     * 
     * @see #buildFeatureTypeMap(ElementDeclaration[])
     * 
     * @param elementDeclarations
     *            element declarations of the feature types to process
     */
    protected void buildSubstitutionMap( ElementDeclaration[] elementDeclarations ) {
        Iterator<FeatureType> iter = featureTypeMap.values().iterator();
        while ( iter.hasNext() ) {
            FeatureType featureType = iter.next();
            List<FeatureType> substitutionList = new ArrayList<FeatureType>();
            LOG.logDebug( "Collecting possible substitutions for feature type '" + featureType.getName() + "'." );
            for ( int i = 0; i < elementDeclarations.length; i++ ) {
                if ( elementDeclarations[i].isAbstract() ) {
                    LOG.logDebug( "Skipping '" + elementDeclarations[i].getName() + "' as it is abstract." );
                } else if ( elementDeclarations[i].isSubstitutionFor( featureType.getName() ) ) {
                    LOG.logDebug( "Feature type '" + elementDeclarations[i].getName()
                                  + "' is a concrete substitution for feature type '" + featureType.getName() + "'." );
                    FeatureType substitution = this.featureTypeMap.get( elementDeclarations[i].getName() );
                    substitutionList.add( substitution );
                }
            }
            this.substitutionMap.put( featureType, substitutionList );
        }
    }

    protected FeatureType buildFeatureType( ElementDeclaration element )
                            throws XMLParsingException, UnknownCRSException {
        LOG.logDebug( "Building feature type from element declaration '" + element.getName() + "'..." );
        QualifiedName name = new QualifiedName( element.getName().getLocalName(), getTargetNamespace() );
        ComplexTypeDeclaration complexType = (ComplexTypeDeclaration) element.getType().getTypeDeclaration();
        ElementDeclaration[] subElements = complexType.getElements();
        PropertyType[] properties = new PropertyType[subElements.length];
        for ( int i = 0; i < properties.length; i++ ) {
            properties[i] = buildPropertyType( subElements[i] );
        }
        return FeatureFactory.createFeatureType( name, element.isAbstract(), properties );
    }

    protected PropertyType buildPropertyType( ElementDeclaration element )
                            throws XMLSchemaException {
        AbstractPropertyType propertyType = null;
        QualifiedName propertyName = new QualifiedName( element.getName().getLocalName(), getTargetNamespace() );
        QualifiedName typeName = element.getType().getName();
        int type = determinePropertyType( element );
        if ( typeName == null ) {
            throw new XMLSchemaException( "No type defined for the property '" + propertyName
                                          + "'. No inline definitions supported." );
        }
        if ( typeName.isInNamespace( XSDNS ) ) {
            propertyType = FeatureFactory.createSimplePropertyType( propertyName, type, element.getMinOccurs(),
                                                                    element.getMaxOccurs() );
        } else {
            switch ( type ) {
            case Types.FEATURE: {
                propertyType = FeatureFactory.createFeaturePropertyType( propertyName, element.getMinOccurs(),
                                                                         element.getMaxOccurs() );
                break;
            }
            case Types.GEOMETRY: {
                propertyType = FeatureFactory.createGeometryPropertyType( propertyName, typeName,
                                                                          element.getMinOccurs(),
                                                                          element.getMaxOccurs() );
                break;
            }
            default: {
                // hack to make extended simple types work...
                propertyType = FeatureFactory.createSimplePropertyType( propertyName, type, element.getMinOccurs(),
                                                                        element.getMaxOccurs() );
                // throw new XMLSchemaException( "Unexpected type '"
                // + type + "' in buildPropertyType()." );
            }
            }
        }
        return propertyType;
    }

    /**
     * Heuristic method that tries to determine the type of GML property that is defined in an XSD element declaration.
     * 
     * @param element
     *            <code>ElementDeclaration</code> that is a GML property definition
     * @return type code from <code>Types</code>
     * @throws UndefinedXSDTypeException
     * 
     * @see Types
     */
    protected final int determinePropertyType( ElementDeclaration element )
                            throws UndefinedXSDTypeException {
        QualifiedName typeName = element.getType().getName();
        LOG.logDebug( "Determining property type code for property type='" + typeName + "'..." );
        int type = Types.FEATURE;
        if ( element.getType().isAnonymous() ) {
            LOG.logDebug( "Inline declaration. Assuming generic GML feature of some kind." );
        } else if ( typeName.isInNamespace( XSDNS ) ) {
            LOG.logDebug( "Must be a basic XSD type." );
            try {
                type = Types.getJavaTypeForXSDType( typeName.getLocalName() );
            } catch ( UnknownTypeException e ) {
                throw new UndefinedXSDTypeException( e.getMessage(), e );
            }
        } else if ( typeName.isInNamespace( GMLNS ) ) {
            LOG.logDebug( "Maybe a geometry property type?" );
            try {
                type = Types.getJavaTypeForGMLType( typeName.getLocalName() );
                LOG.logDebug( "Yes." );
            } catch ( UnknownTypeException e ) {
                LOG.logDebug( "No. Must be a generic GML feature of some kind." );
            }
        } else {
            LOG.logDebug( "Checking for basic XSD type." );
            boolean found = false;
            try {
                type = Types.getJavaTypeForXSDType( typeName.getLocalName() );
                found = true;
            } catch ( UnknownTypeException e ) {
                throw new UndefinedXSDTypeException( e.getMessage(), e );
            }
            if ( !found ) {
                LOG.logDebug( "Should be a primitive type in our own namespace." );
                if ( !typeName.isInNamespace( getTargetNamespace() ) ) {
                    throw new UndefinedXSDTypeException( "Type '" + typeName
                                                         + "' cannot be resolved (not in a supported namespace)." );
                }
                SimpleTypeDeclaration simpleType = getSimpleTypeDeclaration( typeName );
                if ( simpleType == null ) {
                    throw new UndefinedXSDTypeException( "Simple type '" + typeName + "' cannot be resolved." );
                }
                typeName = simpleType.getRestrictionBaseType().getName();
                LOG.logDebug( "Simple base type: '" + typeName + "'. Must be a basic XSD Type." );
                try {
                    type = Types.getJavaTypeForXSDType( typeName.getLocalName() );
                } catch ( UnknownTypeException e ) {
                    throw new UndefinedXSDTypeException( e );
                }
            }
        }
        return type;
    }

    /**
     * Returns a string representation of the object.
     * 
     * @return a string representation of the object
     */
    @Override
    public String toString() {

        Map<FeatureType, List<FeatureType>> substitutesMap = buildSubstitutesMap();

        StringBuffer sb = new StringBuffer( "GML schema targetNamespace='" );
        sb.append( getTargetNamespace() );
        sb.append( "'\n" );
        sb.append( "\n*** " );
        sb.append( featureTypeMap.size() );
        sb.append( " feature type declarations ***\n" );
        Iterator<FeatureType> featureTypeIter = featureTypeMap.values().iterator();
        while ( featureTypeIter.hasNext() ) {
            FeatureType featureType = featureTypeIter.next();
            sb.append( featureTypeToString( featureType, substitutesMap ) );
            if ( featureTypeIter.hasNext() ) {
                sb.append( "\n\n" );
            }
        }
        return sb.toString();
    }

    private Map<FeatureType, List<FeatureType>> buildSubstitutesMap() {

        Map<FeatureType, List<FeatureType>> substitutesMap = new HashMap<FeatureType, List<FeatureType>>();

        for ( FeatureType ft : getFeatureTypes() ) {
            List<FeatureType> substitutesList = new ArrayList<FeatureType>();
            for ( FeatureType substitution : getFeatureTypes() ) {
                if ( isValidSubstitution( substitution, ft ) ) {
                    substitutesList.add( substitution );
                }
            }
            substitutesMap.put( ft, substitutesList );
        }
        return substitutesMap;
    }

    private String featureTypeToString( FeatureType ft, Map<FeatureType, List<FeatureType>> substitutesMap ) {
        StringBuffer sb = new StringBuffer( "- " );
        if ( ft.isAbstract() ) {
            sb.append( "(abstract) " );
        }
        sb.append( "Feature type '" );
        sb.append( ft.getName() );
        sb.append( "'\n" );

        FeatureType[] substFTs = getSubstitutions( ft );
        if ( substFTs.length > 0 ) {
            sb.append( "  is implemented by: " );
            for ( int i = 0; i < substFTs.length; i++ ) {
                sb.append( "'" );
                sb.append( substFTs[i].getName().getLocalName() );
                if ( substFTs[i].isAbstract() ) {
                    sb.append( " (abstract)" );
                }
                sb.append( "'" );
                if ( i != substFTs.length - 1 ) {
                    sb.append( "," );
                } else {
                    sb.append( "\n" );
                }
            }
        } else {
            sb.append( "  has no concrete implementations?!\n" );
        }

        List<FeatureType> substitutesList = substitutesMap.get( ft );
        sb.append( "  substitutes      : " );
        for ( int i = 0; i < substitutesList.size(); i++ ) {
            sb.append( "'" );
            sb.append( substitutesList.get( i ).getName().getLocalName() );
            if ( substitutesList.get( i ).isAbstract() ) {
                sb.append( " (abstract)" );
            }
            sb.append( "'" );
            if ( i != substitutesList.size() - 1 ) {
                sb.append( "," );
            }
        }
        sb.append( "\n" );

        PropertyType[] properties = ft.getProperties();
        for ( int i = 0; i < properties.length; i++ ) {
            PropertyType pt = properties[i];
            sb.append( " + '" );
            sb.append( pt.getName() );
            if ( pt instanceof ComplexPropertyType ) {
                sb.append( "', Type: '" );
                sb.append( ( (ComplexPropertyType) pt ).getTypeName() );
            }
            sb.append( "', SQLType: " );
            try {
                sb.append( Types.getTypeNameForSQLTypeCode( pt.getType() ) );
            } catch ( UnknownTypeException e ) {
                sb.append( "unknown" );
            }
            sb.append( ", min: " );
            sb.append( pt.getMinOccurs() );
            sb.append( ", max: " );
            sb.append( pt.getMaxOccurs() );
            if ( i != properties.length - 1 ) {
                sb.append( "\n" );
            }
        }
        return sb.toString();
    }
}
