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
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.framework.xml.XMLTools;
import org.deegree.i18n.Messages;
import org.deegree.model.filterencoding.AbstractFilter;
import org.deegree.model.filterencoding.ComplexFilter;
import org.deegree.model.filterencoding.Filter;
import org.deegree.model.filterencoding.FilterConstructionException;
import org.deegree.ogcbase.CommonNamespaces;
import org.deegree.ogcbase.ExceptionCode;
import org.deegree.ogcbase.PropertyPath;
import org.deegree.ogcbase.PropertyPathFactory;
import org.deegree.ogcbase.SortProperty;
import org.deegree.ogcwebservices.InvalidParameterValueException;
import org.deegree.ogcwebservices.MissingParameterValueException;
import org.deegree.ogcwebservices.OGCWebServiceException;
import org.deegree.ogcwebservices.OperationNotSupportedException;
import org.deegree.ogcwebservices.csw.discovery.GetRecords.RESULT_TYPE;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 *
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class GetRecordsDocument_2_0_2 extends GetRecordsDocument {

    private static final long serialVersionUID = -4987866178075594539L;

    private static final ILogger LOG = LoggerFactory.getLogger( GetRecordsDocument_2_0_2.class );

    private static final String XML_TEMPLATE = "GetRecords2.0.2Template.xml";

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
    @Override
    public GetRecords parse( String id )
                            throws OGCWebServiceException {

        // '<csw202:GetRecords>'-element (required)
        try {
            Element contextNode = (Element) XMLTools.getRequiredNode( this.getRootElement(), "self::csw202:GetRecords",
                                                                      nsContext );
            // 'service'-attribute (mandatory for 2.0.2, must be CSW)
            String service = XMLTools.getRequiredNodeAsString( contextNode, "@service", nsContext );
            if ( !"CSW".equals( service ) ) {
                throw new OGCWebServiceException( "GetRecordsDocument_2_0_2",
                                                  Messages.getMessage( "CSW_INVALID_SERVICE_PARAM" ),
                                                  ExceptionCode.INVALIDPARAMETERVALUE );
            }

            boolean isEBRIM = ( contextNode.getOwnerDocument().lookupPrefix(
                                                                             CommonNamespaces.OASIS_EBRIMNS.toASCIIString() ) != null );

            // 'version'-attribute (mandatory for 2.0.2)
            String version = XMLTools.getRequiredNodeAsString( contextNode, "@version", nsContext );
            if ( !"2.0.2".equals( version ) ) {
                throw new OGCWebServiceException( "GetRecordsDocument_2_0_2",
                                                  Messages.getMessage( "CSW_NOT_SUPPORTED_VERSION",
                                                                       GetRecords.DEFAULT_VERSION, "2.0.2", version ),
                                                  ExceptionCode.INVALIDPARAMETERVALUE );
            }

            // 'requestId'-attribute (optional)
            String requestId = XMLTools.getNodeAsString( contextNode, "@requestId", nsContext, id );

            // 'resultType'-attribute
            // type="csw202:ResultType" use="optional" default="hits"
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

            String defaultOutputSchema = GetRecords.DEFAULT_OUTPUTSCHEMA_202;
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

            // '<csw202:DistributedSearch>'-element (optional)
            Node distributedSearchElement = XMLTools.getNode( contextNode, "csw202:DistributedSearch", nsContext );
            int hopCount = GetRecords.DEFAULT_HOPCOUNT;
            if ( distributedSearchElement != null ) {
                hopCount = XMLTools.getNodeAsInt( contextNode, "@hopCount", nsContext, GetRecords.DEFAULT_HOPCOUNT );
            }

            // '<csw202:ResponseHandler>'-elements (optional)
            String rHandler = XMLTools.getNodeAsString( contextNode, "csw202:ResponseHandler", nsContext, null );
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

            // '<csw202:Query>'-elements (required)
            // List nl = XMLTools.getRequiredNodes( contextNode, "csw202:Query", nsContext );
            Element queryNode = (Element) XMLTools.getRequiredNode( contextNode, "csw202:Query", nsContext );

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

            // '<csw202:ElementSetName>'-element (optional)
            Element elementSetNameElement = (Element) XMLTools.getNode( queryNode, "csw202:ElementSetName", nsContext );
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
                                String msg = Messages.getMessage( "CSW_ELEMENT_SET_NAME_TYPENAME_ALIAS", tmpVar );
                                throw new OGCWebServiceException( msg, ExceptionCode.INVALIDPARAMETERVALUE );
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
                // '<csw202:ElementName>'-element (required, if no
                // '<csw202:ElementSetName>' is given)
                List<Node> elementNameList = XMLTools.getNodes( queryNode, "csw202:ElementName", nsContext );
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

            // '<csw202:Constraint>'-element (optional)
            Element constraintElement = (Element) XMLTools.getNode( queryNode, "csw202:Constraint", nsContext );
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
                    String cqlText = XMLTools.getNodeAsString( constraintElement, "csw202:CqlText", nsContext, null );
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
            throw new OGCWebServiceException( xmlpe.getMessage(), ExceptionCode.MISSINGPARAMETERVALUE );
        } catch ( URISyntaxException urise ) {
            LOG.logError( "CatalogGetRecords", urise );
            throw new OGCWebServiceException( urise.getMessage(), ExceptionCode.MISSINGPARAMETERVALUE );
        }
    }

    @Override
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
