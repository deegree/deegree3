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
package org.deegree.tools.crs.georeferencing.model;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.List;

import org.deegree.tools.crs.georeferencing.model.points.AbstractGRPoint;
import org.deegree.tools.crs.georeferencing.model.points.Point4Values;

/**
 * Abstract base class for all 2D scenes.
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public abstract class Abstract2DModel {

    protected List<Point4Values> selectedPoints;

    protected Point4Values lastAbstractPoint;

    protected float roundFloat( float value ) {
        BigDecimal b = new BigDecimal( value );
        b = b.round( new MathContext( 2 ) );
        return b.floatValue();
    }

    protected float roundDouble( double value ) {
        BigDecimal b = new BigDecimal( value );
        b = b.round( new MathContext( 2 ) );
        return b.floatValue();
    }

    public List<Point4Values> getSelectedPoints() {
        return selectedPoints;
    }

    public Point4Values getLastAbstractPoint() {
        return lastAbstractPoint;
    }

    public void setLastAbstractPoint( AbstractGRPoint lastAbstractPoint ) {
        // if ( lastAbstractPoint != null ) {
        // this.lastAbstractPoint = new Point3Values( lastAbstractPoint );
        // } else {
        // this.lastAbstractPoint = null;
        // }
    }

    public void addToSelectedPoints( Point4Values point ) {

        selectedPoints.add( point );

    }

    protected abstract void updateSelectedPoints();

    // public abstract FootprintPoint getClosestPoint( AbstractGRPoint point2d );

}
