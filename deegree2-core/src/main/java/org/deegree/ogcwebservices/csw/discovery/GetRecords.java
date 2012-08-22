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

import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.StringTools;
import org.deegree.framework.xml.NamespaceContext;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.framework.xml.XMLTools;
import org.deegree.i18n.Messages;
import org.deegree.model.filterencoding.AbstractFilter;
import org.deegree.model.filterencoding.Filter;
import org.deegree.ogcbase.CommonNamespaces;
import org.deegree.ogcbase.ExceptionCode;
import org.deegree.ogcbase.SortProperty;
import org.deegree.ogcwebservices.InvalidParameterValueException;
import org.deegree.ogcwebservices.MissingParameterValueException;
import org.deegree.ogcwebservices.OGCWebServiceException;
import org.deegree.ogcwebservices.OperationNotSupportedException;
import org.deegree.ogcwebservices.csw.AbstractCSWRequest;
import org.deegree.ogcwebservices.csw.CSWPropertiesAccess;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Class representation of a <code>GetRecords</code> request.
 * <p>
 * The primary means of resource discovery in the general model are the two operations search and present. In the HTTP
 * protocol binding these are combined in the form of the mandatory <code>GetRecords</code> operation, which does a
 * search.
 * <p>
 * Parameters specific to the <code>GetRecords</code> -request (omitting REQUEST, SERVICE and VERSION):
 * <table * border="1">
 * <tr>
 * <th>Name</th>
 * <th>Occurences</th>
 * <th>Function</th>
 * </tr>
 * <tr>
 * <td align="center">NAMESPACE</td>
 * <td align="center">0|1</td>
 * <td>The NAMESPACE parameter is included in the KVP encoding to allow clients to bind any namespace prefixes that
 * might be used for qualified names specified in other parameters. For example, the typeName parameter may include
 * qualified names of the form namespace prefix:name. The value of the NAMESPACE parameter is a comma separated list of
 * character strings of the form [namespace prefix:] namespace url. Not including the name namespace prefix binds the
 * specified URL to the default namespace. As in XML, only one default namespace may be bound. This parameter is not
 * required for the XML encoding since XML includes a mechanism for binding namespace prefixes.</td>
 * </tr>
 * <tr>
 * <td align="center">resultType</td>
 * <td align="center">0|1 (default: RESULTS)</td>
 * <td>The resultType parameter may have the values HITS, RESULTS or VALIDATE and is used to indicate whether the
 * catalogue service returns the full result set, the number of hits the query found or validates the request. If the
 * resultType parameter is set to HITS, the catalogue service shall return an empty &lt;GetRecordsResponse&gt; element
 * with the numberOfRecordsMatched attribute set to indicate the number of hits. The other attributes may be set to zero
 * or not specified at all if they are optional. If the resultType parameter is set to HITS, then the values for the
 * parameters outputFormat and outputSchema (if specified) shall be ignored since no actual records will be returned. If
 * the resultType parameter is set to RESULTS, the catalogue service should generate a complete response with the
 * &lt;GetRecordsResponse&gt; element containing the result set for the request. If the resultType parameter is set to
 * VALIDATE, the catalogue service shall validate the request and return an empty &lt;GetRecordsResponse&gt;. All
 * mandatory attributes may be given a value of zero and all optional attributes may be omitted. If the request does not
 * validate then a service exception shall be raised as describe in Subclause 10.3.2.3.</td>
 * </tr>
 * <tr>
 * <td align="center">outputFormat</td>
 * <td align="center">0|1 (default: text/xml)</td>
 * <td>The outputFormat parameter is used to control the format of the output that is generated in response to a
 * GetRecords request. Its value must be a MIME type. The default value, "text/xml", means that the output shall be an
 * XML document. All registries shall at least support XML as an output format. Other output formats may be supported
 * and may include output formats such as TEXT (MIME type text/plain), or HTML (MIME type text/html). The list of output
 * formats that a CSW instance provides must be advertised in the Capabilities document. In the case where the output
 * format is text/xml, the CSW must generate an XML document that validates against a schema document that is specified
 * in the output document via the xsi:schemaLocation attribute defined in XML.</td>
 * </tr>
 * <tr>
 * <td align="center">outputSchema</td>
 * <td align="center">0|1 (default: OGCCORE)</td>
 * <td>The outputSchema parameter is used to indicate the schema of the output that is generated in response to a
 * GetRecords request. The default value for this parameter shall be OGCCORE indicating that the schema for the core
 * returnable properties (as defined in subclause 6.3.3) shall be used. Application profiles may define additional
 * values for outputSchema and may redefine the default value but all profiles must support the value OGCCORE. Examples
 * values for the outputSchema parameter might be FGDC, or ISO19119, ISO19139 or ANZLIC. The list of supported output
 * schemas must be advertised in the capabilities document.
 * </tr>
 * <tr>
 * <td align="center">startPosition</td>
 * <td align="center">0|1 (default: 1)</td>
 * <td>The startPosition paramater is used to indicate at which record position the catalogue should start generating
 * output. The default value is 1 meaning it starts at the first record in the result set.</td>
 * </tr>
 * <tr>
 * <td align="center">maxRecords</td>
 * <td align="center">0|1 (default: 10)</td>
 * <td>The maxRecords parameter is used to define the maximum number of records that should be returned from the result
 * set of a query. If it is not specified, then 10 records shall be returned. If its value is set to zero, then the
 * behavior is indentical to setting "resultType=HITS" as described above.</td>
 * </tr>
 * <tr>
 * <td align="center">typeName</td>
 * <td align="center">1</td>
 * <td>The typeName parameter is a list of record type names that define a set of metadata record element names which
 * will be constrained in the predicate of the query. In addition, all or some of the these names may be specified in
 * the query to define which metadata record elements the query should present in the response to the GetRecords
 * operation.</td>
 * </tr>
 * <tr>
 * <td align="center">ElementSetName / ElementName</td>
 * <td align="center">* (default: 10)</td>
 * <td>The ElementName parameter is used to specify one or more metadata record elements that the query should present
 * in the response to the a GetRecords operation. Well known sets of element may be named, in which case the
 * ElementSetName parameter may be used (e.g.brief, summary or full). If neither parameter is specified, then a CSW
 * shall present all metadata record elements. As mentioned above, if the outputFormat parameter is set to text/xml,
 * then the response to the GetRecords operation shall validate against a schema document that is referenced in the
 * response using the xmlns attributes. If the set of metadata record elements that the client specifies in the query in
 * insufficient to generate a valid XML response document, a CSW may augment the list of elements presented to the
 * client in order to be able to generate a valid document. Thus a client application should expect to receive more than
 * the requested elements if the output format is set to XML.</td>
 * </tr>
 * <tr>
 * <td align="center">CONSTRAINTLANGUAGE / Constraint</td>
 * <td align="center">0|1</td>
 * <td>Each request encoding (XML and KVP) has a specific mechanism for specifying the predicate language that will be
 * used to constrain a query. In the XML encoding, the element &lt;Constraint&gt; is used to define the query predicate.
 * The root element of the content of the &lt;Constraint&gt; element defines the predicate language that is being used.
 * Two possible root elements are &lt;ogc:Filter&gt; for the OGC XML filter encoding, and &lt;csw:CqlText&gt; for a
 * common query language string. An example predicate specification in the XML encoding is:
 * 
 * &lt;Constraint&gt; &lt;CqlText&gt;prop1!=10&lt;/CqlText&gt; &lt;/Constraint&gt;
 * 
 * In the KVP encoding, the parameter CONSTRAINTLANGUAGE is used to specify the predicate language being used. The
 * Constraint parameter is used to specify the actual predicate. For example, to specify a CQL predicate, the following
 * parameters would be set in the KVP encoding: <br>
 * 
 * ...CONSTRAINTLANGUAGE=CQL_TEXT&amp;CONSTRAINT=&quot;prop1!=10&quot;...
 * 
 * </td>
 * </tr>
 * <tr>
 * <td align="center">SortBy</td>
 * <td align="center">0|1</td>
 * <td>The result set may be sorted by specifying one or more metadata record elements upon which to sort. In KVP
 * encoding, the SORTBY parameter is used to specify the list of sort elements. The value for the SORTBY parameter is a
 * comma-separated list of metadata record element names upon which to sort the result set. The format for each element
 * in the list shall be either element name:A indicating that the element values should be sorted in ascending order or
 * element name:D indicating that the element values should be sorted in descending order. For XML encoded requests, the
 * &lt;ogc:SortBy&gt; element is used to specify a list of sort metadata record elements. The attribute sortOrder is
 * used to specify the sort order for each element. Valid values for the sortOrder attribute are ASC indicating an
 * ascending sort and DESC indicating a descending sort.</td>
 * </tr>
 * <tr>
 * <td align="center">DistributedSearch / hopCount</td>
 * <td align="center">0|1 (default: FALSE)</td>
 * <td>The DistributedSearch parameter may be used to indicate that the query should be distributed. The default query
 * behaviour, if the DistributedSearch parameter is set to FALSE (or is not specified at all), is to execute the query
 * on the local server. In the XML encoding, if the &lt;DistributedSearch&gt; element is not specified then the query is
 * executed on the local server. <br>
 * <br>
 * The hopCount parameter controls the distributed query behaviour by limiting the maximum number of message hops before
 * the search is terminated. Each catalogue decrements this value by one when the request is received and does not
 * propagate the request if the hopCount=0.</td>
 * </tr>
 * <tr>
 * <td align="center">ResponseHandler</td>
 * <td align="center">0|1</td>
 * <td>The ResponseHandler parameter is a flag that indicates how the GetRecords operation should be processed by a CSW.
 * If the parameter is not present, then the GetRecords operation is processed synchronously meaning that the client
 * sends the GetRecords request to a CSW and waits to receive a valid response or exception message. The CSW immediately
 * processes the GetRecords request while the client waits for a response. The problem with this mode of operation is
 * that the client may timeout waiting for the CSW to process the request. If the ResponseHandler parameter is present,
 * the GetRecords operation is processed asynchronously. In this case, the CSW responds immediately to a client's
 * request with an acknowledgment message that tells the client that the request has been received and validated, and
 * notification of completion will be sent to the URI specified as the value of the ResponseHandler parameter.</td>
 * </tr>
 * </table>
 * 
 * @author <a href="mailto:mschneider@lat-lon.de">Markus Schneider </a>
 * @version $Revision$
 * 
 * 
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth </a>
 * @author <a href="mailto:tfr@users.sourceforge.net">Torsten Friebe </a>
 * @author <a href="mailto:mschneider@lat-lon.de">Markus Schneider </a>
 * 
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */

