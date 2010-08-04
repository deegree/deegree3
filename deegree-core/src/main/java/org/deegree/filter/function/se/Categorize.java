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

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import static org.deegree.commons.utils.ColorUtils.decodeWithAlpha;
import static org.deegree.commons.utils.JavaUtils.generateToString;
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

import org.deegree.commons.tom.TypedObjectNode;
import org.deegree.commons.tom.primitive.PrimitiveValue;
import org.deegree.coverage.raster.AbstractRaster;
import org.deegree.coverage.raster.data.RasterData;
import org.deegree.filter.MatchableObject;
import org.deegree.filter.expression.Function;
import org.deegree.rendering.r2d.se.parser.SymbologyParser;
import org.deegree.rendering.r2d.se.unevaluated.Continuation;
import org.deegree.rendering.r2d.styling.RasterStyling;
import org.deegree.rendering.r2d.utils.RasterDataUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <code>Categorize</code>
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author <a href="mailto:a.aiordachioaie@jacobs-university.de">Andrei Aiordachioaie</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class Categorize extends Function {

    private static final Logger LOG = LoggerFactory.getLogger( Categorize.class );

    private StringBuffer value;

    private Continuation<StringBuffer> contn;

    private boolean precedingBelongs = false;

    private List<StringBuffer> values = new ArrayList<StringBuffer>();

    private Color[] valuesArray = null;

    private List<StringBuffer> thresholds = new ArrayList<StringBuffer>();

    private Float[] thresholdsArray = null;

    private LinkedList<Continuation<StringBuffer>> valueContns = new LinkedList<Continuation<StringBuffer>>();

    private LinkedList<Continuation<StringBuffer>> thresholdContns = new LinkedList<Continuation<StringBuffer>>();

    /***/
    public Categorize() {
        super( "Categorize", null );
    }

    private static final String eval( StringBuffer initial, Continuation<StringBuffer> contn, MatchableObject f ) {
        StringBuffer sb = new StringBuffer( initial.toString().trim() );
        if ( contn != null ) {
            contn.evaluate( sb, f );
        }
        return sb.toString();
    }

    @Override
    public TypedObjectNode[] evaluate( MatchableObject f ) {
        String val = eval( value, contn, f );

        Iterator<StringBuffer> vals = values.iterator();
        Iterator<StringBuffer> barriers = thresholds.iterator();
        Iterator<Continuation<StringBuffer>> valContns = valueContns.iterator();
        Iterator<Continuation<StringBuffer>> barrierContns = thresholdContns.iterator();

        String curVal = eval( vals.next(), valContns.next(), f );
        while ( barriers.hasNext() ) {
            String cur = eval( barriers.next(), barrierContns.next(), f );
            String nextVal = eval( vals.next(), valContns.next(), f );
            if ( cur.equals( val ) ) {
                return new TypedObjectNode[] { new PrimitiveValue( precedingBelongs ? curVal : nextVal ) };
            }
            if ( val.compareTo( cur ) == -1 ) {
                return new TypedObjectNode[] { new PrimitiveValue( curVal ) };
            }
            curVal = nextVal;
        }

        return new TypedObjectNode[] { new PrimitiveValue( curVal ) };
    }

    /**
     * Construct an image map, as the result of the Categorize operation
     * 
     * @param raster
     *            input raster
     * @param style
     *            raster style, that contains channel mappings (if applicable)
     * @return a buffered image with the processed data
     */
    public BufferedImage evaluateRaster( AbstractRaster raster, RasterStyling style ) {
        BufferedImage img = null;
        int col = -1, row = -1;
        RasterData data = raster.getAsSimpleRaster().getRasterData();

        RasterDataUtility converter = new RasterDataUtility( raster, style.channelSelection );

        img = new BufferedImage( data.getColumns(), data.getRows(), BufferedImage.TYPE_INT_ARGB );
        LOG.trace( "Created image with H={}, L={}", img.getHeight(), img.getWidth() );
        for ( row = 0; row < img.getHeight(); row++ ) {
            for ( col = 0; col < img.getWidth(); col++ ) {
                Color c = lookup2( converter.get( col, row ) );
                img.setRGB( col, row, c.getRGB() );
            }
        }

        return img;
    }

    /**
     * Looks up a value in the current categories and thresholds. Uses binary search for optimization.
     * 
     * @param value
     *            value
     * @return Category value
     */
    public final Color lookup2( final double value ) {
        int pos = Arrays.binarySearch( thresholdsArray, new Float( value ) );
        if ( pos >= 0 ) {
            // found exact value in the thresholds array
            if ( precedingBelongs == false ) {
                pos++;
            }
        } else {
            pos = pos * ( -1 ) - 1;
        }

        return valuesArray[pos];
    }

    /**
     * Looks up a value in the current categories and thresholds. Naive implementation. Used for comparisons in tests.
     * 
     * @param val
     *            double value
     * @return rgb int value, for storing in a BufferedImage
     */
    public final Color lookup( final double val ) {
        Iterator<StringBuffer> ts = thresholds.iterator();
        Iterator<StringBuffer> vs = values.iterator();

        Float threshold = Float.parseFloat( ts.next().toString() );

        while ( ( precedingBelongs ? ( threshold < val ) : ( threshold <= val ) ) && ts.hasNext() ) {
            threshold = Float.parseFloat( ts.next().toString() );
            vs.next();
        }

        String col = vs.next().toString();
        Color c = decodeWithAlpha( col );
        return c;
    }

    /**
     * @param in
     * @throws XMLStreamException
     */
    public void parse( XMLStreamReader in )
                            throws XMLStreamException {
        in.require( START_ELEMENT, null, "Categorize" );

        String belong = in.getAttributeValue( null, "thresholdsBelongTo" );
        if ( belong == null ) {
            belong = in.getAttributeValue( null, "threshholdsBelongTo" );
        }
        if ( belong != null ) {
            precedingBelongs = belong.equals( "preceding" );
        }

        while ( !( in.isEndElement() && in.getLocalName().equals( "Categorize" ) ) ) {
            in.nextTag();

            if ( in.getLocalName().equals( "LookupValue" ) ) {
                value = new StringBuffer();
                contn = SymbologyParser.INSTANCE.updateOrContinue( in, "LookupValue", value, SBUPDATER, null ).second;
            }

            if ( in.getLocalName().equals( "Threshold" ) ) {
                StringBuffer sb = new StringBuffer();
                thresholdContns.add( SymbologyParser.INSTANCE.updateOrContinue( in, "Threshold", sb, SBUPDATER, null ).second );
                thresholds.add( sb );
            }

            if ( in.getLocalName().equals( "Value" ) ) {
                StringBuffer sb = new StringBuffer();
                valueContns.add( SymbologyParser.INSTANCE.updateOrContinue( in, "Value", sb, SBUPDATER, null ).second );
                values.add( sb );
            }

        }

        in.require( END_ELEMENT, null, "Categorize" );

        buildLookupArrays();
    }

    @Override
    public String toString() {
        return generateToString( this );
    }

    /** Create the sorted lookup arrays from the StringBuffer lists */
    void buildLookupArrays() {
        LOG.debug( "Building look-up arrays, for binary search... " );
        if ( valuesArray == null ) {
            valuesArray = new Color[values.size()];
            List<Color> list = new ArrayList<Color>( values.size() );
            Iterator<StringBuffer> i = values.iterator();
            while ( i.hasNext() ) {
                list.add( decodeWithAlpha( i.next().toString() ) );
            }
            valuesArray = list.toArray( valuesArray );
        }

        if ( thresholdsArray == null ) {
            thresholdsArray = new Float[thresholds.size()];
            List<Float> list = new ArrayList<Float>( thresholds.size() );
            Iterator<StringBuffer> i = thresholds.iterator();
            while ( i.hasNext() ) {
                list.add( Float.parseFloat( i.next().toString() ) );
            }
            thresholdsArray = list.toArray( thresholdsArray );
        }
    }

}
