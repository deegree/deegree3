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

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Iterator;

import org.deegree.graphics.sld.ParameterValueType;
import org.deegree.graphics.sld.TextSymbolizer;
import org.deegree.graphics.transformation.GeoTransform;
import org.deegree.model.feature.Feature;
import org.deegree.model.spatialschema.Geometry;

/**
 * <tt>DisplayElement</tt> that encapsulates a <tt>GM_Object</tt> (geometry), a <tt>ParameterValueType</tt> (caption)
 * and a <tt>TextSymbolizer</tt> (style).
 * <p>
 * The graphical (say: screen) representations of this <tt>DisplayElement</tt> are <tt>Label</tt>-instances. These are
 * generated either when the <tt>paint</tt>-method is called or assigned externally using the <tt>setLabels</tt>- or
 * <tt>addLabels</tt>-methods.
 * </p>
 * 
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author <a href="mailto:mschneider@lat-lon.de">Markus Schneider</a>
 * @version $Revision$ $Date$
 */

public class LabelDisplayElement extends GeometryDisplayElement {

    /** Use serialVersionUID for interoperability. */
    private final static long serialVersionUID = -7870967255670858503L;

    private ParameterValueType label = null;

    // null means that the labels have to be created inside the paint-method
    // (and have not been set externally)
    private ArrayList<Label> labels = null;

    private final double pixelsize;

    /**
     * Creates a new LabelDisplayElement object.
     * <p>
     * 
     * @param feature
     *            associated <tt>Feature</tt>
     * @param geometry
     *            associated <tt>GM_Object</tt>
     * @param symbolizer
     *            associated <tt>TextSymbolizer</tt>
     */
    public LabelDisplayElement( Feature feature, Geometry geometry, TextSymbolizer symbolizer, double pixelsize ) {
        super( feature, geometry, symbolizer );
        this.pixelsize = pixelsize;
        setLabel( symbolizer.getLabel() );
    }

    /**
     * Sets the caption of the label.
     * 
     * @param label
     */
    public void setLabel( ParameterValueType label ) {
        this.label = label;
    }

    /**
     * Returns the caption of the label as <tt>ParameterValueType<tt>.
     * 
     * @return label
     */
    public ParameterValueType getLabel() {
        return label;
    }

    /**
     * Renders the <tt>DisplayElement</tt> to the submitted graphic context. If the <tt>Label</tt>-represenations have
     * been assigned externally, these labels are used, else <tt>Label</tt>-instances are created automatically using
     * the <tt>LabelFactory</tt>.
     * <p>
     * 
     * @param g
     *            <tt>Graphics</tt> context to be used
     * @param projection
     *            <tt>GeoTransform</tt> to be used
     */
    public void paint( Graphics g, GeoTransform projection, double scale ) {
        synchronized ( symbolizer ) {
            ( (ScaledFeature) feature ).setScale( scale );
            if ( label == null )
                return;
            Graphics2D g2D = (Graphics2D) g;
            if ( labels == null ) {
                try {
                    setLabels( LabelFactory.createLabels( this, projection, g2D, pixelsize ) );
                } catch ( Exception e ) {
                    e.printStackTrace();
                }
            }
            // paint all labels
            if ( labels == null ) {
                return;
            }
            Iterator<Label> it = labels.iterator();
            while ( it.hasNext() ) {
                Label lab = it.next();
                lab.paint( g2D );
            }

            // mark the labels as unset (for the next paint-call)
            labels = null;
        }
    }

    @Override
    public boolean doesScaleConstraintApply( double scale ) {
        return ( symbolizer.getMinScaleDenominator() <= scale ) && ( symbolizer.getMaxScaleDenominator() > scale );
    }

    /**
     * Removes all <tt>Label<tt> representations for this
     * <tt>LabelDisplayElement</tt>.
     */
    public void clearLabels() {
        labels = null;
    }

    /**
     * Adds a <tt>Label</tt> representation that is to be considered when the <tt>LabelDisplayElement</tt> is painted to
     * the view.
     * 
     * @param label
     */
    public void addLabel( Label label ) {
        if ( labels == null ) {
            labels = new ArrayList<Label>( 100 );
        }
        labels.add( label );
    }

    /**
     * Adds <tt>Label</tt> representations that are to be considered when the <tt>LabelDisplayElement</tt> is painted to
     * the view.
     * 
     * @param labels
     */
    public void addLabels( Label[] labels ) {
        if ( this.labels == null ) {
            this.labels = new ArrayList<Label>( 100 );
        }
        for ( int i = 0; i < labels.length; i++ ) {
            this.labels.add( labels[i] );
        }
    }

    /**
     * Sets the <tt>Label</tt> representations that are to be considered when the <tt>LabelDisplayElement</tt> is
     * painted to the view.
     * 
     * @param labels
     */
    public void setLabels( Label[] labels ) {
        this.labels = new ArrayList<Label>( 100 );
        for ( int i = 0; i < labels.length; i++ ) {
            this.labels.add( labels[i] );
        }
    }
}
