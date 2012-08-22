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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.transform.TransformerException;

import org.deegree.datatypes.QualifiedName;
import org.deegree.enterprise.servlet.OGCServletController;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.StringTools;
import org.deegree.framework.util.TimeTools;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.framework.xml.XSLTDocument;
import org.deegree.framework.xml.schema.XSDocument;
import org.deegree.i18n.Messages;
import org.deegree.io.datastore.PropertyPathResolvingException;
import org.deegree.model.feature.Feature;
import org.deegree.model.feature.FeatureCollection;
import org.deegree.model.feature.FeatureException;
import org.deegree.model.feature.FeatureProperty;
import org.deegree.model.feature.GMLFeatureAdapter;
import org.deegree.model.filterencoding.ComplexFilter;
import org.deegree.model.filterencoding.Expression;
import org.deegree.model.filterencoding.Literal;
import org.deegree.model.filterencoding.OperationDefines;
import org.deegree.model.filterencoding.PropertyIsCOMPOperation;
import org.deegree.model.filterencoding.PropertyName;
import org.deegree.ogcbase.ExceptionCode;
import org.deegree.ogcbase.PropertyPath;
import org.deegree.ogcbase.PropertyPathFactory;
import org.deegree.ogcwebservices.InvalidParameterValueException;
import org.deegree.ogcwebservices.OGCWebServiceException;
import org.deegree.ogcwebservices.csw.CSWExceptionCode;
import org.deegree.ogcwebservices.csw.capabilities.CatalogueOperationsMetadata;
import org.deegree.ogcwebservices.csw.configuration.CatalogueConfiguration;
import org.deegree.ogcwebservices.csw.configuration.CatalogueConfigurationDocument;
import org.deegree.ogcwebservices.csw.configuration.CatalogueOutputSchemaParameter;
import org.deegree.ogcwebservices.csw.configuration.CatalogueOutputSchemaValue;
import org.deegree.ogcwebservices.csw.configuration.CatalogueTypeNameSchemaParameter;
import org.deegree.ogcwebservices.csw.configuration.CatalogueTypeNameSchemaValue;
import org.deegree.ogcwebservices.csw.discovery.GetRecords.RESULT_TYPE;
import org.deegree.ogcwebservices.wfs.WFService;
import org.deegree.ogcwebservices.wfs.capabilities.WFSCapabilities;
import org.deegree.ogcwebservices.wfs.operation.FeatureResult;
import org.deegree.ogcwebservices.wfs.operation.GetFeature;
import org.deegree.ogcwebservices.wfs.operation.GetFeatureDocument;
import org.deegree.ogcwebservices.wfs.operation.Query;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * The Discovery class allows clients to discover resources registered in a catalogue, by providing four operations
 * named <code>query</code>,<code>present</code>, <code>describeRecordType</code>, and <code>getDomain</code>. This
 * class has a required association from the Catalogue Service class, and is thus always implemented by all Catalogue
 * Service implementations. The Session class can be included with the Discovery class, in associations with the
 * Catalogue Service class. The &quote;query&quote; and &quote;present&quote; operations may be executed in a session or
 * stateful context. If a session context exists, the dynamic model uses internal states of the session and the allowed
 * transitions between states. When the &quote;query&quote; and &quote;present&quote; state does not include a session
 * between a server and a client, any memory or shared information between the client and the server may be based on
 * private understandings or features available in the protocol binding. The describeRecordType and getDomain operations
 * do not require a session context.
 * 
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth </a>
 * @author <a href="mailto:tfr@users.sourceforge.net">Torsten Friebe </a>
 * 
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
public class Discovery {

    private static final ILogger LOG = LoggerFactory.getLogger( Discovery.class );

    // Keys are Strings, values are XSLDocuments
    protected static final Map<String, XSLTDocument> IN_XSL = new HashMap<String, XSLTDocument>();

    // Keys are Strings, values are XSLDocuments
    protected static final Map<String, XSLTDocument> OUT_XSL = new HashMap<String, XSLTDocument>();

    // Keys are Strings, values are URLs
    protected static final Map<String, URL> SCHEMA_URLS = new HashMap<String, URL>();

    // Keys are Strings, values are XMLFragments
    protected static final Map<String, XSDocument> SCHEMA_DOCS = new HashMap<String, XSDocument>();

    protected static final String DEFAULT_SCHEMA = "DublinCore";

    protected static final String OGC_CORE_SCHEMA = "OGCCORE";

    protected CatalogueConfiguration cswConfiguration = null;

    /**
     * The complete data access of a catalog service is managed by one instances of WFService.
     */
    protected WFService wfsResource; // single instance only for this CSW

    /**
     * to be used with reflections
     */
    public Discovery() {

    }

    /**
     * @param wfsService
     *            to contact
     * @param cswConfiguration
     *            of this service
     */
    public Discovery( WFService wfsService, CatalogueConfiguration cswConfiguration ) {
        init( wfsService, cswConfiguration );
    }

