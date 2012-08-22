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
package org.deegree.ogcwebservices.wmps;

import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.CharsetUtils;
import org.deegree.framework.util.IDGenerator;
import org.deegree.framework.util.NetWorker;
import org.deegree.framework.util.StringTools;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.graphics.MapFactory;
import org.deegree.graphics.sld.AbstractStyle;
import org.deegree.graphics.sld.FeatureTypeConstraint;
import org.deegree.graphics.sld.LayerFeatureConstraints;
import org.deegree.graphics.sld.RemoteOWS;
import org.deegree.graphics.sld.UserLayer;
import org.deegree.graphics.sld.UserStyle;
import org.deegree.model.feature.FeatureCollection;
import org.deegree.model.feature.GMLFeatureCollectionDocument;
import org.deegree.model.filterencoding.Filter;
import org.deegree.ogcwebservices.OGCWebServiceException;
import org.deegree.ogcwebservices.wfs.WFService;
import org.deegree.ogcwebservices.wfs.operation.FeatureResult;
import org.deegree.ogcwebservices.wfs.operation.GetFeature;
import org.deegree.ogcwebservices.wms.capabilities.Layer;
import org.deegree.ogcwebservices.wms.configuration.AbstractDataSource;
import org.w3c.dom.Element;

/**
 * This a copy of the WMS package.
 *
 * class for accessing the data of one user layer and creating <tt>DisplayElement</tt>s and a
 * <tt>Thrme</tt> from it. The class extends <tt>Thread</tt> and implements the run method, so
 * that a parallel data accessing from several layers is possible.
 *
 * @version $Revision$
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version 1.0. $Revision$, $Date$
 *
 * @since 2.0
 */
class GetMapServiceInvokerForUL extends Thread {

    private static final ILogger LOG = LoggerFactory.getLogger( GetMapServiceInvokerForUL.class );

    private final DefaultGetMapHandler handler;

    private UserLayer layer = null;

    private UserStyle[] styles = null;

    private int index = 0;

    GetMapServiceInvokerForUL( DefaultGetMapHandler handler, UserLayer layer, int index ) {
        this.layer = layer;
        this.handler = handler;
        AbstractStyle[] tmp = layer.getStyles();
        this.styles = new UserStyle[tmp.length];
        for ( int i = 0; i < tmp.length; i++ ) {
            this.styles[i] = (UserStyle) tmp[i];
        }

        this.index = index;
    }

    /**
     * overrides/implements the run-method of <tt>Thread</tt>
     */
    @Override
    public void run() {

        try {
            if ( this.layer.getRemoteOWS() == null || this.layer.getRemoteOWS().getService().equals( RemoteOWS.WFS ) ) {
                handleWFS();
            } else if ( this.layer.getRemoteOWS().getService().equals( RemoteOWS.WCS ) ) {
                handleWCS();
            }
        } catch ( Exception e ) {
            LOG.logError( "", e );
            OGCWebServiceException exce = new OGCWebServiceException(
                                                                      "ServiceInvokerForUL: " + this.layer.getName(),
                                                                      "Couldn't perform query!"
                                                                                              + StringTools.stackTraceToString( e ) );
            this.handler.putTheme( this.index, exce );
            this.handler.increaseCounter();

            return;
        }

    }

    /**
     * handles requests against a WFS
     *
     * @throws Exception
     */
    private void handleWFS()
                            throws Exception {

        FeatureCollection fc = null;
        String request = createGetFeatureRequest();

        if ( this.layer.getRemoteOWS() != null ) {
            // handle request against a remote WFS
            RemoteOWS remoteOWS = this.layer.getRemoteOWS();
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
            WFService wfs = getResponsibleService();
            FeatureResult fr = (FeatureResult) wfs.doService( gfr );
            fc = (FeatureCollection) fr.getResponse();
        }
        org.deegree.graphics.Layer fl = MapFactory.createFeatureLayer( this.layer.getName(), this.handler.reqCRS, fc );
        this.handler.putTheme( this.index, MapFactory.createTheme( this.layer.getName(), fl, this.styles ) );
        this.handler.increaseCounter();

    }

