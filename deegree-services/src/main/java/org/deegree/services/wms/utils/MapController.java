//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
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
package org.deegree.services.wms.utils;

import static java.awt.Color.white;
import static java.awt.image.BufferedImage.TYPE_INT_ARGB;
import static java.lang.Math.abs;
import static java.lang.Math.min;
import static java.lang.System.currentTimeMillis;
import static java.util.Collections.singleton;
import static org.deegree.commons.utils.math.MathUtils.round;
import static org.deegree.cs.CRS.EPSG_4326;
import static org.deegree.protocol.wms.Utils.calcResolution;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.deegree.commons.utils.Pair;
import org.deegree.cs.CRS;
import org.deegree.cs.coordinatesystems.CoordinateSystem;
import org.deegree.cs.exceptions.TransformationException;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.feature.FeatureCollection;
import org.deegree.feature.GenericFeatureCollection;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.GeometryFactory;
import org.deegree.geometry.GeometryTransformer;
import org.deegree.protocol.wms.Utils;
import org.deegree.rendering.r2d.se.unevaluated.Style;
import org.deegree.services.wms.MapService;
import org.deegree.services.wms.WMSException.InvalidDimensionValue;
import org.deegree.services.wms.WMSException.MissingDimensionValue;
import org.deegree.services.wms.controller.ops.GetFeatureInfo;
import org.deegree.services.wms.controller.ops.GetMap;
import org.deegree.services.wms.model.layers.Layer;