public class GetRecords extends AbstractCSWRequest {

    private static final long serialVersionUID = 2796229558893029054L;

    private static final ILogger LOG = LoggerFactory.getLogger( GetRecords.class );

    protected static final String DEFAULT_OUTPUTFORMAT = "application/xml";

    protected static final String DEFAULT_OUTPUTSCHEMA = "csw:Record";

    protected static final String DEFAULT_OUTPUTSCHEMA_202 = "http://www.opengis.net/cat/csw/2.0.2";

    protected static final int DEFAULT_STARTPOSITION = 1;

    protected static final int DEFAULT_MAX_RECORDS = 10;

    protected static final int DEFAULT_HOPCOUNT = 2;

    protected static final String DEFAULT_VERSION = "2.0.0";

    /**
     * defining HITS as String
     */
    public static String RESULT_TYPE_STRING_HITS = "hits";

    /**
     * defining VALIDATE as String
     */
    public static String RESULT_TYPE_STRING_VALIDATE = "validate";

    /**
     * defining RESULTS as String
     */
    public static String RESULT_TYPE_STRING_RESULTS = "results";

    private static NamespaceContext nsContext = CommonNamespaces.getNamespaceContext();

    private RESULT_TYPE resultType = RESULT_TYPE.RESULTS;

    // keys are Strings (namespace prefix or "" for default namespace), values
    // are URIs
    private Map<String, URI> namespace;

