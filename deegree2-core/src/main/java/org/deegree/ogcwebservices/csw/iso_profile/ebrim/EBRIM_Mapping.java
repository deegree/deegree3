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
package org.deegree.ogcwebservices.csw.iso_profile.ebrim;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Queue;

import org.deegree.datatypes.QualifiedName;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.StringTools;
import org.deegree.framework.xml.NamespaceContext;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.framework.xml.XMLTools;
import org.deegree.ogcbase.CommonNamespaces;
import org.deegree.ogcwebservices.OGCWebServiceException;
import org.deegree.ogcwebservices.csw.discovery.GetRecordsDocument;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Maps valid ebrim propertypaths including variables (aka aliases) e.g. $o1 to the according wfs-propertyPaths.
 *
 * @version $Revision$
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 *
 * @version 1.0. $Revision$, $Date$
 *
 * @since 2.0
 */
public class EBRIM_Mapping {
    private final static ILogger LOG = LoggerFactory.getLogger( EBRIM_Mapping.class );

    private final Properties mapping = new Properties();

    private final static NamespaceContext nsContext = CommonNamespaces.getNamespaceContext();

    private final static String rimNS = CommonNamespaces.OASIS_EBRIMNS.toString();

    private final static String wrsNS = CommonNamespaces.WRS_EBRIMNS.toString();

    private Map<String, QualifiedName> variables;

    // Used to do some parsing of QNames and variables.
    private GetRecordsDocument gd;

    // the root document node to handle the prefixes.
    private Node rootNode = null;

    // /**
    // * A very important method, it allows the retrieval of the original dom tree from the xslt-processor, which in
    // it's
    // * turn can be used to find the prefix mappings used in the document.
    // *
    // * @param context
    // * (org.apache.xalan.extensions.XSLProcessorContext) the xslt Context, which will be given by the xalan
    // * processor
    // * @param extElem
    // * (org.apache.xalan.templates.ElemExtensionCall) a class encapsulating some calling parameters from the
    // * xslt script, it is not used in this class.
    // */
    // public void init( Object context, @SuppressWarnings("unused")
    // Object extElem ) {
    // // public void init(org.apache.xalan.extensions.XSLProcessorContext context, @SuppressWarnings("unused")
    // // org.apache.xalan.templates.ElemExtensionCall extElem ) {
    // LOG.logWarning( "The EBRIM_Mapping should use the xalan api, but currently not supported" );
    // // rootNode = context.getSourceTree();
    // if ( LOG.getLevel() == ILogger.LOG_DEBUG ) {
    // LOG.logDebug( " in init, a test to find the 'rim' prefix" );
    // URI ns = null;
    // try {
    // ns = XMLTools.getNamespaceForPrefix( "rim", rootNode );
    // LOG.logDebug( " The sourcetree we got from the XSLProcessorContext was the following:" );
    // LOG.logDebug( "-----------------------------------------------------" );
    // XMLFragment frag = new XMLFragment( (Element) rootNode );
    // frag.prettyPrint( System.out );
    // LOG.logDebug( "-----------------------------------------------------" );
    // } catch ( URISyntaxException e ) {
    // LOG.logError( "CSW (ebRIM) EBRIM_Mapping: Couldn't get a namespace for prefix rim because: ", e );
    // } catch ( TransformerException e ) {
    // LOG.logError( "CSW (ebRIM) EBRIM_Mapping: Couldn't output the sourcetree because: ", e );
    // }
    // if ( ns != null ) {
    // LOG.logDebug( " For the 'rim:' prefix we found following ns: " + ns.toASCIIString() );
    // } else {
    // LOG.logError( "CSW (ebRIM) EBRIM_Mapping: No namespace found for 'rim:' prefix!" );
    // }
    // }
    // }

    /**
     * The constructor parses the configuration file 'adv_catalog.properties' containing the propertypath mappings from
     * ebrim to gml.
     *
     */
    public EBRIM_Mapping() {
        try {
            InputStream is = EBRIM_Mapping.class.getResourceAsStream( "ebrim_catalog.properties" );
            BufferedReader br = new BufferedReader( new InputStreamReader( is ) );
            String line = null;
            int lineCount = 1;
            while ( ( line = br.readLine() ) != null ) {
                line = line.trim();
                if ( !( line.startsWith( "#" ) || "".equals( line ) ) ) {
                    String[] tmp = StringTools.toArray( line, "=", false );
                    if ( tmp == null ) {
                        LOG.logError( lineCount + ") Found an error, please correct following line: " + line );
                    } else {
                        if ( tmp.length < 2 ) {
                            if ( tmp.length > 0 ) {
                                LOG.logError( lineCount + ") No value applied for key: " + tmp[0]
                                              + ". Key-value pairs must be seperated by an '='sign." );
                            } else {
                                LOG.logError( lineCount + ") The : " + tmp[0]
                                              + ". Key-value pairs must be seperated by an '='sign." );
                            }
                        } else if ( tmp.length > 2 ) {
                            LOG.logError( lineCount
                                          + ") Only one seperator '=' is allowed in a key-value pair, please correct following line: "
                                          + line );
                        } else {
                            mapping.put( tmp[0], tmp[1] );
                        }
                    }
                }
                lineCount++;
            }

        } catch ( IOException e ) {
            LOG.logError(
                          "CSW (ebRIM) EBRIM_Mapping: An error occurred while trying to parse the 'adv_catalog.properties' file.",
                          e );
            e.printStackTrace();
        }
        LOG.logDebug( " The Ebrim-Mapper found following Mappings:\n" + mapping );
        variables = new HashMap<String, QualifiedName>();
        gd = new GetRecordsDocument();
    }

