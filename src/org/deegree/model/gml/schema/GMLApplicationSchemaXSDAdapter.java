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
package org.deegree.model.gml.schema;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.xerces.impl.dv.XSSimpleType;
import org.apache.xerces.xs.XSComplexTypeDefinition;
import org.apache.xerces.xs.XSConstants;
import org.apache.xerces.xs.XSElementDeclaration;
import org.apache.xerces.xs.XSModelGroup;
import org.apache.xerces.xs.XSObjectList;
import org.apache.xerces.xs.XSParticle;
import org.apache.xerces.xs.XSSimpleTypeDefinition;
import org.apache.xerces.xs.XSTerm;
import org.apache.xerces.xs.XSTypeDefinition;
import org.deegree.model.feature.types.FeatureType;
import org.deegree.model.feature.types.GenericFeatureType;
import org.deegree.model.feature.types.PropertyType;
import org.deegree.model.feature.types.SimplePropertyType;
import org.deegree.model.generic.xsd.ApplicationSchemaXSDAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO add documentation here
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
public class GMLApplicationSchemaXSDAdapter {

    private Logger LOG = LoggerFactory.getLogger( ApplicationSchemaXSDAdapter.class );

    private XSModelGMLAnalyzer analyzer;

    public GMLApplicationSchemaXSDAdapter( String url, GMLVersion gmlVersion ) throws ClassCastException,
                            ClassNotFoundException, InstantiationException, IllegalAccessException {
        analyzer = new XSModelGMLAnalyzer( url, gmlVersion );
    }

    public FeatureType[] extractFeatureTypes() {

        List<XSElementDeclaration> featureElementDecls = analyzer.getFeatureElementDeclarations( null, false );
        FeatureType[] fts = new FeatureType[featureElementDecls.size()];
        int i = 0;
        for ( XSElementDeclaration featureElementDecl : featureElementDecls ) {
            fts[i++] = buildFeatureType( featureElementDecl );
        }
        return fts;
    }

    private FeatureType buildFeatureType( XSElementDeclaration featureElementDecl ) {
        QName ftName = new QName( featureElementDecl.getName(), featureElementDecl.getNamespace() );
        LOG.debug( "Building feature type declaration: '" + ftName + "'" );

        if ( featureElementDecl.getTypeDefinition().getType() == XSTypeDefinition.SIMPLE_TYPE ) {
            String msg = "The type of feature element '" + ftName
                         + "' is simple, but feature elements must always have a complex type.";
            throw new IllegalArgumentException( msg );
        }

        // extract property types from type definition
        List<PropertyType> pts = new ArrayList<PropertyType>();
        XSComplexTypeDefinition typeDef = (XSComplexTypeDefinition) featureElementDecl.getTypeDefinition();

        // element contents
        switch ( typeDef.getContentType() ) {
        case XSComplexTypeDefinition.CONTENTTYPE_ELEMENT: {
            XSParticle particle = typeDef.getParticle();
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
                            int minOccurs = particle.getMinOccurs();
                            int maxOccurs = particle.getMaxOccursUnbounded() ? -1 : particle.getMaxOccurs();
                            addPropertyType( pts, elementDecl2, minOccurs, maxOccurs );
                        }
                        case XSConstants.WILDCARD: {
                            LOG.warn( "Unhandled particle: WILDCARD" );
                            break;
                        }
                        case XSConstants.MODEL_GROUP: {
                            XSModelGroup modelGroup2 = (XSModelGroup) particle2.getTerm();
                            addPropertyTypes( pts, modelGroup2 );
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
            // contents = new Sequence( elements );
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
            XSSimpleTypeDefinition simpleType = typeDef.getSimpleType();
            QName simpleTypeName = new QName( simpleType.getNamespace(), simpleType.getName() );
            // contents = new SimpleContent( simpleTypeName );
            break;
        }
        default: {
            assert false;
        }
        }

        return new GenericFeatureType( ftName, pts, featureElementDecl.getAbstract() );
    }

    private void addPropertyTypes( List<PropertyType> pts, XSModelGroup modelGroup ) {
        LOG.info( " - processing model group..." );
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
                XSParticle particle = (XSParticle) sequence.item( i );
                switch ( particle.getTerm().getType() ) {
                case XSConstants.ELEMENT_DECLARATION: {
                    XSElementDeclaration elementDecl = (XSElementDeclaration) particle.getTerm();
                    int minOccurs = particle.getMinOccurs();
                    int maxOccurs = particle.getMaxOccursUnbounded() ? -1 : particle.getMaxOccurs();
                    addPropertyType( pts, elementDecl, minOccurs, maxOccurs );
                    break;
                }
                case XSConstants.WILDCARD: {
                    LOG.warn( "Unhandled particle: WILDCARD" );
                    break;
                }
                case XSConstants.MODEL_GROUP: {
                    XSModelGroup modelGroup2 = (XSModelGroup) particle.getTerm();
                    addPropertyTypes( pts, modelGroup2 );
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
    }

    private void addPropertyType( List<PropertyType> pts, XSElementDeclaration elementDecl, int minOccurs, int maxOccurs ) {
        QName ptName = new QName( elementDecl.getNamespace(), elementDecl.getName() );
        LOG.debug( "*** Found property declaration: '" + elementDecl.getName() + "'." );
        XSTypeDefinition typeDef = elementDecl.getTypeDefinition();
        switch ( typeDef.getTypeCategory() ) {
        case XSTypeDefinition.SIMPLE_TYPE: {
            SimplePropertyType pt = new SimplePropertyType( ptName, minOccurs, maxOccurs, (XSSimpleType) typeDef );
            pts.add( pt );
            break;
        }
        case XSTypeDefinition.COMPLEX_TYPE: {
            QName typeName = new QName (typeDef.getName(), typeDef.getNamespace());
            SimplePropertyType pt = new SimplePropertyType( ptName, minOccurs, maxOccurs, typeName );
            pts.add( pt );
            break;
        }
        }
    }
}
