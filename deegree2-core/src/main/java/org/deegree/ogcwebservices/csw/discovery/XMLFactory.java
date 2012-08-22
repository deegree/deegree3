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

import static org.deegree.ogcbase.CommonNamespaces.CSW202NS;
import static org.deegree.ogcbase.CommonNamespaces.CSWNS;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.deegree.datatypes.QualifiedName;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.StringTools;
import org.deegree.framework.util.TimeTools;
import org.deegree.framework.xml.XMLException;
import org.deegree.framework.xml.XMLTools;
import org.deegree.ogcbase.CommonNamespaces;
import org.deegree.ogcbase.PropertyPath;
import org.deegree.ogcbase.SortProperty;
import org.deegree.ogcwebservices.InvalidParameterValueException;
import org.deegree.ogcwebservices.OGCWebServiceException;
import org.deegree.ogcwebservices.csw.CSWExceptionCode;
import org.deegree.ogcwebservices.csw.CSWPropertiesAccess;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth </a>
 * @author <a href="mailto:mschneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class XMLFactory extends org.deegree.ogcbase.XMLFactory {

    private static final ILogger LOG = LoggerFactory.getLogger( XMLFactory.class );

    /**
     * Exports a <code>GetRecordsResponse</code> instance to a <code>GetRecordsResponseDocument</code>.
     * 
     * @param response
     * @return DOM representation of the <code>GetRecordsResponse</code>
     * @throws XMLException
     *             if XML template could not be loaded
     */
    public static GetRecordsResultDocument export( GetRecordsResult response )
                            throws XMLException {
        // 'version'-attribute
        String version = response.getRequest().getVersion();
        if ( version == null || "".equals( version.trim() ) ) {
            version = "2.0.0";
        }

        GetRecordsResultDocument responseDocument = new GetRecordsResultDocument( version );

        try {
            Element rootElement = responseDocument.getRootElement();
            Document doc = rootElement.getOwnerDocument();

            // set required namespaces
            Element recordRespRoot = response.getSearchResults().getRecords().getOwnerDocument().getDocumentElement();
            NamedNodeMap nnm = recordRespRoot.getAttributes();
            for ( int i = 0; i < nnm.getLength(); i++ ) {
                Node node = nnm.item( i );
                if ( node instanceof Attr ) {
                    rootElement.setAttribute( node.getNodeName(), node.getNodeValue() );
                }
            }

            rootElement.setAttribute( "version", version );
            String namespace = ( version.equals( "2.0.2" ) ? CSW202NS.toString() : CSWNS.toString() );

            // 'RequestId'-element (optional)
            if ( response.getRequest().getId() != null ) {
                Element requestIdElement = doc.createElementNS( namespace, "csw:RequestId" );
                requestIdElement.appendChild( doc.createTextNode( response.getRequest().getId() ) );
                rootElement.appendChild( requestIdElement );
            }

            // 'SearchStatus'-element (required)
            Element searchStatusElement = doc.createElementNS( namespace, "csw:SearchStatus" );
            // 'status'-attribute (required)
            if ( !version.equals( "2.0.2" ) ) {
                searchStatusElement.setAttribute( "status", response.getSearchStatus().getStatus() );
            }
            // 'timestamp'-attribute (optional)
            if ( response.getSearchStatus().getTimestamp() != null ) {
                Date date = response.getSearchStatus().getTimestamp();
                String time = TimeTools.getISOFormattedTime( date );
                searchStatusElement.setAttribute( "timestamp", time );
            }
            rootElement.appendChild( searchStatusElement );

            // 'SeachResults'-element (required)
            Element searchResultsElement = doc.createElementNS( namespace, "csw:SearchResults" );
            SearchResults results = response.getSearchResults();

            // 'resultSetId'-attribute (optional)
            if ( results.getResultSetId() != null ) {
                searchResultsElement.setAttribute( "resultSetId", results.getResultSetId().toString() );
            }
            // 'elementSet'-attribute (optional)
            if ( results.getElementSet() != null ) {
                searchResultsElement.setAttribute( "elementSet", results.getElementSet().toString() );
            }
            // 'recordSchema'-attribute (optional)
            if ( results.getRecordSchema() != null ) {
                searchResultsElement.setAttribute( "recordSchema", results.getRecordSchema().toString() );
            }
            // 'numberOfRecordsMatched'-attribute (required)
            searchResultsElement.setAttribute( "numberOfRecordsMatched", "" + results.getNumberOfRecordsMatched() );
            // 'numberOfRecordsReturned'-attribute (required)
            searchResultsElement.setAttribute( "numberOfRecordsReturned", "" + results.getNumberOfRecordsReturned() );
            // 'nextRecord'-attribute (required)
            searchResultsElement.setAttribute( "nextRecord", "" + results.getNextRecord() );
            // 'expires'-attribute (optional)
            if ( results.getExpires() != null ) {
                Date date = results.getExpires();
                String time = TimeTools.getISOFormattedTime( date );
                searchResultsElement.setAttribute( "expires", time );
            }
            // append all children of the records container node
            NodeList nl = results.getRecords().getChildNodes();
            for ( int i = 0; i < nl.getLength(); i++ ) {
                Node copy = doc.importNode( nl.item( i ), true );
                searchResultsElement.appendChild( copy );
            }
            rootElement.appendChild( searchResultsElement );
        } catch ( Exception e ) {
            LOG.logError( e.getMessage(), e );
            throw new XMLException( e.getMessage() );
        }
        return responseDocument;
    }

    /**
     * Exports a instance of {@link GetRecordByIdResult} to a {@link GetRecordByIdResultDocument}.
     * 
     * @param response
     * @return a new document
     * @throws XMLException
     */
    public static GetRecordByIdResultDocument export( GetRecordByIdResult response )
                            throws XMLException {

        GetRecordByIdResultDocument doc = new GetRecordByIdResultDocument();

        try {
            doc.createEmptyDocument( response.getRequest().getVersion() );
            Document owner = doc.getRootElement().getOwnerDocument();
            if ( response != null && response.getRecords() != null ) {
                for ( Node record : response.getRecords() ) {
                    Node copy = owner.importNode( record, true );
                    doc.getRootElement().appendChild( copy );
                }
            } else if ( "2.0.2".equals( response.getRequest().getVersion() )
                        && ( response == null || response.getRecord() == null ) ) {
                throw new OGCWebServiceException( "A record with the given ID does nor exist in the CSW",
                                                  CSWExceptionCode.INVALIDPARAMETERVALUE );
            }
        } catch ( Exception e ) {
            LOG.logError( e.getMessage(), e );
            throw new XMLException( e.getMessage() );
        }

        return doc;
    }

    /**
     * Exports a <code>DescribeRecordResponse</code> instance to a <code>DescribeRecordResponseDocument</code>.
     * 
     * @param response
     * @return DOM representation of the <code>DescribeRecordResponse</code>
     * @throws XMLException
     *             if XML template could not be loaded
     */
    public static DescribeRecordResultDocument export( DescribeRecordResult response )
                            throws XMLException {

        DescribeRecordResultDocument responseDocument = new DescribeRecordResultDocument();

        String ns = response.getRequest().getVersion().equals( "2.0.2" ) ? CSW202NS.toString() : CSWNS.toString();

        try {
            responseDocument.createEmptyDocument( response.getRequest().getVersion() );
            Element rootElement = responseDocument.getRootElement();
            Document doc = rootElement.getOwnerDocument();

            // 'SchemaComponent'-elements (required)
            SchemaComponent[] components = response.getSchemaComponents();
            for ( int i = 0; i < components.length; i++ ) {
                Element schemaComponentElement = doc.createElementNS( ns, "csw:SchemaComponent" );

                // 'targetNamespace'-attribute (required)
                schemaComponentElement.setAttribute( "targetNamespace", components[i].getTargetNamespace().toString() );

                // 'parentSchema'-attribute (optional)
                if ( components[i].getParentSchema() != null ) {
                    schemaComponentElement.setAttribute( "parentSchema", components[i].getParentSchema().toString() );
                }

                // 'schemaLanguage'-attribute (required)
                schemaComponentElement.setAttribute( "schemaLanguage", components[i].getSchemaLanguage().toString() );

                XMLTools.insertNodeInto( components[i].getSchema().getRootElement(), schemaComponentElement );
                rootElement.appendChild( schemaComponentElement );
            }
        } catch ( Exception e ) {
            LOG.logError( e.getMessage(), e );
            throw new XMLException( e.getMessage() );
        }
        return responseDocument;
    }

    /**
     * Exports a <code>GetRecords</code> instance to a <code>GetRecordsDocument</code>.
     * 
     * @param request
     * @return DOM representation of the <code>GetRecords</code>
     * @throws XMLException
     *             if some elements could not be appended
     * @throws OGCWebServiceException
     *             if an error occurred while creating the xml-representation of the GetRecords bean.
     */
    public static GetRecordsDocument exportWithVersion( GetRecords request )
                            throws XMLException, OGCWebServiceException {

        GetRecordsDocument getRecordsDocument = null;

        // read class for version depenging parsing of GetRecords request from properties
        String className = CSWPropertiesAccess.getString( "GetRecords" + request.getVersion() );
        Class<?> clzz = null;
        try {
            clzz = Class.forName( className );
        } catch ( ClassNotFoundException e ) {
            LOG.logError( e.getMessage(), e );
            throw new InvalidParameterValueException( e.getMessage(), e );
        }
        try {
            getRecordsDocument = (GetRecordsDocument) clzz.newInstance();
        } catch ( InstantiationException e ) {
            LOG.logError( e.getMessage(), e );
            throw new InvalidParameterValueException( e.getMessage(), e );
        } catch ( IllegalAccessException e ) {
            LOG.logError( e.getMessage(), e );
            throw new InvalidParameterValueException( e.getMessage(), e );
        }

        // getRecordsDocument = new GetRecordsDocument();
        try {
            getRecordsDocument.createEmptyDocument();
        } catch ( Exception e ) {
            throw new XMLException( e.getMessage() );
        }
        Element rootElement = getRecordsDocument.getRootElement();
        Document doc = rootElement.getOwnerDocument();

        // 'version'-attribute
        rootElement.setAttribute( "version", request.getVersion() );

        // 'version'-attribute
        rootElement.setAttribute( "service", "CSW" );

        // 'resultType'-attribute
        rootElement.setAttribute( "resultType", request.getResultTypeAsString() );

        // 'outputFormat'-attribute
        rootElement.setAttribute( "outputFormat", request.getOutputFormat() );

        // 'outputSchema'-attribute
        rootElement.setAttribute( "outputSchema", request.getOutputSchema() );

        // 'startPosition'-attribute
        rootElement.setAttribute( "startPosition", "" + request.getStartPosition() );

        // 'maxRecords'-attribute
        rootElement.setAttribute( "maxRecords", "" + request.getMaxRecords() );

        URI localCSWNS = CSWNS;
        if ( request.getVersion().equals( "2.0.2" ) ) {
            localCSWNS = CSW202NS;
        }

        // '<csw:DistributedSearch>'-element
        if ( request.getHopCount() != -1 ) {
            Element distributedSearchElement = doc.createElementNS( localCSWNS.toString(), "csw:DistributedSearch" );

            // 'hopCount'-attribute
            distributedSearchElement.setAttribute( "hopCount", "" + request.getHopCount() );
            rootElement.appendChild( distributedSearchElement );
        }

        // '<csw:ResponseHandler>'-elements (optional)
        URI responseHandler = request.getResponseHandler();
        if ( responseHandler != null ) {
            Element responseHandlerElement = doc.createElementNS( localCSWNS.toString(), "csw:ResponseHandler" );
            responseHandlerElement.appendChild( doc.createTextNode( responseHandler.toASCIIString() ) );
            rootElement.appendChild( responseHandlerElement );

        }

        // '<csw:Query>'-elements (required)
        Query query = request.getQuery();
        if ( query != null ) {
            LOG.logDebug( "Adding the csw:Query element to the csw:GetRecords document" );
            Element queryElement = doc.createElementNS( localCSWNS.toString(), "csw:Query" );

            // 'typeName'-attribute
            // Testing for the list of typenames.
            List<QualifiedName> typeNames = query.getTypeNamesAsList();
            Map<String, QualifiedName> aliases = new HashMap<String, QualifiedName>(
                                                                                     query.getDeclaredTypeNameVariables() );
            if ( typeNames.size() > 0 ) {
                appendTypeNamesAttribute( rootElement, queryElement, typeNames, aliases );
            } else {
                String s = StringTools.listToString( query.getTypeNamesAsList(), ',' );
                queryElement.setAttribute( "typeNames", s );
            }

            // '<csw:ElementSetName>'-element (optional)
            if ( query.getElementSetName() != null ) {
                Element elementSetNameElement = doc.createElementNS( localCSWNS.toString(), "csw:ElementSetName" );
                List<QualifiedName> elementSetNameTypeNamesList = query.getElementSetNameTypeNamesList();
                if ( query.getElementSetNameVariables() != null && query.getElementSetNameVariables().size() > 0 ) {
                    throw new OGCWebServiceException(
                                                      "The elementSetName element in a csw:GetRecords request may not refrerence variables (aka. aliases), aborting request" );
                }
                if ( elementSetNameTypeNamesList.size() > 0 ) {
                    appendTypeNamesAttribute( rootElement, elementSetNameElement, elementSetNameTypeNamesList, null );
                }
                elementSetNameElement.appendChild( doc.createTextNode( query.getElementSetName() ) );
                queryElement.appendChild( elementSetNameElement );
            }

            // '<csw:ElementName>'-elements (optional)
            if ( query.getElementNamesAsPropertyPaths() != null ) {
                List<PropertyPath> elementNames = query.getElementNamesAsPropertyPaths();
                for ( int j = 0; j < elementNames.size(); j++ ) {
                    Element elementNameElement = doc.createElementNS( localCSWNS.toString(), "csw:ElementName" );
                    elementNameElement.appendChild( doc.createTextNode( elementNames.get( j ).getAsString() ) );
                    queryElement.appendChild( elementNameElement );
                }
            }

            // '<csw:Constraint>'-element (optional)
            if ( query.getContraint() != null ) {
                Element constraintElement = doc.createElementNS( localCSWNS.toString(), "csw:Constraint" );
                constraintElement.setAttribute( "version", "1.1.0" );
                org.deegree.model.filterencoding.XMLFactory.appendFilter( constraintElement, query.getContraint() );
                queryElement.appendChild( constraintElement );
            }

            // '<ogc:SortBy>'-element (optional)
            SortProperty[] sortProperties = query.getSortProperties();
            if ( sortProperties != null && sortProperties.length != 0 ) {
                Element sortByElement = doc.createElementNS( OGCNS.toString(), "ogc:SortBy" );

                // '<ogc:SortProperty>'-elements
                for ( int j = 0; j < sortProperties.length; j++ ) {
                    Element sortPropertiesElement = doc.createElementNS( OGCNS.toString(), "ogc:SortProperty" );

                    // '<ogc:PropertyName>'-element (required)
                    Element propertyNameElement = doc.createElementNS( OGCNS.toString(), "ogc:PropertyName" );
                    appendPropertyPath( propertyNameElement, sortProperties[j].getSortProperty() );

                    // '<ogc:SortOrder>'-element (optional)
                    Element sortOrderElement = doc.createElementNS( OGCNS.toString(), "ogc:SortOrder" );
                    Node tn = doc.createTextNode( sortProperties[j].getSortOrder() ? "ASC" : "DESC" );
                    sortOrderElement.appendChild( tn );

                    sortPropertiesElement.appendChild( propertyNameElement );
                    sortPropertiesElement.appendChild( sortOrderElement );
                    sortByElement.appendChild( sortPropertiesElement );
                }
                queryElement.appendChild( sortByElement );
            }
            rootElement.appendChild( queryElement );
        }
        return getRecordsDocument;
    }

    /**
     * Exports a <code>GetRecords</code> instance to a <code>GetRecordsDocument</code>.
     * 
     * @param request
     * @return DOM representation of the <code>GetRecords</code>
     * @throws XMLException
     *             if some elements could not be appended
     * @throws OGCWebServiceException
     *             if an error occurred while creating the xml-representation of the GetRecords bean.
     */
    public static GetRecordsDocument export( GetRecords request )
                            throws XMLException, OGCWebServiceException {

        GetRecordsDocument getRecordsDocument = null;
        
        getRecordsDocument = new GetRecordsDocument();
        try {
            getRecordsDocument.createEmptyDocument();
        } catch ( Exception e ) {
            throw new XMLException( e.getMessage() );
        }
        Element rootElement = getRecordsDocument.getRootElement();
        Document doc = rootElement.getOwnerDocument();

        // 'version'-attribute
        rootElement.setAttribute( "version", request.getVersion() );

        // 'resultType'-attribute
        rootElement.setAttribute( "resultType", request.getResultTypeAsString() );

        // 'outputFormat'-attribute
        rootElement.setAttribute( "outputFormat", request.getOutputFormat() );

        // 'outputSchema'-attribute
        rootElement.setAttribute( "outputSchema", request.getOutputSchema() );

        // 'startPosition'-attribute
        rootElement.setAttribute( "startPosition", "" + request.getStartPosition() );

        // 'maxRecords'-attribute
        rootElement.setAttribute( "maxRecords", "" + request.getMaxRecords() );

        // '<csw:DistributedSearch>'-element
        if ( request.getHopCount() != -1 ) {
            Element distributedSearchElement = doc.createElementNS( CSWNS.toString(), "csw:DistributedSearch" );

            // 'hopCount'-attribute
            distributedSearchElement.setAttribute( "hopCount", "" + request.getHopCount() );
            rootElement.appendChild( distributedSearchElement );
        }

        // '<csw:ResponseHandler>'-elements (optional)
        URI responseHandler = request.getResponseHandler();
        if ( responseHandler != null ) {
            Element responseHandlerElement = doc.createElementNS( CSWNS.toString(), "csw:ResponseHandler" );
            responseHandlerElement.appendChild( doc.createTextNode( responseHandler.toASCIIString() ) );
            rootElement.appendChild( responseHandlerElement );

        }

        // '<csw:Query>'-elements (required)
        Query query = request.getQuery();
        if ( query != null ) {
            LOG.logDebug( "Adding the csw:Query element to the csw:GetRecords document" );
            Element queryElement = doc.createElementNS( CSWNS.toString(), "csw:Query" );

            // 'typeName'-attribute
            // Testing for the list of typenames.
            List<QualifiedName> typeNames = query.getTypeNamesAsList();
            Map<String, QualifiedName> aliases = new HashMap<String, QualifiedName>(
                                                                                     query.getDeclaredTypeNameVariables() );
            if ( typeNames.size() > 0 ) {
                appendTypeNamesAttribute( rootElement, queryElement, typeNames, aliases );
            } else {

                String s = StringTools.arrayToString( query.getTypeNames(), ',' );
                queryElement.setAttribute( "typeNames", s );
            }

            // '<csw:ElementSetName>'-element (optional)
            if ( query.getElementSetName() != null ) {
                Element elementSetNameElement = doc.createElementNS( CSWNS.toString(), "csw:ElementSetName" );
                List<QualifiedName> elementSetNameTypeNamesList = query.getElementSetNameTypeNamesList();
                if ( query.getElementSetNameVariables() != null && query.getElementSetNameVariables().size() > 0 ) {
                    throw new OGCWebServiceException(
                                                      "The elementSetName element in a csw:GetRecords request may not refrerence variables (aka. aliases), aborting request" );
                }
                if ( elementSetNameTypeNamesList.size() > 0 ) {
                    appendTypeNamesAttribute( rootElement, elementSetNameElement, elementSetNameTypeNamesList, null );
                }
                elementSetNameElement.appendChild( doc.createTextNode( query.getElementSetName() ) );
                queryElement.appendChild( elementSetNameElement );
            }

            // '<csw:ElementName>'-elements (optional)
            if ( query.getElementNamesAsPropertyPaths() != null ) {
                List<PropertyPath> elementNames = query.getElementNamesAsPropertyPaths();
                for ( int j = 0; j < elementNames.size(); j++ ) {
                    Element elementNameElement = doc.createElementNS( CSWNS.toString(), "csw:ElementName" );
                    elementNameElement.appendChild( doc.createTextNode( elementNames.get( j ).getAsString() ) );
                    queryElement.appendChild( elementNameElement );
                }
            }

            // '<csw:Constraint>'-element (optional)
            if ( query.getContraint() != null ) {
                Element constraintElement = doc.createElementNS( CSWNS.toString(), "csw:Constraint" );
                constraintElement.setAttribute( "version", "1.1.0" );
                org.deegree.model.filterencoding.XMLFactory.appendFilter( constraintElement, query.getContraint() );
                queryElement.appendChild( constraintElement );
            }

            // '<ogc:SortBy>'-element (optional)
            SortProperty[] sortProperties = query.getSortProperties();
            if ( sortProperties != null && sortProperties.length != 0 ) {
                Element sortByElement = doc.createElementNS( OGCNS.toString(), "ogc:SortBy" );

                // '<ogc:SortProperty>'-elements
                for ( int j = 0; j < sortProperties.length; j++ ) {
                    Element sortPropertiesElement = doc.createElementNS( OGCNS.toString(), "ogc:SortProperty" );

                    // '<ogc:PropertyName>'-element (required)
                    Element propertyNameElement = doc.createElementNS( OGCNS.toString(), "ogc:PropertyName" );
                    appendPropertyPath( propertyNameElement, sortProperties[j].getSortProperty() );

                    // '<ogc:SortOrder>'-element (optional)
                    Element sortOrderElement = doc.createElementNS( OGCNS.toString(), "ogc:SortOrder" );
                    Node tn = doc.createTextNode( sortProperties[j].getSortOrder() ? "ASC" : "DESC" );
                    sortOrderElement.appendChild( tn );

                    sortPropertiesElement.appendChild( propertyNameElement );
                    sortPropertiesElement.appendChild( sortOrderElement );
                    sortByElement.appendChild( sortPropertiesElement );
                }
                queryElement.appendChild( sortByElement );
            }
            rootElement.appendChild( queryElement );
        }
        return getRecordsDocument;
    }

    /**
     * 
     * @param rootElement
     *            the first node of the Docuement
     * @param toBeInserted
     *            to which the typeNames attribute will be appended
     * @param typeNames
     *            to be inserted into the toBeInserted element
     * @param aliases
     *            may be <code>null</code>, if not each typeName must have exactly one alias defined, which will be
     *            inserted after the typename (e.g. typename=$o). If map must contain a mapping from variable to
     *            qualifiedName (e.g. [o, typeName]);
     */
    protected static void appendTypeNamesAttribute( Element rootElement, Element toBeInserted,
                                                    List<QualifiedName> typeNames, Map<String, QualifiedName> aliases ) {
        if ( !typeNames.isEmpty() ) {
            for ( QualifiedName qName : typeNames ) {
                LOG.logDebug( "found typeName: " + qName );
            }
            LOG.logDebug( "for the element: " + toBeInserted.getNodeName()
                          + " we are trying to set the typeNames attribute." );
            StringBuffer sb = new StringBuffer();
            int count = 0;
            for ( QualifiedName qName : typeNames ) {
                if ( qName.getLocalName() != null ) {
                    URI ns = qName.getNamespace();
                    String prefix = qName.getPrefix();
                    if ( ns != null && prefix != null ) {
                        URI boundNS = null;
                        try {
                            boundNS = XMLTools.getNamespaceForPrefix( prefix, toBeInserted );
                        } catch ( URISyntaxException e ) {
                            // why for crying out loud an UriSyntax exception while lookin up stuff
                            // (without giving
                            // an
                            // uri).
                        }
                        LOG.logDebug( "ElementSetName/@typeNames: Found the namespace " + boundNS + " for the prefix: "
                                      + prefix + " from typename (localname) : " + qName.getLocalName() );
                        if ( boundNS == null ) {
                            if ( CommonNamespaces.OASIS_EBRIMNS.equals( ns ) ) {
                                XMLTools.appendNSBinding( rootElement, "rim", ns );
                                LOG.logDebug( toBeInserted.getLocalName()
                                              + "/@typeName: While no namespace was bound to the prefix: " + prefix
                                              + " the namespace: " + ns
                                              + " has been bound to 'rim' in the the root element." );
                            } else {
                                XMLTools.appendNSBinding( rootElement, prefix, ns );
                                LOG.logDebug( toBeInserted.getLocalName()
                                              + "/@typeName: While no namespace was bound to the prefix: " + prefix
                                              + " the namespace: " + ns + " has been bound to '" + prefix
                                              + "' in the the root element." );
                            }

                        }
                    }
                    String typeName = prefix;
                    if ( typeName != null ) {
                        typeName += ":" + qName.getLocalName();
                    }
                    sb.append( typeName );
                    if ( aliases != null ) {
                        if ( aliases.containsValue( qName ) ) {
                            Set<String> keys = aliases.keySet();
                            for ( String key : keys ) {
                                if ( aliases.get( key ).equals( qName ) ) {
                                    sb.append( "=" ).append( key );
                                    aliases.remove( key );
                                    break;
                                }
                            }
                        } else if ( aliases.size() > 0 ) {
                            LOG.logError( "No variable mapping found for typename: "
                                          + typeName
                                          + " this may not be, because every single typename must or no typename may have a variable!" );
                        }
                    }
                    if ( ++count < typeNames.size() ) {
                        sb.append( " " );
                    }
                }
            }
            if ( !"null".equals( sb.toString().trim() ) && !"".equals( sb.toString().trim() ) ) {
                LOG.logDebug( "for the element: " + toBeInserted.getNodeName()
                              + " we are settin the typeNames attribute to: " + sb.toString() );
                toBeInserted.setAttribute( "typeNames", sb.toString() );
            }
        }
    }

    /**
     * Exports a <code>GetRecordById</code> instance to a <code>GetRecordByIdDocument</code>.
     * 
     * @param request
     * @return DOM representation of the <code>GetRecordById</code>
     * @throws XMLException
     *             if XML template could not be loaded
     */
    public static GetRecordByIdDocument export( GetRecordById request )
                            throws XMLException {

        GetRecordByIdDocument getRecordByIdDoc = new GetRecordByIdDocument();
        try {
            getRecordByIdDoc.createEmptyDocument();
        } catch ( Exception e ) {
            throw new XMLException( e.getMessage() );
        }
        Element rootElement = getRecordByIdDoc.getRootElement();
        Document doc = rootElement.getOwnerDocument();

        // 'version'-attribute
        rootElement.setAttribute( "version", request.getVersion() );

        String[] ids = request.getIds();
        for ( int i = 0; i < ids.length; i++ ) {
            Element idElement = doc.createElementNS( CSWNS.toString(), "csw:Id" );
            idElement.appendChild( doc.createTextNode( ids[i] ) );
            rootElement.appendChild( idElement );
        }

        String elementSetName = request.getElementSetName();
        if ( elementSetName != null ) {
            Element esnElement = doc.createElementNS( CSWNS.toString(), "csw:ElementSetName" );
            esnElement.appendChild( doc.createTextNode( elementSetName ) );
            rootElement.appendChild( esnElement );
        }

        return getRecordByIdDoc;
    }
}
