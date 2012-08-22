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

import java.util.Iterator;
import java.util.LinkedList;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.graphics.sld.SLDFactory.ThresholdsBelongTo;
import org.deegree.model.filterencoding.FilterEvaluationException;

/**
 * <code>Categorize</code> encapsulates data from a categorize element in a RasterSymbolizer.
 *
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class Categorize {

    private static final ILogger LOG = LoggerFactory.getLogger( Categorize.class );

    // private String fallbackValue;

    private LinkedList<Float> thresholds;

    private LinkedList<Long> values;

    private LinkedList<Boolean> opacities;

    private ThresholdsBelongTo thresholdsBelongTo;

    /**
     *
     */
    public Categorize() {
        // this.fallbackValue = fallbackValue;
    }

    /**
     * @param thresholds
     */
    public void setThresholds( LinkedList<ParameterValueType> thresholds ) {
        this.thresholds = new LinkedList<Float>();
        for ( ParameterValueType pvt : thresholds ) {
            try {
                this.thresholds.add( Float.valueOf( ( pvt.evaluate( null ) ) ) );
            } catch ( NumberFormatException e ) {
                LOG.logError( "A number in a threshold value of a RasterSymbolizer could not be parsed.", e );
            } catch ( FilterEvaluationException e ) {
                LOG.logError( "A threshold value could not be parsed in a RasterSymbolizer.", e );
            }
        }
    }

    /**
     * @param values
     */
    public void setValues( LinkedList<ParameterValueType> values ) {
        this.values = new LinkedList<Long>();
        opacities = new LinkedList<Boolean>();

        for ( ParameterValueType pvt : values ) {
            try {
                String s = pvt.evaluate( null );
                this.values.add( Long.valueOf( s.substring( 1 ), 16 ) );
                opacities.add( new Boolean( s.length() > 7 ) );
            } catch ( FilterEvaluationException e ) {
                LOG.logError( "A color value could not be parsed in a RasterSymbolizer.", e );
            }
        }
    }

    /**
     * @param thresholdsBelongTo
     */
    public void setThresholdsBelongTo( ThresholdsBelongTo thresholdsBelongTo ) {
        this.thresholdsBelongTo = thresholdsBelongTo;
    }

    /**
     * @param val
     * @param opacity
     *            the opacity to use if none has been set for the value
     * @return a categorized value
     */
    public int categorize( float val, int opacity ) {
        Iterator<Float> ts = thresholds.iterator();
        Iterator<Long> vs = values.iterator();
        Iterator<Boolean> os = opacities.iterator();

        // hope this is interpreted correctly.
        // preceding: value is classified to our value, if value is >= threshold
        // succeeding: value is classified to our value, if value is > threshold
        boolean preceding = thresholdsBelongTo == SLDFactory.ThresholdsBelongTo.PRECEDING;

        float threshold = ts.next().floatValue();

        while ( ( preceding ? ( threshold < val ) : ( threshold <= val ) ) && ts.hasNext() ) {
            threshold = ts.next().floatValue();
            vs.next();
            os.next();
        }

        int res = vs.next().intValue();
        if ( os.next().booleanValue() ) {
            return res;
        }
        return res | opacity;
    }
}
