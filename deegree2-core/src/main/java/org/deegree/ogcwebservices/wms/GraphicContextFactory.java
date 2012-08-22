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
package org.deegree.ogcwebservices.wms;

import static java.awt.image.DataBuffer.TYPE_BYTE;
import static java.awt.image.Raster.createBandedRaster;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;

import javax.media.jai.PlanarImage;

import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

/**
 *
 *
 * @version $Revision$
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 */
public class GraphicContextFactory {

    /**
     * creates a graphic target object for the passed mime type. The target will be the object where to render the to. A
     * <tt>BufferedImage</tt> for raster image mime types and a DOM <tt>Document</tt> for SVG.
     *
     * @param mimeType
     *            mime type to create a object for
     * @param width
     *            width of the desired target object
     * @param height
     *            height of the desired target object
     *
     * @return object to render to
     */
    public static synchronized Object createGraphicTarget( String mimeType, int width, int height ) {
        // to avoid errors:
        mimeType = mimeType.toLowerCase();

        Object o = null;
        if ( mimeType.equals( "image/jpg" ) || mimeType.equals( "image/jpeg" ) || mimeType.equals( "image/bmp" )
             || mimeType.equals( "image/tif" ) || mimeType.equals( "image/tiff" ) ) {
            o = new BufferedImage( width, height, BufferedImage.TYPE_INT_RGB );
        } else if ( mimeType.equals( "image/gif" ) || mimeType.equals( "image/png" )
                    || mimeType.equals( "image/png; mode=24bit" ) ) {
            o = new BufferedImage( width, height, BufferedImage.TYPE_INT_ARGB );
        } else if ( mimeType.equals( "image/svg+xml" ) || mimeType.equals( "image/svg xml" ) ) {
            // Get a DOMImplementation
            DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();

            // Create an instance of org.w3c.dom.Document
            o = domImpl.createDocument( null, "svg", null );
        } else if ( mimeType.equals( "image/png; mode=8bit" ) ) {
            ColorModel cm = PlanarImage.getDefaultColorModel( TYPE_BYTE, 4 );
            o = new BufferedImage( cm, createBandedRaster( TYPE_BYTE, width, height, 4, null ), false, null );
        }

        return o;
    }

    /**
     * creates a graphic context for the passed target considering the passed mime type
     *
     * @param mimeType
     *            mime type of the graphic (target)
     * @param target
     *            object to render to.
     *
     * @return graphic context of the target
     */
    public static synchronized Graphics createGraphicContext( String mimeType, Object target ) {
        // to avoid errors:
        mimeType = mimeType.toLowerCase();

        Graphics g = null;

        if ( mimeType.equals( "image/jpg" ) || mimeType.equals( "image/jpeg" ) || mimeType.equals( "image/bmp" )
             || mimeType.equals( "image/tif" ) || mimeType.equals( "image/tiff" ) || mimeType.equals( "image/gif" )
             || mimeType.equals( "image/png" ) || mimeType.equals( "image/png; mode=8bit" )
             || mimeType.equals( "image/png; mode=24bit" ) ) {
            g = ( (BufferedImage) target ).getGraphics();
        } else if ( mimeType.equals( "image/svg+xml" ) || mimeType.equals( "image/svg xml" ) ) {
            // Create an instance of the SVG Generator
            g = new SVGGraphics2D( (Document) target );
        }

        return g;
    }
}
