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

import static org.apache.xerces.xs.XSConstants.DERIVATION_EXTENSION;
import static org.apache.xerces.xs.XSConstants.DERIVATION_LIST;
import static org.apache.xerces.xs.XSConstants.DERIVATION_RESTRICTION;
import static org.apache.xerces.xs.XSConstants.DERIVATION_UNION;
import static org.apache.xerces.xs.XSConstants.TYPE_DEFINITION;
import static org.w3c.dom.DOMError.SEVERITY_ERROR;
import static org.w3c.dom.DOMError.SEVERITY_FATAL_ERROR;
import static org.w3c.dom.DOMError.SEVERITY_WARNING;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;

import org.apache.xerces.impl.xs.XMLSchemaLoader;
import org.apache.xerces.impl.xs.util.StringListImpl;
import org.apache.xerces.xs.XSComplexTypeDefinition;
import org.apache.xerces.xs.XSConstants;
import org.apache.xerces.xs.XSElementDeclaration;
import org.apache.xerces.xs.XSModel;
import org.apache.xerces.xs.XSNamedMap;
import org.apache.xerces.xs.XSNamespaceItemList;
import org.apache.xerces.xs.XSTypeDefinition;
import org.deegree.commons.xml.XMLProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.DOMError;
import org.w3c.dom.DOMErrorHandler;

