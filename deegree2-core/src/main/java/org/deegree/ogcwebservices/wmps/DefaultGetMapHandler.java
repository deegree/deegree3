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

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.CharsetUtils;
import org.deegree.framework.util.ImageUtils;
import org.deegree.framework.util.MapUtils;
import org.deegree.framework.util.StringTools;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.graphics.MapFactory;
import org.deegree.graphics.MapView;
import org.deegree.graphics.Theme;
import org.deegree.graphics.optimizers.LabelOptimizer;
import org.deegree.graphics.sld.AbstractLayer;
import org.deegree.graphics.sld.AbstractStyle;
import org.deegree.graphics.sld.NamedLayer;
import org.deegree.graphics.sld.NamedStyle;
import org.deegree.graphics.sld.SLDFactory;
import org.deegree.graphics.sld.StyledLayerDescriptor;
import org.deegree.graphics.sld.UserLayer;
import org.deegree.graphics.sld.UserStyle;
import org.deegree.i18n.Messages;
import org.deegree.model.crs.CRSFactory;
import org.deegree.model.crs.CoordinateSystem;
import org.deegree.model.crs.GeoTransformer;
import org.deegree.model.spatialschema.Envelope;
import org.deegree.model.spatialschema.Geometry;
import org.deegree.model.spatialschema.GeometryFactory;
import org.deegree.ogcbase.InvalidSRSException;
import org.deegree.ogcwebservices.OGCWebServiceException;
import org.deegree.ogcwebservices.wmps.configuration.WMPSConfiguration;
import org.deegree.ogcwebservices.wmps.configuration.WMPSDeegreeParams;
import org.deegree.ogcwebservices.wmps.operation.PrintMap;
import org.deegree.ogcwebservices.wms.LayerNotDefinedException;
import org.deegree.ogcwebservices.wms.StyleNotDefinedException;
import org.deegree.ogcwebservices.wms.capabilities.Layer;
import org.deegree.ogcwebservices.wms.capabilities.ScaleHint;
import org.deegree.ogcwebservices.wms.configuration.AbstractDataSource;
import org.deegree.ogcwebservices.wms.operation.GetMap;

