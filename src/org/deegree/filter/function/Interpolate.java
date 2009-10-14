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
package org.deegree.filter.function;

import static java.awt.Color.decode;
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
import org.deegree.rendering.r2d.utils.Raster2RawData;
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

    private static byte mode = 1; /* Values in range 1..3, for linear, cosine, cubic */

    /***/
    public Interpolate() {
        super( "Interpolate", null );
    }

    private static final Color interpolateColorLinear( final Color fst, final Color snd, final double f ) {
        final double f1m = 1 - f;
        int red = (int) ( fst.getRed() * f1m + snd.getRed() * f );
        int green = (int) ( fst.getGreen() * f1m + snd.getGreen() * f );
        int blue = (int) ( fst.getBlue() * f1m + snd.getBlue() * f );
        int alpha = (int) ( fst.getAlpha() * f1m + snd.getAlpha() * f );
        return new Color( red, green, blue, alpha );
    }

    private static final double interpolateLinear( final double fst, final double snd, final double f ) {
        return fst * ( 1 - f ) + snd * f;
    }

    private static final Color interpolateColorCubic( final Color fst, final Color snd, final double f ) {
        // TODO: fix computation
        final double f1m = 1 - f;
        int red = (int) ( fst.getRed() * f1m + snd.getRed() * f );
        int green = (int) ( fst.getGreen() * f1m + snd.getGreen() * f );
        int blue = (int) ( fst.getBlue() * f1m + snd.getBlue() * f );
        int alpha = (int) ( fst.getAlpha() * f1m + snd.getAlpha() * f );
        return new Color( red, green, blue, alpha );
    }

    private static final double interpolateCubic( final double fst, final double snd, final double f ) {
        // TODO: fix formula
        return fst * ( 1 - f ) + snd * f;
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

    private static final Color interpolateColor( final Color fst, final Color snd, final double f ) {
        switch ( mode ) {
        case 1:
            return interpolateColorLinear( fst, snd, f );
        case 2:
            return interpolateColorCosine( fst, snd, f );
        case 3:
            return interpolateColorCubic( fst, snd, f );
        }
        return null;
    }

    private static final double interpolate( final double fst, final double snd, final double f ) {
        switch ( mode ) {
        case 1:
            return interpolateLinear( fst, snd, f );
        case 2:
            return interpolateCosine( fst, snd, f );
        case 3:
            return interpolateCubic( fst, snd, f );
        }
        return 0.0;
    }

    @Override
    public Object[] evaluate( MatchableObject f ) {
        StringBuffer sb = new StringBuffer( value.toString().trim() );
        if ( contn != null ) {
            contn.evaluate( sb, f );
        }

        double val = parseDouble( sb.toString() );

        Iterator<Double> data = datas.iterator();
        Iterator<StringBuffer> vals = values.iterator();
        Iterator<Continuation<StringBuffer>> contns = valueContns.iterator();

        double cur = data.next();
        StringBuffer intVal = vals.next();
        Continuation<StringBuffer> contn = contns.next();

        while ( val > cur && data.hasNext() ) {
            cur = data.next();
            intVal = vals.next();
            contn = contns.next();
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
        String sndString = buf.toString();

        double next = data.next();
        double fac = ( val - cur ) / ( next - cur );

        if ( color ) {
            Color fst = decode( sndString );
            Color snd = decode( sndString );
            Color res = interpolateColor( fst, snd, fac );
            return new Object[] { "#" + toHexString( res.getRGB() ) };
        }

        return new Object[] { "" + interpolate( parseDouble( fstString ), parseDouble( sndString ), fac ) };
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

    /* Create the sorted lookup arrays from the linked lists */
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
     * Construct an image map, as the result of the Categorize operation
     * 
     * @param values
     *            Array of float values, that are the inputs to the categorize operation
     * @return a buffered image
     */
    public BufferedImage evaluateRaster( AbstractRaster raster ) {
        BufferedImage img = null;
        long start = System.nanoTime();
        int col = -1, row = -1;
        Color c = null;
        int rgb = 0;
        RasterData data = raster.getAsSimpleRaster().getRasterData();

        buildLookupArrays();

        try {
            Raster2RawData converter = new Raster2RawData( raster );
            Float[][] mat = (Float[][]) converter.parse();

            img = new BufferedImage( data.getWidth(), data.getHeight(), BufferedImage.TYPE_INT_RGB );
            LOG.debug( "Created image with H={}, L={}", img.getHeight(), img.getWidth() );
            for ( row = 0; row < img.getHeight(); row++ )
                for ( col = 0; col < img.getWidth(); col++ ) {
                    Float val = mat[row][col];
                    rgb = (val != null) ? lookupColor2( val ).getRGB() : 0;
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
     * Looks up a value in the current data values, and returns an interpolated value. Uses binary search for
     * optimization.
     * 
     * @param input
     *            value
     * @return the corresponding Color
     */
    public Color lookupColor2( double value ) {
        int l = dataArray.length - 1;
        if ( value <= dataArray[0] || value >= dataArray[l] ) {
            if ( value <= dataArray[0] )
                return colorArray[0];
            if ( value >= dataArray[l] ) {
                // LOG.trace( "bigger!" );
                return colorArray[l];
            }
        }

        int pos = Arrays.binarySearch( dataArray, value );
        if ( pos < 0 ) {
            pos = pos * ( -1 ) - 1;
        }

//        LOG.debug( "Found positions {} and {}", pos - 1, pos );
//        LOG.debug( "Going to do division to {}", dataArray[pos] - dataArray[pos - 1] );
        double f = ( value - dataArray[pos - 1] ) / ( dataArray[pos] - dataArray[pos - 1] );
//        LOG.debug( "Interpolating between {} and {} ", dataArray[pos - 1], dataArray[pos] );
//        LOG.debug( "Interpolating with fraction {} ", f );
        Color color = interpolateColor( colorArray[pos - 1], colorArray[pos], f );
//        LOG.debug( "Found color: {}", color );
        return color;
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

    public String printArray( Object[] a ) {
        String result = a[0].toString();
        for ( int i = 1; i < a.length; i++ )
            result += ", " + a[i].toString();
        result = "{" + result + "}";
        return result;
    }
}
