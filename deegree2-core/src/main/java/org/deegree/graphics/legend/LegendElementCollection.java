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
package org.deegree.graphics.legend;

import static org.deegree.framework.log.LoggerFactory.getLogger;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import org.deegree.framework.log.ILogger;

/**
 * <tt>LegendElementCollection</tt> is a collection of <tt>LegendElement</tt>s and is a <tt>LegendElement</tt>
 * too. It can be used to group elements or to create more complex elements.
 * <p>
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @version $Revision$ $Date$
 */
public class LegendElementCollection extends LegendElement {

    private static final ILogger LOG = getLogger( LegendElementCollection.class );

    ArrayList<LegendElement> collection;

    /**
     *
     */
    String title = "";

    /**
     * empty constructor
     *
     */
    public LegendElementCollection() {
        this.collection = new ArrayList<LegendElement>();
    }

    /**
     * @param title
     */
    public LegendElementCollection( String title ) {
        super();
        this.collection = new ArrayList<LegendElement>();
        this.title = title;
    }

    /**
     * adds a <tt>LegendElement</tt> to the collection.
     *
     * @param legendElement
     *            to add
     */
    public void addLegendElement( LegendElement legendElement ) {
        this.collection.add( legendElement );
    }

    /**
     *
     * @return all legend elements
     */
    public LegendElement[] getLegendElements() {
        return collection.toArray( new LegendElement[collection.size()] );
    }

    /**
     * sets the title of the <tt>LegendElement</tt>. The title will be displayed on top of the
     * <tt>LegendElementCollection</tt>
     *
     * @param title
     *            title of the <tt>LegendElement</tt>
     *
     */
    public void setTitle( String title ) {
        this.title = title;
    }

    /**
     * returns the title of the <tt>LegendElement</tt>
     *
     * @return the title of the <tt>LegendElement</tt>
     *
     */
    public String getTitle() {
        return title;
    }

    /**
     * returns the number of legend elements as an int
     *
     * @return number of legend elements
     */
    public int getSize() {
        return this.collection.size();
    }

    /**
     * @param mime
     *            the mime type of the requested legend (like image/png, etc)
     *
     * @return the legend as image
     */
    @Override
    public BufferedImage exportAsImage( String mime )
                            throws LegendException {

        int[] titleFontMetrics;
        int titleheight = 0; // height of the title (default: 0, none)

        int maxheight = 0; // maximum width of resulting Image
        int maxwidth = 0; // maximum height of resulting Image
        int buffer = 10; // bufferspace between LegendElements and Title (eventually)

        LegendElement[] le = getLegendElements();
        BufferedImage[] imagearray = new BufferedImage[le.length];
        BufferedImage bi = null;
        int imageType = 0;
        if ( mime.equalsIgnoreCase( "image/gif" ) || mime.equalsIgnoreCase( "image/png" ) ) {
            imageType = BufferedImage.TYPE_INT_ARGB;
        } else {
            imageType = BufferedImage.TYPE_INT_RGB;
        }

        for ( int i = 0; i < le.length; i++ ) {
            imagearray[i] = le[i].exportAsImage( mime );
            maxheight += ( imagearray[i].getHeight() + buffer );
            if ( maxwidth < imagearray[i].getWidth() ) {
                maxwidth = imagearray[i].getWidth();
            }
        }

        // printing the title (or not)
        Graphics g = null;
        if ( getTitle() != null && getTitle().length() > 0 ) {
            titleFontMetrics = calculateFontMetrics( getTitle() );
            titleheight = titleFontMetrics[1] + titleFontMetrics[2];
            maxheight += titleheight;

            // is title wider than the maxwidth?
            if ( maxwidth <= titleFontMetrics[0] ) {
                maxwidth = titleFontMetrics[0];
            }

            bi = new BufferedImage( maxwidth, maxheight, imageType );
            g = bi.getGraphics();
            if ( imageType == BufferedImage.TYPE_INT_RGB ) {
                g.setColor( java.awt.Color.WHITE );
                g.fillRect( 0, 0, bi.getWidth(), bi.getHeight() );
            }
            g.setColor( java.awt.Color.BLACK );
            g.drawString( getTitle(), 0, 0 + titleheight );
        } else {
            bi = new BufferedImage( maxwidth, maxheight, imageType );
            g = bi.getGraphics();
            if ( imageType == BufferedImage.TYPE_INT_RGB ) {
                g.setColor( java.awt.Color.WHITE );
                g.fillRect( 0, 0, bi.getWidth(), bi.getHeight() );
            }
        }

        int offsety = titleheight + buffer;
        for ( int j = 0; j < imagearray.length; j++ ) {
            g.drawImage( imagearray[j], 0, offsety, null );
            offsety += imagearray[j].getHeight() + buffer;
            if ( LOG.isDebug() ) {
                LOG.logDebug( "LegendElementCollection.exportAsImage: j=" + j + ", imagearray[j].getHeight()="
                              + imagearray[j].getHeight() );
            }
        }

        return bi;
    }
}
