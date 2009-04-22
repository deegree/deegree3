//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------
 This file is part of deegree.
 Copyright (C) 2001-2009 by:
 Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 Lesser General Public License for more details.
 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 Contact:

 Andreas Poth
 lat/lon GmbH
 Aennchenstr. 19
 53177 Bonn
 Germany
 E-Mail: poth@lat-lon.de

 Prof. Dr. Klaus Greve
 Department of Geography
 University of Bonn
 Meckenheimer Allee 166
 53115 Bonn
 Germany
 E-Mail: greve@giub.uni-bonn.de
 ---------------------------------------------------------------------------*/

package org.deegree.protocol.wms.client;

import static org.deegree.commons.utils.ArrayUtils.join;
import static org.deegree.commons.utils.HttpUtils.IMAGE;
import static org.deegree.commons.utils.HttpUtils.XML;
import static org.deegree.commons.xml.CommonNamespaces.getNamespaceContext;
import static org.deegree.crs.coordinatesystems.GeographicCRS.WGS84;
import static org.deegree.geometry.GeometryFactoryCreator.getInstance;
import static org.deegree.protocol.i18n.Messages.get;
import static org.deegree.protocol.wms.client.WMSClient111.Requests.GetCapabilities;
import static org.deegree.protocol.wms.client.WMSClient111.Requests.GetMap;
import static org.slf4j.LoggerFactory.getLogger;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.LinkedList;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.utils.Pair;
import org.deegree.commons.xml.NamespaceContext;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.XPath;
import org.deegree.crs.CRS;
import org.deegree.crs.exceptions.UnknownCRSException;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.GeometryFactoryCreator;
import org.slf4j.Logger;

