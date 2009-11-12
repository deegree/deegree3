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
package org.deegree.feature.types.property;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.deegree.commons.types.datetime.Date;
import org.deegree.commons.types.datetime.DateTime;
import org.deegree.commons.types.datetime.Time;

/**
 * Primitive type system. Based on XML schema types, but stripped down to leave out any distinctions
 * that are not necessary in the feature model.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public enum PrimitiveType {
    /** Property value is of class <code>String</code>. */
    STRING( String.class ),
    /** Property value is of class <code>Boolean</code>. */
    BOOLEAN( Boolean.class ),
    /** Property value is of class <code>BigDecimal</code>. */
    DECIMAL( BigDecimal.class ),
    /**
     * Property value is of class <code>Double</code> (needed because BigDecimal cannot express "NaN", "-INF" and
     * "INF"), which are required by <code>xs:double</code> / <code>xs:float</code>.
     */
    DOUBLE( Double.class ),
    /** Property value is of class <code>BigInteger</code>. */
    INTEGER( BigInteger.class ),
    /** Property value is of class {@link Date}. */
    DATE( Date.class ),
    /** Property value is of class {@link DateTime}. */
    DATE_TIME( DateTime.class ),
    /** Property value is of class {@link Time}. */
    TIME( Time.class );

    private Class<?> valueClass;

    private PrimitiveType( Class<?> valueClass ) {
        this.valueClass = valueClass;
    }

    /**
     * Returns the class that primitive values of this type must have.
     * 
     * @return the corresponding class for values
     */
    public Class<?> getValueClass() {
        return valueClass;
    }
}
