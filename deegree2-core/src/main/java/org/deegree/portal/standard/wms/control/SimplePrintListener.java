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

package org.deegree.portal.standard.wms.control;

import java.awt.Graphics;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.deegree.datatypes.QualifiedName;
import org.deegree.enterprise.control.RPCMember;
import org.deegree.enterprise.control.RPCMethodCall;
import org.deegree.enterprise.control.RPCParameter;
import org.deegree.enterprise.control.RPCStruct;
import org.deegree.enterprise.control.RPCWebEvent;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.ImageUtils;
import org.deegree.framework.util.StringTools;
import org.deegree.i18n.Messages;
import org.deegree.model.crs.CoordinateSystem;
import org.deegree.model.spatialschema.Envelope;
import org.deegree.model.spatialschema.GeometryFactory;
import org.deegree.model.spatialschema.Point;
import org.deegree.ogcbase.ImageURL;
import org.deegree.ogcwebservices.OWSUtils;
import org.deegree.ogcwebservices.getcapabilities.OGCCapabilities;
import org.deegree.ogcwebservices.wms.capabilities.LegendURL;
import org.deegree.ogcwebservices.wms.capabilities.WMSCapabilities;
import org.deegree.ogcwebservices.wms.capabilities.WMSCapabilitiesDocument;
import org.deegree.ogcwebservices.wms.capabilities.WMSCapabilitiesDocumentFactory;
import org.deegree.owscommon_new.HTTP;
import org.deegree.owscommon_new.Operation;
import org.deegree.portal.Constants;
import org.deegree.portal.PortalException;
import org.deegree.portal.common.control.AbstractSimplePrintListener;
import org.deegree.portal.context.ContextException;
import org.deegree.portal.context.Format;
import org.deegree.portal.context.FormatList;
import org.deegree.portal.context.General;
import org.deegree.portal.context.Layer;
import org.deegree.portal.context.LayerList;
import org.deegree.portal.context.Server;
import org.deegree.portal.context.Style;
import org.deegree.portal.context.StyleList;
import org.deegree.portal.context.ViewContext;