    /**
     * maps a property name of GetRecords, Delete and Update request from the catalogue schema to the underlying WFS
     * schema
     *
     * @param node
     *            containing the propertyPath
     * @return the mapped propertypath
     * @throws XMLParsingException
     */
    public String mapPropertyPath( Node node )
                            throws XMLParsingException {

        String propertyNode = XMLTools.getNodeAsString( node, ".", nsContext, null );
        propertyNode = stripRoot( propertyNode );
        LOG.logDebug( "The supplied xml-Node results into following (normalized) propertypath: " + propertyNode );
        LOG.logDebug( "EBRIMG_Mapping#mapPropertyPath: We have got the variables: " + variables.toString() );

        // Setting the node which will be used to find the prefixes.
        Node prefixResolverNode = getPrefixResolverNode( node );

        String[] props = propertyNode.split( "/" );
        StringBuffer result = new StringBuffer( "/" );
        int count = 0;
        String previousMapping = null;
        for ( String propertyName : props ) {
            LOG.logDebug( "Trying to map propertyName: " + propertyName );
            if ( propertyName != null ) {
                // check if the propertyname references a variable.
                String tmpVarReference = null;
                String mappedName = null;
                if ( propertyName.startsWith( "$" ) ) {
                    tmpVarReference = propertyName;
                    if ( !variables.containsKey( propertyName.substring( 1 ) )
                         || variables.get( propertyName.substring( 1 ) ) == null ) {

                        LOG.logError( "CSW (ebRIM) EBRIM_Mapping: The referenced Variable '" + propertyName
                                      + "' in the propertyNode: '" + propertyNode
                                      + "' has not been declared this can't be!" );
                        LOG.logError( "CSW (ebRIM) EBRIM_Mapping: We have got the variables: " + variables.toString() );
                    } else {
                        // get the rim/wrs object to which this variable is bound to.
                        propertyName = createMapStringForQName( variables.get( propertyName.substring( 1 ) ) );
                        LOG.logDebug( " trying to find mapping for a property: " + propertyName
                                      + ", with found variable: " + tmpVarReference );
                        mappedName = mapping.getProperty( propertyName );
                    }
                } else if ( propertyName.startsWith( "@" ) ) {
                    if ( previousMapping == null ) {
                        LOG.logError( "CSW (ebRIM) EBRIM_Mapping: a propertyName may not start with the @ sign, trying to find a mapping without the @ for: "
                                      + propertyNode );
                        mappedName = mapping.getProperty( propertyName.substring( 1 ) );
                    } else {
                        // first check the RegistryObject attributes
                        // @id=app:iduri
                        // @home=app:home
                        // @lid=app:liduri
                        // @objectType=app:objectType
                        // @status=app:status
                        LOG.logDebug( " trying to find mapping for attribute: " + propertyName );
                        mappedName = mapping.getProperty( propertyName );
                        if ( mappedName == null || "".equals( mappedName ) ) {
                            LOG.logDebug( " the single attribute had no result, trying to find mapping with the dereferenced previous property @property: "
                                          + ( previousMapping + "/" + propertyName ) );
                            mappedName = mapping.getProperty( previousMapping + "/" + propertyName );
                        }
                    }
                } else {
                    LOG.logDebug( " Trying to find mapping for simple propertyName: " + propertyName );
                    QualifiedName qName = null;
                    try {
                        qName = gd.parseQNameFromString( propertyName, prefixResolverNode, true );
                    } catch ( URISyntaxException e ) {
                        LOG.logError( "CSW (ebRIM) EBRIM_Mapping: Could not create QualifiedName from propertyName: "
                                      + propertyName + ", creating simple QualifiedName.", e );
                        qName = new QualifiedName( propertyName );
                    }
                    propertyName = createMapStringForQName( qName );
                    LOG.logDebug( " trying to find mapping for a property: " + propertyName );
                    /**
                     * This hack is needed, because both person and organization use the following features,
                     * telephoneNumber@*, EmailAddress/@* and a link to Address.
                     */
                    if ( propertyName.endsWith( "TelephoneNumber" ) || propertyName.endsWith( "Address" ) ) {
                        propertyName = previousMapping + "/" + propertyName;
                    }
                    mappedName = mapping.getProperty( propertyName );
                }
                if ( mappedName == null ) {
                    LOG.logDebug( "The propertyName: " + propertyName
                                  + " could not be mapped, maybe a Valuelist or AnyValue?" );
                    // Very dirty trick to map
                    // app:slots/app:Slot/app:values/app:SlotValues/app:geometry
                    // and app:slots/app:Slot/app:values/app:SlotValues/stringValue.
                    // from incoming rim:ValueList/rim:Value
                    // as well as the wrs:ValueList/wrs:AnyValue (defined in pkg-basic.xsd)
                    if ( ( "rim:Value".equals( propertyName ) && ( "rim:ValueList".equals( previousMapping ) || "wrs:ValueList".equals( previousMapping ) ) )
                         || ( ( "wrs:AnyValue".equals( propertyName ) || "rim:AnyValue".equals( propertyName ) ) && ( "wrs:ValueList".equals( previousMapping ) || "rim:ValueList".equals( previousMapping ) ) ) ) {
                        if ( ( count + 1 ) != props.length ) {
                            LOG.logError( "CSW (ebRIM) EBRIM_Mapping: More properties will follow this one, this is strange, because a rim:Value can only be located at the end of a propertyPath" );
                        }
                        Node parentNode = XMLTools.getNode( node, "..", nsContext );
                        String parent = parentNode.getLocalName();

                        LOG.logDebug( " It seems we want to map the rim:Slot/rim:ValueList/rim:Value or rim:Slot/wrs:ValueList/wrs:AnyValue, the value of the parent of this propertyName is: "
                                      + parent );
                        if ( parent != null ) {
                            if ( parent.startsWith( "Property" ) ) {// hurray a property therefore a
                                // stringValue
                                LOG.logDebug( parent + " starts with 'Property' we therefore map to app:stringValue." );
                                mappedName = "app:stringValue";
                            } else {
                                LOG.logDebug( parent
                                              + " doesn't start with 'Property' we therefore map to app:geometry." );
                                mappedName = "app:geometry";
                            }
                        }
                    } else {
                        LOG.logInfo( "CSW (ebRIM) EBRIM_Mapping: found no mapping for: " + propertyName );
                        mappedName = "";
                    }
                }
                if ( !"".equals( mappedName ) ) {
                    // save the found Mapping, it might be needed to map the following propertyName
                    // if it is a property (starting with @)
                    previousMapping = propertyName;
                    result.append( addWFSPropertyPath( mappedName, tmpVarReference, ( count == 0 ) ) );
                    if ( ( count + 1 ) != props.length ) {
                        result.append( "/" );
                    }
                }
                // next property/type
                count++;
            }
        }

        return result.toString();
    }

