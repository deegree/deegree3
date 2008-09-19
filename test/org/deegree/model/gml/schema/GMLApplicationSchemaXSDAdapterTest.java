package org.deegree.model.gml.schema;


import org.apache.xerces.xs.XSComplexTypeDefinition;
import org.apache.xerces.xs.XSConstants;
import org.apache.xerces.xs.XSElementDeclaration;
import org.apache.xerces.xs.XSModelGroup;
import org.apache.xerces.xs.XSObjectList;
import org.apache.xerces.xs.XSParticle;
import org.apache.xerces.xs.XSSimpleTypeDefinition;
import org.apache.xerces.xs.XSTypeDefinition;
import org.deegree.model.feature.types.FeatureType;
import org.junit.Test;

public class GMLApplicationSchemaXSDAdapterTest {

    @Test
    public void testParsing () throws ClassCastException, ClassNotFoundException, InstantiationException, IllegalAccessException {

        String schemaURL = this.getClass().getResource( "Road.xsd").toString();
        GMLApplicationSchemaXSDAdapter adapter = new GMLApplicationSchemaXSDAdapter (schemaURL, GMLVersion.VERSION_31);
        FeatureType [] fts = adapter.extractFeatureTypes();
        for ( FeatureType ft : fts ) {
            System.out.println (ft);
        }
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
