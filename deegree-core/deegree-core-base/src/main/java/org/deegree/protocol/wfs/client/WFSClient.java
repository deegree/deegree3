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
package org.deegree.protocol.wfs.client;

import static org.deegree.protocol.wfs.WFSRequestType.DescribeFeatureType;
import static org.deegree.protocol.wfs.WFSVersion.WFS_100;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.tom.gml.GMLObject;
import org.deegree.commons.tom.ows.Version;
import org.deegree.feature.types.AppSchema;
import org.deegree.feature.types.FeatureType;
import org.deegree.gml.GMLInputFactory;
import org.deegree.gml.GMLStreamReader;
import org.deegree.gml.GMLVersion;
import org.deegree.gml.feature.StreamFeatureCollection;
import org.deegree.gml.feature.schema.AppSchemaXSDDecoder;
import org.deegree.protocol.ows.client.AbstractOWSClient;
import org.deegree.protocol.ows.exception.OWSException;
import org.deegree.protocol.ows.exception.OWSExceptionReport;
import org.deegree.protocol.wfs.WFSRequestType;
import org.deegree.protocol.wfs.WFSVersion;
import org.deegree.protocol.wfs.capabilities.WFS100CapabilitiesAdapter;
import org.deegree.protocol.wfs.capabilities.WFS110CapabilitiesAdapter;
import org.deegree.protocol.wfs.capabilities.WFS200CapabilitiesAdapter;
import org.deegree.protocol.wfs.capabilities.WFSCapabilitiesAdapter;
import org.deegree.protocol.wfs.getgmlobject.GetGmlObject;
import org.deegree.protocol.wfs.metadata.WFSFeatureType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * API-level client for accessing servers that implement the <a
 * href="http://www.opengeospatial.org/standards/wfs">OpenGIS Web Feature Service (WFS) 1.0.0/1.1.0/2.0.0</a> protocol.
 * 
 * <h4>Initialization</h4> In the initial step, one constructs a new {@link WFSClient} instance by invoking the
 * constructor with a URL to a WFS capabilities document. This usually is a <code>GetCapabilities</code> request
 * (including necessary parameters) to a WFS service.
 * 
 * <pre>
 * ...
 *   URL capabilitiesUrl = new URL( "http://...?service=WFS&version=1.0.0&request=GetCapabilities" );
 *   WFSClient wfsClient = new WFSClient( capabilitiesUrl );
 * ...
 * </pre>
 * 
 * Afterwards, the initialized {@link WFSClient} instance is bound to the specified service and allows to access service
 * metadata, feature type information as well as performing queries and transactions.
 * 
 * <h4>Accessing service metadata</h4> The method {@link #getMetadata()} allows to access service metadata announced by
 * the service, such as title, abstract, provider etc.
 * 
 * <h4>Accessing feature type information</h4> ...
 * 
 * <h4>Retrieving feature instances</h4> ...
 * 
 * <h4>Retrieving individual GML objects</h4> ...
 * 
 * <h4>Performing transactions</h4> ...
 * 
 * <h4>Locking features</h4> ...
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class WFSClient extends AbstractOWSClient<WFSCapabilitiesAdapter> {

    private static final Logger LOG = LoggerFactory.getLogger( WFSClient.class );

    private WFSVersion version;

    private final List<WFSFeatureType> wfsFts;

    private AppSchema schema;

    /**
     * Creates a new {@link WFSClient} instance with default behavior.
     * 
     * @param capaUrl
     *            url of a WFS capabilities document, usually this is a GetCapabilities request to a WFS service, must
     *            not be <code>null</code>
     * @throws OWSExceptionReport
     *             if the server replied with an exception report
     * @throws XMLStreamException
     * @throws IOException
     *             if a communication/network problem occured
     */
    public WFSClient( URL capaUrl ) throws OWSExceptionReport, XMLStreamException, IOException {
        super( capaUrl );
        initOperationUrls();
        wfsFts = capaDoc.parseFeatureTypeList();
    }

    /**
     * Creates a new {@link WFSClient} instance with options.
     * 
     * @param capaUrl
     *            url of a WFS capabilities document, usually this is a <code>GetCapabilities</code> request to a WFS
     *            service, must not be <code>null</code>
     * @param schema
     *            application schema that describes the feature types offered by the service, can be <code>null</code>
     *            (in this case, the client performs <code>DescribeFeatureType</code> requests to determine the schema)
     * @throws OWSExceptionReport
     *             if the server replied with a service exception report
     * @throws XMLStreamException
     * @throws IOException
     *             if a communication/network problem occured
     */
    public WFSClient( URL capaUrl, AppSchema schema ) throws OWSExceptionReport, XMLStreamException, IOException {
        this( capaUrl );
        this.schema = schema;
    }

    private void initOperationUrls() {
        for ( WFSRequestType requestType : WFSRequestType.values() ) {
            // TODO should we only consider the operations supported by the actual version?
            addURLs( requestType, capaDoc );
        }
    }

    private void addURLs( WFSRequestType request, WFSCapabilitiesAdapter capaDoc ) {
        // URL[] urls = new URL[2];
        // try {
        // urls[0] = capaDoc.getOperationURL( request.name(), false );
        // LOG.debug( "URL for operation: '" + request + "' (GET): " + urls[0] );
        // } catch ( Throwable t ) {
        // String msg = "Error retrieving URL for operation '" + request + "' (GET): " + t.getMessage();
        // LOG.warn( msg );
        // }
        // try {
        // urls[1] = capaDoc.getOperationURL( request.name(), true );
        // LOG.debug( "URL for operation: '" + request + "' (POST): " + urls[1] );
        // } catch ( Throwable t ) {
        // String msg = "Error retrieving URL for operation '" + request + "' (POST): " + t.getMessage();
        // LOG.warn( msg );
        // }
        // requestTypeToURLs.put( request, urls );
    }

    /**
     * Returns the WFS protocol version in use.
     * 
     * @return the WFS protocol version in use, never <code>null</code>
     */
    public WFSVersion getServiceVersion() {
        return version;
    }

    /**
     * Returns (metadata of) all feature types offered by the service.
     * 
     * @return metadata of the feature types, never <code>null</code>
     */
    public List<WFSFeatureType> getFeatureTypes() {
        return wfsFts;
    }

    /**
     * Returns (metadata of) the specified feature type offered by the service.
     * 
     * @return metadata of the feature type, or <code>null</code> if no such feature type exists
     */
    public WFSFeatureType getFeatureType( QName ftName ) {
        // TODO
        return null;
    }

    /**
     * Returns the (GML) {@link AppSchema} for all {@link FeatureType}s offered by this server.
     * 
     * @return application schema, never <code>null</code>
     */
    public AppSchema getAppSchema() {
        if ( schema == null ) {
            try {
                URL url = getGetUrl( DescribeFeatureType.name() );
                String requestUrl = url + "?version=1.0.0&service=WFS&request=DescribeFeatureType";
                LOG.info( "Using URL '" + url + "': " + requestUrl );
                AppSchemaXSDDecoder schemaDecoder = new AppSchemaXSDDecoder( null, null, requestUrl );
                schema = schemaDecoder.extractFeatureTypeSchema();
            } catch ( Throwable t ) {
                t.printStackTrace();
            }
        }
        return schema;
    }

    public StreamFeatureCollection getFeatures( QName ftName )
                            throws OWSException {

        URL requestUrl = null;
        try {
            URL url = getGetUrl( WFSRequestType.GetFeature.name() );
            requestUrl = new URL( url.toString() + "?version=1.0.0&service=WFS&request=GetFeature&typeName="
                                  + ftName.getLocalPart() );
        } catch ( MalformedURLException e ) {
            // should never happen
        }
        StreamFeatureCollection fc = null;
        try {
            GMLVersion gmlVersion = getAppSchema().getGMLSchema().getVersion();
            GMLStreamReader gmlReader = GMLInputFactory.createGMLStreamReader( gmlVersion, requestUrl );
            gmlReader.setApplicationSchema( schema );
            // TODO default SRS
            fc = gmlReader.readFeatureCollectionStream();
        } catch ( Throwable t ) {
            t.printStackTrace();
        }

        return fc;
    }

    public GMLObject getGMLObject( GetGmlObject request ) {
        return null;
    }

    @Override
    protected WFSCapabilitiesAdapter getCapabilitiesAdapter( OMElement root, String versionAttr )
                            throws IOException {

        QName rootEl = root.getQName();

        // for all versions (1.0.0/1.1.0/2.0.0), root element is "WFS_Capabilities"
        if ( "WFS_Capabilities".equalsIgnoreCase( rootEl.getLocalPart() ) ) {
            version = WFS_100;
            if ( versionAttr != null ) {
                try {
                    Version ogcVersion = Version.parseVersion( versionAttr );
                    version = WFSVersion.valueOf( ogcVersion );
                } catch ( Throwable t ) {
                    String msg = "WFS capabilities document has unsupported version '" + versionAttr + "'.";
                    throw new IllegalArgumentException( msg );
                }
            } else {
                LOG.warn( "No version attribute in WFS capabilities document. Defaulting to 1.0.0." );
            }
        } else {
            // TODO
            String msg = "Unexpected GetCapabilities response element: '" + rootEl + "'.";
            throw new IOException( msg );
        }

        switch ( version ) {
        case WFS_100: {
            WFS100CapabilitiesAdapter capaDoc = new WFS100CapabilitiesAdapter();
            capaDoc.setRootElement( root );
            return capaDoc;
        }
        case WFS_110: {
            WFS110CapabilitiesAdapter capaDoc = new WFS110CapabilitiesAdapter();
            capaDoc.setRootElement( root );
            return capaDoc;
        }
        case WFS_200: {
            WFS200CapabilitiesAdapter capaDoc = new WFS200CapabilitiesAdapter();
            capaDoc.setRootElement( root );
            return capaDoc;
        }
        }
        throw new RuntimeException( "Internal error: unhandled WFS service version '" + version + "'." );
    }
}