    /**
     * @param wfsService
     * @param serviceConfiguration
     */
    public void init( WFService wfsService, CatalogueConfiguration serviceConfiguration ) {
        this.wfsResource = wfsService;
        this.cswConfiguration = serviceConfiguration;
        try {
            CatalogueOperationsMetadata catalogMetadata = (CatalogueOperationsMetadata) cswConfiguration.getOperationsMetadata();
            CatalogueOutputSchemaParameter outputSchemaParameter = (CatalogueOutputSchemaParameter) catalogMetadata.getGetRecords().getParameter( "outputSchema" );

            CatalogueConfigurationDocument document = new CatalogueConfigurationDocument();
            document.setSystemId( cswConfiguration.getSystemId() );
            CatalogueOutputSchemaValue[] values = outputSchemaParameter.getSpecializedValues();
            for ( int i = 0; i < values.length; i++ ) {
                CatalogueOutputSchemaValue value = values[i];
                String schemaName = value.getValue().toUpperCase();
                URL fileURL = document.resolve( value.getInXsl() );
                LOG.logInfo( StringTools.concat( 300, "Input schema '", schemaName,
                                                 "' is processed using XSLT-sheet from URL '", fileURL, "'" ) );
                XSLTDocument inXSLSheet = new XSLTDocument();
                inXSLSheet.load( fileURL );
                IN_XSL.put( schemaName, inXSLSheet );

                fileURL = document.resolve( value.getOutXsl() );
                LOG.logInfo( StringTools.concat( 300, "Output schema '", schemaName,
                                                 "' is processed using XSLT-sheet from URL '", fileURL, "'" ) );
                XSLTDocument outXSLSheet = new XSLTDocument();
                outXSLSheet.load( fileURL );
                OUT_XSL.put( schemaName, outXSLSheet );

            }

            // read and store schema definitions
            // each type(Name) provided by a CS-W is assigned to one schema
            CatalogueTypeNameSchemaParameter outputTypeNameParameter = (CatalogueTypeNameSchemaParameter) catalogMetadata.getGetRecords().getParameter( "typeName" );
            CatalogueTypeNameSchemaValue[] tn_values = outputTypeNameParameter.getSpecializedValues();
            for ( int i = 0; i < tn_values.length; i++ ) {
                CatalogueTypeNameSchemaValue value = tn_values[i];
                URL fileURL = document.resolve( value.getSchema() );
                XSDocument schemaDoc = new XSDocument();
                schemaDoc.load( fileURL );
                String typeName = value.getValue().toUpperCase();
                LOG.logInfo( StringTools.concat( 300, "Schema for type '", typeName,
                                                 "' is defined in XSD-file at URL '", fileURL, "'" ) );
                SCHEMA_URLS.put( typeName, fileURL );
                SCHEMA_DOCS.put( typeName, schemaDoc );
            }
        } catch ( Exception e ) {
            e.printStackTrace();
            LOG.logError( "Error while creating CSW Discovery: " + e.getMessage(), e );
        }
        WFSCapabilities capa = wfsResource.getCapabilities();
        LOG.logInfo( "CSW Discovery initialized with WFS resource, wfs version: " + capa.getVersion() );
    }

    /**
     * Performs the submitted <code>DescribeRecord</code> -request.
     * 
     * TODO: Check output schema & Co.
     * 
     * @param request
     * @return The DescribeRecordResult created from the given request
     * @throws OGCWebServiceException
     */
    public DescribeRecordResult describeRecordType( DescribeRecord request )
                            throws OGCWebServiceException {

        // requested output format must be 'text/xml'
        if ( !( "text/xml".equals( request.getOutputFormat() ) || "application/xml".equals( request.getOutputFormat() ) ) ) {
            String s = Messages.getMessage( "CSW_DESCRIBERECORD_INVALID_FORMAT", request.getOutputFormat() );
            throw new OGCWebServiceException( getClass().getName(), s, ExceptionCode.INVALID_FORMAT );
        }

        // requested schema language must be 'XMLSCHEMA'
        if ( !( "XMLSCHEMA".equals( request.getSchemaLanguage().toString() ) || "http://www.w3.org/XML/Schema".equals( request.getSchemaLanguage().toString() ) ) ) {
            String s = Messages.getMessage( "CSW_DESCRIBERECORD_INVALID_SCHEMA", request.getSchemaLanguage() );
            throw new InvalidParameterValueException( s );
        }

        // if no type names are specified, describe all known types
        String[] typeNames = request.getTypeNames();
        if ( typeNames == null || typeNames.length == 0 ) {
            typeNames = SCHEMA_DOCS.keySet().toArray( new String[SCHEMA_DOCS.keySet().size()] );
        }

        SchemaComponent[] schemaComponents = new SchemaComponent[typeNames.length];

        for ( int i = 0; i < typeNames.length; i++ ) {
            XSDocument doc = SCHEMA_DOCS.get( typeNames[i].toUpperCase() );
            if ( doc == null ) {
                LOG.logDebug( "Discovery.describeRecord, no key found for: " + typeNames[i].toUpperCase()
                              + " trying again with added 'RIM:' prefix" );
                doc = SCHEMA_DOCS.get( "RIM:" + typeNames[i].toUpperCase() );
            }
            if ( doc == null ) {
                String msg = Messages.getMessage( "CSW_DESCRIBERECORD_UNSUPPORTED_TN", typeNames[i] );
                throw new OGCWebServiceException( getClass().getName(), msg );
            }
            try {
                schemaComponents[i] = new SchemaComponent( doc, doc.getTargetNamespace(), null, new URI( "XMLSCHEMA" ) );
            } catch ( URISyntaxException e ) {
                throw new OGCWebServiceException( this.getClass().getName(), e.getMessage() );
            } catch ( XMLParsingException e ) {
                throw new OGCWebServiceException( this.getClass().getName(), e.getMessage() );
            }
        }

        return new DescribeRecordResult( request, "2.0.0", schemaComponents );
    }

