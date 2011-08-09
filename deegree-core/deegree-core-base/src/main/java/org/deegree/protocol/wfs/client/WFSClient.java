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

import java.io.IOException;
import java.net.URL;

import javax.xml.namespace.QName;

import org.deegree.feature.StreamFeatureCollection;
import org.deegree.gml.GMLObject;
import org.deegree.protocol.ows.exception.OWSException;
import org.deegree.protocol.ows.metadata.ServiceMetadata;
import org.deegree.protocol.wfs.getfeature.GetFeature;
import org.deegree.protocol.wfs.getgmlobject.GetGmlObject;

/**
 * API-level client for accessing web services that implement the <a
 * href="http://www.opengeospatial.org/standards/wfs">WebFeatureService (WFS) 1.0.0/1.1.0/2.0</a> protocol.
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

    // [0]: GET, [1]: POST
    private final URL[] describeFeatureTypeURLs = new URL[2];

    // [0]: GET, [1]: POST
    private final URL[] getFeatureURLs = new URL[2];

    // [0]: GET, [1]: POST
    private final URL[] getGmlObjectURLs = new URL[2];

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
        // TODO
    }

    /**
     * Returns the WFS protocol version in use.
     * 
     * @return the WFS protocol version in use, never <code>null</code>
     */
    public String getServiceVersion() {
        return null;
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
        // TODO
        return null;
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

    public StreamFeatureCollection getFeatures( GetFeature request )
                            throws OWSException {
        return null;
    }

    public GMLObject getGMLObject( GetGmlObject request ) {
        return null;
    }

    // TODO Transaction, LockFeature, GetFeatureWithLock, WFS 2.0 requests
}