    /**
     * maps the property names of given typenames in the typeNames attribute of a GetRecords request to the catalogue
     * schema of the underlying WFS schema
     *
     * @param node
     *            the GetRecords request Node
     * @return the mapped propertypath
     * @throws XMLParsingException
     */
    public String mapTypeNames( Node node )
                            throws XMLParsingException {
        String typeNamesAttribute = XMLTools.getNodeAsString( node, ".", nsContext, null );
        Map<String, QualifiedName> tmpVariables = new HashMap<String, QualifiedName>();
        // Setting the node which will be used to find the prefixes.
        Node prefixResolverNode = getPrefixResolverNode( node );

        List<QualifiedName> typeNames = parseTypeList( prefixResolverNode, typeNamesAttribute, tmpVariables );
        LOG.logDebug( " found following qNames of the typeNames: " + typeNames );
        // adding new aliases to the query/@typenames
        String newAliasPreFix = "kQhtYHHp_";
        StringBuffer queryTypeNameAttrSB = new StringBuffer();
        if ( tmpVariables.size() == 0 ) {
            for ( int i = 0; i < typeNames.size(); ++i ) {
                String aliasPrefix = newAliasPreFix + i;
                tmpVariables.put( newAliasPreFix + i, typeNames.get( i ) );
                queryTypeNameAttrSB.append( "rim:" + typeNames.get( i ).getLocalName() ).append( "=" ).append(
                                                                                                               aliasPrefix );
                if ( ( i + 1 ) < typeNames.size() ) {
                    queryTypeNameAttrSB.append( " " );
                }
            }
        }

        StringBuffer resultString = new StringBuffer();
        int qNameCounter = 0;
        // Because we need to reuse the member variables map, we've made a local copy of it.
        variables.putAll( tmpVariables );
        LOG.logDebug( " EBRIM_Mapping#mapTypeNames: We have got the variables: " + variables.toString() );
        List<String> varDefs = new ArrayList<String>();
        for ( QualifiedName qName : typeNames ) {
            URI ns = qName.getNamespace();
            String prefix = qName.getPrefix();
            // for debugging purposes
            if ( prefix == null ) {
                prefix = "";
            } else {
                prefix += ":";
            }
            if ( ns == null || rimNS.equals( ns.toString() ) ) {
                LOG.logDebug( " We found the following namespace for the ElementSetName/@typeName: " + ns
                              + " so we map to the prefix rim." );
                prefix = "rim:";
            }
            String result = mapping.getProperty( prefix + qName.getLocalName() );
            LOG.logDebug( " for the FeatureType: " + prefix + qName.getLocalName() + " we found following mapping: "
                          + result );
            if ( result != null ) {
                resultString.append( result );
                // get the mapped variable for this qName
                String var = getVariableForQName( qName, tmpVariables );
                if ( var != null ) {
                    // resultString.append( "=" ).append( var );
                    varDefs.add( var );
                }
            } else {
                LOG.logInfo( "CSW (ebRIM) EBRIM_Mapping: Found no mapping for: " + prefix + qName.getLocalName()
                             + ", so ignoring it." );
            }
            if ( ++qNameCounter < typeNames.size() ) {
                resultString.append( " " );
            }
        }
        // append the aliases= variables to the resultstring
        if ( varDefs.size() != 0 ) {
            LOG.logDebug( " The defined variables list is not empty, we therefore append the alias keyword to the typeName" );
            resultString.append( " aliases=" );
            for ( int i = 0; i < varDefs.size(); ++i ) {
                resultString.append( varDefs.get( i ) );
                if ( ( i + 1 ) < varDefs.size() ) {
                    resultString.append( " " );
                }
            }
        }

        return resultString.toString();
    }