/**
 * <code>WMSClient111</code>
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class WMSClient111 {

    private static final NamespaceContext nsContext = getNamespaceContext();

    private static final Logger LOG = getLogger( WMSClient111.class );

    /**
     * <code>Requests</code>
     * 
     * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
     * @author last edited by: $Author$
     * 
     * @version $Revision$, $Date$
     */
    public enum Requests {
        /**
         * 
         */
        GetMap, /**
         * 
         */
        GetCapabilities, /**
         * 
         */
        GetFeatureInfo, /**
         * 
         */
        GetLegendGraphic
    }

    private XMLAdapter capabilities;

    /**
     * @param url
     */
    public WMSClient111( URL url ) {
        this( new XMLAdapter( url ) );
    }

    /**
     * @param capabilities
     */
    public WMSClient111( XMLAdapter capabilities ) {
        checkCapabilities( capabilities );
        this.capabilities = capabilities;
    }

    private void checkCapabilities( XMLAdapter capabilities ) {
        OMElement root = capabilities.getRootElement();
        String version = root.getAttributeValue( new QName( "version" ) );
        if ( !version.equals( "1.1.1" ) ) {
            throw new IllegalArgumentException( get( "WMSCLIENT.WRONG_VERSION_CAPABILITIES", version, "1.1.1" ) );
        }
        if ( !root.getLocalName().equals( "WMT_MS_Capabilities" ) ) {
            throw new IllegalArgumentException( get( "WMSCLIENT.NO_WMS_CAPABILITIES", root.getLocalName(),
                                                     "WMT_MS_Capabilities" ) );
        }
    }

    /**
     * TODO implement updateSequence handling to improve network performance
     */
    public void refreshCapabilities() {
        String url = getAddress( GetCapabilities, true );
        if ( !url.endsWith( "?" ) && !url.endsWith( "&" ) ) {
            url += url.indexOf( "?" ) == -1 ? "?" : "&";
        }
        url += "request=GetCapabilities&version=1.1.1&service=WMS";
        try {
            XMLAdapter adapter = new XMLAdapter( new URL( url ) );
            checkCapabilities( adapter );
            capabilities = adapter;
        } catch ( MalformedURLException e ) {
            LOG.debug( "Malformed capabilities URL?", e );
        }
    }

    /**
     * @param request
     * @return true, if an according section was found in the capabilities
     */
    public boolean isOperationSupported( Requests request ) {
        XPath xp = new XPath( "//" + request.name(), null );
        return capabilities.getElement( capabilities.getRootElement(), xp ) != null;
    }

    /**
     * @param request
     * @return the image formats defined for the request, or null, if request is not supported
     */
    public LinkedList<String> getFormats( Requests request ) {
        if ( !isOperationSupported( request ) ) {
            return null;
        }
        XPath xp = new XPath( "//" + request.name() + "/Format", null );
        LinkedList<String> list = new LinkedList<String>();
        Object res = capabilities.evaluateXPath( xp, capabilities.getRootElement() );
        if ( res instanceof List ) {
            for ( Object o : (List<?>) res ) {
                list.add( ( (OMElement) o ).getText() );
            }
        }
        return list;
    }

    /**
     * @param request
     * @param get
     *            true means HTTP GET, false means HTTP POST
     * @return the address, or null, if not defined or request unavailable
     */
    public String getAddress( Requests request, boolean get ) {
        if ( !isOperationSupported( request ) ) {
            return null;
        }
        return capabilities.getNodeAsString( capabilities.getRootElement(), new XPath( "//" + request.name()
                                                                                       + "/DCPType/HTTP/"
                                                                                       + ( get ? "Get" : "Post" )
                                                                                       + "/OnlineResource/@xlink:href",
                                                                                       nsContext ), null );
    }

    /**
     * @param name
     * @return true, if the WMS advertises a layer with that name
     */
    public boolean hasLayer( String name ) {
        return capabilities.getNode( capabilities.getRootElement(), new XPath( "//Layer[Name = '" + name + "']", null ) ) != null;
    }

    /**
     * @param name
     * @return all coordinate system names, also inherited ones
     */
    public LinkedList<String> getCoordinateSystems( String name ) {
        LinkedList<String> list = new LinkedList<String>();
        if ( !hasLayer( name ) ) {
            return list;
        }
        OMElement elem = capabilities.getElement( capabilities.getRootElement(), new XPath( "//Layer[Name = '" + name
                                                                                            + "']", null ) );
        List<OMElement> es = capabilities.getElements( elem, new XPath( "SRS", null ) );
        while ( ( elem = (OMElement) elem.getParent() ).getLocalName().equals( "Layer" ) ) {
            es.addAll( capabilities.getElements( elem, new XPath( "SRS", null ) ) );
        }
        for ( OMElement e : es ) {
            if ( !list.contains( e.getText() ) ) {
                list.add( e.getText() );
            }
        }
        return list;
    }

    /**
     * @param layer
     * @return the envelope, or null, if none was found
     */
    public Envelope getLatLonBoundingBox( String layer ) {
        double[] min = new double[2];
        double[] max = new double[2];

        OMElement elem = capabilities.getElement( capabilities.getRootElement(), new XPath( "//Layer[Name = '" + layer
                                                                                            + "']", null ) );
        while ( elem.getLocalName().equals( "Layer" ) ) {
            OMElement bbox = capabilities.getElement( elem, new XPath( "LatLonBoundingBox", null ) );
            if ( bbox != null ) {
                try {
                    min[0] = Double.parseDouble( bbox.getAttributeValue( new QName( "minx" ) ) );
                    min[1] = Double.parseDouble( bbox.getAttributeValue( new QName( "miny" ) ) );
                    max[0] = Double.parseDouble( bbox.getAttributeValue( new QName( "maxx" ) ) );
                    max[1] = Double.parseDouble( bbox.getAttributeValue( new QName( "maxy" ) ) );
                    return GeometryFactoryCreator.getInstance().getGeometryFactory().createEnvelope( min, max, new CRS(WGS84) );
                } catch ( NumberFormatException nfe ) {
                    LOG.warn( get( "WMSCLIENT.SERVER_INVALID_NUMERIC_VALUE", nfe.getLocalizedMessage() ) );
                }
            } else {
                elem = (OMElement) elem.getParent();
            }
        }

        return null;
    }

    /**
     * @param layers
     * @return a merged envelope of all the layer's envelopes
     */
    public Envelope getLatLonBoundingBox( List<String> layers ) {
        Envelope res = null;

        for ( String name : layers ) {
            if ( res == null ) {
                res = getLatLonBoundingBox( name );
            } else {
                res = res.merge( getLatLonBoundingBox( name ) );
            }
        }

        return res;
    }

    /**
     * @param srs
     * @param layer
     * @return the envelope, or null, if none was found
     * @throws UnknownCRSException
     */
    public Envelope getBoundingBox( String srs, String layer )
                            throws UnknownCRSException {
        double[] min = new double[2];
        double[] max = new double[2];

        OMElement elem = capabilities.getElement( capabilities.getRootElement(), new XPath( "//Layer[Name = '" + layer
                                                                                            + "']", null ) );
        while ( elem.getLocalName().equals( "Layer" ) ) {
            OMElement bbox = capabilities.getElement( elem, new XPath( "BoundingBox[@SRS = '" + srs + "']", null ) );
            if ( bbox != null ) {
                try {
                    min[0] = Double.parseDouble( bbox.getAttributeValue( new QName( "minx" ) ) );
                    min[1] = Double.parseDouble( bbox.getAttributeValue( new QName( "miny" ) ) );
                    max[0] = Double.parseDouble( bbox.getAttributeValue( new QName( "maxx" ) ) );
                    max[1] = Double.parseDouble( bbox.getAttributeValue( new QName( "maxy" ) ) );
                    return getInstance().getGeometryFactory().createEnvelope( min, max, new CRS( srs ) );
                } catch ( NumberFormatException nfe ) {
                    LOG.warn( get( "WMSCLIENT.SERVER_INVALID_NUMERIC_VALUE", nfe.getLocalizedMessage() ) );
                }
            } else {
                elem = (OMElement) elem.getParent();
            }
        }

        return null;
    }

    /**
     * @param srs
     * @param layers
     * @return the merged envelope, or null, if none was found
     * @throws UnknownCRSException
     */
    public Envelope getBoundingBox( String srs, List<String> layers )
                            throws UnknownCRSException {
        Envelope res = null;

        for ( String name : layers ) {
            if ( res == null ) {
                res = getBoundingBox( srs, name );
            } else {
                res = res.merge( getBoundingBox( srs, name ) );
            }
        }

        return res;
    }

    /**
     * @param layers
     * @param width
     * @param height
     * @param bbox
     * @param srs
     * @param format
     * @param validate
     *            whether to validate the values against the capabilities. Example: a format is requested that the
     *            server does not advertise. So the first advertised format will be used, and an entry will be put in
     *            the validationErrors list that says just that.
     * @param validationErrors
     *            a list of validation actions
     * @return an image from the server, or an error message from the service exception
     * @throws IOException
     */
    public Pair<BufferedImage, String> getMap( List<String> layers, int width, int height, Envelope bbox,
                                               CRS srs, String format, boolean validate,
                                               List<String> validationErrors )
                            throws IOException {
        try {

            if ( validate ) {
                LinkedList<String> formats = getFormats( GetMap );
                if ( !formats.contains( format ) ) {
                    format = formats.get( 0 );
                    validationErrors.add( "Using format " + format + " instead." );
                }
                // TODO validate srs, width, height, rest, etc
            }

            String url = getAddress( GetMap, true );
            if ( url == null ) {
                LOG.warn( get( "WMSCLIENT.SERVER_NO_GETMAP_URL" ), "Capabilities: ", capabilities );
                return null;
            }
            if ( !url.endsWith( "?" ) && !url.endsWith( "&" ) ) {
                url += url.indexOf( "?" ) == -1 ? "?" : "&";
            }
            url += "request=GetMap&version=1.1.1&service=WMS&layers=" + join( ",", layers ) + "&styles=&width=" + width
                   + "&height=" + height + "&bbox=" + bbox.getMin().getX() + "," + bbox.getMin().getY() + ","
                   + bbox.getMax().getX() + "," + bbox.getMax().getY() + "&srs=" + srs.getName() + "&format="
                   + format;

            Pair<BufferedImage, String> res = new Pair<BufferedImage, String>();
            URL theUrl = new URL( url );
            URLConnection conn = theUrl.openConnection();
            conn.connect();
            if ( LOG.isTraceEnabled() ) {
                LOG.trace( "Requesting from " + theUrl );
                LOG.trace( "Content type is " + conn.getContentType() );
                LOG.trace( "Content encoding is " + conn.getContentEncoding() );
            }
            if ( conn.getContentType().startsWith( format ) ) {
                res.first = IMAGE.work( conn.getInputStream() );
            } else if ( conn.getContentType().startsWith( "application/vnd.ogc.se_xml" ) ) {
                res.second = XML.work( conn.getInputStream() ).toString();
            } else { // try and find out the hard way
                res.first = IMAGE.work( conn.getInputStream() );
                if ( res.first == null ) {
                    conn = theUrl.openConnection();
                    res.second = XML.work( conn.getInputStream() ).toString();
                }
            }

            return res;
        } catch ( MalformedURLException e ) {
            LOG.debug( "GetMap URL malformed?", e );
            return null;
        }
    }

}
