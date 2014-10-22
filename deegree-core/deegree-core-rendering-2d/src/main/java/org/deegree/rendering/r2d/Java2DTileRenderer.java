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

 Occam Labs UG (haftungsbeschränkt)
 Godesberger Allee 139, 53175 Bonn
 Germany
 http://www.occamlabs.de/

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.rendering.r2d;

import org.deegree.commons.utils.math.MathUtils;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.exceptions.TransformationException;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.cs.persistence.CRSManager;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.GeometryTransformer;
import org.deegree.tile.Tile;
import org.deegree.tile.TileIOException;
import org.slf4j.Logger;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.Iterator;

import static java.awt.Color.RED;
import static java.awt.image.BufferedImage.TYPE_4BYTE_ABGR;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * <code>Java2DTileRenderer</code>
 * 
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @author last edited by: $Author: mschneider $
 * 
 * @version $Revision: 31882 $, $Date: 2011-09-15 02:05:04 +0200 (Thu, 15 Sep 2011) $
 */
public class Java2DTileRenderer implements TileRenderer {

    private static final Logger LOG = getLogger( Java2DTileRenderer.class );

    private final Graphics2D graphics;

    private final int width;

    private final int height;

    private final Envelope envelope;

    private AffineTransform worldToScreen = new AffineTransform();

    /**
     * When transform is called, all tiles are rendered into main graphics.
     * 
     * @param graphics
     *            main graphics, never <code>null</code>
     * @param width
     *            query width
     * @param height
     *            query height
     * @param envelope
     *            query envelope, never <code>null</code>
     */
    public Java2DTileRenderer( Graphics2D graphics, int width, int height, Envelope envelope ) {
        this.graphics = graphics;
        this.width = width;
        this.height = height;
        this.envelope = envelope;
        RenderHelper.getWorldToScreenTransform( worldToScreen, envelope, width, height );
    }

    @Override
    public void render( Iterator<Tile> tiles ) {
        BufferedImage image = new BufferedImage( width, height, TYPE_4BYTE_ABGR );
        Graphics g = image.getGraphics();
        ICRS crsOfTile = renderAllTilesInTileCrs( tiles, g );
        renderToMainGraphics( image, crsOfTile );
    }

    private ICRS renderAllTilesInTileCrs( Iterator<Tile> tiles, Graphics g ) {
        ICRS crsOfTile = null;
        AffineTransform worldToScreenTransformInTileCrs = null;
        if ( tiles.hasNext() ) {
            Tile firstTile = tiles.next();
            crsOfTile = firstTile.getEnvelope().getCoordinateSystem();
            worldToScreenTransformInTileCrs = createWorldToScreenTransform( crsOfTile );
            renderInTileCrs( firstTile, g, worldToScreenTransformInTileCrs );
        }
        while ( tiles.hasNext() ) {
            renderInTileCrs( tiles.next(), g, worldToScreenTransformInTileCrs );
        }
        return crsOfTile;
    }

    private AffineTransform createWorldToScreenTransform( ICRS sourceCrs ) {
        try {
            if ( !sourceCrs.equals( envelope.getCoordinateSystem() ) ) {
                AffineTransform worldToScreenInTileCrs = new AffineTransform();
                Envelope transformedEnvelope = transformQueryEnvelope( sourceCrs );
                RenderHelper.getWorldToScreenTransform( worldToScreenInTileCrs, transformedEnvelope, width, height );
                return worldToScreenInTileCrs;
            }
            return worldToScreen;
        } catch ( UnknownCRSException e ) {
            handleWorldToScreenTransformException( e );
            return worldToScreen;
        } catch ( TransformationException e ) {
            handleWorldToScreenTransformException( e );
            return worldToScreen;
        }
    }

    private void renderInTileCrs( Tile tile, Graphics g, AffineTransform worldToScreenInTileCrs ) {
        if ( tile == null ) {
            LOG.debug( "Not rendering null tile." );
            return;
        }
        BufferedImage image = tile.getAsImage();
        drawImage( image, g, worldToScreenInTileCrs, tile.getEnvelope() );
    }

    private void renderToMainGraphics( BufferedImage image, ICRS sourceCrs ) {
        if ( sourceCrs != null && !envelope.getCoordinateSystem().equals( sourceCrs ) ) {
            BufferedImage transformedImage = transformImage( image, sourceCrs );
            drawImage( transformedImage, graphics, worldToScreen, envelope );
        } else {
            drawImage( image, graphics, worldToScreen, envelope );
        }
    }

    private BufferedImage transformImage( BufferedImage image, ICRS sourceCrs ) {
        try {
            Envelope sourceEnvelope = transformQueryEnvelope( sourceCrs );
            GeotoolsRasterTransformer transformer = new GeotoolsRasterTransformer( sourceEnvelope, envelope );
            return transformer.transform( image );
        } catch ( UnknownCRSException e ) {
            handleTransformImageException( e );
            return image;
        } catch ( TransformationException e ) {
            handleTransformImageException( e );
            return image;
        }
    }

    private Envelope transformQueryEnvelope( ICRS targetCrs )
                            throws UnknownCRSException, TransformationException {
        try {
            ICRS crs = CRSManager.lookup( targetCrs.getAlias() );
            return new GeometryTransformer( crs ).transform( envelope );
        } catch ( TransformationException e ) {
            LOG.warn( "Could not transform envelope: " + e.getMessage() );
            e.printStackTrace();
            throw e;
        } catch ( UnknownCRSException e ) {
            LOG.warn( "Could not transform envelope as CRS is unknown: " + e.getMessage() );
            e.printStackTrace();
            throw e;
        }
    }

    private void drawImage( BufferedImage image, Graphics g, AffineTransform worldToScreenTransform, Envelope env ) {
        int minx, miny, maxx, maxy;
        Point2D.Double minPoint = (Point2D.Double) worldToScreenTransform.transform( new Point2D.Double(
                                                                                                         env.getMin().get0(),
                                                                                                         env.getMin().get1() ),
                                                                                     null );
        minx = MathUtils.round( minPoint.x );
        miny = MathUtils.round( minPoint.y );
        Point2D.Double maxPoint = (Point2D.Double) worldToScreenTransform.transform( new Point2D.Double(
                                                                                                         env.getMax().get0(),
                                                                                                         env.getMax().get1() ),
                                                                                     null );
        maxx = MathUtils.round( maxPoint.x );
        maxy = MathUtils.round( maxPoint.y );

        int minxCorrected = Math.min( minx, maxx );
        int minyCorrected = Math.min( miny, maxy );
        int maxxCorrected = Math.max( minx, maxx );
        int maxyCorrected = Math.max( miny, maxy );

        try {
            g.drawImage( image, minxCorrected, minyCorrected, maxxCorrected - minxCorrected, maxyCorrected
                                                                                             - minyCorrected, null );
        } catch ( TileIOException e ) {
            LOG.debug( "Error retrieving image: " + e.getMessage() );
            g.setColor( RED );
            g.fillRect( minx, miny, maxx - minx, maxy - miny );
        }
    }

    private void handleWorldToScreenTransformException( Exception e ) {
        LOG.warn( "Envelope could not be transformed to source CRS. World-To-Screen-Transformation of query envelope is used! Reason: "
                  + e.getMessage() );
    }

    private void handleTransformImageException( Exception e ) {
        LOG.warn( "Envelope could not be transformed to source CRS. Geotools transformation is canceled! Reason: "
                  + e.getMessage() );
    }

}