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
import java.util.List;

import org.deegree.datatypes.QualifiedName;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.framework.xml.XMLTools;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Parser for XML schema documents.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author <a href="mailto:deshmukh@lat-lon.de">Anup Deshmukh </a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class XSDocument extends XMLFragment {

    private static final long serialVersionUID = 4371672452129797159L;

    private URI targetNamespace;

    private final ILogger LOG = LoggerFactory.getLogger( XSDocument.class );

    /**
     * Returns the class representation of the underlying schema document.
     *
     * @return class representation of the underlying schema document
     * @throws XMLParsingException
     * @throws XMLSchemaException
     */
    public XMLSchema parseXMLSchema()
                            throws XMLParsingException, XMLSchemaException {
        SimpleTypeDeclaration[] simpleTypes = extractSimpleTypeDeclarations();
        ComplexTypeDeclaration[] complexTypes = extractComplexTypeDeclarations();
        ElementDeclaration[] elementDeclarations = extractElementDeclarations();
        return new XMLSchema( getTargetNamespace(), simpleTypes, complexTypes, elementDeclarations );
    }

    /**
     * Returns the target namespace of the underlying schema document.
     *
     * @return target namespace of the underlying schema document
     * @throws XMLParsingException
     */
    public synchronized URI getTargetNamespace()
                            throws XMLParsingException {
        if ( this.targetNamespace == null ) {
            this.targetNamespace = XMLTools.getNodeAsURI( this.getRootElement(), "@targetNamespace", nsContext, null );
        }
        return this.targetNamespace;
    }

    /**
     * Extracts all global (top-level) simple type declarations from the underlying schema document.
     *
     * @return all global (top-level) simple type declarations
     * @throws XMLParsingException
     *             if the document is not a valid XML Schema document or does not match the
     *             limitations of this class
     */
    public SimpleTypeDeclaration[] extractSimpleTypeDeclarations()
                            throws XMLParsingException {
        List<Element> simpleTypeElements = XMLTools.getElements( this.getRootElement(), getFullName( "simpleType" ),
                                                                 nsContext );
        LOG.logDebug( "Found " + simpleTypeElements.size() + " simple type declarations." );
        SimpleTypeDeclaration[] simpleTypeDeclarations = new SimpleTypeDeclaration[simpleTypeElements.size()];
        for ( int i = 0; i < simpleTypeDeclarations.length; i++ ) {
            simpleTypeDeclarations[i] = parseSimpleTypeDeclaration( simpleTypeElements.get( i ) );
        }
        return simpleTypeDeclarations;
    }

    /**
     * Extracts all global (top-level) complex type declarations from the underlying schema
     * document.
     *
     * @return all global (top-level) complex type declarations
     * @throws XMLParsingException
     *             if the document is not a valid XML Schema document or does not match the
     *             limitations of this class
     */
    public ComplexTypeDeclaration[] extractComplexTypeDeclarations()
                            throws XMLParsingException {
        List<Element> complexTypeElements = XMLTools.getElements( this.getRootElement(), getFullName( "complexType" ),
                                                                  nsContext );
        LOG.logDebug( "Found " + complexTypeElements.size() + " complex type declarations." );
        ComplexTypeDeclaration[] complexTypeDeclarations = new ComplexTypeDeclaration[complexTypeElements.size()];
        for ( int i = 0; i < complexTypeDeclarations.length; i++ ) {
            complexTypeDeclarations[i] = parseComplexTypeDeclaration( complexTypeElements.get( i ) );
        }
        return complexTypeDeclarations;
    }

    /**
     * Extracts all global (top-level) element declarations from the underlying schema document.
     *
     * @return all global (top-level) element declarations
     * @throws XMLParsingException
     *             if the document is not a valid XML Schema document or does not match the
     *             limitations of this class
     */
    public ElementDeclaration[] extractElementDeclarations()
                            throws XMLParsingException {
        List<Element> complexTypeElements = XMLTools.getElements( this.getRootElement(), getFullName( "element" ),
                                                                  nsContext );
        LOG.logDebug( "Found " + complexTypeElements.size() + " element declarations." );
        ElementDeclaration[] elementDeclarations = new ElementDeclaration[complexTypeElements.size()];
        for ( int i = 0; i < elementDeclarations.length; i++ ) {
            elementDeclarations[i] = parseElementDeclaration( complexTypeElements.get( i ) );
        }
        return elementDeclarations;
    }

    /**
     * Returns the root element of the complex type declaration for the given name.
     *
     * @param name
     *            the name of the complex type declaration to look up (w/o namespace)
     * @return the root element of the complex type declaration or null, if the requested complex
     *         type is not declared
     */
    public Element getComplexTypeDeclaration( String name ) {
        String xPath = getFullName( "complexType[name=\"]" ) + name + "\"]";
        Element element = null;
        try {
            element = (Element) XMLTools.getNode( getRootElement(), xPath, nsContext );
        } catch ( XMLParsingException e ) {
            // happens if requested complex type is not declared
        }
        return element;
    }

    /**
     * Parses the given <code>Element</code> as an 'xs:element' declaration.
     *
     * @param element
     *            'xs:element' declaration to be parsed
     * @return object representation of the declaration
     * @throws XMLParsingException
     *             if the document is not a valid XML Schema document or does not match the
     *             limitations of this class
     */
    protected ElementDeclaration parseElementDeclaration( Element element )
                            throws XMLParsingException {

        QualifiedName name = new QualifiedName( XMLTools.getRequiredNodeAsString( element, "@name", nsContext ),
                                                getTargetNamespace() );

        if ( name.getLocalName().length() == 0 ) {
            String msg = "Error in schema document. Empty name (\"\") in element declaration " + "found.";
            throw new XMLSchemaException( msg );
        }

        LOG.logDebug( "Parsing element declaration '" + name + "'." );

        boolean isAbstract = XMLTools.getNodeAsBoolean( element, "@abstract", nsContext, false );

        TypeReference typeReference = null;
        Node typeNode = XMLTools.getNode( element,
                                          "@type|xs:simpleType/xs:restriction/@base|xs:simpleType/xs:extension/@base",
                                          nsContext );
        if ( typeNode != null ) {
            typeReference = new TypeReference( parseQualifiedName( typeNode ) );
        } else {
            // inline type declaration
            Element elem = (Element) XMLTools.getRequiredNode( element, getFullName( "complexType" ), nsContext );
            TypeDeclaration type = parseComplexTypeDeclaration( elem );
            typeReference = new TypeReference( type );
        }

        int minOccurs = XMLTools.getNodeAsInt( element, "@minOccurs", nsContext, 1 );
        int maxOccurs = -1;
        String maxOccursString = XMLTools.getNodeAsString( element, "@maxOccurs", nsContext, "1" );
        if ( !"unbounded".equals( maxOccursString ) ) {
            try {
                maxOccurs = Integer.parseInt( maxOccursString );
            } catch ( NumberFormatException e ) {
                throw new XMLParsingException( "Invalid value ('" + maxOccursString + "') in 'maxOccurs' attribute. "
                                               + "Must be a valid integer value or 'unbounded'." );
            }
        }

        QualifiedName substitutionGroup = null;
        Node substitutionGroupNode = XMLTools.getNode( element, "@substitutionGroup", nsContext );
        if ( substitutionGroupNode != null ) {
            substitutionGroup = parseQualifiedName( substitutionGroupNode );
        }

        return new ElementDeclaration( name, isAbstract, typeReference, minOccurs, maxOccurs, substitutionGroup );
    }

    /**
     * Parses the given <code>Element</code> as an 'xs:simpleType' declaration.
     * <p>
     * The following limitations apply:
     * <ul>
     * <li>the type must be defined using 'restriction' (of a basic xsd type)</li>
     * <li>the content model (enumeration, ...) is not evaluated</li>
     * </ul>
     * </p>
     *
     * @param element
     *            'xs:simpleType' declaration to be parsed
     * @return object representation of the declaration
     * @throws XMLParsingException
     *             if the document is not a valid XML Schema document or does not match the
     *             limitations of this class
     */
    protected SimpleTypeDeclaration parseSimpleTypeDeclaration( Element element )
                            throws XMLParsingException {

        QualifiedName name = null;
        String localName = XMLTools.getNodeAsString( element, "@name", nsContext, null );
        if ( localName != null ) {
            name = new QualifiedName( localName, getTargetNamespace() );
            if ( localName.length() == 0 ) {
                String msg = "Error in schema document. Empty name (\"\") in simpleType " + "declaration found.";
                throw new XMLSchemaException( msg );
            }
        }

        LOG.logDebug( "Parsing simple type declaration '" + name + "'." );

        Node restrictionBaseNode = XMLTools.getRequiredNode( element, getFullName( "restriction/@base" ), nsContext );
        TypeReference restrictionBase = new TypeReference( parseQualifiedName( restrictionBaseNode ) );

        return new SimpleTypeDeclaration( name, restrictionBase );
    }

    /**
     * Parses the given <code>Element</code> as an 'xs:complexType' declaration.
     *
     * @param element
     *            'xs:complexType' declaration to be parsed
     * @return object representation of the declaration
     * @throws XMLParsingException
     *             if the document is not a valid XML Schema document or does not match the
     *             limitations of this class
     */
    protected ComplexTypeDeclaration parseComplexTypeDeclaration( Element element )
                            throws XMLParsingException {

        QualifiedName name = null;
        String localName = XMLTools.getNodeAsString( element, "@name", nsContext, null );
        if ( localName != null ) {
            name = new QualifiedName( localName, getTargetNamespace() );
            if ( localName.length() == 0 ) {
                String msg = "Error in schema document. Empty name (\"\") for complexType " + "declaration found.";
                throw new XMLSchemaException( msg );
            }
        }
        LOG.logDebug( "Parsing complex type declaration '" + name + "'." );

        List<Element> subElementList = null;
        TypeReference extensionBase = null;
        Node extensionBaseNode = XMLTools.getNode( element, getFullName( "complexContent/" )
                                                            + getFullName( "extension/@base" ), nsContext );
        if ( extensionBaseNode != null ) {
            extensionBase = new TypeReference( parseQualifiedName( extensionBaseNode ) );
            subElementList = XMLTools.getElements( element, getFullName( "complexContent/" )
                                                            + getFullName( "extension/" ) + getFullName( "sequence/" )
                                                            + getFullName( "element" ), nsContext );
        } else {
            subElementList = XMLTools.getRequiredElements( element, getFullName( "sequence/" )
                                                                    + getFullName( "element" ), nsContext );
        }

        ElementDeclaration[] subElements = new ElementDeclaration[subElementList.size()];
        for ( int i = 0; i < subElements.length; i++ ) {
            Element subElement = subElementList.get( i );
            subElements[i] = parseElementDeclaration( subElement );
        }

        return new ComplexTypeDeclaration( name, extensionBase, subElements );
    }

    /**
     * Prepends the prefix of the RootElement to the given local name.
     * <p>
     * If the prefix of the RootElement is empty, "xs:" is prepended.
     *
     * @param localName
     *            to this the prefix will be prepended
     * @return prefix + localName
     */
    protected String getFullName( String localName ) {
        String ret;
        Element root = this.getRootElement();
        String prefix = root.getPrefix();

        if ( prefix != null && prefix.length() > 0 ) {
            URI uri = nsContext.getURI( prefix );
            if ( null == uri ) {
                String nsUri = root.lookupNamespaceURI( prefix );
                try {
                    nsContext.addNamespace( prefix, new URI( nsUri ) ); // synchronized ???
                } catch ( Exception exc ) {
                    LOG.logError( "failed to add namespace: " + nsUri, exc );
                }
            }
            ret = prefix + ':' + localName;
        } else {
            // fallback
            ret = "xs:" + localName;
        }
        return ret;
    }
}
