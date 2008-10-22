//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------

 This file is part of deegree.
 Copyright (C) 2001-2008 by:
 EXSE, Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 Contact:

 Andreas Poth  
 lat/lon GmbH 
 Aennchenstr. 19
 53115 Bonn
 Germany
 E-Mail: poth@lat-lon.de

 Prof. Dr. Klaus Greve
 Department of Geography
 University of Bonn
 Meckenheimer Allee 166
 53115 Bonn
 Germany
 E-Mail: greve@giub.uni-bonn.de


 ---------------------------------------------------------------------------*/
package org.deegree.model.geometry.primitive;

/**
 * An <code>OrientableCurve</code> consists of a wrapped base {@link Curve} and an additional orientation.
 * <p>
 * If the orientation is not reversed, then the OrientableCurve is identical to the base curve. If the orientation is
 * reversed, then the OrientableCurve is related to the base curve with a parameterization that reverses the sense of
 * the curve traversal.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
public interface OrientableCurve extends Curve {

    /**
     * Returns true, if the orientation of the base curve is reversed.
     * 
     * @return true, if the orientation of the base curve is reversed, fale otherwise (identical orientation)
     */
    public boolean isReversed();

    /**
     * Returns the {@link Curve} that this <code>OrientableCurve</code> is based on.
     * 
     * @return the base <code>Curve</code>
     */
    public Curve getBaseCurve();
}