    /**
     * @param request
     *            which is not handled
     * @return just a new empty DomainValues instance.
     * 
     */
    public DomainValues getDomain( @SuppressWarnings("unused") GetDomain request ) {
        // todo not implemented, yet
        return new DomainValues();
    }

    private String normalizeOutputSchema( String outputSchema )
                            throws InvalidParameterValueException {
        LOG.logDebug( "Normalizing following outputschema: " + outputSchema );
        if ( outputSchema == null ) {
            LOG.logDebug( "Setting the outputSchema to: " + DEFAULT_SCHEMA );
            outputSchema = DEFAULT_SCHEMA;
        } else if ( outputSchema.equalsIgnoreCase( OGC_CORE_SCHEMA ) ) {
            LOG.logDebug( "Setting the outputSchema to: " + DEFAULT_SCHEMA );
            outputSchema = DEFAULT_SCHEMA;
        }
        outputSchema = outputSchema.toUpperCase();
        if ( IN_XSL.get( outputSchema ) == null ) {
            String msg = "Unsupported output schema '" + outputSchema + "' requested. Supported schemas are: ";
            Iterator<String> it = IN_XSL.keySet().iterator();
            while ( it.hasNext() ) {
                msg += it.next();
                if ( it.hasNext() ) {
                    msg += ", ";
                } else {
                    msg += ".";
                }
            }
            throw new InvalidParameterValueException( msg );
        }
        return outputSchema;
    }

    private String getAllNamespaceDeclarations( Document doc ) {
        Map<String, String> nsp = new HashMap<String, String>();
        nsp = collect( nsp, doc );

        Iterator<String> iter = nsp.keySet().iterator();
        StringBuffer sb = new StringBuffer( 1000 );
        while ( iter.hasNext() ) {
            String s = iter.next();
            String val = nsp.get( s );
            sb.append( s ).append( ":" ).append( val );
            if ( iter.hasNext() ) {
                sb.append( ';' );
            }
        }
        return sb.toString();
    }

    private Map<String, String> collect( Map<String, String> nsp, Node node ) {
        NamedNodeMap nnm = node.getAttributes();
        if ( nnm != null ) {
            for ( int i = 0; i < nnm.getLength(); i++ ) {
                String s = nnm.item( i ).getNodeName();
                if ( s.startsWith( "xmlns:" ) ) {
                    nsp.put( s.substring( 6, s.length() ), nnm.item( i ).getNodeValue() );
                }
            }
        }
        NodeList nl = node.getChildNodes();
        if ( nl != null ) {
            for ( int i = 0; i < nl.getLength(); i++ ) {
                collect( nsp, nl.item( i ) );
            }
        }
        return nsp;
    }

