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

/**
 * This is the basis of all symbolizers. It defines the method <tt>getGeometry</tt> that's common
 * to all symbolizers.
 * <p>
 * ----------------------------------------------------------------------
 * </p>
 *
 * @author <a href="mailto:k.lupp@web.de">Katharina Lupp</a>
 * @version $Revision$ $Date$
 */

public abstract class AbstractSymbolizer implements Symbolizer {

    /**
     * The max denominator initialized with 9E99;
     */
    protected double maxDenominator = 9E99;

    /**
     * The min denominator initialized with 0;
     */
    protected double minDenominator = 0;

    /**
     * The geometry of the sympbolizer
     */
    protected Geometry geometry = null;

    /**
     * The class to render.
     */
    protected String responsibleClass = null;

    /**
     * default constructor
     */
    AbstractSymbolizer() {
        //nothing.
    }

    /**
     * constructor initializing the class with the <Symbolizer>
     * @param geometry
     */
    AbstractSymbolizer( Geometry geometry ) {
        setGeometry( geometry );
    }

    /**
     * constructor initializing the class with the <Symbolizer>
     * @param geometry
     * @param resonsibleClass
     */
    AbstractSymbolizer( Geometry geometry, String resonsibleClass ) {
        setGeometry( geometry );
        setResponsibleClass( resonsibleClass );
    }

    /**
     * The Geometry element is optional and if it is absent then the default geometry property of
     * the feature type that is used in the containing FeatureStyleType is used. The precise meaning
     * of default geometry property is system-dependent. Most frequently, feature types will have
     * only a single geometry property.
     *
     * @return the geometry of the symbolizer
     *
     */
    public Geometry getGeometry() {
        return geometry;
    }

    /**
     * sets the <Geometry>
     *
     * @param geometry
     *            the geometry of the symbolizer
     *
     */
    public void setGeometry( Geometry geometry ) {
        this.geometry = geometry;
    }

    /**
     * @return the MinScaleDenominator
     */
    public double getMinScaleDenominator() {
        return minDenominator;
    }

    /**
     * @param minDenominator
     *            the MinScaleDenominator
     */
    public void setMinScaleDenominator( double minDenominator ) {
        this.minDenominator = minDenominator;
    }

    /**
     * @return the MaxScaleDenominator
     */
    public double getMaxScaleDenominator() {
        return maxDenominator;
    }

    /**
     * @param maxDenominator
     *            the MaxScaleDenominator
     */
    public void setMaxScaleDenominator( double maxDenominator ) {
        this.maxDenominator = maxDenominator;
    }

    /**
     * returns the name of a class that will be used for rendering the current symbolizer. This
     * enables a user to define his own rendering class (DisplayElement) for a symbolizer to realize
     * styles/renderings that can't be defined using SLD at the moment.<BR>
     * The returned class must extend
     * org.deegree_impl.graphics.displayelements.GeometryDisplayElement_Impl<BR>
     * For default the method returns the deegree default class name for rendering the current
     * symbolizer.
     *
     * @return the name of a class that will be used for rendering the current symbolizer.
     *
     */
    public String getResponsibleClass() {
        return responsibleClass;
    }

    /**
     * sets a class that will be used for rendering the current symbolizer. This enables a user to
     * define his own rendering class (DisplayElement) for a symbolizer to realize styles/renderings
     * that can't be defined using SLD at the moment.<BR>
     * The passed class must extend
     * org.deegree_impl.graphics.displayelements.GeometryDisplayElement_Impl
     *
     * @param responsibleClass
     *
     */
    public void setResponsibleClass( String responsibleClass ) {
        this.responsibleClass = responsibleClass;
    }

}