/**
 * This class prints the View context. It inherits from AbstractSimplePrintListner and implement the abstract method
 * getViewContext
 *
 * TODO The methods changeBBox(), changeLayerList(), extractBBox() are already implemented in AbstractContextListner.
 * The question now is wether to inherit from AbstractContextListner instead of AbstractListner?
 *
 * @author <a href="mailto:mays@lat-lon.de">Judit Mays</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class SimplePrintListener extends AbstractSimplePrintListener {

    private static final ILogger LOG = LoggerFactory.getLogger( SimplePrintListener.class );

    private String missingImg = null;

    @Override
    protected ViewContext getViewContext( RPCWebEvent rpc ) {

        init();
        RPCMethodCall mc = rpc.getRPCMethodCall();
        RPCParameter[] params = mc.getParameters();
        RPCStruct struct = (RPCStruct) params[2].getValue();

        HttpSession session = ( (HttpServletRequest) getRequest() ).getSession();
        ViewContext vc = (ViewContext) session.getAttribute( Constants.CURRENTMAPCONTEXT );
        // change values: BBOX and Layer List
        Envelope bbox = extractBBox( (RPCStruct) struct.getMember( "boundingBox" ).getValue(), null );
        RPCMember[] layerList = ( (RPCStruct) struct.getMember( "layerList" ).getValue() ).getMembers();
        try {
            changeBBox( vc, bbox );
            changeLayerList( vc, layerList );
        } catch ( PortalException e ) {
            LOG.logError( "An Error occured while trying to get the Viewcontext" );
            return null;
            // TODO This method implements its abstract method in AbstractSimplePrintListner
            // Preferably if the abstract header could be changed to throw a adequate excpetion
            // since this header is already implemented by another class
        }
        return vc;
    }

    private void init() {

        String tmp = getInitParameter( "MISSING_IMAGE" );
        if ( tmp != null ) {
            missingImg = StringTools.concat( 200, getHomePath(), tmp );
            LOG.logDebug( "MissingLegend PATH = ", missingImg );
        }
    }

    /**
     * Convenience method to extract the boundig box from an rpc fragment.
     *
     * @param bboxStruct
     *            the <code>RPCStruct</code> containing the bounding box. For example,
     *            <code>&lt;member&gt;&lt;name&gt;boundingBox&lt;/name&gt;etc...</code>.
     * @param crs
     *            a coordinate system value, may be null.
     * @return an envelope with the boundaries defined in the rpc structure
     */
    protected Envelope extractBBox( RPCStruct bboxStruct, CoordinateSystem crs ) {

        Double minx = (Double) bboxStruct.getMember( Constants.RPC_BBOXMINX ).getValue();
        Double miny = (Double) bboxStruct.getMember( Constants.RPC_BBOXMINY ).getValue();
        Double maxx = (Double) bboxStruct.getMember( Constants.RPC_BBOXMAXX ).getValue();
        Double maxy = (Double) bboxStruct.getMember( Constants.RPC_BBOXMAXY ).getValue();

        Envelope bbox = GeometryFactory.createEnvelope( minx.doubleValue(), miny.doubleValue(), maxx.doubleValue(),
                                                        maxy.doubleValue(), crs );
        return bbox;
    }

    /**
     * changes the layer list of the ViewContext vc according to the information contained in the rpcLayerList
     *
     * @param vc
     *            The original ViewContext where the changes will be applied to
     * @param rpcLayerList
     *            the current layerlist
     * @throws PortalException
     */
    protected void changeLayerList( ViewContext vc, RPCMember[] rpcLayerList )
                            throws PortalException {
        LayerList layerList = vc.getLayerList();
        ArrayList<Layer> nLayers = new ArrayList<Layer>( rpcLayerList.length );

        // this is needed to keep layer order
        // order is correct in rpc call JavaScript, but get lost in translation...
        LOG.logDebug( "Layerlist length: " + rpcLayerList.length );
        for ( int i = 0; i < rpcLayerList.length; i++ ) {
            String[] v = StringTools.toArray( (String) rpcLayerList[i].getValue(), "|", false );
            String layerName = rpcLayerList[i].getName();

            String title = layerName;
            if ( v.length > 5 ) {
                // See if there is a title field and use it if exists
                title = v[5];
            }
            boolean isQueryable = false;
            if ( v.length > 6 ) {
                // see if there's a isQuerzable field and use it if exists
                isQueryable = v[6].equalsIgnoreCase( "true" );
            }

            boolean isVisible = Boolean.valueOf( v[0] ).booleanValue();
            LOG.logDebug( "Service url: " + v[4] );
            // server address must be set!! otherwise an outdated layer might be taken.
            Layer l = layerList.getLayer( layerName, v[4] );
            if ( l != null ) {
                // needed to reconstruct new layer order
                // otherwise layer order is still from original context
                LOG.logDebug( StringTools.concat( 100, "Layer ", layerName, " is defined in WMC" ) );
                l.setHidden( !isVisible );
            } else {
                LOG.logDebug( StringTools.concat( 100, "Layer ", layerName, " is undefined by the WMC" ) );
                if ( layerList.getLayers().length == 0 ) {
                    // FIXME is this Exception Correct?
                    throw new PortalException( Messages.getMessage( "IGEO_STD_CNTXT_ERROR_EMPTY_LAYERLIST" ) );
                }

                Layer p = layerList.getLayers()[0];
                // a new layer must be created because it is not prsent in the current context.
                // This is the case if the client has loaded an additional WMS
                String[] tmp = StringTools.toArray( v[2], " ", false );
                try {
                    v[4] = OWSUtils.validateHTTPGetBaseURL( v[4] );
                    WMSCapabilitiesDocument doc = WMSCapabilitiesDocumentFactory.getWMSCapabilitiesDocument( new URL(
                                                                                                                      v[4]
                                                                                                                                              + "request=GetCapabilities&service=WMS" ) );
                    LOG.logDebug( "Service base url: " + v[4] );
                    OGCCapabilities capa = doc.parseCapabilities();

                    URL url = getLegendURL( capa, layerName, null );
                    LOG.logDebug( "Final obtained legend url: " + url.toExternalForm() );
                    ImageURL tmpImg = p.getStyleList().getCurrentStyle().getLegendURL();
                    LegendURL legendUrl = new LegendURL( tmpImg.getWidth(), tmpImg.getHeight(), tmpImg.getFormat(), url );
                    Style style = new Style( "default", "", null, legendUrl, true );
                    StyleList styleList = new StyleList( new org.deegree.portal.context.Style[] { style } );
                    // end of changes
                    Server server = new Server( v[3], tmp[1], tmp[0], new URL( v[4] ), capa );

                    Format[] formats = new Format[] { new Format( "image/png", true ) };
                    FormatList fl = new FormatList( formats );
                    l = new Layer( server, layerName, title, "", p.getSrs(), null, null, fl, styleList, isQueryable,
                                   !isVisible, null );
                } catch ( Exception e ) {
                    throw new PortalException( StringTools.stackTraceToString( e ) );
                }
            }
            nLayers.add( l );
        }
        try {
            nLayers.trimToSize();
            Layer[] ls = new Layer[nLayers.size()];
            ls = nLayers.toArray( ls );
            vc.setLayerList( new LayerList( ls ) );
        } catch ( ContextException e ) {
            throw new PortalException( Messages.getMessage( "IGEO_STD_CNTXT_ERROR_SET_LAYERLIST",
                                                            StringTools.stackTraceToString( e.getStackTrace() ) ) );
        }
    }

    /**
     * This method is called, when no legend image could be retreived from the information given in the WebMapContext
     * document. The layer was probably added during runtime dynamically.
     *
     * It is attempted to get the legend url from style info in the WMS Capabilities, first with the passed style info,
     * second for the default style, third for the only available style. (If the WMS capabilities holds more than one
     * style, but no style was passed with the layer info, then one cannot know "the right style". Therefore no style is
     * taken from WMS capabilities, in this case.)
     *
     * Then, it is attempted to get the legend image from a GetLegendGraphics request, if the WMS capabilities state,
     * that this request is supported by the WMS.
     *
     * If all this fails, the image is taken from init param "missingImage".
     *
     * @param capa
     * @param layerName
     * @param styleName
     * @return URL of the Legend
     * @throws Exception
     */
    private URL getLegendURL( OGCCapabilities capa, String layerName, String styleName )
                            throws Exception {

        // changes added to enable fetching the legend graphic through multiple methods
        WMSCapabilities wmsCapa = (WMSCapabilities) capa;
        org.deegree.ogcwebservices.wms.capabilities.Layer ogcLayer = wmsCapa.getLayer( layerName );
        URL url = null;
        if ( styleName == null ) {
            styleName = "default";
        }
        org.deegree.ogcwebservices.wms.capabilities.Style ogcStyle = ogcLayer.getStyleResource( styleName );// stylename

        // 1. get style for layer
        // first try with "default" and then with "" (empty string) as style name
        LOG.logDebug( "Trying first method" );
        if ( ogcStyle == null ) {
            LOG.logDebug( "Layer has no valid default style definition. Obtaining default style from OGCLayer" );
            ogcStyle = ogcLayer.getStyleResource( "" );
        }

        // try to get another style for layer (only, if there is just one style definition)
        if ( ogcStyle == null ) {
            LOG.logDebug( "Layer has no valid default style definition." );
            org.deegree.ogcwebservices.wms.capabilities.Style[] styles = ogcLayer.getStyles();
            if ( styles.length == 1 ) {
                // using the only available style definition as default style definition
                LOG.logDebug( "Layer has only one style definition. Assuming this as default style." );
                ogcStyle = styles[0];
            } else {
                // more than one style definition available, but non is the default style.
                // Therefore no style definition can be chosen. Nothing happens here.
            }
        }

        // 2. try to get legend url from style definition
        if ( ogcStyle != null ) {
            LOG.logDebug( "Trying second method" );
            LegendURL[] legendUrls = ogcStyle.getLegendURL();
            if ( legendUrls != null && legendUrls.length > 0 ) {
                // First field of the array contains a url to the legend
                url = legendUrls[0].getOnlineResource();
                LOG.logDebug( "Legend url from the ogcStyle is: " + url.toExternalForm() );
            }
        }

        // 3. try to get legend url from getLegendGraphic request
        if ( ogcStyle == null || url == null ) {
            LOG.logDebug( "Trying third method" );
            // either layer does not have a style info at all
            // or layer has a style info, but this style does not contain a legend url

            QualifiedName name = new QualifiedName( "GetLegendGraphic" );
            Operation op = wmsCapa.getOperationMetadata().getOperation( name );
            if ( op != null ) {
                HTTP http = (HTTP) op.getDCP().get( 0 );
                url = http.getGetOnlineResources().get( 0 );
            } else {
                LOG.logDebug( "GetLegendGrpahic not served by the service." );
            }
            LOG.logDebug( "LegendURLs can not be extracted from ogcStyle." );
        }
        if ( url != null ) {
            try {
                // If no exception is thrown here, then everythis is ok, otherwise we load the missing legend image
                ImageUtils.loadImage( url );
            } catch ( Exception e ) {
                // The path is invalid. Loading the missing image from controller.xml
                LOG.logDebug( StringTools.concat( 100, "The url : " + url,
                                                  " could not be loaded. Loading missing image" ) );
                url = createMissingLegend( ogcLayer.getTitle() );
            }
        }
        return url;
    }

    /**
     * Tries to create the missing image from the given path in the controller.xml If this image is invalid we try to
     * create our own empty legend with/without layer title, depending on the configurations
     *
     * @param layerTitle
     * @return Missing legend URL
     */
    private URL createMissingLegend( String layerTitle ) {

        URL url = null;
        try {
            // using default length. width and format
            BufferedImage img = createEmptyLegend( layerTitle );
            // create the folder print in webapp if we don't have it
            File printDir = new File( getHomePath() + "print" );
            if ( !printDir.exists() ) {
                printDir.mkdir();
            }
            File imgFile = findavailableName( printDir, "missingLegend", ".png" );
            ImageUtils.saveImage( img, imgFile, 1.0f );
            url = new URL( "file:///" + imgFile.getAbsolutePath() );

        } catch ( Exception e ) {
            LOG.logError( e.getLocalizedMessage() );
        }
        return url;
    }

    /**
     * Creates an empty legend in case the path to the MISSING_IMAGE parameter in the controller is invalid
     *
     * @param layerTitle
     * @return Empty legend BufferedImage
     */
    private BufferedImage createEmptyLegend( String layerTitle ) {

        BufferedImage legend = new BufferedImage( 1, 1, BufferedImage.TYPE_INT_ARGB );
        if ( missingImg != null ) {
            try {
                legend = ImageUtils.loadImage( missingImg );
                // if MISSING_IMAGE path is valid and useLayerTitle is false, just return the missing image
                return legend;
            } catch ( Exception e ) {
                // its ok, we already have a valid legend BufferedImage
            }
        }

        // if useLayerTitle == true,add the title to the image
        LOG.logDebug( "Adding title to the missing image" );
        BufferedImage missingLegend = new BufferedImage( 80, 15, BufferedImage.TYPE_INT_RGB );
        Graphics g = missingLegend.getGraphics();
        Rectangle2D rect = g.getFontMetrics().getStringBounds( layerTitle, g );

        missingLegend = new BufferedImage( rect.getBounds().width + 80, legend.getHeight() + 15,
                                           BufferedImage.TYPE_INT_ARGB );
        g.dispose();
        g = missingLegend.getGraphics();
        g.drawImage( legend, 0, 0, null );
        g.dispose();
        return missingLegend;
    }

    /**
     * Finds the first non used name in the folder name that starts with the given prefix. Ex. if prefix is print, then
     * it looks if print1 does not exist to use, if not then print2, if not then print3 etc..
     *
     * @param folderName
     * @param prefix
     * @param suffix
     *            (extension)of the generated path, ex .png or .jpg
     * @return Available file name
     */
    private File findavailableName( File folderName, String prefix, String suffix ) {
        String filename = folderName.getAbsolutePath();
        if ( !filename.endsWith( "/" ) && !filename.endsWith( "\\" ) ) {
            filename += "/";
        }
        filename += prefix;
        int counter = 1;
        while ( true ) {
            File file = new File( StringTools.concat( 100, filename, counter++, suffix ) );
            if ( !file.exists() ) {
                return file;
            }
        }
    }

    /**
     * changes the bounding box of a given view context
     *
     * @param vc
     *            the view context to be changed
     * @param bbox
     *            the new bounding box
     * @throws PortalException
     */
    public static final void changeBBox( ViewContext vc, Envelope bbox )
                            throws PortalException {
        General gen = vc.getGeneral();

        CoordinateSystem cs = gen.getBoundingBox()[0].getCoordinateSystem();
        Point[] p = new Point[] { GeometryFactory.createPoint( bbox.getMin(), cs ),
                                 GeometryFactory.createPoint( bbox.getMax(), cs ) };
        try {
            gen.setBoundingBox( p );
        } catch ( ContextException e ) {
            LOG.logError( e.getMessage(), e );
            throw new PortalException( Messages.getMessage( "IGEO_STD_CNTXT_ERROR_SET_BBOX" ) );
        }
    }

}