    /**
     * Performs a <code>GetRecords</code> request.
     * <p>
     * This involves the following steps:
     * <ul>
     * <li><code>GetRecords</code>-><code>GetRecordsDocument</code></li>
     * <li><code>GetRecordsDocument</code>-><code>GetFeatureDocument</code> using XSLT</li>
     * <li><code>GetFeatureDocument</code>-><code>GetFeature</code></li>
     * <li><code>GetFeature</code> request is performed against the underlying WFS</li>
     * <li>WFS answers with a <code>FeatureResult</code> object (which contains a <code>FeatureCollection</code>)</li>
     * <li><code>FeatureCollection</code>-> GMLFeatureCollectionDocument (as a String)</li>
     * <li>GMLFeatureCollectionDocument</code>-><code>GetRecordsResultDocument</code> using XSLT</li>
     * <li><code>GetRecordsResultDocument</code>-><code>GetRecordsResult</code></li>
     * </ul>
     * </p>
     * 
     * @param getRecords
     * @return GetRecordsResult
     * @throws OGCWebServiceException
     */
    public GetRecordsResult query( GetRecords getRecords )
                            throws OGCWebServiceException {
        GetFeature getFeature = null;
        XMLFragment getFeatureDocument = null;
        Object wfsResponse = null;
        GetRecordsResult cswResponse = null;
        String outputSchema = normalizeOutputSchema( getRecords.getOutputSchema() );

        // TODO remove this (only necessary because determineRecordsMatched changes the resultType)
        String resultType = getRecords.getResultTypeAsString();

        XMLFragment getRecordsDocument = new XMLFragment( XMLFactory.export( getRecords ).getRootElement() );
        try {
            String nsp = getAllNamespaceDeclarations( getRecordsDocument.getRootElement().getOwnerDocument() );
            // incoming GetRecord request must be transformed to a GetFeature
            // request because the underlying 'data engine' of the CSW is a WFS
            XSLTDocument xslSheet = IN_XSL.get( outputSchema );
            synchronized ( xslSheet ) {
                Map<String, String> param = new HashMap<String, String>();
                param.put( "NSP", nsp );
                if ( LOG.isDebug() ) {
                    LOG.logDebug( "Input GetRecords request:\n" + getRecordsDocument.getAsPrettyString() );
                }
                try {
                    getFeatureDocument = xslSheet.transform( getRecordsDocument, XMLFragment.DEFAULT_URL, null, param );
                } catch ( MalformedURLException e ) {
                    LOG.logError( e.getMessage(), e );
                }
                if ( LOG.isDebug() ) {
                    LOG.logDebugXMLFile( "first", getFeatureDocument );
                    // LOG.logDebug( "*****First Generated WFS GetFeature request:\n"
                    // + getFeatureDocument.getAsPrettyString() );
                }
                xslSheet.notifyAll();
            }

        } catch ( TransformerException e ) {
            String msg = "Can't transform GetRecord request to WFS GetFeature request: " + e.getMessage();
            LOG.logError( msg, e );
            throw new OGCWebServiceException( msg );
        }

        // if ( LOG.getLevel() == ILogger.LOG_DEBUG ) {
        // StringWriter sw = new StringWriter( 5000 );
        // try {
        // getFeatureDocument.prettyPrint( sw );
        // } catch ( TransformerException e ) {
        // getFeatureDocument.write( sw );
        // }
        // LOG.logDebug( sw.getBuffer().toString() );
        // }

        try {
            LOG.logDebug( "Creating the GetFeature bean from the transformed GetRecordsDocument" );
            getFeature = GetFeature.create( getRecords.getId(), getFeatureDocument.getRootElement() );
        } catch ( Exception e ) {
            String msg = "Cannot generate object representation for GetFeature request: " + e.getMessage();
            LOG.logError( msg, e );
            throw new OGCWebServiceException( msg );
        }

        try {
            LOG.logDebug( "Sending the GetFeature Request to the local wfs" );
            wfsResponse = wfsResource.doService( getFeature );
        } catch ( OGCWebServiceException e ) {
            String msg = "Generated WFS GetFeature request failed: " + e.getMessage();
            LOG.logError( msg, e );
            throw new OGCWebServiceException( msg );
        }

        // theoretical it is possible the result of a GetFeature request is not
        // an instance of FeatureResult; but this never should happen
        if ( !( wfsResponse instanceof FeatureResult ) ) {
            String msg = "Unexpected result type '" + wfsResponse.getClass().getName()
                         + "' from WFS (must be FeatureResult)." + " Maybe a FeatureType is not correctly registered!?";
            LOG.logError( msg );
            throw new OGCWebServiceException( msg );
        }

        FeatureResult featureResult = (FeatureResult) wfsResponse;

        // this never should happen too - but it is possible
        if ( !( featureResult.getResponse() instanceof FeatureCollection ) ) {
            String msg = "Unexpected reponse type: '" + featureResult.getResponse().getClass().getName() + " "
                         + featureResult.getResponse().getClass()
                         + "' in FeatureResult of WFS (must be a FeatureCollection).";
            LOG.logError( msg );
            throw new OGCWebServiceException( msg );
        }
        FeatureCollection featureCollection = (FeatureCollection) featureResult.getResponse();

        try {
            int numberOfRecordsReturned = featureCollection.size();
            int numberOfMatchedRecords = 0;
            int startPosition = getRecords.getStartPosition();
            if ( startPosition < 1 )
                startPosition = 1;
            int nextRecord = startPosition + featureCollection.size();

            if ( getRecords.getResultType().equals( RESULT_TYPE.HITS ) ) {
                numberOfMatchedRecords = Integer.parseInt( featureCollection.getAttribute( "numberOfFeatures" ) );
            } else {
                // if result type does not equal 'HITS', a separate request must
                // be created and performed to determine how many records match
                // the query
                LOG.logDebug( "Going to determine the number of matched records" );
                numberOfMatchedRecords = determineRecordsMatched( getRecords );
            }

            HashMap<String, String> params = new HashMap<String, String>();
            params.put( "REQUEST_ID", getRecords.getId() );
            if ( numberOfRecordsReturned != 0 ) {
                params.put( "SEARCH_STATUS", "complete" );
            } else {
                params.put( "SEARCH_STATUS", "none" );
            }
            params.put( "TIMESTAMP", TimeTools.getISOFormattedTime() );
            List<QualifiedName> typenames = getRecords.getQuery().getTypeNamesAsList();
            // this is a bit critical because
            // a) not the complete result can be validated but just single records
            // b) it is possible that several different record types are part
            // of a response that must be validated against different schemas
            String s = null;
            String version = getRecords.getVersion();
            if ( version == null || "".equals( version.trim() ) ) {
                version = GetRecords.DEFAULT_VERSION;
            }
            if ( "2.0.0".equals( version ) ) {
                s = StringTools.concat( 300, OGCServletController.address, "?service=CSW&version=2.0.0&",
                                        "request=DescribeRecord&typeName=", typenames.get( 0 ).getPrefix(), ":",
                                        typenames.get( 0 ).getLocalName() );
            } else {
                s = StringTools.concat( 300, OGCServletController.address, "?service=CSW&version=" + version + "&",
                                        "request=DescribeRecord&typeName=", typenames.get( 0 ).getFormattedString() );
            }
            params.put( "VERSION", version );
            params.put( "RECORD_SCHEMA", s );
            params.put( "RECORDS_MATCHED", "" + numberOfMatchedRecords );
            params.put( "RECORDS_RETURNED", "" + numberOfRecordsReturned );
            params.put( "NEXT_RECORD", "" + nextRecord );
            String elementSet = getRecords.getQuery().getElementSetName();
            if ( elementSet == null ) {
                elementSet = "brief";
            }
            params.put( "ELEMENT_SET", elementSet.toLowerCase() );
            params.put( "RESULT_TYPE", resultType );
            params.put( "REQUEST_NAME", "GetRecords" );

            ByteArrayOutputStream bos = new ByteArrayOutputStream( 50000 );
            GMLFeatureAdapter ada = new GMLFeatureAdapter( true );

            ada.export( featureCollection, bos );
            if ( LOG.getLevel() == ILogger.LOG_DEBUG ) {
                s = new String( bos.toByteArray() );
                LOG.logDebug( s );
                LOG.logDebugFile( "CSW_GetRecord_FC", "xml", s );
            }

            // vice versa to request transforming the feature collection being result
            // to the GetFeature request must be transformed into a GetRecords result
            ByteArrayInputStream bis = new ByteArrayInputStream( bos.toByteArray() );
            XSLTDocument xslSheet = OUT_XSL.get( outputSchema );
            XMLFragment resultDocument = xslSheet.transform( bis, null, null, params );
            GetRecordsResultDocument cswResponseDocument = new GetRecordsResultDocument();
            cswResponseDocument.setRootElement( resultDocument.getRootElement() );
            cswResponse = cswResponseDocument.parseGetRecordsResponse( getRecords );
        } catch ( IOException e ) {
            String msg = "Can't transform WFS response (FeatureCollection) to CSW response: " + e.getMessage();
            LOG.logError( msg, e );
            throw new OGCWebServiceException( msg );

        } catch ( FeatureException e ) {
            String msg = "Can't transform WFS response (FeatureCollection) to CSW response: " + e.getMessage();
            LOG.logError( msg, e );
            throw new OGCWebServiceException( msg );

        } catch ( TransformerException e ) {
            String msg = "Can't transform WFS response (FeatureCollection) to CSW response: " + e.getMessage();
            LOG.logError( msg, e );
            throw new OGCWebServiceException( msg );

        }

        return cswResponse;
    }

