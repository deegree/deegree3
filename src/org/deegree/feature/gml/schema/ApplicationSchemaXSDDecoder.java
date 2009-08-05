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
package org.deegree.feature.gml.schema;

import static org.deegree.commons.xml.CommonNamespaces.GMLNS;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.deegree.commons.gml.GMLVersion;
import org.deegree.commons.gml.schema.XSModelGMLAnalyzer;
import org.deegree.commons.xml.CommonNamespaces;
import org.deegree.feature.types.ApplicationSchema;
import org.deegree.feature.types.FeatureType;
import org.deegree.feature.types.GenericFeatureCollectionType;
import org.deegree.feature.types.GenericFeatureType;
import org.deegree.feature.types.property.CodePropertyType;
import org.deegree.feature.types.property.CustomComplexPropertyType;
import org.deegree.feature.types.property.EnvelopePropertyType;
import org.deegree.feature.types.property.FeaturePropertyType;
import org.deegree.feature.types.property.GeometryPropertyType;
import org.deegree.feature.types.property.MeasurePropertyType;
import org.deegree.feature.types.property.PropertyType;
import org.deegree.feature.types.property.SimplePropertyType;
import org.deegree.feature.types.property.GeometryPropertyType.CoordinateDimension;
import org.deegree.feature.types.property.GeometryPropertyType.GeometryType;
import org.deegree.feature.types.property.SimplePropertyType.PrimitiveType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides convenient access to the {@link FeatureType} hierarchy defined in a GML schema infoset.
 * <p>
 * Note that the generated {@link ApplicationSchema} contains only user-defined feature types, i.e. all types from the
 * GML namespace (e.g. <code>gml:_Feature</code> or <code>gml:FeatureCollection</code>) are ignored. This supports to
 * work with the application schemas without relying on GML (and GML-version) specific details.
 * </p>
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
public class ApplicationSchemaXSDDecoder {

    private Logger LOG = LoggerFactory.getLogger( ApplicationSchemaXSDDecoder.class );

    private XSModelGMLAnalyzer analyzer;

    private Map<QName, XSElementDeclaration> ftNameToftElement = new HashMap<QName, XSElementDeclaration>();

    private Map<QName, XSElementDeclaration> geometryNameToGeometryElement = new HashMap<QName, XSElementDeclaration>();

    private Map<QName, FeatureType> ftNameToft = new HashMap<QName, FeatureType>();

    // key: name of ft A, value: name of ft B (A is in substitionGroup B)
    private Map<QName, QName> ftSubstitutionGroupRelation = new HashMap<QName, QName>();

    // stores all feature property types, so the reference to the contained FeatureType can be resolved,
    // after all FeatureTypes have been created
    private List<FeaturePropertyType> featurePropertyTypes = new ArrayList<FeaturePropertyType>();

    public ApplicationSchemaXSDDecoder( GMLVersion gmlVersion, String... schemaUrls ) throws ClassCastException,
                            ClassNotFoundException, InstantiationException, IllegalAccessException {
        analyzer = new XSModelGMLAnalyzer( gmlVersion, schemaUrls );
        List<XSElementDeclaration> featureElementDecls = analyzer.getFeatureElementDeclarations( null, false );
        for ( XSElementDeclaration elementDecl : featureElementDecls ) {
            QName ftName = new QName( elementDecl.getNamespace(), elementDecl.getName() );
            ftNameToftElement.put( ftName, elementDecl );
            XSElementDeclaration substitutionElement = elementDecl.getSubstitutionGroupAffiliation();
            if ( substitutionElement != null ) {
                QName substitutionElementName = new QName( substitutionElement.getNamespace(),
                                                           substitutionElement.getName() );
                ftSubstitutionGroupRelation.put( ftName, substitutionElementName );
            }
        }
        List<XSElementDeclaration> geometryElementDecls = analyzer.getGeometryElementDeclarations( null, false );
        for ( XSElementDeclaration elementDecl : geometryElementDecls ) {
            QName ftName = new QName( elementDecl.getNamespace(), elementDecl.getName() );
            geometryNameToGeometryElement.put( ftName, elementDecl );
        }
    }

