//$HeadURL: svn+ssh://aionita@svn.wald.intevation.org/deegree/base/trunk/resources/eclipse/files_template.xml $
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

package org.deegree.protocol.wpvs.client;

import static org.deegree.commons.utils.net.HttpUtils.IMAGE;
import static org.deegree.commons.utils.net.HttpUtils.XML;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.deegree.commons.utils.Pair;
import org.deegree.commons.xml.NamespaceContext;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.XPath;
import org.deegree.protocol.wpvs.WPVSConstants.WPVSRequestType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The <code>WPVSClient</code> class supports the functionality of sending requests to the Web Perspective View Service
 * (WPVS).
 * 
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 * @author last edited by: $Author: ionita $
 * 
 * @version $Revision: $, $Date: $
 */
public class WPVSClient {

    private final static Logger LOG = LoggerFactory.getLogger( WPVSClient.class );

    private final NamespaceContext nsContext;

    private XMLAdapter capabilities;

    /**
     * 
     * @param url
     *            to retrieve the capabilities from
     */
    public WPVSClient( URL url ) {
        this( new XMLAdapter( url ) );
    }

    /**
     * retrieve the capabilities from an xml file
     * 
     * @param capabilities
     */
    @SuppressWarnings("unchecked")
    public WPVSClient( XMLAdapter capabilities ) {
        this.capabilities = capabilities;
        // add namespaces to namespace context, to be used later with xpath
        nsContext = new NamespaceContext();
        // get all defined namespaces from getCapabilities in order to define the namespace context
        Iterator<OMNamespace> nss = this.capabilities.getRootElement().getAllDeclaredNamespaces();
        while ( nss.hasNext() ) {
            OMNamespace omNs = nss.next();
            if ( omNs != null ) {
                nsContext.addNamespace( omNs.getPrefix(), omNs.getNamespaceURI() );
            }
        }
    }

    /**
     * 
     * @return all datasets that are queryable
     */
    public synchronized List<String> getQueryableDatasets() {
        List<String> res = new LinkedList<String>();
        XPath xp = new XPath( "//wpvs:Dataset[@queryable='true']", nsContext );
        List<OMElement> datasets = capabilities.getElements( capabilities.getRootElement(), xp );
        for ( OMElement node : datasets ) {
            if ( node != null ) {
                XPath xpName = new XPath( "wpvs:Name", nsContext );
                String name = capabilities.getNodeAsString( node, xpName, null );
                if ( name != null ) {
                    res.add( name );
                    LOG.info( "dataset found in GetCapabilities: " + name );
                }
            } else
                LOG.warn( "Found an empty dataset!" );
        }
        return res;
    }

    /**
     * 
     * @return all elevation models defined
     */
    public synchronized List<String> getElevationModels() {
        List<String> res = new LinkedList<String>();
        XPath xp = new XPath( "//wpvs:ElevationModel", nsContext );
        List<OMElement> elModels = capabilities.getElements( capabilities.getRootElement(), xp );
        for ( OMElement node : elModels )
            if ( node != null ) {
                XPath xpName = new XPath( "wpvs:Name", nsContext );
                String name = capabilities.getNodeAsString( node, xpName, null );
                if ( name != null )
                    res.add( name );
            }
        return res;
    }

    /**
     * 
     * @param request
     *            the type of {@link WPVSRequestType}
     * @return whether the request is defined in the getCapabilities xml file
     */
    public synchronized boolean isOperationSupported( WPVSRequestType request ) {
        XPath xp = new XPath( "//ows:Operation[@name='" + request.name() + "']", nsContext );
        return capabilities.getElement( capabilities.getRootElement(), xp ) != null;
    }

    /**
     * 
     * @param request
     *            the type of {@link WPVSRequestType}
     * @param get
     *            whether it is a Get or a Post request
     * @return the url of the requested operation
     */
    public synchronized String getAddress( WPVSRequestType request, boolean get ) {
        if ( !isOperationSupported( request ) )
            return null;

        String xpathStr = "//ows:Operation[@name=\"" + request.name() + "\"]";
        xpathStr += "/ows:DCP/ows:HTTP/" + ( get ? "ows:Get" : "ows:Post" ) + "/@xlink:href";

        OMElement root = capabilities.getRootElement();
        String res = capabilities.getNodeAsString( root, new XPath( xpathStr, nsContext ), null );
        return res;
    }

    /**
     * 
     * @param url
     * @return a {@link Pair} of {@link BufferedImage} and {@link String}. In case the WPVS returns an image the former
     *         will be used, otherwise an XML response ( as a String ) will be returned in the second value.
     * @throws IOException
     */
    public synchronized Pair<BufferedImage, String> getView( URL url )
                            throws IOException {
        Pair<BufferedImage, String> res = new Pair<BufferedImage, String>();
        InputStream inStream = url.openStream();
        res.first = IMAGE.work( inStream );
        // rb: the following will never happen (I guess)
        if ( res.first == null ) {
            res.second = XML.work( url.openStream() ).toString();
        }
        inStream.close();

        return res;
    }
}