    /**
     * Returns the number of records matching a GetRecords request.
     * 
     * @param getRecords
     * @return the number of records matching a GetRecords request
     * @throws OGCWebServiceException
     */
    protected int determineRecordsMatched( GetRecords getRecords )
                            throws OGCWebServiceException {
        getRecords.setResultType( GetRecords.RESULT_TYPE.HITS );
        getRecords.setStartPosition( 1 );
        GetFeature getFeature = null;
        XMLFragment getFeatureDocument = null;
        Object wfsResponse = null;
        String outputSchema = normalizeOutputSchema( getRecords.getOutputSchema() );

        XMLFragment getRecordsDocument = new XMLFragment( XMLFactory.export( getRecords ).getRootElement() );
        try {
            LOG.logDebug( "Getting the xslt sheet for the determination of the number of matched records" );
            String nsp = getAllNamespaceDeclarations( getRecordsDocument.getRootElement().getOwnerDocument() );
            XSLTDocument xslSheet = IN_XSL.get( outputSchema );

            synchronized ( xslSheet ) {
                Map<String, String> param = new HashMap<String, String>();
                param.put( "NSP", nsp );
                try {
                    getFeatureDocument = xslSheet.transform( getRecordsDocument, XMLFragment.DEFAULT_URL, null, param );
                } catch ( MalformedURLException e ) {
                    LOG.logError( e.getMessage(), e );
                }
                LOG.logDebug( "*****Second Generated WFS GetFeature request (to determine records matched):\n"
                              + getFeatureDocument.getAsPrettyString() );
                xslSheet.notifyAll();
            }
            // getFeatureDocument = xslSheet.transform( getRecordsDocument );
            // LOG.logDebug( "Generated WFS GetFeature request (HITS):\n" + getFeatureDocument );
        } catch ( TransformerException e ) {
            e.printStackTrace();
            String msg = "Can't transform GetRecord request to WFS GetFeature request: " + e.getMessage();
            LOG.logError( msg, e );
            throw new OGCWebServiceException( msg );
        }

        try {
            getFeature = GetFeature.create( getRecords.getId(), getFeatureDocument.getRootElement() );
        } catch ( Exception e ) {
            String msg = "Cannot generate object representation for GetFeature request: " + e.getMessage();
            LOG.logError( msg, e );
            throw new OGCWebServiceException( msg );
        }

        try {
            wfsResponse = wfsResource.doService( getFeature );
        } catch ( OGCWebServiceException e ) {
            String msg = "Generated WFS GetFeature request failed: " + e.getMessage();
            LOG.logError( msg, e );
            throw new OGCWebServiceException( msg );
        }

        if ( !( wfsResponse instanceof FeatureResult ) ) {
            String msg = "Unexpected result type '" + wfsResponse.getClass().getName()
                         + "' from WFS (must be FeatureResult)." + " Maybe a FeatureType is not correctly registered!?";
            LOG.logError( msg );
            throw new OGCWebServiceException( msg );
        }

        FeatureResult featureResult = (FeatureResult) wfsResponse;

        if ( !( featureResult.getResponse() instanceof FeatureCollection ) ) {
            String msg = "Unexpected reponse type: '" + featureResult.getResponse().getClass().getName() + " "
                         + featureResult.getResponse().getClass()
                         + "' in FeatureResult of WFS (must be a FeatureCollection).";
            LOG.logError( msg );
            throw new OGCWebServiceException( msg );
        }
        FeatureCollection featureCollection = (FeatureCollection) featureResult.getResponse();

        return Integer.parseInt( featureCollection.getAttribute( "numberOfFeatures" ) );
    }