    /**
     * This method take an elementSetName node and conerts it's content into a list of wfs:PropertyName nodes. Depending
     * on the (String) value of the elementSetNameNode (brief, summary, full) the propertyNames will have different
     * values. For this method to work a temporal document is builded from which one element is created, this element
     * will hold all child <wfs:PropertyName> elements.
     *
     * @param elementSetNameNode
     *            the ElementSetName Node of the incoming GetRecords request.
     * @return a Nodelist containing <wfs:PropertyName> nodes.
     */
    public NodeList mapElementSetName( Node elementSetNameNode ) {

        // creating an empty document, so we can append nodes to it, which will be returned as a
        // Nodelist to the xslt script.
        Document doc = XMLTools.create();
        Element resultElement = doc.createElement( "wfs:result" );
        Node newQueryNode = doc.importNode( elementSetNameNode.getParentNode(), true );
        try {
            String elementSetName = XMLTools.getNodeAsString( elementSetNameNode, ".", nsContext, null );
            LOG.logDebug( " Found following elementSetName: " + elementSetName );
            if ( elementSetName != null ) {
                // Setting the node which will be used to find the prefixes.
                Node prefixResolverNode = getPrefixResolverNode( newQueryNode );
                String typeNamesAttribute = XMLTools.getNodeAsString( elementSetNameNode, "@typeNames", nsContext, null );
                // Element queryElement = (Element) newElementSetNameNode.getParentNode();
                Element queryElement = (Element) newQueryNode;
                String queryTypeNamesAttribute = XMLTools.getNodeAsString( queryElement, "@typeNames", nsContext, null );
                if ( queryTypeNamesAttribute == null ) {
                    LOG.logError( "CSW (ebRIM) EBRIM_Mapping: no typeNames attribute found in the csw:Query element, this may not be!!!" );
                }

                // First find the variables in the csw:Query/@typeNames.
                Map<String, QualifiedName> varsInQuery = new HashMap<String, QualifiedName>();
                varsInQuery.putAll( variables );
                // List<QualifiedName> queryTypeNames =
                // parseTypeList( prefixResolverNode, queryTypeNamesAttribute, varsInQuery );
                if ( varsInQuery.size() == 0 ) {
                    LOG.logError( "CSW (ebRIM) EBRIM_Mapping: We found no variables in the query, something is terribly wrong" );
                }
                // StringBuffer queryTypeNameAttrSB = new StringBuffer( queryTypeNamesAttribute );
                // boolean resetQueryTypeNames = false;

                if ( typeNamesAttribute == null ) {
                    // if ( varsInQuery.size() == 0 ) {
                    // resetQueryTypeNames = true;
                    // }
                    LOG.logDebug( " no typeNames attribute found in the csw:ElementSetName node, therefore taking the typeNames of the query node." );
                    typeNamesAttribute = queryTypeNamesAttribute;
                }
                // if ( varsInQuery.size() == 0 && !typeNamesAttribute.equals( queryTypeNamesAttribute ) ) {
                // // adding new aliases to the query/@typenames
                // String newAliasPreFix = "kQhtYHHp_";
                // queryTypeNameAttrSB = new StringBuffer();
                // for ( int i = 0; i < queryTypeNames.size(); ++i ) {
                // String aliasPrefix = newAliasPreFix + i;
                // varsInQuery.put( newAliasPreFix + i, queryTypeNames.get( i ) );
                // queryTypeNameAttrSB.append( "rim:" + queryTypeNames.get( i ).getLocalName() ).append( "=" ).append(
                // aliasPrefix );
                // if ( ( i + 1 ) < queryTypeNames.size() ) {
                // queryTypeNameAttrSB.append( " " );
                // }
                // }
                // queryElement.setAttribute( "typeNames", queryTypeNameAttrSB.toString() );
                // }

                if ( typeNamesAttribute != null ) {

                    Map<String, QualifiedName> vars = new HashMap<String, QualifiedName>();
                    List<QualifiedName> typeNames = parseTypeList( prefixResolverNode, typeNamesAttribute, vars );
                    if ( vars.size() != 0 && !typeNamesAttribute.equals( queryTypeNamesAttribute ) ) {
                        LOG.logError( "CSW (ebRIM) EBRIM_Mapping: Found variables (aliases) in the ElementSetName/@typeNames attribute this is not allowed, we will not process them." );
                    }
                    LOG.logDebug( " Parent of the elementSetName has a local name (should be query): "
                                  + queryElement.getLocalName() );
                    Element constraint = XMLTools.getElement( queryElement, "csw:Constraint", nsContext );
                    if ( constraint == null ) {
                        LOG.logDebug( " No contraint node found, therefore creating one." );
                        Element a = queryElement.getOwnerDocument().createElementNS(
                                                                                     CommonNamespaces.CSWNS.toASCIIString(),
                                                                                     "csw:Constraint" );
                        queryElement.getOwnerDocument().importNode( a, false );
                        constraint = (Element) queryElement.appendChild( a );
                    }
                    Element filter = XMLTools.getElement( constraint, "ogc:Filter", nsContext );
                    if ( filter == null ) {
                        LOG.logDebug( " No filter node found, therefore creating one." );
                        // Element a = queryElement.getOwnerDocument().createElementNS(
                        // CommonNamespaces.OGCNS.toASCIIString(),
                        // "ogc:Filter" );
                        Element a = doc.createElementNS( CommonNamespaces.OGCNS.toASCIIString(), "ogc:Filter" );
                        // queryElement.getOwnerDocument().importNode( a, false );
                        doc.importNode( a, false );
                        filter = (Element) constraint.appendChild( a );
                    }
                    Node firstOriginalFilterNode = filter.getFirstChild();
                    Element andNode = null;
                    if ( firstOriginalFilterNode != null ) {
                        LOG.logDebug( " The ogc:Filter has a firstChild node, therefore creating an extra ogc:And." );
                        Element a = doc.createElementNS( CommonNamespaces.OGCNS.toASCIIString(), "ogc:And" );
                        // queryElement.getOwnerDocument().importNode( a, false );
                        doc.importNode( a, false );
                        andNode = (Element) filter.appendChild( a );
                        andNode.appendChild( firstOriginalFilterNode );
                    }
                    // Element tmpNode = queryElement.getOwnerDocument().createElementNS(
                    // CommonNamespaces.OGCNS.toASCIIString(),
                    // "ogc:And" );
                    Element tmpNode = doc.createElementNS( CommonNamespaces.OGCNS.toASCIIString(), "ogc:And" );
                    // queryElement.getOwnerDocument().importNode( tmpNode, false );
                    doc.importNode( tmpNode, false );
                    Element topNode = null;
                    if ( andNode != null ) {
                        if ( typeNames.size() > 1 ) {
                            topNode = (Element) andNode.appendChild( tmpNode );
                        } else {
                            topNode = andNode;
                        }
                    } else {
                        if ( typeNames.size() > 1 ) {
                            topNode = (Element) filter.appendChild( tmpNode );
                        } else {
                            topNode = filter;
                        }

                    }
                    // a random string to be used for the extra filter.

                    for ( int i = 0; i < typeNames.size(); ++i ) {
                        QualifiedName qName = typeNames.get( i );
                        URI ns = qName.getNamespace();
                        String prefix = qName.getPrefix();
                        // for debugging purposes
                        if ( prefix == null ) {
                            prefix = "";
                        } else {
                            prefix += ":";
                        }
                        if ( ns == null || rimNS.equals( ns.toString() ) ) {
                            LOG.logDebug( " We found the following namespace for the ElementSetName/@typeName: " + ns
                                          + " so we map to the prefix rim." );
                            prefix = "rim:";
                        }
                        String result = mapping.getProperty( prefix + qName.getLocalName() );
                        LOG.logDebug( " for the FeatureType: " + prefix + qName.getLocalName()
                                      + " we found following mapping: " + result );
                        if ( result != null ) {
                            if ( !"app:RegistryObject".equals( result ) ) {
                                LOG.logError( "CSW (ebRIM) EBRIM_Mapping: the given typeName is not a RegistryObject, and can therefore not be returned: "
                                              + prefix + qName.getLocalName() );
                            } else {
                                String aliasPrefix = getVariableForQName( qName, varsInQuery );
                                LOG.logDebug( " in elementSetName found alias: " + aliasPrefix + " for the typename: "
                                              + qName.getLocalName() );
                                if ( aliasPrefix == null || "".equals( aliasPrefix ) ) {
                                    LOG.logError( "CSW (ebRIM) EBRIM_Mapping: in elementSetName found no alias for the typeName: "
                                                  + qName.getLocalName() + " this cannot be!" );
                                }

                                //
                                appendRegistryObjects( resultElement, "/$" + aliasPrefix );
                                // appendRegistryObjects( resultElement, elementSetName, "/" + result );

                                // now appending an ogc:PropertyIsEqual to the ogc:Or node of the ogc:Filter
                                // tmpNode = queryElement.getOwnerDocument().createElementNS(
                                // CommonNamespaces.OGCNS.toASCIIString(),
                                // "ogc:PropertyIsEqualTo" );
                                tmpNode = doc.createElementNS( CommonNamespaces.OGCNS.toASCIIString(),
                                                               "ogc:PropertyIsEqualTo" );

                                // queryElement.getOwnerDocument().importNode( tmpNode, false );
                                doc.importNode( tmpNode, false );
                                Element equalTo = (Element) topNode.appendChild( tmpNode );
                                equalTo.setAttribute( "matchCase", "true" );

                                // create the propertyName node referencing the app:RegistryObject/app:type
                                // tmpNode = queryElement.getOwnerDocument().createElementNS(
                                // CommonNamespaces.OGCNS.toASCIIString(),
                                // "ogc:PropertyName" );
                                tmpNode = doc.createElementNS( CommonNamespaces.OGCNS.toASCIIString(),
                                                               "ogc:PropertyName" );
                                // queryElement.getOwnerDocument().importNode( tmpNode, false );
                                doc.importNode( tmpNode, false );
                                Element propName = (Element) equalTo.appendChild( tmpNode );
                                XMLTools.setNodeValue( propName, "$" + aliasPrefix + "/rim:type" );

                                // create the literal (localname) node to which the propertyname
                                // app:RegistryObject/app:type should match
                                // tmpNode = queryElement.getOwnerDocument().createElementNS(
                                // CommonNamespaces.OGCNS.toASCIIString(),
                                // "ogc:Literal" );
                                tmpNode = doc.createElementNS( CommonNamespaces.OGCNS.toASCIIString(), "ogc:Literal" );
                                // queryElement.getOwnerDocument().importNode( tmpNode, false );
                                doc.importNode( tmpNode, false );
                                Element literal = (Element) equalTo.appendChild( tmpNode );
                                XMLTools.setNodeValue( literal, qName.getLocalName() );

                                // define a new alias for the typeNames
                                // queryTypeNameAttrSB.append( "rim:" + qName.getLocalName() ).append( "=" ).append(
                                // aliasPrefix );
                                // if ( ( i + 1 ) < typeNames.size() ) {
                                // queryTypeNameAttrSB.append( " " );
                                // }

                            }
                        } else {
                            LOG.logInfo( "CSW (ebRIM) EBRIM_Mapping: Found no mapping for: " + prefix
                                         + qName.getLocalName() + ", so ignoring it." );
                        }
                    }
                    // add the typeNames attribute to the QueryElement
                    // if ( resetQueryTypeNames ) {
                    // queryElement.setAttribute( "typeNames", queryTypeNameAttrSB.toString() );
                    // }
                    // if ( LOG.getLevel() == ILogger.LOG_DEBUG ) {
                    // XMLFragment docTest = new XMLFragment( queryElement );
                    // LOG.logDebug( " newly created query Element: \n"
                    // + docTest.getAsPrettyString() );
                    // }
                }
            }
        } catch ( XMLParsingException e ) {
            LOG.logError( "CSW (ebRIM) EBRIM_Mapping: following error occured while trying to map elementSetName node",
                          e );
        }
        NodeList nodeList = resultElement.getChildNodes();
        for ( int i = 0; i < nodeList.getLength(); ++i ) {
            LOG.logDebug( " Node " + i + " has a localName: " + nodeList.item( i ).getLocalName() );
        }

        return resultElement.getChildNodes();
    }

