/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -
 and
 - grit graphische Informationstechnik Beratungsgesellschaft mbH -

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

 grit graphische Informationstechnik Beratungsgesellschaft mbH
 Landwehrstr. 143, 59368 Werne
 Germany
 http://www.grit.de/

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
package org.deegree.style.utils;

import java.awt.image.BufferedImage;
import java.io.InputStream;

import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.XMLAbstractTranscoder;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SvgImageTranscoder extends ImageTranscoder {
    
    public class SvgImageOutput extends TranscoderOutput {

        private BufferedImage image;

        public SvgImageOutput() {
        }

        public BufferedImage getBufferedImage() {
            return this.image;
        }

        public void setImage( BufferedImage image ) {
            this.image = image;
        }
    }
    
    static final Logger LOG = LoggerFactory.getLogger( SvgImageTranscoder.class );

    public SvgImageTranscoder() {
        super();
    }

    public void setXmlParserClass( String clsName ) {
        addTranscodingHint( XMLAbstractTranscoder.KEY_XML_PARSER_CLASSNAME, clsName );
    }

    @Override
    public BufferedImage createImage( int w, int h ) {
        return new BufferedImage( w, h, BufferedImage.TYPE_INT_ARGB );
    }

    @Override
    public void writeImage( BufferedImage image, TranscoderOutput output )
                            throws TranscoderException {
        if ( !( output instanceof SvgImageOutput ) ) {
            throw new TranscoderException( "TranscoderOutput has to be of type BuffferedImageOutput" );
        }

        ( (SvgImageOutput) output ).setImage( image );
    }

    public void transcode( InputStream input, String fileName, SvgImageOutput output )
                            throws TranscoderException {
        long ts, te;
        ts = System.currentTimeMillis();

        super.transcode( new org.apache.batik.transcoder.TranscoderInput( input ), output );

        te = System.currentTimeMillis();
        LOG.info( "Loaded svg '{}' in {} ms", fileName, ( te - ts ) );
    }
    
    public SvgImageOutput createOutput() {
        return new SvgImageOutput();
    }
}
