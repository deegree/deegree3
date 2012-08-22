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
package org.deegree.datatypes.parameter;

/**
 * Thrown when a parameter with an invalid value was encountered.
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class InvalidParameterValueException extends IllegalArgumentException {

    private static final long serialVersionUID = 470707628607887728L;

    private final String parameterName;

    private final Object value;

    /**
     * Creates an exception with the specified invalid value.
     *
     * @param message
     *            The detail message. The detail message is saved for later retrieval by the
     *            {@link #getMessage()} method.
     * @param parameterName
     *            The parameter name.
     * @param value
     *            The invalid parameter value.
     */
    public InvalidParameterValueException( String message, String parameterName, Object value ) {
        super( message );
        this.parameterName = parameterName;
        this.value = value;
    }

    /**
     * Creates an exception with the specified invalid value as a floating point.
     *
     * @param message
     *            The detail message. The detail message is saved for later retrieval by the
     *            {@link #getMessage()} method.
     * @param parameterName
     *            The parameter name.
     * @param value
     *            The invalid parameter value.
     */
    public InvalidParameterValueException( String message, String parameterName, double value ) {
        this( message, parameterName, new Double( value ) );
    }

    /**
     * Creates an exception with the specified invalid value as an integer.
     *
     * @param message
     *            The detail message. The detail message is saved for later retrieval by the
     *            {@link #getMessage()} method.
     * @param parameterName
     *            The parameter name.
     * @param value
     *            The invalid parameter value.
     */
    public InvalidParameterValueException( String message, String parameterName, int value ) {
        this( message, parameterName, new Integer( value ) );
    }

    /**
     * Returns the parameter name.
     *
     * @return the parameter name.
     */
    public String getParameterName() {
        return parameterName;
    }

    /**
     * Returns the invalid parameter value.
     *
     * @return the invalid parameter value.
     */
    public Object getValue() {
        return value;
    }
}