    /**
     * Maps the typename of the given Element if the targetNamespace attribute of the given Node equals the rimNS.
     *
     * @param typeNameElement
     *            the TypeName Element inside a DescribeRecord request.
     * @return the mapping of the featureType requested or an empty string (e.g. "").
     */
    public String mapTypeNameElement( Node typeNameElement ) {
        String typeNameValue = null;
        if ( typeNameElement != null ) {
            try {
                String targetNamespace = XMLTools.getRequiredNodeAsString( typeNameElement, "@targetNamespace",
                                                                           nsContext );
                if ( rimNS.equals( targetNamespace ) ) {
                    typeNameValue = XMLTools.getNodeAsString( typeNameElement, ".", nsContext, null );
                    if ( typeNameValue != null ) {
                        typeNameValue = stripRoot( typeNameValue.trim() );
                        int index = typeNameValue.lastIndexOf( ":" );
                        if ( index != -1 ) {
                            typeNameValue = typeNameValue.substring( index );
                        }
                        typeNameValue = mapping.getProperty( "rim:" + typeNameValue );
                    }
                } else {
                    LOG.logDebug( " The given namespace: " + targetNamespace
                                  + " can not be mapped to the rim namespace: " + rimNS + " so no mapping is done" );
                }
            } catch ( XMLParsingException e ) {
                LOG.logInfo( e.getMessage() );
            }
        }
        if ( typeNameValue == null ) {
            typeNameValue = "";
        }
        return typeNameValue;

    }