/**
 * This is a copy of the WMS package.
 * 
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class DefaultGetMapHandler implements GetMapHandler {

    private static final ILogger LOG = LoggerFactory.getLogger( DefaultGetMapHandler.class );

    protected GetMap request;

    private Object[] themes;

    protected double scale = 0;

    private int count = 0;

    protected CoordinateSystem reqCRS;

    private WMPSConfiguration configuration;

    private BufferedImage copyrightImg;

    private Graphics graph;

    /**
     * Creates a new GetMapHandler object.
     * 
     * @param configuration
     * @param request
     *            request to perform
     * @throws OGCWebServiceException
     */
    public DefaultGetMapHandler( WMPSConfiguration configuration, GetMap request ) throws OGCWebServiceException {
        this.request = request;
        this.configuration = configuration;

        try {
            // get copyright image if possible
            this.copyrightImg = ImageUtils.loadImage( configuration.getDeegreeParams().getCopyright() );
        } catch ( Exception e ) {
            // eat it
        }

        try {
            this.reqCRS = CRSFactory.create( this.request.getSrs() );
        } catch ( Exception e ) {
            throw new InvalidSRSException( "SRS: " + request.getSrs() + "is nor known by the deegree WMS" );
        }

    }

    /**
     * returns the configuration used by the handler
     * 
     * @return WMPSConfiguration
     */
    public WMPSConfiguration getConfiguration() {
        return this.configuration;
    }

    /**
     * increases the counter variable that holds the number of services that has sent a response. All data are available
     * if the counter value equals the number of requested layers.
     */
    protected synchronized void increaseCounter() {
        this.count++;
    }

    /**
     * performs a GetMap request and returns the result encapsulated within a <tt>GetMapResult</tt> object.
     * <p>
     * The method throws an WebServiceException that only shall be thrown if an fatal error occurs that makes it
     * impossible to return a result. If something went wrong performing the request (none fatal error) The exception
     * shall be encapsulated within the response object to be returned to the client as requested (GetMap-Request
     * EXCEPTION-Parameter).
     * 
     * @param printMap
     * @param g
     * @throws OGCWebServiceException
     */
    public void performGetMap( PrintMap printMap, Graphics g )
                            throws OGCWebServiceException {

        this.graph = g;

        try {
            CoordinateSystem crs = CRSFactory.create( this.request.getSrs() );
            double dpi = printMap.getDpi();
            if ( dpi < 0 ) {
                dpi = configuration.getDeegreeParams().getPrintMapParam().getTargetResolution();
            }
            double pixelSize = WMPSConfiguration.INCH2M / dpi;
            this.scale = MapUtils.calcScale( this.request.getWidth(), this.request.getHeight(),
                                             this.request.getBoundingBox(), crs, pixelSize );
            LOG.logInfo( "OGC WMPS scale denominator: " + this.scale );
        } catch ( Exception e ) {
            LOG.logDebug( "-", e );
            throw new OGCWebServiceException( "Couldn't calculate scale! " + e );
        }

        StyledLayerDescriptor sld = null;
        try {
            sld = toSLD( this.request.getLayers(), this.request.getStyledLayerDescriptor() );
        } catch ( XMLParsingException e1 ) {
            // should never happen
            e1.printStackTrace();
        }

        AbstractLayer[] layers = sld.getLayers();
        // get the number of themes assigned to the selected layers
        // notice that there maybe more themes as there are layers because
        // 1 .. n datasources can be assigned to one layer.
        int cntTh = countNumberOfThemes( layers, this.scale );
        this.themes = new Object[cntTh];
        // invokes the data supplyer for each layer in an independent thread
        int kk = 0;
        for ( int i = 0; i < layers.length; i++ ) {
            if ( layers[i] instanceof NamedLayer ) {
                String styleName = null;
                if ( i < this.request.getLayers().length ) {
                    styleName = this.request.getLayers()[i].getStyleName();
                }
                kk = invokeNamedLayer( layers[i], kk, styleName );
            } else {
                GetMapServiceInvokerForUL si = new GetMapServiceInvokerForUL( this, (UserLayer) layers[i], kk++ );

                si.start();
            }
        }
        waitForFinished();
        renderMap();

    }

    /**
     * Invoke the named layer
     * 
     * @param layer
     * @param kk
     * @param styleName
     * @return int
     * @throws OGCWebServiceException
     */
    private int invokeNamedLayer( AbstractLayer layer, int kk, String styleName )
                            throws OGCWebServiceException {

        Layer lay = this.configuration.getLayer( layer.getName() );

        if ( validate( lay, layer.getName() ) ) {
            UserStyle us = getStyles( (NamedLayer) layer, styleName );
            AbstractDataSource[] ds = lay.getDataSource();

            for ( int j = 0; j < ds.length; j++ ) {

                ScaleHint scaleHint = ds[j].getScaleHint();
                if ( this.scale >= scaleHint.getMin() && this.scale < scaleHint.getMax()
                     && isValidArea( ds[j].getValidArea() ) ) {
                    GetMapServiceInvokerForNL si = new GetMapServiceInvokerForNL( this, lay, ds[j], us, kk++ );
                    si.start();
                }
            }
        } else {
            // set theme to null if no data are available for the requested
            // area and/or scale
            this.themes[kk++] = null;
            increaseCounter();
        }
        return kk;
    }

    /**
     * returns the number of <code>DataSource</code>s involved in a GetMap request
     * 
     * @param layers
     * @param currentscale
     * @return int
     */
    private int countNumberOfThemes( AbstractLayer[] layers, double currentscale ) {
        int cnt = 0;
        for ( int i = 0; i < layers.length; i++ ) {
            if ( layers[i] instanceof NamedLayer ) {
                Layer lay = this.configuration.getLayer( layers[i].getName() );
                AbstractDataSource[] ds = lay.getDataSource();
                for ( int j = 0; j < ds.length; j++ ) {

                    ScaleHint scaleHint = ds[j].getScaleHint();
                    if ( currentscale >= scaleHint.getMin() && currentscale < scaleHint.getMax()
                         && isValidArea( ds[j].getValidArea() ) ) {

                        cnt++;
                    }
                }
            } else {
                cnt++;
            }
        }
        return cnt;
    }

    /**
     * returns true if the requested boundingbox intersects with the valid area of a datasource
     * 
     * @param validArea
     * @return boolean
     */
    private boolean isValidArea( Geometry validArea ) {

        if ( validArea != null ) {
            try {
                Envelope env = this.request.getBoundingBox();
                Geometry geom = GeometryFactory.createSurface( env, this.reqCRS );
                if ( !this.reqCRS.getIdentifier().equals( validArea.getCoordinateSystem().getIdentifier() ) ) {
                    // if requested CRS is not identical to the CRS of the valid area
                    // a transformation must be performed before intersection can
                    // be checked
                    GeoTransformer gt = new GeoTransformer( validArea.getCoordinateSystem() );
                    geom = gt.transform( geom );
                }
                return geom.intersects( validArea );
            } catch ( Exception e ) {
                // should never happen
                LOG.logError( "could not validate WMS datasource area", e );
            }
        }
        return true;
    }

    /**
     * runs a loop until all sub requestes (one for each layer) has been finished or the maximum time limit has been
     * exceeded.
     * 
     * @throws OGCWebServiceException
     */
    private void waitForFinished()
                            throws OGCWebServiceException {
        if ( this.count < this.themes.length ) {
            // waits until the requested layers are available as <tt>DisplayElements</tt>
            // or the time limit has been reached.
            // if count == themes.length then no request must be performed
            long timeStamp = System.currentTimeMillis();
            long lapse = 0;
            long timeout = 1000 * ( this.configuration.getDeegreeParams().getRequestTimeLimit() - 1 );
            do {
                try {
                    Thread.sleep( 50 );
                    lapse += 50;
                } catch ( InterruptedException e ) {
                    throw new OGCWebServiceException( "GetMapHandler", "fatal exception waiting for "
                                                                       + "GetMapHandler results" );
                }
            } while ( this.count < this.themes.length && lapse < timeout );
            if ( System.currentTimeMillis() - timeStamp >= timeout ) {
                throw new OGCWebServiceException( "Processing of the GetMap request " + "exceeds timelimit" );
            }
        }
    }

    /**
     * 
     * @param layers
     * @param inSLD
     * @return StyledLayerDescriptor
     * @throws XMLParsingException
     */
    private StyledLayerDescriptor toSLD( GetMap.Layer[] layers, StyledLayerDescriptor inSLD )
                            throws XMLParsingException {
        StyledLayerDescriptor sld = null;

        if ( layers != null && layers.length > 0 && inSLD == null ) {
            // Adds the content from the LAYERS and STYLES attribute to the SLD
            StringBuffer sb = new StringBuffer( 5000 );
            sb.append( "<?xml version=\"1.0\" encoding=\"" + CharsetUtils.getSystemCharset() + "\"?>" );
            sb.append( "<StyledLayerDescriptor version=\"1.0.0\" " );
            sb.append( "xmlns='http://www.opengis.net/sld'>" );

            for ( int i = 0; i < layers.length; i++ ) {
                sb.append( "<NamedLayer>" );
                sb.append( "<Name>" + layers[i].getName() + "</Name>" );
                sb.append( "<NamedStyle><Name>" + layers[i].getStyleName() + "</Name></NamedStyle></NamedLayer>" );
            }
            sb.append( "</StyledLayerDescriptor>" );

            try {
                sld = SLDFactory.createSLD( sb.toString() );
            } catch ( XMLParsingException e ) {
                throw new XMLParsingException( StringTools.stackTraceToString( e ) );
            }
        } else if ( layers != null && layers.length > 0 && inSLD != null ) {
            // if layers not null and sld is not null then SLD layers just be
            // considered if present in the layers list
            List<String> list = new ArrayList<String>();
            for ( int i = 0; i < layers.length; i++ ) {
                list.add( layers[i].getName() );
            }

            List<AbstractLayer> newList = new ArrayList<AbstractLayer>( 20 );
            AbstractLayer[] al = inSLD.getLayers();
            for ( int i = 0; i < al.length; i++ ) {
                if ( list.contains( al[i].getName() ) ) {
                    newList.add( al[i] );
                }
            }
            al = new AbstractLayer[newList.size()];
            sld = new StyledLayerDescriptor( newList.toArray( al ), inSLD.getVersion() );
        } else {
            // if no layers are defined ...
            sld = inSLD;
        }

        return sld;
    }

    /**
     * returns the <tt>UserStyle</tt>s assigned to a named layer
     * 
     * @param sldLayer
     *            layer to get the styles for
     * @param styleName
     *            requested stylename (from the KVP encoding)
     * @return UserStyle
     * @throws OGCWebServiceException
     */
    private UserStyle getStyles( NamedLayer sldLayer, String styleName )
                            throws OGCWebServiceException {

        AbstractStyle[] styles = sldLayer.getStyles();
        UserStyle us = null;

        // to avoid retrieving the layer again for each style
        Layer layer = null;
        layer = this.configuration.getLayer( sldLayer.getName() );
        int i = 0;
        while ( us == null && i < styles.length ) {
            if ( styles[i] instanceof NamedStyle ) {
                // styles will be taken from the WMS's style repository
                us = getPredefinedStyle( styles[i].getName(), sldLayer.getName(), layer );
            } else {
                // if the requested style fits the name of the defined style or
                // if the defined style is marked as default and the requested
                // style if 'default' the condition is true. This includes that
                // if more than one style with the same name or more than one
                // style is marked as default always the first will be choosen
                if ( styleName == null || ( styles[i].getName() != null && styles[i].getName().equals( styleName ) )
                     || ( styleName.equalsIgnoreCase( "$DEFAULT" ) && ( (UserStyle) styles[i] ).isDefault() ) ) {
                    us = (UserStyle) styles[i];
                }
            }
            i++;
        }
        if ( us == null ) {
            // this may happens if the SLD contains a named layer but not
            // a style! yes this is valid according to SLD spec 1.0.0
            us = getPredefinedStyle( styleName, sldLayer.getName(), layer );
        }
        return us;
    }

    /**
     * Returns a Predifined UserStyle
     * 
     * @param styleName
     * @param layerName
     * @param layer
     * @return UserStyle
     * @throws StyleNotDefinedException
     */
    private UserStyle getPredefinedStyle( String styleName, String layerName, Layer layer )
                            throws StyleNotDefinedException {
        UserStyle us = null;

        if ( "default".equals( styleName ) ) {
            us = layer.getStyle( styleName );
        }

        if ( us == null ) {
            if ( styleName == null || styleName.length() == 0 || styleName.equals( "$DEFAULT" )
                 || styleName.equals( "default" ) ) {
                styleName = "default:" + layerName;
            }
        }

        us = layer.getStyle( styleName );
        if ( us == null && !( styleName.startsWith( "default" ) ) && !( styleName.startsWith( "$DEFAULT" ) ) ) {
            String s = Messages.getMessage( "WMS_STYLENOTDEFINED", styleName, layer );
            throw new StyleNotDefinedException( s );
        }
        return us;
    }

    /**
     * validates if the requested layer matches the conditions of the request if not a <tt>WebServiceException</tt> will
     * be thrown. If the layer matches the request, but isn't able to deviever data for the requested area and/or scale
     * false will be returned. If the layer matches the request and contains data for the requested area and/or scale
     * true will be returned.
     * 
     * @param layer
     *            layer as defined at the capabilities/configuration
     * @param name
     *            name of the layer (must be submitted seperatly because the layer parameter can be <tt>null</tt>
     * @return boolean
     * @throws OGCWebServiceException
     */
    private boolean validate( Layer layer, String name )
                            throws OGCWebServiceException {

        // check if layer is available
        if ( layer == null ) {
            throw new LayerNotDefinedException( "Layer: " + name + " is not known by the WMS" );
        }

        if ( !layer.isSrsSupported( this.request.getSrs() ) ) {
            throw new InvalidSRSException( "SRS: " + this.request.getSrs() + "is not known by layer: " + name );
        }

        // check for valid coordinated reference system
        String[] srs = layer.getSrs();
        boolean tmp = false;
        for ( int i = 0; i < srs.length; i++ ) {
            if ( srs[i].equalsIgnoreCase( this.request.getSrs() ) ) {
                tmp = true;
                break;
            }
        }

        if ( !tmp ) {
            throw new InvalidSRSException( "layer: " + name + " can't be " + "delievered in SRS: "
                                           + this.request.getSrs() );
        }

        // check bounding box
        try {

            Envelope bbox = this.request.getBoundingBox();
            Envelope layerBbox = layer.getLatLonBoundingBox();
            if ( !this.request.getSrs().equalsIgnoreCase( "EPSG:4326" ) ) {
                // transform the bounding box of the request to EPSG:4326
                GeoTransformer gt = new GeoTransformer( CRSFactory.create( "EPSG:4326" ) );
                bbox = gt.transform( bbox, this.reqCRS );
            }
            if ( !bbox.intersects( layerBbox ) ) {
                return false;
            }

        } catch ( Exception e ) {
            LOG.logError( e.getMessage(), e );
            throw new OGCWebServiceException( "couldn't compare bounding boxes\n" + e.toString() );
        }

        return true;
    }

    /**
     * put a theme to the passed index of the themes array. The second param passed is a <tt>Theme</tt> or an exception
     * 
     * @param index
     * @param o
     */
    protected synchronized void putTheme( int index, Object o ) {
        this.themes[index] = o;
    }

    /**
     * renders the map from the <tt>DisplayElement</tt>s
     */
    private void renderMap() {

        // GetMapResult response = null;
        OGCWebServiceException exce = null;

        ArrayList<Object> list = new ArrayList<Object>( 50 );
        for ( int i = 0; i < this.themes.length; i++ ) {
            if ( this.themes[i] instanceof Exception ) {
                exce = new OGCWebServiceException( "GetMapHandler_Impl: renderMap", this.themes[i].toString() );
            }
            if ( this.themes[i] instanceof OGCWebServiceException ) {
                exce = (OGCWebServiceException) this.themes[i];
                break;
            }
            if ( this.themes[i] != null ) {
                list.add( this.themes[i] );
            }
        }

        if ( exce == null ) {
            // only if no exception occured
            try {
                Theme[] th = list.toArray( new Theme[list.size()] );
                MapView map = null;
                if ( th.length > 0 ) {
                    double tr = configuration.getDeegreeParams().getPrintMapParam().getTargetResolution();
                    tr = WMPSConfiguration.INCH2M / tr;
                    map = MapFactory.createMapView( "deegree WMS", this.request.getBoundingBox(), this.reqCRS, th, tr );
                }
                this.graph.setClip( 0, 0, this.request.getWidth(), this.request.getHeight() );
                if ( !this.request.getTransparency() ) {
                    this.graph.setColor( this.request.getBGColor() );
                    this.graph.fillRect( 0, 0, this.request.getWidth(), this.request.getHeight() );
                }
                if ( map != null ) {
                    Theme[] allthemes = map.getAllThemes();
                    map.addOptimizer( new LabelOptimizer( allthemes ) );
                    // antialiasing must be switched of for gif output format
                    // because the antialiasing may create more than 255 colors
                    // in the map/image, even just a few colors are defined in
                    // the styles
                    if ( !this.configuration.getDeegreeParams().isAntiAliased() ) {
                        ( (Graphics2D) this.graph ).setRenderingHint( RenderingHints.KEY_ANTIALIASING,
                                                                      RenderingHints.VALUE_ANTIALIAS_ON );
                        ( (Graphics2D) this.graph ).setRenderingHint( RenderingHints.KEY_TEXT_ANTIALIASING,
                                                                      RenderingHints.VALUE_TEXT_ANTIALIAS_ON );
                    }
                    map.paint( this.graph );
                }
            } catch ( Exception e ) {
                LOG.logError( e.getMessage(), e );
                exce = new OGCWebServiceException( "GetMapHandler_Impl: renderMap", e.toString() );
            }
        }

        // print a copyright note at the left lower corner of the map
        printCopyright( this.graph, this.request.getHeight() );

    }

    /**
     * prints a copyright note at left side of the map bottom. The copyright note will be extracted from the WMS
     * capabilities/configuration
     * 
     * @param g
     *            graphic context of the map
     * @param heigth
     *            height of the map in pixel
     */
    private void printCopyright( Graphics g, int heigth ) {

        WMPSDeegreeParams dp = this.configuration.getDeegreeParams();
        String copyright = dp.getCopyright();
        if ( this.copyrightImg != null ) {
            g.drawImage( this.copyrightImg, 8, heigth - this.copyrightImg.getHeight() - 5, null );
        } else {
            if ( copyright != null ) {
                g.setFont( new Font( "SANSSERIF", Font.PLAIN, 14 ) );
                g.setColor( Color.BLACK );
                g.drawString( copyright, 8, heigth - 15 );
                g.drawString( copyright, 10, heigth - 15 );
                g.drawString( copyright, 8, heigth - 13 );
                g.drawString( copyright, 10, heigth - 13 );
                g.setColor( Color.WHITE );
                g.setFont( new Font( "SANSSERIF", Font.PLAIN, 14 ) );
                g.drawString( copyright, 9, heigth - 14 );
                // g.dispose();
            }
        }
    }
}
