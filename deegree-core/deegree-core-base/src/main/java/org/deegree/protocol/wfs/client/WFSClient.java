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

import static org.deegree.protocol.ows.exception.OWSException.VERSION_NEGOTIATION_FAILED;
import static org.deegree.protocol.wfs.WFSVersion.WFS_100;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.tom.ows.Version;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.feature.StreamFeatureCollection;
import org.deegree.feature.types.ApplicationSchema;
import org.deegree.feature.types.GenericApplicationSchema;
import org.deegree.feature.types.FeatureType;
import org.deegree.gml.GMLInputFactory;
import org.deegree.gml.GMLObject;
import org.deegree.gml.GMLStreamReader;
import org.deegree.gml.GMLVersion;
import org.deegree.gml.feature.schema.ApplicationSchemaXSDDecoder;
import org.deegree.protocol.ows.exception.OWSException;
import org.deegree.protocol.ows.metadata.ServiceMetadata;
import org.deegree.protocol.wfs.WFSVersion;
import org.deegree.protocol.wfs.getgmlobject.GetGmlObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * API-level client for accessing web services that implement the <a
 * href="http://www.opengeospatial.org/standards/wfs">OpenGIS Web Feature Service (WFS) 1.0.0/1.1.0/2.0.0</a> protocol.
 * 
 * <h4>Initialization</h4> In the initial step, one constructs a new {@link WFSClient} instance by invoking the
 * constructor with a URL to a WFS capabilities document. In most cases, this will be a GetCapabilities request
 * (including necessary parameters) to a WFS service.
 * 
 * <pre>
 * ...
 *   URL capabilitiesUrl = new URL( "http://...?service=WFS&version=1.0.0&request=GetCapabilities" );
 *   WFSClient wfsClient = new WFSClient( capabilitiesUrl );
 * ...
 * </pre>
 * 
 * Afterwards, the {@link WFSClient} instance is bound to the specified service and allows to access service metadata,
 * feature type information as well as performing queries and transactions.
 * 
 * <h4>Accessing service metadata</h4> The method {@link #getMetadata()} allows to access the metadata announced by the
 * service, such as title, abstract, provider etc.
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
public class WFSClient {

    private static Logger LOG = LoggerFactory.getLogger( WFSClient.class );

    // [0]: GET, [1]: POST
    private final URL[] describeFeatureTypeURLs = new URL[2];

    // [0]: GET, [1]: POST
    private final URL[] getFeatureURLs = new URL[2];

    // [0]: GET, [1]: POST
    private final URL[] getGmlObjectURLs = new URL[2];

    private WFSVersion version;

    private final WFSFeatureType[] wfsFts;

    private ApplicationSchema schema;

    /**
     * Creates a new {@link WFSClient} instance.
     * 
     * @param capabilitiesURL
     *            url of a WFS capabilities document, usually this is a GetCapabilities request to a WFS service, must
     *            not be <code>null</code>
     * @throws IOException
     *             if a communication/network problem occured
     * @throws OWSException
     *             if the server replied with an exception
     */
    public WFSClient( URL capabilitiesURL ) throws IOException, OWSException {
        WFSCapabilitiesAdapter capaAdapter = retrieveCapabilities( capabilitiesURL );

        // TODO
        this.describeFeatureTypeURLs[0] = new URL( "http://deegree3-testing.deegree.org/deegree-utah-demo/services?" );
        this.describeFeatureTypeURLs[1] = new URL( "http://deegree3-testing.deegree.org/deegree-utah-demo/services" );

        List<WFSFeatureType> wfsFts = capaAdapter.getFeatureTypes();
        this.wfsFts = wfsFts.toArray( new WFSFeatureType[wfsFts.size()] );
    }

    /**
     * 
     * TODO cope with WFS exception reports
     * 
     * @param capabilitiesURL
     * @return
     * @throws IOException
     * @throws OWSException
     */
    private WFSCapabilitiesAdapter retrieveCapabilities( URL capabilitiesURL )
                            throws IOException, OWSException {

        XMLAdapter responseDoc = null;
        try {
            LOG.trace( "Retrieving capabilities document from '{}'", capabilitiesURL );
            responseDoc = new XMLAdapter( capabilitiesURL );
        } catch ( Throwable e ) {
            String msg = "Unable to retrieve/parse capabilities document from URL '" + capabilitiesURL + "': "
                         + e.getMessage();
            throw new IOException( msg );
        }

        OMElement root = responseDoc.getRootElement();
        QName rootEl = root.getQName();
        String versionAttr = root.getAttributeValue( new QName( "version" ) );
        LOG.trace( "Response document root element/version attribute: " + rootEl + "/" + versionAttr );

        WFSCapabilitiesAdapter capaDoc = null;
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

            capaDoc = new WFSCapabilitiesAdapter( version );
            capaDoc.setRootElement( responseDoc.getRootElement() );
            capaDoc.setSystemId( responseDoc.getSystemId() );
        } else if ( "ExceptionReport".equalsIgnoreCase( rootEl.getLocalPart() ) ) {
            // TODO
            throw new OWSException( "Server responded with exception report", VERSION_NEGOTIATION_FAILED );
        } else {
            // TODO
            String msg = "Unexpected GetCapabilities response element: '" + rootEl + "'.";
            throw new OWSException( msg, VERSION_NEGOTIATION_FAILED );
        }
        return capaDoc;
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
     * Returns the metadata (ServiceIdentification, ServiceProvider) of the service.
     * 
     * @return the metadata of the service, never <code>null</code>
     */
    public ServiceMetadata getMetadata() {
        // TODO
        return null;
    }

    /**
     * Returns (metadata of) all feature types offered by the service.
     * 
     * @return metadata of the feature types, never <code>null</code>
     */
    public WFSFeatureType[] getFeatureTypes() {
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
     * Returns the (GML) {@link ApplicationSchema} for all {@link FeatureType}s offered by this server.
     * 
     * @return application schema, never <code>null</code>
     */
    public ApplicationSchema getApplicationSchema() {
        if ( schema == null ) {
            try {
                String url = "http://deegree3-testing.deegree.org/deegree-utah-demo/services?version=1.0.0&service=WFS&request=DescribeFeatureType";
                System.out.println( "URL: " + url );
                ApplicationSchemaXSDDecoder schemaDecoder = new ApplicationSchemaXSDDecoder( null, null, url );
                schema = schemaDecoder.extractFeatureTypeSchema();
            } catch ( Throwable t ) {
                t.printStackTrace();
            }
        }
        return schema;
    }

    public StreamFeatureCollection getFeatures( QName ftName )
                            throws OWSException {
        // TODO
        String url = "http://deegree3-testing.deegree.org/deegree-utah-demo/services?version=1.0.0&service=WFS&request=GetFeature&typeName="
                     + ftName.getLocalPart();

        StreamFeatureCollection fc = null;
        try {
            GMLVersion gmlVersion = getApplicationSchema().getGMLSchema().getVersion();
            GMLStreamReader gmlReader = GMLInputFactory.createGMLStreamReader( gmlVersion, new URL( url ) );
            gmlReader.setApplicationSchema( schema );
            // TODO default SRS
            fc = gmlReader.readStreamFeatureCollection();
        } catch ( Throwable t ) {
            t.printStackTrace();
        }

        return fc;
    }

    public GMLObject getGMLObject( GetGmlObject request ) {
        return null;
    }

    // TODO Transaction, LockFeature, GetFeatureWithLock, WFS 2.0 requests
}