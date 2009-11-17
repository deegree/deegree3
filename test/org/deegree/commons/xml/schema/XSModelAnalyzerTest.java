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
package org.deegree.commons.xml.schema;

import static org.junit.Assert.assertEquals;

import java.util.List;

import javax.xml.namespace.QName;

import org.apache.xerces.xs.XSComplexTypeDefinition;
import org.apache.xerces.xs.XSConstants;
import org.apache.xerces.xs.XSElementDeclaration;
import org.apache.xerces.xs.XSModelGroup;
import org.apache.xerces.xs.XSObjectList;
import org.apache.xerces.xs.XSParticle;
import org.apache.xerces.xs.XSTerm;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for the {@link XSModelAnalyzer}.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
public class XSModelAnalyzerTest {

    private static Logger LOG = LoggerFactory.getLogger( XSModelAnalyzerTest.class );

    /**
     * Check the correct determining of substitutable elements.
     * 
     * @throws ClassCastException
     * @throws ClassNotFoundException
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    @Test
    public void testConcreteFeatureElements()
                            throws ClassCastException, ClassNotFoundException, InstantiationException,
                            IllegalAccessException {

        XSModelAnalyzer analyzer = new XSModelAnalyzer(
                                                        XSModelAnalyzerTest.class.getResource( "Philosopher.xsd" ).toString() );
        QName abstractFeatureElementName = new QName( "http://www.opengis.net/gml", "_Feature" );
        List<XSElementDeclaration> concreteFeatureElements = analyzer.getSubstitutions( abstractFeatureElementName,
                                                                                        null, true, true );
        assertEquals( 5, concreteFeatureElements.size() );
    }

    @Test
    public void testGML311SF()
                            throws ClassCastException, ClassNotFoundException, InstantiationException,
                            IllegalAccessException {

        XSModelAnalyzer analyzer = new XSModelAnalyzer( "http://schemas.opengis.net/gml/2.1.2/geometry.xsd" );
        QName abstractFeatureElementName = new QName( "http://www.opengis.net/gml", "_Geometry" );
        List<XSElementDeclaration> geometryElements = analyzer.getSubstitutions( abstractFeatureElementName, null,
                                                                                 true, false );

        for ( XSElementDeclaration decl : geometryElements ) {

            XSComplexTypeDefinition typeDef = (XSComplexTypeDefinition) decl.getTypeDefinition();
            System.out.println( "element: " + decl.getName() + ", type: " + typeDef.getName() );

            switch ( typeDef.getContentType() ) {
            case XSComplexTypeDefinition.CONTENTTYPE_ELEMENT:
                LOG.debug( "CONTENTTYPE_ELEMENT" );
                XSParticle particle = typeDef.getParticle();
                XSTerm term = particle.getTerm();
                switch ( term.getType() ) {
                case XSConstants.MODEL_GROUP:
                    traverse( (XSModelGroup) term );
                    break;
                }
            }
        }
    }

    private void traverse( XSModelGroup modelGroup ) {

        switch ( modelGroup.getCompositor() ) {
        case XSModelGroup.COMPOSITOR_ALL: {
            LOG.info( "Unhandled model group: COMPOSITOR_ALL" );
            break;
        }
        case XSModelGroup.COMPOSITOR_CHOICE: {
            XSObjectList choice = modelGroup.getParticles();
            for ( int i = 0; i < choice.getLength(); i++ ) {
                XSParticle particle2 = (XSParticle) choice.item( i );
                switch ( particle2.getTerm().getType() ) {
                case XSConstants.ELEMENT_DECLARATION: {
                    XSElementDeclaration elementDecl2 = (XSElementDeclaration) particle2.getTerm();
                    int minOccurs2 = particle2.getMinOccurs();
                    int maxOccurs2 = particle2.getMaxOccursUnbounded() ? -1 : particle2.getMaxOccurs();
                    QName elementName = new QName( elementDecl2.getNamespace(), elementDecl2.getName() );
                    System.out.println( "- property: " + elementName + ", min: " + minOccurs2 + ", max: " + maxOccurs2 );
                    break;
                }
                case XSConstants.WILDCARD: {
                    LOG.info( "Unhandled particle: WILDCARD" );
                    break;
                }
                case XSConstants.MODEL_GROUP: {
                    traverse( (XSModelGroup) particle2.getTerm() );
                    break;
                }
                }
            }
            break;
        }
        case XSModelGroup.COMPOSITOR_SEQUENCE: {
            LOG.debug( "Found sequence." );
            XSObjectList sequence = modelGroup.getParticles();
            for ( int i = 0; i < sequence.getLength(); i++ ) {
                XSParticle particle2 = (XSParticle) sequence.item( i );
                switch ( particle2.getTerm().getType() ) {
                case XSConstants.ELEMENT_DECLARATION: {
                    XSElementDeclaration elementDecl2 = (XSElementDeclaration) particle2.getTerm();
                    int minOccurs2 = particle2.getMinOccurs();
                    int maxOccurs2 = particle2.getMaxOccursUnbounded() ? -1 : particle2.getMaxOccurs();
                    QName elementName = new QName( elementDecl2.getNamespace(), elementDecl2.getName() );
                    System.out.println( "- property: " + elementName + ", min: " + minOccurs2 + ", max: " + maxOccurs2 );
                    break;
                }
                case XSConstants.WILDCARD: {
                    LOG.info( "Unhandled particle: WILDCARD" );
                    break;
                }
                case XSConstants.MODEL_GROUP: {
                    traverse( (XSModelGroup) particle2.getTerm() );
                    break;
                }
                }
            }
        }
        default: {
            assert false;
        }
        }
    }
}