    /**
     * Performs a <code>GetRecordById</code> request.
     * <p>
     * This involves the following steps:
     * <ul>
     * <li><code>GetRecordById</code>-><code>GetRecordByIdDocument</code></li>
     * <li><code>GetRecordByIdDocument</code>-><code>GetFeatureDocument</code> using XSLT</li>
     * <li><code>GetFeatureDocument</code>-><code>GetFeature</code></li>
     * <li><code>GetFeature</code> request is performed against the underlying WFS</li>
     * <li>WFS answers with a <code>FeatureResult</code> object (which contains a <code>FeatureCollection</code>)</li>
     * <li><code>FeatureCollection</code>-> GMLFeatureCollectionDocument (as a String)</li>
     * <li>GMLFeatureCollectionDocument</code>-><code>GetRecordsResultDocument</code> using XSLT</li>
     * <li><code>GetRecordsResultDocument</code>-><code>GetRecordsResult</code></li>
     * </ul>
     * </p>
     * 
     * @param getRecordById
     * @return The GetRecordByIdResult created from teh given GetRecordById
     * @throws OGCWebServiceException
     */
    public GetRecordByIdResult query( GetRecordById getRecordById )
                            throws OGCWebServiceException {

        GetFeature getFeature = null;
        XMLFragment getFeatureDocument = null;
        Object wfsResponse = null;
        GetRecordByIdResult cswResponse = null;
        String outputSchema = cswConfiguration.getDeegreeParams().getDefaultOutputSchema();

        XMLFragment getRecordsDocument = new XMLFragment( XMLFactory.export( getRecordById ).getRootElement() );
        try {
            XSLTDocument xslSheet = IN_XSL.get( outputSchema.toUpperCase() );
            getFeatureDocument = xslSheet.transform( getRecordsDocument );
            LOG.logDebug( "Generated WFS GetFeature request:\n" + getFeatureDocument );
        } catch ( TransformerException e ) {
            String msg = "Can't transform GetRecordById request to WFS GetFeature request: " + e.getMessage();
            LOG.logError( msg, e );
            throw new OGCWebServiceException( msg );
        }

        if ( LOG.getLevel() == ILogger.LOG_DEBUG ) {
            StringWriter sw = new StringWriter( 5000 );
            getFeatureDocument.write( sw );
            LOG.logDebug( sw.getBuffer().toString() );
        }

        try {
            getFeature = GetFeature.create( getRecordById.getId(), getFeatureDocument.getRootElement() );
        } catch ( Exception e ) {
            String msg = "Cannot generate object representation for GetFeature request: " + e.getMessage();
            LOG.logError( msg, e );
            throw new OGCWebServiceException( msg );
        }

        try {
            wfsResponse = wfsResource.doService( getFeature );
        } catch ( OGCWebServiceException e ) {
            String msg = "Generated WFS GetFeature request failed: " + e.getMessage();
            LOG.logError( msg, e );
            throw new OGCWebServiceException( msg );
        }

        if ( !( wfsResponse instanceof FeatureResult ) ) {
            String msg = "Unexpected result type '" + wfsResponse.getClass().getName()
                         + "' from WFS (must be FeatureResult)." + " Maybe a FeatureType is not correctly registered!?";
            LOG.logError( msg );
            throw new OGCWebServiceException( msg );
        }

        FeatureResult featureResult = (FeatureResult) wfsResponse;

        if ( !( featureResult.getResponse() instanceof FeatureCollection ) ) {
            String msg = "Unexpected reponse type: '" + featureResult.getResponse().getClass().getName() + " "
                         + featureResult.getResponse().getClass()
                         + "' in FeatureResult of WFS (must be a FeatureCollection).";
            LOG.logError( msg );
            throw new OGCWebServiceException( msg );
        }
        FeatureCollection featureCollection = (FeatureCollection) featureResult.getResponse();

        try {
            int numberOfMatchedRecords = featureCollection == null ? 0 : featureCollection.size();
            int startPosition = 1;
            long maxRecords = Integer.MAX_VALUE;
            long numberOfRecordsReturned = startPosition + maxRecords < numberOfMatchedRecords ? maxRecords
                                                                                              : numberOfMatchedRecords
                                                                                                - startPosition + 1;
            long nextRecord = numberOfRecordsReturned + startPosition > numberOfMatchedRecords ? 0
                                                                                              : numberOfRecordsReturned
                                                                                                + startPosition;

            HashMap<String, String> params = new HashMap<String, String>();
            params.put( "REQUEST_ID", getRecordById.getId() );
            if ( numberOfRecordsReturned != 0 ) {
                params.put( "SEARCH_STATUS", "complete" );
            } else {
                params.put( "SEARCH_STATUS", "none" );
            }
            params.put( "TIMESTAMP", TimeTools.getISOFormattedTime() );
            String s = OGCServletController.address + "?service=CSW&version=2.0.0&request=DescribeRecord";
            params.put( "RECORD_SCHEMA", s );
            params.put( "RECORDS_MATCHED", "" + numberOfMatchedRecords );
            params.put( "RECORDS_RETURNED", "" + numberOfRecordsReturned );
            params.put( "NEXT_RECORD", "" + nextRecord );
            params.put( "ELEMENT_SET", "full" );
            params.put( "REQUEST_NAME", "GetRecordById" );

            featureCollection.setAttribute( "byID", "true" );
            ByteArrayOutputStream bos = new ByteArrayOutputStream( 50000 );
            GMLFeatureAdapter ada = new GMLFeatureAdapter( true );
            ada.export( featureCollection, bos );

            if ( LOG.getLevel() == ILogger.LOG_DEBUG ) {
                LOG.logDebug( new String( bos.toByteArray() ) );
            }

            ByteArrayInputStream bis = new ByteArrayInputStream( bos.toByteArray() );
            if ( "2.0.2".equals( getRecordById.getVersion() ) && getRecordById.getOutputSchema() != null ) {
                outputSchema = normalizeOutputSchema( getRecordById.getOutputSchema() );
            }
            XSLTDocument xslSheet = OUT_XSL.get( outputSchema.toUpperCase() );
            XMLFragment resultDocument = xslSheet.transform( bis, null, null, params );
            GetRecordByIdResultDocument cswResponseDocument = new GetRecordByIdResultDocument();
            cswResponseDocument.setRootElement( resultDocument.getRootElement() );
            cswResponse = cswResponseDocument.parseGetRecordByIdResponse( getRecordById );
        } catch ( Exception e ) {
            e.printStackTrace();
            String msg = "Can't transform WFS response (FeatureCollection) " + "to CSW response: " + e.getMessage();
            LOG.logError( msg, e );
            throw new OGCWebServiceException( msg );
        }

        return cswResponse;
    }