    /**
     * Searches for a given qName in the given map and removes the mapping if it is found.
     *
     * @param qName
     *            to search for
     * @param tmpVariables
     *            some temporary variables
     * @return the mapped variable or <code>null</code> if none was found.
     */
    private String getVariableForQName( QualifiedName qName, Map<String, QualifiedName> tmpVariables ) {
        if ( tmpVariables.containsValue( qName ) ) {
            for ( String variable : tmpVariables.keySet() ) {
                if ( qName.equals( tmpVariables.get( variable ) ) ) {
                    tmpVariables.remove( variable );
                    return variable;
                }
            }
        }
        return null;
    }

    /**
     * A simple method to split the given typeNamesAttribute in to type names and finds the optional variables inside
     * them.
     *
     * @param node
     *            the context node which will be used to search for the prefixes.
     * @param typeNamesAttribute
     *            the String containing the typenames attribute values.
     * @param vars
     *            a map in which the found variables will be stored
     * @return a list with typenames parsed from the typeNamesAttribute String, if no typenames were found the size will
     *         be 0.
     */
    private List<QualifiedName> parseTypeList( Node node, String typeNamesAttribute, Map<String, QualifiedName> vars ) {
        LOG.logDebug( " Trying to map following typeName: " + typeNamesAttribute );
        // System.out.println( "einmal: " + root );
        // DOMPrinter.printNode( root, "");
        String[] splitter = typeNamesAttribute.split( " " );
        List<QualifiedName> typeNames = new ArrayList<QualifiedName>();

        for ( String s : splitter ) {
            try {
                LOG.logDebug( " Trying to parse following typeName (with/without variables): " + s );
                if ( !s.startsWith( "$" ) ) {
                    gd.findVariablesInTypeName( s, node, typeNames, vars, true );
                } else {
                    // Attention, I don't know if this is the wanted behavior, we loose the variable declaration.
                    LOG.logDebug( " Because the given typeName starts with an '$'-sign, we first dereference the alias: "
                                  + s.substring( 1 ) );
                    if ( variables.containsKey( s.substring( 1 ) ) ) {
                        QualifiedName qName = variables.get( s.substring( 1 ) );
                        LOG.logDebug( " \t the alias: " + s.substring( 1 )
                                      + " was therefore replaced with the propertyName: " + qName.getPrefix() + ":"
                                      + qName.getLocalName() );
                        typeNames.add( qName );
                    } else {
                        LOG.logError( "CSW (ebRIM) EBRIM_Mapping: \t the alias was not declared, this cannot be!" );
                    }
                }

            } catch ( OGCWebServiceException e ) {
                LOG.logError( e.getMessage(), e );
            } catch ( URISyntaxException e ) {
                LOG.logError( e.getMessage(), e );
            }
        }
        return typeNames;
    }

