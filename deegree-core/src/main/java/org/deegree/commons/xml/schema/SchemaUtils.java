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

import static javax.xml.XMLConstants.DEFAULT_NS_PREFIX;
import static org.apache.xerces.xs.XSComplexTypeDefinition.CONTENTTYPE_ELEMENT;
import static org.apache.xerces.xs.XSComplexTypeDefinition.CONTENTTYPE_EMPTY;
import static org.deegree.commons.xml.CommonNamespaces.XSNS;

import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.xerces.xs.XSAttributeDeclaration;
import org.apache.xerces.xs.XSAttributeUse;
import org.apache.xerces.xs.XSComplexTypeDefinition;
import org.apache.xerces.xs.XSElementDeclaration;
import org.apache.xerces.xs.XSModelGroup;
import org.apache.xerces.xs.XSObjectList;
import org.apache.xerces.xs.XSParticle;
import org.apache.xerces.xs.XSTerm;
import org.apache.xerces.xs.XSTypeDefinition;
import org.apache.xerces.xs.XSWildcard;
import org.deegree.commons.utils.Pair;

/**
 * The <code></code> class TODO add class documentation here.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class SchemaUtils {

    /**
     * Writes a wrapper schema document for the given namespace imports.
     * 
     * @param writer
     *            xml stream to write to, must not be <code>null</code>
     * @param targetNamespace
     *            target namespace of the document, must not be <code>null</code>
     * @param nsImports
     *            namespace imports, must not be <code>null</code>
     * @throws XMLStreamException
     */
    public static void writeWrapperDoc( XMLStreamWriter writer, String targetNamespace,
                                        List<Pair<String, String>> nsImports )
                            throws XMLStreamException {

        writer.setPrefix( DEFAULT_NS_PREFIX, XSNS );
        writer.writeStartElement( "schema" );
        writer.writeNamespace( DEFAULT_NS_PREFIX, XSNS );
        writer.writeAttribute( "attributeFormDefault", "unqualified" );
        writer.writeAttribute( "elementFormDefault", "qualified" );
        writer.writeAttribute( "targetNamespace", targetNamespace );

        for ( Pair<String, String> nsImport : nsImports ) {
            if ( nsImport.first.equals( targetNamespace ) ) {
                writer.writeEmptyElement( "include" );
            } else {
                writer.writeEmptyElement( "import" );
                writer.writeAttribute( "namespace", nsImport.first );
            }
            writer.writeAttribute( "schemaLocation", nsImport.second );
        }

        // end 'xs:schema'
        writer.writeEndElement();
    }

    public static List<String> getPaths( XSComplexTypeDefinition typeDef ) {
        List<String> paths = new ArrayList<String>();
        addPaths( typeDef, paths, "" );
        return paths;
    }

    private static void addPaths( XSComplexTypeDefinition typeDef, List<String> paths, String base ) {

        // attributes
        XSObjectList attributeUses = typeDef.getAttributeUses();
        for ( int i = 0; i < attributeUses.getLength(); i++ ) {
            XSAttributeDeclaration attributeDecl = ( (XSAttributeUse) attributeUses.item( i ) ).getAttrDeclaration();
            if ( attributeDecl.getNamespace() == null ) {
                paths.add( base + "/@" + attributeDecl.getName() );
            } else {
                paths.add( base + "/@{" + attributeDecl.getNamespace() + "}" + attributeDecl.getName() );
            }
        }

        // text node
        if ( typeDef.getContentType() != CONTENTTYPE_EMPTY && typeDef.getContentType() != CONTENTTYPE_ELEMENT ) {
            paths.add( base + "/text()" );
        }

        // elements
        XSParticle particle = typeDef.getParticle();
        if ( particle != null ) {
            addPaths( particle, paths, base );
        }
    }

    private static void addPaths( XSParticle particle, List<String> paths, String base ) {
        if ( particle.getMaxOccursUnbounded() ) {
            addPaths( particle.getTerm(), paths, base + "[*]" );
        } else {
            for ( int i = 1; i <= particle.getMaxOccurs(); i++ ) {
                addPaths( particle.getTerm(), paths, base + "[" + i + "]" );
            }
        }
    }

    private static void addPaths( XSTerm term, List<String> paths, String base ) {
        if ( term instanceof XSElementDeclaration ) {
            addPaths( (XSElementDeclaration) term, paths, base );
        } else if ( term instanceof XSModelGroup ) {
            addPaths( (XSModelGroup) term, paths, base );
        } else {
            addPaths( (XSWildcard) term, paths, base );
        }
    }

    private static void addPaths( XSElementDeclaration elementDecl, List<String> paths, String base ) {

        // TODO substitutions
        if ( elementDecl.getNamespace() == null ) {
            base += "/" + elementDecl.getName();
        } else {
            base += "/{" + elementDecl.getNamespace() + "}" + elementDecl.getName();
        }

        XSTypeDefinition typeDef = elementDecl.getTypeDefinition();
        if ( typeDef instanceof XSComplexTypeDefinition ) {
            addPaths( (XSComplexTypeDefinition) typeDef, paths, base );
        } else {
            paths.add( base + "/text()" );
        }
    }

    private static void addPaths( XSModelGroup modelGroup, List<String> paths, String base ) {
        base += "compositor_" + modelGroup.getCompositor();
        XSObjectList particles = modelGroup.getParticles();
        for ( int i = 0; i < particles.getLength(); i++ ) {
            XSParticle particle = (XSParticle) particles.item( i );
            addPaths( particle, paths, base );
        }
    }

    private static void addPaths( XSWildcard wildCard, List<String> paths, String base ) {
        // TODO
    }
}