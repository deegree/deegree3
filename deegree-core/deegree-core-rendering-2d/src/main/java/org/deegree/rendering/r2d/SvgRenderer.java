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

import static javax.media.jai.JAI.create;
import static org.apache.batik.transcoder.SVGAbstractTranscoder.KEY_HEIGHT;
import static org.apache.batik.transcoder.SVGAbstractTranscoder.KEY_WIDTH;
import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.deegree.commons.utils.math.MathUtils.round;
import static org.slf4j.LoggerFactory.getLogger;

import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.media.jai.RenderedOp;

import org.apache.batik.ext.awt.image.codec.MemoryCacheSeekableStream;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.deegree.commons.utils.ComparablePair;
import org.deegree.style.styling.components.Graphic;
import org.slf4j.Logger;

/**
 * Renders svg images onto buffered images.
 * 
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @author last edited by: $Author: mschneider $
 * 
 * @version $Revision: 31882 $, $Date: 2011-09-15 02:05:04 +0200 (Thu, 15 Sep 2011) $
 */
class SvgRenderer {

    private static final Logger LOG = getLogger( SvgRenderer.class );

    final LinkedHashMap<ComparablePair<String, Integer>, BufferedImage> svgCache = new LinkedHashMap<ComparablePair<String, Integer>, BufferedImage>(
                                                                                                                                                      256 ) {
        private static final long serialVersionUID = -6847956873232942891L;

        @Override
        protected boolean removeEldestEntry( Map.Entry<ComparablePair<String, Integer>, BufferedImage> eldest ) {
            return size() > 256; // yeah, hardcoded max size... TODO
        }
    };

    BufferedImage prepareSvg( Rectangle2D.Double rect, Graphic g ) {
        BufferedImage img = null;
        ComparablePair<String, Integer> cp = new ComparablePair<String, Integer>( g.imageURL, round( g.size ) );
        if ( svgCache.containsKey( cp ) ) {
            img = svgCache.get( cp );
        } else {
            PNGTranscoder t = new PNGTranscoder();

            t.addTranscodingHint( KEY_WIDTH, new Float( rect.width ) );
            t.addTranscodingHint( KEY_HEIGHT, new Float( rect.height ) );

            TranscoderInput input = new TranscoderInput( g.imageURL );

            // TODO improve performance by writing a custom transcoder output directly rendering on an image, or
            // even on the target graphics
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            TranscoderOutput output = new TranscoderOutput( out );
            InputStream in = null;

            // TODO cache images
            try {
                t.transcode( input, output );
                out.flush();
                in = new ByteArrayInputStream( out.toByteArray() );
                MemoryCacheSeekableStream mcss = new MemoryCacheSeekableStream( in );
                RenderedOp rop = create( "stream", mcss );
                img = rop.getAsBufferedImage();
                svgCache.put( cp, img );
            } catch ( TranscoderException e ) {
                LOG.warn( "Could not rasterize svg '{}': {}", g.imageURL, e.getLocalizedMessage() );
            } catch ( IOException e ) {
                LOG.warn( "Could not rasterize svg '{}': {}", g.imageURL, e.getLocalizedMessage() );
            } finally {
                closeQuietly( out );
                closeQuietly( in );
            }
        }
        return img;
    }

}