    /**
     *
     * @param mappedName
     *            to which the appropriate propertyPath will be added.
     * @param variable
     *            starting with the "$" which will replace the mappedName.
     * @param isFirst
     *            true if the given mappedName was the the firstelement on the requested xpath.
     */
    private String addWFSPropertyPath( String mappedName, String variable, boolean isFirst ) {
        StringBuffer toBeAdded = new StringBuffer();
        if ( "app:RegistryObject".equalsIgnoreCase( mappedName ) ) {
            if ( !isFirst ) {
                toBeAdded.append( "app:linkedRegistryObject/app:LINK_RegObj_RegObj/app:registryObject/" );
            }
        } else {
            if ( isFirst ) {
                toBeAdded.append( "app:RegistryObject/" );
            }
            if ( "app:Name".equals( mappedName ) ) {
                toBeAdded.append( "app:name/" );
            } else if ( "app:Description".equalsIgnoreCase( mappedName ) ) {
                toBeAdded.append( "app:description/" );
            } else if ( "app:Slot".equals( mappedName ) ) {
                toBeAdded.append( "app:slots/" );
            } else if ( "app:VersionInfo".equals( mappedName ) ) {
                toBeAdded.append( "app:versionInfo/" );
            } else if ( "app:ObjectRef".equals( mappedName ) ) {
                toBeAdded.append( "app:auditableEvent/app:AuditableEvent/app:affectedObjects/" );
            } else if ( "app:ExtrinsicObject".equals( mappedName ) ) {
                // this is actually the rim:ExtrinsicObject/rim:ContentVersionInfo element
                toBeAdded.append( "app:extrinsicObject" );
            }
        }
        if ( variable != null && !"".equals( variable.trim() ) ) {
            mappedName = variable;
        }
        return ( toBeAdded.toString() + mappedName );

    }

