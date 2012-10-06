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

package org.deegree.remoteows.wms;

import static java.util.Arrays.asList;
import static org.deegree.commons.tom.primitive.BaseType.STRING;
import static org.deegree.commons.utils.ArrayUtils.join;
import static org.deegree.commons.utils.ProxyUtils.getHttpProxyPassword;
import static org.deegree.commons.utils.ProxyUtils.getHttpProxyUser;
import static org.deegree.commons.utils.kvp.KVPUtils.toQueryString;
import static org.deegree.commons.utils.net.HttpUtils.IMAGE;
import static org.deegree.commons.utils.net.HttpUtils.XML;
import static org.deegree.commons.xml.CommonNamespaces.getNamespaceContext;
import static org.deegree.commons.xml.stax.XMLStreamUtils.nextElement;
import static org.deegree.cs.coordinatesystems.GeographicCRS.WGS84;
import static org.deegree.gml.GMLInputFactory.createGMLStreamReader;
import static org.deegree.gml.GMLVersion.GML_2;
import static org.deegree.protocol.i18n.Messages.get;
import static org.deegree.protocol.oldwms.WMSConstants.WMSRequestType.GetCapabilities;
import static org.deegree.protocol.oldwms.WMSConstants.WMSRequestType.GetFeatureInfo;
import static org.deegree.protocol.oldwms.WMSConstants.WMSRequestType.GetMap;
import static org.slf4j.LoggerFactory.getLogger;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.concurrent.Callable;

import javax.imageio.ImageIO;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.concurrent.Executor;
import org.deegree.commons.tom.gml.property.Property;
import org.deegree.commons.tom.gml.property.PropertyType;
import org.deegree.commons.utils.Pair;
import org.deegree.commons.utils.ProxyUtils;
import org.deegree.commons.xml.NamespaceBindings;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.XMLParsingException;
import org.deegree.commons.xml.XPath;
import org.deegree.coverage.raster.SimpleRaster;
import org.deegree.coverage.raster.data.RasterData;
import org.deegree.coverage.raster.data.nio.PixelInterleavedRasterData;
import org.deegree.coverage.raster.geom.RasterGeoReference;
import org.deegree.coverage.raster.geom.RasterGeoReference.OriginLocation;
import org.deegree.coverage.raster.utils.RasterFactory;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.cs.persistence.CRSManager;
import org.deegree.feature.FeatureCollection;
import org.deegree.feature.GenericFeature;
import org.deegree.feature.GenericFeatureCollection;
import org.deegree.feature.property.SimpleProperty;
import org.deegree.feature.types.GenericFeatureType;
import org.deegree.feature.types.property.SimplePropertyType;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.GeometryFactory;
import org.deegree.gml.GMLStreamReader;
import org.deegree.protocol.oldwms.WMSConstants.WMSRequestType;
import org.slf4j.Logger;