    private String outputFormat;

    private String outputSchema;

    private int startPosition;

    private int maxRecords;

    private int hopCount;

    private URI responseHandler;

    // private Query[] queries;

    private Query query;

    /**
     * Creates a new <code>GetRecords</code> instance.
     * 
     * @param id
     * @param version
     * @param vendorSpecificParameters
     * @param namespace
     * @param resultType
     * @param outputFormat
     * @param outputSchema
     * @param startPosition
     * @param maxRecords
     * @param hopCount
     * @param responseHandler
     * @param query
     */
    public GetRecords( String id, String version, Map<String, String> vendorSpecificParameters,
                       Map<String, URI> namespace, RESULT_TYPE resultType, String outputFormat, String outputSchema,
                       int startPosition, int maxRecords, int hopCount, URI responseHandler, Query query ) {
        super( version, id, vendorSpecificParameters );
        this.namespace = namespace;
        this.resultType = resultType;
        this.outputFormat = outputFormat;
        this.outputSchema = outputSchema;
        this.startPosition = startPosition;
        this.maxRecords = maxRecords;
        this.hopCount = hopCount;
        this.responseHandler = responseHandler;
        this.query = query;
    }

    /**
     * creates a GetRecords request from the XML fragment passed. The passed element must be valid against the OGC CSW
     * 2.0 GetRecords schema.
     * 
     * TODO respect namespaces (use QualifiedNames) for type names
     * 
     * @param id
     *            unique ID of the request
     * @param root
     *            root element of the GetRecors request
     * @return a GetRecords instance with given id and parsed values from the root element
     * @throws MissingParameterValueException
     *             if a required parameter was not set
     * @throws InvalidParameterValueException
     *             if a parameter is invalid
     * @throws OGCWebServiceException
     *             if something went wrong while creating the Request
     */
    public static GetRecords create( String id, Element root )
                            throws MissingParameterValueException, InvalidParameterValueException,
                            OGCWebServiceException {
        String version = null;
        try {
            // first try to read verdsion attribute which is optional for CSW 2.0.0 and 2.0.1
            version = XMLTools.getNodeAsString( root, "./@version", nsContext, null );
        } catch ( XMLParsingException e ) {
            // default version?
        }
        if ( version == null ) {
            // if no version attribute has been set try mapping namespace URI to a version;
            // this is not well defined for 2.0.0 and 2.0.1 which uses the same namespace.
            // in this case 2.0.0 will be returned!
            version = CSWPropertiesAccess.getString( root.getNamespaceURI() );
        }

        // read class for version depenging parsing of GetRecords request from properties
        String className = CSWPropertiesAccess.getString( "GetRecords" + version );
        Class<?> clzz = null;
        try {
            clzz = Class.forName( className );
        } catch ( ClassNotFoundException e ) {
            LOG.logError( e.getMessage(), e );
            throw new InvalidParameterValueException( e.getMessage(), e );
        }
        GetRecordsDocument document = null;
        try {
            document = (GetRecordsDocument) clzz.newInstance();
        } catch ( InstantiationException e ) {
            LOG.logError( e.getMessage(), e );
            throw new InvalidParameterValueException( e.getMessage(), e );
        } catch ( IllegalAccessException e ) {
            LOG.logError( e.getMessage(), e );
            throw new InvalidParameterValueException( e.getMessage(), e );
        }

        document.setRootElement( root );

        GetRecords ogcRequest = document.parse( id );

        return ogcRequest;
    }