/**
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class MapController {

    private static final GeometryFactory fac = new GeometryFactory();

    private Envelope envelope;

    private int width;

    private int height;

    private MapService service;

    private List<Long> repaintList = Collections.synchronizedList( new ArrayList<Long>() );

    private LinkedList<Layer> layers = new LinkedList<Layer>();

    private BufferedImage currentImage;

    private BufferedImage panImage;

    private int panx, pany;

    private Layer queryLayer;

    private boolean repainting;

    private int zoomMinx = -1, zoomMiny = -1, zoomMaxx = -1, zoomMaxy = -1;

    private int originalWidth = -1, originalHeight = -1;

    private boolean resizing;

    /**
     * @param service
     * @param crs
     * @param width
     * @param height
     */
    public MapController( MapService service, CRS crs, int width, int height ) {
        repaintList.add( currentTimeMillis() );
        this.width = width;
        this.height = height;
        this.service = service;
        envelope = service.getRootLayer().getBbox();
        envelope = envelope == null ? fac.createEnvelope( -180, -90, 180, 90, EPSG_4326 ) : envelope;
        try {
            envelope = new GeometryTransformer( crs.getWrappedCRS() ).transform( envelope );
        } catch ( TransformationException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch ( UnknownCRSException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        ensureAspect();
    }

    /**
     * @return true, if resizing is in progress
     */
    public boolean isResizing() {
        return resizing;
    }

    /**
     * @return the currently displayed map
     */
    public BufferedImage getCurrentImage() {
        return currentImage;
    }

    /**
     * @return the crs of the map
     */
    public CRS getCRS() {
        return envelope.getCoordinateSystem();
    }

    /**
     * @return the current query layer, or null, if none is selected
     */
    public Layer getQueryLayer() {
        return queryLayer;
    }

    /**
     * @return true, if the controller is currently repainting
     */
    public boolean isRepainting() {
        return repainting;
    }

    private void ensureAspect() {
        double minx = envelope.getMin().get0();
        double miny = envelope.getMin().get1();

        double ratio = (double) height / (double) width;
        double w, h;
        if ( envelope.getSpan1() / envelope.getSpan0() < 1 ) {
            w = envelope.getSpan0();
            h = w * ratio;
            miny += ( envelope.getSpan1() - h ) / 2;
        } else {
            h = envelope.getSpan1();
            w = h / ratio;
            minx += ( envelope.getSpan0() - w ) / 2;
        }

        envelope = fac.createEnvelope( minx, miny, minx + w, miny + h, envelope.getCoordinateSystem() );
    }

    private GetMap getCurrentQuery() {
        List<Style> styles = new ArrayList<Style>( layers.size() );
        for ( Layer l : layers ) {
            styles.add( service.getStyles().getDefault( l.getName() ) );
        }
        return new GetMap( service, layers, styles, width, height, envelope );
    }

    /**
     * @param g
     * @param preview
     */
    public void paintMap( Graphics2D g, boolean preview ) {
        // make sure no new entries come in when catching the time
        long repaintStart;
        synchronized ( repaintList ) {
            repaintStart = currentTimeMillis();
        }
        repainting = true;
        if ( repaintList.isEmpty() || preview || resizing ) {
            if ( zoomMinx >= 0 && zoomMiny >= 0 && zoomMaxx >= 0 && zoomMaxy >= 0 ) {
                BufferedImage newImage = new BufferedImage( width, height, TYPE_INT_ARGB );
                Graphics2D g2 = newImage.createGraphics();
                g2.setPaint( Color.white );
                g2.fillRect( 0, 0, width, height );
                g2.drawImage( currentImage, 0, 0, width, height, null );
                g2.setPaint( new Color( 0xcc, 0xcc, 0xcc, 0xa0 ) );
                g2.fillRect( min( zoomMinx, zoomMaxx ), min( zoomMiny, zoomMaxy ), abs( zoomMinx - zoomMaxx ),
                             abs( zoomMiny - zoomMaxy ) );
                g2.setPaint( Color.red );
                g2.drawRect( min( zoomMinx, zoomMaxx ), min( zoomMiny, zoomMaxy ), abs( zoomMinx - zoomMaxx ),
                             abs( zoomMiny - zoomMaxy ) );
                g2.dispose();
                g.drawImage( newImage, 0, 0, width, height, null );
            } else {
                if ( originalWidth > 0 && originalHeight > 0 ) {
                    g.drawImage( currentImage, 0, 0, originalWidth, originalHeight, null );
                } else {
                    g.drawImage( currentImage, 0, 0, width, height, null );
                }
            }
            return;
        }
        if ( originalWidth > 0 && originalHeight > 0 ) {
            g.drawImage( currentImage, 0, 0, originalWidth, originalHeight, null );
        } else {
            g.drawImage( currentImage, 0, 0, width, height, null );
        }
        GetMap gm = getCurrentQuery();
        try {
            currentImage = service.getMapImage( gm ).first;
            g.setPaint( white );
            g.fillRect( 0, 0, width, height );
            g.drawImage( currentImage, 0, 0, width, height, null );
            originalWidth = -1;
            originalHeight = -1;
            // TODO how to use this and also use the buffered images?
            // service.paintMap( g, gm, new LinkedList<String>() );
        } catch ( MissingDimensionValue e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch ( InvalidDimensionValue e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // the list is synchronized, but I'm unsure whether it's sufficient when using the iterator
        synchronized ( repaintList ) {
            ListIterator<Long> iterator = repaintList.listIterator();
            while ( iterator.hasNext() ) {
                if ( iterator.next() < repaintStart ) {
                    iterator.remove();
                }
            }
            // TODO better would be to cancel the repainting of course
            if ( !repaintList.isEmpty() ) {
                paintMap( g, preview );
            }
            repainting = false;
        }
    }

    /**
     * @param radius
     * @param x
     * @param y
     * @return do a get feature info internally
     */
    public FeatureCollection getFeatures( int radius, int x, int y ) {
        if ( queryLayer == null ) {
            return new GenericFeatureCollection();
        }
        GetFeatureInfo gfi = new GetFeatureInfo( singleton( queryLayer ),
                                                 singleton( service.getStyles().getDefault( queryLayer.getName() ) ),
                                                 radius, envelope, x, y, width, height, 6 );
        try {
            return service.getFeatures( gfi ).first;
        } catch ( MissingDimensionValue e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch ( InvalidDimensionValue e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @param layers
     */
    public void setLayers( List<Layer> layers ) {
        checkpoint: if ( layers.size() == this.layers.size() ) {
            Iterator<Layer> myLayers = this.layers.iterator();
            Iterator<Layer> newLayers = layers.iterator();
            while ( myLayers.hasNext() ) {
                if ( myLayers.next() != newLayers.next() ) {
                    break checkpoint;
                }
            }
            return;
        }
        repaintList.add( currentTimeMillis() );
        this.layers.clear();
        this.layers.addAll( layers );
    }

    /**
     * 
     */
    public void startResize() {
        originalWidth = width;
        originalHeight = height;
        resizing = true;
    }

    /**
     * 
     */
    public void stopResize() {
        resizing = false;
    }

    /**
     * @param width
     * @param height
     */
    public void setSize( int width, int height ) {
        if ( width == this.width && height == this.height ) {
            return;
        }
        repaintList.add( currentTimeMillis() );

        double res = calcResolution( envelope, this.width, this.height );
        double minx = envelope.getMin().get0();
        double miny = envelope.getMin().get1();
        double maxx = minx + width * res;
        double maxy = miny + height * res;

        envelope = fac.createEnvelope( minx, miny, maxx, maxy, envelope.getCoordinateSystem() );

        this.width = width;
        this.height = height;
        ensureAspect();
    }

    /**
     * @param factor
     *            if positive, zoom out
     * @param centerx
     *            if negative, the center will remain unchanged
     * @param centery
     *            if negative, the center will remain unchanged
     */
    public void zoom( double factor, int centerx, int centery ) {
        if ( centerx < 0 ) {
            centerx = width / 2;
        }
        if ( centery < 0 ) {
            centery = height / 2;
        }

        // create preview
        panImage = null;
        BufferedImage cur = currentImage;
        currentImage = new BufferedImage( width, height, TYPE_INT_ARGB );
        Graphics2D g = currentImage.createGraphics();
        g.setPaint( white );
        g.fillRect( 0, 0, width, height );
        double f = factor > 0 ? 1 / factor : abs( factor );
        int neww = round( width * f );
        int newh = round( height * f );
        int newx = round( ( width / 2 - centerx ) * f ) + ( width - neww ) / 2;
        int newy = round( ( height / 2 - centery ) * f ) + ( height - newh ) / 2;
        g.drawImage( cur, newx, newy, neww, newh, null );
        g.dispose();

        repaintList.add( currentTimeMillis() );
        centery = height - centery;
        double w = envelope.getSpan0() * factor;
        double h = envelope.getSpan1() * factor;
        double ex = envelope.getMin().get0();
        double ey = envelope.getMin().get1();
        double res = calcResolution( envelope, width, height );
        double minx = ex + centerx * res - w / 2;
        double miny = ey + centery * res - h / 2;
        envelope = fac.createEnvelope( minx, miny, minx + w, miny + h, envelope.getCoordinateSystem() );
        ensureAspect();
    }

    /**
     * 
     */
    public void endPanning() {
        panImage = null;
    }

    /**
     * parameters are in window coordinates
     * 
     * @param minx
     * @param miny
     * @param maxx
     * @param maxy
     */
    public void setEnvelope( int minx, int miny, int maxx, int maxy ) {
        // pre-correct aspect of screen coords for preview
        int oldw = maxx - minx;
        int oldh = maxy - miny;
        double aspect = (double) oldh / (double) oldw;
        double screenAspect = (double) height / (double) width;
        if ( aspect < screenAspect ) {
            int newh = round( oldw * screenAspect );
            int diff = abs( oldh - newh ) / 2;
            miny -= diff;
            maxy = miny + newh;
        } else {
            int neww = round( oldh / screenAspect );
            int diff = abs( oldw - neww ) / 2;
            minx -= diff;
            maxx += diff;
        }

        if ( maxx - minx == width && maxy - miny == height ) {
            if ( panImage == null ) {
                panImage = currentImage;
                panx = 0;
                pany = 0;
            }
            // create preview
            BufferedImage cur = panImage;
            currentImage = new BufferedImage( width, height, TYPE_INT_ARGB );
            Graphics2D g = currentImage.createGraphics();
            g.setPaint( white );
            g.fillRect( 0, 0, width, height );
            g.drawImage( cur, 0, 0, width, height, minx + panx, miny + pany, maxx + panx, maxy + pany, null );
            g.dispose();
            panx += minx;
            pany += miny;
        } else {
            panImage = null;
            // create preview
            BufferedImage cur = currentImage;
            currentImage = new BufferedImage( width, height, TYPE_INT_ARGB );
            Graphics2D g = currentImage.createGraphics();
            g.setPaint( white );
            g.fillRect( 0, 0, width, height );
            g.drawImage( cur, 0, 0, width, height, minx, miny, maxx, maxy, null );
            g.dispose();
        }

        // deal with upper/left vs lower/left issue for y coords
        int h = miny;
        miny = height - maxy;
        maxy = height - h;

        repaintList.add( currentTimeMillis() );
        double res = calcResolution( envelope, width, height );
        double envminx = envelope.getMin().get0();
        double envminy = envelope.getMin().get1();
        envelope = fac.createEnvelope( envminx + minx * res, envminy + miny * res, envminx + maxx * res, envminy + maxy
                                                                                                         * res,
                                       envelope.getCoordinateSystem() );
    }

    /**
     * @param sourcex
     * @param sourcey
     * @param currentx
     * @param currenty
     */
    public void pan( int sourcex, int sourcey, int currentx, int currenty ) {
        if ( sourcex != currentx || sourcey != currenty ) {
            repaintList.add( currentTimeMillis() );
        }
        setEnvelope( sourcex - currentx, sourcey - currenty, sourcex - currentx + width, sourcey - currenty + height );
    }

    /**
     * @return whether parameters have changed and the map needs repainting
     */
    public boolean needsRepaint() {
        return !repaintList.isEmpty();
    }

    /**
     * @param x
     * @param y
     * @return the world coordinates
     */
    public Pair<Double, Double> translate( int x, int y ) {
        double res = calcResolution( envelope, width, height );
        return new Pair<Double, Double>( envelope.getMin().get0() + x * res, envelope.getMin().get1() + ( width - y )
                                                                             * res );
    }

    /**
     * 
     */
    public void zoomToMaxExtent() {
        repaintList.add( currentTimeMillis() );
        try {
            panImage = null;
            CoordinateSystem crs = envelope.getCoordinateSystem().getWrappedCRS();
            envelope = service.getRootLayer().getBbox();
            envelope = envelope == null ? fac.createEnvelope( -180, -90, 180, 90, EPSG_4326 ) : envelope;
            envelope = new GeometryTransformer( crs ).transform( envelope );
        } catch ( TransformationException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch ( UnknownCRSException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        ensureAspect();
    }

    /**
     * @return the current scale (0.28 mm pixelsize)
     */
    public double getCurrentScale() {
        try {
            return Utils.calcScaleWMS130( width, height, envelope, envelope.getCoordinateSystem().getWrappedCRS() );
        } catch ( UnknownCRSException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * @param layer
     */
    public void setQueryLayer( Layer layer ) {
        this.queryLayer = layer;
    }

    /**
     * @param minx
     * @param miny
     */
    public void setZoomRectStart( int minx, int miny ) {
        this.zoomMinx = minx;
        this.zoomMiny = miny;
    }

    /**
     * @param x
     * @param y
     */
    public void setZoomRectEnd( int x, int y ) {
        this.zoomMaxx = x;
        this.zoomMaxy = y;
    }

    /**
     * 
     */
    public void stopZoomin() {
        zoomMinx = -1;
        zoomMiny = -1;
        zoomMaxx = -1;
        zoomMaxy = -1;
    }

}
