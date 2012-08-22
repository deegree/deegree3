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
package org.deegree.graphics.sld;

import static org.deegree.framework.xml.XMLTools.escape;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;

import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.bridge.UserAgentAdapter;
import org.apache.batik.dom.svg.SAXSVGDocumentFactory;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.transcoder.SVGAbstractTranscoder;
import org.apache.batik.transcoder.Transcoder;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.StringTools;
import org.deegree.framework.xml.Marshallable;
import org.deegree.model.feature.Feature;
import org.deegree.model.feature.FeatureProperty;
import org.w3c.dom.svg.SVGDocument;

import com.sun.media.jai.codec.MemoryCacheSeekableStream;

/**
 * The ExternalGraphic element allows a reference to be made to an external graphic file with a Web URL. The
 * OnlineResource sub-element gives the URL and the Format sub-element identifies the expected document MIME type of a
 * successful fetch. Knowing the MIME type in advance allows the styler to select the best- supported format from the
 * list of URLs with equivalent content.
 * 
 * 
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class ExternalGraphic implements Marshallable {

    private static final ILogger LOG = LoggerFactory.getLogger( ExternalGraphic.class );

    private static BufferedImage defaultImage = null;
    static {
        if ( defaultImage == null ) {
            defaultImage = new BufferedImage( 1, 1, BufferedImage.TYPE_INT_ARGB );
        }
    }

    private BufferedImage image = null;

    private String format = null;

    private URL onlineResource = null;

    private String title;

    private TranscoderInput input = null;

    private ByteArrayOutputStream bos = null;

    private TranscoderOutput output = null;

    private Transcoder trc = null;

    private static Map<String, BufferedImage> cache = null;
    static {
        if ( cache == null ) {
            cache = new HashMap<String, BufferedImage>( 500 );
        }
    }

    /**
     * Creates a new ExternalGraphic_Impl object.
     * 
     * @param format
     * @param onlineResource
     */
    ExternalGraphic( String format, URL onlineResource ) {
        this( format, onlineResource, null );
    }

    /**
     * Creates a new ExternalGraphic_Impl object.
     * 
     * @param format
     * @param onlineResource
     * @param title
     *            the title of the online resource, may be <code>null</code>
     */
    ExternalGraphic( String format, URL onlineResource, String title ) {
        setFormat( format );
        setOnlineResource( onlineResource );
        setTitle( title );
    }

    /**
     * the Format sub-element identifies the expected document MIME type of a successful fetch.
     * 
     * @return Format of the external graphic
     * 
     */
    public String getFormat() {
        return format;
    }

    /**
     * the title of th online resource
     * 
     * @return Format of the external graphic
     * 
     */
    public String getTitle() {
        return title;
    }

    /**
     * sets the format (MIME type)
     * 
     * @param format
     *            Format of the external graphic
     * 
     */
    public void setFormat( String format ) {
        this.format = format;
    }

    /**
     * The OnlineResource gives the URL of the external graphic
     * 
     * @return URL of the external graphic
     * 
     */
    public URL getOnlineResource() {
        return onlineResource;
    }

    /**
     * sets the title of the online resource
     * 
     * @param format
     *            Format of the external graphic
     * 
     */
    public void setTitle( String title ) {
        this.title = title;
    }

    /**
     * sets the online resource / URL of the external graphic
     * 
     * @param onlineResource
     *            URL of the external graphic
     * 
     */
    public void setOnlineResource( URL onlineResource ) {
        this.onlineResource = onlineResource;
        // TODO
        // removing this is just experimental; possibly initializeOnlineResource( Feature feature )
        // must be adapted
        // String file = onlineResource.getFile();
        // int idx = file.indexOf( "$" );
        // if ( idx == -1 ) {
        // retrieveImage( onlineResource );
        // }
    }

    /**
     * @param onlineResource
     */
    private void retrieveImage( URL onlineResource ) {

        try {
            String t = onlineResource.toURI().toASCIIString();
            if ( t.trim().toLowerCase().endsWith( ".svg" ) ) {
                // initialize the the classes required for svg handling
                bos = new ByteArrayOutputStream( 2000 );
                output = new TranscoderOutput( bos );
                // PNGTranscoder is needed to handle transparent parts
                // of a SVG
                trc = new PNGTranscoder();
                try {
                    input = new TranscoderInput( t );
                } catch ( Exception e ) {
                    e.printStackTrace();
                }
            } else {
                InputStream is = onlineResource.openStream();
                MemoryCacheSeekableStream mcss = new MemoryCacheSeekableStream( is );
                RenderedOp rop = JAI.create( "stream", mcss );
                image = rop.getAsBufferedImage();
                mcss.close();
                is.close();
            }
        } catch ( Exception e ) {
            LOG.logError( e.getMessage(), e );
        }
    }

    /**
     * returns the external graphic as an image. this method is not part of the sld specifications but it is added for
     * speed up applications
     * 
     * @param targetSizeX
     * @param targetSizeY
     * @param feature
     * 
     * @return the external graphic as BufferedImage
     */
    public BufferedImage getAsImage( int targetSizeX, int targetSizeY, Feature feature ) {

        if ( ( ( this.input == null ) && ( this.image == null ) ) || feature != null ) {
            URL onlineResource = initializeOnlineResource( feature );
            if ( onlineResource != null ) {
                LOG.logDebug( "image URL:", onlineResource );
                image = cache.get( onlineResource.getFile() );
                if ( image == null ) {
                    retrieveImage( onlineResource );
                    cache.put( onlineResource.getFile(), image );
                }
            } else {
                LOG.logDebug( "image URL can not be created return default image instead" );
                return defaultImage;
            }
        }

        if ( input != null ) {
            image = cache.get( targetSizeX + '_' + targetSizeY + '_' + onlineResource.getFile() );
            if ( image == null ) {
                if ( targetSizeX <= 0 ) {
                    targetSizeX = 1;
                }
                if ( targetSizeY <= 0 ) {
                    targetSizeY = 1;
                }

                trc.addTranscodingHint( SVGAbstractTranscoder.KEY_HEIGHT, new Float( targetSizeX ) );
                trc.addTranscodingHint( SVGAbstractTranscoder.KEY_WIDTH, new Float( targetSizeY ) );
                try {
                    trc.transcode( input, output );
                    bos.flush();
                    bos.close();
                    ByteArrayInputStream is = new ByteArrayInputStream( bos.toByteArray() );
                    MemoryCacheSeekableStream mcss = new MemoryCacheSeekableStream( is );
                    RenderedOp rop = JAI.create( "stream", mcss );
                    image = rop.getAsBufferedImage();
                    cache.put( targetSizeX + '_' + targetSizeY + '_' + onlineResource.getFile(), image );
                    mcss.close();
                } catch ( Throwable e ) {
                    LOG.logDebug( e.getMessage(), e );
                    LOG.logError( e.getMessage() );
                }
            }
        }
        if ( image == null ) {
            LOG.logDebug( "image URL can not be created return default image instead" );
            image = defaultImage;
        }

        return image;
    }

    /**
     * @param graphics
     * @param feature
     * @param height
     * @param width
     * @param y
     * @param x
     * @return null, if graphic was painted through vector rendering, else the image to be painted elsewhere (in
     *         {@link Graphic})
     */
    public BufferedImage paint( Graphics2D graphics, Feature feature, int x, int y, int width, int height,
                                double rotation ) {
        URL url = initializeOnlineResource( feature );

        if ( url.toExternalForm().toLowerCase().endsWith( ".svg" ) ) {

            try {
                SAXSVGDocumentFactory f = new SAXSVGDocumentFactory(
                                                                     "com.sun.org.apache.xerces.internal.parsers.SAXParser",
                                                                     false );

                SVGDocument document = (SVGDocument) f.createDocument( url.toExternalForm() );

                UserAgentAdapter adapter = new UserAgentAdapter();
                BridgeContext bridgeContext = new BridgeContext( adapter );

                GVTBuilder builder = new GVTBuilder();

                GraphicsNode node = builder.build( bridgeContext, document );
                Rectangle2D bounds = node.getGeometryBounds();

                AffineTransform t = graphics.getTransform();
                double aspect = bounds.getWidth() / bounds.getHeight();
                width = (int) Math.round( width * aspect );

                double w = bounds.getMaxX() - bounds.getMinX();
                double h = bounds.getMaxY() - bounds.getMinY();

                double scalex = width / bounds.getWidth();
                double scaley = height / bounds.getHeight();
                t.translate( x, y );
                AffineTransform orig = graphics.getTransform();

                AffineTransform nodeTransform = new AffineTransform();
                nodeTransform.scale( scalex, scaley );
                nodeTransform.rotate( Math.toRadians( rotation ) );
                nodeTransform.translate( -bounds.getMinX() - w / 2, -bounds.getMinY() - h / 2 );
                node.setTransform( nodeTransform );

                graphics.setTransform( t );

                node.paint( graphics );

                graphics.setTransform( orig );
            } catch ( Throwable e ) {
                LOG.logWarning( "Using raster rendering for svg symbol, because vector rendering failed: "
                                + e.getLocalizedMessage() );
                // try to render it the old way
                return getAsImage( width, height, feature );
            }
            return null;
        }
        return getAsImage( width, height, feature );
    }

    /**
     * @param feature
     * @return online resource URL
     */
    private URL initializeOnlineResource( Feature feature ) {

        String file = null;
        try {
            file = this.onlineResource.toURI().toASCIIString();
            LOG.logDebug( "external graphic pattern: ", file );
        } catch ( URISyntaxException e1 ) {
            e1.printStackTrace();
        }
        String[] tags = StringTools.extractStrings( file, "$", "$" );

        if ( tags != null ) {
            FeatureProperty[] properties = feature.getProperties();
            if ( properties != null ) {
                for ( int i = 0; i < tags.length; i++ ) {
                    String tag = tags[i].substring( 1, tags[i].length() - 1 );
                    for ( int j = 0; j < properties.length; j++ ) {
                        if ( properties[j] != null && properties[j].getValue() != null
                             && properties[j].getName().getLocalName().equals( tag ) ) {
                            String to = properties[j].getValue().toString();
                            file = StringTools.replace( file, tags[i], to, true );
                        }
                    }
                }
            }
        }
        URL onlineResource = null;
        try {
            onlineResource = new URL( file );
            LOG.logDebug( "external graphic URL: ", file );
        } catch ( MalformedURLException e ) {
            LOG.logError( e.getMessage(), e );
        }
        return onlineResource;
    }

    /**
     * sets the external graphic as an image.
     * 
     * @param image
     *            the external graphic as BufferedImage
     */
    public void setAsImage( BufferedImage image ) {
        this.image = image;
    }

    /**
     * exports the content of the ExternalGraphic as XML formated String
     * 
     * @return xml representation of the ExternalGraphic
     */
    @Override
    public String exportAsXML() {

        StringBuffer sb = new StringBuffer( 200 );
        sb.append( "<ExternalGraphic>" );
        sb.append( "<OnlineResource xmlns:xlink='http://www.w3.org/1999/xlink' " );
        if ( title != null ) {
            sb.append( "xlink:title='" ).append( title ).append( "' " );
        }
        sb.append( "xlink:type='simple' xlink:href='" );
        try {
            sb.append( onlineResource.toURI().toASCIIString() + "'/>" );
        } catch ( URISyntaxException e ) {
            e.printStackTrace();
        }
        sb.append( "<Format>" ).append( escape( format ) ).append( "</Format>" );
        sb.append( "</ExternalGraphic>" );
        return sb.toString();
    }

}