    /**
     * Returns the responsible service.
     *
     * @return Exception
     * @throws OGCWebServiceException
     */
    private WFService getResponsibleService()
                            throws OGCWebServiceException {

        LayerFeatureConstraints lfc = this.layer.getLayerFeatureConstraints();
        FeatureTypeConstraint[] ftc = lfc.getFeatureTypeConstraint();
        Layer root = this.handler.getConfiguration().getLayer();
        WFService wfs = findService( root, ftc[0].getFeatureTypeName().getPrefixedName() );
        if ( wfs == null ) {
            throw new OGCWebServiceException( this.getName(), "feature type: " + ftc[0].getFeatureTypeName()
                                                              + " is not serverd by this WMS/WFS" );
        }
        return wfs;

    }

    /**
     * searches/findes the WFService that is resposible for handling the feature types of the
     * current request. If no WFService instance can be found <code>null</code> will be returned
     * to indicated that the current feature type is not served by the internal WFS of a WMS
     *
     * @param currentlayer
     * @param featureType
     * @return WFService
     * @throws OGCWebServiceException
     */
    private WFService findService( Layer currentlayer, String featureType )
                            throws OGCWebServiceException {
        Layer[] layers = currentlayer.getLayer();
        for ( int i = 0; i < layers.length; i++ ) {
            AbstractDataSource[] ad = layers[i].getDataSource();
            if ( ad != null ) {
                for ( int j = 0; j < ad.length; j++ ) {
                    if ( ad[j].getName().getPrefixedName().equals( featureType ) ) {
                        return (WFService) ad[j].getOGCWebService();
                    }
                }
            }
            // recursion
            WFService wfs = findService( layers[i], featureType );
            if ( wfs != null ) {
                return wfs;
            }
        }

        return null;
    }

    /**
     * creates a GetFeature request related to the UserLayer encapsulated in this object
     *
     * @return String
     * @throws Exception
     */
    private String createGetFeatureRequest()
                            throws Exception {

        LayerFeatureConstraints lfc = this.layer.getLayerFeatureConstraints();
        FeatureTypeConstraint[] ftc = lfc.getFeatureTypeConstraint();

        // no filter condition has been defined
        StringBuffer sb = new StringBuffer( 5000 );
        sb.append( "<?xml version='1.0' encoding='UTF-8'?>" );
        sb.append( "<GetFeature xmlns='http://www.opengis.net/wfs' " );
        sb.append( "xmlns:ogc='http://www.opengis.net/ogc' " );
        sb.append( "xmlns:gml='http://www.opengis.net/gml' " );
        sb.append( "xmlns:app=\"http://www.deegree.org/app\" " );
        sb.append( "service='WFS' version='1.1.0' " );
        sb.append( "outputFormat='text/xml; subtype=gml/3.1.1'>" );
        for ( int i = 0; i < ftc.length; i++ ) {
            sb.append( "<Query typeName='" + ftc[i].getFeatureTypeName() + "'>" );
            Filter filter = ftc[i].getFilter();
            if ( filter != null ) {
                sb.append( filter.to110XML() );
            }
            sb.append( "</Query>" );
        }
        sb.append( "</GetFeature>" );

        return sb.toString();
    }

    /**
     * handles requests against a WCS
     *
     * @throws Exception
     */
    private void handleWCS()
                            throws Exception {
        throw new UnsupportedOperationException( "The WCS support has not been implemented as of now. "
                                                 + "Please bear with us." );
        /*
         * TODO RemoteOWS remoteOWS = layer.getRemoteOWS(); URL url = remoteOWS.getOnlineResource();
         *
         * NetWorker nw = new NetWorker( url ); MemoryCacheSeekableStream mcss = new
         * MemoryCacheSeekableStream( nw.getInputStream() );
         *
         * RenderedOp rop = JAI.create("stream", mcss);
         *
         * GC_GridCoverage gc = new ImageGridCoverage(rop.getAsBufferedImage(),
         * request.getBoundingBox(), reqCRS, false); mcss.close();
         *
         * org.deegree.graphics.Layer rl = MapFactory.createRasterLayer(layer.getName(), gc);
         *
         * putTheme(index, MapFactory.createTheme(layer.getName(), rl)); mcss.close();
         * increaseCounter();
         */

    }
}
