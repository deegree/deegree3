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
package org.deegree.portal.portlet.modules.wfs.actions.portlets;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.jetspeed.portal.Portlet;
import org.deegree.datatypes.Types;
import org.deegree.enterprise.WebUtils;
import org.deegree.enterprise.control.RPCException;
import org.deegree.enterprise.control.RPCFactory;
import org.deegree.enterprise.control.RPCMethodCall;
import org.deegree.enterprise.control.RPCParameter;
import org.deegree.enterprise.control.RPCStruct;
import org.deegree.enterprise.control.RPCUtils;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.StringTools;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.framework.xml.XSLTDocument;
import org.deegree.model.crs.CRSFactory;
import org.deegree.model.crs.CoordinateSystem;
import org.deegree.model.crs.GeoTransformer;
import org.deegree.model.crs.UnknownCRSException;
import org.deegree.model.feature.Feature;
import org.deegree.model.feature.FeatureCollection;
import org.deegree.model.feature.FeatureFactory;
import org.deegree.model.feature.FeatureProperty;
import org.deegree.model.feature.GMLFeatureAdapter;
import org.deegree.model.feature.GMLFeatureCollectionDocument;
import org.deegree.model.feature.schema.FeatureType;
import org.deegree.model.spatialschema.Geometry;
import org.deegree.ogcwebservices.OGCWebServiceException;
import org.deegree.ogcwebservices.OWSUtils;
import org.deegree.ogcwebservices.getcapabilities.InvalidCapabilitiesException;
import org.deegree.ogcwebservices.wfs.capabilities.WFSCapabilities;
import org.deegree.ogcwebservices.wfs.capabilities.WFSCapabilitiesDocument;
import org.deegree.ogcwebservices.wfs.operation.GetFeature;
import org.deegree.portal.PortalException;
import org.deegree.portal.portlet.modules.actions.IGeoPortalPortletPerform;

/**
 *
 *
 * @version $Revision$
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version 1.0. $Revision$, $Date$
 *
 * @since 2.0
 */
public class WFSClientPortletPerform extends IGeoPortalPortletPerform {

    private static final ILogger LOG = LoggerFactory.getLogger( WFSClientPortletPerform.class );

    protected static final String INIT_TARGETSRS = "TARGETSRS";

    protected static final String INIT_XSLT = "XSLT";

    private static Map<String, WFSCapabilities> capaMap = new HashMap<String, WFSCapabilities>();

    /**
     * @param request
     * @param portlet
     * @param servletContext
     */
    public WFSClientPortletPerform( HttpServletRequest request, Portlet portlet, ServletContext servletContext ) {
        super( request, portlet, servletContext );

    }

    protected void doGetfeature()
                            throws PortalException, OGCWebServiceException {

        RPCParameter[] rpcParams = extractRPCParameters();
        Map<String, FeatureCollection> allFCs = new HashMap<String, FeatureCollection>();
        for ( int i = 1; i < rpcParams.length; i++ ) {
            // first field will be skipped because it contains informations
            // about the desired result format
            RPCStruct struct = (RPCStruct) rpcParams[i].getValue();

            String tmp = RPCUtils.getRpcPropertyAsString( struct, "featureTypes" );

            String[] arr = StringTools.toArray( tmp, ",", true );
            String[] xmlns = new String[arr.length];
            String[] featureTypes = new String[arr.length];
            for ( int j = 0; j < arr.length; j++ ) {
                int p = arr[j].lastIndexOf( ':' );
                xmlns[j] = arr[j].substring( 0, p );
                featureTypes[j] = arr[j].substring( p + 1, arr[j].length() );
            }

            for ( int j = 0; j < featureTypes.length; j++ ) {

                String query = createQuery( struct, xmlns, featureTypes );

                LOG.logDebug( "queried feature type: " + xmlns[j] + featureTypes[j] );
                LOG.logDebug( "Query: \n" + query );

                Map<String, FeatureCollection> fcs = null;

                try {
                    fcs = performQuery( featureTypes[j], xmlns[j], query );
                } catch ( UnsupportedEncodingException e ) {
                    LOG.logError( e.getMessage(), e );
                    throw new PortalException( e.getMessage(), e );
                }

                if ( getInitParam( INIT_TARGETSRS ) != null ) {
                    Iterator<String> iter = fcs.keySet().iterator();
                    while ( iter.hasNext() ) {
                        String key = iter.next();
                        FeatureCollection tmpFc = fcs.get( key );
                        fcs.put( key, transformGeometries( tmpFc ) );
                    }
                }

                allFCs.putAll( fcs );

            }

        }
        writeGetFeatureResult( allFCs, (String) rpcParams[0].getValue() );
    }