    /**
     * Recursively strips all leading '.' and '/' from the given String.
     *
     * @param toBeStripped
     *            the String to be stripped
     * @return the stripped String.
     */
    private String stripRoot( String toBeStripped ) {
        if ( toBeStripped != null ) {
            if ( toBeStripped.startsWith( "/" ) || toBeStripped.startsWith( "." ) ) {
                LOG.logDebug( " stripping first character of: " + toBeStripped );
                return stripRoot( toBeStripped.substring( 1 ) );
            }
        }
        return toBeStripped;
    }

    /**
     * appends the given mapped elementSetName/@typeNames value according to the elementSetNameValue to the given
     * resultElement.
     *
     * @param resultElement
     *            to append the <wfs:PropertyNames> to
     * @param resultedMapping
     *            the mapped TypeNames-Value
     */
    private void appendRegistryObjects( Element resultElement, String resultedMapping ) {
        // if ( "full".equalsIgnoreCase( elementSetNameValue ) ) {
        XMLTools.appendElement( resultElement, CommonNamespaces.WFSNS, "wfs:PropertyName", resultedMapping );
        // } else {
        // XMLTools.appendElement( resultElement, CommonNamespaces.WFSNS, "wfs:PropertyName", resultedMapping
        // + "/app:iduri" );
        // XMLTools.appendElement( resultElement, CommonNamespaces.WFSNS, "wfs:PropertyName", resultedMapping
        // + "/app:liduri" );
        // XMLTools.appendElement( resultElement, CommonNamespaces.WFSNS, "wfs:PropertyName", resultedMapping
        // + "/app:objectType" );
        // XMLTools.appendElement( resultElement, CommonNamespaces.WFSNS, "wfs:PropertyName", resultedMapping
        // + "/app:status" );
        // XMLTools.appendElement( resultElement, CommonNamespaces.WFSNS, "wfs:PropertyName", resultedMapping
        // + "/app:versionInfo" );
        // if ( "summary".equalsIgnoreCase( elementSetNameValue ) ) {
        // XMLTools.appendElement( resultElement, CommonNamespaces.WFSNS, "wfs:PropertyName", resultedMapping
        // + "/app:slots" );
        // XMLTools.appendElement( resultElement, CommonNamespaces.WFSNS, "wfs:PropertyName",
        // resultedMapping + "/app:name/app:Name" );
        // XMLTools.appendElement( resultElement, CommonNamespaces.WFSNS, "wfs:PropertyName",
        // resultedMapping + "/app:description/app:Description" );
        // }
        // }
    }

    /**
     * Sets the prefix to the localname of the given qName to rim: if the NS of the QName equals the rimNS or the qName
     * prefix == null (or empty) or the namespace of the given QName == null. Else the found prefix of the QName is
     * taken (happens for example with the wrs prefix)
     *
     * @param qName
     *            to be parsed.
     * @return the prefix:localname part of the qName with prefix bound to "rim" or the qNames prefix.
     */
    private String createMapStringForQName( QualifiedName qName ) {
        URI ns = qName.getNamespace();
        String prefix = qName.getPrefix();
        if ( ns == null || rimNS.equals( ns.toString() ) || qName.getPrefix() == null
             || "".equals( qName.getPrefix().trim() ) ) {
            prefix = "rim:";
        } else if ( wrsNS.equals( ns.toString() ) ) {
            prefix = CommonNamespaces.WRS_EBRIM_PREFIX + ":";
        }
        return prefix + qName.getLocalName();
    }

    /**
     * A simple method, which will return a node in which a prefix can be search.
     *
     * @param xsltNode
     *            the local node from the calling xslt processor to one of the functions.
     * @return the original rootNode if it is not null, otherwise the given xsltNode.
     */
    private Node getPrefixResolverNode( Node xsltNode ) {
        Node tmpNode = rootNode;
        if ( tmpNode == null ) {
            tmpNode = xsltNode;
        }
        Queue<Node> nodes = new LinkedList<Node>();
        nodes.offer( tmpNode );
        while ( !nodes.isEmpty() ) {
            tmpNode = nodes.poll();
            if ( xsltNode.getNodeType() == Node.ATTRIBUTE_NODE ) {
                NamedNodeMap nnm = tmpNode.getAttributes();
                for ( int j = 0; j < nnm.getLength(); ++j ) {
                    if ( xsltNode.isEqualNode( nnm.item( j ) ) ) {
                        return nnm.item( j );
                    }
                }
            } else {
                if ( xsltNode.isEqualNode( tmpNode ) ) {
                    return tmpNode;
                }
            }
            NodeList nl = tmpNode.getChildNodes();
            for ( int i = 0; i < nl.getLength(); ++i ) {
                nodes.offer( nl.item( i ) );
            }
        }
        return xsltNode;
    }
}