    /**
     * Creates a new <code>GetRecords</code> instance from the values stored in the submitted Map. Keys (parameter
     * names) in the Map must be uppercase.
     * 
     * @TODO evaluate vendorSpecificParameter
     * 
     * @param kvp
     *            Map containing the parameters
     * @return a GetRecords instance with given id and values from the kvp
     * @exception InvalidParameterValueException
     * @exception MissingParameterValueException
     * @throws OperationNotSupportedException
     *             if an CQL_TEXT constrain is requested
     */
    public static GetRecords create( Map<String, String> kvp )
                            throws InvalidParameterValueException, MissingParameterValueException,
                            OperationNotSupportedException {

        // String version = "2.0.0";
        // Map<String, String> vendorSpecificParameters = null;
        // RESULT_TYPE resultType = RESULT_TYPE.HITS;
        // String outputFormat = "text/xml";
        // String outputSchema = "OGCCORE";
        // int startPosition = 1;
        // int maxRecords = 10;
        // int hopCount = 2;

        String service = getParam( "SERVICE", kvp, "CSW" );
        if ( !"CSW".equals( service ) ) {
            throw new InvalidParameterValueException( "GetRecordDocument",
                                                      Messages.getMessage( "CSW_INVALID_SERVICE_PARAM" ),
                                                      ExceptionCode.INVALIDPARAMETERVALUE );
        }

        String id = getParam( "ID", kvp, "" );
        LOG.logDebug( "GetRecordRequest id=" + id );

        String version = getParam( "VERSION", kvp, DEFAULT_VERSION );
        if ( !( DEFAULT_VERSION.equals( version ) || "2.0.1".equals( version ) || "2.0.2".equals( version ) ) ) {
            throw new InvalidParameterValueException( "GetRecords", Messages.getMessage( "CSW_NOT_SUPPORTED_VERSION",
                                                                                         GetRecords.DEFAULT_VERSION,
                                                                                         "2.0.1", "2.0.2", version ),
                                                      ExceptionCode.INVALIDPARAMETERVALUE );
        }

        // extract namespace mappings
        Map<String, URI> namespaceMappings = getNSMappings( getParam( "NAMESPACE", kvp, null ) );

        String resultTypeString = getParam( "RESULTTYPE", kvp, RESULT_TYPE_STRING_HITS );
        RESULT_TYPE resultType = RESULT_TYPE.HITS;
        if ( RESULT_TYPE_STRING_HITS.equalsIgnoreCase( resultTypeString ) ) {
            resultType = RESULT_TYPE.HITS;
        } else if ( RESULT_TYPE_STRING_RESULTS.equalsIgnoreCase( resultTypeString ) ) {
            resultType = RESULT_TYPE.RESULTS;
        } else if ( RESULT_TYPE_STRING_VALIDATE.equalsIgnoreCase( resultTypeString ) ) {
            resultType = RESULT_TYPE.VALIDATE;
        } else {
            throw new InvalidParameterValueException( "GetRecords",
                                                      Messages.getMessage( "CSW_INVALID_RESULTTYPE", resultTypeString,
                                                                           GetRecords.RESULT_TYPE_STRING_HITS,
                                                                           GetRecords.RESULT_TYPE_STRING_RESULTS,
                                                                           GetRecords.RESULT_TYPE_STRING_VALIDATE ),
                                                      ExceptionCode.INVALIDPARAMETERVALUE );
        }

        String outputFormat = getParam( "OUTPUTFORMAT", kvp, DEFAULT_OUTPUTFORMAT );
        String defaultOutputSchema = DEFAULT_OUTPUTSCHEMA;
        if ( version.equals( "2.0.2" ) ) {
            defaultOutputSchema = DEFAULT_OUTPUTSCHEMA_202;
        }
        String outputSchema = getParam( "OUTPUTSCHEMA", kvp, defaultOutputSchema );
        int startPosition = getParamAsInt( "STARTPOSITION", kvp, DEFAULT_STARTPOSITION );
        if ( startPosition < 1 ) {
            String msg = Messages.getMessage( "CSW_INVALID_STARTPOSITION", new Integer( startPosition ) );
            throw new InvalidParameterValueException( msg );
        }
        int maxRecords = getParamAsInt( "MAXRECORDS", kvp, DEFAULT_MAX_RECORDS );

        if ( maxRecords < 0 ) {
            maxRecords = DEFAULT_MAX_RECORDS;
        }

        // build one Query object for each specified typeName
        String tmp = getRequiredParam( "TYPENAMES", kvp );
        String[] typeNames = StringTools.toArray( tmp, ",", false );
        if ( typeNames.length == 0 ) {
            throw new MissingParameterValueException( "Mandatory parameter 'TYPENAMES' is missing!" );
        }

        String elementSetName = kvp.remove( "ELEMENTSETNAME" );
        String elementName = kvp.remove( "ELEMENTNAME" );
        String[] elementNames = null;

        if ( version.equals( "2.0.2" ) ) {
            if ( elementSetName == null ) {
                elementSetName = "summary";
            } else {
                if ( elementName != null ) {
                    LOG.logInfo( Messages.getMessage( "CSW_ELEMENT_SET_NAME_DUPLICATE" ) );
                } else {
                    elementNames = StringTools.toArray( elementName, ",", false );
                }
            }
        } else {

            if ( elementSetName == null ) {
                elementSetName = kvp.remove( "ELEMENTNAME" );
            } else {
                String test = kvp.remove( "ELEMENTNAME" );
                if ( test != null ) {
                    LOG.logInfo( Messages.getMessage( "CSW_ELEMENT_SET_NAME_DUPLICATE" ) );
                }
            }

            if ( elementSetName != null ) {
                elementNames = StringTools.toArray( elementSetName, ",", false );
                if ( elementNames.length == 0 ) {
                    elementNames = null;
                }
            }
            if ( elementNames == null ) {
                elementNames = new String[] { "full" };
            }

        }

        String constraintString = kvp.remove( "CONSTRAINT" );
        if ( constraintString == null ) {
            // not really clear if CSW 2.0.2 uses parameter QUERYCONSTRAINT instead
            constraintString = kvp.remove( "QUERYCONSTRAINT" );
        }
        Filter constraint = null;
        String constraintLanguage = null;
        String cnstrntVersion = null;
        if ( constraintString != null ) {
            // build Filter object (from CONSTRAINT parameter)
            constraintLanguage = kvp.remove( "CONSTRAINTLANGUAGE" );
            if ( constraintLanguage != null ) {
                if ( "CQL_TEXT".equalsIgnoreCase( constraintLanguage.trim() ) ) {
                    throw new OperationNotSupportedException( Messages.getMessage( "CSW_NO_CQL_IMPLEMENTATION" ) );
                } else if ( !"FILTER".equalsIgnoreCase( constraintLanguage.trim() ) ) {
                    throw new InvalidParameterValueException( Messages.getMessage( "CSW_INVALID_CONSTRAINT_LANGUAGE",
                                                                                   constraintLanguage.trim() ) );
                }
            } else {
                throw new InvalidParameterValueException( Messages.getMessage( "CSW_CQL_NOR_FILTER_KVP" ) );
            }
            cnstrntVersion = kvp.remove( "CONSTRAINT_LANGUAGE_VERSION" );
            if ( "2.0.2".equals( version ) && cnstrntVersion == null ) {
                throw new InvalidParameterValueException(
                                                          Messages.getMessage( "CSW_MISSING_CONSTRAINT_LANGUAGE_VERSION" ) );
            }

            try {
                Document doc = XMLTools.parse( new StringReader( constraintString ) );
                Element element = doc.getDocumentElement();
                constraint = AbstractFilter.buildFromDOM( element, "1.0.0".equals( cnstrntVersion ) );
            } catch ( Exception e ) {
                String msg = "An error occured when parsing the 'CONSTRAINT' parameter " + "Filter expression: "
                             + e.getMessage();
                throw new InvalidParameterValueException( msg );
            }
        }

        SortProperty[] sortProperties = SortProperty.create( kvp.remove( "SORTBY" ), namespaceMappings );

        // Query[] queries = new Query[typeNames.length];
        // for ( int i = 0; i < typeNames.length; i++ ) {
        Query query = new Query( elementSetName, elementNames, constraint, sortProperties, typeNames );
        // }

        // find out if the query should be performed locally or in a distributed
        // fashion
        int hopCount = DEFAULT_HOPCOUNT;
        String distributedSearch = getParam( "DISTRIBUTEDSEARCH", kvp, "false" );
        if ( distributedSearch.equalsIgnoreCase( "true" ) ) {
            hopCount = getParamAsInt( "HOPCOUNT", kvp, DEFAULT_HOPCOUNT );
        }

        String rHandler = kvp.remove( "RESPONSEHANDLER" );
        URI responseHandler = null;
        if ( rHandler != null ) {
            try {
                responseHandler = new URI( rHandler );
            } catch ( URISyntaxException e ) {
                throw new InvalidParameterValueException(
                                                          Messages.getMessage( "CSW_INVALID_RESPONSE_HANDLER", rHandler ) );
            }
            throw new OperationNotSupportedException( Messages.getMessage( "CSW_NO_REPONSE_HANDLER_IMPLEMENTATION" ) );

        }

        return new GetRecords( id, version, kvp, namespaceMappings, resultType, outputFormat, outputSchema,
                               startPosition, maxRecords, hopCount, responseHandler, query );
    }

