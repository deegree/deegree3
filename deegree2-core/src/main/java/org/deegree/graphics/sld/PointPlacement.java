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
import org.deegree.framework.xml.Marshallable;
import org.deegree.model.feature.Feature;
import org.deegree.model.filterencoding.FilterEvaluationException;
import org.deegree.model.filterencoding.PropertyName;

/**
 * Incarnation of a sld:PointPlacement-element. For a PointPlacement, the anchor point of the label
 * and a linear displacement from the point can be specified, to allow a graphic symbol to be
 * plotted directly at the point. This might be useful to label a city, for example. For a
 * LinePlacement, a perpendicular offset can be specified, to allow the line itself to be plotted
 * also. This might be useful for labelling a road or a river, for example.
 * <p>
 *
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class PointPlacement implements Marshallable {

    private static final ILogger LOG = LoggerFactory.getLogger( PointPlacement.class );

    private ParameterValueType rotation = null;

    private ParameterValueType[] anchorPoint = null;

    private ParameterValueType[] displacement = null;

    // should the placement be optimized?
    private boolean auto = false;

    /**
     * package protected default constructor
     *
     */
    PointPlacement() {
        //again nothing.
    }

    /**
     * Creates a new PointPlacement object.
     *
     * @param anchorPoint
     * @param displacement
     * @param rotation
     * @param auto
     */
    public PointPlacement( ParameterValueType[] anchorPoint, ParameterValueType[] displacement,
                           ParameterValueType rotation, boolean auto ) {
        this.anchorPoint = anchorPoint;
        this.displacement = displacement;
        this.rotation = rotation;
        this.auto = auto;
    }

    /**
     * returns the anchor points (x and y) as array of
     *
     * @see ParameterValueType
     * @return anchor point as
     * @see ParameterValueType
     */
    public ParameterValueType[] getAnchorPoint() {
        return anchorPoint;
    }

    /**
     * returns the displacements (x and y) as array of
     *
     * @see ParameterValueType
     * @return displacements (x and y) as array of
     * @see ParameterValueType
     */
    public ParameterValueType[] getDisplacement() {
        return displacement;
    }

    /**
     * returns the rotation of ParameterValueType
     * @return the rotation
     */
    public ParameterValueType getRotation() {
        return rotation;
    }

    /**
     * The AnchorPoint element of a PointPlacement gives the location inside of a label to use for
     * anchoring the label to the main-geometry point.
     * <p>
     * </p>
     * The coordinates are given as two floating-point numbers in the AnchorPointX and AnchorPointY
     * elements each with values between 0.0 and 1.0 inclusive. The bounding box of the label to be
     * rendered is considered to be in a coorindate space from 0.0 (lower-left corner) to 1.0
     * (upper-right corner), and the anchor position is specified as a point in this space. The
     * default point is X=0, Y=0.5, which is at the middle height of the left-hand side of the
     * label. A system may choose different anchor points to de-conflict labels.
     * <p>
     *
     * @param feature
     *            specifies the <tt>Feature</tt> to be used for evaluation of the underlying
     *            'sld:ParameterValueType'
     * @return 2 double values: x ([0]) and y ([0])
     * @throws FilterEvaluationException
     *             if the evaluation fails
     */
    public double[] getAnchorPoint( Feature feature )
                            throws FilterEvaluationException {
        double[] anchorPointVal = { 0.0, 0.5 };

        if ( anchorPoint != null ) {
            anchorPointVal[0] = Double.parseDouble( anchorPoint[0].evaluate( feature ) );
            anchorPointVal[1] = Double.parseDouble( anchorPoint[1].evaluate( feature ) );
        }

        return anchorPointVal;
    }

    /**
     * @see PointPlacement#getAnchorPoint(Feature)
     *      <p>
     * @param anchorPoint
     *            anchorPoint for the PointPlacement
     */
    public void setAnchorPoint( double[] anchorPoint ) {
        ParameterValueType pvt = null;
        ParameterValueType[] pvtArray = new ParameterValueType[anchorPoint.length];
        for ( int i = 0; i < anchorPoint.length; i++ ) {
            pvt = StyleFactory.createParameterValueType( "" + anchorPoint[i] );
            pvtArray[i] = pvt;
        }
        this.anchorPoint = pvtArray;
    }

    /**
     * The Displacement element of a PointPlacement gives the X and Y displacements from the
     * main-geometry point to render a text label.
     * <p>
     * </p>
     * This will often be used to avoid over-plotting a graphic symbol marking a city or some such
     * feature. The displacements are in units of pixels above and to the right of the point. A
     * system may reflect this displacement about the X and/or Y axes to de-conflict labels. The
     * default displacement is X=0, Y=0.
     * <p>
     *
     * @param feature
     *            specifies the <tt>Feature</tt> to be used for evaluation of the underlying
     *            'sld:ParameterValueType'
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
     * @see PointPlacement#getDisplacement(Feature)
     *      <p>
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

    /**
     * The Rotation of a PointPlacement gives the clockwise rotation of the label in degrees from
     * the normal direction for a font (left-to-right for Latin- derived human languages at least).
     * <p>
     *
     * @param feature
     *            specifies the <tt>Feature</tt> to be used for evaluation of the underlying
     *            'sld:ParameterValueType'
     * @return double value describing the rotation parameter
     * @throws FilterEvaluationException
     *             if the evaluation fails*
     */
    public double getRotation( Feature feature )
                            throws FilterEvaluationException {
        double rot = 0.0;

        if ( rotation != null ) {
            String tmp = rotation.evaluate( feature );
            if ( tmp != null ) {
                try {
                    rot = Double.parseDouble( tmp );
                } catch ( NumberFormatException e ) {
                    LOG.logInfo( "could not parse rotation value as float, use 0° as default: ", tmp );
                }
            }
        }

        return rot;
    }


    /**
     * Returns the property name of the rotation if available
     *
     * @return the property name or null, if no property name is set
     */
    public PropertyName getRotationPropertyName() {
        if ( rotation != null ) {
            Object[] o = rotation.getComponents();
            for ( int i = 0; i < o.length; i++ ) {
                if ( o[i] instanceof PropertyName ) {
                    return (PropertyName) o[i];
                }
            }
        }
        return null;
    }

    /**
     * @see PointPlacement#getRotation(Feature)
     * @param rotation
     *            the rotation to be set for the PointPlacement
     */
    public void setRotation( double rotation ) {
        ParameterValueType pvt = null;
        pvt = StyleFactory.createParameterValueType( "" + rotation );
        this.rotation = pvt;
    }

    /**
     * Returns whether the placement should be optimized or not.
     * <p>
     *
     * @return true, if it should be optimized
     *
     */
    public boolean isAuto() {
        return auto;
    }

    /**
     * <p>
     *
     * @param auto
     *
     */
    public void setAuto( boolean auto ) {
        this.auto = auto;
    }

    /**
     * exports the content of the PointPlacement as XML formated String
     *
     * @return xml representation of the PointPlacement
     */
    public String exportAsXML() {

        StringBuffer sb = new StringBuffer( 1000 );
        sb.append( "<PointPlacement" );
        if ( auto ) {
            sb.append( " auto='true'" );
        }
        sb.append( ">" );
        if ( anchorPoint != null && anchorPoint.length > 1 ) {
            sb.append( "<AnchorPoint>" ).append( "<AnchorPointX>" );
            sb.append( ( (Marshallable) anchorPoint[0] ).exportAsXML() );
            sb.append( "</AnchorPointX>" ).append( "<AnchorPointY>" );
            sb.append( ( (Marshallable) anchorPoint[1] ).exportAsXML() );
            sb.append( "</AnchorPointY>" ).append( "</AnchorPoint>" );
        }
        if ( displacement != null && displacement.length > 1 ) {
            sb.append( "<Displacement>" ).append( "<DisplacementX>" );
            sb.append( ( (Marshallable) displacement[0] ).exportAsXML() );
            sb.append( "</DisplacementX>" ).append( "<DisplacementY>" );
            sb.append( ( (Marshallable) displacement[1] ).exportAsXML() );
            sb.append( "</DisplacementY>" ).append( "</Displacement>" );
        }
        if ( rotation != null ) {
            sb.append( "<Rotation>" );
            sb.append( ( (Marshallable) rotation ).exportAsXML() );
            sb.append( "</Rotation>" );
        }

        sb.append( "</PointPlacement>" );

        return sb.toString();
    }
}