    /**
     * Contacts the wfsResource to find a rim:ExtrinsicObject which contains the
     * {@link GetRepositoryItem#getRepositoryItemID()} and retrieves it's
     * app:RegistryObject/app:extrinsicObject/app:ExtrinsicObject/app:object. The value in this property will then be
     * written to the response stream (e.g. sent to the requester).
     * 
     * @param request
     *            the created OGCRequest
     * @return the repository item response
     * @throws OGCWebServiceException
     */
    public GetRepositoryItemResponse guery( GetRepositoryItem request )
                            throws OGCWebServiceException {
        // Some properterypaths which are used for the creation of a complex filter.
        URI appURI = URI.create( "http://www.deegree.org/app" );

        QualifiedName registryObject = new QualifiedName( "app", "RegistryObject", appURI );
        Expression iduriExpr = new PropertyName( new QualifiedName( "app", "iduri", appURI ) );
        Expression idLiteral = new Literal( request.getRepositoryItemID().toString() );
        PropertyIsCOMPOperation idOperator = new PropertyIsCOMPOperation( OperationDefines.PROPERTYISEQUALTO,
                                                                          iduriExpr, idLiteral );
        ComplexFilter idFilter = new ComplexFilter( idOperator );

        FeatureCollection featureCollectionOnId = null;
        try {
            FeatureResult fr = sendWFSGetFeature( registryObject, idFilter );
            if ( fr != null ) {
                featureCollectionOnId = (FeatureCollection) fr.getResponse();
            }
        } catch ( OGCWebServiceException e ) {
            throw new OGCWebServiceException( "The requested item " + request.getRepositoryItemID()
                                              + " could not be retrieved from the csw backend: " + e.getMessage(),
                                              CSWExceptionCode.WRS_NOTFOUND );
        }
        if ( featureCollectionOnId == null ) {
            throw new OGCWebServiceException( "The requested item " + request.getRepositoryItemID()
                                              + " could not be retrieved from the csw backend.",
                                              CSWExceptionCode.WRS_NOTFOUND );
        }
        String numbOfFeatures = featureCollectionOnId.getAttribute( "numberOfFeatures" );
        int featureCount = 0;
        try {
            featureCount = Integer.parseInt( numbOfFeatures );
            LOG.logDebug( "the number of features in the GetFeature was: " + featureCount );
        } catch ( NumberFormatException nfe ) {
            // nottin
        }

        GetRepositoryItemResponse response = null;
        // Check the number of hits we've found, if the id allready exists it means we want to set the status of the
        // object to invalid.
        // String newID = id;
        if ( featureCount > 1 ) {
            throw new OGCWebServiceException( "The id : " + request.getRepositoryItemID()
                                              + " is not unique. This repositoryItem can therefore not be retrieved.",
                                              CSWExceptionCode.WRS_NOTFOUND );
        } else if ( featureCount == 0 ) {
            throw new OGCWebServiceException(
                                              "The id: "
                                                                      + request.getRepositoryItemID()
                                                                      + " corresponds to no rim:ExtrinsicObject. This repositoryItem can therefore not be retrieved.",
                                              CSWExceptionCode.WRS_NOTFOUND );

        } else {
            Feature f = featureCollectionOnId.getFeature( 0 );
            if ( f != null ) {
                PropertyPath pp = PropertyPathFactory.createPropertyPath( registryObject );
                pp.append( PropertyPathFactory.createPropertyPathStep( new QualifiedName( "app", "extrinsicObject",
                                                                                          appURI ) ) );
                pp.append( PropertyPathFactory.createPropertyPathStep( new QualifiedName( "app", "ExtrinsicObject",
                                                                                          appURI ) ) );
                pp.append( PropertyPathFactory.createPropertyPathStep( new QualifiedName( "app", "object", appURI ) ) );
                FeatureProperty retrievedObject = null;
                try {
                    retrievedObject = f.getDefaultProperty( pp );
                } catch ( PropertyPathResolvingException ppre ) {
                    throw new OGCWebServiceException(
                                                      "The id: "
                                                                              + request.getRepositoryItemID()
                                                                              + " has no repository item stored, there is nothing to be retrieved.",
                                                      CSWExceptionCode.WRS_NOTFOUND );

                }
                if ( retrievedObject == null || retrievedObject.getValue() == null ) {
                    throw new OGCWebServiceException(
                                                      "The id: "
                                                                              + request.getRepositoryItemID()
                                                                              + " has no repository item stored, there is nothing to be retrieved.",
                                                      CSWExceptionCode.WRS_NOTFOUND );
                }

                String repositoryItem = (String) retrievedObject.getValue();
                LOG.logDebug( "found the repositoryItem: " + repositoryItem );

                pp = PropertyPathFactory.createPropertyPath( registryObject );
                pp.append( PropertyPathFactory.createPropertyPathStep( new QualifiedName( "app", "extrinsicObject",
                                                                                          appURI ) ) );
                pp.append( PropertyPathFactory.createPropertyPathStep( new QualifiedName( "app", "ExtrinsicObject",
                                                                                          appURI ) ) );
                pp.append( PropertyPathFactory.createPropertyPathStep( new QualifiedName( "app", "mimeType", appURI ) ) );
                FeatureProperty mimeType = null;
                try {
                    mimeType = f.getDefaultProperty( pp );
                } catch ( PropertyPathResolvingException ppre ) {
                    LOG.logError( "The mimetype value (of the GetRepositoryItem: " + request.getRepositoryItemID()
                                  + ") was not set, setting content header to 'application/xml' " );
                }
                if ( mimeType == null || mimeType.getValue() == null ) {
                    LOG.logError( "The mimetype value (of the GetRepositoryItem: " + request.getRepositoryItemID()
                                  + ") was not set, setting content header to 'application/xml' " );
                }

                try {
                    XMLFragment itemFrag = new XMLFragment( new StringReader( repositoryItem ), XMLFragment.DEFAULT_URL );
                    response = new GetRepositoryItemResponse( request.getId(), request.getRepositoryItemID(), itemFrag );
                } catch ( SAXException e ) {
                    LOG.logError( e.getLocalizedMessage(), e );
                    throw new OGCWebServiceException( null, "The resulting repository item was not of type xml: "
                                                            + e.getLocalizedMessage(),
                                                      CSWExceptionCode.NOAPPLICABLECODE );
                } catch ( IOException e ) {
                    LOG.logError( e.getLocalizedMessage(), e );
                    throw new OGCWebServiceException( null, "The resulting repository item was not of type xml: "
                                                            + e.getLocalizedMessage(),
                                                      CSWExceptionCode.NOAPPLICABLECODE );
                }
            }
        }
        return response;
    }

