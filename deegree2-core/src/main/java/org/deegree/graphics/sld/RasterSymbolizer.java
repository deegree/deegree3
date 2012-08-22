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

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.Pair;
import org.deegree.framework.xml.Marshallable;
import org.deegree.model.filterencoding.FilterEvaluationException;
import org.deegree.ogcbase.CommonNamespaces;

/**
 * <code>RasterSymbolizer</code> encapsulates the Symbology Encoding values that may have been set.
 * Note that everything is optional, so all values may be null.
 *
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class RasterSymbolizer extends AbstractSymbolizer implements Marshallable {

    private static final ILogger LOG = LoggerFactory.getLogger( RasterSymbolizer.class );

    private double opacity;

    private boolean opacitySet;

    private Categorize categorize;

    private Interpolate interpolate;

    private boolean scaleSet;

    private boolean shaded;

    private int dim;

    private float[] kernel;

    private double gamma = 1;

    /**
     * Initializes nothing.
     */
    public RasterSymbolizer() {
        // initialize nothing
    }

    /**
     * @param min
     * @param max
     */
    public RasterSymbolizer( double min, double max ) {
        if ( min > 0 || max > 0 ) {
            scaleSet = true;
            this.minDenominator = min;
            this.maxDenominator = max;
        }
    }

    /**
     * @param opac
     */
    public void setOpacity( ParameterValueType opac ) {
        try {
            opacity = Double.parseDouble( opac.evaluate( null ) );
            opacitySet = true;
        } catch ( NumberFormatException e ) {
            LOG.logError( "The opacity value of a RasterSymbolizer could not be parsed.", e );
        } catch ( FilterEvaluationException e ) {
            LOG.logError( "The opacity value of a RasterSymbolizer could not be parsed.", e );
        }
    }

    /**
     * @return the opacity or 1, if none was set
     */
    public double getOpacity() {
        return opacitySet ? opacity : 1;
    }

    /**
     * @param categorize
     */
    public void setCategorize( Categorize categorize ) {
        this.categorize = categorize;
    }

    /**
     * @param interpolate
     */
    public void setInterpolate( Interpolate interpolate ) {
        this.interpolate = interpolate;
    }

    /**
     * @return the categorize
     */
    public Categorize getCategorize() {
        return categorize;
    }

    /**
     * @return the interpolate
     */
    public Interpolate getInterpolate() {
        return interpolate;
    }

    /**
     * @return true, if none of the options has been set (scale is ignored)
     */
    public boolean isDefault() {
        return ( !opacitySet ) && categorize == null && interpolate == null;
    }

    /**
     * @param shaded
     */
    public void setShaded( boolean shaded ) {
        this.shaded = shaded;
    }

    /**
     * @return true, if the symbolization should be shaded
     */
    public boolean getShaded() {
        return shaded;
    }

    /**
     * @param scale
     * @return true, if no scale hints have been set or if the given scale is valid
     */
    public boolean scaleValid( double scale ) {
        return ( scaleSet && minDenominator <= scale && maxDenominator >= scale ) || !scaleSet;
    }

    @Override
    public String toString() {
        return opacity + ", " + categorize + ", " + interpolate;
    }

    /**
     * @param dim
     * @param kernel
     */
    public void setShadeKernel( int dim, float[] kernel ) {
        this.dim = dim;
        this.kernel = kernel;
    }

    /**
     * @return the shade kernel to be used
     */
    public Pair<Integer, float[]> getShadeKernel() {
        return new Pair<Integer, float[]>( dim, kernel );
    }

    /**
     * @param gamma
     */
    public void setGamma( double gamma ) {
        this.gamma = gamma;
    }

    /**
     * @return the gamma value
     */
    public double getGamma() {
        return gamma;
    }

    /**
     * exports the content of the RasterSymbolizer as XML formated String. At the moment only the
     * export of opacity and gamma value is supported!
     *
     *
     * @return xml representation of the RasterSymbolizer
     */
    public String exportAsXML() {
        StringBuffer sb = new StringBuffer( 200 );
        sb.append( "<se:RasterSymbolizer xmlns:se=\"" + CommonNamespaces.SENS + "\" >" );
        sb.append( "<se:Opacity>" );
        sb.append( this.opacity );
        sb.append( "</se:Opacity>" );
        sb.append( "<se:ContrastEnhancement>" );
        sb.append( "<se:GammaValue>" );
        sb.append( this.gamma );
        sb.append( "</se:GammaValue>" );
        sb.append( "</se:ContrastEnhancement>" );
        sb.append( "</se:RasterSymbolizer>" );
        return sb.toString();
    }

}