    /**
     * creates a WFS query depending on requested construction type
     *
     * @param struct
     * @param xmlns
     * @param featureTypes
     * @return the query
     * @throws PortalException
     */
    private String createQuery( RPCStruct struct, String[] xmlns, String[] featureTypes )
                            throws PortalException {
        String query = null;
        String template = RPCUtils.getRpcPropertyAsString( struct, "queryTemplate" );
        if ( template != null ) {
            RPCParameter[] filterProps = null;
            if ( struct.getMember( "filterProperties" ) != null ) {
                filterProps = (RPCParameter[]) struct.getMember( "filterProperties" ).getValue();
            }
            query = createQueryFromTemplate( template, filterProps );
        } else if ( parameter.get( "FILTER" ) != null ) {
            String filter = parameter.get( "FILTER" );
            query = createQueryFromFilter( featureTypes, xmlns, filter );
        } else {
            String filter = createFilterFromProperties();
            query = createQueryFromFilter( featureTypes, xmlns, filter );
        }
        return query;
    }

    /**
     * extracts the
     *
     * @see RPCParameter array from the RPC method call
     * @return the array
     * @throws PortalException
     */
    protected RPCParameter[] extractRPCParameters()
                            throws PortalException {
        String tmp = parameter.get( "RPC" );

        StringReader sr = new StringReader( tmp );
        RPCMethodCall rpcMethod = null;
        try {
            rpcMethod = RPCFactory.createRPCMethodCall( sr );
        } catch ( RPCException e ) {
            LOG.logError( e.getMessage(), e );
            throw new PortalException( e.getMessage() );
        }

        RPCParameter[] rpcParams = rpcMethod.getParameters();
        return rpcParams;
    }

    /**
     * performs a transaction against a WFS-T or a database. The backend type to be used by a
     * transaction depends on a portlets initParameters.
     *
     */
    public void doTransaction() {
        System.out.println( parameter );
        // throw new UnsupportedOperationException();
    }

    /**
     * writes the result into the forwarded request object
     *
     * @param xml
     * @param fc
     * @throws PortalException
     */
    private void writeGetFeatureResult( Map<String, FeatureCollection> fcs, String format )
                            throws PortalException {
        if ( "XML".equals( format ) ) {
            XMLFragment xml = new XMLFragment();

            if ( fcs != null ) {
                FeatureCollection fc = FeatureFactory.createFeatureCollection( "ID", 1000 );
                Iterator<String> iter = fcs.keySet().iterator();
                while ( iter.hasNext() ) {
                    fc.addAllUncontained( fcs.get( iter.next() ) );
                }
                ByteArrayOutputStream bos = new ByteArrayOutputStream( 100000 );
                try {
                    new GMLFeatureAdapter().export( fc, bos );
                    xml.load( new ByteArrayInputStream( bos.toByteArray() ), XMLFragment.DEFAULT_URL );
                } catch ( Exception e ) {
                    LOG.logError( e.getMessage(), e );
                    throw new PortalException( "could not export feature collection as GML", e );
                }
                if ( getInitParam( INIT_XSLT ) != null ) {
                    xml = transform( xml );
                }
            }

            request.setAttribute( "RESULT", xml );
        } else {
            request.setAttribute( "RESULT", fcs );
        }
    }

    /**
     * transforms the result of a WFS request using the XSLT script defined by an init parameter
     *
     * @param xml
     * @return the transformed XML
     * @throws PortalException
     */
    private XMLFragment transform( XMLFragment xml )
                            throws PortalException {
        String xslF = getInitParam( INIT_XSLT );
        File file = new File( xslF );
        if ( !file.isAbsolute() ) {
            file = new File( sc.getRealPath( xslF ) );
        }
        XSLTDocument xslt = new XSLTDocument();
        try {
            xslt.load( file.toURI().toURL() );
            xml = xslt.transform( xml );
        } catch ( Exception e ) {
            LOG.logError( e.getMessage(), e );
            throw new PortalException( "could not transform result of WFS request", e );
        }
        return xml;
    }