/**
 * Provides convenient methods to retrieve "relevant" element and type declarations of an XML schema infoset.
 * <p>
 * This functionality is very handy for extracting higher-level structures defined using XML schema, such as GML feature
 * types.
 * </p>
 * 
 * @see org.deegree.gml.schema.GMLSchemaAnalyzer
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
public class XSModelAnalyzer {

    private static final Logger LOG = LoggerFactory.getLogger( XSModelAnalyzer.class );

    /** The XML schema infoset. */
    protected final XSModel xmlSchema;

    /**
     * Creates a new <code>XSModelAnalyzer</code> for the given (Xerces) XML schema infoset.
     * 
     * @param xmlSchema
     *            schema infoset, must not be <code>null</code>
     */
    public XSModelAnalyzer( XSModel xmlSchema ) {
        this.xmlSchema = xmlSchema;
    }

    /**
     * Creates a new {@link XSModelAnalyzer} instance that reads the schema documents from the given URLs.
     * 
     * @param schemaUrls
     *            locations of the schema documents, must not be <code>null</code> and contain at least one entry
     * @throws ClassCastException
     * @throws ClassNotFoundException
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    public XSModelAnalyzer( String... schemaUrls ) throws ClassCastException, ClassNotFoundException,
                            InstantiationException, IllegalAccessException {
        xmlSchema = loadModel( schemaUrls );
    }

    public XSNamespaceItemList getNamespaces() {
        return xmlSchema.getNamespaceItems();
    }

    /**
     * Returns the XML schema infoset (represented as a Xerces {@link XSModel}).
     * 
     * @return the XML schema infoset
     */
    public XSModel getXSModel() {
        return xmlSchema;
    }

    /**
     * Returns the declarations of all elements that are substitutable for a given element declaration.
     * 
     * @param elementDecl
     *            element declaration, must not be <code>null</code>
     * @param namespace
     *            only element declarations in this namespace are returned, set to <code>null</code> for all namespaces
     * @param transitive
     *            if true, also substitutions for substitutions (and so on) are included
     * @param onlyConcrete
     *            if true, only concrete (non-abstract) declarations are returned
     * @return the declarations of all substitution elements in the requested namespace
     */
    public List<XSElementDeclaration> getSubstitutions( XSElementDeclaration elementDecl, String namespace,
                                                        boolean transitive, boolean onlyConcrete ) {

        // NOTE: XSModel#getSubstitutionGroup() would be much easier, but doesn't seem to work correctly for XSModels
        // that have been loaded from multiple files which have overlapping includes

        // first collect all element names, because XSModels seem to contain multiple XSElementDeclaration
        // elements for the same name (when multiple schema files are involved)
        Set<QName> elementNames = new HashSet<QName>();
        XSNamedMap elementDecls = xmlSchema.getComponents( XSConstants.ELEMENT_DECLARATION );
        for ( int i = 0; i < elementDecls.getLength(); i++ ) {
            XSElementDeclaration candidate = (XSElementDeclaration) elementDecls.item( i );
            if ( namespace == null || namespace.equals( candidate.getNamespace() ) ) {
                if ( !onlyConcrete || !candidate.getAbstract() ) {
                    if ( transitive ) {
                        if ( candidate.getNamespace().equals( elementDecl.getNamespace() )
                             && candidate.getName().equals( elementDecl.getName() ) ) {
                            elementNames.add( new QName( candidate.getNamespace(), candidate.getName() ) );
                            continue;
                        }
                    }
                    XSElementDeclaration substitutionGroup = candidate.getSubstitutionGroupAffiliation();
                    while ( substitutionGroup != null ) {
                        if ( substitutionGroup.getNamespace().equals( elementDecl.getNamespace() )
                             && substitutionGroup.getName().equals( elementDecl.getName() ) ) {
                            elementNames.add( new QName( candidate.getNamespace(), candidate.getName() ) );
                            break;
                        }
                        if ( transitive ) {
                            substitutionGroup = substitutionGroup.getSubstitutionGroupAffiliation();
                        } else {
                            substitutionGroup = null;
                        }
                    }
                }
            }
        }

        List<XSElementDeclaration> substDecls = new ArrayList<XSElementDeclaration>( elementNames.size() );
        for ( QName name : elementNames ) {
            substDecls.add( xmlSchema.getElementDeclaration( name.getLocalPart(), name.getNamespaceURI() ) );
        }

        return substDecls;
    }

    /**
     * Returns the subtypes for the given type definition.
     * 
     * @param typeDef
     *            type definition, must not be <code>null</code>
     * @param namespace
     *            only type definitions in this namespace are returned, set to <code>null</code> for all namespaces
     * @param transitive
     *            if true, also subtypes of subtypes (and so on) are included
     * @param onlyConcrete
     *            if true, only concrete (non-abstract) definitions are returned
     * @return the definition of all sub type definitions in the requested namespace
     */
    public List<XSTypeDefinition> getSubtypes( XSTypeDefinition typeDef, String namespace, boolean transitive,
                                               boolean onlyConcrete ) {
        Set<QName> typeNames = new HashSet<QName>();
        XSNamedMap typeDefs = xmlSchema.getComponents( TYPE_DEFINITION );
        for ( int i = 0; i < typeDefs.getLength(); i++ ) {
            XSTypeDefinition candidate = (XSTypeDefinition) typeDefs.item( i );
            if ( namespace == null || namespace.equals( candidate.getNamespace() ) ) {
                boolean isAbstract = false;
                if ( candidate instanceof XSComplexTypeDefinition ) {
                    isAbstract = ( (XSComplexTypeDefinition) candidate ).getAbstract();
                }
                if ( !onlyConcrete || !isAbstract ) {
                    if ( transitive ) {
                        if ( candidate.derivedFromType( typeDef,
                                                        (short) ( DERIVATION_RESTRICTION | DERIVATION_EXTENSION
                                                                  | DERIVATION_UNION | DERIVATION_LIST ) ) ) {
                            typeNames.add( new QName( candidate.getNamespace(), candidate.getName() ) );
                        }
                    } else {
                        XSTypeDefinition candidateBaseType = candidate.getBaseType();
                        if ( typeDef.getName().equals( candidateBaseType.getName() )
                             && typeDef.getNamespace().equals( candidateBaseType.getNamespace() ) ) {
                            typeNames.add( new QName( candidate.getNamespace(), candidate.getName() ) );
                        }
                    }
                }
            }
        }

        List<XSTypeDefinition> subTypes = new ArrayList<XSTypeDefinition>( typeNames.size() );
        for ( QName name : typeNames ) {
            subTypes.add( xmlSchema.getTypeDefinition( name.getLocalPart(), name.getNamespaceURI() ) );
        }
        return subTypes;
    }

    /**
     * Returns the declarations of all elements that are substitutable for a given element name.
     * 
     * @param elementName
     *            qualified name of the element, must not be <code>null</code>
     * @param namespace
     *            only element declarations in this namespace are returned, set to <code>null</code> for all namespaces
     * @param transitive
     *            if true, also substitutions for substitutions (and so one) are included
     * @param onlyConcrete
     *            if true, only concrete (non-abstract) declarations are returned
     * @return the declarations of all substitution elements in the requested namespace
     */
    public List<XSElementDeclaration> getSubstitutions( QName elementName, String namespace, boolean transitive,
                                                        boolean onlyConcrete ) {
        XSElementDeclaration elementDecl = xmlSchema.getElementDeclaration( elementName.getLocalPart(),
                                                                            elementName.getNamespaceURI() );
        if ( elementDecl == null ) {
            String msg = "The schema does not declare a top-level element with name '" + elementName + "'.";
            throw new IllegalArgumentException( msg );
        }
        return getSubstitutions( elementDecl, namespace, transitive, onlyConcrete );
    }

    /**
     * Creates a Xerces {@link XSModel} from the schemas at the given URLs, using the {@link RedirectingEntityResolver},
     * so OGC schemas URLs are redirected to a local copy.
     * 
     * @param schemaUrls
     *            locations of the schema documents, must not be <code>null</code> and contain at least one entry
     * @return the XML schema infoset, never <code>null</code>
     * @throws ClassCastException
     * @throws ClassNotFoundException
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    public static XSModel loadModel( String... schemaUrls )
                            throws ClassCastException, ClassNotFoundException, InstantiationException,
                            IllegalAccessException {

        XMLSchemaLoader schemaLoader = new XMLSchemaLoader();
        DOMConfiguration config = schemaLoader.getConfig();
        ErrorHandler errorHandler = new ErrorHandler();

        config.setParameter( "error-handler", errorHandler );
        config.setParameter( "validate", Boolean.TRUE );

        RedirectingEntityResolver resolver = new RedirectingEntityResolver();
        for ( int i = 0; i < schemaUrls.length; i++ ) {
            schemaUrls[i] = resolver.redirect( schemaUrls[i] );
        }
        schemaLoader.setEntityResolver( resolver );

        // // TODO what about preparsing of GML schemas?
        // try {
        // schemaLoader.setProperty( XMLGRAMMAR_POOL, GrammarPoolManager.getGrammarPool(
        // "http://schemas.opengis.net/gml/3.1.1/base/gml.xsd" ) );
        // } catch ( XMLConfigurationException e ) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // } catch ( XNIException e ) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // } catch ( IOException e ) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }

        XSModel model = schemaLoader.loadURIList( new StringListImpl( schemaUrls, schemaUrls.length ) );
        if ( !errorHandler.getErrors().isEmpty() ) {
            throw new XMLProcessingException( errorHandler.getErrors().get( 0 ) );
        }
        for ( String warning : errorHandler.getWarnings() ) {
            LOG.debug( warning );
        }
        return model;
    }
}

class ErrorHandler implements DOMErrorHandler {

    private List<String> warnings = new ArrayList<String>();

    private List<String> errors = new ArrayList<String>();

    List<String> getWarnings() {
        return warnings;
    }

    List<String> getErrors() {
        return errors;
    }

    public boolean handleError( DOMError domError ) {
        switch ( domError.getSeverity() ) {
        case SEVERITY_WARNING: {
            String msg = "Problem in schema document (systemId: " + domError.getLocation().getUri() + ", line: "
                         + domError.getLocation().getLineNumber() + ", column: "
                         + domError.getLocation().getColumnNumber() + ") " + domError.getMessage();
            warnings.add( msg );
            break;
        }
        case SEVERITY_ERROR:
        case SEVERITY_FATAL_ERROR: {
            String msg = "Severe error in schema document (systemId: " + domError.getLocation().getUri() + ", line: "
                         + domError.getLocation().getLineNumber() + ", column: "
                         + domError.getLocation().getColumnNumber() + ") " + domError.getMessage();
            errors.add( msg );
        }
        }
        return true;
    }
}