    /**
     * Used to specify a namespace and its prefix. Format must be [ <prefix>:] <url>. If the prefix is not specified
     * then this is the default namespace
     * <p>
     * Zero or one (Optional) ; Include value for each distinct namespace used by all qualified names in the request. If
     * not included, all qualified names are in default namespace
     * <p>
     * The NAMESPACE parameter is included in the KVP encoding to allow clients to bind any namespace prefixes that
     * might be used for qualified names specified in other parameters. For example, the typeName parameter may include
     * qualified names of the form namespace prefix:name.
     * <p>
     * The value of the NAMESPACE parameter is separated list of character strings of the form [namespace
     * prefix:]namespace url. Not including the name namespace prefix binds the specified URL to the default namespace.
     * As in XML, only one default namespace may be bound.
     * 
     * @return the mapped namespaces or <code>null</code> if all qualified names are in default namespace.
     * 
     */
    public Map<String, URI> getNamespace() {
        return this.namespace;
    }

    /**
     * The resultType parameter may have the values HITS, RESULTS or VALIDATE and is used to indicate whether the
     * catalogue service returns the full result set, the number of hits the query found or validates the request.
     * <p>
     * If the resultType parameter is set to HITS, the catalogue service shall return an empty
     * &lt;GetRecordsResponse&gt;element with the numberOfRecordsMatched attribute set to indicate the number of hits.
     * The other attributes may be set to zero or not specified at all if they are optional.
     * <p>
     * If the resultType parameter is set to HITS, then the values for the parameters outputFormat and outputSchema (if
     * specified) shall be ignored since no actual records will be returned
     * <p>
     * If the resultType parameter is set to RESULTS, the catalogue service should generate a complete response with the
     * &lt;GetRecordsResponse&gt;element containing the result set for the request
     * <p>
     * If the resultType parameter is set to VALIDATE, the catalogue service shall validate the request and return an
     * empty &lt;GetRecordsResponse&gt;. All mandatory attributes may be given a value of zero and all optional
     * attributes may be omitted. If the request does not validate then a service exception shall be raised
     * 
     * @return one of HITS, RESULTS or VALIDATE
     * 
     */
    public RESULT_TYPE getResultType() {
        return this.resultType;
    }