    /**
     * transforms the geometry properties of the features contained in the passed feature collection
     * into the target CRS given by an init parameter
     *
     * @param fc
     * @return the transformed feature collection
     * @throws PortalException
     */
    private FeatureCollection transformGeometries( FeatureCollection fc )
                            throws PortalException {
        String cs = getInitParam( INIT_TARGETSRS );
        CoordinateSystem crs;
        try {
            crs = CRSFactory.create( cs );
        } catch ( UnknownCRSException e1 ) {
            throw new PortalException( e1.getMessage(), e1 );
        }
        if ( crs == null ) {
            throw new PortalException( "CRS: " + cs + " is not known by deegree" );
        }
        try {
            GeoTransformer gt = new GeoTransformer( crs );
            for ( int i = 0; i < fc.size(); i++ ) {
                Feature feature = fc.getFeature( i );
                FeatureType ft = feature.getFeatureType();
                FeatureProperty[] fp = feature.getProperties();
                for ( int j = 0; j < fp.length; j++ ) {
                    if ( ft.getProperty( fp[j].getName() ).getType() == Types.GEOMETRY ) {
                        Geometry geom = (Geometry) fp[j].getValue();
                        if ( !crs.equals( geom.getCoordinateSystem() ) ) {
                            geom = gt.transform( geom );
                            fp[j].setValue( geom );
                        }
                    }
                }
            }
        } catch ( Exception e ) {
            LOG.logError( e.getMessage(), e );
            throw new PortalException( "could not transform geometries to target CRS: " + cs, e );
        }
        return fc;
    }

    /**
     * performs a GetFeature query against one or more WFS's
     *
     * @param featureType
     * @param namespace
     * @param query
     * @return the map
     * @throws OGCWebServiceException
     * @throws UnsupportedEncodingException
     */
    private Map<String, FeatureCollection> performQuery( String featureType, String namespace, String query )
                            throws OGCWebServiceException, UnsupportedEncodingException {
        // WFS to contact
        String addr = getInitParam( namespace + ':' + featureType );
        if ( addr == null ) {
            // if a client does not send the name of the target WFS
            // 'WFS' will be used to get the target WFS address from
            // the portlets init-parameter
            addr = getInitParam( "WFS" );
        }
        if ( addr == null ) {
            throw new OGCWebServiceException( "WFS: " + namespace + ':' + featureType + " is not known by the portal" );
        }

        // a featuretype may be assigned to more than one WFS
        String[] addresses = StringTools.toArray( addr, ",", false );
        Map<String, FeatureCollection> docs = new HashMap<String, FeatureCollection>();
        for ( int i = 0; i < addresses.length; i++ ) {
            if ( capaMap.get( addresses[i] ) == null ) {
                // if the WFS Capabilities has not already been read from this
                // address it will be done now. The result will be stored in the
                // static Map 'capaMap' to be available at the next call
                loadWFSCapabilities( addresses[i] );
            }

            URL url = OWSUtils.getHTTPPostOperationURL( capaMap.get( addresses[i] ), GetFeature.class );

            LOG.logDebug( "performing query: ", query );
            StringRequestEntity re = new StringRequestEntity( query, "text/xml", Charset.defaultCharset().toString() );
            PostMethod post = new PostMethod( url.toExternalForm() );
            post.setRequestEntity( re );
            InputStream is = null;
            try {
                HttpClient client = new HttpClient();
                client = WebUtils.enableProxyUsage( client, url );
                client.executeMethod( post );
                is = post.getResponseBodyAsStream();
            } catch ( Exception e ) {
                LOG.logInfo( url.toExternalForm() );
                LOG.logError( e.getMessage(), e );
                throw new OGCWebServiceException( "could not perform query against the WFS: " + namespace + ':'
                                                  + featureType );
            }
            try {
                GMLFeatureCollectionDocument xml = new GMLFeatureCollectionDocument();
                xml.load( is, addresses[i] );
                // put the result on a Map that will be forced to the client
                // which is responsible for what to do with it. Because the keys
                // of the Map are the WFS addresses the client is able to reconstruct
                // the source of the result parts
                docs.put( addresses[i], xml.parse() );
            } catch ( Exception e ) {
                LOG.logError( e.getMessage(), e );
                throw new OGCWebServiceException( "could not parse response from WFS: " + namespace + ':' + featureType
                                                  + " as XML" );
            }
        }
        return docs;
    }

