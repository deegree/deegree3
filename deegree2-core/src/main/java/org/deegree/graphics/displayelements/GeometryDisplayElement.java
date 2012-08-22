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
package org.deegree.graphics.displayelements;

import java.io.Serializable;

import org.deegree.graphics.sld.Symbolizer;
import org.deegree.model.feature.Feature;
import org.deegree.model.spatialschema.Envelope;
import org.deegree.model.spatialschema.Geometry;
import org.deegree.model.spatialschema.Position;

/**
 * Basic interface of all display elements that are related to a geometry (this is the common case).
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
abstract class GeometryDisplayElement extends AbstractDisplayElement implements Serializable {

    /** Use serialVersionUID for interoperability. */
    private final static long serialVersionUID = 465725117946501686L;

    /**
     * the geometry
     */
    protected Geometry geometry;

    /**
     * the symbolizer
     */
    protected Symbolizer symbolizer;

    /**
     * the highlighted Symbolizer
     */
    protected Symbolizer highlightSymbolizer;

    /**
     * the selected Symbolizer
     */
    protected Symbolizer selectedSymbolizer;

    /**
     * The placement?
     */
    protected Object placement;

    /**
     * Creates a new GeometryDisplayElement object.
     *
     * @param feature
     * @param geometry
     */
    GeometryDisplayElement( Feature feature, Geometry geometry ) {
        super( feature );
        setGeometry( geometry );
    }

    /**
     * Creates a new GeometryDisplayElement object.
     *
     * @param feature
     * @param geometry
     * @param symbolizer
     */
    GeometryDisplayElement( Feature feature, Geometry geometry, Symbolizer symbolizer ) {
        super( feature );
        setGeometry( geometry );
        setSymbolizer( symbolizer );
        setHighlightSymbolizer( symbolizer );
        setSelectedSymbolizer( symbolizer );
    }

    /**
     * Creates a new GeometryDisplayElement object.
     *
     * @param feature
     * @param geometry
     * @param symbolizer
     * @param selectedSymbolizer
     * @param highlightSymbolizer
     */
    GeometryDisplayElement( Feature feature, Geometry geometry, Symbolizer symbolizer, Symbolizer highlightSymbolizer,
                            Symbolizer selectedSymbolizer ) {
        super( feature );
        setGeometry( geometry );
        setSymbolizer( symbolizer );
        setSelectedSymbolizer( selectedSymbolizer );
        setHighlightSymbolizer( highlightSymbolizer );
    }

    /**
     * Returns a new {@link Envelope} for the given envelope that has a border of percent * (with |
     * height) on all sides around it (the longer side is used to determine the border size).
     *
     * @param env
     * @param percent
     * @return envelope with border around it
     */
    protected Envelope growEnvelope( Envelope env, float percent ) {
        Position minPos = env.getMin();
        Position maxPos = env.getMax();
        double h = maxPos.getX() - minPos.getX();
        double w = maxPos.getY() - minPos.getY();
        h = Math.abs( h );
        w = Math.abs( w );
        double maxSide = Math.max( w, h );
        return env.getBuffer( maxSide * percent );
    }

    /**
     * Overwrites the default placement of the <tt>DisplayElement</tt>. This method is used by
     * the <tt>PlacementOptimizer</tt> to minimize the overlapping of labels, for example.
     * <p>
     *
     * @param o
     *            the placement to be used
     */
    public void setPlacement( Object o ) {
        placement = o;
    }

    /**
     * sets the geometry that determines the position the DisplayElement will be rendered to
     * @param geometry to set
     */
    public void setGeometry( Geometry geometry ) {
        this.geometry = geometry;
    }

    /**
     * returns the geometry that determines the position the DisplayElement will be rendered to
     * @return the geometry that determines the position the DisplayElement will be rendered to
     */
    public Geometry getGeometry() {
        return geometry;
    }

    /**
     * sets the rules that determines how the geometry will be rendered
     * @param symbolizer
     */
    public void setSymbolizer( Symbolizer symbolizer ) {
        this.symbolizer = symbolizer;
    }

    /**
     * Returns the symbolizer that determines how the geometry will be rendered.
     * @return the symbolizer that determines how the geometry will be rendered.
     */
    public Symbolizer getSymbolizer() {
        return symbolizer;
    }

    /**
     * sets the rule that determines how the geometry will be rendered when it's highlighted
     *
     * @param rule
     *            symbolizer defining rendering style
     */
    public void setHighlightSymbolizer( Symbolizer rule ) {
        this.highlightSymbolizer = rule;
    }

    /**
     * returns the symbolizer that determines how the geometry will be rendered if it's highlighted
     * @return the symbolizer that determines how the geometry will be rendered if it's highlighted
     */
    public Symbolizer getHighlightSymbolizer() {
        return highlightSymbolizer;
    }

    /**
     * sets the rule that determines how the geometry will be rendered when it's selected
     *
     * @param rule
     *            symbolizer defining rendering style
     */
    public void setSelectedSymbolizer( Symbolizer rule ) {
        selectedSymbolizer = rule;
    }

    /**
     * returns the symbolizer that determines how the geometry will be rendered if it's selected
     * @return the symbolizer that determines how the geometry will be rendered if it's selected
     */
    public Symbolizer getSelectedSymbolizer() {
        return selectedSymbolizer;
    }

    /**
     * Returns if the <tt>DisplayElement</tt> should be painted at the current scale or not.
     */
    @Override
    public boolean doesScaleConstraintApply( double scale ) {
        return symbolizer.getMinScaleDenominator() <= scale && symbolizer.getMaxScaleDenominator() > scale;
    }
}
