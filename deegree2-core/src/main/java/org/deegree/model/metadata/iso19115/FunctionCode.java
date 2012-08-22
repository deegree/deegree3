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

package org.deegree.model.metadata.iso19115;

import java.io.Serializable;

/**
 * FunctionCode.java
 *
 * Created on 16. September 2002, 10:02
 * <p>
 * ----------------------------------------------------------------------
 * </p>
 *
 * @author <a href="mailto:schaefer@lat-lon.de">Axel Schaefer</a>
 * @version $Revision$ $Date$
 */

public class FunctionCode implements Serializable {

    String value = null;

    /**
     *
     * @param value
     */
    public FunctionCode( String value ) {
        setValue( value );
    }

    /**
     * returns the attribute "value". use="required" Possible values are:
     * <ul>
     * <li>access
     * <li>additionalInformation
     * <li>download
     * <li>order
     * <li>search
     *
     * @return the value-attribute
     *
     */
    public String getValue() {
        return value;
    }

    /**
     * @see #getValue()
     * @param value
     */
    public void setValue( String value ) {
        this.value = value;
    }

    /**
     * to String method
     *
     * @return string representation
     */
    public String toString() {
        String ret = null;
        ret = "value = " + value;
        return ret;
    }

}