/**
 * Allows for easy performing of requests again WMS 1.1.1 compliant map services.
 * 
 * TODO refactor timeout and tiled request code
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
@Deprecated
public class OldWMSClient111 {

    private static final NamespaceBindings nsContext = getNamespaceContext();

    // needed in the worker
    static final Logger LOG = getLogger( OldWMSClient111.class );

    // needed in the worker
    int maxMapWidth = -1;

    // needed in the worker
    int maxMapHeight = -1;

    // needed in the worker
    XMLAdapter capabilities;

    int connectionTimeout = 5;

    int requestTimeout = 60;

    String httpBasicUser;

    String httpBasicPass;

    /**
     * @param url
     * @param connectionTimeout
     *            default is 5 seconds
     * @param requestTimeout
     *            default is 60 seconds
     * @param user
     *            http basic username
     * @param pass
     *            http basic password
     */
    public OldWMSClient111( URL url, int connectionTimeout, int requestTimeout, String user, String pass ) {
        this.connectionTimeout = connectionTimeout;
        this.requestTimeout = requestTimeout;
        this.httpBasicUser = user;
        this.httpBasicPass = pass;
        try {
            if ( httpBasicUser != null ) {
                capabilities = new XMLAdapter();
                capabilities.load( url, httpBasicUser, httpBasicPass );
            } else {
                capabilities = new XMLAdapter( url );
            }
        } catch ( Exception e ) {
            LOG.error( e.getLocalizedMessage(), e );
            throw new NullPointerException( "Could not read from URL: " + url + " error was: "
                                            + e.getLocalizedMessage() );
        }
        checkCapabilities( this.capabilities );
    }

    /**
     * @param url
     * @param connectionTimeout
     *            default is 5 seconds
     * @param requestTimeout
     *            default is 60 seconds
     */
    public OldWMSClient111( URL url, int connectionTimeout, int requestTimeout ) {
        this( url );
        this.connectionTimeout = connectionTimeout;
        this.requestTimeout = requestTimeout;
    }

    /**
     * @param url
     */
    public OldWMSClient111( URL url ) {
        try {
            capabilities = new XMLAdapter( url );
        } catch ( Exception e ) {
            LOG.error( e.getLocalizedMessage(), e );
            throw new NullPointerException( "Could not read from URL: " + url + " error was: "
                                            + e.getLocalizedMessage() );
        }
        checkCapabilities( this.capabilities );
    }

    /**
     * @param capabilities
     */
    public OldWMSClient111( XMLAdapter capabilities ) {
        checkCapabilities( capabilities );
        this.capabilities = capabilities;
    }

    /**
     * @return the system id of the capabilities document.
     */
    public String getSystemId() {
        return this.capabilities.getSystemId();
    }

    /**
     * Sets the maximum map size that the server will process. If a larger map is requested, it will be broken down into
     * multiple GetMap requests.
     * 
     * @param maxWidth
     *            maximum number of pixels in x-direction, or -1 for unrestricted width
     * @param maxHeight
     *            maximum number of pixels in y-direction, or -1 for unrestricted height
     */
    public void setMaxMapDimensions( int maxWidth, int maxHeight ) {
        maxMapWidth = maxWidth;
        maxMapHeight = maxHeight;
    }

    private void checkCapabilities( XMLAdapter capabilities ) {
        OMElement root = capabilities.getRootElement();
        String version = root.getAttributeValue( new QName( "version" ) );
        if ( !"1.1.1".equals( version ) ) {
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
            XMLAdapter adapter;
            if ( httpBasicUser != null ) {
                adapter = new XMLAdapter();
                adapter.load( new URL( url ), httpBasicUser, httpBasicPass );
            } else {
                adapter = new XMLAdapter( new URL( url ) );
            }
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
    public boolean isOperationSupported( WMSRequestType request ) {
        XPath xp = new XPath( "//" + request, null );
        return capabilities.getElement( capabilities.getRootElement(), xp ) != null;
    }

    /**
     * @param request
     * @return the image formats defined for the request, or null, if request is not supported
     */
    public LinkedList<String> getFormats( WMSRequestType request ) {
        if ( !isOperationSupported( request ) ) {
            return null;
        }
        XPath xp = new XPath( "//" + request + "/Format", null );
        LinkedList<String> list = new LinkedList<String>();
        Object res = capabilities.evaluateXPath( xp, capabilities.getRootElement() );
        if ( res instanceof List<?> ) {
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
    public String getAddress( WMSRequestType request, boolean get ) {

        if ( !isOperationSupported( request ) ) {
            return null;
        }
        return capabilities.getNodeAsString( capabilities.getRootElement(), new XPath( "//" + request
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
        if ( elem == null ) {
            LOG.warn( "Could not get a layer with name: " + layer );
        } else {
            while ( elem.getLocalName().equals( "Layer" ) ) {
                OMElement bbox = capabilities.getElement( elem, new XPath( "LatLonBoundingBox", null ) );
                if ( bbox != null ) {
                    try {
                        min[0] = Double.parseDouble( bbox.getAttributeValue( new QName( "minx" ) ) );
                        min[1] = Double.parseDouble( bbox.getAttributeValue( new QName( "miny" ) ) );
                        max[0] = Double.parseDouble( bbox.getAttributeValue( new QName( "maxx" ) ) );
                        max[1] = Double.parseDouble( bbox.getAttributeValue( new QName( "maxy" ) ) );
                        return new GeometryFactory().createEnvelope( min, max, CRSManager.getCRSRef( WGS84 ) );
                    } catch ( NumberFormatException nfe ) {
                        LOG.warn( get( "WMSCLIENT.SERVER_INVALID_NUMERIC_VALUE", nfe.getLocalizedMessage() ) );
                    }
                } else {
                    elem = (OMElement) elem.getParent();
                }
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
     */
    public Envelope getBoundingBox( String srs, String layer ) {
        double[] min = new double[2];
        double[] max = new double[2];

        OMElement elem = capabilities.getElement( capabilities.getRootElement(), new XPath( "//Layer[Name = '" + layer
                                                                                            + "']", null ) );
        while ( elem != null && elem.getLocalName().equals( "Layer" ) ) {
            OMElement bbox = capabilities.getElement( elem, new XPath( "BoundingBox[@SRS = '" + srs + "']", null ) );
            if ( bbox != null ) {
                try {
                    min[0] = Double.parseDouble( bbox.getAttributeValue( new QName( "minx" ) ) );
                    min[1] = Double.parseDouble( bbox.getAttributeValue( new QName( "miny" ) ) );
                    max[0] = Double.parseDouble( bbox.getAttributeValue( new QName( "maxx" ) ) );
                    max[1] = Double.parseDouble( bbox.getAttributeValue( new QName( "maxy" ) ) );
                    return new GeometryFactory().createEnvelope( min, max, CRSManager.getCRSRef( srs ) );
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
     * @return the names of all layers that have a name
     */
    public List<String> getNamedLayers() {
        return asList( capabilities.getNodesAsStrings( capabilities.getRootElement(), new XPath( "//Layer/Name", null ) ) );
    }

    /**
     * @param srs
     * @param layers
     * @return the merged envelope, or null, if none was found
     */
    public Envelope getBoundingBox( String srs, List<String> layers ) {
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
     * 
     * @param layers
     * @param width
     * @param height
     * @param bbox
     * @param srs
     * @param format
     * @param transparent
     * @param errorsInImage
     *            if true, no exceptions are thrown or validation errors are returned. The returned pair allows contains
     *            an image of the expected size.
     * @param timeout
     *            number of seconds to wait for a response from the WMS, use -1 for no constraints
     * @param validate
     *            whether to validate the values against the capabilities. Example: a format is requested that the
     *            server does not advertise. So the first advertised format will be used, and an entry will be put in
     *            the validationErrors list that says just that.
     * @param validationErrors
     *            a list of validation actions
     * @return an image from the server, or an error message from the service exception
     * @throws IOException
     */
    public Pair<BufferedImage, String> getMap( List<String> layers, int width, int height, Envelope bbox, ICRS srs,
                                               String format, boolean transparent, boolean errorsInImage, int timeout,
                                               boolean validate, List<String> validationErrors )
                            throws IOException {
        return getMap( layers, width, height, bbox, srs, format, transparent, errorsInImage, timeout, validate,
                       validationErrors, null );
    }

    /**
     * @param hardParameters
     *            parameters to override in the request, may be null
     * @throws IOException
     */
    public Pair<BufferedImage, String> getMap( List<String> layers, int width, int height, Envelope bbox, ICRS srs,
                                               String format, boolean transparent, boolean errorsInImage, int timeout,
                                               boolean validate, List<String> validationErrors,
                                               Map<String, String> hardParameters )
                            throws IOException {
        Worker worker = new Worker( layers, width, height, bbox, srs, format, transparent, errorsInImage, validate,
                                    validationErrors, hardParameters );

        Pair<BufferedImage, String> result;
        try {
            if ( timeout == -1 ) {
                result = worker.call();
            } else {

                result = Executor.getInstance().performSynchronously( worker, timeout * 1000 );
            }
        } catch ( Throwable e ) {
            throw new IOException( e.getMessage(), e );
        }

        return result;
    }

    /**
     * @param hardParameters
     *            parameters to override in the request, may be null
     * @throws IOException
     */
    public FeatureCollection getFeatureInfo( List<String> queryLayers, int width, int height, int x, int y,
                                             Envelope bbox, ICRS srs, int count, Map<String, String> hardParameters )
                            throws IOException {
        String url = getAddress( GetFeatureInfo, true );
        if ( url == null ) {
            LOG.warn( get( "WMSCLIENT.SERVER_NO_GETMAP_URL" ), "Capabilities: ", capabilities );
            return null;
        }
        if ( !url.endsWith( "?" ) && !url.endsWith( "&" ) ) {
            url += url.indexOf( "?" ) == -1 ? "?" : "&";
        }
        String lays = join( ",", queryLayers );

        Map<String, String> map = new HashMap<String, String>();
        map.put( "request", "GetFeatureInfo" );
        map.put( "version", "1.1.1" );
        map.put( "service", "WMS" );
        map.put( "layers", lays );
        map.put( "query_layers", lays );
        map.put( "styles", "" );
        map.put( "width", Integer.toString( width ) );
        map.put( "height", Integer.toString( height ) );
        map.put( "bbox", bbox.getMin().get0() + "," + bbox.getMin().get1() + "," + bbox.getMax().get0() + ","
                         + bbox.getMax().get1() );
        map.put( "srs", srs.getAlias() );
        map.put( "format", getFormats( GetMap ).getFirst() );
        map.put( "info_format", "application/vnd.ogc.gml" );
        map.put( "x", Integer.toString( x ) );
        map.put( "y", Integer.toString( y ) );
        map.put( "feature_count", Integer.toString( count ) );
        if ( hardParameters != null ) {
            for ( Entry<String, String> e : hardParameters.entrySet() ) {
                if ( map.containsKey( e.getKey().toLowerCase() ) ) {
                    LOG.debug( "Overriding preset parameter {}.", e.getKey() );
                    map.put( e.getKey().toLowerCase(), e.getValue() );
                } else
                    map.put( e.getKey(), e.getValue() );
            }
        }

        url += toQueryString( map );

        URL theUrl = new URL( url );
        LOG.debug( "Connecting to URL " + theUrl );
        URLConnection conn = ProxyUtils.openURLConnection( theUrl, getHttpProxyUser( true ),
                                                           getHttpProxyPassword( true ), httpBasicUser, httpBasicPass );
        conn.setConnectTimeout( connectionTimeout * 1000 );
        conn.setReadTimeout( requestTimeout * 1000 );
        conn.connect();
        LOG.debug( "Connected." );

        XMLInputFactory fac = XMLInputFactory.newInstance();
        XMLStreamReader xmlReader = null;
        try {
            xmlReader = fac.createXMLStreamReader( conn.getInputStream() );
            xmlReader.next();
            // ESRI workaround
            if ( ( xmlReader.getNamespaceURI() == null || xmlReader.getNamespaceURI().isEmpty() )
                 && xmlReader.getLocalName().equals( "FeatureInfoResponse" ) ) {
                return readESRICollection( xmlReader );
            }
            // myWMS workaround
            if ( ( xmlReader.getNamespaceURI() == null || xmlReader.getNamespaceURI().isEmpty() )
                 && xmlReader.getLocalName().equals( "featureInfo" ) ) {
                return readMyWMSCollection( xmlReader );
            }
            GMLStreamReader reader = createGMLStreamReader( GML_2, xmlReader );
            return reader.readFeatureCollection();
        } catch ( XMLStreamException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch ( XMLParsingException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch ( UnknownCRSException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            try {
                if ( xmlReader != null ) {
                    xmlReader.close();
                }
            } catch ( XMLStreamException e ) {
                LOG.trace( "Stack trace:", e );
            }
        }

        return null;
    }

    private FeatureCollection readESRICollection( XMLStreamReader reader )
                            throws NoSuchElementException, XMLStreamException {
        GenericFeatureCollection col = new GenericFeatureCollection();

        int count = 0;
        nextElement( reader );
        while ( reader.isStartElement() && reader.getLocalName().equals( "FIELDS" ) ) {
            List<PropertyType> props = new ArrayList<PropertyType>( reader.getAttributeCount() );
            List<Property> propValues = new ArrayList<Property>( reader.getAttributeCount() );
            for ( int i = 0; i < reader.getAttributeCount(); ++i ) {
                String name = reader.getAttributeLocalName( i );
                name = name.substring( name.lastIndexOf( "." ) + 1 );
                String value = reader.getAttributeValue( i );
                SimplePropertyType tp = new SimplePropertyType( new QName( name ), 0, 1, STRING, null, null );
                propValues.add( new SimpleProperty( tp, value ) );
                props.add( tp );
            }
            GenericFeatureType ft = new GenericFeatureType( new QName( "feature" ), props, false );
            col.add( new GenericFeature( ft, "esri_" + ++count, propValues, null ) );
            nextElement( reader );
        }

        return col;
    }

    private FeatureCollection readMyWMSCollection( XMLStreamReader reader )
                            throws NoSuchElementException, XMLStreamException {
        GenericFeatureCollection col = new GenericFeatureCollection();

        nextElement( reader );
        while ( reader.isStartElement() && reader.getLocalName().equals( "query_layer" ) ) {

            String ftName = reader.getAttributeValue( null, "name" );
            int count = 0;

            nextElement( reader );
            while ( reader.isStartElement() && reader.getLocalName().equals( "object" ) ) {

                List<PropertyType> props = new ArrayList<PropertyType>();
                List<Property> propValues = new ArrayList<Property>();

                nextElement( reader );
                while ( !( reader.isEndElement() && reader.getLocalName().equals( "object" ) ) ) {
                    String name = reader.getLocalName();
                    String value = reader.getElementText();
                    SimplePropertyType tp = new SimplePropertyType( new QName( name ), 0, 1, STRING, null, null );
                    propValues.add( new SimpleProperty( tp, value ) );
                    props.add( tp );
                    nextElement( reader );
                }

                GenericFeatureType ft = new GenericFeatureType( new QName( ftName ), props, false );
                col.add( new GenericFeature( ft, "ftName_" + ++count, propValues, null ) );
                nextElement( reader );
            }
            nextElement( reader );

        }

        return col;
    }

    /**
     * @param layers
     * @param width
     * @param height
     * @param bbox
     * @param srs
     * @param format
     * @param transparent
     * @param errorsInImage
     *            if true, no exceptions are thrown or validation errors are returned. The returned pair allows contains
     *            an image of the expected size.
     * @param timeout
     *            number of seconds to wait for a response from the WMS, use -1 for no constraints
     * @param validate
     *            whether to validate the values against the capabilities. Example: a format is requested that the
     *            server does not advertise. So the first advertised format will be used, and an entry will be put in
     *            the validationErrors list that says just that.
     * @param validationErrors
     *            a list of validation actions
     * @return an image from the server (using RGB or RGB color model, encoded as {@link PixelInterleavedRasterData}),
     *         or an error message from the service exception
     * @throws IOException
     */
    public Pair<SimpleRaster, String> getMapAsSimpleRaster( List<String> layers, int width, int height, Envelope bbox,
                                                            ICRS srs, String format, boolean transparent,
                                                            boolean errorsInImage, int timeout, boolean validate,
                                                            List<String> validationErrors )
                            throws IOException {

        Pair<BufferedImage, String> imageResponse = getMap( layers, width, height, bbox, srs, format, transparent,
                                                            errorsInImage, timeout, validate, validationErrors );
        Pair<SimpleRaster, String> response = new Pair<SimpleRaster, String>();
        if ( imageResponse.first != null ) {
            BufferedImage img = imageResponse.first;
            RasterData rasterData = RasterFactory.rasterDataFromImage( img );
            RasterGeoReference rasterEnv = RasterGeoReference.create( OriginLocation.OUTER, bbox, img.getWidth(),
                                                                      img.getHeight() );
            SimpleRaster raster = new SimpleRaster( rasterData, bbox, rasterEnv );
            response.first = raster;
        } else {
            response.second = imageResponse.second;
        }
        return response;
    }

    // -----------------------------------------------------------------------
    // Callable that does the HTTP communication, so WMSClient111#getMap()
    // can return with a reliable timeout
    // -----------------------------------------------------------------------

    private class Worker implements Callable<Pair<BufferedImage, String>> {

        private List<String> layers;

        private int width;

        private int height;

        private Envelope bbox;

        private ICRS srs;

        private String format;

        private boolean transparent;

        private boolean errorsInImage;

        private boolean validate;

        private List<String> validationErrors;

        private final Map<String, String> hardParameters;

        Worker( List<String> layers, int width, int height, Envelope bbox, ICRS srs, String format,
                boolean transparent, boolean errorsInImage, boolean validate, List<String> validationErrors,
                Map<String, String> hardParameters ) {
            this.layers = layers;
            this.width = width;
            this.height = height;
            this.bbox = bbox;
            this.srs = srs;
            this.format = format;
            this.transparent = transparent;
            this.errorsInImage = errorsInImage;
            this.validate = validate;
            this.validationErrors = validationErrors;
            this.hardParameters = hardParameters;
        }

        @Override
        public Pair<BufferedImage, String> call()
                                throws Exception {
            return getMap( layers, width, height, bbox, srs, format, transparent, errorsInImage, validate,
                           validationErrors );
        }

        private Pair<BufferedImage, String> getMap( List<String> layers, int width, int height, Envelope bbox,
                                                    ICRS srs, String format, boolean transparent,
                                                    boolean errorsInImage, boolean validate,
                                                    List<String> validationErrors )
                                throws IOException {
            if ( ( maxMapWidth != -1 && width > maxMapWidth ) || ( maxMapHeight != -1 && height > maxMapHeight ) ) {
                return getTiledMap( layers, width, height, bbox, srs, format, transparent, errorsInImage, validate,
                                    validationErrors );
            }

            Pair<BufferedImage, String> res = new Pair<BufferedImage, String>();

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

                Map<String, String> map = new HashMap<String, String>();
                map.put( "request", "GetMap" );
                map.put( "version", "1.1.1" );
                map.put( "service", "WMS" );
                map.put( "layers", join( ",", layers ) );
                map.put( "styles", "" );
                map.put( "width", Integer.toString( width ) );
                map.put( "height", Integer.toString( height ) );
                map.put( "bbox", bbox.getMin().get0() + "," + bbox.getMin().get1() + "," + bbox.getMax().get0() + ","
                                 + bbox.getMax().get1() );
                map.put( "srs", srs.getAlias() );
                map.put( "format", format );
                map.put( "transparent", Boolean.toString( transparent ) );
                if ( hardParameters != null ) {
                    for ( Entry<String, String> e : hardParameters.entrySet() ) {
                        if ( map.containsKey( e.getKey().toLowerCase() ) ) {
                            LOG.debug( "Overriding preset parameter {}.", e.getKey() );
                            map.put( e.getKey().toLowerCase(), e.getValue() );
                        } else
                            map.put( e.getKey(), e.getValue() );
                    }
                }

                url += toQueryString( map );

                URL theUrl = new URL( url );
                LOG.debug( "Connecting to URL " + theUrl );
                URLConnection conn = ProxyUtils.openURLConnection( theUrl, ProxyUtils.getHttpProxyUser( true ),
                                                                   ProxyUtils.getHttpProxyPassword( true ),
                                                                   httpBasicUser, httpBasicPass );
                conn.setConnectTimeout( connectionTimeout * 1000 );
                conn.setReadTimeout( requestTimeout * 1000 );
                conn.connect();
                LOG.debug( "Connected." );
                if ( LOG.isTraceEnabled() ) {
                    LOG.trace( "Requesting from " + theUrl );
                    LOG.trace( "Content type is " + conn.getContentType() );
                    LOG.trace( "Content encoding is " + conn.getContentEncoding() );
                }
                if ( conn.getContentType() != null && conn.getContentType().startsWith( format ) ) {
                    res.first = IMAGE.work( conn.getInputStream() );
                } else if ( conn.getContentType() != null
                            && conn.getContentType().startsWith( "application/vnd.ogc.se_xml" ) ) {
                    res.second = XML.work( conn.getInputStream() ).toString();
                } else { // try and find out the hard way
                    res.first = IMAGE.work( conn.getInputStream() );
                    if ( res.first == null ) {
                        conn = theUrl.openConnection();
                        res.second = XML.work( conn.getInputStream() ).toString();
                    }
                }
                LOG.debug( "Received response." );
            } catch ( RuntimeException e ) {
                LOG.info( "Error performing GetMap request: " + e.getMessage(), e );
                res.second = e.getMessage();
            }

            if ( errorsInImage && res.first == null ) {
                // TODO create image of type RGBA / RGB
                res.first = createErrorImage( res.second, width, height, transparent ? BufferedImage.TYPE_4BYTE_ABGR
                                                                                    : BufferedImage.TYPE_3BYTE_BGR );
                res.second = null;
            }

            if ( LOG.isDebugEnabled() && res.first != null ) {
                File tmpFile = File.createTempFile( "WMSClient", ".png" );
                ImageIO.write( res.first, "png", tmpFile );
            }

            return res;
        }

        private BufferedImage createErrorImage( String error, int width, int height, int type ) {

            BufferedImage result = new BufferedImage( width, height, type );
            Graphics2D g = (Graphics2D) result.getGraphics();
            // TODO use optimized coordinates and font size
            g.setColor( Color.BLACK );
            g.fillRect( 0, 0, width - 1, height - 1 );
            g.setColor( Color.WHITE );
            g.drawString( "Error: " + error, 0, 12 );
            return result;

        }

        // TODO handle axis direction and order correctly, depends on srs
        private Pair<BufferedImage, String> getTiledMap( List<String> layers, int width, int height, Envelope bbox,
                                                         ICRS srs, String format, boolean transparent,
                                                         boolean errorsInImage, boolean validate,
                                                         List<String> validationErrors )
                                throws IOException {

            Pair<BufferedImage, String> response = new Pair<BufferedImage, String>();
            BufferedImage compositedImage = null;
            if ( transparent ) {
                // TODO create image of type RGBA
                compositedImage = new BufferedImage( width, height, BufferedImage.TYPE_4BYTE_ABGR );
            } else {
                // TODO create image of type RGB
                compositedImage = new BufferedImage( width, height, BufferedImage.TYPE_3BYTE_BGR );
            }

            response.first = compositedImage;

            RasterGeoReference rasterEnv = RasterGeoReference.create( OriginLocation.OUTER, bbox, width, height );

            if ( maxMapWidth != -1 ) {
                int xMin = 0;
                while ( xMin <= width - 1 ) {
                    int xMax = xMin + maxMapWidth - 1;
                    if ( xMax > width - 1 ) {
                        xMax = width - 1;
                    }
                    if ( maxMapHeight != -1 ) {
                        int yMin = 0;
                        while ( yMin <= height - 1 ) {
                            int yMax = yMin + maxMapHeight - 1;
                            if ( yMax > height - 1 ) {
                                yMax = height - 1;
                            }
                            getAndSetSubImage( compositedImage, layers, xMin, ( xMax - xMin ) + 1, yMin,
                                               ( yMax - yMin ) + 1, rasterEnv, srs, format, transparent, errorsInImage );
                            yMin = yMax + 1;
                        }
                    }
                    xMin = xMax + 1;
                }
            } else {
                if ( maxMapHeight != -1 ) {
                    int yMin = 0;
                    while ( yMin <= height - 1 ) {
                        int yMax = yMin + maxMapHeight - 1;
                        if ( yMax > height - 1 ) {
                            yMax = height - 1;
                        }
                        int xMin = 0;
                        int xMax = width - 1;
                        getAndSetSubImage( compositedImage, layers, xMin, ( xMax - xMin ) + 1, yMin,
                                           ( yMax - yMin ) + 1, rasterEnv, srs, format, transparent, errorsInImage );
                        yMin = yMax + 1;
                    }
                }
            }
            return response;
        }

        private void getAndSetSubImage( BufferedImage targetImage, List<String> layers, int xMin, int width, int yMin,
                                        int height, RasterGeoReference rasterEnv, ICRS crs, String format,
                                        boolean transparent, boolean errorsInImage )
                                throws IOException {

            double[] min = rasterEnv.getWorldCoordinate( xMin, yMin + height );
            double[] max = rasterEnv.getWorldCoordinate( xMin + width, yMin );

            Envelope env = new GeometryFactory().createEnvelope( min, max, crs );
            Pair<BufferedImage, String> response = getMap( layers, width, height, env, crs, format, transparent,
                                                           errorsInImage, false, null );
            if ( response.second != null ) {
                throw new IOException( response.second );
            }
            targetImage.getGraphics().drawImage( response.first, xMin, yMin, null );
        }
    }
}
