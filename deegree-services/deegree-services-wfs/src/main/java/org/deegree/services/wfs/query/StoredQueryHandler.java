//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2011 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -

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
package org.deegree.services.wfs.query;

import static org.deegree.commons.ows.exception.OWSException.DUPLICATE_STORED_QUERY_ID_VALUE;
import static org.deegree.commons.ows.exception.OWSException.INVALID_PARAMETER_VALUE;
import static org.deegree.commons.ows.exception.OWSException.OPERATION_PROCESSING_FAILED;
import static org.deegree.protocol.wfs.WFSConstants.WFS_200_NS;
import static org.deegree.protocol.wfs.WFSConstants.WFS_200_SCHEMA_URL;
import static org.deegree.services.wfs.WebFeatureService.getXMLResponseWriter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.commons.ows.exception.OWSException;
import org.deegree.commons.tom.ows.LanguageString;
import org.deegree.commons.xml.stax.IndentingXMLStreamWriter;
import org.deegree.feature.types.FeatureType;
import org.deegree.protocol.wfs.storedquery.CreateStoredQuery;
import org.deegree.protocol.wfs.storedquery.DescribeStoredQueries;
import org.deegree.protocol.wfs.storedquery.DropStoredQuery;
import org.deegree.protocol.wfs.storedquery.ListStoredQueries;
import org.deegree.protocol.wfs.storedquery.Parameter;
import org.deegree.protocol.wfs.storedquery.QueryExpressionText;
import org.deegree.protocol.wfs.storedquery.StoredQueryDefinition;
import org.deegree.protocol.wfs.storedquery.xml.StoredQueryDefinition200Encoder;
import org.deegree.protocol.wfs.storedquery.xml.StoredQueryDefinitionXMLAdapter;
import org.deegree.services.controller.utils.HttpResponseBuffer;
import org.deegree.services.wfs.WebFeatureService;
import org.deegree.workspace.ResourceInitException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles {@link CreateStoredQuery}, {@link DescribeStoredQueries}, {@link DropStoredQuery} and
 * {@link ListStoredQueries} requests for the {@link WebFeatureService}.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class StoredQueryHandler {

    private static final Logger LOG = LoggerFactory.getLogger( StoredQueryHandler.class );

    public static final String LANGUAGE_WFS_QUERY_EXPRESSION = "urn:ogc:def:queryLanguage:OGC-WFS::WFSQueryExpression";

    public static final String GET_FEATURE_BY_ID = "urn:ogc:def:query:OGC-WFS::GetFeatureById";

    public static final String GET_FEATURE_BY_TYPE = "urn:ogc:def:query:OGC-WFS::GetFeatureByType";

    private final Map<String, StoredQueryDescription> idToQuery = Collections.synchronizedMap( new TreeMap<String, StoredQueryDescription>() );

    private final WebFeatureService wfs;

    private final File managedStoredQueryDirectory;

    private boolean supportsManagedStoredQuery;

    /**
     * @param wfs
     *            never <code>null</code>
     * @param storedQueryTemplates
     *            the configured stored query templates, may be empty but never <code>null</code>
     * @param managedStoredQueryDirectory
     *            directory to store the stored queries created by CreateStoredQuery request, may be <code>null</code>
     *            and must not exist (the operations CreateStoredQuery and DropStoredQuery are not supported then).
     */
    public StoredQueryHandler( WebFeatureService wfs, List<URL> storedQueryTemplates,
                               File managedStoredQueryDirectory ) {
        this.wfs = wfs;
        this.managedStoredQueryDirectory = managedStoredQueryDirectory;
        this.supportsManagedStoredQuery = supportsManagedStoredQuery( managedStoredQueryDirectory );
        loadFixStoredQueries();
        loadConfiguredStoredQueries( storedQueryTemplates );
        loadManagedStoredQueries( managedStoredQueryDirectory );
    }

    /**
     * @return <code>true</code> if supported, <code>false</code> otherwise
     */
    public boolean isManagedStoredQuerySupported() {
        return supportsManagedStoredQuery;
    }

    /**
     * Performs the given {@link CreateStoredQuery} request.
     * 
     * @param request
     *            request to be handled, must not be <code>null</code>
     * @param response
     *            response that is used to write the result, must not be <code>null</code>
     * @throws IOException
     * @throws XMLStreamException
     * @throws OWSException
     */
    public void doCreateStoredQuery( CreateStoredQuery request, HttpResponseBuffer response )
                            throws IOException, XMLStreamException, OWSException {
        if ( managedStoredQueryDirectory == null )
            throw new OWSException( "Performing CreateStoredQuery requests is not configured.",
                                    OPERATION_PROCESSING_FAILED );
        if ( !managedStoredQueryDirectory.exists() )
            throw new OWSException( "Performing CreateStoredQuery requests is not configured.",
                                    OPERATION_PROCESSING_FAILED );

        checkIdsOfStoredQueries( request );
        checkLanguageOfStoredQueries( request );
        handleCreateStoredQuery( request );
        writeCreateStoredQueryResponse( response );
    }

    /**
     * Performs the given {@link DescribeStoredQueries} request.
     * 
     * @param request
     *            request to be handled, must not be <code>null</code>
     * @param response
     *            response that is used to write the result, must not be <code>null</code>
     * @throws IOException
     * @throws XMLStreamException
     * @throws OWSException
     */
    public void doDescribeStoredQueries( DescribeStoredQueries request, HttpResponseBuffer response )
                            throws XMLStreamException, IOException, OWSException {

        List<StoredQueryDefinition> returnedDescriptions = new ArrayList<StoredQueryDefinition>();
        if ( request.getStoredQueryIds().length == 0 ) {
            for ( StoredQueryDescription storedQueryDescription : idToQuery.values() ) {
                returnedDescriptions.add( storedQueryDescription.definition );

            }
        } else {
            for ( String id : request.getStoredQueryIds() ) {
                StoredQueryDescription queryDescription = idToQuery.get( id );
                if ( queryDescription == null ) {
                    String msg = "No StoredQuery with id '" + id + "' is known to this server.";
                    throw new OWSException( msg, INVALID_PARAMETER_VALUE );
                }
                returnedDescriptions.add( queryDescription.definition );
            }
        }

        String schemaLocation = WFS_200_NS + " " + WFS_200_SCHEMA_URL;
        XMLStreamWriter writer = getXMLResponseWriter( response, "text/xml", schemaLocation );
        writer.setDefaultNamespace( WFS_200_NS );
        writer.writeStartElement( WFS_200_NS, "DescribeStoredQueriesResponse" );
        writer.writeDefaultNamespace( WFS_200_NS );
        for ( StoredQueryDefinition queryDef : returnedDescriptions ) {
            writer.writeStartElement( WFS_200_NS, "StoredQueryDescription" );
            writer.writeAttribute( "id", queryDef.getId() );
            for ( LanguageString title : queryDef.getTitles() ) {
                writer.writeStartElement( WFS_200_NS, "Title" );
                if ( title.getLanguage() != null ) {
                    // check this
                    writer.writeAttribute( "xml:lang", title.getLanguage() );
                }
                writer.writeCharacters( title.getString() );
                writer.writeEndElement();
            }
            for ( LanguageString abstr : queryDef.getAbstracts() ) {
                writer.writeStartElement( WFS_200_NS, "Abstract" );
                if ( abstr.getLanguage() != null ) {
                    // check this
                    writer.writeAttribute( "xml:lang", abstr.getLanguage() );
                }
                writer.writeCharacters( abstr.getString() );
                writer.writeEndElement();
            }

            // TODO <xsd:element ref="ows:Metadata" minOccurs="0" maxOccurs="unbounded"/>

            // <xsd:element name="Parameter" type="wfs:ParameterExpressionType" minOccurs="0"
            // maxOccurs="unbounded"/>
            for ( Parameter parameter : queryDef.getParameters() ) {
                writer.writeStartElement( WFS_200_NS, "Parameter" );

                // <xsd:attribute name="name" type="xsd:string" use="required"/>
                writer.writeAttribute( "name", parameter.getName() );

                // <xsd:attribute name="type" type="xsd:QName" use="required"/>
                QName type = parameter.getType();
                String prefixedName = type.getLocalPart();
                if ( type.getPrefix() != null ) {
                    prefixedName = type.getPrefix() + ":" + type.getLocalPart();
                    writer.writeNamespace( type.getPrefix(), type.getNamespaceURI() );
                }
                writer.writeAttribute( "type", prefixedName );

                // <xsd:element ref="wfs:Title" minOccurs="0" maxOccurs="unbounded"/>
                for ( LanguageString title : parameter.getTitles() ) {
                    writer.writeStartElement( WFS_200_NS, "Title" );
                    if ( title.getLanguage() != null ) {
                        // check this
                        writer.writeAttribute( "xml:lang", title.getLanguage() );
                    }
                    writer.writeCharacters( title.getString() );
                    writer.writeEndElement();
                }

                // <xsd:element ref="wfs:Abstract" minOccurs="0" maxOccurs="unbounded"/>
                for ( LanguageString abstr : parameter.getAbstracts() ) {
                    writer.writeStartElement( WFS_200_NS, "Abstract" );
                    if ( abstr.getLanguage() != null ) {
                        // check this
                        writer.writeAttribute( "xml:lang", abstr.getLanguage() );
                    }
                    writer.writeCharacters( abstr.getString() );
                    writer.writeEndElement();
                }

                // <xsd:element ref="ows:Metadata" minOccurs="0" maxOccurs="unbounded"/>

                writer.writeEndElement();
            }

            // <xsd:element name="QueryExpressionText" type="wfs:QueryExpressionTextType" minOccurs="1"
            // maxOccurs="unbounded"/>
            List<QueryExpressionText> queryExprTexts = queryDef.getQueryExpressionTextEls();
            for ( QueryExpressionText queryExprText : queryExprTexts ) {
                writer.writeStartElement( WFS_200_NS, "QueryExpressionText" );

                // <xsd:attribute name="returnFeatureTypes" type="wfs:ReturnFeatureTypesListType" use="required"/>
                List<QName> returnFeatureTypes = queryExprText.getReturnFeatureTypes();
                writer.writeAttribute( "returnFeatureTypes",
                                       collectReturnFeatureTypesAndTransformToString( writer, returnFeatureTypes ) );

                writer.writeAttribute( "language", queryExprText.getLanguage() );
                if ( queryExprText.isPrivate() ) {
                    writer.writeAttribute( "isPrivate", "true" );
                } else {
                    // TODO export actual query expression
                    // XMLStreamReader reader = queryDef.getRootElement().getXMLStreamReader();
                    // skipStartDocument( reader );
                    // writeElement( writer, reader );
                }
                writer.writeEndElement();
            }
            writer.writeEndElement();
        }
        writer.writeEndElement();
    }

    /**
     * Performs the given {@link DropStoredQuery} request.
     * 
     * @param request
     *            request to be handled, must not be <code>null</code>
     * @param response
     *            response that is used to write the result, must not be <code>null</code>
     * @throws OWSException
     * @throws URISyntaxException
     * @throws IOException
     * @throws XMLStreamException
     */
    public void doDropStoredQuery( DropStoredQuery request, HttpResponseBuffer response )
                            throws OWSException, URISyntaxException, XMLStreamException, IOException {
        checkIdOfDropStoredQueryRequest( request );
        handleDropStoredQuery( request );
        writeDropStoredQueryResponse( response );
    }

    /**
     * Performs the given {@link ListStoredQueries} request.
     * 
     * @param request
     *            request to be handled, must not be <code>null</code>
     * @param response
     *            response that is used to write the result, must not be <code>null</code>
     * @throws IOException
     * @throws XMLStreamException
     */
    public void doListStoredQueries( ListStoredQueries request, HttpResponseBuffer response )
                            throws XMLStreamException, IOException {
        String schemaLocation = WFS_200_NS + " " + WFS_200_SCHEMA_URL;
        XMLStreamWriter writer = getXMLResponseWriter( response, "text/xml", schemaLocation );
        writer.setDefaultNamespace( WFS_200_NS );
        writer.writeStartElement( WFS_200_NS, "ListStoredQueriesResponse" );
        writer.writeDefaultNamespace( WFS_200_NS );

        for ( StoredQueryDescription queryDescription : idToQuery.values() ) {
            StoredQueryDefinition queryDef = queryDescription.definition;
            writer.writeStartElement( WFS_200_NS, "StoredQuery" );
            writer.writeAttribute( "id", queryDef.getId() );
            for ( LanguageString title : queryDef.getTitles() ) {
                writer.writeStartElement( WFS_200_NS, "Title" );
                if ( title.getLanguage() != null ) {
                    writer.writeAttribute( "xml:lang", title.getLanguage() );
                }
                writer.writeCharacters( title.getString() );
                writer.writeEndElement();

                List<QName> configuredReturnFeatureTypes = collectFeatureTypes( queryDef );
                List<QName> ftNames = collectAndSortFeatureTypesToExport( configuredReturnFeatureTypes );
                for ( QName ftName : ftNames ) {
                    writer.writeStartElement( WFS_200_NS, "ReturnFeatureType" );
                    String prefix = ftName.getPrefix();
                    if ( prefix == null && ftName.getNamespaceURI() != null ) {
                        prefix = "app";
                    }
                    if ( ftName.getPrefix() != null ) {
                        writer.writeNamespace( prefix, ftName.getNamespaceURI() );
                        writer.writeCharacters( prefix + ":" + ftName.getLocalPart() );
                    } else {
                        writer.writeCharacters( ftName.getLocalPart() );
                    }
                    writer.writeEndElement();
                }
            }
            writer.writeEndElement();
        }
        writer.writeEndElement();
    }

    /**
     * @param id
     * @return true, if the stored query is known
     */
    public boolean hasStoredQuery( String id ) {
        return idToQuery.get( id ) != null;
    }

    /**
     * @param id
     * @return <code>null</code>, if the stored query could not be found
     */
    public URL getStoredQueryTemplate( String id ) {
        return idToQuery.get( id ) != null ? idToQuery.get( id ).url : null;
    }

    List<QName> collectAndSortFeatureTypesToExport( List<QName> configuredReturnFeatureTypes ) {
        Collection<FeatureType> featureTypes = wfs.getStoreManager().getFeatureTypes();
        List<QName> ftNames = collectFeatureTypes( configuredReturnFeatureTypes, featureTypes );
        Collections.sort( ftNames, new Comparator<QName>() {
            @Override
            public int compare( QName arg0, QName arg1 ) {
                String s0 = arg0.toString();
                String s1 = arg1.toString();
                return s0.compareTo( s1 );
            }
        } );
        return ftNames;
    }

    private void loadFixStoredQueries() {
        parseAndAddStoredQuery( StoredQueryHandler.class.getResource( "idquery.xml" ), false );
        parseAndAddStoredQuery( StoredQueryHandler.class.getResource( "typequery.xml" ), false );
    }

    private void loadConfiguredStoredQueries( List<URL> storedQueryTemplates ) {
        for ( URL u : storedQueryTemplates ) {
            parseAndAddStoredQuery( u, false );
        }
    }

    private void loadManagedStoredQueries( File managedStoredQueryDirectory ) {
        if ( !supportsManagedStoredQuery ) {
            LOG.warn( "Managed stored query directory does not exist. "
                      + "CreateStoredQuery/DropStoredQuery requests cannot be processed." );
            return;
        }
        for ( File managedStoredQuery : managedStoredQueryDirectory.listFiles() ) {
            try {
                URL url = managedStoredQuery.toURI().toURL();
                parseAndAddStoredQuery( url, true );
            } catch ( IOException e ) {
                throw new ResourceInitException( "Error initializing managed stored query from " + managedStoredQuery
                                                 + ":" + e.getMessage(), e );
            }
        }
    }

    private boolean supportsManagedStoredQuery( File managedStoredQueryDirectory ) {
        return managedStoredQueryDirectory != null && managedStoredQueryDirectory.exists();
    }

    private void handleCreateStoredQuery( CreateStoredQuery request )
                            throws XMLStreamException, FactoryConfigurationError, IOException {
        List<StoredQueryDefinition> queryDefinitionsToAdd = request.getQueryDefinitions();
        for ( StoredQueryDefinition storedQueryDefinitionToAdd : queryDefinitionsToAdd ) {
            addManagedStoredQueryDefinition( storedQueryDefinitionToAdd );
        }
    }

    private void handleDropStoredQuery( DropStoredQuery request )
                            throws OWSException {
        String storedQueryId = request.getStoredQueryId();
        StoredQueryDescription storedQueryDescription = idToQuery.get( storedQueryId );
        URL url = storedQueryDescription.url;
        LOG.debug( "Remove StoredQuery with {} from {}", storedQueryId, url );
        try {
            boolean wasDeleted = new File( url.toURI() ).delete();
            if ( !wasDeleted ) {
                String msg = "Stored query with id '" + storedQueryId + "' could not be dropped.";
                throw new OWSException( msg, OPERATION_PROCESSING_FAILED );
            }
            idToQuery.remove( storedQueryId );
        } catch ( Exception e ) {
            LOG.warn( "Could not remove stored query with id {} from {}", storedQueryId, url );
            LOG.trace( "Could not remove stored query", e );
            String msg = "Stored query with id '" + storedQueryId + "' could not be dropped.";
            throw new OWSException( msg, OPERATION_PROCESSING_FAILED );
        }
    }

    private void addManagedStoredQueryDefinition( StoredQueryDefinition storedQueryDefinitionToAdd )
                            throws XMLStreamException, FactoryConfigurationError, IOException {
        File file = new File( managedStoredQueryDirectory, UUID.randomUUID().toString() + ".xml" );
        FileOutputStream fileOutputStream = new FileOutputStream( file );
        XMLStreamWriter writer = new IndentingXMLStreamWriter( XMLOutputFactory.newInstance().createXMLStreamWriter( fileOutputStream ) );
        try {
            StoredQueryDefinition200Encoder.export( storedQueryDefinitionToAdd, writer );
        } finally {
            writer.close();
            fileOutputStream.close();
        }
        String storedQueryId = storedQueryDefinitionToAdd.getId();
        idToQuery.put( storedQueryId,
                       new StoredQueryDescription( storedQueryDefinitionToAdd, file.toURI().toURL(), true ) );
    }

    private List<QName> collectFeatureTypes( List<QName> configuredReturnFeatureTypes,
                                             Collection<FeatureType> featureTypes ) {
        if ( configuredReturnFeatureTypes != null && configuredReturnFeatureTypes.size() > 0 ) {
            return collectConfiguredFeatureTypes( featureTypes, configuredReturnFeatureTypes );
        } else
            return collectAllFeatureTypes( featureTypes );
    }

    private List<QName> collectFeatureTypes( StoredQueryDefinition queryDef ) {
        Collection<FeatureType> featureTypes = wfs.getStoreManager().getFeatureTypes();
        List<QueryExpressionText> queryExpressionTextEls = queryDef.getQueryExpressionTextEls();
        List<QName> allConfiguredReturnFeatureTypes = new ArrayList<QName>();
        for ( QueryExpressionText queryExpressionTextEl : queryExpressionTextEls ) {
            List<QName> configuredReturnFeatureTypes = queryExpressionTextEl.getReturnFeatureTypes();
            List<QName> featureTypesToExport = collectFeatureTypes( configuredReturnFeatureTypes, featureTypes );
            for ( QName featureTypeToExport : featureTypesToExport ) {
                if ( !allConfiguredReturnFeatureTypes.contains( featureTypeToExport ) )
                    allConfiguredReturnFeatureTypes.add( featureTypeToExport );
            }
        }
        return allConfiguredReturnFeatureTypes;
    }

    private List<QName> collectAllFeatureTypes( Collection<FeatureType> featureTypes ) {
        List<QName> ftNames = new ArrayList<QName>( featureTypes.size() );
        for ( FeatureType ft : featureTypes ) {
            ftNames.add( ft.getName() );
        }
        return ftNames;
    }

    private List<QName> collectConfiguredFeatureTypes( Collection<FeatureType> featureTypes,
                                                       List<QName> configuredReturnFeatureTypeNames ) {
        List<QName> ftNames = new ArrayList<QName>( featureTypes.size() );
        for ( QName configuredReturnFeatureTypeName : configuredReturnFeatureTypeNames ) {
            FeatureType featureType = findFeatureType( configuredReturnFeatureTypeName, featureTypes );
            if ( featureType == null )
                throw new IllegalArgumentException( "The FeatureType name " + configuredReturnFeatureTypeName
                                                    + " configured in the stored query is not supported by this WFS!" );
            QName name = featureType.getName();
            if ( !ftNames.contains( name ) )
                ftNames.add( name );
        }
        return ftNames;
    }

    private FeatureType findFeatureType( QName configuredReturnFeatureTypeName, Collection<FeatureType> featureTypes ) {
        for ( FeatureType featureType : featureTypes ) {
            if ( configuredReturnFeatureTypeName.equals( featureType.getName() ) )
                return featureType;
        }
        return null;
    }

    private String collectReturnFeatureTypesAndTransformToString( XMLStreamWriter writer,
                                                                  List<QName> configuredReturnFeatureTypes )
                                                                                          throws XMLStreamException {
        List<QName> ftNames = collectAndSortFeatureTypesToExport( configuredReturnFeatureTypes );
        StringBuilder returnFeatureTypes = new StringBuilder();
        Set<String> exportedPrefixes = new HashSet<String>();
        for ( QName ftName : ftNames ) {
            if ( returnFeatureTypes.length() != 0 ) {
                returnFeatureTypes.append( ' ' );
            }
            String prefixedName = ftName.getLocalPart();
            if ( ftName.getPrefix() != null ) {
                prefixedName = ftName.getPrefix() + ":" + ftName.getLocalPart();
                if ( !exportedPrefixes.contains( ftName.getPrefix() ) ) {
                    writer.writeNamespace( ftName.getPrefix(), ftName.getNamespaceURI() );
                    exportedPrefixes.add( ftName.getPrefix() );
                }
                returnFeatureTypes.append( prefixedName );
            }
        }
        return returnFeatureTypes.toString();
    }

    private void parseAndAddStoredQuery( URL u, boolean isManaged ) {
        try {
            StoredQueryDefinitionXMLAdapter xmlAdapter = new StoredQueryDefinitionXMLAdapter();
            xmlAdapter.load( u );
            addStoredQuery( xmlAdapter.parse(), u, isManaged );
        } catch ( Exception e ) {
            LOG.warn( "Could not parse stored query " + u.toString() + ". Reason: " + e.getMessage() );
            LOG.trace( "Stack trace:", e );
        }
    }

    private void addStoredQuery( StoredQueryDefinition queryDefinition, URL u, boolean isManaged ) {
        LOG.info( "Adding stored query definition with id '{}' from {}", queryDefinition.getId(), u );
        idToQuery.put( queryDefinition.getId(), new StoredQueryDescription( queryDefinition, u, isManaged ) );
    }

    private void writeCreateStoredQueryResponse( HttpResponseBuffer response )
                            throws XMLStreamException, IOException {
        String schemaLocation = WFS_200_NS + " " + WFS_200_SCHEMA_URL;
        XMLStreamWriter writer = getXMLResponseWriter( response, "text/xml", schemaLocation );
        writer.setDefaultNamespace( WFS_200_NS );
        writer.writeStartElement( WFS_200_NS, "CreateStoredQueryResponse" );
        writer.writeDefaultNamespace( WFS_200_NS );
        writer.writeAttribute( "status", "OK" );
        writer.writeEndElement();
    }

    private void writeDropStoredQueryResponse( HttpResponseBuffer response )
                            throws XMLStreamException, IOException {
        String schemaLocation = WFS_200_NS + " " + WFS_200_SCHEMA_URL;
        XMLStreamWriter writer = getXMLResponseWriter( response, "text/xml", schemaLocation );
        writer.setDefaultNamespace( WFS_200_NS );
        writer.writeStartElement( WFS_200_NS, "DropStoredQueryResponse" );
        writer.writeDefaultNamespace( WFS_200_NS );
        writer.writeAttribute( "status", "OK" );
        writer.writeEndElement();
    }

    private void checkIdsOfStoredQueries( CreateStoredQuery request )
                            throws OWSException {
        for ( StoredQueryDefinition storedQueryDefinition : request.getQueryDefinitions() ) {
            String id = storedQueryDefinition.getId();
            if ( hasStoredQuery( id ) ) {
                String msg = "Stored query with id '" + id + "' is already known.";
                throw new OWSException( msg, DUPLICATE_STORED_QUERY_ID_VALUE, id );
            }
        }
    }

    private void checkLanguageOfStoredQueries( CreateStoredQuery request )
                            throws OWSException {
        for ( StoredQueryDefinition storedQueryDefinition : request.getQueryDefinitions() ) {
            List<QueryExpressionText> queryExpressionTexts = storedQueryDefinition.getQueryExpressionTextEls();
            for ( QueryExpressionText queryExpressionText : queryExpressionTexts ) {
                String language = queryExpressionText.getLanguage();
                if ( !LANGUAGE_WFS_QUERY_EXPRESSION.equals( language ) ) {
                    String msg = "Stored query with id '" + queryExpressionTexts +
                                 "' contains an unsupported language " + language + ". Currently only " +
                                 LANGUAGE_WFS_QUERY_EXPRESSION + " is supported";
                    throw new OWSException( msg, OWSException.INVALID_PARAMETER_VALUE, "language" );
                }
            }
        }
    }
    private void checkIdOfDropStoredQueryRequest( DropStoredQuery request )
                            throws OWSException {
        String storedQueryId = request.getStoredQueryId();
        if ( !hasStoredQuery( storedQueryId ) ) {
            String msg = "Stored query with id '" + storedQueryId + "' is not known.";
            throw new OWSException( msg, INVALID_PARAMETER_VALUE, "storedQueryId" );
        } else if ( !idToQuery.get( storedQueryId ).isManaged ) {
            String msg = "Stored query with id '" + storedQueryId + "' is configured by the service provider. "
                         + "It cannot be removed by a DropStoredQuery request.";
            throw new OWSException( msg, INVALID_PARAMETER_VALUE, "storedQueryId" );
        }
    }

    private class StoredQueryDescription {

        private final StoredQueryDefinition definition;

        private final URL url;

        private final boolean isManaged;

        private StoredQueryDescription( StoredQueryDefinition definition, URL url, boolean isManaged ) {
            this.definition = definition;
            this.url = url;
            this.isManaged = isManaged;
        }
    }

}