    /**
     * The resultType parameter may have the values HITS, RESULTS or VALIDATE and is used to indicate whether the
     * catalogue service returns the full result set, the number of hits the query found or validates the request.
     * <p>
     * If the resultType parameter is set to HITS, the catalogue service shall return an empty
     * &lt;GetRecordsResponse&gt;element with the numberOfRecordsMatched attribute set to indicate the number of hits.
     * The other attributes may be set to zero or not specified at all if they are optional.
     * <p>
     * If the resultType parameter is set to HITS, then the values for the parameters outputFormat and outputSchema (if
     * specified) shall be ignored since no actual records will be returned
     * <p>
     * If the resultType parameter is set to RESULTS, the catalogue service should generate a complete response with the
     * &lt;GetRecordsResponse&gt;element containing the result set for the request
     * <p>
     * If the resultType parameter is set to VALIDATE, the catalogue service shall validate the request and return an
     * empty &lt;GetRecordsResponse&gt;. All mandatory attributes may be given a value of zero and all optional
     * attributes may be omitted. If the request does not validate then a service exception shall be raised
     * 
     * @return the resulttype as a String, one of "HITS", "VALIDATE" or "RESULTS"
     * 
     */
    public String getResultTypeAsString() {
        String resultTypeString = null;
        switch ( this.resultType ) {
        case HITS: {
            resultTypeString = RESULT_TYPE_STRING_HITS;
            break;
        }
        case RESULTS: {
            resultTypeString = RESULT_TYPE_STRING_RESULTS;
            break;
        }
        case VALIDATE: {
            resultTypeString = RESULT_TYPE_STRING_VALIDATE;
            break;
        }
        }
        return resultTypeString;
    }