    /**
     * performs a GetCapabilities request against the passed address and stores the result (if it is
     * a valid WFS capabilities document) in a static Map.
     *
     * @param addr
     * @throws OGCWebServiceException
     * @throws InvalidCapabilitiesException
     */
    private void loadWFSCapabilities( String addr )
                            throws OGCWebServiceException, InvalidCapabilitiesException {

        LOG.logDebug( "reading capabilities from: ", addr );
        WFSCapabilitiesDocument doc = new WFSCapabilitiesDocument();
        try {
            doc.load( new URL( OWSUtils.validateHTTPGetBaseURL( addr )
                               + "version=1.1.0&service=WFS&request=GetCapabilities" ) );
        } catch ( Exception e ) {
            LOG.logInfo( OWSUtils.validateHTTPGetBaseURL( addr ) + "version=1.1.0&service=WFS&request=GetCapabilities" );
            LOG.logError( e.getMessage(), e );
            throw new OGCWebServiceException( "could not read capabilities from WFS: " + addr );
        }
        WFSCapabilities capa = (WFSCapabilities) doc.parseCapabilities();
        capaMap.put( addr, capa );
    }

    /**
     * creates a WFS GetFeature query from a named template and a set of KVP-encoded properties
     *
     * @param queryTemplate
     * @param filterProps
     * @return the query
     * @throws PortalException
     */
    private String createQueryFromTemplate( String queryTemplate, RPCParameter[] filterProps )
                            throws PortalException {

        queryTemplate = getInitParam( queryTemplate );
        if ( !( new File( queryTemplate ).isAbsolute() ) ) {
            queryTemplate = sc.getRealPath( queryTemplate );
        }
        StringBuffer template = new StringBuffer( 10000 );
        try {
            BufferedReader br = new BufferedReader( new FileReader( queryTemplate ) );
            String line = null;
            while ( ( line = br.readLine() ) != null ) {
                template.append( line );
            }
            br.close();
        } catch ( IOException e ) {
            LOG.logError( e.getMessage(), e );
            throw new PortalException( "could not read query template: " + parameter.get( "TEMPLATE" ) );
        }
        String query = template.toString();
        if ( filterProps != null ) {
            for ( int i = 0; i < filterProps.length; i++ ) {
                RPCStruct struct = (RPCStruct) filterProps[i].getValue();
                String name = RPCUtils.getRpcPropertyAsString( struct, "propertyName" );
                String value = RPCUtils.getRpcPropertyAsString( struct, "value" );
                value = StringTools.replace( value, "XXX", "%", true );
                query = StringTools.replace( query, '$' + name, value, true );
            }
        }
        return query;
    }

    /**
     * creates a WFS GetFeature query from a OGC filter expression send from a client
     *
     * @return the query
     * @throws PortalException
     */
    private String createQueryFromFilter( String[] featureTypes, String[] xmlns, String filter ) {
        StringBuffer query = new StringBuffer( 20000 );
        String format = "text/xml; subtype=gml/3.1.1";
        int maxFeatures = -1;
        String resultType = "results";
        if ( parameter.get( "OUTPUTFORMAT" ) != null ) {
            format = parameter.get( "OUTPUTFORMAT" );
        }
        if ( parameter.get( "MAXFEATURE" ) != null ) {
            maxFeatures = Integer.parseInt( parameter.get( "MAXFEATURE" ) );
        }
        if ( parameter.get( "RESULTTYPE" ) != null ) {
            resultType = parameter.get( "RESULTTYPE" );
        }
        query.append( "<wfs:GetFeature outputFormat='" ).append( format );
        query.append( "' maxFeatures='" ).append( maxFeatures ).append( "' " );
        query.append( " resultType='" ).append( resultType ).append( "' " );
        for ( int i = 0; i < xmlns.length; i++ ) {
            String[] tmp = StringTools.toArray( xmlns[i], "=", false );
            query.append( "xmlns:" ).append( tmp[0] ).append( "='" );
            query.append( tmp[1] ).append( "' " );
        }
        query.append( "xmlns:wfs='http://www.opengis.net/wfs' " );
        query.append( "xmlns:ogc='http://www.opengis.net/ogc' " );
        query.append( "xmlns:gml='http://www.opengis.net/gml' " );
        query.append( ">" );

        query.append( "<wfs:Query " );
        for ( int i = 0; i < featureTypes.length; i++ ) {
            query.append( "typeName='" ).append( featureTypes[i] );
            if ( i < featureTypes.length - 1 ) {
                query.append( "," );
            }
        }
        query.append( "'>" );
        query.append( filter );
        query.append( "</wfs:Query></wfs:GetFeature>" );

        return query.toString();
    }

