//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
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
package org.deegree.filter.function.se;

import static java.lang.Double.parseDouble;
import static java.lang.Integer.toHexString;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import static org.deegree.commons.utils.math.MathUtils.round;
import static org.deegree.rendering.r2d.se.parser.SymbologyParser.updateOrContinue;
import static org.deegree.rendering.r2d.se.unevaluated.Continuation.SBUPDATER;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.deegree.coverage.raster.AbstractRaster;
import org.deegree.coverage.raster.data.RasterData;
import org.deegree.filter.MatchableObject;
import org.deegree.filter.expression.Function;
import org.deegree.rendering.r2d.se.unevaluated.Continuation;
import org.deegree.rendering.r2d.styling.RasterStyling;
import org.deegree.rendering.r2d.utils.RasterDataUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <code>Interpolate</code>
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author <a href="mailto:a.aiordachioaie@jacobs-university.de">Andrei Aiordachioaie</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class Interpolate extends Function {

    private static final Logger LOG = LoggerFactory.getLogger( Interpolate.class );

    private StringBuffer value;

    private Continuation<StringBuffer> contn;

    private LinkedList<Double> datas = new LinkedList<Double>();

    private Double[] dataArray;

    private LinkedList<StringBuffer> values = new LinkedList<StringBuffer>();

    private Double[] valuesArray;

    private Color[] colorArray;

    private LinkedList<Continuation<StringBuffer>> valueContns = new LinkedList<Continuation<StringBuffer>>();

    private boolean color = false;

    private boolean linear = true, cosine, cubic;

    private byte mode = 1; /* Values in range 1..3, for linear, cosine, cubic. Default is linear. */

    /***/
    public Interpolate() {
        super( "Interpolate", null );
    }

    /** Linear interpolation between two colors, with fraction f */
    private static final Color interpolateColorLinear( final Color fst, final Color snd, final double f ) {
        final double f1m = 1 - f;
        int red = (int) Math.round( fst.getRed() * f1m + snd.getRed() * f );
        int green = (int) Math.round( fst.getGreen() * f1m + snd.getGreen() * f );
        int blue = (int) Math.round( fst.getBlue() * f1m + snd.getBlue() * f );
        int alpha = (int) Math.round( fst.getAlpha() * f1m + snd.getAlpha() * f );
        return new Color( red, green, blue, alpha );
    }

    /* Linear interpolation between two numbers with fraction f */
    private static final double interpolateLinear( final double fst, final double snd, final double f ) {
        return fst * ( 1 - f ) + snd * f;
    }

    /*
     * Cubic interpolation between y1 and y2, with fraction f. y0 and y3 are extra points, such that y0-y1-y2-y3 are
     * ordered.
     */
    private static final double interpolateCubic( final double y0, final double y1, final double y2, final double y3,
                                                  final double f ) {
        double a0, a1, a2, a3, f2;

        f2 = f * f;
        a0 = y3 - y2 - y0 + y1;
        a1 = y0 - y1 - a0;
        a2 = y2 - y0;
        a3 = y1;

        return ( a0 * f * f2 + a1 * f2 + a2 * f + a3 );
    }

    private static final Color interpolateColorCosine( final Color fst, final Color snd, final double f ) {
        final double mu = ( 1 - Math.cos( f * Math.PI ) ) / 2;
        final double m1m = 1 - mu;
        int red = (int) ( fst.getRed() * m1m + snd.getRed() * mu );
        int green = (int) ( fst.getGreen() * m1m + snd.getGreen() * mu );
        int blue = (int) ( fst.getBlue() * m1m + snd.getBlue() * mu );
        int alpha = (int) ( fst.getAlpha() * m1m + snd.getAlpha() * mu );
        return new Color( red, green, blue, alpha );
    }

    private static final double interpolateCosine( final double fst, final double snd, final double f ) {
        final double mu = ( 1 - Math.cos( f * Math.PI ) ) / 2;
        return fst * ( 1 - mu ) + snd * mu;
    }

    private final Color interpolateColor( final int pos1, final int pos2, final double f ) {
        if ( color == false )
            return null;

        switch ( mode ) {
        case 1:
            return interpolateColorLinear( colorArray[pos1], colorArray[pos2], f );
        case 2:
            return interpolateColorCosine( colorArray[pos1], colorArray[pos2], f );
        case 3:
            // Cubic interpolation needs 4 points.
            // Create extra 2 points with the same slope on both sides of the input points
            double r,
            g,
            b,
            a;
            double r1 = colorArray[pos1].getRed(),
            g1 = colorArray[pos1].getGreen(),
            b1 = colorArray[pos1].getBlue(),
            a1 = colorArray[pos1].getAlpha();

            double r2 = colorArray[pos2].getRed(),
            g2 = colorArray[pos2].getGreen(),
            b2 = colorArray[pos2].getBlue(),
            a2 = colorArray[pos2].getAlpha();

            if ( pos1 == 0 || pos2 == colorArray.length - 1 ) {
                // Cubic interpolation needs 4 points, not just two. Interpolate for each of RGBA channels.
                double aux1, aux2;
                aux1 = interpolateLinear( r1, r2, -1 );
                aux2 = interpolateLinear( r1, r2, 2 );
                r = interpolateCubic( aux1, r1, r2, aux2, f );

                aux1 = interpolateLinear( g1, g2, -1 );
                aux2 = interpolateLinear( g1, g2, 2 );
                g = interpolateCubic( aux1, g1, g2, aux2, f );

                aux1 = interpolateLinear( b1, b2, -1 );
                aux2 = interpolateLinear( b1, b2, 2 );
                b = interpolateCubic( aux1, b1, b2, aux2, f );

                aux1 = interpolateLinear( a1, a2, -1 );
                aux2 = interpolateLinear( a1, a2, 2 );
                a = interpolateCubic( aux1, a1, a2, aux2, f );

                if ( color == true ) // value range is 0-255
                    return new Color( (int) r, (int) g, (int) b, (int) a );
                // value range is 0-1
                return new Color( (float) r, (float) g, (float) b, (float) a );
            }

            r = interpolateCubic( colorArray[pos1 - 1].getRed(), r1, r2, colorArray[pos2 + 1].getRed(), f );
            g = interpolateCubic( colorArray[pos1 - 1].getGreen(), g1, g2, colorArray[pos2 + 1].getGreen(), f );
            b = interpolateCubic( colorArray[pos1 - 1].getBlue(), b1, b2, colorArray[pos2 + 1].getBlue(), f );
            a = interpolateCubic( colorArray[pos1 - 1].getAlpha(), a1, a2, colorArray[pos2 + 1].getAlpha(), f );
            // Sometimes the cubic interpolation overshoots, so enforce 0-255 interval
            r = fixRange( r, 0, 255 );
            g = fixRange( g, 0, 255 );
            b = fixRange( b, 0, 255 );
            a = fixRange( a, 0, 255 );

            return new Color( (int) r, (int) g, (int) b, (int) a );

        default:
            LOG.error( "Invalid value for interpolation type: {}", mode );
            throw new RuntimeException( "Invalid value for interpolation type: " + mode );
        }

    }

    /* Adapt an input value <i>r</i> to an interval [<i>low</i>,<i>high</i>] */
    private double fixRange( double r, double low, double high ) {
        r = r < low ? low : r;
        r = r > high ? high : r;
        return r;
    }

    private final double interpolate( final int pos1, final int pos2, final double f ) {
        switch ( mode ) {
        case 1:
            return interpolateLinear( valuesArray[pos1], valuesArray[pos2], f );
        case 2:
            return interpolateCosine( valuesArray[pos1], valuesArray[pos2], f );
        case 3:
            if ( pos1 == 0 || pos2 == valuesArray.length - 1 ) {
                // Cubic interpolation needs 4 points, not just two.
                // If we are at the first/last intervals, create 2 new points by interpolating linearly, on both sides
                // of the segment to be interpolated.
                double aux1, aux2;
                aux1 = interpolateLinear( valuesArray[pos1], valuesArray[pos2], -1 );
                aux2 = interpolateLinear( valuesArray[pos1], valuesArray[pos2], 2 );
                return interpolateCubic( aux1, valuesArray[pos1], valuesArray[pos2], aux2, f );
            }

            return interpolateCubic( valuesArray[pos1 - 1], valuesArray[pos1], valuesArray[pos2],
                                     valuesArray[pos2 + 1], f );
        default:
            LOG.error( "Invalid value for interpolation type: {}", mode );
            throw new RuntimeException( "Invalid value for interpolation type: " + mode );
        }
    }

    @Override
    public Object[] evaluate( MatchableObject f ) {
        buildLookupArrays();

        StringBuffer sb = new StringBuffer( value.toString().trim() );
        if ( contn != null ) {
            contn.evaluate( sb, f );
        }

        double val = parseDouble( sb.toString() );

        Iterator<Double> data = datas.iterator();
        Iterator<StringBuffer> vals = values.iterator();
        Iterator<Continuation<StringBuffer>> contns = valueContns.iterator();

        double cur = data.next();
        int pos = 0;
        StringBuffer intVal = vals.next();
        Continuation<StringBuffer> contn = contns.next();

        while ( val > cur && data.hasNext() ) {
            cur = data.next();
            intVal = vals.next();
            contn = contns.next();
            pos++;
        }

        StringBuffer buf = new StringBuffer( intVal.toString().trim() );
        if ( contn != null ) {
            contn.evaluate( sb, f );
        }
        String fstString = buf.toString();

        if ( !data.hasNext() ) {
            return new Object[] { fstString };
        }

        buf = new StringBuffer( vals.next().toString().trim() );
        contn = contns.next();
        if ( contn != null ) {
            contn.evaluate( sb, f );
        }

        double next = data.next();
        double fac = ( val - cur ) / ( next - cur );

        if ( color ) {
            Color res = interpolateColor( pos - 1, pos, fac );
            return new Object[] { "#" + toHexString( res.getRGB() ) };
        }

        return new Object[] { "" + interpolate( pos - 1, pos, fac ) };
    }

    /**
     * @param in
     * @throws XMLStreamException
     */
    public void parse( XMLStreamReader in )
                            throws XMLStreamException {
        in.require( START_ELEMENT, null, "Interpolate" );

        LOG.trace( "Parsing SE XML document for Interpolate... " );
        String mode = in.getAttributeValue( null, "mode" );
        if ( mode != null ) {
            linear = mode.equals( "linear" );
            cosine = mode.equals( "cosine" );
            cubic = mode.equals( "cubic" );
            if ( linear )
                this.mode = 1;
            if ( cosine )
                this.mode = 2;
            if ( cubic )
                this.mode = 3;
        }

        String method = in.getAttributeValue( null, "method" );
        if ( method != null ) {
            color = method.equals( "color" );
        }

        while ( !( in.isEndElement() && in.getLocalName().equals( "Interpolate" ) ) ) {
            in.nextTag();

            if ( in.getLocalName().equals( "LookupValue" ) ) {
                value = new StringBuffer();
                contn = updateOrContinue( in, "LookupValue", value, SBUPDATER, null );
            }

            if ( in.getLocalName().equals( "InterpolationPoint" ) ) {
                while ( !( in.isEndElement() && in.getLocalName().equals( "InterpolationPoint" ) ) ) {
                    in.nextTag();

                    if ( in.getLocalName().equals( "Data" ) ) {
                        datas.add( Double.valueOf( in.getElementText() ) );
                    }

                    if ( in.getLocalName().equals( "Value" ) ) {
                        StringBuffer sb = new StringBuffer();
                        valueContns.add( updateOrContinue( in, "Value", sb, SBUPDATER, null ) );
                        values.add( sb );
                    }
                }
            }

        }

        in.require( END_ELEMENT, null, "Interpolate" );
    }

    /**
     * @param in
     * @throws XMLStreamException
     */
    public void parseSLD100( XMLStreamReader in )
                            throws XMLStreamException {
        color = true;

        while ( !( in.isEndElement() && in.getLocalName().equals( "ColorMap" ) ) ) {
            in.nextTag();

            if ( in.getLocalName().equals( "ColorMapEntry" ) ) {
                String color = in.getAttributeValue( null, "color" );
                String opacity = in.getAttributeValue( null, "opacity" );
                String quantity = in.getAttributeValue( null, "quantity" );
                datas.add( quantity != null ? Double.valueOf( quantity ) : 0 );
                if ( opacity != null ) {
                    color = "#" + toHexString( round( parseDouble( opacity ) * 255 ) ) + color.substring( 1 );
                }
                values.add( new StringBuffer( color ) );
                // for legend generation, later on?
                // String label= in.getAttributeValue(null, "label" );
                in.nextTag();
            }
        }
    }

    /** Create the sorted lookup arrays from the linked lists, so that we can perform binary search. */
    public void buildLookupArrays() {
        LOG.debug( "Building look-up arrays, for binary search... " );
        if ( color == true && colorArray == null ) {
            colorArray = new Color[values.size()];
            List<Color> list = new ArrayList<Color>( values.size() );
            Iterator<StringBuffer> i = values.iterator();
            while ( i.hasNext() ) {
                list.add( Color.decode( i.next().toString() ) );
            }
            colorArray = list.toArray( colorArray );
        }
        if ( color == false && valuesArray == null ) {
            valuesArray = new Double[values.size()];
            List<Double> list = new ArrayList<Double>( values.size() );
            Iterator<StringBuffer> i = values.iterator();
            while ( i.hasNext() ) {
                list.add( Double.parseDouble( i.next().toString() ) );
            }
            valuesArray = list.toArray( valuesArray );
        }

        if ( dataArray == null ) {
            dataArray = new Double[datas.size()];
            List<Double> list = new ArrayList<Double>( datas.size() );
            Iterator<Double> i = datas.iterator();
            while ( i.hasNext() )
                list.add( Double.parseDouble( i.next().toString() ) );
            dataArray = list.toArray( dataArray );
        }
    }

    /**
     * Construct an image map, as the result of the Interpolate operation
     * 
     * @param raster
     *            input raster
     * @param style
     *            raster style, containing channel mappings (if applicable)
     * @return a buffered image with the processed data
     */
    public BufferedImage evaluateRaster( AbstractRaster raster, RasterStyling style ) {
        BufferedImage img = null;
        long start = System.nanoTime();
        int col = -1, row = -1;
        int rgb = 0;
        RasterData data = raster.getAsSimpleRaster().getRasterData();

        buildLookupArrays();

        try {
            RasterDataUtility rawData = new RasterDataUtility( raster );

            img = new BufferedImage( data.getWidth(), data.getHeight(), BufferedImage.TYPE_INT_ARGB );
            LOG.debug( "Created image with H={}, L={}", img.getHeight(), img.getWidth() );
            for ( row = 0; row < img.getHeight(); row++ )
                for ( col = 0; col < img.getWidth(); col++ ) {
                    float val = rawData.get( col, row );
                    rgb = lookup2Color( val ).getRGB();
                    img.setRGB( col, row, rgb );
                }
        } catch ( Exception e ) {
            LOG.error( "Error while building image, @ row={}, col={}: " + e.getMessage(), row, col );
            // e.printStackTrace();
        } finally {
            long end = System.nanoTime();
            LOG.debug( "Built interpolated ColorMap with total time {} ms", ( end - start ) / 1000000 );
        }
        return img;
    }

    /**
     * Performs interpolation on a value, and returns a color built from the interpolated value. Uses binary search for
     * optimization.
     * 
     * @param value
     *            value
     * @return the corresponding Color
     */
    public Color lookup2Color( double value ) {
        Color color;

        int l = dataArray.length - 1;
        if ( value <= dataArray[0] || value >= dataArray[l] ) {
            if ( this.color == true ) {
                if ( value <= dataArray[0] )
                    return colorArray[0];
                if ( value >= dataArray[l] ) {
                    return colorArray[l];
                }
            } else if ( this.color == false ) {
                if ( value <= dataArray[0] ) {
                    int val = valuesArray[0].intValue();
                    return new Color( val, val, val );
                }
                if ( value >= dataArray[l] ) {
                    int val = valuesArray[0].intValue();
                    return new Color( val, val, val );
                }
            }
        }

        int pos = Arrays.binarySearch( dataArray, value );
        if ( pos < 0 ) {
            pos = pos * ( -1 ) - 1;
        }

        double f = ( value - dataArray[pos - 1] ) / ( dataArray[pos] - dataArray[pos - 1] );
        if ( this.color == true )
            color = interpolateColor( pos - 1, pos, f );
        else {
            double d = interpolate( pos - 1, pos, f );
            int val = (int) fixRange( d, 0, 255 );
            color = new Color( val, val, val );
        }
        return color;
    }

    /**
     * Looks up a value in the current data values. Uses binary search for optimization.
     * 
     * @param value
     * @return the interpolated value
     */
    public double lookup2( double value ) {
        int l = dataArray.length - 1;
        if ( value <= dataArray[0] || value >= dataArray[l] ) {
            if ( this.color == true ) {
                if ( value <= dataArray[0] )
                    return colorArray[0].getRGB();
                if ( value >= dataArray[l] ) {
                    return colorArray[l].getRGB();
                }
            } else if ( this.color == false ) {
                if ( value <= dataArray[0] ) {
                    return valuesArray[0];
                }
                if ( value >= dataArray[l] ) {
                    return valuesArray[0];
                }
            }
        }

        int pos = Arrays.binarySearch( dataArray, value );
        if ( pos < 0 ) {
            pos = pos * ( -1 ) - 1;
        }

        double f = ( value - dataArray[pos - 1] ) / ( dataArray[pos] - dataArray[pos - 1] );
        double val = interpolate( pos - 1, pos, f );
        return val;
    }

    @Override
    public String toString() {
        String r = "\nCategorize [ ";
        r += "\nDatas: " + datas.toString();
        r += "\nValues: " + values.toString();
        if ( dataArray != null )
            r += "\nData Array: " + printArray( dataArray );
        if ( valuesArray != null )
            r += "\nValues Array: " + printArray( valuesArray );
        if ( colorArray != null )
            r += "\nColor Array: " + printArray( colorArray );
        r += "\n Color mode: " + color;
        r += "\n Interpolation type: " + mode + " (1=linear, 2=cosine, 3=cubic)";
        r += "\n ]";
        return r;
    }

    /**
     * @param a
     * @return a string
     */
    public String printArray( Object[] a ) {
        String result = a[0].toString();
        for ( int i = 1; i < a.length; i++ )
            result += ", " + a[i].toString();
        result = "{" + result + "}";
        return result;
    }
}
