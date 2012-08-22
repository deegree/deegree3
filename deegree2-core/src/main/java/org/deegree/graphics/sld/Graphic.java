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

import static java.lang.Math.toRadians;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.xml.Marshallable;
import org.deegree.graphics.transformation.GeoTransform;
import org.deegree.model.feature.Feature;
import org.deegree.model.filterencoding.FilterEvaluationException;
import org.deegree.model.spatialschema.MultiPoint;
import org.deegree.model.spatialschema.Point;

/**
 * A Graphic is a "graphic symbol" with an inherent shape, color, and size. Graphics can either be referenced from an
 * external URL in a common format (such as GIF or SVG) or may be derived from a Mark. Multiple external URLs may be
 * referenced with the semantic that they all provide the same graphic in different formats. The "hot spot" to use for
 * rendering at a point or the start and finish handle points to use for rendering a graphic along a line must either be
 * inherent in the external format or are system- dependent. The default size of an image format (such as GIF) is the
 * inherent size of the image. The default size of a format without an inherent size is 16 pixels in height and the
 * corresponding aspect in width. If a size is specified, the height of the graphic will be scaled to that size and the
 * corresponding aspect will be used for the width. The default if neither an ExternalURL nor a Mark is specified is to
 * use the default Mark with a size of 6 pixels. The size is in pixels and the rotation is in degrees clockwise, with 0
 * (default) meaning no rotation. In the case that a Graphic is derived from a font-glyph Mark, the Size specified here
 * will be used for the final rendering. Allowed CssParameters are "opacity", "size", and "rotation".
 * 
 * 
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class Graphic implements Marshallable {

    private static final ILogger LOG = LoggerFactory.getLogger( Graphic.class );

    // default values
    /**
     * The default Opacity = 1;
     */
    public static final double OPACITY_DEFAULT = 1.0;

    /**
     * The default size is -1
     */
    public static final double SIZE_DEFAULT = -1;

    /**
     * The default rotation is 0
     */
    public static final double ROTATION_DEFAULT = 0.0;

    private List<Object> marksAndExtGraphics = new ArrayList<Object>();

    private BufferedImage image;

    private ParameterValueType opacity;

    private ParameterValueType rotation;

    private ParameterValueType size = null;

    private ParameterValueType[] displacement;

    /**
     * Creates a new <code>Graphic</code> instance.
     * <p>
     * 
     * @param marksAndExtGraphics
     *            the image will be based upon these
     * @param opacity
     *            opacity that the resulting image will have
     * @param size
     *            image height will be scaled to this value, respecting the proportions
     * @param rotation
     *            image will be rotated clockwise for positive values, negative values result in anti-clockwise rotation
     */
    public Graphic( Object[] marksAndExtGraphics, ParameterValueType opacity, ParameterValueType size,
                    ParameterValueType rotation ) {
        setMarksAndExtGraphics( marksAndExtGraphics );
        this.opacity = opacity;
        this.size = size;
        this.rotation = rotation;
    }

    /**
     * Creates a new <tt>Graphic</tt> instance.
     * <p>
     * 
     * @param marksAndExtGraphics
     *            the image will be based upon these
     * @param opacity
     *            opacity that the resulting image will have
     * @param size
     *            image height will be scaled to this value, respecting the proportions
     * @param rotation
     *            image will be rotated clockwise for positive values, negative values result in anti-clockwise rotation
     * @param displacement
     */
    public Graphic( Object[] marksAndExtGraphics, ParameterValueType opacity, ParameterValueType size,
                    ParameterValueType rotation, ParameterValueType[] displacement ) {
        setMarksAndExtGraphics( marksAndExtGraphics );
        this.opacity = opacity;
        this.size = size;
        this.rotation = rotation;
        this.displacement = displacement;
    }

    /**
     * Creates a new <tt>Graphic</tt> instance based on the default <tt>Mark</tt>: a square.
     * <p>
     * 
     * @param opacity
     *            opacity that the resulting image will have
     * @param size
     *            image height will be scaled to this value, respecting the proportions
     * @param rotation
     *            image will be rotated clockwise for positive values, negative values result in anti-clockwise rotation
     */
    protected Graphic( ParameterValueType opacity, ParameterValueType size, ParameterValueType rotation ) {
        Mark[] marks = new Mark[1];
        marks[0] = new Mark( "square", null, null );
        setMarksAndExtGraphics( marks );
        this.opacity = opacity;
        this.size = size;
        this.rotation = rotation;
    }

    /**
     * returns the ParameterValueType representation of opacity
     * 
     * @return the ParameterValueType representation of opacity
     */
    public ParameterValueType getOpacity() {
        return opacity;
    }

    /**
     * returns the ParameterValueType representation of rotation
     * 
     * @return the ParameterValueType representation of rotation
     */
    public ParameterValueType getRotation() {
        return rotation;
    }

    /**
     * returns the ParameterValueType representation of rotation
     * 
     * @return the ParameterValueType representation of rotation
     */
    public ParameterValueType[] getDisplacement() {
        return displacement;
    }

    /**
     * returns the ParameterValueType representation of size
     * 
     * @return the ParameterValueType representation of size
     */
    public ParameterValueType getSize() {
        return size;
    }

    /**
     * Creates a new <tt>Graphic</tt> instance based on the default <tt>Mark</tt>: a square.
     */
    protected Graphic() {
        this( null, null, null );
    }

    /**
     * Returns an object-array that enables the access to the stored <tt>ExternalGraphic</tt> and <tt>Mark</tt>
     * -instances.
     * <p>
     * 
     * @return contains <tt>ExternalGraphic</tt> and <tt>Mark</tt> -objects
     * 
     */
    public Object[] getMarksAndExtGraphics() {
        Object[] objects = new Object[marksAndExtGraphics.size()];
        return marksAndExtGraphics.toArray( objects );
    }

    /**
     * Sets the <tt>ExternalGraphic</tt>/ <tt>Mark<tt>-instances that the image
     * will be based on.
     * <p>
     * 
     * @param object
     *            to be used as basis for the resulting image
     */
    public void setMarksAndExtGraphics( Object[] object ) {
        image = null;
        this.marksAndExtGraphics.clear();

        if ( object != null ) {
            for ( int i = 0; i < object.length; i++ ) {
                marksAndExtGraphics.add( object[i] );
            }
        }
    }

    /**
     * Adds an Object to an object-array that enables the access to the stored <tt>ExternalGraphic</tt> and
     * <tt>Mark</tt> -instances.
     * <p>
     * 
     * @param object
     *            to be used as basis for the resulting image
     */
    public void addMarksAndExtGraphic( Object object ) {
        marksAndExtGraphics.add( object );
    }

    /**
     * Removes an Object from an object-array that enables the access to the stored <tt>ExternalGraphic</tt> and
     * <tt>Mark</tt> -instances.
     * <p>
     * 
     * @param object
     *            to be used as basis for the resulting image
     */
    public void removeMarksAndExtGraphic( Object object ) {
        marksAndExtGraphics.remove( marksAndExtGraphics.indexOf( object ) );
    }

    /**
     * The Opacity element gives the opacity to use for rendering the graphic.
     * <p>
     * 
     * @param feature
     *            specifies the <tt>Feature</tt> to be used for evaluation of the underlying 'sld:ParameterValueType'
     * @return the (evaluated) value of the parameter
     * @throws FilterEvaluationException
     *             if the evaluation fails or the value is invalid
     */
    public double getOpacity( Feature feature )
                            throws FilterEvaluationException {
        double opacityVal = OPACITY_DEFAULT;

        if ( opacity != null ) {
            String value = opacity.evaluate( feature );

            try {
                opacityVal = Double.parseDouble( value );
            } catch ( NumberFormatException e ) {
                throw new FilterEvaluationException( "Given value for parameter 'opacity' ('" + value
                                                     + "') has invalid format!" );
            }

            if ( ( opacityVal < 0.0 ) || ( opacityVal > 1.0 ) ) {
                throw new FilterEvaluationException( "Value for parameter 'opacity' (given: '" + value
                                                     + "') must be between 0.0 and 1.0!" );
            }
        }

        return opacityVal;
    }

    /**
     * The Opacity element gives the opacity of to use for rendering the graphic.
     * <p>
     * 
     * @param opacity
     *            Opacity to be set for the graphic
     */
    public void setOpacity( double opacity ) {
        ParameterValueType pvt = null;
        pvt = StyleFactory.createParameterValueType( "" + opacity );
        this.opacity = pvt;
    }

    /**
     * The Size element gives the absolute size of the graphic in pixels encoded as a floating-point number. This
     * element is also used in other contexts than graphic size and pixel units are still used even for font size. The
     * default size for an object is context-dependent. Negative values are not allowed.
     * <p>
     * 
     * @param feature
     *            specifies the <tt>Feature</tt> to be used for evaluation of the underlying 'sld:ParameterValueType'
     * @return the (evaluated) value of the parameter
     * @throws FilterEvaluationException
     *             if the evaluation fails or the value is invalid
     */
    public double getSize( Feature feature )
                            throws FilterEvaluationException {
        double sizeVal = SIZE_DEFAULT;

        if ( size != null ) {
            String value = size.evaluate( feature );

            try {
                sizeVal = Double.parseDouble( value );
            } catch ( NumberFormatException e ) {
                throw new FilterEvaluationException( "Given value for parameter 'size' ('" + value
                                                     + "') has invalid format!" );
            }

            if ( sizeVal <= 0.0 ) {
                throw new FilterEvaluationException( "Value for parameter 'size' (given: '" + value
                                                     + "') must be greater than 0!" );
            }
        }

        return sizeVal;
    }

    /**
     * @see org.deegree.graphics.sld.Graphic#getSize(Feature) <p>
     * @param size
     *            size to be set for the graphic
     */
    public void setSize( double size ) {
        ParameterValueType pvt = null;
        pvt = StyleFactory.createParameterValueType( "" + size );
        this.size = pvt;
    }

    /**
     * @see org.deegree.graphics.sld.Graphic#getSize(Feature) <p>
     * @param size
     *            size as ParameterValueType to be set for the graphic
     */
    public void setSize( ParameterValueType size ) {
        this.size = size;
    }

    /**
     * The Rotation element gives the rotation of a graphic in the clockwise direction about its center point in radian,
     * encoded as a floating- point number. Negative values mean counter-clockwise rotation. The default value is 0.0
     * (no rotation).
     * <p>
     * 
     * @param feature
     *            specifies the <tt>Feature</tt> to be used for evaluation of the underlying 'sld:ParameterValueType'
     * @return the (evaluated) value of the parameter
     * @throws FilterEvaluationException
     *             if the evaluation fails or the value is invalid
     */
    public double getRotation( Feature feature )
                            throws FilterEvaluationException {
        double rotVal = ROTATION_DEFAULT;

        if ( rotation != null ) {
            String value = rotation.evaluate( feature );

            try {
                rotVal = Double.parseDouble( value );
            } catch ( NumberFormatException e ) {
                LOG.logError( e.getMessage(), e );
                throw new FilterEvaluationException( "Given value for parameter 'rotation' ('" + value
                                                     + "') has invalid format!" );
            }
        }

        return rotVal;
    }

    /**
     * The Displacement element of a PointPlacement gives the X and Y displacements from the main-geometry point to
     * render a text label.
     * <p>
     * </p>
     * This will often be used to avoid over-plotting a graphic symbol marking a city or some such feature. The
     * displacements are in units of pixels above and to the right of the point. A system may reflect this displacement
     * about the X and/or Y axes to de-conflict labels. The default displacement is X=0, Y=0.
     * <p>
     * 
     * @param feature
     *            specifies the <tt>Feature</tt> to be used for evaluation of the underlying 'sld:ParameterValueType'
     * @return 2 double values: x ([0]) and y ([0])
     * @throws FilterEvaluationException
     *             if the evaluation fails*
     */
    public double[] getDisplacement( Feature feature )
                            throws FilterEvaluationException {
        double[] displacementVal = { 0.0, 0.0 };
        if ( displacement != null ) {
            displacementVal[0] = Double.parseDouble( displacement[0].evaluate( feature ) );
            displacementVal[1] = Double.parseDouble( displacement[1].evaluate( feature ) );
        }

        return displacementVal;
    }

    /**
     * @see org.deegree.graphics.sld.Graphic#getRotation(Feature) <p>
     * @param rotation
     *            rotation to be set for the graphic
     */
    public void setRotation( double rotation ) {
        ParameterValueType pvt = null;
        pvt = StyleFactory.createParameterValueType( "" + rotation );
        this.rotation = pvt;
    }

    /**
     * 
     * @param rotation
     *            rotation to be set for the graphic
     */
    public void setRotation( ParameterValueType rotation ) {
        this.rotation = rotation;
    }

    /**
     * @see PointPlacement#getDisplacement(Feature) <p>
     * @param displacement
     */
    public void setDisplacement( double[] displacement ) {
        ParameterValueType pvt = null;
        ParameterValueType[] pvtArray = new ParameterValueType[displacement.length];
        for ( int i = 0; i < displacement.length; i++ ) {
            pvt = StyleFactory.createParameterValueType( "" + displacement[i] );
            pvtArray[i] = pvt;
        }
        this.displacement = pvtArray;
    }

    private BufferedImage drawSinglePoint( GeoTransform transform, Point p, Feature feature, Graphics2D graphics,
                                           ExternalGraphic ext, int size, double[] dis, double rotation )
                            throws FilterEvaluationException {
        // TODO also consider size of symbol
        int x = (int) Math.round( transform.getDestX( p.getX() ) + 0.5 + dis[0] );
        int y = (int) Math.round( transform.getDestY( p.getY() ) + 0.5 + dis[1] );
        BufferedImage img = ext.paint( graphics, feature, x, y, size, size, rotation );
        if ( img != null ) {
            // fallback if svg rendering failed at some point
            return getAsImage( feature );
        }
        return null;
    }

    public BufferedImage getAsImage( Feature feature, GeoTransform transform,
                                     org.deegree.model.spatialschema.Geometry geometry, Graphics2D graphics,
                                     double[] dis )
                            throws FilterEvaluationException {
        int size = (int) getSize( feature );
        double rotation = getRotation( feature );
        for ( int i = 0; i < marksAndExtGraphics.size(); i++ ) {
            Object o = marksAndExtGraphics.get( i );
            if ( o instanceof ExternalGraphic ) {
                if ( geometry instanceof Point ) {
                    Point p = (Point) geometry;
                    BufferedImage img = drawSinglePoint( transform, p, feature, graphics, (ExternalGraphic) o, size,
                                                         dis, rotation );
                    if ( img != null ) {
                        // fallback if svg rendering failed at some point
                        return img;
                    }
                } else {
                    if ( geometry instanceof MultiPoint ) {
                        MultiPoint mp = (MultiPoint) geometry;
                        for ( Point p : mp.getAllPoints() ) {
                            BufferedImage img = drawSinglePoint( transform, p, feature, graphics, (ExternalGraphic) o,
                                                                 size, dis, rotation );
                            if ( img != null ) {
                                // fallback if svg rendering failed at some point
                                return img;
                            }
                        }
                    }
                }
                break;
            }
            return getAsImage( feature );
        }
        return null;
    }

    /**
     * Returns a <tt>BufferedImage</tt> representing this object. The image respects the 'Opacity', 'Size' and
     * 'Rotation' parameters. If the 'Size'-parameter is omitted, the height of the first <tt>ExternalGraphic</tt> is
     * used. If there is none, the default value of 6 pixels is used.
     * <p>
     * 
     * @param feature
     * 
     * @return the <tt>BufferedImage</tt> ready to be painted
     * @throws FilterEvaluationException
     *             if the evaluation fails
     */
    public BufferedImage getAsImage( Feature feature )
                            throws FilterEvaluationException {
        int intSizeX = (int) getSize( feature );
        int intSizeY = intSizeX;

        // calculate the size of the first ExternalGraphic
        int intSizeImgX = -1;
        int intSizeImgY = -1;
        for ( int i = 0; i < marksAndExtGraphics.size(); i++ ) {
            Object o = marksAndExtGraphics.get( i );
            if ( o instanceof ExternalGraphic ) {
                BufferedImage extImage = ( (ExternalGraphic) o ).getAsImage( intSizeX, intSizeY, feature );
                intSizeImgX = extImage.getWidth();
                intSizeImgY = extImage.getHeight();
                break;
            }
        }

        if ( intSizeX < 0 ) {
            // if size is unspecified
            if ( intSizeImgX < 0 ) {
                // if there are no ExternalGraphics, use default value of 6 pixels
                intSizeX = 6;
                intSizeY = 6;
            } else {
                // if there are ExternalGraphics, use width and height of the first
                intSizeX = intSizeImgX;
                intSizeY = intSizeImgY;
            }
        } else {
            // if size is specified
            if ( intSizeImgY < 0 ) {
                // if there are no ExternalGraphics, use default intSizeX
                intSizeY = intSizeX;
            } else {
                // if there are ExternalGraphics, use the first to find the height
                intSizeY = (int) Math.round( ( ( (double) intSizeImgY ) / ( (double) intSizeImgX ) ) * intSizeX );
            }
        }

        if ( intSizeX <= 0 || intSizeY <= 0 ) {
            // if there are no ExternalGraphics, use default value of 1 pixel
            LOG.logDebug( intSizeX + " - " + intSizeY );
            intSizeX = 6;
            intSizeY = 6;
        }

        double r = getRotation( feature );
        int sX = intSizeX;
        int sY = intSizeY;
        if ( r != 0 ) {
            if ( sX > sY ) {
                sX = (int) Math.ceil( 2 * sX / Math.sqrt( 2 ) );
            } else {
                sX = (int) Math.ceil( 2 * sY / Math.sqrt( 2 ) );
            }
            sY = sX;
        }
        image = new BufferedImage( sX, sY, BufferedImage.TYPE_INT_ARGB );

        Graphics2D g = (Graphics2D) image.getGraphics();

        for ( int i = 0; i < marksAndExtGraphics.size(); i++ ) {
            Object o = marksAndExtGraphics.get( i );
            BufferedImage extImage = null;

            if ( o instanceof ExternalGraphic ) {
                extImage = ( (ExternalGraphic) o ).getAsImage( intSizeX, intSizeY, feature );
            } else {
                extImage = ( (Mark) o ).getAsImage( feature, sX );
                intSizeImgX = extImage.getWidth();
                intSizeImgY = extImage.getHeight();
            }

            if ( intSizeImgX > 0 ) {
                double scale = intSizeImgX > intSizeImgY ? ( (double) intSizeX / intSizeImgX )
                                                        : ( (double) intSizeY / (double) intSizeImgY );
                g.scale( scale, scale );
                // in the intSizeImgX/Y-Coordinatesystem

                // rotation around the center of the image. center in intSizeImg-coordinates is sX (already scaled size)
                // / 2 =>
                // sX / scale
                g.rotate( toRadians( r ), ( sX / scale ) / 2, ( sY / scale ) / 2 );

                // same here: translation from center of rotation, so that image starts at 0, 0:
                g.translate( ( sX / scale ) / 2 - intSizeImgX / 2, ( sY / scale ) / 2 - intSizeImgY / 2 );
            }

            g.drawImage( extImage, 0, 0, null );
        }

        // use the default Mark if there are no Marks / ExternalGraphics
        // specified at all
        if ( marksAndExtGraphics.size() == 0 ) {
            Mark mark = new Mark();
            BufferedImage extImage = mark.getAsImage( feature, intSizeX );
            g.drawImage( extImage, sX / 2 - intSizeX / 2, sY / 2 - intSizeY / 2, intSizeX, intSizeY, null );
        }

        g.dispose();
        return image;
    }

    /**
     * Sets a <tt>BufferedImage</tt> representing this object. The image respects the 'Opacity', 'Size' and 'Rotation'
     * parameters.
     * <p>
     * 
     * @param bufferedImage
     *            BufferedImage to be set
     */
    public void setAsImage( BufferedImage bufferedImage ) {
        image = bufferedImage;
    }

    /**
     * exports the content of the Graphic as XML formated String
     * 
     * @return xml representation of the Graphic
     */
    public String exportAsXML() {

        StringBuffer sb = new StringBuffer( 1000 );
        sb.append( "<Graphic>" );
        for ( int i = 0; i < marksAndExtGraphics.size(); i++ ) {
            sb.append( ( (Marshallable) marksAndExtGraphics.get( i ) ).exportAsXML() );
        }
        if ( opacity != null ) {
            sb.append( "<Opacity>" );
            sb.append( ( (Marshallable) opacity ).exportAsXML() );
            sb.append( "</Opacity>" );
        }
        if ( size != null ) {
            sb.append( "<Size>" );
            sb.append( ( (Marshallable) size ).exportAsXML() );
            sb.append( "</Size>" );
        }
        if ( rotation != null ) {
            sb.append( "<Rotation>" );
            sb.append( ( (Marshallable) rotation ).exportAsXML() );
            sb.append( "</Rotation>" );
        }
        if ( displacement != null && displacement.length > 1 ) {
            sb.append( "<Displacement>" ).append( "<DisplacementX>" );
            sb.append( ( (Marshallable) displacement[0] ).exportAsXML() );
            sb.append( "</DisplacementX>" ).append( "<DisplacementY>" );
            sb.append( ( (Marshallable) displacement[1] ).exportAsXML() );
            sb.append( "</DisplacementY>" ).append( "</Displacement>" );
        }
        sb.append( "</Graphic>" );

        return sb.toString();
    }

}
