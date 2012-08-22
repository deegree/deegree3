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
package org.deegree.ogcwebservices.wms;

import static java.lang.Boolean.FALSE;

import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.deegree.datatypes.QualifiedName;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.CharsetUtils;
import org.deegree.framework.util.IDGenerator;
import org.deegree.framework.util.NetWorker;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.graphics.MapFactory;
import org.deegree.graphics.sld.AbstractStyle;
import org.deegree.graphics.sld.FeatureTypeConstraint;
import org.deegree.graphics.sld.LayerFeatureConstraints;
import org.deegree.graphics.sld.RemoteOWS;
import org.deegree.graphics.sld.StyleUtils;
import org.deegree.graphics.sld.UserLayer;
import org.deegree.graphics.sld.UserStyle;
import org.deegree.model.feature.FeatureCollection;
import org.deegree.model.feature.GMLFeatureCollectionDocument;
import org.deegree.model.filterencoding.Filter;
import org.deegree.ogcbase.PropertyPath;
import org.deegree.ogcwebservices.OGCWebService;
import org.deegree.ogcwebservices.OGCWebServiceException;
import org.deegree.ogcwebservices.wfs.operation.FeatureResult;
import org.deegree.ogcwebservices.wfs.operation.GetFeature;
import org.w3c.dom.Element;

/**
 * class for accessing the data of one user layer and creating <tt>DisplayElement</tt>s and a <tt>Thrme</tt> from it.
 * The class extends <tt>Thread</tt> and implements the run method, so that a parallel data accessing from several
 * layers is possible.
 * 
 * @version $Revision$
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version 1.0. $Revision$, $Date$
 * 
 * @since 2.0
 */
class GetMapServiceInvokerForUL extends GetMapServiceInvoker implements Callable<Object> {

    private static final ILogger LOG = LoggerFactory.getLogger( GetMapServiceInvokerForUL.class );

    private UserLayer layer = null;

    private UserStyle[] styles = null;

    /**
     * 
     * @param handler
     * @param layer
     * @param scale
     *            current mapscale denominator
     */
    GetMapServiceInvokerForUL( DefaultGetMapHandler handler, UserLayer layer, double scale ) {
        super( handler, scale );

        this.layer = layer;
        AbstractStyle[] tmp = layer.getStyles();
        styles = new UserStyle[tmp.length];
        for ( int i = 0; i < tmp.length; i++ ) {
            styles[i] = (UserStyle) tmp[i];
        }

    }

    public Object call() {

        try {
            if ( layer.getRemoteOWS() == null || layer.getRemoteOWS().getService().equals( RemoteOWS.WFS ) ) {
                return handleWFS();
            } else if ( layer.getRemoteOWS().getService().equals( RemoteOWS.WCS ) ) {
                return handleWCS();
            }

        } catch ( OGCWebServiceException e ) {
            return e;
        } catch ( Exception e ) {
            LOG.logError( e.getMessage(), e );
            OGCWebServiceException exce = new OGCWebServiceException( "ServiceInvokerForUL: " + layer.getName(),
                                                                      "Couldn't perform query!" );
            return exce;
        }

        return null;
    }

    /**
     * handles requests against a WFS
     */
    private Object handleWFS()
                            throws Exception {

        FeatureCollection fc = null;
        String request = createGetFeatureRequest();
        LOG.logDebug( request );
        if ( layer.getRemoteOWS() != null ) {
            // handle request against a remote WFS
            RemoteOWS remoteOWS = layer.getRemoteOWS();
            URL url = remoteOWS.getOnlineResource();

            NetWorker nw = new NetWorker( CharsetUtils.getSystemCharset(), url, request );
            InputStreamReader isr = new InputStreamReader( nw.getInputStream(), CharsetUtils.getSystemCharset() );
            GMLFeatureCollectionDocument doc = new GMLFeatureCollectionDocument();
            doc.load( isr, url.toString() );
            Element root = doc.getRootElement();

            if ( root.getNodeName().indexOf( "Exception" ) > -1 ) {
                ByteArrayOutputStream bos = new ByteArrayOutputStream( 1000 );
                doc.write( bos );
                throw new Exception( new String( bos.toByteArray() ) );
            }
            fc = doc.parse();
        } else {
            // handle request agaist a local WFS; this is bit problematic
            // because deegree WMS is able to handle more than one
            // local WFS. At the moment the WFS will be used that will
            // returned by the WFServiceFactory as default
            XMLFragment xml = new XMLFragment( new StringReader( request ), XMLFragment.DEFAULT_URL );
            Element root = xml.getRootElement();
            // create OGCWebServiceEvent object
            IDGenerator idg = IDGenerator.getInstance();
            GetFeature gfr = GetFeature.create( "" + idg.generateUniqueID(), root );

            // returns the WFS responsible for handling current feature type
            OGCWebService wfs = getResponsibleService( layer );
            FeatureResult fr = (FeatureResult) wfs.doService( gfr );
            fc = (FeatureCollection) fr.getResponse();
        }
        org.deegree.graphics.Layer fl = MapFactory.createFeatureLayer( layer.getName(), this.handler.getRequestCRS(),
                                                                       fc );
        return MapFactory.createTheme( layer.getName(), fl, styles );
    }