    /**
     * creates an OGC FE filter from a set of KVP-encode properties and logical opertaions
     *
     * @return the filter
     */
    private String createFilterFromProperties() {
        String tmp = parameter.get( "FILTERPROPERTIES" );
        if ( tmp != null ) {
            String[] properties = StringTools.extractStrings( tmp, "{", "}" );
            String logOp = parameter.get( "LOGICALOPERATOR" );
            StringBuffer filter = new StringBuffer( 10000 );
            filter.append( "<ogc:Filter>" );
            if ( properties.length > 1 ) {
                filter.append( "<ogc:" ).append( logOp ).append( '>' );
            }
            for ( int i = 0; i < properties.length; i++ ) {
                String[] prop = StringTools.extractStrings( tmp, "[", "]" );
                if ( "!=".equals( prop[1] ) || "NOT LIKE".equals( prop[1] ) ) {
                    filter.append( "<ogc:Not>" );
                }
                if ( "=".equals( prop[1] ) || "!=".equals( prop[1] ) ) {
                    filter.append( "<ogc:PropertyIsEqualTo>" );
                    filter.append( "<ogc:PropertyName>" ).append( prop[0] ).append( "</ogc:PropertyName>" );
                    filter.append( "<ogc:Literal>" ).append( prop[2] ).append( "</ogc:Literal>" );
                    filter.append( "</ogc:PropertyIsEqualTo>" );
                } else if ( ">=".equals( prop[1] ) ) {
                    filter.append( "<ogc:PropertyIsGreaterThanOrEqualTo>" );
                    filter.append( "<ogc:PropertyName>" ).append( prop[0] ).append( "</ogc:PropertyName>" );
                    filter.append( "<ogc:Literal>" ).append( prop[2] ).append( "</ogc:Literal>" );
                    filter.append( "</ogc:PropertyIsGreaterThanOrEqualTo>" );
                } else if ( ">".equals( prop[1] ) ) {
                    filter.append( "<ogc:PropertyIsGreaterThan>" );
                    filter.append( "<ogc:PropertyName>" ).append( prop[0] ).append( "</ogc:PropertyName>" );
                    filter.append( "<ogc:Literal>" ).append( prop[2] ).append( "</ogc:Literal>" );
                    filter.append( "</ogc:PropertyIsGreaterThan>" );
                } else if ( "<=".equals( prop[1] ) ) {
                    filter.append( "<ogc:PropertyIsLessThanOrEqualTo>" );
                    filter.append( "<ogc:PropertyName>" ).append( prop[0] ).append( "</ogc:PropertyName>" );
                    filter.append( "<ogc:Literal>" ).append( prop[2] ).append( "</ogc:Literal>" );
                    filter.append( "</ogc:PropertyIsLessThanOrEqualTo>" );
                } else if ( "<".equals( prop[1] ) ) {
                    filter.append( "<ogc:PropertyIsLessThan>" );
                    filter.append( "<ogc:PropertyName>" ).append( prop[0] ).append( "</ogc:PropertyName>" );
                    filter.append( "<ogc:Literal>" ).append( prop[2] ).append( "</ogc:Literal>" );
                    filter.append( "</ogc:PropertyIsLessThan>" );
                } else if ( "LIKE".equals( prop[1] ) || "NOT LIKE".equals( prop[1] ) ) {
                    filter.append( "<ogc:PropertyIsLike wildCard='%' singleChar='#' escape='!'>" );
                    filter.append( "<ogc:PropertyName>" ).append( prop[0] ).append( "</ogc:PropertyName>" );
                    filter.append( "<ogc:Literal>" ).append( prop[2] ).append( "</ogc:Literal>" );
                    filter.append( "</ogc:PropertyIsLike>" );
                }
                if ( "!=".equals( prop[1] ) || "NOT LIKE".equals( prop[1] ) ) {
                    filter.append( "</ogc:Not>" );
                }
            }
            if ( properties.length > 1 ) {
                filter.append( "</ogc:" ).append( logOp ).append( '>' );
            }
            filter.append( "</ogc:Filter>" );
            return filter.toString();
        }
        return "";
    }

}
