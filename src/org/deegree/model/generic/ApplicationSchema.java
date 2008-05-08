//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------

 This file is part of deegree.
 Copyright (C) 2001-2008 by:
 EXSE, Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 Contact:

 Andreas Poth  
 lat/lon GmbH 
 Aennchenstr. 19
 53115 Bonn
 Germany
 E-Mail: poth@lat-lon.de

 Prof. Dr. Klaus Greve
 Department of Geography
 University of Bonn
 Meckenheimer Allee 166
 53115 Bonn
 Germany
 E-Mail: greve@giub.uni-bonn.de


 ---------------------------------------------------------------------------*/
package org.deegree.model.generic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xerces.xs.XSAttributeDeclaration;
import org.apache.xerces.xs.XSAttributeUse;
import org.apache.xerces.xs.XSComplexTypeDefinition;
import org.apache.xerces.xs.XSConstants;
import org.apache.xerces.xs.XSElementDeclaration;
import org.apache.xerces.xs.XSModel;
import org.apache.xerces.xs.XSModelGroup;
import org.apache.xerces.xs.XSNamedMap;
import org.apache.xerces.xs.XSNamespaceItem;
import org.apache.xerces.xs.XSNamespaceItemList;
import org.apache.xerces.xs.XSObjectList;
import org.apache.xerces.xs.XSParticle;
import org.apache.xerces.xs.XSSimpleTypeDefinition;
import org.apache.xerces.xs.XSTerm;
import org.apache.xerces.xs.XSTypeDefinition;
import org.deegree.model.generic.implementation.schema.GenericAttributeType;
import org.deegree.model.generic.implementation.schema.GenericElementType;
import org.deegree.model.generic.schema.AttributeType;
import org.deegree.model.generic.schema.ContentModel;
import org.deegree.model.generic.schema.ElementType;
import org.deegree.model.generic.schema.Sequence;
import org.deegree.model.generic.schema.SimpleContent;