    /**
     * Generates and sends a GetFeature to the wfsResource.
     * 
     * @param registryObject
     *            the QName of the registryObject e.g. app:RegistryObject (xmlns:app="http://www.deegree.org/app")
     * @param filter
     *            a ogc:Filter representation containing the (app:iduri isequal requestID) mapping.
     * @return the FeatureResult of the given filter or <code>null</code> if something went wrong.
     * @throws OGCWebServiceException
     *             thrown if the wfsResource encounters any problems
     */
    private FeatureResult sendWFSGetFeature( QualifiedName registryObject, ComplexFilter filter )
                            throws OGCWebServiceException {
        Query q = Query.create( registryObject, filter );
        GetFeature gfwl = GetFeature.create( "1.1.0", "0",
                                             org.deegree.ogcwebservices.wfs.operation.GetFeature.RESULT_TYPE.RESULTS,
                                             "text/xml; subtype=gml/3.1.1", "no_handle", -1, 0, -1, -1,
                                             new Query[] { q } );
        // GetFeature gfwl = GetFeature.create( "1.1.0", "0", RESULT_TYPE.RESULTS, "text/xml; subtype=gml/3.1.1",
        // "no_handle", -1, 0, -1, -1, new Query[] { q } );
        if ( LOG.isDebug() ) {
            try {
                GetFeatureDocument gd = org.deegree.ogcwebservices.wfs.XMLFactory.export( gfwl );
                LOG.logDebug( " The getFeature:\n" + gd.getAsPrettyString() );
            } catch ( IOException e ) {
                LOG.logError( "CSW (Ebrim) GetRepositoryItem-Filter:  An error occurred while trying to get a debugging output for the generated GetFeatureDocument: "
                              + e.getMessage() );
            } catch ( XMLParsingException e ) {
                LOG.logError( "CSW (Ebrim) GetRepositoryItem-Filter:  An error occurred while trying to get a debugging output for the generated GetFeatureDocument: "
                              + e.getMessage() );
            }
        }

        Object response = wfsResource.doService( gfwl );
        if ( response instanceof FeatureResult ) {
            return (FeatureResult) response;
        }
        throw new OGCWebServiceException( "No valid response from the backend while retrieving GetRepositoryItem." );
        // LOG.logDebug( "Got no valid response from the wfsResource, returning null" );
        // return null;
    }

}
