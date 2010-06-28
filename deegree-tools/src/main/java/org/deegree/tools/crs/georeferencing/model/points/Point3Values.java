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
package org.deegree.tools.crs.georeferencing.model.points;

/**
 * Datastructure for every information needed to manipulate a point.
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class Point3Values {

    private AbstractGRPoint initialValue;

    private AbstractGRPoint oldValue;

    private AbstractGRPoint newValue;

    public Point3Values( AbstractGRPoint oldValue, AbstractGRPoint initialValue, AbstractGRPoint newValue ) {
        this.oldValue = oldValue;
        this.initialValue = initialValue;
        this.newValue = newValue;
    }

    public Point3Values( AbstractGRPoint initialValue ) {
        this.oldValue = initialValue;
        this.initialValue = initialValue;
        this.newValue = initialValue;
    }

    public AbstractGRPoint getInitialValue() {
        return initialValue;
    }

    public AbstractGRPoint getOldValue() {
        return oldValue;
    }

    public AbstractGRPoint getNewValue() {
        return newValue;
    }

    public void setNewValue( AbstractGRPoint newValue ) {
        this.oldValue = this.newValue;
        this.newValue = newValue;
    }

    @Override
    public String toString() {
        String s = "<" + oldValue + ", " + initialValue + ", " + newValue + ">";
        return s;
    }

}
