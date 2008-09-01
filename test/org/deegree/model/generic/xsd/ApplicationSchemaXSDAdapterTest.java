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
package org.deegree.model.generic.xsd;

import org.apache.xerces.xs.XSComplexTypeDefinition;
import org.apache.xerces.xs.XSConstants;
import org.apache.xerces.xs.XSElementDeclaration;
import org.apache.xerces.xs.XSImplementation;
import org.apache.xerces.xs.XSLoader;
import org.apache.xerces.xs.XSModel;
import org.apache.xerces.xs.XSModelGroup;
import org.apache.xerces.xs.XSNamedMap;
import org.apache.xerces.xs.XSObjectList;
import org.apache.xerces.xs.XSParticle;
import org.apache.xerces.xs.XSSimpleTypeDefinition;
import org.apache.xerces.xs.XSTypeDefinition;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;

/**
 * TODO add documentation here
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
public class ApplicationSchemaXSDAdapterTest {

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp()
                            throws Exception {
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown()
                            throws Exception {
    }

    @Test
    public void parseSchema()
                            throws ClassCastException, ClassNotFoundException, InstantiationException,
                            IllegalAccessException {

        System.setProperty( DOMImplementationRegistry.PROPERTY, "org.apache.xerces.dom.DOMXSImplementationSourceImpl" );
        DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
        XSImplementation impl = (XSImplementation) registry.getDOMImplementation( "XS-Loader" );
        XSLoader schemaLoader = impl.createXSLoader( null );

        // String schemaURL = "file:///home/schneider/workspace/d3_commons/resources/model/examples/ipo/ipo.xsd";
        // String schemaURL =
        // "file:///home/schneider/workspace/d3_commons/resources/model/examples/imro2006/IMRO2006.xsd";
        // String schemaURL =
        // "file:///home/schneider/workspace/d3_commons/resources/model/examples/imro2008/IMRO2008.xsd";

        // String schemaURL =
        // "file:///home/schneider/workspace/d3_commons/resources/model/examples/philosopher/Philosopher.xsd";
        String schemaURL = "file:///home/schneider/workspace/vrom_roonline2//docs/spezifikation/schemas/imro2008/local-IMRO2008.xsd";
        // String schemaURL =
        // "file:///home/schneider/workspace/d3_commons/resources/model/examples/xplanung/XPlanGml.xsd";
        // String schemaURL =
        // "file:///home/schneider/workspace/d3_commons/resources/model/examples/generic_xml/test3.xsd";
        XSModel model = schemaLoader.loadURI( schemaURL );

        // XSNamedMap elementMap = model.getComponentsByNamespace( XSConstants.ELEMENT_DECLARATION,
        // "http://www.geonovum.nl/imro2008" );
        XSNamedMap elementMap = model.getComponentsByNamespace( XSConstants.ELEMENT_DECLARATION,
                                                                "http://www.geonovum.nl/imro/2008/1" );
        for ( int i = 0; i < elementMap.getLength(); i++ ) {
            XSElementDeclaration elementDecl = (XSElementDeclaration) elementMap.item( i );
            System.out.println( toString( elementDecl ) + "\n" );
        }

        // ApplicationSchemaXSDAdapter adapter = new ApplicationSchemaXSDAdapter (model);
        // ApplicationSchema appSchema = adapter.parse();
        // ObjectType ot = appSchema.getObjectType( new QName ("http://www.deegree.org/app", "Country") );
        // System.out.println (ot);
    }

    String toString( XSElementDeclaration elementDecl ) {
        String s = "Element name: '" + elementDecl.getName() + "', (" + elementDecl.getNamespace() + ")";
        s += "\n - abstract: " + ( elementDecl.getAbstract() ? "true" : "false" );

        if ( elementDecl.getSubstitutionGroupAffiliation() != null ) {
            s += "\n - substitutionGroup hierarchy: ";
            s += generateSubstitutionHierarchy( elementDecl.getSubstitutionGroupAffiliation(), "  " );
        }

        XSTypeDefinition typeDef = elementDecl.getTypeDefinition();
        switch ( typeDef.getTypeCategory() ) {
        case XSTypeDefinition.SIMPLE_TYPE: {
            s += generateSimpleContentInfo( (XSSimpleTypeDefinition) typeDef );
            break;
        }
        case XSTypeDefinition.COMPLEX_TYPE: {
            s += generateComplexContentInfo( (XSComplexTypeDefinition) typeDef );
            break;
        }
        default: {
            // cannot happen
        }
        }
        return s;
    }

    String generateSimpleContentInfo( XSSimpleTypeDefinition simpleType ) {
        String s = "\n - simple type: '" + simpleType.getName() + "' (" + simpleType.getNamespace() + ")";
        XSTypeDefinition baseType = simpleType.getBaseType();
        if ( baseType != null ) {
            s += "\n - type hierarchy:";
            s += generateTypeHierarchy( baseType, "  " );
        }
        return s;
    }

    String generateComplexContentInfo( XSComplexTypeDefinition complexType ) {
        String s = "\n - complex type: '" + complexType.getName() + "' (" + complexType.getNamespace() + ")";
        XSTypeDefinition baseType = complexType.getBaseType();
        if ( baseType != null ) {
            s += "\n - type hierarchy:";
            s += generateTypeHierarchy( baseType, "  " );
        }

        s += "\n - content model: ";
        switch ( complexType.getContentType() ) {
        case XSComplexTypeDefinition.CONTENTTYPE_ELEMENT: {
            s += "element only";
            XSParticle particle = complexType.getParticle();
            s += generateParticleHierarchy( particle, "  " );
            break;
        }
        case XSComplexTypeDefinition.CONTENTTYPE_EMPTY: {
            s += "empty";
            break;
        }
        case XSComplexTypeDefinition.CONTENTTYPE_MIXED: {
            s += "mixed";
            XSParticle particle = complexType.getParticle();
            s += generateParticleHierarchy( particle, "  " );
            break;
        }
        case XSComplexTypeDefinition.CONTENTTYPE_SIMPLE: {
            s += "simple";
            break;
        }
        default: {
            // cannot happen
        }
        }

        return s;
    }

    String generateTypeHierarchy( XSTypeDefinition type, String indent ) {

        String s = "\n" + indent + "-> '" + type.getName() + "' (" + type.getNamespace() + "'): ";
        switch ( type.getTypeCategory() ) {
        case XSTypeDefinition.SIMPLE_TYPE: {
            s += "simple";
            break;
        }
        case XSTypeDefinition.COMPLEX_TYPE: {
            s += "complex";
            break;
        }
        default: {
            // cannot happen
        }
        }
        if ( type.getBaseType() != null && type.getBaseType() != type ) {
            s += generateTypeHierarchy( type.getBaseType(), " " + indent );
        }
        return s;
    }

    String generateParticleHierarchy( XSParticle particle, String indent ) {

        String s = "";

        switch ( particle.getTerm().getType() ) {
        case XSConstants.MODEL_GROUP: {
            XSModelGroup modelGroup = (XSModelGroup) particle.getTerm();
            switch ( modelGroup.getCompositor() ) {
            case XSModelGroup.COMPOSITOR_ALL: {
                s = "\n" + indent + "- all " + generateOccurenceInfo( particle );
                XSObjectList subParticles = modelGroup.getParticles();
                for ( int i = 0; i < subParticles.getLength(); i++ ) {
                    XSParticle subParticle = (XSParticle) subParticles.item( i );
                    s += generateParticleHierarchy( subParticle, " " + indent );
                }
                break;
            }
            case XSModelGroup.COMPOSITOR_CHOICE: {
                s = "\n" + indent + "- choice " + generateOccurenceInfo( particle );
                XSObjectList subParticles = modelGroup.getParticles();
                for ( int i = 0; i < subParticles.getLength(); i++ ) {
                    XSParticle subParticle = (XSParticle) subParticles.item( i );
                    s += generateParticleHierarchy( subParticle, " " + indent );
                }
                break;
            }
            case XSModelGroup.COMPOSITOR_SEQUENCE: {
                if ( !isParticleRedundant( particle ) ) {
                    s = "\n" + indent + "- sequence " + generateOccurenceInfo( particle );
                    XSObjectList subParticles = modelGroup.getParticles();
                    for ( int i = 0; i < subParticles.getLength(); i++ ) {
                        XSParticle subParticle = (XSParticle) subParticles.item( i );
                        s += generateParticleHierarchy( subParticle, " " + indent );
                    }
                } else {
                    XSObjectList subParticles = modelGroup.getParticles();
                    for ( int i = 0; i < subParticles.getLength(); i++ ) {
                        XSParticle subParticle = (XSParticle) subParticles.item( i );
                        s += generateParticleHierarchy( subParticle, indent );
                    }
                }
                break;
            }
            default: {
                // cannot happen
            }
            }
            break;
        }
        case XSConstants.ELEMENT_DECLARATION: {
            XSElementDeclaration elementDecl = (XSElementDeclaration) particle.getTerm();
            s = "\n" + indent + "- element: '" + elementDecl.getName() + "' (" + elementDecl.getNamespace() + ") "
                + generateOccurenceInfo( particle );
            break;
        }
        case XSConstants.WILDCARD: {
            s = "\n" + indent + "- wildcard " + generateOccurenceInfo( particle );
            break;
        }
        default: {
            // cannot happen
        }
        }
        return s;
    }

    boolean isParticleRedundant( XSParticle particle ) {
        if ( particle.getMaxOccursUnbounded() ) {
            return true;
        }
        return particle.getMinOccurs() == 1 && particle.getMaxOccurs() == 1;
    }

    String generateOccurenceInfo( XSParticle particle ) {
        return "(minOccurs=" + particle.getMinOccurs() + ", maxOccurs="
               + ( particle.getMaxOccursUnbounded() ? "unbounded" : particle.getMaxOccurs() ) + ")";
    }

    String generateSubstitutionHierarchy( XSElementDeclaration elementDecl, String indent ) {
        if ( elementDecl == null ) {
            return "";
        }
        String s = "\n" + indent + "-> '" + elementDecl.getName() + "' (" + elementDecl.getNamespace() + ")";
        s += generateSubstitutionHierarchy( elementDecl.getSubstitutionGroupAffiliation(), indent + " " );
        return s;
    }
}
