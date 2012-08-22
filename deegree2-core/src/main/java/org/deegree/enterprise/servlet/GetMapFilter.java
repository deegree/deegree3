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

package org.deegree.enterprise.servlet;

import static java.awt.Color.decode;
import static java.awt.Color.white;
import static java.awt.image.BufferedImage.TYPE_INT_ARGB;
import static java.util.Arrays.asList;
import static java.util.Collections.disjoint;
import static java.util.Collections.sort;
import static org.deegree.framework.log.LoggerFactory.getLogger;
import static org.deegree.framework.util.CollectionUtils.collectionToString;
import static org.deegree.framework.util.CollectionUtils.map;
import static org.deegree.framework.util.StringTools.arrayToString;
import static org.deegree.framework.util.WebappResourceResolver.resolveFileLocation;
import static org.deegree.framework.xml.XMLTools.appendElement;
import static org.deegree.framework.xml.XMLTools.importStringFragment;
import static org.deegree.model.crs.CRSFactory.create;
import static org.deegree.model.spatialschema.GMLGeometryAdapter.exportAsBox;
import static org.deegree.model.spatialschema.GeometryFactory.createEnvelope;
import static org.deegree.ogcbase.CommonNamespaces.OGCNS;
import static org.deegree.ogcbase.CommonNamespaces.WFSNS;
import static org.deegree.ogcbase.CommonNamespaces.getNamespaceContext;
import static org.deegree.ogcwebservices.OGCRequestFactory.createFromKVP;
import static org.deegree.ogcwebservices.wfs.WFServiceFactory.createInstance;
import static org.deegree.ogcwebservices.wfs.WFServiceFactory.setConfiguration;
import static org.deegree.ogcwebservices.wfs.operation.GetFeature.create;
import static org.deegree.ogcwebservices.wms.WMServiceFactory.getService;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import org.deegree.datatypes.QualifiedName;
import org.deegree.enterprise.ServiceException;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.util.CollectionUtils.Mapper;
import org.deegree.framework.xml.InvalidConfigurationException;
import org.deegree.framework.xml.NamespaceContext;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.graphics.sld.AbstractLayer;
import org.deegree.model.crs.UnknownCRSException;
import org.deegree.model.feature.Feature;
import org.deegree.model.feature.FeatureCollection;
import org.deegree.model.spatialschema.Envelope;
import org.deegree.ogcwebservices.OGCWebServiceException;
import org.deegree.ogcwebservices.OGCWebServiceRequest;
import org.deegree.ogcwebservices.wfs.WFService;
import org.deegree.ogcwebservices.wfs.operation.FeatureResult;
import org.deegree.ogcwebservices.wms.operation.GetMap;
import org.deegree.ogcwebservices.wms.operation.GetMapResult;
import org.deegree.ogcwebservices.wms.operation.GetMap.Layer;
import org.w3c.dom.Element;

