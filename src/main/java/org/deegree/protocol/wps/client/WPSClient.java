//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
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
package org.deegree.protocol.wps.client;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.tom.ows.CodeType;
import org.deegree.protocol.ows.exception.OWSException;
import org.deegree.protocol.ows.metadata.ServiceMetadata;
import org.deegree.protocol.wps.client.process.Process;
import org.deegree.protocol.wps.client.process.ProcessExecution;
import org.deegree.protocol.wps.client.process.ProcessInfo;
import org.deegree.protocol.wps.client.wps100.WPS100CapabilitiesAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * API-level client for accessing services that implement the <a
 * href="http://www.opengeospatial.org/standards/wps">WebProcessingService (WPS) 1.0.0</a> protocol.
 * 
 * <h4>Initialization</h4> In the initial step, one constructs a new {@link WPSClient} instance by invoking the
 * constructor with a URL to a WPS capabilities document. In most cases, this will be a GetCapabilities request
 * (including necessary parameters) to a WPS service.
 * 
 * <pre>
 * ...
 *   URL processUrl = new URL( "http://...?service=WPS&version=1.0.0&request=GetCapabilities" );
 *   WPSClient wpsClient = new WPSClient( processUrl );
 * ...
 * </pre>
 * 
 * Afterwards, the {@link WPSClient} instance is bound to the specified service and allows to access announced service
 * metadata, process information as well as the execution of processes.
 * 
 * <h4>Accessing service metadata</h4> The method {@link #getMetadata()} allows to access the metadata announced by the
 * service, such as title, abstract, provider etc.
 * 
 * <h4>Getting process information</h4> The method {@link #getProcesses()} allows to find out about the processes
 * offered by the service. Additionally (if one knows the identifier of a process beforehand), one can use
 * {@link #getProcess(String)} or {@link #getProcess(String, String)} to retrieve a specific process. The
 * {@link Process} class allows to execute a process and offers methods to access detail information such as title,
 * abstract, input parameter types and output parameters types:
 * 
 * <pre>
 * ...
 *   Process buffer = wpsClient.getProcess ("Buffer");
 *   System.out.println ("Abstract for Buffer process: " + buffer.getAbstract());
 *   System.out.println ("Number of input parameters: " + buffer.getInputTypes().length);
 *   System.out.println ("Number of output parameters: " + buffer.getOutputTypes().length);
 * ...
 * </pre>
 * 
 * <h4>Executing a process</h4> When executing a request, the method {@link Process#prepareExecution()} must be used to
 * create a {@link ProcessExecution} context first. This context provides methods for setting the input parameters,
 * controlling the desired output parameter behaviour and invoking the execution.
 * 
 * <pre>
 * ...
 *   Process buffer = wpsClient.getProcess ("Buffer");
 * 
 *   // get execution context
 *   ProcessExecution execution = buffer.prepareExecution();
 *   
 *   // add input parameters
 *   execution.addLiteralInput( "BufferDistance", null, "0.1", "double", "unity" );
 *   execution.addXMLInput( "GMLInput", null, gmlFileUrl, "text/xml", null, null );
 *   
 *   // perform execution
 *   ExecutionOutputs outputs = execution.execute();
 *   
 *   // access individual output values
 *   ComplexOutput bufferedGeometry = outputs.getXML ("BufferedGeometry", null);
 *   XMLStreamReader xmlStream = bufferedGeometry.getAsXMLStream();
 * ...
 * </pre>
 * 
 * <h4>Providing input</h4>Input is straightforwardly specified for the <b>literal and bbox cases</b>: <br/>
 * <br/> {@link ProcessExecution#addLiteralInput(String, String, String, String, String)} respectively <br/>
 * <br/> {@link ProcessExecution#addBBoxInput(String, String, double[], double[], String)}
 * 
 * <pre>
 * ...
 *   execution.addLiteralInput( &quot;BufferDistance&quot;, null, &quot;0.1&quot;, &quot;double&quot;, &quot;unity&quot; );
 *   execution.addBBoxInput( "BBOXInput", null, new double[] { 0, 0 }, new double[] { 90, 180 }, "EPSG:4326" );
 * ...
 * </pre>
 * 
 * For complex data however, the are methods that provide <b>XML and binary data separately</b>. One can provide the
 * <b>input as reference</b> by giving an URL and whether it is external (using http, ftp, etc. protocols) or local
 * (file protocol). <br/> {@link ProcessExecution#addXMLInput(String, String, URL, boolean, String, String, String)} and
 * respectively <br/>
 * <br/> {@link ProcessExecution#addBinaryInput(String, String, URL, boolean, String, String)}
 * 
 * <pre>
 * ...
 *   URL externalURL = new URL( "http://..." );
 *   execution.addXMLInput( "XMLInput1", null, externalURL, true, "text/xml", null, null );
 *   URL localURL = new URL( "file:/home/musterman/xmlInput.xml" );
 *   execution.addXMLInput( "XMLInput2", null, localURL, false, "text/xml", null, null );
 *   
 *   // and for binary data, as local file
 *   execution.addBinaryInput( "BinaryInput1", null, localBinaryURL, false, "image/png", null );
 *   // OR, from the web
 *   execution.addBinaryInput( "BinaryInput2", null, externalBinaryURL, true, "image/png", null );
 * ...
 * </pre>
 * 
 * Alternatively, one can simply provide the <b>input inline as stream</b>. In the case of XML it will be a
 * {@link XMLStreamReader}, while for the binary data expected will be an {@link InputStream}.<br/>
 * <br/> {@link ProcessExecution#addXMLInput(String, String, XMLStreamReader, String, String, String)} respectively <br/>
 * <br/> {@link ProcessExecution#addBinaryInput(String, String, java.io.InputStream, String, String)}
 * 
 * <pre>
 * ...
 *   execution.addXMLInput( "XMLInput", null, reader, "text/xml", null, null );
 *   // respectively, in the case of binary input
 *   execution.addBinaryInput( "BinaryInput", null, is, "image/png", null );
 * ...
 * </pre>
 * 
 * <h4>Controlling output</h4>By omitting to set the outputs, the process will generate the default ones, as specified
 * in the process definition. However, the user can take control on precisely what outputs he/she wants by using
 * {@link ProcessExecution#addOutput(String, String, String, boolean, String, String, String)} and specifying the id of
 * the respective wanted output. Among the parameters there is the possibility of specifying to <b>retrieve the output
 * as reference or inline</b>.
 * 
 * <pre>
 * ...
 *      // BBOXOutput will be returned inline
 *      execution.addOutput( "BBOXOutput", null, null, false, null, null, null );
 *      // BinaryOutput will be returned as reference 
 *      execution.addOutput( "BinaryOutput", null, null, true, null, null, null );
 *      // the other (not specified) outputs will be skipped.
 * ...
 * </pre>
 * 
 * <h4>Executing a process: raw output mode</h4> There is also the possibility of selecting the WPS
 * <code>RawOutput</code> mode, where the (single) output parameter is not going to be wrapped in an ExecuteResponse XML
 * document, but directly returned as a resource. For this one must set {@link Process#prepareRawExecution()} to
 * retrieve a suitable execution context. The server shall respond with this sole output resource.
 * 
 * <pre>
 * ...
 *   Process buffer = wpsClient.getProcess ("Buffer");
 * 
 *   // get raw execution context
 *   RawProcessExecution execution = buffer.prepareRawExecution();
 *   
 *   // add input parameters
 *   execution.addLiteralInput( "BufferDistance", null, "0.1", "double", "unity" );
 *   execution.addXMLInput( "GMLInput", null, gmlFileUrl, "text/xml", null, null );
 *   
 *   // invoke RawOutput mode execution (returns a single output parameter)
 *   ComplexOutput output = execution.execute("GMLOutput", null, "text/xml", null, null);
 * ...
 * </pre>
 * 
 * <h4>Executing a process: asynchronous mode</h4> Instead of using {@link ProcessExecution#execute()}, one can also
 * request asynchronous execution via the {@link ProcessExecution#executeAsync()} method. In the latter case, the call
 * will return immediately, but the result is not necessarily available yet. In order to check for completion, the
 * {@link ProcessExecution#getState()} is available, that will poll the server for the current status (if it wasn't
 * finished anyway). Additionally, the method {@link ProcessExecution#getPercentCompleted()} is available to check the
 * execution progress (if the process supports it).
 * 
 * <pre>
 * ...
 *   Process buffer = wpsClient.getProcess ("Buffer");
 * 
 *   // get execution context
 *   ProcessExecution execution = buffer.prepareExecution();
 *   
 *   // add input parameters
 *   execution.addLiteralInput( "BufferDistance", null, "0.1", "double", "unity" );
 *   execution.addXMLInput( "GMLInput", null, gmlFileUrl, "text/xml", null, null );
 *   
 *   // invoke asynchronous execution (returns immediately)
 *   execution.executeAsync();
 *   
 *   // do other stuff
 *   ...
 *   
 *   // check execution state
 *   if (execution.getState() == SUCCEEDED) {
 *       ExecutionOutputs outputs = execution.getOutputs();
 *       ...
 *       // access the outputs as in synchronous case
 *       ComplexOutput bufferedGeometry = outputs.getXML ("BufferedGeometry", null);
 *       XMLStreamReader xmlStream = bufferedGeometry.getAsXMLStream();
 *   }
 * ...
 * </pre>
 * 
 * <h4>Implementation notes</h4>
 * <ul>
 * <li>Supported protocol versions: WPS 1.0.0</li>
 * <li>The implementation is thread-safe, a single {@link WPSClient} instance can be shared among multiple threads.</li>
 * </ul>
 * 
 * <h4>TODOs</h4>
 * <ul>
 * <li>Implement input parameter passing for POST-references.</li>
 * <li>Cope with exceptions reports that are returned for <code>GetCapabilities</code> and <code>DescribeProcess</code>
 * requests.</li>
 * <li>Determine the correct XML encoding for HTTP-requests/responses, see http://www.ietf.org/rfc/rfc2376.txt</li>
 * <li>Clean up exception handling.</li>
 * <li>Enable/document a way to set connection parameters (timeout, proxy settings, ...)</li>
 * <li>Support metadata in multiple languages (as mandated by the WPS spec).</li>
 * <li>Check validity (cardinality, order) of input and output parameters in {@link ProcessExecution}.</li>
 * </ul>
 * 
 * @see Process
 * @see ProcessExecution
 * 
 * @author <a href="mailto:walenciak@uni-heidelberg.de">Georg Walenciak</a>
 * @author <a href="mailto:kiehle@lat-lon.de">Christian Kiehle</a>
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class WPSClient {

    private static Logger LOG = LoggerFactory.getLogger( WPSClient.class );

    private final ServiceMetadata metadata;

    // [0]: GET, [1]: POST
    private final URL[] describeProcessURLs = new URL[2];

    // [0]: GET, [1]: POST
    private final URL[] executeURLs = new URL[2];

    // using LinkedHashMap because it keeps insertion order
    private final Map<CodeType, Process> processIdToProcess = new LinkedHashMap<CodeType, Process>();

    // using LinkedHashMap because it keeps insertion order
    private final Map<String, Process> processIdSimpleToProcess = new LinkedHashMap<String, Process>();

    /**
     * Creates a new {@link WPSClient} instance.
     * 
     * @param capabilitiesURL
     *            url of a WPS capabilities document, usually this is a GetCapabilities request to a WPS service, must
     *            not be <code>null</code>
     * @throws IOException
     *             if a communication/network problem occured
     * @throws OWSException
     *             if the server replied with an exception
     */
    public WPSClient( URL capabilitiesURL ) throws IOException, OWSException {

        WPS100CapabilitiesAdapter capabilitiesDoc = retrieveCapabilities( capabilitiesURL );

        // TODO what if server only supports Get? What is optional and what is mandatory?
        describeProcessURLs[0] = capabilitiesDoc.getOperationURL( "DescribeProcess", false );
        describeProcessURLs[1] = capabilitiesDoc.getOperationURL( "DescribeProcess", true );
        executeURLs[0] = capabilitiesDoc.getOperationURL( "Execute", false );
        executeURLs[1] = capabilitiesDoc.getOperationURL( "Execute", true );

        metadata = capabilitiesDoc.parseMetadata();

        for ( ProcessInfo processInfo : capabilitiesDoc.getProcesses() ) {
            Process process = new Process( this, processInfo );
            processIdToProcess.put( process.getId(), process );
            processIdSimpleToProcess.put( process.getId().getCode(), process );
        }
    }

    private WPS100CapabilitiesAdapter retrieveCapabilities( URL capabilitiesURL )
                            throws IOException {

        WPS100CapabilitiesAdapter capabilitiesDoc = null;
        try {
            LOG.trace( "Retrieving capabilities document from {}", capabilitiesURL );
            capabilitiesDoc = new WPS100CapabilitiesAdapter();
            capabilitiesDoc.load( capabilitiesURL );
        } catch ( Exception e ) {
            String msg = "Unable to retrieve/parse capabilities document from URL '" + capabilitiesURL + "': "
                         + e.getMessage();
            throw new IOException( msg );
        }

        OMElement root = capabilitiesDoc.getRootElement();
        System.out.println(root.toString());
        String protocolVersion = root.getAttributeValue( new QName( "version" ) );
        if ( !"1.0.0".equals( protocolVersion ) ) {
            String msg = "Capabilities document has unsupported version " + protocolVersion + ".";
            throw new UnsupportedOperationException( msg );
        }
        String service = root.getAttributeValue( new QName( "service" ) );
        if ( !service.equalsIgnoreCase( "WPS" ) ) {
            String msg = "Capabilities document is not a WPS capabilities document.";
            throw new IllegalArgumentException( msg );
        }
        return capabilitiesDoc;
    }

    /**
     * Returns the WPS protocol version in use.
     * <p>
     * NOTE: Currently, this is always "1.0.0" (as the client only supports this version).
     * </p>
     * 
     * @return the WPS protocol version in use, never <code>null</code>
     */
    public String getServiceVersion() {
        return "1.0.0";
    }

    /**
     * Returns the metadata (ServiceIdentification, ServiceProvider) of the service.
     * 
     * @return the metadata of the service, never <code>null</code>
     */
    public ServiceMetadata getMetadata() {
        return metadata;
    }

    /**
     * Returns all processes offered by the service.
     * 
     * @return all processes offered by the service, may be empty, but never <code>null</code>
     */
    public Process[] getProcesses() {
        return processIdToProcess.values().toArray( new Process[processIdToProcess.size()] );
    }

    /**
     * Returns the specified process instance (ignoring the codespace of the identifier).
     * <p>
     * NOTE: This is a convenience method that ignores the optional codespace that a process identifier may have. If a
     * server actually offers two processes with the same identifier, but different codespace, let's say 'Buffer'
     * (codespace: 'Sextante') and 'Buffer' (codespace: 'GRASS'), then it's not defined which of the ones will be
     * returned, when this method is called with parameter 'Buffer'. To be on the safe side, use
     * {@link #getProcess(String, String)}.
     * </p>
     * 
     * @param id
     *            process identifier, never <code>null</code>
     * @return process instance, can be <code>null</code> (if no process with the specified identifier and code space is
     *         offered by the services)
     */
    public Process getProcess( String id ) {
        return processIdSimpleToProcess.get( id );
    }

    /**
     * Returns the specified process instance.
     * 
     * @param id
     *            process identifier, never <code>null</code>
     * @param idCodeSpace
     *            codespace of the process identifier, may be <code>null</code> (for identifiers that don't use a code
     *            space)
     * @return process instance, can be <code>null</code> (if no process with the specified identifier and code space is
     *         offered by the services)
     */
    public Process getProcess( String id, String idCodeSpace ) {
        return processIdToProcess.get( new CodeType( id, idCodeSpace ) );
    }

    /**
     * Returns the URL announced by the service for issuing <code>DescribeProcess</code> requests.
     * 
     * @param post
     *            if set to true, the URL for POST requests will be returned, otherwise the URL for GET requests will be
     *            returned
     * @return the <code>DescribeProcess</code> URL, may be <code>null</code> (if the server doesn't provide a binding
     *         for the specified request method)
     */
    public URL getDescribeProcessURL( boolean post ) {
        return describeProcessURLs[post ? 1 : 0];
    }

    /**
     * Returns the URL announced by the service for issuing <code>Execute</code> requests.
     * 
     * @param post
     *            if set to true, the URL for POST requests will be returned, otherwise the URL for GET requests will be
     *            returned
     * @return the <code>Execute</code> URL, may be <code>null</code> (if the server doesn't provide a binding for the
     *         specified request method)
     */
    public URL getExecuteURL( boolean post ) {
        return executeURLs[post ? 1 : 0];
    }
}