    public ApplicationSchema extractFeatureTypeSchema() {

        for ( QName ftName : ftNameToftElement.keySet() ) {
            FeatureType ft = buildFeatureType( ftNameToftElement.get( ftName ) );
            if ( !CommonNamespaces.GMLNS.equals( ft.getName().getNamespaceURI() ) ) {
                ftNameToft.put( ftName, ft );
            }
        }
        // resolveFtReferences();

        FeatureType[] fts = ftNameToft.values().toArray( new FeatureType[ftNameToft.size()] );
        Map<FeatureType, FeatureType> ftSubstitution = new HashMap<FeatureType, FeatureType>();
        for ( QName ftName : ftSubstitutionGroupRelation.keySet() ) {
            QName substitutionFtName = ftSubstitutionGroupRelation.get( ftName );
            ftSubstitution.put( ftNameToft.get( ftName ), ftNameToft.get( substitutionFtName ) );
        }
        return new ApplicationSchema( fts, ftSubstitution, analyzer.getXSModel() );
    }

    private void resolveFtReferences() {
        for ( FeaturePropertyType pt : featurePropertyTypes ) {
            LOG.debug( "Resolving reference to feature type: '" + pt.getFTName() + "'" );
            pt.resolve( ftNameToft.get( pt.getFTName() ) );
        }
    }

