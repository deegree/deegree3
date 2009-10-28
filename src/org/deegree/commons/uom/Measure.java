//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/deegree3/commons/trunk/src/org/deegree/model/feature/Feature.java $
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
package org.deegree.commons.uom;

import java.math.BigDecimal;

/**
 * Number with a scale.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
public class Measure {

    private BigDecimal value;

    private String uomURI;

    // angular degree --> urn:ogc:def:uom:EPSG:6.3:9102
    // radian --> urn:ogc:def:uom:EPSG::9101
    // meter --> urn:ogc:def:uom:EPSG:6.3:9001
    // unity --> urn:ogc:def:uom:EPSG:6.3:8805
    // from URN definitions
    public Measure( BigDecimal value, String uomURI ) {
        this.value = value;
        this.uomURI = uomURI;
    }

    public Measure( String value, String uomURI ) throws NumberFormatException {
        this.value = new BigDecimal( value );
        this.uomURI = uomURI;
    }

    public BigDecimal getValue() {
        return value;
    }

    public double getValueAsDouble() {
        return value.doubleValue();
    }

    public String getUomUri() {
        return uomURI;
    }

    @Override
    public boolean equals( Object obj ) {
        if ( !( obj instanceof Measure ) ) {
            return false;
        }
        Measure m = (Measure) obj;
        // NOTE: don't use #equals() for BigDecimal, because new BigDecimal("155.00") is not equal to
        // new BigDecimal("155")
        if ( value.compareTo( m.value ) == 0 ) {
            if ( uomURI != null ) {
                return uomURI.equals( m.uomURI );
            }
            return m.uomURI == null;
        }
        return false;
    }

    @Override
    public String toString() {
        return value + ( uomURI == null ? "" : " " + uomURI );
    }
}
