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
package org.deegree.framework.xml.schema;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.deegree.datatypes.QualifiedName;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;

/**
 * Represents an XML schema document.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class XMLSchema {

    private final static ILogger LOG = LoggerFactory.getLogger( XMLSchema.class );

    private URI targetNamespace;

    // keys: QualifiedName (element names), values: ElementDeclaration
    private Map<QualifiedName, ElementDeclaration> elementMap = new HashMap<QualifiedName, ElementDeclaration>();

    // keys: QualifiedName (type names), values: TypeDeclaration
    private Map<QualifiedName, TypeDeclaration> typeMap = new HashMap<QualifiedName, TypeDeclaration>();

    // keys: QualifiedName (type names), values: ComplexTypeDeclaration
    private Map<QualifiedName, ComplexTypeDeclaration> complexTypeMap = new HashMap<QualifiedName, ComplexTypeDeclaration>();

    // keys: QualifiedName (type names), values: SimpleTypeDeclaration
    private Map<QualifiedName, SimpleTypeDeclaration> simpleTypeMap = new HashMap<QualifiedName, SimpleTypeDeclaration>();

    /**
     * Creates a new <code>XMLSchema</code> instance from the given parameters.
     *
     * @param targetNamespace
     * @param simpleTypes
     * @param complexTypes
     * @param elementDeclarations
     * @throws XMLSchemaException
     */
    public XMLSchema( URI targetNamespace, SimpleTypeDeclaration[] simpleTypes,
                     ComplexTypeDeclaration[] complexTypes, ElementDeclaration[] elementDeclarations )
                            throws XMLSchemaException {
        this.targetNamespace = targetNamespace;
        for ( int i = 0; i < elementDeclarations.length; i++ ) {
            elementMap.put( elementDeclarations[i].getName(), elementDeclarations[i] );
        }
        for ( int i = 0; i < simpleTypes.length; i++ ) {
            simpleTypeMap.put( simpleTypes[i].getName(), simpleTypes[i] );
            typeMap.put( simpleTypes[i].getName(), simpleTypes[i] );
        }
        for ( int i = 0; i < complexTypes.length; i++ ) {
            complexTypeMap.put( complexTypes[i].getName(), complexTypes[i] );
            typeMap.put( complexTypes[i].getName(), complexTypes[i] );
        }
        resolveReferences();
    }

    /**
     * Returns the target namespace of the schema document.
     *
     * @return the target namespace
     */
    public URI getTargetNamespace() {
        return this.targetNamespace;
    }

    /**
     * Returns all <code>ElementDeclaration</code>s that are defined in the schema.
     *
     * @return all ElementDeclarations that are defined in the schema
     */
    public ElementDeclaration[] getElementDeclarations() {
        return this.elementMap.values().toArray( new ElementDeclaration[this.elementMap.size()] );
    }

    /**
     * Returns all <code>SimpleTypeDeclaration</code>s that are defined in the schema.
     *
     * @return all SimpleTypeDeclarations that are defined in the schema
     */
    public SimpleTypeDeclaration[] getSimpleTypeDeclarations() {
        return this.simpleTypeMap.values().toArray(
                                                    new SimpleTypeDeclaration[this.simpleTypeMap.size()] );
    }

    /**
     * Returns all <code>ComplexTypeDeclaration</code>s that are defined in the schema.
     *
     * @return all ComplexTypeDeclarations that are defined in the schema
     */
    public ComplexTypeDeclaration[] getComplexTypeDeclarations() {
        return this.complexTypeMap.values().toArray(
                                                     new ComplexTypeDeclaration[this.complexTypeMap.size()] );
    }

    /**
     * Looks up the <code>ElementDeclaration</code> for the given <code>QualifiedName</code>.
     *
     * @param qName
     *            the QualifiedName to look up
     * @return the ElementDeclaration, if an element with the given name is defined in the schema,
     *         null otherwise
     */
    public ElementDeclaration getElementDeclaration( QualifiedName qName ) {
        return this.elementMap.get( qName );
    }

    /**
     * Looks up the <code>TypeDeclaration</code> for the given <code>QualifiedName</code>.
     *
     * @param qName
     *            the QualifiedName to look up
     * @return the TypeDeclaration, if a type with the given name is defined in the schema, null
     *         otherwise
     */
    public TypeDeclaration getTypeDeclaration( QualifiedName qName ) {
        return this.typeMap.get( qName );
    }

    /**
     * Looks up the <code>SimpleTypeDeclaration</code> for the given <code>QualifiedName</code>.
     *
     * @param qName
     *            the QualifiedName to look up
     * @return the SimpleTypeDeclaration, if a simple type with the given name is defined in the
     *         schema, null otherwise
     */
    public SimpleTypeDeclaration getSimpleTypeDeclaration( QualifiedName qName ) {
        return this.simpleTypeMap.get( qName );
    }

    /**
     * Looks up the <code>ComplexTypeDeclaration</code> for the given <code>QualifiedName</code>.
     *
     * @param qName
     *            the QualifiedName to look up
     * @return the ComplexTypeDeclaration, if a complex type with the given name is defined in the
     *         schema, null otherwise
     */
    public ComplexTypeDeclaration getComplexTypeDeclaration( QualifiedName qName ) {
        return this.complexTypeMap.get( qName );
    }

    /**
     * Looks up the <code>ElementDeclaration</code> for the given local name (without namespace).
     *
     * @param name
     *            the (unqualified) name to look up
     * @return the ElementDeclaration, if an element with the given name is defined in the schema,
     *         null otherwise
     */
    public ElementDeclaration getElementDeclaration( String name ) {
        return getElementDeclaration( new QualifiedName( name, this.targetNamespace ) );
    }

    /**
     * Looks up the <code>TypeDeclaration</code> for the given local name (without namespace).
     *
     * @param name
     *            the (unqualified) name to look up
     * @return the TypeDeclaration, if a type with the given name is defined in the schema, null
     *         otherwise
     */
    public TypeDeclaration getTypeDeclaration( String name ) {
        return getTypeDeclaration( new QualifiedName( name, this.targetNamespace ) );
    }

    /**
     * Looks up the <code>SimpleTypeDeclaration</code> for the given local name (without
     * namespace).
     *
     * @param name
     *            the (unqualified) name to look up
     * @return the SimpleTypeDeclaration, if a simple type with the given name is defined in the
     *         schema, null otherwise
     */
    public SimpleTypeDeclaration getSimpleTypeDeclaration( String name ) {
        return getSimpleTypeDeclaration( new QualifiedName( name, this.targetNamespace ) );
    }

    /**
     * Looks up the <code>ComplexTypeDeclaration</code> for the given local name (without
     * namespace).
     *
     * @param name
     *            the (unqualified) name to look up
     * @return the ComplexTypeDeclaration, if a complex type with the given name is defined in the
     *         schema, null otherwise
     */
    public ComplexTypeDeclaration getComplexTypeDeclaration( String name ) {
        return getComplexTypeDeclaration( new QualifiedName( name, this.targetNamespace ) );
    }

    private void resolveReferences()
                            throws UnresolvableReferenceException {
        LOG.logDebug( "Resolving references for namespace '" + this.targetNamespace + "'." );
        for( ElementDeclaration elemDec : elementMap.values() ){
            resolveReferences( elemDec );
        }
        for( TypeDeclaration tDec : typeMap.values() ){
            resolveReferences( tDec );
        }
    }

    private void resolveReferences( ElementDeclaration element )
                            throws UnresolvableReferenceException {
        LOG.logDebug( "Resolving references in element declaration '"
                      + element.getName().getLocalName() + "'." );
        ElementReference substitutionGroup = element.getSubstitutionGroup();
        if ( substitutionGroup != null ) {
            resolveElement( substitutionGroup );
        }
        TypeReference typeReference = element.getType();
        resolveType( typeReference );
    }

    private void resolveReferences( TypeDeclaration typeDeclaration )
                            throws UnresolvableReferenceException {
        LOG.logDebug( "Resolving references in type declaration '"
                      + typeDeclaration.getName().getLocalName() + "'." );
        if ( typeDeclaration instanceof SimpleTypeDeclaration ) {
            LOG.logDebug( "SimpleType." );
            SimpleTypeDeclaration simpleType = (SimpleTypeDeclaration) typeDeclaration;
            TypeReference typeReference = simpleType.getRestrictionBaseType();
            if ( typeReference != null ) {
                LOG.logDebug( "restriction base='" + typeReference.getName() + "'" );
                try {
                    resolveType( typeReference );
                } catch ( XMLSchemaException e ) {
                    throw new UndefinedXSDTypeException( "Declaration of type '"
                                                         + typeDeclaration.getName()
                                                         + "' derives type '"
                                                         + typeReference.getName()
                                                         + "' which is not a defined simple type." );
                }
            }
        } else {
            LOG.logDebug( "ComplexType." );
            ComplexTypeDeclaration complexType = (ComplexTypeDeclaration) typeDeclaration;
            TypeReference typeReference = complexType.getExtensionBaseType();
            if ( typeReference != null ) {
                LOG.logDebug( "extension base='" + typeReference.getName() + "'" );
                try {
                    resolveType( typeReference );
                } catch ( XMLSchemaException e ) {
                    throw new UndefinedXSDTypeException( "Declaration of type '"
                                                         + typeDeclaration.getName()
                                                         + "' derives type '"
                                                         + typeReference.getName()
                                                         + "' which is not a defined complex type." );
                }
            }
            ElementDeclaration[] elements = complexType.getExplicitElements();
            for ( int i = 0; i < elements.length; i++ ) {
                resolveReferences( elements[i] );
            }
        }
    }

    private void resolveElement( ElementReference elementReference )
                            throws UndefinedElementException {
        if ( !elementReference.isResolved() ) {
            LOG.logDebug( "Resolving reference to element '"
                          + elementReference.getName().getLocalName() + "'." );
            if ( elementReference.getName().isInNamespace( this.targetNamespace ) ) {
                ElementDeclaration element = elementMap.get( elementReference.getName() );
                if ( element == null ) {
                    LOG.logDebug( "Cannot be resolved!" );
                    throw new UndefinedElementException( "Element '" + elementReference.getName()
                                                         + "' is not defined." );
                }
                LOG.logDebug( "OK." );
                elementReference.resolve( element );
            } else {
                LOG.logDebug( "Skipped (not in target namespace)." );
                elementReference.resolve();
            }
        }
    }

    private void resolveType( TypeReference typeReference )
                            throws UnresolvableReferenceException {
        if ( !typeReference.isResolved() ) {
            if ( typeReference.isAnonymous() ) {
                LOG.logDebug( "Inline type..." );
                // type is defined inline
                TypeDeclaration type = typeReference.getTypeDeclaration();
                typeReference.resolve();
                if ( type instanceof ComplexTypeDeclaration ) {
                    ComplexTypeDeclaration complexType = (ComplexTypeDeclaration) type;
                    ElementDeclaration[] subElements = complexType.getExplicitElements();
                    for ( int i = 0; i < subElements.length; i++ ) {
                        resolveReferences( subElements[i] );
                    }
                }
            } else {
                LOG.logDebug( "Resolving reference to type: '" + typeReference.getName() + "'..." );
                if ( typeReference.getName().isInNamespace( this.targetNamespace ) ) {
                    TypeDeclaration type = typeMap.get( typeReference.getName() );
                    if ( type == null ) {
                        LOG.logDebug( "Cannot be resolved!" );
                        throw new UndefinedXSDTypeException( "Type '" + typeReference.getName()
                                                             + "' is not a defined type." );
                    }
                    LOG.logDebug( "OK." );
                    typeReference.resolve( type );
                } else {
                    LOG.logDebug( "Skipped (not in target / schema namespace)." );
                }
            }
        }
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer( "XML Schema targetNamespace='" );
        sb.append( targetNamespace );
        sb.append( "'\n" );
        sb.append( "\n*** " );
        sb.append( elementMap.size() );
        sb.append( " global element declarations ***\n" );
        for( ElementDeclaration elemDec : elementMap.values() ){
            sb.append( elemDec.toString( "" ) );
        }
        sb.append( "\n*** " );
        sb.append( simpleTypeMap.size() );
        sb.append( " global simple type declarations ***\n" );
        for( SimpleTypeDeclaration type : simpleTypeMap.values() ){
            sb.append( type.toString( "" ) );
        }
        sb.append( "\n*** " );
        sb.append( complexTypeMap.size() );
        sb.append( " global complex type declarations ***\n" );
        for( ComplexTypeDeclaration type : complexTypeMap.values() ){
            sb.append( type.toString( "" ) );
        }
        return sb.toString();
    }
}