    private FeatureType buildFeatureType( XSElementDeclaration featureElementDecl ) {
        QName ftName = new QName( featureElementDecl.getNamespace(), featureElementDecl.getName() );
        LOG.debug( "Building feature type declaration: '" + ftName + "'" );

        if ( featureElementDecl.getTypeDefinition().getType() == XSTypeDefinition.SIMPLE_TYPE ) {
            String msg = "The schema type of feature element '" + ftName
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
                    LOG.debug( "Unhandled model group: COMPOSITOR_ALL" );
                    break;
                }
                case XSModelGroup.COMPOSITOR_CHOICE: {
                    LOG.debug( "Unhandled model group: COMPOSITOR_CHOICE" );
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
                            PropertyType pt = buildPropertyType( elementDecl2, minOccurs, maxOccurs );
                            pts.add( pt );
                        }
                        case XSConstants.WILDCARD: {
                            LOG.debug( "Unhandled particle: WILDCARD" );
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
                LOG.debug( "Unhandled particle: WILDCARD" );
                break;
            }
            case XSConstants.ELEMENT_DECLARATION: {
                LOG.debug( "Unhandled particle: ELEMENT_DECLARATION" );
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
            LOG.debug( "Unhandled content type: EMPTY" );
            break;
        }
        case XSComplexTypeDefinition.CONTENTTYPE_MIXED: {
            LOG.debug( "Unhandled content type: MIXED" );
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

        List<XSElementDeclaration> fcDecls = analyzer.getFeatureCollectionElementDeclarations( null, false );
        if ( fcDecls.contains( featureElementDecl ) ) {
            return new GenericFeatureCollectionType( ftName, pts, featureElementDecl.getAbstract() );
        }

        return new GenericFeatureType( ftName, pts, featureElementDecl.getAbstract() );
    }

    private void addPropertyTypes( List<PropertyType> pts, XSModelGroup modelGroup ) {
        LOG.debug( " - processing model group..." );
        switch ( modelGroup.getCompositor() ) {
        case XSModelGroup.COMPOSITOR_ALL: {
            LOG.debug( "Unhandled model group: COMPOSITOR_ALL" );
            break;
        }
        case XSModelGroup.COMPOSITOR_CHOICE: {
            LOG.debug( "Unhandled model group: COMPOSITOR_CHOICE" );
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
                    PropertyType pt = buildPropertyType( elementDecl, minOccurs, maxOccurs );
                    pts.add( pt );
                    break;
                }
                case XSConstants.WILDCARD: {
                    LOG.debug( "Unhandled particle: WILDCARD" );
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

    private PropertyType buildPropertyType( XSElementDeclaration elementDecl, int minOccurs, int maxOccurs ) {
        PropertyType pt = null;
        QName ptName = new QName( elementDecl.getNamespace(), elementDecl.getName() );
        LOG.debug( "*** Found property declaration: '" + elementDecl.getName() + "'." );
        XSTypeDefinition typeDef = elementDecl.getTypeDefinition();
        switch ( typeDef.getTypeCategory() ) {
        case XSTypeDefinition.SIMPLE_TYPE: {
            PrimitiveType type = getPrimitiveType( (XSSimpleType) typeDef );
            pt = new SimplePropertyType( ptName, minOccurs, maxOccurs, type );
            break;
        }
        case XSTypeDefinition.COMPLEX_TYPE: {
            pt = buildPropertyType( elementDecl, (XSComplexTypeDefinition) typeDef, minOccurs, maxOccurs );
            break;
        }
        }
        return pt;
    }

    private PropertyType buildPropertyType( XSElementDeclaration elementDecl, XSComplexTypeDefinition typeDef,
                                            int minOccurs, int maxOccurs ) {

        PropertyType pt = null;
        QName ptName = new QName( elementDecl.getNamespace(), elementDecl.getName() );
        LOG.debug( "- Property definition '" + ptName + "' uses a complex type for content definition." );
        pt = buildFeaturePropertyType( elementDecl, typeDef, minOccurs, maxOccurs );
        if ( pt == null ) {
            pt = buildGeometryPropertyType( elementDecl, typeDef, minOccurs, maxOccurs );
            if ( pt == null ) {
                if ( typeDef.getName() != null ) {
                    // TODO improve detection of property types
                    QName typeName = new QName( typeDef.getNamespace(), typeDef.getName() );
                    if ( typeName.equals( QName.valueOf( "{http://www.opengis.net/gml}CodeType" ) ) ) {
                        LOG.debug( "Identified a CodePropertyType." );
                        pt = new CodePropertyType( ptName, minOccurs, maxOccurs );
                    } else if ( typeName.equals( QName.valueOf( "{http://www.opengis.net/gml}BoundingShapeType" ) ) ) {
                        LOG.debug( "Identified a EnvelopePropertyType." );
                        pt = new EnvelopePropertyType( ptName, minOccurs, maxOccurs );
                    } else if ( typeName.equals( QName.valueOf( "{http://www.opengis.net/gml}LengthType" ) ) ) {
                        LOG.debug( "Identified a LengthPropertyType." );
                        pt = new MeasurePropertyType( ptName, minOccurs, maxOccurs );

                    } else {
                        pt = new CustomComplexPropertyType( ptName, minOccurs, maxOccurs, typeName );
                    }
                } else {
                    pt = new CustomComplexPropertyType( ptName, minOccurs, maxOccurs, null );
                }
            }
        }
        return pt;
    }

    /**
     * Analyzes the given complex type definition and returns a {@link FeaturePropertyType} if it defines a feature
     * property.
     * 
     * @param elementDecl
     * @param typeDef
     * @param minOccurs
     * @param maxOccurs
     * @return corresponding {@link FeaturePropertyType} or null, if declaration does not define a feature property
     */
    private FeaturePropertyType buildFeaturePropertyType( XSElementDeclaration elementDecl,
                                                          XSComplexTypeDefinition typeDef, int minOccurs, int maxOccurs ) {

        QName ptName = new QName( elementDecl.getNamespace(), elementDecl.getName() );
        switch ( typeDef.getContentType() ) {
        case XSComplexTypeDefinition.CONTENTTYPE_ELEMENT: {
            LOG.debug( "CONTENTTYPE_ELEMENT" );
            XSParticle particle = typeDef.getParticle();
            XSTerm term = particle.getTerm();
            switch ( term.getType() ) {
            case XSConstants.MODEL_GROUP: {
                XSModelGroup modelGroup = (XSModelGroup) term;
                switch ( modelGroup.getCompositor() ) {
                case XSModelGroup.COMPOSITOR_ALL: {
                    LOG.debug( "Unhandled model group: COMPOSITOR_ALL" );
                    break;
                }
                case XSModelGroup.COMPOSITOR_CHOICE: {
                    LOG.debug( "Unhandled model group: COMPOSITOR_CHOICE" );
                    break;
                }
                case XSModelGroup.COMPOSITOR_SEQUENCE: {
                    LOG.debug( "Found sequence." );
                    XSObjectList sequence = modelGroup.getParticles();
                    if ( sequence.getLength() != 1 ) {
                        LOG.debug( "Length = '" + sequence.getLength() + "' -> cannot be a feature property." );
                        return null;
                    }
                    XSParticle particle2 = (XSParticle) sequence.item( 0 );
                    switch ( particle2.getTerm().getType() ) {
                    case XSConstants.ELEMENT_DECLARATION: {
                        XSElementDeclaration elementDecl2 = (XSElementDeclaration) particle2.getTerm();
                        int minOccurs2 = particle2.getMinOccurs();
                        int maxOccurs2 = particle2.getMaxOccursUnbounded() ? -1 : particle2.getMaxOccurs();
                        QName elementName = new QName( elementDecl2.getNamespace(), elementDecl2.getName() );
                        if ( ftNameToftElement.get( elementName ) != null ) {
                            LOG.debug( "Identified a feature property." );
                            FeaturePropertyType pt = null;
                            if (GMLNS.equals( elementName.getNamespaceURI() )) {
                                pt = new FeaturePropertyType( ptName, minOccurs, maxOccurs, null );
                            } else {
                                pt = new FeaturePropertyType( ptName, minOccurs, maxOccurs, elementName );
                            }
                            featurePropertyTypes.add( pt );                            
                            return pt;
                        }
                    }
                    case XSConstants.WILDCARD: {
                        LOG.debug( "Unhandled particle: WILDCARD" );
                        break;
                    }
                    case XSConstants.MODEL_GROUP: {
                        LOG.debug( "Unhandled particle: MODEL_GROUP" );
                        break;
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
                LOG.debug( "Unhandled particle: WILDCARD" );
                break;
            }
            case XSConstants.ELEMENT_DECLARATION: {
                LOG.debug( "Unhandled particle: ELEMENT_DECLARATION" );
                break;
            }
            default: {
                assert false;
            }
            }
            // contents = new Sequence( elements );
            break;
        }
        }
        return null;
    }

    /**
     * Analyzes the given complex type definition and returns a {@link GeometryPropertyType} if it defines a geometry
     * property.
     * 
     * @param elementDecl
     * @param typeDef
     * @param minOccurs
     * @param maxOccurs
     * @return corresponding {@link GeometryPropertyType} or null, if declaration does not define a geometry property
     */
    private GeometryPropertyType buildGeometryPropertyType( XSElementDeclaration elementDecl,
                                                            XSComplexTypeDefinition typeDef, int minOccurs,
                                                            int maxOccurs ) {

        QName ptName = new QName( elementDecl.getNamespace(), elementDecl.getName() );
        switch ( typeDef.getContentType() ) {
        case XSComplexTypeDefinition.CONTENTTYPE_ELEMENT: {
            LOG.debug( "CONTENTTYPE_ELEMENT" );
            XSParticle particle = typeDef.getParticle();
            XSTerm term = particle.getTerm();
            switch ( term.getType() ) {
            case XSConstants.MODEL_GROUP: {
                XSModelGroup modelGroup = (XSModelGroup) term;
                switch ( modelGroup.getCompositor() ) {
                case XSModelGroup.COMPOSITOR_ALL: {
                    LOG.debug( "Unhandled model group: COMPOSITOR_ALL" );
                    break;
                }
                case XSModelGroup.COMPOSITOR_CHOICE: {
                    LOG.debug( "Unhandled model group: COMPOSITOR_CHOICE" );
                    break;
                }
                case XSModelGroup.COMPOSITOR_SEQUENCE: {
                    LOG.debug( "Found sequence." );
                    XSObjectList sequence = modelGroup.getParticles();
                    if ( sequence.getLength() != 1 ) {
                        LOG.debug( "Length = '" + sequence.getLength() + "' -> cannot be a feature property." );
                        return null;
                    }
                    XSParticle particle2 = (XSParticle) sequence.item( 0 );
                    switch ( particle2.getTerm().getType() ) {
                    case XSConstants.ELEMENT_DECLARATION: {
                        XSElementDeclaration elementDecl2 = (XSElementDeclaration) particle2.getTerm();
                        int minOccurs2 = particle2.getMinOccurs();
                        int maxOccurs2 = particle2.getMaxOccursUnbounded() ? -1 : particle2.getMaxOccurs();
                        QName elementName = new QName( elementDecl2.getNamespace(), elementDecl2.getName() );
                        if ( geometryNameToGeometryElement.get( elementName ) != null ) {
                            LOG.debug( "Identified a geometry property." );
                            GeometryType geometryType = getGeometryType( elementName );
                            return new GeometryPropertyType( ptName, minOccurs, maxOccurs, geometryType,
                                                             CoordinateDimension.DIM_2_OR_3 );
                        }
                    }
                    case XSConstants.WILDCARD: {
                        LOG.debug( "Unhandled particle: WILDCARD" );
                        break;
                    }
                    case XSConstants.MODEL_GROUP: {
                        LOG.debug( "Unhandled particle: MODEL_GROUP" );
                        break;
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
                LOG.debug( "Unhandled particle: WILDCARD" );
                break;
            }
            case XSConstants.ELEMENT_DECLARATION: {
                LOG.debug( "Unhandled particle: ELEMENT_DECLARATION" );
                break;
            }
            default: {
                assert false;
            }
            }
            // contents = new Sequence( elements );
            break;
        }
        }
        return null;
    }

    private GeometryType getGeometryType( QName gmlGeometryName ) {
        LOG.debug( "Mapping '" + gmlGeometryName + "'..." );
        return null;
    }

    private PrimitiveType getPrimitiveType( XSSimpleType typeDef ) {
        LOG.debug( "Mapping '" + typeDef.getName() + "'..." );
        return null;
    }
}
