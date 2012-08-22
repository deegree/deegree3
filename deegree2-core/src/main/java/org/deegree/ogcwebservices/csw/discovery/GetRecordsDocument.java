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

package org.deegree.ogcwebservices.csw.discovery;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.deegree.datatypes.QualifiedName;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.StringTools;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.framework.xml.XMLTools;
import org.deegree.i18n.Messages;
import org.deegree.model.filterencoding.AbstractFilter;
import org.deegree.model.filterencoding.AbstractOperation;
import org.deegree.model.filterencoding.ComplexFilter;
import org.deegree.model.filterencoding.Expression;
import org.deegree.model.filterencoding.Filter;
import org.deegree.model.filterencoding.FilterConstructionException;
import org.deegree.model.filterencoding.LogicalOperation;
import org.deegree.model.filterencoding.Operation;
import org.deegree.model.filterencoding.PropertyIsBetweenOperation;
import org.deegree.model.filterencoding.PropertyIsCOMPOperation;
import org.deegree.model.filterencoding.PropertyIsInstanceOfOperation;
import org.deegree.model.filterencoding.PropertyIsLikeOperation;
import org.deegree.model.filterencoding.PropertyIsNullOperation;
import org.deegree.model.filterencoding.PropertyName;
import org.deegree.model.filterencoding.SpatialOperation;
import org.deegree.ogcbase.CommonNamespaces;
import org.deegree.ogcbase.ExceptionCode;
import org.deegree.ogcbase.PropertyPath;
import org.deegree.ogcbase.PropertyPathFactory;
import org.deegree.ogcbase.SortProperty;
import org.deegree.ogcwebservices.InvalidParameterValueException;
import org.deegree.ogcwebservices.MissingParameterValueException;
import org.deegree.ogcwebservices.OGCWebServiceException;
import org.deegree.ogcwebservices.OperationNotSupportedException;
import org.deegree.ogcwebservices.csw.AbstractCSWRequestDocument;
import org.deegree.ogcwebservices.csw.discovery.GetRecords.RESULT_TYPE;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Represents an XML GetRecords document of an OGC CSW 2.0.0 and 2.0.1 compliant service.
 *
 * @author <a href="mailto:tfr@users.sourceforge.net">Torsten Friebe </a>
 * @author <a href="mailto:mschneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class GetRecordsDocument extends AbstractCSWRequestDocument {

    private static final long serialVersionUID = 2796229558893029054L;

    private static final ILogger LOG = LoggerFactory.getLogger( GetRecordsDocument.class );

    private static final String XML_TEMPLATE = "GetRecordsTemplate.xml";

    /**
     * Extracts a <code>GetRecords</code> representation of this object.
     *
     * @param id
     *            unique ID of the request
     * @return GetRecords representation of this object
     * @throws MissingParameterValueException
     * @throws InvalidParameterValueException
     * @throws OperationNotSupportedException
     *             if an CqlText constrained is requested
     * @throws OGCWebServiceException
     *             if something else went wrong
     */
    public GetRecords parse( String id )
                            throws OGCWebServiceException {

        // '<csw:GetRecords>'-element (required)
        try {
            Element contextNode = (Element) XMLTools.getRequiredNode( this.getRootElement(), "self::csw:GetRecords",
                                                                      nsContext );
            // 'service'-attribute (optional, must be CSW)
            String service = XMLTools.getNodeAsString( contextNode, "@service", nsContext, "CSW" );
            if ( !"CSW".equals( service ) ) {
                throw new OGCWebServiceException( "GetRecordsDocument",
                                                  Messages.getMessage( "CSW_INVALID_SERVICE_PARAM" ),
                                                  ExceptionCode.INVALIDPARAMETERVALUE );
            }

            String defaultVersion = GetRecords.DEFAULT_VERSION;
            boolean isEBRIM = ( contextNode.getOwnerDocument().lookupPrefix(
                                                                             CommonNamespaces.OASIS_EBRIMNS.toASCIIString() ) != null );
            if ( !isEBRIM ) {
                isEBRIM = isEbrimDefined( contextNode );
            }
            LOG.logDebug( "GetRecordsDocument: For the namespaceDefinition of the ebrim catalogue, the value is: "
                          + isEBRIM );
            if ( isEBRIM ) {
                defaultVersion = "2.0.1";
            }

            // 'version'-attribute (optional)
            String version = XMLTools.getNodeAsString( contextNode, "@version", nsContext, defaultVersion );
            if ( !( GetRecords.DEFAULT_VERSION.equals( version ) || "2.0.1".equals( version ) ) ) {
                throw new OGCWebServiceException( "GetRecordsDocument",
                                                  Messages.getMessage( "CSW_NOT_SUPPORTED_VERSION",
                                                                       GetRecords.DEFAULT_VERSION, "2.0.1", version ),
                                                  ExceptionCode.INVALIDPARAMETERVALUE );
            }

            // 'requestId'-attribute (optional)
            String requestId = XMLTools.getNodeAsString( contextNode, "@requestId", nsContext, id );

            // 'resultType'-attribute
            // type="csw:ResultType" use="optional" default="hits"
            String resultTypeString = XMLTools.getNodeAsString( contextNode, "@resultType", nsContext,
                                                                GetRecords.RESULT_TYPE_STRING_HITS );
            RESULT_TYPE resultType = RESULT_TYPE.RESULTS;
            if ( GetRecords.RESULT_TYPE_STRING_HITS.equalsIgnoreCase( resultTypeString ) ) {
                resultType = RESULT_TYPE.HITS;
            } else if ( GetRecords.RESULT_TYPE_STRING_RESULTS.equalsIgnoreCase( resultTypeString ) ) {
                resultType = RESULT_TYPE.RESULTS;
            } else if ( GetRecords.RESULT_TYPE_STRING_VALIDATE.equalsIgnoreCase( resultTypeString ) ) {
                resultType = RESULT_TYPE.VALIDATE;
            } else {
                throw new OGCWebServiceException( Messages.getMessage( "CSW_INVALID_RESULTTYPE", resultTypeString,
                                                                       GetRecords.RESULT_TYPE_STRING_HITS,
                                                                       GetRecords.RESULT_TYPE_STRING_RESULTS,
                                                                       GetRecords.RESULT_TYPE_STRING_VALIDATE ),
                                                  ExceptionCode.INVALIDPARAMETERVALUE );
            }

            // 'outputFormat'-attribute
            // type="xsd:string" use="optional" default="text/xml"
            String outputFormat = XMLTools.getNodeAsString( contextNode, "@outputFormat", nsContext,
                                                            GetRecords.DEFAULT_OUTPUTFORMAT );

            String defaultOutputSchema = GetRecords.DEFAULT_OUTPUTSCHEMA;
            if ( isEBRIM ) {
                defaultOutputSchema = CommonNamespaces.OASIS_EBRIMNS.toASCIIString();
            }
            // 'outputSchema'-attribute
            // type="xsd:anyURI" use="optional" default="OGCCORE"
            String outputSchema = XMLTools.getNodeAsString( contextNode, "@outputSchema", nsContext,
                                                            defaultOutputSchema );

            // 'startPosition'-attribute
            // type="xsd:positiveInteger" use="optional" default="1"
            int startPosition = XMLTools.getNodeAsInt( contextNode, "@startPosition", nsContext,
                                                       GetRecords.DEFAULT_STARTPOSITION );
            if ( startPosition < 1 ) {
                throw new OGCWebServiceException( Messages.getMessage( "CSW_INVALID_STARTPOSITION",
                                                                       new Integer( startPosition ) ),
                                                  ExceptionCode.INVALIDPARAMETERVALUE );
            }

            // 'maxRecords'-attribute
            // type="xsd:nonNegativeInteger" use="optional" default="10"
            int maxRecords = XMLTools.getNodeAsInt( contextNode, "@maxRecords", nsContext,
                                                    GetRecords.DEFAULT_MAX_RECORDS );

            // '<csw:DistributedSearch>'-element (optional)
            Node distributedSearchElement = XMLTools.getNode( contextNode, "csw:DistributedSearch", nsContext );
            int hopCount = GetRecords.DEFAULT_HOPCOUNT;
            if ( distributedSearchElement != null ) {
                hopCount = XMLTools.getNodeAsInt( contextNode, "@hopCount", nsContext, GetRecords.DEFAULT_HOPCOUNT );
            }

            // '<csw:ResponseHandler>'-elements (optional)
            String rHandler = XMLTools.getNodeAsString( contextNode, "csw:ResponseHandler", nsContext, null );
            URI responseHandler = null;
            if ( rHandler != null ) {
                try {
                    responseHandler = new URI( rHandler );
                } catch ( URISyntaxException e ) {
                    throw new OGCWebServiceException( Messages.getMessage( "CSW_INVALID_RESPONSE_HANDLER", rHandler ),
                                                      ExceptionCode.INVALIDPARAMETERVALUE );
                }
                LOG.logWarning( Messages.getMessage( "CSW_NO_REPONSE_HANDLER_IMPLEMENTATION" ) );

            }

            // '<csw:Query>'-elements (required)
            // List nl = XMLTools.getRequiredNodes( contextNode, "csw:Query", nsContext );
            Element queryNode = (Element) XMLTools.getRequiredNode( contextNode, "csw:Query", nsContext );

            Map<String, QualifiedName> declaredVariables = new HashMap<String, QualifiedName>();
            List<QualifiedName> queryTypeNames = new ArrayList<QualifiedName>();

            // 'typeName'-attribute use="required"
            String tNames = XMLTools.getRequiredNodeAsString( queryNode, "@typeNames", nsContext );
            String[] simpleTypeNames = tNames.split( " " );
            // only bind the prefixes to namespaces if the version is 2.0.0
            boolean bindTypeNamesToNS = !GetRecords.DEFAULT_VERSION.equals( version );
            // Find any variables
            for ( String typeName : simpleTypeNames ) {
                findVariablesInTypeName( typeName, queryNode, queryTypeNames, declaredVariables, bindTypeNamesToNS );
            }

            // '<csw:ElementSetName>'-element (optional)
            Element elementSetNameElement = (Element) XMLTools.getNode( queryNode, "csw:ElementSetName", nsContext );
            String elementSetName = null;
            List<QualifiedName> elementSetNameTypeNames = null;
            Map<String, QualifiedName> elementSetNameVariables = null;
            List<PropertyPath> elementNames = new ArrayList<PropertyPath>();
            // choice construct
            if ( elementSetNameElement != null ) {
                // must contain one of the values 'brief', 'summary' or
                // 'full'
                elementSetName = XMLTools.getRequiredNodeAsString( elementSetNameElement, "text()", nsContext,
                                                                   new String[] { "brief", "summary", "full" } );
                tNames = elementSetNameElement.getAttribute( "typeNames" );
                if ( tNames != null ) {
                    String[] esnTypeNames = tNames.split( " " );
                    elementSetNameVariables = new HashMap<String, QualifiedName>();
                    elementSetNameTypeNames = new ArrayList<QualifiedName>();
                    for ( String tn : esnTypeNames ) {
                        if ( tn.trim().startsWith( "$" ) ) {
                            String tmpVar = tn.trim().substring( 1 );
                            if ( !declaredVariables.containsKey( tmpVar ) ) {
                                throw new OGCWebServiceException(
                                                                  Messages.getMessage(
                                                                                       "CSW_ELEMENT_SET_NAME_TYPENAME_ALIAS",
                                                                                       tmpVar ),
                                                                  ExceptionCode.INVALIDPARAMETERVALUE );
                            }
                            elementSetNameVariables.put( tmpVar, declaredVariables.get( tmpVar ) );
                        } else {
                            QualifiedName qName = parseQNameFromString( tn.trim(), elementSetNameElement,
                                                                        bindTypeNamesToNS );
                            elementSetNameTypeNames.add( qName );
                        }
                    }
                }

            } else {
                // '<csw:ElementName>'-element (required, if no
                // '<csw:ElementSetName>' is given)
                List<Node> elementNameList = XMLTools.getNodes( queryNode, "csw:ElementName", nsContext );
                if ( elementNameList.size() == 0 ) {
                    throw new XMLParsingException( Messages.getMessage( "CSW_MISSING_QUERY_ELEMENT(SET)NAME" ) );
                }
                for ( Node n : elementNameList ) {
                    QualifiedName elementName = XMLTools.getNodeAsQualifiedName( n, "text()", nsContext, null );
                    if ( elementName != null ) {
                        elementNames.add( PropertyPathFactory.createPropertyPath( elementName ) );
                    }
                }

            }

            // '<csw:Constraint>'-element (optional)
            Element constraintElement = (Element) XMLTools.getNode( queryNode, "csw:Constraint", nsContext );
            Filter constraint = null;
            if ( constraintElement != null ) {
                String ver = XMLTools.getRequiredNodeAsString( constraintElement, "@version", nsContext );
                if ( !"1.0.0".equals( ver ) && !"1.1.0".equals( ver ) ) {
                    throw new OGCWebServiceException( Messages.getMessage( "CSW_INVALID_CONSTRAINT_VERSION", ver ),
                                                      ExceptionCode.INVALIDPARAMETERVALUE );
                }
                Node filterElement = XMLTools.getNode( constraintElement, "ogc:Filter", nsContext );
                if ( filterElement != null ) {
                    try {
                        constraint = AbstractFilter.buildFromDOM( (Element) filterElement, false );
                    } catch ( FilterConstructionException fce ) {
                        throw new OGCWebServiceException( Messages.getMessage( "CSW_INVALID_CONSTRAINT_CONTENT",
                                                                               fce.getMessage() ),
                                                          ExceptionCode.INVALIDPARAMETERVALUE );
                    }
                } else {
                    String cqlText = XMLTools.getNodeAsString( constraintElement, "csw:CqlText", nsContext, null );
                    if ( cqlText == null ) {
                        throw new OGCWebServiceException( Messages.getMessage( "CSW_CQL_NOR_FILTER" ),
                                                          ExceptionCode.INVALIDPARAMETERVALUE );
                    }

                    throw new OGCWebServiceException( Messages.getMessage( "CSW_NO_CQL_IMPLEMENTATION" ),
                                                      ExceptionCode.OPERATIONNOTSUPPORTED );
                }
            }
            // find undeclared referenced variables used in the filter element.
            if ( constraint instanceof ComplexFilter ) {
                checkReferencedVariables( (ComplexFilter) constraint, declaredVariables );
            }

            // '<ogc:SortBy>'-element (optional)
            Node sortByElement = XMLTools.getNode( queryNode, "ogc:SortBy", nsContext );
            SortProperty[] sortProperties = null;
            if ( sortByElement != null ) {
                List<Node> sortPropertyList = XMLTools.getNodes( sortByElement, "ogc:SortProperty", nsContext );
                if ( sortPropertyList.size() == 0 ) {
                    throw new OGCWebServiceException( Messages.getMessage( "CSW_NO_SORTPROPERTY_LIST" ),
                                                      ExceptionCode.INVALIDPARAMETERVALUE );

                }
                sortProperties = new SortProperty[sortPropertyList.size()];
                for ( int j = 0; j < sortPropertyList.size(); j++ ) {
                    sortProperties[j] = SortProperty.create( (Element) sortPropertyList.get( j ) );
                }
            }

            Query query = new Query( elementSetName, elementSetNameTypeNames, elementSetNameVariables, elementNames,
                                     constraint, sortProperties, queryTypeNames, declaredVariables );

            // in the future the vendorSpecificParameters
            Map<String, String> vendorSpecificParameters = parseDRMParams( this.getRootElement() );
            return new GetRecords( requestId, version, vendorSpecificParameters, null, resultType, outputFormat,
                                   outputSchema, startPosition, maxRecords, hopCount, responseHandler, query );
        } catch ( XMLParsingException xmlpe ) {
            LOG.logError( "CatalogGetRecords", xmlpe );
            throw new OGCWebServiceException( xmlpe.getMessage(), ExceptionCode.INVALIDPARAMETERVALUE );
        } catch ( URISyntaxException urise ) {
            LOG.logError( "CatalogGetRecords", urise );
            throw new OGCWebServiceException( urise.getMessage(), ExceptionCode.INVALIDPARAMETERVALUE );
        }
    }

    /**
     * @param contextNode
     * @return true if the namespace "urn:oasis:names:tc:ebxml- regrep:xsd:rim:3.0" was found in one
     *         of the nodes of the dom-tree.
     */
    protected boolean isEbrimDefined( Node contextNode ) {

        boolean isEbRim = contextNode.lookupPrefix( CommonNamespaces.OASIS_EBRIMNS.toASCIIString() ) != null;
        if ( !isEbRim ) {
            NodeList nl = contextNode.getChildNodes();
            for ( int i = 0; i < nl.getLength(); ++i ) {
                isEbRim = isEbrimDefined( nl.item( i ) );
                if ( isEbRim ) {
                    return true;
                }
            }
        }
        return isEbRim;
    }

    /**
     * Helper method to find any declared variables in given Query/@typeNames
     *
     * @param typeName
     *            the type name to test
     * @param queryNode
     *            the querynode (used to find a given prefix)
     * @param typeNames
     *            a list to save the typeName (as QualifiedNames) in
     * @param variables
     *            a Map containing the vars/QualifiedName mappings
     * @param bindTypeNameToNS
     *            if the namespaces should be bounded to the typeNames
     * @throws URISyntaxException
     *             if the prefix is not bound to a namespace
     * @throws OGCWebServiceException
     *             if a variable name is unambiguous
     */
    public void findVariablesInTypeName( String typeName, Node queryNode, List<QualifiedName> typeNames,
                                         Map<String, QualifiedName> variables, boolean bindTypeNameToNS )
                            throws OGCWebServiceException, URISyntaxException {
        LOG.logDebug( "testing for variables in typeName: " + typeName );
        int variableIndex = typeName.lastIndexOf( '=' );
        String tmpTypeName = typeName;
        if ( variableIndex != -1 ) {
            // find the typeNames
            tmpTypeName = typeName.substring( 0, variableIndex ).trim();
            LOG.logDebug( "typeName contains variables" );
        }

        // creating the qualified name
        QualifiedName qName = parseQNameFromString( tmpTypeName, queryNode, bindTypeNameToNS );
        typeNames.add( qName );
        if ( variableIndex != -1 ) {
            if ( ( variableIndex + 1 ) < typeName.length() ) {
                // find the variables which should be referenced with the $-sign
                String allVars = typeName.substring( variableIndex + 1 );
                String[] vars = allVars.split( "," );
                for ( String var : vars ) {
                    LOG.logDebug( "found var: " + var );
                    if ( variables.put( var.trim(), qName ) != null ) {
                        String out = Messages.getMessage( "CSW_AMBIGUOUS_VARIABLE_DEF", var.trim() );
                        throw new OGCWebServiceException( "GetRecords", out, ExceptionCode.INVALIDPARAMETERVALUE );
                    }
                }
            }
        }
    }

    /**
     * @param typeName
     *            to be transformed to a QName
     * @param queryNode
     *            needed to get the namespace
     * @param bindTypeNameToNS
     *            if true the namespace will be bound to the qualified name
     * @return a QualifiedName representing the typeName
     * @throws URISyntaxException
     */
    public QualifiedName parseQNameFromString( String typeName, Node queryNode, boolean bindTypeNameToNS )
                            throws URISyntaxException {
        int prefixIndex = typeName.indexOf( ':' );
        String preFix = null;
        URI nameSpace = null;
        String localName = typeName;
        if ( prefixIndex != -1 ) {
            preFix = typeName.substring( 0, prefixIndex ).trim();
            if ( bindTypeNameToNS ) {
                LOG.logDebug( "Trying to find namespace binding for the prefix: " + preFix + " on node queryNode: "
                              + queryNode.getNodeName() );
                nameSpace = XMLTools.getNamespaceForPrefix( preFix, queryNode );
            } else {
                LOG.logDebug( "Not binding namespaces for the prefix: " + preFix + " on node queryNode: "
                              + queryNode.getNodeName() + " because the version of the GetRecordsRequest is not 2.0.2" );
            }
            // for version 2.0.0 no namespace checkin is required following versions should check if
            // the returned namespace is null.
            if ( ( prefixIndex + 1 ) < typeName.length() ) {
                localName = typeName.substring( prefixIndex + 1 ).trim();
            } else {
                localName = typeName.substring( prefixIndex ).trim();
            }
        }
        LOG.logDebug( "found prefix: " + preFix );
        LOG.logDebug( "found localName: " + localName );
        LOG.logDebug( "found namespace: " + nameSpace );
        return new QualifiedName( preFix, localName, nameSpace );
    }

    /**
     * Iterates over the Operations of a complexfilter to find if non declared variables are used.
     *
     * @param constraint
     * @param variables
     * @throws OGCWebServiceException
     */
    protected void checkReferencedVariables( ComplexFilter constraint, Map<String, QualifiedName> variables )
                            throws OGCWebServiceException {
        AbstractOperation topOperation = (AbstractOperation) constraint.getOperation();
        if ( topOperation instanceof LogicalOperation ) {
            List<Operation> operations = ( (LogicalOperation) topOperation ).getArguments();
            for ( Operation op : operations ) {
                findNonDeclaredVariables( (AbstractOperation) op, variables );
            }
        } else {
            findNonDeclaredVariables( topOperation, variables );
        }
    }

    /**
     * (Recursively) finds a reference to a non declared variable in the propertyname of the given
     * operation.
     *
     * @param operation
     *            to be checked
     * @param variables
     *            which were declared
     * @throws OGCWebServiceException
     *             if such a reference is found
     */
    protected void findNonDeclaredVariables( AbstractOperation operation, Map<String, QualifiedName> variables )
                            throws OGCWebServiceException {
        if ( operation instanceof LogicalOperation ) {
            List<Operation> operations = ( (LogicalOperation) operation ).getArguments();
            for ( Operation op : operations ) {
                findNonDeclaredVariables( (AbstractOperation) op, variables );
            }
        } else if ( operation instanceof SpatialOperation ) {
            findNonDeclaredVariables( ( (SpatialOperation) operation ).getPropertyName(), variables );
        } else {
            if ( operation instanceof PropertyIsBetweenOperation ) {
                findNonDeclaredVariables( ( (PropertyIsBetweenOperation) operation ).getPropertyName(), variables );
            } else if ( operation instanceof PropertyIsCOMPOperation ) {
                Expression expr = ( (PropertyIsCOMPOperation) operation ).getFirstExpression();
                if ( expr instanceof PropertyName ) {
                    findNonDeclaredVariables( ( (PropertyName) expr ), variables );
                }
                expr = ( (PropertyIsCOMPOperation) operation ).getSecondExpression();
                if ( expr instanceof PropertyName ) {
                    findNonDeclaredVariables( ( (PropertyName) expr ), variables );
                }
            } else if ( operation instanceof PropertyIsInstanceOfOperation ) {
                findNonDeclaredVariables( ( (PropertyIsInstanceOfOperation) operation ).getPropertyName(), variables );
            } else if ( operation instanceof PropertyIsLikeOperation ) {
                findNonDeclaredVariables( ( (PropertyIsLikeOperation) operation ).getPropertyName(), variables );
            } else if ( operation instanceof PropertyIsNullOperation ) {
                findNonDeclaredVariables( ( (PropertyIsNullOperation) operation ).getPropertyName(), variables );
            }
        }
    }

    /**
     * Parse the string representation of the the propertyname to find a variable reference to a non
     * declared Variable.
     *
     * @param propName
     *            to check
     * @param variables
     *            which were declared
     * @throws InvalidParameterValueException
     *             if such a reference was found.
     */
    protected void findNonDeclaredVariables( PropertyName propName, Map<String, QualifiedName> variables )
                            throws OGCWebServiceException {
        String propertyPath = propName.toString();
        String[] foundVariables = StringTools.extractStrings( propertyPath, "$", "/" );
        if ( foundVariables != null && foundVariables.length > 0 ) {
            LOG.logDebug( "found following variables in properertyName: " + propName.toString() );
            for ( String var : foundVariables ) {
                LOG.logDebug( "variable: " + var );
                if ( !variables.containsKey( var ) ) {
                    throw new OGCWebServiceException( Messages.getMessage( "CSW_VARIABLE_NOT_DEFINED", var ),
                                                      ExceptionCode.INVALIDPARAMETERVALUE );
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.deegree.framework.xml.XMLFragment#createEmptyDocument()
     */
    void createEmptyDocument()
                            throws IOException, SAXException {
        URL url = GetRecordsDocument.class.getResource( XML_TEMPLATE );
        if ( url == null ) {
            throw new IOException( "The resource '" + XML_TEMPLATE + " could not be found." );
        }
        load( url );
    }
}