    /**
     * creates a GetFeature request related to the UserLayer encapsulated in this object
     */
    private String createGetFeatureRequest()
                            throws Exception {

        LayerFeatureConstraints lfc = layer.getLayerFeatureConstraints();
        FeatureTypeConstraint[] ftc = lfc.getFeatureTypeConstraint();

        List<UserStyle> styleList = Arrays.asList( styles );
        List<PropertyPath> pp = StyleUtils.extractRequiredProperties( ftc[0].getFeatureTypeName(), styleList, scaleDen );
        LOG.logDebug( "required properties: ", pp );
        pp = findGeoProperties( layer, ftc, pp );
        Map<String, URI> namesp = extractNameSpaceDef( pp );
        for ( int i = 0; i < ftc.length; i++ ) {
            QualifiedName qn = ftc[i].getFeatureTypeName();
            namesp.put( qn.getPrefix(), qn.getNamespace() );
        }

        StringBuffer sb = new StringBuffer( 5000 );
        sb.append( "<?xml version='1.0' encoding='" + CharsetUtils.getSystemCharset() + "'?>" );
        sb.append( "<GetFeature xmlns='http://www.opengis.net/wfs' " );
        sb.append( "xmlns:ogc='http://www.opengis.net/ogc' " );
        sb.append( "xmlns:gml='http://www.opengis.net/gml' " );

        Iterator<String> iter = namesp.keySet().iterator();
        while ( iter.hasNext() ) {
            String pre = iter.next();
            URI nsp = namesp.get( pre );
            if ( !pre.equals( "xmlns" ) ) {
                sb.append( "xmlns:" ).append( pre ).append( "='" );
                sb.append( nsp.toASCIIString() ).append( "' " );
            }
        }

        sb.append( "service='WFS' version='1.1.0' " );
        sb.append( "outputFormat='text/xml; subtype=gml/3.1.1'>" );
        for ( int i = 0; i < ftc.length; i++ ) {
            QualifiedName qn = ftc[i].getFeatureTypeName();
            sb.append( "<Query typeName='" ).append( qn.getPrefixedName() ).append( "'>" );

            for ( int j = 0; j < pp.size(); j++ ) {
                if ( !pp.get( j ).getAsString().endsWith( "$SCALE" ) ) {
                    // $SCALE is a dynamicly created property of each feature
                    // and can not be requested
                    sb.append( "<PropertyName>" ).append( pp.get( j ).getAsString() );
                    sb.append( "</PropertyName>" );
                }
            }

            Filter filter = ftc[i].getFilter();
            if ( filter != null ) {
                sb.append( filter.to110XML() );
            }
            sb.append( "</Query>" );
        }
        sb.append( "</GetFeature>" );

        LOG.logDebug( sb.toString() );

        return sb.toString();
    }

    /**
     * handles requests against a WCS
     */
    private Object handleWCS()
                            throws Exception {
        return FALSE;
        /*
         * TODO RemoteOWS remoteOWS = layer.getRemoteOWS(); URL url = remoteOWS.getOnlineResource();
         * 
         * NetWorker nw = new NetWorker( url ); MemoryCacheSeekableStream mcss = new MemoryCacheSeekableStream(
         * nw.getInputStream() );
         * 
         * RenderedOp rop = JAI.create("stream", mcss);
         * 
         * GC_GridCoverage gc = new ImageGridCoverage(rop.getAsBufferedImage(), request.getBoundingBox(), reqCRS,
         * false); mcss.close();
         * 
         * org.deegree.graphics.Layer rl = MapFactory.createRasterLayer(layer.getName(), gc);
         * 
         * putTheme(index, MapFactory.createTheme(layer.getName(), rl)); mcss.close(); increaseCounter();
         */

    }

}
