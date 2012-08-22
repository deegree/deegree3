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

package org.deegree.framework.util;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.batik.bridge.BaseScriptingEnvironment;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.BridgeException;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.bridge.ViewBox;
import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.apache.batik.dom.svg.SVGOMDocument;
import org.apache.batik.dom.util.DOMUtilities;
import org.apache.batik.ext.awt.image.GraphicsUtil;
import org.apache.batik.gvt.CanvasGraphicsNode;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.transcoder.SVGAbstractTranscoder;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.TranscodingHints;
import org.apache.batik.transcoder.keys.BooleanKey;
import org.apache.batik.util.ParsedURL;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.svg.SVGSVGElement;

/**
 * <code>AliasingSVGTranscoder</code> to create a BufferedImage from an SVG with the possibility to
 * set preferences of aliasing
 *
 * @author <a href="mailto:buesching@lat-lon.de">Lyn Buesching</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 *
 */
public class AliasingSVGTranscoder extends SVGAbstractTranscoder {

    public static final TranscodingHints.Key KEY_ALIASING = new BooleanKey();

    private BufferedImage bufferedImage = null;

    @Override
    protected void transcode( Document document, String uri, TranscoderOutput output )
                            throws TranscoderException {

        if ( ( document != null ) && !( document.getImplementation() instanceof SVGDOMImplementation ) ) {
            DOMImplementation impl;
            impl = (DOMImplementation) hints.get( KEY_DOM_IMPLEMENTATION );
            // impl = SVGDOMImplementation.getDOMImplementation();
            document = DOMUtilities.deepCloneDocument( document, impl );
            if ( uri != null ) {
                try {
                    URL url = new URL( uri );
                    ( (SVGOMDocument) document ).setURLObject( url );
                } catch ( MalformedURLException mue ) {
                }
            }
        }

        ctx = createBridgeContext();
        SVGOMDocument svgDoc = (SVGOMDocument) document;
        SVGSVGElement root = svgDoc.getRootElement();

        // build the GVT tree
        builder = new GVTBuilder();
        // flag that indicates if the document is dynamic

        boolean isDynamic = ( hints.containsKey( KEY_EXECUTE_ONLOAD )
                              && ( (Boolean) hints.get( KEY_EXECUTE_ONLOAD ) ).booleanValue() && ctx.isDynamicDocument( svgDoc ) );

        GraphicsNode gvtRoot;
        try {
            if ( isDynamic )
                ctx.setDynamicState( BridgeContext.DYNAMIC );

            gvtRoot = builder.build( ctx, svgDoc );

            // dispatch an 'onload' event if needed
            if ( ctx.isDynamic() ) {
                BaseScriptingEnvironment se;
                se = new BaseScriptingEnvironment( ctx );
                se.loadScripts();
                se.dispatchSVGLoadEvent();
            }
        } catch ( BridgeException ex ) {
            throw new TranscoderException( ex );
        }

        // get the 'width' and 'height' attributes of the SVG document
        float docWidth = (float) ctx.getDocumentSize().getWidth();
        float docHeight = (float) ctx.getDocumentSize().getHeight();

        setImageSize( docWidth, docHeight );

        // compute the preserveAspectRatio matrix
        AffineTransform Px;

        // take the AOI into account if any
        if ( hints.containsKey( KEY_AOI ) ) {
            Rectangle2D aoi = (Rectangle2D) hints.get( KEY_AOI );
            // transform the AOI into the image's coordinate system
            Px = new AffineTransform();
            double sx = width / aoi.getWidth();
            double sy = height / aoi.getHeight();
            double scale = Math.min( sx, sy );
            Px.scale( scale, scale );
            double tx = -aoi.getX() + ( width / scale - aoi.getWidth() ) / 2;
            double ty = -aoi.getY() + ( height / scale - aoi.getHeight() ) / 2;
            ;
            Px.translate( tx, ty );
            // take the AOI transformation matrix into account
            // we apply first the preserveAspectRatio matrix
            curAOI = aoi;
        } else {
            String ref = new ParsedURL( uri ).getRef();

            try {
                Px = ViewBox.getViewTransform( ref, root, width, height );
            } catch ( BridgeException ex ) {
                throw new TranscoderException( ex );
            }

            if ( Px.isIdentity() && ( width != docWidth || height != docHeight ) ) {
                // The document has no viewBox, we need to resize it by hand.
                // we want to keep the document size ratio
                float xscale, yscale;
                xscale = width / docWidth;
                yscale = height / docHeight;
                float scale = Math.min( xscale, yscale );
                Px = AffineTransform.getScaleInstance( scale, scale );
            }

            curAOI = new Rectangle2D.Float( 0, 0, width, height );
        }

        CanvasGraphicsNode cgn = getCanvasGraphicsNode( gvtRoot );
        if ( cgn != null ) {
            cgn.setViewingTransform( Px );
            curTxf = new AffineTransform();
        } else {
            curTxf = Px;
        }

        gvtRoot = renderImage( output, gvtRoot, Px, (int) width, (int) height );

        this.root = gvtRoot;

    }

    private GraphicsNode renderImage( TranscoderOutput output, GraphicsNode gvtRoot, AffineTransform Px, int w, int h )
                            throws TranscoderException {

        Graphics2D g2d = createGraphics( w, h );
        // Check anti-aliasing preference
        if ( hints.containsKey( KEY_ALIASING ) ) {
            boolean antialias = ( (Boolean) hints.get( KEY_ALIASING ) ).booleanValue();
            g2d.setRenderingHint( RenderingHints.KEY_ANTIALIASING, antialias ? RenderingHints.VALUE_ANTIALIAS_ON
                                                                            : RenderingHints.VALUE_ANTIALIAS_OFF );
        } else {
            g2d.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
        }

        g2d.clip( new java.awt.Rectangle( 0, 0, w, h ) );
        g2d.transform( Px );
        gvtRoot.paint( g2d );
        g2d.dispose();
        return null;
    }

    private Graphics2D createGraphics( int w, int h ) {
        bufferedImage = new BufferedImage( w, h, BufferedImage.TYPE_INT_ARGB );
        Graphics2D g2d = GraphicsUtil.createGraphics( bufferedImage );
        return g2d;
    }

    /**
     * @return BufferedImage the created BufferedImage
     */
    public BufferedImage getBufferedImage() {
        return bufferedImage;
    }

}
