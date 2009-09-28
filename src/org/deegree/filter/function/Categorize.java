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

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import static org.deegree.rendering.r2d.se.parser.SymbologyParser.updateOrContinue;
import static org.deegree.rendering.r2d.se.unevaluated.Continuation.SBUPDATER;

import java.util.Iterator;
import java.util.LinkedList;

import java.util.List;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.deegree.filter.MatchableObject;
import org.deegree.filter.expression.Function;
import org.deegree.rendering.r2d.se.unevaluated.Continuation;
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
    private String[] valuesArray = null;

    private List<StringBuffer> thresholds = new ArrayList<StringBuffer>();
    private String[] thresholdsArray =  null;

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
    public Object[] evaluate( MatchableObject f ) {
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
                return new Object[] { precedingBelongs ? curVal : nextVal };
            }
            if ( val.compareTo( cur ) == -1 ) {
                return new Object[] { curVal };
            }
            curVal = nextVal;
        }

        return new Object[] { curVal };
    }

    /**
     * Construct an image map, as the result of the Categorize operation
     * @param values Array of int values, that are the inputs to the categorize operation
     * @return a buffered image
     */
    public BufferedImage buildImage(Integer[][] values)
    {
        BufferedImage img;
        long start = System.nanoTime();

        buildLookupArrays();

        try
        {
            img = new BufferedImage(values[0].length, values.length, BufferedImage.TYPE_INT_RGB);
            LOG.debug("Created image with H={}, L={}", img.getHeight(), img.getWidth());
            for (int j = 0; j < img.getHeight(); j++)
                for (int i = 0; i < img.getWidth(); i++)
                {
                    float val = values[j][i];
                    int rgb = lookup(val).getRGB();
                    img.setRGB(i, j, rgb);
                }
        }
        finally
        {
            long end = System.nanoTime();
            LOG.debug("Built image with total time {} ms", (end-start)/1000000);
        }
        return img;
    }

    /**
     * Looks up a value in the current categories and thresholds. Uses binary search for optimization.
     * @param input value
     * @return Category value
     */
    public Color lookup2(double value)
    {
        // TODO: howto use this?
        boolean preceding = precedingBelongs;
        
        int pos = Arrays.binarySearch(thresholdsArray, String.valueOf(value));
        if (pos >=0 )
        {
            // found in the thresholds array
        }
        else
        {
            pos = pos * (-1) - 1;
        }
        String color = valuesArray[pos+1].toString();
//        LOG.debug("Color {} : {}", (((StringBuffer[])values.toArray(new StringBuffer[20]))[pos].toString()), Color.decode(color));
        return Color.decode(color);
    }

    /**
     * Looks up a value in the current categories and thresholds. Naive implementation.
     * @param input double value
     * @return rgb int value, for storing in a BufferedImage
     */
    public Color lookup(double val)
    {
        Iterator<StringBuffer> ts = thresholds.iterator();
        Iterator<StringBuffer> vs = values.iterator();

        // TODO: howto use this?
        boolean preceding = precedingBelongs;

        Float threshold = Float.parseFloat(ts.next().toString());

        while ( ( preceding ? ( threshold < val ) : ( threshold <= val ) ) && ts.hasNext() ) {
            threshold = Float.parseFloat(ts.next().toString());
            vs.next();
        }

        Color c = Color.decode(vs.next().toString());
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
                contn = updateOrContinue( in, "LookupValue", value, SBUPDATER, null );
            }

            if ( in.getLocalName().equals( "Threshold" ) ) {
                StringBuffer sb = new StringBuffer();
                thresholdContns.add( updateOrContinue( in, "Threshold", sb, SBUPDATER, null ) );
                thresholds.add( sb );
            }

            if ( in.getLocalName().equals( "Value" ) ) {
                StringBuffer sb = new StringBuffer();
                valueContns.add( updateOrContinue( in, "Value", sb, SBUPDATER, null ) );
                values.add( sb );
            }

        }

        in.require( END_ELEMENT, null, "Categorize" );
    }

//    public String toString()
//    {
//        String r = "";
//        if (contn != null)
//            r += contn.toString();
//        return r;
//    }

    @Override
    public String toString()
    {
        String r = "\nCategorize [ ";
        r += "\nValues: " + values.toString();
        r += "\nThresholds: " + thresholds.toString();
        if (valuesArray != null)
            r += "\nValues Array: " + valuesArray.toString();
        if (thresholdsArray != null)
            r += "\nThresholds Array: " + thresholdsArray.toString();
        r += "\n ]";
        return r;
    }

    /* Create the sorted lookup arrays from the StringBuffers */
    private void buildLookupArrays()
    {
        if (valuesArray == null)
        {
            valuesArray = new String[values.size()];
            List<String> list = new ArrayList<String>(values.size());
            Iterator<StringBuffer> i = values.iterator();
            while (i.hasNext())
                list.add(i.next().toString());
            valuesArray = list.toArray(valuesArray);
            Arrays.sort(valuesArray);
        }

        if (thresholdsArray == null)
        {
            thresholdsArray = new String[thresholds.size()];
            List<String> list = new ArrayList<String>(thresholds.size());
            Iterator<StringBuffer> i = thresholds.iterator();
            while (i.hasNext())
                list.add(i.next().toString());
            thresholdsArray = list.toArray(thresholdsArray);
            Arrays.sort(thresholdsArray);
        }
    }
}