/**
 * <code>GetMapFilter</code>
 *
 * Init parameters:
 *
 * <ul>
 * <li>prefix - default is app</li>
 * <li>namespace - default is http://www.deegree.org/app</li>
 * <li>typeName - no default</li>
 * <li>geometryProperty - default is app:geometry</li>
 * <li>propertyName - no default</li>
 * <li>excludedLayers - default is empty list (no excluded layers)</li>
 * <li>coverStyle - default is not to request cover layers</li>
 * <li>coverColor - default is #ffffff</li>
 * <li>onlyForSLDRequests - the cover-layer mechanism is only used for SLD requests</li>
 * </ul>
 *
 * It is assumed that the WMS and its local WFS are configured in the same context as the filter. Take note that if you
 * choose a property which occurs with null values, these features will be SKIPPED and NOT PAINTED, you will MISS THEM.
 *
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class GetMapFilter implements Filter {

    private static final ILogger LOG = getLogger( GetMapFilter.class );

    private static final NamespaceContext nsContext = getNamespaceContext();

    private String prefix = "app", namespace = "http://www.deegree.org/app", typeName, geomProperty = "app:geometry",
                            filterProperty, wmsFilterProperty, sortProperty, coverStyle;

    private Color coverColor = white;

    private boolean onlyForSLDRequests;

    private QualifiedName filterPropertyQ, sortPropertyQ;

    private TreeSet<String> excludedLayers;

    private WFService wfs;

    public void destroy() {
        // nothing to do
    }

    private static Map<String, String> normalizeMap( ServletRequest request, ServletResponse response, FilterChain chain )
                            throws IOException, ServletException {
        Map<?, ?> params = request.getParameterMap();

        Map<String, String> map = new TreeMap<String, String>();

        for ( Object key : params.keySet() ) {
            map.put( ( (String) key ).toUpperCase(), arrayToString( (String[]) params.get( key ), ',' ) );
        }

        if ( map.size() == 0 ) {
            chain.doFilter( request, response );
            return null;
        }

        if ( map.get( "SERVICE" ) == null || !map.get( "SERVICE" ).equalsIgnoreCase( "wms" ) ) {
            chain.doFilter( request, response );
            return null;
        }

        if ( map.get( "REQUEST" ) == null || !map.get( "REQUEST" ).equalsIgnoreCase( "getmap" ) ) {
            chain.doFilter( request, response );
            return null;
        }

        if ( map.get( "FILTERPROPERTY" ) != null ) {
            chain.doFilter( request, response );
            return null;
        }

        if ( LOG.isDebug() ) {
            LOG.logDebug( "Incoming request values", map );
        }

        return map;
    }

    public void doFilter( ServletRequest request, ServletResponse response, FilterChain chain )
                            throws IOException, ServletException {

        Map<String, String> map = normalizeMap( request, response, chain );

        if ( map == null ) {
            return;
        }

        WMSHandler handler = new WMSHandler();
        // in case the request parsing goes wrong...
        handler.determineExceptionFormat( null, null, null, (HttpServletResponse) response );

        GetMap getMap;
        try {
            getMap = (GetMap) createFromKVP( new TreeMap<String, String>( map ) );
        } catch ( OGCWebServiceException e ) {
            handler.writeServiceExceptionReport( e );
            LOG.logError( "Unknown error", e );
            return;
        }

        Envelope env;
        try {
            env = createEnvelope( map.get( "BBOX" ), create( map.get( "SRS" ) ) );
        } catch ( UnknownCRSException e ) {
            // just in case...
            LOG.logWarning( "deegree could not find the coordinate system " + map.get( "SRS" ) + "." );
            LOG.logWarning( "Continuing without SRS. Probably the failure will come later..." );
            env = createEnvelope( map.get( "BBOX" ), null );
        }

        List<String> values = doGetFeature( env );

        TreeSet<String> lays = new TreeSet<String>();
        lays.addAll( map( getMap.getLayers(), new Mapper<String, Layer>() {
            public String apply( Layer u ) {
                return u.getName();
            }
        } ) );
        if ( getMap.getStyledLayerDescriptor() != null ) {
            lays.addAll( map( getMap.getStyledLayerDescriptor().getNamedLayers(), new Mapper<String, AbstractLayer>() {
                public String apply( AbstractLayer u ) {
                    return u.getName();
                }
            } ) );
        }

        if ( !disjoint( lays, excludedLayers ) ) {
            chain.doFilter( request, response );
            return;
        }

        if ( values == null ) {
            // output error message?
            chain.doFilter( request, response );
            return;
        }

        sort( values );

        // remove duplicates
        LinkedList<String> uniq = new LinkedList<String>();
        for ( String v : values ) {
            if ( !uniq.contains( v ) ) {
                uniq.add( v );
            }
        }

        // request and combine the maps
        try {
            DummyRequest req = new DummyRequest( getMap );
            handler.perform( req, (HttpServletResponse) response );
            boolean requestCovers = coverStyle != null
                                    && ( ( map.get( "SLD" ) != null || map.get( "SLD_BODY" ) != null ) || !onlyForSLDRequests );
            GetMapResult result = renderMaps( requestCovers, getMap, doGetMaps( uniq, map, lays ) );

            if ( result == null ) {
                // probably an empty map, but before we do it by hand...
                chain.doFilter( request, response );
                return;
            }

            handler.setRequest( getMap );
            handler.handleGetMapResponse( result );
        } catch ( OGCWebServiceException e ) {
            handler.writeServiceExceptionReport( e );
            LOG.logError( "Unknown error", e );
        } catch ( ServiceException e ) {
            handler.writeServiceExceptionReport( new OGCWebServiceException( e.getLocalizedMessage() ) );
            LOG.logError( "Unknown error", e );
        }

    }

    private GetMapResult renderMaps( boolean requestCovers, GetMap req, LinkedList<Object> imgs ) {
        if ( imgs.size() == 0 ) {
            return null;
        }

        if ( imgs.size() == 1 ) {
            return (GetMapResult) imgs.poll();
        }

        BufferedImage first = (BufferedImage) ( (GetMapResult) imgs.peek() ).getMap();

        BufferedImage img = new BufferedImage( first.getWidth(), first.getHeight(), TYPE_INT_ARGB );

        GetMapResult result = new GetMapResult( req, img );

        Graphics2D g = img.createGraphics();

        for ( Object i : imgs ) {
            GetMapResult res = (GetMapResult) i;
            if ( res.getException() != null ) {
                result = res;
                break;
            }

            g.drawImage( (Image) res.getMap(), 0, 0, null );
        }

        g.dispose();

        LOG.logDebug( "Finished painting maps in GetMapFilter." );

        if ( requestCovers ) {
            LOG.logDebug( "Replacing colored pixels by transparent pixels." );
            for ( int x = 0; x < img.getWidth(); ++x ) {
                for ( int y = 0; y < img.getHeight(); ++y ) {
                    if ( coverColor.equals( new Color( img.getRGB( x, y ) ) ) ) {
                        img.setRGB( x, y, 0 );
                    }
                }
            }
        }

        return result;
    }

    private LinkedList<Object> doGetMaps( List<String> values, Map<String, String> map, TreeSet<String> layers )
                            throws OGCWebServiceException {

        boolean requestCovers = coverStyle != null
                                && ( ( map.get( "SLD" ) != null || map.get( "SLD_BODY" ) != null ) || !onlyForSLDRequests );

        if ( LOG.isDebug() ) {
            LOG.logDebug( "The filter will request " + values.size() + " maps." );
            LOG.logDebug( "Found values", values );
        }

        int ls = layers.size();
        String styles = "";
        if ( requestCovers ) {
            for ( int i = 0; i < ls; ++i ) {
                styles += coverStyle;
                if ( i != ls - 1 ) {
                    styles += ",";
                }
            }
        }

        LinkedList<Object> imgs = new LinkedList<Object>();

        TreeMap<String, String> newMap = new TreeMap<String, String>();

        map.put( "FILTERPROPERTY", wmsFilterProperty );
        for ( String val : values ) {
            if ( requestCovers && !val.equals( values.get( 0 ) ) ) {
                newMap.clear();
                newMap.putAll( map );
                newMap.remove( "FILTERVALUE" );
                newMap.remove( "TRANSPARENT" );
                if ( newMap.get( "LAYERS" ) == null ) {
                    if ( !layers.isEmpty() ) {
                        newMap.put( "LAYERS", collectionToString( layers, "," ) );
                        newMap.put( "STYLES", styles );
                        newMap.remove( "SLD" );
                    }
                } else {
                    newMap.put( "STYLES", styles );
                }
                newMap.put( "FILTERVALUE", val );
                newMap.put( "TRANSPARENT", "true" );
                if ( LOG.isDebug() ) {
                    LOG.logDebug( "Requested a cover layer", newMap );
                }
                imgs.add( getService().doService( createFromKVP( newMap ) ) );
            }
            newMap.clear();
            newMap.putAll( map );
            newMap.remove( "FILTERVALUE" );
            newMap.remove( "TRANSPARENT" );
            if ( val.equals( values.get( 0 ) ) ) {
                newMap.put( "TRANSPARENT", map.get( "TRANSPARENT" ) );
            } else {
                newMap.put( "TRANSPARENT", "true" );
            }
            newMap.put( "FILTERVALUE", val );
            if ( LOG.isDebug() ) {
                LOG.logDebug( "Requested a filtered layer", newMap );
            }
            imgs.add( getService().doService( createFromKVP( newMap ) ) );
        }

        LOG.logDebug( "Finished requesting maps in GetMapFilter." );

        return imgs;
    }

    private List<String> doGetFeature( Envelope bbox ) {
        XMLFragment doc = new XMLFragment( new QualifiedName( "wfs:GetFeature", WFSNS ) );

        Element root = doc.getRootElement();
        root.setAttribute( "version", "1.1.0" );
        root.setAttribute( "xmlns:" + prefix, namespace );
        Element elem = appendElement( root, WFSNS, "wfs:Query" );
        elem.setAttribute( "typeName", typeName );

        appendElement( elem, WFSNS, "wfs:PropertyName", filterProperty );
        appendElement( elem, WFSNS, "wfs:PropertyName", sortProperty );
        elem = appendElement( elem, OGCNS, "ogc:Filter" );
        elem = appendElement( elem, OGCNS, "ogc:BBOX" );
        appendElement( elem, OGCNS, "ogc:PropertyName", geomProperty );

        StringBuffer str = exportAsBox( bbox );
        Element el = importStringFragment( str.toString(), root.getOwnerDocument() );
        el.setAttribute( "srsName", bbox.getCoordinateSystem().getIdentifier() );
        elem.appendChild( el );

        if ( LOG.isDebug() ) {
            LOG.logDebug( "GetFeature request", doc.getAsPrettyString() );
        }

        try {
            FeatureResult res = (FeatureResult) wfs.doService( create( "bogus", doc.getRootElement() ) );
            FeatureCollection col = (FeatureCollection) res.getResponse();

            TreeMap<String, String> values = new TreeMap<String, String>();

            for ( int i = 0; i < col.size(); ++i ) {
                Feature f = col.getFeature( i );
                Object v1 = f.getProperties( filterPropertyQ )[0].getValue();
                Object v2 = f.getProperties( sortPropertyQ )[0].getValue();
                if ( v1 != null && v2 != null ) {
                    values.put( v2.toString(), v1.toString() );
                }
            }

            LinkedList<String> r = new LinkedList<String>();
            for ( String key : values.keySet() ) {
                r.add( values.get( key ) );
            }

            return r;
        } catch ( OGCWebServiceException e ) {
            LOG.logError( "An unknown error occurred", e );
        }

        return null;
    }

    public void init( FilterConfig config )
                            throws ServletException {
        excludedLayers = new TreeSet<String>();

        try {
            Enumeration<?> e = config.getInitParameterNames();
            while ( e.hasMoreElements() ) {
                String name = (String) e.nextElement();
                String iname = name.toLowerCase();
                if ( iname.equals( "namespace" ) ) {
                    namespace = config.getInitParameter( name );
                }
                if ( iname.equals( "prefix" ) ) {
                    prefix = config.getInitParameter( name );
                }
                if ( iname.equals( "typename" ) ) {
                    typeName = config.getInitParameter( name );
                }
                if ( iname.equals( "sortproperty" ) ) {
                    sortProperty = config.getInitParameter( name );
                }
                if ( iname.equals( "filterproperty" ) ) {
                    filterProperty = config.getInitParameter( name );
                }
                if ( iname.equals( "wmsfilterproperty" ) ) {
                    wmsFilterProperty = config.getInitParameter( name );
                }
                if ( iname.equals( "geometryproperty" ) ) {
                    geomProperty = config.getInitParameter( name );
                }
                if ( iname.equals( "excludelayers" ) ) {
                    excludedLayers.addAll( asList( config.getInitParameter( name ).split( "," ) ) );
                }
                if ( iname.equals( "coverstyle" ) ) {
                    coverStyle = config.getInitParameter( name );
                }
                if ( iname.equals( "covercolor" ) ) {
                    coverColor = decode( config.getInitParameter( name ) );
                }
                if ( iname.equals( "onlyforsldrequests" ) ) {
                    onlyForSLDRequests = config.getInitParameter( name ).equalsIgnoreCase( "true" );
                }
                if ( iname.equals( "wfsconfiguration" ) ) {
                    String cfg = config.getInitParameter( name );
                    setConfiguration( resolveFileLocation( cfg, config.getServletContext(), LOG ) );
                    wfs = createInstance();
                }
            }

            URI uri = new URI( namespace );
            nsContext.addNamespace( prefix, uri );
            filterPropertyQ = new QualifiedName( filterProperty, uri );
            sortPropertyQ = new QualifiedName( sortProperty, uri );

            LOG.logInfo( "GetMap filter initialized." );
        } catch ( URISyntaxException e ) {
            LOG.logError( "Your configuration is not correct. The namespace is not an URI", e );
        } catch ( MalformedURLException e ) {
            LOG.logError( "Unknown error", e );
        } catch ( InvalidConfigurationException e ) {
            LOG.logError( "The WFS configuration was not correct", e );
        } catch ( IOException e ) {
            LOG.logError( "The WFS configuration could not be read", e );
        } catch ( OGCWebServiceException e ) {
            LOG.logError( "The WFS could not be initialized", e );
        }
    }

    /**
     * <code>DummyRequest</code>
     *
     * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
     * @author last edited by: $Author$
     *
     * @version $Revision$, $Date$
     */
    public static class DummyRequest implements OGCWebServiceRequest {

        private OGCWebServiceRequest req;

        /**
         * @param req
         */
        public DummyRequest( OGCWebServiceRequest req ) {
            this.req = req;
        }

        public String getId() {
            return req.getId();
        }

        public String getRequestParameter()
                                throws OGCWebServiceException {
            return null;
        }

        public String getServiceName() {
            return req.getServiceName();
        }

        public String getVendorSpecificParameter( String name ) {
            return null;
        }

        public Map<String, String> getVendorSpecificParameters() {
            return null;
        }

        public String getVersion() {
            return req.getVersion();
        }
    }

}