    /**
     * sets the resultType of a request. This may be useful to perform a request first with resultType = HITS to
     * determine the total number of records matching a query and afterwards performing the same request with resultType
     * = RESULTS (and maxRecords &lt; number of matched records).
     * 
     * @param resultType
     */
    public void setResultType( RESULT_TYPE resultType ) {
        this.resultType = resultType;
    }

    /**
     * setst the startPosition of a request. This may be useful to perform a request first with startPosition = 0 to
     * determine the total number of records matching a query.
     * 
     * @param startPosition
     */
    public void setStartPosition( int startPosition ) {
        this.startPosition = startPosition;
    }

    /**
     * returns <= 0 if no distributed search shall be performed. otherwise the recursion depht is returned.
     * <p>
     * The hopCount parameter controls the distributed query behaviour by limiting the maximum number of message hops
     * before the search is terminated. Each catalogue decrements this value by one when the request is received and
     * does not propagate the request if the hopCount=0
     * 
     * @return <= 0 if no distributed search shall be performed. otherwise the recursion depht is returned.
     * 
     */
    public int getHopCount() {
        return this.hopCount;
    }

    /**
     * Value is Mime type;The only value that must be supported is text/xml. Other suppored values may include text/html
     * and text/plain
     * <p>
     * The outputFormat parameter is used to control the format of the output that is generated in response to a
     * GetRecords request. Its value must be a MIME type. The default value, "text/xml", means that the output shall be
     * an XML document. All registries shall at least support XML as an output format. Other output formats may be
     * supported and may include output formats such as TEXT (MIME type text/plain), or HTML (MIME type text/html). The
     * list of output formats that a CSW instance provides must be advertised in the Capabilities document
     * <p>
     * In the case where the output format is text/xml, the CSW must generate an XML document that validates against a
     * schema document that is specified in the output document via the xsi:schemaLocation attribute defined in XML
     * 
     * @return Value is a Mime type
     * 
     */
    public String getOutputFormat() {
        return this.outputFormat;
    }