/**
 * An <code>ApplicationSchema</code> wraps an XML Schema InfoSet to provide a view of its elements with a generic
 * {@link ElementType} semantic.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
public class ApplicationSchema {

    private Log LOG = LogFactory.getLog( ApplicationSchema.class );

    private XSModel xmlSchema;

    // global elements defined in the schema
    private Map<QName, ElementType> globalElementsMap = new HashMap<QName, ElementType>();

    /**
     * Constructs a new <code>ApplicationSchema</code> from the given XML Schema InfoSet.
     * 
     * @param xmlSchema
     */
    public ApplicationSchema( XSModel xmlSchema ) {
        this.xmlSchema = xmlSchema;
        analyzeNamespaces( xmlSchema );
        analyzeTypeDefinitions( xmlSchema );
        // analyzeElementDeclarations( xmlSchema );
    }

    private void analyzeNamespaces( XSModel xmlSchema ) {
        LOG.info( "Analyzing namespaces..." );
        XSNamespaceItemList nsItemList = xmlSchema.getNamespaceItems();
        for ( int i = 0; i < nsItemList.getLength(); i++ ) {
            XSNamespaceItem item = nsItemList.item( i );
            LOG.info( item.getSchemaNamespace() );
        }
    }

    private void analyzeTypeDefinitions( XSModel xmlSchema ) {
        XSNamespaceItemList nsItemList = xmlSchema.getNamespaceItems();
        for ( int i = 0; i < nsItemList.getLength(); i++ ) {
            XSNamespaceItem item = nsItemList.item( i );
            LOG.info( "Analyzing type definitions (namespace='" + item.getSchemaNamespace() + ")..." );
            XSNamedMap typeMap = item.getComponents( XSConstants.TYPE_DEFINITION );
            for ( int j = 0; j < typeMap.getLength(); j++ ) {
                XSTypeDefinition typeDef = (XSTypeDefinition) typeMap.item( j );
                System.out.println( typeDef.getName() );
            }
        }        
    }

    private void analyzeElementDeclarations( XSModel xmlSchema ) {
        XSNamespaceItemList nsItemList = xmlSchema.getNamespaceItems();
        XSNamespaceItem item = nsItemList.item( 0 );
        LOG.info( "Analyzing element declaration (namespace='" + item.getSchemaNamespace() + ")..." );
        XSNamedMap typeMap = item.getComponents( XSConstants.TYPE_DEFINITION );
        for ( int i = 0; i < typeMap.getLength(); i++ ) {
            XSTypeDefinition typeDef = (XSTypeDefinition) typeMap.item( i );
            System.out.println( typeDef.getName() );
        }
    }

    private ElementType buildElementType( XSElementDeclaration elementDecl ) {

        ContentModel contents = null;
        ElementType elementType = null;
        QName elementName = new QName( elementDecl.getNamespace(), elementDecl.getName() );
        XSTypeDefinition typeDefinition = elementDecl.getTypeDefinition();

        List<AttributeType> attributes = new ArrayList<AttributeType>();

        switch ( typeDefinition.getTypeCategory() ) {
        case XSTypeDefinition.COMPLEX_TYPE: {
            List<ElementType> elements = new ArrayList<ElementType>();
            XSComplexTypeDefinition complexType = (XSComplexTypeDefinition) typeDefinition;

            // gather attribute types
            XSObjectList attributeUses = complexType.getAttributeUses();
            for ( int j = 0; j < attributeUses.getLength(); j++ ) {
                XSAttributeUse attributeUse = (XSAttributeUse) attributeUses.item( j );
                XSAttributeDeclaration attributeDecl = attributeUse.getAttrDeclaration();

                QName attrName = new QName( attributeDecl.getNamespace(), attributeDecl.getName() );
                AttributeType attribute = new GenericAttributeType( attrName, attributeUse.getRequired() );
                attributes.add( attribute );
            }

            // element contents
            switch ( complexType.getContentType() ) {
            case XSComplexTypeDefinition.CONTENTTYPE_ELEMENT: {
                XSParticle particle = complexType.getParticle();
                XSTerm term = particle.getTerm();
                switch ( term.getType() ) {
                case XSConstants.MODEL_GROUP: {
                    XSModelGroup modelGroup = (XSModelGroup) term;
                    switch ( modelGroup.getCompositor() ) {
                    case XSModelGroup.COMPOSITOR_ALL: {
                        LOG.warn( "Unhandled model group: COMPOSITOR_ALL" );
                        break;
                    }
                    case XSModelGroup.COMPOSITOR_CHOICE: {
                        LOG.warn( "Unhandled model group: COMPOSITOR_CHOICE" );
                        break;
                    }
                    case XSModelGroup.COMPOSITOR_SEQUENCE: {
                        XSObjectList sequence = modelGroup.getParticles();
                        for ( int i = 0; i < sequence.getLength(); i++ ) {
                            XSParticle particle2 = (XSParticle) sequence.item( i );
                            switch ( particle2.getTerm().getType() ) {
                            case XSConstants.ELEMENT_DECLARATION: {
                                XSElementDeclaration elementDecl2 = (XSElementDeclaration) particle2.getTerm();
                                elements.add( buildElementType( elementDecl2 ) );
                            }
                            case XSConstants.WILDCARD: {
                                LOG.warn( "Unhandled particle: WILDCARD" );
                                break;
                            }
                            case XSConstants.MODEL_GROUP: {
                                LOG.warn( "Unhandled particle: MODEL_GROUP" );
                                break;
                            }
                            }
                        }
                        break;
                    }
                    default: {
                        assert false;
                    }
                    }
                    break;
                }
                case XSConstants.WILDCARD: {
                    LOG.warn( "Unhandled particle: WILDCARD" );
                    break;
                }
                case XSConstants.ELEMENT_DECLARATION: {
                    LOG.warn( "Unhandled particle: ELEMENT_DECLARATION" );
                    break;
                }
                default: {
                    assert false;
                }
                }
                contents = new Sequence( elements );
                break;
            }
            case XSComplexTypeDefinition.CONTENTTYPE_EMPTY: {
                LOG.warn( "Unhandled content type: EMPTY" );
                break;
            }
            case XSComplexTypeDefinition.CONTENTTYPE_MIXED: {
                LOG.warn( "Unhandled content type: MIXED" );
                break;
            }
            case XSComplexTypeDefinition.CONTENTTYPE_SIMPLE: {
                XSSimpleTypeDefinition simpleType = complexType.getSimpleType();
                QName simpleTypeName = new QName( simpleType.getNamespace(), simpleType.getName() );
                contents = new SimpleContent( simpleTypeName );
                break;
            }
            default: {
                assert false;
            }
            }
            break;
        }
        case XSTypeDefinition.SIMPLE_TYPE: {
            XSSimpleTypeDefinition simpleType = (XSSimpleTypeDefinition) typeDefinition;
            QName simpleTypeName = null;
            if ( simpleType.getName() != null ) {
                simpleTypeName = new QName( simpleType.getNamespace(), simpleType.getName() );
            }
            contents = new SimpleContent( simpleTypeName );
            break;
        }
        default: {
            assert false;
        }
        }
        elementType = new GenericElementType( elementName, attributes, contents );
        return elementType;
    }

    /**
     * Returns all {@link StructuredObjectType}s that are defined in the schema.
     * 
     * @return all feature types of the schema
     */
    // public StructuredObjectType[] getStructuredTypes() {
    // return typeMap.values().toArray( new StructuredObjectType[typeMap.size()] );
    // }
    /**
     * Looks up the {@link StructuredObjectType} with the given {@link QName}.
     * 
     * @param qName
     *            the QualifiedName to look up
     * @return the StructuredObjectType, if it is defined in the schema, null otherwise
     */
    // public StructuredObjectType getFeatureType( QName qName ) {
    // return typeMap.get( qName );
    // }
    // private void buildFeatureContentModel( XSParticle particle, List<PropertyType> properties ) {
    //
    // XSTerm term = particle.getTerm();
    // switch ( term.getType() ) {
    // case XSConstants.ELEMENT_DECLARATION: {
    // XSElementDeclaration elementDecl = (XSElementDeclaration) term;
    // int minOccurs = particle.getMinOccurs();
    // int maxOccurs = particle.getMaxOccurs();
    // QName propertyName = new QName( term.getNamespace(), term.getName() );
    // XSTypeDefinition typeDef = elementDecl.getTypeDefinition();
    // switch ( typeDef.getTypeCategory() ) {
    // case XSTypeDefinition.SIMPLE_TYPE: {
    // properties.add( buildSimplePropertyType( propertyName, minOccurs, maxOccurs,
    // (XSSimpleTypeDefinition) typeDef ) );
    // break;
    // }
    // case XSTypeDefinition.COMPLEX_TYPE: {
    // properties.add( buildComplexPropertyType( propertyName, minOccurs, maxOccurs,
    // (XSComplexTypeDefinition) typeDef ) );
    // break;
    // }
    // default: {
    // assert false;
    // }
    // }
    //
    // break;
    // }
    // case XSConstants.MODEL_GROUP: {
    // XSModelGroup modelGroup = (XSModelGroup) term;
    // switch ( modelGroup.getCompositor() ) {
    // case XSModelGroup.COMPOSITOR_ALL:
    // case XSModelGroup.COMPOSITOR_CHOICE: {
    // String msg = "Cannot build feature type: schema uses choice/mixed content model (only sequences are supported).";
    // throw new RuntimeException( msg );
    // }
    // }
    // XSObjectList particles = modelGroup.getParticles();
    // for ( int i = 0; i < particles.getLength(); i++ ) {
    // XSParticle particle2 = (XSParticle) particles.item( i );
    // buildFeatureContentModel( particle2, properties );
    // }
    // break;
    // }
    // case XSConstants.WILDCARD: {
    // String msg = "Cannot build feature type: schema uses wildcards in content model (only sequences are supported).";
    // throw new RuntimeException( msg );
    // }
    // }
    // }
    // private PropertyType buildComplexPropertyType( QName propertyName, int minOccurs, int maxOccurs,
    // XSComplexTypeDefinition typeDef ) {
    //
    // System.out.println( "complex property type definition: " + propertyName + ", " + typeDef.getName() );
    // ComplexPropertyType pt = null;
    // if ( GML_NS.equals( typeDef.getNamespace() ) ) {
    // String localName = typeDef.getName();
    // if ( "FeaturePropertyType".equals( localName ) ) {
    // pt = new FeaturePropertyType( propertyName, minOccurs, maxOccurs );
    // } else if ( "GeometryPropertyType".equals( localName ) ) {
    // pt = new GeometryPropertyType( propertyName, minOccurs, maxOccurs );
    // }
    // }
    // return pt;
    // }
    // private SimplePropertyType buildSimplePropertyType( QName propertyName, int minOccurs, int maxOccurs,
    // XSSimpleTypeDefinition typeDef ) {
    // System.out.println( "simple property type definition: " + propertyName );
    // SimplePropertyType pt = new SimplePropertyType( propertyName, minOccurs, maxOccurs, typeDef.getBuiltInKind() );
    // return pt;
    // }
    private static void printParticle( XSParticle particle, String indent ) {
        System.out.print( indent + "particle: name=" + particle.getName() + ", minOccurs=" + particle.getMinOccurs()
                          + ", maxOccurs=" + particle.getMaxOccurs() + ", type: " );
        XSTerm term = particle.getTerm();
        switch ( term.getType() ) {
        case XSConstants.ELEMENT_DECLARATION: {
            System.out.println( "element declaration ({" + term.getNamespace() + "}:" + term.getName() + ")" );
            break;
        }
        case XSConstants.MODEL_GROUP: {
            System.out.print( "model group (" );
            XSModelGroup modelGroup = (XSModelGroup) term;
            switch ( modelGroup.getCompositor() ) {
            case XSModelGroup.COMPOSITOR_ALL: {
                System.out.println( "all)" );
                break;
            }
            case XSModelGroup.COMPOSITOR_CHOICE: {
                System.out.println( "choice)" );
                break;
            }
            case XSModelGroup.COMPOSITOR_SEQUENCE: {
                System.out.println( "sequence)" );
                break;
            }
            }
            XSObjectList particles = modelGroup.getParticles();
            for ( int i = 0; i < particles.getLength(); i++ ) {
                XSParticle particle2 = (XSParticle) particles.item( i );
                printParticle( particle2, indent + "  " );
            }
            break;
        }
        case XSConstants.WILDCARD: {
            System.out.println( "wildcard" );
            break;
        }
        }
    }
}