    /**
     * The outputSchema parameter is used to indicate the schema of the output that is generated in response to a
     * GetRecords request. The default value for this parameter shall be OGCCORE indicating that the schema for the core
     * returnable properties shall be used. Application profiles may define additional values for outputSchema and may
     * redefine the default value but all profiles must support the value OGCCORE
     * <p>
     * Examples values for the outputSchema parameter might be FGDC, or ISO19119, ISO19139 or ANZLIC. The list of
     * supported output schemas must be advertised in the capabilities document
     * 
     * @return The default value for this parameter shall be OGCCORE
     * 
     */
    public String getOutputSchema() {
        return this.outputSchema;
    }

    /**
     * @return the number of the first returned dataset. Zero or one (Optional)Default value is 1. If startPosition >
     *         the number of datasets satisfying the constraint, no dataset will be returned
     * 
     */
    public int getStartPosition() {
        return this.startPosition;
    }

    /**
     * @return The maxRecords parameter. It is used to define the maximum number of records that should be returned from
     *         the result set of a query. If it is not specified, then 10 records shall be returned. If its value is set
     *         to zero, then the behavior is indentical to setting "resultType=HITS"
     * 
     */
    public int getMaxRecords() {
        return this.maxRecords;
    }

    /**
     * @return the location of a response adress to which an asynchronous result may be sent.
     */
    public URI getResponseHandler() {
        return responseHandler;
    }

    /**
     * @return the query object.
     */
    public Query getQuery() {
        return query;
    }

    /**
     * @see #getQuery()
     * @param query
     */
    public void setQuery( Query query ) {
        this.query = query;
    }

    /**
     * The <code>RESULT_TYPE</code> a simple enum which defines some result values of a GetRecord.
     * 
     * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
     * 
     * @author last edited by: $Author$
     * 
     * @version $Revision$, $Date$
     * 
     */

    public static enum RESULT_TYPE {
        /**
         * HITS, the catalogue service shall return an empty &lt;GetRecordsResponse&gt;element with the
         * numberOfRecordsMatched attribute set to indicate the number of hits
         */
        HITS,
        /**
         * VALIDATE, the catalogue service shall validate the request
         */
        VALIDATE,
        /**
         * RESULTS, the catalogue service should generate a complete response with the &lt;GetRecordsResponse&gt;element
         * containing the result set for the request
         */
        RESULTS
    }
}
